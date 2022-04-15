package handmadevehicle.entity.parts;

import javax.vecmath.Vector3d;

public class VectorAndHitSide {
	Vector3d vector3d;
	Vector3d vector3d_relative;
	Vector3d vector3d_normal;
	int hitside;
	public VectorAndHitSide(Vector3d avec,Vector3d relative,int side){
		this.vector3d = avec;
		this.vector3d_relative = relative;
		this.hitside = side;
	}
	public VectorAndHitSide(Vector3d avec,Vector3d relative,Vector3d normal,int side){
		this.vector3d = avec;
		this.vector3d_relative = relative;
		this.vector3d_normal = normal;
		this.hitside = side;
	}
}
