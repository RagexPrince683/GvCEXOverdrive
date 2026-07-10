package handmadeguns.compat;

import handmadeguns.HandmadeGunsCore;
import handmadeguns.items.GunInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.Random;

/**
 * Client-side owner for real HMG aim recoil when Combatives owns the visual
 * camera impulse layer. Combatives offsets are render-only, so this controller
 * mutates the local player's pitch/yaw in small tick-stable steps and later
 * recovers only the recoil contribution it actually applied.
 */
public final class HMGAimRecoilController {
    private static final long BURST_RESET_MS = 180L;
    private static final float APPLY_FACTOR = 0.62F;
    private static final float MIN_STEP = 0.0025F;

    private static float pendingPitch;
    private static float pendingYaw;
    private static float accumulatedPitch;
    private static float accumulatedYaw;
    private static int burstShots;
    private static long lastShotTime;
    private static int weaponKey;
    private static long burstSeed;
    private static float horizontalDrift;
    private static float horizontalDirection;
    private static EntityPlayer owner;
    private static float previousPitch;
    private static float previousYaw;
    private static boolean havePreviousRotation;
    private static boolean active;
    private static long lastDebugTime;

    private HMGAimRecoilController() {}

    public static void onShot(EntityPlayer player, ItemStack weapon, GunInfo gunInfo, float recoilPitchAmount, boolean ads) {
        if (!HandmadeGunsCore.enableCombativesAimRecoilIntegration || !isEligible(player) || gunInfo == null) return;

        long now = System.currentTimeMillis();
        int key = weaponKey(weapon);
        if (owner != player || weaponKey != key || now - lastShotTime > BURST_RESET_MS) {
            resetBurstOnly(player, key, now ^ key ^ player.getCommandSenderName().hashCode());
        }

        owner = player;
        active = true;
        burstShots++;
        lastShotTime = now;
        if (!havePreviousRotation) {
            previousPitch = player.rotationPitch;
            previousYaw = player.rotationYaw;
            havePreviousRotation = true;
        }

        Random random = new Random(burstSeed + burstShots * 1103515245L);
        float baseline = clamp(Math.abs(recoilPitchAmount), 0.05F, 12.0F);
        float rpm = gunInfo.rpm > 0 ? gunInfo.rpm : Math.max(60.0F, 1200.0F / Math.max(1.0F, gunInfo.cycle));
        float fireRateNorm = clamp((rpm - 300.0F) / 900.0F, 0.0F, 1.0F);
        float heavyNorm = 1.0F - fireRateNorm;
        float shotIndex = Math.min(burstShots - 1, 8);
        float firstShot = burstShots == 1 ? 1.25F + heavyNorm * 0.20F : 1.0F;
        float build = 1.0F + Math.min(shotIndex, 4.0F) * (0.05F + fireRateNorm * 0.025F);
        float sustained = burstShots > 5 ? 0.92F + random.nextFloat() * 0.10F : build;
        float perShotRateScale = 1.0F - fireRateNorm * 0.25F;

        float pitch = clamp(baseline * firstShot * sustained * perShotRateScale * (float) HandmadeGunsCore.combativesAimRecoilVerticalScale, 0.05F, maxPitchCap());
        float targetDirection = horizontalDirection;
        if (Math.abs(targetDirection) < 0.01F) targetDirection = random.nextBoolean() ? 1.0F : -1.0F;
        if (random.nextFloat() < 0.14F + Math.min(burstShots, 8) * 0.015F) targetDirection = -targetDirection;
        horizontalDrift = clamp(horizontalDrift * 0.70F + targetDirection * (0.25F + random.nextFloat() * 0.16F), -1.0F, 1.0F);
        horizontalDirection = horizontalDrift >= 0.0F ? 1.0F : -1.0F;
        float yaw = clamp(pitch * (0.10F + heavyNorm * 0.03F) * horizontalDrift * (float) HandmadeGunsCore.combativesAimRecoilHorizontalScale, -maxYawCap(), maxYawCap());

        float availablePitch = Math.max(0.0F, maxPitchCap() - Math.abs(accumulatedPitch + pendingPitch));
        float availableYaw = Math.max(0.0F, maxYawCap() - Math.abs(accumulatedYaw + pendingYaw));
        float queuedPitch = Math.min(pitch, availablePitch);
        float queuedYaw = clamp(yaw, -availableYaw, availableYaw);
        pendingPitch += queuedPitch;
        pendingYaw += queuedYaw;
        logShot(queuedPitch, queuedYaw);
    }

