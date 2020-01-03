package com.walker.devolayexamples.recording;

import com.walker.devolay.DevolayAudioFrame;
import com.walker.devolay.DevolayVideoFrame;
import org.bytedeco.ffmpeg.avcodec.AVCodec;
import org.bytedeco.ffmpeg.avcodec.AVCodecContext;
import org.bytedeco.ffmpeg.avcodec.AVPacket;
import org.bytedeco.ffmpeg.avformat.AVFormatContext;
import org.bytedeco.ffmpeg.avformat.AVIOContext;
import org.bytedeco.ffmpeg.avformat.AVOutputFormat;
import org.bytedeco.ffmpeg.avformat.AVStream;
import org.bytedeco.ffmpeg.avutil.*;
import org.bytedeco.ffmpeg.swresample.SwrContext;
import org.bytedeco.ffmpeg.swscale.SwsContext;
import org.bytedeco.javacpp.*;

import java.io.IOException;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static com.walker.devolayexamples.recording.JavaCPPUtilities.*;

import static org.bytedeco.ffmpeg.avcodec.AVCodecContext.*;
import static org.bytedeco.ffmpeg.global.avcodec.*;
import static org.bytedeco.ffmpeg.global.avformat.*;
import static org.bytedeco.ffmpeg.global.avutil.*;
import static org.bytedeco.ffmpeg.global.swresample.*;
import static org.bytedeco.ffmpeg.global.swscale.*;

public class JavaCPPDynamicRecorder {

    private static final List<AVCodec> preferredVideoCodecs = List.of(
            avcodec_find_encoder(AV_CODEC_ID_H265),
            avcodec_find_encoder(AV_CODEC_ID_H264),
            avcodec_find_encoder(AV_CODEC_ID_VP9));
    private static final List<AVCodec> preferredAudioCodecs = List.of(
            avcodec_find_encoder(AV_CODEC_ID_FLAC),
            avcodec_find_encoder(AV_CODEC_ID_WAVPACK),
            avcodec_find_encoder(AV_CODEC_ID_AAC));

    private AVOutputFormat format;
    private AVFormatContext formatContext;
    private AVCodec videoCodec, audioCodec;
    private AVPacket videoPacket, audioPacket;
    private AVStream videoStream, audioStream;
    private AVFrame postResampleVideoFrame;
    private AVFrame postResampleAudioFrame;
    private AVCodecContext videoContext, audioContext;
    private int frameCount, sampleCount;

    private boolean contextOpened = false;

    private static class VideoInfo {
        private int width, height;
        private int fpsN, fpsD;
        private int pixelFormat;

        private IntPointer linesize;

        private VideoInfo(DevolayVideoFrame videoFrame) {
            width = videoFrame.getXResolution();
            height = videoFrame.getYResolution();
            fpsN = videoFrame.getFrameRateN();
            fpsD = videoFrame.getFrameRateD();
            pixelFormat = fourCCToPixFmt(videoFrame.getFourCCType());
        }

        private boolean matches(DevolayVideoFrame videoFrame) {
            return  videoFrame.getXResolution() == width &&
                    videoFrame.getYResolution() == height &&
                    // A limitation of FFMPEG is that there is no built-in fps resampling, so changing FPS can be fixed, but needs custom logic
                    // videoFrame.getFrameRateN() == fpsN &&
                    // videoFrame.getFrameRateD() == fpsD &&
                    fourCCToPixFmt(videoFrame.getFourCCType()) == pixelFormat;
        }
    }

    private static class AudioInfo {
        private int sampleFormat;
        private int sampleRate;
        private long channelLayout;
        private int channels;

        private AudioInfo(DevolayAudioFrame audioFrame) {
            sampleFormat = getFrameSampleFormat(audioFrame);
            sampleRate = audioFrame.getSampleRate();
            channelLayout = getFrameChannelLayout(audioFrame);
            channels = audioFrame.getChannels();
        }

        private boolean matches(DevolayAudioFrame audioFrame) {
            return getFrameSampleFormat(audioFrame) == sampleFormat &&
                    audioFrame.getSampleRate() == sampleRate &&
                    getFrameChannelLayout(audioFrame) == channelLayout &&
                    audioFrame.getChannels() == channels;
        }
    }

