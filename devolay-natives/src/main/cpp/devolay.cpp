#include "devolay.h"

#include <cstdio>
#include <cstdlib>

#ifdef _WIN32
#include <windows.h>
#else
#include <stdlib.h>
#include <dlfcn.h>
#endif

#include <vector>

#include <filesystem>

#include "../headers/com_walker_devolay_Devolay.h"

namespace fs = std::filesystem;

static const NDIlib_v3 *ndiLib = (NDIlib_v3 *)calloc(1, sizeof(NDIlib_v3));

const NDIlib_v3 *getNDILib() {
    return ndiLib;
}

jint Java_com_walker_devolay_Devolay_nLoadLibraries(JNIEnv * env, jclass jClazz) {
    std::vector<std::string> locations;

    char *redistFolder = getenv(NDILIB_REDIST_FOLDER);
    if(redistFolder != nullptr) {
        locations.emplace_back(std::string(redistFolder));
    }

#if defined(__linux__) || defined(__APPLE__)
    locations.emplace_back("/usr/lib");
    locations.emplace_back("/usr/local/lib");
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
            LPCSTR libNameC = possibleLibPath.string().c_str();
            HMODULE hNDILib = LoadLibraryA(libNameC);

            if(hNDILib) {
                const NDIlib_v3* (*NDIlib_v3_load)(void) = NULL;
                *((FARPROC*)&NDIlib_v3_load) = GetProcAddress(hNDILib, "NDIlib_v3_load");

                if (NDIlib_v3_load != nullptr) {
                    ndiLib = NDIlib_v3_load();

                    ndiLib->NDIlib_initialize();
                    return 0;
                } else {
                    FreeLibrary(hNDILib);

                    printf("Failed to load NDI_v3_load function.");
                    return -2;
                }
            } else {
                printf("Library failed to load.");
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