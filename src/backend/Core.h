#pragma once


// --- Must have includes ---
#include <glm/glm.hpp>
#include <iostream>
#include <format>
#include <exception>

// --- Primitive types ---
using u8 = uint8_t;
using i32 = int32_t;
using u32 = uint32_t;
using f32 = float;

// --- Preprocessors ----
#define RT_ASSERT(cond, ...) do { if(!(cond)) { std::cerr << std::format(__VA_ARGS__) << "\n"; throw std::exception("ASSERT"); } } while(0)
#define RT_LOG(...) std::cout << std::format(__VA_ARGS__) << "\n"

// Fast random
inline u32 PCG_Hash(u32 input)
{
	u32 state = input * 747796405u + 2891336453u;
	u32 word = ((state >> ((state >> 28u) + 4u)) ^ state) * 277803737u;
	return (word >> 22u) ^ word;
}

inline f32 RandomFloat(u32& seed)
{
	seed = PCG_Hash(seed);

	constexpr f32 scale = 1.0f / std::numeric_limits<u32>::max();
	return (f32)seed * scale;
}

inline glm::vec3 InUnitSphere(u32& seed)
{
	return glm::normalize(glm::vec3(
		RandomFloat(seed) * 2.0f - 1.0f,
		RandomFloat(seed) * 2.0f - 1.0f,
		RandomFloat(seed) * 2.0f - 1.0f)
	);
}

// Generates random float between 0.0f and 1.0f
inline f32 Float01(u32& seed)
{
	seed = PCG_Hash(seed);

	constexpr f32 scale = (1.0f / UINT32_MAX);
	return seed * scale;
}

// Convert float values to bytes
inline long ConvertToUInt(const glm::vec4& color)
{
	u8 r = (u8)(color.r * 255.0f);
	u8 g = (u8)(color.g * 255.0f);
	u8 b = (u8)(color.b * 255.0f);
	u8 a = (u8)(color.a * 255.0f);

#ifdef BACKEND_DEBUG
	return (a << 24) | (b << 16) | (g << 8) | r;
#else
	return (a << 24) | (r << 16) | (g << 8) | b;
#endif
}