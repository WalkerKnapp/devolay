#include "devolay.h"

#include <cstdio>

#include "../headers/com_walker_devolay_DevolaySource.h"

JNIEXPORT void JNICALL Java_com_walker_devolay_DevolaySource_deallocSource(JNIEnv *env, jclass jClazz, jlong pSource) {
    //delete reinterpret_cast<NDIlib_source_t *>(pSource);
}

JNIEXPORT jstring JNICALL Java_com_walker_devolay_DevolaySource_getSourceName(JNIEnv *env, jclass jClazz, jlong pSource) {
    return env->NewStringUTF(reinterpret_cast<NDIlib_source_t *>(pSource)->p_ndi_name);
}
