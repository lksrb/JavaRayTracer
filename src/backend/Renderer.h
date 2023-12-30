#pragma once

#include "Core.h"
#include "Camera.h"
#include "Scene.h"

#include <vector>
#include <execution>
#include <limits>

struct Ray
{
	glm::vec3 Origin;
	glm::vec3 Direction;
};

struct HitPayload
{
	f32 HitDistance;
	glm::vec3 WorldPosition;
	glm::vec3 WorldNormal;

	i32 ObjectIndex;
};

class Renderer
{
public:
	struct Settings
	{
		bool Accumulate = true;
	};

	Renderer();
	~Renderer();

	Scene& GetScene();

	void OnResize(u32 width, u32 height);
	void OnUpdate(f32 ts);

	void OnRender(u32* renderImage);

	glm::vec4 PerPixel(u32 x, u32 y);

	void ResetAccumulation() { m_FrameIndex = 1; }

	HitPayload TraceRay(const Ray& ray);
	HitPayload Miss(const Ray& ray);
	HitPayload ClosestHit(const Ray& ray, f32 hitDistance, i32 objectIndex);
private:
	glm::vec4* m_AccumulationData = nullptr;
	u32 m_FrameIndex = 1;
	std::vector<u32> m_ImageHorizontalIter, m_ImageVerticalIter;
	std::vector<glm::vec4> m_Pixels;

	u32 m_ViewportWidth = 0;
	u32 m_ViewportHeight = 0;

	Scene m_Scene;
	Camera m_MainCamera;

	Settings m_Settings;
};