package com.walker.devolayexamples;

import com.walker.devolay.Devolay;
import com.walker.devolay.DevolayAudioFrame;
import com.walker.devolay.DevolaySender;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;


/**
 * Adapted from NDIlib_Send_Audio.cpp
 */
public class SendAudioExample {
    public static void main(String[] args) {
        Devolay.loadLibraries();

        DevolaySender sender = new DevolaySender("Devolay Example Audio", null, false, true);

        final int sampleRate = 48000;
        final int channelCount = 4;
        // One tenth of a second of audio
        final int sampleCount = sampleRate;

        final ByteBuffer data = ByteBuffer
                .allocateDirect((sampleCount * channelCount * Float.SIZE) / Byte.SIZE)
                .order(ByteOrder.LITTLE_ENDIAN);

        DevolayAudioFrame audioFrame = new DevolayAudioFrame();
        audioFrame.setSampleRate(sampleRate);
        audioFrame.setChannels(channelCount);
        audioFrame.setSamples(sampleCount);
        audioFrame.setData(data);
        audioFrame.setChannelStride((sampleCount * Float.SIZE) / Byte.SIZE);

        // Generate 10 seconds of 4 different sine wave frequencies
        final float c6NoteFreq = 1046.50f;
        final float e6NoteFreq = 1318.51f;
        final float g6NoteFreq = 1567.98f;
        final float c7NoteFreq = 2093.00f;
        final float[] frequencyPerChannel = {c6NoteFreq, e6NoteFreq, g6NoteFreq, c7NoteFreq};
        // Thirty seconds of samples
        final int totalSamplesToSend = sampleRate * 30;

        int[] totalSamplesPerChannel = new int[channelCount];

        for(int i = 0; i < totalSamplesToSend; i += sampleCount) {
            data.position(0);
            for(int ch = 0; ch < channelCount; ch++) {
                for(int sample = 0; sample < sampleCount; sample++) {
                    float val = (float) Math.sin(totalSamplesPerChannel[ch] * Math.PI * frequencyPerChannel[ch] * (1f/sampleRate));
                    data.putFloat(val);
                    totalSamplesPerChannel[ch]++;
                }
            }
            data.flip();

            sender.sendAudioFrame(audioFrame);
        }

        // Destroy the references to each. Not necessary, but can free up the memory faster than Java's GC by itself
        audioFrame.close();
        sender.close();
    }
}
