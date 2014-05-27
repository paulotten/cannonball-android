
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

#define ASSIGN_VERTEX(QUAD, X, Y, U, V) \
	QUAD.pos[0] = X; QUAD.pos[1] = Y; \
	QUAD.texcoord[0] = U; QUAD.texcoord[1] = V;

#define ASSIGN_QUAD_FROM_BBOX(QUAD, BOX_POS, BOX_TEX, TEX_SCALE) \
	QUAD.vertices[0].pos[0] = BOX_POS.min[0]; QUAD.vertices[0].pos[1] = BOX_POS.max[1]; \
	QUAD.vertices[1].pos[0] = BOX_POS.min[0]; QUAD.vertices[1].pos[1] = BOX_POS.min[1]; \
	QUAD.vertices[2].pos[0] = BOX_POS.max[0]; QUAD.vertices[2].pos[1] = BOX_POS.max[1]; \
	QUAD.vertices[3].pos[0] = BOX_POS.max[0]; QUAD.vertices[3].pos[1] = BOX_POS.min[1]; \
	QUAD.vertices[0].texcoord[0] = ((double)BOX_TEX.min[0]) / TEX_SCALE; QUAD.vertices[0].texcoord[1] = ((double)BOX_TEX.max[1]) / TEX_SCALE; \
	QUAD.vertices[1].texcoord[0] = ((double)BOX_TEX.min[0]) / TEX_SCALE; QUAD.vertices[1].texcoord[1] = ((double)BOX_TEX.min[1]) / TEX_SCALE; \
	QUAD.vertices[2].texcoord[0] = ((double)BOX_TEX.max[0]) / TEX_SCALE; QUAD.vertices[2].texcoord[1] = ((double)BOX_TEX.max[1]) / TEX_SCALE; \
	QUAD.vertices[3].texcoord[0] = ((double)BOX_TEX.max[0]) / TEX_SCALE; QUAD.vertices[3].texcoord[1] = ((double)BOX_TEX.min[1]) / TEX_SCALE; 
