package hmggvcmob.ai.newai;

import handmadeguns.network.PacketSpawnParticle;
import handmadevehicle.entity.EntityDummy_rider;
import handmadevehicle.entity.parts.SeatObject;
import handmadevehicle.entity.parts.logics.BaseLogic;
import handmadevehicle.entity.parts.turrets.WeaponCategory;
import handmadevehicle.entity.prefab.Prefab_Vehicle_Base;
import net.minecraft.entity.EntityLiving;

import javax.vecmath.Vector3d;

import static handmadeguns.HandmadeGunsCore.HMG_proxy;
import static handmadevehicle.Utils.LinePrediction;
import static handmadevehicle.Utils.getDistanceSq;
import static hmggvcmob.util.GVCUtil.*;
import static java.lang.Math.*;
import static net.minecraft.util.MathHelper.wrapAngleTo180_double;
import static net.minecraft.util.MathHelper.wrapAngleTo180_float;

public class AIAttackEntityByAirPlane extends AIAttackToEntity{
	public AIAttackEntityByAirPlane(EntityLiving shooter, AIAttackManager aiAttackManager) {
		super(shooter, aiAttackManager);
	}
	BaseLogic baseLogic;
	@Override
	public boolean shouldExecute() {
		//TODO 車両操縦を優先しつつ、乗っている座席に武装が無ければfalseを返す
		if(super.shouldExecute()) {//ターゲットチェック
			baseLogic = getDrivingVehicle(this.shooter);
			if (baseLogic != null && !baseLogic.prefab_vehicle.T_Land_F_Plane)
				return super.shouldExecute();
		}
		return false;
	}
	boolean AirToGround;
	int groundHeight;
	double alt;
	@Override
	public void movePosition() {

		baseLogic.setControl_throttle_down(false);
		baseLogic.setControl_throttle_up(false);
		baseLogic.setControl_yaw_Right(false);
		baseLogic.setControl_yaw_Left(false);
		baseLogic.setControl_Space(false);
		baseLogic.setControl_brake(false);
		baseLogic.setControl_flap(false);

		groundHeight = shooter.worldObj.getHeightValue((int) baseLogic.mc_Entity.posX, (int) baseLogic.mc_Entity.posZ);//target alt
		alt = (baseLogic.mc_Entity.posY - groundHeight);
		AirToGround = isOnGround(target);
		if(AirToGround){
			if(!onAttackPos)moveToAttackPosition_Ground();//攻撃行程へ移行
		}else {

		}
	}
	boolean onAttackPos = false;

	boolean separating = true;
	public int reChaseCool = 0;
	public void moveToAttackPosition_Ground(){

		Vector3d targetStandingPos = getSeeingPosition();
		if(targetStandingPos == null)return;
		Vector3d thisPos = new Vector3d(shooter.posX,shooter.posY,shooter.posZ);
		Vector3d planeToTargetStanding = new Vector3d();
		planeToTargetStanding.sub(targetStandingPos,thisPos);
		Vector3d attackStartPos = new Vector3d(targetStandingPos);

		float divePitch = (float) -toDegrees(asin(planeToTargetStanding.y/ planeToTargetStanding.length()));


		if(!onAttackPos){
			float cof = 0.5f;
//			baseLogic.maxbank_fromOther = baseLogic.prefab_vehicle.maxbank/3;
			separating = !(baseLogic.prefab_vehicle.startDive > divePitch && alt> baseLogic.prefab_vehicle.cruiseALT);
			if(separating)cof = (float) (1 + 25/getDistanceSq(thisPos,targetStandingPos));
			attackStartPos.interpolate(thisPos,cof);
			attackStartPos.y = max(groundHeight + baseLogic.prefab_vehicle.cruiseALT + 30,target.posY + baseLogic.prefab_vehicle.cruiseALT + 30);
//			PacketSpawnParticle packetSpawnParticle = new PacketSpawnParticle( baseLogic.mc_Entity.posX,
//					baseLogic.mc_Entity.posY+2,
//					baseLogic.mc_Entity.posZ,
//					attackStartPos.x,
//					attackStartPos.y,
//					attackStartPos.z, 3);
//			packetSpawnParticle.trailwidth = 0.1f;
//			packetSpawnParticle.animationspeed = 1;
//			packetSpawnParticle.name = "Vapour";
//			packetSpawnParticle.fuse = 1;
//			HMG_proxy.spawnParticles(packetSpawnParticle);
			Vector3d planeToTarget = new Vector3d();
			planeToTarget.sub(attackStartPos,thisPos);
			baseLogic.server_easyMode_yawTarget = wrapAngleTo180_float(-(float) toDegrees(atan2(planeToTarget.x, planeToTarget.z)));
			float targetpitch = (float) -toDegrees(asin(planeToTarget.y/planeToTarget.length()));

			if(targetpitch < baseLogic.prefab_vehicle.maxClimb){
				targetpitch = baseLogic.prefab_vehicle.maxClimb;
			}
			baseLogic.server_easyMode_pitchTarget = targetpitch;
			baseLogic.setControl_flap(true);
			baseLogic.setControl_throttle_up(true);
			if(!separating){
				float dif = wrapAngleTo180_float((float) (baseLogic.bodyrotationYaw - baseLogic.server_easyMode_yawTarget));
				if(abs(dif) < 15)onAttackPos = true;
			}
		}
	}

