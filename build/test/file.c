#ifndef NELEM
#define NELEM(x) ((int)(sizeof(x) / sizeof((x)[0])))
#endif

/*
中文ok——
Copyright (c) 2008, The Android Open Source Project
All rights reserved.

*/

#include <nativehelper/JNIHelp.h>
#include <nativehelper/jni.h>

#include <assert.h>
#include <dlfcn.h>
#include <stdio.h>
#include <string.h>
#include <sys/stat.h>
#include <utils/Log.h>

#include "jhead.h"



// Define the line below to turn on poor man's debugging output
#undef SUPERDEBUG

// Various tests
#undef REALLOCTEST
#undef OUTOFMEMORYTEST1

static void addExifAttibute(JNIEnv *env, jmethodID putMethod, jobject hashMap, char* key, char* value) {
    jstring jkey = (*env)->NewStringUTF(env, key);
    jstring jvalue = (*env)->NewStringUTF(env, value);

    jobject jobject_of_entryset = (*env)->CallObjectMethod(env, hashMap, putMethod, jkey, jvalue);

    (*env)->ReleaseStringUTFChars(env, jkey, key);
    (*env)->ReleaseStringUTFChars(env, jvalue, value);
}

extern void ResetJpgfile();

//END