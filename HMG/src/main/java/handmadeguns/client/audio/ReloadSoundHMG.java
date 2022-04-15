package handmadeguns.client.audio;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import javax.vecmath.Vector3d;

import static handmadeguns.HandmadeGunsCore.HMG_proxy;
import static handmadevehicle.events.HMVRenderSomeEvent.entityCurrentPos;
import static handmadevehicle.events.HMVRenderSomeEvent.entityPos;
import static java.lang.Math.sqrt;

@SideOnly(Side.CLIENT)
public class ReloadSoundHMG extends MovingSound
{
	private final Entity attachedEntity;
	private static final String __OBFID = "CL_00001118";

	private int prevslot;

	public ReloadSoundHMG(Entity p_i45105_1_, String soundName, boolean repeat, float soundLV, float soundSP)
	{
		super(new ResourceLocation(soundName));
		this.attachedEntity = p_i45105_1_;
		this.repeat = repeat;
		this.field_147665_h = 0;
		savedfield_147663_c = this.field_147663_c = soundSP;
		this.volume = soundLV;
		if(p_i45105_1_ instanceof EntityPlayer)
			prevslot = ((EntityPlayer) p_i45105_1_).inventory.currentItem;
		if(p_i45105_1_ == FMLClientHandler.instance().getClientPlayerEntity() || FMLClientHandler.instance().getClientPlayerEntity().getDistanceSqToEntity(p_i45105_1_) < 1){
			this.field_147666_i = ISound.AttenuationType.NONE;
		}
	}

	private double disttoPlayer = -1;
	private float savedfield_147663_c;
	/**
	 * Updates the JList with a new model.
	 */
	public void update()
	{
		if (
				this.attachedEntity.isDead ||
						(attachedEntity instanceof EntityPlayer &&
								(prevslot != ((EntityPlayer) attachedEntity).inventory.currentItem ||
										(((EntityPlayer) attachedEntity).getHeldItem() != null && ((EntityPlayer) attachedEntity).getHeldItem().getTagCompound() != null && (((EntityPlayer) attachedEntity).getHeldItem().getTagCompound().getBoolean("CannotReload"))))))
		{
			this.donePlaying = true;
			this.repeat = false;
		}
		else
		{
		}

		if (
				this.attachedEntity.isDead ||
						(attachedEntity instanceof EntityPlayer &&
								(prevslot != ((EntityPlayer) attachedEntity).inventory.currentItem ||
										(((EntityPlayer) attachedEntity).getHeldItem() != null && ((EntityPlayer) attachedEntity).getHeldItem().getTagCompound() != null && (((EntityPlayer) attachedEntity).getHeldItem().getTagCompound().getBoolean("CannotReload"))))))
		{
			this.donePlaying = true;
			this.repeat = false;
		}
		else
		{
			Vector3d entitycurrentPos = entityCurrentPos(this.attachedEntity);
			this.xPosF = (float) entitycurrentPos.x;
			this.yPosF = (float) entitycurrentPos.y;
			this.zPosF = (float) entitycurrentPos.z;
			Entity renderViewEntity = HMG_proxy.getMCInstance().renderViewEntity;
			double prevdisttoPlayer = disttoPlayer;
			disttoPlayer = HMG_proxy.getMCInstance().renderViewEntity.getDistanceSq(this.xPosF,
					this.yPosF,
					this.zPosF);
			if(attachedEntity != FMLClientHandler.instance().getClientPlayerEntity()) {
				if (disttoPlayer > 64) {
					this.xPosF = (float) (entityCurrentPos(renderViewEntity).x + (this.attachedEntity.posX - renderViewEntity.posX) / sqrt(disttoPlayer) * 8);
					this.yPosF = (float) (entityCurrentPos(renderViewEntity).y + (this.attachedEntity.posY - renderViewEntity.posY) / sqrt(disttoPlayer) * 8);
					this.zPosF = (float) (entityCurrentPos(renderViewEntity).z + (this.attachedEntity.posZ - renderViewEntity.posZ) / sqrt(disttoPlayer) * 8);
				} else if (disttoPlayer < 1) {
					this.xPosF = (float) (entityCurrentPos(renderViewEntity).x);
					this.yPosF = (float) (entityCurrentPos(renderViewEntity).y);
					this.zPosF = (float) (entityCurrentPos(renderViewEntity).z);
				} else {
					this.xPosF = (float) (entityCurrentPos(this.attachedEntity).x);
					this.yPosF = (float) (entityCurrentPos(this.attachedEntity).y);
					this.zPosF = (float) (entityCurrentPos(this.attachedEntity).z);
				}
				float soundpitch = savedfield_147663_c;
				this.field_147663_c = soundpitch;

				if (disttoPlayer > 1024) {
					volume /= disttoPlayer / 1024;
				}
			}else {

			}
		}
	}
}