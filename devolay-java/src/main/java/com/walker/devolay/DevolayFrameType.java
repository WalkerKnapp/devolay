package com.walker.devolay;

public enum DevolayFrameType {
    NONE(0),
    VIDEO(1),
    AUDIO(2),
    METADATA(3),
    ERROR(4),

    /**
     * Indicates that the settings on the input have changed.
     * This will be returned from DevolayReceiver#receiveCapture when a device is known
     * to have new settings, for instance the web URL has changed or the device is now
     * a PTZ camera.
     */
    STATUS_CHANGE(100);

    final int id;

    DevolayFrameType(int id) {
        this.id = id;
    }

    public static DevolayFrameType valueOf(int id) {
        switch (id) {
            case 0: return NONE;
            case 1: return VIDEO;
            case 2: return AUDIO;
            case 3: return METADATA;
            case 4: return ERROR;
            case 100: return STATUS_CHANGE;
            default: throw new IllegalArgumentException("Unknown frame type id: " + id);
        }
    }
}
