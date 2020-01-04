package com.walker.devolayexamples;

import com.walker.devolay.*;

import javax.sound.sampled.*;
import java.nio.ByteBuffer;

/**
 * An example of using the javax.sound.* api to monitor the audio of the first found source.
 */
public class MonitorExample {

    private static final int sampleRate = 48000;
    private static final int channelCount = 2;

    private static SourceDataLine soundLine;

    public static void main(String[] args) throws LineUnavailableException, InterruptedException {
        // We will reformat the planar float data to 16 bit signed data
        AudioFormat audioFormat = new AudioFormat(sampleRate, 16, channelCount, true, false);
        final DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        soundLine = (SourceDataLine) AudioSystem.getLine(info);
        soundLine.open();
        soundLine.start();

        receive();
    }

    public static void receive() throws InterruptedException {
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

        // Run at 30Hz
        final float clockSpeed = 30;

        // Create initial frames to be used for capturing
        DevolayVideoFrame videoFrame = new DevolayVideoFrame();
        DevolayAudioFrame audioFrame = new DevolayAudioFrame();

        // Setup frame to convert floating-point data to 16s data
        DevolayAudioFrameInterleaved16s interleaved16s = new DevolayAudioFrameInterleaved16s();
        interleaved16s.setReferenceLevel(20); // Recommended level for receiving in NDI docs
        interleaved16s.setData(ByteBuffer.allocateDirect((int) (sampleRate/clockSpeed) * channelCount * Short.BYTES));

        // Attach the frame-synchronizer to ensure that audio is dynamically resampled based on request frequency.
        DevolayFrameSync frameSync = new DevolayFrameSync(receiver);

        // Run for one minute
        long startTime = System.currentTimeMillis();
        while(System.currentTimeMillis() - startTime < 1000 * 60) {

            // Capture a video frame
            if(frameSync.captureVideo(videoFrame)) { // Only returns true if a video frame was returned
                // Handle video data here
                System.out.println("Received video data: " + videoFrame.getFourCCType().name());
            }

            // Capture audio samples
            frameSync.captureAudio(audioFrame, sampleRate, channelCount, (int) (sampleRate/clockSpeed));
            // Convert the given float data to interleaved 16s data
            DevolayUtilities.planarFloatToInterleaved16s(audioFrame, interleaved16s);

            System.out.println("Received audio data: " + audioFrame.getSamples());

            // Get the audio data in a byte array, needed to write to a SourceDataLine
            byte[] audioData = new byte[audioFrame.getSamples() * Short.BYTES * audioFrame.getChannels()];
            interleaved16s.getData().get(audioData);
            // Write the audio data to the javax.sound api.
            soundLine.write(audioData, 0, audioData.length);

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
