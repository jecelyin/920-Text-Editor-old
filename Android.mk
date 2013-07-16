# Copyright 2008, The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-subdir-java-files)
LOCAL_PACKAGE_NAME := 920-Text-Editor

LOCAL_JNI_SHARED_LIBRARIES := libgrep \
	libCharsetDetector \
	libhighlight

include $(BUILD_PACKAGE)

include $(LOCAL_PATH)/jni/highlight/Android.mk \
    $(LOCAL_PATH)/jni/charset-detector/Android.mk  \
    $(LOCAL_PATH)/jni/grep/Android.mk
#自动加载所有目录下的Android.mk
#include $(call all-subdir-makefiles)
