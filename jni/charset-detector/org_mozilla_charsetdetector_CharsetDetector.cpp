#include "org_mozilla_charsetdetector_CharsetDetector.h"
#include "include/chardetect.h"

#include <stdio.h>

JNIEXPORT jlong JNICALL Java_org_mozilla_charsetdetector_CharsetDetector_chardet_1create
  (JNIEnv *env, jclass jclazz)
{
	chardet_t ret = NULL;
	int result = chardet_create(&ret);
	if ( result == CHARDET_RESULT_OK ){
		return (jlong)(ret);
	}
	return 0;
}

JNIEXPORT void JNICALL Java_org_mozilla_charsetdetector_CharsetDetector_chardet_1destroy
  (JNIEnv *env, jclass jclazz, jlong det)
{
	chardet_destroy((chardet_t) det);
}

JNIEXPORT jint JNICALL Java_org_mozilla_charsetdetector_CharsetDetector_chardet_1handle_1data
  (JNIEnv *env , jclass jclazz , jlong det, jbyteArray data , jint offset , jint len)
{
	jint ret;
	jbyte *ndata = (jbyte*)env->GetPrimitiveArrayCritical(data, 0);
	if ( ndata != 0 ){

		ret = chardet_handle_data((chardet_t)det, (const char*)ndata+offset , len);

		env->ReleasePrimitiveArrayCritical( data, ndata, JNI_ABORT);
		return ret;
	}
	return -1;
}

JNIEXPORT jint JNICALL Java_org_mozilla_charsetdetector_CharsetDetector_chardet_1data_1end
  (JNIEnv *env, jclass jclazz, jlong det)
{
	return chardet_data_end((chardet_t) det);

}

JNIEXPORT jint JNICALL Java_org_mozilla_charsetdetector_CharsetDetector_chardet_1reset
  (JNIEnv *env, jclass jclazz, jlong det)
{
	return chardet_reset((chardet_t) det);
}

JNIEXPORT jstring JNICALL Java_org_mozilla_charsetdetector_CharsetDetector_chardet_1get_1charset
  (JNIEnv *env, jclass jclazz, jlong det)
{
	char	namebuf[256];

	int result = chardet_get_charset((chardet_t) det, namebuf, sizeof(namebuf) );
	if ( result == CHARDET_RESULT_OK ){
		return env->NewStringUTF( namebuf);
	}
	return 0;
}
