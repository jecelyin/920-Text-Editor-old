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

LOCAL_MODULE    := libCharsetDetector
LOCAL_MODULE_TAGS := optional

LOCAL_C_INCLUDES += $(LOCAL_PATH)/src/tables \
    $(JNI_H_INCLUDE) \
    $(LOCAL_PATH)/src \
    $(LOCAL_PATH)/include

LOCAL_SRC_FILES := \
    src/CharDistribution.cpp \
    src/JpCntx.cpp \
    src/LangBulgarianModel.cpp \
    src/LangCyrillicModel.cpp \
    src/LangGreekModel.cpp \
    src/LangHebrewModel.cpp \
    src/LangHungarianModel.cpp \
    src/LangThaiModel.cpp \
    src/nsBig5Prober.cpp \
    src/nsCharSetProber.cpp \
    src/nsEUCJPProber.cpp \
    src/nsEUCKRProber.cpp \
    src/nsEUCTWProber.cpp \
    src/nsEscCharsetProber.cpp \
    src/nsEscSM.cpp \
    src/nsGB2312Prober.cpp \
    src/nsHebrewProber.cpp \
    src/nsLatin1Prober.cpp \
    src/nsMBCSGroupProber.cpp \
    src/nsMBCSSM.cpp \
    src/nsSBCSGroupProber.cpp \
    src/nsSBCharSetProber.cpp \
    src/nsSJISProber.cpp \
    src/nsUTF8Prober.cpp \
    src/nsUniversalDetector.cpp \
    src/entry/impl.cpp \
    org_mozilla_charsetdetector_CharsetDetector.cpp

LOCAL_CPPFLAGS += -O3 \
	-fno-rtti -fno-exceptions -nostdinc++ \
	-D_REENTRANT
LOCAL_ARM_MODE := arm
LOCAL_PRELINK_MODULE := false

include $(BUILD_SHARED_LIBRARY)
