#include "devolay.h"

#include <cstdio>

#include "com_walker_devolay_DevolayReceiver.h"

jlong Java_com_walker_devolay_DevolayReceiver_receiveCreate
    (JNIEnv *env, jclass jClazz, jlong pSource, jint jColorFormat, jint jReceiveBandwidth, jboolean jAllowVideoFields, jstring jName) {

    auto *recv_create = new NDIlib_recv_create_v3_t();

    if(pSource != 0) {
        recv_create->source_to_connect_to = *(reinterpret_cast<NDIlib_source_t *>(pSource));
    }
    recv_create->color_format = static_cast<NDIlib_recv_color_format_e>(jColorFormat);
    recv_create->bandwidth = static_cast<NDIlib_recv_bandwidth_e>(jReceiveBandwidth);
    recv_create->allow_video_fields = jAllowVideoFields;

    auto *isCopy = new jboolean();
    *isCopy = JNI_TRUE;
    if(jName != nullptr) {
        const char *name = env->GetStringUTFChars(jName, isCopy);
        recv_create->p_ndi_recv_name = name;
    }
    delete isCopy;

    auto *ret = getNDILib()->NDIlib_recv_create_v3(recv_create);
    delete recv_create;
    return reinterpret_cast<jlong>(ret);
}

jlong Java_com_walker_devolay_DevolayReceiver_receiveCreateDefaultSettings(JNIEnv *env, jclass jClazz) {
    auto *recv_create_struct = new NDIlib_recv_create_v3_t();
    auto *ret = getNDILib()->NDIlib_recv_create_v3(recv_create_struct);
    delete recv_create_struct;
    return reinterpret_cast<jlong>(ret);
}

void Java_com_walker_devolay_DevolayReceiver_receiveDestroy(JNIEnv *env, jclass jClazz, jlong pReceiver) {
    delete reinterpret_cast<NDIlib_recv_instance_t *>(pReceiver);
}

void Java_com_walker_devolay_DevolayReceiver_receiveConnect(JNIEnv *env, jclass jClazz, jlong pReceiver, jlong pSource) {
    getNDILib()->NDIlib_recv_connect(reinterpret_cast<NDIlib_recv_instance_t *>(pReceiver), reinterpret_cast<NDIlib_source_t *>(pSource));
}

jint Java_com_walker_devolay_DevolayReceiver_receiveCaptureV2(JNIEnv *env, jclass jClazz, jlong pReceiver, jlong pVideoFrame, jlong pAudioFrame, jlong pMetadataFrame, jint timeout) {
    return getNDILib()->NDIlib_recv_capture_v2(reinterpret_cast<NDIlib_recv_instance_t *>(pReceiver),
                                  reinterpret_cast<NDIlib_video_frame_v2_t *>(pVideoFrame),
                                  reinterpret_cast<NDIlib_audio_frame_v2_t *>(pAudioFrame),
                                  reinterpret_cast<NDIlib_metadata_frame_t *>(pMetadataFrame),
                                  timeout);
}

void Java_com_walker_devolay_DevolayReceiver_freeVideoV2(JNIEnv *env, jclass jClazz, jlong pReceiver, jlong pVideoFrame) {
    getNDILib()->NDIlib_recv_free_video_v2(reinterpret_cast<NDIlib_recv_instance_t *>(pReceiver),
                              reinterpret_cast<NDIlib_video_frame_v2_t *>(pVideoFrame));
}

void Java_com_walker_devolay_DevolayReceiver_freeAudioV2(JNIEnv *env, jclass jClazz, jlong pReceiver, jlong pAudioFrame) {
    getNDILib()->NDIlib_recv_free_audio_v2(reinterpret_cast<NDIlib_recv_instance_t *>(pReceiver),
                              reinterpret_cast<NDIlib_audio_frame_v2_t *>(pAudioFrame));
}

void Java_com_walker_devolay_DevolayReceiver_freeMetadata(JNIEnv *env, jclass jClazz, jlong pReceiver, jlong pMetadataFrame) {
    getNDILib()->NDIlib_recv_free_metadata(reinterpret_cast<NDIlib_recv_instance_t *>(pReceiver),
                              reinterpret_cast<NDIlib_metadata_frame_t *>(pMetadataFrame));
}

jboolean Java_com_walker_devolay_DevolayReceiver_receiveSendMetadata(JNIEnv *env, jclass jClazz, jlong pReceiver, jlong pMetadataFrame) {
    return getNDILib()->NDIlib_recv_send_metadata(reinterpret_cast<NDIlib_recv_instance_t *>(pReceiver),
                                     reinterpret_cast<NDIlib_metadata_frame_t *>(pMetadataFrame));
}

void Java_com_walker_devolay_DevolayReceiver_receiveGetPerformance(JNIEnv *env, jclass jClazz, jlong pReceiver, jlong pTotalPerformance, jlong pDroppedPerformance) {
    getNDILib()->NDIlib_recv_get_performance(reinterpret_cast<NDIlib_recv_instance_t *>(pReceiver),
                                reinterpret_cast<NDIlib_recv_performance_t *>(pTotalPerformance),
                                reinterpret_cast<NDIlib_recv_performance_t *>(pDroppedPerformance));
}

void Java_com_walker_devolay_DevolayReceiver_receiveClearConnectionMetadata(JNIEnv *env, jclass jClazz, jlong pReceiver) {
    getNDILib()->NDIlib_recv_clear_connection_metadata(reinterpret_cast<NDIlib_recv_instance_t *>(pReceiver));
}

void Java_com_walker_devolay_DevolayReceiver_receiveAddConnectionMetadata(JNIEnv *env, jclass jClazz, jlong pReceiver, jlong pMetadataFrame) {
    getNDILib()->NDIlib_recv_add_connection_metadata(reinterpret_cast<NDIlib_recv_instance_t *>(pReceiver),
                                        reinterpret_cast<NDIlib_metadata_frame_t *>(pMetadataFrame));
}

jint Java_com_walker_devolay_DevolayReceiver_receiveGetNoConnections(JNIEnv *env, jclass jClazz, jlong pReceiver) {
    return getNDILib()->NDIlib_recv_get_no_connections(reinterpret_cast<NDIlib_recv_instance_t *>(pReceiver));
}

jstring Java_com_walker_devolay_DevolayReceiver_receiveGetWebControl(JNIEnv *env, jclass jClazz, jlong pReceiver) {
    const char *webControl = getNDILib()->NDIlib_recv_get_web_control(reinterpret_cast<NDIlib_recv_instance_t *>(pReceiver));
    auto *ret = env->NewStringUTF(webControl);
    getNDILib()->NDIlib_recv_free_string(reinterpret_cast<NDIlib_recv_instance_t *>(pReceiver), webControl);
    return ret;
}
