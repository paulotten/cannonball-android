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
	const static int START_MASK = (1 << COIN) | (1 << MENU);
	const static int MUSIC_MASK = (1 << COIN) | (1 << START) | (1 << MENU) | 
		(1 << DPAD_LEFT) | (1 << DPAD_RIGHT);
	const static int BEST_MASK = (1 << MENU) | (1 << DPAD_LEFT) | (1 << DPAD_RIGHT) | (1 << ACCEL);
	const static int INGAME_MASK = FRONTEND_MASK | (1 << DPAD_LEFT) | (1 << DPAD_RIGHT) | 
		(1 << BRAKE) | (1 << GEAR) | (1 << MENU);
		
	const static int NORMAL_ATLAS = 0;
	const static int PRESSED_ATLAS = 1;
	
	Overlay (void);
	~Overlay (void);

	void init (void);
	
	void load (void);
	
	void unload (void);

	void tick (void);

	void draw (void);

    static int map_control_overlay (int control);

private:

	int filesize(const char*);

	uint32_t texture_atlas[2];
	uint32_t texture_width;
	uint32_t texture_height;
	
	uint16_t loaded;

	uint16_t active_panels;
	uint16_t pressed_panels;
	quad_t panels[PANEL_COUNT];
	
};

extern Overlay overlay;
