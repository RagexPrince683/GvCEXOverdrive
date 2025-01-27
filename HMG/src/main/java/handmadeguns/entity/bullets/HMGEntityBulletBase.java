package handmadeguns.entity.bullets;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.util.*;

//import littleMaidMobX.LMM_EntityLittleMaid;
//import littleMaidMobX.LMM_EntityLittleMaidAvatar;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import handmadeguns.HMGAddBullets;
import handmadeguns.HMGMessageKeyPressedC;
import handmadeguns.HMGPacketHandler;
import handmadeguns.HandmadeGunsCore;
import handmadeguns.Util.SoundInfo;
import handmadeguns.Util.TrailInfo;
import handmadeguns.Util.GunsUtils;
import handmadeguns.Util.sendEntitydata;
import handmadeguns.entity.*;
import handmadeguns.network.PacketFixClientbullet;
import handmadeguns.network.PacketSpawnParticle;
import handmadevehicle.HMVChunkLoaderManager;
import handmadevehicle.Utils;
import io.netty.buffer.ByteBuf;
import littleMaidMobX.LMM_EntityLittleMaid;
import littleMaidMobX.LMM_EntityLittleMaidAvatar;
import littleMaidMobX.LMM_EntityLittleMaidAvatarMP;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.S27PacketExplosion;
import net.minecraft.util.*;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.ForgeChunkManager;

import javax.vecmath.AxisAngle4d;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;

import static handmadeguns.HMGAddBullets.soundRicochetlist;
import static handmadeguns.HMGAddBullets.soundlist;
import static handmadeguns.HandmadeGunsCore.*;
import static handmadeguns.Util.GunsUtils.getmovingobjectPosition_forBlock;
import static handmadeguns.network.PacketShotBullet.fromObject;
import static handmadeguns.network.PacketShotBullet.toObject;
import static handmadevehicle.Utils.*;
import static java.lang.Math.*;
import static net.minecraft.util.MathHelper.wrapAngleTo180_float;
import static net.minecraft.world.World.MAX_ENTITY_RADIUS;

public class HMGEntityBulletBase extends Entity implements IEntityAdditionalSpawnData
{
	private static final boolean isDebugMessage = false;
	public Entity thrower;
	public Entity avoidEntity;
	protected Block inBlock;
	public boolean noex = true;
	public Entity hitedentity;
	protected int xTile = -1;
	protected int yTile = -1;
	protected int zTile = -1;
	public boolean inGround;
	protected double damage;
	public double knockbackXZ = 0.0001;
	public double knockbackY = 0;
	public double bulletStability;
	public int igniteBooster = 0;
	public int Booster_combustion_end = 20;
	public float resistance = 0.99f;
	public float resistanceinwater = 0.4f;
	public float acceleration = 0f;
	public int accelerationDelay = 0;
	public int accelerationFuse = -1;
	public SoundInfo flyingSoundInfo = null;
//			new SoundInfo("handmadeguns:handmadeguns.bulletflyby",
//															1f,
//															1f,
//															5f,
//															1f
//	);
	public SoundInfo ricochetSoundInfo = null;
//	public SoundInfo ricochetSoundInfo = new SoundInfo("handmadeguns:handmadeguns.Ricochet",1,1,1,16);
	public float firstSpeed;
	public int canPenetrate_entity = 1;
	private int hitedCNT = 0;
	
	public int killCNT = -10;
	
	
	public float damageRange = -1;
	//public int fuse;
	
	/**
	 * Is the entity that throws this 'thing' (snowball, ender pearl, eye of ender or potion)
	 */
	protected int ticksInGround;
	protected int ticksInAir;
	public int fuse=0;
	
	protected int Bdamege;
	public float ex;
	public boolean canex = cfg_blockdestroy;
	public boolean canbounce = false;
	public float bouncerate = 0.2f;
	public float bouncelimit = 45;
	public float gra = 0.029f;
	
	public String bulletTypeName = "default";
	public int modelid = -1;
	public Vec3 lockedpos;
	public Entity homingEntity;
	public Vec3 lockedBlockPos;

	public boolean SACLOS_Homing = false;

	public boolean isSemiActive = false;
	public boolean isActive = true;

	public float induction_precision = 10f;
	public float seekerwidth = 90f;
	public int soundcool;
	
	public boolean trail = false;
	public int traillength;
	public float   trailWidth		 = 0.2f;
	public float   animationspeed		 = 1;
	public String  trailtexture	  = null;
	public String smoketexture = null;
	public float  smokeWidth		 = 1f;
	public int	 smoketime		  = 10;
	public boolean trailglow = true;
	public boolean smokeglow = true;
	
	public boolean soundstoped = true;

	public HMGEntityBulletBase[] childBullets;
	public float childSpread;
	public float childSpeed;
	public float childRotationOffset_Circle;
	public float childRotationOffset_Pop;

	public boolean hasVT;
	public double VTRange;
	public double VTWidth;
	public double firstX;
	public double firstY;
	public double firstZ;

	public boolean chunkLoaderBullet = false;

	
	//int i = mod_IFN_GuerrillaVsCommandGuns.RPGExplosiontime;
	protected void entityInit() {
	
	}
	
	public HMGEntityBulletBase(World par1World)
	{
		super(par1World);
		renderDistanceWeight = 4096;
		if (worldObj != null) {
			isImmuneToFire = !worldObj.isRemote;
		}
		this.setSize(0.25F, 0.25F);
		//this.fuse = 30;
	}
	
	public HMGEntityBulletBase(World par1World, Entity par2Entity, int damege, float bspeed, float bure,String bulletTypeName)
	{
		super(par1World);
//		System.out.println("" + bure);
		this.thrower = par2Entity;
		this.setSize(0.25F, 0.25F);
		this.setLocationAndAngles(par2Entity.posX, par2Entity.posY + (double)par2Entity.getEyeHeight()*0.85, par2Entity.posZ, (par2Entity instanceof EntityLivingBase ? ((EntityLivingBase)par2Entity).rotationYawHead : par2Entity.rotationYaw), par2Entity.rotationPitch);
		Vec3 look = GunsUtils.getLook(1.0f,par2Entity);
		if(look != null) {
			firstX= this.posX = par2Entity.posX + look.xCoord/2;
			firstY = this.posY = par2Entity.posY + look.yCoord/2 + par2Entity.getEyeHeight();
			firstZ = this.posZ = par2Entity.posZ + look.zCoord/2;
			this.motionX = look.xCoord;
			this.motionZ = look.yCoord;
			this.motionY = look.zCoord;
		}
		this.setPosition(this.posX, this.posY, this.posZ);
		this.yOffset = 0.0F;
		//this.setThrowableHeading(this.motionX, this.motionY, this.motionZ, this.func_70182_d(), 1.0F);
		//this.fuse = 30;
		this.Bdamege = damege;
		//this.Bspeed = bspeed;
		//this.Bure = bure;
		setHeadingFromThrower(thrower,bspeed,bure);
		this.bulletTypeName = bulletTypeName;
	};
	public void setdamage(int value){
		Bdamege = value;
	}
	public void setcanex(boolean value){
		canex = value;
	}
	
	public HMGEntityBulletBase(World par1World, double par2, double par4, double par6)
	{
		
		super(par1World);
		this.ticksInGround = 0;
		this.setSize(0.25F, 0.25F);
		this.setPosition(par2, par4, par6);
		this.yOffset = 0.0F;
		//this.fuse = 30;
	}
	
	public void setThrowableHeading(double par1, double par3, double par5, float par7, float par8)
	{
		firstSpeed = par7;
		par8 /=2;
		float f2 = MathHelper.sqrt_double(par1 * par1 + par3 * par3 + par5 * par5);
		par1 /= (double)f2;
		par3 /= (double)f2;
		par5 /= (double)f2;
		par1 += this.rand.nextGaussian() * (double)(this.rand.nextBoolean() ? -1 : 1) * 0.01 * (double)par8;
		par3 += this.rand.nextGaussian() * (double)(this.rand.nextBoolean() ? -1 : 1) * 0.01 * (double)par8;
		par5 += this.rand.nextGaussian() * (double)(this.rand.nextBoolean() ? -1 : 1) * 0.01 * (double)par8;
		par1 *= (double)par7;
		par3 *= (double)par7;
		par5 *= (double)par7;
		this.motionX = par1;
		this.motionY = par3;
		this.motionZ = par5;
		float f3 = MathHelper.sqrt_double(par1 * par1 + par5 * par5);
		this.prevRotationYaw = this.rotationYaw = (float)(atan2(par1, par5) * 180.0D / Math.PI);
		this.prevRotationPitch = this.rotationPitch = (float)(atan2(par3, (double)f3) * 180.0D / Math.PI);
		this.ticksInGround = 0;
	}
	
