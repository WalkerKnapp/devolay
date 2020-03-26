#include "devolay.h"

#include <cstdio>
#include <string.h>

#include "../headers/com_walker_devolay_DevolayAudioFrame.h"
#include "../headers/com_walker_devolay_DevolayAudioFrameInterleaved16s.h"
#include "../headers/com_walker_devolay_DevolayAudioFrameInterleaved32s.h"
#include "../headers/com_walker_devolay_DevolayAudioFrameInterleaved32f.h"
#include "../headers/com_walker_devolay_DevolayMetadataFrame.h"
#include "../headers/com_walker_devolay_DevolayVideoFrame.h"

/** Audio Frame **/

jlong Java_com_walker_devolay_DevolayAudioFrame_createNewAudioFrameDefaultSettings(JNIEnv * env, jclass jClazz) {
    NDIlib_audio_frame_v2_t *NDI_audio_frame = new NDIlib_audio_frame_v2_t();
    return (jlong) NDI_audio_frame;
}

void Java_com_walker_devolay_DevolayAudioFrame_destroyAudioFrame(JNIEnv *env, jclass jClazz, jlong pFrame) {
    delete reinterpret_cast<NDIlib_audio_frame_v2_t *>(pFrame);
}

void Java_com_walker_devolay_DevolayAudioFrame_setSampleRate(JNIEnv *env, jclass jClazz, jlong pFrame, jint jSampleRate) {
    reinterpret_cast<NDIlib_audio_frame_v2_t *>(pFrame)->sample_rate = jSampleRate;
}

jint Java_com_walker_devolay_DevolayAudioFrame_getSampleRate(JNIEnv *env, jclass jClazz, jlong pFrame) {
    return reinterpret_cast<NDIlib_audio_frame_v2_t *>(pFrame)->sample_rate;
}

void Java_com_walker_devolay_DevolayAudioFrame_setNoChannels(JNIEnv *env, jclass jClazz, jlong pFrame, jint jNoChannels) {
    reinterpret_cast<NDIlib_audio_frame_v2_t *>(pFrame)->no_channels = jNoChannels;
}

jint Java_com_walker_devolay_DevolayAudioFrame_getNoChannels(JNIEnv *env, jclass jClazz, jlong pFrame) {
    return reinterpret_cast<NDIlib_audio_frame_v2_t *>(pFrame)->no_channels;
}

void Java_com_walker_devolay_DevolayAudioFrame_setNoSamples(JNIEnv *env , jclass jClazz, jlong pFrame, jint jNoSamples) {
    reinterpret_cast<NDIlib_audio_frame_v2_t *>(pFrame)->no_samples = jNoSamples;
}

jint Java_com_walker_devolay_DevolayAudioFrame_getNoSamples(JNIEnv *env, jclass jClazz, jlong pFrame) {
    return reinterpret_cast<NDIlib_audio_frame_v2_t *>(pFrame)->no_samples;
}

void Java_com_walker_devolay_DevolayAudioFrame_setTimecode(JNIEnv *env, jclass jClazz, jlong pFrame, jlong jTimecode) {
    reinterpret_cast<NDIlib_audio_frame_v2_t *>(pFrame)->timecode = jTimecode;
}

jlong Java_com_walker_devolay_DevolayAudioFrame_getTimecode(JNIEnv *env, jclass jClazz, jlong pFrame) {
    return reinterpret_cast<NDIlib_audio_frame_v2_t *>(pFrame)->timecode;
}

void Java_com_walker_devolay_DevolayAudioFrame_setChannelStride(JNIEnv *env, jclass jClazz, jlong pFrame, jint jChannelStride) {
    reinterpret_cast<NDIlib_audio_frame_v2_t *>(pFrame)->channel_stride_in_bytes = jChannelStride;
}

jint Java_com_walker_devolay_DevolayAudioFrame_getChannelStride(JNIEnv *env, jclass jClazz, jlong pFrame) {
    return reinterpret_cast<NDIlib_audio_frame_v2_t *>(pFrame)->channel_stride_in_bytes;
}

