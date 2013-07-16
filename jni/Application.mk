# The ARMv7 is significanly faster due to the use of the hardware FPU
APP_ABI := armeabi armeabi-v7a
APP_PLATFORM ：= android-8

APP_BUILD_SCRIPT := $(call my-dir)/highlight/Android.mk \
    $(call my-dir)/charset-detector/Android.mk  \
    $(call my-dir)/grep/Android.mk
