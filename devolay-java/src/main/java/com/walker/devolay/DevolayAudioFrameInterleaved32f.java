package com.walker.devolay;

import java.nio.ByteBuffer;

/**
 * An audio frame that stores floating-point interleaved samples
 */
public class DevolayAudioFrameInterleaved32f implements AutoCloseable {

    static {
        // TODO: Implement this forced reference more effectively
        Devolay.loadLibraries();
    }

    final long structPointer;

    public DevolayAudioFrameInterleaved32f() {
        this.structPointer = createNewAudioFrameInterleaved32fDefaultSettings();
    }

    public void setSampleRate(int sampleRate) {
        setSampleRate(structPointer, sampleRate);
    }
    public int getSampleRate() {
        return getSampleRate(structPointer);
    }

    public void setChannels(int channels) {
        setNoChannels(structPointer, channels);
    }
    public int getChannels() {
        return getNoChannels(structPointer);
    }

    public void setSamples(int samples) {
        setNoSamples(structPointer, samples);
    }
    public int getSamples() {
        return getNoSamples(structPointer);
    }

    public void setTimecode(long timecode) {
        setTimecode(structPointer, timecode);
    }
    public long getTimecode() {
        return getTimecode(structPointer);
    }

    public void setData(ByteBuffer data) {
        setData(structPointer, data);
    }
    public ByteBuffer getData() {
        return getData(structPointer);
    }

    @Override
    public void close() {
        // TODO: Auto-clean resources.
        destroyAudioFrameInterleaved32f(structPointer);
    }

    // Native Methods

    private static native long createNewAudioFrameInterleaved32fDefaultSettings();
    private static native void destroyAudioFrameInterleaved32f(long structPointer);

    private static native void setSampleRate(long structPointer, int sampleRate);
    private static native int getSampleRate(long structPointer);
    private static native void setNoChannels(long structPointer, int channels);
    private static native int getNoChannels(long structPointer);
    private static native void setNoSamples(long structPointer, int samples);
    private static native int getNoSamples(long structPointer);
    private static native void setTimecode(long structPointer, long timecode);
    private static native long getTimecode(long structPointer);
    private static native void setData(long structPointer, ByteBuffer data);
    private static native ByteBuffer getData(long structPointer);
}
