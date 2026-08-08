package handmadeguns.compat;

import cpw.mods.fml.common.Loader;
import handmadeguns.HandmadeGunsCore;
import handmadeguns.items.GunInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.Random;

/**
 * Optional Combatives camera-recoil adapter. This class keeps every Combatives
 * camera API lookup and call in one place so normal HMG client code can load
 * without Combatives on the classpath.
 */
public final class HMGRecoilBridge {
    private static final String KICK_ID = "hmg_overdrive:hmg_recoil_kick";
    private static final String PUNCH_ID = "hmg_overdrive:hmg_recoil_punch";
    private static final String SUSTAINED_ID = "hmg_overdrive:hmg_recoil_sustained";
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
    private static Method getApiVersionMethod;
    private static Method getCapabilitiesMethod;
    private static int apiVersion;
    private static Set<?> capabilities;
    private static boolean capabilitiesKnown;
    private static boolean supportsPitch;
    private static boolean supportsYaw;
    private static boolean supportsRoll;
    private static boolean supportsTranslation;
    private static boolean supportsFov;
    private static boolean supportsCustomImpulses;

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
        if (!isLocalFirstPersonPlayer(player) || gunInfo == null || !isCombativesCameraActive() || !supportsCustomImpulses || !supportsPitch) return false;

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
        float pitch = clamp(baseline * firstShot * sustained * perShotRateScale * 0.95F, 0.35F, 5.0F);

        float horizontalBase = pitch * (0.12F + heavyNorm * 0.04F);
        float targetDirection = STATE.previousHorizontalDirection;
        if (Math.abs(targetDirection) < 0.01F) targetDirection = random.nextBoolean() ? 1.0F : -1.0F;
        if (random.nextFloat() < 0.16F + Math.min(STATE.consecutiveShots, 8) * 0.015F) targetDirection = -targetDirection;
        STATE.horizontalDrift = clamp(STATE.horizontalDrift * 0.72F + targetDirection * (0.28F + random.nextFloat() * 0.18F), -1.0F, 1.0F);
        STATE.previousHorizontalDirection = STATE.horizontalDrift >= 0.0F ? 1.0F : -1.0F;
        float yaw = clamp(horizontalBase * STATE.horizontalDrift * 1.25F, -2.0F, 2.0F);
        float roll = clamp(-yaw * 0.55F, -2.2F, 2.2F);
        float rearward = clamp(-0.018F * pitch * (1.0F + heavyNorm * 0.35F), -0.14F, -0.006F);
        float duration = clamp(0.16F + heavyNorm * 0.06F, 0.15F, 0.24F);

        boolean kick = submitImpulse(KICK_ID, -pitch, supportsYaw ? yaw : 0.0F, supportsRoll ? roll : 0.0F,
                0.0F, supportsTranslation && ads ? -0.004F : 0.0F, supportsTranslation ? rearward : 0.0F,
                0.0F, duration, 0.0F, 0.0F, "SMOOTH", "STRONG", "ADD");
        if (!kick) {
            logRecoilFallback("base kick rejected");
            STATE.resetAll();
            return false;
        }

