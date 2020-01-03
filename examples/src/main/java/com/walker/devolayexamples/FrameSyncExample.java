package com.walker.devolayexamples;

import com.walker.devolay.*;

/**
 * Adapted from NDIlib_Recv_FrameSync.cpp
 */
public class FrameSyncExample {
    public static void main(String[] args) throws InterruptedException {
        Devolay.loadLibraries();

        DevolayReceiver receiver = new DevolayReceiver();

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

        // Create initial frames to be used for capturing
        DevolayVideoFrame videoFrame = new DevolayVideoFrame();
        DevolayAudioFrame audioFrame = new DevolayAudioFrame();

        // Attach the frame-synchronizer to ensure that audio is dynamically resampled based on request frequency.
        DevolayFrameSync frameSync = new DevolayFrameSync(receiver);

        // Run at 30Hz
        final float clockSpeed = 30;

        // Run for one minute
        long startTime = System.currentTimeMillis();
        while(System.currentTimeMillis() - startTime < 1000 * 60) {

            // Capture a video frame
            if(frameSync.captureVideo(videoFrame)) { // Only returns true if a video frame was returned
                // Handle video data here
                System.out.println("Received video data: " + videoFrame.getFourCCType().name());
            }

            // Capture audio samples
            frameSync.captureAudio(audioFrame, 48000, 2, (int) (48000/clockSpeed));

            // Handle audio data here
            System.out.println("Received audio data: " + audioFrame.getSamples());

            // Here is the clock. The frame-sync is smart enough to adapt the video and audio to match 30Hz with this.
            Thread.sleep((long) (1000/clockSpeed));
        }

        // Destroy the references to each. Not necessary, but can free up the memory faster than Java's GC by itself
        videoFrame.close();
        audioFrame.close();
        // Make sure to close the framesync before the receiver
        frameSync.close();
        receiver.close();
    }
}
