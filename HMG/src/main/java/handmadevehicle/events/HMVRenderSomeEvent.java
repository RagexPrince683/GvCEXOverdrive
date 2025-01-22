package handmadevehicle.events;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import handmadeguns.HandmadeGunsCore;
//import handmadeguns.Util.EntityLinkedPos_Motion;
import handmadeguns.event.RenderTickSmoothing;
import handmadeguns.items.GunInfo;
import handmadevehicle.entity.EntityCameraDummy;
import handmadevehicle.entity.EntityVehicle;
import handmadevehicle.entity.parts.IVehicle;
import handmadevehicle.entity.parts.SeatObject;
import handmadevehicle.entity.parts.logics.BaseLogic;
import handmadevehicle.entity.parts.turrets.TurretObj;
import handmadevehicle.entity.parts.turrets.WeaponCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import javax.script.ScriptException;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import static handmadeguns.HandmadeGunsCore.HMG_proxy;
import static handmadeguns.event.HMGEventZoom.*;
import static handmadevehicle.HMVehicle.HMV_Proxy;
import static handmadevehicle.Utils.*;
import static handmadevehicle.render.RenderVehicle.partialTicks;
import static java.lang.Math.*;
import static net.minecraft.util.MathHelper.wrapAngleTo180_float;
import static org.lwjgl.opengl.ARBFramebufferObject.*;
import static org.lwjgl.opengl.ARBFramebufferObject.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.EXTFramebufferObject.*;
import static org.lwjgl.opengl.EXTFramebufferObject.GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME_EXT;
import static org.lwjgl.opengl.GL11.*;

public class HMVRenderSomeEvent {

	public static int playerSeatID;
	//public static ArrayList<EntityLinkedPos_Motion> missile_Pos_Motion;
	//public static ArrayList<EntityLinkedPos_Motion>  target_Pos_Motion = new ArrayList<EntityLinkedPos_Motion>();
	static boolean needrest = true;
	static boolean needrest_zoom = true;
	private double zLevel = 0;
	private static final IModelCustom attitude_indicator = AdvancedModelLoader.loadModel(new ResourceLocation("handmadevehicle:textures/model/Attitude indicator.mqo"));
	private static final ResourceLocation attitude_indicator_texture = new ResourceLocation("handmadevehicle:textures/model/Attitude indicator.png");

	public static boolean receivedTargetData;

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void renderridding(RenderPlayerEvent.Pre event)
	{
		if(event.entityPlayer==HMV_Proxy.getEntityPlayerInstance() && event.entityPlayer.ridingEntity instanceof IVehicle && FMLClientHandler.instance().getClient().gameSettings.thirdPersonView == 0)event.setCanceled(true);
	}
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void renderfov(FOVUpdateEvent event)
	{
		Minecraft minecraft = FMLClientHandler.instance().getClient();
		EntityPlayer entityplayer = minecraft.thePlayer;
		if (entityplayer.ridingEntity instanceof IVehicle){
		}else {
		}
	}
//	@SideOnly(Side.CLIENT)
//    @SubscribeEvent
//	  public void renderoffset(EntityViewRenderEvent.RenderFogEvent event)
//	  {
//		Minecraft minecraft = FMLClientHandler.instance().getClient();
//		World world = FMLClientHandler.instance().getWorldClient();
//		EntityLivingBase entityLiving = event.entity;
//		EntityPlayer entityplayer = minecraft.thePlayer;
//		ItemStack itemstack = ((EntityPlayer) (entityplayer)).getCurrentEquippedItem();
//		if (entityplayer.ridingEntity instanceof EntityPMCBase && entityplayer.ridingEntity != null) {//1
//			EntityPMCBase balaam = (EntityPMCBase) entityplayer.ridingEntity;
//			if(minecraft.gameSettings.thirdPersonView == 1){
//				float rotep = entityplayer.rotationPitch * (2 * (float) Math.PI / 360);
//				float x = (float) (2 * Math.cos(rotep));
//				float y = (float) (2 * Math.sin(rotep)) * balaam.overlayhight_3;
//
//				float ix2 = 0;
//				float iz2 = 0;
//				float f12 = entityplayer.rotationYawHead * (2 * (float) Math.PI / 360);
//				ix2 += (float) (MathHelper.sin(f12) * balaam.overlaywidth_3);
//				iz2 -= (float) (MathHelper.cos(f12) * balaam.overlaywidth_3);
//
//				//float ix3 = 0;
//				//float iz3 = 0;
//				//ix3 += (float) (MathHelper.sin(f12) * balaam.overlaywidth_3*x);
//				//iz3 -= (float) (MathHelper.cos(f12) * balaam.overlaywidth_3*x);
//				{
//				GL11.glTranslatef(-ix2, -balaam.overlayhight_3-y, -iz2);
//				}
//			}else if(minecraft.gameSettings.thirdPersonView == 0){
//				GL11.glTranslatef(0, -balaam.overlayhight, 0);
//			}
//		}//1
//	  }

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void renderPlayer(RenderPlayerEvent.Pre event){
		Minecraft minecraft = FMLClientHandler.instance().getClient();
		EntityPlayer entityplayer = minecraft.thePlayer;
		if(event.entityLiving != entityplayer)return;




	}
	public static double renderTickTime;

	public static float prevPlayerYaw;
	public static float prevPlayerPit;