        submitImpulse(PUNCH_ID, -pitch * 0.12F, supportsYaw ? yaw * 0.22F : 0.0F, supportsRoll ? roll * 0.18F : 0.0F,
                0.0F, 0.0F, supportsTranslation ? rearward * 0.30F : 0.0F, 0.0F,
                0.11F, 0.0F, 22.0F + fireRateNorm * 10.0F, "SMOOTH", "NORMAL", "ADD");
        updateSustainedFire(player, gunInfo);
        return true;
    }

    public static void updateSustainedFire(EntityPlayer player, GunInfo gunInfo) {
        if (!isLocalFirstPersonPlayer(player) || gunInfo == null || STATE.consecutiveShots < 3 || !isCombativesCameraActive()) return;
        float rpm = gunInfo.rpm > 0 ? gunInfo.rpm : 600.0F;
        float fireRateNorm = clamp((rpm - 300.0F) / 900.0F, 0.0F, 1.0F);
        float pressure = clamp(0.01F + fireRateNorm * 0.025F + Math.min(STATE.consecutiveShots, 10) * 0.002F, 0.01F, 0.055F);
        submitImpulse(SUSTAINED_ID, -pressure * 4.0F, supportsYaw ? STATE.horizontalDrift * pressure * 5.0F : 0.0F,
                supportsRoll ? -STATE.horizontalDrift * pressure * 2.5F : 0.0F, 0.0F, 0.0F,
                supportsTranslation ? -pressure : 0.0F, 0.0F, 0.12F, 0.0F,
                12.0F + fireRateNorm * 10.0F, "SMOOTH", "BACKGROUND", "ADD");
    }

    public static void resetWeaponState() {
        STATE.resetAll();
    }

    /** Tick lifecycle hook. Burst state must not survive release, reload, death, or a lost camera API. */
    public static void onClientTick(EntityPlayer player, ItemStack weapon, boolean triggerDown, boolean reloading) {
        if (player == null || !player.isEntityAlive() || player.worldObj == null || reloading
                || (!triggerDown && System.currentTimeMillis() - STATE.lastShotTime > BURST_RESET_MS)
                || (STATE.weaponKey != 0 && STATE.weaponKey != weaponKey(weapon))) {
            resetWeaponState();
        }
    }

    public static boolean submitDiagnosticPitchOnlyImpulse() {
        if (!HandmadeGunsCore.enableCombativesRecoilDebug || !isCombativesCameraActive()) return false;
        return submitImpulse("hmg_overdrive:hmg_recoil_diag_pitch", -7.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.16F, 0.0F, 0.0F, "SMOOTH", "STRONG", "ADD");
    }

    public static boolean submitDiagnosticYawOnlyImpulse() {
        if (!HandmadeGunsCore.enableCombativesRecoilDebug || !isCombativesCameraActive()) return false;
        return supportsYaw && submitImpulse("hmg_overdrive:hmg_recoil_diag_yaw", 0.0F, 4.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.16F, 0.0F, 0.0F, "SMOOTH", "NORMAL", "ADD");
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
            discoverApiCapabilities();
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static void discoverApiCapabilities() {
        // API v1 introduced discovery. Pre-discovery builds are treated as the original
        // pitch-only custom-impulse surface; optional channels are never assumed.
        apiVersion = 0;
        capabilitiesKnown = false;
        supportsPitch = true;
        supportsCustomImpulses = true;
        supportsYaw = supportsRoll = supportsTranslation = supportsFov = false;
        try {
            getApiVersionMethod = apiClass.getMethod("getApiVersion");
            getCapabilitiesMethod = apiClass.getMethod("getCapabilities");
            apiVersion = ((Number) getApiVersionMethod.invoke(null)).intValue();
            Object value = getCapabilitiesMethod.invoke(null);
            if (value instanceof Set) {
                capabilities = (Set<?>) value;
                capabilitiesKnown = true;
                supportsPitch = hasCapability("ROTATION_PITCH");
                supportsYaw = hasCapability("ROTATION_YAW");
                supportsRoll = hasCapability("ROTATION_ROLL");
                supportsTranslation = hasCapability("TRANSLATION");
                supportsFov = hasCapability("FOV");
                supportsCustomImpulses = hasCapability("CUSTOM_IMPULSES");
            }
        } catch (Throwable ignored) {
            // Compatibility with camera API builds predating version/capability discovery.
        }
        HandmadeGunsCore.Debug("Combatives camera API discovered version=%s capabilitiesKnown=%s capabilities=%s", apiVersion, capabilitiesKnown, capabilities);
    }

    private static boolean hasCapability(String name) {
        if (capabilities == null) return false;
        for (Object capability : capabilities) if (name.equals(String.valueOf(capability))) return true;
        return false;
    }

    private static boolean submitImpulse(String id, float pitch, float yaw, float roll, float x, float y, float z, float fov, float duration, float attack, float frequency, String decay, String priority, String stacking) {
        try {
            Object builder = builderMethod.invoke(null, id);
            Class<?> builderClass = builder.getClass();
            builderClass.getMethod("rotation", float.class, float.class, float.class).invoke(builder, pitch, yaw, roll);
            if (supportsTranslation) builderClass.getMethod("translation", float.class, float.class, float.class).invoke(builder, x, y, z);
            if (supportsFov && fov != 0.0F) builderClass.getMethod("fov", float.class).invoke(builder, fov);
            builderClass.getMethod("duration", float.class).invoke(builder, duration);
            builderClass.getMethod("attackTime", float.class).invoke(builder, attack);
            builderClass.getMethod("oscillationFrequency", float.class).invoke(builder, frequency);
            builderClass.getMethod("decayType", decayTypeClass).invoke(builder, enumValue(decayTypeClass, decay));
            builderClass.getMethod("priority", priorityClass).invoke(builder, enumValue(priorityClass, priority));
            builderClass.getMethod("stackingMode", stackingModeClass).invoke(builder, enumValue(stackingModeClass, stacking));
            Object impulse = builderClass.getMethod("build").invoke(builder);
            boolean accepted = Boolean.TRUE.equals(submitImpulseMethod.invoke(null, impulse));
            logRecoilImpulse(id, pitch, yaw, roll, x, y, z, duration, attack, frequency, decay, priority, stacking, accepted);
            return accepted;
        } catch (Throwable t) {
            // A linkage/invocation failure is not a validation rejection. Disable the
            // cached bridge so hot firing paths immediately use legacy recoil thereafter.
            combativesAvailable = false;
            logRecoilImpulse(id, pitch, yaw, roll, x, y, z, duration, attack, frequency, decay, priority, stacking, false);
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

    private static void logRecoilImpulse(String id, float pitch, float yaw, float roll, float x, float y, float z, float duration, float attack, float frequency, String decay, String priority, String stacking, boolean accepted) {
        if (HandmadeGunsCore.enableCombativesRecoilDebug) {
            HandmadeGunsCore.Debug("Combatives recoil impulse id=%s pitch=%.3f yaw=%.3f roll=%.3f translate=(%.3f,%.3f,%.3f) duration=%.3f attack=%.3f frequency=%.3f decay=%s priority=%s stacking=%s accepted=%s", id, pitch, yaw, roll, x, y, z, duration, attack, frequency, decay, priority, stacking, accepted);
        }
    }

    private static void logRecoilFallback(String reason) {
        if (HandmadeGunsCore.enableCombativesRecoilDebug) {
            HandmadeGunsCore.Debug("Combatives recoil fallback used: %s ownership=legacy-look-rotation", reason);
        }
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
            if (HandmadeGunsCore.enableCombativesRecoilDebug) {
                HandmadeGunsCore.Debug("Combatives recoil weapon state reset weaponKey=%s previousShots=%s", weaponKey, consecutiveShots);
            }
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
