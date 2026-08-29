package handmadeguns.compat;

import cpw.mods.fml.common.Loader;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/** Common-side optional adapter for Combatives' authoritative gameplay ray. */
public final class HMGPointOfAimBridge {
    private static boolean resolutionAttempted;
    private static boolean available;
    private static Method authoritativeMethod;
    private static Field originField;
    private static Field directionField;

    private HMGPointOfAimBridge() { }

    public static AimRay getAuthoritativeRay(EntityPlayer player) {
        if (player == null || !resolve()) return null;
        try {
            Object ray = authoritativeMethod.invoke(null, player);
            Object origin = originField.get(ray);
            Object direction = directionField.get(ray);
            if (!(origin instanceof Vec3) || !(direction instanceof Vec3)) return null;
            return new AimRay((Vec3) origin, (Vec3) direction);
        } catch (Throwable ignored) {
            available = false;
            return null;
        }
    }

    private static boolean resolve() {
        if (resolutionAttempted) return available;
        resolutionAttempted = true;
        if (!Loader.isModLoaded("combatives")) return false;
        try {
            Class<?> rayClass = Class.forName("com.glowingfederal.combatives.interaction.InteractionRay");
            authoritativeMethod = rayClass.getMethod("authoritative", EntityPlayer.class);
            originField = rayClass.getField("origin");
            directionField = rayClass.getField("direction");
            available = true;
        } catch (Throwable ignored) {
            available = false;
        }
        return available;
    }

    public static final class AimRay {
        public final Vec3 origin;
        public final Vec3 direction;

        private AimRay(Vec3 origin, Vec3 direction) {
            this.origin = origin;
            this.direction = direction;
        }
    }
}
