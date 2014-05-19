/***************************************************************************
    Overlay. 
    
    - Renders the System 16 Video Layers
    - Handles Reads and Writes to these layers from the main game code
    - Interfaces with platform specific rendering code

    Copyright Chris White.
    See license.txt for more details.
***************************************************************************/

#pragma once

#include "stdint.hpp"
#include "globals.hpp"
#include "quad.hpp"

#include <SDL_opengl.h>

class Overlay
{
public:

	Overlay();
	~Overlay();

	void init();

	void tick();

	void draw();

private:

	// Panel IDs
	const static int DPAD = 0;
	const static int ACCEL = 1;
	const static int BRAKE = 2;
	const static int GEAR = 3;
	const static int MENU = 4;

	GLuint textureAtlas;

	quad_t panels[5];

};

extern Overlay overlay;
