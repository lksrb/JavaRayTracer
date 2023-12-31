#include "Renderer.h"

#include "Scene.h"

Renderer::Renderer() : m_MainCamera(45.0f, 0.1f, 100.0f)
{   
  /* // Materials
	Material& pinkMaterial = m_Scene.Materials.emplace_back();
	pinkMaterial.Albedo = { 1.0f, 0.0f, 1.0f };
	pinkMaterial.Roughness = 0.0f;

	Material& blueMaterial = m_Scene.Materials.emplace_back();
	blueMaterial.Albedo = { 0.2f, 0.3f, 1.0f };
	blueMaterial.Roughness = 0.2f;

	Material& orangeMaterial = m_Scene.Materials.emplace_back();
	//orangeMaterial.Albedo = { 1.0f, 1.0f, 1.0f };
	orangeMaterial.Albedo = { 0.8f, 0.5f, 0.2f };
	orangeMaterial.Roughness = 0.1f;
	orangeMaterial.EmissionColor = orangeMaterial.Albedo;
	orangeMaterial.EmissionPower = 3.65f;

	// Scene objects
	{
		SceneObject& sphere = m_Scene.SceneObjects.emplace_back();
		sphere.Position = { 0.0f, 0.0f, 0.0f };
		sphere.Radius = 1.0f;
		sphere.MaterialIndex = 0;
	}

	{
		SceneObject& sphere = m_Scene.SceneObjects.emplace_back();
		sphere.Position = { 0.0f, -101.0f, 0.0f };
		sphere.Radius = 100.0f;
		sphere.MaterialIndex = 1;
	}

	{
		SceneObject& sphere = m_Scene.SceneObjects.emplace_back();
		sphere.Position = { 9.2f, -1.2f, -9.6f };
		sphere.Radius = 9.2f;
		sphere.MaterialIndex = 2;
	}*/
}

Renderer::~Renderer()
{
	delete[] m_AccumulationData;
	m_AccumulationData = nullptr;
}

Scene& Renderer::GetScene()
{
	return m_Scene;
}

void Renderer::OnResize(u32 width, u32 height)
{
	m_ViewportWidth = width;
	m_ViewportHeight = height;

	delete[] m_AccumulationData;
	m_AccumulationData = new glm::vec4[width * height];

	m_ImageHorizontalIter.resize(width);
	m_ImageVerticalIter.resize(height);

	for (u32 i = 0; i < width; i++)
		m_ImageHorizontalIter[i] = i;

	for (u32 i = 0; i < height; i++)
		m_ImageVerticalIter[i] = i;

	m_Pixels.resize(width * height);
	m_MainCamera.OnResize(m_ViewportWidth, m_ViewportHeight);

	ResetAccumulation();
}

void Renderer::OnUpdate(f32 ts)
{
	if (m_MainCamera.OnUpdate(ts))
	{
		// Camera moved => frame index reset is required for accumulation
		ResetAccumulation();
	}
}

void Renderer::OnRender(u32* renderImage)
{
	if (m_FrameIndex == 1)
		memset(m_AccumulationData, 0, m_ViewportWidth * m_ViewportHeight * sizeof(glm::vec4));

	// ~2m -> 1920x1080
	std::for_each(std::execution::par, m_ImageVerticalIter.begin(), m_ImageVerticalIter.end(), [this, renderImage](u32 y)
		{
			std::for_each(std::execution::par, m_ImageHorizontalIter.begin(), m_ImageHorizontalIter.end(), [this, renderImage, y](u32 x)
				{
#if !RT_TEST
					glm::vec4 color = PerPixel(x, y);
					m_AccumulationData[x + y * m_ViewportWidth] += color;

					glm::vec4 accumulatedColor = m_AccumulationData[x + y * m_ViewportWidth];
					accumulatedColor /= (f32)m_FrameIndex;

					accumulatedColor = glm::clamp(accumulatedColor, glm::vec4(0.0f), glm::vec4(1.0f));
					renderImage[x + y * m_ViewportWidth] = ConvertToUInt(accumulatedColor);
#else
					glm::vec4 color = PerPixel(x, y);
					color = glm::clamp(color, glm::vec4(0.0f), glm::vec4(1.0f));
					renderImage[x + y * m_ViewportWidth] = ConvertToUInt(color);
#endif
				});
		});

	if (m_Settings.Accumulate)
		++m_FrameIndex;
	else
		m_FrameIndex = 1;
}

