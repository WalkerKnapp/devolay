package com.walker.devolay;

public class DevolayReceiver extends DevolayFrameCleaner implements AutoCloseable {
    // Receive only metadata
    public static final int RECEIVE_BANDWIDTH_METADATA_ONLY = -10;
    // Receive only audio and metadata
    public static final int RECEIVE_BANDWIDTH_AUDIO_ONLY = 10;
    // Receive metadata, audio, and video at a lower bandwidth and resolution
    public static final int RECEIVE_BANDWIDTH_LOWEST = 0;
    // Receive metadata, audio, and video at full resolution
    public static final int RECEIVE_BANDWIDTH_HIGHEST = 100;

    public enum ColorFormat {
        /**
         * When there is an alpha channel, BGRA, otherwise BGRX
         */
        BGRX_BGRA(0),
        /**
         * When there is an alpha channel, BGRA, otherwise UYVY
         */
        UYVY_BGRA(1),
        /**
         * When there is an alpha channel, RGBA, otherwise BGRX
         */
        RGBX_RGBA(2),
        /**
         * When there is an alpha channel, RGBA, otherwise UYVY
         */
        UYVY_RGBA(3),
        /**
         * Use the fastest available color format for the incoming video signal.
         * Different platforms may vary in what format this will choose.
         *
         * When using this format, allow_video_fields is true, and a source supplies fields, individual fields will always be delivered.
         *
         * For most video sources on most platforms, the following will be used:
         *      No alpha channel: UYVY
         *      Alpha channel: UYVA
         */
        FASTEST(100),
        /**
         * Use the format that is closest to native for the incoming codec for the best quality.
         * Allows for receiving on 16bpp color from many sources
         *
         * When using this format, allow_video_fields is true, and a source supplies fields, individual fields will always be delivered.
         *
         * For most video sources on most platforms, the following will be used:
         *      No alpha channel: P216 or UYVY
         *      Alpha channel: PA16 or UYVA
         */
        BEST(101);

        private int id;

        ColorFormat(int id) {
            this.id = id;
        }
    }

    /**
     * Holds the reference to the NDIlib_send_instance_t object
     */
    final long ndilibRecievePointer;

    public DevolayReceiver(DevolaySource source, ColorFormat colorFormat, int receiveBandwidth, boolean allowVideoFields, String name) {
        // TODO: Implement this forced reference more effectively
        Devolay.loadLibraries();

        this.ndilibRecievePointer = receiveCreate(source.structPointer, colorFormat.id, receiveBandwidth, allowVideoFields, name);
    }

    public DevolayReceiver(DevolaySource source) {
        this(source, ColorFormat.UYVY_BGRA, RECEIVE_BANDWIDTH_HIGHEST, true, null);
    }

    public DevolayReceiver(ColorFormat colorFormat, int receiveBandwidth, boolean allowVideoFields, String name) {
        // TODO: Implement this forced reference more effectively
        Devolay.loadLibraries();

        this.ndilibRecievePointer = receiveCreate(0L, colorFormat.id, receiveBandwidth, allowVideoFields, name);
    }

    public DevolayReceiver() {
        // TODO: Implement this forced reference more effectively
        Devolay.loadLibraries();

        this.ndilibRecievePointer = receiveCreateDefaultSettings();
    }

    /**
     * Changes the connection to another video source, or disconnects it by passing null.
     * Allows you to preserve a receiver instead of creating new ones.
     *
     * @param source The source to connect to, or null
     */
    public void connect(DevolaySource source) {
        receiveConnect(ndilibRecievePointer, source.structPointer);
    }

    /**
     * Allows receiving of video, audio, and metadata frames.
     * Any of the frames can be replaced with null, data will not be captured for those types in this call.
     * This call can be called simultaneously on separate threads to receive audio, video, and metadata separately.
     *
     * This will return DevolayFrameType#NONE if no data is received in the timeout span.
     * This will return DevolayFrameType#ERROR if the connection is lost.
     *
     * Frames that are filled with data MUST be later manually cleared, with calling #freeBuffer, #close, or with a try-with-resources
     *
     * This method will free any previously allocated data from all frames, even if data is not written to a given frame.
     *
     * @param videoFrame A video frame to put data into. Any existing data will be replaced. If null, video will not be captured.
     * @param audioFrame A audio frame to put data into. Any existing data will be replaced. If null, audio will not be captured.
     * @param metadataFrame A metadata frame to put data into. Any existing data will be replaced. If null, metadata will not be captured.
     * @param timeout A timeout in milliseconds for the capture
     *
     * @return The frame type that was captured, DevolayFrameType#NONE, or DevolayFrameType#ERROR
     */
    public DevolayFrameType receiveCapture(DevolayVideoFrame videoFrame, DevolayAudioFrame audioFrame, DevolayMetadataFrame metadataFrame, int timeout) {
        if(videoFrame != null) {
            videoFrame.freeBuffer();
        }
        if(audioFrame != null) {
            audioFrame.freeBuffer();
        }
        if(metadataFrame != null) {
            metadataFrame.freeBuffer();
        }

        int type = receiveCaptureV2(ndilibRecievePointer,
                videoFrame == null ? 0L : videoFrame.structPointer,
                audioFrame == null ? 0L : audioFrame.structPointer,
                metadataFrame == null ? 0L : metadataFrame.structPointer,
                timeout);

        DevolayFrameType frameType = DevolayFrameType.valueOf(type);

        if(frameType == DevolayFrameType.VIDEO && videoFrame != null) {
            videoFrame.allocatedBufferSource.set(this);
        } else if (frameType == DevolayFrameType.AUDIO && audioFrame != null) {
            audioFrame.allocatedBufferSource.set(this);
        } else if (frameType == DevolayFrameType.METADATA && metadataFrame != null) {
            metadataFrame.allocatedBufferSource.set(this);
        }

        return frameType;
    }

