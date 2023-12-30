package Core;

// They are just spheres for now
class SceneObject {
    private static int s_IDGenerator = 1; // "Unique" ID generator

    public final int ID;
    public String Name;
    public Vector3 Position;
    public float Radius;
    public int MaterialIndex;

    public SceneObject(String name)
    {
        Name = name;
        Position = new Vector3(0.0f);
        Radius = 1.0f;
        MaterialIndex = 0; // Default material
        ID = s_IDGenerator++;
    }
}