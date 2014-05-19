/***************************************************************************
Overlay.

- Renders the System 16 Video Layers
- Handles Reads and Writes to these layers from the main game code
- Interfaces with platform specific rendering code

Copyright Chris White.
See license.txt for more details.
***************************************************************************/

#include <iostream>

#include "android_debug.h"

#include "overlay.hpp"

#include <stb_image.c>

Overlay overlay;

Overlay::Overlay(void)
{
}

Overlay::~Overlay(void)
{
}

void Overlay::init(void)
{
	int x, y, comp;

	std::ifstream src("", std::ios::in | std::ios::binary);
	if (!src)
	{
		std::cout << "cannot open rom: " << "" << std::endl;
		//return 1; // fail
	}

	// Read file
	unsigned char *data = new unsigned char[length];
	src.read(data, length);

	data = stbi_load_from_memory(data, &x, &y, &comp, 0);
	fclose(file);

	//assign texture

	stbi_image_free(data);
}

void Overlay::tick(void)
{

}

void Overlay::draw(void)
{

}