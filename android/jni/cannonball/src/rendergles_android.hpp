/***************************************************************************
    Open GL ES Video Rendering.  
    
    Useful References:
    http://www.sdltutorials.com/sdl-opengl-tutorial-basics
    http://www.opengl.org/wiki/Common_Mistakes
    http://open.gl/textures

    Copyright Chris White.
    See license.txt for more details.
***************************************************************************/

#pragma once

#include "renderbase.hpp"
#include "quad.hpp"

#include <GLES/gl.h>

typedef struct
{
	GLfloat pos[2];
	GLfloat texcoord[2];
} vertex_t;

typedef struct
{
	vertex_t vertices[4];
} quad_t;

#define ASSIGN_VERTEX(o, x, y, u, v) \
	o.pos[0] = x; o.pos[1] = y; \
	o.texcoord[0] = u; o.texcoord[1] = v;

class RenderGLES : public RenderBase
{

public:

	RenderGLES();
    bool init(int src_width, int src_height, 
              int scale,
              int video_mode,
              int scanlines);
    void disable();
    bool start_frame();
    bool finalize_frame();
    void draw_frame(uint16_t* pixels);

private:

    // Texture IDs
    const static int SCREEN = 0;
    const static int SCANLN = 1;

    GLuint textures[2];

	quad_t screen[2];

};