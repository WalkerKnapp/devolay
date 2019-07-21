package com.walker.devolay;

public enum DevolayFrameFourCCType {
    // YCbCr color space
    NDIlib_FourCC_type_UYVY('U', 'Y', 'V', 'Y'),

    // 4:2:0 formats
    NDIlib_FourCC_type_YV12('Y', 'V', '1', '2'),
    NDIlib_FourCC_type_NV12('N', 'V', '1', '2'),
    NDIlib_FourCC_type_I420('I', '4', '2', '0'),

    // BGRA
    NDIlib_FourCC_type_BGRA('B', 'G', 'R', 'A'),
    NDIlib_FourCC_type_BGRX('B', 'G', 'R', 'X'),

    // RGBA
    NDIlib_FourCC_type_RGBA('R', 'G', 'B', 'A'),
    NDIlib_FourCC_type_RGBX('R', 'G', 'B', 'X'),

    // This is a UYVY buffer followed immediately by an alpha channel buffer.
    // If the stride of the YCbCr component is "stride", then the alpha channel
    // starts at image_ptr + yres*stride. The alpha channel stride is stride/2.
    NDIlib_FourCC_type_UYVA('U', 'Y', 'V', 'A');

    int id;

    DevolayFrameFourCCType(char c1, char c2, char c3, char c4) {
        id = (c1 & 0xFF) | ((c2 & 0xFF) << 8) | ((c3 & 0xFF) << 16) | ((c4 & 0xFF) << 24);
    }
}
