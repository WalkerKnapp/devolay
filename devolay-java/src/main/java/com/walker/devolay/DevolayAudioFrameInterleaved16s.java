package com.walker.devolay;

import java.nio.ByteBuffer;

public class DevolayAudioFrameInterleaved16s implements AutoCloseable {

    final long structPointer;

    public DevolayAudioFrameInterleaved16s() {
        // TODO: Implement this forced reference more effectively
        Devolay.loadLibraries();

        this.structPointer = createNewAudioFrameInterleaved16sDefaultSettings();
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

    public void setReferenceLevel(int referenceLevel) {
        setReferenceLevel(structPointer, referenceLevel);
    }
    public long getReferenceLevel() {
        return getReferenceLevel(structPointer);
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
        destroyAudioFrameInterleaved16s(structPointer);
    }

    // Native Methods

    private static native long createNewAudioFrameInterleaved16sDefaultSettings();
    private static native void destroyAudioFrameInterleaved16s(long structPointer);

    private static native void setSampleRate(long structPointer, int sampleRate);
    private static native int getSampleRate(long structPointer);
    private static native void setNoChannels(long structPointer, int channels);
    private static native int getNoChannels(long structPointer);
    private static native void setNoSamples(long structPointer, int samples);
    private static native int getNoSamples(long structPointer);
    private static native void setTimecode(long structPointer, long timecode);
    private static native long getTimecode(long structPointer);
    private static native void setReferenceLevel(long structPointer, int referenceLevel);
    private static native int getReferenceLevel(long structPointer);
    private static native void setData(long structPointer, ByteBuffer data);
    private static native ByteBuffer getData(long structPointer);
}
