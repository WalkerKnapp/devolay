#include <jni.h>
#include <cstdio>

#include <Processing.NDI.Lib.h>

#include "../headers/com_walker_devolay_DevolaySource.h"

void Java_com_walker_devolay_DevolaySource_deallocSource(JNIEnv *env, jclass jClazz, jlong pSource) {
    printf("deallocSource\n");
}

jstring Java_com_walker_devolay_DevolaySource_getSourceName(JNIEnv *env, jclass jClazz, jlong pSource) {
    printf("getSourceName\n");
    return nullptr;
}
