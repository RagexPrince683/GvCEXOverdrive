package hmggvcmob.entity;

import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;

public interface IGVCmob {
    //���x�����������܂Ƃ߂ČĂׂ�悤�ɂ��悤
    float getviewWide();
    boolean canSeeTarget(Entity target);
    boolean canhearsound(Entity target);
}
