#include "devolay.h"

#include "com_walker_devolay_DevolayFramesync.h"

jlong JNICALL Java_com_walker_devolay_DevolayFramesync_framesyncCreate(JNIEnv *env, jclass jClazz, jlong pReceiver) {
    return (jlong) getNDILib()->NDIlib_framesync_create(reinterpret_cast<NDIlib_recv_instance_t *>(pReceiver));
}

void JNICALL Java_com_walker_devolay_DevolayFramesync_framesyncDestroy(JNIEnv *env, jclass jClazz, jlong pFramesync) {
    getNDILib()->NDIlib_framesync_destroy(reinterpret_cast<NDIlib_framesync_instance_t *>(pFramesync));
}

void JNICALL Java_com_walker_devolay_DevolayFramesync_framesyncCaptureAudio
(JNIEnv *env, jclass jClazz, jlong pFramesync, jlong pFrame, jint jSampleRate, jint jNoChannels, jint jNoSamples) {
    getNDILib()->NDIlib_framesync_capture_audio(reinterpret_cast<NDIlib_framesync_instance_t *>(pFramesync),
            reinterpret_cast<NDIlib_audio_frame_v2_t *>(pFrame),
            jSampleRate, jNoChannels, jNoSamples);
}

// For some reason, this isn't implemented in the DynamicLoad spec for NDI. It may be added in a future version
/*jint JNICALL Java_com_walker_devolay_DevolayFramesync_framesyncAudioQueueDepth
        (JNIEnv *env, jclass jClazz, jlong pFramesync) {
    return getNDILib()->NDIlib_framesync_audio_queue_depth(reinterpret_cast<NDIlib_framesync_instance_t *>(pFramesync));
}*/

void JNICALL Java_com_walker_devolay_DevolayFramesync_framesyncFreeAudio
(JNIEnv *env, jclass jClazz, jlong pFramesync, jlong pFrame) {
    getNDILib()->NDIlib_framesync_free_audio(reinterpret_cast<NDIlib_framesync_instance_t *>(pFramesync),
            reinterpret_cast<NDIlib_audio_frame_v2_t *>(pFrame));
}

jboolean JNICALL Java_com_walker_devolay_DevolayFramesync_framesyncCaptureVideo
(JNIEnv *env, jclass jClazz, jlong pFramesync, jlong pFrame, jint jFrameFormatType) {
    getNDILib()->NDIlib_framesync_capture_video(reinterpret_cast<NDIlib_framesync_instance_t *>(pFramesync),
            reinterpret_cast<NDIlib_video_frame_v2_t *>(pFrame),
            (NDIlib_frame_format_type_e)jFrameFormatType);

    return reinterpret_cast<NDIlib_video_frame_v2_t *>(pFrame)->p_data != (uint8_t *)0;
}

void JNICALL Java_com_walker_devolay_DevolayFramesync_framesyncFreeVideo
(JNIEnv *env, jclass jClazz, jlong pFramesync, jlong pFrame) {
    getNDILib()->NDIlib_framesync_free_video(reinterpret_cast<NDIlib_framesync_instance_t *>(pFramesync),
            reinterpret_cast<NDIlib_video_frame_v2_t *>(pFrame));
}