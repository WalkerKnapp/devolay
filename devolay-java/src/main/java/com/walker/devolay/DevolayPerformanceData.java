package com.walker.devolay;

public class DevolayPerformanceData implements AutoCloseable {

    final long totalPerformanceStructPointer;
    final long droppedPerformanceStructPointer;

    public DevolayPerformanceData() {
        // TODO: Implement this forced reference more effectively
        Devolay.loadLibraries();

        this.totalPerformanceStructPointer = createPerformanceStruct();
        this.droppedPerformanceStructPointer = createPerformanceStruct();
    }

    public long getTotalVideoFrames() {
        return getPerformanceStructVideoFrames(totalPerformanceStructPointer);
    }

    public long getTotalAudioFrames() {
        return getPerformanceStructAudioFrames(totalPerformanceStructPointer);
    }

    public long getTotalMetadataFrames() {
        return getPerformanceStructMetadataFrames(totalPerformanceStructPointer);
    }

    public long getDroppedVideoFrames() {
        return getPerformanceStructVideoFrames(droppedPerformanceStructPointer);
    }

    public long getDroppedAudioFrames() {
        return getPerformanceStructAudioFrames(droppedPerformanceStructPointer);
    }

    public long getDroppedMetadataFrames() {
        return getPerformanceStructMetadataFrames(droppedPerformanceStructPointer);
    }

    @Override
    public void close() {
        // TODO: Auto-clean resources.
        destroyPerformanceStruct(totalPerformanceStructPointer);
        destroyPerformanceStruct(droppedPerformanceStructPointer);
    }

    // Native Methods
    private static native long createPerformanceStruct();
    private static native void destroyPerformanceStruct(long structPointer);

    private static native long getPerformanceStructVideoFrames(long structPointer);
    private static native long getPerformanceStructAudioFrames(long structPointer);
    private static native long getPerformanceStructMetadataFrames(long structPointer);
}
