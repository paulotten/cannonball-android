/***************************************************************************
    Overlay. 
    
    - Renders the System 16 Video Layers
    - Handles Reads and Writes to these layers from the main game code
    - Interfaces with platform specific rendering code

    Copyright Chris White.
    See license.txt for more details.
***************************************************************************/

#include <iostream>
#include <fstream>

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
	int x, y, comp, length;
	unsigned char *data;

	std::string path = "res/overlay/main.png";
	std::ifstream src(path.c_str(), std::ios::in | std::ios::binary);
	if (!src)
	{
		std::cout << "cannot open rom: " << "" << std::endl;
		//return 1; // fail
	}

	// Read file
	char* buffer = new char[length];
	src.read(buffer, length);

	data = stbi_load_from_memory((unsigned char*)buffer, length, &x, &y, &comp, 0);

	delete[] buffer;
	src.close();

	//assign texture

	stbi_image_free(data);

	glGenTextures(1, &textureAtlas); 
	
	glBindTexture(GL_TEXTURE_2D, textureAtlas);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
	glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA,
		1, 1, 0,                // texture width, texture height
		GL_RGBA, GL_UNSIGNED_SHORT_5_6_5,    // Data format in pixel array
		NULL);

	// --------------------------------------------------------------------------------------------
	// Initalize Panel Quads
	// --------------------------------------------------------------------------------------------
	
	ASSIGN_VERTEX(panels[DPAD].vertices[0], 0, 1, 0, 1)
	ASSIGN_VERTEX(panels[DPAD].vertices[1], 0, 0, 0, 0)
	ASSIGN_VERTEX(panels[DPAD].vertices[2], 1, 1, 1, 1)
	ASSIGN_VERTEX(panels[DPAD].vertices[3], 1, 0, 1, 0)

	ASSIGN_VERTEX(panels[ACCEL].vertices[0], 0, 1, 0, 1)
	ASSIGN_VERTEX(panels[ACCEL].vertices[1], 0, 0, 0, 0)
	ASSIGN_VERTEX(panels[ACCEL].vertices[2], 1, 1, 1, 1)
	ASSIGN_VERTEX(panels[ACCEL].vertices[3], 1, 0, 1, 0)

	ASSIGN_VERTEX(panels[BRAKE].vertices[0], 0, 1, 0, 1)
	ASSIGN_VERTEX(panels[BRAKE].vertices[1], 0, 0, 0, 0)
	ASSIGN_VERTEX(panels[BRAKE].vertices[2], 1, 1, 1, 1)
	ASSIGN_VERTEX(panels[BRAKE].vertices[3], 1, 0, 1, 0)

	ASSIGN_VERTEX(panels[GEAR].vertices[0], 0, 1, 0, 1)
	ASSIGN_VERTEX(panels[GEAR].vertices[1], 0, 0, 0, 0)
	ASSIGN_VERTEX(panels[GEAR].vertices[2], 1, 1, 1, 1)
	ASSIGN_VERTEX(panels[GEAR].vertices[3], 1, 0, 1, 0)

	ASSIGN_VERTEX(panels[MENU].vertices[0], 0, 1, 0, 1)
	ASSIGN_VERTEX(panels[MENU].vertices[1], 0, 0, 0, 0)
	ASSIGN_VERTEX(panels[MENU].vertices[2], 1, 1, 1, 1)
	ASSIGN_VERTEX(panels[MENU].vertices[3], 1, 0, 1, 0)
}

void Overlay::tick(void)
{
	//check if buttons are pushed hopefully via SDL_MouseButtonEvent
}

void Overlay::draw(void)
{

}