package com.lulan.shincolle.entity.destroyer;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import com.lulan.shincolle.ai.EntityAIShipRangeAttack;
import com.lulan.shincolle.entity.BasicEntityShipHostile;
import com.lulan.shincolle.entity.other.EntityAbyssMissile;
import com.lulan.shincolle.entity.other.EntityRensouhouBoss;
import com.lulan.shincolle.handler.ConfigHandler;
import com.lulan.shincolle.init.ModItems;
import com.lulan.shincolle.network.S2CSpawnParticle;
import com.lulan.shincolle.proxy.CommonProxy;
import com.lulan.shincolle.reference.ID;
import com.lulan.shincolle.reference.Reference;

import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;

public class EntityDestroyerShimakazeBoss extends BasicEntityShipHostile implements IBossDisplayData {

	public int numRensouhou;
	
	public EntityDestroyerShimakazeBoss(World world) {
		super(world);
		this.setSize(1.4F, 6F);
		this.setCustomNameTag(StatCollector.translateToLocal("entity.shincolle.EntityDestroyerShimakaze.name"));
		this.setStateMinor(ID.M.ShipClass, ID.Ship.DestroyerShimakaze);
		this.dropItem = new ItemStack(ModItems.ShipSpawnEgg, 1, getStateMinor(ID.M.ShipClass)+2);
		ignoreFrustumCheck = true;	//即使不在視線內一樣render
		
        //basic attr
        this.atk = (float) ConfigHandler.scaleBossSmall[ID.ATK] * 0.75F;
        this.atkSpeed = (float) ConfigHandler.scaleBossSmall[ID.SPD] * 1.2F;
        this.atkRange = (float) ConfigHandler.scaleBossSmall[ID.HIT] * 0.75F;
        this.defValue = (float) ConfigHandler.scaleBossSmall[ID.DEF] * 0.75F;
        this.movSpeed = (float) ConfigHandler.scaleBossSmall[ID.MOV] * 0.75F;
        this.numRensouhou = 10;

        //AI flag
        this.StartEmotion = 0;
        this.StartEmotion2 = 0;
        this.headTilt = false;
 
	    //設定基本屬性
	    getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(ConfigHandler.scaleBossSmall[ID.HP] * 0.75F);
		getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(this.movSpeed);
		getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(atkRange + 32); //此為找目標, 路徑的範圍
		getEntityAttribute(SharedMonsterAttributes.knockbackResistance).setBaseValue(1D);
		if(this.getHealth() < this.getMaxHealth()) this.setHealth(this.getMaxHealth());
				
		//設定AI
		this.setAIList();
		this.setAITargetList();
	}
	
	@Override
	protected boolean canDespawn() {
        return this.ticksExisted > 6000;
    }
	
	@Override
	public float getEyeHeight() {
		return this.height * 0.5F;
	}
	
	//setup AI
	@Override
	protected void setAIList() {
		super.setAIList();

		//use range attack
		this.tasks.addTask(1, new EntityAIShipRangeAttack(this));
	}
	
	//num rensouhou++
	@Override
  	public void onLivingUpdate() {
  		super.onLivingUpdate();
          
  		if(!worldObj.isRemote) {
  			//add aura to master every N ticks
  			if(this.ticksExisted % 128 == 0) {
  				//add num of rensouhou
  				if(this.numRensouhou < 10) numRensouhou++;
  			}
  		}    
  	}
	
	//招喚連裝砲進行攻擊
  	@Override
  	public boolean attackEntityWithAmmo(Entity target) {
  		//check num rensouhou
  		if(this.numRensouhou <= 0) {
  			return false;
  		}
  		else {
  			this.numRensouhou--;
  		}
  		
        //play entity attack sound
        if(this.rand.nextInt(10) > 5) {
        	this.playSound(Reference.MOD_ID+":ship-hitsmall", ConfigHandler.volumeShip, 1F / (this.getRNG().nextFloat() * 0.4F + 0.8F));
        }

        //發射者煙霧特效 (招喚連裝砲不使用特效, 但是要發送封包來設定attackTime)
        TargetPoint point = new TargetPoint(this.dimension, this.posX, this.posY, this.posZ, 32D);
		CommonProxy.channelP.sendToAllAround(new S2CSpawnParticle(this, 0, true), point);
  		
		//spawn airplane
        if(target instanceof EntityLivingBase) {
        	EntityRensouhouBoss rensoho = new EntityRensouhouBoss(this.worldObj, this, target);
            this.worldObj.spawnEntityInWorld(rensoho);
            //show emotes
			applyEmotesReaction(3);
            return true;
        }

        return false;
	}
  	
