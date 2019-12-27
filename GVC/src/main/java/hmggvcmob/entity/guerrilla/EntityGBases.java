package hmggvcmob.entity.guerrilla;

import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.common.registry.GameRegistry;
import handmadeguns.HMGGunMaker;
import handmadeguns.entity.IFF;
import handmadeguns.entity.PlacedGunEntity;
import handmadeguns.items.guns.HMGItem_Unified_Guns;
import handmadevehicle.SlowPathFinder.WorldForPathfind;
import handmadevehicle.entity.EntityDummy_rider;
import handmadevehicle.entity.EntityVehicle;
import handmadevehicle.entity.parts.ITurretUser;
import handmadevehicle.entity.parts.turrets.TurretObj;
import handmadevehicle.entity.prefab.Prefab_Vehicle_Base;
import hmggvcmob.GVCMobPlus;
import hmggvcmob.IflagBattler;
import handmadevehicle.SlowPathFinder.ModifiedPathNavigater;
import hmggvcmob.ai.*;
import hmggvcmob.camp.CampObj;
import hmggvcmob.entity.EntityBodyHelper_modified;
import hmggvcmob.entity.EntityMoveHelperModified;
import hmggvcmob.entity.IGVCmob;
import hmggvcmob.entity.VehicleSpawnGachaOBJ;
import hmggvcmob.entity.friend.EntitySoBases;
import hmggvcmob.entity.friend.GVCEntityFlag;
import net.minecraft.block.Block;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.scoreboard.Team;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static handmadeguns.Util.Utils.getmovingobjectPosition_forBlock;
import static handmadevehicle.Utils.canMoveEntity;
import static handmadevehicle.entity.EntityVehicle.EntityVehicle_spawnByMob;
import static hmggvcmob.GVCMobPlus.*;
import static java.lang.Math.abs;
import static java.lang.Math.pow;
import static net.minecraft.util.MathHelper.wrapAngleTo180_float;

public class EntityGBases extends EntityMob implements IflagBattler,IGVCmob, IFF , ITurretUser {
    private EntityBodyHelper_modified bodyHelper;
    public float viewWide = 0.75f;
    public int staningtime;
    public float spread = 10;
    public double movespeed = 0.3d;
    int placing;
    boolean canuseAlreadyPlacedGun = true;
    boolean canusePlacedGun = true;
    public String summoningVehicle;//nullは無し。自然湧きはLivingSpawnEvent.SpecialSpawnで設定する
    public static VehicleSpawnGachaOBJ[] vehicleSpawnGachaOBJ;
    public static int vehilceGacha_rate_sum;
    public boolean candespawn = true;
	public int deathTicks;
	public int type = 0;
	public Block flag;


    public int flagx;
    public int flagy;
    public int flagz;
    
    private ModifiedPathNavigater modifiedPathNavigater;
    WorldForPathfind worldForPathfind;
    
    EntityAISwimming aiSwimming;
    AIattackOnCollide AIattackOncollidetoPlayer;
    AIattackOnCollide              AIattackOncollidetoVillager;
    AIattackOnCollide              AIattackOncollidetoSoldier;
    EntityAIRestrictOpenDoor AIRestrictOpenDoor;
    net.minecraft.entity.ai.EntityAIOpenDoor EntityAIOpenDoor;
    EntityAIMoveTowardsRestriction AIMoveTowardsRestriction;
    hmggvcmob.ai.AIMoveThroughVillage AIMoveThroughVillage;
    AIAttackGun aiAttackGun;
    
