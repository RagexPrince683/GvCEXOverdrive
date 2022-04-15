package hmggvcmob.ai.newai;

import handmadevehicle.entity.EntityDummy_rider;
import handmadevehicle.entity.parts.SeatObject;
import handmadevehicle.entity.parts.logics.BaseLogic;
import handmadevehicle.entity.parts.turrets.WeaponCategory;
import hmggvcmob.entity.IGVCmob;
import net.minecraft.entity.EntityLiving;

import javax.vecmath.Vector3d;

import java.util.Random;

import static hmggvcmob.util.GVCUtil.*;
import static java.lang.Math.sqrt;

public class AIAttackEntityByTank extends AIAttackToEntity{
	private int fireCool;
	private int firingTime;
	public int fireCool_setting = 30;
	public int firingTime_setting = 120;

	public AIAttackEntityByTank(EntityLiving shooter, AIAttackManager aiAttackManager) {
		super(shooter, aiAttackManager);
	}
	BaseLogic ridingVehicle,drivingVehicle;
	@Override
	public boolean shouldExecute() {
		//TODO 車両操縦を優先しつつ、乗っている座席に武装が無ければfalseを返す
		if(super.shouldExecute()) {//ターゲットチェック
			ridingVehicle = getRidingVehicle(this.shooter);
			drivingVehicle = getDrivingVehicle(this.shooter);
			if (ridingVehicle != null && drivingVehicle != null && drivingVehicle.prefab_vehicle.T_Land_F_Plane)
				return true;//地上車両操縦
			if (ridingVehicle != null && drivingVehicle == null)
				return true;//砲塔を操作する
		}
		return false;
	}
	private float dist_move_max = 3600;
	private float dist_move_min = 900;
	private float dist_fire_max = 256*256;
	private float dist_fire_min = 0;
	private int dir_modeChangeCool = 600;
	private boolean moveRound_Dir;
	private boolean moveRound = false;
	@Override
	public void movePosition() {
		if(drivingVehicle != null){
			Vector3d currentAttackToPosition = getSeeingPosition();
			if (currentAttackToPosition != null) {
				double dist = drivingVehicle.mc_Entity.getDistanceSq(currentAttackToPosition.x, currentAttackToPosition.y, currentAttackToPosition.z);
				boolean canSeeTargetPos = ((IGVCmob)shooter).canSeePos(currentAttackToPosition);
				float speed;
				if(!canSeeTargetPos){
					dist_move_max = (float) sqrt(dist) - 10;
					dist_move_min = dist_move_max-30;

					if(dist_move_max < 0 )dist_move_max = 0;
					if(dist_move_min < 0 )dist_move_min = 0;

					dist_move_max *= dist_move_max;
					dist_move_min *= dist_move_min;
				}else {
					if(dist_move_max < 3600){
						dist_move_max = (float) sqrt(dist) + 10;
						dist_move_min = dist_move_max-30;

						if(dist_move_max < 0 )dist_move_max = 0;
						if(dist_move_min < 0 )dist_move_min = 0;

						dist_move_max *= dist_move_max;
						dist_move_min *= dist_move_min;
					}
				}
				if (moveRound) {
					speed = 1;
				} else if (dist < dist_move_min) {
					speed = -1;
				} else if (dist > dist_move_max) {
					speed = 1;
				} else {
					speed = 0;
				}
				if (speed != 0) {
					Vector3d relativeCurrentAttackToPosition = new Vector3d(currentAttackToPosition);
					Vector3d moveTo = new Vector3d(drivingVehicle.mc_Entity.posX, drivingVehicle.mc_Entity.posY, drivingVehicle.mc_Entity.posZ);
					relativeCurrentAttackToPosition.sub(moveTo);
					if (moveRound) {
						Vector3d relativeCurrentAttackToPosition_Copy = new Vector3d(relativeCurrentAttackToPosition);
						relativeCurrentAttackToPosition.x = relativeCurrentAttackToPosition_Copy.z;
						relativeCurrentAttackToPosition.z = -relativeCurrentAttackToPosition_Copy.x;

						if (moveRound_Dir) {
							relativeCurrentAttackToPosition.x *= -1;
							relativeCurrentAttackToPosition.z *= -1;
						}
						if (dist < dist_move_min) {
							relativeCurrentAttackToPosition.sub(relativeCurrentAttackToPosition_Copy);
						} else if (dist > dist_move_max) {
							relativeCurrentAttackToPosition.add(relativeCurrentAttackToPosition_Copy);
						}
					}
					moveTo.add(relativeCurrentAttackToPosition);
					if(isOnGround(target))((IGVCmob)shooter).getMoveToPositionMng().getMoveToPos().set(
							moveTo.x,
							moveTo.y,
							moveTo.z);
					((IGVCmob)shooter).getMoveToPositionMng().setMovingSpeed(speed);
				}else {
					((IGVCmob)shooter).getMoveToPositionMng().stop();
				}
			}
			dir_modeChangeCool--;
			if (dir_modeChangeCool < 0) {
				dir_modeChangeCool = 1500 + rnd.nextInt(500);
				moveRound_Dir = rnd.nextBoolean();
				moveRound = rnd.nextBoolean();
			}
		}
	}
	public void resetTask(){
		dist_move_max = 3600;
		dist_move_min = 900;
	}

