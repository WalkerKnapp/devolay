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

#define GHC_WITH_EXCEPTIONS
#include <filesystem.hpp>
namespace fs = ghc::filesystem;

#include "../headers/me_walkerknapp_devolay_Devolay.h"

static const NDIlib_v3 *ndiLib = (NDIlib_v3 *)calloc(1, sizeof(NDIlib_v3));

const NDIlib_v3 *getNDILib() {
    return ndiLib;
}

JNIEXPORT jint JNICALL Java_me_walkerknapp_devolay_Devolay_nLoadLibraries(JNIEnv * env, jclass jClazz, jstring extractedNdiLibrary) {
    std::vector<std::string> locations;

    char *redistFolder = getenv(NDILIB_REDIST_FOLDER);
    if(redistFolder != nullptr) {
        locations.emplace_back(std::string(redistFolder));
    }

    if (extractedNdiLibrary != nullptr) {
        const char *extractedNdiLibraryCstr = env->GetStringUTFChars(extractedNdiLibrary, 0);
        locations.emplace_back(std::string(extractedNdiLibraryCstr));
        env->ReleaseStringUTFChars(extractedNdiLibrary, extractedNdiLibraryCstr);
    }

#if defined(__linux__) || defined(__APPLE__)
    locations.emplace_back("/usr/lib");
    locations.emplace_back("/usr/local/lib");
#endif
#if defined(__APPLE__)
    locations.emplace_back("/Library/NDI SDK for Apple/lib/x64");
#endif

    for(const std::string& possiblePath : locations) {
        fs::path possibleLibPath(possiblePath);
        possibleLibPath += "/";
        possibleLibPath += NDILIB_LIBRARY_NAME;

        //printf("Testing for NDI at %s\n", possibleLibPath.string().c_str());

        if(fs::exists(possibleLibPath) && fs::is_regular_file(possibleLibPath)) {
            //printf("Found NDI library at '%s'\n", possibleLibPath.c_str());

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
