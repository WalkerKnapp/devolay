package com.walker.devolayexamples.recording;

import com.walker.devolay.DevolayAudioFrame;
import com.walker.devolay.DevolayFrameFourCCType;
import org.bytedeco.ffmpeg.avcodec.AVCodec;
import org.bytedeco.ffmpeg.avutil.AVRational;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.LongPointer;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.PointerPointer;

import java.awt.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.bytedeco.ffmpeg.global.avutil.*;

public class JavaCPPUtilities {
    public static boolean isCodecFramerateCompatible(AVCodec codec, int frameRateN, int frameRateD) {
        AVRational fps = codec.supported_framerates();

        // If a nullptr is returned, the codec is compatible with all framerates.
        if(fps == null) {
            return true;
        }

        int i = 0;
        while(fps != null && fps.den() != 0) {

            if(fps.num() == frameRateN && fps.den() == frameRateD) {
                return true;
            }
            if(((double)fps.num()) / ((double)fps.den()) == ((double)frameRateN) / ((double)frameRateD)) {
                return true;
            }

            fps = fps.position(++i);
        }

        return false;
    }

    public static AVRational pickBestFramerate(AVCodec codec, int desiredFrameRateN, int desiredFrameRateD) {
        AVRational fps = codec.supported_framerates();

        // If a nullptr is returned, the codec is compatible with all framerates, return desired
        if(fps == null) {
            return av_make_q(desiredFrameRateN, desiredFrameRateD);
        }

        return fps.position(av_find_nearest_q_idx(av_make_q(desiredFrameRateN, desiredFrameRateD), fps));
    }

    public static boolean isCodecPixelFormatCompatible(AVCodec codec, int pixelFmt) {
        IntPointer pPixFmt = codec.pix_fmts();

        if(pPixFmt == null) {
            return true;
        }

        int i = 0;
        int testFmt;
        while((testFmt = pPixFmt.get(i++)) != -1) {
            if(pixelFmt == testFmt) {
                return true;
            }
        }

        return false;
    }

    public static int pickBestPixelFormat(AVCodec codec, int desiredPixelFormat, boolean hasAlpha) {
        IntPointer pPixFmt = codec.pix_fmts();

        // If a nullptr is returned, all pixel formats are supported
        if(pPixFmt == null) {
            return desiredPixelFormat;
        }

        IntPointer loss = new IntPointer(1);
        int bestFmt = -1;

        int i = 0;
        int testFmt;
        while((testFmt = pPixFmt.get(i++)) != -1) {
            bestFmt = av_find_best_pix_fmt_of_2(bestFmt, testFmt, desiredPixelFormat, hasAlpha ? 1 : 0, loss);
        }

        return bestFmt;
    }

    public static boolean isCodecSampleRateCompatible(AVCodec codec, int sampleRate) {
        IntPointer pSampleRate = codec.supported_samplerates();

        if(pSampleRate == null) {
            return true;
        }

        int i = 0;
        int testRate;
        while((testRate = pSampleRate.get(i++)) != 0) {
            if(sampleRate == testRate) {
                return true;
            }
        }

        return false;
    }

    public static int pickBestSampleRate(AVCodec codec, int desiredSampleRate) {
        IntPointer pSampleRate = codec.supported_samplerates();

        // If a nullptr is returned, all sample rates are supported.
        if(pSampleRate == null) {
            return desiredSampleRate;
        }

        int bestSampleRate = 0;
        int bestDelta = Integer.MAX_VALUE;

        int i = 0;
        int testRate;
        while((testRate = pSampleRate.get(i++)) != 0) {
            if(Math.abs(desiredSampleRate - testRate) < bestDelta) {
                bestSampleRate = testRate;
                bestDelta = Math.abs(desiredSampleRate - testRate);
            }
        }

        return bestSampleRate;
    }

    public static boolean isCodecSampleFormatCompatible(AVCodec codec, int sampleFmt) {
        IntPointer pSampleFmt = codec.sample_fmts();

        if(pSampleFmt == null) {
            return true;
        }

        int i = 0;
        int testFmt;
        while((testFmt = pSampleFmt.get(i++)) != 0) {
            if(sampleFmt == testFmt) {
                return true;
            }
        }

        return false;
    }

