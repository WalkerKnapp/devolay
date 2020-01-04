package com.walker.devolay;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicReference;

/**
 * An audio frame that stores floating-point planar samples
 */
public class DevolayAudioFrame implements AutoCloseable {

    static {
        // TODO: Implement this forced reference more effectively
        Devolay.loadLibraries();
    }

    final long structPointer;

    // set when a buffer is allocated by a source that later needs to be freed w/ that source.
    AtomicReference<DevolayFrameCleaner> allocatedBufferSource = new AtomicReference<>();

    public DevolayAudioFrame() {
        this.structPointer = createNewAudioFrameDefaultSettings();
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

    public void setChannelStride(int channelStride) {
        setChannelStride(structPointer, channelStride);
    }
    public int getChannelStride() {
        return getChannelStride(structPointer);
    }

    public void setMetadata(String metadata) {
        setMetadata(structPointer, metadata);
    }
    public String getMetadata() {
        return getMetadata(structPointer);
    }

    public void setTimestamp(long timestamp) {
        setTimestamp(structPointer, timestamp);
    }
    public long getTimestamp() {
        return getTimestamp(structPointer);
    }

    /**
     * Sets the audio data to a floating-point, planar DIRECT buffer.
     *
     * Most audio will consist of interleaved data, so should use DevolayUtilities.interleavedFloatToPlanarFloat
     * before setting.
     *
     * @param data A planar ByteBuffer of floating point samples. MUST be a direct ByteBuffer.
     */
    public void setData(ByteBuffer data) {
        freeBuffer();
        setData(structPointer, data);
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
            allocatedBufferSource.getAndSet(null).freeAudio(this);
        }
    }

    @Override
    public void close() {
        // TODO: Auto-clean resources.
        destroyAudioFrame(structPointer);
    }

    // Native Methods

    private static native long createNewAudioFrameDefaultSettings();
    private static native void destroyAudioFrame(long structPointer);

    private static native void setSampleRate(long structPointer, int sampleRate);
    private static native int getSampleRate(long structPointer);
    private static native void setNoChannels(long structPointer, int noChannels);
    private static native int getNoChannels(long structPointer);
    private static native void setNoSamples(long structPointer, int noSamples);
    private static native int getNoSamples(long structPointer);
    private static native void setTimecode(long structPointer, long timecode);
    private static native long getTimecode(long structPointer);
    private static native void setChannelStride(long structPointer, int channelStride);
    private static native int getChannelStride(long structPointer);
    private static native void setMetadata(long structPointer, String metadata);
    private static native String getMetadata(long structPointer);
    private static native void setTimestamp(long structPointer, long timestamp);
    private static native long getTimestamp(long structPointer);
    private static native void setData(long structPointer, ByteBuffer buffer);
    private static native ByteBuffer getData(long structPointer);
}
