package com.walker.devolay;

import com.walker.devolay.Devolay;

import java.lang.ref.Cleaner;
import java.nio.ByteBuffer;

public class DevolayAudioFrame implements AutoCloseable {

    static class State implements Runnable {
        private long structPointer;

        State(long pointer) {
            this.structPointer = pointer;
        }

        public void run() {
            destroyAudioFrame(structPointer);
        }
    }


    private final State state;
    private final Cleaner.Cleanable cleanable;
    final long structPointer;

    public DevolayAudioFrame() {
        this.structPointer = createNewAudioFrameDefaultSettings();

        this.state = new State(structPointer);
        this.cleanable = Devolay.cleaner.register(this, state);
    }

    public void setSampleRate(int sampleRate) {
        setSampleRate(structPointer, sampleRate);
    }

    public void setChannels(int channels) {
        setNoChannels(structPointer, channels);
    }

    public void setTimecode(long timecode) {
        setTimecode(structPointer, timecode);
    }

    public void setChannelStride(int channelStride) {
        setChannelStride(structPointer, channelStride);
    }

    public void setMetadata(String metadata) {
        setMetadata(structPointer, metadata);
    }

    public void setTimestamp(long timestamp) {
        setTimestamp(structPointer, timestamp);
    }

    public void setData(ByteBuffer data) {
        setData(structPointer, data);
    }

    @Override
    public void close() {
        cleanable.clean();
    }

    // Native Methods

    private static native long createNewAudioFrameDefaultSettings();
    private static native void destroyAudioFrame(long structPointer);

    private static native void setSampleRate(long structPointer, int sampleRate);
    private static native void setNoChannels(long structPointer, int noChannels);
    private static native void setTimecode(long structPointer, long timecode);
    private static native void setChannelStride(long structPointer, int channelStride);
    private static native void setMetadata(long structPointer, String metadata);
    private static native void setTimestamp(long structPointer, long timestamp);
    private static native void setData(long structPointer, ByteBuffer buffer);
}
