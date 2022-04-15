package hmggvcmob.ai.newai;

import handmadeguns.entity.PlacedGunEntity;
import handmadeguns.items.guns.HMGItem_Unified_Guns;
import handmadevehicle.SlowPathFinder.WorldForPathfind;
import handmadevehicle.entity.EntityDummy_rider;
import hmggvcmob.entity.IGVCmob;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.MathHelper;

import javax.vecmath.Vector3d;
import java.util.List;
import java.util.Random;

import static hmggvcmob.util.GVCUtil.getRidingEntity;
import static hmggvcmob.util.GVCUtil.isOnGround;
import static java.lang.Math.abs;

public class AIAttackEntityByGun extends AIAttackToEntity {
	public float maxshootrange;//射程
	public float minshootrange;//射程
	public int bursttime;//一連射の時間
	public int burstingtime;//撃ってる時間
	public int burstcool = 20;//連射の間隔
	public int burstcoolcnt;
	private final int Warningblank = 50;
	private int Warningcoolcnt;
	public int retriggerCool;
	private boolean isSelectorChecked = false;
	private int aiming = 0;//狙ってる時間

	private int dir_modeChangeCool = 600;
	private boolean roundDir;
	private boolean moveRound = false;

	private int standingTime;

	public AIAttackEntityByGun(EntityLiving guerrilla,AIAttackManager aiAttackManager){
		super(guerrilla,aiAttackManager);
		maxshootrange = 200;
		minshootrange = 4;
		bursttime = 40;
	}
	public boolean forceStop = false;
	@Override
	public boolean shouldExecute() {
		if(forceStop){
			forceStop = false;
			return false;
		}
		return super.shouldExecute();
	}

	@Override
	public void movePosition() {
		Vector3d aimingPoint = getSeeingPosition();
		if(!target.onGround && (getRidingEntity(target) == null || !getRidingEntity(target).onGround))return;//飛行してる目標相手に移動しない
		boolean see = shooter.canEntityBeSeen(target);
		boolean canSee = !shooter.isPotionActive(Potion.blindness);
		canSee &= see;
		canSeeState = canSee;
		if((shooter.getHeldItem() == null || shooter.getHeldItem().getItem() instanceof HMGItem_Unified_Guns) && aimingPoint != null){
			double maxrange = maxshootrange * maxshootrange;
			double minrange = minshootrange * minshootrange;
			if (shooter.ridingEntity == null && (shooter.getHeldItem() == null || !(shooter.getHeldItem().getItem() instanceof HMGItem_Unified_Guns))) {
				maxrange = 0;
				minrange = 0;
				moveRound = false;
			}
			double tocurrentAttackToPosition = shooter.getDistanceSq(aimingPoint.x, aimingPoint.y, aimingPoint.z);
			Vector3d reCurToTargetPosition = new Vector3d(aimingPoint);
			Vector3d moveToVec = new Vector3d(shooter.posX, shooter.posY, shooter.posZ);
			Vector3d reCurToTargetPosition_copy = new Vector3d(reCurToTargetPosition);
			reCurToTargetPosition.sub(moveToVec);
			byte state = -1;//stop

			{
				if (!canSee || maxrange < tocurrentAttackToPosition)
					state = 1;//go
				else if (minrange > tocurrentAttackToPosition)
					state = 3;//back
				else if (moveRound)
					state = 2;//slow
			}

			double speed = 0;
			if (state == 1) {
				speed = 1;
			} else if (state == 2) {
				speed = 0.6;
			}else
			if (state == 3) {
				speed = -1;
			}
			if(moveRound) {
				reCurToTargetPosition.x = reCurToTargetPosition_copy.z * -1;
				reCurToTargetPosition.z = reCurToTargetPosition_copy.x;
				if (roundDir){
					reCurToTargetPosition.x *= -1;
					reCurToTargetPosition.z *= -1;
				}
				reCurToTargetPosition_copy.scale(speed);
				reCurToTargetPosition.add(reCurToTargetPosition_copy);
				speed = 0.6;
			}else {
				reCurToTargetPosition.scale(speed != 0?1:0);
			}
			moveToVec.add(reCurToTargetPosition);

//				shooter.getNavigator().setPath(worldForPathfind.getEntityPathToXYZ(shooter,
//						MathHelper.floor_double(moveToVec.x),
//						MathHelper.floor_double(moveToVec.y),
//						MathHelper.floor_double(moveToVec.z),
//						80, true, false, fagetMoveToPositionMnglse, false), speed);
			if(isOnGround(target))((IGVCmob)shooter).getMoveToPositionMng().getMoveToPos().set(
					moveToVec.x,
					moveToVec.y,
					moveToVec.z);
			((IGVCmob)shooter).getMoveToPositionMng().setMovingSpeed(speed);
		}
		dir_modeChangeCool--;
		if(shooter.getNavigator().getPath() == null)standingTime++;
		else standingTime--;
		if(dir_modeChangeCool < 0 || standingTime>100){
			dir_modeChangeCool = 20 + rnd.nextInt(60);
			roundDir = rnd.nextBoolean();
			moveRound = rnd.nextBoolean();
		}
	}

