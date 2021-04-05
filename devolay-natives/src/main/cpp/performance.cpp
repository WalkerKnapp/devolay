#include "devolay.h"

#include "me_walkerknapp_devolay_DevolayPerformanceData.h"

JNIEXPORT jlong JNICALL Java_me_walkerknapp_devolay_DevolayPerformanceData_createPerformanceStruct(JNIEnv *env, jclass jClazz) {
    auto *ret = new NDIlib_recv_performance_t();
    return (jlong) ret;
}

JNIEXPORT void JNICALL Java_me_walkerknapp_devolay_DevolayPerformanceData_destroyPerformanceStruct(JNIEnv *env, jclass jClazz, jlong pStruct) {
    delete reinterpret_cast<NDIlib_recv_performance_t *>(pStruct);
}

JNIEXPORT jlong JNICALL Java_me_walkerknapp_devolay_DevolayPerformanceData_getPerformanceStructVideoFrames(JNIEnv *env, jclass jClazz, jlong pStruct) {
    return reinterpret_cast<NDIlib_recv_performance_t *>(pStruct)->video_frames;
}

JNIEXPORT jlong JNICALL Java_me_walkerknapp_devolay_DevolayPerformanceData_getPerformanceStructAudioFrames(JNIEnv *env, jclass jClazz, jlong pStruct) {
    return reinterpret_cast<NDIlib_recv_performance_t *>(pStruct)->audio_frames;
}

JNIEXPORT jlong JNICALL Java_me_walkerknapp_devolay_DevolayPerformanceData_getPerformanceStructMetadataFrames(JNIEnv *env, jclass jClazz, jlong pStruct) {
    return reinterpret_cast<NDIlib_recv_performance_t *>(pStruct)->metadata_frames;
}