    public EntityGBases(World par1World) {
        super(par1World);
        canRideVehicle = true;
        this.bodyHelper = new EntityBodyHelper_modified(this);
        this.moveHelper = new EntityMoveHelperModified(this);
        renderDistanceWeight = 16384;
        this.worldForPathfind = new WorldForPathfind(worldObj);
        this.modifiedPathNavigater = new ModifiedPathNavigater(this, worldObj,worldForPathfind);



        ObfuscationReflectionHelper.setPrivateValue(EntityLiving.class, this, moveHelper, "moveHelper", "field_70765_h");
        ObfuscationReflectionHelper.setPrivateValue(EntityLiving.class, this, modifiedPathNavigater, "navigator", "field_70699_by");

        aiSwimming =new EntityAISwimming(this);
        AIattackOncollidetoPlayer  =new AIattackOnCollide(this, EntityPlayer.class, 1.0D, true);
        AIattackOncollidetoVillager= new AIattackOnCollide(this, EntityVillager.class, 1.0D, true);
        AIattackOncollidetoSoldier =new AIattackOnCollide(this, EntitySoBases.class, 1.0D, true);
        AIRestrictOpenDoor         =new EntityAIRestrictOpenDoor(this);
        EntityAIOpenDoor           =new EntityAIOpenDoor(this, true);
        AIMoveTowardsRestriction   =new EntityAIMoveTowardsRestriction(this, 1.0D);
        AIMoveThroughVillage       =new AIMoveThroughVillage(this, 1.0D, false);
        this.getNavigator().setBreakDoors(true);
        this.tasks.addTask(0, aiSwimming);
        this.tasks.addTask(0, new AIAttackByTank(this, null, worldForPathfind, this));
        this.tasks.addTask(3, AIattackOncollidetoPlayer);
        this.tasks.addTask(3, AIattackOncollidetoVillager);
        this.tasks.addTask(3, AIattackOncollidetoSoldier);
        this.tasks.addTask(3, new AIAttackFlag(this, this,worldForPathfind));
        this.tasks.addTask(3, new AIDriveTank(this, null, worldForPathfind));
        this.tasks.addTask(4, AIRestrictOpenDoor);
        this.tasks.addTask(5, EntityAIOpenDoor);
        this.tasks.addTask(6, AIMoveTowardsRestriction);
        this.tasks.addTask(7, AIMoveThroughVillage);
        //こっから先は待機時（？）
        this.tasks.addTask(8, new EntityAIWander(this, 0.3));
        this.tasks.addTask(8, new EntityAILookIdle(this));
        //ターゲティング
        this.targetTasks.addTask(1, new AIHurtByTarget(this, true));
        this.targetTasks.addTask(2, new AINearestAttackableTarget(this, EntityPlayer.class, 0, true));
        this.targetTasks.addTask(2, new AINearestAttackableTarget(this, EntitySoBases.class, 0, true));
        this.targetTasks.addTask(2, new AINearestAttackableTarget(this, EntityVillager.class, 0, true));
        this.targetTasks.addTask(2, new AINearestAttackableTarget(this, EntityGolem.class, 0, true));
        this.targetTasks.addTask(2, new AINearestAttackableTarget(this, GVCEntityFlag.class, 0, false));
        this.targetTasks.addTask(4, new AITargetFlag(this,this,this));
        this.setCanPickUpLoot(true);
    }

    protected float func_110146_f(float p_110146_1_, float p_110146_2_)
    {
        if (this.isAIEnabled())
        {
            this.bodyHelper.func_75664_a();
            return p_110146_2_;
        }
        else
        {
            return super.func_110146_f(p_110146_1_, p_110146_2_);
        }
    }

    public boolean attackEntityFrom(DamageSource source, float par2)
    {
        if(!worldObj.isRemote) {
            if (this.getHeldItem() != null && (source.getEntity() != getAttackTarget()||par2>7) && (source.getDamageType().equals("mob") || source.getDamageType().equals("player"))) {
                this.entityDropItem(this.getHeldItem(), 1);
                if (this.getHeldItem().getItem() instanceof HMGItem_Unified_Guns && ((HMGItem_Unified_Guns) this.getHeldItem().getItem()).getcurrentMagazine(this.getHeldItem()) != null) {
                    this.dropItem(((HMGItem_Unified_Guns) this.getHeldItem().getItem()).getcurrentMagazine(this.getHeldItem()), 1);
                }
                staningtime = 10;
                this.setCurrentItemOrArmor(0, null);
            }
        }
        return super.attackEntityFrom(source, par2);
        
    }
    public void dropMagazine(){
        if (this.getHeldItem().getItem() instanceof HMGItem_Unified_Guns && ((HMGItem_Unified_Guns) this.getHeldItem().getItem()).getcurrentMagazine(this.getHeldItem()) != null) {
            this.dropItem(((HMGItem_Unified_Guns) this.getHeldItem().getItem()).getcurrentMagazine(this.getHeldItem()), 1);
        }
    }
    
    
    
    public void onLivingUpdate()
    {
        this.updateArmSwingProgress();
        float f = this.getBrightness(1.0F);
        
        if (f > 0.5F)
        {
            this.entityAge += 2;
        }
        super.onLivingUpdate();
    }
    public int getVerticalFaceSpeed()
    {
        return 90;
    }
    
