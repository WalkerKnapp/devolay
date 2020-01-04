#include "devolay.h"
#include <functional>

#include "../headers/com_walker_devolay_DevolaySender.h"

jlong Java_com_walker_devolay_DevolaySender_sendCreate(JNIEnv *env, jclass jClazz, jstring jNdiName, jstring jGroups, jboolean jClockVideo, jboolean jClockAudio) {
    auto *NDI_send_create_desc = new NDIlib_send_create_t();

    auto *isCopy = new jboolean();
    *isCopy = JNI_TRUE;
    if(jNdiName != nullptr) {
        const char *name = env->GetStringUTFChars(jNdiName, isCopy);
        NDI_send_create_desc->p_ndi_name = name;
    }
    if(jGroups != nullptr) {
        const char *groups = env->GetStringUTFChars(jGroups, isCopy);
        NDI_send_create_desc->p_groups = groups;
    }
    delete isCopy;

    NDI_send_create_desc->clock_video = jClockVideo;
    NDI_send_create_desc->clock_audio = jClockAudio;

    auto ret = getNDILib()->NDIlib_send_create(NDI_send_create_desc);
    delete NDI_send_create_desc;
    return (jlong) ret;
}

jlong Java_com_walker_devolay_DevolaySender_sendCreateDefaultSettings(JNIEnv *env, jclass jClazz) {
    NDIlib_send_create_t NDI_send_create_desc;

    return reinterpret_cast<jlong>(getNDILib()->NDIlib_send_create(&NDI_send_create_desc));
}

void Java_com_walker_devolay_DevolaySender_sendDestroy(JNIEnv *env, jclass jClazz, jlong pSender) {
    getNDILib()->NDIlib_send_destroy(reinterpret_cast<NDIlib_send_instance_t>(pSender));
}

void Java_com_walker_devolay_DevolaySender_sendVideoV2(JNIEnv *env, jclass jClazz, jlong pSender, jlong pFrame) {
    getNDILib()->NDIlib_send_send_video_v2(reinterpret_cast<NDIlib_send_instance_t>(pSender),
                                      reinterpret_cast<const NDIlib_video_frame_v2_t *>(pFrame));
}

void Java_com_walker_devolay_DevolaySender_sendVideoAsyncV2(JNIEnv *env, jclass jClazz, jlong pSender, jlong pFrame) {
    getNDILib()->NDIlib_send_send_video_async_v2(reinterpret_cast<NDIlib_send_instance_t>(pSender),
                                      reinterpret_cast<const NDIlib_video_frame_v2_t *>(pFrame));
}

void Java_com_walker_devolay_DevolaySender_sendAudioV2(JNIEnv *env, jclass jClazz, jlong pSender, jlong pFrame) {
    getNDILib()->NDIlib_send_send_audio_v2(reinterpret_cast<NDIlib_send_instance_t>(pSender),
                                      reinterpret_cast<const NDIlib_audio_frame_v2_t *>(pFrame));
}

void Java_com_walker_devolay_DevolaySender_sendAudioInterleaved16s(JNIEnv *env, jclass jClazz, jlong pSender, jlong pFrame) {
    getNDILib()->NDIlib_util_send_send_audio_interleaved_16s(reinterpret_cast<NDIlib_send_instance_t>(pSender),
                                                             reinterpret_cast<const NDIlib_audio_frame_interleaved_16s_t *>(pFrame));
}

void Java_com_walker_devolay_DevolaySender_sendAudioInterleaved32s(JNIEnv *env, jclass jClazz, jlong pSender, jlong pFrame) {
    getNDILib()->NDIlib_util_send_send_audio_interleaved_32s(reinterpret_cast<NDIlib_send_instance_t>(pSender),
                                                             reinterpret_cast<const NDIlib_audio_frame_interleaved_32s_t *>(pFrame));
}

void Java_com_walker_devolay_DevolaySender_sendAudioInterleaved32f(JNIEnv *env, jclass jClazz, jlong pSender, jlong pFrame) {
    getNDILib()->NDIlib_util_send_send_audio_interleaved_32f(reinterpret_cast<NDIlib_send_instance_t>(pSender),
                                                             reinterpret_cast<const NDIlib_audio_frame_interleaved_32f_t *>(pFrame));
}

void Java_com_walker_devolay_DevolaySender_sendMetadata(JNIEnv *env, jclass jClazz, jlong pSender, jlong pFrame) {
    getNDILib()->NDIlib_send_send_metadata(reinterpret_cast<NDIlib_send_instance_t>(pSender),
                                      reinterpret_cast<const NDIlib_metadata_frame_t *>(pFrame));
}

jbyte Java_com_walker_devolay_DevolaySender_getTally(JNIEnv *env, jclass jClazz, jlong pSender, jint jTimeoutMs) {
    return 0;
}

jint Java_com_walker_devolay_DevolaySender_getNoConnections(JNIEnv *env, jclass jClazz, jlong pSender, jint jTimeoutMs) {
    return getNDILib()->NDIlib_send_get_no_connections(reinterpret_cast<NDIlib_send_instance_t>(pSender), jTimeoutMs);
}

void Java_com_walker_devolay_DevolaySender_clearConnectionMetadata(JNIEnv *env, jclass jClazz, jlong pSender) {
    getNDILib()->NDIlib_send_clear_connection_metadata(reinterpret_cast<NDIlib_send_instance_t>(pSender));
}

void Java_com_walker_devolay_DevolaySender_addConnectionMetadata(JNIEnv *env, jclass jClazz, jlong pSender, jlong pFrame) {
    getNDILib()->NDIlib_send_add_connection_metadata(reinterpret_cast<NDIlib_send_instance_t>(pSender),
                                                reinterpret_cast<const NDIlib_metadata_frame_t *>(pFrame));
}

void Java_com_walker_devolay_DevolaySender_setFailover(JNIEnv *env, jclass jClazz, jlong pSender, jlong pFailoverSource) {
    getNDILib()->NDIlib_send_set_failover(reinterpret_cast<NDIlib_send_instance_t>(pSender),
                                     reinterpret_cast<const NDIlib_source_t *>(pFailoverSource));
}

jlong Java_com_walker_devolay_DevolaySender_getSource(JNIEnv *env, jclass jClazz, jlong pSender) {
    return (jlong) getNDILib()->NDIlib_send_get_source_name(reinterpret_cast<NDIlib_send_instance_t>(pSender));
}