void Java_com_walker_devolay_DevolayAudioFrame_setMetadata(JNIEnv *env, jclass jClazz, jlong pFrame, jstring jMetadata) {
    auto *isCopy = new jboolean();
    *isCopy = JNI_TRUE;
    const char *metadata = env->GetStringUTFChars(jMetadata, isCopy);
    delete isCopy;

    reinterpret_cast<NDIlib_audio_frame_v2_t *>(pFrame)->p_metadata = metadata;
}

jstring Java_com_walker_devolay_DevolayAudioFrame_getMetadata(JNIEnv *env, jclass jClazz, jlong pFrame) {
    return env->NewStringUTF(reinterpret_cast<NDIlib_audio_frame_v2_t *>(pFrame)->p_metadata);
}

void Java_com_walker_devolay_DevolayAudioFrame_setTimestamp(JNIEnv *env, jclass jClazz, jlong pFrame, jlong jTimestamp) {
    reinterpret_cast<NDIlib_audio_frame_v2_t *>(pFrame)->timestamp = jTimestamp;
}

jlong Java_com_walker_devolay_DevolayAudioFrame_getTimestamp(JNIEnv *env, jclass jClazz, jlong pFrame) {
    return reinterpret_cast<NDIlib_audio_frame_v2_t *>(pFrame)->timestamp;
}

void Java_com_walker_devolay_DevolayAudioFrame_setData(JNIEnv *env, jclass jClazz, jlong pFrame, jobject jData) {
    auto frame = reinterpret_cast<NDIlib_audio_frame_v2_t *>(pFrame);

    if(env->GetDirectBufferCapacity(jData) < frame->no_samples * frame->no_channels * sizeof(float)) {
        env->ThrowNew(env->FindClass("java/lang/IllegalArgumentException"), "Audio buffer not large enough.");
        return;
    }

    frame->p_data = static_cast<float *>(env->GetDirectBufferAddress(jData));
}

jobject Java_com_walker_devolay_DevolayAudioFrame_getData(JNIEnv *env, jclass jClazz, jlong pFrame) {
    auto frame = reinterpret_cast<NDIlib_audio_frame_v2_t *>(pFrame);

    // TODO: Resolve this sizing issue. If the no samples, no channels are increased, will read unknown memory. Maybe force deallocate data on changing one of those?
    return env->NewDirectByteBuffer(frame->p_data, frame->no_samples * frame->no_channels * sizeof(float));
}

/** Audio Frame 16s Interleaved **/
jlong Java_com_walker_devolay_DevolayAudioFrameInterleaved16s_createNewAudioFrameInterleaved16sDefaultSettings(JNIEnv *env, jclass jClazz) {
    auto *NDI_audio_frame = new NDIlib_audio_frame_interleaved_16s_t();
    return (jlong) NDI_audio_frame;
}

void Java_com_walker_devolay_DevolayAudioFrameInterleaved16s_destroyAudioFrameInterleaved16s(JNIEnv *env, jclass jClazz, jlong pFrame) {
    delete reinterpret_cast<NDIlib_audio_frame_interleaved_16s_t *>(pFrame);
}

void Java_com_walker_devolay_DevolayAudioFrameInterleaved16s_setSampleRate(JNIEnv *env, jclass jClazz, jlong pFrame, jint jSampleRate) {
    reinterpret_cast<NDIlib_audio_frame_interleaved_16s_t *>(pFrame)->sample_rate = jSampleRate;
}

jint Java_com_walker_devolay_DevolayAudioFrameInterleaved16s_getSampleRate(JNIEnv *env, jclass jClazz, jlong pFrame) {
    return reinterpret_cast<NDIlib_audio_frame_interleaved_16s_t *>(pFrame)->sample_rate;
}

void Java_com_walker_devolay_DevolayAudioFrameInterleaved16s_setNoChannels(JNIEnv *env, jclass jClazz, jlong pFrame, jint jNoChannels) {
    reinterpret_cast<NDIlib_audio_frame_interleaved_16s_t *>(pFrame)->no_channels = jNoChannels;
}

jint Java_com_walker_devolay_DevolayAudioFrameInterleaved16s_getNoChannels(JNIEnv *env, jclass jClazz, jlong pFrame) {
    return reinterpret_cast<NDIlib_audio_frame_interleaved_16s_t *>(pFrame)->no_channels;
}

