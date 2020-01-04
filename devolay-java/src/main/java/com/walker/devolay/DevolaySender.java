package com.walker.devolay;

/**
 * Equivalent to NDIlib_send_instance_t
 */
public class DevolaySender implements AutoCloseable {

    /**
     * Holds the reference to the NDIlib_send_instance_t object
     */
    private final long ndilibSendInstancePointer;

    /**
     * Creates a NDIlib_send_create_t instance, and uses it to initialize the internal NDIlib_send_instance_t object.
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

    public DevolaySender(String ndiName, String groups) {
        this(ndiName, groups, true, true);
    }

    public DevolaySender(String ndiName) {
        this(ndiName, null, true, true);
    }

    public DevolaySender() {
        this.ndilibSendInstancePointer = sendCreateDefaultSettings();
    }

    public void sendVideoFrame(DevolayVideoFrame frame) {
        sendVideoV2(ndilibSendInstancePointer, frame.structPointer);
    }

    public void sendVideoFrameAsync(DevolayVideoFrame frame) {
        sendVideoAsyncV2(ndilibSendInstancePointer, frame.structPointer);
    }

    public void sendAudioFrame(DevolayAudioFrame frame) {
        sendAudioV2(ndilibSendInstancePointer, frame.structPointer);
    }

    public void sendAudioFrameInterleaved16s(DevolayAudioFrameInterleaved16s frame) {
        sendAudioInterleaved16s(ndilibSendInstancePointer, frame.structPointer);
    }

    public void sendAudioFrameInterleaved32s(DevolayAudioFrameInterleaved32s frame) {
        sendAudioInterleaved32s(ndilibSendInstancePointer, frame.structPointer);
    }

    public void sendAudioFrameInterleaved32f(DevolayAudioFrameInterleaved32f frame) {
        sendAudioInterleaved32f(ndilibSendInstancePointer, frame.structPointer);
    }

    public void sendMetadataFrame(DevolayMetadataFrame frame) {
        sendMetadata(ndilibSendInstancePointer, frame.structPointer);
    }

    public DevolayTally getTally(int timeoutMs) {
        byte tallyResult = getTally(ndilibSendInstancePointer, timeoutMs);
        if(tallyResult == (byte)-1) {
            return null;
        } else {
            return new DevolayTally((tallyResult & 1) == 1, ((tallyResult & 0xFF) & (1 << 1)) == (1 << 1));
        }
    }

    public int getConnectionCount(int timeoutMs) {
        return getNoConnections(ndilibSendInstancePointer, timeoutMs);
    }

    public void clearConnectionMetadata() {
        clearConnectionMetadata(ndilibSendInstancePointer);
    }

    public void addConnectionMetadata(DevolayMetadataFrame frame) {
        addConnectionMetadata(ndilibSendInstancePointer, frame.structPointer);
    }

    public void setFailoverSource(DevolaySource source) {
        setFailover(ndilibSendInstancePointer, source.structPointer);
    }

    /**
     * Creates a COPY of the DevolaySource struct for this sender.
     *
     * @return A copy of the DevolaySource this sender is for.
     */
    public DevolaySource getSource() {
        return new DevolaySource(getSource(ndilibSendInstancePointer));
    }

    @Override
    public void close() {
        // TODO: Auto-clean resources.
        sendDestroy(ndilibSendInstancePointer);
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

    private static native byte getTally(long sendInstance, int timeoutMs);

    private static native int getNoConnections(long sendInstance, int timeoutMs);

    private static native void clearConnectionMetadata(long sendInstance);
    private static native void addConnectionMetadata(long sendInstance, long metadataFrameInstance);

    private static native void setFailover(long sendInstance, long failoverSourceInstance);

    private static native long getSource(long sendInstance);
}
