package hmggvcmob.entity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import handmadeguns.entity.bullets.HMGEntityBulletExprode;
import hmggvcmob.entity.friend.GVCEntitySoldierRPG;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

import java.util.List;

import static handmadeguns.HandmadeGunsCore.cfg_defgravitycof;

public class EntitySupportTGT extends Entity {
    private int field_145791_d = -1;
    private int field_145792_e = -1;
    private int field_145789_f = -1;
    private Block field_145790_g;
    private int inData;
    private boolean inGround;
    /** 1 if the player can pick up the arrow */
    public int canBePickedUp;
    /** Seems to be some sort of timer for animating an arrow. */
    public int arrowShake;
    /** The owner of this arrow. */
    public EntityLivingBase shootingEntity;
    public int type;
    private int ticksInGround;
    private int ticksInAir;
    private double damage = 2.0D;
    /** The amount of knockback an arrow applies when it hits a mob. */
    private int knockbackStrength;
    private static final String __OBFID = "CL_00001715";
    public int fuse;
    public double deathposX;
    public double deathposY;
    public double deathposZ;

    public EntitySupportTGT(World p_i1753_1_)
    {
        super(p_i1753_1_);
        this.renderDistanceWeight = 10.0D;
        this.setSize(0.5F, 0.5F);
        this.fuse = 32;
    }

    public EntitySupportTGT(World p_i1754_1_, double p_i1754_2_, double p_i1754_4_, double p_i1754_6_)
    {
        super(p_i1754_1_);
        this.renderDistanceWeight = 10.0D;
        this.setSize(0.5F, 0.5F);
        this.setPosition(p_i1754_2_, p_i1754_4_, p_i1754_6_);
        this.yOffset = 0.0F;
        this.fuse = 32;
    }

    public EntitySupportTGT(World p_i1755_1_, EntityLivingBase p_i1755_2_, EntityLivingBase p_i1755_3_, float p_i1755_4_, float p_i1755_5_)
    {
        super(p_i1755_1_);
        this.renderDistanceWeight = 10.0D;
        this.shootingEntity = p_i1755_2_;
        this.fuse = 32;

        if (p_i1755_2_ instanceof EntityPlayer)
        {
            this.canBePickedUp = 1;
        }

        this.posY = p_i1755_2_.posY + (double)p_i1755_2_.getEyeHeight() - 0.10000000149011612D;
        double d0 = p_i1755_3_.posX - p_i1755_2_.posX;
        double d1 = p_i1755_3_.boundingBox.minY + (double)(p_i1755_3_.height / 3.0F) - this.posY;
        double d2 = p_i1755_3_.posZ - p_i1755_2_.posZ;
        double d3 = (double) MathHelper.sqrt_double(d0 * d0 + d2 * d2);

        if (d3 >= 1.0E-7D)
        {
            float f2 = (float)(Math.atan2(d2, d0) * 180.0D / Math.PI) - 90.0F;
            float f3 = (float)(-(Math.atan2(d1, d3) * 180.0D / Math.PI));
            double d4 = d0 / d3;
            double d5 = d2 / d3;
            this.setLocationAndAngles(p_i1755_2_.posX + d4, this.posY, p_i1755_2_.posZ + d5, f2, f3);
            this.yOffset = 0.0F;
            float f4 = (float)d3 * 0.2F;
            this.setThrowableHeading(d0, d1 + (double)f4, d2, p_i1755_4_, p_i1755_5_);
        }
    }