	public void setThrowableHeading(double par1, double par3, double par5, float par7, float par8,Entity shooter)
	{
		par8 /=2;
		float f2 = MathHelper.sqrt_double(par1 * par1 + par3 * par3 + par5 * par5);
		par1 /= (double)f2;
		par3 /= (double)f2;
		par5 /= (double)f2;
		par1 += this.rand.nextGaussian() * (double)(this.rand.nextBoolean() ? -1 : 1) * 0.01 * (double)par8;
		par3 += this.rand.nextGaussian() * (double)(this.rand.nextBoolean() ? -1 : 1) * 0.01 * (double)par8;
		par5 += this.rand.nextGaussian() * (double)(this.rand.nextBoolean() ? -1 : 1) * 0.01 * (double)par8;
		par1 *= (double)par7;
		par3 *= (double)par7;
		par5 *= (double)par7;
		float f3 = MathHelper.sqrt_double(par1 * par1 + par5 * par5);
		this.prevRotationYaw = this.rotationYaw = (float)(atan2(par1, par5) * 180.0D / Math.PI);
		this.prevRotationPitch = this.rotationPitch = (float)(atan2(par3, (double)f3) * 180.0D / Math.PI);
		if(shooter!= null) {
			double motionlength = sqrt(shooter.motionX * shooter.motionX + shooter.motionY * shooter.motionY + shooter.motionZ * shooter.motionZ);
			if (motionlength > 0.01) {
				par1 += (shooter.motionX / motionlength + this.rand.nextGaussian() * (double) (this.rand.nextBoolean() ? -1 : 1) * 0.01 * (double) par8) * motionlength;
				par3 += (shooter.motionY / motionlength + this.rand.nextGaussian() * (double) (this.rand.nextBoolean() ? -0.5 : 0.5) * 0.01 * (double) par8) * motionlength;
				par5 += (shooter.motionZ / motionlength + this.rand.nextGaussian() * (double) (this.rand.nextBoolean() ? -1 : 1) * 0.01 * (double) par8) * motionlength;
			}
		}
		this.motionX = par1;
		this.motionY = par3;
		this.motionZ = par5;
		this.ticksInGround = 0;
	}
	
	@Override
	public void setVelocity(double par1, double par3, double par5) {
		this.motionX = par1;
		this.motionY = par3;
		this.motionZ = par5;
		
		{
			float var7 = MathHelper.sqrt_double(par1 * par1 + par5 * par5);
			this.prevRotationYaw = this.rotationYaw = (float)(atan2(par1, par5) * 180.0D / Math.PI);
			this.prevRotationPitch = this.rotationPitch = (float)(atan2(par3, (double)var7) * 180.0D / Math.PI);
			this.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
			this.ticksInGround = 0;
		}
	}
	
	public void setHeadingFromThrower(Entity entityThrower, float velocity, float inaccuracy)
	{
		Vec3 look = GunsUtils.getLook(1.0f,entityThrower);
		if(look == null) {
		}else {
			this.setThrowableHeading(look.xCoord, look.yCoord, look.zCoord, velocity, inaccuracy);
			this.prevRotationPitch = this.rotationPitch = entityThrower.rotationPitch;
		}
	}
	public void setHeadingFromThrower(float rotationPitchIn, float rotationYawIn, float pitchOffset, float velocity, float inaccuracy)
	{
//			this.motionX = (look.xCoord+this.rand.nextGaussian() * (double)(this.rand.nextBoolean() ? -1 : 1) * 0.00249999994 * (double)inaccuracy) * velocity;
//			this.motionY = (look.yCoord+this.rand.nextGaussian() * (double)(this.rand.nextBoolean() ? -1 : 1) * 0.00249999994 * (double)inaccuracy) * velocity;
//			this.motionZ = (look.zCoord+this.rand.nextGaussian() * (double)(this.rand.nextBoolean() ? -1 : 1) * 0.00249999994 * (double)inaccuracy) * velocity;
		Vec3 look = getLook(1.0f,rotationYawIn,rotationPitchIn);
		this.setThrowableHeading(look.xCoord, look.yCoord, look.zCoord, velocity, inaccuracy);
	}
	public void setHeadingFromThrower(float rotationPitchIn, float rotationYawIn, float velocity, float inaccuracy)
	{
//			this.motionX = (look.xCoord+this.rand.nextGaussian() * (double)(this.rand.nextBoolean() ? -1 : 1) * 0.00249999994 * (double)inaccuracy) * velocity;
//			this.motionY = (look.yCoord+this.rand.nextGaussian() * (double)(this.rand.nextBoolean() ? -1 : 1) * 0.00249999994 * (double)inaccuracy) * velocity;
//			this.motionZ = (look.zCoord+this.rand.nextGaussian() * (double)(this.rand.nextBoolean() ? -1 : 1) * 0.00249999994 * (double)inaccuracy) * velocity;
		Vec3 look = getLook(1.0f,rotationYawIn,rotationPitchIn);
		this.setThrowableHeading(look.xCoord, look.yCoord, look.zCoord, velocity, inaccuracy);
	}
	
	
	/*protected float func_70182_d()
	{
		return 5F;
	}*/
	
	@SideOnly(Side.CLIENT)
	public int getBrightnessForRender(float par1)
	{
		return 15728880;
	}
	
	public float getBrightness(float par1)
	{
		return 15.0F;
	}
	
	@Override
	protected void readEntityFromNBT(NBTTagCompound p_70037_1_) {
	
	}
	
	@Override
	protected void writeEntityToNBT(NBTTagCompound p_70014_1_) {
	}


	/** The entity's X coordinate at the previous tick, used to calculate position during rendering routines */
	public double lastTickPosX2;
	/** The entity's Y coordinate at the previous tick, used to calculate position during rendering routines */
	public double lastTickPosY2;
	/** The entity's Z coordinate at the previous tick, used to calculate position during rendering routines */
	public double lastTickPosZ2;


