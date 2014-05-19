
#pragma once

typedef struct
{
	float pos[2];
	float texcoord[2];
} vertex_t;

typedef struct
{
	vertex_t vertices[4];
} quad_t;

#define ASSIGN_VERTEX(o, x, y, u, v) \
	o.pos[0] = x; o.pos[1] = y; \
	o.texcoord[0] = u; o.texcoord[1] = v;