    private VideoInfo videoRecordingInfo, videoTargetInfo;
    private AudioInfo audioRecordingInfo, audioTargetInfo;

    private SwsContext swsContext;
    private SwrContext swrContext;

    // At runtime, all compatible codecs will be added.
    private Set<AVCodec> availableVideoCodecs = new TreeSet<>(Comparator.comparingInt(AVCodec::id));
    private Set<AVCodec> availableAudioCodecs = new TreeSet<>(Comparator.comparingInt(AVCodec::id));

    public void setup(String muxerName, Path outputFile) throws IOException {

        av_log_set_level(AV_LOG_TRACE);

        format = av_guess_format(muxerName, outputFile.getFileName().toString(), null);
        if(format == null) {
            throw new IllegalStateException("Unable to find a suitable output muxer for file: " + outputFile.getFileName().toString());
        }
        formatContext = avformat_alloc_context();
        formatContext.oformat(format);

        Pointer i = new Pointer((Pointer)null);
        AVCodec testCodec;
        while((testCodec = av_codec_iterate(i)) != null) {
            if(av_codec_is_encoder(testCodec) > 0 && avformat_query_codec(format, testCodec.id(), FF_COMPLIANCE_STRICT) > 0) {
                switch(testCodec.type()) {
                    case AVMEDIA_TYPE_AUDIO:
                        availableAudioCodecs.add(testCodec);
                        break;
                    case AVMEDIA_TYPE_VIDEO:
                        availableVideoCodecs.add(testCodec);
                        break;
                }
            }
        }

        // Setup video encoder
        if(canSupportVideo()) {
            videoPacket = av_packet_alloc();
            if(videoPacket == null) {
                throw new IllegalStateException("Unable to initialize video context");
            }
            videoStream = avformat_new_stream(formatContext, videoCodec);
            if(videoStream == null) {
                throw new IllegalStateException("Unable to initialize video stream.");
            }
        }

        // Setup audio encoder
        if(canSupportAudio()) {
            audioPacket = av_packet_alloc();
            if(audioPacket == null) {
                throw new IllegalStateException("Unable to initialize audio context");
            }
            audioStream = avformat_new_stream(formatContext, audioCodec);
            if(audioStream == null) {
                throw new IllegalStateException("Unable to initialize audio stream.");
            }
        }

        // Open the output context for the file.
        Files.deleteIfExists(outputFile);
        if((format.flags() & AVFMT_NOFILE) != AVFMT_NOFILE){
            AVIOContext avioContext = new AVIOContext();
            int ret = avio_open(avioContext, outputFile.toAbsolutePath().toString().replace('\\', '/'), AVIO_FLAG_WRITE);
            if(ret < 0){
                throw new IllegalStateException("Failed to open export file for writing: " + translateError(ret));
            }
            formatContext.pb(avioContext);
        }

        int ret;
        if((ret = avformat_write_header(formatContext, (AVDictionary)null)) < 0) {
            throw new IllegalStateException("Failed to write content header: " + translateError(ret));
        }

        contextOpened = true;
    }

