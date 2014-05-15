
#include <jni.h>
#include <errno.h>

#include <android/sensor.h>
#include <android/log.h>
#include <android/input.h>
#include <android/asset_manager.h>

#include "SDL_version.h"
#include "SDL_thread.h"
#include "SDL_main.h"

#include "SDL_android.h"

#ifdef __cplusplus
#define C_LINKAGE "C"
#else
#define C_LINKAGE
#endif

#ifndef SDL_JAVA_PACKAGE_PATH
#error You have to define SDL_JAVA_PACKAGE_PATH to your package path with dots replaced with underscores, for example "com_example_SanAngeles"
#endif
#define JAVA_EXPORT_NAME2(name,package) Java_##package##_##name
#define JAVA_EXPORT_NAME1(name,package) JAVA_EXPORT_NAME2(name,package)
#define JAVA_EXPORT_NAME(name) JAVA_EXPORT_NAME1(name,SDL_JAVA_PACKAGE_PATH)

static int argc = 0;
static char ** argv = NULL;

AAssetManager * __assetManager = NULL;

extern C_LINKAGE void
JAVA_EXPORT_NAME(DemoRenderer_nativeInit) (JNIEnv * env, jobject obj, jobject assetManager)
{
	__assetManager = (AAssetManager *)AAssetManager_fromJava(env, assetManager);
	
	if (__assetManager == NULL)
	{
		__android_log_print(ANDROID_LOG_ERROR, "libSDL", "Error Assigning AAssetManager");
	}
	else
	{
		__android_log_print(ANDROID_LOG_INFO, "libSDL", "Assigned AAssetManager");
	}

	__android_log_print(ANDROID_LOG_INFO, "libSDL", "Calling SDL_main");

	SDL_main(argc, argv);
}
