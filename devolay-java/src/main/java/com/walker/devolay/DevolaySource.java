package com.walker.devolay;

import java.util.concurrent.atomic.AtomicBoolean;

public class DevolaySource implements AutoCloseable {

    private final AtomicBoolean isClosed;

    final long structPointer;

    DevolaySource(long pointer) {
        // TODO: Implement this forced reference more effectively
        Devolay.loadLibraries();

        this.isClosed = new AtomicBoolean(false);
        this.structPointer = pointer;
    }

    public String getSourceName() {
        if(isClosed.get()) {
            throw new IllegalStateException("Cannot access attribute of closed DevolaySource. Please read the javadocs for DevolayFinder#getCurrentSources");
        }
        return getSourceName(structPointer);
    }

    @Override
    public void close() {
        // TODO: Auto-clean resources.
        deallocSource(structPointer);

        isClosed.set(true);
    }

    private static native void deallocSource(long pointer);

    private static native String getSourceName(long structPointer);
}
