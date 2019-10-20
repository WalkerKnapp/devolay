package com.walker.devolay;

import java.lang.ref.Cleaner;
import java.util.concurrent.atomic.AtomicBoolean;

public class DevolaySource implements AutoCloseable {

    static class State implements Runnable {
        private long structPointer;
        public AtomicBoolean isClosed = new AtomicBoolean(false);

        State(long pointer) {
            this.structPointer = pointer;
        }

        public void run() {
            isClosed.set(true);
            deallocSource(structPointer);
        }
    }

    private final State state;
    private final Cleaner.Cleanable cleanable;
    final long structPointer;

    DevolaySource(long pointer) {
        this.structPointer = pointer;

        this.state = new State(structPointer);
        this.cleanable = Devolay.cleaner.register(this, state);
    }

    public String getSourceName() {
        if(state.isClosed.get()) {
            throw new IllegalStateException("Cannot access attribute of closed DevolaySource. Please read the javadocs for DevolayFinder#getCurrentSources");
        }
        return getSourceName(structPointer);
    }

    @Override
    public void close() {
        cleanable.clean();
    }

    private static native void deallocSource(long pointer);

    private static native String getSourceName(long structPointer);
}
