#include "devolay.h"

#include <cstdio>
#include <cstdlib>
#include <vector>

#ifdef _WIN32
#include <windows.h>
#else
#include <stdlib.h>
#include <dlfcn.h>
#endif

#ifdef __ANDROID__
#include <android/log.h>
#define printf(...) __android_log_print(ANDROID_LOG_DEBUG, "TAG", __VA_ARGS__);
#endif

#define GHC_WITH_EXCEPTIONS
#include <filesystem.hpp>
namespace fs = ghc::filesystem;

#include "../headers/me_walkerknapp_devolay_Devolay.h"

static const NDIlib_v3 *ndiLib = (NDIlib_v3 *)calloc(1, sizeof(NDIlib_v3));

const NDIlib_v3 *getNDILib() {
    return ndiLib;
}

JNIEXPORT jint JNICALL Java_me_walkerknapp_devolay_Devolay_nLoadLibraries(JNIEnv * env, jclass jClazz, jstring jNdiLibraryPath) {
    std::vector<fs::path> locations;

    char *redistFolder = getenv(NDILIB_REDIST_FOLDER);
    if(redistFolder != nullptr) {
        locations.emplace_back(fs::path(std::string(redistFolder) + "/" + NDILIB_LIBRARY_NAME));
    }

    if (jNdiLibraryPath != nullptr) {
        const char* ndiLibraryCstr = env->GetStringUTFChars(jNdiLibraryPath, nullptr);
        locations.emplace_back(fs::path(std::string(ndiLibraryCstr)));
        env->ReleaseStringUTFChars(jNdiLibraryPath, ndiLibraryCstr);
    }

#if defined(__linux__) || defined(__APPLE__)
    locations.emplace_back(fs::path(std::string("/usr/lib/") + NDILIB_LIBRARY_NAME));
    locations.emplace_back(fs::path(std::string("/usr/local/lib/") + NDILIB_LIBRARY_NAME));
#endif
#if defined(__APPLE__)
    locations.emplace_back(fs::path(std::string("/Library/NDI SDK for Apple/lib/x64/") + NDILIB_LIBRARY_NAME));
#endif


    for(const fs::path& possibleLibPath : locations) {

        printf("Testing for NDI at %s\n", possibleLibPath.string().c_str());

        if(fs::exists(possibleLibPath) && fs::is_regular_file(possibleLibPath)) {
            printf("Found NDI library at '%s'\n", possibleLibPath.c_str());

            // Load NDI library
#ifdef _WIN32
            std::string libPathString = possibleLibPath.string();
            std::wstring libPathWString = std::wstring(libPathString.begin(), libPathString.end());
            LPCWSTR libNameC = libPathWString.c_str();
            HMODULE hNDILib = LoadLibraryW(libNameC);

            if(hNDILib) {
                const NDIlib_v3* (*NDIlib_v3_load)() = nullptr;
                *((FARPROC*)&NDIlib_v3_load) = GetProcAddress(hNDILib, "NDIlib_v3_load");

                if (NDIlib_v3_load != nullptr) {
                    ndiLib = NDIlib_v3_load();

                    ndiLib->initialize();
                    return 0;
                } else {
                    FreeLibrary(hNDILib);

                    printf("Failed to load NDI_v3_load function.");
                    return -2;
                }
            } else {
                printf("Library failed to load: %lu\n", GetLastError());
            }
#else
            void *hNDILib = dlopen(possibleLibPath.c_str(), RTLD_LOCAL | RTLD_LAZY);

            if(hNDILib) {
                const NDIlib_v3* (*NDIlib_v3_load)(void) = NULL;
                *((void**)&NDIlib_v3_load) = dlsym(hNDILib, "NDIlib_v3_load");

                if (NDIlib_v3_load != nullptr) {
                    ndiLib = NDIlib_v3_load();

                    ndiLib->NDIlib_initialize();
                    return 0;
                } else {
                    dlclose(hNDILib);

                    printf("Failed to load NDI_v3_load function.");
                    return -2;
                }
            } else {
                printf("Library failed to load.");
            }
#endif
        }
    }

    return -1;
}

JNIEXPORT jstring JNICALL Java_me_walkerknapp_devolay_Devolay_nGetVersion(JNIEnv *env, jclass jClazz) {
    return env->NewStringUTF(getNDILib()->version());
}

JNIEXPORT jboolean JNICALL Java_me_walkerknapp_devolay_Devolay_nIsSupportedCpu(JNIEnv *env, jclass jClazz) {
    return getNDILib()->is_supported_CPU();
}
