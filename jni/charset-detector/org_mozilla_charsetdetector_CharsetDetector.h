
#include <jni.h>

#ifndef _Included_org_mozilla_charsetdetector_CharsetDetector
#define _Included_org_mozilla_charsetdetector_CharsetDetector
#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jstring JNICALL Java_org_mozilla_charsetdetector_CharsetDetector_get_1encoding(
		JNIEnv *env, jclass jclazz, jstring file);

#ifdef __cplusplus
}
#endif

#endif
