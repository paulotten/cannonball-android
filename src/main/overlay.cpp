/***************************************************************************
    Overlay. 
    
    - Renders the System 16 Video Layers
    - Handles Reads and Writes to these layers from the main game code
    - Interfaces with platform specific rendering code

    Copyright Chris White.
    See license.txt for more details.
***************************************************************************/

//implement overlay.active into engine/outrun.cpp
//GS_START1, GS_START2, GS_START3, GS_INGAME

#include <iostream>
#include <fstream>

#include "overlay.hpp"
#include "video.hpp"

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
	stbi_uc* data;

	std::string path = "res/overlay/main.png"; //put into const
	std::ifstream src(path.c_str(), std::ios::in | std::ios::binary);
	if (!src)
	{
		std::cout << "cannot open rom: " << "" << std::endl;
		//return 1; // fail
	}

	length = filesize(path.c_str());

	// Read file
	char* buffer = new char[length];
	src.read(buffer, length);

	data = stbi_load_from_memory((unsigned char*)buffer, length, &x, &y, &comp, 0);

	delete[] buffer;
	src.close();

	//assign texture
	glGenTextures(1, &textureAtlas); 
	
	glBindTexture(GL_TEXTURE_2D, textureAtlas);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
	glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA,
		x, y, 0,								// texture width, texture height
		GL_RGBA, GL_UNSIGNED_INT_8_8_8_8_REV,    // Data format in pixel array
		data);

	stbi_image_free(data);

	// --------------------------------------------------------------------------------------------
	// Initalize Panel Quads
	// --------------------------------------------------------------------------------------------
	
	ASSIGN_VERTEX(panels[DPAD].vertices[0], 0, 128, 0, 0.5)
	ASSIGN_VERTEX(panels[DPAD].vertices[1], 0, 0, 0, 0)
	ASSIGN_VERTEX(panels[DPAD].vertices[2], 128, 128, 0.5, 0.5)
	ASSIGN_VERTEX(panels[DPAD].vertices[3], 128, 0, 0.5, 0)

	ASSIGN_VERTEX(panels[ACCEL].vertices[0], 128, 128, 0, 1)
	ASSIGN_VERTEX(panels[ACCEL].vertices[1], 128, 0, 0, 0.5)
	ASSIGN_VERTEX(panels[ACCEL].vertices[2], 256, 128, 0.5, 1)
	ASSIGN_VERTEX(panels[ACCEL].vertices[3], 256, 0, 0.5, 0.5)

	ASSIGN_VERTEX(panels[BRAKE].vertices[0], 0, 1, 0.6, 1)
	ASSIGN_VERTEX(panels[BRAKE].vertices[1], 0, 0, 0.6, 0)
	ASSIGN_VERTEX(panels[BRAKE].vertices[2], 1, 1, 1, 1)
	ASSIGN_VERTEX(panels[BRAKE].vertices[3], 1, 0, 1, 0)

	ASSIGN_VERTEX(panels[GEAR].vertices[0], 0, 1, 0, 1)
	ASSIGN_VERTEX(panels[GEAR].vertices[1], 0, 0, 0, 0)
	ASSIGN_VERTEX(panels[GEAR].vertices[2], 1, 1, 1, 1)
	ASSIGN_VERTEX(panels[GEAR].vertices[3], 1, 0, 1, 0)

	//?
	ASSIGN_VERTEX(panels[MENU].vertices[0], 0, 1, 0, 1)
	ASSIGN_VERTEX(panels[MENU].vertices[1], 0, 0, 0, 0)
	ASSIGN_VERTEX(panels[MENU].vertices[2], 1, 1, 1, 1)
	ASSIGN_VERTEX(panels[MENU].vertices[3], 1, 0, 1, 0)

	active = true;
}

void Overlay::tick(void)
{
	//check if buttons are pushed hopefully via SDL_MouseButtonEvent
}

void Overlay::draw(void)
{
	glMatrixMode(GL_MODELVIEW);
	glPushMatrix();
	glLoadIdentity();

	glOrtho(0, video.get_scn_width(), video.get_scn_height(), 0, 0, 1);         // left, right, bottom, top, near, far

	glEnableClientState(GL_VERTEX_ARRAY);
	glEnableClientState(GL_TEXTURE_COORD_ARRAY);

	glEnable(GL_BLEND);
	glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

	glEnable(GL_TEXTURE_2D);
	glBindTexture(GL_TEXTURE_2D, textureAtlas);

	for (int i = 0; i < 5; ++i)
	{
		glVertexPointer(2, GL_FLOAT, sizeof(vertex_t), panels[i].vertices[0].pos);
		glTexCoordPointer(2, GL_FLOAT, sizeof(vertex_t), panels[i].vertices[0].texcoord);
		glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
	}

	glDisable(GL_TEXTURE_2D);

	glDisable(GL_BLEND);

	glDisableClientState(GL_VERTEX_ARRAY);
	glDisableClientState(GL_TEXTURE_COORD_ARRAY);

	glPopMatrix();
}

int Overlay::filesize(const char* filename)
{
	std::ifstream in(filename, std::ifstream::in | std::ifstream::binary);
	in.seekg(0, std::ifstream::end);
	int size = (int)in.tellg();
	in.close();
	return size;
}