    public EntitySupportTGT(World p_i1756_1_, EntityLivingBase p_i1756_2_, float p_i1756_3_)
    {
        super(p_i1756_1_);
        this.renderDistanceWeight = 100.0D;
        this.shootingEntity = p_i1756_2_;
        this.fuse = 32;

        if (p_i1756_2_ instanceof EntityPlayer)
        {
            this.canBePickedUp = 1;
        }

        this.setSize(0.1F, 0.1F);
        this.setLocationAndAngles(p_i1756_2_.posX, p_i1756_2_.posY + (double)p_i1756_2_.getEyeHeight(), p_i1756_2_.posZ, p_i1756_2_.rotationYaw, p_i1756_2_.rotationPitch);
        this.posX -= (double)(MathHelper.cos(this.rotationYaw / 180.0F * (float)Math.PI) * 0.16F);
        this.posY -= 0.10000000149011612D;
        this.posZ -= (double)(MathHelper.sin(this.rotationYaw / 180.0F * (float)Math.PI) * 0.16F);
        this.setPosition(this.posX, this.posY, this.posZ);
        this.yOffset = 0.0F;
        this.motionX = (double)(-MathHelper.sin(this.rotationYaw / 180.0F * (float)Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float)Math.PI));
        this.motionZ = (double)(MathHelper.cos(this.rotationYaw / 180.0F * (float)Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float)Math.PI));
        this.motionY = (double)(-MathHelper.sin(this.rotationPitch / 180.0F * (float)Math.PI));
        this.setThrowableHeading(this.motionX, this.motionY, this.motionZ, p_i1756_3_ * 1.5F, 1.0F);
    }

    protected void entityInit()
    {
        this.dataWatcher.addObject(16, Byte.valueOf((byte)0));
    }

    /**
     * Similar to setArrowHeading, it's point the throwable entity to a x, y, z direction.
     */
    public void setThrowableHeading(double p_70186_1_, double p_70186_3_, double p_70186_5_, float p_70186_7_, float p_70186_8_)
    {
        float f2 = MathHelper.sqrt_double(p_70186_1_ * p_70186_1_ + p_70186_3_ * p_70186_3_ + p_70186_5_ * p_70186_5_);
        p_70186_1_ /= (double)f2;
        p_70186_3_ /= (double)f2;
        p_70186_5_ /= (double)f2;
        p_70186_1_ += this.rand.nextGaussian() * (double)(this.rand.nextBoolean() ? -1 : 1) * 0.0D * (double)p_70186_8_;
        p_70186_3_ += this.rand.nextGaussian() * (double)(this.rand.nextBoolean() ? -1 : 1) * 0.0D * (double)p_70186_8_;
        p_70186_5_ += this.rand.nextGaussian() * (double)(this.rand.nextBoolean() ? -1 : 1) * 0.0D * (double)p_70186_8_;
        p_70186_1_ *= (double)p_70186_7_;
        p_70186_3_ *= (double)p_70186_7_;
        p_70186_5_ *= (double)p_70186_7_;
        this.motionX = p_70186_1_;
        this.motionY = p_70186_3_;
        this.motionZ = p_70186_5_;
        float f3 = MathHelper.sqrt_double(p_70186_1_ * p_70186_1_ + p_70186_5_ * p_70186_5_);
        this.prevRotationYaw = this.rotationYaw = (float)(Math.atan2(p_70186_1_, p_70186_5_) * 180.0D / Math.PI);
        this.prevRotationPitch = this.rotationPitch = (float)(Math.atan2(p_70186_3_, (double)f3) * 180.0D / Math.PI);
        this.ticksInGround = 0;
    }

    /**
     * Sets the position and rotation. Only difference from the other one is no bounding on the rotation. Args: posX,
     * posY, posZ, yaw, pitch
     */
    @SideOnly(Side.CLIENT)
    public void setPositionAndRotation2(double p_70056_1_, double p_70056_3_, double p_70056_5_, float p_70056_7_, float p_70056_8_, int p_70056_9_)
    {
        this.setPosition(p_70056_1_, p_70056_3_, p_70056_5_);
        this.setRotation(p_70056_7_, p_70056_8_);
    }

    /**
     * Sets the velocity to the args. Args: x, y, z
     */
    @SideOnly(Side.CLIENT)
    public void setVelocity(double p_70016_1_, double p_70016_3_, double p_70016_5_)
    {
        this.motionX = p_70016_1_;
        this.motionY = p_70016_3_;
        this.motionZ = p_70016_5_;

        if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F)
        {
            float f = MathHelper.sqrt_double(p_70016_1_ * p_70016_1_ + p_70016_5_ * p_70016_5_);
            this.prevRotationYaw = this.rotationYaw = (float)(Math.atan2(p_70016_1_, p_70016_5_) * 180.0D / Math.PI);
            this.prevRotationPitch = this.rotationPitch = (float)(Math.atan2(p_70016_3_, (double)f) * 180.0D / Math.PI);
            this.prevRotationPitch = this.rotationPitch;
            this.prevRotationYaw = this.rotationYaw;
            this.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
            this.ticksInGround = 0;
        }
    }


    @SideOnly(Side.CLIENT)
    public int getBrightnessForRender(float par1)
    {
        return 15728880;
    }

    public float getBrightness(float par1)
    {
        return 1.0F;
    }

    protected boolean isValidLightLevel()
    {
        return true;
    }


    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate()
    {
        super.onUpdate();
        if(!this.worldObj.isRemote && this.shootingEntity == null){
            setDead();
        }
        if(!worldObj.getChunkFromBlockCoords((int)posX - 1 ,(int)posZ - 1).isChunkLoaded)this.setDead();
        Block block = this.worldObj.getBlock(this.field_145791_d, this.field_145792_e, this.field_145789_f);

        if (block.getMaterial() != Material.air)
        {
            block.setBlockBoundsBasedOnState(this.worldObj, this.field_145791_d, this.field_145792_e, this.field_145789_f);
            AxisAlignedBB axisalignedbb = block.getCollisionBoundingBoxFromPool(this.worldObj, this.field_145791_d, this.field_145792_e, this.field_145789_f);

            if (axisalignedbb != null && axisalignedbb.isVecInside(Vec3.createVectorHelper(this.posX, this.posY, this.posZ)))
            {
                this.inGround = true;
            }
        }

        if (this.arrowShake > 0)
        {
            --this.arrowShake;
        }

        if (this.inGround)
        {
            int j = this.worldObj.getBlockMetadata(this.field_145791_d, this.field_145792_e, this.field_145789_f);

            if (block == this.field_145790_g && j == this.inData)
            {
                ++this.ticksInGround;

                if (this.ticksInGround == 5)
                {
                    this.setDead();
                }
            }
            else
            {
                this.inGround = false;
                //this.motionX *= (double)(this.rand.nextFloat() * 0.2F);
                //this.motionY *= (double)(this.rand.nextFloat() * 0.2F);
                //this.motionZ *= (double)(this.rand.nextFloat() * 0.2F);
                this.ticksInGround = 0;
                this.ticksInAir = 0;
            }
        }
        else
        {
            ++this.ticksInAir;
            Vec3 vec31 = Vec3.createVectorHelper(this.posX, this.posY, this.posZ);
            Vec3 vec3 = Vec3.createVectorHelper(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
            MovingObjectPosition movingobjectposition = this.worldObj.func_147447_a(vec31, vec3, false, true, false);
            vec31 = Vec3.createVectorHelper(this.posX, this.posY, this.posZ);
            vec3 = Vec3.createVectorHelper(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);

            if (movingobjectposition != null)
            {
                vec3 = Vec3.createVectorHelper(movingobjectposition.hitVec.xCoord, movingobjectposition.hitVec.yCoord, movingobjectposition.hitVec.zCoord);
            }

            Entity entity = null;
            List list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox.addCoord(this.motionX, this.motionY, this.motionZ).expand(1.0D, 1.0D, 1.0D));
            double d0 = 0.0D;
            int i;
            float f1;

            for (i = 0; i < list.size(); ++i)
            {
                Entity entity1 = (Entity)list.get(i);

                if (entity1.canBeCollidedWith() && ((this.shootingEntity != null && entity1 != this.shootingEntity.riddenByEntity &&entity1 != this.shootingEntity.ridingEntity && entity1 != this.shootingEntity) || this.ticksInAir >= 5))
                {
                    f1 = 0.3F;
                    AxisAlignedBB axisalignedbb1 = entity1.boundingBox.expand((double)f1, (double)f1, (double)f1);
                    MovingObjectPosition movingobjectposition1 = axisalignedbb1.calculateIntercept(vec31, vec3);

                    if (movingobjectposition1 != null)
                    {
                        double d1 = vec31.distanceTo(movingobjectposition1.hitVec);

                        if (d1 < d0 || d0 == 0.0D)
                        {
                            entity = entity1;
                            d0 = d1;
                        }
                    }
                }
            }

            if (entity != null)
            {
                movingobjectposition = new MovingObjectPosition(entity);
            }
            if(!worldObj.isRemote && shootingEntity != null) {
                if (movingobjectposition != null) {
                    if (movingobjectposition.entityHit != null) {
                        this.field_145791_d = (int) movingobjectposition.entityHit.posX + 1;
                        this.field_145792_e = (int) movingobjectposition.entityHit.posY + 1;
                        this.field_145789_f = (int) movingobjectposition.entityHit.posZ + 1;
                        setDead();
                    } else {
                        this.field_145791_d = movingobjectposition.blockX;
                        this.field_145792_e = movingobjectposition.blockY;
                        this.field_145789_f = movingobjectposition.blockZ;
                        setDead();
                    }
                    switch (type) {
                        case 0: {
                            if (!this.worldObj.isRemote) {
                                if(shootingEntity instanceof EntityPlayerMP) ((EntityPlayerMP) shootingEntity).addChatComponentMessage(new ChatComponentTranslation(
                                        "roger that! We will support artillery toward X = "+field_145791_d+", Z = "+field_145789_f+"!"));
                                for(int l= 0;l<4;l++) {
                                    HMGEntityBulletExprode var3 = new HMGEntityBulletExprode(this.worldObj, shootingEntity, 100, 0, 0);
                                    var3.canbounce = false;
                                    var3.fuse = 0;
                                    var3.gra = (float) (0.49 / cfg_defgravitycof);
                                    var3.canex = true;
                                    var3.bulletTypeName = "byfrou01_Bomb";
                                    var3.setLocationAndAngles(field_145791_d + rand.nextInt(20) * (this.rand.nextBoolean() ? -1 : 1) / 6, shootingEntity.posY + 255D, field_145789_f + rand.nextInt(20) * (this.rand.nextBoolean() ? -1 : 1) / 6,
                                            0, 90);
                                    var3.setHeadingFromThrower(0, 0, 0, 0, 0);
                                    var3.motionX = var3.motionY = var3.motionZ = 0;
                                    this.worldObj.spawnEntityInWorld(var3);
                                }
                            }
                            break;
                        }
                        case 1: {
                            if (!this.worldObj.isRemote) {
                                if (shootingEntity instanceof EntityPlayerMP)
                                    ((EntityPlayerMP) shootingEntity).addChatComponentMessage(new ChatComponentTranslation(
                                            "roger that! we send a gun ship to X= " + field_145791_d + ", Z = " + field_145789_f + "!"));
                                GVCEntitySoldierRPG entityskeleton = new GVCEntitySoldierRPG(worldObj);
                                Vec3 playersdir = Vec3.createVectorHelper(shootingEntity.posX - field_145791_d,shootingEntity.posY - field_145792_e,shootingEntity.posZ - field_145789_f);
                                playersdir = playersdir.normalize();
                                entityskeleton.setLocationAndAngles(field_145791_d + playersdir.xCoord * 80, field_145792_e + 100, field_145789_f + playersdir.zCoord * 80, shootingEntity.rotationYawHead, 0.0F);
                                entityskeleton.summoningVehicle = "Mi-24";
                                entityskeleton.makePlatoon();
                                entityskeleton.setTargetCampPosition(new int[]{field_145791_d, field_145792_e, field_145789_f});
                                this.worldObj.spawnEntityInWorld(entityskeleton);
                            }
                            break;
                        }
                        case 2: {
                            if (!this.worldObj.isRemote) {
                                if (shootingEntity instanceof EntityPlayerMP)
                                    ((EntityPlayerMP) shootingEntity).addChatComponentMessage(new ChatComponentTranslation(
                                            "roger that! we send a fighter to X= " + field_145791_d + ", Z = " + field_145789_f + "!"));
                                GVCEntitySoldierRPG entityskeleton = new GVCEntitySoldierRPG(worldObj);
                                Vec3 playersdir = Vec3.createVectorHelper(shootingEntity.posX - field_145791_d,shootingEntity.posY - field_145792_e,shootingEntity.posZ - field_145789_f);
                                playersdir = playersdir.normalize();
                                entityskeleton.setLocationAndAngles(field_145791_d + playersdir.xCoord * 80, field_145792_e + 140, field_145789_f + playersdir.zCoord * 80, shootingEntity.rotationYawHead, 0.0F);
                                entityskeleton.summoningVehicle = "MiG-29";
                                entityskeleton.makePlatoon();
                                entityskeleton.setTargetCampPosition(new int[]{field_145791_d, field_145792_e, field_145789_f});
                                this.worldObj.spawnEntityInWorld(entityskeleton);
                            }
                            break;
                        }
                        case 3: {
                            if (!this.worldObj.isRemote) {
                                if (shootingEntity instanceof EntityPlayerMP)
                                    ((EntityPlayerMP) shootingEntity).addChatComponentMessage(new ChatComponentTranslation(
                                            "roger that! we send a Attacker to X= " + field_145791_d + ", Z = " + field_145789_f + "!"));
                                GVCEntitySoldierRPG entityskeleton = new GVCEntitySoldierRPG(worldObj);
                                Vec3 playersdir = Vec3.createVectorHelper(shootingEntity.posX - field_145791_d,shootingEntity.posY - field_145792_e,shootingEntity.posZ - field_145789_f);
                                playersdir = playersdir.normalize();
                                entityskeleton.setLocationAndAngles(field_145791_d + playersdir.xCoord * 80, field_145792_e + 100, field_145789_f + playersdir.zCoord * 80, shootingEntity.rotationYawHead, 0.0F);
                                entityskeleton.summoningVehicle = "SU-25";
                                entityskeleton.makePlatoon();
                                entityskeleton.setTargetCampPosition(new int[]{field_145791_d, field_145792_e, field_145789_f});
                                this.worldObj.spawnEntityInWorld(entityskeleton);
                            }
                            break;
                        }
                    }
                    inGround = true;
                }
            }

            this.posX += this.motionX;
            this.posY += this.motionY;
            this.posZ += this.motionZ;
            this.setPosition(posX,posY,posZ);
        }



        if (this.fuse-- <= 0)
        {
            this.setDead();
        }

    }



    @Override
    public void setDead() {
        super.setDead();

        worldObj.setLightValue(EnumSkyBlock.Block, field_145791_d, field_145792_e, field_145789_f, 0x00);
        worldObj.func_147451_t(field_145791_d - 1, field_145792_e, field_145789_f);
        worldObj.func_147451_t(field_145791_d + 1, field_145792_e, field_145789_f);
        worldObj.func_147451_t(field_145791_d, field_145792_e - 1, field_145789_f);
        worldObj.func_147451_t(field_145791_d, field_145792_e + 1, field_145789_f);
        worldObj.func_147451_t(field_145791_d, field_145792_e, field_145789_f - 1);
        worldObj.func_147451_t(field_145791_d, field_145792_e, field_145789_f + 1);
        worldObj.func_147451_t(field_145791_d, field_145792_e, field_145789_f);


        this.deathposX = this.posX;
        this.deathposY = this.posY;
        this.deathposZ = this.posZ;

    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    public void writeEntityToNBT(NBTTagCompound p_70014_1_)
    {
        p_70014_1_.setShort("xTile", (short)this.field_145791_d);
        p_70014_1_.setShort("yTile", (short)this.field_145792_e);
        p_70014_1_.setShort("zTile", (short)this.field_145789_f);
        p_70014_1_.setShort("life", (short)this.ticksInGround);
        p_70014_1_.setByte("inTile", (byte)Block.getIdFromBlock(this.field_145790_g));
        p_70014_1_.setByte("inData", (byte)this.inData);
        p_70014_1_.setByte("shake", (byte)this.arrowShake);
        p_70014_1_.setByte("inGround", (byte)(this.inGround ? 1 : 0));
        p_70014_1_.setByte("pickup", (byte)this.canBePickedUp);
        p_70014_1_.setDouble("damage", this.damage);
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readEntityFromNBT(NBTTagCompound p_70037_1_)
    {
        this.field_145791_d = p_70037_1_.getShort("xTile");
        this.field_145792_e = p_70037_1_.getShort("yTile");
        this.field_145789_f = p_70037_1_.getShort("zTile");
        this.ticksInGround = p_70037_1_.getShort("life");
        this.field_145790_g = Block.getBlockById(p_70037_1_.getByte("inTile") & 255);
        this.inData = p_70037_1_.getByte("inData") & 255;
        this.arrowShake = p_70037_1_.getByte("shake") & 255;
        this.inGround = p_70037_1_.getByte("inGround") == 1;

        if (p_70037_1_.hasKey("damage", 99))
        {
            this.damage = p_70037_1_.getDouble("damage");
        }

        if (p_70037_1_.hasKey("pickup", 99))
        {
            this.canBePickedUp = p_70037_1_.getByte("pickup");
        }
        else if (p_70037_1_.hasKey("player", 99))
        {
            this.canBePickedUp = p_70037_1_.getBoolean("player") ? 1 : 0;
        }
    }

    /**
     * Called by a player entity when they collide with an entity
     */
    public void onCollideWithPlayer(EntityPlayer p_70100_1_)
    {

    }
    @SideOnly(Side.CLIENT)
    public float getShadowSize()
    {
        return 0.0F;
    }

}
