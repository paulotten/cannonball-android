/***************************************************************************
    SDL Based Input Handling.

    Populates keys array with user input.
    If porting to a non-SDL platform, you would need to replace this class.

    Copyright Chris White.
    See license.txt for more details.
***************************************************************************/

#pragma once

#include <SDL.h>

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

class Input
{
public:

    enum presses
    {
        LEFT  = 0,
        RIGHT = 1,
        UP    = 2,
        DOWN  = 3,
        ACCEL = 4,
        BRAKE = 5,
        GEAR1 = 6,
        GEAR2 = 7,

        START = 8,
        COIN  = 9,
        VIEWPOINT = 10,
        
        PAUSE = 11,
        STEP  = 12,
        TIMER = 13,
        MENU = 14,     
    };

    bool keys[15];
    bool keys_old[15];

	bool touch;

    // Has gamepad been found?
    bool gamepad;

    // Use analog controls
    int analog;

    // Latch last key press for redefines
    int key_press;

    // Latch last joystick button press for redefines
    int16_t joy_button;

	static const uint8_t MOTION_COUNT = 8;

    // Analog Controls
    int a_wheel;
    int a_accel;
    int a_brake;

    Input(void);
    ~Input(void);

    void init(int, int*, int*, const int, int*, int*);
    void close();

    void handle_key_up(SDL_keysym*);
    void handle_key_down(SDL_keysym*);
    void handle_joy_axis(SDL_JoyAxisEvent*);
    void handle_joy_down(SDL_JoyButtonEvent*);
    void handle_joy_up(SDL_JoyButtonEvent*);
	void handle_motion(SDL_MouseMotionEvent*);
    void frame_done();
    bool is_pressed(presses p);
	bool has_pressed(presses p);
    bool is_analog_l();
    bool is_analog_r();
    bool is_analog_select();

private:
    static const int CENTRE = 0x80;

    // Digital Dead Zone
    static const int DIGITAL_DEAD = 3200;

    // SDL Joystick / Keypad
    SDL_Joystick *stick;

	// SDL Motions [Touch Count]
	bounding_box_t panels_collsion[15];

    // Configurations for keyboard and joypad
    int* pad_config;
    int* key_config;
    int* axis;

    int wheel_zone;
    int wheel_dead;
    int pedals_dead;

    static const int DELAY_RESET = 60;
    int delay;

	void handle_key(const int, const bool);
	void handle_joy(const uint8_t, const bool);
};

extern Input input;