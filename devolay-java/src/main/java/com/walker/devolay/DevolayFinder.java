package com.walker.devolay;

public class DevolayFinder implements AutoCloseable {
    /**
     * Holds the reference to the NDIlib_find_instance_t object
     */
    private final long ndiLibFindInstancePointer;

    private DevolaySource[] previouslyQueriedSources;

    /**
     * Creates a NDIlib_find_create_t instance, and uses it to initialize the internal NDIlib_send_instance_t object.
     *
     * @param showLocalSources If NDI sources running on the local machine should be included.
     * @param groups The groups this DevolayFinder should query for.
     * @param extraIps A comma-separated list of IP addresses NDI should additionally query for. See Processing.NDI.Find.h
     */
    public DevolayFinder(boolean showLocalSources, String groups, String extraIps) {
        // TODO: Implement this forced reference more effectively
        Devolay.loadLibraries();

        this.ndiLibFindInstancePointer = findCreate(showLocalSources, groups, extraIps);
    }

    public DevolayFinder(boolean showLocalSources, String groups) {
        this(showLocalSources, groups, null);
    }

    public DevolayFinder(boolean showLocalSources) {
        this(showLocalSources, null, null);
    }

    public DevolayFinder() {
        // TODO: Implement this forced reference more effectively
        Devolay.loadLibraries();

        this.ndiLibFindInstancePointer = findCreateDefaultSettings();
    }

    /**
     * Queries for sources that are currently active and visible to this DevolayFinder instance.
     * The DevolaySource instances created with this method are only valid until
     * the next DevolayFinder#getCurrentSources or DevolayFinder#close call.
     *
     * @return An array of DevolaySources with information about each current source.
     */
    public synchronized DevolaySource[] getCurrentSources() {
        if(previouslyQueriedSources != null) {
            for(DevolaySource prev : previouslyQueriedSources) {
                prev.close();
            }
        }
        long[] currentSources = findGetCurrentSources(ndiLibFindInstancePointer);
        DevolaySource[] currentDevolaySources = new DevolaySource[currentSources.length];
        for(int i = 0; i < currentSources.length; i++) {
            currentDevolaySources[i] = new DevolaySource(currentSources[i]);
        }

        this.previouslyQueriedSources = currentDevolaySources;

        return currentDevolaySources;
    }

    /**
     * Waits until the number of online sources have changed, or until the timeout is reached.
     *
     * @param timeout The timeout for the query in milliseconds, or 0 for no timeout.
     * @return true if the sources have changed, false if the timeout was reached.
     */
    public boolean waitForSources(int timeout) {
        return findWaitForSources(ndiLibFindInstancePointer, timeout);
    }

    @Override
    public void close() {
        if(previouslyQueriedSources != null) {
            for(DevolaySource prev : previouslyQueriedSources) {
                prev.close();
            }
        }
        // TODO: Auto-clean resources.
        findDestroy(ndiLibFindInstancePointer);
    }

    // Native Methods
    private static native long findCreate(boolean showLocalSources, String groups, String extraIps);
    // Should offer a small performance/memory improvement over using #findCreate with the default values.
    private static native long findCreateDefaultSettings();
    private static native void findDestroy(long structPointer);

    private static native long[] findGetCurrentSources(long structPointer);
    private static native boolean findWaitForSources(long structPointer, int timeout);
}
