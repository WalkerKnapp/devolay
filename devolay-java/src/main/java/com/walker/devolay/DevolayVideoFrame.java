package com.walker.devolay;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicReference;

public class DevolayVideoFrame implements AutoCloseable {

    final long structPointer;

    // set when a buffer is allocated by a receiver that later needs to be freed w/ that receiver.
    AtomicReference<DevolayFrameCleaner> allocatedBufferSource = new AtomicReference<>();

    public DevolayVideoFrame() {
        // TODO: Implement this forced reference more effectively
        Devolay.loadLibraries();

        this.structPointer = createNewVideoFrameDefaultSettings();
    }

    public void setResolution(int width, int height) {
        setXRes(structPointer, width);
        setYRes(structPointer, height);
    }
    public int getXResolution() {
        return getXRes(structPointer);
    }
    public int getYResolution() {
        return getYRes(structPointer);
    }

    public void setFourCCType(DevolayFrameFourCCType type) {
        setFourCCType(structPointer, type.id);
    }
    public DevolayFrameFourCCType getFourCCType() {
        return DevolayFrameFourCCType.valueOf(getFourCCType(structPointer));
    }

    public void setFrameRate(int numerator, int denominator) {
        setFrameRateN(structPointer, numerator);
        setFrameRateD(structPointer, denominator);
    }
    public int getFrameRateN() {
        return getFrameRateN(structPointer);
    }
    public int getFrameRateD() {
        return getFrameRateD(structPointer);
    }

    public void setAspectRatio(float aspectRatio) {
        setPictureAspectRatio(structPointer, aspectRatio);
    }
    public float getAspectRatio() {
        return getPictureAspectRatio(structPointer);
    }

    public void setFormatType(DevolayFrameFormatType type) {
        setFrameFormatType(structPointer, type.id);
    }
    public DevolayFrameFormatType getFormatType() {
        return DevolayFrameFormatType.valueOf(getFrameFormatType(structPointer));
    }

    public void setTimecode(long timecode) {
        setTimecode(structPointer, timecode);
    }
    public long getTimecode() {
        return getTimecode(structPointer);
    }

    public void setLineStride(int lineStride) {
        setLineStride(structPointer, lineStride);
    }
    public int getLineStride() {
        return getLineStride(structPointer);
    }

    public void setMetadata(String metadata) {
        setMetadata(structPointer, metadata);
    }
    public String getMetadata() {
        return getMetadata(structPointer);
    }

    public void setTimestamp(int timestamp) {
        setTimestamp(structPointer, timestamp);
    }
    public int getTimestamp() {
        return getTimestamp(structPointer);
    }

    public void setData(ByteBuffer buffer) {
        freeBuffer();
        setData(structPointer, buffer);
    }
    public ByteBuffer getData() {
        return getData(structPointer);
    }

    /**
     * If a buffer is allocated by a Devolay process (DevolayReceiver#receiveCapture), free the buffer.
     * This allows a previously used frame to be reused in DevolayReceiver#receiveCapture
     */
    public void freeBuffer() {
        if(allocatedBufferSource.get() != null) {
            allocatedBufferSource.getAndSet(null).freeVideo(this);
        }
    }

    @Override
    public void close() {
        freeBuffer();
        // TODO: Auto-clean resources.
        destroyVideoFrame(structPointer);
    }

    // Native Functions

    private static native long createNewVideoFrameDefaultSettings();
    private static native void destroyVideoFrame(long pointer);

    private static native void setXRes(long pointer, int xRes);
    private static native void setYRes(long pointer, int yRes);
    private static native int getXRes(long pointer);
    private static native int getYRes(long pointer);

    private static native void setFourCCType(long pointer, int fourCCType);
    private static native int getFourCCType(long pointer);

    private static native void setFrameRateN(long pointer, int frameRateN);
    private static native void setFrameRateD(long pointer, int frameRateD);
    private static native int getFrameRateN(long pointer);
    private static native int getFrameRateD(long pointer);

    private static native void setPictureAspectRatio(long pointer, float aspectRatio);
    private static native float getPictureAspectRatio(long pointer);

    private static native void setFrameFormatType(long pointer, int frameFormatType);
    private static native int getFrameFormatType(long pointer);

    private static native void setTimecode(long pointer, long timecode);
    private static native long getTimecode(long pointer);

    private static native void setLineStride(long pointer, int lineStride);
    private static native int getLineStride(long pointer);

    private static native void setMetadata(long pointer, String metadata);
    private static native String getMetadata(long pointer);

    private static native void setTimestamp(long pointer, int timestamp);
    private static native int getTimestamp(long pointer);

    /**
     *
     * @param pointer
     * @param buffer MUST BE A DIRECT BUFFER, ALLOCATED USING BUFFER#ALLOCATEDIRECT()
     */
    private static native void setData(long pointer, ByteBuffer buffer);
    private static native ByteBuffer getData(long pointer);
}