	@Override
	public void aimTarget() {
		Vector3d aimingPoint = getSeeingPosition();
//        System.out.println("" + aimingPoint);
		if(aimingPoint != null)shooter.getLookHelper().setLookPosition(aimingPoint.x, aimingPoint.y, aimingPoint.z, 1000000, 1000000);
	}

	@Override
	public void fireWeapon() {
		Vector3d aimingPoint = getSeeingPosition();
		if(aimingPoint == null)return;//目標位置不明では撃てない
		double maxrange = maxshootrange * maxshootrange;
		boolean canSeeTargetPos = ((IGVCmob)shooter).canSeePos(aimingPoint);
		double tocurrentAttackToPosition = shooter.getDistanceSq(aimingPoint.x, aimingPoint.y, aimingPoint.z);

		if (!canSeeTargetPos) {
			aiming = 0;
			shooter.setSneaking(false);
		} else {
			aiming++;
			shooter.setSneaking(true);
		}

//        System.out.println("\r" + "canSeeState " + canSeeState + "aiming" + aiming + "burstingtime" + burstingtime);

		if (maxrange > tocurrentAttackToPosition && !shooter.isPotionActive(Potion.blindness) &&
				((canSeeState && aiming > 40) || (!canSeeState && Warningcoolcnt < 0 && tocurrentAttackToPosition > 16))
				&& burstingtime > 0 && retriggerCool <= 0 && canSeeTargetPos) {
			ItemStack gunStack = null;
			if (shooter.ridingEntity instanceof PlacedGunEntity) {
				gunStack = ((PlacedGunEntity) shooter.ridingEntity).gunStack;
			}else
			if (shooter.getHeldItem() != null) {
				gunStack = shooter.getHeldItem();
			}

			if (gunStack != null && gunStack.getItem() instanceof HMGItem_Unified_Guns) {
				((HMGItem_Unified_Guns) gunStack.getItem()).checkTags(gunStack);
				gunStack.getTagCompound().setBoolean("IsTriggered", true);
				shooter.getEntityData().setBoolean("HMGisUsingItem", true);
				checkSelector(gunStack);
				if (((HMGItem_Unified_Guns) gunStack.getItem()).getburstCount(gunStack.getTagCompound().getInteger("HMGMode")) != -1) {
					retriggerCool = rnd.nextInt((int) (abs(((HMGItem_Unified_Guns) gunStack.getItem()).gunInfo.recoil) + 1) * 3);
				}
			}
			forget = 0;
		}
		burstingtime--;
		if (!canSeeState) {
			forget++;
		}
		if (burstingtime < 0) {
			if (burstcoolcnt < 0) {
				burstingtime = rnd.nextInt(bursttime);
				burstcoolcnt = burstcool;
			} else {
				burstcoolcnt--;
			}
		}
		if (!canSeeState && Warningcoolcnt < -20) {
			Warningcoolcnt = rnd.nextInt(Warningblank) + 80;
		}
		Warningcoolcnt--;
		retriggerCool--;
	}

//    public boolean setUp(){
//        EntityLivingBase entityliving = shooter.getAttackTarget();
//        if(shooter instanceof EntityGBases && entityliving instanceof EntityGBases){
//            entityliving = null;
//        }
//        if(shooter instanceof EntitySoBases && entityliving instanceof EntitySoBases){
//            entityliving = null;
//        }
//        if (entityliving == null || entityliving.isDead || forget > 500000) {
//            aiming = 0;
//            shooter.setAttackTarget(null);
//            shooter.setSneaking(false);
//            forget = 0;
//            lastSeenPosition = null;
//            return false;
//        } else {
//            target = entityliving;
//            return true;
//        }
//    }