void Java_com_walker_devolay_DevolayAudioFrameInterleaved16s_setNoSamples(JNIEnv *env, jclass jClazz, jlong pFrame, jint jNoSamples) {
    reinterpret_cast<NDIlib_audio_frame_interleaved_16s_t *>(pFrame)->no_samples = jNoSamples;
}

jint Java_com_walker_devolay_DevolayAudioFrameInterleaved16s_getNoSamples(JNIEnv *env, jclass jClazz, jlong pFrame) {
    return reinterpret_cast<NDIlib_audio_frame_interleaved_16s_t *>(pFrame)->no_samples;
}

void Java_com_walker_devolay_DevolayAudioFrameInterleaved16s_setTimecode(JNIEnv *env, jclass jClazz, jlong pFrame, jlong jTimecode) {
    reinterpret_cast<NDIlib_audio_frame_interleaved_16s_t *>(pFrame)->timecode = jTimecode;
}

jlong Java_com_walker_devolay_DevolayAudioFrameInterleaved16s_getTimecode(JNIEnv *env, jclass jClazz, jlong pFrame) {
    return reinterpret_cast<NDIlib_audio_frame_interleaved_16s_t *>(pFrame)->timecode;
}

void Java_com_walker_devolay_DevolayAudioFrameInterleaved16s_setReferenceLevel(JNIEnv *env, jclass jClazz, jlong pFrame, jint jReferenceLevel) {
    reinterpret_cast<NDIlib_audio_frame_interleaved_16s_t *>(pFrame)->reference_level = jReferenceLevel;
}

jint Java_com_walker_devolay_DevolayAudioFrameInterleaved16s_getReferenceLevel(JNIEnv *env, jclass jClazz, jlong pFrame) {
    return reinterpret_cast<NDIlib_audio_frame_interleaved_16s_t *>(pFrame)->reference_level;
}

void Java_com_walker_devolay_DevolayAudioFrameInterleaved16s_setData(JNIEnv *env, jclass jClazz, jlong pFrame, jobject jData) {
    reinterpret_cast<NDIlib_audio_frame_interleaved_16s_t *>(pFrame)->p_data = static_cast<int16_t *>(env->GetDirectBufferAddress(jData));
}

jobject Java_com_walker_devolay_DevolayAudioFrameInterleaved16s_getData(JNIEnv *env, jclass jClazz, jlong pFrame) {
    auto frame = reinterpret_cast<NDIlib_audio_frame_interleaved_16s_t *>(pFrame);

    // TODO: Resolve this sizing issue. If the no samples, no channels are increased, will read unknown memory. Maybe force deallocate data on changing one of those?
    return env->NewDirectByteBuffer(frame->p_data, frame->no_samples * frame->no_channels * sizeof(int16_t));
}

/** Audio Frame 32s Interleaved **/
jlong Java_com_walker_devolay_DevolayAudioFrameInterleaved32s_createNewAudioFrameInterleaved32sDefaultSettings(JNIEnv *env, jclass jClazz) {
    auto *NDI_audio_frame = new NDIlib_audio_frame_interleaved_32s_t();
    return (jlong) NDI_audio_frame;
}

void Java_com_walker_devolay_DevolayAudioFrameInterleaved32s_destroyAudioFrameInterleaved32s(JNIEnv *env, jclass jClazz, jlong pFrame) {
    delete reinterpret_cast<NDIlib_audio_frame_interleaved_32s_t *>(pFrame);
}

void Java_com_walker_devolay_DevolayAudioFrameInterleaved32s_setSampleRate(JNIEnv *env, jclass jClazz, jlong pFrame, jint jSampleRate) {
    reinterpret_cast<NDIlib_audio_frame_interleaved_32s_t *>(pFrame)->sample_rate = jSampleRate;
}

jint Java_com_walker_devolay_DevolayAudioFrameInterleaved32s_getSampleRate(JNIEnv *env, jclass jClazz, jlong pFrame) {
    return reinterpret_cast<NDIlib_audio_frame_interleaved_32s_t *>(pFrame)->sample_rate;
}

void Java_com_walker_devolay_DevolayAudioFrameInterleaved32s_setNoChannels(JNIEnv *env, jclass jClazz, jlong pFrame, jint jNoChannels) {
    reinterpret_cast<NDIlib_audio_frame_interleaved_32s_t *>(pFrame)->no_channels = jNoChannels;
}

