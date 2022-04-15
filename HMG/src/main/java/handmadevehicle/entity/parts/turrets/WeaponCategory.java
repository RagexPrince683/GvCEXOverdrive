package handmadevehicle.entity.parts.turrets;

import handmadeguns.HMGPacketHandler;
import handmadeguns.Util.EntityLinkedPos_Motion;
import handmadeguns.network.PacketChangeMagazineType;
import handmadevehicle.entity.parts.IDriver;
import handmadevehicle.entity.parts.SeatObject;
import handmadevehicle.entity.parts.logics.BaseLogic;
import handmadevehicle.entity.prefab.Prefab_WeaponCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;

import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;

import static handmadevehicle.HMVehicle.HMV_Proxy;
import static handmadevehicle.Utils.*;
import static java.lang.Math.toDegrees;


public class WeaponCategory {
	public Prefab_WeaponCategory prefab_weaponCategory;

	public TurretObj[][] _aimControl_TurretGroups;
	public TurretObj[][] ___Fire_____TurretGroups;

	public TurretObj[][] _targeting__TurretGroups;//use as radar

	public TurretObj[] criterionTurret;

	public int currentFireGroup;
	public int currentFireTurret;
	public int currentFireInterval;

	public Entity currentUser;
	public BaseLogic linkedBaseLogic;


	public Entity target = null;
	public EntityLinkedPos_Motion targetPos = null;
	public Vec3 lockedBlockPos = null;



	public WeaponCategory(BaseLogic logic) {
		linkedBaseLogic = logic;

	}

	public void weaponCurrentUserUpdate(SeatObject seatObject,Entity currentUser){

//		if(currentUser instanceof EntityPlayer)System.out.println("debug" + prefab_weaponCategory.name);
//		if(currentUser instanceof EntityPlayer)System.out.println("debug___Fire_____TurretGroups");
		for(TurretObj[] turretObjs: ___Fire_____TurretGroups)for(TurretObj turretObj:turretObjs){
			turretObj.currentEntity = currentUser;
			if(turretObj.currentEntity instanceof EntityPlayer){
//				System.out.println("debug" + turretObj.prefab_turret.turretName);
				turretObj.playerControl = true;
			}
		}

//		if(currentUser instanceof EntityPlayer)System.out.println("debug_aimControl_TurretGroups");
		for(TurretObj[] turretObjs: _aimControl_TurretGroups)for(TurretObj turretObj:turretObjs){
			turretObj.currentEntity = currentUser;
			if(turretObj.currentEntity instanceof EntityPlayer){
//				System.out.println("debug" + turretObj.prefab_turret.turretName);
//				System.out.println("debug" + turretObj.prefab_turret.gunInfo.turretanglelimtYawMax);
				turretObj.playerControl = true;
			}
		}


//		if(currentUser instanceof EntityPlayer)System.out.println("debug_targeting__TurretGroups");
		for(TurretObj[] turretObjs: _targeting__TurretGroups)for(TurretObj turretObj:turretObjs){
			turretObj.currentEntity = currentUser;
			if(turretObj.currentEntity instanceof EntityPlayer){
//				System.out.println("debug" + turretObj.prefab_turret.turretName);
				turretObj.playerControl = true;
			}
		}
//		if(currentUser instanceof EntityPlayer)System.out.println("debug--------");


		{//SyncTarget
			target = null;
			targetPos = null;
			lockedBlockPos = null;
			for(TurretObj[] turretObjs: _targeting__TurretGroups){
				for(TurretObj turretObj:turretObjs){
					if(turretObj.target != null) {
						target = turretObj.target;
						targetPos = turretObj.targetPos;
					}
					if(turretObj.lockedBlockPos != null)
						lockedBlockPos = turretObj.lockedBlockPos;
				}
			}

			for(TurretObj[] aimTurretObjs: _aimControl_TurretGroups)for(TurretObj turretObj:aimTurretObjs){
//				turretObj.lockedBlockPos = lockedBlockPos;
			}
			for(TurretObj[] fireTurretObjs: ___Fire_____TurretGroups)for(TurretObj turretObj:fireTurretObjs){
//				System.out.println("" + target);
				turretObj.lockedBlockPos = lockedBlockPos;
				turretObj.target = target;
				turretObj.targetPos = targetPos;
			}
		}

		if(currentUser != null){
			for(TurretObj[] aAimGroup : _aimControl_TurretGroups){
				boolean first = true;
				for(TurretObj aAimTurret : aAimGroup){

					if(currentUser instanceof EntityPlayer)
						if(seatObject.syncToPlayerAngle){
							if(!seatObject.prefab_seat.stabilizedView && currentUser == HMV_Proxy.getEntityPlayerInstance()){
								;

								Quat4d currentcamRot = new Quat4d(linkedBaseLogic.bodyRot);
								currentcamRot.mul(linkedBaseLogic.camerarot_current);
								double[] cameraxyz = eulerfromQuat((currentcamRot));
								cameraxyz[0] = toDegrees(cameraxyz[0]);
								cameraxyz[1] = toDegrees(cameraxyz[1]);
								cameraxyz[2] = toDegrees(cameraxyz[2]);
								aAimTurret.aimtoAngle(cameraxyz[1], cameraxyz[0]);
							}else
								aAimTurret.aimtoAngle(((EntityPlayer) currentUser).rotationYaw, currentUser.rotationPitch);
						}
						else ;
					else if(currentUser instanceof EntityLiving && currentUser instanceof IDriver && ((IDriver) currentUser).getAimPos() != null){
						try {
							if(!NaNCheck(((IDriver) currentUser).getAimPos()))
								aAimTurret.aimToPos(
										((IDriver) currentUser).getAimPos().x,
										((IDriver) currentUser).getAimPos().y,
										((IDriver) currentUser).getAimPos().z);
						}catch (Exception e){
							((EntityLiving) currentUser).setAttackTarget(null);
							e.printStackTrace();
						}
					}
				}

			}

		}
	}