    public void processVideoFrame(DevolayVideoFrame videoFrame) {
        // Setup initial video encoder if it hasn't already been
        if(videoContext == null) {
            setupVideoEncoder(videoFrame);
        }

        if(tryToOpenContext()) {
            int ret;
            /*if ((ret = av_frame_make_writable(postResampleVideoFrame)) < 0) {
                throw new IllegalStateException("Failed to make video frame writable: " + translateError(ret));
            }*/

            // See if frame needs to be resampled
            if (videoRecordingInfo.matches(videoFrame)) {
                // Can directly put frame data into struct.
                postResampleVideoFrame.data(0, new BytePointer(videoFrame.getData()));
            } else {
                if (!videoTargetInfo.matches(videoFrame)) {
                    // A swsContext needs to be created for converting this format to the recording format.
                    videoTargetInfo = new VideoInfo(videoFrame);
                    createSwsContext();
                }

                // Resample the frame from the devolay buffer
                sws_scale(swsContext,
                        splitVideoPlanes(videoFrame.getData(), videoTargetInfo.linesize, videoTargetInfo.height, videoTargetInfo.pixelFormat), videoTargetInfo.linesize, 0, videoTargetInfo.height,
                        postResampleVideoFrame.data(), postResampleVideoFrame.linesize());
            }

            postResampleVideoFrame.pts(frameCount++);

            // Send the frame to be encoded
            if ((ret = avcodec_send_frame(videoContext, postResampleVideoFrame)) < 0) {
                throw new IllegalStateException("Failed to submit a video frame to encode: " + translateError(ret));
            }

            // Read packets from the encoder and mux the output.
            while (ret >= 0) {
                ret = avcodec_receive_packet(videoContext, videoPacket);
                if (ret < 0 && ret != AVERROR_EAGAIN() && ret != AVERROR_EOF()) {
                    throw new IllegalStateException("Encountered error while encoding frame: " + translateError(ret));
                } else if (ret >= 0) {
                    videoPacket.pts(frameCount - 1);
                    videoPacket.dts(frameCount - 1);
                    videoPacket.stream_index(videoStream.index());
                    ret = av_write_frame(formatContext, videoPacket);
                    if (ret < 0) {
                        throw new IllegalStateException("Failed to write a video frame: " + translateError(ret));
                    }
                }
            }
        }
    }

    private void setupVideoEncoder(DevolayVideoFrame baseFrame) {
        // Set the target and recording parameters, so if these switch on future frames, a swsContext can be allocated.
        videoTargetInfo = new VideoInfo(baseFrame);
        videoRecordingInfo = new VideoInfo(baseFrame);

        // Check comparability of possible codecs with the given frame
        HashSet<AVCodec> compatibleCodecs = new HashSet<>();
        boolean useDefaultFramerate = false, useDefaultPixelFormat = false;

        List<AVCodec> pixelFormatCompatibleCodecs = availableVideoCodecs.stream()
                .filter(codec -> isCodecPixelFormatCompatible(codec, fourCCToPixFmt(baseFrame.getFourCCType())))
                .collect(Collectors.toList());
        List<AVCodec> framerateCompatibleCodecs = availableVideoCodecs.stream()
                .filter(codec -> isCodecFramerateCompatible(codec, baseFrame.getFrameRateN(), baseFrame.getFrameRateD()))
                .collect(Collectors.toList());

        for(AVCodec c : pixelFormatCompatibleCodecs) {
            if(framerateCompatibleCodecs.contains(c)) {
                compatibleCodecs.add(c);
            }
        }
        if(compatibleCodecs.isEmpty()) {
            // No codecs support both pixel format and framerate, give up on pixel format.
            useDefaultPixelFormat = true;
            compatibleCodecs.addAll(framerateCompatibleCodecs);
            if(compatibleCodecs.isEmpty()) {
                // No codecs support framerate, give up on framerate
                useDefaultFramerate = true;
                compatibleCodecs.addAll(availableVideoCodecs);
            }
        }


        if(compatibleCodecs.size() == 0) {
            throw new IllegalStateException("No codecs in muxer support framerate " + baseFrame.getFrameRateN() + "/" + baseFrame.getFrameRateD() + " and pixel format " + baseFrame.getFourCCType().name());
        }
        for(AVCodec preferredCodec : preferredVideoCodecs) {
            if(compatibleCodecs.contains(preferredCodec)) {
                videoCodec = preferredCodec;
                break;
            }
        }
        if(videoCodec == null) {
            videoCodec = compatibleCodecs.stream().findFirst().orElseThrow(() -> new IllegalStateException("No codecs found."));
        }

        // If we need to fall back on defaults, pick pixel formats and framerates to use
        if(useDefaultPixelFormat) {
            videoRecordingInfo.pixelFormat = pickBestPixelFormat(videoCodec, fourCCToPixFmt(baseFrame.getFourCCType()), fourCCHasAlpha(baseFrame.getFourCCType()));
        }
        if(useDefaultFramerate) {
            AVRational pickedFps = pickBestFramerate(videoCodec, baseFrame.getFrameRateN(), baseFrame.getFrameRateD());
            videoRecordingInfo.fpsN = pickedFps.num();
            videoRecordingInfo.fpsD = pickedFps.den();
        }

        formatContext.video_codec(videoCodec);

        // Create the encoder context with the decided codec
        videoContext = avcodec_alloc_context3(videoCodec);
        if(videoContext == null) {
            throw new IllegalStateException("Unable to initialize video context");
        }
        if((format.flags() & AVFMT_GLOBALHEADER) == AVFMT_GLOBALHEADER){
            videoContext.flags(videoContext.flags() | AV_CODEC_FLAG_GLOBAL_HEADER);
        }

        videoContext.codec(videoCodec);
        videoContext.width(videoRecordingInfo.width);
        videoContext.height(videoRecordingInfo.height);
        videoContext.time_base(av_make_q(videoRecordingInfo.fpsN, videoRecordingInfo.fpsD));
        videoContext.pix_fmt(videoRecordingInfo.pixelFormat);

        int ret;
        if((ret = avcodec_open2(videoContext, videoCodec, (AVDictionary) null)) < 0) {
            throw new IllegalStateException("Failed to open video codec: " + translateError(ret));
        }

        // Create frames to copy video to
        postResampleVideoFrame = allocVideoFrame(videoContext.pix_fmt(), videoContext.width(), videoContext.height());

        // Create a video stream in the muxer
        videoStream.time_base(videoContext.time_base());
        if((ret = avcodec_parameters_from_context(videoStream.codecpar(), videoContext)) < 0) {
            throw new IllegalStateException("Failed to copy video stream parameters: " + translateError(ret));
        }

        // Setup resampler if immediately needed
        if(!videoRecordingInfo.matches(baseFrame)) {
            createSwsContext();
        }
    }

