#include "devolay.h"

#include <cstring>

#include "../headers/me_walkerknapp_devolay_DevolaySender.h"

#ifdef __ANDROID__
#include <android/log.h>
#define printf(...) __android_log_print(ANDROID_LOG_DEBUG, "TAG", __VA_ARGS__);
#endif

JNIEXPORT jlong JNICALL Java_me_walkerknapp_devolay_DevolaySender_sendCreate(JNIEnv *env, jclass jClazz, jstring jNdiName, jstring jGroups, jboolean jClockVideo, jboolean jClockAudio) {
    auto *NDI_send_create_desc = new NDIlib_send_create_t();

    if(jNdiName != nullptr) {
        const char *ndiName = env->GetStringUTFChars(jNdiName, nullptr);
        NDI_send_create_desc->p_ndi_name = strdup(ndiName);
        env->ReleaseStringUTFChars(jNdiName, ndiName);
    }
    if(jGroups != nullptr) {
        const char *groups = env->GetStringUTFChars(jGroups, nullptr);
        NDI_send_create_desc->p_groups = strdup(groups);
        env->ReleaseStringUTFChars(jGroups, groups);
    }

    NDI_send_create_desc->clock_video = jClockVideo;
    NDI_send_create_desc->clock_audio = jClockAudio;

    auto ret = getNDILib()->send_create(NDI_send_create_desc);

    delete NDI_send_create_desc->p_ndi_name;
    delete NDI_send_create_desc->p_groups;
    delete NDI_send_create_desc;

    return (jlong) ret;
}

JNIEXPORT jlong JNICALL Java_me_walkerknapp_devolay_DevolaySender_sendCreateDefaultSettings(JNIEnv *env, jclass jClazz) {
    NDIlib_send_create_t NDI_send_create_desc;

    return reinterpret_cast<jlong>(getNDILib()->send_create(&NDI_send_create_desc));
}

JNIEXPORT void JNICALL Java_me_walkerknapp_devolay_DevolaySender_sendDestroy(JNIEnv *env, jclass jClazz, jlong pSender) {
    getNDILib()->send_destroy(reinterpret_cast<NDIlib_send_instance_t>(pSender));
}

JNIEXPORT void JNICALL Java_me_walkerknapp_devolay_DevolaySender_sendVideoV2(JNIEnv *env, jclass jClazz, jlong pSender, jlong pFrame) {
    getNDILib()->send_send_video_v2(reinterpret_cast<NDIlib_send_instance_t>(pSender),
                                      reinterpret_cast<const NDIlib_video_frame_v2_t *>(pFrame));
}

JNIEXPORT void JNICALL Java_me_walkerknapp_devolay_DevolaySender_sendVideoAsyncV2(JNIEnv *env, jclass jClazz, jlong pSender, jlong pFrame) {
    getNDILib()->send_send_video_async_v2(reinterpret_cast<NDIlib_send_instance_t>(pSender),
                                      reinterpret_cast<const NDIlib_video_frame_v2_t *>(pFrame));
}

JNIEXPORT void JNICALL Java_me_walkerknapp_devolay_DevolaySender_sendAudioV2(JNIEnv *env, jclass jClazz, jlong pSender, jlong pFrame) {
    getNDILib()->send_send_audio_v2(reinterpret_cast<NDIlib_send_instance_t>(pSender),
                                      reinterpret_cast<const NDIlib_audio_frame_v2_t *>(pFrame));
}

JNIEXPORT void JNICALL Java_me_walkerknapp_devolay_DevolaySender_sendAudioInterleaved16s(JNIEnv *env, jclass jClazz, jlong pSender, jlong pFrame) {
    getNDILib()->util_send_send_audio_interleaved_16s(reinterpret_cast<NDIlib_send_instance_t>(pSender),
                                                             reinterpret_cast<const NDIlib_audio_frame_interleaved_16s_t *>(pFrame));
}

