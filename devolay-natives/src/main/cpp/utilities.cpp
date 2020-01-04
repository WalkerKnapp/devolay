#include "devolay.h"

#include "com_walker_devolay_DevolayUtilities.h"

void Java_com_walker_devolay_DevolayUtilities_convertToInterleaved16s(JNIEnv *env, jclass jClazz, jlong pSourceFrame, jlong pTargetFrame) {
    getNDILib()->NDIlib_util_audio_to_interleaved_16s_v2(reinterpret_cast<NDIlib_audio_frame_v2_t *>(pSourceFrame),
                                                         reinterpret_cast<NDIlib_audio_frame_interleaved_16s_t *>(pTargetFrame));
}

void Java_com_walker_devolay_DevolayUtilities_convertFromInterleaved16s(JNIEnv *env, jclass jClazz, jlong pSourceFrame, jlong pTargetFrame) {
    getNDILib()->NDIlib_util_audio_from_interleaved_16s_v2(reinterpret_cast<NDIlib_audio_frame_interleaved_16s_t *>(pSourceFrame),
                                                        reinterpret_cast<NDIlib_audio_frame_v2_t *>(pTargetFrame));
}

void Java_com_walker_devolay_DevolayUtilities_convertToInterleaved32s(JNIEnv *env, jclass jClazz, jlong pSourceFrame, jlong pTargetFrame) {
    getNDILib()->NDIlib_util_audio_to_interleaved_32s_v2(reinterpret_cast<NDIlib_audio_frame_v2_t *>(pSourceFrame),
                                                         reinterpret_cast<NDIlib_audio_frame_interleaved_32s_t *>(pTargetFrame));
}

void Java_com_walker_devolay_DevolayUtilities_convertFromInterleaved32s(JNIEnv *env, jclass jClazz, jlong pSourceFrame, jlong pTargetFrame) {
    getNDILib()->NDIlib_util_audio_from_interleaved_32s_v2(reinterpret_cast<NDIlib_audio_frame_interleaved_32s_t *>(pSourceFrame),
                                                           reinterpret_cast<NDIlib_audio_frame_v2_t *>(pTargetFrame));
}

void Java_com_walker_devolay_DevolayUtilities_convertToInterleaved32f(JNIEnv *env, jclass jClazz, jlong pSourceFrame, jlong pTargetFrame) {
    getNDILib()->NDIlib_util_audio_to_interleaved_32f_v2(reinterpret_cast<NDIlib_audio_frame_v2_t *>(pSourceFrame),
                                                         reinterpret_cast<NDIlib_audio_frame_interleaved_32f_t *>(pTargetFrame));
}

void Java_com_walker_devolay_DevolayUtilities_convertFromInterleaved32f(JNIEnv *env, jclass jClazz, jlong pSourceFrame, jlong pTargetFrame) {
    getNDILib()->NDIlib_util_audio_from_interleaved_32f_v2(reinterpret_cast<NDIlib_audio_frame_interleaved_32f_t *>(pSourceFrame),
                                                           reinterpret_cast<NDIlib_audio_frame_v2_t *>(pTargetFrame));
}
