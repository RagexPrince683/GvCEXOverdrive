package com.glowingfederal.combatives.compat.mcheli;

import net.minecraft.entity.Entity;

/**
 * Optional boundary for MCHeli camera entities.
 *
 * <p>MCHeli's gunner/always-camera path installs {@code mcheli.MCH_ViewEntityDummy}
 * as Minecraft's render view entity.  The dummy extends EntityPlayerSP but is not
 * riding the vehicle, so a plain isRiding check incorrectly assigns its already
 * positioned camera to Combatives.  Class names keep this helper bootstrap-safe
 * when MCHeli is absent.</p>
 */
public final class MCHeliCameraCompat {
    private static final String VIEW_ENTITY_DUMMY = "mcheli.MCH_ViewEntityDummy";

    private MCHeliCameraCompat() { }

    public static boolean ownsCamera(Entity renderViewEntity) {
        return renderViewEntity != null && hasClassName(renderViewEntity.getClass(), VIEW_ENTITY_DUMMY);
    }

    private static boolean hasClassName(Class<?> type, String name) {
        for (Class<?> cursor = type; cursor != null; cursor = cursor.getSuperclass()) {
            if (name.equals(cursor.getName())) {
                return true;
            }
        }
        return false;
    }
}