    public static void onClientTick(EntityPlayer player) {
        if (player == null) return;
        if (!isEligible(player)) {
            resetAll("invalid-player");
            return;
        }
        if (owner != null && owner != player) resetAll("player-replaced");
        owner = player;
        active = active || Math.abs(pendingPitch) > MIN_STEP || Math.abs(pendingYaw) > MIN_STEP || Math.abs(accumulatedPitch) > MIN_STEP || Math.abs(accumulatedYaw) > MIN_STEP;
        if (!active) {
            previousPitch = player.rotationPitch;
            previousYaw = player.rotationYaw;
            havePreviousRotation = true;
            return;
        }

        float mousePitch = havePreviousRotation ? normalizePitchDelta(player.rotationPitch - previousPitch) : 0.0F;
        float mouseYaw = havePreviousRotation ? normalizeYaw(player.rotationYaw - previousYaw) : 0.0F;
        // Upward recoil in Minecraft is negative pitch. Positive mouse pitch means the player pulled down.
        if (mousePitch > 0.0F && accumulatedPitch > 0.0F) accumulatedPitch = Math.max(0.0F, accumulatedPitch - mousePitch);
        if (Math.signum(mouseYaw) == -Math.signum(accumulatedYaw)) accumulatedYaw -= Math.signum(accumulatedYaw) * Math.min(Math.abs(accumulatedYaw), Math.abs(mouseYaw));

        float appliedPitch = applyPendingPitch(player);
        float appliedYaw = applyPendingYaw(player);
        long sinceShot = lastShotTime == 0L ? Long.MAX_VALUE : System.currentTimeMillis() - lastShotTime;
        boolean recovering = sinceShot >= HandmadeGunsCore.combativesAimRecoilRecoveryDelayMs;
        float recoveredPitch = 0.0F;
        float recoveredYaw = 0.0F;
        if (recovering) {
            float recoveryFactor = clamp((float) HandmadeGunsCore.combativesAimRecoilRecoverySpeed / 20.0F, 0.01F, 0.45F);
            recoveredPitch = recoverPitch(player, recoveryFactor);
            recoveredYaw = recoverYaw(player, recoveryFactor);
        }
        if (Math.abs(pendingPitch) <= MIN_STEP && Math.abs(pendingYaw) <= MIN_STEP && Math.abs(accumulatedPitch) <= MIN_STEP && Math.abs(accumulatedYaw) <= MIN_STEP) active = false;
        logTick(appliedPitch, appliedYaw, recoveredPitch, recoveredYaw, mousePitch, mouseYaw, sinceShot, recovering);
        previousPitch = player.rotationPitch;
        previousYaw = player.rotationYaw;
        havePreviousRotation = true;
    }

    public static void reset(String reason) { resetAll(reason); }
    public static void resetWeaponBuildUp() { burstShots = 0; horizontalDrift = 0.0F; horizontalDirection = 0.0F; weaponKey = 0; }
    public static float getAccumulatedPitch() { return accumulatedPitch; }
    public static float getAccumulatedYaw() { return accumulatedYaw; }

    private static float applyPendingPitch(EntityPlayer player) {
        if (Math.abs(pendingPitch) <= MIN_STEP) return 0.0F;
        float step = pendingPitch * APPLY_FACTOR;
        player.rotationPitch = clamp(player.rotationPitch - step, -90.0F, 90.0F);
        pendingPitch -= step;
        accumulatedPitch += step;
        return step;
    }

    private static float applyPendingYaw(EntityPlayer player) {
        if (Math.abs(pendingYaw) <= MIN_STEP) return 0.0F;
        float step = pendingYaw * APPLY_FACTOR;
        player.rotationYaw += step;
        pendingYaw -= step;
        accumulatedYaw += step;
        return step;
    }

