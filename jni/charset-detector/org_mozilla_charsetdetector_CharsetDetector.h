
#include <jni.h>

#ifndef _Included_org_mozilla_charsetdetector_CharsetDetector
#define _Included_org_mozilla_charsetdetector_CharsetDetector
#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jlong JNICALL Java_org_mozilla_charsetdetector_CharsetDetector_chardet_1create
  (JNIEnv *, jclass);

JNIEXPORT void JNICALL Java_org_mozilla_charsetdetector_CharsetDetector_chardet_1destroy
  (JNIEnv *, jclass, jlong);

JNIEXPORT jint JNICALL Java_org_mozilla_charsetdetector_CharsetDetector_chardet_1handle_1data
  (JNIEnv *, jclass, jlong, jbyteArray,jint,jint);

JNIEXPORT jint JNICALL Java_org_mozilla_charsetdetector_CharsetDetector_chardet_1data_1end
  (JNIEnv *, jclass, jlong);

JNIEXPORT jint JNICALL Java_org_mozilla_charsetdetector_CharsetDetector_chardet_1reset
  (JNIEnv *, jclass, jlong);

JNIEXPORT jstring JNICALL Java_org_mozilla_charsetdetector_CharsetDetector_chardet_1get_1charset
  (JNIEnv *, jclass, jlong);

#ifdef __cplusplus
}
#endif

#endif