	public static boolean trackFlag = false;
	public static float trackedPlayerYaw;
	public static float trackedPlayerPit;

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void renderTick(TickEvent.RenderTickEvent event)
	{
		Minecraft minecraft = FMLClientHandler.instance().getClient();
		EntityPlayer entityplayer = minecraft.thePlayer;
		renderTickTime = event.renderTickTime;
		if(entityplayer != null)switch(event.phase)
		{
			case START :
				if(entityplayer.ridingEntity instanceof IVehicle){
					ObfuscationReflectionHelper.setPrivateValue(EntityRenderer.class, minecraft.entityRenderer, 0, "camRoll", "field_78495_O");
					ObfuscationReflectionHelper.setPrivateValue(EntityRenderer.class, minecraft.entityRenderer, 0, "prevCamRoll", "field_78505_P");
					ObfuscationReflectionHelper.setPrivateValue(EntityRenderer.class, minecraft.entityRenderer, 4, "thirdPersonDistance", "E", "field_78490_B");
					ObfuscationReflectionHelper.setPrivateValue(EntityRenderer.class, minecraft.entityRenderer,
							1.0d, "cameraZoom", "field_78503_V");
					needrest = true;
					BaseLogic logic = ((IVehicle) entityplayer.ridingEntity).getBaseLogic();
					Entity vehicleBody = entityplayer.ridingEntity;
					{
						Vector3d nowPos = new Vector3d();
						nowPos.interpolate(
								new Vector3d(
										vehicleBody.prevPosX,
										vehicleBody.prevPosY,
										vehicleBody.prevPosZ),
								new Vector3d(
										vehicleBody.posX,
										vehicleBody.posY,
										vehicleBody.posZ),
								renderTickTime);

						Quat4d currentVehicleQuat = new Quat4d(0,0,0,1);
						if(getQuat4DLength(logic.prevbodyRot)>0 && getQuat4DLength(logic.bodyRot)>0)
							currentVehicleQuat.interpolate(logic.prevbodyRot,logic.bodyRot, (double) renderTickTime);
//									if (abs(logic.pitchladder) > 0.001) {
//										Vector3d axisx = transformVecByQuat(new Vector3d(1, 0, 0), currentVehicleQuat);
//										AxisAngle4d axisxangled = new AxisAngle4d(axisx, toRadians(-logic.pitchladder * renderTickTime / 4));
//										currentVehicleQuat = quatRotateAxis(currentVehicleQuat, axisxangled);
//									}
//									if (abs(logic.yawladder) > 0.001) {
//										Vector3d axisy = transformVecByQuat(new Vector3d(0, 1, 0), currentVehicleQuat);
//										AxisAngle4d axisyangled = new AxisAngle4d(axisy, toRadians(logic.yawladder * renderTickTime / 4));
//										currentVehicleQuat = quatRotateAxis(currentVehicleQuat, axisyangled);
//									}
//									if (abs(logic.rollladder) > 0.001) {
//										Vector3d axisz = transformVecByQuat(new Vector3d(0, 0, 1), currentVehicleQuat);
//										AxisAngle4d axiszangled = new AxisAngle4d(axisz, toRadians(logic.rollladder * renderTickTime / 4));
//										currentVehicleQuat = quatRotateAxis(currentVehicleQuat, axiszangled);
//									}

						double[] xyz = eulerfromQuat((currentVehicleQuat));
						xyz[0] = toDegrees(xyz[0]);
						xyz[1] = toDegrees(xyz[1]);
						xyz[2] = toDegrees(xyz[2]);

						if(!logic.seatObjects[playerSeatID].prefab_seat.stabilizedView) {
							//if (!logic.ispilot(entityplayer) || (!logic.mouseStickMode) || logic.prefab_vehicle.T_Land_F_Plane) {
							//	float f1 = HMG_proxy.getMCInstance().gameSettings.mouseSensitivity * 0.6F + 0.2F;
							//	float f2 = f1 * f1 * f1 * 8.0F;
							//	float f3 = (float) HMG_proxy.getMCInstance().mouseHelper.deltaX * f2;
							//	float f4 = (float) HMG_proxy.getMCInstance().mouseHelper.deltaY * f2;
							//	logic.cameraYaw += f3 * 0.15D;
							//	logic.cameraPitch += f4 * 0.15D;
							//	if (logic.cameraPitch > 90) logic.cameraPitch = 90;
							//	if (logic.cameraPitch < -90) logic.cameraPitch = -90;
							//}

							Quat4d Headrot = new Quat4d(0,0,0,1);
							Headrot = quatRotateAxis(Headrot, new AxisAngle4d(unitX, toRadians(logic.cameraPitch) / 2));
							Headrot = quatRotateAxis(Headrot, new AxisAngle4d(unitY, toRadians(logic.cameraYaw) / 2));
							logic.camerarot.set(Headrot);
							logic.camerarot_current.set(logic.camerarot);

//							System.out.println("y" + xyz[0] + " , x" + xyz[1] + " , z" + xyz[2] + " , renderTickTime" + renderTickTime);

//								Vector3d bodyvector = transformVecByQuat(new Vector3d(0, 0, 1), currentVehicleQuat);
//								Vector3d tailwingvector = transformVecByQuat(new Vector3d(0, 1, 0), currentVehicleQuat);
//								Vector3d mainwingvector = transformVecByQuat(new Vector3d(1, 0, 0), currentVehicleQuat);
//
//								transformVecforMinecraft(tailwingvector);
//								transformVecforMinecraft(bodyvector);
//								transformVecforMinecraft(mainwingvector);
//								mainwingvector.scale(logic.getCamerapos()[0]-logic.prefab_vehicle.rotcenter[0]);
//								tailwingvector.scale(logic.getCamerapos()[1]-logic.prefab_vehicle.rotcenter[1]);
//								bodyvector.scale(logic.getCamerapos()[2]-logic.prefab_vehicle.rotcenter[2]);
							if (logic.camera != null) {
								Quat4d currentcamRot = new Quat4d(currentVehicleQuat);
								//currentcamRot.mul(HMV_Proxy.iszooming() && logic.prefab_vehicle.camerarot_zoom != null ?logic.prefab_vehicle.camerarot_zoom: logic.camerarot_current);
								double[] cameraxyz = eulerfromQuat((currentcamRot));
								cameraxyz[0] = toDegrees(cameraxyz[0]);
								cameraxyz[1] = toDegrees(cameraxyz[1]);
								cameraxyz[2] = toDegrees(cameraxyz[2]);
								if(logic.ispilot(entityplayer) && (logic.seatObjects[playerSeatID].mainWeapon == null || logic.seatObjects[playerSeatID].mainWeapon[logic.seatObjects[playerSeatID].currentWeaponMode].prefab_weaponCategory.userSittingTurretID == -1)) {
									//Vector3d cameraPos_Global = new Vector3d(logic.getCamerapos());
									//cameraPos_Global.sub(new Vector3d(logic.prefab_vehicle.rotcenter));
									//cameraPos_Global = transformVecByQuat(cameraPos_Global, currentVehicleQuat);
									//cameraPos_Global.add(new Vector3d(logic.prefab_vehicle.rotcenter));
									//transformVecforMinecraft(cameraPos_Global);
									//logic.camera.setLocationAndAngles(
									//		vehicleBody.prevPosX + (vehicleBody.posX - vehicleBody.prevPosX) * renderTickTime + cameraPos_Global.x,
									//		vehicleBody.prevPosY + (vehicleBody.posY - vehicleBody.prevPosY) * renderTickTime + cameraPos_Global.y - entityplayer.yOffset,
									//		vehicleBody.prevPosZ + (vehicleBody.posZ - vehicleBody.prevPosZ) * renderTickTime + cameraPos_Global.z,
									//		(float) cameraxyz[1], (float) cameraxyz[0]);
								} else {
									logic.riderPosUpdate_camera(nowPos, currentVehicleQuat,renderTickTime);
//										logic.camera.setLocationAndAngles(
//												entityplayer.posX,
//												entityplayer.posY - entityplayer.yOffset,
//												entityplayer.posZ,
//												(float) cameraxyz[1], (float) cameraxyz[0]);
								}
								minecraft.renderViewEntity = logic.camera;
								logic.camera.rotationYaw = (float) cameraxyz[1];
								logic.camera.prevRotationYaw = (float) cameraxyz[1];
								logic.camera.rotationYawHead = (float) cameraxyz[1];
								logic.camera.prevRotationYawHead = (float) cameraxyz[1];
								if(Double.isNaN(cameraxyz[0])){
									cameraxyz[0] = 0;
								}
								logic.camera.rotationPitch     = (float) cameraxyz[0];
								logic.camera.prevRotationPitch = (float) cameraxyz[0];
								trackFlag = true;
								trackedPlayerYaw = minecraft.thePlayer.rotationYaw = logic.camera.rotationYaw;
								trackedPlayerPit = minecraft.thePlayer.rotationPitch = logic.camera.rotationPitch;
								ObfuscationReflectionHelper.setPrivateValue(EntityRenderer.class, minecraft.entityRenderer, (float) cameraxyz[2], "camRoll", "R", "field_78495_O");
								ObfuscationReflectionHelper.setPrivateValue(EntityRenderer.class, minecraft.entityRenderer, (float) cameraxyz[2], "prevCamRoll", "field_78505_P");
							}

							minecraft.thePlayer.prevRotationYaw = minecraft.thePlayer.rotationYaw;
							minecraft.thePlayer.prevRotationPitch = minecraft.thePlayer.rotationPitch;
						} else if(HMV_Proxy.iszooming()){

							prevPlayerYaw = wrapAngleTo180_float(prevPlayerYaw);

							Quat4d Headrot = new Quat4d(0,0,0,1);
							{
								Headrot = quatRotateAxis(Headrot, new AxisAngle4d(unitX, toRadians(logic.cameraPitch) / 2));
								Headrot = quatRotateAxis(Headrot, new AxisAngle4d(unitY, toRadians(logic.cameraYaw) / 2));

								Quat4d invertVehicleQuat = new Quat4d(currentVehicleQuat);
								invertVehicleQuat.inverse();

								Headrot.mul(invertVehicleQuat, Headrot);
							}
							double[] Localcameraxyz = eulerfromQuat(Headrot);


							{

								if(logic.ispilot(entityplayer) && (logic.seatObjects[playerSeatID].mainWeapon == null || logic.seatObjects[playerSeatID].mainWeapon[logic.seatObjects[playerSeatID].currentWeaponMode].prefab_weaponCategory.userSittingTurretID == -1)) {
									{
										//Vector3d cameraPos_Global = new Vector3d(logic.getCamerapos());
										//cameraPos_Global.sub(new Vector3d(logic.prefab_vehicle.rotcenter));
										//cameraPos_Global = transformVecByQuat(cameraPos_Global, currentVehicleQuat);
										//cameraPos_Global.add(new Vector3d(logic.prefab_vehicle.rotcenter));
										//transformVecforMinecraft(cameraPos_Global);
										//logic.camera.setLocationAndAngles(
										//		vehicleBody.prevPosX + (vehicleBody.posX - vehicleBody.prevPosX) * renderTickTime + cameraPos_Global.x,
										//		vehicleBody.prevPosY + (vehicleBody.posY - vehicleBody.prevPosY) * renderTickTime + cameraPos_Global.y - entityplayer.yOffset,
										//		vehicleBody.prevPosZ + (vehicleBody.posZ - vehicleBody.prevPosZ) * renderTickTime + cameraPos_Global.z,
										//		(float) logic.camera.rotationYaw, (float) logic.camera.rotationPitch);
									}
								}else {
									logic.riderPosUpdate_camera(nowPos, currentVehicleQuat,renderTickTime);
//										logic.camera.setLocationAndAngles(
//												entityplayer.posX,
//												entityplayer.posY - entityplayer.yOffset,
//												entityplayer.posZ,
//												(float) cameraxyz[1], (float) cameraxyz[0]);
								}

								minecraft.renderViewEntity = logic.camera;
								double f1 = HMG_proxy.getMCInstance().gameSettings.mouseSensitivity * 0.6 + 0.2;
								double f2 = f1 * f1 * f1 * 8.0;
								double f3 = (HMG_proxy.getMCInstance().mouseHelper.deltaX * f2);
								double f4 = (HMG_proxy.getMCInstance().mouseHelper.deltaY * f2);
								logic.cameraYaw   += f3 * 0.15D * cos(Localcameraxyz[2]) - f4 * 0.15D * sin(Localcameraxyz[2]);
								logic.cameraPitch -= f3 * 0.15D * sin(Localcameraxyz[2]) + f4 * 0.15D * cos(Localcameraxyz[2]);

								if (logic.cameraPitch > 90 ) logic.cameraPitch = 90;
								if (logic.cameraPitch < -90) logic.cameraPitch = -90;

								logic.camera.rotationYaw   = minecraft.thePlayer.rotationYaw = logic.cameraYaw;
								logic.camera.rotationPitch = minecraft.thePlayer.rotationPitch = logic.cameraPitch;
								trackFlag = true;
								trackedPlayerYaw = minecraft.thePlayer.rotationYaw;
								trackedPlayerPit = minecraft.thePlayer.rotationPitch;


								logic.camera.prevRotationYaw = (float) logic.cameraYaw;
								logic.camera.rotationYawHead = (float) logic.cameraYaw;
								logic.camera.prevRotationYawHead = (float) logic.cameraYaw;
								logic.camera.prevRotationPitch = (float) logic.cameraPitch;
							}
							Localcameraxyz[0] = toDegrees(Localcameraxyz[0]);
							Localcameraxyz[1] = toDegrees(Localcameraxyz[1]);
							Localcameraxyz[2] = toDegrees(Localcameraxyz[2]);
//							System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
//							System.out.println("current   Y " + prevPlayerYaw);
//							System.out.println("current   P " + prevPlayerPit);
//
//							boolean flag = false;
//							if ((!logic.mouseStickMode && logic.ispilot(entityplayer)) || logic.prefab_vehicle.T_Land_F_Plane) {
//								float f1 = HMG_proxy.getMCInstance().gameSettings.mouseSensitivity * 0.6F + 0.2F;
//								float f2 = f1 * f1 * f1 * 8.0F;
//								float f3 = (float) HMG_proxy.getMCInstance().mouseHelper.deltaX * f2;
//								float f4 = (float) HMG_proxy.getMCInstance().mouseHelper.deltaY * f2;
//								Localcameraxyz[1] += f3 * 0.15D;
//								Localcameraxyz[0] += f4 * 0.15D;
//
//								Quat4d differ = new Quat4d(0,0,0,1);
////								differ.mul(invertVehicleQuat);
//
//								differ = quatRotateAxis(differ, new AxisAngle4d(unitX, -toRadians(f4 * 0.15D) / 2));
//								differ = quatRotateAxis(differ, new AxisAngle4d(unitY, toRadians(f3 * 0.15D) / 2));
//								{
////									Vector3d lookVec = new Vector3d(0, 0, 1);
////									lookVec = transformVecByQuat(lookVec, newHeadRot_Global);
////									lookVec = transformVecByQuat(lookVec, invertVehicleQuat);
////
//////							toDegrees(atan2(lookVec.x,lookVec.z)) , toDegrees(asin(lookVec.y));
////
////									Quat4d newRot_forCalcRoll = new Quat4d(0, 0, 0, 1);
////									newRot_forCalcRoll = quatRotateAxis(newRot_forCalcRoll, new AxisAngle4d(unitX, asin(lookVec.y) / 2));
////									newRot_forCalcRoll = quatRotateAxis(newRot_forCalcRoll, new AxisAngle4d(unitY, atan2(lookVec.x, lookVec.z) / 2));
////
////									logic.camerarot.set(newRot_forCalcRoll);
////									logic.camerarot_current.set(logic.camerarot);
////									Quat4d currentcamRot = new Quat4d(currentVehicleQuat);
////									currentcamRot.mul(HMV_Proxy.iszooming() && logic.prefab_vehicle.camerarot_zoom != null ? logic.prefab_vehicle.camerarot_zoom : logic.camerarot_current);
////
////									double[] cameraxyz = eulerfromQuat((currentcamRot));
//									differ = quatRotateAxis(differ, new AxisAngle4d(unitZ, -Localcameraxyz[2] / 2));
//								}
//
//								newHeadRot_Global.mul(differ);
//
//								if (Localcameraxyz[0] > 90) Localcameraxyz[0] = 90;
//								if (Localcameraxyz[0] < -90) Localcameraxyz[0] = -90;
//
//								flag = true;
//							}
////							newHeadRot_Global = quatRotateAxis(newHeadRot_Global, new AxisAngle4d(unitX, toRadians(Localcameraxyz[0]) / 2));
////							newHeadRot_Global = quatRotateAxis(newHeadRot_Global, new AxisAngle4d(unitY, toRadians(Localcameraxyz[1]) / 2));
//
//
//
//							double[] NewCameraxyz = eulerfromQuat(newHeadRot_Global);
//							NewCameraxyz[0] = -toDegrees(NewCameraxyz[0]);
//							NewCameraxyz[1] = toDegrees(NewCameraxyz[1]);
//							NewCameraxyz[2] = toDegrees(NewCameraxyz[2]);
//							System.out.println("NewCamera Y " + NewCameraxyz[1]);
//							System.out.println("NewCamera P " + NewCameraxyz[0]);
//							if (NewCameraxyz[0] > 90){
//								NewCameraxyz[0] = 90;
//								NewCameraxyz[1] += 180;
//								NewCameraxyz[1] = wrapAngleTo180_double(NewCameraxyz[1]);
//							}else
//							if (NewCameraxyz[0] < -90) {
//								NewCameraxyz[0] = -90;
//								NewCameraxyz[1] += 180;
//								NewCameraxyz[1] = wrapAngleTo180_double(NewCameraxyz[1]);
//							}
//							if(flag){
//								minecraft.thePlayer.rotationYaw = (float) NewCameraxyz[1];
//								minecraft.thePlayer.rotationPitch = (float) NewCameraxyz[0];
//							}
//							System.out.println("New       Y " + minecraft.thePlayer.rotationYaw);
//							System.out.println("New       P " + minecraft.thePlayer.rotationPitch);

							{
								Headrot = new Quat4d(0,0,0,1);

								Headrot = quatRotateAxis(Headrot, new AxisAngle4d(unitX, toRadians(logic.camera.rotationPitch) / 2));
								Headrot = quatRotateAxis(Headrot, new AxisAngle4d(unitY, toRadians(logic.camera.rotationYaw) / 2));

								Quat4d invertVehicleQuat = new Quat4d(currentVehicleQuat);
								invertVehicleQuat.inverse();

								Headrot.mul(invertVehicleQuat, Headrot);
							}
							double[] NextLocalcameraxyz = eulerfromQuat(Headrot);
							NextLocalcameraxyz[2] = toDegrees(NextLocalcameraxyz[2]);

							{
//								Vector3d lookVec = new Vector3d(0, 0, 1);
//								lookVec = transformVecByQuat(lookVec, newHeadRot_Global);
//								lookVec = transformVecByQuat(lookVec, invertVehicleQuat);
//
////							toDegrees(atan2(lookVec.x,lookVec.z)) , toDegrees(asin(lookVec.y));
//
//								Quat4d newRot_forCalcRoll = new Quat4d(0, 0, 0, 1);
//								newRot_forCalcRoll = quatRotateAxis(newRot_forCalcRoll, new AxisAngle4d(unitX, asin(lookVec.y) / 2));
//								newRot_forCalcRoll = quatRotateAxis(newRot_forCalcRoll, new AxisAngle4d(unitY, atan2(lookVec.x, lookVec.z) / 2));
//
//								logic.camerarot.set(newRot_forCalcRoll);
//								logic.camerarot_current.set(logic.camerarot);
//								Quat4d currentcamRot = new Quat4d(currentVehicleQuat);
//								currentcamRot.mul(HMV_Proxy.iszooming() && logic.prefab_vehicle.camerarot_zoom != null ? logic.prefab_vehicle.camerarot_zoom : logic.camerarot_current);
//
//								double[] cameraxyz = eulerfromQuat((currentcamRot));
//								cameraxyz[0] = toDegrees(cameraxyz[0]);
//								cameraxyz[1] = toDegrees(cameraxyz[1]);
//								cameraxyz[2] = toDegrees(cameraxyz[2]);
								ObfuscationReflectionHelper.setPrivateValue(EntityRenderer.class, minecraft.entityRenderer, (float) -NextLocalcameraxyz[2], "camRoll", "R", "field_78495_O");
								ObfuscationReflectionHelper.setPrivateValue(EntityRenderer.class, minecraft.entityRenderer, (float) -NextLocalcameraxyz[2], "prevCamRoll", "field_78505_P");
							}

//							System.out.println("y" + xyz[0] + " , x" + xyz[1] + " , z" + xyz[2] + " , renderTickTime" + renderTickTime);

//								Vector3d bodyvector = transformVecByQuat(new Vector3d(0, 0, 1), currentVehicleQuat);
//								Vector3d tailwingvector = transformVecByQuat(new Vector3d(0, 1, 0), currentVehicleQuat);
//								Vector3d mainwingvector = transformVecByQuat(new Vector3d(1, 0, 0), currentVehicleQuat);
//
//								transformVecforMinecraft(tailwingvector);
//								transformVecforMinecraft(bodyvector);
//								transformVecforMinecraft(mainwingvector);
//								mainwingvector.scale(logic.getCamerapos()[0]-logic.prefab_vehicle.rotcenter[0]);
//								tailwingvector.scale(logic.getCamerapos()[1]-logic.prefab_vehicle.rotcenter[1]);
//								bodyvector.scale(logic.getCamerapos()[2]-logic.prefab_vehicle.rotcenter[2]);

//							if (logic.camera != null) {
////								if(logic.seatObjects[playerSeatID].mainWeapon == null || logic.seatObjects[playerSeatID].mainWeapon[logic.seatObjects[playerSeatID].currentWeaponMode].prefab_weaponCategory.userSittingTurretID == -1) {
////									{
////										Vector3d cameraPos_Global = new Vector3d(logic.getCamerapos());
////										cameraPos_Global.sub(new Vector3d(logic.prefab_vehicle.rotcenter));
////										cameraPos_Global = transformVecByQuat(cameraPos_Global, currentVehicleQuat);
////										cameraPos_Global.add(new Vector3d(logic.prefab_vehicle.rotcenter));
////										transformVecforMinecraft(cameraPos_Global);
////										logic.camera.setLocationAndAngles(
////												vehicleBody.prevPosX + (vehicleBody.posX - vehicleBody.prevPosX) * renderTickTime + cameraPos_Global.x,
////												vehicleBody.prevPosY + (vehicleBody.posY - vehicleBody.prevPosY) * renderTickTime + cameraPos_Global.y - entityplayer.yOffset,
////												vehicleBody.prevPosZ + (vehicleBody.posZ - vehicleBody.prevPosZ) * renderTickTime + cameraPos_Global.z,
////												(float) cameraxyz[1], (float) cameraxyz[0]);
////									}
////								} else {
////									logic.riderPosUpdate_camera(nowPos, currentVehicleQuat,renderTickTime);
//////										logic.camera.setLocationAndAngles(
//////												entityplayer.posX,
//////												entityplayer.posY - entityplayer.yOffset,
//////												entityplayer.posZ,
//////												(float) cameraxyz[1], (float) cameraxyz[0]);
////								}
////								minecraft.renderViewEntity = logic.camera;
////								logic.camera.rotationYaw = (float) cameraxyz[1];
////								logic.camera.prevRotationYaw = (float) cameraxyz[1];
////								logic.camera.rotationYawHead = (float) cameraxyz[1];
////								logic.camera.prevRotationYawHead = (float) cameraxyz[1];
////								if(Double.isNaN(cameraxyz[0])){
////									cameraxyz[0] = 0;
////								}
////								logic.camera.rotationPitch = (float) cameraxyz[0];
////								logic.camera.prevRotationPitch = (float) cameraxyz[0];
//
////								minecraft.thePlayer.rotationYaw = (float) -cameraxyz[1];
////								minecraft.thePlayer.rotationPitch = (float) cameraxyz[0];
//							}
							minecraft.thePlayer.prevRotationYaw = minecraft.thePlayer.rotationYaw;
							minecraft.thePlayer.prevRotationPitch = minecraft.thePlayer.rotationPitch;
						}else {
							logic.cameraYaw   = entityplayer.rotationYaw;
							logic.cameraPitch = entityplayer.rotationPitch;
							if ((minecraft.renderViewEntity instanceof EntityCameraDummy)) minecraft.renderViewEntity = entityplayer;
						}
//							logic.riderPosUpdate_forRender(nowPos, currentVehicleQuat);
						logic.bodyrotationYaw = (float) xyz[1];
						logic.prevbodyrotationYaw = (float) xyz[1];
						logic.bodyrotationPitch = (float) xyz[0];
						logic.prevbodyrotationPitch = (float) xyz[0];
						vehicleBody.rotationPitch = (float) xyz[0];
						vehicleBody.prevRotationPitch = (float) xyz[0];
						logic.bodyrotationRoll = (float) xyz[2];
						logic.prevbodyrotationRoll = (float) xyz[2];
						//ObfuscationReflectionHelper.setPrivateValue(EntityRenderer.class, minecraft.entityRenderer, logic.prefab_vehicle.thirdPersonDistance, "thirdPersonDistance", "E", "field_78490_B");
						needrest = true;

						//if (logic.prefab_vehicle.sightTex[playerSeatID] != null) {
						//	if(HMV_Proxy.iszooming()){
						//		{
						//			TurretObj turretObj = getPlayerControllingMainTurret(entityplayer);
						//			if (((IVehicle) entityplayer.ridingEntity).getBaseLogic().prefab_vehicle.prefab_seats.length > playerSeatID &&
						//					((IVehicle) entityplayer.ridingEntity).getBaseLogic().prefab_vehicle.prefab_seats[playerSeatID].zoomLevel > 0) {
						//				ObfuscationReflectionHelper.setPrivateValue(EntityRenderer.class, minecraft.entityRenderer,
						//						((IVehicle) entityplayer.ridingEntity).getBaseLogic().prefab_vehicle.prefab_seats[playerSeatID].zoomLevel, "cameraZoom", "field_78503_V");
						//			}else
						//			if(turretObj != null) {
						//				if (turretObj.getCurrentGuninfo() != null && turretObj.getCurrentGuninfo().scopezoombase != 1) {
						//					ObfuscationReflectionHelper.setPrivateValue(EntityRenderer.class, minecraft.entityRenderer,
						//							turretObj.getCurrentGuninfo().scopezoombase, "cameraZoom", "field_78503_V");
						//				}
						//			}
						//		}
						//	}else {
						//		ObfuscationReflectionHelper.setPrivateValue(EntityRenderer.class, minecraft.entityRenderer,
						//				1.0d, "cameraZoom", "field_78503_V");
						//	}
						//}
						prevPlayerYaw = minecraft.thePlayer.rotationYaw;
						prevPlayerPit = minecraft.thePlayer.rotationPitch;
					}
				} else {
					if (needrest) {
						ObfuscationReflectionHelper.setPrivateValue(EntityRenderer.class, minecraft.entityRenderer, 0, "camRoll", "field_78495_O");
						ObfuscationReflectionHelper.setPrivateValue(EntityRenderer.class, minecraft.entityRenderer, 0, "prevCamRoll", "field_78505_P");
						ObfuscationReflectionHelper.setPrivateValue(EntityRenderer.class, minecraft.entityRenderer, 4, "thirdPersonDistance", "E", "field_78490_B");
						ObfuscationReflectionHelper.setPrivateValue(EntityRenderer.class, minecraft.entityRenderer,
								1.0d, "cameraZoom", "field_78503_V");
						needrest = false;
						if ((minecraft.renderViewEntity instanceof EntityCameraDummy)) minecraft.renderViewEntity = entityplayer;
					}
				}
				break;
			case END:
				if(trackFlag){
					minecraft.thePlayer.rotationYaw = trackedPlayerYaw;
					minecraft.thePlayer.rotationPitch = trackedPlayerPit;
					trackFlag = false;
				}
				if ((minecraft.renderViewEntity instanceof EntityCameraDummy)) minecraft.renderViewEntity = entityplayer;
				break;
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void drawScreenEvent(GuiScreenEvent.DrawScreenEvent.Pre event) {
	}
	boolean NeedReset;
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void renderover(RenderGameOverlayEvent.Post event) {

		if (event.type == RenderGameOverlayEvent.ElementType.ALL) {

			GL11.glEnable(GL_BLEND);
			GL11.glEnable(GL11.GL_ALPHA_TEST);
			GL11.glAlphaFunc(GL11.GL_GREATER, 0);
//			ArrayList<EntityLinkedPos_Motion> tempDel = new ArrayList<>();
//			for (EntityLinkedPos_Motion a_target_pos_motion : target_Pos_Motion) {
//				if (a_target_pos_motion.for_aliveCnt > event.partialTicks) a_target_pos_motion.livingTime++;
//				if (a_target_pos_motion.livingTime > 1){
//					tempDel.add(a_target_pos_motion);
//				}
//				a_target_pos_motion.for_aliveCnt = event.partialTicks;
//			}
//			target_Pos_Motion.removeAll(tempDel);
//			tempDel = new ArrayList<>();
//			for (EntityLinkedPos_Motion a_missile_pos_motion : missile_Pos_Motion) {
//				if (a_missile_pos_motion.for_aliveCnt > event.partialTicks) a_missile_pos_motion.livingTime++;
//				if (a_missile_pos_motion.livingTime > 20) tempDel.add(a_missile_pos_motion);
//				a_missile_pos_motion.for_aliveCnt = event.partialTicks;
//			}
//			missile_Pos_Motion.removeAll(tempDel);

			Minecraft minecraft = FMLClientHandler.instance().getClient();
			EntityPlayer entityplayer = minecraft.thePlayer;

			if (entityplayer.ridingEntity instanceof IVehicle) {

				GuiIngameForge.renderCrosshairs = true;
				NeedReset = true;
				BaseLogic logic = ((IVehicle) entityplayer.ridingEntity).getBaseLogic();
				Entity vehicleBody = entityplayer.ridingEntity;
				ScaledResolution scaledresolution = new ScaledResolution(minecraft, minecraft.displayWidth,
						minecraft.displayHeight);
				int i = scaledresolution.getScaledWidth();
				int j = scaledresolution.getScaledHeight();

				boolean skip_HUD = false;
				//if (logic.prefab_vehicle.script_global != null) {
				//	try {
				//		skip_HUD = (boolean) logic.prefab_vehicle.script_global.invokeFunction("GUI_rendering_HUD", this, vehicleBody, i, j);
				//	} catch (NoSuchMethodException | ScriptException e) {
				//		e.printStackTrace();
				//	}
				//}
		//		if (!skip_HUD && logic.ispilot(entityplayer)) {
		//			if (!logic.prefab_vehicle.T_Land_F_Plane) {
		//				Entity planebody = entityplayer.ridingEntity;
		//				if (logic.prefab_vehicle.displayModernHud)
		//					displayFlyersHUD_AftGen2(logic, logic.prevbodyRot, logic.bodyRot, planebody, logic.forVapour_PrevMotionVec, event);
		//				else
		//					displayFlyersHUD(logic, logic.prevbodyRot, logic.bodyRot, planebody, logic.forVapour_PrevMotionVec, event);
//		//		Quat4d tempquat = new Quat4d(
//		//				logic.prevbodyRot.x * (1-event.partialTicks) + logic.bodyRot.x * event.partialTicks ,
//		//				logic.prevbodyRot.y * (1-event.partialTicks) + logic.bodyRot.y * event.partialTicks ,
//		//				logic.prevbodyRot.z * (1-event.partialTicks) + logic.bodyRot.z * event.partialTicks ,
//		//				logic.prevbodyRot.w * (1-event.partialTicks) + logic.bodyRot.w * event.partialTicks );
////	//								if (abs(logic.pitchladder) > 0.001) {
////	//									Vector3d axisx = transformVecByQuat(new Vector3d(1, 0, 0), tempquat);
////	//									AxisAngle4d axisxangled = new AxisAngle4d(axisx, toRadians(-logic.pitchladder * renderTickTime / 4));
////	//									tempquat = quatRotateAxis(tempquat, axisxangled);
////	//								}
////	//								if (abs(logic.yawladder) > 0.001) {
////	//									Vector3d axisy = transformVecByQuat(new Vector3d(0, 1, 0), tempquat);
////	//									AxisAngle4d axisyangled = new AxisAngle4d(axisy, toRadians(logic.yawladder * renderTickTime / 4));
////	//									tempquat = quatRotateAxis(tempquat, axisyangled);
////	//								}
////	//								if (abs(logic.rollladder) > 0.001) {
////	//									Vector3d axisz = transformVecByQuat(new Vector3d(0, 0, 1), tempquat);
////	//									AxisAngle4d axiszangled = new AxisAngle4d(axisz, toRadians(logic.rollladder * renderTickTime / 4));
////	//									tempquat = quatRotateAxis(tempquat, axiszangled);
////	//								}
////
//		//		GL11.glPushMatrix();
//		//		double width = scaledresolution.getScaledWidth_double();
//		//		double height = scaledresolution.getScaledHeight_double();
//		//		//HUDは300×650
//		//		//一度3.6px
//		//		//横幅を合わせる
//		//		double scale = width/300;
//		//		double sizeW = width;
//		//		double sizeH = sizeW * 650/300;
//		//		double[] xyz = eulerfromQuat((tempquat));
//		//		xyz[0] = toDegrees(xyz[0]);
//		//		xyz[1] = toDegrees(xyz[1]);
//		//		xyz[2] = toDegrees(xyz[2]);
//		//		GL11.glEnable(GL11.GL_BLEND);
//		//		GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.8F);
//		//		GL11.glTranslatef((float)width/2,(float) height/2,0);
//		//		GL11.glRotatef((float) -xyz[2],0,0,1);
//		//		GL11.glTranslatef(-(float)width/2,-(float) height/2,0);
//		//		minecraft.renderEngine.bindTexture(new ResourceLocation("gvcmob:textures/items/HUD.png"));
//		//		drawTexturedModalRect(0,height/2 - 325 * scale - xyz[0] * 3.6*scale,sizeW,sizeH);
//		//		GL11.glPopMatrix();
////
//		//		GL11.glPushMatrix();
//		//		scale = width/300;
//		//		sizeW = width;
//		//		sizeH = width;
//		//		minecraft.renderEngine.bindTexture(new ResourceLocation("gvcmob:textures/items/HUD2.png"));
////
//		//		drawTexturedModalRect(0,height/2 - 150 * scale,sizeW,sizeH);
//		//		GL11.glPopMatrix();
////
////
//		//		GL11.glPushMatrix();
//		//		Vector3d forDisplayPlaneMotion = new Vector3d(
//		//				logic.posX - logic.prevPosX,
//		//				logic.posY - logic.prevPosY,
//		//				-(logic.posZ - logic.prevPosZ));
////
//		//		Quat4d quat4d = new Quat4d(0,0,0,1);
//		//		quat4d.inverse(tempquat);
//		//		forDisplayPlaneMotion = transformVecByQuat(forDisplayPlaneMotion,quat4d);
//		//		forDisplayPlaneMotion.scale(-1);
//		//		double angle = toDegrees(forDisplayPlaneMotion.angle(new Vector3d(0,0,1)));
////
//		//		forDisplayPlaneMotion.z = 0;
//		//		forDisplayPlaneMotion.normalize();
////
//		//		GL11.glRotatef((float) xyz[2],0,0,1);
//		//		GL11.glTranslatef((float)( forDisplayPlaneMotion.x * angle * 3.6 * scale), (float)( forDisplayPlaneMotion.y * angle * 3.6 * scale),0);
//		//		GL11.glRotatef((float) -xyz[2],0,0,1);
////
//		//		minecraft.renderEngine.bindTexture(new ResourceLocation("gvcmob:textures/items/HUD3.png"));
//		//		drawTexturedModalRect(0,height/2 - 150 * scale,sizeW,sizeH);
////
//		//		GL11.glPopMatrix();
//
//		//		fontrenderer.drawStringWithShadow("Missile : " + logic.rocket + " : " + (logic.missile != null ? "Continue radar irradiation" : logic.illuminated != null?"LOCK":""), i - 300, j - 20 - 10, color);
		//			}
		//		}
				{
					IVehicle vehicle = (IVehicle) entityplayer.ridingEntity;
					FontRenderer fontrenderer = minecraft.fontRenderer;
					GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
					{
						GL11.glPushMatrix();
						boolean skip = false;
						//if (vehicle.getBaseLogic().prefab_vehicle.script_global != null) {
						//	try {
						//		skip = (boolean) vehicle.getBaseLogic().prefab_vehicle.script_global.invokeFunction("GUI_rendering_2D", this, vehicle, i, j);
						//	} catch (NoSuchMethodException | ScriptException e) {
						//		e.printStackTrace();
						//	}
						//}
						if (!skip) {

							{


								String hp = String.format("%1$3d", (int) logic.health);
								//String mhp = String.format("%1$3d", (int) logic.prefab_vehicle.maxhealth);
								String th = String.valueOf(((int) (vehicle.getBaseLogic().throttle * 10f)) / 10f);

								SeatObject playerSeatObject = vehicle.getBaseLogic().seatObjects[playerSeatID];
								if (playerSeatObject.mainWeapon != null) {
									if (playerSeatObject.currentWeaponMode >= playerSeatObject.mainWeapon.length)
										playerSeatObject.currentWeaponMode = 0;
									TurretObj turretObj = playerSeatObject.mainWeapon[playerSeatObject.currentWeaponMode].getCriterionTurret();
//									turretObj = getActiveTurret(turretObj);
									String name = "MAIN";
									if (playerSeatObject.mainWeapon.length > 0) {
										if(playerSeatObject.mainWeapon.length != 1)name = name.concat("-" + playerSeatObject.currentWeaponMode);
										if(playerSeatObject.mainWeapon[playerSeatObject.currentWeaponMode].prefab_weaponCategory.name != null)name = name.concat("-" + playerSeatObject.mainWeapon[playerSeatObject.currentWeaponMode].prefab_weaponCategory.name);
									}
									if (turretObj != null && turretObj.gunStack != null)
										displayGunState(turretObj, fontrenderer, i, j, i, 60, name);
									else
										fontrenderer.drawStringWithShadow(name,
												0, j - 60, 0xFFFFFF);
								}
								if (playerSeatObject.subWeapon != null) {
									TurretObj turretObj = playerSeatObject.subWeapon.getCriterionTurret();
//									turretObj = getActiveTurret(turretObj);
									if (turretObj != null && turretObj.gunStack != null)
										displayGunState(turretObj, fontrenderer, i, j, i, 50, "SUB ");
									else
										fontrenderer.drawStringWithShadow(playerSeatObject.subWeapon == null ? "No sub" : "SUB " + playerSeatObject.subWeapon.prefab_weaponCategory.name,
												0, j - 50, 0xFFFFFF);
								}


								//fontrenderer.drawStringWithShadow("HP " + hp + "/" + mhp + " : throttle " + th, i - 240, j - 90, 0xFFFFFF);
								//fontrenderer.drawStringWithShadow("TH" + th, (i/2) - 80, j/2 + 0, 0xFFFFFF);
								//fontrenderer.drawStringWithShadow("Speed"+ speed, (i/2) - 80, j/2 +20, 0xFFFFFF);
								GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
								int color = 0xFFFFFF;
								fontrenderer.drawStringWithShadow("Speed : " + (int) (-vehicle.getBaseLogic().localMotionVec.z * 72), i - 300, j - 70, color);
								if(HMV_Proxy.flap_click())fontrenderer.drawStringWithShadow("Flap Down", i - 300, j - 90, color);
								//g.drawTexturedModelRectFromIcon(i-70, j-63, armor.getIconFromDamage(0), 16, 16);

							}
							GL11.glPopMatrix();

							GuiIngame g = minecraft.ingameGUI;
							GL11.glPushMatrix();//21
							{
								float currentRotationYaw = (minecraft.renderViewEntity.prevRotationYaw + (minecraft.renderViewEntity.rotationYaw - minecraft.renderViewEntity.prevRotationYaw) * partialTicks);

//								float currentRotationPit = (minecraft.renderViewEntity.prevRotationPitch + (minecraft.renderViewEntity.rotationPitch - minecraft.renderViewEntity.prevRotationPitch) * partialTicks);
								GL11.glEnable(GL11.GL_BLEND);
								GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.8F);
								GL11.glTranslatef(scaledresolution.getScaledWidth()/2f - 32, scaledresolution.getScaledHeight() -  60, 0F);
//								GL11.glTranslatef(32, scaledresolution.getScaledHeight() - 32, 0F);
//								GL11.glTranslatef(-32, -(scaledresolution.getScaledHeight() - 32), 0F);
								GL11.glScalef(0.25f, 0.25f, 1);
								minecraft.renderEngine.bindTexture(new ResourceLocation("handmadevehicle:textures/items/bodyIcon2.png"));
								g.drawTexturedModalRect(0, 0, 0, 0, 256, 256);
								GL11.glScalef(4, 4, 1);

								GL11.glTranslatef(32, 32, 0F);
								GL11.glRotatef((vehicle.getBaseLogic().prevbodyrotationYaw + (vehicle.getBaseLogic().bodyrotationYaw - vehicle.getBaseLogic().prevbodyrotationYaw) * partialTicks) - currentRotationYaw, 0.0F, 0.0F, 1.0F);
								GL11.glTranslatef(-32,-32, 0F);
								//drawTexturedModalRect(scaledresolution.getScaledWidth()/2 -0,  scaledresolution.getScaledHeight()/2 +24, 0,0, 256, 256);
								GL11.glScalef(0.25f, 0.25f, 1);
								minecraft.renderEngine.bindTexture(new ResourceLocation("handmadevehicle:textures/items/bodyIcon1.png"));
								g.drawTexturedModalRect(0, 0, 0, 0, 256, 256);
							}
						}
						GL11.glPopMatrix();
					}

					setUp3DView(minecraft, event.partialTicks);

					float currentRotationYaw = (minecraft.renderViewEntity.prevRotationYawHead + (minecraft.renderViewEntity.rotationYawHead - minecraft.renderViewEntity.prevRotationYawHead) * partialTicks);

					float currentRotationPit = (minecraft.renderViewEntity.prevRotationPitch + (minecraft.renderViewEntity.rotationPitch - minecraft.renderViewEntity.prevRotationPitch) * partialTicks);


					GL11.glPushMatrix();
					GL11.glRotatef(0, 1.0F, 0.0F, 0.0F);
					GL11.glRotatef(180, 0.0F, 1.0F, 0.0F);
					boolean skip = false;
					//if (vehicle.getBaseLogic().prefab_vehicle.script_global != null) {
					//	try {
					//		skip = (boolean) vehicle.getBaseLogic().prefab_vehicle.script_global.invokeFunction("GUI_rendering_3D", this, vehicle);
					//	} catch (NoSuchMethodException | ScriptException e) {
					//		e.printStackTrace();
					//	}
					//}
					if (!skip) {
						//if (!vehicle.getBaseLogic().detectedList.isEmpty()) {
						//	BaseLogic baseLogic = vehicle.getBaseLogic();
						//	for (EntityLinkedPos_Motion a_pos_motion : baseLogic.detectedList) {
						//		Vector3d vecToLockTargetPos = new Vector3d();
						//		vecToLockTargetPos.add(entityCurrentPos(a_pos_motion));
						//		vecToLockTargetPos.sub(entityCurrentPos(minecraft.renderViewEntity));
						//		vecToLockTargetPos.normalize();
						//		RotateVectorAroundY(vecToLockTargetPos, currentRotationYaw);
						//		RotateVectorAroundX(vecToLockTargetPos, currentRotationPit);
						//		renderLockOnMarker(minecraft, vehicle.getBaseLogic().prefab_vehicle.searchedMarker, vecToLockTargetPos);
						//		GL11.glPushMatrix();
						//		GL11.glTranslatef((float)vecToLockTargetPos.x,(float)vecToLockTargetPos.y,(float)vecToLockTargetPos.z);
						//		fontrenderer.drawStringWithShadow(String.valueOf(vecToLockTargetPos.length()),
						//				0, 10, 0xFFFFFF);
						//		GL11.glPopMatrix();
						//	}
						//}

						SeatObject playerSeatObject = vehicle.getBaseLogic().seatObjects[playerSeatID];
						if(playerSeatObject.mainWeapon != null){
//							System.out.println("debug" + turretObj.prefab_turret.turretName);
							WeaponCategory mainWeapon = playerSeatObject.mainWeapon[playerSeatObject.currentWeaponMode];
							if(mainWeapon.getDisplayCriterionTurret() != null) {
								TurretObj displayCriterionTurret = mainWeapon.getDisplayCriterionTurret();
								//if(mainWeapon.targetPos != null)
								//	try{
								//		//displayTurretLockTarget(mainWeapon,displayCriterionTurret,vehicle,logic,mainWeapon.targetPos);
								//	}catch (Exception e){
								//		e.printStackTrace();
								//	}
//								System.out.println("" + mainWeapon.lockedBlockPos);
								if(mainWeapon.lockedBlockPos != null)
									try{
										//displayTurretLockTarget(mainWeapon,displayCriterionTurret,vehicle,logic,new EntityLinkedPos_Motion(mainWeapon.lockedBlockPos,-1));
									}catch (Exception e){
										e.printStackTrace();
									}
//								System.out.println("debug" + a_pos_motion);

							}
						}

						//if (vehicle.getBaseLogic().prefab_vehicle.sightTex[playerSeatID] != null) {
						//	if(HMV_Proxy.iszooming()){
						//		renderPumpkinBlur(minecraft, new ResourceLocation(vehicle.getBaseLogic().prefab_vehicle.sightTex[playerSeatID]));
						//		GuiIngameForge.renderCrosshairs = false;
						//		NeedReset = true;
//
//						//		 {
//						//			TurretObj turretObj = getPlayerUsingMainTurret(entityplayer);
//						//			if(turretObj != null) {
//						//				System.out.println("debug");
//						//				if (((IVehicle) entityplayer.ridingEntity).getBaseLogic().prefab_vehicle.prefab_seats.length > playerSeatID &&
//						//						((IVehicle) entityplayer.ridingEntity).getBaseLogic().prefab_vehicle.prefab_seats[playerSeatID].zoomLevel > 0) {
//						//					System.out.println("debug");
//						//					ObfuscationReflectionHelper.setPrivateValue(EntityRenderer.class, minecraft.entityRenderer,
//						//							((IVehicle) entityplayer.ridingEntity).getBaseLogic().prefab_vehicle.prefab_seats[playerSeatID].zoomLevel, "cameraZoom", "field_78503_V");
//						//				}else if (turretObj.gunItem != null && turretObj.gunItem.gunInfo.scopezoombase != 1) {
//						//					System.out.println("debug");
//						//					ObfuscationReflectionHelper.setPrivateValue(EntityRenderer.class, minecraft.entityRenderer,
//						//							turretObj.gunItem.gunInfo.scopezoombase, "cameraZoom", "field_78503_V");
//						//				}
//						//			}
//						//		}
						//	}
						//}

					}
					GL11.glPopMatrix();
					setUp2DView(minecraft);
				}
			} else {
				if (NeedReset) GuiIngameForge.renderCrosshairs = true;
				NeedReset = false;
			}
			minecraft.getTextureManager().bindTexture(Gui.icons);
		}
	}
	public void displayGunState(TurretObj turretObj,FontRenderer fontrenderer,int i,int j,int posx,int posy,String name){

		if (turretObj.readytoFire()) {
			name = name.concat("  " + turretObj.getName());
			fontrenderer.drawStringWithShadow(name + " Ready remain:" + turretObj.gunItem.remain_Bullet(turretObj.gunStack) + "/" + turretObj.max_Bullet(),
					i - posx, j - posy, 0xFFFFFF);
		} else if (turretObj.isreloading()) {
			fontrenderer.drawStringWithShadow(name + "       Reloading" + turretObj.getDummyStackTag().getInteger("RloadTime") + "/" + turretObj.gunItem.reloadTime(turretObj.gunStack),
					i - posx, j - posy, 0xFFFFFF);
		} else if (turretObj.isLoading()) {
			if (turretObj.prefab_turret.gunInfo.cycle > 5)
				fontrenderer.drawStringWithShadow(name + "         Loading" + turretObj.getDummyStackTag().getByte("Bolt") + "/" + turretObj.prefab_turret.gunInfo.cycle,
						i - posx, j - posy, 0xFFFFFF);
			else
				fontrenderer.drawStringWithShadow(name + " Ready remain:" + turretObj.gunItem.remain_Bullet(turretObj.gunStack) + "/" + turretObj.max_Bullet(),
						i - posx, j - posy, 0xFFFFFF);
		}
//		System.out.println("" + name);

	}

	//public void displayTurretLockTarget(WeaponCategory mainWeapon, TurretObj displayCriterionTurret, IVehicle vehicle, BaseLogic logic
	//,EntityLinkedPos_Motion targetPos){
	//	Minecraft minecraft = FMLClientHandler.instance().getClient();
	//	FontRenderer fontrenderer = minecraft.fontRenderer;
//
	//	float currentRotationYaw = (minecraft.renderViewEntity.prevRotationYawHead + (minecraft.renderViewEntity.rotationYawHead - minecraft.renderViewEntity.prevRotationYawHead) * partialTicks);
//
	//	float currentRotationPit = (minecraft.renderViewEntity.prevRotationPitch + (minecraft.renderViewEntity.rotationPitch - minecraft.renderViewEntity.prevRotationPitch) * partialTicks);
//
//
	//	Vector3d vecToLockTargetPos = new Vector3d();
	//	vecToLockTargetPos.add(entityCurrentPos(targetPos));
	//	vecToLockTargetPos.sub(entityCurrentPos(minecraft.renderViewEntity));
	//	double range = vecToLockTargetPos.length();
	//	vecToLockTargetPos.normalize();
	//	RotateVectorAroundY(vecToLockTargetPos, currentRotationYaw);
	//	RotateVectorAroundX(vecToLockTargetPos, currentRotationPit);
	//	renderLockOnMarker(minecraft, displayCriterionTurret.getCurrentGuninfo().lockOnMarker, vecToLockTargetPos);
	//	GL11.glPushMatrix();
	//	{
	//		GL11.glRotatef(-(float) ObfuscationReflectionHelper.getPrivateValue(EntityRenderer.class, minecraft.entityRenderer, "camRoll", "R", "field_78495_O"), 0, 0, 1);
	//		GL11.glTranslatef((float) vecToLockTargetPos.x, (float) vecToLockTargetPos.y, (float) vecToLockTargetPos.z);
	//		GL11.glRotatef((float) ObfuscationReflectionHelper.getPrivateValue(EntityRenderer.class, minecraft.entityRenderer, "camRoll", "R", "field_78495_O"), 0, 0, 1);
	//		GL11.glScalef(-0.005f, -0.005f, 0.005f);
	//		GL11.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
	//		fontrenderer.drawStringWithShadow(String.valueOf((int) range),
	//				10, 5, 0xFFFFFF);
//
	//		Vector3d motionVec = new Vector3d(
	//				targetPos.motionX,
	//				targetPos.motionY,
	//				targetPos.motionZ);
//
	//		if (motionVec.length() > 0.1)
	//			fontrenderer.drawStringWithShadow(String.valueOf((int) (motionVec.length() * 72)),
	//					10, -5, 0xFFFFFF);
	//	}
	//	GL11.glPopMatrix();
//
//	//							System.out.println("debug" + turretObj.getName());
	//	if (displayCriterionTurret.getCurrentGuninfo().displayPredict) {
	//		if (displayCriterionTurret.getCurrentGuninfo().displayPredict_MoveSight) {
	//			GL11.glPushMatrix();
	//			rotationToVehicleNose(vehicle);
//
	//			Quat4d turretLooking = new Quat4d(logic.bodyRot);
	//			turretLooking.inverse();
	//			turretLooking.mul(displayCriterionTurret.motherRot);
	//			if (displayCriterionTurret.getCurrentGuninfo().displayPredict_ConsiderMyLooking)
	//				turretLooking.mul(displayCriterionTurret.turretRot);
//
	//			double[] xyz = eulerfromQuat((turretLooking));
	//			xyz[0] = toDegrees(xyz[0]);
	//			xyz[1] = toDegrees(xyz[1]);
	//			xyz[2] = toDegrees(xyz[2]);
	//			GL11.glRotated(-xyz[1], 0, 1, 0);
	//			GL11.glRotated(xyz[0], 1, 0, 0);
//	//									System.out.println("debug");
	//			Vector3d toTGT = new Vector3d(
	//					targetPos.posX - minecraft.renderViewEntity.posX,
	//					targetPos.posY - minecraft.renderViewEntity.posY,
	//					targetPos.posZ - minecraft.renderViewEntity.posZ);
	//			Vector3d motionVec = new Vector3d(
	//					minecraft.renderViewEntity.motionX - targetPos.motionX,
	//					minecraft.renderViewEntity.motionY - targetPos.motionY,
	//					minecraft.renderViewEntity.motionZ - targetPos.motionZ);
//	//								RotateVectorAroundY(motionVec, minecraft.renderViewEntity.rotationYawHead);
	//			RotateVectorAroundY(motionVec, -toDegrees(atan2(toTGT.x, toTGT.z)));
//	//								System.out.println("" + motionVec);
//	//								RotateVectorAroundX(motionVec, minecraft.renderViewEntity.rotationPitch);
	//			RotateVectorAroundX(motionVec, -toDegrees(atan2(toTGT.y, sqrt(toTGT.x * toTGT.x + toTGT.z * toTGT.z))));
//	//								System.out.println("" + motionVec);
	//			Vector3d vecTo_Target = new Vector3d(0, 0, toTGT.length());
	//			Vector3d PredictedTargetPos =
	//					LinePrediction(new Vector3d(),
	//							vecTo_Target,
	//							motionVec,
	//							displayCriterionTurret.getTerminalspeed());
//
	//			PredictedTargetPos.normalize();
	//			renderLockOnMarker(minecraft, displayCriterionTurret.getCurrentGuninfo().predictMarker, PredictedTargetPos);
	//			GL11.glPopMatrix();
	//		} else {
//	//									System.out.println("debug");
	//			Vector3d PredictedTargetPos =
	//					LinePrediction(new Vector3d(minecraft.renderViewEntity.posX,
	//									minecraft.renderViewEntity.posY,
	//									minecraft.renderViewEntity.posZ),
	//							new Vector3d(
	//									targetPos.posX,
	//									targetPos.posY,
	//									targetPos.posZ),
	//							new Vector3d(
	//									targetPos.motionX - minecraft.renderViewEntity.motionX,
	//									targetPos.motionY - minecraft.renderViewEntity.motionY,
	//									targetPos.motionZ - minecraft.renderViewEntity.motionZ),
	//							displayCriterionTurret.getTerminalspeed());
	//			PredictedTargetPos.sub(new Vector3d(
	//					minecraft.renderViewEntity.posX,
	//					minecraft.renderViewEntity.posY,
	//					minecraft.renderViewEntity.posZ));
	//			RotateVectorAroundY(vecToLockTargetPos, currentRotationYaw);
	//			RotateVectorAroundX(vecToLockTargetPos, currentRotationPit);
	//			renderLockOnMarker(minecraft, displayCriterionTurret.getCurrentGuninfo().predictMarker, PredictedTargetPos);
	//		}
	//	}
	//}

	public void displayFlyersHUD(BaseLogic logic,Quat4d prevbodyRot, Quat4d bodyRot, Entity plane, Vector3d prevmotionVec, RenderGameOverlayEvent.Post event){
		GL11.glColor4f(1,1,1,1);
		GL11.glAlphaFunc(GL11.GL_GREATER, 0);
		GL11.glEnable(GL_BLEND);
		Minecraft minecraft = FMLClientHandler.instance().getClient();
		FontRenderer fontrenderer = minecraft.fontRenderer;

		ScaledResolution scaledresolution = new ScaledResolution(minecraft, minecraft.displayWidth,
				minecraft.displayHeight);
		Quat4d tempquat = new Quat4d(
				prevbodyRot.x * (1-event.partialTicks) + bodyRot.x * event.partialTicks ,
				prevbodyRot.y * (1-event.partialTicks) + bodyRot.y * event.partialTicks ,
				prevbodyRot.z * (1-event.partialTicks) + bodyRot.z * event.partialTicks ,
				prevbodyRot.w * (1-event.partialTicks) + bodyRot.w * event.partialTicks );
		double[] xyz = eulerfromQuat((tempquat));
		xyz[0] = toDegrees(xyz[0]);
		xyz[1] = toDegrees(xyz[1]);
		xyz[2] = toDegrees(xyz[2]);


		GL11.glPushMatrix();
		double width = scaledresolution.getScaledWidth_double();
		double height = scaledresolution.getScaledHeight_double();
		//HUDは300×650
		//一度3.6px
		//横幅を合わせる
		double scale = width/300;
		float sizeW = (float) width;
		float sizeH = (float) height;
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDepthFunc(GL11.GL_LEQUAL);
		GL11.glDepthMask(true);
		GL11.glEnable(GL11.GL_CULL_FACE);
		minecraft.renderEngine.bindTexture(attitude_indicator_texture);
		GL11.glTranslatef(sizeW - sizeW/6,sizeH - sizeH/6, 0);
		GL11.glScalef(30,-30,30);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GUI_HUD_Cut(1,1);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		attitude_indicator.renderPart("obj2");
		GL11.glTranslatef(0,0, -3.0000f);
		GL11.glRotatef((float) xyz[2],0,0,1);
		GL11.glRotatef((float) -xyz[0]/5,1,0,0);
		GL11.glTranslatef(0,0, 3.0000f);
		attitude_indicator.renderPart("obj1");
		GL11.glDepthMask(false);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glPopMatrix();
	}
	public void GUI_HUD_Cut(float width,float height){
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glDepthMask(true);
		GL11.glDepthFunc(GL11.GL_ALWAYS);
		GL11.glColorMask(false,false,false,false);
		GL11.glBegin(GL11.GL_POLYGON);
		GL11.glVertex3d( width + 8	,  height + 8	, 1);
		GL11.glVertex3d(-width - 8	,  height + 8	, 1);
		GL11.glVertex3d(-width		,  height		, 1);
		GL11.glVertex3d( width		,  height		, 1);
		GL11.glEnd();

		GL11.glBegin(GL11.GL_POLYGON);
		GL11.glVertex3d(-width - 8	,  -height - 8	, 1);
		GL11.glVertex3d( width + 8	,  -height - 8	, 1);
		GL11.glVertex3d( width		,  -height		, 1);
		GL11.glVertex3d(-width		,  -height		, 1);
		GL11.glEnd();

		GL11.glBegin(GL11.GL_POLYGON);
		GL11.glVertex3d( width + 8	,  height + 8	, 1);
		GL11.glVertex3d( width		,  height		, 1);
		GL11.glVertex3d( width		,  -height		, 1);
		GL11.glVertex3d( width + 8	,  -height - 8	, 1);
		GL11.glEnd();

		GL11.glBegin(GL11.GL_POLYGON);
		GL11.glVertex3d( -width - 8	,  -height - 8	, 1);
		GL11.glVertex3d( -width		,  -height		, 1);
		GL11.glVertex3d( -width		,  height		, 1);
		GL11.glVertex3d( -width - 8	,  height + 8	, 1);
		GL11.glEnd();
		GL11.glColorMask(true,true,true,true);
		GL11.glDepthFunc(GL11.GL_LEQUAL);
	}

	public void displayFlyersHUD_AftGen2(BaseLogic logic,Quat4d prevbodyRot, Quat4d bodyRot, Entity plane, Vector3d prevmotionVec, RenderGameOverlayEvent.Post event){
		GL11.glAlphaFunc(GL11.GL_GREATER, 0);
		GL11.glEnable(GL_BLEND);
		Minecraft minecraft = FMLClientHandler.instance().getClient();
		FontRenderer fontrenderer = minecraft.fontRenderer;

		ScaledResolution scaledresolution = new ScaledResolution(minecraft, minecraft.displayWidth,
				minecraft.displayHeight);
		Quat4d tempquat = new Quat4d(
				prevbodyRot.x * (1-event.partialTicks) + bodyRot.x * event.partialTicks ,
				prevbodyRot.y * (1-event.partialTicks) + bodyRot.y * event.partialTicks ,
				prevbodyRot.z * (1-event.partialTicks) + bodyRot.z * event.partialTicks ,
				prevbodyRot.w * (1-event.partialTicks) + bodyRot.w * event.partialTicks );


		Vector3d forDisplayplaneMotion = logic.localMotionVec;

		GL11.glPushMatrix();
		double width = scaledresolution.getScaledWidth_double();
		double height = scaledresolution.getScaledHeight_double();
		//HUDは300×650
		//一度3.6px
		//横幅を合わせる
		double scale = width/300;
		double sizeW = width;
		double sizeH = sizeW * 650/300;
		double[] xyz = eulerfromQuat((tempquat));
		xyz[0] = toDegrees(xyz[0]);
		xyz[1] = toDegrees(xyz[1]);
		xyz[2] = toDegrees(xyz[2]);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.8F);

//		GL11.glRotatef((float) xyz[2],0,0,1);
//		GL11.glTranslatef((float)( forDisplayplaneMotion.x * angle * 3.6 * scale), (float)( forDisplayplaneMotion.y * angle * 3.6 * scale),0);
//		GL11.glRotatef((float) -xyz[2],0,0,1);

		GL11.glTranslatef((float)width/2,(float) height/2,0);
		GL11.glRotatef((float) -xyz[2],0,0,1);
		GL11.glTranslatef(-(float)width/2,-(float) height/2,0);

		minecraft.renderEngine.bindTexture(new ResourceLocation("handmadevehicle:textures/items/HUD.png"));
//		drawTexturedModalRect(0,height/2 - 325 * scale - (xyz[0] - toDegrees(asin(motionvecE.y))) * 1.8 * scale,sizeW,sizeH);
//		System.out.println(xyz[0]);
		double offset = -xyz[0] % 5;
		int currentLineNum = (int) ((xyz[0])/5);
//		if(offset<0)offset+=5;
//		if(xyz[0]>0)currentLineNum +=1;
		float lineWidh = 0.5f;
		float lineLength = (float) (sizeW/6);
		float midBlankLength = (float) (sizeW/24);
		float lineLength2 = 0.5f;
//		for(int num = 0;num + currentLineNum < 18&& num < 5;num ++){
//			drawHudLine(sizeW/2 + midBlankLength/2 + (lineLength - midBlankLength)/4 * 3 ,height/2 + offset * 3.6 * scale + num * 5 * 3.6 * scale,(lineLength - midBlankLength)/2,lineWidh * scale);
//			drawHudLine(sizeW/2 - midBlankLength/2 - (lineLength - midBlankLength)/4 * 3 ,height/2 + offset * 3.6 * scale + num * 5 * 3.6 * scale,(lineLength - midBlankLength)/2,lineWidh * scale);
//			drawHudLine(sizeW/2 + lineLength + lineWidh * scale/2,
//					height/2 + offset * 3.6 * scale + num * 5 * 3.6 * scale + (num + currentLineNum) * scale/2,
//					lineWidh * scale,
//					(num + currentLineNum) * scale * lineLength2);
//
//			fontrenderer.drawString(String.valueOf((num + currentLineNum) * 5),
//					(int)(sizeW/2 + lineLength + lineWidh * scale/2),
//					(int)(height/2 + offset * 3.6 * scale + num * 5 * 3.6 * scale + (num + currentLineNum) * scale/2),
//					0xFAFF9E);
//
//			drawHudLine(sizeW/2 - lineLength + lineWidh * scale/2,
//					height/2 + offset * 3.6 * scale + num * 5 * 3.6 * scale + (num + currentLineNum) * scale/2,
//					lineWidh * scale,
//					(num + currentLineNum) * scale * lineLength2);
//
//			fontrenderer.drawString(String.valueOf((num + currentLineNum) * 5),
//					(int)(sizeW/2 - lineLength + lineWidh * scale/2),
//					(int)(height/2 + offset * 3.6 * scale + num * 5 * 3.6 * scale + (num + currentLineNum) * scale/2),
//					0xFAFF9E);
//		}
		int num = -3;
		if(num + currentLineNum < -18){
			num = currentLineNum - 18;
		}
		for(;num + currentLineNum < 18 && num < 4;num ++){
			double lineupperPosY = height / 2 + offset * 3.6 * scale + num * 5 * 3.6 * scale;
			double lineunderPosY = height / 2 + offset * 3.6 * scale + num * 5 * 3.6 * scale + lineWidh * scale;
			drawHudLine(
					sizeW/2 + midBlankLength/2,
					lineupperPosY,
					sizeW/2 + lineLength/2,
					lineunderPosY);
			double lineunderPosY2 = lineupperPosY - (currentLineNum + num) * lineLength2 * scale;
			drawHudLine(
					sizeW/2 + lineLength/2,
					lineupperPosY,
					sizeW/2 + lineLength/2 - lineWidh * scale,
					lineunderPosY2);


			drawHudLine(
					sizeW/2 - lineLength/2,
					lineupperPosY,
					sizeW/2 - midBlankLength/2,
					lineunderPosY);
			drawHudLine(
					sizeW/2 - lineLength/2,
					lineupperPosY,
					sizeW/2 - lineLength/2 + lineWidh * scale,
					lineunderPosY2);

			double numberPosY = height / 2 + offset * 3.6 * scale + num * 5 * 3.6 * scale + (num + currentLineNum) * scale / 2;
			fontrenderer.drawString(String.valueOf((num + currentLineNum) * 5),
					(int)(sizeW/2 + lineLength + lineWidh * scale/2),
					(int) numberPosY,
					0xFAFF9E);

//			drawHudLine(sizeW/2 - lineLength + lineWidh * scale/2,
//					height/2 + offset * 3.6 * scale + num * 5 * 3.6 * scale + (num + currentLineNum) * scale/2,
//					lineWidh * scale,
//					(num + currentLineNum) * scale * lineLength2);

			fontrenderer.drawString(String.valueOf((num + currentLineNum) * 5),
					(int)(sizeW/2 - lineLength + lineWidh * scale/2),
					(int) numberPosY,
					0xFAFF9E);
		}
//		drawTexturedModalRect(0,height/2 - 325 * scale - xyz[0] * 3.6 * scale,sizeW,sizeH);
		GL11.glPopMatrix();

		GL11.glPushMatrix();
		scale = width/300;
		sizeW = width;
		sizeH = width;
		minecraft.renderEngine.bindTexture(new ResourceLocation("handmadevehicle:textures/items/HUD2.png"));

		drawTexturedModalRect(0,height/2 - 150 * scale,sizeW,sizeH);
		GL11.glPopMatrix();


		GL11.glPushMatrix();
		scale = width/300;
		sizeW = width;
		sizeH = width;

		GL11.glTranslatef((float)(width/2 + -forDisplayplaneMotion.x * 300 * scale), (float) (height/2 + (float)(-forDisplayplaneMotion.y * 300 * scale)),0);

		GL11.glScalef(0.1f,0.1f,0.1f);
		minecraft.renderEngine.bindTexture(new ResourceLocation("handmadevehicle:textures/items/HUD3.png"));
		drawTexturedModalRect(-sizeW/2,-sizeH/2,sizeW,sizeH);

		GL11.glPopMatrix();
	}
	/**
	 * Draws a textured rectangle at the stored z-value. Args: x, y, u, v, width, height
	 */
	public void drawTexturedModalRect(double p_73729_1_, double p_73729_2_, double p_73729_5_, double p_73729_6_)
	{
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV((double)(p_73729_1_), (double)(p_73729_2_ + p_73729_6_), (double)this.zLevel			, 0, 1);
		tessellator.addVertexWithUV((double)(p_73729_1_ + p_73729_5_), (double)(p_73729_2_ + p_73729_6_), (double)this.zLevel	, 1, 1);
		tessellator.addVertexWithUV((double)(p_73729_1_ + p_73729_5_), (double)(p_73729_2_), (double)this.zLevel			, 1, 0);
		tessellator.addVertexWithUV((double)(p_73729_1_), (double)(p_73729_2_), (double)this.zLevel						, 0, 0);
		tessellator.draw();
	}