    public PathNavigate getNavigator()
    {
        return this.modifiedPathNavigater;
    }
    private EntityMoveHelperModified moveHelper;
    protected void updateAITasks()
    {
        super.updateAITasks();
    }
    public float moveToDir = 0;
    public void setAIMoveSpeed(float p_70659_1_)
    {
        super.setAIMoveSpeed(p_70659_1_);
        this.setMoveForward(p_70659_1_);
        moveToDir = this.rotationYaw;
    }
    public void moveEntityWithHeading(float p_70612_1_, float p_70612_2_){
        this.rotationYaw = moveToDir;
        super.moveEntityWithHeading(p_70612_1_,p_70612_2_);
    }

    public void moveFlying(float p_70060_1_, float p_70060_2_, float p_70060_3_)
    {
        float f3 = p_70060_1_ * p_70060_1_ + p_70060_2_ * p_70060_2_;

        if (f3 >= 1.0E-4F)
        {
            f3 = MathHelper.sqrt_float(f3);

            if (f3 < 1.0F)
            {
                f3 = 1.0F;
            }
            f3 = p_70060_3_ / f3;
            f3 = abs(f3);
            p_70060_1_ *= f3;
            p_70060_2_ *= f3;
            float f4 = MathHelper.sin(this.rotationYaw * (float)Math.PI / 180.0F);
            float f5 = MathHelper.cos(this.rotationYaw * (float)Math.PI / 180.0F);
            this.motionX += (double)(-p_70060_2_ * f4 + p_70060_1_ * f5);
            this.motionZ += (double)( p_70060_2_ * f5 + p_70060_1_ * f4);
        }
    }
    public EntityMoveHelper getMoveHelper()
    {
        return this.moveHelper;
    }
	/**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readEntityFromNBT(NBTTagCompound p_70037_1_)
    {
        super.readEntityFromNBT(p_70037_1_);
    }
	/**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    public void writeEntityToNBT(NBTTagCompound p_70014_1_)
    {
        super.writeEntityToNBT(p_70014_1_);
    }
    protected void entityInit() {
		super.entityInit();
    }


    
    
    public boolean CanAttack(Entity entity){
    	return true;
    }
	public void onUpdate()
    {
        super.onUpdate();
        if(summoningVehicle != null) {
            //todo 車両スポーン処理を入れる

            int var12 = MathHelper.floor_double((double) (this.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
            System.out.println(summoningVehicle);
            EntityVehicle bespawningEntity = EntityVehicle_spawnByMob(worldObj,summoningVehicle);
            bespawningEntity.setLocationAndAngles(this.posX, this.posY, this.posZ, var12 , 0.0F);
            if(bespawningEntity.checkObstacle()) {
                worldObj.spawnEntityInWorld(bespawningEntity);
                if(bespawningEntity.pickupEntity(this,0)) {
                }
                bespawningEntity.canUseByMob = true;
                bespawningEntity.despawn = true;
                Prefab_Vehicle_Base prefab_vehicle = bespawningEntity.getBaseLogic().prefab_vehicle;
                for(int slotID = 0 ;slotID < prefab_vehicle.weaponSlotNum;slotID++) {
                    if(prefab_vehicle.weaponSlot_linkedTurret_StackWhiteList.get(slotID) != null) {
                        int randUsingSlot = rand.nextInt(prefab_vehicle.weaponSlot_linkedTurret_StackWhiteList.get(slotID).length);
                        {
                            String whiteList = prefab_vehicle.weaponSlot_linkedTurret_StackWhiteList.get(slotID)[randUsingSlot];
                            Item check = GameRegistry.findItem("HandmadeGuns", whiteList);
                            if (check instanceof HMGItem_Unified_Guns && ((HMGItem_Unified_Guns) check).gunInfo.guerrila_can_use) {
                                bespawningEntity.getBaseLogic().inventoryVehicle.setInventorySlotContents(slotID, new ItemStack(check));
                            }
                        }
                    }else{
                        int randUsingSlot = rand.nextInt(GVCMobPlus.Guns_CanUse.size());
                        Item choosenGun = GVCMobPlus.Guns_CanUse.get(randUsingSlot);
                        bespawningEntity.getBaseLogic().inventoryVehicle.setInventorySlotContents(slotID, new ItemStack(choosenGun));
                    }
                }
                if(!prefab_vehicle.T_Land_F_Plane){
                    bespawningEntity.setLocationAndAngles(this.posX, 128, this.posZ, var12 , 0.0F);
                    bespawningEntity.getBaseLogic().throttle = prefab_vehicle.throttle_Max;
                }
            }
            summoningVehicle = null;
        }
        if(this.getAttackTarget() != null && this.getAttackTarget().isDead)this.setAttackTarget(null);

        if (!this.worldObj.isRemote && (this.worldObj.difficultySetting == EnumDifficulty.PEACEFUL && this.candespawn)) {
            this.setDead();
        }
    
        staningtime--;
        this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(movespeed);
    
    
        {
            List PlaceGunDetector = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox.expand(2, 3.0D, 2));
        
            if (PlaceGunDetector != null && !PlaceGunDetector.isEmpty()) {
                for (int i = 0; i < PlaceGunDetector.size(); ++i) {
                    if(cfg_guerrillacanusePlacedGun && canuseAlreadyPlacedGun && !worldObj.isRemote && ridingEntity == null && this.getAttackTarget() != null) {
                        Entity colliedentity = (Entity) PlaceGunDetector.get(i);
                        if (colliedentity.riddenByEntity == null && colliedentity instanceof PlacedGunEntity) {
                            placing++;
                            if (placing > 60) {
                                placing = 0;
                                this.mountEntity((PlacedGunEntity) colliedentity);
                            }
                            this.setCurrentItemOrArmor(0, null);
                            break;
                        }
                    }
                }
            }
        }
        if(this.getHeldItem() != null && this.aiAttackGun != null){
            this.getHeldItem().getItem().onUpdate(this.getHeldItem(),worldObj,this,0,true);
            if(!worldObj.isRemote && cfg_guerrillacanusePlacedGun && canusePlacedGun && ridingEntity == null && onGround &&this.getAttackTarget() != null && this.getHeldItem().getItem()instanceof HMGItem_Unified_Guns && ((HMGItem_Unified_Guns) this.getHeldItem().getItem()).gunInfo.fixAsEntity){
                placing ++;
                if(placing>60) {
                    placing = 0;
                    //視線方向のブロックを調べる
                    int headdirction_four = getdirection_in4(
                            wrapAngleTo180_float(rotationYawHead));
//					System.out.println("debug" + headdirction_four);
                    Block willGunSetBlock = null;
                    int[] offset = new int[2];
                    switch (headdirction_four) {
                        case 0:
                            //south
                            willGunSetBlock = worldObj.getBlock((int) posX-1, (int) posY-1, (int) posZ);
                            offset[0] = -1;
                            offset[1] = 0;
                            break;
                        case -1:
                            //east
                            willGunSetBlock = worldObj.getBlock((int) posX, (int) posY-1, (int) posZ-1);
                            offset[0] = 0;
                            offset[1] = -1;
                            break;
                        case 1:
                            //west
                            willGunSetBlock = worldObj.getBlock((int) posX-2, (int) posY-1, (int) posZ-1);
                            offset[0] = -2;
                            offset[1] = -1;
                            break;
                        case 2:
                            //north
                            willGunSetBlock = worldObj.getBlock((int) posX-1, (int) posY-1, (int) posZ - 2);
                            offset[0] = -1;
                            offset[1] = -2;
                            break;
                    }
                    if(willGunSetBlock == null && ((HMGItem_Unified_Guns) this.getHeldItem().getItem()).gunInfo.needfix){
                        willGunSetBlock = worldObj.getBlock((int) posX-1, (int) posY-1, (int) posZ - 1);
                        offset[0] = -1;
                        offset[1] = -1;
                    }
                    if (willGunSetBlock != null && willGunSetBlock != Blocks.air) {
                        PlacedGunEntity gunEntity = new PlacedGunEntity(worldObj, getHeldItem());
                        gunEntity.setLocationAndAngles((int) this.posX + 0.5f + offset[0], this.posY + 1.8, (int) this.posZ + 0.5f + offset[1],
                                this.rotationYaw,
                                this.rotationPitch);
                        gunEntity.issummonbyMob = true;
                        gunEntity.baserotationYaw = this.rotationYaw;
                        worldObj.spawnEntityInWorld(gunEntity);
                        this.mountEntity(gunEntity);
                        this.setCurrentItemOrArmor(0, null);
                    }
                }
            }

//			if(bullets != this.getHeldItem().getItemDamage()){
//				this.aiAttackGun.burstingtime++;
//			}
        
        
            if(this.getEntityData().getBoolean("HMGisUsingItem")){
                this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(movespeed/4);
            }else{
                this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(movespeed);
            }
            this.getEntityData().setBoolean("HMGisUsingItem",false);
        }
    
        if(!worldObj.isRemote &&
                   this.ridingEntity instanceof PlacedGunEntity &&
                   ((PlacedGunEntity) ridingEntity).issummonbyMob &&
                   ridingEntity.ridingEntity == null &&
                   (this.getAttackTarget() == null || this.getAttackTarget().isDead)){
            this.setCurrentItemOrArmor(0,((PlacedGunEntity) ridingEntity).gunStack);
            ((PlacedGunEntity) ridingEntity).gunStack = null;
            ridingEntity.setDead();
            ridingEntity = null;
        }
        if(ridingEntity instanceof PlacedGunEntity){
            if(this.getEntityData().getBoolean("HMGisUsingItem")){
                ((PlacedGunEntity) ridingEntity).firing = true;
            }else {
                ((PlacedGunEntity) ridingEntity).firing = false;
            }
        }
        if(modifiedPathNavigater.getSpeed() < 0 && rand.nextInt(10)==0 && this.getAttackTarget() == null)modifiedPathNavigater.setSpeed(1);
        this.getEntityData().setBoolean("HMGisUsingItem",false);
    }
    
    
    
    
    
    
    
    public IEntityLivingData onSpawnWithEgg(IEntityLivingData par1EntityLivingData)
    {
        par1EntityLivingData = super.onSpawnWithEgg(par1EntityLivingData);
        {
            this.addRandomArmor();
            this.enchantEquipment();
        }
        return par1EntityLivingData;
    }
    
    
    
    public boolean canAttackClass(Class par1Class)
    {
        return EntityCreature.class != par1Class;
    }

    /**
     * Returns the sound this mob makes when it is hurt.
     */
    protected String getHurtSound()
    {
        return "mob.irongolem.hit";
    }