    /**
     * Sends an up-stream metadata frame to the currently connected source.
     *
     * @param metadataFrame The frame to send to the source.
     * @return true if a source is connected, and false if this receiver is disconnected.
     */
    public boolean sendMetadata(DevolayMetadataFrame metadataFrame) {
        return receiveSendMetadata(ndilibRecievePointer, metadataFrame.structPointer);
    }

    // TODO: NDIlib_recv_set_tally

    /**
     * Fills in a DevolayPerformanceData structure with performance information about the receiver.
     * This can be useful to tell if the processing of data is keeping up with real-time.
     *
     * @param performanceData The performance structure to fill
     */
    public void queryPerformance(DevolayPerformanceData performanceData) {
        receiveGetPerformance(ndilibRecievePointer,
                performanceData.totalPerformanceStructPointer,
                performanceData.droppedPerformanceStructPointer);
    }

    // TODO: NDIlib_recv_get_queue

    /**
     * Connection based metadata (send automatically when a new connection is received) is cleared.
     */
    public void clearConnectionMetadata() {
        receiveClearConnectionMetadata(ndilibRecievePointer);
    }

    /**
     * Adds a metadata frame to send on each new connection. If there is already a connection, this is sent immediately.
     *
     * @param frame The metadata frame to send
     */
    public void addConnectionMetadata(DevolayMetadataFrame frame) {
        receiveAddConnectionMetadata(ndilibRecievePointer, frame.structPointer);
    }

    /**
     * Gets the number of connections to this receiver. This will normally return 0 or 1, for either connected or not connected.
     *
     * @return The amount of connections.
     */
    public int getConnectionCount() {
        return receiveGetNoConnections(ndilibRecievePointer);
    }

    /**
     * Gets the URL that can be used for configuration of this input.
     * This may return null if there is no web interface.
     * The return otherwise will be a fully formed URL, for instance "http://10.28.1.192/configuration/"
     * To avoid the need to poll this function, you can monitor #receiveCapture for a DevolayFrameType.STATUS_CHANGE
     *
     * @return The URL to the web interface for this source. May be null.
     */
    public String getWebControl() {
        return receiveGetWebControl(ndilibRecievePointer);
    }

    @Override
    public void close() {
        // TODO: Auto-clean resources.
        receiveDestroy(ndilibRecievePointer);
    }

    @Override
    void freeVideo(DevolayVideoFrame frame) {
        freeVideoV2(ndilibRecievePointer, frame.structPointer);
    }

    @Override
    void freeAudio(DevolayAudioFrame frame) {
        freeAudioV2(ndilibRecievePointer, frame.structPointer);
    }

    @Override
    public void freeMetadata(DevolayMetadataFrame frame) {
        freeMetadata(ndilibRecievePointer, frame.structPointer);
    }

    // Native methods
    private static native long receiveCreate(long pSource, int colorFormat, int receiveBandwidth, boolean allowVideoFields, String name);
    private static native long receiveCreateDefaultSettings();
    private static native void receiveDestroy(long structPointer);

    private static native void receiveConnect(long structPointer, long pSource);

    private static native int receiveCaptureV2(long structPointer, long pVideoFrame, long pAudioFrame, long pMetadataFrame, int timeout);
    private static native void freeVideoV2(long structPointer, long pVideoFrame);
    private static native void freeAudioV2(long structPointer, long pAudioFrame);
    private static native void freeMetadata(long structPointer, long pMetadata);
    private static native boolean receiveSendMetadata(long structPointer, long pMetadataFrame);

    private static native void receiveGetPerformance(long structPointer, long pTotalPerformance, long pDroppedPerformance);

    private static native void receiveClearConnectionMetadata(long structPointer);
    private static native void receiveAddConnectionMetadata(long structPointer, long pMetadataFrame);

    public static native int receiveGetNoConnections(long structPointer);
    public static native String receiveGetWebControl(long structPointer);
}
