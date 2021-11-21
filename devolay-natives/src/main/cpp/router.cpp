#include "devolay.h"

#include <cstring>

#include "../headers/me_walkerknapp_devolay_DevolayRouter.h"

JNIEXPORT jlong JNICALL Java_me_walkerknapp_devolay_DevolayRouter_routingCreate(JNIEnv *env, jclass jClazz, jstring jNdiName, jstring jGroups) {
    auto *NDI_routing_create_desc = new NDIlib_routing_create_t();

    if (jNdiName != nullptr) {
        const char *ndiName = env->GetStringUTFChars(jNdiName, nullptr);
        NDI_routing_create_desc->p_ndi_name = strdup(ndiName);
        env->ReleaseStringUTFChars(jNdiName, ndiName);
    }
    if (jGroups != nullptr) {
        const char *groups = env->GetStringUTFChars(jGroups, nullptr);
        NDI_routing_create_desc->p_groups = strdup(groups);
        env->ReleaseStringUTFChars(jGroups, groups);
    }

    auto ret = getNDILib()->routing_create(NDI_routing_create_desc);

    delete NDI_routing_create_desc->p_ndi_name;
    delete NDI_routing_create_desc->p_groups;
    delete NDI_routing_create_desc;

    return (jlong) ret;
}

JNIEXPORT void JNICALL Java_me_walkerknapp_devolay_DevolayRouter_routingChange(JNIEnv *env, jclass jClazz, jlong pRouter, jlong pSource) {
    getNDILib()->routing_change(reinterpret_cast<NDIlib_routing_instance_t>(pRouter), reinterpret_cast<NDIlib_source_t *>(pSource));
}

JNIEXPORT void JNICALL Java_me_walkerknapp_devolay_DevolayRouter_routingClear(JNIEnv *env, jclass jClazz, jlong pRouter) {
    getNDILib()->routing_clear(reinterpret_cast<NDIlib_routing_instance_t>(pRouter));
}

JNIEXPORT jint JNICALL Java_me_walkerknapp_devolay_DevolayRouter_routingNoConnections(JNIEnv *env, jclass jClazz, jlong pRouter, jint jTimeoutMs) {
    return getNDILib()->routing_get_no_connections(reinterpret_cast<NDIlib_routing_instance_t>(pRouter), jTimeoutMs);
}

JNIEXPORT jlong JNICALL Java_me_walkerknapp_devolay_DevolayRouter_routingSource(JNIEnv *env, jclass jClazz, jlong pRouter) {
    return (jlong) getNDILib()->routing_get_source_name(reinterpret_cast<NDIlib_routing_instance_t>(pRouter));
}

JNIEXPORT void JNICALL Java_me_walkerknapp_devolay_DevolayRouter_routingDestroy(JNIEnv *env, jclass jClazz, jlong pRouter) {
    getNDILib()->routing_destroy(reinterpret_cast<NDIlib_routing_instance_t>(pRouter));
}