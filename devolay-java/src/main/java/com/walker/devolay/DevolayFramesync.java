package com.walker.devolay;

public class DevolayFramesync extends DevolayFrameCleaner implements AutoCloseable {
    static {
        // TODO: Implement this forced reference more effectively
        Devolay.loadLibraries();
    }

    /**
     * A pointer to the NDIlib_framesync_instance_t instance
     */
    private final long structPointer;

    /**
     * Creates a frame synchronizer object bound to a given receiver.
     *
     * Once this receiver has been bound to a frame-sync, it should solely be used to recover video frames.
     * The underlying receiver can still be used for other operations (tally, PTZ, meta-data, etc.)
     *
     * @param receiver The receiver to bind to.
     */
    public DevolayFramesync(DevolayReceiver receiver) {
        structPointer = framesyncCreate(receiver.ndilibRecievePointer);
    }

    public void captureAudio(DevolayAudioFrame audioFrame, int sampleRate, int channelCount, int sampleCount) {

    }

    public int getAudioQueueDepth() {
        return 0;
    }

    public void captureVideo(DevolayVideoFrame videoFrame) {
        captureVideo(videoFrame, DevolayFrameFormatType.PROGRESSIVE);
    }

    public void captureVideo(DevolayVideoFrame videoFrame, DevolayFrameFormatType formatType) {

    }

    @Override
    public void close() {
        // TODO: Auto-clean resources.
        framesyncDestroy(structPointer);
    }

    @Override
    void freeVideo(DevolayVideoFrame videoFrame) {

    }

    @Override
    void freeAudio(DevolayAudioFrame audioFrame) {

    }

    @Override
    void freeMetadata(DevolayMetadataFrame metadataFrame) {

    }

    // Native methods
    private static native long framesyncCreate(long pReceiver);
    private static native void framesyncDestroy(long pFramesync);

}