    public static int pickBestSampleFormat(AVCodec codec, int desiredSampleFmt) {
        IntPointer pSampleFmt = codec.sample_fmts();

        // If nullptr is returned, and sample format is supported
        if(pSampleFmt == null) {
            return desiredSampleFmt;
        }

        int bestFormat = -1;
        int bestBPS = 0;

        int i = 0;
        int testRate;
        while((testRate = pSampleFmt.get(i++)) != 0) {
            if(av_get_bytes_per_sample(testRate) > bestBPS) {
                bestFormat = testRate;
                bestBPS = av_get_bytes_per_sample(testRate);
            }
        }

        return bestFormat;
    }

    public static boolean isCodecChannelLayoutCompatible(AVCodec codec, long channelLayout) {
        LongPointer pChannelLayout = codec.channel_layouts();

        if(pChannelLayout == null) {
            return true;
        }

        int i = 0;
        long testLayout;
        while((testLayout = pChannelLayout.get(i++)) != 0) {
            if(channelLayout == testLayout) {
                return true;
            }
        }

        return false;
    }

    public static long pickBestChannelLayout(AVCodec codec, long desiredChannelLayout) {
        LongPointer pChannelLayout = codec.channel_layouts();

        // If nullptr is returned, all channel layouts are supported
        if(pChannelLayout == null) {
            return desiredChannelLayout;
        }

        long bestLayout = 0;
        int channelDelta = Integer.MAX_VALUE;

        int i = 0;
        long testLayout;
        while((testLayout = pChannelLayout.get(i++)) != 0) {
            if(desiredChannelLayout == testLayout) {
                return desiredChannelLayout;
            }
            if(Math.abs(av_get_channel_layout_nb_channels(testLayout) - av_get_channel_layout_nb_channels(desiredChannelLayout)) < channelDelta) {
                bestLayout = testLayout;
                channelDelta = Math.abs(av_get_channel_layout_nb_channels(testLayout) - av_get_channel_layout_nb_channels(desiredChannelLayout));
            }
        }

        return bestLayout;
    }

    public static int fourCCToPixFmt(DevolayFrameFourCCType fourCC) {
        switch (fourCC) {
            case UYVY: return AV_PIX_FMT_UYVY422;
            case NV12: return AV_PIX_FMT_NV12;
            case I420: return AV_PIX_FMT_YUV420P;
            case BGRA: return AV_PIX_FMT_BGRA;
            case BGRX: return AV_PIX_FMT_BGR0;
            case RGBA: return AV_PIX_FMT_RGBA;
            case RGBX: return AV_PIX_FMT_RGB0;
            case YV12:
            case UYVA:
                return -1; // TODO: Handle incompatible color formats
        }
        return -1;
    }

    public static boolean fourCCHasAlpha(DevolayFrameFourCCType fourCC) {
        switch (fourCC) {
            case UYVY:
            case NV12:
            case I420:
            case BGRX:
            case RGBX:
            case YV12:
                return false;
            case BGRA:
            case RGBA:
            case UYVA:
                return true;
        }
        return false;
    }

    public static int getFrameSampleFormat(DevolayAudioFrame audioFrame) {
        return AV_SAMPLE_FMT_FLTP;
    }

    public static long getFrameChannelLayout(DevolayAudioFrame audioFrame) {
        return av_get_default_channel_layout(audioFrame.getChannels());
    }

    public static String translateError(int errorCode) {
        byte[] errBuf = new byte[255];
        av_strerror(errorCode, errBuf, 255);
        return new String(errBuf);
    }

    public static PointerPointer splitAudioPlanes(ByteBuffer buffer, int channels, int channelStride) {
        PointerPointer pp = new PointerPointer(channels);

        for(int i = 0; i < channels; i++) {
            pp.put(i, new Pointer(buffer).position(channelStride * i));
        }

        return pp;
    }

    public static PointerPointer splitVideoPlanes(ByteBuffer buffer, IntPointer linesize, int height, int pixelFormat) {
        PointerPointer pp = new PointerPointer(av_pix_fmt_count_planes(pixelFormat));

        int pos = 0;

        for(int i = 0; i < av_pix_fmt_count_planes(pixelFormat); i++) {
            pp.put(new Pointer(buffer).position(pos));

            pos += linesize.get(i) * height;
        }

        return pp;
    }
}