	@Override
	public void aimTarget() {
		Prefab_Vehicle_Base prefab_vehicle = baseLogic.prefab_vehicle;
		baseLogic.setControl_throttle_up(true);

		Vector3d targetPos = getSeeingPosition();
		if(targetPos != null)shooter.getLookHelper().setLookPosition(targetPos.x, targetPos.y, targetPos.z, 1000000, 1000000);
		else return;
		Vector3d thisPos = new Vector3d(shooter.posX,shooter.posY,shooter.posZ);
		Vector3d planeToTarget = new Vector3d();
		Vector3d relMotionVec = new Vector3d(target.motionX,target.motionY,target.motionZ);
		relMotionVec.sub(baseLogic.mc_Entity.getBaseLogic().motionvec);
		SeatObject seatObject = getRidingSeat((EntityDummy_rider) shooter.ridingEntity);
		seatObject.searchMainWeapon();
		WeaponCategory mainWeapon = seatObject.currentMainWeapon();
		WeaponCategory sub_Weapon = seatObject.subWeapon;
		double weaponSpeed = 0;
		WeaponCategory currentWeapon = null;
		if(mainWeapon != null && mainWeapon.hasWaitToReadyWeapon()){
			currentWeapon = mainWeapon;
			weaponSpeed = mainWeapon.getSpeed();
		}else {
			seatObject.searchMainWeapon();
			mainWeapon = seatObject.currentMainWeapon();
		}
		if(mainWeapon != null && mainWeapon.hasWaitToReadyWeapon()) {
			currentWeapon = mainWeapon;
			weaponSpeed = mainWeapon.getSpeed();
		}else
		if(sub_Weapon != null && sub_Weapon.hasWaitToReadyWeapon()){
			currentWeapon = sub_Weapon;
			weaponSpeed = sub_Weapon.getSpeed();
		}

		Vector3d predictedTargetPos =
				LinePrediction(thisPos,
						targetPos,
						relMotionVec,weaponSpeed
				);
		planeToTarget.sub(predictedTargetPos,thisPos);
		//攻撃行程。対地攻撃照準
		float targetyaw  = wrapAngleTo180_float(-(float) toDegrees(atan2(planeToTarget.x, planeToTarget.z)));
		float targetpitch = (float) -toDegrees(asin(planeToTarget.y/planeToTarget.length()));

		if(AirToGround){
			if(onAttackPos){

//				PacketSpawnParticle packetSpawnParticle = new PacketSpawnParticle( baseLogic.mc_Entity.posX,
//						baseLogic.mc_Entity.posY+2,
//						baseLogic.mc_Entity.posZ,
//						predictedTargetPos.x,
//						predictedTargetPos.y,
//						predictedTargetPos.z, 3);
//				packetSpawnParticle.trailwidth = 0.1f;
//				packetSpawnParticle.animationspeed = 1;
//				packetSpawnParticle.name = "Vapour";
//				packetSpawnParticle.fuse = 1;
//				HMG_proxy.spawnParticles(packetSpawnParticle);
				baseLogic.server_easyMode_yawTarget = targetyaw;
				baseLogic.server_easyMode_pitchTarget = targetpitch;

				if(targetpitch > prefab_vehicle.maxDive || alt < prefab_vehicle.minALT){
					onAttackPos = false;
					separating = true;
				}
			}
		}else {
			if ((alt + baseLogic.mc_Entity.motionY * 10 < prefab_vehicle.minALT)) {
				if(!prefab_vehicle.type_F_Plane_T_Heli){
					if (alt < prefab_vehicle.cruiseALT)
						baseLogic.server_easyMode_pitchTarget = prefab_vehicle.maxClimb;
					else
						baseLogic.server_easyMode_pitchTarget = 0;
				}else {
					if (alt < prefab_vehicle.cruiseALT)
						baseLogic.server_easyMode_pitchTarget = prefab_vehicle.maxClimb*baseLogic.localMotionVec.z;
					else
						baseLogic.server_easyMode_pitchTarget = prefab_vehicle.cruiseNoseDown;
				}
				baseLogic.server_easyMode_yawTarget = targetyaw;
				if(alt < prefab_vehicle.minALT)baseLogic.setControl_flap(true);
			} else {
				if(reChaseCool>0){
					reChaseCool--;
					if(!prefab_vehicle.type_F_Plane_T_Heli){
						baseLogic.server_easyMode_pitchTarget = prefab_vehicle.maxClimb;
					}else {
						baseLogic.server_easyMode_pitchTarget = prefab_vehicle.maxClimb*baseLogic.localMotionVec.z;
					}
				}else {
					if(targetpitch>prefab_vehicle.maxDive && alt < prefab_vehicle.minALT){
						reChaseCool=300;
					}
					baseLogic.server_easyMode_pitchTarget = !prefab_vehicle.type_F_Plane_T_Heli ? -20 : 0;

					baseLogic.server_easyMode_yawTarget = targetyaw;
					if(target.ridingEntity instanceof EntityDummy_rider) {
						double enemyYawing = ((EntityDummy_rider) target.ridingEntity).linkedBaseLogic.bodyrotationYaw - ((EntityDummy_rider) target.ridingEntity).linkedBaseLogic.prevbodyrotationYaw;
						baseLogic.rollTarget_fromOther = (float) toDegrees(enemyYawing);
//								System.out.println("debug" + (toDegrees(enemyYawing)));
					}
					baseLogic.server_easyMode_pitchTarget = targetpitch;
					double angularDifferenceYaw = wrapAngleTo180_double(targetyaw - baseLogic.bodyrotationYaw);
					if (abs(angularDifferenceYaw) > 60) {
						baseLogic.server_easyMode_pitchTarget *= 1 - (abs(angularDifferenceYaw) - 60) / 150;
						baseLogic.server_easyMode_pitchTarget -= 5;
					}
				}
			}
			baseLogic.setControl_throttle_up(true);
		}
		if(baseLogic.bodyvector != null && -baseLogic.bodyvector.dot(planeToTarget)/planeToTarget.length() > cos(toRadians(prefab_vehicle.yawsightwidthmin)) && currentWeapon != null){
			if(currentWeapon != null && currentWeapon == mainWeapon && (currentWeapon.target == null || isCollectLocking(currentWeapon.target , target)))seatObject.gunTrigger[0] = true;
			if(sub_Weapon != null && (currentWeapon == sub_Weapon || prefab_vehicle.useMain_withSub) && (sub_Weapon.target == null || isCollectLocking(currentWeapon.target , target)))seatObject.gunTrigger[1] = true;
		}
	}

	@Override
	public void fireWeapon() {

	}
}