	public void onUpdate() {
		super.onUpdate();
		this.worldObj.theProfiler.startSection("HMG_Bullet");
//		if(!worldObj.isRemote)System.out.println("Y " + (this.posY - firstY) + "\t\tDist " + sqrt(pow(this.posX - firstX,2) + pow(this.posZ - firstZ,2)));
		if(Double.isNaN( this.motionX ) || Double.isNaN( this.motionY ) || Double.isNaN( this.motionZ )){//エラー対応
			this.motionX =this.motionY =this.motionZ =0;
			this.posX = this.posY = this.posZ = 0;
			System.out.println("debug");
			this.setDead();
			return;
		}
		if(!this.worldObj.isRemote && this.thrower  == null){//主の居ない弾は削除
			setDead();
		}
		if(!worldObj.isRemote && !this.inGround && worldObj.blockExists(this.xTile, this.yTile, this.zTile)) {
			Block block = this.worldObj.getBlock(this.xTile, this.yTile, this.zTile);
			if (block.getMaterial() != Material.air) {
				block.setBlockBoundsBasedOnState(this.worldObj, this.xTile, this.yTile, this.zTile);
				AxisAlignedBB axisalignedbb = block.getCollisionBoundingBoxFromPool(this.worldObj, this.xTile, this.yTile, this.zTile);
				
				if (axisalignedbb != null && axisalignedbb.isVecInside(Vec3.createVectorHelper(this.posX, this.posY, this.posZ))) {
					this.inBlock = block;
					this.inGround = true;
				}
			}
		}
		fuse--;
		if(fuse==0){//時限信管が作動したらその場で衝突処理
			this.posX += this.motionX;
			this.posY += this.motionY;
			this.posZ += this.motionZ;
			onImpact(new MovingObjectPosition((int)posX,(int)posY,(int)posZ,-1,Vec3.createVectorHelper(this.posX,this.posY,this.posZ),true));
			setDead();
			return;
		}
		if (this.inGround)
		{
			if (worldObj.blockExists(this.xTile, this.yTile, this.zTile) && this.worldObj.getBlock(this.xTile, this.yTile, this.zTile) == this.inBlock)
			{
				ticksInAir = 0;
				this.motionX=
						this.motionY=
								this.motionZ=0;
				if(lockedpos != null){
					this.posX = lockedpos.xCoord;
					this.posY = lockedpos.yCoord;
					this.posZ = lockedpos.zCoord;
				}
				if(fuse<0){
					setDead();
				}
			}
			else
			{
				this.inGround = false;
				this.motionX *= (double)(this.rand.nextFloat() * 0.2F);
				this.motionY *= (double)(this.rand.nextFloat() * 0.2F);
				this.motionZ *= (double)(this.rand.nextFloat() * 0.2F);
				this.ticksInGround = 0;
				this.ticksInAir = 0;
			}
		} else {
			airboneupdate();
		}
		this.func_145775_I();
		this.setPosition(this.posX, this.posY, this.posZ);
//		System.out.println(" " + this + "  " + inGround);
		if(worldObj.isRemote && !isDead && accelerationDelay < ticksInAir && (accelerationFuse == -1 || accelerationFuse > ticksInAir)){
			if(trail) {
				PacketSpawnParticle packetSpawnParticle = new PacketSpawnParticle(lastTickPosX2, lastTickPosY2, lastTickPosZ2,
																						 posX,
																						 posY,
																						 posZ, 3);
				packetSpawnParticle.trailwidth = trailWidth;
				packetSpawnParticle.animationspeed = animationspeed;
				packetSpawnParticle.name = trailtexture;
				packetSpawnParticle.fuse = traillength;
				if (trailglow) packetSpawnParticle.id += 100;
				HMG_proxy.spawnParticles(packetSpawnParticle);
			}
			preclientUpdate();
		}
		this.lastTickPosX2 = this.posX;
		this.lastTickPosY2 = this.posY;
		this.lastTickPosZ2 = this.posZ;
		this.worldObj.theProfiler.endSection();
	}
	
	public Entity getThrower() {
		return this.thrower;
	}
	
	
	/**
	 * Called when this EntityThrowable hits a block or entity.
	 */
	protected void onImpact(MovingObjectPosition var1)
	{
		if(worldObj.isRemote && ticksInAir>0 && ricochetSoundInfo != null && var1.hitVec != null &&
				   motionX * motionX + motionY * motionY + motionZ * motionZ > ricochetSoundInfo.MinBltSP * ricochetSoundInfo.MinBltSP && getDistanceSqToEntity(HMG_proxy.getEntityPlayerInstance()) < ricochetSoundInfo.MaxDist*ricochetSoundInfo.MaxDist){
			worldObj.playSound(var1.hitVec.xCoord,var1.hitVec.yCoord,var1.hitVec.zCoord,ricochetSoundInfo.sound,ricochetSoundInfo.LV,ricochetSoundInfo.SP,false);
		}

		if (var1.entityHit != null && noex)
		{
			int var2 = this.Bdamege;
//			System.out.println("debug" + this.thrower +"  "+ var1.entityHit);
			if(islmmloaded && HandmadeGunsCore.cfg_FriendFireLMM){
				if((this.thrower instanceof LMM_EntityLittleMaid || this.thrower instanceof LMM_EntityLittleMaidAvatar || this.thrower instanceof LMM_EntityLittleMaidAvatarMP))
				{
					if (var1.entityHit instanceof LMM_EntityLittleMaid) {
						var2 = 0;
					}
					if (var1.entityHit instanceof LMM_EntityLittleMaidAvatar) {
						var2 = 0;
					}
					if (var1.entityHit instanceof EntityPlayer) {
						var2 = 0;
					}
				}else if(this.thrower instanceof EntityPlayer){
					if (var1.entityHit instanceof LMM_EntityLittleMaid) {
						var2 = 0;
					}
					if (var1.entityHit instanceof LMM_EntityLittleMaidAvatar) {
						var2 = 0;
					}
				}
			}
			if(this.thrower instanceof IFF){
				if(((IFF) this.thrower).is_this_entity_friend(var1.entityHit)){
					var2 = 0;
				}
			}
			var1.entityHit.hurtResistantTime = 0;

			double moXback = var1.entityHit.motionX;
			double moYback = var1.entityHit.motionY;
			double moZback = var1.entityHit.motionZ;

			boolean flag;
			if(var1.entityHit instanceof I_SPdamageHandle){
				flag = ((I_SPdamageHandle)var1.entityHit).attackEntityFrom_with_Info(var1,(new EntityDamageSourceIndirect("arrow", this, this.getThrower())).setProjectile(),var2);
			}else {
				flag = var1.entityHit.attackEntityFrom((new EntityDamageSourceIndirect("arrow", this, this.getThrower())).setProjectile(),var2);
			}
			if(flag){
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
						if(((EntityLivingBase) var1.entityHit).getHealth() < 0)var1.entityHit.hurtResistantTime = 20;
					}
				}
			}else if(var1.entityHit.attackEntityFrom((new EntityDamageSourceIndirect("penetrate", this, this.getThrower()).setProjectile()),(float)var2)){
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
			if (!this.worldObj.isRemote)
			{
				if(this.getThrower() != null&&getThrower() instanceof EntityPlayerMP){
					HMGPacketHandler.INSTANCE.sendTo(new HMGMessageKeyPressedC(10, this.getThrower().getEntityId()),(EntityPlayerMP)this.getThrower());
				}
				this.setDead();
			}
			if(var1.entityHit instanceof EntityLiving)for (int i = 0; i < 4; ++i) {
//					worldObj.spawnParticle("snowballpoof", this.posX, this.posY,
				worldObj.spawnParticle("reddust",
						var1.hitVec.xCoord, var1.hitVec.yCoord, var1.hitVec.zCoord,
						0.0D, 0.0D, 0.0D);
			}
		}else{
			Block lblock = worldObj.getBlock(var1.blockX, var1.blockY, var1.blockZ);
			int lmeta = worldObj.getBlockMetadata(var1.blockX, var1.blockY, var1.blockZ);
			if (checkDestroyBlock(var1, var1.blockX, var1.blockY, var1.blockZ, lblock, lmeta)) {
				if (!this.worldObj.isRemote)
				{
					onBreakBlock(var1, var1.blockX, var1.blockY, var1.blockZ, lblock, lmeta);
				}
			} else {
				for (int i = 0; i < 4; ++i) {
//					worldObj.spawnParticle("snowballpoof", this.posX, this.posY,
					worldObj.spawnParticle("smoke",
							var1.hitVec.xCoord, var1.hitVec.yCoord, var1.hitVec.zCoord,
							0.0D, 0.0D, 0.0D);
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
			}
			if (!this.worldObj.isRemote)
			{
				if(!this.canbounce) this.setDead();
			}
		}
	}
	
	
	@Override
	public void setDead() {
		if(damageRange>0){
			List list = worldObj.loadedEntityList;
			for (int j = 0; j < list.size(); ++j) {
				Entity entity1 = (Entity) list.get(j);
				if ((this.isInWater() == entity1.isInWater()) && entity1.canBeCollidedWith() && (ticksInAir > 20 + accelerationDelay ||
															(iscandamageentity(entity1)))) {
					double f = this.getDistanceSq(entity1.posX,entity1.posY + entity1.height/2,entity1.posZ);
					if(f < damageRange * damageRange){
						f = damageRange * damageRange-f;
						Vec3 vec3 = Vec3.createVectorHelper(this.posX, this.posY, this.posZ);
						Vec3 vec31 = Vec3.createVectorHelper(entity1.posX,entity1.posY + entity1.height/2,entity1.posZ);
						MovingObjectPosition movingobjectposition = getmovingobjectPosition_forBlock(worldObj,vec3, vec31);//衝突するブロックを調べる
						if(movingobjectposition == null){
							int var2 = this.Bdamege;
							if(islmmloaded&&(this.thrower instanceof LMM_EntityLittleMaid || this.thrower instanceof LMM_EntityLittleMaidAvatar || this.thrower instanceof LMM_EntityLittleMaidAvatarMP || (cfg_FriendFirePlayerToLMM && this.thrower instanceof EntityPlayer)) && HandmadeGunsCore.cfg_FriendFireLMM){
								if (entity1 instanceof LMM_EntityLittleMaid)
								{
									var2 = 0;
								}
								if (entity1 instanceof LMM_EntityLittleMaidAvatar)
								{
									var2 = 0;
								}
								if (entity1 instanceof EntityPlayer)
								{
									var2 = 0;
								}
							}
							if(this.thrower instanceof IFF){
								if(((IFF) this.thrower).is_this_entity_friend(entity1)){
									var2 = 0;
								}
							}
							Vector3d accelVec = new Vector3d(entity1.posX,entity1.posY,entity1.posZ);
							accelVec.sub(new Vector3d(this.posX,this.posY,this.posZ));
							accelVec.normalize();
							entity1.hurtResistantTime = 0;
							entity1.attackEntityFrom((new EntityDamageSourceIndirect("explosion", this, this.getThrower())).setProjectile().setExplosion(), (float) (var2*f/(damageRange * damageRange)));
							entity1.motionX -=accelVec.x *  var2/12000 * f /(damageRange * damageRange);
							entity1.motionY -=accelVec.y *  var2/12000 * f /(damageRange * damageRange);
							entity1.motionZ -=accelVec.z *  var2/12000 * f /(damageRange * damageRange);
						}
					}
				}
			}
		}

		if(ticket != null && myChunk != null)ForgeChunkManager.unforceChunk(ticket,myChunk);
		super.setDead();
	}
	