	public void weaponTriggerUpdateByUser(Entity currentUser, SeatObject seatObject , int triggerType) {
//		System.out.println("debug" + this.prefab_weaponCategory.name);
//		System.out.println("debug" + seatObject.gunTrigger[triggerType]);
		this.currentUser = currentUser;

		if(currentUser instanceof IDriver){
			if(triggerType == 0)
				((IDriver) currentUser).setWeaponMain(this);
			else if(triggerType == 1)
				((IDriver) currentUser).setWeaponSub(this);
		}
		if(seatObject.seekerKey || currentUser instanceof EntityLiving){//LockOn
			for(TurretObj[] turretObjs: _targeting__TurretGroups){
				for(TurretObj turretObj:turretObjs){
					turretObj.seekerUpdateSwitch = true;
				}
			}
		}
		if(seatObject.bulletTypeKey) {

			for(TurretObj[] fireTurretObjs: ___Fire_____TurretGroups)for(TurretObj turretObj:fireTurretObjs){
//				System.out.println("" + target);
				turretObj.cycleMagazineType();
			}

			seatObject.bulletTypeKey = false;
		}
		if (seatObject.gunTrigger[triggerType]) {
			if (seatObject.gunTriggerFreeze[triggerType] <= 0) {
				int shootNum = 0;
				for(int i = 0 ; i < ___Fire_____TurretGroups.length ; i++){
//					System.out.println("debug");
					if(currentFireGroup >= ___Fire_____TurretGroups.length){
						currentFireGroup = 0;
					}
					TurretObj[] turretObjs = ___Fire_____TurretGroups[currentFireGroup];
					boolean shootFlag = false;
					for(int j = 0 ; j < turretObjs.length ; j++){
//						System.out.println("debug");
						if(currentFireTurret >= turretObjs.length){
							currentFireTurret = 0;
						}
						TurretObj turretObj = turretObjs[currentFireTurret];
						currentFireTurret++;

						boolean flag = turretObj.fire();
						shootFlag |= flag;
						if((flag) && this.prefab_weaponCategory.sequentiallyFire){
							shootNum++;
						}

//						System.out.println("GR" + currentFireGroup);
//						System.out.println("TR" + currentFireTurret);
//						System.out.println("SH" + shootNum);
//						System.out.println("PR" + this.prefab_weaponCategory.perFireNum);
						if(shootNum >= this.prefab_weaponCategory.perFireNum){
							break;
						}
					}
					if(!this.prefab_weaponCategory.sequentiallyFire){
						if(shootFlag)shootNum++;
						else currentFireTurret = 0;
					}
					if(shootNum >= this.prefab_weaponCategory.perFireNum){
						break;
					}
					currentFireGroup++;
				}
				seatObject.gunTriggerFreeze[triggerType] = prefab_weaponCategory.fireInterval;
			}else{
				seatObject.gunTriggerFreeze[triggerType] -= 1;
			}
		} else {
			seatObject.gunTriggerFreeze[triggerType] -= 1;
		}
	}

