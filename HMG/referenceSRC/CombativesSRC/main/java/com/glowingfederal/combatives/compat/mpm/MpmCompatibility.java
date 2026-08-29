package com.glowingfederal.combatives.compat.mpm;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Collections;
import java.util.WeakHashMap;

import com.glowingfederal.combatives.config.CombativesConfig;
import cpw.mods.fml.common.Loader;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

/** Optional, common-side boundary around MPM+. No MPM type appears in a signature. */
public final class MpmCompatibility {
    public static final int DEFAULT_RAW_SIZE = 5;
    private static final String MOD_ID = "moreplayermodels";
    private static boolean initialized;
    private static boolean available;
    private static Method getData;
    private static Method getEntity;
    private static Field size;
    private static final Map<EntityPlayer, Geometry> serverGeometry = Collections.synchronizedMap(
            new WeakHashMap<EntityPlayer, Geometry>());

    private MpmCompatibility() { }

    public static Geometry resolve(EntityPlayer player) {
        if (player != null && player.worldObj != null && player.worldObj.isRemote) {
            Geometry synchronizedGeometry = serverGeometry.get(player);
            if (synchronizedGeometry != null) return synchronizedGeometry;
        }
        return resolveLocal(player);
    }

    /** Resolve MPM's local cache. On a server this is the authoritative saved model data. */
    public static Geometry resolveLocal(EntityPlayer player) {
        if (!CombativesConfig.enableMpmHitboxScaling || player == null || !isAvailable()) {
            return Geometry.DEFAULT;
        }
        try {
            Object data = getData.invoke(null, player);
            if (data == null) return Geometry.DEFAULT;
            int raw = size.getInt(data);
            // MPM's NBT reader and /size command define 1..10; its renderer uses size / 5 uniformly.
            if (raw < 1 || raw > 10) return new Geometry(raw, 1.0F, 1.0F, 1.0F, false, null);
            float resolved = raw / (float) DEFAULT_RAW_SIZE;
            if (!isUsable(resolved)) return new Geometry(raw, 1.0F, 1.0F, 1.0F, false, null);

            Object disguised = getEntity.invoke(data, player);
            if (disguised instanceof EntityLivingBase) {
                EntityLivingBase entity = (EntityLivingBase) disguised;
                float widthScale = entity.width / 0.6F;
                float heightScale = entity.height / 1.8F;
                float eyeScale = entity.getEyeHeight() / 1.62F;
                if (isUsable(widthScale) && isUsable(heightScale)) {
                    if (!isUsable(eyeScale)) eyeScale = heightScale;
                    return new Geometry(raw, resolved * widthScale, resolved * heightScale,
                            resolved * eyeScale, true, entity.getClass().getName());
                }
            }
            return new Geometry(raw, resolved, resolved, resolved, true, null);
        } catch (Throwable ignored) {
            // Optional compatibility must degrade normally if an incompatible MPM revision is present.
            return Geometry.DEFAULT;
        }
    }

    /** Install geometry resolved by the server; client MPM render data is never authoritative. */
    public static void applyServerGeometry(EntityPlayer player, Geometry geometry) {
        if (player != null && geometry != null) serverGeometry.put(player, geometry);
    }

    public static boolean hasEntityDisguise(EntityPlayer player) {
        if (player == null || !isAvailable()) return false;
        try {
            Object data = getData.invoke(null, player);
            return data != null && getEntity.invoke(data, player) instanceof EntityLivingBase;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static boolean isUsable(float value) {
        return !Float.isNaN(value) && !Float.isInfinite(value) && value > 0.0F;
    }

    private static synchronized boolean isAvailable() {
        if (initialized) return available;
        initialized = true;
        if (!Loader.isModLoaded(MOD_ID)) return false;
        try {
            Class<?> modelData = Class.forName("noppes.mpm.ModelData", false,
                    MpmCompatibility.class.getClassLoader());
            getData = modelData.getMethod("getData", EntityPlayer.class);
            getEntity = modelData.getMethod("getEntity", EntityPlayer.class);
            size = modelData.getField("size");
            available = true;
        } catch (Throwable ignored) {
            available = false;
        }
        return available;
    }

    public static final class Geometry {
        public static final Geometry DEFAULT = new Geometry(DEFAULT_RAW_SIZE, 1.0F, 1.0F, 1.0F, false, null);
        public final int rawSize;
        public final float widthScale;
        public final float heightScale;
        public final float eyeScale;
        public final boolean fromMpm;
        public final String disguiseClass;

        public Geometry(int rawSize, float widthScale, float heightScale, float eyeScale,
                boolean fromMpm, String disguiseClass) {
            this.rawSize = rawSize;
            this.widthScale = widthScale;
            this.heightScale = heightScale;
            this.eyeScale = eyeScale;
            this.fromMpm = fromMpm;
            this.disguiseClass = disguiseClass;
        }
    }
}
