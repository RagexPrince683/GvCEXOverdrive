package handmadeguns.client;

import net.minecraft.entity.item.EntityItem;
import org.lwjgl.opengl.GL11;

import static handmadeguns.HandmadeGunsCore.enableGunGroundPhysicsRender;

public class HMGDroppedGunRenderHelper {

    public static boolean isDroppedEntityRender(Object... data) {
        if (data == null) return false;
        for (Object object : data) {
            if (object instanceof EntityItem) return true;
        }
        return false;
    }

    public static boolean applyGroundTransform(Object... data) {
        if (!enableGunGroundPhysicsRender || !isDroppedEntityRender(data)) return false;

        EntityItem entityItem = null;
        for (Object object : data) {
            if (object instanceof EntityItem) {
                entityItem = (EntityItem) object;
                break;
            }
        }

        float yaw = entityItem == null ? 0.0F : ((entityItem.getEntityId() * 37) % 360);
        GL11.glTranslatef(0.0F, -0.08F, 0.0F);
        GL11.glRotatef(yaw, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
        GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
        return true;
    }
}