	public void onBlockDestroyed(int blockX, int blockY, int blockZ) {
		//int bid = worldObj.getBlock(blockX, blockY, blockZ);
		int bmd = worldObj.getBlockMetadata(blockX, blockY, blockZ);
		Block block = worldObj.getBlock(blockX, blockY, blockZ);
		if(block == null) {
			return;
		}
		worldObj.playAuxSFX(2001, blockX, blockY, blockZ, (bmd  << 12));
		boolean flag = worldObj.setBlockToAir(blockX, blockY, blockZ);
		if (block != null && flag) {
			block.onBlockDestroyedByPlayer(worldObj, blockX, blockY, blockZ, bmd);
			
		}
	}
	
	
	public boolean checkDestroyBlock(MovingObjectPosition var1, int pX, int pY, int pZ, Block pBlock, int pMetadata) {
		if ((pBlock.getMaterial() == Material.glass)
					|| (pBlock instanceof BlockFlowerPot)
					|| (pBlock instanceof BlockTNT)
					|| (pBlock instanceof BlockDoublePlant)
				) {
			return true;
		}
		return false;
	}
	
	
	public boolean onBreakBlock(MovingObjectPosition var1, int pX, int pY, int pZ, Block pBlock, int pMetadata) {
		this.Debug("destroy: %d, %d, %d", pX, pY, pZ);
		if (pBlock instanceof BlockTNT) {
			removeBlock(pX, pY, pZ, pBlock, pMetadata);
			pBlock.onBlockDestroyedByExplosion(worldObj, pX, pY, pZ, new Explosion(worldObj, getThrower(), pX, pY, pZ, 0.0F));
			return true;
		} else {
			removeBlock(pX, pY, pZ, pBlock, pMetadata);
			pBlock.onBlockDestroyedByPlayer(worldObj, pX, pY, pZ, pMetadata);
			//this.entityDropItem(new ItemStack(pBlock), 1);
			return false;
		}
	}
	
