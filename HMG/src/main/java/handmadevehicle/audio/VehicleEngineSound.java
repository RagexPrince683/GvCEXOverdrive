package handmadevehicle.audio;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import handmadeguns.Util.GunsUtils;
import handmadevehicle.entity.parts.HasLoopSound;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;

import javax.vecmath.Vector3d;

import static handmadeguns.HandmadeGunsCore.HMG_proxy;
import static handmadevehicle.HMVehicle.HMV_Proxy;
import static handmadevehicle.events.HMVRenderSomeEvent.entityCurrentPos;
import static handmadevehicle.events.HMVRenderSomeEvent.entityPos;
import static java.lang.Math.sqrt;

@SideOnly(Side.CLIENT)
public class VehicleEngineSound extends MovingSound
{
	private final Entity attachedEntity;
	private final HasLoopSound hasLoopSound;
	private float maxdist;
	private double disttoPlayer = -1;
	private final String sound;
	
	public VehicleEngineSound(Entity p_i45105_1_, float maxdist)
	{
		super(new ResourceLocation(((HasLoopSound) p_i45105_1_).getsound()));
		this.field_147666_i = AttenuationType.LINEAR;
		this.sound = ((HasLoopSound) p_i45105_1_).getsound();
		this.attachedEntity = p_i45105_1_;
		this.hasLoopSound = (HasLoopSound) p_i45105_1_;
		this.repeat = true;
		this.field_147665_h = 0;
		this.maxdist = maxdist;
		volume = 1;
		if(p_i45105_1_ == FMLClientHandler.instance().getClientPlayerEntity() || FMLClientHandler.instance().getClientPlayerEntity().getDistanceSqToEntity(p_i45105_1_) < 1){
			this.field_147666_i = ISound.AttenuationType.NONE;
		}
		update();
	}

	private boolean killer = false;
	/**
	 * Updates the JList with a new model.
	 */
	public void update()
	{
		if (this.attachedEntity.isDead || killer)
		{
			this.donePlaying = true;
		}
		else
		{
			hasLoopSound.yourSoundIsremain(sound);
			if(sound == hasLoopSound.getsound()) {//ŽQÆ“n‚µ‚¾‚©‚ç–â‘è‚È‚¢‚Í‚¸

				Entity renderViewEntity = HMG_proxy.getMCInstance().renderViewEntity;
				double prevdisttoPlayer = disttoPlayer;
				Vector3d entitycurrentPos = entityPos(this.attachedEntity);
				disttoPlayer = attachedEntity.getDistanceSqToEntity(HMG_proxy.getMCInstance().renderViewEntity);
				if(disttoPlayer>64) {
					this.xPosF = (float) (renderViewEntity.posX + (this.attachedEntity.posX - renderViewEntity.posX) / sqrt(disttoPlayer) * 8);
					this.yPosF = (float) (renderViewEntity.posY + (this.attachedEntity.posY + 2 - renderViewEntity.posY) / sqrt(disttoPlayer) * 8) + 1.5f;
					this.zPosF = (float) (renderViewEntity.posZ + (this.attachedEntity.posZ - renderViewEntity.posZ) / sqrt(disttoPlayer) * 8);
				}else {
					this.xPosF = (float) entitycurrentPos.x;
					this.yPosF = (float) entitycurrentPos.y + 1.5f;
					this.zPosF = (float) entitycurrentPos.z;
				}
				float soundpitch = hasLoopSound.getsoundPitch();
				this.field_147663_c = soundpitch;
				volume = 4;

				if (disttoPlayer < maxdist * maxdist) {

					if(disttoPlayer > 256){
						volume /=disttoPlayer/256;
					}
					if(!hasLoopSound.getCanSeeFlag())volume /=32;
					if (prevdisttoPlayer != -1) {
						float doppler = (float) (sqrt(prevdisttoPlayer) - sqrt(disttoPlayer));
						float tempsp = (318.8f / (318.8f - doppler * 20f));
						field_147663_c = soundpitch * tempsp;
					}
					if (field_147663_c < 0.01) {
						this.field_147663_c = 0.0F;
						this.volume = 0.0F;
					}
				} else {
					this.field_147663_c = 0.0F;
					this.volume = 0.0F;
				}
			}else {
				killer = true;
			}
		}
	}

	public void init()
	{
		{
			{

				Entity renderViewEntity = HMG_proxy.getMCInstance().renderViewEntity;
				double prevdisttoPlayer = disttoPlayer;
				Vector3d entitycurrentPos = entityPos(this.attachedEntity);
				disttoPlayer = attachedEntity.getDistanceSqToEntity(HMG_proxy.getMCInstance().renderViewEntity);
				if(disttoPlayer>64) {
					Vector3d renderViewEntityCurrentPos = entityPos(renderViewEntity);
					this.xPosF = (float) (renderViewEntityCurrentPos.x + (entitycurrentPos.x - renderViewEntityCurrentPos.x) / sqrt(disttoPlayer) * 8);
					this.yPosF = (float) (renderViewEntityCurrentPos.y + (entitycurrentPos.y - renderViewEntityCurrentPos.y) / sqrt(disttoPlayer) * 8);
					this.zPosF = (float) (renderViewEntityCurrentPos.z + (entitycurrentPos.z - renderViewEntityCurrentPos.z) / sqrt(disttoPlayer) * 8);
				}else {
					this.xPosF = (float) entitycurrentPos.x;
					this.yPosF = (float) entitycurrentPos.y;
					this.zPosF = (float) entitycurrentPos.z;
				}
				float soundpitch = hasLoopSound.getsoundPitch();
				this.field_147663_c = soundpitch;
				volume = 4;

				if (disttoPlayer < maxdist * maxdist) {

					if(disttoPlayer > 256){
						volume /=disttoPlayer/256;
					}
					if(!hasLoopSound.getCanSeeFlag())volume /=disttoPlayer;
					if (prevdisttoPlayer != -1) {
						float doppler = (float) (sqrt(prevdisttoPlayer) - sqrt(disttoPlayer));
						float tempsp = (318.8f / (318.8f - doppler * 20f));
						field_147663_c = soundpitch * tempsp;
					}
					if (field_147663_c < 0.01) {
						this.field_147663_c = 0.0F;
						this.volume = 0.0F;
					}
				} else {
					this.field_147663_c = 0.0F;
					this.volume = 0.0F;
				}
			}
		}
	}
}