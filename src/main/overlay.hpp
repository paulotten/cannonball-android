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

    enum panels
    {
		DPAD_LEFT = 0,
		DPAD_RIGHT = 1,
		DPAD_UP = 2,
		DPAD_DOWN = 3,
		ACCEL = 4,
		BRAKE = 5,
		GEAR = 6,
		COIN = 7,
		
		START = 8,
		
		MENU = 9,
	};

	const static int PANEL_COUNT = 10;

	const static int FRONTEND_MASK = (1 << DPAD_UP) | (1 << DPAD_DOWN) | (1 << ACCEL);
	const static int INGAME_MASK = FRONTEND_MASK | (1 << DPAD_LEFT) | (1 << DPAD_RIGHT);
	
	Overlay();
	~Overlay();

	void init();

	void tick();

	void draw();

private:

	int filesize(const char*);

	uint32_t texture_atlas;

	uint16_t active_panels;
	quad_t panels[PANEL_COUNT];
	
};

extern Overlay overlay;
