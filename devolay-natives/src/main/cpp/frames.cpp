#include <jni.h>
#include <cstdio>

#include <Processing.NDI.Lib.h>

#include "../headers/com_walker_devolay_DevolayAudioFrame.h"
#include "../headers/com_walker_devolay_DevolayAudioFrameInterleaved16s.h"
#include "../headers/com_walker_devolay_DevolayAudioFrameInterleaved32s.h"
#include "../headers/com_walker_devolay_DevolayMetadataFrame.h"
#include "../headers/com_walker_devolay_DevolayVideoFrame.h"

/** Audio Frame **/

jlong Java_com_walker_devolay_DevolayAudioFrame_createNewAudioFrameDefaultSettings(JNIEnv * env, jclass jClazz) {
    printf("createNewAudioFrameDefaultSettings\n");
    NDIlib_audio_frame_v2_t *NDI_audio_frame = new NDIlib_audio_frame_v2_t();
    return (jlong) NDI_audio_frame;
}

void Java_com_walker_devolay_DevolayAudioFrame_destroyAudioFrame(JNIEnv *env, jclass jClazz, jlong pFrame) {
    printf("destroyAudioFrame\n");

    delete reinterpret_cast<NDIlib_audio_frame_v2_t *>(pFrame);
}

void Java_com_walker_devolay_DevolayAudioFrame_setSampleRate(JNIEnv *env, jclass jClazz, jlong pFrame, jint jSampleRate) {
    printf("setSampleRate\n");

    reinterpret_cast<NDIlib_audio_frame_v2_t *>(pFrame)->sample_rate = jSampleRate;
}

void Java_com_walker_devolay_DevolayAudioFrame_setNoChannels(JNIEnv *env, jclass jClazz, jlong pFrame, jint jNoChannels) {
    printf("setNoChannels\n");

    reinterpret_cast<NDIlib_audio_frame_v2_t *>(pFrame)->no_channels = jNoChannels;
}

void Java_com_walker_devolay_DevolayAudioFrame_setNoSamples(JNIEnv *env , jclass jClazz, jlong pFrame, jint jNoSamples) {
    printf("setNoSamples\n");

    reinterpret_cast<NDIlib_audio_frame_v2_t *>(pFrame)->no_samples = jNoSamples;
}

void Java_com_walker_devolay_DevolayAudioFrame_setTimecode(JNIEnv *env, jclass jClazz, jlong pFrame, jlong jTimecode) {
    printf("setTimecode\n");

    reinterpret_cast<NDIlib_audio_frame_v2_t *>(pFrame)->timecode = jTimecode;
}

void Java_com_walker_devolay_DevolayAudioFrame_setChannelStride(JNIEnv *env, jclass jClazz, jlong pFrame, jint jChannelStride) {
    printf("setChannelStride\n");

    reinterpret_cast<NDIlib_audio_frame_v2_t *>(pFrame)->channel_stride_in_bytes = jChannelStride;
}

void Java_com_walker_devolay_DevolayAudioFrame_setMetadata(JNIEnv *env, jclass jClazz, jlong pFrame, jstring jMetadata) {
    printf("setMetadata\n");
    auto *isCopy = new jboolean();
    *isCopy = JNI_TRUE;
    const char *metadata = env->GetStringUTFChars(jMetadata, isCopy);
    delete isCopy;

    reinterpret_cast<NDIlib_audio_frame_v2_t *>(pFrame)->p_metadata = metadata;
}

void Java_com_walker_devolay_DevolayAudioFrame_setTimestamp(JNIEnv *env, jclass jClazz, jlong pFrame, jlong jTimestamp) {
    printf("setTimestamp\n");

    reinterpret_cast<NDIlib_audio_frame_v2_t *>(pFrame)->timestamp = jTimestamp;
}

void Java_com_walker_devolay_DevolayAudioFrame_setData(JNIEnv *env, jclass jClazz, jlong pFrame, jobject jData) {
    printf("setData\n");

    reinterpret_cast<NDIlib_audio_frame_v2_t *>(pFrame)->p_data = static_cast<float *>(env->GetDirectBufferAddress(jData));
}

/** Audio Frame 16s Interleaved **/
jlong Java_com_walker_devolay_DevolayAudioFrameInterleaved16s_createNewAudioFrameInterleaved16sDefaultSettings(JNIEnv *env, jclass jClazz) {
    auto *NDI_audio_frame = new NDIlib_audio_frame_interleaved_16s_t();
    return (jlong) NDI_audio_frame;
}

void Java_com_walker_devolay_DevolayAudioFrameInterleaved16s_destroyAudioFrameInterleaved16s(JNIEnv *env, jclass jClazz, jlong pFrame) {
    delete reinterpret_cast<NDIlib_audio_frame_interleaved_16s_t *>(pFrame);
}

