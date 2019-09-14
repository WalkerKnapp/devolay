#include <jni.h>
#include <cstdio>
#include <cstdlib>
#include <filesystem>
#include <atomic>

#include "devolay.h"

#define BOOST_DLL_USE_STD_FS
#define _CPPUNWIND
#include <boost/dll.hpp>

#include "../headers/com_walker_devolay_Devolay.h"

namespace fs = boost::dll::fs;

//static std::shared_ptr<NDIlib_v3> ndiLib;
static boost::dll::shared_library *ndiLibraryRef = nullptr;
static NDIlib_v3 *ndiLib = (NDIlib_v3 *)calloc(1, sizeof(NDIlib_v3));

NDIlib_v3 *getNDILib() {
    return ndiLib;
}

jint Java_com_walker_devolay_Devolay_nLoadLibraries(JNIEnv * env, jclass jClazz) {
    printf("nLoadLibraries\n");
    std::vector<std::string> locations;
    locations.emplace_back(getenv(NDILIB_REDIST_FOLDER));
#if defined(__linux__) || defined(__APPLE__)
    locations.emplace_back("/usr/lib");
    locations.emplace_back("/usr/local/lib")
#endif

    for(const std::string& possiblePath : locations) {
        fs::path possibleLibPath(possiblePath);
        possibleLibPath += "/";
        possibleLibPath += NDILIB_LIBRARY_NAME;

        printf("Testing for NDI at %s\n", possibleLibPath.string().c_str());

        if(fs::exists(possibleLibPath) && fs::is_regular_file(possibleLibPath)) {
            printf("Found NDI library at '%s'\n", possibleLibPath.string().c_str());

            ndiLibraryRef = new boost::dll::shared_library(possibleLibPath);

            if(ndiLibraryRef) {
                const NDIlib_v3* (*NDIlib_v3_load)(void) = NULL;
                NDIlib_v3_load = ndiLibraryRef->get<const NDIlib_v3 *()>("NDIlib_v3_load");

                if (NDIlib_v3_load != nullptr) {
                    const NDIlib_v3 *ndiLibInitial = NDIlib_v3_load();
                    memcpy(ndiLib, ndiLibInitial, sizeof(NDIlib_v3));

                    // I'm not sure why, but the memory gets deallocated if you don't copy it.
                    //ndiLib.store( (const NDIlib_v3 *) calloc(1, sizeof(NDIlib_v3)));
                    //memcpy((void *)ndiLib, (void *)ndiLibInitial, sizeof(NDIlib_v3));

                    printf("Loaded library\n");

                    ndiLib->NDIlib_initialize();
                    printf("context1 pointer: %p\n", ndiLib);
                    printf("context1 value: %i\n", ndiLib->NDIlib_is_supported_CPU());

                    return 0;
                } else {
                    printf("Failed to load NDI_v3_load function.");
                    return -2;
                }
            } else {
                printf("Library failed to load.");
            }
        }
    }

    return -1;
}