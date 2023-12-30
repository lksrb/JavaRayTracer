package Core;

class Vector3 {
    public float X,Y,Z;

    public Vector3(float x, float y, float z)
    {
        X = x;
        Y = y;
        Z = z;
    }

    public Vector3(float scalar)
    {
        X = Y = Z = scalar;
    }
}