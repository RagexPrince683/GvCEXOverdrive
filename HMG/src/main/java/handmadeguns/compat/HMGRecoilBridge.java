package handmadeguns.compat;

import cpw.mods.fml.common.Loader;
import handmadeguns.HandmadeGunsCore;
import handmadeguns.items.GunInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.lang.reflect.Method;
import java.util.Random;

/**
 * Optional Combatives camera-recoil adapter. This class keeps every Combatives
 * camera API lookup and call in one place so normal HMG client code can load
 * without Combatives on the classpath.
 */
public final class HMGRecoilBridge {
    private static final String SOURCE_ID = "hmg_overdrive:weapon_recoil";
    private static final String PUNCH_ID = "hmg_overdrive:weapon_recoil/punch";
    private static final long BURST_RESET_MS = 180L;

    private static boolean availabilityChecked;
    private static boolean combativesAvailable;
    private static boolean loggedEnabled;
    private static boolean loggedFallback;
    private static Class<?> apiClass;
    private static Class<?> impulseClass;
    private static Class<?> decayTypeClass;
    private static Class<?> priorityClass;
    private static Class<?> stackingModeClass;
    private static Method isAvailableMethod;
    private static Method submitImpulseMethod;
    private static Method builderMethod;

    private static final RecoilState STATE = new RecoilState();

    private HMGRecoilBridge() {}

    public static boolean isCombativesAvailable() {
        if (!availabilityChecked) {
            availabilityChecked = true;
            combativesAvailable = Loader.isModLoaded("combatives") && loadCameraApi();
        }
        return combativesAvailable;
    }

    public static boolean isCombativesCameraActive() {
        if (!HandmadeGunsCore.enableCombativesRecoilIntegration || !isCombativesAvailable()) {
            logFallbackOnce();
            return false;
        }
        try {
            boolean active = Boolean.TRUE.equals(isAvailableMethod.invoke(null));
            if (active) logEnabledOnce(); else logFallbackOnce();
            return active;
        } catch (Throwable t) {
            combativesAvailable = false;
            logFallbackOnce();
            return false;
        }
    }

    public static boolean applyShotRecoil(EntityPlayer player, ItemStack weapon, GunInfo gunInfo, float recoilPitchAmount, boolean ads) {
        if (!isLocalFirstPersonPlayer(player) || gunInfo == null || !isCombativesCameraActive()) return false;

        long now = System.currentTimeMillis();
        int weaponKey = weaponKey(weapon);
        if (STATE.weaponKey != weaponKey || now - STATE.lastShotTime > BURST_RESET_MS) {
            STATE.resetBurst(weaponKey, now ^ weaponKey ^ player.getCommandSenderName().hashCode());
        }

        STATE.consecutiveShots++;
        STATE.lastShotTime = now;

        Random random = new Random(STATE.burstSeed + STATE.consecutiveShots * 1103515245L);
        float baseline = clamp(Math.abs(recoilPitchAmount), 0.05F, 12.0F);
        float rpm = gunInfo.rpm > 0 ? gunInfo.rpm : Math.max(60.0F, 1200.0F / Math.max(1.0F, gunInfo.cycle));
        float fireRateNorm = clamp((rpm - 300.0F) / 900.0F, 0.0F, 1.0F);
        float heavyNorm = 1.0F - fireRateNorm;
        float shotIndex = Math.min(STATE.consecutiveShots - 1, 8);
        float firstShot = STATE.consecutiveShots == 1 ? 1.35F + heavyNorm * 0.25F : 1.0F;
        float build = 1.0F + Math.min(shotIndex, 4.0F) * (0.045F + fireRateNorm * 0.025F);
        float sustained = STATE.consecutiveShots > 5 ? 0.94F + random.nextFloat() * 0.10F : build;
        float perShotRateScale = 1.0F - fireRateNorm * 0.22F;
        float pitch = clamp(baseline * firstShot * sustained * perShotRateScale, 0.04F, 9.0F);

        float horizontalBase = pitch * (0.12F + heavyNorm * 0.04F);
        float targetDirection = STATE.previousHorizontalDirection;
        if (Math.abs(targetDirection) < 0.01F) targetDirection = random.nextBoolean() ? 1.0F : -1.0F;
        if (random.nextFloat() < 0.16F + Math.min(STATE.consecutiveShots, 8) * 0.015F) targetDirection = -targetDirection;
        STATE.horizontalDrift = clamp(STATE.horizontalDrift * 0.72F + targetDirection * (0.28F + random.nextFloat() * 0.18F), -1.0F, 1.0F);
        STATE.previousHorizontalDirection = STATE.horizontalDrift >= 0.0F ? 1.0F : -1.0F;
        float yaw = clamp(horizontalBase * STATE.horizontalDrift, -2.0F, 2.0F);
        float roll = clamp(-yaw * 0.55F, -2.2F, 2.2F);
        float rearward = clamp(-0.018F * pitch * (1.0F + heavyNorm * 0.35F), -0.14F, -0.006F);
        float duration = clamp(0.13F + heavyNorm * 0.09F, 0.10F, 0.25F);

        boolean main = submitImpulse(SOURCE_ID, -pitch, yaw, roll, 0.0F, ads ? -0.004F : 0.0F, rearward, duration, 0.0F, 0.0F, "SMOOTH", "STRONG", "ADD");
        boolean punch = submitImpulse(PUNCH_ID, -pitch * 0.30F, yaw * 0.35F, roll * 0.25F, 0.0F, 0.0F, rearward * 0.45F, 0.055F, 0.0F, 34.0F + fireRateNorm * 12.0F, "SMOOTH", "NORMAL", "ADD");
        updateSustainedFire(player, gunInfo);
        return main || punch;
    }