    /**
     * Returns the sound this mob makes on death.
     */
    protected String getDeathSound()
    {
        return "mob.irongolem.death";
    }

    protected void func_145780_a(int p_145780_1_, int p_145780_2_, int p_145780_3_, Block p_145780_4_)
    {
        this.playSound("mob.irongolem.walk", 1F, 1.0F);
    }
    
    protected boolean canDespawn()
    {
        return candespawn && getAttackTarget() == null;
    }
    
    /**
     * Returns true if the newer Entity AI code should be run
     */

	public static float getMobScale() {
		return 8;
	}


    @Override
    public boolean canEntityBeSeen(Entity p_70685_1_)
    {

        Vec3 vec3 = Vec3.createVectorHelper(this.posX, this.posY + this.getEyeHeight(), this.posZ);
        Vec3 vec31 = Vec3.createVectorHelper(p_70685_1_.posX, p_70685_1_.posY + p_70685_1_.getEyeHeight(), p_70685_1_.posZ);

        MovingObjectPosition movingobjectposition = handmadeguns.Util.Utils.getmovingobjectPosition_forBlock(worldObj,vec3, vec31, false, true, false);//衝突するブロックを調べる
        return movingobjectposition == null && canSeeTarget(p_70685_1_);
    }
    /**
     * returns a (normalized) vector of where this entity is looking
     */
    public Vec3 getLookVec()
    {
        return this.getLook(1.0F);
    }