    private AVFrame allocVideoFrame(int pixFmt, int width, int height) {
        AVFrame frame = av_frame_alloc();
        if(frame == null){
            throw new IllegalStateException("Failed to allocate a video frame.");
        }
        frame.format(pixFmt);
        frame.width(width);
        frame.height(height);
        return frame;
    }

    private void createSwsContext() {
        swsContext = sws_getContext(videoTargetInfo.width,
                videoTargetInfo.height,
                videoTargetInfo.pixelFormat,
                videoRecordingInfo.width,
                videoRecordingInfo.height,
                videoRecordingInfo.pixelFormat,
                SWS_BICUBIC, null, null, (DoublePointer) null);
        if(swsContext == null){
            throw new IllegalStateException("Unable to initialize video resample context.");
        }

        // When resampling is enabled, the ffmpeg frame needs it's own buffer for data to be resampled to
        int ret;
        if((ret = av_frame_get_buffer(postResampleVideoFrame, 0)) < 0) {
            throw new IllegalStateException("Failed to allocate a frame for the resampled video data: " + translateError(ret));
        }

        // Compute linesizes for target frame (needed for resampling)
        videoTargetInfo.linesize = new IntPointer(av_pix_fmt_count_planes(videoTargetInfo.pixelFormat));
        av_image_fill_linesizes(videoTargetInfo.linesize, videoTargetInfo.pixelFormat, videoTargetInfo.width);
    }