	public static void Debug(String pText, Object... pData) {
		if (isDebugMessage) {
			System.out.println(String.format("GunsBase-" + pText, pData));
		}
	}
	
	
	protected void removeBlock(int pX, int pY, int pZ, Block pBlock, int pMetadata) {
		worldObj.playAuxSFX(2001, pX, pY, pZ, Block.getIdFromBlock(pBlock) + (pMetadata << 12));
		worldObj.setBlockToAir(pX, pY, pZ);
	}
	
	
	public void explode(double x,double y,double z,float level,boolean candestroy)
	{
		noex = true;
		if(!worldObj.isRemote){
			HMGExplosion explosion = new HMGExplosion(worldObj,thrower,x,y,z, level);
			explosion.isFlaming = false;
			explosion.isSmoking = candestroy;
			if (net.minecraftforge.event.ForgeEventFactory.onExplosionStart(worldObj, explosion)) return;
			explosion.doExplosionA();
			explosion.doExplosionB(false);

			if (!candestroy)
			{
				explosion.affectedBlockPositions.clear();
			}

			Iterator iterator = worldObj.playerEntities.iterator();

			while (iterator.hasNext())
			{
				EntityPlayer entityplayer = (EntityPlayer)iterator.next();

				if (entityplayer.getDistanceSq(x, y, z) < 4096.0D)
				{
					((EntityPlayerMP)entityplayer).playerNetServerHandler.sendPacket(new S27PacketExplosion(x, y, z, level, explosion.affectedBlockPositions, (Vec3)explosion.func_77277_b().get(entityplayer)));
				}
			}
//			this.worldObj.createExplosion(thrower,x,y,z, level, candestroy);
		}
		
		
	}
	public void writeSpawnData(ByteBuf buffer){
		PacketBuffer lpbuf = new PacketBuffer(buffer);
		lpbuf.writeFloat(bouncerate);
		lpbuf.writeFloat(bouncelimit);
		lpbuf.writeFloat(gra);
		lpbuf.writeFloat(acceleration);
		lpbuf.writeInt(accelerationDelay);
		lpbuf.writeInt(accelerationFuse);
		lpbuf.writeFloat(resistance);
		lpbuf.writeInt(fuse);
		lpbuf.writeBoolean(canbounce);
		lpbuf.writeFloat(rotationYaw);
		lpbuf.writeFloat(rotationPitch);

		lpbuf.writeDouble(this.posX);
		lpbuf.writeDouble(this.posY);
		lpbuf.writeDouble(this.posZ);
		try {
			byte[] typename = fromObject(bulletTypeName);
			lpbuf.writeInt(typename.length);
			lpbuf.writeBytes(typename);
		} catch (IOException e) {
			e.printStackTrace();
		}
		sendEntitydata data = new sendEntitydata(this);
		try {
			lpbuf.writeInt(fromObject(data).length);
			lpbuf.writeBytes(fromObject(data));
		} catch (NotSerializableException e){
			e.printStackTrace();
		} catch (IOException e){
			e.printStackTrace();
		}
		lpbuf.writeInt(fuse);
		if(thrower != null) {
			lpbuf.writeInt(thrower.getEntityId());
		}else{
			lpbuf.writeInt(-1);
		}
		
	}
	public void readSpawnData(ByteBuf additionalData){
		PacketBuffer lpbuf = new PacketBuffer(additionalData);
		bouncerate = lpbuf.readFloat();
		bouncelimit = lpbuf.readFloat();
		gra = lpbuf.readFloat();
		acceleration = lpbuf.readFloat();
		accelerationDelay = lpbuf.readInt();
		accelerationFuse = lpbuf.readInt();
		resistance = lpbuf.readFloat();
		fuse = lpbuf.readInt();
		canbounce = lpbuf.readBoolean();
		rotationYaw = lpbuf.readFloat();
		rotationPitch = lpbuf.readFloat();

		this.lastTickPosX2 = lpbuf.readDouble();
		this.lastTickPosY2 = lpbuf.readDouble();
		this.lastTickPosZ2 = lpbuf.readDouble();
		byte[] temp = new byte[lpbuf.readInt()];
		lpbuf.readBytes(temp);
		try {
			bulletTypeName = (String) toObject(temp);
//			System.out.println("debug" + modelname);
			if(HMGAddBullets.indexlist != null&& !HMGAddBullets.indexlist.isEmpty() && bulletTypeName !=null&& !bulletTypeName.isEmpty() && !bulletTypeName.equals("default")) {
				modelid = HMGAddBullets.indexlist.get(bulletTypeName);
				SoundInfo tempsoundinfo = soundlist.get(modelid);
				if(tempsoundinfo != null) {
					flyingSoundInfo = tempsoundinfo;
				}
				SoundInfo tempSoundRicochet = soundRicochetlist.get(modelid);
				if(tempSoundRicochet != null) {
					ricochetSoundInfo = tempSoundRicochet;
				}
				TrailInfo temptrailinfo = HMGAddBullets.trailsettings.get(modelid);
				if(temptrailinfo != null){
					trail = temptrailinfo.enabletrai && rand.nextFloat()<=temptrailinfo.trailProbability;
					traillength = temptrailinfo.traillength;
					trailWidth = temptrailinfo.trailWidth;
					animationspeed = temptrailinfo.animationspeed;
					trailtexture = temptrailinfo.trailtexture;
					smoketexture  = temptrailinfo.smoketexture;
					smokeWidth = temptrailinfo.smokeWidth;
					smoketime = temptrailinfo.smoketime;
					
					trailglow = temptrailinfo.trailglow;
					smokeglow = temptrailinfo.smokeglow;
				}
				if(HMGAddBullets.modellist.get(modelid) == null)modelid = -1;
//				System.out.println("modelid " + modelid);
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		temp = new byte[lpbuf.readInt()];
		lpbuf.readBytes(temp);
		try{
			sendEntitydata data = (sendEntitydata) toObject(temp);
			this.motionX = data.motionX;
			this.motionY = data.motionY;
			this.motionZ = data.motionZ;
		}catch (OptionalDataException e){
			e.printStackTrace();
		}catch (StreamCorruptedException e){
			e.printStackTrace();
		}catch (ClassNotFoundException e){
			e.printStackTrace();
		}catch (IOException e){
			e.printStackTrace();
		}catch (ClassCastException e){
			e.printStackTrace();
		}
		this.fuse = lpbuf.readInt();
		thrower = worldObj.getEntityByID(lpbuf.readInt());
	}
	
	public Vec3 getLook(float p_70676_1_,float rotationYawin,float rotationPitchin)
	{
		float f1;
		float f2;
		float f3;
		float f4;
		
		if (p_70676_1_ == 1.0F)
		{
			f1 = MathHelper.cos(-rotationYawin * 0.017453292F - (float)Math.PI);
			f2 = MathHelper.sin(-rotationYawin * 0.017453292F - (float)Math.PI);
			f3 = -MathHelper.cos(-rotationPitchin * 0.017453292F);
			f4 = MathHelper.sin(-rotationPitchin * 0.017453292F);
			return Vec3.createVectorHelper((double)(f2 * f3), (double)f4, (double)(f1 * f3));
		}
		else
		{
			f1 = MathHelper.cos(-rotationYawin * 0.017453292F - (float)Math.PI);
			f2 = MathHelper.sin(-rotationYawin * 0.017453292F - (float)Math.PI);
			f3 = -MathHelper.cos(-rotationPitchin * 0.017453292F);
			f4 = MathHelper.sin(-rotationPitchin * 0.017453292F);
			return Vec3.createVectorHelper((double)(f2 * f3)*p_70676_1_, (double)f4*p_70676_1_, (double)(f1 * f3)*p_70676_1_);
		}
	}
	
	protected boolean iscandamageentity(Entity entity){
		return Utils.iscandamageentity(thrower,entity);
	}
	
	@Override
	public boolean writeToNBTOptional(NBTTagCompound p_70039_1_)
	{
		
		return false;
	}
	public void preclientUpdate(){
		if(smoketexture != null && (accelerationFuse == -1 || accelerationFuse > ticksInAir)) {
			int length = 5;
			for(int i=0;i<length;i++) {
				PacketSpawnParticle packet = new PacketSpawnParticle(posX + motionX/length * i, posY + motionY/length * i, posZ + motionZ/length * i, 0, 0, 0, 1);
				packet.name = smoketexture;
				packet.scale = smokeWidth;
				packet.fuse = smoketime;
				if (smokeglow) packet.id += 100;
				HMG_proxy.spawnParticles(packet);
			}
		}
		if(ticksInAir>0 && flyingSoundInfo != null && soundstoped &&
				this.getspeed() > flyingSoundInfo.MinBltSP * flyingSoundInfo.MinBltSP &&
				getDistanceSq(
						HMG_proxy.getMCInstance().renderViewEntity.posX,
						HMG_proxy.getMCInstance().renderViewEntity.posY,
						HMG_proxy.getMCInstance().renderViewEntity.posZ)
						< flyingSoundInfo.MaxDist*flyingSoundInfo.MaxDist){
			HMG_proxy.playsoundatBullet(flyingSoundInfo.sound,flyingSoundInfo.LV,flyingSoundInfo.SP,flyingSoundInfo.MinBltSP,flyingSoundInfo.MaxDist,this,true);
			soundstoped = false;
		}
	}
	
	
	public boolean applyacceleration(){
		{
//			Vec3 bulletVec = getLook(1,rotationYaw,rotationPitch);
			Vector3d bulletVec = new Vector3d(0,0,1);
			RotateVectorAroundX(bulletVec,-rotationPitch);
			RotateVectorAroundY(bulletVec,-rotationYaw);
			if(NaNCheck(bulletVec))bulletVec = new Vector3d(0,0,1);

			if(accelerationDelay < ticksInAir && (accelerationFuse == -1 || accelerationFuse > ticksInAir)) {
				this.motionX += bulletVec.x * acceleration;
				this.motionY += bulletVec.y * acceleration;
				this.motionZ += bulletVec.z * acceleration;
			}
//				worldObj.playSoundAtEntity(this, "handmadeguns:handmadeguns." + flyingSound,flyingSoundLV, flyingSoundSP);
		}
		return false;
	}
	public double getspeed(){
		return MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ + this.motionY * this.motionY);
	}
	public double getTerminalspeed(){
		if(acceleration > 0){
			//A*0.9 + 0.1 = A
			//(1.0 - 0.9) * A = 0.1
			//A = 0.1 * (1.0 - 0.9)
			//Terminal = acceleration * (1 - resistance)
			return acceleration/(1-resistance);
		}else
		return MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ + this.motionY * this.motionY);
	}
	public double getspeed(Entity entity){
		return MathHelper.sqrt_double(entity.motionX * entity.motionX + entity.motionZ * entity.motionZ + entity.motionY * entity.motionY);
	}
	public double getspeedSq(){
		return (this.motionX * this.motionX + this.motionZ * this.motionZ + this.motionY * this.motionY);
	}
	public void airboneupdate(){
		onGround = false;
		Vec3 backupmotion = Vec3.createVectorHelper(motionX,motionY,motionZ);
		++this.ticksInAir;
		
		double remainingMovelength = backupmotion.lengthVector();
		Vec3 hitedpos = null;
		Vec3 motionVec = Vec3.createVectorHelper(motionX,motionY,motionZ);
		boolean changemotionflag = false;
//		int breakcnt = 0;
		//反射・ヒット処理
		Vec3 vec3 = Vec3.createVectorHelper(this.posX, this.posY, this.posZ);
		Vec3 vec31 = Vec3.createVectorHelper(this.posX + motionVec.xCoord, this.posY + motionVec.yCoord, this.posZ + motionVec.zCoord);
		MovingObjectPosition movingobjectposition = GunsUtils.getmovingobjectPosition_forBlock(worldObj,vec3, vec31,3,null,new Material[]{Material.leaves});//衝突するブロックを調べる 葉は貫通
		//これをやるときに除外判定があればここまでやる必要はなかったのだ、故に作った。
//			while (movingobjectposition != null) {
//				hitblock = this.worldObj.getBlock(movingobjectposition.blockX, movingobjectposition.blockY, movingobjectposition.blockZ);
//				if ((hitblock.getMaterial() == Material.plants) || (hitblock.getMaterial() == Material.leaves) || ((
//																														   hitblock.getMaterial() == Material.glass ||
//																																   hitblock instanceof BlockFence ||
//																																   hitblock instanceof BlockFenceGate ||
//																																   hitblock == Blocks.iron_bars) && rand.nextInt(5) < 2)) {
//					if (breakcnt > 100) {
//						break;
//					}
//					breakcnt++;
//					Vec3 penerater = Vec3.createVectorHelper(motionVec.xCoord, motionVec.yCoord, motionVec.zCoord);
//					penerater = penerater.normalize();
//					boolean flag =
//							((this.posX + motionVec.xCoord - movingobjectposition.hitVec.xCoord)<0 && (this.posX + motionVec.xCoord - movingobjectposition.hitVec.xCoord-penerater.xCoord)>0) || ((this.posX + motionVec.xCoord - movingobjectposition.hitVec.xCoord)>0 && (this.posX + motionVec.xCoord - movingobjectposition.hitVec.xCoord-penerater.xCoord)<0) &&
//																																																					   ((this.posY + motionVec.yCoord - movingobjectposition.hitVec.yCoord)<0 && (this.posY + motionVec.yCoord - movingobjectposition.hitVec.yCoord-penerater.yCoord)>0) || ((this.posY + motionVec.yCoord - movingobjectposition.hitVec.yCoord)>0 && (this.posY + motionVec.yCoord - movingobjectposition.hitVec.yCoord-penerater.yCoord)<0) &&
//																																																																																																				  ((this.posZ + motionVec.zCoord - movingobjectposition.hitVec.zCoord)<0 && (this.posZ + motionVec.zCoord - movingobjectposition.hitVec.zCoord-penerater.zCoord)>0) || ((this.posZ + motionVec.zCoord - movingobjectposition.hitVec.zCoord)>0 && (this.posZ + motionVec.zCoord - movingobjectposition.hitVec.zCoord-penerater.zCoord)<0);//処理が本来動く範囲を超えた
//					if(flag){
//						movingobjectposition = null;
//						break;
//					}
//					vec3 = Vec3.createVectorHelper(movingobjectposition.hitVec.xCoord + penerater.xCoord, movingobjectposition.hitVec.yCoord + penerater.yCoord, movingobjectposition.hitVec.zCoord + penerater.zCoord);
//					vec31 = Vec3.createVectorHelper(this.posX + motionVec.xCoord, this.posY + motionVec.yCoord, this.posZ + motionVec.zCoord);
//					movingobjectposition = this.worldObj.func_147447_a(vec3, vec31, false, true, false);
//				} else {
//					break;
//				}
//			}
//			breakcnt++;
//			if (breakcnt > 50) {//50回も反射するって無いやろお前…
//				inGround = true;
//				System.out.println("debug1" + hitedpos);
//				System.out.println("debug2" + lastpos);
//				System.out.println("debug3" + motionVec);
//			}

		if(hasVT && accelerationDelay < ticksInAir && (homingEntity == null || awayFlag || forceVT)){
			List list = this.getEntitiesWithinAABBExcludingEntity(this,
					this.boundingBox.expand(VTRange + abs(this.motionX), VTRange + abs(this.motionY), VTRange + abs(this.motionZ)));
			for (int j = 0; j < list.size(); ++j) {
				Entity entity1 = (Entity) list.get(j);
				if (entity1.width > 1 && entity1.height > 1 && iscandamageentity(entity1) && getDistanceSqToEntity(entity1) < VTRange) {
					Vector3d toTGT = new Vector3d(entity1.posX - this.posX, entity1.posY - this.posY, entity1.posZ - this.posZ);
					Vector3d motion = new Vector3d(motionX, motionY, motionZ);
					toTGT.normalize();
					motion.normalize();
					double angle = acos(motion.dot(toTGT));
					if (angle < toRadians(VTWidth)) {
						this.explode(this.posX, this.posY, this.posZ, ex, cfg_blockdestroy && canex);
						break;
					}
				}
			}
		}

		vec3 = Vec3.createVectorHelper(this.posX, this.posY, this.posZ);
		vec31 = Vec3.createVectorHelper(this.posX + motionVec.xCoord, this.posY + motionVec.yCoord, this.posZ + motionVec.zCoord);
		if (movingobjectposition != null) {
			vec31 = Vec3.createVectorHelper(movingobjectposition.hitVec.xCoord, movingobjectposition.hitVec.yCoord, movingobjectposition.hitVec.zCoord);
		}
		if(remainingMovelength > firstSpeed/10){

		}else {
			killCNT++;
		}
		List entitylist = this.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox.addCoord(motionVec.xCoord, motionVec.yCoord, motionVec.zCoord).expand(1, 1, 1));
		ArrayList<MovingObjectPosition_And_Entity> entities = new ArrayList<MovingObjectPosition_And_Entity>();
		for (int j = 0; j < entitylist.size(); ++j) {
			Entity entity1 = (Entity) entitylist.get(j);
			if(entity1 == avoidEntity)continue;
			if(entity1 instanceof IProjectile)continue;
			if (entity1.canBeCollidedWith() && (ticksInAir > 8 ||
					(iscandamageentity(entity1)))) {
				entities.add(new MovingObjectPosition_And_Entity(entity1));
			}
		}
		double d0 = 0.0D;
		double d1;
		float f = 0.1F;
		if(!entities.isEmpty()) {
			MovingObjectPosition_And_Entity backup = entities.get(0);//cnt - 1
			for (int cnt = 0; cnt < entities.size(); cnt++) {
				MovingObjectPosition_And_Entity movingObjectPosition_and_entity = entities.get(cnt);
				AxisAlignedBB axisalignedbb = movingObjectPosition_and_entity.entity.boundingBox.expand((double) f, (double) f, (double) f);
				MovingObjectPosition movingobjectposition1 = axisalignedbb.calculateIntercept(vec3, vec31);
				movingObjectPosition_and_entity.movingObjectPosition = movingobjectposition1;
				if (movingobjectposition1 != null) {
					d1 = vec3.distanceTo(movingobjectposition1.hitVec);
					if ((d1 < d0 || d0 == 0.0D) && cnt > 0) {
						entities.set(cnt, backup);
						entities.set(cnt-1, movingObjectPosition_and_entity);
					}else {
						d0 = d1;
						backup = movingObjectPosition_and_entity;
					}
				}else {
					entities.remove(cnt);
					cnt--;
				}
			}
		}
//				System.out.println("debug" + entities);
		for(MovingObjectPosition_And_Entity current : entities){
			if(!canbounce && canPenetrate_entity > 0 && canPenetrate_entity <= hitedCNT){
				fuse--;
				break;
			}
			hitedCNT++;
			MovingObjectPosition movingobjectposition1 = current.movingObjectPosition;
			if (movingobjectposition1 != null) {
				vec3.xCoord = movingobjectposition1.hitVec.xCoord;
				vec3.yCoord = movingobjectposition1.hitVec.yCoord;
				vec3.zCoord = movingobjectposition1.hitVec.zCoord;

				movingobjectposition = new MovingObjectPosition(current.entity);
				movingobjectposition.hitVec = vec3;
				movingobjectposition.sideHit = movingobjectposition1.sideHit;
				movingobjectposition.hitInfo = movingobjectposition1.hitInfo;
				avoidEntity = current.entity;
				if (canbounce && !isDead) {
					this.onImpact(movingobjectposition);
					motionX *= 0.5;
					motionY *= 0.5;
					motionZ *= 0.5;
					changemotionflag = true;
				} else {
					this.onImpact(movingobjectposition);
				}
			}
		}
		if(killCNT>0 && fuse < 0)this.setDead();
		int hitside = -1;
		if (movingobjectposition != null) {
			hitedpos = movingobjectposition.hitVec;
			if (movingobjectposition.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY && movingobjectposition.entityHit != null &&  movingobjectposition.hitVec != null) {
			} else if (movingobjectposition.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
				this.onImpact(movingobjectposition);
				this.xTile = movingobjectposition.blockX;
				this.yTile = movingobjectposition.blockY;
				this.zTile = movingobjectposition.blockZ;
				this.inBlock = this.worldObj.getBlock(this.xTile, this.yTile, this.zTile);
				hitside = movingobjectposition.sideHit;
			
				if(canbounce){
					switch (hitside) {//ヒットさせる処理
						case 0:
						case 1://Y面
							if (atan2(sqrt(motionY * motionY), sqrt(motionX * motionX + motionZ * motionZ)) > toRadians(bouncelimit))
								canbounce = false;
							break;
						case 2:
						case 3://Z面
							if (atan2(sqrt(motionZ * motionZ), sqrt(motionX * motionX + motionY * motionY)) > toRadians(bouncelimit))
								canbounce = false;
							break;
						case 4:
						case 5://X MAN
							if (atan2(sqrt(motionX * motionX), sqrt(motionY * motionY + motionZ * motionZ)) > toRadians(bouncelimit))
								canbounce = false;
							break;
					}
				}
				if(!canbounce) {
					this.inGround = true;
					lockedpos = movingobjectposition.hitVec;
				}
				if (this.inBlock.getMaterial() != Material.air) {
					this.inBlock.onEntityCollidedWithBlock(this.worldObj, this.xTile, this.yTile, this.zTile, this);
				}
			}
			if (canbounce) {
				switch (hitside) {
					case 1:
						if(motionY < 0)
							onGround = true;
						motionY = -motionY * bouncerate;
						if(onGround && motionY<0){
							motionY = 0;
						}
						changemotionflag = true;
						break;
					case 0:
						motionY = -motionY * bouncerate;
						changemotionflag = true;
						break;
					case 2:
					case 3:
						motionZ = -motionZ * bouncerate;
						changemotionflag = true;
						break;
					case 4:
					case 5:
						motionX = -motionX * bouncerate;
						changemotionflag = true;
						break;
				}
				lockedpos = null;
				inGround = false;
			}
		}
		if(hitedpos != null) {
			this.posX = hitedpos.xCoord;
			this.posY = hitedpos.yCoord;
			this.posZ = hitedpos.zCoord;
		}else {
			this.posX += this.motionX;
			this.posY += this.motionY;
			this.posZ += this.motionZ;
		}
//			if(inGround && canbounce){
//				this.motionX = backupmotion.xCoord;
//				this.motionY = backupmotion.yCoord;
//				this.motionZ = backupmotion.zCoord;
//				switch (hitside) {
//					case 0:
//					case 1:
//						this.motionY = -backupmotion.yCoord * bouncerate;
//						break;
//					case 2:
//					case 3:
//						this.motionZ = -backupmotion.zCoord * bouncerate;
//						break;
//					case 4:
//					case 5:
//						this.motionX = -backupmotion.xCoord * bouncerate;
//						break;
//				}
//				if(!this.worldObj.isRemote) HMGPacketHandler.INSTANCE.sendToAll(new PacketFixClientbullet(this.getEntityId(),this));
//				//方向転換したのでモーション値をクライアントに送信
//				inGround = false;
//				hitedpos = null;
//			}
		float f2 = (float) getspeed();
		rotationYaw = wrapAngleTo180_float(rotationYaw);
		this.rotationYaw = this.rotationYaw + wrapAngleTo180_float((float) (-atan2(this.motionX, this.motionZ) * 180.0D / Math.PI) - this.rotationYaw)* (1-1 / (1 + f2 * 0.03f));
		this.rotationPitch = this.rotationPitch + ((float) (-atan2(this.motionY, (double) sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ)) * 180.0D / Math.PI) - this.rotationPitch)* (1-1 / (1 + f2 * 0.02f));
		if(bulletStability != 0){
			Vector3d bulletAxis = Utils.getLook2(1,this.rotationYaw,this.rotationPitch);
			Vector3d motionVec2 = Utils.getjavaxVecObj(motionVec);
			bulletAxis.normalize();
			bulletAxis.scale(motionVec2.length());
			motionVec2.interpolate(bulletAxis,bulletStability);
			changemotionflag = true;
			this.motionX = motionVec2.x;
			this.motionY = motionVec2.y;
			this.motionZ = motionVec2.z;
//			if(!worldObj.isRemote)System.out.println("" + this.motionZ);
		}

		
		changemotionflag |= changeVector();
		if(inGround && hitedpos != null) {
			this.posX = hitedpos.xCoord;
			this.posY = hitedpos.yCoord;
			this.posZ = hitedpos.zCoord;
			if(inGround) {
				this.motionX =
						this.motionY =
								this.motionZ = 0;
				changemotionflag |= true;
			}
		}
		if(changemotionflag && !this.worldObj.isRemote) HMGPacketHandler.INSTANCE.sendToAll(new PacketFixClientbullet(this.getEntityId(), this));
	}

	boolean awayFlag = false;
	public boolean forceVT = false;
	public boolean changeVector(){
//		if(!worldObj.isRemote)System.out.println("" + this.motionZ);
		boolean ismotionupdate = false;
		float f3 = resistance;
		
		if (this.isInWater()) {
			for (int l = 0; l < 4; ++l) {
				float f4 = 0.25F;
				this.worldObj.spawnParticle("bubble", this.posX - this.motionX * (double) f4, this.posY - this.motionY * (double) f4, this.posZ - this.motionZ * (double) f4, this.motionX, this.motionY, this.motionZ);
			}
			f3 *= resistanceinwater;
		}
		
		if (this.isWet()) {
			this.extinguish();
		}
		this.motionX *= (double) f3;
		this.motionY *= (double) f3;
		this.motionZ *= (double) f3;
		//ATGM用のSACLOS方式を作る
		//MACLOSは実装しないぞ、絶対
		//相対位置ベクトルと、同じ長さの視線ベクトルの差の方向に誘導すればそれらしいんじゃないか

		if(SACLOS_Homing && thrower != null){
			thrower.getEntityData().setInteger("SACLOS_HOMING",this.getEntityId());
			Vector3d thisPosVec = new Vector3d(this.posX,this.posY,this.posZ);
			Vector3d userPosVec = new Vector3d(thrower.posX,thrower.posY + thrower.getEyeHeight(),thrower.posZ);
			Vector3d userLooking = getjavaxVecObj(thrower.getLookVec());

			Vector3d relativePosVec = new Vector3d();
			relativePosVec.sub(thisPosVec,userPosVec);

			userLooking.scale(relativePosVec.length() + 1 + this.getTerminalspeed()*relativePosVec.dot(userLooking)/relativePosVec.length());

			Vector3d userLookingPosition = new Vector3d(userLooking);
			userLookingPosition.add(userPosVec);

			Vector3d thisMotionVec = new Vector3d(this.motionX, this.motionY, this.motionZ);
			thisMotionVec.scale(-1);

			Vector3d PredictedTargetPos =
					LinePrediction(thisPosVec,
							userLookingPosition,
							thisMotionVec,
							this.getTerminalspeed());

			towardToPos(PredictedTargetPos.x,
					PredictedTargetPos.y,
					PredictedTargetPos.z);

		}else
		if(homingEntity != null) {
//			System.out.println("" + homingEntity);
			NBTTagCompound targetnbt = homingEntity.getEntityData();
			Vector3d thisPosVec = new Vector3d(this.posX,this.posY,this.posZ);
			Vector3d targetVec = new Vector3d(homingEntity.posX, homingEntity.posY + (homingEntity.height/2), homingEntity.posZ);
			Vector3d targetMotionVec = new Vector3d(homingEntity.motionX, homingEntity.motionY, homingEntity.motionZ);
			Vector3d PredictedTargetPos =
					LinePrediction(thisPosVec,
							targetVec,
							targetMotionVec,
					this.getTerminalspeed());
//			{
//				Vector3d toPredicate = new Vector3d(PredictedTargetPos);
//				toPredicate.sub(new Vector3d(homingEntity.posX, homingEntity.posY + (homingEntity.height/2), homingEntity.posZ));
////				System.out.println(toPredicate);
////				System.out.println(rotationYaw + "\t,\t" + rotationPitch);
//			}
			towardToPos(PredictedTargetPos.x,
					PredictedTargetPos.y,
					PredictedTargetPos.z);
			Vector3d toTargetVec = new Vector3d();
			toTargetVec.sub(targetVec,thisPosVec);

			Vector3d thisMotionVec = new Vector3d(this.motionX, this.motionY, this.motionZ);

			Vector3d relativePosition = new Vector3d(PredictedTargetPos);
			relativePosition.sub(thisPosVec);

//			System.out.println("" + awayFlag);

			if((isSemiActive && !isActive) || toDegrees(toTargetVec.angle(getjavaxVecObj(Utils.getLook(1,rotationYaw,rotationPitch))))>seekerwidth){
//				if(!worldObj.isRemote)System.out.println("debug" + homingEntity);
//				if(!worldObj.isRemote)System.out.println("debug" + toDegrees(targetVec.angle(getjavaxVecObj(Utils.getLook(1,rotationYaw,rotationPitch)))));
				resetLock();
			}
//            System.out.println(rotationYaw + "\t,\t" + rotationPitch);
			ismotionupdate = true;

			targetnbt.setBoolean("behome", true);
			if (targetnbt.getBoolean("flare"))
				resetLock();
		}
		if(lockedBlockPos != null){
			towardToPos(lockedBlockPos.xCoord,lockedBlockPos.yCoord,lockedBlockPos.zCoord);
			ismotionupdate = true;
		}
		ismotionupdate |= applyacceleration();
		this.motionY -= (double) gra * cfg_defgravitycof;
		if(onGround && this.motionY < 0)this.motionY = 0;
		if(this.onGround){
			this.motionX *= 0.8;
			this.motionY *= 0.8;
			this.motionZ *= 0.8;
		}
		return ismotionupdate;
	}

	void towardToPos(double targetX,double targetY,double targetZ){
		Vector3d course = new Vector3d(targetX - this.posX, targetY - this.posY, targetZ - this.posZ);

		double dist = course.lengthSquared();

		double[] targetPitch = Utils.CalculateGunElevationAngle(this.posX,this.posY,this.posZ,
				targetX,targetY,targetZ,
				gra * cfg_defgravitycof,
				this.getTerminalspeed());
		course.normalize();
		if(dist > 1000000) {
			if (targetPitch[2] != -1) {
				course.y = sin(toRadians(targetPitch[0]));
			} else if (course.y < 0.707106781186547524f) {
				course.y = 0.707106781186547524f;//1/route(2);
			}
		}
		Vector3d backupmotion;
//				= new Vector3d(motionX,motionY,motionZ);
//		backupmotion.normalize();
		backupmotion = Utils.getLook2(1,this.rotationYaw,this.rotationPitch);
		Vector3d axis = new Vector3d();
		axis.cross(backupmotion,course);
		axis.normalize();
		Quat4d thisRot = new Quat4d(0,0,0,1);
		AxisAngle4d axisyangledy = new AxisAngle4d(unitX, toRadians(this.rotationPitch)/2);
		thisRot = handmadevehicle.Utils.quatRotateAxis(thisRot,axisyangledy);
		AxisAngle4d axisyangledx = new AxisAngle4d(unitY, -toRadians(this.rotationYaw)/2);
		thisRot = handmadevehicle.Utils.quatRotateAxis(thisRot,axisyangledx);

		double rad = acos(backupmotion.dot(course));
		if(toDegrees(rad)>induction_precision){
			rad = toRadians(induction_precision);
			awayFlag = true;
		}else {
			awayFlag = false;
		}
		AxisAngle4d axisyangledChase = new AxisAngle4d(axis, rad/4);
		thisRot = handmadevehicle.Utils.quatRotateAxis(thisRot,axisyangledChase);

		double[] xyz = handmadevehicle.Utils.eulerfromQuat(thisRot);
		if(!Double.isNaN(xyz[0])){
			rotationPitch = (float) -toDegrees(xyz[0]);
		}
		if (!Double.isNaN(xyz[1])) {
			rotationYaw = (float) -toDegrees(xyz[1]);
		}
	}
	private void resetLock(){
		homingEntity = null;
		lockedBlockPos = null;
	}
	public List getEntitiesWithinAABBExcludingEntity(Entity p_72839_1_, AxisAlignedBB p_72839_2_)
	{
		return this.getEntitiesWithinAABBExcludingEntity(p_72839_1_, p_72839_2_, (IEntitySelector)null);
	}

	public List getEntitiesWithinAABBExcludingEntity(Entity p_94576_1_, AxisAlignedBB p_94576_2_, IEntitySelector p_94576_3_)
	{
		ArrayList arraylist = new ArrayList();
		int i = MathHelper.floor_double((p_94576_2_.minX - MAX_ENTITY_RADIUS) / 16.0D);
		int j = MathHelper.floor_double((p_94576_2_.maxX + MAX_ENTITY_RADIUS) / 16.0D);
		int k = MathHelper.floor_double((p_94576_2_.minZ - MAX_ENTITY_RADIUS) / 16.0D);
		int l = MathHelper.floor_double((p_94576_2_.maxZ + MAX_ENTITY_RADIUS) / 16.0D);

		for (int i1 = i; i1 <= j; ++i1)
		{
			for (int j1 = k; j1 <= l; ++j1)
			{
				if (worldObj.getChunkProvider().chunkExists(i1, j1))
				{
					getEntitiesWithinAABBForEntity(worldObj.getChunkFromChunkCoords(i1, j1),p_94576_1_, p_94576_2_, arraylist, p_94576_3_);
				}
			}
		}

		return arraylist;
	}

	public void getEntitiesWithinAABBForEntity(Chunk chunk, Entity p_76588_1_, AxisAlignedBB p_76588_2_, List p_76588_3_, IEntitySelector p_76588_4_)
	{
		int i = MathHelper.floor_double((p_76588_2_.minY - World.MAX_ENTITY_RADIUS) / 16.0D);
		int j = MathHelper.floor_double((p_76588_2_.maxY + World.MAX_ENTITY_RADIUS) / 16.0D);
		i = MathHelper.clamp_int(i, 0, chunk.entityLists.length - 1);
		j = MathHelper.clamp_int(j, 0, chunk.entityLists.length - 1);

		for (int k = i; k <= j; ++k)
		{
			List list1 = chunk.entityLists[k];

			for (int l = 0; l < list1.size(); ++l)
			{
				Entity entity1 = (Entity)list1.get(l);

				if (entity1 != p_76588_1_ && !(entity1 instanceof HMGEntityBulletBase) && entity1.boundingBox.intersectsWith(p_76588_2_) && (p_76588_4_ == null || p_76588_4_.isEntityApplicable(entity1)))
				{
					p_76588_3_.add(entity1);
					Entity[] aentity = entity1.getParts();

					if (aentity != null)
					{
						for (int i1 = 0; i1 < aentity.length; ++i1)
						{
							entity1 = aentity[i1];

							if (entity1 != p_76588_1_ && entity1.boundingBox.intersectsWith(p_76588_2_) && (p_76588_4_ == null || p_76588_4_.isEntityApplicable(entity1)))
							{
								p_76588_3_.add(entity1);
							}
						}
					}
				}
			}
		}
	}



	private ForgeChunkManager.Ticket ticket;
	private final Set<ChunkCoordIntPair> loadedChunks = new HashSet();

	ChunkCoordIntPair myChunk;
	public void forceChunkLoading(int x, int z)
	{
//		System.out.println("debug");
		if(this.worldObj.isRemote)
		{
			//this.setupChunks(x, z);
		}
		else
		{
			if(this.ticket == null)
			{
				if(!this.requestTicket()){
					System.out.println("unable get ticket");
					return;
				}
			}
//            System.out.println(""+(int)fMaid.posX/16 + " , " + (int)fMaid.posZ/16);
			if(!(x == (int)this.posX/16 && z == (int)this.posZ/16))
			{
				this.setupChunks(x, z);
			}
			this.setupChunks(x, z);
			for(ChunkCoordIntPair chunk : this.loadedChunks)
			{
				ForgeChunkManager.forceChunk(this.ticket, chunk);
			}
			myChunk = new ChunkCoordIntPair(x, z);//省くと機能しない
			ForgeChunkManager.forceChunk(this.ticket, myChunk);
		}
	}
	private boolean requestTicket()
	{
		ForgeChunkManager.Ticket chunkTicket = HMVChunkLoaderManager.INSTANCE.getNewTicket(this.worldObj, ForgeChunkManager.Type.ENTITY);
		if(chunkTicket != null)
		{
			int depth = 25;
			chunkTicket.getModData();
			chunkTicket.setChunkListDepth(depth);
			chunkTicket.bindEntity(this);
			this.setChunkTicket(chunkTicket);
			return true;
		}
		System.out.println("[HMG] Failed to get ticket (Chunk Loader)");
		return false;
	}
	public void setChunkTicket(ForgeChunkManager.Ticket par1)
	{
		if(this.ticket != par1)
		{
			ForgeChunkManager.releaseTicket(this.ticket);
		}
		this.ticket = par1;
	}
	private void setupChunks(int xChunk, int zChunk)
	{
		int rad = 1;
		HMVChunkLoaderManager.INSTANCE.getChunksAround(this.loadedChunks, xChunk, zChunk, rad);
	}
}