jint Java_com_walker_devolay_DevolayAudioFrameInterleaved32s_getNoChannels(JNIEnv *env, jclass jClazz, jlong pFrame) {
    return reinterpret_cast<NDIlib_audio_frame_interleaved_32s_t *>(pFrame)->no_channels;
}

void Java_com_walker_devolay_DevolayAudioFrameInterleaved32s_setNoSamples(JNIEnv *env, jclass jClazz, jlong pFrame, jint jNoSamples) {
    reinterpret_cast<NDIlib_audio_frame_interleaved_32s_t *>(pFrame)->no_samples = jNoSamples;
}

jint Java_com_walker_devolay_DevolayAudioFrameInterleaved32s_getNoSamples(JNIEnv *env, jclass jClazz, jlong pFrame) {
    return reinterpret_cast<NDIlib_audio_frame_interleaved_32s_t *>(pFrame)->no_samples;
}

void Java_com_walker_devolay_DevolayAudioFrameInterleaved32s_setTimecode(JNIEnv *env, jclass jClazz, jlong pFrame, jlong jTimecode) {
    reinterpret_cast<NDIlib_audio_frame_interleaved_32s_t *>(pFrame)->timecode = jTimecode;
}

jlong Java_com_walker_devolay_DevolayAudioFrameInterleaved32s_getTimecode(JNIEnv *env, jclass jClazz, jlong pFrame) {
    return reinterpret_cast<NDIlib_audio_frame_interleaved_32s_t *>(pFrame)->timecode;
}

void Java_com_walker_devolay_DevolayAudioFrameInterleaved32s_setReferenceLevel(JNIEnv *env, jclass jClazz, jlong pFrame, jint jReferenceLevel) {
    reinterpret_cast<NDIlib_audio_frame_interleaved_32s_t *>(pFrame)->reference_level = jReferenceLevel;
}

jint Java_com_walker_devolay_DevolayAudioFrameInterleaved32s_getReferenceLevel(JNIEnv *env, jclass jClazz, jlong pFrame) {
    return reinterpret_cast<NDIlib_audio_frame_interleaved_32s_t *>(pFrame)->reference_level;
}

void Java_com_walker_devolay_DevolayAudioFrameInterleaved32s_setData(JNIEnv *env, jclass jClazz, jlong pFrame, jobject jData) {
    reinterpret_cast<NDIlib_audio_frame_interleaved_32s_t *>(pFrame)->p_data = static_cast<int32_t *>(env->GetDirectBufferAddress(jData));
}

jobject Java_com_walker_devolay_DevolayAudioFrameInterleaved32s_getData(JNIEnv *env, jclass jClazz, jlong pFrame) {
    auto frame = reinterpret_cast<NDIlib_audio_frame_interleaved_32s_t *>(pFrame);

    // TODO: Resolve this sizing issue. If the no samples, no channels are increased, will read unknown memory. Maybe force deallocate data on changing one of those?
    return env->NewDirectByteBuffer(frame->p_data, frame->no_samples * frame->no_channels * sizeof(int32_t));
}

/** Audio frame 32f Interleaved **/
jlong Java_com_walker_devolay_DevolayAudioFrameInterleaved32f_createNewAudioFrameInterleaved32fDefaultSettings(JNIEnv *env, jclass jClazz) {
    auto *NDI_audio_frame = new NDIlib_audio_frame_interleaved_32f_t();
    return (jlong) NDI_audio_frame;
}

void Java_com_walker_devolay_DevolayAudioFrameInterleaved32f_destroyAudioFrameInterleaved32f(JNIEnv *env, jclass jClazz, jlong pFrame) {
    delete reinterpret_cast<NDIlib_audio_frame_interleaved_32f_t *>(pFrame);
}

void Java_com_walker_devolay_DevolayAudioFrameInterleaved32f_setSampleRate(JNIEnv *env, jclass jClazz, jlong pFrame, jint jSampleRate) {
    reinterpret_cast<NDIlib_audio_frame_interleaved_32f_t *>(pFrame)->sample_rate = jSampleRate;
}

jint Java_com_walker_devolay_DevolayAudioFrameInterleaved32f_getSampleRate(JNIEnv *env, jclass jClazz, jlong pFrame) {
    return reinterpret_cast<NDIlib_audio_frame_interleaved_32f_t *>(pFrame)->sample_rate;
}