    public void processAudioFrame(DevolayAudioFrame audioFrame) {
        int ret;

        if(audioContext == null) {
            setupAudioEncoder(audioFrame);
            tryToOpenContext();

            // A SWR context needs to be opened anyways, to buffer samples to send to the encoder.
            createSwrContext();
        }

        if(tryToOpenContext()) {

            if (!audioTargetInfo.matches(audioFrame)) {
                // A different swrContext needs to be created for converting this format to the recording format.
                audioTargetInfo = new AudioInfo(audioFrame);
                createSwrContext();
            }

            if ((ret = av_frame_make_writable(postResampleAudioFrame)) < 0) {
                throw new IllegalStateException("Failed to make audio frame writable: " + translateError(ret));
            }

            // Resample the data from the devolay frame to the ffmpeg frame. Repeats until enough frames have been filled to hold the entire frame.
            int samples = swr_convert(swrContext,
                    postResampleAudioFrame.data(), postResampleAudioFrame.nb_samples(),
                    splitAudioPlanes(audioFrame.getData(), audioFrame.getChannels(), audioFrame.getChannelStride()), audioFrame.getSamples());

            for(int i = 0; (i + postResampleAudioFrame.nb_samples()) < audioFrame.getSamples(); i += postResampleAudioFrame.nb_samples()) {
                postResampleAudioFrame.pts(sampleCount += postResampleAudioFrame.nb_samples());

                // Submit the frame to encode
                if ((ret = avcodec_send_frame(audioContext, postResampleAudioFrame)) < 0) {
                    throw new IllegalStateException("Failed to submit an audio frame to encode: " + translateError(ret));
                }
                // Read packets from the encoder and mux the output
                while (ret >= 0) {
                    ret = avcodec_receive_packet(audioContext, audioPacket);
                    if (ret < 0 && ret != AVERROR_EAGAIN() && ret != AVERROR_EOF()) {
                        throw new IllegalStateException("Encountered error while encoding audio frame: " + translateError(ret));
                    } else if (ret >= 0) {
                        audioPacket.stream_index(audioStream.index());
                        ret = av_write_frame(formatContext, audioPacket);
                        if (ret < 0) {
                            throw new IllegalStateException("Failed to write audio frame: " + translateError(ret));
                        }
                    }
                }

                samples = swr_convert(swrContext,
                        postResampleAudioFrame.data(), postResampleAudioFrame.nb_samples(), null, 0);

                if(samples <= 0) {
                    break;
                }
            }
        }
    }

