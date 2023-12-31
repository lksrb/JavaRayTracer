# JavaRayTracer
Implementation of software path tracer.

## Specification
Language: Java 20, C++ 20

Platform: Windows

### Features
- Add, edit or remove spheres and materials
- Diffuse lighting, reflection and emission 

## Getting started
Currently only available to build and run using IntelliJ IDEA and Visual Studio 2022.

<ins>**1. Downloading the repository**</ins>

Start by cloning this repository using `git clone https://www.github.com/lksrb/JavaRayTracer`.

<ins>**2. Setting up the project**</ins>

Open this repository using IntelliJ IDEA. Then navigate to `src/backend`, open VS solution `backend.sln` and build `Release`.

This will generate a DLL module which java frondend already know about.

<ins>**3. Usage**</ins>

You can explore whole scene using an interactive camera. When you want to rotate the camera hold right mouse button and drag to desired location. 

If you want to move the camera into different directions, you can use:
- W - Forward
- S - Backward
- A - Left
- D - Right
- Q - Down
- E - Up


Happy pathtracing!

## Showcase
Emission material:
![Emission material](https://raw.githubusercontent.com/lksrb/RayTracing/master/res/raytraced.png)
