package me.walkerknapp.devolay;

/**
 * An object used to create a fake "routed" source, which mirrors another source and can seamlessly switch between them.
 */
public class DevolayRouter implements AutoCloseable {
    /**
     * Holds the reference to the NDIlib_routing_instance_t object
     */
    private final long ndiLibRoutingInstancePointer;

    /**
     * Creates an allocates a router instance.
     *
     * @param ndiName The name of the routed NDI source to create, or {@code null} to auto-generate.
     * @param groups The groups this source should be a part of, or {@code null} for default.
     */
    public DevolayRouter(String ndiName, String groups) {
        // TODO: Implement this forced reference more effectively
        Devolay.loadLibraries();

        this.ndiLibRoutingInstancePointer = routingCreate(ndiName, groups);
    }

    /**
     * Creates and allocates a router instance.
     *
     * The routed source is assigned to the NDI default group.
     *
     * @param ndiName The name of the routed NDI source to create, or {@code null} to auto-generate.
     */
    public DevolayRouter(String ndiName) {
        this(ndiName, null);
    }

    /**
     * Creates and allocates a router instance.
     *
     * The routed source is assigned to the default NDI group.
     * The name for the routed NDI source is automatically generated.
     */
    public DevolayRouter() {
        this(null, null);
    }

    /**
     * Change the source that is being mirrored to the routed source.
     *
     * @param source A source given by a {@link DevolayFinder} instance. This must not be {@code null}: instead, call {@link #clearSource()}.
     */
    public void setSource(DevolaySource source) {
        routingChange(this.ndiLibRoutingInstancePointer, source.structPointer);
    }

    /**
     * Retrieves information about the source that is currently being mirrored to the routed source.
     * The {@link DevolaySource} instance created with this method is valid until {@link #close()} is called.
     *
     * @return A {@link DevolaySource} instance with information about the current source, or null if this source is disconnected.
     */
    public DevolaySource getSource() {
        long sourcePointer = routingSource(this.ndiLibRoutingInstancePointer);
        if (sourcePointer != 0L) {
            return new DevolaySource(sourcePointer);
        } else {
            return null;
        }
    }

    /**
     * Clear the source that is being mirrored to the routed source.
     * Subsequent frames read from this source will be 29.97fps 16x8 black frames with no audio.
     */
    public void clearSource() {
        routingClear(this.ndiLibRoutingInstancePointer);
    }

    /**
     * Queries the current number of receivers connected to the routed source.
     *
     * This can be used to stop doing work when nothing is actually connected to the source,
     * which can greatly improve network and compute performance.
     *
     * By setting timeoutMs to 0, this function will immediately return the number of connections,
     * but by specifying a timeout, this function blocks until the number of connections is non-zero.
     *
     * This function will still return 0 if the query times out.
     *
     * @param timeoutMs The timeout in milliseconds.
     * @return The number of receivers connected to the routed source.
     */
    public int getConnectionCount(int timeoutMs) {
        return routingNoConnections(this.ndiLibRoutingInstancePointer, timeoutMs);
    }

    @Override
    public void close() {
        // TODO: Auto-clean resources.
        routingDestroy(ndiLibRoutingInstancePointer);
    }


    // Native Methods

    private static native long routingCreate(String ndiName, String groups);

    private static native void routingChange(long routingInstance, long sourceInstance);
    private static native void routingClear(long routingInstance);
    private static native int routingNoConnections(long routingInstance, int timeoutMs);
    private static native long routingSource(long routingInstance);

    private static native void routingDestroy(long routingInstance);
}
