package com.walker.devolayexamples.recording;

import com.walker.devolay.*;
import org.bytedeco.ffmpeg.avcodec.AVCodec;
import org.bytedeco.ffmpeg.avcodec.AVCodecContext;
import org.bytedeco.ffmpeg.avcodec.AVPacket;
import org.bytedeco.ffmpeg.avformat.AVFormatContext;
import org.bytedeco.ffmpeg.avformat.AVOutputFormat;
import org.bytedeco.ffmpeg.avformat.AVStream;
import org.bytedeco.ffmpeg.avutil.AVDictionary;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.ffmpeg.swresample.SwrContext;
import org.bytedeco.ffmpeg.swscale.SwsContext;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import static org.bytedeco.ffmpeg.avcodec.AVCodecContext.FF_COMPLIANCE_STRICT;
import static org.bytedeco.ffmpeg.global.avcodec.*;
import static org.bytedeco.ffmpeg.global.avformat.*;
import static org.bytedeco.ffmpeg.global.avutil.*;

/**
 * Records the first source found using Javacpp-ffmpeg.
 *
 * Notes:
 *  - Because frame/audio information is dynamic and is supplied per-frame, checks are done each frame to
 *    see if reformatting needs to be done, and the encoders are opened on the first frame of each type
 */
public class RecordingExample {

    public static void main(String[] args) throws IOException, InterruptedException {

        JavaCPPDynamicRecorder recorder = new JavaCPPDynamicRecorder();

        Devolay.loadLibraries();

        DevolayReceiver receiver = new DevolayReceiver(DevolayReceiver.ColorFormat.BGRX_BGRA,
                DevolayReceiver.RECEIVE_BANDWIDTH_HIGHEST,
                false, null);

        // Create a finder
        try(DevolayFinder finder = new DevolayFinder()) {
            // Query for sources
            DevolaySource[] sources;
            while ((sources = finder.getCurrentSources()).length == 0) {
                // If none found, wait until the list changes
                System.out.println("Waiting for sources...");
                finder.waitForSources(5000);
            }

            // Connect to the first source found
            System.out.println("Connecting to source: " + sources[0].getSourceName());
            receiver.connect(sources[0]);
        }

        // Setup encoder as much as possible without image/audio information
        recorder.setup(null, Paths.get("output.mp4"));

        // Create frames to capture with if capturing video/audio, otherwise don't capture
        DevolayVideoFrame videoFrame = recorder.canSupportVideo() ? new DevolayVideoFrame() : null;
        DevolayAudioFrame audioFrame = recorder.canSupportAudio() ? new DevolayAudioFrame() : null;

        Thread.sleep(500);

        // Run until source is closed
        while(receiver.getConnectionCount() > 0) {
            // Capture with a timeout of 5000 milliseconds
            switch (receiver.receiveCapture(videoFrame, audioFrame, null, 5000)) {
                case NONE:
                    System.out.println("No data received.");
                    break;
                case VIDEO:
                    recorder.processVideoFrame(videoFrame);
                    videoFrame.close();

                    videoFrame = new DevolayVideoFrame();
                    break;
                case AUDIO:
                    recorder.processAudioFrame(audioFrame);
                    audioFrame.close();

                    audioFrame = new DevolayAudioFrame();
                    break;
            }
        }

        recorder.close();

    }
}
