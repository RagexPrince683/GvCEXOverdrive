package hmggvcmob.ai.newai;

import handmadeguns.entity.IFF;
import handmadevehicle.entity.EntityDummy_rider;
import hmggvcmob.entity.IGVCmob;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;

import javax.vecmath.Vector3d;

public abstract class AIAttackToEntity extends AIAttack {
	protected Entity target;

	protected boolean canSeeState;
	protected int forget = 0;//忘れるまで

	public AIAttackToEntity(EntityLiving shooter, AIAttackManager aiAttackManager) {
		super(shooter, aiAttackManager);
	}

	@Override
	public boolean shouldExecute() {
		EntityLivingBase entityTarget = shooter.getAttackTarget();
		if(shooter instanceof IFF && ((IFF) shooter).is_this_entity_friend(entityTarget)){
			return false;
		}
		if (entityTarget == null || entityTarget.isDead) {
			shooter.setAttackTarget(null);
			shooter.setSneaking(false);
			return false;
		} else {
			target = entityTarget;
			return true;
		}
	}

	public void updateTask(){
		movePosition();
		aimTarget();
		fireWeapon();
		if(shooter instanceof IGVCmob){
			((IGVCmob) shooter).setAimPos(aiAttackManager.getAimingPoint());
		}
	}


	public Vector3d getSeeingPosition(){
		if(target != null && shooter.canEntityBeSeen(target)){
			Entity aimTo = target;
			if(target.ridingEntity != null){
				if(target.ridingEntity instanceof EntityDummy_rider){
					aimTo = ((EntityDummy_rider) target.ridingEntity).linkedBaseLogic.mc_Entity;
				}else {
					aimTo = target.ridingEntity;
				}
			}
			if(aiAttackManager.getAimingPoint() == null)aiAttackManager.createAimingPoint();
			aiAttackManager.getAimingPoint().set(aimTo.posX,aimTo.posY + aimTo.height*0.75,aimTo.posZ);
		}
		return aiAttackManager.getAimingPoint();
	}
}