void Java_com_walker_devolay_DevolayAudioFrameInterleaved32f_setNoChannels(JNIEnv *env, jclass jClazz, jlong pFrame, jint jNoChannels) {
    reinterpret_cast<NDIlib_audio_frame_interleaved_32f_t *>(pFrame)->no_channels = jNoChannels;
}

jint Java_com_walker_devolay_DevolayAudioFrameInterleaved32f_getNoChannels(JNIEnv *env, jclass jClazz, jlong pFrame) {
    return reinterpret_cast<NDIlib_audio_frame_interleaved_32f_t *>(pFrame)->no_channels;
}

void Java_com_walker_devolay_DevolayAudioFrameInterleaved32f_setNoSamples(JNIEnv *env, jclass jClazz, jlong pFrame, jint jNoSamples) {
    reinterpret_cast<NDIlib_audio_frame_interleaved_32f_t *>(pFrame)->no_samples = jNoSamples;
}

jint Java_com_walker_devolay_DevolayAudioFrameInterleaved32f_getNoSamples(JNIEnv *env, jclass jClazz, jlong pFrame) {
    return reinterpret_cast<NDIlib_audio_frame_interleaved_32f_t *>(pFrame)->no_samples;
}

void Java_com_walker_devolay_DevolayAudioFrameInterleaved32f_setTimecode(JNIEnv *env, jclass jClazz, jlong pFrame, jlong jTimecode) {
    reinterpret_cast<NDIlib_audio_frame_interleaved_32f_t *>(pFrame)->timecode = jTimecode;
}

jlong Java_com_walker_devolay_DevolayAudioFrameInterleaved32f_getTimecode(JNIEnv *env, jclass jClazz, jlong pFrame) {
    return reinterpret_cast<NDIlib_audio_frame_interleaved_32f_t *>(pFrame)->timecode;
}

void Java_com_walker_devolay_DevolayAudioFrameInterleaved32f_setData(JNIEnv *env, jclass jClazz, jlong pFrame, jobject jData) {
    reinterpret_cast<NDIlib_audio_frame_interleaved_32f_t *>(pFrame)->p_data = static_cast<float *>(env->GetDirectBufferAddress(jData));
}

jobject Java_com_walker_devolay_DevolayAudioFrameInterleaved32f_getData(JNIEnv *env, jclass jClazz, jlong pFrame) {
    auto frame = reinterpret_cast<NDIlib_audio_frame_interleaved_32f_t *>(pFrame);

    // TODO: Resolve this sizing issue. If the no samples, no channels are increased, will read unknown memory. Maybe force deallocate data on changing one of those?
    return env->NewDirectByteBuffer(frame->p_data, frame->no_samples * frame->no_channels * sizeof(float));
}

/** Metadata Frame **/
jlong Java_com_walker_devolay_DevolayMetadataFrame_createNewMetadataFrameDefaultSettings(JNIEnv *env, jclass jClazz) {
    auto *NDI_metadata_frame = new NDIlib_metadata_frame_t();
    return (jlong) NDI_metadata_frame;
}

void Java_com_walker_devolay_DevolayMetadataFrame_destroyMetadataFrame(JNIEnv *env, jclass jClazz, jlong pFrame) {
    delete reinterpret_cast<NDIlib_metadata_frame_t *>(pFrame);
}

jstring Java_com_walker_devolay_DevolayMetadataFrame_getData(JNIEnv *env, jclass jClazz, jlong pFrame) {
    auto *NDI_metadata_frame = reinterpret_cast<NDIlib_metadata_frame_t *>(pFrame);
    return env->NewStringUTF(NDI_metadata_frame->p_data);
}

void Java_com_walker_devolay_DevolayMetadataFrame_setData(JNIEnv *env, jclass jClazz, jlong pFrame, jstring jData) {
    auto *isCopy = new jboolean();
    *isCopy = JNI_FALSE;
    const char *data = env->GetStringUTFChars(jData, isCopy);
    delete isCopy;

    char *mutable_data = new char[env->GetStringUTFLength(jData)];

    strcpy(mutable_data, data);

    env->ReleaseStringUTFChars(jData, data);

    reinterpret_cast<NDIlib_metadata_frame_t *>(pFrame)->p_data = mutable_data;
}