	public boolean hasWaitToReadyWeapon(){
		for(TurretObj[] turretObjs: ___Fire_____TurretGroups)for(TurretObj turretObj:turretObjs){
			if(turretObj.replaceCoolDown <= 0) {
				if (!turretObj.isreloading() && turretObj.gunItem != null) {
					return true;
				} else if (turretObj.canReload()) {
					return true;
				}
			}
		}
		return false;
	}

	public void weaponTransformUpdateByUser(Entity currentUser, int seatCnt , SeatObject seatObject , BaseLogic baseLogic){
		this.currentUser = currentUser;

		SeatObject seatObject_Zoom = null;
		if(seatCnt != -1 && seatCnt < baseLogic.seatObjects_zoom.length)seatObject_Zoom = baseLogic.seatObjects_zoom[seatCnt];

		if(this.prefab_weaponCategory.userSittingTurretID != -1) {
			TurretObj sittingGun = baseLogic.allturrets[this.prefab_weaponCategory.userSittingTurretID];
			Vector3d tempplayerPos;
			if(this.prefab_weaponCategory.userSittingOffset != null){//replace
				tempplayerPos = new Vector3d(this.prefab_weaponCategory.userSittingOffset);
			}else
			if(currentUser == HMV_Proxy.getEntityPlayerInstance() && HMV_Proxy.iszooming() && seatObject_Zoom != null){
				tempplayerPos = new Vector3d(seatObject_Zoom.pos);
			}else {
				tempplayerPos = new Vector3d(seatObject.pos);
			}
			Vector3d temp = sittingGun.getUserPosition(tempplayerPos);
			transformVecforMinecraft(temp);
//			System.out.println(temp);
			currentUser.setPosition(temp.x,
					temp.y - (baseLogic.worldObj.isRemote && currentUser == HMV_Proxy.getEntityPlayerInstance() ? 0 : (currentUser.getEyeHeight() - currentUser.yOffset)),
					temp.z);
			currentUser.posX = temp.x;
			currentUser.posY = temp.y - (baseLogic.worldObj.isRemote && currentUser == HMV_Proxy.getEntityPlayerInstance() ? 0 : (currentUser.getEyeHeight() - currentUser.yOffset));
			currentUser.posZ = temp.z;
		}
	}

	public double getSpeed() {
		if(getCriterionTurret() != null && getCriterionTurret().gunItem != null)return getCriterionTurret().gunItem.getTerminalspeed();
		return 0;
	}

	public TurretObj getDisplayCriterionTurret() {
		if(prefab_weaponCategory.CriterionTurret == -1)return getCriterionTurret();
		return linkedBaseLogic.allturrets[prefab_weaponCategory.CriterionTurret];
	}

	public TurretObj getCriterionTurret() {
		for(TurretObj[] turretObjs: ___Fire_____TurretGroups)for(TurretObj turretObj:turretObjs){
			if(!turretObj.isreloading() && turretObj.gunItem != null){
				return turretObj;
			}else if(turretObj.isreloading() || turretObj.canReload()){
				return turretObj;
			}
		}
		return null;
	}

	public boolean aimState(){
		for(TurretObj[] turretObjs: _aimControl_TurretGroups)for(TurretObj turretObj:turretObjs){
			if(turretObj.readyaim){
				return true;
			}
		}
		return false;
	}

	public boolean isReady(){
		TurretObj criterionTurret = getCriterionTurret();
		boolean isAimReady = false;
		for(TurretObj[] turrets : _aimControl_TurretGroups){
			for(TurretObj turretObj:turrets){
				isAimReady |= turretObj.readyaim && criterionTurret.aimIn;
			}
		}
		return criterionTurret != null && !criterionTurret.isreloading() && isAimReady;
	}

	public static class WeaponCategorySyncData{
		public WeaponCategorySyncData(){

		}
	}

	public boolean isLockSuccess(Entity AITarget){
		if(_targeting__TurretGroups.length == 0 || target == AITarget){
			return true;
		}else{
			for(TurretObj[] turrets : ___Fire_____TurretGroups){
				for(TurretObj turretObj:turrets){
					if(!turretObj.getCurrentGuninfo().canlock || turretObj.getCurrentGuninfo().induction_precision <= 0 || turretObj.target == AITarget)
						return true;
				}
			}
			return false;
		}
	}
}
