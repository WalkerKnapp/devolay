package com.walker.devolay;

import java.lang.ref.Cleaner;
import java.nio.ByteBuffer;

public class DevolayAudioFrameInterleaved32s implements AutoCloseable {

    static class State implements Runnable {
        private long structPointer;

        State(long pointer) {
            this.structPointer = pointer;
        }

        public void run() {
            destroyAudioFrameInterleaved16s(structPointer);
        }
    }


    private final DevolayAudioFrame.State state;
    private final Cleaner.Cleanable cleanable;
    final long structPointer;

    public DevolayAudioFrameInterleaved32s() {
        this.structPointer = createNewAudioFrameInterleaved16sDefaultSettings();

        this.state = new DevolayAudioFrame.State(structPointer);
        this.cleanable = Devolay.cleaner.register(this, state);
    }

    public void setSampleRate(int sampleRate) {
        setSampleRate(structPointer, sampleRate);
    }

    public void setChannels(int channels) {
        setNoChannels(structPointer, channels);
    }

    public void setSamples(int samples) {
        setNoSamples(structPointer, samples);
    }

    public void setTimecode(long timecode) {
        setTimecode(structPointer, timecode);
    }

    public void setReferenceLevel(int referenceLevel) {
        setReferenceLevel(structPointer, referenceLevel);
    }

    public void setData(ByteBuffer data) {
        setData(structPointer, data);
    }

    @Override
    public void close() {
        cleanable.clean();
    }

    // Native Methods

    private static native long createNewAudioFrameInterleaved16sDefaultSettings();
    private static native void destroyAudioFrameInterleaved16s(long structPointer);

    private native void setSampleRate(long structPointer, int sampleRate);
    private native void setNoChannels(long structPointer, int channels);
    private native void setNoSamples(long structPointer, int samples);
    private native void setTimecode(long structPointer, long timecode);
    private native void setReferenceLevel(long structPointer, int referenceLevel);
    private native void setData(long structPointer, ByteBuffer data);
}
