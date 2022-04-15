package handmadevehicle;

import handmadevehicle.entity.parts.turrets.FireRist;
import handmadevehicle.entity.prefab.Prefab_Turret;

import javax.vecmath.Vector3d;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import static handmadeguns.HMGGunMaker.readFireInfo;
import static java.lang.Boolean.parseBoolean;
import static java.lang.Float.parseFloat;
import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;

public class AddWeapon {
	public static final HashMap<String,Prefab_Turret> prefab_turretHashMap = new HashMap<>();
	public static void load( boolean isClient, File file){
		int line = 0;
		String current = "";
		try {
			//File file = new File(configfile,"hmg_handmadeguns.txt");
			if (checkBeforeReadfile(file))
			{
				
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file),"Shift-JIS"));
				String str;
				Prefab_Turret prefab_turret = new Prefab_Turret();
				String name = null;
				float   modelhigh = 0;
				float   modelhighr = 0;
				float   modelhighs = 0;
				float   modelwidthx = 0;
				float   modelwidthxr = 0;
				float   modelwidthxs = 0;
				float   modelwidthz = 0;
				float   modelwidthzr = 0;
				float   modelwidthzs = 0;
				while((str = br.readLine()) != null) {
					current = str;
					line++;
					String[] type = str.split(",");
					switch (type[0]){
						case "WeaponName":
							name = type[1];
							if(prefab_turretHashMap.containsKey(name))prefab_turret = prefab_turretHashMap.get(name);
							prefab_turret.turretName = name;
							break;
						case "WeaponDisplayName":
							prefab_turret.turretName = type[1];
							break;
						case "turretYawCenterpos":
							prefab_turret.gunInfo.posGetter.turretRotationYawPoint = new double[]{parseDouble(type[1]), parseDouble(type[2]), parseDouble(type[3])};
							prefab_turret.gunInfo.posGetter.turretYawCenterpos = new Vector3d(parseDouble(type[1]),parseDouble(type[2]),parseDouble(type[3]));
							break;
						case "turretPitchCenterpos":
							prefab_turret.gunInfo.posGetter.turretRotationPitchPoint = new double[]{parseDouble(type[1]), parseDouble(type[2]), parseDouble(type[3])};
							prefab_turret.gunInfo.posGetter.turretPitchCenterpos = new Vector3d(parseDouble(type[1]),parseDouble(type[2]),parseDouble(type[3]));
							break;
						case "cannonPos":
							prefab_turret.gunInfo.posGetter.cannonPos = new Vector3d(parseDouble(type[1]),parseDouble(type[2]),parseDouble(type[3]));
							prefab_turret.gunInfo.posGetter.cartPos = new Vector3d(parseDouble(type[1]),parseDouble(type[2]),parseDouble(type[3]));
							break;
						case "multicannonPos":
							prefab_turret.gunInfo.posGetter.multiCannonPos = new Vector3d[(type.length-1)/3];
							prefab_turret.gunInfo.posGetter.multiCartPos = new Vector3d[(type.length-1)/3];
							for(int id = 0; id < prefab_turret.gunInfo.posGetter.multiCannonPos.length; id++){
								prefab_turret.gunInfo.posGetter.multiCannonPos[id] = new Vector3d(parseDouble(type[id * 3 + 1]),parseDouble(type[id * 3 + 2]),parseDouble(type[id * 3 + 3]));
								prefab_turret.gunInfo.posGetter.multiCartPos[id] = new Vector3d(parseDouble(type[id * 3 + 1]),parseDouble(type[id * 3 + 2]),parseDouble(type[id * 3 + 3]));
							}
							break;
						case "cartPos":
							prefab_turret.gunInfo.posGetter.cartPos = new Vector3d(parseDouble(type[1]),parseDouble(type[2]),parseDouble(type[3]));
							break;
						case "multiCartPos":
							prefab_turret.gunInfo.posGetter.multiCartPos = new Vector3d[(type.length-1)/3];
							for(int id = 0; id < prefab_turret.gunInfo.posGetter.multiCartPos.length; id++){
								prefab_turret.gunInfo.posGetter.multiCartPos[id] = new Vector3d(parseDouble(type[id * 3 + 1]),parseDouble(type[id * 3 + 2]),parseDouble(type[id * 3 + 3]));
							}
							break;
						case "linked_MotherTrigger":
							prefab_turret.linked_MotherTrigger = parseBoolean(type[1]);
							break;
						case "fireAll_child":
						case "salvo_fire_child":
							prefab_turret.salvo_fire_child = parseBoolean(type[1]);
							break;
						case "fireAll_cannon":
							prefab_turret.fireAll_cannon = parseBoolean(type[1]);
							break;
						case "fire_cannon_perOneShot":
							prefab_turret.fire_cannon_perOneShot = parseInt(type[1]);
							break;
						case "positionLinked":
							prefab_turret.positionLinked = parseBoolean(type[1]);
							break;
						case "onlyAim":
							prefab_turret.onlyAim = true;
							break;
						case "onlyRadar":
							prefab_turret.onlyRadar = true;
							break;
						case "syncTurretAngle":
							prefab_turret.syncTurretAngle = parseBoolean(type[1]);
							break;
						case "lockToPilotAngle":
							prefab_turret.lockToPilotAngle = parseBoolean(type[1]);
							break;
						case "lockOnByVehicle":
							prefab_turret.lockOnByVehicle = parseBoolean(type[1]);
							break;
						case "elevationType":
							prefab_turret.elevationType = parseInt(type[1]);
							break;
						case "turretanglelimtYawMax":
							prefab_turret.gunInfo.turretanglelimtYawMax = parseFloat(type[1]);
							break;
						case "turretanglelimtYawmin":
							prefab_turret.gunInfo.turretanglelimtYawmin = parseFloat(type[1]);
							break;
						case "turretanglelimtPitchMax":
							prefab_turret.gunInfo.turretanglelimtPitchMax = parseFloat(type[1]);
							break;
						case "turretanglelimtPitchmin":
							prefab_turret.gunInfo.turretanglelimtPitchmin = parseFloat(type[1]);
							break;
						case "turretspeedY":
							prefab_turret.gunInfo.turretspeedY = (float) parseDouble(type[1]);
							break;
						case "turretspeedP":
							prefab_turret.gunInfo.turretspeedP = (float) parseDouble(type[1]);
							break;
						case "traverseSound":
							prefab_turret.traverseSound = type[1];
							break;
						case "traversesoundLV":
							prefab_turret.traversesoundLV = parseFloat(type[1]);
							break;
						case "traversesoundPitch":
							prefab_turret.traversesoundPitch = parseFloat(type[1]);
							break;
						case "hasReflexSight":
							prefab_turret.hasReflexSight = parseBoolean(type[1]);
							break;
						case "fireRists_First":
							prefab_turret.fireRists = new FireRist[parseInt(type[1])];
							break;
						case "fireRists":
							prefab_turret.fireRists[parseInt(type[1])] = new FireRist(parseFloat(type[2]),parseFloat(type[3]),parseFloat(type[4]),parseFloat(type[5]));
							break;
						case "userOnBarrell":
							prefab_turret.userOnBarrell = parseBoolean(type[1]);
							break;
						case "flashoffset":
							prefab_turret.flashoffset = parseDouble(type[1]);
							break;
						case "childFireBlank":
							prefab_turret.childFireBlank = parseInt(type[1]);
							break;
						case "useVehicleInventory":
							prefab_turret.useVehicleInventory = parseBoolean(type[1]);
							break;
						case "canReloadAirBone":
							prefab_turret.canReloadAirBone = parseBoolean(type[1]);
							break;
						case "needGunStack":
							prefab_turret.needGunStack = parseBoolean(type[1]);
							break;
						case "useGunSight":
							prefab_turret.useGunSight = parseBoolean(type[1]);
							break;
						case "useVehicleRadar":
							prefab_turret.useVehicleRadar = parseBoolean(type[1]);
							break;
						case "forceSyncTarget":
							prefab_turret.forceSyncTarget = parseBoolean(type[1]);
							break;
						case "syncMotherTarget":
							prefab_turret.syncMotherTarget = parseBoolean(type[1]);
							break;
						case "canAutoPickUpStack":
							prefab_turret.canAutoPickUpStack = parseBoolean(type[1]);
							break;
						case "replaceStackTime":
							prefab_turret.replaceStackTime = parseInt(type[1]);
							break;
						case "gunStackwhitelist":
							prefab_turret.gunStackwhitelist = new String[type.length-1];
						    {
						    	for(int i = 1;i < type.length;i++){
									prefab_turret.gunStackwhitelist[i-1] = type[i];
								}
						    }
							break;
						case "noStackRestriction":
							prefab_turret.gunStackwhitelist = new String[0];
							break;
						case "End":
							prefab_turretHashMap.put(name,prefab_turret);
							break;
						case "ModelHigh":
						case "ADSOffsetY":
							modelhigh = parseFloat(type[1]) - 1.8f;
							modelhighr = parseFloat(type[2]) - 1.8f;
							modelhighs = parseFloat(type[3]) - 1.8f;
							break;
						case "ModelWidthX":
						case "ADSOffsetX":
							modelwidthx = -0.693f + parseFloat(type[1]);
							modelwidthxr = -0.693f + parseFloat(type[2]);
							modelwidthxs = -0.693f + parseFloat(type[3]);
							break;
						case "ModelWidthZ":
						case "ADSOffsetZ":
							modelwidthz = parseFloat(type[1]) + 0.5f;
							modelwidthzr = parseFloat(type[2]) + 0.5f;
							modelwidthzs = parseFloat(type[3]) + 0.5f;
							break;

						case "SimpleADSOffsetX":
							modelwidthx = -parseFloat(type[1]);
							modelwidthxr = -parseFloat(type[2]);
							modelwidthxs = -parseFloat(type[3]);
							break;
						case "SimpleADSOffsetY":
							modelhigh = -parseFloat(type[1]);
							modelhighr = -parseFloat(type[2]);
							modelhighs = -parseFloat(type[3]);
							break;
						case "SimpleADSOffsetZ":
							modelwidthz = parseFloat(type[1]);
							modelwidthzr = parseFloat(type[2]);
							modelwidthzs = parseFloat(type[3]);
							break;
						case "setADSPointToINFO":
							prefab_turret.gunInfo.setmodelADSPosAndRotation_ForVehicle(modelwidthx,modelhigh,modelwidthz);
							prefab_turret.gunInfo.setADSoffsetRed_ForVehicle(modelwidthxr,modelhighr,modelwidthzr);
							prefab_turret.gunInfo.setADSoffsetScope_ForVehicle(modelwidthxs,modelhighs,modelwidthzs);
							break;
					}
					readFireInfo(prefab_turret.gunInfo,type);
					
				}
				br.close();  // ファイルを閉じる
			}
			else
			{
				System.out.println("Failed in Line " + line);
				System.out.println("" + current);
			}
		} catch (Exception e){
			System.out.println("Failed in Line " + line);
			System.out.println("" + current);
			e.printStackTrace();
		}
	}
	
	private static boolean checkBeforeReadfile(File file){
		if (file.exists()){
			if (file.isFile() && file.canRead()){
				return true;
			}
		}
		
		return false;
	}
}
