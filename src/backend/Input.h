#pragma once

#include "Core.h"

#include <glm/glm.hpp>

enum class Key : u32
{
	// Alphabet
	A = 0x41,
	B = 0x42,
	C = 0x43,
	D = 0x44,
	E = 0x45,
	F = 0x46,
	G = 0x47,
	H = 0x48,
	I = 0x49,
	J = 0x4A,
	K = 0x4B,
	L = 0x4C,
	M = 0x4D,
	N = 0x4E,
	O = 0x4F,
	P = 0x50,
	Q = 0x51,
	R = 0x52,
	S = 0x53,
	T = 0x54,
	U = 0x55,
	V = 0x56,
	W = 0x57,
	X = 0x58,
	Y = 0x59,
	Z = 0x5A,

	// Controls
	LeftShift = 0xA0,
	RightShift = 0xA1,
	LeftControl = 0xA2,
	RightControl = 0xA3,
	LeftAlt = 0xA4,
	RightAlt = 0xA5,

	// Arrows
	Left = 0x25,
	Up = 0x26,
	Right = 0x27,
	Down = 0x28,

	// General
	Space = 0x20,
	Enter = 0x0D,
	Escape = 0x1B,
	Tab = 0x09
};

enum class MouseButton : u32
{
	Left = 0x01,
	Right = 0x02,
	Middle = 0x04
};

class Input
{
public:
	static bool IsKeyDown(Key key);
	static bool IsMouseButtonDown(MouseButton button);

	static glm::vec2 GetMousePosition();
};