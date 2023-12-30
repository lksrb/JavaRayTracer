#include "Input.h"

#define WIN32_LEAN_AND_MEAN
#include <Windows.h>

bool Input::IsKeyDown(Key key)
{
	return (::GetAsyncKeyState(static_cast<int>(key)) & 0x8000) != 0;
}

bool Input::IsMouseButtonDown(MouseButton button)
{
	return (::GetAsyncKeyState(static_cast<int>(button)) & 0x8000) != 0;
}

glm::vec2 Input::GetMousePosition()
{
	POINT cursorPos;
	::GetCursorPos(&cursorPos);

	return { cursorPos.x, cursorPos.y };
}
