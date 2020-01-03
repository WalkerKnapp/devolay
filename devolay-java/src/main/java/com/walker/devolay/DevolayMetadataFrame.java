package com.walker.devolay;

import java.util.concurrent.atomic.AtomicReference;

public class DevolayMetadataFrame implements AutoCloseable {

    final long structPointer;

    // set when a buffer is allocated by a receiver that later needs to be freed w/ that receiver.
    AtomicReference<DevolayFrameCleaner> allocatedBufferSource = new AtomicReference<>();

    public DevolayMetadataFrame() {
        // TODO: Implement this forced reference more effectively
        Devolay.loadLibraries();

        this.structPointer = createNewMetadataFrameDefaultSettings();
    }

    public String getData() {
        return getData(structPointer);
    }

    public void setData(String data) {
        freeBuffer();
        setData(structPointer, data);
    }

    public long getTimecode() {
        return getTimecode(structPointer);
    }

    public void setTimecode(long timecode) {
        setTimecode(structPointer, timecode);
    }

    /**
     * If a buffer is allocated by a Devolay process (DevolayReceiver#receiveCapture), free the buffer.
     * This allows a previously used frame to be reused in DevolayReceiver#receiveCapture
     */
    public void freeBuffer() {
        if(allocatedBufferSource.get() != null) {
            allocatedBufferSource.getAndSet(null).freeMetadata(this);
        }
    }

    @Override
    public void close() {
        freeBuffer();
        // TODO: Auto-clean resources.
        destroyMetadataFrame(structPointer);
    }

    // Native Methods

    private static native long createNewMetadataFrameDefaultSettings();
    private static native void destroyMetadataFrame(long structPointer);

    private static native String getData(long structPointer);
    private static native void setData(long structPointer, String data);
    private static native long getTimecode(long structPointer);
    private static native void setTimecode(long structPointer, long timecode);
}
