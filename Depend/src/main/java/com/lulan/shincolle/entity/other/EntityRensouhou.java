package com.lulan.shincolle.entity.other;

import com.lulan.shincolle.ai.EntityAIShipRangeAttack;
import com.lulan.shincolle.ai.path.ShipMoveHelper;
import com.lulan.shincolle.ai.path.ShipPathNavigate;
import com.lulan.shincolle.entity.*;
import com.lulan.shincolle.handler.ConfigHandler;
import com.lulan.shincolle.network.S2CEntitySync;
import com.lulan.shincolle.network.S2CSpawnParticle;
import com.lulan.shincolle.proxy.CommonProxy;
import com.lulan.shincolle.reference.ID;
import com.lulan.shincolle.reference.Reference;
import com.lulan.shincolle.server.Explosion;
import com.lulan.shincolle.utility.BlockHelper;
import com.lulan.shincolle.utility.CalcHelper;
import com.lulan.shincolle.utility.EntityHelper;
import com.lulan.shincolle.utility.ParticleHelper;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityRensouhou extends EntityLiving implements IShipCannonAttack {
	
	protected BasicEntityShip host;  	//host target
	protected World world;
	protected ShipPathNavigate shipNavigator;	//水空移動用navigator
	protected ShipMoveHelper shipMoveHelper;
	protected Entity atkTarget;
	protected Entity rvgTarget;					//revenge target
	protected int revengeTime;					//revenge target time
    
    //attributes
	protected float atk;				//damage
	protected float atkSpeed;			//attack speed
	protected float atkRange;			//attack range
	protected float defValue;			//def value
	protected float movSpeed;			//def value
    protected float kbValue;			//knockback value
    
    //AI flag
    protected int numAmmoLight;
    protected int numAmmoHeavy;
    
    //model display
    /**EntityState: 0:HP State 1:Emotion 2:Emotion2*/
	protected byte StateEmotion;		//表情1
	protected byte StateEmotion2;		//表情2
	protected int StartEmotion, StartEmotion2, StartEmotion3;		//表情2 開始時間
	protected boolean headTilt;

	
    public EntityRensouhou(World world) {
		super(world);
		this.setSize(0.5F, 0.8F);
		this.isImmuneToFire = true;
	}
    
    public EntityRensouhou(World world, BasicEntityShip host, Entity target) {
		super(world);
		this.world = world;
        this.host = host;
        this.atkTarget = target;
        this.isImmuneToFire = true;
        shipNavigator = new ShipPathNavigate(this, worldObj);
		shipMoveHelper = new ShipMoveHelper(this, 45F);
        
        //basic attr
        this.atk = host.getStateFinal(ID.ATK);
        this.atkSpeed = host.getStateFinal(ID.SPD) + rand.nextFloat() * 0.5F - 0.25F;
        this.atkRange = host.getStateFinal(ID.HIT) + 1F;
        this.defValue = host.getStateFinal(ID.DEF) * 0.5F;
        this.movSpeed = host.getStateFinal(ID.MOV) * 0.2F + 0.4F;
        
        //AI flag
        this.numAmmoLight = 6;
        this.numAmmoHeavy = 0;
        this.StateEmotion = 0;
        this.StateEmotion2 = 0;
        this.StartEmotion = 0;
        this.StartEmotion2 = 0;
        this.headTilt = false;
           
        //設定發射位置
        this.posX = host.posX + rand.nextDouble() * 3D - 1.5D;
        this.posY = host.posY + 0D;
        this.posZ = host.posZ + rand.nextDouble() * 3D - 1.5D;

    	//check the place is safe to summon
    	if(!BlockHelper.checkBlockSafe(world, (int)posX, (int)posY, (int)posZ)) {
    		this.posX = host.posX;
            this.posY = host.posY;
            this.posZ = host.posZ;
    	}

        this.setPosition(this.posX, this.posY, this.posZ);
 
	    //設定基本屬性
        double mhp = host.getLevel() + host.getStateFinal(ID.HP)*0.2D;
        
	    getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(mhp);
		getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(this.movSpeed);
		getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(host.getStateFinal(ID.HIT) + 32); //此為找目標, 路徑的範圍
		getEntityAttribute(SharedMonsterAttributes.knockbackResistance).setBaseValue(1D);
		if(this.getHealth() < this.getMaxHealth()) this.setHealth(this.getMaxHealth());
				
		//設定AI
		this.setAIList();
	}
    
    @Override
	public float getEyeHeight() {
		return this.height * 2F;
	}
	
	//setup AI
	protected void setAIList() {
		this.clearAITasks();
		this.clearAITargetTasks();

		this.getNavigator().setEnterDoors(true);
		this.getNavigator().setAvoidsWater(false);
		this.getNavigator().setCanSwim(true);
		
		this.tasks.addTask(1, new EntityAIShipRangeAttack(this));
		this.setEntityTarget(atkTarget);
	}
	
	@Override
    public boolean attackEntityFrom(DamageSource attacker, float atk) {
		//disable 
		if(attacker.getDamageType() == "inWall") {
			return false;
		}
		
		//calc dodge
		if(EntityHelper.canDodge(this, 0F)) {
			return false;
		}
		
		//set hurt face
    	if(this.getStateEmotion(ID.S.Emotion) != ID.Emotion.O_O) {
    		this.setStateEmotion(ID.S.Emotion, ID.Emotion.O_O, true);
    	}
        
        //無敵的entity傷害無效
  		if(this.isEntityInvulnerable()) {	
        	return false;
        }
  		
  		if(attacker.getSourceOfDamage() != null) {
  			Entity entity = attacker.getSourceOfDamage();
  			
  			//不會對自己造成傷害
  			if(entity.equals(this)) {  
  				return false;
  			}
  			
  			//若攻擊方為player, 則修正傷害
  			if(entity instanceof EntityPlayer) {
				//若禁止friendlyFire, 則傷害設為0
				if(!ConfigHandler.friendlyFire) {
					return false;
				}
  			}
  			
  			//def calc
  			float reduceAtk = atk;
  			
  			if(host != null) {
  				reduceAtk = atk * (1F - this.getDefValue() * 0.01F);
  			}
  			
  			//ship vs ship, config傷害調整
  			if(entity instanceof BasicEntityShip || entity instanceof BasicEntityAirplane || 
  			   entity instanceof EntityRensouhou || entity instanceof BasicEntityMount) {
  				reduceAtk = reduceAtk * (float)ConfigHandler.dmgSummon * 0.01F;
  			}
  			
  			//ship vs ship, damage type傷害調整
  			if(entity instanceof IShipAttackBase) {
  				//get attack time for damage modifier setting (day, night or ...etc)
  				int modSet = this.worldObj.provider.isDaytime() ? 0 : 1;
  				reduceAtk = CalcHelper.calcDamageByType(reduceAtk, ((IShipAttackBase) entity).getDamageType(), this.getDamageType(), modSet);
  			}
  			
  	        if(reduceAtk < 1) reduceAtk = 1;
  	        
  	        return super.attackEntityFrom(attacker, reduceAtk);
  		}
    	
    	return false;
	}
	
	@Override
	public void onUpdate() {
		super.onUpdate();
		
		//client side
		if(this.worldObj.isRemote) {
			//有移動時, 產生水花特效
			//(注意此entity因為設為非高速更新, client端不會更新motionX等數值, 需自行計算)
			double motX = this.posX - this.prevPosX;
			double motZ = this.posZ - this.prevPosZ;
			double parH = this.posY - (int)this.posY;
			
			if(motX != 0 || motZ != 0) {
				ParticleHelper.spawnAttackParticleAt(this.posX + motX*1.5D, this.posY, this.posZ + motZ*1.5D, 
						-motX*0.5D, 0D, -motZ*0.5D, (byte)15);
			}
		}
		//server side
		else {
			boolean setdead = false;
			
			//owner消失(通常是server restart)
			if(this.host == null) {
				setdead = true;
			}
			else {
				//超過60秒自動消失
				if(this.ticksExisted > 1200) {
					setdead = true;
				}

				//target is dead
				if(this.getEntityTarget() == null || this.getEntityTarget().isDead) {
					//change target
					if(this.host != null && this.host.getEntityTarget() != null &&
					   this.host.getEntityTarget().isEntityAlive()) {
						this.atkTarget = this.host.getEntityTarget();
					}
					else {
						setdead = true;
					}	
				}
				
				//防止溺死
				if(this.isInWater() && this.ticksExisted % 100 == 0) {
					this.setAir(300);
				}
			}
			
			//is done
			if(setdead) {
				//歸還彈藥
				if(this.host != null) {
					//召喚時消耗4 light ammo, 連裝砲可6次攻擊, 回收時次數要-2
					this.numAmmoLight -= 2;
					if(this.numAmmoLight < 0) this.numAmmoLight = 0;
					
					//連裝砲數量+1
					int numR = ((IShipSummonAttack)host).getNumServant();
					if(numR < 6) ((IShipSummonAttack)host).setNumServant(numR + 1);
					
					//歸還彈藥
					host.setAmmoLight(host.getAmmoLight() + this.getAmmoLight() * host.getAmmoConsumption());
				}
				
				this.setDead();
			}
		}
	}

	@Override
	public byte getStateEmotion(int id) {
		return id == 1 ? StateEmotion : StateEmotion2;
	}
	
	@Override
	public void setStateEmotion(int id, int value, boolean sync) {	
		switch(id) {
		case 1:
			StateEmotion = (byte) value;
			break;
		case 2:
			StateEmotion2 = (byte) value;
			break;
		default:
			break;
		}
		
		if(sync && !worldObj.isRemote) {
			TargetPoint point = new TargetPoint(this.dimension, this.posX, this.posY, this.posZ, 32D);
			CommonProxy.channelE.sendToAllAround(new S2CEntitySync(this, 4), point);
		}
	}

	@Override
	public boolean getStateFlag(int flag) {
		switch(flag) {
		default:
			return true;
		case ID.F.OnSightChase:
			if(host != null) return host.getStateFlag(flag);
			return false;
		}
	}

	@Override
	public void setStateFlag(int id, boolean flag) {
		this.headTilt = flag;
	}

	@Override
	public int getFaceTick() {
		return this.StartEmotion;
	}

	@Override
	public int getHeadTiltTick() {
		return this.StartEmotion2;
	}

	@Override
	public void setFaceTick(int par1) {
		this.StartEmotion = par1;
	}

	@Override
	public void setHeadTiltTick(int par1) {
		this.StartEmotion2 = par1;
	}

	@Override
	public int getTickExisted() {
		return this.ticksExisted;
	}

	@Override
	public int getAttackTime() {
		return this.attackTime;
	}
	
    @Override
	public boolean isAIEnabled() {
		return true;
	}
  	
    //clear AI
  	protected void clearAITasks() {
  	   tasks.taskEntries.clear();
  	}
  	
  	//clear target AI
  	protected void clearAITargetTasks() {
  	   targetTasks.taskEntries.clear();
  	}
    
  	@Override
	public Entity getEntityTarget() {
		return this.atkTarget;
	}
  	
  	@Override
	public void setEntityTarget(Entity target) {
		this.atkTarget = target;
	}
    
    @Override
	//light attack
	public boolean attackEntityWithAmmo(Entity target) {
		float atkLight = CalcHelper.calcDamageBySpecialEffect(this, target, this.atk, 0);

		//play cannon fire sound at attacker
        playSound(Reference.MOD_ID+":ship-firesmall", ConfigHandler.volumeFire, 0.7F / (this.getRNG().nextFloat() * 0.4F + 0.8F));
        
        //此方法比getLook還正確 (client sync問題)
        float distX = (float) (target.posX - this.posX);
        float distY = (float) (target.posY - this.posY);
        float distZ = (float) (target.posZ - this.posZ);
        float distSqrt = MathHelper.sqrt_float(distX*distX + distY*distY + distZ*distZ);
        distX = distX / distSqrt;
        distY = distY / distSqrt;
        distZ = distZ / distSqrt;
		
		//發射者煙霧特效
        TargetPoint point0 = new TargetPoint(this.dimension, this.posX, this.posY, this.posZ, 64D);
		CommonProxy.channelP.sendToAllAround(new S2CSpawnParticle(this, 6, this.posX, this.posY, this.posZ, distX, distY, distZ, true), point0);
		
		//calc miss chance, if not miss, calc cri/multi hit
		TargetPoint point = new TargetPoint(this.dimension, this.host.posX, this.host.posY, this.host.posZ, 64D);
        float missChance = 0.2F + 0.15F * (distSqrt / host.getStateFinal(ID.HIT)) - 0.001F * host.getLevel();
        missChance -= this.host.getEffectEquip(ID.EF_MISS);		//equip miss reduce
        if(missChance > 0.35F) missChance = 0.35F;	//max miss chance
  		
        //calc miss chance
        if(this.rand.nextFloat() < missChance) {
        	atkLight = 0;	//still attack, but no damage
        	//spawn miss particle
        	
        	CommonProxy.channelP.sendToAllAround(new S2CSpawnParticle(this.host, 10, false), point);
        }
        else {
        	//roll cri -> roll double hit -> roll triple hit (triple hit more rare)
        	//calc critical
        	if(this.rand.nextFloat() < this.host.getEffectEquip(ID.EF_CRI)) {
        		atkLight *= 1.5F;
        		//spawn critical particle
            	CommonProxy.channelP.sendToAllAround(new S2CSpawnParticle(this.host, 11, false), point);
        	}
        	else {
        		//calc double hit
            	if(this.rand.nextFloat() < this.host.getEffectEquip(ID.EF_DHIT)) {
            		atkLight *= 2F;
            		//spawn double hit particle
            		CommonProxy.channelP.sendToAllAround(new S2CSpawnParticle(this.host, 12, false), point);
            	}
            	else {
            		//calc double hit
                	if(this.rand.nextFloat() < this.host.getEffectEquip(ID.EF_THIT)) {
                		atkLight *= 3F;
                		//spawn triple hit particle
                		CommonProxy.channelP.sendToAllAround(new S2CSpawnParticle(this.host, 13, false), point);
                	}
            	}
        	}
        }
        
        //vs player = 25% dmg
  		if(target instanceof EntityPlayer) {
  			atkLight *= 0.25F;
  			
  			//check friendly fire
    		if(!ConfigHandler.friendlyFire) {
    			atkLight = 0F;
    		}
    		else if(atkLight > 59F) {
    			atkLight = 59F;	//same with TNT
    		}
  		}

	    //將atk跟attacker傳給目標的attackEntityFrom方法, 在目標class中計算傷害
	    //並且回傳是否成功傷害到目標
	    boolean isTargetHurt = target.attackEntityFrom(DamageSource.causeMobDamage(this).setProjectile(), atkLight);

	    //if attack success
	    if(isTargetHurt) {
	        //display hit particle on target
	        TargetPoint point1 = new TargetPoint(this.dimension, target.posX, target.posY, target.posZ, 64D);
			CommonProxy.channelP.sendToAllAround(new S2CSpawnParticle(target, 9, false), point1);
        }

		Explosion.ShipCannonExplosion(world, this, target.posX, target.posY, target.posZ, atk, isTargetHurt, ConfigHandler.PowerLimit * 0.7f);

		//消耗彈藥計算
  		if(numAmmoLight > 0) {
  			numAmmoLight--;
  			
  			if(numAmmoLight <= 0) {
  				this.setDead();
  			}
  		}

	    return isTargetHurt;
	}

	@Override
	public boolean attackEntityWithHeavyAmmo(Entity target) {
		return false;
	}
    
    //水中跟岩漿中不會下沉
    @Override
    public void moveEntityWithHeading(float p_70612_1_, float p_70612_2_) {
        double d0;

        if(this.isInWater() || this.handleLavaMovement()) {
            this.moveFlying(p_70612_1_, p_70612_2_, 0.04F);
            this.moveEntity(this.motionX, this.motionY, this.motionZ);
            this.motionX *= 0.8D;
            this.motionY *= 0.8D;
            this.motionZ *= 0.8D;
        }
        else {
            float f2 = 0.91F;

            if (this.onGround)
            {
                f2 = this.worldObj.getBlock(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.boundingBox.minY) - 1, MathHelper.floor_double(this.posZ)).slipperiness * 0.91F;
            }

            float f3 = 0.16277136F / (f2 * f2 * f2);
            float f4;

            if (this.onGround)
            {
                f4 = this.getAIMoveSpeed() * f3;
            }
            else
            {
                f4 = this.jumpMovementFactor;
            }

            this.moveFlying(p_70612_1_, p_70612_2_, f4);
            f2 = 0.91F;

            if (this.onGround)
            {
                f2 = this.worldObj.getBlock(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.boundingBox.minY) - 1, MathHelper.floor_double(this.posZ)).slipperiness * 0.91F;
            }

            if (this.isOnLadder())
            {
                float f5 = 0.15F;

                if (this.motionX < (-f5))
                {
                    this.motionX = (-f5);
                }

                if (this.motionX > f5)
                {
                    this.motionX = f5;
                }

                if (this.motionZ < (-f5))
                {
                    this.motionZ = (-f5);
                }

                if (this.motionZ > f5)
                {
                    this.motionZ = f5;
                }

                this.fallDistance = 0.0F;

                if (this.motionY < -0.15D)
                {
                    this.motionY = -0.15D;
                }

                boolean flag = this.isSneaking();

                if (flag && this.motionY < 0.0D)
                {
                    this.motionY = 0.0D;
                }
            }

            this.moveEntity(this.motionX, this.motionY, this.motionZ);

            if (this.isCollidedHorizontally && this.isOnLadder())
            {
                this.motionY = 0.2D;
            }

            if (this.worldObj.isRemote && (!this.worldObj.blockExists((int)this.posX, 0, (int)this.posZ) || !this.worldObj.getChunkFromBlockCoords((int)this.posX, (int)this.posZ).isChunkLoaded))
            {
                if (this.posY > 0.0D)
                {
                    this.motionY = -0.1D;
                }
                else
                {
                    this.motionY = 0.0D;
                }
            }
            else
            {
                this.motionY -= 0.08D;
            }

            this.motionY *= 0.9800000190734863D;
            this.motionX *= f2;
            this.motionZ *= f2;
        }

        this.prevLimbSwingAmount = this.limbSwingAmount;
        d0 = this.posX - this.prevPosX;
        double d1 = this.posZ - this.prevPosZ;
        float f6 = MathHelper.sqrt_double(d0 * d0 + d1 * d1) * 4.0F;

        if (f6 > 1.0F)
        {
            f6 = 1.0F;
        }

        this.limbSwingAmount += (f6 - this.limbSwingAmount) * 0.4F;
        this.limbSwing += this.limbSwingAmount;
    }

	@Override
	public float getAttackSpeed() {
		return this.atkSpeed;
	}

	@Override
	public float getAttackRange() {
		return this.atkRange;
	}
	
	@Override
	public float getMoveSpeed() {
		return this.movSpeed;
	}
	
	@Override
	public int getAmmoLight() {
		return this.numAmmoLight;
	}

	@Override
	public int getAmmoHeavy() {
		return this.numAmmoHeavy;
	}

	@Override
	public boolean hasAmmoLight() {
		return this.numAmmoLight > 0;
	}

	@Override
	public boolean hasAmmoHeavy() {
		return false;
	}

	@Override
	public void setAmmoLight(int num) {
		this.numAmmoLight = num;
	}

	@Override
	public void setAmmoHeavy(int num) {
		this.numAmmoHeavy = num;
	}

	@Override
	public float getAttackDamage() {	//not used for rensouhou
		return 0;
	}

	@Override
	public boolean getIsRiding() {
		return false;
	}

	@Override
	public boolean getIsSprinting() {
		return false;
	}

	@Override
	public boolean getIsSitting() {
		return false;
	}

	@Override
	public boolean getIsSneaking() {
		return false;
	}
	
	@Override
	public ShipPathNavigate getShipNavigate() {
		return this.shipNavigator;
	}

	@Override
	public ShipMoveHelper getShipMoveHelper() {
		return this.shipMoveHelper;
	}
	
	//update ship move helper
	@Override
	protected void updateAITasks() {
		super.updateAITasks();
		
        EntityHelper.updateShipNavigator(this);
    }
	
	@Override
	public boolean canFly() {
		return false;
	}
	
	@Override
	public boolean canBreatheUnderwater() {
		return true;
	}

	@Override
	public boolean getIsLeashed() {
		return false;
	}

	@Override
	public int getLevel() {
		if(host != null) return this.host.getLevel();
		return 150;
	}

	@Override
	public boolean useAmmoLight() {
		return true;
	}

	@Override
	public boolean useAmmoHeavy() {
		return false;
	}
	
	@Override
	public int getStateMinor(int id) {
		return 0;
	}

	@Override
	public void setStateMinor(int state, int par1) {}
	
	@Override
	public float getEffectEquip(int id) {
		if(host != null) return host.getEffectEquip(id);
		return 0F;
	}
	
	@Override
	public float getDefValue() {
		return this.defValue;
	}

	@Override
	public void setEntitySit() {}

	@Override
	public float getModelRotate(int par1) {
		return 0;
	}

	@Override
	public void setModelRotate(int par1, float par2) {}

	@Override
	public boolean getAttackType(int par1) {
		return true;
	}

	@Override
	public int getPlayerUID() {
		if(host != null) return this.host.getPlayerUID();
		return -1;
	}

	@Override
	public void setPlayerUID(int uid) {}
	
	@Override
	public Entity getHostEntity() {
		return this.host;
	}

	@Override
	public int getDamageType() {
		return ID.ShipDmgType.DESTROYER;
	}
	
	@Override
	public Entity getEntityRevengeTarget() {
		return this.rvgTarget;
	}

	@Override
	public int getEntityRevengeTime() {
		return this.revengeTime;
	}

	@Override
	public void setEntityRevengeTarget(Entity target) {
		this.rvgTarget = target;
	}
  	
  	@Override
	public void setEntityRevengeTime() {
		this.revengeTime = this.ticksExisted;
	}
  	
  	@Override
	public int getAttackAniTick() {
		return this.StartEmotion3;
	}

	@Override
	public void setAttackAniTick(int par1) {
		this.StartEmotion3 = par1;
	}

	@Override
	public float getSwingTime(float partialTick) {
		return 0;
	}
	

}
