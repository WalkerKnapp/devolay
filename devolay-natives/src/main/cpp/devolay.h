#ifndef DEVOLAY_DEVOLAY_H
#define DEVOLAY_DEVOLAY_H

#include <stdint.h>
#include <stddef.h>

#include <Processing.NDI.Lib.h>

#ifdef __int64
#define __int64_t __int64
#endif
#ifndef __int64
#define __int64 long long
#endif

#include <jni.h>

extern const NDIlib_v3 *getNDILib();

#endif //DEVOLAY_DEVOLAY_H
