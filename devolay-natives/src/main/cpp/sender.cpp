#include <jni.h>
#include <cstdio>

#include <Processing.NDI.Lib.h>
#include <vcruntime_typeinfo.h>
#include <functional>

#include "devolay.h"

#include "../headers/com_walker_devolay_DevolaySender.h"

jlong Java_com_walker_devolay_DevolaySender_sendCreate(JNIEnv *env, jclass jClazz, jstring jNdiName, jstring jGroups, jboolean jClockVideo, jboolean jClockAudio) {
    printf("sendCreate\n");

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

    printf("context2 pointer: %p\n", getNDILib());
    printf("context2 func: %p\n", getNDILib()->NDIlib_is_supported_CPU);

    printf("typeof func: %s\n", typeid(getNDILib()->NDIlib_is_supported_CPU).name());

    std::function<bool(void)> supportedFunc = getNDILib()->NDIlib_is_supported_CPU;

    printf("typeof supportedFunc: %s\n", typeid(supportedFunc).name());
    printf("target: %s\n", supportedFunc.target_type().name());

    printf("read: %ld\n", *((long *)getNDILib()->NDIlib_is_supported_CPU));
    printf("supportedFunc: %i\n", supportedFunc());


    printf("context2 value: %i\n", getNDILib()->NDIlib_is_supported_CPU());

    auto ret = getNDILib()->NDIlib_send_create(NDI_send_create_desc);
    delete NDI_send_create_desc;
    return (jlong) ret;
}

jlong Java_com_walker_devolay_DevolaySender_sendCreateDefaultSettings(JNIEnv *env, jclass jClazz) {
    printf("sendCreateDefaultSettings\n");

    NDIlib_send_create_t NDI_send_create_desc;

    return reinterpret_cast<jlong>(getNDILib()->NDIlib_send_create(&NDI_send_create_desc));
}

void Java_com_walker_devolay_DevolaySender_sendDestroy(JNIEnv *env, jclass jClazz, jlong pSender) {
    printf("sendDestroy\n");

    getNDILib()->NDIlib_send_destroy(reinterpret_cast<NDIlib_send_instance_t>(pSender));
}

void Java_com_walker_devolay_DevolaySender_sendVideoV2(JNIEnv *env, jclass jClazz, jlong pSender, jlong pFrame) {
    printf("sendVideoV2\n");

    getNDILib()->NDIlib_send_send_video_v2(reinterpret_cast<NDIlib_send_instance_t>(pSender),
                                      reinterpret_cast<const NDIlib_video_frame_v2_t *>(pFrame));
}

void Java_com_walker_devolay_DevolaySender_sendVideoAsyncV2(JNIEnv *env, jclass jClazz, jlong pSender, jlong pFrame) {
    printf("sendVideoAsyncV2\n");

    getNDILib()->NDIlib_send_send_video_async_v2(reinterpret_cast<NDIlib_send_instance_t>(pSender),
                                      reinterpret_cast<const NDIlib_video_frame_v2_t *>(pFrame));
}

void Java_com_walker_devolay_DevolaySender_sendAudioV2(JNIEnv *env, jclass jClazz, jlong pSender, jlong pFrame) {
    printf("sendAudioV2\n");

    getNDILib()->NDIlib_send_send_audio_v2(reinterpret_cast<NDIlib_send_instance_t>(pSender),
                                      reinterpret_cast<const NDIlib_audio_frame_v2_t *>(pFrame));
}

void Java_com_walker_devolay_DevolaySender_sendMetadata(JNIEnv *env, jclass jClazz, jlong pSender, jlong pFrame) {
    printf("sendMetadata\n");

    getNDILib()->NDIlib_send_send_metadata(reinterpret_cast<NDIlib_send_instance_t>(pSender),
                                      reinterpret_cast<const NDIlib_metadata_frame_t *>(pFrame));
}

jbyte Java_com_walker_devolay_DevolaySender_getTally(JNIEnv *env, jclass jClazz, jlong pSender, jint jTimeoutMs) {
    printf("getTally\n");

    return 0;
}

jint Java_com_walker_devolay_DevolaySender_getNoConnections(JNIEnv *env, jclass jClazz, jlong pSender, jint jTimeoutMs) {
    printf("getNoConnections\n");
    return getNDILib()->NDIlib_send_get_no_connections(reinterpret_cast<NDIlib_send_instance_t>(pSender), jTimeoutMs);
}

void Java_com_walker_devolay_DevolaySender_clearConnectionMetadata(JNIEnv *env, jclass jClazz, jlong pSender) {
    printf("clearConnectionMetadata\n");
    getNDILib()->NDIlib_send_clear_connection_metadata(reinterpret_cast<NDIlib_send_instance_t>(pSender));
}

void Java_com_walker_devolay_DevolaySender_addConnectionMetadata(JNIEnv *env, jclass jClazz, jlong pSender, jlong pFrame) {
    printf("addConnectionMetadata\n");
    getNDILib()->NDIlib_send_add_connection_metadata(reinterpret_cast<NDIlib_send_instance_t>(pSender),
                                                reinterpret_cast<const NDIlib_metadata_frame_t *>(pFrame));
}

void Java_com_walker_devolay_DevolaySender_setFailover(JNIEnv *env, jclass jClazz, jlong pSender, jlong pFailoverSource) {
    printf("setFailover\n");
    getNDILib()->NDIlib_send_set_failover(reinterpret_cast<NDIlib_send_instance_t>(pSender),
                                     reinterpret_cast<const NDIlib_source_t *>(pFailoverSource));
}

jlong Java_com_walker_devolay_DevolaySender_getSource(JNIEnv *env, jclass jClazz, jlong pSender) {
    printf("getSource\n");
    return (jlong) getNDILib()->NDIlib_send_get_source_name(reinterpret_cast<NDIlib_send_instance_t>(pSender));
}
