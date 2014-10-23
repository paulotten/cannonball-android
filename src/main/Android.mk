LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := cannonball

ifndef SDL_JAVA_PACKAGE_PATH
$(error Please define SDL_JAVA_PACKAGE_PATH to the path of your Java package with dots replaced with underscores, for example "com_example_SanAngeles")
endif

LOCAL_CPP_EXTENSION := .cpp
LOCAL_C_INCLUDES := \
	$(LOCAL_PATH)/sdl \
	$(LOCAL_PATH)/../../external/stb \
	$(LOCAL_PATH)/../../external/sdl-$(SDL_VERSION)/include \
	/cygdrive/c/common/boost_1_55_0 \

LOCAL_SRC_FILES := \
	directx/ffeedback.cpp \
	engine/audio/osound.cpp \
	engine/audio/osoundint.cpp \
	engine/oanimseq.cpp \
	engine/oattractai.cpp \
	engine/obonus.cpp \
	engine/ocrash.cpp \
	engine/oferrari.cpp \
	engine/ohiscore.cpp \
	engine/ohud.cpp \
	engine/oinitengine.cpp \
	engine/oinputs.cpp \
	engine/olevelobjs.cpp \
	engine/ologo.cpp \
	engine/omap.cpp \
	engine/omusic.cpp \
	engine/ooutputs.cpp \
	engine/opalette.cpp \
	engine/oroad.cpp \
	engine/osmoke.cpp \
	engine/osprite.cpp \
	engine/osprites.cpp \
	engine/ostats.cpp \
	engine/otiles.cpp \
	engine/otraffic.cpp \
	engine/outils.cpp \
	engine/outrun.cpp \
	frontend/config.cpp \
	frontend/menu.cpp \
	frontend/ttrial.cpp \
	hwaudio/segapcm.cpp \
	hwaudio/soundchip.cpp \
	hwaudio/ym2151.cpp \
	hwvideo/hwroad.cpp \
	hwvideo/hwsprites.cpp \
	hwvideo/hwtiles.cpp \
	sdl/audio.cpp \
	sdl/input.cpp \
	sdl/renderbase.cpp \
	sdl/rendergles_android.cpp \
	sdl/timer.cpp \
	main.cpp \
	romloader_android.cpp \
	roms.cpp \
	trackloader.cpp \
	utils.cpp \
	video.cpp \
	overlay_android.cpp \
	main_android.c \

LOCAL_CFLAGS := -DSDL_JAVA_PACKAGE_PATH=$(SDL_JAVA_PACKAGE_PATH) \
	-D__ANDROID__ -frtti -fexceptions -DWITH_GLES -DSTB_IMAGE_IMPLEMENTATION
LOCAL_LDLIBS := -lGLESv1_CM -lc -lm -landroid -llog -frtti -fexceptions

LOCAL_SHARED_LIBRARIES := \
	sdl-$(SDL_VERSION)

include $(BUILD_SHARED_LIBRARY)
