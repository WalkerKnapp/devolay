package com.walker.devolay;

public class DevolayFrameSync extends DevolayFrameCleaner implements AutoCloseable {
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
    public DevolayFrameSync(DevolayReceiver receiver) {
        structPointer = framesyncCreate(receiver.ndilibRecievePointer);
    }

    /**
     * Pull audio samples from the frame-sync queue and put into a given frame.
     * This function will always return immediately, with silence if no new audio data is present.
     *
     * This function should be called at the rate you want audio, and it will automatically adapt the incoming
     * signal to match the rate at which you are calling.
     *
     * Note: sampleRate and channelCount do not have to match the incoming audio signal, and will be converted automatically.
     *
     * If you want to know the current incoming audio format, you can call with all parameters set to zero, ie:
     * {@code DevolayFramesync#captureAudio(audioFrame, 0, 0, 0);}
     * will fill audioFrame with the current audio format.
     *
     * At any time you can specify sampleRate and channelCount as 0, and audioFrame will be filled with the current received audio foramt.
     *
     * @param audioFrame A frame to capture audio data into.
     * @param sampleRate A desired sample rate to fill audio at. Audio will be converted if necessary.
     * @param channelCount A desired channel count to fill audio at. Audio will be converted if necessary.
     * @param sampleCount A desired sample count to capture.
     */
    public void captureAudio(DevolayAudioFrame audioFrame, int sampleRate, int channelCount, int sampleCount) {
        audioFrame.freeBuffer();

        framesyncCaptureAudio(structPointer, audioFrame.structPointer, sampleRate, channelCount, sampleCount);

        audioFrame.allocatedBufferSource.set(this);
    }

    /*
      For some reason, NDIlib_framesync_audio_queue_depth isn't included in the DynamicLoad spec
      It may be added in a future version, but it doesn't exist currently.

      Approximate the current depth of the audio queue, in number of samples you're able to request.
      Take care using this function, as the frame-sync is meant to match the calling rate.
      If the system has an inaccurate clock, this function can be useful, for instance:
      <pre>{@code
     * while(true) {
     *     int sampleCount = framesync.getAudioQueueDepth();
     *     framesync.captureAudio(..., sampleCount);
     *     playAudio(...);
     *     innacurateSleep(33ms);
     * }
     * }</pre>

      Due to the real-time nature of the framesync, this function is not guaranteed to match the real count, but it
      will always return at least the minimum count in the queue until the next #captureAudio call.

      @return The current depth of the audio queue in samples
     */
    /*public int getAudioQueueDepth() {
        return framesyncAudioQueueDepth(structPointer);
    }*/

    /**
     * Pull video samples from the frame-sync queue.
     * This function will always return immediately, using time-base correction if needed.
     *
     * The FrameFormatType can be specified, which is then used to return the best possible frame.
     * Note that field-based frame-sync means that the frame-sync attempts to match the fielded input
     * phase with the frame requests so you have the most correct ordering on output.
     *
     * Note: The same frame can be returned multiple times.
     *
     * If no video frames have been received, this will return false, and have a {@code null} video data.
     *
     * @param videoFrame A frame to capture video data into.
     * @param formatType The desired frame format type.
     * @return Whether a video frame has been received.
     */
    public boolean captureVideo(DevolayVideoFrame videoFrame, DevolayFrameFormatType formatType) {
        videoFrame.freeBuffer();

        boolean started = framesyncCaptureVideo(structPointer, videoFrame.structPointer, formatType.id);

        if(started) {
            videoFrame.allocatedBufferSource.set(this);
        }

        return started;
    }

    /**
     * Pull video samples from the frame-sync queue.
     * This function will always return immediately, using time-base correction if needed.
     *
     * This function will get frames formatted as {@code DevolayFrameFormatType.PROGRESSIVE}
     * Note: The same frame can be returned multiple times.
     *
     * If no video frames have been received, this will return false, and have a {@code null} video data.
     *
     * @param videoFrame A frame to capture video data into.
     * @return Whether a video frame has been received.
     */
    public boolean captureVideo(DevolayVideoFrame videoFrame) {
        return captureVideo(videoFrame, DevolayFrameFormatType.PROGRESSIVE);
    }

    @Override
    public void close() {
        // TODO: Auto-clean resources.
        framesyncDestroy(structPointer);
    }

    @Override
    void freeVideo(DevolayVideoFrame videoFrame) {
        framesyncFreeVideo(structPointer, videoFrame.structPointer);
    }

    @Override
    void freeAudio(DevolayAudioFrame audioFrame) {
        framesyncFreeAudio(structPointer, audioFrame.structPointer);
    }

    @Override
    void freeMetadata(DevolayMetadataFrame metadataFrame) {
        throw new UnsupportedOperationException("Tried to free metadata frame with framesync. This should be unreachable, please open an issue at https://github.com/WalkerKnapp/devolay/issues.");
    }

    // Native methods
    private static native long framesyncCreate(long pReceiver);
    private static native void framesyncDestroy(long pFramesync);

    private static native void framesyncCaptureAudio(long pFramesync, long pFrame, int sampleRate, int noChannels, int noSamples);
    //private static native int framesyncAudioQueueDepth(long pFramesync);
    private static native void framesyncFreeAudio(long pFramesync, long pFrame);

    private static native boolean framesyncCaptureVideo(long pFramesync, long pFrame, int frameFormat);
    private static native void framesyncFreeVideo(long pFramesync, long pFrame);

}
