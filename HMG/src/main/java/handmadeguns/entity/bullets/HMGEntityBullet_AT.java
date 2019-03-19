package handmadeguns.entity.bullets;

//import littleMaidMobX.LMM_EntityLittleMaid;
//import littleMaidMobX.LMM_EntityLittleMaidAvatar;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import handmadeguns.HMGMessageKeyPressedC;
import handmadeguns.HMGPacketHandler;
import handmadeguns.HandmadeGunsCore;
import handmadeguns.entity.IFF;
import io.netty.buffer.ByteBuf;
import littleMaidMobX.LMM_EntityLittleMaid;
import littleMaidMobX.LMM_EntityLittleMaidAvatar;
import littleMaidMobX.LMM_EntityLittleMaidAvatarMP;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import static handmadeguns.HandmadeGunsCore.islmmloaded;

public class HMGEntityBullet_AT extends HMGEntityBulletBase implements IEntityAdditionalSpawnData
{
	public HMGEntityBullet_AT(World worldIn) {
		super(worldIn);
	}

	public HMGEntityBullet_AT(World worldIn, Entity throwerIn, int damege, float bspeed, float bure, String modelname) {
		super(worldIn, throwerIn, damege, bspeed, bure,modelname);
	}

	public HMGEntityBullet_AT(World worldIn, double x, double y, double z) {
		super(worldIn, x, y, z);
	}


	/**
	 * Called when this EntityThrowable hits a block or entity.
	 */
	protected void onImpact(MovingObjectPosition var1)
	{
		super.onImpact(var1);
		if (var1.entityHit != null)
		{
			int var2 = 1;

			if(islmmloaded&&(this.thrower instanceof LMM_EntityLittleMaid || this.thrower instanceof LMM_EntityLittleMaidAvatar || this.thrower instanceof LMM_EntityLittleMaidAvatarMP) && HandmadeGunsCore.cfg_FriendFireLMM){
				if (var1.entityHit instanceof LMM_EntityLittleMaid)
				{
					var2 = 0;
				}
				if (var1.entityHit instanceof LMM_EntityLittleMaidAvatar)
				{
					var2 = 0;
				}
				if (var1.entityHit instanceof EntityPlayer)
				{
					var2 = 0;
				}
			}
			if(this.thrower instanceof IFF){
				if(((IFF) this.thrower).is_this_entity_friend(var1.entityHit)){
					var2 = 0;
				}
			}
			var1.entityHit.hurtResistantTime = 0;

			double moXback = var1.entityHit.motionX;//ノックバック無効化用
			double moYback = var1.entityHit.motionY;//跳ね上がり無効化用
			double moZback = var1.entityHit.motionZ;//ノックバック無効化用
			if(var1.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, this.getThrower()), (float)var2)){
				var1.entityHit.motionX = moXback;
				var1.entityHit.motionY = moYback;
				var1.entityHit.motionZ = moZback;
				Vec3 knockvec = this.getLook((float) knockbackXZ,-this.rotationYaw,-this.rotationPitch);
				if(var1.entityHit instanceof EntityLivingBase){
					if(this.rand.nextDouble() >= ((EntityLivingBase)var1.entityHit).getEntityAttribute(SharedMonsterAttributes.knockbackResistance).getAttributeValue()){
						var1.entityHit.isAirBorne = true;
						var1.entityHit.motionX += knockvec.xCoord;
						var1.entityHit.motionY += knockvec.yCoord + knockbackY;
						var1.entityHit.motionZ += knockvec.zCoord;
					}
				}
			}
			if(var1.entityHit instanceof EntityLivingBase){
				EntityLivingBase lebl = (EntityLivingBase)var1.entityHit;
				lebl.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id,60 * 10, 10));
				lebl.addPotionEffect(new PotionEffect(Potion.blindness.id,60 * 10, 10));
			}


            /*Entity lel = (Entity)var1.entityHit;

			lel.addVelocity((motionX/20D),
							(-motionY-2D),
							(motionZ/20D));*/
			if (!this.worldObj.isRemote)
			{
				if(this.getThrower() != null&&getThrower() instanceof EntityPlayerMP){
					HMGPacketHandler.INSTANCE.sendTo(new HMGMessageKeyPressedC(10, this.getThrower().getEntityId()),(EntityPlayerMP)this.getThrower());
				}
				this.setDead();
				//this.explode();
			}
		}else {
			Block lblock = worldObj.getBlock(var1.blockX, var1.blockY, var1.blockZ);
			int lmeta = worldObj.getBlockMetadata(var1.blockX, var1.blockY, var1.blockZ);
			if (checkDestroyBlock(var1, var1.blockX, var1.blockY, var1.blockZ, lblock, lmeta)) {
				if (!this.worldObj.isRemote)
				{
					onBreakBlock(var1, var1.blockX, var1.blockY, var1.blockZ, lblock, lmeta);
				}
			} else {
				// 雋ｫ騾壹〒縺阪↑縺九▲縺�
				// 逹�ｼｾ繝代�繝�ぅ繧ｯ繝ｫ
				for (int i = 0; i < 8; ++i) {
//					worldObj.spawnParticle("snowballpoof", this.posX, this.posY,
					worldObj.spawnParticle("smoke",
							var1.hitVec.xCoord, var1.hitVec.yCoord, var1.hitVec.zCoord,
							0.0D, 0.0D, 0.0D);
				}
			}
			Block block = this.worldObj.getBlock(var1.blockX,
					var1.blockY,
					var1.blockZ);
			if(!block.isAir(worldObj,var1.blockX,
					var1.blockY,
					var1.blockZ)) {
				worldObj.playSoundEffect((float) var1.hitVec.xCoord, (float) var1.hitVec.yCoord, (float) var1.hitVec.zCoord, new ResourceLocation(block.stepSound.getStepResourcePath()).getResourcePath(), (block.stepSound.getVolume() + 1.0F) / 2.0F, block.stepSound.getPitch() * 0.8F);
				this.worldObj.spawnParticle("blockcrack_" + Block.getIdFromBlock(block) + "_" +
								this.worldObj.getBlockMetadata(var1.blockX,
										var1.blockY,
										var1.blockZ)
						, var1.hitVec.xCoord, var1.hitVec.yCoord, var1.hitVec.zCoord, 4.0D * ((double) this.rand.nextFloat() - 0.5D), 0.5D, ((double) this.rand.nextFloat() - 0.5D) * 4.0D);
			}
			if (!this.worldObj.isRemote)
			{
				if(!this.canbounce) this.setDead();
				//this.explode();
			}
		}
	}
	public void writeSpawnData(ByteBuf buffer){
		super.writeSpawnData(buffer);
	}
	public void readSpawnData(ByteBuf additionalData){
		super.readSpawnData(additionalData);
	}
}