    /**
     * interpolated look vector
     */
    public Vec3 getLook(float p_70676_1_)
    {
        float f1;
        float f2;
        float f3;
        float f4;

        if (p_70676_1_ == 1.0F)
        {
            f1 = MathHelper.cos(-this.rotationYawHead * 0.017453292F - (float)Math.PI);
            f2 = MathHelper.sin(-this.rotationYawHead * 0.017453292F - (float)Math.PI);
            f3 = -MathHelper.cos(-this.rotationPitch * 0.017453292F);
            f4 = MathHelper.sin(-this.rotationPitch * 0.017453292F);
            return Vec3.createVectorHelper((double)(f2 * f3), (double)f4, (double)(f1 * f3));
        }
        else
        {
            f1 = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * p_70676_1_;
            f2 = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * p_70676_1_;
            f3 = MathHelper.cos(-f2 * 0.017453292F - (float)Math.PI);
            f4 = MathHelper.sin(-f2 * 0.017453292F - (float)Math.PI);
            float f5 = -MathHelper.cos(-f1 * 0.017453292F);
            float f6 = MathHelper.sin(-f1 * 0.017453292F);
            return Vec3.createVectorHelper((double)(f4 * f5), (double)f6, (double)(f3 * f5));
        }
    }
    
    @Override
    public float getviewWide() {
        return viewWide;
    }
    
