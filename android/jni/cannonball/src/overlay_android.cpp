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
#include "video.hpp"
#include "engine\outrun.hpp"

#include <stb_image.c>

#include "android_debug.h"
#include <GLES/gl.h>

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
	stbi_uc* data, * data4444;

	std::string path = "overlay/main.png"; //put into const
	AAsset * asset = AAssetManager_open(__assetManager, path.c_str(), 3);
	if (!asset)
	{
		printf("cannot open overlay/main.png");
	}

	length = static_cast<int>(AAsset_getLength(asset));

	// Read file
	char * buffer = new char[length];
	AAsset_read(asset, buffer, length);

	data = stbi_load_from_memory((unsigned char*)buffer, length, &x, &y, &comp, 0);

	delete[] buffer;
	AAsset_close(asset);

	printf("overlay/main.png processed");

	data4444 = new stbi_uc[length / 2];

	stbi_uc c = 0;
	for (int i = 0; i < length; i += 2)
	{
		c = (data[i] & 0xF0) | (data[i+1] >> 4);
		data4444[i / 2] = c;
	}

	printf("overlay/main.png processed 1");

	//assign texture
	glGenTextures(1, &textureAtlas);

	glBindTexture(GL_TEXTURE_2D, textureAtlas);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
	glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA,
		x, y, 0,								// texture width, texture height
		GL_RGBA, GL_UNSIGNED_SHORT_4_4_4_4,    // Data format in pixel array
		data4444);

	printf("overlay/main.png processed 2");

	delete[] data4444;
	stbi_image_free(data);

	printf("overlay/main.png processed 3");

	// --------------------------------------------------------------------------------------------
	// Initalize Panel Quads
	// --------------------------------------------------------------------------------------------

	int scn_width = video.get_scn_width();
	int scn_height = video.get_scn_height();

	ASSIGN_VERTEX(panels[DPAD].vertices[0], 32, scn_height - 64, 0, 0.5)
	ASSIGN_VERTEX(panels[DPAD].vertices[1], 32, scn_height - 128 - 64, 0, 0)
	ASSIGN_VERTEX(panels[DPAD].vertices[2], 128 + 32, scn_height - 64, 0.5, 0.5)
	ASSIGN_VERTEX(panels[DPAD].vertices[3], 128 + 32, scn_height - 128 - 64, 0.5, 0)

	ASSIGN_VERTEX(panels[ACCEL].vertices[0], scn_width - 128 - 32, scn_height - 64, 0, 1)
	ASSIGN_VERTEX(panels[ACCEL].vertices[1], scn_width - 128 - 32, scn_height - 128 - 64, 0, 0.5)
	ASSIGN_VERTEX(panels[ACCEL].vertices[2], scn_width - 32, scn_height - 64, 0.5, 1)
	ASSIGN_VERTEX(panels[ACCEL].vertices[3], scn_width - 32, scn_height - 128 - 64, 0.5, 0.5)

	ASSIGN_VERTEX(panels[BRAKE].vertices[0], scn_width - 128 - 96 - 32, scn_height - 64, 0.6, 1)
	ASSIGN_VERTEX(panels[BRAKE].vertices[1], scn_width - 128 - 96 - 32, scn_height - 96 - 64, 0.6, 0.6)
	ASSIGN_VERTEX(panels[BRAKE].vertices[2], scn_width - 128 - 32, scn_height - 64, 1, 1)
	ASSIGN_VERTEX(panels[BRAKE].vertices[3], scn_width - 128 - 32, scn_height - 96 - 64, 1, 0.6)

	ASSIGN_VERTEX(panels[GEAR].vertices[0], scn_width - 96 - 32, scn_height - 128 - 64, 0.6, 0.6)
	ASSIGN_VERTEX(panels[GEAR].vertices[1], scn_width - 96 - 32, scn_height - 128 - 96 - 64, 0.6, 0.2)
	ASSIGN_VERTEX(panels[GEAR].vertices[2], scn_width - 32, scn_height - 128 - 64, 1, 0.6)
	ASSIGN_VERTEX(panels[GEAR].vertices[3], scn_width - 32, scn_height - 128 - 96 - 64, 1, 0.2)

	ASSIGN_VERTEX(panels[MENU].vertices[0], 0, 1, 0, 1)
	ASSIGN_VERTEX(panels[MENU].vertices[1], 0, 0, 0, 0)
	ASSIGN_VERTEX(panels[MENU].vertices[2], 1, 1, 1, 1)
	ASSIGN_VERTEX(panels[MENU].vertices[3], 1, 0, 1, 0)

	active = true;
}

void Overlay::tick(void)
{
	active = outrun.game_state == GS_START1
		|| outrun.game_state == GS_START2
		|| outrun.game_state == GS_START3
		|| outrun.game_state == GS_INGAME;

	if (active)
	{


		//check if buttons are pushed hopefully via SDL_MouseButtonEvent
	}
}

void Overlay::draw(void)
{
	printf("overlay draw");

	glMatrixMode(GL_MODELVIEW);
	glPushMatrix();
	glLoadIdentity();

	glOrthof(0, video.get_scn_width(), video.get_scn_height(), 0, 0, 1);         // left, right, bottom, top, near, far

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

int Overlay::filesize(const char*)
{
	return 0;
}