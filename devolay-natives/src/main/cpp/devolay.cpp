#include <jni.h>
#include <cstdio>
#include <cstdlib>
#include <filesystem>

#include <Processing.NDI.Lib.h>

#define BOOST_DLL_USE_STD_FS
#define _CPPUNWIND
#include <boost/dll.hpp>

#include "../headers/com_walker_devolay_Devolay.h"

namespace fs = boost::dll::fs;

extern const NDIlib_v3* ndiLib;

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
        possibleLibPath += NDILIB_LIBRARY_NAME;

        if(fs::exists(possibleLibPath) && fs::is_regular_file(possibleLibPath)) {
            printf("Found NDI library at '%s'\n", possibleLibPath.string().c_str());

            boost::dll::shared_library lib(possibleLibPath);

            auto ndiLibLoadFunc = lib.get<NDIlib_v3()>("NDIlib_v3_load");
        }
    }

    return 0;
}