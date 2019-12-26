package hmggvcmob.entity;

import hmggvcmob.ai.AIAttackGun;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;

import javax.vecmath.Vector3d;

public interface IGVCmob {
    //���x�����������܂Ƃ߂ČĂׂ�悤�ɂ��悤
    float getviewWide();
    boolean canSeeTarget(Entity target);
    boolean canhearsound(Entity target);
    default Vector3d getSeeingPosition(){
        return getAttackGun().getSeeingPosition();
    }
    AIAttackGun getAttackGun();
}