    @Override
    public boolean canSeeTarget(Entity target) {
        boolean flag;
        flag = canhearsound(target);
        if (!flag) {
            Vec3 lookVec = getLookVec();
            Vec3 toTGTvec = Vec3.createVectorHelper(target.posX - posX, target.posY + target.getEyeHeight() - (posY + getEyeHeight()), target.posZ - posZ);
            toTGTvec = toTGTvec.normalize();
            float enemylight = target.getBrightness(1.0f);
            float thislight = this.getBrightness(1.0f);
            return getDistanceSqToEntity(target) < pow(60 * (1 + enemylight - thislight) * (0.1 + thislight*0.9),2)&& lookVec.squareDistanceTo(toTGTvec) < getviewWide()  * (1 + enemylight - thislight) * (0.4 + thislight*0.6)/2;
        }else{
            return true;
        }
    }
    
    @Override
    public boolean canhearsound(Entity target) {
        boolean flag;
        double dist = getDistanceToEntity(target);
        flag = dist < target.getEntityData().getFloat("GunshotLevel") * 10;
        return flag;
    }

    @Override
    public AIAttackGun getAttackGun() {
        return aiAttackGun;
    }

    @Override
    public boolean is_this_entity_friend(Entity entity) {
        return entity instanceof EntityGBases;
    }
    
