package com.walker.devolay;

import java.lang.ref.Cleaner;

public class DevolaySource implements AutoCloseable {

    static class State implements Runnable {
        private long structPointer;

        State(long pointer) {
            this.structPointer = pointer;
        }

        public void run() {
            deallocSource(structPointer);
        }
    }

    private final DevolayAudioFrame.State state;
    private final Cleaner.Cleanable cleanable;
    final long structPointer;

    DevolaySource(long pointer) {
        this.structPointer = pointer;

        this.state = new DevolayAudioFrame.State(structPointer);
        this.cleanable = Devolay.cleaner.register(this, state);
    }

    public String getSourceName() {
        return getSourceName(structPointer);
    }

    @Override
    public void close() throws Exception {
        cleanable.clean();
    }

    private static native void deallocSource(long pointer);

    private static native String getSourceName(long structPointer);
}