	public void drawHudLine(double p_73729_1_, double p_73729_2_, double p_73729_5_, double p_73729_6_){
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_CULL_FACE);
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.setColorOpaque_I(0xFAFF9E);
		tessellator.addVertex((double)(p_73729_1_), (double)(p_73729_6_), (double)this.zLevel);
		tessellator.addVertex((double)(p_73729_5_), (double)(p_73729_6_), (double)this.zLevel);
		tessellator.addVertex((double)(p_73729_5_), (double)(p_73729_2_), (double)this.zLevel);
		tessellator.addVertex((double)(p_73729_1_), (double)(p_73729_2_), (double)this.zLevel);
		tessellator.draw();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}
	private TurretObj getPlayerControllingMainTurret(EntityPlayer entityplayer){
		SeatObject playerSeatObject = ((IVehicle) entityplayer.ridingEntity).getBaseLogic().seatObjects[playerSeatID];
		if (playerSeatObject.mainWeapon != null) {
			return playerSeatObject.mainWeapon[playerSeatObject.currentWeaponMode].getCriterionTurret();
		}
		return null;
	}
	private TurretObj getDisplayMainTurret(EntityPlayer entityplayer){
		SeatObject playerSeatObject = ((IVehicle) entityplayer.ridingEntity).getBaseLogic().seatObjects[playerSeatID];
		if (playerSeatObject.mainWeapon != null) {
			return playerSeatObject.mainWeapon[playerSeatObject.currentWeaponMode].getDisplayCriterionTurret();
		}
		return null;
	}

	private TurretObj getPlayerControllingSubTurret(EntityPlayer entityplayer){
		SeatObject playerSeatObject = ((IVehicle) entityplayer.ridingEntity).getBaseLogic().seatObjects[playerSeatID];
		if (playerSeatObject.subWeapon != null) {
			return playerSeatObject.subWeapon.getDisplayCriterionTurret();
		}
		return null;
	}

	private TurretObj getDisplaySubTurret(EntityPlayer entityplayer){
		SeatObject playerSeatObject = ((IVehicle) entityplayer.ridingEntity).getBaseLogic().seatObjects[playerSeatID];
		if (playerSeatObject.subWeapon != null) {
			return playerSeatObject.subWeapon.getDisplayCriterionTurret();
		}
		return null;
	}

	private static TurretObj getActiveTurret(TurretObj turretObj){
		if(turretObj != null && turretObj.gunStack != null){
			return turretObj;
		}
		return null;
	}
	private static TurretObj getActiveTurret2(TurretObj turretObj){

		GunInfo currentGunInfo = turretObj.getCurrentGuninfo();
		if(currentGunInfo != null){
//			System.out.println(turretObj.turretID_OnVehicle);
			return turretObj;
		}else{
			TurretObj temp;
			if(!turretObj.getChilds().isEmpty())for(TurretObj child:turretObj.getChilds()){
				temp = getActiveTurret(child);
				if(temp != null)
					return temp;
			}
			if(!turretObj.getChildsOnBarrel().isEmpty())for(TurretObj child:turretObj.getChildsOnBarrel()){
				temp = getActiveTurret(child);
				if(temp != null)
					return temp;
			}
		}
		return null;
	}

	public void rotationToVehicleNose(IVehicle vehicle){
		EntityLivingBase renderViewEntity = FMLClientHandler.instance().getClient().renderViewEntity;
		BaseLogic vehicleBaseLogic = vehicle.getBaseLogic();
		Vector3f next_ypr = new Vector3f(vehicleBaseLogic.bodyrotationYaw,vehicleBaseLogic.bodyrotationPitch,vehicleBaseLogic.bodyrotationRoll);
		Vector3f prev_ypr = new Vector3f(vehicleBaseLogic.prevbodyrotationYaw,vehicleBaseLogic.prevbodyrotationPitch,vehicleBaseLogic.prevbodyrotationRoll);
		Vector3f current_ypr = new Vector3f();
		current_ypr.interpolate(next_ypr,prev_ypr, HandmadeGunsCore.smooth);

		float roll = ObfuscationReflectionHelper.getPrivateValue(EntityRenderer.class, FMLClientHandler.instance().getClient().entityRenderer, "camRoll", "R", "field_78495_O");
		GL11.glRotatef(-roll,0,0,1);
		GL11.glRotatef(-(renderViewEntity.rotationPitch * (partialTicks) + renderViewEntity.prevRotationPitch * (1 - partialTicks)),1,0,0);
		GL11.glRotatef(renderViewEntity.rotationYawHead,0,1,0);
		GL11.glRotatef(-current_ypr.x,0,1,0);
		GL11.glRotatef(current_ypr.y,1,0,0);
		GL11.glRotatef(current_ypr.z,0,0,1);
	}

	public int renderFont(String string,int color,boolean dropShadow){
		Minecraft minecraft = FMLClientHandler.instance().getClient();
//		0xFFFFFF
		return minecraft.fontRenderer.drawString(string, 0, 0, color,dropShadow);
	}

	public int renderFont_getWidth(String string){
		Minecraft minecraft = FMLClientHandler.instance().getClient();
		return minecraft.fontRenderer.getStringWidth(string);
	}
	public Minecraft getMinecraft(){
		return FMLClientHandler.instance().getClient();
	}

	public static Vector3d entityCurrentPos(Entity entity){
		Vector3d vector3d = new Vector3d(
				entity.prevPosX + (entity.posX - entity.prevPosX) * partialTicks,
				entity.prevPosY + (entity.posY - entity.prevPosY) * partialTicks,
				entity.prevPosZ + (entity.posZ - entity.prevPosZ) * partialTicks);
		if(entity == FMLClientHandler.instance().getClient().renderViewEntity){
			vector3d.y += FMLClientHandler.instance().getClient().renderViewEntity.getEyeHeight() - FMLClientHandler.instance().getClient().renderViewEntity.yOffset;
		}
		return vector3d;
	}
	public static Vector3d entityPos(Entity entity){
		return new Vector3d(
				entity.prevPosX,
				entity.prevPosY,
				entity.prevPosZ);
	}

	//public static Vector3d entityCurrentPos(EntityLinkedPos_Motion entity){
	//	return new Vector3d(
	//			entity.posX + (entity.motionX) * partialTicks,
	//			entity.posY + (entity.motionY) * partialTicks,
	//			entity.posZ + (entity.motionZ) * partialTicks);
	//}
}