void Java_com_walker_devolay_DevolayAudioFrameInterleaved16s_setSampleRate(JNIEnv *env, jobject jClazz, jlong pFrame, jint jSampleRate) {
    reinterpret_cast<NDIlib_audio_frame_interleaved_16s_t *>(pFrame)->sample_rate = jSampleRate;
}

void Java_com_walker_devolay_DevolayAudioFrameInterleaved16s_setNoChannels(JNIEnv *env, jobject jClazz, jlong pFrame, jint jNoChannels) {
    reinterpret_cast<NDIlib_audio_frame_interleaved_16s_t *>(pFrame)->no_channels = jNoChannels;
}

void Java_com_walker_devolay_DevolayAudioFrameInterleaved16s_setNoSamples(JNIEnv *env, jobject jClazz, jlong pFrame, jint jNoSamples) {
    reinterpret_cast<NDIlib_audio_frame_interleaved_16s_t *>(pFrame)->no_samples = jNoSamples;
}

void Java_com_walker_devolay_DevolayAudioFrameInterleaved16s_setTimecode(JNIEnv *env, jobject jClazz, jlong pFrame, jlong jTimecode) {
    reinterpret_cast<NDIlib_audio_frame_interleaved_16s_t *>(pFrame)->timecode = jTimecode;
}

void Java_com_walker_devolay_DevolayAudioFrameInterleaved16s_setReferenceLevel(JNIEnv *env, jobject jClazz, jlong pFrame, jint jReferenceLevel) {
    reinterpret_cast<NDIlib_audio_frame_interleaved_16s_t *>(pFrame)->reference_level = jReferenceLevel;
}

void Java_com_walker_devolay_DevolayAudioFrameInterleaved16s_setData(JNIEnv *env, jobject jClazz, jlong pFrame, jobject jData) {
    reinterpret_cast<NDIlib_audio_frame_interleaved_16s_t *>(pFrame)->p_data = static_cast<int16_t *>(env->GetDirectBufferAddress(jData));
}

/** Audio Frame 32s Interleaved **/
jlong Java_com_walker_devolay_DevolayAudioFrameInterleaved32s_createNewAudioFrameInterleaved32sDefaultSettings(JNIEnv *env, jclass jClazz) {
    auto *NDI_audio_frame = new NDIlib_audio_frame_interleaved_32s_t();
    return (jlong) NDI_audio_frame;
}

void Java_com_walker_devolay_DevolayAudioFrameInterleaved32s_destroyAudioFrameInterleaved32s(JNIEnv *env, jclass jClazz, jlong pFrame) {
    delete reinterpret_cast<NDIlib_audio_frame_interleaved_32s_t *>(pFrame);
}

void Java_com_walker_devolay_DevolayAudioFrameInterleaved32s_setSampleRate(JNIEnv *env, jobject jClazz, jlong pFrame, jint jSampleRate) {
    reinterpret_cast<NDIlib_audio_frame_interleaved_32s_t *>(pFrame)->sample_rate = jSampleRate;
}

void Java_com_walker_devolay_DevolayAudioFrameInterleaved32s_setNoChannels(JNIEnv *env, jobject jClazz, jlong pFrame, jint jNoChannels) {
    reinterpret_cast<NDIlib_audio_frame_interleaved_32s_t *>(pFrame)->no_channels = jNoChannels;
}

void Java_com_walker_devolay_DevolayAudioFrameInterleaved32s_setNoSamples(JNIEnv *env, jobject jClazz, jlong pFrame, jint jNoSamples) {
    reinterpret_cast<NDIlib_audio_frame_interleaved_32s_t *>(pFrame)->no_samples = jNoSamples;
}

void Java_com_walker_devolay_DevolayAudioFrameInterleaved32s_setTimecode(JNIEnv *env, jobject jClazz, jlong pFrame, jlong jTimecode) {
    reinterpret_cast<NDIlib_audio_frame_interleaved_32s_t *>(pFrame)->timecode = jTimecode;
}

void Java_com_walker_devolay_DevolayAudioFrameInterleaved32s_setReferenceLevel(JNIEnv *env, jobject jClazz, jlong pFrame, jint jReferenceLevel) {
    reinterpret_cast<NDIlib_audio_frame_interleaved_32s_t *>(pFrame)->reference_level = jReferenceLevel;
}

void Java_com_walker_devolay_DevolayAudioFrameInterleaved32s_setData(JNIEnv *env, jobject jClazz, jlong pFrame, jobject jData) {
    reinterpret_cast<NDIlib_audio_frame_interleaved_32s_t *>(pFrame)->p_data = static_cast<int32_t *>(env->GetDirectBufferAddress(jData));
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
    NDIlib_video_frame_v2_t *NDI_video_frame = new NDIlib_video_frame_v2_t();
    return (jlong) NDI_video_frame;
}

