package com.walker.devolay;

import java.nio.ByteBuffer;

public class DevolayUtilities {
    /**
     * Reads interleaved floating-point audio data from interleavedData, and writes sequential planes to the planarData buffer.
     *
     * interleavedData is formatted with one sample for each channel in sequence, as in (for 4 frames, 2 channels):
     * aa bb aa bb aa bb aa bb
     *
     * planarData is formatted with sequential planes for each channel, as in (for 4 frames, 2 channels):
     * aa aa aa aa bb bb bb bb
     *
     * @param interleavedData A ByteBuffer of interleaved floating-point samples to convert.
     * @param planarData A ByteBuffer to write planar floating-point samples to.
     * @param frames The total number of frames of audio
     * @param channels The number of channels/planes of data in the audio.
     */
    public static void interleavedFloatToPlanarFloat(ByteBuffer interleavedData, ByteBuffer planarData, int frames, int channels) {
        // No conversion is needed when there is one channel.
        if (channels == 1) {
            planarData.put(interleavedData);
            return;
        }

        // Set the initial offsets of each plane
        int[] planePos = new int[channels];
        // Skip the first plane, as it will always be at position 0
        for(int i = 1; i < channels; i++) {
            planePos[i] = i * frames * Float.BYTES;
        }

        // Copy all frames
        for (int f = 0; f < frames; f++) {
            for(int i = 0; i < channels; i++) {
                planarData.putFloat(planePos[i], interleavedData.getFloat());
                planePos[i] += Float.BYTES;
            }
        }
    }

    /**
     * Reads interleaved floating-point audio data from interleavedData, and writes sequential planes to the planarData buffer.
     *
     * interleavedData is formatted with one sample for each channel in sequence, as in (for 4 frames, 2 channels):
     * aa bb aa bb aa bb aa bb
     *
     * planarData is formatted with sequential planes for each channel, as in (for 4 frames, 2 channels):
     * aa aa aa aa bb bb bb bb
     *
     * Infers the frame count from the size of interleavedData
     *
     * @param interleavedData A ByteBuffer of interleaved floating-point samples to convert.
     * @param planarData A ByteBuffer to write planar floating-point samples to.
     * @param channels The number of channels/planes of data in the audio.
     */
    public static void interleavedFloatToPlanarFloat(ByteBuffer interleavedData, ByteBuffer planarData, int channels) {
        interleavedFloatToPlanarFloat(interleavedData, planarData, interleavedData.remaining() / (channels * Float.BYTES), channels);
    }

    /**
     * Reads planar floating-point audio data from planarData, and writes interleaved samples to the interleavedData buffer.
     *
     * planarData is formatted with sequential planes for each channel, as in (for 4 frames, 2 channels):
     * aa aa aa aa bb bb bb bb
     *
     * interleavedData is formatted with one sample for each channel in sequence, as in (for 4 frames, 2 channels):
     * aa bb aa bb aa bb aa bb
     *
     * @param planarData A ByteBuffer to of planes of floating-point samples to convert.
     * @param interleavedData A ByteBuffer to write interleaved floating-point samples to.
     * @param frames The total number of frames of audio
     * @param channels The number of channels/planes of data in the audio.
     */
    public static void planarFloatToInterleavedFloat(ByteBuffer planarData, ByteBuffer interleavedData, int frames, int channels) {
        // No conversion is needed when there is one channel
        if (channels == 1) {
            interleavedData.put(planarData);
            return;
        }

        // Compute the offset each plane should be in the input data
        int[] planePos = new int[channels];
        // Skip the first plane, as it will always be at position 0
        for(int i = 1; i < channels; i++) {
            planePos[i] = i * frames * Float.BYTES;
        }

        // Copy all frames
        for (int f = 0; f < frames; f++) {
            for(int i = 0; i < channels; i++) {
                interleavedData.putFloat(planarData.getFloat(planePos[f]));
                planePos[f] += Float.BYTES;
            }
        }
    }

    /**
     * Reads planar floating-point audio data from planarData, and writes interleaved samples to the interleavedData buffer.
     *
     * planarData is formatted with sequential planes for each channel, as in (for 4 frames, 2 channels):
     * aa aa aa aa bb bb bb bb
     *
     * interleavedData is formatted with one sample for each channel in sequence, as in (for 4 frames, 2 channels):
     * aa bb aa bb aa bb aa bb
     *
     * Infers the frame count from the size of planarData
     *
     * @param planarData A ByteBuffer to of planes of floating-point samples to convert.
     * @param interleavedData A ByteBuffer to write interleaved floating-point samples to.
     * @param channels The number of channels/planes of data in the audio.
     */
    public static void planarFloatToInterleavedFloat(ByteBuffer planarData, ByteBuffer interleavedData, int channels) {
        planarFloatToInterleavedFloat(planarData, interleavedData, planarData.remaining() / (channels * Float.BYTES), channels);
    }
}
