# Copyright (C) 2009 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := grep
LOCAL_C_INCLUDES += $(JNI_H_INCLUDE) 
LOCAL_C_INCLUDES := $(LOCAL_PATH)/include
LOCAL_SRC_FILES := com_jecelyin_editor_Grep.c  src/grep.c  src/wfopen.c  src/xregcomp.c \
  src/get_line_from_file.c  src/llist.c  src/wfopen_input.c  src/getopt32.c  src/recursive_action.c  \
  src/xfuncs_printf.c

LOCAL_LDLIBS := -llog
LOCAL_CFLAGS += -O3 

LOCAL_ARM_MODE := arm
#LOCAL_C_INCLUDES += $(JNI_H_INCLUDE)
#LOCAL_C_INCLUDES += external/icu4c/common
#LOCAL_PRELINK_MODULE := false

include $(BUILD_SHARED_LIBRARY)

