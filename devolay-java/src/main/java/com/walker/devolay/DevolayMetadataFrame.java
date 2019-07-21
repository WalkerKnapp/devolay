package com.walker.devolay;

import java.lang.ref.Cleaner;

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

    public DevolayMetadataFrame() {
        this.structPointer = createNewMetadataFrameDefaultSettings();

        this.state = new State(structPointer);
        this.cleanable = Devolay.cleaner.register(this, state);
    }

    public String getData() {
        return getData(structPointer);
    }

    public void setData(String data) {
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