    public static void updateSustainedFire(EntityPlayer player, GunInfo gunInfo) {
        if (!isLocalFirstPersonPlayer(player) || gunInfo == null || STATE.consecutiveShots < 3 || !isCombativesCameraActive()) return;
        float rpm = gunInfo.rpm > 0 ? gunInfo.rpm : 600.0F;
        float fireRateNorm = clamp((rpm - 300.0F) / 900.0F, 0.0F, 1.0F);
        float pressure = clamp(0.01F + fireRateNorm * 0.025F + Math.min(STATE.consecutiveShots, 10) * 0.002F, 0.01F, 0.055F);
        submitImpulse("hmg_overdrive:weapon_recoil/pressure", -pressure * 18.0F, STATE.horizontalDrift * pressure * 4.0F, -STATE.horizontalDrift * pressure * 2.5F, 0.0F, 0.0F, -pressure, 0.09F, 0.0F, 18.0F + fireRateNorm * 18.0F, "SMOOTH", "BACKGROUND", "ADD");
    }

    public static void resetWeaponState() {
        STATE.resetAll();
    }

    private static boolean loadCameraApi() {
        try {
            apiClass = Class.forName("com.combatives.api.camera.CombativesCameraAPI");
            impulseClass = Class.forName("com.combatives.api.camera.CameraImpulse");
            decayTypeClass = Class.forName("com.combatives.api.camera.CameraDecayType");
            priorityClass = Class.forName("com.combatives.api.camera.CameraPriority");
            stackingModeClass = Class.forName("com.combatives.api.camera.CameraStackingMode");
            isAvailableMethod = apiClass.getMethod("isAvailable");
            submitImpulseMethod = apiClass.getMethod("submitImpulse", impulseClass);
            builderMethod = impulseClass.getMethod("builder", String.class);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static boolean submitImpulse(String id, float pitch, float yaw, float roll, float x, float y, float z, float duration, float attack, float frequency, String decay, String priority, String stacking) {
        try {
            Object builder = builderMethod.invoke(null, id);
            Class<?> builderClass = builder.getClass();
            builderClass.getMethod("rotation", float.class, float.class, float.class).invoke(builder, pitch, yaw, roll);
            builderClass.getMethod("translation", float.class, float.class, float.class).invoke(builder, x, y, z);
            builderClass.getMethod("duration", float.class).invoke(builder, duration);
            builderClass.getMethod("attackTime", float.class).invoke(builder, attack);
            builderClass.getMethod("oscillationFrequency", float.class).invoke(builder, frequency);
            builderClass.getMethod("decayType", decayTypeClass).invoke(builder, enumValue(decayTypeClass, decay));
            builderClass.getMethod("priority", priorityClass).invoke(builder, enumValue(priorityClass, priority));
            builderClass.getMethod("stackingMode", stackingModeClass).invoke(builder, enumValue(stackingModeClass, stacking));
            Object impulse = builderClass.getMethod("build").invoke(builder);
            return Boolean.TRUE.equals(submitImpulseMethod.invoke(null, impulse));
        } catch (Throwable t) {
            return false;
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Object enumValue(Class<?> enumClass, String name) {
        return Enum.valueOf((Class<? extends Enum>) enumClass.asSubclass(Enum.class), name);
    }

    private static boolean isLocalFirstPersonPlayer(EntityPlayer player) {
        Minecraft mc = Minecraft.getMinecraft();
        return mc != null && player != null && mc.thePlayer == player && mc.gameSettings.thirdPersonView == 0;
    }

    private static int weaponKey(ItemStack stack) {
        if (stack == null) return 0;
        int damage = stack.getItemDamage();
        int id = stack.getItem() == null ? 0 : System.identityHashCode(stack.getItem());
        return id * 31 + damage;
    }

    private static float clamp(float value, float min, float max) {
        return value < min ? min : value > max ? max : value;
    }

    private static void logEnabledOnce() {
        if (!loggedEnabled) {
            loggedEnabled = true;
            HandmadeGunsCore.Debug("Combatives camera recoil integration detected and enabled.");
        }
    }

    private static void logFallbackOnce() {
        if (!loggedFallback) {
            loggedFallback = true;
            HandmadeGunsCore.Debug("Combatives camera recoil unavailable or disabled; using legacy HMG recoil.");
        }
    }

    private static final class RecoilState {
        int consecutiveShots;
        long lastShotTime;
        float horizontalDrift;
        float previousHorizontalDirection;
        long burstSeed;
        int weaponKey;

        void resetBurst(int weaponKey, long seed) {
            this.consecutiveShots = 0;
            this.lastShotTime = 0L;
            this.horizontalDrift = 0.0F;
            this.previousHorizontalDirection = 0.0F;
            this.burstSeed = seed;
            this.weaponKey = weaponKey;
        }

        void resetAll() {
            resetBurst(0, 0L);
        }
    }
}
