LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_SRC_PATH := ../../../src/main/
LOCAL_MODULE := cannonball

ifndef SDL_JAVA_PACKAGE_PATH
$(error Please define SDL_JAVA_PACKAGE_PATH to the path of your Java package with dots replaced with underscores, for example "com_example_SanAngeles")
endif

LOCAL_CPP_EXTENSION := .cpp
LOCAL_C_INCLUDES := \
	$(LOCAL_PATH)/src \
	$(LOCAL_PATH)/../../../src/main \
	$(LOCAL_PATH)/../../../src/main/sdl \
	$(LOCAL_PATH)/../../../external \
	$(LOCAL_PATH)/../sdl-$(SDL_VERSION)/include \
	/cygdrive/c/boost_1_52_0 \

LOCAL_SRC_FILES := \
	$(LOCAL_SRC_PATH)directx/ffeedback.cpp \
	$(LOCAL_SRC_PATH)engine/audio/osound.cpp \
	$(LOCAL_SRC_PATH)engine/audio/osoundint.cpp \
	$(LOCAL_SRC_PATH)engine/oanimseq.cpp \
	$(LOCAL_SRC_PATH)engine/oattractai.cpp \
	$(LOCAL_SRC_PATH)engine/obonus.cpp \
	$(LOCAL_SRC_PATH)engine/ocrash.cpp \
	$(LOCAL_SRC_PATH)engine/oferrari.cpp \
	$(LOCAL_SRC_PATH)engine/ohiscore.cpp \
	$(LOCAL_SRC_PATH)engine/ohud.cpp \
	$(LOCAL_SRC_PATH)engine/oinitengine.cpp \
	$(LOCAL_SRC_PATH)engine/oinputs.cpp \
	$(LOCAL_SRC_PATH)engine/olevelobjs.cpp \
	$(LOCAL_SRC_PATH)engine/ologo.cpp \
	$(LOCAL_SRC_PATH)engine/omap.cpp \
	$(LOCAL_SRC_PATH)engine/omusic.cpp \
	$(LOCAL_SRC_PATH)engine/ooutputs.cpp \
	$(LOCAL_SRC_PATH)engine/opalette.cpp \
	$(LOCAL_SRC_PATH)engine/oroad.cpp \
	$(LOCAL_SRC_PATH)engine/osmoke.cpp \
	$(LOCAL_SRC_PATH)engine/osprite.cpp \
	$(LOCAL_SRC_PATH)engine/osprites.cpp \
	$(LOCAL_SRC_PATH)engine/ostats.cpp \
	$(LOCAL_SRC_PATH)engine/otiles.cpp \
	$(LOCAL_SRC_PATH)engine/otraffic.cpp \
	$(LOCAL_SRC_PATH)engine/outils.cpp \
	$(LOCAL_SRC_PATH)engine/outrun.cpp \
	$(LOCAL_SRC_PATH)frontend/config.cpp \
	$(LOCAL_SRC_PATH)frontend/menu.cpp \
	$(LOCAL_SRC_PATH)frontend/ttrial.cpp \
	$(LOCAL_SRC_PATH)hwaudio/segapcm.cpp \
	$(LOCAL_SRC_PATH)hwaudio/soundchip.cpp \
	$(LOCAL_SRC_PATH)hwaudio/ym2151.cpp \
	$(LOCAL_SRC_PATH)hwvideo/hwroad.cpp \
	$(LOCAL_SRC_PATH)hwvideo/hwsprites.cpp \
	$(LOCAL_SRC_PATH)hwvideo/hwtiles.cpp \
	$(LOCAL_SRC_PATH)sdl/audio.cpp \
	$(LOCAL_SRC_PATH)sdl/input.cpp \
	$(LOCAL_SRC_PATH)sdl/renderbase.cpp \
	src/rendergles_android.cpp \
	$(LOCAL_SRC_PATH)sdl/timer.cpp \
	$(LOCAL_SRC_PATH)main.cpp \
	src/romloader_android.cpp \
	$(LOCAL_SRC_PATH)roms.cpp \
	$(LOCAL_SRC_PATH)trackloader.cpp \
	$(LOCAL_SRC_PATH)utils.cpp \
	$(LOCAL_SRC_PATH)video.cpp \
	src/overlay_android.cpp \
	src/main_android.c \

LOCAL_CFLAGS := -DSDL_JAVA_PACKAGE_PATH=$(SDL_JAVA_PACKAGE_PATH) \
	-D__ANDROID__ -frtti -fexceptions -DWITH_GLES
LOCAL_LDLIBS := -lGLESv1_CM -lc -lm -landroid -llog -frtti -fexceptions

LOCAL_SHARED_LIBRARIES := \
	sdl-$(SDL_VERSION)

include $(BUILD_SHARED_LIBRARY)
