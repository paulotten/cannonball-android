
#pragma once

typedef struct
{
	int min[2];
	int max[2];
} bounding_box_t;

#define ASSIGN_BOUNDING_BOX(BOX, MIN_X, MIN_Y, MAX_X, MAX_Y) \
	BOX.min[0] = MIN_X; BOX.min[1] = MIN_Y; \
	BOX.max[0] = MAX_X; BOX.max[1] = MAX_Y;

#define CHECK_BOUNDING_BOX(BOX, X, Y) \
	(BOX.min[0] <= X && BOX.max[0] >= X && \
	BOX.min[1] <= Y && BOX.max[1] >= Y)