    private static float recoverPitch(EntityPlayer player, float factor) {
        if (Math.abs(accumulatedPitch) <= MIN_STEP) return 0.0F;
        float step = accumulatedPitch * factor;
        player.rotationPitch = clamp(player.rotationPitch + step, -90.0F, 90.0F);
        accumulatedPitch -= step;
        return step;
    }

    private static float recoverYaw(EntityPlayer player, float factor) {
        if (Math.abs(accumulatedYaw) <= MIN_STEP) return 0.0F;
        float step = accumulatedYaw * factor;
        player.rotationYaw -= step;
        accumulatedYaw -= step;
        return step;
    }

    private static void resetBurstOnly(EntityPlayer player, int key, long seed) {
        owner = player;
        weaponKey = key;
        burstSeed = seed;
        burstShots = 0;
        horizontalDrift = 0.0F;
        horizontalDirection = 0.0F;
        previousPitch = player.rotationPitch;
        previousYaw = player.rotationYaw;
        havePreviousRotation = true;
    }

    private static void resetAll(String reason) {
        pendingPitch = pendingYaw = accumulatedPitch = accumulatedYaw = 0.0F;
        burstShots = 0;
        lastShotTime = 0L;
        weaponKey = 0;
        burstSeed = 0L;
        horizontalDrift = horizontalDirection = 0.0F;
        owner = null;
        havePreviousRotation = false;
        active = false;
        if (HandmadeGunsCore.enableCombativesRecoilDebug) HandmadeGunsCore.Debug("Combatives aim recoil reset reason=%s", reason);
    }

    private static boolean isEligible(EntityPlayer player) {
        Minecraft mc = Minecraft.getMinecraft();
        return HandmadeGunsCore.enableCombativesAimRecoilIntegration && mc != null && player != null && mc.thePlayer == player && player.worldObj != null && player.isEntityAlive() && !player.isDead && mc.gameSettings.thirdPersonView == 0;
    }

    private static int weaponKey(ItemStack stack) { return stack == null || stack.getItem() == null ? 0 : System.identityHashCode(stack.getItem()) * 31 + stack.getItemDamage(); }
    private static float maxPitchCap() { return Math.max(0.5F, (float) HandmadeGunsCore.combativesAimRecoilMaxPitch); }
    private static float maxYawCap() { return Math.max(0.25F, (float) HandmadeGunsCore.combativesAimRecoilMaxYaw); }
    private static float clamp(float value, float min, float max) { return value < min ? min : value > max ? max : value; }
    private static float normalizePitchDelta(float value) { return clamp(value, -180.0F, 180.0F); }
    private static float normalizeYaw(float value) { while (value > 180.0F) value -= 360.0F; while (value < -180.0F) value += 360.0F; return value; }

    private static void logShot(float pitch, float yaw) {
        if (HandmadeGunsCore.enableCombativesRecoilDebug) HandmadeGunsCore.Debug("Combatives aim recoil shot weaponKey=%s ownership=new-aim shotPitch=%.3f shotYaw=%.3f pending=(%.3f,%.3f) accumulated=(%.3f,%.3f)", weaponKey, pitch, yaw, pendingPitch, pendingYaw, accumulatedPitch, accumulatedYaw);
    }

    private static void logTick(float appliedPitch, float appliedYaw, float recoveredPitch, float recoveredYaw, float mousePitch, float mouseYaw, long sinceShot, boolean recovering) {
        if (!HandmadeGunsCore.enableCombativesRecoilDebug) return;
        long now = System.currentTimeMillis();
        if (now - lastDebugTime < 50L && appliedPitch == 0.0F && appliedYaw == 0.0F && recoveredPitch == 0.0F && recoveredYaw == 0.0F) return;
        lastDebugTime = now;
        HandmadeGunsCore.Debug("Combatives aim recoil tick weaponKey=%s ownership=new-aim pending=(%.3f,%.3f) applied=(%.3f,%.3f) accumulated=(%.3f,%.3f) sinceShotMs=%s recovery=%s recovered=(%.3f,%.3f) mouseDelta=(%.3f,%.3f)", weaponKey, pendingPitch, pendingYaw, appliedPitch, appliedYaw, accumulatedPitch, accumulatedYaw, sinceShot, recovering, recoveredPitch, recoveredYaw, mousePitch, mouseYaw);
    }
}
