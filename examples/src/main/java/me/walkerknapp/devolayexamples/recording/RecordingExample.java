package me.walkerknapp.devolayexamples.recording;

import me.walkerknapp.devolay.*;
import me.walkerknapp.devolay.*;

import java.io.IOException;
import java.nio.file.Paths;

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
