package hmggvcmob.entity;

import hmggvcmob.tile.TileEntityFlag;
import net.minecraft.entity.Entity;

public interface IGVCmob {
    //���x�����������܂Ƃ߂ČĂׂ�悤�ɂ��悤
    float getviewWide();
    boolean canSeeTarget(Entity target);
    boolean canhearsound(Entity target);
    void setspawnedtile(TileEntityFlag flag);
}
