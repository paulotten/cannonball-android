
# Available libraries: mad (GPL-ed!) sdl_mixer sdl_image sdl_ttf sdl_net sdl_blitpool sdl_gfx sdl_sound intl xml2 lua jpeg png ogg flac tremor vorbis freetype xerces curl theora fluidsynth lzma lzo2 mikmod openal timidity zzip bzip2 yaml-cpp python boost_date_time boost_filesystem boost_iostreams boost_program_options boost_regex boost_signals boost_system boost_thread glu avcodec avdevice avfilter avformat avresample avutil swscale swresample bzip2 
APP_MODULES := cannonball sdl-1.2 png

# To filter out static libs from all libs in makefile
APP_AVAILABLE_STATIC_LIBS := 
APP_ABI := armeabi

SDL_JAVA_PACKAGE_PATH := com_octodev_cannonball

SDL_CURDIR_PATH := com.octodev.cannonball

SDL_TRACKBALL_KEYUP_DELAY := 1

SDL_VIDEO_RENDER_RESIZE := 1

COMPILED_LIBRARIES := 

APPLICATION_ADDITIONAL_CFLAGS := -finline-functions -O2 -DBUILD_TYPE=LINUX32 -DTARGET_LNX=1 -Werror=strict-aliasing -Werror=cast-align -Werror=pointer-arith -Werror=address
APPLICATION_ADDITIONAL_LDFLAGS := 
APPLICATION_OVERLAPS_SYSTEM_HEADERS := n
APPLICATION_SUBDIRS_BUILD := src/*
APPLICATION_BUILD_EXCLUDE := 
APPLICATION_CUSTOM_BUILD_SCRIPT := 

SDL_ADDITIONAL_CFLAGS := -DSDL_ANDROID_KEYCODE_MOUSE=UNKNOWN -DSDL_ANDROID_KEYCODE_0=LCTRL -DSDL_ANDROID_KEYCODE_1=LALT -DSDL_ANDROID_KEYCODE_2=SPACE -DSDL_ANDROID_KEYCODE_3=RETURN -DSDL_ANDROID_KEYCODE_4=RETURN
SDL_VERSION := 1.2

