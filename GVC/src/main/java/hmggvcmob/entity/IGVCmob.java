package hmggvcmob.entity;

import hmggvcmob.tile.TileEntityFlag;
import net.minecraft.entity.Entity;

public interface IGVCmob {
    //今度初期処理をまとめて呼べるようにしよう
    float getviewWide();
    boolean canSeeTarget(Entity target);
    boolean canhearsound(Entity target);
    void setspawnedtile(TileEntityFlag flag);
}
