package handmadeguns.client.modelLoader.obj_modelloaderMod.obj;

public class HMGVertex
{
    public float x, y, z;

    public HMGVertex(float x, float y)
    {
        this(x, y, 0F);
    }

    public HMGVertex(float x, float y, float z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void normalize()
    {
        double d = length();
        this.x /= d;
        this.y /= d;
        this.z /= d;
    }
    public double length(){
        return Math.sqrt(x*x + y*y + z*z);
    }

    public void add(HMGVertex v)
    {
        this.x += v.x;
        this.y += v.y;
        this.z += v.z;
    }

    public boolean equal(HMGVertex v)
    {
        return this.x==v.x && this.y==v.y && this.z==v.z;
    }
    public double angle(HMGVertex v){
        return this.dot(v)/this.length()/v.length();
    }
    public double dot(HMGVertex v){
        return this.x * v.x + this.y * v.y + this.z * v.z;
    }
}