    public boolean canPickUpLoot(){
        return staningtime < 0;
    }
    public boolean isAIEnabled() {
        return true;
    }
    
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
    }
    
    public void addRandomArmor() {
        super.addRandomArmor();
    }
    
    public boolean isConverting() {
        return false;
    }
    
    public void setDead(){
        super.setDead();
    }
    
    
    
    @Override
    public boolean getCanSpawnHere()
    {
        return super.getCanSpawnHere();
    }
    @Override
    public float getBlockPathWeight(int p_70783_1_, int p_70783_2_, int p_70783_3_)
    {
        return (0.5F - this.worldObj.getLightBrightness(p_70783_1_, p_70783_2_, p_70783_3_));
    }
    @Override
    protected boolean isValidLightLevel()
    {
        int i = MathHelper.floor_double(this.posX);
        int j = MathHelper.floor_double(this.boundingBox.minY);
        int k = MathHelper.floor_double(this.posZ);
        if(!worldObj.canBlockSeeTheSky(i, j, k)) return super.isValidLightLevel();
        
        
        if (this.worldObj.getSavedLightValue(EnumSkyBlock.Sky, i, j, k) <= this.rand.nextInt(32))
        {
            return false;
        }
        else
        {
            int l = this.worldObj.getBlockLightValue(i, j, k);

            if (this.worldObj.isThundering())
            {
                int i1 = this.worldObj.skylightSubtracted;
                this.worldObj.skylightSubtracted = 10;
                l = this.worldObj.getBlockLightValue(i, j, k);
                this.worldObj.skylightSubtracted = i1;
            }

            return l <= this.rand.nextInt(8);
        }
    }
    
    
    
    public int getdirection_in4(float globalDir){
        if(globalDir <= 45 && globalDir > -45){
            return 0;
        }
        if(globalDir <= 135 && globalDir > 45){
            return 1;
        }
        if(globalDir <= -45 && globalDir > -135){
            return -1;
        }
        if(globalDir <= -135 || globalDir > 135){
            return 2;
        }
        return 0;
    }


    public boolean canRideVehicle;
    protected void collideWithNearbyEntities()
    {
        if(!worldObj.isRemote) {
            List list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox.expand(0.20000000298023224D, 0.0D, 0.20000000298023224D));

            if (list != null && !list.isEmpty()) {
                for (int i = 0; i < list.size(); ++i) {
                    Entity entity = (Entity) list.get(i);

                    if (entity.canBePushed()) {
                        this.collideWithEntity(entity);
                    }
                    if (this.ridingEntity == null && !entity.isDead) {
                        if (entity instanceof EntityVehicle && ((EntityVehicle) entity).canUseByMob) {
                            Entity pilot = ((EntityVehicle) entity).getBaseLogic().getRiddenEntityList()[((EntityVehicle) entity).getpilotseatid()];
                            if ((pilot == null || is_this_entity_friend(pilot)) && !((EntityVehicle) entity).getBaseLogic().isRidingEntity(this)) {
//                            System.out.println("" + );
                                ((EntityVehicle) entity).pickupEntity(this, seatID);
                            }
                        }else {
                            if(cfg_guerrillacanusePlacedGun && canuseAlreadyPlacedGun && !worldObj.isRemote && this.getAttackTarget() != null) {
                                if (entity.riddenByEntity == null && entity instanceof PlacedGunEntity) {
                                    placing++;
                                    if (placing > 60) {
                                        placing = 0;
                                        this.mountEntity((PlacedGunEntity) entity);
                                    }
                                    this.setCurrentItemOrArmor(0, null);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public VehicleSpawnGachaOBJ[] getVehicleGacha() {
        return vehicleSpawnGachaOBJ;
    }

    public int getVehicleGacha_rate_sum() {
        return vehilceGacha_rate_sum;
    }

    public void setVehicleName(String string) {
        summoningVehicle = string;
    }

    public int seatID;
    public TurretObj currentMainTurret;
    public TurretObj currentSubTurret;
    @Override
    public void setSeatID(int id) {
        seatID = id;
    }

    @Override
    public void setTurretMain(TurretObj turret) {
        currentMainTurret = turret;
    }

    @Override
    public void setTurretSub(TurretObj turret) {
        currentSubTurret = turret;
    }

    @Override
    public int getSeatID() {
        return seatID;
    }

    @Override
    public TurretObj getTurretMain() {
        return currentMainTurret;
    }

    @Override
    public TurretObj getTurretSub() {
        return currentSubTurret;
    }
//TODO
    @Override
    public CampObj getCampObj() {
        return guerrillas;
    }

    public Team getTeam()
    {
        return getCampObj();
    }

    private byte state = -1;
    @Override
    public byte getState() {
        return state;
    }

    @Override
    public void setState(byte state) {
        this.state = state;
    }

    private int[] flagPos;
    @Override
    public int[] getTargetCampPosition() {
        return flagPos;
    }

    @Override
    public void setTargetCampPosition(int[] ints) {
        if(ints == null){
            flag = null;
            return;
        }
        if(flagPos == null)flagPos = new int[3];
        flagPos[0] = ints[0];
        flagPos[1] = ints[1];
        flagPos[2] = ints[2];
    }

    ArrayList<Entity> platoon;
    IflagBattler platoonLeader;

    @Override
    public void makePlatoon() {
        if(canMoveEntity(this) && platoon == null){
            platoon = new ArrayList<>();
            platoonLeader = this;

            List list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, AxisAlignedBB.getBoundingBox(this.posX, this.posY, this.posZ, this.posX + 1.0D, this.posY + 1.0D, this.posZ + 1.0D).expand(32, 32, 32));
            Iterator iterator = list.iterator();

            while (iterator.hasNext())
            {
                Entity entitycreature = (Entity)iterator.next();
                if(entitycreature instanceof EntityGBases && canMoveEntity(this)){
                    platoon.add(entitycreature);
                    ((EntityGBases) entitycreature).joinPlatoon(this);
                }

            }
        }
    }

    @Override
    public void setPlatoon(ArrayList<Entity> entities) {
        platoon = entities;
    }

    @Override
    public void joinPlatoon(IflagBattler iflagBattler) {
        platoonLeader = iflagBattler;
        if(platoonLeader != null)platoon = iflagBattler.getPlatoon();
    }

    @Override
    public ArrayList<Entity> getPlatoon() {
        return platoon;
    }

    @Override
    public IflagBattler getPlatoonLeader() {
        return platoonLeader;
    }

    @Override
    public boolean isThisAttackAbleCamp(CampObj campObj) {
        return campObj != guerrillas;
    }

    @Override
    public boolean isThisFriendCamp(CampObj campObj) {
        return campObj == guerrillas;
    }

    @Override
    public boolean isThisIgnoreSpawnCamp(CampObj campObj) {
        return campObj != guerrillas;
    }
}
