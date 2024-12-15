package handmadevehicle;

import cpw.mods.fml.common.registry.GameRegistry;
import handmadeguns.HMGGunMaker;
import handmadeguns.client.render.HMGGunParts;
import handmadeguns.client.render.HMGGunParts_Motion;
import handmadeguns.items.guns.HMGItem_Unified_Guns;
import handmadevehicle.Items.ItemVehicle;
import handmadevehicle.entity.prefab.*;
import handmadevehicle.render.HMVVehicleParts;
import net.minecraft.item.Item;

import javax.vecmath.Vector3d;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

import static handmadeguns.HandmadeGunsCore.tabshmg;
import static handmadevehicle.AddWeapon.prefab_turretHashMap;
import static handmadevehicle.HMVehicle.tabHMV;
import static java.lang.Boolean.parseBoolean;
import static java.lang.Float.parseFloat;
import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static java.lang.Math.*;
import static net.minecraft.util.MathHelper.wrapAngleTo180_float;

public class AddNewVehicle extends HMGGunMaker {
	//PAIN private static final HashMap<String,Prefab_Vehicle_Base> prefabBaseHashMap = new HashMap<String, Prefab_Vehicle_Base>();
	//PAIN private static Prefab_Vehicle_Base currentVehicleData;

	public static ArrayList<VehicleSpawnGachaOBJ> vehicleSpawnGachaOBJs_Guerrilla = new ArrayList<VehicleSpawnGachaOBJ>(){
		{
			add(new VehicleSpawnGachaOBJ("40"));
		}
	};
	public static int vehicleSpawnGachaOBJs_Guerrilla_sum = 40;
	public static ArrayList<VehicleSpawnGachaOBJ> vehicleSpawnGachaOBJs_Soldier = new ArrayList<VehicleSpawnGachaOBJ>(){
		{
			add(new VehicleSpawnGachaOBJ("20"));
		}
	};
	public static int vehicleSpawnGachaOBJs_Soldier_sum = 20;
	public void load( boolean isClient, File file){
		String str_Debug = null;
		int l = 0;
		try {
			String dataName = null;
			//PAIN 		currentVehicleData = new Prefab_Vehicle_Base();
			VehicleType vehicleType = null;
			Prefab_AttachedWeapon current = null;
			int turretId_current = 0;
			int rootTurretID_current = -1;
			String tabname = null;
			ArrayList<HMGGunParts> partslist = new ArrayList<HMGGunParts>();
			//File file = new File(configfile,"hmg_handmadeguns.txt");
			if (checkBeforeReadfile(file))
			{

				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file),"Shift-JIS"));
				String str = "";
				String str_line = "";
				while((str_line = br.readLine()) != null) {
					l++;
					str = str.concat(str_line);
					//System.out.println(str);
					if(str.contains(";")) {
						str = str.concat(" ");
						str_Debug = str;
						String[] str_temp = str.split(";");
						for(String a_calm:str_temp) {
							str = a_calm;
							String[] type = a_calm.split(",");
							for (int i = 0; i < type.length; i++) {
								type[i] = type[i].trim();
							}
							switch (type[0]) {
								case "Name":
									dataName = type[1];
					//PAIN 				if (prefabBaseHashMap.containsKey(dataName)) currentVehicleData = prefabBaseHashMap.get(dataName);
					//PAIN 				currentVehicleData.weaponSlotNum = 0;
					//PAIN 				currentVehicleData.cargoSlotNum = 0;
									break;
								case "modelName":
									System.out.println(type[1]);
									//PAIN 					currentVehicleData.modelName = type[1];
									break;
								case "modelName_texture":
									//PAIN 				currentVehicleData.modelName_texture = type[1];
									break;
								case "scale":
									//PAIN 				currentVehicleData.scale = parseFloat(type[1]);
									break;
								case "health":
									//PAIN 						currentVehicleData.maxhealth = parseFloat(type[1]);
									break;
								case "soundname":
					//PAIN 				currentVehicleData.SoundName = type[1];
					//PAIN 				currentVehicleData.AFSoundName = type[1] + "AF";
									break;
								case "IdleSoundName":
									//PAIN 				currentVehicleData.IdleSoundName = type[1];
									break;
								case "soundpitch":
									//PAIN 				currentVehicleData.soundpitch = parseFloat(type[1]);
									break;
								case "throttle_Max":
									//PAIN 				currentVehicleData.throttle_Max = parseFloat(type[1]);
									break;
								case "throttle_min":
									//PAIN 				currentVehicleData.throttle_min = parseFloat(type[1]);
									break;
								case "throttle_speed":
									//PAIN 				currentVehicleData.throttle_speed = parseFloat(type[1]);
									break;
								case "rudderSpeed":
									//PAIN 				currentVehicleData.rudderSpeed = parseFloat(type[1]);
									break;
								case "draft":
									//PAIN 				currentVehicleData.draft = parseFloat(type[1]);
									break;
								case "molded_depth":
									//PAIN 				currentVehicleData.molded_depth = parseFloat(type[1]);
									break;
								case "floatOnWater":
									//PAIN 				currentVehicleData.floatOnWater = parseBoolean(type[1]);
									break;
								case "splashsound":
									//PAIN 				currentVehicleData.splashsound = type[1];
									break;
								case "sightTex":
									//PAIN 				currentVehicleData.sightTex[0] = type[1];
									break;
								case "sightTex_toSeat":
									//PAIN 				currentVehicleData.sightTex[parseInt(type[1])] = type[2];
									break;
								case "ParentWeapons_NUM":
									//PAIN 				currentVehicleData.prefab_attachedWeapons = new Prefab_AttachedWeapon[parseInt(type[1])];
									break;
								case "AllWeapons_NUM":
									//PAIN 				currentVehicleData.prefab_attachedWeapons_all = new Prefab_AttachedWeapon[parseInt(type[1])];
									break;
								case "addParentWeapon":
					//PAIN 				currentVehicleData.prefab_attachedWeapons_all[turretId_current] = currentVehicleData.prefab_attachedWeapons[parseInt(type[1])] = new Prefab_AttachedWeapon();
					//PAIN 				current = currentVehicleData.prefab_attachedWeapons[parseInt(type[1])];
									turretId_current++;
									rootTurretID_current++;
									if(prefab_turretHashMap.containsKey(type[2]))current.prefab_turret = prefab_turretHashMap.get(type[2]);
									else {
										Item check = GameRegistry.findItem("HandmadeGuns",type[2]);
										if(check instanceof HMGItem_Unified_Guns){
											current.prefab_turret = new Prefab_Turret(((HMGItem_Unified_Guns) check));
										}
									}
									if(current.prefab_turret.needGunStack){
				//PAIN 						current.linkedGunStackID = currentVehicleData.weaponSlotNum++;
				//PAIN 						currentVehicleData.weaponSlot_linkedTurret_StackWhiteList.add(current.prefab_turret.gunStackwhitelist);
				//PAIN 						currentVehicleData.weaponSlot_linkedTurretID.add(turretId_current-1);
									}


									current.turretsPos = new Vector3d(parseDouble(type[3]), parseDouble(type[4]), parseDouble(type[5]));
									current.prefab_Childturrets = new Prefab_AttachedWeapon[parseInt(type[6])];
									if(type.length >= 8)current.prefab_ChildOnBarrel = new Prefab_AttachedWeapon[parseInt(type[7])];
									break;
								case "setInitialRotation":
									current.initialRotationYaw = parseFloat(type[1]);
									current.initialRotationPitch = parseFloat(type[2]);
									break;
								case "addChildWeapon":
									//PAIN 						currentVehicleData.prefab_attachedWeapons_all[turretId_current] = current.prefab_Childturrets[parseInt(type[1])] = new Prefab_AttachedWeapon();
									current.prefab_Childturrets[parseInt(type[1])].motherTurret = current;
									current.prefab_Childturrets[parseInt(type[1])].motherTurretID = rootTurretID_current;
									current = current.prefab_Childturrets[parseInt(type[1])];
									rootTurretID_current = turretId_current;
									turretId_current++;
									if(prefab_turretHashMap.containsKey(type[2]))current.prefab_turret = prefab_turretHashMap.get(type[2]);
									else {
										Item check = GameRegistry.findItem("HandmadeGuns",type[2]);
										if(check instanceof HMGItem_Unified_Guns){
											current.prefab_turret = new Prefab_Turret(((HMGItem_Unified_Guns) check));
											current.prefab_turret.syncTurretAngle = false;
											current.prefab_turret.linked_MotherTrigger = false;
											current.prefab_turret.useGunSight = true;
										}
									}
									if(current.prefab_turret.needGunStack){
							//PAIN 			current.linkedGunStackID = currentVehicleData.weaponSlotNum++;
							//PAIN 			currentVehicleData.weaponSlot_linkedTurret_StackWhiteList.add(current.prefab_turret.gunStackwhitelist);
							//PAIN 			currentVehicleData.weaponSlot_linkedTurretID.add(turretId_current-1);
									}

									current.turretsPos = new Vector3d(parseDouble(type[3]), parseDouble(type[4]), parseDouble(type[5]));
									current.prefab_Childturrets = new Prefab_AttachedWeapon[parseInt(type[6])];
									if(type.length >= 8)current.prefab_ChildOnBarrel = new Prefab_AttachedWeapon[parseInt(type[7])];
									break;
								case "addBarrelChildWeapon":
//									System.out.println("" + current.prefab_turret.turretName);
//									System.out.println("" + rootTurretID_current);
									//PAIN 			currentVehicleData.prefab_attachedWeapons_all[turretId_current] = current.prefab_ChildOnBarrel[parseInt(type[1])] = new Prefab_AttachedWeapon();
									current.prefab_ChildOnBarrel[parseInt(type[1])].motherTurret = current;
									current.prefab_ChildOnBarrel[parseInt(type[1])].motherTurretID = rootTurretID_current;
									current = current.prefab_ChildOnBarrel[parseInt(type[1])];
									current.onBarrel = true;
									rootTurretID_current = turretId_current;
									turretId_current++;
									if(prefab_turretHashMap.containsKey(type[2]))current.prefab_turret = prefab_turretHashMap.get(type[2]);
									else {
										Item check = GameRegistry.findItem("HandmadeGuns",type[2]);
										if(check instanceof HMGItem_Unified_Guns){
											current.prefab_turret = new Prefab_Turret(((HMGItem_Unified_Guns) check));
											current.prefab_turret.syncTurretAngle = false;
											current.prefab_turret.linked_MotherTrigger = false;
											current.prefab_turret.useGunSight = true;
										}
									}
									if(current.prefab_turret.needGunStack){
								//PAIN 		current.linkedGunStackID = currentVehicleData.weaponSlotNum++;
								//PAIN 		currentVehicleData.weaponSlot_linkedTurret_StackWhiteList.add(current.prefab_turret.gunStackwhitelist);
								//PAIN 		currentVehicleData.weaponSlot_linkedTurretID.add(turretId_current-1);
									}

									current.turretsPos = new Vector3d(parseDouble(type[3]), parseDouble(type[4]), parseDouble(type[5]));
									current.prefab_Childturrets = new Prefab_AttachedWeapon[parseInt(type[6])];
									if(type.length >= 8)current.prefab_ChildOnBarrel = new Prefab_AttachedWeapon[parseInt(type[7])];
									break;
								case "Set_CurrentTurret_to_Mother":
									rootTurretID_current = current.motherTurretID;
									current = current.motherTurret;
									break;

								case "SetUpSeat1_NUM":
			//PAIN 						currentVehicleData.prefab_seats = new Prefab_Seat[parseInt(type[1])];
			//PAIN 						currentVehicleData.prefab_seats_zoom = new Prefab_Seat[parseInt(type[1])];
			//PAIN 						currentVehicleData.sightTex = new String[parseInt(type[1])];
									break;
								case "SetUpSeat2_AddSeat_Normal":
				//PAIN 					if(type.length == 10)currentVehicleData.prefab_seats_zoom[parseInt(type[1])] = currentVehicleData.prefab_seats[parseInt(type[1])] =
				//PAIN 							new Prefab_Seat(new double[]{parseDouble(type[2]), parseDouble(type[3]), parseDouble(type[4])}, parseBoolean(type[5]),parseInt(type[8]), parseInt(type[9]));
				//PAIN 					else if(type.length == 11)currentVehicleData.prefab_seats_zoom[parseInt(type[1])] = currentVehicleData.prefab_seats[parseInt(type[1])] =
				//PAIN 							new Prefab_Seat(new double[]{parseDouble(type[2]), parseDouble(type[3]), parseDouble(type[4])}, parseBoolean(type[5]), parseInt(type[9]), parseInt(type[10]));
				//PAIN 					else if(type.length == 8)currentVehicleData.prefab_seats_zoom[parseInt(type[1])] = currentVehicleData.prefab_seats[parseInt(type[1])] =
				//PAIN 							new Prefab_Seat(new double[]{parseDouble(type[2]), parseDouble(type[3]), parseDouble(type[4])}, parseBoolean(type[5]), parseInt(type[6]), parseInt(type[7]));
									break;
								case "SetUpSeat3_AddSeat_Zoom":
									//PAIN if(type.length == 10)
			//PAIN 							currentVehicleData.prefab_seats_zoom[parseInt(type[1])] =
			//PAIN 									new Prefab_Seat(new double[]{parseDouble(type[2]), parseDouble(type[3]), parseDouble(type[4])}, parseBoolean(type[5]), parseInt(type[8]), parseInt(type[9]));
			//PAIN 						else if(type.length == 11)currentVehicleData.prefab_seats_zoom[parseInt(type[1])] =
			//PAIN 								new Prefab_Seat(new double[]{parseDouble(type[2]), parseDouble(type[3]), parseDouble(type[4])}, parseBoolean(type[5]),parseInt(type[9]), parseInt(type[10]));
			//PAIN 						else if(type.length == 8)currentVehicleData.prefab_seats_zoom[parseInt(type[1])] =
			//PAIN 								new Prefab_Seat(new double[]{parseDouble(type[2]), parseDouble(type[3]), parseDouble(type[4])}, parseBoolean(type[5]), parseInt(type[6]), parseInt(type[7]));

									break;
								case "SetUpSeat4_AddSeat_AdditionalTurret": {
									//PAIN 	currentVehicleData.prefab_seats[parseInt(type[1])].mainid = new int[type.length - 2];
									int cnt = -2;
									for (String column : type) {
										if (cnt < 0) {
											cnt++;
											continue;
										}
										//PAIN 			currentVehicleData.prefab_seats[parseInt(type[1])].mainid[cnt] = parseInt(column);
										cnt++;
									}
								}
								break;
								case "back":
									current = current.motherTurret;
									break;
								case "Tabname":
									tabname = type[1];
									break;
								case "SpawnByMob_Guerrilla": {
									VehicleSpawnGachaOBJ vehicleSpawnGachaOBJ = new VehicleSpawnGachaOBJ(type[1],type[2]);
									vehicleSpawnGachaOBJs_Guerrilla.add(vehicleSpawnGachaOBJ);
									vehicleSpawnGachaOBJs_Guerrilla_sum += vehicleSpawnGachaOBJ.rate;
								}
									break;
								case "SpawnByMob_Soldier": {
									VehicleSpawnGachaOBJ vehicleSpawnGachaOBJ = new VehicleSpawnGachaOBJ(type[1],type[2]);
									vehicleSpawnGachaOBJs_Soldier.add(vehicleSpawnGachaOBJ);
									vehicleSpawnGachaOBJs_Soldier_sum += vehicleSpawnGachaOBJ.rate;
								}
									break;
								case "End":
									if (isClient) {
										//PAIN 							currentVehicleData.setModel();
										//PAIN 							if (!partslist.isEmpty()) currentVehicleData.partslist = partslist;
									}
									//PAIN 					prefabBaseHashMap.put(dataName, currentVehicleData);
									Item check = GameRegistry.findItem("HMVehicle",dataName);
									if(check == null){
										Item itemVehicle;
										GameRegistry.registerItem(itemVehicle = new ItemVehicle(dataName).setUnlocalizedName(dataName), dataName);
										if(tabname == null) itemVehicle.setCreativeTab(tabHMV);
										else if(tabshmg.containsKey(tabname)){
											itemVehicle.setCreativeTab(tabshmg.get(tabname));
										}
										itemVehicle.setTextureName("handmadevehicle:"+dataName);
									}
									break;
							}
							//PAIN 					currentVehicleData.readSettings(type);
							if (isClient) readParts_vehicle(type, partslist);
						}
						if(str.startsWith("//")){
							str = "";
						}
					}
				}
				br.close();  // ファイルを閉じる
			}
			else
			{

			}
		} catch (Exception e){
			System.out.println("Failed in Line " + l);
			System.out.println("" + str_Debug);
			e.printStackTrace();
		}
	}

	//PAIN public static Prefab_Vehicle_Base seachInfo(String typeName){
	//PAIN 	if(prefabBaseHashMap.containsKey(typeName))
	//PAIN 		return prefabBaseHashMap.get(typeName);
	//PAIN 	return null;
	//PAIN }


	public void readParts_vehicle(String[] type,ArrayList<HMGGunParts> partslist){
		readerCnt = 0;
		boolean noLoop = false;
		readParts(type,partslist);
		switch (type[readerCnt++]) {
			case "AddPartsRenderAsTrackInf":
				((HMVVehicleParts)currentParts).AddRenderinfTrack(Float.parseFloat(type[readerCnt++]), Float.parseFloat(type[readerCnt++]), Float.parseFloat(type[readerCnt++]), Float.parseFloat(type[readerCnt++]), Float.parseFloat(type[readerCnt++]), Float.parseFloat(type[readerCnt++]));
				break;
			case "AddTrackPos":
				((HMVVehicleParts)currentParts).AddTrackPositions(type);
				break;
			case "AddIdlePositions":
				((HMVVehicleParts)currentParts).AddIdlePositions(type);
				break;
			case "IsTrack":
				((HMVVehicleParts)currentParts).setIsTrack(Boolean.parseBoolean(type[readerCnt++]), parseInt(type[readerCnt++]));
				break;
			case "setIsIdleAnim":
				((HMVVehicleParts)currentParts).setIsIdleAnim(Boolean.parseBoolean(type[readerCnt++]), parseInt(type[readerCnt++]));
				break;
			case "IsPera":
				((HMVVehicleParts)currentParts).setIsPera(Boolean.parseBoolean(type[readerCnt++]));
				break;
			case "IsCloningTrack":
				((HMVVehicleParts)currentParts).setIsTrack_Cloning(Boolean.parseBoolean(type[readerCnt++]), parseInt(type[readerCnt++]));
				break;
			case "IsCloningIdleAnim":
				((HMVVehicleParts)currentParts).setIsIdleAnim_Cloning(Boolean.parseBoolean(type[readerCnt++]), parseInt(type[readerCnt++]));
				break;
			case "TurretParts":
				((HMVVehicleParts)currentParts).isTurretParts = true;
				((HMVVehicleParts)currentParts).linkedTurretID = parseInt(type[readerCnt++]);
		//PAIN 		Vector3d weaponPos = currentVehicleData.prefab_attachedWeapons_all[((HMVVehicleParts)currentParts).linkedTurretID].turretsPos;
		//PAIN 		currentParts.AddRenderinfDefoffset((float) weaponPos.x / currentVehicleData.scale,
		//PAIN 				(float) weaponPos.y / currentVehicleData.scale,
		//PAIN 				(float) -weaponPos.z / currentVehicleData.scale,
		//PAIN 				0, 0, 0);
				break;
			case "AddCrawlerTrack_noLoop":
				noLoop = true;
			case "AddCrawlerTrack=false":
			case "AddCrawlerTrack=true": {
				float track_pieceLength = parseFloat(type[readerCnt++]);
				readerCnt++;//履帯X位置は取得不要
				//なんで必要だったんだろう？
				float[] posesY = new float[type.length - 3];
				float[] posesZ = new float[type.length - 3];
				float[] betweenRot = new float[type.length - 3];
				for (int id = 0; id < type.length - 3; id++) {
					String temp = type[readerCnt++];
					String[] posY_Z = temp.split("/");
					posesY[id] = parseFloat(posY_Z[0]);
					posesZ[id] = parseFloat(posY_Z[1]);
//					if(id != 0) {
//						float lengthY = currentPosY - prevPosY;
//						float lengthZ = currentPosZ - prevPosZ;
//
//						float trackRot = 180 - (float)toDegrees(atan2(lengthY,lengthZ));
//						if(rotationPosInfo != null){
//							rotationPosInfo.endrotationX = trackRot;
//							rotationPosInfo.setup();
//							((HMVVehicleParts)currentParts).trackPositions.addmotion(rotationPosInfo);
//						}
//						if(id == 1)firstTrackRot = trackRot;
//						float currentOffsetLength = (float) sqrt(lengthY * lengthY +
//								lengthZ * lengthZ);
//						float currentPieceLength = currentOffsetLength/track_pieceLength;
//						HMGGunParts_Motion newPosInfo = new HMGGunParts_Motion();
//
//						System.out.println("debug Start" + prevTrackNum);
//						System.out.println("debug End" + (prevTrackNum + currentPieceLength));
//
//						newPosInfo.startflame = prevTrackNum;
//						newPosInfo.endflame = prevTrackNum + currentPieceLength - 1;
//
//						newPosInfo.startposY = prevPosY;
//						newPosInfo.startposZ = prevPosZ;
//						newPosInfo.startrotationX = trackRot;
//
//
//						newPosInfo.endposY = currentPosY - (lengthY / currentPieceLength);
//						newPosInfo.endposZ = currentPosZ - (lengthZ / currentPieceLength);
//						newPosInfo.endrotationX = trackRot;
//						newPosInfo.setup();
//
//
//						((HMVVehicleParts)currentParts).trackPositions.addmotion(newPosInfo);
//
//
//						rotationPosInfo = new HMGGunParts_Motion();
//						rotationPosInfo.startflame = prevTrackNum + currentPieceLength - 1;
//						rotationPosInfo.startposY = currentPosY - (lengthY / currentPieceLength);
//						rotationPosInfo.startposZ = currentPosZ - (lengthZ / currentPieceLength);
//
//						rotationPosInfo.endflame = prevTrackNum + currentPieceLength;
//						rotationPosInfo.endposY = currentPosY;
//						rotationPosInfo.endposZ = currentPosZ;
//
//						rotationPosInfo.startrotationX = trackRot;
//
//						prevTrackNum = prevTrackNum + currentPieceLength;
//
//					}
//					prevPosY = currentPosY;
//					prevPosZ = currentPosZ;
				}


				for (int id = 0; id < type.length - 4; id++) {
					float lengthY = posesY[id + 1] - posesY[id];
					float lengthZ = posesZ[id + 1] - posesZ[id];
					betweenRot[id] = wrapAngleTo180_float(180 - (float) toDegrees(atan2(lengthY, lengthZ)));
//					System.out.println("------------------------------------------------------------------------------");
//					System.out.println("debug_track lengthY     " + id + " : " + lengthY);
//					System.out.println("debug_track lengthZ     " + id + " : " + lengthZ);
//					System.out.println("debug_track rot         " + id + " : " + betweenRot[id]);
				}

				{
					float lengthY = posesY[0] - posesY[type.length - 4];
					float lengthZ = posesZ[0] - posesZ[type.length - 4];
					betweenRot[type.length - 4] = wrapAngleTo180_float(180 - (float) toDegrees(atan2(lengthY, lengthZ)));

//					System.out.println("------------------------------------------------------------------------------");
//					System.out.println("debug_track lengthY     " + (type.length-4) + " : " + lengthY);
//					System.out.println("debug_track lengthZ     " + (type.length-4) + " : " + lengthZ);
//					System.out.println("debug_track rot         " + (type.length-4) + " : " + betweenRot[type.length-4]);
				}


				float flameCounter = 0;
				//こりゃ多分回転させるのははじめじゃなく後ろの方が良いな


				// あと回転がところどころ逆になるのを修正すべき

				for (int id = 0; id < type.length - 3; id++) {

					int nextCnt = id + 1;
					if (nextCnt >= type.length - 3) {
						if(noLoop)continue;
						nextCnt = 0;
					}
					if(id == type.length - 5 && noLoop) continue;

					int prevCnt = id - 1;
					if (id == 0) {
						if(noLoop)continue;
						prevCnt = type.length - 4;
					}


					int nextNextCnt = id + 2;
					if (nextNextCnt >= type.length - 3) {
						nextNextCnt -= type.length - 3;
					}
					float prevLengthY = posesY[id] - posesY[prevCnt];
					float prevLengthZ = posesZ[id] - posesZ[prevCnt];
					float lengthY = posesY[nextCnt] - posesY[id];
					float lengthZ = posesZ[nextCnt] - posesZ[id];
					float nextLengthY = posesY[nextNextCnt] - posesY[nextCnt];
					float nextLengthZ = posesZ[nextNextCnt] - posesZ[nextCnt];

					float length = (float) sqrt(lengthY * lengthY + lengthZ * lengthZ);


					float endRotationX = betweenRot[id] + wrapAngleTo180_float(betweenRot[nextCnt] - betweenRot[id]) / 2;


					HMGGunParts_Motion start = new HMGGunParts_Motion();
					start.startflame = flameCounter;
					start.startposY = posesY[id];
					start.startposZ = posesZ[id];
					start.startrotationX = betweenRot[prevCnt] + wrapAngleTo180_float(betweenRot[id] - betweenRot[prevCnt]) / 2;

					float trackCnt = (length / track_pieceLength);

					if (length > track_pieceLength) {
						flameCounter += 0.5f;
						start.endflame = flameCounter;
						start.endposY = posesY[id] + lengthY / length * track_pieceLength / 2;
						start.endposZ = posesZ[id] + lengthZ / length * track_pieceLength / 2;
						start.endrotationX = betweenRot[id];

						((HMVVehicleParts) currentParts).trackPositions.addmotion(start);

						HMGGunParts_Motion middle = new HMGGunParts_Motion();

						middle.startflame = start.endflame;
						middle.startposY = start.endposY;
						middle.startposZ = start.endposZ;
						middle.startrotationX = start.endrotationX;

						flameCounter += trackCnt - 1f;
						middle.endflame = flameCounter;
						middle.endposY = posesY[nextCnt] - lengthY / length * track_pieceLength / 2;
						middle.endposZ = posesZ[nextCnt] - lengthZ / length * track_pieceLength / 2;
						middle.endrotationX = middle.startrotationX;

						((HMVVehicleParts) currentParts).trackPositions.addmotion(middle);


						HMGGunParts_Motion end = new HMGGunParts_Motion();

						end.startflame = middle.endflame;
						end.startposY = middle.endposY;
						end.startposZ = middle.endposZ;
						end.startrotationX = middle.endrotationX;

						flameCounter += 0.5f;
						end.endflame = flameCounter;
						end.endposY = posesY[nextCnt];
						end.endposZ = posesZ[nextCnt];
						end.endrotationX = endRotationX;

						((HMVVehicleParts) currentParts).trackPositions.addmotion(end);
					} else {
						start.endposY = posesY[nextCnt];
						start.endposZ = posesZ[nextCnt];
						start.endrotationX = endRotationX;
						flameCounter += trackCnt;

						start.endflame = flameCounter;
						((HMVVehicleParts) currentParts).trackPositions.addmotion(start);
					}
				}

				float flameCounter_float = flameCounter;
				flameCounter = (int) flameCounter;
				if (flameCounter < flameCounter_float) flameCounter++;

				float prevEndFrame = 0;

				for(HMGGunParts_Motion aKey : ((HMVVehicleParts) currentParts).trackPositions.motions){
					aKey.startflame = prevEndFrame;
					prevEndFrame = aKey.endflame *= flameCounter/flameCounter_float;
					aKey.setup2();
				}

				((HMVVehicleParts) currentParts).isbelt = true;
				((HMVVehicleParts) currentParts).setIsTrack_Cloning(true, (int) flameCounter);
				((HMVVehicleParts) currentParts).trackPieceCount = (int)flameCounter;
				if(((HMVVehicleParts) currentParts).trackPieceCount < flameCounter)((HMVVehicleParts) currentParts).trackPieceCount++;

			}
			break;
			case "AddCrawlerTrack_Offset":
				((HMVVehicleParts)currentParts).trackAnimOffset = Float.parseFloat(type[readerCnt++]);
			break;
			case "AddCrawlerTrack_trackAnimSpeed":
				((HMVVehicleParts)currentParts).trackAnimSpeed = Float.parseFloat(type[readerCnt++]);
			break;
			case "isTurret_linkedGunMount":
				((HMVVehicleParts)currentParts).isTurret_linkedGunMount = true;
				break;
			case "AddSomeMotion":
				((HMVVehicleParts)currentParts).AddSomethingMotionKey(type);
				break;
			case "AddSomeInfo":
				((HMVVehicleParts)currentParts).AddRenderinfSomething(Float.parseFloat(type[readerCnt++]), Float.parseFloat(type[readerCnt++]), Float.parseFloat(type[readerCnt++]), Float.parseFloat(type[readerCnt++]), Float.parseFloat(type[readerCnt++]), Float.parseFloat(type[readerCnt++]),parseInt(type[readerCnt]));
				break;
		}
	}
	public HMGGunParts createGunPart(String[] strings){
		return new HMVVehicleParts(strings[1]);
	}
	public HMGGunParts createGunPart(String[] strings,int motherID,HMGGunParts mother){
		return new HMVVehicleParts(strings[1],motherID,mother);
	}
}
