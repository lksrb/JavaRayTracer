#pragma once

#include "Core.h"

#include <glm/glm.hpp>
#include <vector>

struct Material
{
	i32 ID = 0;

	glm::vec3 Albedo{ 1.0f };
	f32 Roughness = 1.0f;
	f32 Metallic = 0.0f;

	glm::vec3 EmissionColor{ 0.0f };
	f32 EmissionPower = 0.0f;

	glm::vec3 GetEmission() const { return EmissionPower * EmissionColor; }
};

struct SceneObject
{
	i32 ID = 0;

	glm::vec3 Position{ 0.0f };
	f32 Radius = 0.5f;

	i32 MaterialIndex = 0;
};

struct Scene
{
	std::vector<SceneObject> SceneObjects;
	std::vector<Material> Materials;
};