# The ARMv7 is significanly faster due to the use of the hardware FPU
APP_BUILD_SCRIPT := $(call my-dir)/highlight/Android.mk \
    $(call my-dir)/charset-detector/Android.mk
