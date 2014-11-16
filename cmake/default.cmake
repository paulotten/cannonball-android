# Default CMake Setup. Used for Win32 Builds.
# Uses OpenGL for Graphics.
# For DirectX, assumes DXSDK_DIR is already configured and pointing to your DirectX setup.

set(sdl_root ../external/sdl-1.2)

if (NOT DEFINED ENV{DXSDK_DIR})
    message(FATAL_ERROR "Warning: DirectX SDK Variable DXSDK_DIR Not Defined!")
endif()

# Use OpenGL for rendering.
set(OPENGL 1)

# Use CannonBoard Serial Support
set(CANNONBOARD 0)

include_directories(
    "${sdl_root}/include"
)

link_libraries(cannonball 
    SDL
    SDLmain
    opengl32 # For OpenGL
    glu32    # For OpenGL
    dxguid   # Direct X Haptic Support
    dinput8  # Direct X Haptic Support
)

# Linking
link_directories(
    "$ENV{DXSDK_DIR}/lib"
    "${sdl_root}/lib"
)

if (CANNONBOARD)
    # Search for additional boost libraries needed for CannonBoard support: Threading & System
    set(BOOST_INCLUDEDIR ${lib_base}/boost_1_54_0)
    find_package(Boost COMPONENTS thread system REQUIRED)
    
    link_libraries(cannonball
        ${Boost_LIBRARIES}
    )

    link_directories(
        ${Boost_LIBRARY_DIRS}
    )
    
    # Windows 32 C++ Flags for Serial Port Support
    add_definitions(-D_WIN32_WINNT=0x0501)

    # This will tell the Boost config system not to automatically select which libraries to link against.
    add_definitions(-DBOOST_ALL_NO_LIB)
endif()

# Location for Cannonball to create save files
# Used to auto-generate setup.hpp with various file paths
set(xml_directory ./)

# SDL Software Rendering Flags (ignored if OpenGL used)
set(sdl_flags "SDL_SWSURFACE | SDL_DOUBLEBUF")