	@Override
	public void resetTask() {
		((IGVCmob)shooter).getMoveToPositionMng().stop();
		if(shooter instanceof IGVCmob){
			((IGVCmob) shooter).setAimPos(null);
		}
		burstingtime = 0;
	}
	//    @Override
//    public void updateTask() {
//        try {
//            if (target != null) {
////                System.out.println("debug");
//                double maxrange = maxshootrange * maxshootrange;
//                double minrange = minshootrange * minshootrange;
//                if (shooter.ridingEntity == null && !(shooter.getHeldItem() != null && shooter.getHeldItem().getItem() instanceof HMGItem_Unified_Guns)) {
//                    maxrange = 0;
//                    minrange = 0;
//                }
//                Vector3d currentAttackToPosition = getSeeingPosition();
//                if (currentAttackToPosition != null) {
//                    if(shooter instanceof IGVCmob){
//                        ((IGVCmob) shooter).setAimPos(currentAttackToPosition);
//                    }
//                    double tocurrentAttackToPosition = shooter.getDistanceSq(currentAttackToPosition.x, currentAttackToPosition.y, currentAttackToPosition.z);
//                    boolean see = shooter.canEntityBeSeen(target);
//                    boolean isnotblinded;
//                    boolean canSee = isnotblinded = !shooter.isPotionActive(Potion.blindness);
//                    canSee &= see;
//                    if (canSee) {
//                        shooter.getLookHelper().setLookPosition(currentAttackToPosition.x, currentAttackToPosition.y, currentAttackToPosition.z, 1000000, 1000000);
//                    }else {
//                        lookAround();
//                    }
//                    if(refindpath < 0|| shooter.getNavigator().noPath() || (shooter.getHeldItem() == null || !(shooter.getHeldItem().getItem() instanceof HMGItem_Unified_Guns))){
//                        setPath(currentAttackToPosition,tocurrentAttackToPosition,canSee);
//                        refindpath = 10;
//                    }
//                    refindpath -= 1;
//                    boolean canSeeTargetPos = ((IGVCmob)shooter).canSeePos(currentAttackToPosition);
////                System.out.println("debug PR" + shooter.rotationPitch);
////                shooter.getLookHelper().onUpdateLook();
////                System.out.println("debug AF" + shooter.rotationPitch);
//                    if (!canSee) {
//                        aiming = 0;
//                        shooter.setSneaking(false);
//                    } else {
//                        aiming++;
//                        shooter.setSneaking(true);
//                    }
//                    if (maxrange > tocurrentAttackToPosition && isnotblinded &&
//                            ((canSee && aiming > 40) || (!canSee && Warningcoolcnt < 0 && tocurrentAttackToPosition > 16))
//                            && burstingtime > 0 && retriggerCool <= 0 && canSeeTargetPos) {
//                        ItemStack gunStack = null;
//                        if (shooter.ridingEntity instanceof PlacedGunEntity) {
//                            gunStack = ((PlacedGunEntity) shooter.ridingEntity).gunStack;
//                        }else
//                        if (shooter.getHeldItem() != null) {
//                            gunStack = shooter.getHeldItem();
//                        }
//
//                        if (gunStack != null && gunStack.getItem() instanceof HMGItem_Unified_Guns) {
//                            gunStack.getTagCompound().setBoolean("IsTriggered", true);
//                            shooter.getEntityData().setBoolean("HMGisUsingItem", true);
//                            checkSelector(gunStack);
//                            if (((HMGItem_Unified_Guns) gunStack.getItem()).getburstCount(gunStack.getTagCompound().getInteger("HMGMode")) != -1) {
//                                retriggerCool = rnd.nextInt((int) (abs(((HMGItem_Unified_Guns) gunStack.getItem()).gunInfo.recoil) + 1) * 3);
//                            }
//                        }
//                        forget = 0;
//                    }
//                    burstingtime--;
//                    if (!canSee) {
//                        forget++;
//                    }
//                    if (burstingtime < 0) {
//                        if (burstcoolcnt < 0) {
//                            burstingtime = rnd.nextInt(bursttime);
//                            burstcoolcnt = burstcool;
//                        } else {
//                            burstcoolcnt--;
//                        }
//                    }
//                    if (!canSee && Warningcoolcnt < -20) {
//                        Warningcoolcnt = rnd.nextInt(Warningblank) + 80;
//                    }
//                    Warningcoolcnt--;
//                    retriggerCool--;
//                }
//            }
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//    }
//    private void lookAround(){
//
//    }
//    private void setPath(Vector3d currentAttackToPosition,double tocurrentAttackToPosition,boolean canSee){
//
//    }
	private void checkSelector(ItemStack gunStack){

		if (!isSelectorChecked) {
			List<Integer> burst = ((HMGItem_Unified_Guns) gunStack.getItem()).gunInfo.burstcount;
			int temp = 0;
			int mode = 0;
			for (int i = 0; i < burst.size(); i++) {
				if (burst.get(i) == -1) {
					mode = i;
					break;
				}
				if (burst.get(i) != 0 && temp < burst.get(i)) {
					temp = burst.get(i);
					mode = i;
				}
			}
			if (burst.size() > mode)
				gunStack.getTagCompound().setInteger("HMGMode", mode);
			isSelectorChecked = true;
		}
	}
	//    private boolean canNavigate()
//    {
//        return this.shooter.onGround || shooter.isInWater() || this.shooter.isRiding() && this.shooter.ridingEntity instanceof EntityChicken;
//    }
}
