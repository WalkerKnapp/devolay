package me.walkerknapp.devolay;

import java.util.concurrent.atomic.AtomicBoolean;

public class DevolaySource implements AutoCloseable {

    private final AtomicBoolean isClosed;

    final long structPointer;

    /**
     * Creates a source instance from a pointer to the internal source object.
     *
     * @param pointer The pointer to the internal source object.
     */
    DevolaySource(long pointer) {
        // TODO: Implement this forced reference more effectively
        Devolay.loadLibraries();

        this.isClosed = new AtomicBoolean(false);
        this.structPointer = pointer;
    }

    /**
     * Returns the name of this source instance.
     *
     * @return The current name of this source.
     */
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

    // Native methods

    private static native void deallocSource(long pointer);

    private static native String getSourceName(long structPointer);
}
