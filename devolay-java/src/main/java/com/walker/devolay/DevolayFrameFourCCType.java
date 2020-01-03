package com.walker.devolay;

public enum DevolayFrameFourCCType {
    // YCbCr color space
    UYVY('U', 'Y', 'V', 'Y'),

    // 4:2:0 formats
    YV12('Y', 'V', '1', '2'),
    NV12('N', 'V', '1', '2'),
    I420('I', '4', '2', '0'),

    // BGRA
    BGRA('B', 'G', 'R', 'A'),
    BGRX('B', 'G', 'R', 'X'),

    // RGBA
    RGBA('R', 'G', 'B', 'A'),
    RGBX('R', 'G', 'B', 'X'),

    // This is a UYVY buffer followed immediately by an alpha channel buffer.
    // If the stride of the YCbCr component is "stride", then the alpha channel
    // starts at image_ptr + yres*stride. The alpha channel stride is stride/2.
    UYVA('U', 'Y', 'V', 'A');

    public int id;

    DevolayFrameFourCCType(char c1, char c2, char c3, char c4) {
        id = (c1 & 0xFF) | ((c2 & 0xFF) << 8) | ((c3 & 0xFF) << 16) | ((c4 & 0xFF) << 24);
    }

    public static DevolayFrameFourCCType valueOf(int id) {
        if(id == UYVY.id) {
            return UYVY;
        } else if (id == YV12.id) {
            return YV12;
        } else if (id == NV12.id) {
            return NV12;
        } else if (id == I420.id) {
            return I420;
        } else if (id == BGRA.id) {
            return BGRA;
        } else if (id == BGRX.id) {
            return BGRX;
        } else if (id == RGBA.id) {
            return RGBA;
        } else if (id == RGBX.id) {
            return RGBX;
        } else if (id == UYVA.id) {
            return UYVA;
        } else {
            return null;
        }
    }
}
