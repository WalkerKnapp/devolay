package com.walker.devolay;

public enum DevolayFrameFormatType {
    /**
     * A progressive frame
     */
    PROGRESSIVE(1),

    /**
     * A fielded frame with the field 0 being on the even lines and field 1 being on the odd lines
     */
    INTERLEAVED(0),

    /**
     * Individual fields
     */
    FIELD_0(2),
    FIELD_1(3);

    int id;

    DevolayFrameFormatType(int id) {
        this.id = id;
    }
}
