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
#include "main.hpp"
#include "overlay.hpp"
#include "video.hpp"
#include "sdl\input.hpp"
#include "engine\outrun.hpp"

#include "android_debug.h"
#include <GLES/gl.h>
#include <stb_image.c>

#include <unistd.h>
#include <jni.h>

#include <android/asset_manager.h>
extern AAssetManager * __assetManager;

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

	std::string path = "overlay/main_normal.png"; //put into const
	AAsset * asset = AAssetManager_open(__assetManager, path.c_str(), 3);
	if (!asset)
	{
		printf("cannot open overlay/main_normal.png");
	}

	length = static_cast<int>(AAsset_getLength(asset));

	// Read file
	char * buffer = new char[length];
	AAsset_read(asset, buffer, length);

	data = stbi_load_from_memory((unsigned char*)buffer, length, &x, &y, &comp, 0);

	delete[] buffer;
	AAsset_close(asset);

	//assign texture
	glGenTextures(1, &texture_atlas);

	glBindTexture(GL_TEXTURE_2D, texture_atlas);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
	glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA,
		x, y, 0,								// texture width, texture height
		GL_RGBA, GL_UNSIGNED_BYTE,    // Data format in pixel array
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
}

void Overlay::tick(void)
{
	if (cannonball::state == cannonball::STATE_MENU)
	{
		active_panels = FRONTEND_MASK;
		input.active_panels = Input::FRONTEND_MASK;
	}
	else if (cannonball::state == cannonball::STATE_GAME)
	{
		switch (outrun.game_state)
		{
		case GS_INIT_GAME:
		case GS_START1:
		case GS_START2:
		case GS_START3:
		case GS_INGAME:
		{
			active_panels = INGAME_MASK;
			input.active_panels = Input::INGAME_MASK;
			break;
		}
		case GS_ATTRACT:
		case GS_INIT_LOGO:
		case GS_LOGO:
		{
			active_panels = START_MASK;
			input.active_panels = Input::START_MASK;
			break;
		}
		case GS_INIT_MUSIC:
		case GS_MUSIC:
		{
			active_panels = MUSIC_MASK;
			input.active_panels = Input::MUSIC_MASK;
			break;
		}
		case GS_INIT_BEST2:
		case GS_BEST2:
		{
			active_panels = BEST_MASK;
			input.active_panels = Input::BEST_MASK;
			break;
		}
		default:
		{
			active_panels = 0;
			input.active_panels = 0;
			break;
		}
		}
	}
	else
	{
		active_panels = 0;
		input.active_panels = 0;
	}
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

	glOrthof(0, video.get_scn_width(), video.get_scn_height(), 0, 0, 1);         // left, right, bottom, top, near, far

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

int Overlay::filesize(const char*)
{
	return 0;
}