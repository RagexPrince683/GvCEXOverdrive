package hmggvcmob.entity.friend;

import handmadeguns.entity.IFF;
import handmadeguns.entity.PlacedGunEntity;
import handmadevehicle.entity.EntityDummy_rider;
import handmadevehicle.entity.parts.Modes;
import hmggvcmob.entity.util.PlatoonOBJ;
import hmggvcmob.camp.CampObj;
import hmggvcmob.entity.IGVCmob;
import hmggvcmob.entity.IflagBattler;
import hmggvcmob.entity.util.PlatoonInfoData;
import hmggvcmob.item.GVCItemPMCDefSetter;
import hmggvcmob.network.GVCMPacketHandler;
import hmggvcmob.network.GVCPacket_PlatoonInfoSync;
import littleMaidMobX.LMM_EntityLittleMaid;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

import static handmadeguns.HandmadeGunsCore.HMG_proxy;
import static handmadeguns.HandmadeGunsCore.islmmloaded;
import static handmadevehicle.Utils.canMoveEntity;
import static handmadevehicle.entity.parts.Modes.Wait;
import static hmggvcmob.GVCMobPlus.*;
import static hmggvcutil.GVCUtils.platoonMatched;

public class EntityPMCBase extends EntitySoBases implements IFF,IGVCmob, IflagBattler {

	public int mode = 0;
	public String platoonName = null;

	public boolean interact(EntityPlayer entityPlayer) {
		if(entityPlayer.getHeldItem() != null && entityPlayer.getHeldItem().getItem() instanceof GVCItemPMCDefSetter && entityPlayer.getHeldItem().hasDisplayName()){

			HMG_proxy.setRightClickTimer();
			platoonName = entityPlayer.getHeldItem().getDisplayName();
			this.setCustomNameTag(platoonName);
			return true;
		}

		List list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox.expand(5, 5
				, 5));

		if (list != null && !list.isEmpty() && cfg_guerrillacanusePlacedGun && canuseAlreadyPlacedGun && !worldObj.isRemote) {
			for (int i = 0; i < list.size(); ++i) {
				Entity entity = (Entity) list.get(i);
				if (entity.riddenByEntity == null && entity instanceof PlacedGunEntity) {
					this.mountEntity((PlacedGunEntity) entity);
					this.setCurrentItemOrArmor(0, null);
					break;
				}
			}
		}
		super.interact(entityPlayer);

		return false;
	}
	protected void applyEntityAttributes()
	{
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(movespeed = 0.33000000417232513D);
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(20.0D);
		this.getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(cfg_guerrillasrach*2);
		//this.getEntityAttribute(SharedMonsterAttributes.knockbackResistance).setBaseValue(30.0D);
	}
	public EntityPMCBase(World par1World) {
		super(par1World);
	}

	public void onUpdate()
	{
		if (platoonOBJ != null && platoonOBJ.platoonTargetEntity == null && leaderEntity_name != null) {
			platoonOBJ.platoonTargetEntity = worldObj.getPlayerEntityByName(leaderEntity_name);
		}
		if(!worldObj.isRemote && getPlatoon()!=null){
			platoonInfoData.isLeader = isPlatoonLeader();
			platoonInfoData.isOnPlatoon = true;
			platoonInfoData.target = new double[]{myPos.getPos().x,myPos.getPos().y,myPos.getPos().z};
			platoonInfoData.mode = getPlatoon().platoonMode;
			platoonInfoData.platoonName = platoonName;
			GVCMPacketHandler.INSTANCE.sendToAll(new GVCPacket_PlatoonInfoSync(platoonInfoData,this));
		}

		super.onUpdate();

		if(rand.nextInt(10) == 0)this.addPotionEffect(new PotionEffect(Potion.regeneration.id, 1, 2));
    }

    public boolean getCanSpawnHere()
    {
    	return false;
    }

	protected boolean canDespawn()
	{
		return false;
	}

	@Override
	public boolean is_this_entity_friend(Entity entity) {
		if(entity instanceof EntityPlayer){
			return true;
		}else if(entity instanceof EntitySoBases) {
			return true;
		}else if(islmmloaded && entity instanceof LMM_EntityLittleMaid){
			return true;
		}
		return false;
	}
	public void readEntityFromNBT(NBTTagCompound p_70037_1_)
	{
		super.readEntityFromNBT(p_70037_1_);
		leaderEntity_name = p_70037_1_.getString("leaderEntity_name");
		if(p_70037_1_.hasKey("platoonName")) platoonName = p_70037_1_.getString("platoonName");
	}

	public void writeEntityToNBT(NBTTagCompound p_70014_1_)
	{
		super.writeEntityToNBT(p_70014_1_);
		if(leaderEntity_name != null)p_70014_1_.setString("leaderEntity_name",leaderEntity_name);
		if(platoonName != null)p_70014_1_.setString("platoonName",platoonName);
	}

	public String leaderEntity_name;

	@Override
	public void makePlatoon() {
		this.setPlatoon(new PlatoonOBJ());

		this.enlistPlatoon(false);
	}
	@Override
	public void makePlatoon_OnLoading() {
		this.setPlatoon(new PlatoonOBJ());

		this.enlistPlatoon(true);
	}
	@Override
	public void enlistPlatoon(boolean force) {
		ArrayList<EntityPMCBase> newComer = new ArrayList<>();
		List nearEntities = worldObj.getEntitiesWithinAABBExcludingEntity(this,boundingBox.expand(32,32,32));

		for(Object obj:nearEntities){
			Entity entity = (Entity)obj;
			if(entity instanceof EntityPMCBase && canMoveEntity(entity)){
				newComer.add((EntityPMCBase) entity);
			}
		}
		List onVehicleEntity = worldObj.getEntitiesWithinAABBExcludingEntity(this,boundingBox.expand(512,512,512));

		for(Object obj:onVehicleEntity){
			Entity entity = (Entity)obj;
			if(entity instanceof EntityPMCBase && entity.ridingEntity instanceof EntityDummy_rider && canMoveEntity(entity)){
				newComer.add((EntityPMCBase) entity);
			}
		}
		for(EntityPMCBase entityPMCBase : newComer){
			//対象の分隊が同名or分隊に未所属時に徴発
			if(platoonMatched(platoonName, (EntityPMCBase) entityPMCBase) &&(((EntityPMCBase) entityPMCBase).getPlatoon() == null || force)){
				((EntityPMCBase) entityPMCBase).setPlatoon(this.platoonOBJ);
				((EntityPMCBase) entityPMCBase).platoonName = platoonName;
			}
		}

	}

	@Override
	public Modes getMobMode() {
		if(platoonOBJ != null)return platoonOBJ.platoonMode;
		return Wait;
	}

	@Override
	public CampObj getCampObj() {
		return forPlayer;
	}

	@Override
	public boolean isThisAttackAbleCamp(CampObj campObj) {
		return campObj == guerrillas;
	}

	@Override
	public boolean isThisFriendCamp(CampObj campObj) {
		return campObj == forPlayer || campObj == soldiers;
	}

	@Override
	public boolean isThisIgnoreSpawnCamp(CampObj campObj) {
		return true;
	}

	final PlatoonInfoData platoonInfoData = new PlatoonInfoData();

	@Override
	public PlatoonInfoData getPlatoonMemberInfo(){
		return platoonInfoData;
	}
}