glm::vec4 Renderer::PerPixel(u32 x, u32 y)
{
#if !RT_TEST
	Ray ray;
	ray.Origin = m_MainCamera.GetPosition();
	ray.Direction = m_MainCamera.GetRayDirections()[x + y * m_ViewportWidth];

	glm::vec3 light(0.0f);
	glm::vec3 contribution(1.0f);

	u32 seed = x + y * m_ViewportWidth;
	seed *= m_FrameIndex;

	constexpr u32 bounces = 10;
	for (u32 i = 0; i < bounces; ++i)
	{
		// Per bounce
		seed += i;

		HitPayload payload = TraceRay(ray);

		// If we did not hit anything, sky color influences the light
		if (payload.HitDistance < 0.0f)
		{
			glm::vec3 skyColor = glm::vec3(0.6f, 0.7f, 0.9f);
			light += skyColor * contribution;
			break;
		}

		const SceneObject& sceneObject = m_Scene.SceneObjects[payload.ObjectIndex];
		const Material& material = m_Scene.Materials[sceneObject.MaterialIndex];

		contribution *= material.Albedo;
		light += material.GetEmission();

		ray.Origin = payload.WorldPosition + payload.WorldNormal * 0.0001f;
		//ray.Direction = glm::reflect(ray.Direction,
		//	payload.WorldNormal + material.Roughness * Walnut::Random::Vec3(-0.5f, 0.5f));
		ray.Direction = glm::normalize(payload.WorldNormal + material.Roughness * InUnitSphere(seed));
	}

	return glm::vec4(light, 1.0f);
#else
	glm::vec3 rayOrigin(0.0f, 0.0f, 2.0f);

	f32 coordX = 2.0f * ((f32)x / m_ViewportWidth) - 1.0f;
	f32 coordY = 1.0f - 2.0f * ((f32)y / m_ViewportHeight);

	glm::vec3 rayDirection(coordX, coordY, -1.0f);

	f32 radius = 0.5f;

	f32 a = glm::dot(rayDirection, rayDirection);
	f32 b = 2.0f * glm::dot(rayOrigin, rayDirection);
	f32 c = glm::dot(rayOrigin, rayOrigin) - radius * radius;

	f32 discriminant = b * b - 4.0f * a * c;

	if (discriminant < 0.0f)
	{
		// Sky color
		return glm::vec4(0.0f, 0.0f, 0.0f, 1.0f);
	}

	f32 closestT = (-b - std::sqrt(discriminant)) / (2.0f * a);
	glm::vec3 hitPoint = rayDirection * closestT + rayOrigin;
	glm::vec3 normal = glm::normalize(hitPoint);

	glm::vec3 lightDir = glm::normalize(glm::vec3(-1.0f));

	f32 d = glm::max(glm::dot(normal, lightDir * -1.0f), 0.0f);

	glm::vec3 sphereColor(1.0f, 0.3f, 0.5f);
	sphereColor *= d;

	return glm::vec4(sphereColor.x, sphereColor.y, sphereColor.z, 1.0f);
#endif
}

HitPayload Renderer::TraceRay(const Ray& ray)
{
	i32 closestSphere = -1;
	f32 hitDistance = std::numeric_limits<f32>::max();

	for (size_t i = 0; i < m_Scene.SceneObjects.size(); ++i)
	{
		const SceneObject& sphere = m_Scene.SceneObjects[i];
		glm::vec3 origin = ray.Origin - sphere.Position;

		f32 a = glm::dot(ray.Direction, ray.Direction);
		f32 b = 2.0f * glm::dot(origin, ray.Direction);
		f32 c = glm::dot(origin, origin) - sphere.Radius * sphere.Radius;

		// Discriminant
		f32 discriminant = b * b - 4.0f * a * c;

		// If no hits, skip
		if (discriminant < 0.0f)
			continue;

		// float t0 = (-b + glm::sqrt(discriminant)) / (2.0f * a); // Currently not used
		f32 closestT = (-b - glm::sqrt(discriminant)) / (2.0f * a);

		if (closestT > 0.0f && closestT < hitDistance)
		{
			hitDistance = closestT;
			closestSphere = (i32)i;
		}
	}

	if (closestSphere < 0)
		return Miss(ray);

	return ClosestHit(ray, hitDistance, closestSphere);
}

HitPayload Renderer::Miss(const Ray& ray)
{
	HitPayload payload;
	payload.HitDistance = -1.0f;
	return payload;
}

HitPayload Renderer::ClosestHit(const Ray& ray, f32 hitDistance, i32 objectIndex)
{
	HitPayload payload;
	payload.HitDistance = hitDistance;
	payload.ObjectIndex = objectIndex;

	const SceneObject& closestSphere = m_Scene.SceneObjects[objectIndex];

	glm::vec3 origin = ray.Origin - closestSphere.Position;
	payload.WorldPosition = origin + ray.Direction * hitDistance;
	payload.WorldNormal = glm::normalize(payload.WorldPosition);

	payload.WorldPosition += closestSphere.Position;
	return payload;
}
