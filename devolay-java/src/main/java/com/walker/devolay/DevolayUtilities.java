package com.walker.devolay;

public class DevolayUtilities {

    /**
     * Convert from the standard planar floating-point audio to interleaved 16s audio.
     *
     * @param srcFrame The frame to take input floating-point data from
     * @param targetFrame The frame to write output interleaved data to.
     */
    public static void planarFloatToInterleaved16s(DevolayAudioFrame srcFrame, DevolayAudioFrameInterleaved16s targetFrame) {
        convertToInterleaved16s(srcFrame.structPointer, targetFrame.structPointer);
    }

    /**
     * Convert from interleaved 16s audio to the standard floating-point audio.
     *
     * @param srcFrame The frame to take interleaved data from.
     * @param targetFrame The frame to write output floating-point data to.
     */
    public static void interleaved16sToPlanarFloat(DevolayAudioFrameInterleaved16s srcFrame, DevolayAudioFrame targetFrame) {
        targetFrame.freeBuffer();
        convertFromInterleaved16s(srcFrame.structPointer, targetFrame.structPointer);
    }

    /**
     * Convert from the standard planar floating-point audio to interleaved 32s audio.
     *
     * @param srcFrame The frame to take input floating-point data from.
     * @param targetFrame The frame to write output interleaved data to.
     */
    public static void planarFloatToInterleaved32s(DevolayAudioFrame srcFrame, DevolayAudioFrameInterleaved32s targetFrame) {
        convertToInterleaved32s(srcFrame.structPointer, targetFrame.structPointer);
    }

    /**
     * Convert from interleaved 32s audio data to the standard planar floating-point audio.
     *
     * @param srcFrame The frame to take input interleaved data from.
     * @param targetFrame The frame to write output floating-point data to.
     */
    public static void interleaved32sToPlanarFloat(DevolayAudioFrameInterleaved32s srcFrame, DevolayAudioFrame targetFrame) {
        targetFrame.freeBuffer();
        convertFromInterleaved32s(srcFrame.structPointer, targetFrame.structPointer);
    }

    /**
     * Convert from the standard floating-point audio data to interleaved floating-point audio data.
     *
     * @param srcFrame The frame to take input floating-point data from.
     * @param targetFrame The frame to write output interleaved floating-point data to.
     */
    public static void planarFloatToInterleavedFloat(DevolayAudioFrame srcFrame, DevolayAudioFrameInterleaved32f targetFrame) {
        convertToInterleaved32f(srcFrame.structPointer, targetFrame.structPointer);
    }

    /**
     * Convert from interleaved floating-point audio data to the standard floating-point audio data.
     *
     * @param srcFrame The frame to take input interleaved floating-point data from.
     * @param targetFrame The frame to write output floating-point data to.
     */
    public static void interleavedFloatToPlanarFloat(DevolayAudioFrameInterleaved32f srcFrame, DevolayAudioFrame targetFrame) {
        targetFrame.freeBuffer();
        convertFromInterleaved32f(srcFrame.structPointer, targetFrame.structPointer);
    }

    // Native methods
    private static native void convertToInterleaved16s(long pSrcFrame, long pDstFrame);
    private static native void convertFromInterleaved16s(long pSrcFrame, long pDstFrame);
    private static native void convertToInterleaved32s(long pSrcFrame, long pDstFrame);
    private static native void convertFromInterleaved32s(long pSrcFrame, long pDstFrame);
    private static native void convertToInterleaved32f(long pSrcFrame, long pDstFrame);
    private static native void convertFromInterleaved32f(long pSrcFrame, long pDstFrame);
}