void Java_com_walker_devolay_DevolayVideoFrame_destroyVideoFrame(JNIEnv *env, jclass jClazz, jlong pFrame) {
    printf("destroyVideoFrame\n");
    delete reinterpret_cast<NDIlib_video_frame_v2_t *>(pFrame);
}

void Java_com_walker_devolay_DevolayVideoFrame_setXRes(JNIEnv *env, jclass jClazz, jlong pFrame, jint jXRes) {
    printf("setXRes\n");
    reinterpret_cast<NDIlib_video_frame_v2_t *>(pFrame)->xres = jXRes;
}

void Java_com_walker_devolay_DevolayVideoFrame_setYRes(JNIEnv *env, jclass jClazz, jlong pFrame, jint jYRes) {
    printf("setYRes\n");
    reinterpret_cast<NDIlib_video_frame_v2_t *>(pFrame)->yres = jYRes;
}

void Java_com_walker_devolay_DevolayVideoFrame_setFourCCType(JNIEnv *env, jclass jClazz, jlong pFrame, jint j4CCType) {
    printf("setFourCCType\n");
    reinterpret_cast<NDIlib_video_frame_v2_t *>(pFrame)->FourCC = static_cast<NDIlib_FourCC_video_type_e>(j4CCType);
}

void Java_com_walker_devolay_DevolayVideoFrame_setFrameRateN(JNIEnv *env, jclass jClazz, jlong pFrame, jint jFrameRateN) {
    printf("setFrameRateN\n");
    reinterpret_cast<NDIlib_video_frame_v2_t *>(pFrame)->frame_rate_N = jFrameRateN;
}

void Java_com_walker_devolay_DevolayVideoFrame_setFrameRateD(JNIEnv *env, jclass jClazz, jlong pFrame, jint jFrameRateD) {
    printf("setFrameRateD\n");
    reinterpret_cast<NDIlib_video_frame_v2_t *>(pFrame)->frame_rate_D = jFrameRateD;
}

void Java_com_walker_devolay_DevolayVideoFrame_setPictureAspectRatio(JNIEnv *env, jclass jClazz, jlong pFrame, jfloat jAspectRatio) {
    printf("setPictureAspectRatio\n");
    reinterpret_cast<NDIlib_video_frame_v2_t *>(pFrame)->picture_aspect_ratio = jAspectRatio;
}

void Java_com_walker_devolay_DevolayVideoFrame_setFrameFormatType(JNIEnv *env, jclass jClazz, jlong pFrame, jint jFrameFormatType) {
    printf("setFrameFormatType\n");
    reinterpret_cast<NDIlib_video_frame_v2_t *>(pFrame)->frame_format_type = static_cast<NDIlib_frame_format_type_e>(jFrameFormatType);
}

void Java_com_walker_devolay_DevolayVideoFrame_setTimecode(JNIEnv *env, jclass jClazz, jlong pFrame, jlong jTimecode) {
    printf("setTimecode\n");
    reinterpret_cast<NDIlib_video_frame_v2_t *>(pFrame)->timecode = jTimecode;
}

void Java_com_walker_devolay_DevolayVideoFrame_setLineStride(JNIEnv *env, jclass jClazz, jlong pFrame, jint jLineStride) {
    printf("setLineStride\n");
    reinterpret_cast<NDIlib_video_frame_v2_t *>(pFrame)->line_stride_in_bytes = jLineStride;
}

void Java_com_walker_devolay_DevolayVideoFrame_setMetadata(JNIEnv *env, jclass jClazz, jlong pFrame, jstring jMetadata) {
    printf("setMetadata\n");
    auto *isCopy = new jboolean();
    *isCopy = JNI_TRUE;
    const char *metadata = env->GetStringUTFChars(jMetadata, isCopy);
    delete isCopy;

    reinterpret_cast<NDIlib_video_frame_v2_t *>(pFrame)->p_metadata = metadata;
}

void Java_com_walker_devolay_DevolayVideoFrame_setTimestamp(JNIEnv *env, jclass jClazz, jlong pFrame, jint jTimestamp) {
    printf("setTimestamp\n");
    reinterpret_cast<NDIlib_video_frame_v2_t *>(pFrame)->timestamp = jTimestamp;
}

void Java_com_walker_devolay_DevolayVideoFrame_setData(JNIEnv *env, jclass jClazz, jlong pFrame, jobject jData) {
    printf("setData\n");
    reinterpret_cast<NDIlib_video_frame_v2_t *>(pFrame)->p_data = static_cast<uint8_t *>(env->GetDirectBufferAddress(jData));
}