JNIEXPORT void JNICALL Java_me_walkerknapp_devolay_DevolaySender_sendAudioInterleaved32s(JNIEnv *env, jclass jClazz, jlong pSender, jlong pFrame) {
    getNDILib()->util_send_send_audio_interleaved_32s(reinterpret_cast<NDIlib_send_instance_t>(pSender),
                                                             reinterpret_cast<const NDIlib_audio_frame_interleaved_32s_t *>(pFrame));
}

JNIEXPORT void JNICALL Java_me_walkerknapp_devolay_DevolaySender_sendAudioInterleaved32f(JNIEnv *env, jclass jClazz, jlong pSender, jlong pFrame) {
    getNDILib()->util_send_send_audio_interleaved_32f(reinterpret_cast<NDIlib_send_instance_t>(pSender),
                                                             reinterpret_cast<const NDIlib_audio_frame_interleaved_32f_t *>(pFrame));
}

JNIEXPORT void JNICALL Java_me_walkerknapp_devolay_DevolaySender_sendMetadata(JNIEnv *env, jclass jClazz, jlong pSender, jlong pFrame) {
    getNDILib()->send_send_metadata(reinterpret_cast<NDIlib_send_instance_t>(pSender),
                                      reinterpret_cast<const NDIlib_metadata_frame_t *>(pFrame));
}

JNIEXPORT jbyte JNICALL Java_me_walkerknapp_devolay_DevolaySender_getTally(JNIEnv *env, jclass jClazz, jlong pSender, jint jTimeoutMs) {
    return 0;
}

JNIEXPORT jint JNICALL Java_me_walkerknapp_devolay_DevolaySender_getNoConnections(JNIEnv *env, jclass jClazz, jlong pSender, jint jTimeoutMs) {
    return getNDILib()->send_get_no_connections(reinterpret_cast<NDIlib_send_instance_t>(pSender), jTimeoutMs);
}

JNIEXPORT void JNICALL Java_me_walkerknapp_devolay_DevolaySender_clearConnectionMetadata(JNIEnv *env, jclass jClazz, jlong pSender) {
    getNDILib()->send_clear_connection_metadata(reinterpret_cast<NDIlib_send_instance_t>(pSender));
}

JNIEXPORT void JNICALL Java_me_walkerknapp_devolay_DevolaySender_addConnectionMetadata(JNIEnv *env, jclass jClazz, jlong pSender, jlong pFrame) {
    getNDILib()->send_add_connection_metadata(reinterpret_cast<NDIlib_send_instance_t>(pSender),
                                                reinterpret_cast<const NDIlib_metadata_frame_t *>(pFrame));
}

JNIEXPORT void JNICALL Java_me_walkerknapp_devolay_DevolaySender_setFailover(JNIEnv *env, jclass jClazz, jlong pSender, jlong pFailoverSource) {
    getNDILib()->send_set_failover(reinterpret_cast<NDIlib_send_instance_t>(pSender),
                                     reinterpret_cast<const NDIlib_source_t *>(pFailoverSource));
}

JNIEXPORT jlong JNICALL Java_me_walkerknapp_devolay_DevolaySender_getSource(JNIEnv *env, jclass jClazz, jlong pSender) {
    return (jlong) getNDILib()->send_get_source_name(reinterpret_cast<NDIlib_send_instance_t>(pSender));
}

JNIEXPORT jint JNICALL Java_me_walkerknapp_devolay_DevolaySender_sendCapture(JNIEnv *env, jclass jClazz, jlong pSender, jlong pMetadataFrame, jint jTimeout) {
    return getNDILib()->send_capture(reinterpret_cast<NDIlib_send_instance_t>(pSender),
                                     reinterpret_cast<NDIlib_metadata_frame_t *>(pMetadataFrame),
                                     jTimeout);
}

JNIEXPORT void JNICALL Java_me_walkerknapp_devolay_DevolaySender_freeMetadata(JNIEnv *env, jclass jClazz, jlong pSender, jlong pMetadataFrame) {
    getNDILib()->send_free_metadata(reinterpret_cast<NDIlib_send_instance_t>(pSender),
                              reinterpret_cast<NDIlib_metadata_frame_t *>(pMetadataFrame));
}
