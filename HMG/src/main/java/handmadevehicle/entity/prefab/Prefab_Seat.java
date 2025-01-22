package handmadevehicle.entity.prefab;


import handmadevehicle.entity.parts.SeatObject;
import handmadevehicle.entity.parts.logics.BaseLogic;
import handmadevehicle.entity.parts.turrets.TurretObj;
import handmadevehicle.entity.parts.turrets.WeaponCategory;

public class Prefab_Seat {
	public double[] pos = new double[3];
	public boolean hasGun;
	public int[] mainid;
	public int sittingMainTurretID = -1;
	public int subid = -1;
	public float userProtect_maxDamageLevel = 0;
	public float zoomLevel = -1;
	public boolean stabilizedView = false;

	public boolean isBlindedSeat = false;

	public Prefab_Seat(){
	}
	public Prefab_Seat(double[] pos, boolean hasGun, int mainid, int subid){
		this.pos = pos;
		this.hasGun = hasGun;
//		this.aimMainGun = aimMainGun;
//		this.aimSubGun = aimSubGun;
//		this.seatOnTurret = seatOnTurret;
		if(mainid != -1)this.mainid = new int[]{mainid};
		this.subid = subid;
	}
	
	//public SeatObject getSeatOBJ(TurretObj[] turrets, Prefab_Vehicle_Base prefab_vehicle_base , BaseLogic baseLogic){
	//	WeaponCategory[] mainGun = null;
	//	WeaponCategory subGun = null;
	//	if(mainid != null && mainid.length > 0) {
	//		mainGun = new WeaponCategory[mainid.length];
	//		int cnt = 0;
	//		for (int groupID : mainid) {
	//			if(groupID >= 0){
	//				mainGun[cnt] = baseLogic.weaponCategories[groupID];
//	//				System.out.println("" + mainGun[cnt].prefab_weaponCategory.name);
	//			}
	//			cnt++;
	//		}
	//	}
//
	//	if(subid >= 0){
	//		subGun = prefab_vehicle_base.weaponCategory[subid].getWeaponGroup(turrets,prefab_vehicle_base,baseLogic);
//	//		if(subGun != null)System.out.println("" + subGun.prefab_weaponCategory.name);
	//	}
//
	//	return new SeatObject(pos,this,mainGun, subGun);
	//}
}
