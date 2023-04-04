package me.walkerknapp.devolay;

/**
 * A structure to connect to and receive frames from {@link DevolaySource} instances.
 */
public class DevolayReceiver extends DevolayFrameCleaner implements AutoCloseable {
    /**
     * A bandwidth mode to receive only metadata from a source.
     */
    public static final int RECEIVE_BANDWIDTH_METADATA_ONLY = -10;

    /**
     * A bandwidth mode to receive only audio from a source.
     */
    public static final int RECEIVE_BANDWIDTH_AUDIO_ONLY = 10;

    /**
     * A bandwidth mode to receive at the lowest supported quality from a source
     */
    public static final int RECEIVE_BANDWIDTH_LOWEST = 0;

    /**
     * A bandwidth mode to receive at the highest supported quality from a source.
     */
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

    /**
     * Creates and allocates a receiver instance attached to a given source.
     * The source can be disconnected or changed later without recreating the instance.
     *
     * The color format selected with this constructor is only a preference, but may fail to apply on certain platforms.
     * With obscure color formats, the color format of each frame received should be checked with {@link DevolayVideoFrame#getFourCCType()}
     *
     * The bandwidth selected with this constructor will modify the compression level and resolution of the source.
     * This is useful for receiving on low-bandwidth connections, such as over WIFI.
     *
     * @param source The source to attach the receiver to.
     * @param colorFormat The color format to transcode received frames to. It is recommended to use {@link DevolayReceiver.ColorFormat#FASTEST}
     * @param receiveBandwidth The bandwidth to receive video at. This can be selected from {@link DevolayReceiver#RECEIVE_BANDWIDTH_METADATA_ONLY}, {@link DevolayReceiver#RECEIVE_BANDWIDTH_AUDIO_ONLY}, {@link DevolayReceiver#RECEIVE_BANDWIDTH_LOWEST}, or {@link DevolayReceiver#RECEIVE_BANDWIDTH_HIGHEST}.
     * @param allowVideoFields Either false to receive only progressive frames (de-interlaced in the receiver), or true to be able to receive both interlaced and progressive frames.
     * @param name The name of the receiver to create. May be null to use a generated name.
     */
    public DevolayReceiver(DevolaySource source, ColorFormat colorFormat, int receiveBandwidth, boolean allowVideoFields, String name) {
        // TODO: Implement this forced reference more effectively
        Devolay.loadLibraries();

        this.ndilibRecievePointer = receiveCreate(source.structPointer, colorFormat.id, receiveBandwidth, allowVideoFields, name);
    }

    /**
     * Creates and allocates a receiver instance attached to a given source.
     * The source can be disconnected or changed later without recreating the instance.
     *
     * The receiver will receive the {@link ColorFormat#UYVY_BGRA} color format, using the {@link DevolayReceiver#RECEIVE_BANDWIDTH_HIGHEST} bandwidth,
     * allows fielded video, and uses a generated name.
     *
     * @param source The source to attach the receiver to.
     */
    public DevolayReceiver(DevolaySource source) {
        this(source, ColorFormat.UYVY_BGRA, RECEIVE_BANDWIDTH_HIGHEST, true, null);
    }

    /**
     * Creates and allocates a receiver that is not connected to a source.
     *
     * @param colorFormat The color format to transcode received frames to. It is recommended to use {@link DevolayReceiver.ColorFormat#FASTEST}
     * @param receiveBandwidth The bandwidth to receive video at. This can be selected from {@link DevolayReceiver#RECEIVE_BANDWIDTH_METADATA_ONLY}, {@link DevolayReceiver#RECEIVE_BANDWIDTH_AUDIO_ONLY}, {@link DevolayReceiver#RECEIVE_BANDWIDTH_LOWEST}, or {@link DevolayReceiver#RECEIVE_BANDWIDTH_HIGHEST}.
     * @param allowVideoFields Either false to receive only progressive frames (de-interlaced in the receiver), or true to be able to receive both interlaced and progressive frames.
     * @param name The name of the receiver to create. May be null to use a generated name.
     */
    public DevolayReceiver(ColorFormat colorFormat, int receiveBandwidth, boolean allowVideoFields, String name) {
        // TODO: Implement this forced reference more effectively
        Devolay.loadLibraries();

        this.ndilibRecievePointer = receiveCreate(0L, colorFormat.id, receiveBandwidth, allowVideoFields, name);
    }

    /**
     * Creates and allocates a receiver that is not connected to a source.
     *
     * The receiver will receive the {@link ColorFormat#UYVY_BGRA} color format, using the {@link DevolayReceiver#RECEIVE_BANDWIDTH_HIGHEST} bandwidth,
     * allows fielded video, and uses a generated name.
     */
    public DevolayReceiver() {
        // TODO: Implement this forced reference more effectively
        Devolay.loadLibraries();

        this.ndilibRecievePointer = receiveCreateDefaultSettings();
    }

    /**
     * Changes the connection to another video source, or disconnects it by passing null.
     * Allows you to preserve a receiver instead of creating new ones.
     *
     * @param source The source to connect to, or null to disconnect.
     */
    public void connect(DevolaySource source) {
        receiveConnect(ndilibRecievePointer, source == null ? 0L : source.structPointer);
    }

    /**
     * Allows receiving of video, audio, and metadata frames.
     * Any of the frames can be replaced with null, data will not be captured for those types in this call.
     * This call can be called simultaneously on separate threads to receive audio, video, and metadata separately.
     *
     * This will return {@link DevolayFrameType#NONE} if no data is received in the timeout span.
     * This will return {@link DevolayFrameType#ERROR} if the connection is lost.
     *
     * Frames that are filled with data MUST be later manually cleared, with calling {@link DevolayVideoFrame#freeBuffer()}, {@link DevolayVideoFrame#close}, or with a try-with-resources.
     *
     * This method will free any previously allocated data from all frames, even if data is not written to a given frame.
     *
     * @param videoFrame A video frame to put data into. Any existing data will be replaced. If null, video will not be captured.
     * @param audioFrame A audio frame to put data into. Any existing data will be replaced. If null, audio will not be captured.
     * @param metadataFrame A metadata frame to put data into. Any existing data will be replaced. If null, metadata will not be captured.
     * @param timeout A timeout in milliseconds for the capture
     *
     * @return The frame type that was captured, {@link DevolayFrameType#NONE}, or {@link DevolayFrameType#ERROR}
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

    /**
     * Set tally state on the source
     *
     * @param tally The {@link DevolayTally} state to send.
     * @return true if a source is connected, false if the receiver is disconnected.
     */
    public boolean setTally(DevolayTally tally) {
        return receiveSetTally(ndilibRecievePointer, tally.isOnProgram(), tally.isOnPreview());
    }

    /**
     * Fills in a {@link DevolayPerformanceData} structure with performance information about the receiver.
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
     * Clear all connection based metadata (send automatically when a new connection is created).
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
     * To avoid the need to poll this function, you can monitor {@link DevolayReceiver#receiveCapture(DevolayVideoFrame, DevolayAudioFrame, DevolayMetadataFrame, int)} for a {@link DevolayFrameType#STATUS_CHANGE}
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
    private static native boolean receiveSetTally(long structPointer, boolean isOnProgram, boolean isOnPreview);

    private static native void receiveGetPerformance(long structPointer, long pTotalPerformance, long pDroppedPerformance);

    private static native void receiveClearConnectionMetadata(long structPointer);
    private static native void receiveAddConnectionMetadata(long structPointer, long pMetadataFrame);

    public static native int receiveGetNoConnections(long structPointer);
    public static native String receiveGetWebControl(long structPointer);
}
