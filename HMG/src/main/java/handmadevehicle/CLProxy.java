package handmadevehicle;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import handmadeguns.KeyBinding_mod;
import handmadeguns.client.render.ModelSetAndData;
import handmadevehicle.audio.TurretSound;
import handmadevehicle.audio.VehicleEngineSound;
import handmadevehicle.audio.VehicleNoRepeatSound;
import handmadevehicle.entity.EntityVehicle;
//import handmadevehicle.entity.parts.HasLoopSound;
import handmadevehicle.entity.parts.ModifiedBoundingBox;
import handmadevehicle.entity.parts.OBB;
import handmadevehicle.entity.parts.turrets.TurretObj;
import handmadevehicle.events.HMVRenderSomeEvent;
import handmadevehicle.render.RenderVehicle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Controller;
import org.lwjgl.input.Controllers;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import static handmadevehicle.HMVehicle.*;
import static handmadevehicle.events.HMVRenderSomeEvent.playerSeatID;
import static java.lang.Math.toDegrees;
import static org.lwjgl.input.Keyboard.isKeyDown;

public class CLProxy extends CMProxy {
	public static final KeyBinding_mod RButton				= new KeyBinding_mod("Fire1", -99, "HMVehicle");
	public static final KeyBinding_mod LButton 				= new KeyBinding_mod("Fire2", -100, "HMVehicle");
	public static final KeyBinding_mod Throttle_up 			= new KeyBinding_mod("throttle UP", Keyboard.KEY_W, "HMVehicle");
	public static final KeyBinding_mod Throttle_down 		= new KeyBinding_mod("throttle Down", Keyboard.KEY_S, "HMVehicle");
	public static final KeyBinding_mod Yaw_Left 			= new KeyBinding_mod("Yaw Left", Keyboard.KEY_A, "HMVehicle");
	public static final KeyBinding_mod Yaw_Right 			= new KeyBinding_mod("Yaw Right", Keyboard.KEY_D, "HMVehicle");
	public static final KeyBinding_mod Throttle_Brake 		= new KeyBinding_mod("Throttle Brake", Keyboard.KEY_SPACE, "HMVehicle");


	public static final KeyBinding_mod Zoom 				= new KeyBinding_mod("CannonCamera", Keyboard.KEY_Z, "HMVehicle");
	
	public static final KeyBinding_mod Flap 				= new KeyBinding_mod("Flap", Keyboard.KEY_F, "HMVehicle");
	public static final KeyBinding_mod Air_Brake 			= new KeyBinding_mod("Air Brake/Wheel Brake", Keyboard.KEY_X, "HMVehicle");
	public static final KeyBinding_mod Flare_Smoke 			= new KeyBinding_mod("Flare/Smoke", Keyboard.KEY_COLON, "HMVehicle");
	public static final KeyBinding_mod Gear_Down_Up 		= new KeyBinding_mod("Gear Down/Up", Keyboard.KEY_G, "HMVehicle");
	public static final KeyBinding_mod Weapon_Mode 			= new KeyBinding_mod("Weapon Mode", Keyboard.KEY_ADD, "HMVehicle");
	public static final KeyBinding_mod Allow_Entity_Ride 	= new KeyBinding_mod("Allow Entity to Ride", Keyboard.KEY_LMENU, "HMVehicle");


	public static final KeyBinding_mod Next_Seat 			= new KeyBinding_mod("Change to Next Seat", Keyboard.KEY_Y, "HMVehicle");
	public static final KeyBinding_mod Previous_Seat 		= new KeyBinding_mod("Change to Previous Seat", Keyboard.KEY_H, "HMVehicle");
	public static final KeyBinding_mod ChangeControl 		= new KeyBinding_mod("Change Control", Keyboard.KEY_N, "HMVehicle");
	public static final KeyBinding_mod ChangeEasyControl 	= new KeyBinding_mod("Change to Easy/Normal Control", Keyboard.KEY_NONE, "HMVehicle");
	public static final KeyBinding_mod resetCamrot 			= new KeyBinding_mod("Reset Camera Rotation", Keyboard.KEY_V, "HMVehicle");
	public static final KeyBinding_mod reloadConfig 		= new KeyBinding_mod("Reload Config Settings", Keyboard.KEY_NONE, "HMVehicle");
	public static final KeyBinding_mod openGUI 				= new KeyBinding_mod("Open Vehicle Gui", Keyboard.KEY_SEMICOLON, "HMVehicle");
	
