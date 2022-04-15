package hmggvcmob.ai.newai;

import hmggvcmob.entity.IGVCmob;
import hmggvcmob.util.EntityRidingState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;

import javax.vecmath.Vector3d;

import static hmggvcmob.util.GVCUtil.getRidingType;

public class AIAttackManager extends EntityAIBase {
	private final EntityLiving shooter;

	private AIAttack currentAI;

	private Vector3d aimingPoint;

	private AIAttackEntityByGun toEntity_Inf;
	private AIAttackEntityByAirPlane toEntity_airPlane;
	private AIAttackEntityByHeli toEntity_Heli;
	private AIAttackEntityByTank toEntity_Tank;

	public AIAttackManager(EntityLiving shooter) {
		this.shooter = shooter;
		toEntity_Inf = new AIAttackEntityByGun(shooter,this);
		toEntity_Tank = new AIAttackEntityByTank(shooter,this);
		toEntity_airPlane = new AIAttackEntityByAirPlane(shooter,this);
		toEntity_Heli = new AIAttackEntityByHeli(shooter,this);
	}

	@Override
	public boolean shouldExecute() {
		currentAI = null;
		EntityRidingState type = getRidingType(shooter);
		switch (type){
			case None:
			case Gun:
				currentAI = toEntity_Inf;
				break;
			case AirPlane:
				currentAI = toEntity_airPlane;
				break;
			case Heli:
				currentAI = toEntity_Heli;
				break;
			case Turret:
			case Tank:
				currentAI = toEntity_Tank;
				break;
		}
		if(currentAI != null){
			return currentAI.shouldExecute();
		}
		return false;
	}

	public Vector3d getAimingPoint(){
		return this.aimingPoint;
	}

	public Vector3d createAimingPoint(){
		if(aimingPoint == null)aimingPoint = new Vector3d();
		return this.aimingPoint;
	}


	@Override
	public void updateTask(){
		if(currentAI != null)currentAI.updateTask();
	}
	@Override
	public void resetTask(){
		if(currentAI != null)currentAI.resetTask();
		aimingPoint = null;
	}
}
