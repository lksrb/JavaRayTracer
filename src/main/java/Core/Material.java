package Core;

class Material {
    private static int s_IDGenerator = 1; // "Unique" ID generator

    public final int ID;
    public String Name;
    public Vector3 Albedo;
    public float Roughness;
    public float Metallic;

    public Vector3 EmissionColor;
    public float EmissionPower;

    public Material(String name)
    {
        ID = s_IDGenerator++;
        Name = name;
        Albedo = new Vector3(0.0f);
        Roughness = 1.0f;
        Metallic = 0.0f;
        EmissionColor = new Vector3(0.0f);
        EmissionPower = 0.0f;
    }
}
