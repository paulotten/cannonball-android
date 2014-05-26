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
#include "quad.hpp"

class Overlay
{
public:

	Overlay();
	~Overlay();

	void init();

	void tick();

	void draw();

	int active;

private:

	int filesize(const char*);

	// Panel IDs
	const static int DPAD = 0;
	const static int ACCEL = 1;
	const static int BRAKE = 2;
	const static int GEAR = 3;
	const static int MENU = 4;

	const static int PANEL_COUNT = MENU + 1;

	uint32_t textureAtlas;

	quad_t panels[PANEL_COUNT];
};

extern Overlay overlay;
