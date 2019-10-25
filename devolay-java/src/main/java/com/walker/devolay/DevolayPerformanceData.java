package com.walker.devolay;

import java.lang.ref.Cleaner;

public class DevolayPerformanceData implements AutoCloseable {
    static class State implements Runnable {
        private long totalStructPointer;
        private long droppedStructPointer;

        State(long totalPointer, long droppedPointer) {
            this.totalStructPointer = totalPointer;
            this.droppedStructPointer = droppedPointer;
        }

        public void run() {
            destroyPerformanceStruct(totalStructPointer);
            destroyPerformanceStruct(droppedStructPointer);
        }
    }

    private final State state;
    private final Cleaner.Cleanable cleanable;

    final long totalPerformanceStructPointer;
    final long droppedPerformanceStructPointer;

    public DevolayPerformanceData() {
        this.totalPerformanceStructPointer = createPerformanceStruct();
        this.droppedPerformanceStructPointer = createPerformanceStruct();

        this.state = new State(totalPerformanceStructPointer, droppedPerformanceStructPointer);
        this.cleanable = Devolay.cleaner.register(this, state);
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
    public void close() throws Exception {
        cleanable.clean();
    }

    // Native Methods
    private static native long createPerformanceStruct();
    private static native void destroyPerformanceStruct(long structPointer);

    private static native long getPerformanceStructVideoFrames(long structPointer);
    private static native long getPerformanceStructAudioFrames(long structPointer);
    private static native long getPerformanceStructMetadataFrames(long structPointer);
}
