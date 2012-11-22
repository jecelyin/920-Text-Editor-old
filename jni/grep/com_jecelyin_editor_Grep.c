/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
#include <jni.h>
#include <string.h>
#include "com_jecelyin_editor_Grep.h"
#include "libbb.h"

JNIEnv *jenv;
jclass jcls;
jmethodID addMatchResult;

JNIEXPORT void JNICALL Java_com_jecelyin_editor_Grep_find(JNIEnv *env, jclass cls, jobjectArray argvs)
{
    jenv = env;
    jcls = cls;

    addMatchResult = (*env)->GetStaticMethodID(env, cls, "addMatchResult","(Ljava/lang/String;ILjava/lang/String;J)V");
    int argc = (*env)->GetArrayLength(env, argvs);
    const char *argvArr[argc+2];
    int i;
    argvArr[0] = __FILE__;
    for(i=0; i<argc; i++) {
        jstring str = (jstring) (*env)->GetObjectArrayElement(env, argvs, i);
        argvArr[i+1] = (*env)->GetStringUTFChars(env, str, NULL );
    }
    argvArr[argc+1]=0;//用于结束循环或判断数组结束
    grep_main(argc+2, argvArr);
    (*env)->DeleteLocalRef(env, addMatchResult);
}

void add_match_result(const char *file, int linenum, const char *line, long offset)
{
    jstring f = (*jenv)->NewStringUTF(jenv, file);
    jstring l = (*jenv)->NewStringUTF(jenv, line);
    (*jenv)->CallStaticVoidMethod(jenv, jcls, addMatchResult, f, linenum, l, offset);
    //(*jenv)->ReleaseStringUTFChars(jenv, f, file);
    //(*jenv)->ReleaseStringUTFChars(jenv, l, line);
    (*jenv)->DeleteLocalRef(jenv, f);
    (*jenv)->DeleteLocalRef(jenv, l);
}
