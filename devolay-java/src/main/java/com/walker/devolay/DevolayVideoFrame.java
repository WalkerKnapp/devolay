package com.walker.devolay;

import java.lang.ref.Cleaner;
import java.nio.ByteBuffer;

public class DevolayVideoFrame implements AutoCloseable {

    static class State implements Runnable {
        private long structPointer;

        State(long pointer) {
            this.structPointer = pointer;
        }

        public void run() {
            destroyVideoFrame(structPointer);
        }
    }

    private final State state;
    private final Cleaner.Cleanable cleanable;
    final long structPointer;

    public DevolayVideoFrame() {
        this.structPointer = createNewVideoFrameDefaultSettings();

        this.state = new State(structPointer);
        this.cleanable = Devolay.cleaner.register(this, state);
    }

    public void setResolution(int width, int height) {
        setXRes(structPointer, width);
        setYRes(structPointer, height);
    }

    public void setFourCCType(DevolayFrameFourCCType type) {
        setFourCCType(structPointer, type.id);
    }

    public void setFrameRate(int numerator, int denominator) {
        setFrameRateN(structPointer, numerator);
        setFrameRateD(structPointer, denominator);
    }

    public void setAspectRatio(float aspectRatio) {
        setPictureAspectRatio(structPointer, aspectRatio);
    }

    public void setFormatType(DevolayFrameFormatType type) {
        setFrameFormatType(structPointer, type.id);
    }

    public void setTimecode(long timecode) {
        setTimecode(structPointer, timecode);
    }

    public void setLineStride(int lineStride) {
        setLineStride(structPointer, lineStride);
    }

    public void setMetadata(String metadata) {
        setMetadata(structPointer, metadata);
    }

    public void setTimestamp(int timestamp) {
        setTimestamp(structPointer, timestamp);
    }

    public void setData(ByteBuffer buffer) {
        setData(structPointer, buffer);
    }

    @Override
    public void close() {
        cleanable.clean();
    }

    // Native Functions

    private static native long createNewVideoFrameDefaultSettings();
    private static native void destroyVideoFrame(long pointer);

    private static native void setXRes(long pointer, int xRes);
    private static native void setYRes(long pointer, int yRes);

    private static native void setFourCCType(long pointer, int fourCCType);

    private static native void setFrameRateN(long pointer, int frameRateN);
    private static native void setFrameRateD(long pointer, int frameRateD);

    private static native void setPictureAspectRatio(long pointer, float aspectRatio);

    private static native void setFrameFormatType(long pointer, int frameFormatType);

    private static native void setTimecode(long pointer, long timecode);

    private static native void setLineStride(long pointer, int lineStride);

    private static native void setMetadata(long pointer, String metadata);

    private static native void setTimestamp(long pointer, int timestamp);

    /**
     *
     * @param pointer
     * @param buffer MUST BE A DIRECT BUFFER, ALLOCATED USING BUFFER#ALLOCATEDIRECT()
     */
    private static native void setData(long pointer, ByteBuffer buffer);
}