	@Override
	public void aimTarget() {
		((IGVCmob)shooter).setAimPos(getSeeingPosition());
		Vector3d aimingPoint = getSeeingPosition();
		if(aimingPoint != null)shooter.getLookHelper().setLookPosition(aimingPoint.x, aimingPoint.y, aimingPoint.z, 1000000, 1000000);
	}

	@Override
	public void fireWeapon() {
		SeatObject ridingSeat = getRidingSeat((EntityDummy_rider) shooter.ridingEntity);

		Vector3d currentAttackToPosition = getSeeingPosition();
		if(currentAttackToPosition == null || ridingVehicle == null || ridingVehicle.mc_Entity == null)return;

		double dist = ridingVehicle.mc_Entity.getDistanceSq(currentAttackToPosition.x, currentAttackToPosition.y, currentAttackToPosition.z);

		boolean needFire = fireCool < 0;
		boolean canSeeTargetPos = ((IGVCmob)shooter).canSeePos(currentAttackToPosition);

		WeaponCategory mainWeapon = ridingSeat.currentMainWeapon();

		if(mainWeapon != null && mainWeapon.hasWaitToReadyWeapon()){
		}else {
			mainWeapon = ridingSeat.currentMainWeapon();
		}

		boolean aimStateMain = false;
		boolean aimStateSub = false;
		ridingSeat.gunTrigger[0] = false;
		ridingSeat.gunTrigger[1] = false;
		if (needFire && dist < dist_fire_max && dist > dist_fire_min && canSeeTargetPos) {
			System.out.println("debug fireProcess");
			if (mainWeapon != null) {
				System.out.println("debug MainGunFire" + mainWeapon.prefab_weaponCategory.name);
				System.out.println("debug MainGun LockState " + mainWeapon.isLockSuccess(target));
				System.out.println("debug MainGun IsReady   " + mainWeapon.isReady());
				aimStateMain = mainWeapon.aimState();
				System.out.println("debug MainGun AimState  " + aimStateMain);
//						System.out.println(iTurretUser.getTurretMain().turretID_OnVehicle);
				if(mainWeapon.isLockSuccess(target) && mainWeapon.isReady()) {
					if (aimStateMain) {
						ridingSeat.gunTrigger[0] = true;
					}
				}else{
					ridingSeat.cycleWeapon();
				}
			}
			if (ridingSeat.subWeapon != null) {
				aimStateSub = (aimStateMain && ridingSeat.subWeapon._aimControl_TurretGroups[0].length == 0) || ridingSeat.subWeapon.aimState();
				;
				if (ridingSeat.subWeapon.isLockSuccess(target) && ridingSeat.subWeapon.isReady() && aimStateSub){
					ridingSeat.gunTrigger[1] = true;
				}
			}
			firingTime--;
		}
		fireCool--;
		if (firingTime < 0) {
			firingTime = rnd.nextInt(firingTime_setting);
			fireCool = rnd.nextInt(fireCool_setting);
		}
	}
}
