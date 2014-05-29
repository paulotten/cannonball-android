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

#include "setup.hpp"
#include "overlay.hpp"
#include "video.hpp"
#include "sdl\input.hpp"
#include "engine\outrun.hpp"

#include <SDL_opengl.h>
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

	std::ifstream src(FILENAME_OVERLAY, std::ios::in | std::ios::binary);
	if (!src)
	{
		std::cout << "cannot open rom: " << "" << std::endl;
		//return 1; // fail
	}

	length = filesize(FILENAME_OVERLAY);

	// Read file
	char* buffer = new char[length];
	src.read(buffer, length);

	data = stbi_load_from_memory((unsigned char*)buffer, length, &x, &y, &comp, 0);

	delete[] buffer;
	src.close();

	//assign texture
	glGenTextures(1, &texture_atlas); 
	
	glBindTexture(GL_TEXTURE_2D, texture_atlas);
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

	int scn_width = video.get_scn_width();
	int scn_height = video.get_scn_height();

	ASSIGN_QUAD_FROM_BBOX(panels[DPAD_LEFT], config.overlay.panel_pos[DPAD_LEFT], config.overlay.panel_texcoord[DPAD_LEFT], x)
	ASSIGN_QUAD_FROM_BBOX(panels[DPAD_RIGHT], config.overlay.panel_pos[DPAD_RIGHT], config.overlay.panel_texcoord[DPAD_RIGHT], x)
	ASSIGN_QUAD_FROM_BBOX(panels[DPAD_UP], config.overlay.panel_pos[DPAD_UP], config.overlay.panel_texcoord[DPAD_UP], x)
	ASSIGN_QUAD_FROM_BBOX(panels[DPAD_DOWN], config.overlay.panel_pos[DPAD_DOWN], config.overlay.panel_texcoord[DPAD_DOWN], x)
	ASSIGN_QUAD_FROM_BBOX(panels[ACCEL], config.overlay.panel_pos[ACCEL], config.overlay.panel_texcoord[ACCEL], x)
	ASSIGN_QUAD_FROM_BBOX(panels[BRAKE], config.overlay.panel_pos[BRAKE], config.overlay.panel_texcoord[BRAKE], x)
	ASSIGN_QUAD_FROM_BBOX(panels[GEAR], config.overlay.panel_pos[GEAR], config.overlay.panel_texcoord[GEAR], x)
	ASSIGN_QUAD_FROM_BBOX(panels[COIN], config.overlay.panel_pos[COIN], config.overlay.panel_texcoord[COIN], x)
	ASSIGN_QUAD_FROM_BBOX(panels[START], config.overlay.panel_pos[START], config.overlay.panel_texcoord[START], x)
	ASSIGN_QUAD_FROM_BBOX(panels[MENU], config.overlay.panel_pos[MENU], config.overlay.panel_texcoord[MENU], x)

	active_panels = INGAME_MASK | (1 << MENU) | (1 << BRAKE) | (1 << GEAR) | (1 << COIN);
}

void Overlay::draw(void)
{
	if (active_panels == 0)
	{
		return;
	}

	glMatrixMode(GL_MODELVIEW);
	glPushMatrix();
	glLoadIdentity();

	glOrtho(0, video.get_scn_width(), video.get_scn_height(), 0, 0, 1);    

	glEnableClientState(GL_VERTEX_ARRAY);
	glEnableClientState(GL_TEXTURE_COORD_ARRAY);

	glEnable(GL_BLEND);
	glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

	glEnable(GL_TEXTURE_2D);
	glBindTexture(GL_TEXTURE_2D, texture_atlas);

	for (uint8_t i = 0; i < PANEL_COUNT; ++i)
	{
		if (((active_panels >> i) & 1) > 0)
		{
			glVertexPointer(2, GL_FLOAT, sizeof(vertex_t), panels[i].vertices[0].pos);
			glTexCoordPointer(2, GL_FLOAT, sizeof(vertex_t), panels[i].vertices[0].texcoord);
			glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
		}
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
