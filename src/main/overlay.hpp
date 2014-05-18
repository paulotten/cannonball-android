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

class Overlay
{
public:

	Overlay();
	~Overlay();

private:

	void loadPNG(char*);

};

extern Overlay overlay;
