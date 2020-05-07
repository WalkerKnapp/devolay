package com.walker.devolay;

/**
 * An object used to send video/audio/metadata frames on a network or across internet connections.
 */
public class DevolaySender extends DevolayFrameCleaner implements AutoCloseable {

    /**
     * Holds the reference to the NDIlib_send_instance_t object
     */
    private final long ndilibSendInstancePointer;

    /**
     * Creates and allocates a sender instance.
     *
     * @param ndiName The name of the NDI source to create, or {@code null} to auto-generate.
     * @param groups The groups this source should be a part of, or {@code null} for default.
     * @param clockVideo A "rate-limiting" when you submit frames to match the current framerate. See Processing.NDI.Send.h
     * @param clockAudio A "rate-limiting" when you submit frames to match the current framerate. See Processing.NDI.Send.h
     */
    public DevolaySender(String ndiName, String groups, boolean clockVideo, boolean clockAudio) {
        // TODO: Implement this forced reference more effectively
        Devolay.loadLibraries();

        this.ndilibSendInstancePointer = sendCreate(ndiName, groups, clockVideo, clockAudio);
    }

    /**
     * Creates and allocates a sender instance.
     *
     * Video and audio are automatically clocked with this sender. See Processing.NDI.Send.h.
     *
     * @param ndiName The name of the NDI source to create, or {@code null} to auto-generate.
     * @param groups The groups this source should be a part of, or {@code null} for default.
     */
    public DevolaySender(String ndiName, String groups) {
        this(ndiName, groups, true, true);
    }

    /**
     * Creates and allocates a sender instance.
     *
     * Video and audio are automatically clocked with this sender. See Processing.NDI.Send.h.
     * This sender is assigned to the NDI default group.
     *
     * @param ndiName The name of the NDI source to create, or {@code null} to auto-generate.
     */
    public DevolaySender(String ndiName) {
        this(ndiName, null, true, true);
    }

    /**
     * Creates and allocates a sender instance.
     *
     * Video and audio are automatically clocked with this sender. See Processing.NDI.Send.h.
     * This sender is assigned to the NDI default group.
     * The name for this source is automatically generated.
     */
    public DevolaySender() {
        this.ndilibSendInstancePointer = sendCreateDefaultSettings();
    }

    /**
     * Synchronously sends a video frame using this sender.
     *
     * @param frame A filled video frame to send.
     */
    public void sendVideoFrame(DevolayVideoFrame frame) {
        sendVideoV2(ndilibSendInstancePointer, frame.structPointer);
    }

    /**
     * Asynchronously sends a video frame with this sender.
     *
     * When this function is called, it will return immediately and schedule the frame to be later sent.
     *
     * The given {@link DevolayVideoFrame} cannot be freed or re-used until a synchronizing event has occurred.
     * Synchronizing events are :
     *  - a {@link DevolaySender#sendVideoFrame(DevolayVideoFrame)} call
     *  - a {@link DevolaySender#sendVideoFrameAsync(DevolayVideoFrame)} call with another frame to send
     *  - a {@link DevolaySender#sendVideoFrameAsync(DevolayVideoFrame)} call with {@code null}
     *  - a {@link DevolaySender#close()} call
     *
     * Submitting frames asynchronously, in general, will yield a significant performance boost over submitting
     * synchronously.
     *
     * @param frame A filled video frame to send that cannot be freed or re-used until a synchronizing event, or null to trigger a synchronization.
     */
    public void sendVideoFrameAsync(DevolayVideoFrame frame) {
        sendVideoAsyncV2(ndilibSendInstancePointer, frame == null ? 0 : frame.structPointer);
    }

    /**
     * Synchronously sends an audio frame with this sender.
     *
     * @param frame A filled audio frame to send.
     */
    public void sendAudioFrame(DevolayAudioFrame frame) {
        sendAudioV2(ndilibSendInstancePointer, frame.structPointer);
    }

    /**
     * Synchronously sends an interleaved 16s audio frame with this sender.
     *
     * @param frame A filled audio frame to send.
     */
    public void sendAudioFrameInterleaved16s(DevolayAudioFrameInterleaved16s frame) {
        sendAudioInterleaved16s(ndilibSendInstancePointer, frame.structPointer);
    }

    /**
     * Synchronously sends an interleaved 32s audio frame with this sender.
     *
     * @param frame A filled audio frame to send.
     */
    public void sendAudioFrameInterleaved32s(DevolayAudioFrameInterleaved32s frame) {
        sendAudioInterleaved32s(ndilibSendInstancePointer, frame.structPointer);
    }

    /**
     * Synchronously sends an interleaved 32f audio frame with this sender.
     *
     * @param frame A filled audio frame to send.
     */
    public void sendAudioFrameInterleaved32f(DevolayAudioFrameInterleaved32f frame) {
        sendAudioInterleaved32f(ndilibSendInstancePointer, frame.structPointer);
    }

    /**
     * Synchronously sends a metadata frame with this sender.
     *
     * @param frame A filled metadata frame to send.
     */
    public void sendMetadataFrame(DevolayMetadataFrame frame) {
        sendMetadata(ndilibSendInstancePointer, frame.structPointer);
    }

