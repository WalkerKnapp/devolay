#include <jni.h>
#include <cstdio>

#include <Processing.NDI.Lib.h>

#include "../headers/com_walker_devolay_DevolayAudioFrame.h"
#include "../headers/com_walker_devolay_DevolayMetadataFrame.h"
#include "../headers/com_walker_devolay_DevolayVideoFrame.h"

/** Audio Frame **/
jlong Java_com_walker_devolay_DevolayAudioFrame_createNewAudioFrameDefaultSettings(JNIEnv * env, jclass jClazz) {
    printf("createNewAudioFrameDefaultSettings\n");
    return 0;
}

void Java_com_walker_devolay_DevolayAudioFrame_destroyAudioFrame(JNIEnv *env, jclass jClazz, jlong pFrame) {
    printf("destroyAudioFrame\n");
}

void Java_com_walker_devolay_DevolayAudioFrame_setSampleRate(JNIEnv *env, jclass jClazz, jlong pFrame, jint jSampleRate) {
    printf("setSampleRate\n");
}

void Java_com_walker_devolay_DevolayAudioFrame_setNoChannels(JNIEnv *env, jclass jClazz, jlong pFrame, jint jNoChannels) {
    printf("setNoChannels\n");
}

void Java_com_walker_devolay_DevolayAudioFrame_setNoSamples(JNIEnv *env , jclass jClazz, jlong pFrame, jint jNoSamples) {
    printf("setNoSamples\n");
}

void Java_com_walker_devolay_DevolayAudioFrame_setTimecode(JNIEnv *env, jclass jClazz, jlong pFrame, jlong jTimecode) {
    printf("setTimecode\n");
}

void Java_com_walker_devolay_DevolayAudioFrame_setChannelStride(JNIEnv *env, jclass jClazz, jlong pFrame, jint jChannelStride) {
    printf("setChannelStride\n");
}

void Java_com_walker_devolay_DevolayAudioFrame_setMetadata(JNIEnv *env, jclass jClazz, jlong pFrame, jstring jMetadata) {
    printf("setMetadata\n");
}

void Java_com_walker_devolay_DevolayAudioFrame_setTimestamp(JNIEnv *env, jclass jClazz, jlong pFrame, jlong jTimestamp) {
    printf("setTimestamp\n");
}

void Java_com_walker_devolay_DevolayAudioFrame_setData(JNIEnv *env, jclass jClazz, jlong pFrame, jobject jData) {
    printf("setData\n");
}

/** Metadata Frame **/
jlong Java_com_walker_devolay_DevolayMetadataFrame_createNewMetadataFrameDefaultSettings(JNIEnv *env, jclass jClazz) {
    printf("createNewMetadataFrameDefaultSettings\n");
    return 0;
}

void Java_com_walker_devolay_DevolayMetadataFrame_destroyMetadataFrame(JNIEnv *env, jclass jClazz, jlong pFrame) {
    printf("destroyMetadataFrame\n");
}

jstring Java_com_walker_devolay_DevolayMetadataFrame_getData(JNIEnv *env, jclass jClazz, jlong pFrame) {
    printf("getData\n");
    return nullptr;
}

void Java_com_walker_devolay_DevolayMetadataFrame_setData(JNIEnv *env, jclass jClazz, jlong pFrame, jstring jData) {
    printf("setData\n");
}

jlong Java_com_walker_devolay_DevolayMetadataFrame_getTimecode(JNIEnv *env, jclass jClazz, jlong pFrame) {
    printf("getTimecode\n");
    return 0;
}

void Java_com_walker_devolay_DevolayMetadataFrame_setTimecode(JNIEnv *env, jclass jClazz, jlong pFrame, jlong jTimecode) {
    printf("setTimecode\n");
}

/** Video Frame **/
jlong Java_com_walker_devolay_DevolayVideoFrame_createNewVideoFrameDefaultSettings(JNIEnv *env, jclass jClazz) {
    printf("createNewVideoFrameDefaultSettings\n");
    return 0;
}

void Java_com_walker_devolay_DevolayVideoFrame_destroyVideoFrame(JNIEnv *env, jclass jClazz, jlong pFrame) {
    printf("destroyVideoFrame\n");
}

void Java_com_walker_devolay_DevolayVideoFrame_setXRes(JNIEnv *env, jclass jClazz, jlong pFrame, jint jXRes) {
    printf("setXRes\n");
}

void Java_com_walker_devolay_DevolayVideoFrame_setYRes(JNIEnv *env, jclass jClazz, jlong pFrame, jint jYRes) {
    printf("setYRes\n");
}

void Java_com_walker_devolay_DevolayVideoFrame_setFourCCType(JNIEnv *env, jclass jClazz, jlong pFrame, jint j4CCType) {
    printf("setFourCCType\n");
}

void Java_com_walker_devolay_DevolayVideoFrame_setFrameRateN(JNIEnv *env, jclass jClazz, jlong pFrame, jint jFrameRateN) {
    printf("setFrameRateN\n");
}

void Java_com_walker_devolay_DevolayVideoFrame_setFrameRateD(JNIEnv *env, jclass jClazz, jlong pFrame, jint jFrameRateD) {
    printf("setFrameRateD\n");
}

void Java_com_walker_devolay_DevolayVideoFrame_setPictureAspectRatio(JNIEnv *env, jclass jClazz, jlong pFrame, jfloat jAspectRatio) {
    printf("setPictureAspectRatio\n");
}

void Java_com_walker_devolay_DevolayVideoFrame_setFrameFormatType(JNIEnv *env, jclass jClazz, jlong pFrame, jint jFrameFormatType) {
    printf("setFrameFormatType\n");
}

void Java_com_walker_devolay_DevolayVideoFrame_setTimecode(JNIEnv *env, jclass jClazz, jlong pFrame, jlong jTimecode) {
    printf("setTimecode\n");
}

void Java_com_walker_devolay_DevolayVideoFrame_setLineStride(JNIEnv *env, jclass jClazz, jlong pFrame, jint jLineStride) {
    printf("setLineStride\n");
}

void Java_com_walker_devolay_DevolayVideoFrame_setMetadata(JNIEnv *env, jclass jClazz, jlong pFrame, jstring jMetadata) {
    printf("setMetadata\n");
}

void Java_com_walker_devolay_DevolayVideoFrame_setTimestamp(JNIEnv *env, jclass jClazz, jlong pFrame, jint jTimestamp) {
    printf("setTimestamp\n");
}

void Java_com_walker_devolay_DevolayVideoFrame_setData(JNIEnv *env, jclass jClazz, jlong pFrame, jobject jData) {
    printf("setData\n");
}
