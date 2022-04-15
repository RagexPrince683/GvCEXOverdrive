package hmggvcmob.entity.util;

import handmadevehicle.entity.EntityDummy_rider;
import handmadevehicle.entity.EntityVehicle;
import hmggvcmob.entity.IGVCmob;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.pathfinding.PathEntity;

import javax.vecmath.Vector3d;
import java.util.Random;

import static handmadevehicle.Utils.canMoveEntity;

public class EntityAndPos {
	public EntityLiving entity;
	double currentSpeed = 0;
	Vector3d pos = new Vector3d();
	Vector3d randMiserVec;
	public EntityAndPos(EntityLiving entity){
		this.entity = entity;
		pos.set(this.entity.posX,this.entity.posY,this.entity.posZ);
		Random random = new Random();
		randMiserVec = new Vector3d(random.nextDouble(),0,random.nextDouble());
		randMiserVec.scale(random.nextDouble() * 8 - 4);
	}
	public Vector3d getPos(){
		return pos;
	}
	public double[] getArrayPos(){
		return new double[]{pos.x,pos.y,pos.z};
	}

	public final void set(double x, double y, double z) {
		pos.set(x,y,z);
	}
	public final void set(Vector3d vector3d,double speed) {
		if(entity.getAttackTarget() == null && canMoveEntity(entity)) {
			pos.set(vector3d);
			currentSpeed = speed;
			((IGVCmob)entity).getMoveToPositionMng().setMovingSpeed(currentSpeed);
			((IGVCmob)entity).getMoveToPositionMng().getMoveToPos().set(pos);
		}
	}
	public final void set_withRand(Vector3d vector3d,double speed) {
		vector3d.add(randMiserVec);
		this.set(vector3d,speed);
	}
	public int repathCool = 0;
	public PathEntity prevPath = null;

	public void update(){
		if(entity instanceof IGVCmob){
			((IGVCmob)entity).getMoveToPositionMng().setMovingSpeed(currentSpeed);
			((IGVCmob)entity).getMoveToPositionMng().getMoveToPos().set(pos);
		}
	}
}