    /**
     * Receives a frame of metadata from any receivers connected to this sender.
     *
     * Frames that are filled with data MUST be later cleared, with calling {@link DevolayMetadataFrame#freeBuffer()}, {@link DevolayMetadataFrame#close}, or with a try-with-resources.
     *
     * This method will free any previously allocated data from all frames, even if data is not written to a given frame.
     *
     * @param metadataFrame A metadata frame to put data into. Any existing data will be replaced. If null, no data will not be captured.
     * @param timeout A timeout in milliseconds for the capture.
     * @return {@link DevolayFrameType#METADATA} if a frame was captured, or {@link DevolayFrameType#NONE} otherwise.
     */
    public DevolayFrameType sendCapture(DevolayMetadataFrame metadataFrame, int timeout) {
        if(metadataFrame != null) {
            metadataFrame.freeBuffer();
        }

        int type = sendCapture(ndilibSendInstancePointer,
                metadataFrame == null ? 0L : metadataFrame.structPointer,
                timeout);

        DevolayFrameType frameType = DevolayFrameType.valueOf(type);
        if (frameType == DevolayFrameType.METADATA && metadataFrame != null) {
            metadataFrame.allocatedBufferSource.set(this);
        }
        return frameType;
    }

    /**
     * Queries the current talley state of this sender (if it is on the main program, or preview).
     *
     * By setting timeoutMs to 0, this function will immediately query the state of the sender, but by specifying a timeout,
     * this function blocks to wait for a change in the talley to occur.
     *
     * This function will return null if the talley times out.
     *
     * @param timeoutMs The timeout in milliseconds.
     * @return The updated talley, or {@code null} if the talley timed out.
     */
    public DevolayTally getTally(int timeoutMs) {
        byte tallyResult = getTally(ndilibSendInstancePointer, timeoutMs);
        if(tallyResult == (byte)-1) {
            return null;
        } else {
            return new DevolayTally((tallyResult & 1) == 1, ((tallyResult & 0xFF) & (1 << 1)) == (1 << 1));
        }
    }

    /**
     * Queries the number of receivers currently connected to the source.
     *
     * This can be used to stop rendering frames when nothing is actually connected to the video source, which can greatly improve network and compute performance.
     *
     * By setting timeoutMs to 0, this function will immediately return the number of connections, but by specifying a timeout,
     * this function blocks to wait until the number of connections is non-zero before returning.
     *
     * This function will still return 0 if the query times out.
     *
     * @param timeoutMs The timeout in milliseconds.
     * @return The number of receivers connected to this sender.
     */
    public int getConnectionCount(int timeoutMs) {
        return getNoConnections(ndilibSendInstancePointer, timeoutMs);
    }

    /**
     * Clears all connection-based metadata that is send automatically each time a new connection is opened.
     */
    public void clearConnectionMetadata() {
        clearConnectionMetadata(ndilibSendInstancePointer);
    }

    /**
     * Adds a frame of metadata to be sent each time a new connection is opened. If a receiver is already connected
     * to this source, this metadata frame will be immediately sent to it.
     *
     * @param frame The frame of metadata to add to the connection.
     */
    public void addConnectionMetadata(DevolayMetadataFrame frame) {
        addConnectionMetadata(ndilibSendInstancePointer, frame.structPointer);
    }

    /**
     * Assign a new failover source to use for this sender. If this video source fails, any receivers will automatically
     * switch over to the specified failover source.
     *
     * @param source A failover source to use, or {@code null} to remove any failover sources.
     */
    public void setFailoverSource(DevolaySource source) {
        setFailover(ndilibSendInstancePointer, source == null ? 0 : source.structPointer);
    }

    /**
     * Creates a COPY of the {@link DevolaySource} struct for this sender.
     *
     * @return A copy of the {@link DevolaySource} this sender is for.
     */
    public DevolaySource getSource() {
        return new DevolaySource(getSource(ndilibSendInstancePointer));
    }

    @Override
    public void close() {
        // TODO: Auto-clean resources.
        sendDestroy(ndilibSendInstancePointer);
    }

    @Override
    void freeVideo(DevolayVideoFrame videoFrame) {
        throw new UnsupportedOperationException("Cannot free DevolayVideoFrame using DevolaySender. This should be unreachable, please open an issue at https://github.com/WalkerKnapp/devolay/issues.");
    }

    @Override
    void freeAudio(DevolayAudioFrame audioFrame) {
        throw new UnsupportedOperationException("Cannot free DevolayAudioFrame using DevolaySender. This should be unreachable, please open an issue at https://github.com/WalkerKnapp/devolay/issues.");
    }

    @Override
    void freeMetadata(DevolayMetadataFrame metadataFrame) {
        freeMetadata(ndilibSendInstancePointer, metadataFrame.structPointer);
    }

    // Native Methods

    private static native long sendCreate(String ndiName, String groups, boolean clockVideo, boolean clockAudio);
    // Should offer a small performance/memory improvement over using #devolaySendCreate with the default values.
    private static native long sendCreateDefaultSettings();
    private static native void sendDestroy(long sendInstance);

    private static native void sendVideoV2(long sendInstance, long videoFrameInstance);
    private static native void sendVideoAsyncV2(long sendInstance, long videoFrameInstance);

    private static native void sendAudioV2(long sendInstance, long audioFrameInstance);
    private static native void sendAudioInterleaved16s(long sendInstance, long audioFrameInstance);
    private static native void sendAudioInterleaved32s(long sendInstance, long audioFrameInstance);
    private static native void sendAudioInterleaved32f(long sendInstance, long audioFrameInstance);

    private static native void sendMetadata(long sendInstance, long metadataFrameInstance);

    private static native int sendCapture(long sendInstance, long metadataFrame, int timeout);
    private static native void freeMetadata(long structPointer, long pMetadata);

    private static native byte getTally(long sendInstance, int timeoutMs);

    private static native int getNoConnections(long sendInstance, int timeoutMs);

    private static native void clearConnectionMetadata(long sendInstance);
    private static native void addConnectionMetadata(long sendInstance, long metadataFrameInstance);

    private static native void setFailover(long sendInstance, long failoverSourceInstance);

    private static native long getSource(long sendInstance);

}
