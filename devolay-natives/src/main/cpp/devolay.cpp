#include "devolay.h"

#include <cstdlib>

#ifdef _WIN32
#include <Windows.h>
#else
#include <stdlib.h>
#include <dlfcn.h>
#endif

#include "../headers/me_walkerknapp_devolay_Devolay.h"

static const NDIlib_v3 *ndiLib = (NDIlib_v3 *)calloc(1, sizeof(NDIlib_v3));

const NDIlib_v3 *getNDILib() {
    return ndiLib;
}

JNIEXPORT jint JNICALL Java_me_walkerknapp_devolay_Devolay_nLoadLibraries(JNIEnv * env, jclass jClazz, jstring jLoadedNdiName) {
#ifdef _WIN32
    const NDIlib_v3* (*NDIlib_v3_load)() = nullptr;
    const char *loadedNdiName = env->GetStringUTFChars(jLoadedNdiName, nullptr);
    *((FARPROC*)&NDIlib_v3_load) = GetProcAddress(GetModuleHandleA(loadedNdiName), "NDIlib_v3_load");
    env->ReleaseStringUTFChars(jLoadedNdiName, loadedNdiName);

    if (NDIlib_v3_load != nullptr) {
        ndiLib = NDIlib_v3_load();

        ndiLib->initialize();
        return 0;
    } else {
        return -2;
    }
#else
    const NDIlib_v3* (*NDIlib_v3_load)(void) = NULL;
    *((void**)&NDIlib_v3_load) = dlsym(RTLD_DEFAULT, "NDIlib_v3_load");

    if (NDIlib_v3_load != nullptr) {
        ndiLib = NDIlib_v3_load();

        ndiLib->NDIlib_initialize();
        return 0;
    } else {
        return -2;
    }
#endif
}

JNIEXPORT jstring JNICALL Java_me_walkerknapp_devolay_Devolay_nGetVersion(JNIEnv *env, jclass jClazz) {
    return env->NewStringUTF(getNDILib()->version());
}

JNIEXPORT jboolean JNICALL Java_me_walkerknapp_devolay_Devolay_nIsSupportedCpu(JNIEnv *env, jclass jClazz) {
    return getNDILib()->is_supported_CPU();
}