  	//五連裝酸素魚雷
  	@Override
  	public boolean attackEntityWithHeavyAmmo(Entity target) {	
		//get attack value
		float atkHeavy = this.atk * 0.9F;
		float kbValue = 0.08F;
		
		//飛彈是否採用直射
		boolean isDirect = false;
		//計算目標距離
		float tarX = (float)target.posX;	//for miss chance calc
		float tarY = (float)target.posY;
		float tarZ = (float)target.posZ;
		float distX = tarX - (float)this.posX;
		float distY = tarY - (float)this.posY;
		float distZ = tarZ - (float)this.posZ;
        float distSqrt = MathHelper.sqrt_float(distX*distX + distY*distY + distZ*distZ);
        float launchPos = (float)posY + height * 0.5F;
        
        //超過一定距離/水中 , 則採用拋物線,  在水中時發射高度較低
        if((distX*distX+distY*distY+distZ*distZ) < 36F || this.getShipDepth() > 0D) {
        	isDirect = true;
        }
	
		//play cannon fire sound at attacker
        this.playSound(Reference.MOD_ID+":ship-fireheavy", ConfigHandler.volumeFire, 0.7F / (this.getRNG().nextFloat() * 0.4F + 0.8F));
        //play entity attack sound
        if(this.getRNG().nextInt(10) > 7) {
        	this.playSound(Reference.MOD_ID+":ship-hitsmall", ConfigHandler.volumeShip, 1F / (this.getRNG().nextFloat() * 0.4F + 0.8F));
        }
        
        //calc miss chance, miss: add random offset(0~6) to missile target 
        float missChance = 0.25F;	//max miss chance = 30%
       
        if(this.rand.nextFloat() < missChance) {
        	tarX = tarX - 5F + this.rand.nextFloat() * 10F;
        	tarY = tarY + this.rand.nextFloat() * 5F;
        	tarZ = tarZ - 5F + this.rand.nextFloat() * 10F;
        	//spawn miss particle
        	TargetPoint point = new TargetPoint(this.dimension, this.posX, this.posY, this.posZ, 64D);
        	CommonProxy.channelP.sendToAllAround(new S2CSpawnParticle(this, 10, false), point);
        }
        
        //發射者煙霧特效 (不使用特效, 但是要發送封包來設定attackTime)
        TargetPoint point = new TargetPoint(this.dimension, this.posX, this.posY, this.posZ, 32D);
		CommonProxy.channelP.sendToAllAround(new S2CSpawnParticle(this, 0, true), point);

        //spawn missile
        EntityAbyssMissile missile1 = new EntityAbyssMissile(this.worldObj, this, 
        		tarX, tarY+target.height*0.2F, tarZ, launchPos, atkHeavy, kbValue, isDirect, -1F);
        EntityAbyssMissile missile2 = new EntityAbyssMissile(this.worldObj, this, 
        		tarX+3F, tarY+target.height*0.2F, tarZ+6F, launchPos, atkHeavy, kbValue, isDirect, -1F);
        EntityAbyssMissile missile3 = new EntityAbyssMissile(this.worldObj, this, 
        		tarX+3F, tarY+target.height*0.2F, tarZ-6F, launchPos, atkHeavy, kbValue, isDirect, -1F);
        EntityAbyssMissile missile4 = new EntityAbyssMissile(this.worldObj, this, 
        		tarX-3F, tarY+target.height*0.2F, tarZ+6F, launchPos, atkHeavy, kbValue, isDirect, -1F);
        EntityAbyssMissile missile5 = new EntityAbyssMissile(this.worldObj, this, 
        		tarX-3F, tarY+target.height*0.2F, tarZ-6F, launchPos, atkHeavy, kbValue, isDirect, -1F);
        
        this.worldObj.spawnEntityInWorld(missile1);
        this.worldObj.spawnEntityInWorld(missile2);
        this.worldObj.spawnEntityInWorld(missile3);
        this.worldObj.spawnEntityInWorld(missile4);
        this.worldObj.spawnEntityInWorld(missile5);
        
        //show emotes
		applyEmotesReaction(3);
		
        return true;
	}
  	
  	@Override
	public int getDamageType() {
		return ID.ShipDmgType.DESTROYER;
	}
  	
  	@Override
	public float getEffectEquip(int id) {
		switch(id) {
		case ID.EF_CRI:
			return 0.15F;
		case ID.EF_AA:  //DD vs AA,ASM effect
		case ID.EF_ASM:
			return this.atk * 0.5F;
		default:
			return 0F;
		}
	}
  	

}
