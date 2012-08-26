
#include "org_mozilla_charsetdetector_CharsetDetector.h"
#include "include/chardetect.h"

#include <stdio.h>


JNIEXPORT jstring JNICALL Java_org_mozilla_charsetdetector_CharsetDetector_get_1encoding(
		JNIEnv *env, jclass jclazz, jstring file) {

	const char * path;
    jboolean iscopy;

	path = env->GetStringUTFChars(file, &iscopy);
    
    char buf[4096];
    char encoding[CHARDET_MAX_ENCODING_NAME];
    size_t len;
    int res = 0;
    chardet_t det = NULL;
    FILE* fp = NULL;

    chardet_create(&det);
    fp = fopen(path, "rb");
    do {
	len = fread(buf, 1, sizeof(buf), fp);
	res = chardet_handle_data(det, buf, len);
    } while (res==CHARDET_RESULT_OK && feof(fp)==0);
    fclose(fp);
    chardet_data_end(det);

    int result = chardet_get_charset(det, encoding, CHARDET_MAX_ENCODING_NAME);

    chardet_destroy(det);
    env->ReleaseStringUTFChars(file, path);

	if (result == CHARDET_RESULT_OK) {
		return env->NewStringUTF(encoding);
	}
	return 0;
}
