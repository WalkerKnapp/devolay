#include <jni.h>
#include <cstdio>

#include <Processing.NDI.Lib.h>

#include "../headers/com_walker_devolay_DevolaySender.h"

jlong Java_com_walker_devolay_DevolaySender_sendCreate(JNIEnv *env, jclass jClazz, jstring jNdiName, jstring jGroups, jboolean jClockVideo, jboolean jClockAudio) {
    printf("sendCreate\n");
    return 0;
}

jlong Java_com_walker_devolay_DevolaySender_sendCreateDefaultSettings(JNIEnv *env, jclass jClazz) {
    printf("sendCreateDefaultSettings\n");
    return 0;
}

void Java_com_walker_devolay_DevolaySender_sendDestroy(JNIEnv *env, jclass jClazz, jlong pSender) {
    printf("sendDestroy\n");
}

void Java_com_walker_devolay_DevolaySender_sendVideoV2(JNIEnv *env, jclass jClazz, jlong pSender, jlong pFrame) {
    printf("sendVideoV2\n");
}

void Java_com_walker_devolay_DevolaySender_sendVideoAsyncV2(JNIEnv *env, jclass jClazz, jlong pSender, jlong pFrame) {
    printf("sendVideoAsyncV2\n");
}

void Java_com_walker_devolay_DevolaySender_sendAudioV2(JNIEnv *env, jclass jClazz, jlong pSender, jlong pFrame) {
    printf("sendAudioV2\n");
}

void Java_com_walker_devolay_DevolaySender_sendMetadata(JNIEnv *env, jclass jClazz, jlong pSender, jlong pFrame) {
    printf("sendMetadata\n");
}

jbyte Java_com_walker_devolay_DevolaySender_getTally(JNIEnv *env, jclass jClazz, jlong pSender, jint jTimeoutMs) {
    printf("getTally\n");
    return 0;
}

jint Java_com_walker_devolay_DevolaySender_getNoConnections(JNIEnv *env, jclass jClazz, jlong pSender, jint jTimeoutMs) {
    printf("getNoConnections\n");
    return 0;
}

void Java_com_walker_devolay_DevolaySender_clearConnectionMetadata(JNIEnv *env, jclass jClazz, jlong pSender) {
    printf("clearConnectionMetadata\n");
}

void Java_com_walker_devolay_DevolaySender_addConnectionMetadata(JNIEnv *env, jclass jClazz, jlong pSender, jlong pFrame) {
    printf("addConnectionMetadata\n");
}

void Java_com_walker_devolay_DevolaySender_setFailover(JNIEnv *env, jclass jClazz, jlong pSender, jlong pFailoverSource) {
    printf("setFailover\n");
}

jlong Java_com_walker_devolay_DevolaySender_getSource(JNIEnv *env, jclass jClazz, jlong pSender) {
    printf("getSource\n");
    return 0;
}