	public static final KeyBinding_mod pitchUp 							= new KeyBinding_mod("Pitch Up/Sus Up", Keyboard.KEY_I, "HMVehicle");
	public static final KeyBinding_mod pitchDown 						= new KeyBinding_mod("Pitch Down/Sus Down", Keyboard.KEY_K, "HMVehicle");
	public static final KeyBinding_mod RollRight 						= new KeyBinding_mod("Roll Right/Sus Right", Keyboard.KEY_L, "HMVehicle");
	public static final KeyBinding_mod RollLeft 						= new KeyBinding_mod("Roll Left/Sus Right", Keyboard.KEY_J, "HMVehicle");
	static boolean reload_stopper;

	static boolean inited = false;
	static int currentStickControllerID;

	public CLProxy() {
		//todo comment out shitlow code
		if(!inited) {
			net.minecraftforge.client.ClientCommandHandler.instance.registerCommand(hmv_commandReloadparm);
			ClientRegistry.registerKeyBinding(Throttle_up.keyBinding);
			ClientRegistry.registerKeyBinding(Throttle_down.keyBinding);
			ClientRegistry.registerKeyBinding(Yaw_Left.keyBinding);
			ClientRegistry.registerKeyBinding(Yaw_Right.keyBinding);
			ClientRegistry.registerKeyBinding(Throttle_Brake.keyBinding);
			ClientRegistry.registerKeyBinding(RButton.keyBinding);
			ClientRegistry.registerKeyBinding(LButton.keyBinding);
			ClientRegistry.registerKeyBinding(Zoom.keyBinding);
			ClientRegistry.registerKeyBinding(Flap.keyBinding);
			ClientRegistry.registerKeyBinding(Air_Brake.keyBinding);
			ClientRegistry.registerKeyBinding(Flare_Smoke.keyBinding);
			ClientRegistry.registerKeyBinding(Next_Seat.keyBinding);
			ClientRegistry.registerKeyBinding(Previous_Seat.keyBinding);
			ClientRegistry.registerKeyBinding(pitchUp.keyBinding);
			ClientRegistry.registerKeyBinding(pitchDown.keyBinding);
			ClientRegistry.registerKeyBinding(RollRight.keyBinding);
			ClientRegistry.registerKeyBinding(RollLeft.keyBinding);
			ClientRegistry.registerKeyBinding(Weapon_Mode.keyBinding);
			ClientRegistry.registerKeyBinding(Allow_Entity_Ride.keyBinding);
			ClientRegistry.registerKeyBinding(ChangeControl.keyBinding);
			ClientRegistry.registerKeyBinding(ChangeEasyControl.keyBinding);
			ClientRegistry.registerKeyBinding(resetCamrot.keyBinding);
			ClientRegistry.registerKeyBinding(reloadConfig.keyBinding);
			ClientRegistry.registerKeyBinding(openGUI.keyBinding);
			ClientRegistry.registerKeyBinding(Gear_Down_Up.keyBinding);
			inited = true;
			
			RenderingRegistry.registerEntityRenderingHandler(EntityVehicle.class,new RenderVehicle());
		}
		try {
			Controllers.create();
		} catch (LWJGLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public ModelSetAndData loadResource_model(String resourceName_model,String resourceName_Texture,float scale){
		return new ModelSetAndData(AdvancedModelLoader.loadModel(new ResourceLocation("handmadevehicle:textures/model/" + resourceName_model)),new ResourceLocation("handmadevehicle:textures/model/" + resourceName_Texture),scale);
	}
	
	//@Override
	//public void playsoundasVehicle(float maxdist, Entity attached){
	//	Minecraft.getMinecraft().getSoundHandler().playSound(new VehicleEngineSound(attached,maxdist));
	//}
	@Override
	public void playsoundasTurret(float maxdist, TurretObj attached){
		Minecraft.getMinecraft().getSoundHandler().playSound(new TurretSound(attached,maxdist));
	}
	//@Override
	//public void playsoundasVehicle_noRepeat(String name , float maxdist, Entity attached, HasLoopSound hasLoopSound,int time){
	//	Minecraft.getMinecraft().getSoundHandler().playSound(new VehicleNoRepeatSound(name,attached,hasLoopSound,maxdist,time));
	//}
	public boolean hasStick(){
		return Controllers.getControllerCount() > 0;
	}
	
	public float getXaxis(){
		if(Controllers.getControllerCount() > 0){
			Controller stick = Controllers.getController(currentStickControllerID);
			if(stick != null && stick.getAxisCount() > cfgControl_axisXID)return stick.getAxisValue(cfgControl_axisXID);
			else currentStickControllerID++;
		}
		return 0;
	}
	public float getYaxis(){
		if(Controllers.getControllerCount() > 0){
			Controller stick = Controllers.getController(currentStickControllerID);
			if(stick != null && stick.getAxisCount() > cfgControl_axisYID)return stick.getAxisValue(cfgControl_axisYID);
			else currentStickControllerID++;
		}
		return 0;
	}
	public float getZaxis(){
		if(Controllers.getControllerCount() > 0){
			Controller stick = Controllers.getController(currentStickControllerID);
			if(stick != null && stick.getAxisCount() > cfgControl_axisZID)return stick.getAxisValue(cfgControl_axisZID);
			else currentStickControllerID++;
		}
		return 0;
	}
	public float getZaxis2(){
		if(Controllers.getControllerCount() > 0){
			Controller stick = Controllers.getController(currentStickControllerID);
			if(stick != null && stick.getAxisCount() > cfgControl_axisZ2ID)return stick.getAxisValue(cfgControl_axisZ2ID);
			else currentStickControllerID++;
		}
		return 0;
	}
	public boolean pitchUp(){
		return pitchUp.isKeyDown_noStop();
	}
	public boolean pitchDown() {
		return pitchDown.isKeyDown_noStop();
	}
	public boolean rollRight(){
		return RollRight.isKeyDown_noStop();
	}
	public boolean rollLeft(){
		return RollLeft.isKeyDown_noStop();
	}
	
	@Override
	public boolean reload(){
		//return Minecraft.getMinecraft().gameSettings.keyBindJump.isKeyDown();
//		return (Keyboard.KEY_R);
		//return false;
		return false;
	}
	
	@Override
	public boolean reload_Semi(){
		
		boolean flag = isKeyDown(Keyboard.KEY_R);
		if(flag){
			if(!reload_stopper) {
				reload_stopper = true;
			}else {
				flag = false;
			}
		}else {
			reload_stopper = false;
		}
		return flag;
	}
	@Override
	public boolean throttle_BrakeKeyDown(){
		return (Throttle_Brake).isKeyDown_noStop();
		//return false;
	}
	
	@Override
	public boolean leftclick(){
		return (LButton).isKeyDown_noStop();
		//return false;
	}
	@Override
	public boolean rightclick(){
		return (RButton).isKeyDown_noStop();
		//return false;
	}
	@Override
	public boolean throttle_up_click(){
		return (Throttle_up).isKeyDown_noStop();
		//return false;
	}
	@Override
	public boolean yaw_Left_click(){
		return (Yaw_Left).isKeyDown_noStop();
		//return false;
	}
	@Override
	public boolean throttle_down_click(){
		return (Throttle_down).isKeyDown_noStop();
		//return false;
	}
	@Override
	public boolean yaw_Right_click(){
		return (Yaw_Right).isKeyDown_noStop();
		//return false;
	}
	@Override
	public boolean zoomclick(){
		return (Zoom).isKeyDown_toggle();
		//return false;
	}
	@Override
	public boolean flap_click(){
		return (Flap).isKeyDown_toggle();
		//return false;
	}
	@Override
	public boolean air_Brake_click(){
		return (Air_Brake).isKeyDown_noStop();
		//return false;
	}
	@Override
	public boolean flare_Smoke_click(){
		return (Flare_Smoke).isKeyDown_noStop();
	}
	@Override
	public boolean gear_Down_Up_click(){
		return Gear_Down_Up.isKeyDown_toggle();
	}
	@Override
	public boolean weapon_Mode_click() {
		return Weapon_Mode.isKeyDown_withStopper();
	}
	@Override
	public boolean allow_Entity_Ride_click(){
		return Allow_Entity_Ride.isKeyDown_withStopper();
	}

	@Override
	public boolean next_Seatclick(){
		return Next_Seat.isKeyDown_withStopper();
	}
	@Override
	public boolean previous_Seatclick(){
		return Previous_Seat.isKeyDown_withStopper();
	}
	@Override
	public boolean changeControlclick(){
		return ChangeControl.isKeyDown_withStopper();
	}
	@Override
	public boolean changeEasyControlMode(){
		return ChangeEasyControl.isKeyDown_withStopper();
	}
	@Override
	public boolean resetCamrotclick(){
		return resetCamrot.isKeyDown_withStopper();
	}
	@Override
	public boolean reloadConfigclick(){
		return reloadConfig.isKeyDown_withStopper();
	}
	@Override
	public boolean openGUIKeyDown(){
		return openGUI.isKeyDown_withStopper();
	}

	@Override
	public boolean iszooming(){
		return Zoom.toggleState();
	}
	
	
	public EntityPlayer getEntityPlayerInstance() {
		return Minecraft.getMinecraft().thePlayer;
	}
	
	
	public static void drawOutlinedBoundingBox(ModifiedBoundingBox p_147590_0_, int p_147590_1_)
	{
		
		GL11.glPushMatrix();
		for(OBB aobb : p_147590_0_.boxes){
			if(aobb != null)drawOutlinedOBB(aobb,p_147590_1_);
		}
		GL11.glPopMatrix();
	}
	
	public void setPlayerSeatID(int id){
		playerSeatID = id;
	}
	
	public static void drawOutlinedOBB(OBB p_147590_0_, int p_147590_1_)
	{
		GL11.glPushMatrix();

		{
			double[] xyz = Utils.eulerfromQuat(p_147590_0_.turretRotation);
			xyz[0] = toDegrees(xyz[0]);
			xyz[1] = toDegrees(xyz[1]);
			xyz[2] = toDegrees(xyz[2]);
			GL11.glTranslatef((float) p_147590_0_.turretRotCenter.x, (float) p_147590_0_.turretRotCenter.y, (float) -p_147590_0_.turretRotCenter.z);

			GL11.glRotatef(-(float) xyz[1], 0.0F, 1.0F, 0.0F);
			GL11.glRotatef((float) xyz[0], 1.0F, 0.0F, 0.0F);
			GL11.glRotatef((float) xyz[2], 0.0F, 0.0F, 1.0F);

			GL11.glTranslatef(-(float) p_147590_0_.turretRotCenter.x, -(float) p_147590_0_.turretRotCenter.y, (float) p_147590_0_.turretRotCenter.z);
		}

		{
			double[] xyz = Utils.eulerfromQuat((p_147590_0_.info.boxRotation));
			xyz[0] = toDegrees(xyz[0]);
			xyz[1] = toDegrees(xyz[1]);
			xyz[2] = toDegrees(xyz[2]);
			GL11.glTranslatef((float) p_147590_0_.info.boxRotCenter.x, (float) p_147590_0_.info.boxRotCenter.y, (float) -p_147590_0_.info.boxRotCenter.z);

			GL11.glRotatef(-(float) xyz[1], 0.0F, 1.0F, 0.0F);
			GL11.glRotatef((float) xyz[0], 1.0F, 0.0F, 0.0F);
			GL11.glRotatef((float) xyz[2], 0.0F, 0.0F, 1.0F);

			GL11.glTranslatef(-(float) p_147590_0_.info.boxRotCenter.x, -(float) p_147590_0_.info.boxRotCenter.y, (float) p_147590_0_.info.boxRotCenter.z);
		}
//		p_147590_0_.info.model_forHitChecks.render();

		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawing(3);
		
		if (p_147590_1_ != -1)
		{
			tessellator.setColorOpaque_I(p_147590_1_);
		}
		
		tessellator.addVertex(p_147590_0_.minvertex.x, p_147590_0_.minvertex.y, -p_147590_0_.minvertex.z);
		tessellator.addVertex(p_147590_0_.maxvertex.x, p_147590_0_.minvertex.y, -p_147590_0_.minvertex.z);
		tessellator.addVertex(p_147590_0_.maxvertex.x, p_147590_0_.minvertex.y, -p_147590_0_.maxvertex.z);
		tessellator.addVertex(p_147590_0_.minvertex.x, p_147590_0_.minvertex.y, -p_147590_0_.maxvertex.z);
		tessellator.addVertex(p_147590_0_.minvertex.x, p_147590_0_.minvertex.y, -p_147590_0_.minvertex.z);
		tessellator.draw();
		tessellator.startDrawing(3);
		
		if (p_147590_1_ != -1)
		{
			tessellator.setColorOpaque_I(p_147590_1_);
		}
		
		tessellator.addVertex(p_147590_0_.minvertex.x, p_147590_0_.maxvertex.y, -p_147590_0_.minvertex.z);
		tessellator.addVertex(p_147590_0_.maxvertex.x, p_147590_0_.maxvertex.y, -p_147590_0_.minvertex.z);
		tessellator.addVertex(p_147590_0_.maxvertex.x, p_147590_0_.maxvertex.y, -p_147590_0_.maxvertex.z);
		tessellator.addVertex(p_147590_0_.minvertex.x, p_147590_0_.maxvertex.y, -p_147590_0_.maxvertex.z);
		tessellator.addVertex(p_147590_0_.minvertex.x, p_147590_0_.maxvertex.y, -p_147590_0_.minvertex.z);
		tessellator.draw();
		tessellator.startDrawing(1);
		
		if (p_147590_1_ != -1)
		{
			tessellator.setColorOpaque_I(p_147590_1_);
		}
		
		tessellator.addVertex(p_147590_0_.minvertex.x, p_147590_0_.minvertex.y, -p_147590_0_.minvertex.z);
		tessellator.addVertex(p_147590_0_.minvertex.x, p_147590_0_.maxvertex.y, -p_147590_0_.minvertex.z);
		tessellator.addVertex(p_147590_0_.maxvertex.x, p_147590_0_.minvertex.y, -p_147590_0_.minvertex.z);
		tessellator.addVertex(p_147590_0_.maxvertex.x, p_147590_0_.maxvertex.y, -p_147590_0_.minvertex.z);
		tessellator.addVertex(p_147590_0_.maxvertex.x, p_147590_0_.minvertex.y, -p_147590_0_.maxvertex.z);
		tessellator.addVertex(p_147590_0_.maxvertex.x, p_147590_0_.maxvertex.y, -p_147590_0_.maxvertex.z);
		tessellator.addVertex(p_147590_0_.minvertex.x, p_147590_0_.minvertex.y, -p_147590_0_.maxvertex.z);
		tessellator.addVertex(p_147590_0_.minvertex.x, p_147590_0_.maxvertex.y, -p_147590_0_.maxvertex.z);
		tessellator.draw();
		GL11.glPopMatrix();
	}
	public boolean isSneaking(){
		return getEntityPlayerInstance() != null && ((EntityPlayerSP) getEntityPlayerInstance()).movementInput.sneak;
	}

	public int clientPlayerSeatID(){
		return playerSeatID;
	}
}
