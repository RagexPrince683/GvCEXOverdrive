package hmggvcmob.ai.newai;

import handmadeguns.entity.PlacedGunEntity;
import handmadevehicle.entity.EntityVehicle;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;

import javax.vecmath.Vector3d;

public abstract class AIAttackPos extends AIAttack {

	private EntityVehicle currentRidingVehicle;
	private PlacedGunEntity currentRidingGun;

	private Vector3d targetPos;
	public AIAttackPos(EntityLiving shooter, AIAttackManager aiAttackManager) {
		super(shooter, aiAttackManager);
	}

	public void setAttackPos (Vector3d targetPos){
		this.targetPos = targetPos;
	}
	@Override
	public boolean shouldExecute() {
		return false;
	}

	public abstract void chaseTarget();
	public abstract void aimTarget();
}
