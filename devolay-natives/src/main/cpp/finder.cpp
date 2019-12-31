#include "devolay.h"

#include "com_walker_devolay_DevolayFinder.h"

jlong Java_com_walker_devolay_DevolayFinder_findCreate(JNIEnv *env, jclass jClazz, jboolean jShowLocalSources, jstring jGroups, jstring jExtraIps) {
    auto *NDI_find_create = new NDIlib_find_create_t();

    NDI_find_create->show_local_sources = jShowLocalSources;

    auto *isCopy = new jboolean();
    *isCopy = JNI_TRUE;
    if(jGroups != nullptr) {
        const char *groups = env->GetStringUTFChars(jGroups, isCopy);
        NDI_find_create->p_groups = groups;
    }
    if(jExtraIps != nullptr) {
        const char *extraIps = env->GetStringUTFChars(jExtraIps, isCopy);
        NDI_find_create->p_extra_ips = extraIps;
    }
    delete isCopy;

    auto ret = getNDILib()->NDIlib_find_create_v2(NDI_find_create);
    delete NDI_find_create;
    return (jlong) ret;
}

jlong Java_com_walker_devolay_DevolayFinder_findCreateDefaultSettings(JNIEnv *env, jclass jClazz) {
    auto *NDI_find_create = new NDIlib_find_create_t();

    auto *ret = getNDILib()->NDIlib_find_create_v2(NDI_find_create);
    delete NDI_find_create;
    return (jlong) ret;
}

void Java_com_walker_devolay_DevolayFinder_findDestroy(JNIEnv *env, jclass jClazz, jlong pFind) {
    getNDILib()->NDIlib_find_destroy(reinterpret_cast<NDIlib_find_instance_t>(pFind));
}

jlongArray Java_com_walker_devolay_DevolayFinder_findGetCurrentSources(JNIEnv *env, jclass jClazz, jlong pFind) {
    uint32_t no_sources = 0;
    const NDIlib_source_t* p_sources = getNDILib()->find_get_current_sources(reinterpret_cast<NDIlib_find_instance_t>(pFind), &no_sources);

    auto ret = env->NewLongArray(no_sources);
    for(uint32_t i = 0; i < no_sources; i++) {
        const NDIlib_source_t *source = &p_sources[i];
        const auto pSource = (const jlong) source;
        env->SetLongArrayRegion(ret, i, 1, &pSource);
    }
    return ret;
}

jboolean Java_com_walker_devolay_DevolayFinder_findWaitForSources(JNIEnv *env, jclass jClazz, jlong pFind, jint jTimeout) {
    return getNDILib()->NDIlib_find_wait_for_sources(reinterpret_cast<NDIlib_find_instance_t>(pFind), jTimeout);
}
