package hmggvcmob.entity;

import handmadeguns.Util.GunsUtils;
import handmadeguns.entity.IMGGunner;
import handmadevehicle.entity.EntityDummy_rider;
import handmadevehicle.entity.parts.logics.BaseLogic;
import hmggvcmob.ai.newai.AIAttackEntityByGun;
import hmggvcmob.entity.util.MoveToPositionMng;
import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;

import javax.vecmath.Vector3d;

public interface IGVCmob extends IMGGunner {
    //今度初期処理をまとめて呼べるようにしよう
    float getviewWide();
    boolean canSeeTarget(Entity target);
    boolean canhearsound(Entity target);
    default boolean canSeePos(Vector3d target){
        if(target == null)return false;
        if(((Entity)this).ridingEntity instanceof EntityDummy_rider){
            BaseLogic connected = ((EntityDummy_rider) ((Entity)this).ridingEntity).linkedBaseLogic;
            if(connected.seatObjects[((EntityDummy_rider) ((Entity)this).ridingEntity).linkedSeatID].prefab_seat.isBlindedSeat)return false;
        }
        Vec3 vec3 = Vec3.createVectorHelper(((Entity)this).posX, ((Entity)this).posY + ((Entity)this).getEyeHeight(), ((Entity)this).posZ);
        Vec3 vec31 = Vec3.createVectorHelper(target.x, target.y, target.z);

        //衝突するブロックを調べる
        return GunsUtils.getMovingObjectPosition_forBlock_CheckEmpty(((Entity)this).worldObj,vec3, vec31,10);
    }

    void setCanDespawn(boolean canDespawn);
    void setAimPos(Vector3d aimPos);
    Vector3d getAimPos();

    MoveToPositionMng getMoveToPositionMng();
}
