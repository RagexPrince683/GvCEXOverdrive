package handmadevehicle.entity.parts;

import handmadevehicle.entity.parts.turrets.WeaponCategory;
import handmadevehicle.entity.prefab.Prefab_Seat;

import javax.vecmath.Vector3d;

public class SeatObject {
    public SeatObject(){
    
    }
    public SeatObject(double[] pos, Prefab_Seat prefab_seat, WeaponCategory[] mainWeapon, WeaponCategory subWeapon){
        this(pos);
        this.prefab_seat = prefab_seat;
        this.mainWeapon = mainWeapon;
        this.subWeapon = subWeapon;
    }
    public SeatObject(double[] pos){
        this.pos = pos;
    }
    public SeatObject(Vector3d pos){
        this.pos = new double[]{pos.x,pos.y,-pos.z};
    }

    public boolean hasAvailableWeapon(){
        try {
            if(this.mainWeapon != null && this.mainWeapon[currentWeaponMode] != null && this.mainWeapon[currentWeaponMode].hasWaitToReadyWeapon())return true;
            if(this.subWeapon != null && this.subWeapon.hasWaitToReadyWeapon())return true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }
    public double[] pos = new double[]{0,1.5,0};
    public Prefab_Seat prefab_seat;
    public boolean[] gunTrigger = {false,false};
    public int[] gunTriggerFreeze = {10,10};
    public boolean seekerKey;
    public boolean bulletTypeKey;


    public boolean syncToPlayerAngle = true;

    public WeaponCategory[] mainWeapon;
    public int currentWeaponMode = 0;
    public WeaponCategory subWeapon;
    public Vector3d currentSeatOffset_fromV = new Vector3d();
    public Vector3d prevSeatOffset_fromV = new Vector3d();

	public WeaponCategory currentMainWeapon() {
	    if(mainWeapon != null)
	        return mainWeapon[currentWeaponMode];
	    return null;
	}
	public void searchMainWeapon(){
	    if(mainWeapon != null)
	    for(int i = 0 ; i < mainWeapon.length;i++){
            if(mainWeapon[currentWeaponMode].hasWaitToReadyWeapon()){
                return;
            }
            cycleWeapon();
        }
    }
    public void cycleWeapon(){
	    currentWeaponMode++;
	    if(currentWeaponMode >= mainWeapon.length)currentWeaponMode = 0;
    }
}