    private void setupAudioEncoder(DevolayAudioFrame baseFrame) {

        // Set the target and recording parameters
        audioRecordingInfo = new AudioInfo(baseFrame);
        audioTargetInfo = new AudioInfo(baseFrame);

        // Find any codec compatibility with the native format
        HashSet<AVCodec> compatibleCodecs = new HashSet<>();
        boolean useDefaultSampleRate = false, useDefaultSampleFormat = false, useDefaultChannelLayout = false;

        List<AVCodec> sampleRateCompatible = availableAudioCodecs.stream()
                .filter(codec -> isCodecSampleRateCompatible(codec, baseFrame.getSampleRate()))
                .collect(Collectors.toList());
        List<AVCodec> sampleFormatCompatible = availableAudioCodecs.stream()
                .filter(codec -> isCodecSampleFormatCompatible(codec, getFrameSampleFormat(baseFrame)))
                .collect(Collectors.toList());
        List<AVCodec> channelLayoutCompatible = availableAudioCodecs.stream()
                .filter(codec -> isCodecChannelLayoutCompatible(codec, getFrameChannelLayout(baseFrame)))
                .collect(Collectors.toList());

        for(AVCodec c : sampleRateCompatible) {
            if(sampleFormatCompatible.contains(c) && channelLayoutCompatible.contains(c)) {
                compatibleCodecs.add(c);
            }
        }
        if(compatibleCodecs.isEmpty()) {
            // No codecs are sample rate compatible. Mark it down to reformat sample rate, and look for other compatibility
            useDefaultSampleRate = true;
            for(AVCodec c : sampleFormatCompatible) {
                if(channelLayoutCompatible.contains(c)) {
                    compatibleCodecs.add(c);
                }
            }
            if(compatibleCodecs.isEmpty()) {
                // No codecs are sample format compatible. Mark it down to reformat sample format
                useDefaultSampleFormat = true;
                compatibleCodecs.addAll(channelLayoutCompatible);
                if(compatibleCodecs.isEmpty()) {
                    // No codecs are channel layout compatible. Mark it down to reformat channel layout
                    useDefaultChannelLayout = true;
                    // Give up on compatibility
                    compatibleCodecs.addAll(availableAudioCodecs);
                }
            }
        }

        if(compatibleCodecs.size() == 0) {
            throw new IllegalStateException("Muxer has no installed audio codecs");
        }
        for(AVCodec preferredCodec : preferredAudioCodecs) {
            if(compatibleCodecs.contains(preferredCodec)) {
                audioCodec = preferredCodec;
                break;
            }
        }
        if(audioCodec == null) {
            audioCodec = compatibleCodecs.stream().findFirst().orElseThrow(() -> new IllegalStateException("No codecs found."));
        }

        // If we need to fall back on defaults, pick sample rates and formats to use
        if(useDefaultSampleRate) {
            audioRecordingInfo.sampleRate = pickBestSampleRate(audioCodec, baseFrame.getSampleRate());
        }
        if(useDefaultSampleFormat) {
            audioRecordingInfo.sampleFormat = pickBestSampleFormat(audioCodec, getFrameSampleFormat(baseFrame));
        }
        if(useDefaultChannelLayout) {
            audioRecordingInfo.channelLayout = pickBestChannelLayout(audioCodec, getFrameChannelLayout(baseFrame));
            audioRecordingInfo.channels = av_get_channel_layout_nb_channels(audioRecordingInfo.channelLayout);
        }

        formatContext.audio_codec(audioCodec);

        // Create the encoder context with the desired codec
        audioContext = avcodec_alloc_context3(audioCodec);
        if(audioContext == null) {
            throw new IllegalStateException("Unable to initialize audio context");
        }
        if((format.flags() & AVFMT_GLOBALHEADER) == AVFMT_GLOBALHEADER){
            audioContext.flags(audioContext.flags() | AV_CODEC_FLAG_GLOBAL_HEADER);
        }
        if((audioCodec.capabilities() & AV_CODEC_CAP_VARIABLE_FRAME_SIZE) == AV_CODEC_CAP_VARIABLE_FRAME_SIZE) {
            audioContext.flags(audioContext.flags() | AV_CODEC_CAP_VARIABLE_FRAME_SIZE);
        }

        audioContext.codec(audioCodec);
        audioContext.sample_fmt(audioRecordingInfo.sampleFormat);
        audioContext.sample_rate(audioRecordingInfo.sampleRate);
        audioContext.channel_layout(audioRecordingInfo.channelLayout);
        audioContext.channels(audioRecordingInfo.channels);
        audioContext.time_base(av_make_q(1, audioRecordingInfo.sampleRate));

        int ret;
        if((ret = avcodec_open2(audioContext, audioCodec, (AVDictionary) null)) < 0) {
            throw new IllegalStateException("Failed to open audio codec: " + translateError(ret));
        }

        // Allocate frames to store the audio data
        postResampleAudioFrame = allocAudioFrame(audioRecordingInfo.sampleFormat, audioRecordingInfo.channelLayout, audioRecordingInfo.sampleRate);
        postResampleAudioFrame.nb_samples(audioContext.frame_size()); // Frame size must always be the amount given by the encoder.

        // Create a audio stream in the muxer
        audioStream.time_base(audioContext.time_base());
        if((ret = avcodec_parameters_from_context(audioStream.codecpar(), audioContext)) < 0) {
            throw new IllegalStateException("Failed to copy video stream parameters: " + translateError(ret));
        }
    }

    private AVFrame allocAudioFrame(int sampleFmt, long channelLayout, int sampleRate) {
        AVFrame frame = av_frame_alloc();
        if(frame == null){
            throw new IllegalStateException("Failed to allocate an audio frame.");
        }
        frame.format(sampleFmt);
        frame.channel_layout(channelLayout);
        frame.sample_rate(sampleRate);
        return frame;
    }


    private void createSwrContext() {
        swrContext = swr_alloc();
        if(swrContext == null) {
            throw new IllegalStateException("Unable to allocate audio resample context.");
        }

        av_opt_set_int(swrContext, "in_sample_fmt", audioTargetInfo.sampleFormat, 0);
        av_opt_set_int(swrContext, "in_sample_rate", audioTargetInfo.sampleRate, 0);
        av_opt_set_int(swrContext, "in_channel_layout", audioTargetInfo.channelLayout, 0);
        av_opt_set_int(swrContext, "out_sample_fmt", audioRecordingInfo.sampleFormat, 0);
        av_opt_set_int(swrContext, "out_sample_rate", audioRecordingInfo.sampleRate, 0);
        av_opt_set_int(swrContext, "out_channel_layout", audioRecordingInfo.channelLayout, 0);

        int ret = swr_init(swrContext);
        if (ret < 0) {
            throw new IllegalStateException("Unable to open audio resample context: " + translateError(ret));
        }

        // Create buffer to resample into
        if((ret = av_frame_get_buffer(postResampleAudioFrame, 0)) < 0) {
            throw new IllegalStateException("Failed to allocate a frame for resampled audio data: " + translateError(ret));
        }
    }

