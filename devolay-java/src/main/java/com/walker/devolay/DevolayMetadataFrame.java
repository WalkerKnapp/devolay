package com.walker.devolay;

import java.lang.ref.Cleaner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class DevolayMetadataFrame implements AutoCloseable {

    static class State implements Runnable {
        private long structPointer;

        State(long pointer) {
            this.structPointer = pointer;
        }

        public void run() {
            destroyMetadataFrame(structPointer);
        }
    }


    private final State state;
    private final Cleaner.Cleanable cleanable;
    final long structPointer;

    // set when a buffer is allocated by a receiver that later needs to be freed w/ that receiver.
    AtomicReference<DevolayReceiver> allocatedBufferSource = new AtomicReference<>();

    public DevolayMetadataFrame() {
        this.structPointer = createNewMetadataFrameDefaultSettings();

        this.state = new State(structPointer);
        this.cleanable = Devolay.cleaner.register(this, state);
    }

    public String getData() {
        return getData(structPointer);
    }

    public void setData(String data) {
        if(allocatedBufferSource.get() != null) {
            allocatedBufferSource.getAndSet(null).freeMetadata(this);
        }
        setData(structPointer, data);
    }

    public long getTimecode() {
        return getTimecode(structPointer);
    }

    public void setTimecode(long timecode) {
        setTimecode(structPointer, timecode);
    }

    @Override
    public void close() {
        if(allocatedBufferSource.get() != null) {
            allocatedBufferSource.getAndSet(null).freeMetadata(this);
        }
        cleanable.clean();
    }

    // Native Methods

    private static native long createNewMetadataFrameDefaultSettings();
    private static native void destroyMetadataFrame(long structPointer);

    private static native String getData(long structPointer);
    private static native void setData(long structPointer, String data);
    private static native long getTimecode(long structPointer);
    private static native void setTimecode(long structPointer, long timecode);
}
