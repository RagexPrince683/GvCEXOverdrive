package handmadevehicle.entity.prefab;

import handmadevehicle.entity.parts.logics.BaseLogic;
import handmadevehicle.entity.parts.turrets.TurretObj;
import handmadevehicle.entity.parts.turrets.WeaponCategory;

import javax.vecmath.Vector3d;

public class Prefab_WeaponCategory {
	public String name;
	public int[][] _aimControl_TurretGroupsID;//1dim:groupID; 2dim 0~:aimTurret

	public int[][] ___Fire_____TurretGroupsID;//1dim:groupID; 2dim 0~:salvo Turrets


	public int[][] _targeting__TurretGroupsID;//use Turret as radar; 1dim: groupID; 2dim: get Target Turrets
	//can duplication. as[0][0] = 1,[1][0] = 1. it is that target of group 0 is target of group 1

	public boolean fireFromReadyAim = true;//wait still aiming turret. preventing waste.

	public boolean sequentiallyFire = false;//not salvo fire in fireGroup

	public int fireInterval = 0;

	public int perFireNum = 1;

	public int userSittingTurretID = -1;
	public Vector3d userSittingOffset;
	public int CriterionTurret = -1;

	//public WeaponCategory getWeaponGroup(TurretObj[] turrets, Prefab_Vehicle_Base prefab_vehicle_base , BaseLogic logic) {
	//	WeaponCategory newWeaponCategory = new WeaponCategory(logic);
	//	newWeaponCategory.prefab_weaponCategory = this;
	//	if(_aimControl_TurretGroupsID != null){
	//		newWeaponCategory._aimControl_TurretGroups = new TurretObj[_aimControl_TurretGroupsID.length][];
	//		int groupID = 0;
	//		for(int[] turretIDs : _aimControl_TurretGroupsID){
	//			newWeaponCategory._aimControl_TurretGroups[groupID] = new TurretObj[turretIDs.length];
	//			int turretID = 0;
	//			for(int aTurretID : turretIDs){
	//				newWeaponCategory._aimControl_TurretGroups[groupID][turretID] = turrets[aTurretID];
	//				turretID++;
	//			}
	//			groupID++;
	//		}
	//	}
//
	//	if(___Fire_____TurretGroupsID != null){
	//		newWeaponCategory.___Fire_____TurretGroups = new TurretObj[___Fire_____TurretGroupsID.length][];
	//		int groupID = 0;
	//		for(int[] turretIDs : ___Fire_____TurretGroupsID){
	//			newWeaponCategory.___Fire_____TurretGroups[groupID] = new TurretObj[turretIDs.length];
	//			int turretID = 0;
	//			for(int aTurretID : turretIDs){
	//				newWeaponCategory.___Fire_____TurretGroups[groupID][turretID] = turrets[aTurretID];
	//				turretID++;
	//			}
	//			groupID++;
	//		}
	//	}
//
	//	if(_targeting__TurretGroupsID != null){
	//		newWeaponCategory._targeting__TurretGroups = new TurretObj[_targeting__TurretGroupsID.length][];
	//		int groupID = 0;
	//		for(int[] turretIDs : _targeting__TurretGroupsID){
	//			newWeaponCategory._targeting__TurretGroups[groupID] = new TurretObj[turretIDs.length];
	//			int turretID = 0;
	//			for(int aTurretID : turretIDs){
	//				newWeaponCategory._targeting__TurretGroups[groupID][turretID] = turrets[aTurretID];
	//				turretID++;
	//			}
	//			groupID++;
	//		}
	//	}
//
	//	return newWeaponCategory;
	//}
}