    public boolean tryToOpenContext() {
        if(contextOpened) {
            return true;
        }

        if(canSupportAudio() && audioContext == null) {
            return false;
        }
        if(canSupportVideo() && videoContext == null) {
            return false;
        }

        int ret;
        if((ret = avformat_write_header(formatContext, (AVDictionary)null)) < 0) {
            throw new IllegalStateException("Failed to write content header: " + translateError(ret));
        }

        contextOpened = true;

        return true;
    }

    public boolean canSupportAudio() {
        return !availableAudioCodecs.isEmpty();
    }

    public boolean canSupportVideo() {
        return !availableVideoCodecs.isEmpty();
    }

    public void close() {
        if(videoContext != null) {
            // Flush video encoder
            int ret = avcodec_send_frame(videoContext, null);
            if (ret < 0) {
                System.err.println("Failed to send flush packet to video encoder, skipping: " + translateError(ret));
            }
            while (ret >= 0) {
                videoPacket.pts(frameCount);
                ret = avcodec_receive_packet(videoContext, videoPacket);
                if (ret < 0 && ret != AVERROR_EAGAIN() && ret != AVERROR_EOF()) {
                    System.err.println("Failed to encode video flush packet, skipping: " + translateError(ret));
                } else if (ret >= 0) {
                    videoPacket.stream_index(videoStream.index());
                    ret = av_interleaved_write_frame(formatContext, videoPacket);
                    if (ret < 0) {
                        System.err.println("Failed to write video flush packet, skipping: " + translateError(ret));
                    }
                }
            }
        }
        if(audioContext != null) {
            // Flush audio encoder
            int ret = avcodec_send_frame(audioContext, null);
            if (ret < 0) {
                System.err.println("Failed to send flush packet to audio encoder, skipping: " + translateError(ret));
            }
            while (ret >= 0) {
                audioPacket.pts(sampleCount);
                ret = avcodec_receive_packet(audioContext, audioPacket);
                if (ret < 0 && ret != AVERROR_EAGAIN() && ret != AVERROR_EOF()) {
                    System.err.println("Failed to encode audio flush packet, skipping: " + translateError(ret));
                } else if (ret >= 0) {
                    audioPacket.stream_index(audioStream.index());
                    ret = av_interleaved_write_frame(formatContext, audioPacket);
                    if (ret < 0) {
                        System.err.println("Failed to write audio flush packet, skipping: " + translateError(ret));
                    }
                }
            }
        }

        // Write muxer trailer
        int ret;
        if((ret = av_write_trailer(formatContext)) < 0) {
            throw new IllegalStateException("Failed to write trailer: " + translateError(ret));
        }
        // Close muxer
        if((formatContext.oformat().flags() & AVFMT_NOFILE) != AVFMT_NOFILE){
            avio_close(formatContext.pb());

            System.out.println("Finished! Wrote " + formatContext.pb().pos() + " bytes");
        } else {
            System.out.println("Finished! Wrote 0 bytes.");
        }

        // Free encoder and muxer
        if(videoContext != null) avcodec_free_context(videoContext);
        if(audioContext != null) avcodec_free_context(audioContext);
        if(videoPacket != null) av_packet_free(videoPacket);
        if(audioPacket != null) av_packet_free(audioPacket);
        if(postResampleVideoFrame != null) av_frame_free(postResampleVideoFrame);
        if(postResampleVideoFrame != null) av_frame_free(postResampleAudioFrame);
        if(swsContext != null) sws_freeContext(swsContext);
        if(swrContext != null) swr_free(swrContext);
    }
}
