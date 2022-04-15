package hmggvcmob.tile;

import hmggvcmob.camp.CampObjAndPos;
import hmggvcmob.entity.IGVCmob;
import hmggvcmob.entity.IHasVehicleGacha;
import hmggvcmob.entity.guerrilla.EntityGBases;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import javax.vecmath.Vector3d;
import java.util.Random;

import static hmggvcmob.GVCMobPlus.campsHash;

public class TileEntityMobSpawner_OneTime extends TileEntity {
    private static final String __OBFID = "CL_00000360";

    public String mobName;
    public String vehicleName;
    public Vector3d offset = new Vector3d(0,0,0);
    public float performDist = 65536;//(256)

    public void updateEntity()
    {
        super.updateEntity();
        worldObj = getWorldObj();
        if(worldObj == null || isInvalid() || worldObj.isRemote)return;
        EntityPlayer closestPlayer = worldObj.getClosestPlayer(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D, performDist);
        Random rnd = new Random();
        if(closestPlayer != null && !closestPlayer.capabilities.isCreativeMode){
            Entity entity = EntityList.createEntityByName(mobName, worldObj);
            entity.setLocationAndAngles(
                    this.xCoord + 0.5D + offset.x,
                    this.yCoord + 0.5D + offset.y,
                    this.zCoord + 0.5D + offset.z,rnd.nextInt(360),rnd.nextInt(180)-90);

            if(entity instanceof IHasVehicleGacha){
                ((IHasVehicleGacha) entity).setVehicleName(vehicleName);
            }
            if(entity instanceof IGVCmob){
                ((IGVCmob) entity).setCanDespawn(false);
            }

            worldObj.spawnEntityInWorld(entity);
            this.invalidate();
            worldObj.setBlockToAir(this.xCoord,this.yCoord,this.zCoord);
        };
    }

    public TileEntityMobSpawner_OneTime(){
    }

    public void readFromNBT(NBTTagCompound p_145839_1_)
    {
        super.readFromNBT(p_145839_1_);
        mobName = p_145839_1_.getString("mobName");
        vehicleName = p_145839_1_.getString("vehicleName");
        int[] pos = p_145839_1_.getIntArray("offset");
        offset = new Vector3d(pos[0],pos[1],pos[2]);
    }
    public void writeToNBT(NBTTagCompound p_145841_1_)
    {
        super.writeToNBT(p_145841_1_);
        p_145841_1_.setString("mobName",mobName);
        p_145841_1_.setString("vehicleName",vehicleName);
        p_145841_1_.setIntArray("offset",new int[]{(int)offset.x,(int)offset.y,(int)offset.z});
    }
}
