package hmggvcmob.entity.guerrilla;


import hmggvcmob.entity.friend.EntitySoBase;
import hmggvcmob.entity.friend.EntitySoBases;
import hmggvcmob.entity.friend.GVCEntitySoldierHeli;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import java.util.List;

public class GVCEntityGuerrilla_HeliSpawner extends EntitySoBase
{
    public GVCEntityGuerrilla_HeliSpawner(World par1World)
    {
        super(par1World);
        setSize(20,10);
    }
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(movespeed = 0.33000000417232513D);
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(20.0D);
        this.getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(60.0D);
        //this.getEntityAttribute(SharedMonsterAttributes.knockbackResistance).setBaseValue(30.0D);
    }
    
    public void addRandomArmor()
    {
        super.addRandomArmor();
    }
    public void onUpdate(){
        if(!worldObj.isRemote) {
            GVCEntityWZ10AttackHeli soldierHeli = new GVCEntityWZ10AttackHeli(worldObj);
            soldierHeli.copyLocationAndAnglesFrom(this);
            worldObj.spawnEntityInWorld(soldierHeli);
        }
        setDead();
    }
    
	public boolean isConverting() {
		return false;
	}
	
    protected String getLivingSound()
    {
        return "mob.skeleton.say";
    }

    protected String getHurtSound()
    {
        return "mob.skeleton.hurt";
    }

    protected String getDeathSound()
    {
        return "mob.skeleton.death";
    }

    protected void func_145780_a(int p_145780_1_, int p_145780_2_, int p_145780_3_, Block p_145780_4_)
    {
        this.playSound("mob.skeleton.step", 0.15F, 1.0F);
    }
    public boolean getCanSpawnHere()
    {
        return super.getCanSpawnHere() && this.isValidLightLevel();
    }
    protected boolean isValidLightLevel()
    {
        EntityPlayer player = worldObj.getClosestPlayer(posX, posY, posZ, -1);
        List nearEntitys = worldObj.getEntitiesWithinAABBExcludingEntity(player, AxisAlignedBB.getBoundingBox(player.posX - 32, player.posY - 32, player.posZ - 32, player.posX + 32, player.posY + 32, player.posZ + 32));
        int frndcnt = 0;
        for (Object te : nearEntitys) {
            if (!(te instanceof EntityLiving)) {
                continue;
            }
            if(te instanceof EntitySoBases){
                frndcnt++;
            }
        }
        if(frndcnt >8){
            return true;
        }else {
            return false;
        }
    }
}
