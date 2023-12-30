#include "Renderer.h"

#if BACKEND_DEBUG
	#define STB_IMAGE_WRITE_IMPLEMENTATION
	#include <stb/stbi_image_write.h>
#endif

#include <iostream>
#include <jni.h>

static Renderer s_Renderer;

// Hooks
extern "C"
{
	// Renderer Hooks
	// Renderer Hooks
	// Renderer Hooks

	JNIEXPORT void JNICALL Java_Core_Renderer_OnResizeNative(JNIEnv* env, jobject, jint width, jint height)
	{
		s_Renderer.OnResize(width, height);
	}

	JNIEXPORT void JNICALL Java_Core_Renderer_RenderNative(JNIEnv* env, jobject, jfloat ts, jintArray array, jint width, jint height)
	{
		// Update renderer
		s_Renderer.OnUpdate(ts);

		// JInt will always be 4 bytes so this works
		u32* data = static_cast<u32*>(env->GetPrimitiveArrayCritical(array, nullptr));

		s_Renderer.OnRender(data);

		env->ReleasePrimitiveArrayCritical(array, data, 0);
	}

	// SceneObjects Hooks
	// SceneObjects Hooks
	// SceneObjects Hooks

	JNIEXPORT void JNICALL Java_Core_SceneEditorPanel_OnSceneObjectAddNative(JNIEnv* env, jobject, jobject sceneObjectInstance)
	{
		jclass clazz = env->GetObjectClass(sceneObjectInstance);

		auto& sceneObject = s_Renderer.GetScene().SceneObjects.emplace_back();
		sceneObject.ID = env->GetIntField(sceneObjectInstance, env->GetFieldID(clazz, "ID", "I"));
		sceneObject.Radius = env->GetFloatField(sceneObjectInstance, env->GetFieldID(clazz, "Radius", "F"));

		// Get the field ID of the 'vector' field in MyClass
		jfieldID positionField = env->GetFieldID(clazz, "Position", "LCore/Vector3;");

		// Get the Vector3 object from the 'vector' field
		jobject positionObject = env->GetObjectField(sceneObjectInstance, positionField);

		// Get the class of Vector3
		jclass vector3Class = env->GetObjectClass(positionObject);

		sceneObject.Position.x = env->GetFloatField(positionObject, env->GetFieldID(vector3Class, "X", "F"));
		sceneObject.Position.y = env->GetFloatField(positionObject, env->GetFieldID(vector3Class, "Y", "F"));
		sceneObject.Position.z = env->GetFloatField(positionObject, env->GetFieldID(vector3Class, "Z", "F"));

		sceneObject.MaterialIndex = env->GetIntField(sceneObjectInstance, env->GetFieldID(clazz, "MaterialIndex", "I"));

		// Redraw whole image
		s_Renderer.ResetAccumulation();
	}

	JNIEXPORT void JNICALL Java_Core_SceneEditorPanel_OnSceneObjectRemoveNative(JNIEnv* env, jobject, jobject sceneObjectInstance)
	{
		jclass clazz = env->GetObjectClass(sceneObjectInstance);
		i32 id = env->GetIntField(sceneObjectInstance, env->GetFieldID(clazz, "ID", "I"));

		auto& scene = s_Renderer.GetScene().SceneObjects;

		for (auto it = scene.begin(); it != scene.end(); ++it)
		{
			if ((*it).ID == id)
			{
				scene.erase(it);
				break;
			}
		}

		// Redraw whole image
		s_Renderer.ResetAccumulation();
	}

	JNIEXPORT void JNICALL Java_Core_SceneEditorPanel_OnSceneObjectValueChangeNative(JNIEnv* env, jobject, jobject sceneObjectInstance)
	{
		jclass sceneObjectClass = env->GetObjectClass(sceneObjectInstance);
		i32 id = env->GetIntField(sceneObjectInstance, env->GetFieldID(sceneObjectClass, "ID", "I"));

		auto& scene = s_Renderer.GetScene().SceneObjects;

		// Find matching scene object and update all fields
		for (auto it = scene.begin(); it != scene.end(); ++it)
		{
			if (auto& sceneObject = (*it); sceneObject.ID == id)
			{
				// IMPROVE: We could cache field ids
				sceneObject.Radius = env->GetFloatField(sceneObjectInstance, env->GetFieldID(sceneObjectClass, "Radius", "F"));

				// Position
				{
					jfieldID positionField = env->GetFieldID(sceneObjectClass, "Position", "LCore/Vector3;");
					jobject positionObject = env->GetObjectField(sceneObjectInstance, positionField);
					jclass vector3Class = env->GetObjectClass(positionObject);

					sceneObject.Position.x = env->GetFloatField(positionObject, env->GetFieldID(vector3Class, "X", "F"));
					sceneObject.Position.y = env->GetFloatField(positionObject, env->GetFieldID(vector3Class, "Y", "F"));
					sceneObject.Position.z = env->GetFloatField(positionObject, env->GetFieldID(vector3Class, "Z", "F"));
				}
				sceneObject.MaterialIndex = env->GetIntField(sceneObjectInstance, env->GetFieldID(sceneObjectClass, "MaterialIndex", "I"));
				break;
			}
		}

		// Redraw whole image
		s_Renderer.ResetAccumulation();
	}

	// Material Hooks
	// Material Hooks
	// Material Hooks

	JNIEXPORT void JNICALL Java_Core_MaterialEditorPanel_OnMaterialAddNative(JNIEnv* env, jobject, jobject materialInstance)
	{
		jclass materialClass = env->GetObjectClass(materialInstance);

		auto& material = s_Renderer.GetScene().Materials.emplace_back();
		material.ID = env->GetIntField(materialInstance, env->GetFieldID(materialClass, "ID", "I"));

		// Albedo
		{
			jfieldID albedoField = env->GetFieldID(materialClass, "Albedo", "LCore/Vector3;");
			jobject albedoObject = env->GetObjectField(materialInstance, albedoField);
			jclass vector3Class = env->GetObjectClass(albedoObject);

			material.Albedo.r = env->GetFloatField(albedoObject, env->GetFieldID(vector3Class, "X", "F"));
			material.Albedo.g = env->GetFloatField(albedoObject, env->GetFieldID(vector3Class, "Y", "F"));
			material.Albedo.b = env->GetFloatField(albedoObject, env->GetFieldID(vector3Class, "Z", "F"));
		}

		// Roughness
		material.Roughness = env->GetFloatField(materialInstance, env->GetFieldID(materialClass, "Roughness", "F"));

		// Metallic
		material.Metallic = env->GetFloatField(materialInstance, env->GetFieldID(materialClass, "Metallic", "F"));

		// Emission color
		{
			jfieldID emissionColorField = env->GetFieldID(materialClass, "EmissionColor", "LCore/Vector3;");
			jobject emissionColorObject = env->GetObjectField(materialInstance, emissionColorField);
			jclass vector3Class = env->GetObjectClass(emissionColorObject);

			material.EmissionColor.r = env->GetFloatField(emissionColorObject, env->GetFieldID(vector3Class, "X", "F"));
			material.EmissionColor.g = env->GetFloatField(emissionColorObject, env->GetFieldID(vector3Class, "Y", "F"));
			material.EmissionColor.b = env->GetFloatField(emissionColorObject, env->GetFieldID(vector3Class, "Z", "F"));
		}

		// Emission power
		material.EmissionPower = env->GetFloatField(materialInstance, env->GetFieldID(materialClass, "EmissionPower", "F"));

		// Redraw whole image
		s_Renderer.ResetAccumulation();
	}

	JNIEXPORT jint JNICALL Java_Core_MaterialEditorPanel_OnMaterialRemoveNative(JNIEnv* env, jobject, jobject materialInstance)
	{
		jclass clazz = env->GetObjectClass(materialInstance);
		i32 id = env->GetIntField(materialInstance, env->GetFieldID(clazz, "ID", "I"));

		auto& materials = s_Renderer.GetScene().Materials;

		bool found = false;

		i32 index = -1;
		for (auto it = materials.begin(); it != materials.end(); ++it)
		{
			if ((*it).ID == id)
			{
				found = true;
				materials.erase(it); 
				break;
			}

			index++;
		}

		// Redraw whole image
		s_Renderer.ResetAccumulation();

		return found ? (jint)index + 1 : -1;
	}

	JNIEXPORT void JNICALL Java_Core_MaterialEditorPanel_OnMaterialValueChangeNative(JNIEnv* env, jobject, jobject materialInstance)
	{
		jclass materialClass = env->GetObjectClass(materialInstance);
		i32 id = env->GetIntField(materialInstance, env->GetFieldID(materialClass, "ID", "I"));

		auto& materials = s_Renderer.GetScene().Materials;

		// Find matching scene object and update all fields
		for (auto it = materials.begin(); it != materials.end(); ++it)
		{
			if (auto& material = (*it); material.ID == id)
			{
				// Albedo
				{
					jfieldID albedoField = env->GetFieldID(materialClass, "Albedo", "LCore/Vector3;");
					jobject albedoObject = env->GetObjectField(materialInstance, albedoField);
					jclass vector3Class = env->GetObjectClass(albedoObject);

					material.Albedo.r = env->GetFloatField(albedoObject, env->GetFieldID(vector3Class, "X", "F"));
					material.Albedo.g = env->GetFloatField(albedoObject, env->GetFieldID(vector3Class, "Y", "F"));
					material.Albedo.b = env->GetFloatField(albedoObject, env->GetFieldID(vector3Class, "Z", "F"));
				}

				// Roughness
				material.Roughness = env->GetFloatField(materialInstance, env->GetFieldID(materialClass, "Roughness", "F"));

				// Metallic
				material.Metallic = env->GetFloatField(materialInstance, env->GetFieldID(materialClass, "Metallic", "F"));

				// Emission color
				{
					jfieldID emissionColorField = env->GetFieldID(materialClass, "EmissionColor", "LCore/Vector3;");
					jobject emissionColorObject = env->GetObjectField(materialInstance, emissionColorField);
					jclass vector3Class = env->GetObjectClass(emissionColorObject);

					material.EmissionColor.r = env->GetFloatField(emissionColorObject, env->GetFieldID(vector3Class, "X", "F"));
					material.EmissionColor.g = env->GetFloatField(emissionColorObject, env->GetFieldID(vector3Class, "Y", "F"));
					material.EmissionColor.b = env->GetFloatField(emissionColorObject, env->GetFieldID(vector3Class, "Z", "F"));
				}

				// Emission power
				material.EmissionPower = env->GetFloatField(materialInstance, env->GetFieldID(materialClass, "EmissionPower", "F"));

				break;
			}
		}

		// Redraw whole image
		s_Renderer.ResetAccumulation();
	}
}

#ifdef BACKEND_DEBUG

void Save(const u32* rawPixels, int width, int height, const char* filePath)
{
	if (!stbi_write_png(filePath, width, height, 4, rawPixels, 4 * width))
	{
		std::cerr << "Failed to write an image!\n";
		return;
	}

	std::cout << "Image saved!\n";
}

int main()
{
	u32 width = 1200;
	u32 height = 800;
	u32* image = new u32[width * height];
	s_Renderer.OnResize(width, height);

	while (true)
	{
		if (Input::IsKeyDown(Key::E))
			break;

		constexpr f32 ts = 1 / 60.0f;

		s_Renderer.OnUpdate(ts);
		s_Renderer.OnRender(image);
	}

	Save(image, width, height, "test.png");

	delete[] image;


}

#endif