jlong Java_com_walker_devolay_DevolayMetadataFrame_getTimecode(JNIEnv *env, jclass jClazz, jlong pFrame) {
    return reinterpret_cast<NDIlib_metadata_frame_t *>(pFrame)->timecode;
}

void Java_com_walker_devolay_DevolayMetadataFrame_setTimecode(JNIEnv *env, jclass jClazz, jlong pFrame, jlong jTimecode) {
    reinterpret_cast<NDIlib_metadata_frame_t *>(pFrame)->timecode = jTimecode;
}

/** Video Frame **/
jlong Java_com_walker_devolay_DevolayVideoFrame_createNewVideoFrameDefaultSettings(JNIEnv *env, jclass jClazz) {
    auto *NDI_video_frame = new NDIlib_video_frame_v2_t();
    return (jlong) NDI_video_frame;
}

void Java_com_walker_devolay_DevolayVideoFrame_destroyVideoFrame(JNIEnv *env, jclass jClazz, jlong pFrame) {
    delete reinterpret_cast<NDIlib_video_frame_v2_t *>(pFrame);
}

void Java_com_walker_devolay_DevolayVideoFrame_setXRes(JNIEnv *env, jclass jClazz, jlong pFrame, jint jXRes) {
    reinterpret_cast<NDIlib_video_frame_v2_t *>(pFrame)->xres = jXRes;
}

void Java_com_walker_devolay_DevolayVideoFrame_setYRes(JNIEnv *env, jclass jClazz, jlong pFrame, jint jYRes) {
    reinterpret_cast<NDIlib_video_frame_v2_t *>(pFrame)->yres = jYRes;
}

jint Java_com_walker_devolay_DevolayVideoFrame_getXRes(JNIEnv *env, jclass jClazz, jlong pFrame) {
    return reinterpret_cast<NDIlib_video_frame_v2_t *>(pFrame)->xres;
}

jint Java_com_walker_devolay_DevolayVideoFrame_getYRes(JNIEnv *env, jclass jClazz, jlong pFrame) {
    return reinterpret_cast<NDIlib_video_frame_v2_t *>(pFrame)->yres;
}

void Java_com_walker_devolay_DevolayVideoFrame_setFourCCType(JNIEnv *env, jclass jClazz, jlong pFrame, jint j4CCType) {
    reinterpret_cast<NDIlib_video_frame_v2_t *>(pFrame)->FourCC = static_cast<NDIlib_FourCC_video_type_e>(j4CCType);
}

jint Java_com_walker_devolay_DevolayVideoFrame_getFourCCType(JNIEnv *env, jclass jClazz, jlong pFrame) {
    return reinterpret_cast<NDIlib_video_frame_v2_t *>(pFrame)->FourCC;
}

void Java_com_walker_devolay_DevolayVideoFrame_setFrameRateN(JNIEnv *env, jclass jClazz, jlong pFrame, jint jFrameRateN) {
    reinterpret_cast<NDIlib_video_frame_v2_t *>(pFrame)->frame_rate_N = jFrameRateN;
}

void Java_com_walker_devolay_DevolayVideoFrame_setFrameRateD(JNIEnv *env, jclass jClazz, jlong pFrame, jint jFrameRateD) {
    reinterpret_cast<NDIlib_video_frame_v2_t *>(pFrame)->frame_rate_D = jFrameRateD;
}

jint Java_com_walker_devolay_DevolayVideoFrame_getFrameRateN(JNIEnv *env, jclass jClazz, jlong pFrame) {
    return reinterpret_cast<NDIlib_video_frame_v2_t *>(pFrame)->frame_rate_N;
}

jint Java_com_walker_devolay_DevolayVideoFrame_getFrameRateD(JNIEnv *env, jclass jClazz, jlong pFrame) {
    return reinterpret_cast<NDIlib_video_frame_v2_t *>(pFrame)->frame_rate_D;
}

void Java_com_walker_devolay_DevolayVideoFrame_setPictureAspectRatio(JNIEnv *env, jclass jClazz, jlong pFrame, jfloat jAspectRatio) {
    reinterpret_cast<NDIlib_video_frame_v2_t *>(pFrame)->picture_aspect_ratio = jAspectRatio;
}

