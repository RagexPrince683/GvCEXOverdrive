package handmadevehicle.render;

import handmadeguns.client.render.HMGGunParts;
import handmadeguns.client.render.HMGGunParts_Motion;
import handmadeguns.client.render.HMGGunParts_Motion_PosAndRotation;
import handmadeguns.client.render.HMGGunParts_Motions;

import static handmadeguns.HMGGunMaker.readerCnt;
import static java.lang.Integer.parseInt;

public class HMVVehicleParts extends HMGGunParts {
	public int linkedTurretID = -1;
	public boolean isTurretParts = false;
	public boolean isTurret_linkedGunMount = false;
	public int trackPieceCount;
	public float trackAnimOffset;
	public float trackAnimSpeed  = 1;
	public float idleAnimSpeed = 1;
	public int idlePieceCount;
	public float idleAnimOffset;
	private HMGGunParts_Motion_PosAndRotation peraPosAndRotation;
	private HMGGunParts_Motion_PosAndRotation idlePosAndRotation;
	public HMGGunParts_Motions trackPositions = new HMGGunParts_Motions();
	public HMGGunParts_Motions idlePositions = new HMGGunParts_Motions();
	private HMGGunParts_Motion_PosAndRotation[] somethingPosAndRotation = new HMGGunParts_Motion_PosAndRotation[17];//x,y,z,z2,yaw,pitch,roll,gear,flap,brake,speed
	public HMGGunParts_Motions[] somethingMotionKey;
	public boolean isTrack;
	public boolean isPera;
	public boolean isIdleAnim;

	public HMGGunParts_Motions[] trackMotionKeyCache;

	public void AddSomethingMotionKey(String[] type){
		if(somethingMotionKey == null)somethingMotionKey = new HMGGunParts_Motions[17];
		HMGGunParts_Motion motion = new HMGGunParts_Motion(type);
		int id = parseInt(type[readerCnt++]);
		if(somethingMotionKey[id] == null) somethingMotionKey[id] = new HMGGunParts_Motions();
		somethingMotionKey[id].addmotion(motion);
	}
	public void AddTrackPositions(String[] type){
		HMGGunParts_Motion motion = new HMGGunParts_Motion(type);
		trackPositions.addmotion(motion);
		isbelt = true;
	}
	public void AddIdlePositions(String[] type){
		HMGGunParts_Motion motion = new HMGGunParts_Motion(type);
		idlePositions.addmotion(motion);
		isbelt = true;
	}
	
	public void setIsTrack(boolean isPera,int trackPieceCount){
		this.isTrack = isPera;
		this.trackPieceCount = trackPieceCount;
	}

	public void setIsIdleAnim(boolean isPera,int trackPieceCount){
		this.isIdleAnim = isPera;
		this.idlePieceCount = trackPieceCount;
	}
	public void setIsPera(boolean isPera){
		this.isPera = isPera;
	}
	public void setIsTrack_Cloning(boolean isTrack,int trackPieceCount){
		this.isTrack = isTrack;
		this.trackPieceCount = trackPieceCount;
		this.isavatar = true;
	}
	public void setIsIdleAnim_Cloning(boolean isPera,int trackPieceCount){
		this.isIdleAnim = isPera;
		this.idlePieceCount = trackPieceCount;
		this.isavatar = true;
	}
	
	public void AddRenderinfTrack(float offsetX, float offsetY, float offsetZ, float rotationX, float rotationY, float rotationZ){
		peraPosAndRotation = new HMGGunParts_Motion_PosAndRotation(offsetX,offsetY,offsetZ,rotationX,rotationY,rotationZ);
	}

	public void AddRenderinfSomething(float offsetX, float offsetY, float offsetZ, float rotationX, float rotationY, float rotationZ,int id){
		somethingPosAndRotation[id] = new HMGGunParts_Motion_PosAndRotation(offsetX,offsetY,offsetZ,rotationX,rotationY,rotationZ);
	}
	public HMGGunParts_Motion_PosAndRotation getRenderinfOfPeraPosAndRotation(){
		return peraPosAndRotation;
	}
	public HMGGunParts_Motion_PosAndRotation getRenderinfOfSomethingPosAndRotation(int id){
		return somethingPosAndRotation[id];
	}
	public HMGGunParts_Motion_PosAndRotation getTrackPositions(float flame){
		return trackPositions.getpartsMotion(flame);
	}
	public HMGGunParts_Motion_PosAndRotation getSomethingPositions(float flame,int id){
		if(somethingMotionKey[id] == null)return null;
		return somethingMotionKey[id].getpartsMotion(flame);
	}
	
	public HMVVehicleParts(String string) {
		super(string);
	}
	
	public HMVVehicleParts(String string, int motherID, HMGGunParts mother) {
		super(string,motherID,mother);
	}

	public HMGGunParts_Motion_PosAndRotation getRenderinfOfIdlePosAndRotation() {
		return idlePosAndRotation;
	}

	public HMGGunParts_Motion_PosAndRotation getidlePositions(float flame) {
		return idlePositions.getpartsMotion(flame);
	}
}
