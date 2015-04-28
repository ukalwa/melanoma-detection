LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

include C:/Users/Upender/Downloads/OpenCV-android-sdk/sdk/native/jni/OpenCV.mk

LOCAL_MODULE    := active_contour
LOCAL_SRC_FILES := jni_part.cpp ac_withoutedges_yuv.cpp activecontour.cpp linked_list.cpp
LOCAL_LDLIBS +=  -llog -ldl

include $(BUILD_SHARED_LIBRARY)