jfloat Java_com_walker_devolay_DevolayVideoFrame_getPictureAspectRatio(JNIEnv *env, jclass jClazz, jlong pFrame) {
    return reinterpret_cast<NDIlib_video_frame_v2_t *>(pFrame)->picture_aspect_ratio;
}

void Java_com_walker_devolay_DevolayVideoFrame_setFrameFormatType(JNIEnv *env, jclass jClazz, jlong pFrame, jint jFrameFormatType) {
    reinterpret_cast<NDIlib_video_frame_v2_t *>(pFrame)->frame_format_type = static_cast<NDIlib_frame_format_type_e>(jFrameFormatType);
}

jint Java_com_walker_devolay_DevolayVideoFrame_getFrameFormatType(JNIEnv *env, jclass jClazz, jlong pFrame) {
    return reinterpret_cast<NDIlib_video_frame_v2_t *>(pFrame)->frame_format_type;
}

void Java_com_walker_devolay_DevolayVideoFrame_setTimecode(JNIEnv *env, jclass jClazz, jlong pFrame, jlong jTimecode) {
    reinterpret_cast<NDIlib_video_frame_v2_t *>(pFrame)->timecode = jTimecode;
}

jlong Java_com_walker_devolay_DevolayVideoFrame_getTimecode(JNIEnv *env, jclass jClazz, jlong pFrame) {
    return reinterpret_cast<NDIlib_video_frame_v2_t *>(pFrame)->timecode;
}

void Java_com_walker_devolay_DevolayVideoFrame_setLineStride(JNIEnv *env, jclass jClazz, jlong pFrame, jint jLineStride) {
    reinterpret_cast<NDIlib_video_frame_v2_t *>(pFrame)->line_stride_in_bytes = jLineStride;
}

jint Java_com_walker_devolay_DevolayVideoFrame_getLineStride(JNIEnv *env, jclass jClazz, jlong pFrame) {
    return reinterpret_cast<NDIlib_video_frame_v2_t *>(pFrame)->line_stride_in_bytes;
}

void Java_com_walker_devolay_DevolayVideoFrame_setMetadata(JNIEnv *env, jclass jClazz, jlong pFrame, jstring jMetadata) {
    auto *isCopy = new jboolean();
    *isCopy = JNI_TRUE;
    const char *metadata = env->GetStringUTFChars(jMetadata, isCopy);
    delete isCopy;

    reinterpret_cast<NDIlib_video_frame_v2_t *>(pFrame)->p_metadata = metadata;
}

jstring Java_com_walker_devolay_DevolayVideoFrame_getMetadata(JNIEnv *env, jclass jClazz, jlong pFrame) {
    return env->NewStringUTF(reinterpret_cast<NDIlib_video_frame_v2_t *>(pFrame)->p_metadata);
}

void Java_com_walker_devolay_DevolayVideoFrame_setTimestamp(JNIEnv *env, jclass jClazz, jlong pFrame, jint jTimestamp) {
    reinterpret_cast<NDIlib_video_frame_v2_t *>(pFrame)->timestamp = jTimestamp;
}

jint Java_com_walker_devolay_DevolayVideoFrame_getTimestamp(JNIEnv *env, jclass jClazz, jlong pFrame) {
    return reinterpret_cast<NDIlib_video_frame_v2_t *>(pFrame)->timestamp;
}

void Java_com_walker_devolay_DevolayVideoFrame_setData(JNIEnv *env, jclass jClazz, jlong pFrame, jobject jData) {
    reinterpret_cast<NDIlib_video_frame_v2_t *>(pFrame)->p_data = static_cast<uint8_t *>(env->GetDirectBufferAddress(jData));
}

jobject Java_com_walker_devolay_DevolayVideoFrame_getData(JNIEnv *env, jclass jClazz, jlong pFrame) {
    auto *frame = reinterpret_cast<NDIlib_video_frame_v2_t *>(pFrame);
    if(frame->p_data) {
        // TODO: Resolve this sizing issue. If the no samples, no channels are increased, will read unknown memory. Maybe force deallocate data on changing one of those?
        return env->NewDirectByteBuffer(frame->p_data, frame->line_stride_in_bytes * frame->yres);
    } else {
        return nullptr;
    }
}
