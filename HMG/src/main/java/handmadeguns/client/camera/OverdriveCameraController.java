package handmadeguns.client.camera;

import handmadeguns.HandmadeGunsCore;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

import java.util.Random;

public final class OverdriveCameraController {
    private static final Random RANDOM = new Random();
    private static float yawOffset, pitchOffset, rollOffset, movementPitch, bobPhase, fovOffset;
    private static float shakePitch, shakeYaw, shakeRoll;
    private static float recoilShake, explosionShake, landingShake, damageShake;
    private static double lastMotionY;
    private static boolean wasOnGround = true;
    private static int lastHurtTime;

    private OverdriveCameraController() {}

    public static void initClient() { reset(); }

    public static void reset() {
        yawOffset = pitchOffset = rollOffset = movementPitch = bobPhase = fovOffset = 0.0F;
        shakePitch = shakeYaw = shakeRoll = 0.0F;
        recoilShake = explosionShake = landingShake = damageShake = 0.0F;
        lastMotionY = 0.0D;
        wasOnGround = true;
        lastHurtTime = 0;
    }

    public static void update(Minecraft mc, float partialTicks) {
        if (!HandmadeGunsCore.cfg_ClientCamera_MasterEnabled || mc == null || mc.theWorld == null || mc.thePlayer == null) {
            reset();
            return;
        }
        EntityLivingBase player = mc.thePlayer;
        float smooth = clamp01(HandmadeGunsCore.cfg_ClientCamera_SmoothingStrength);
        if (HandmadeGunsCore.cfg_ClientCamera_RotationSmoothingEnabled) {
            float yawDelta = MathHelper.wrapAngleTo180_float(player.rotationYaw - player.prevRotationYaw);
            float pitchDelta = player.rotationPitch - player.prevRotationPitch;
            yawOffset = approach(yawOffset, clamp(-yawDelta * 0.35F, -HandmadeGunsCore.cfg_ClientCamera_MaxYawOffset, HandmadeGunsCore.cfg_ClientCamera_MaxYawOffset), smooth);
            pitchOffset = approach(pitchOffset, clamp(-pitchDelta * 0.25F, -HandmadeGunsCore.cfg_ClientCamera_MaxPitchOffset, HandmadeGunsCore.cfg_ClientCamera_MaxPitchOffset), smooth);
        } else {
            yawOffset = pitchOffset = 0.0F;
        }

        double dx = player.posX - player.prevPosX;
        double dz = player.posZ - player.prevPosZ;
        float forward = MathHelper.sin(player.rotationYaw * 0.017453292F) * (float) dx - MathHelper.cos(player.rotationYaw * 0.017453292F) * (float) dz;
        float strafe = MathHelper.cos(player.rotationYaw * 0.017453292F) * (float) dx + MathHelper.sin(player.rotationYaw * 0.017453292F) * (float) dz;
        if (HandmadeGunsCore.cfg_ClientCamera_MotionTiltEnabled) {
            rollOffset = approach(rollOffset, clamp(-strafe * 85.0F, -HandmadeGunsCore.cfg_ClientCamera_MaxRoll, HandmadeGunsCore.cfg_ClientCamera_MaxRoll), clamp01(HandmadeGunsCore.cfg_ClientCamera_TiltReturnSpeed));
            float targetPitch = clamp(-forward * 45.0F + (float) -player.motionY * 2.0F, -HandmadeGunsCore.cfg_ClientCamera_MaxMovementPitch, HandmadeGunsCore.cfg_ClientCamera_MaxMovementPitch);
            movementPitch = approach(movementPitch, targetPitch, clamp01(HandmadeGunsCore.cfg_ClientCamera_TiltReturnSpeed));
        } else {
            rollOffset = approach(rollOffset, 0.0F, 0.3F);
            movementPitch = approach(movementPitch, 0.0F, 0.3F);
        }

        float speed = MathHelper.sqrt_double(dx * dx + dz * dz);
        bobPhase += speed * HandmadeGunsCore.cfg_ClientCamera_BobSpeed * (player.isSprinting() ? HandmadeGunsCore.cfg_ClientCamera_SprintBobMultiplier : 1.0F);
        if (!wasOnGround && player.onGround && lastMotionY < -0.35D) addLandingShake((float) Math.min(1.5D, -lastMotionY));
        wasOnGround = player.onGround;
        if (player.hurtTime > lastHurtTime) addDamageShake(1.0F);
        lastHurtTime = player.hurtTime;
        lastMotionY = player.motionY;
        updateShake();
    }

    public static void applyCameraRotations() {
        if (!HandmadeGunsCore.cfg_ClientCamera_MasterEnabled) return;
        GL11.glRotatef(clamp(pitchOffset + movementPitch + shakePitch, -HandmadeGunsCore.cfg_ClientCamera_MaxPitchOffset - HandmadeGunsCore.cfg_ClientCamera_MaxShakePitch, HandmadeGunsCore.cfg_ClientCamera_MaxPitchOffset + HandmadeGunsCore.cfg_ClientCamera_MaxShakePitch), 1.0F, 0.0F, 0.0F);
        GL11.glRotatef(clamp(yawOffset + shakeYaw, -HandmadeGunsCore.cfg_ClientCamera_MaxYawOffset - HandmadeGunsCore.cfg_ClientCamera_MaxShakeYaw, HandmadeGunsCore.cfg_ClientCamera_MaxYawOffset + HandmadeGunsCore.cfg_ClientCamera_MaxShakeYaw), 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(clamp(rollOffset + shakeRoll, -HandmadeGunsCore.cfg_ClientCamera_MaxRoll - HandmadeGunsCore.cfg_ClientCamera_MaxShakeRoll, HandmadeGunsCore.cfg_ClientCamera_MaxRoll + HandmadeGunsCore.cfg_ClientCamera_MaxShakeRoll), 0.0F, 0.0F, 1.0F);
    }

    public static void applyCustomBob(float partialTicks) {
        if (!HandmadeGunsCore.cfg_ClientCamera_MasterEnabled || !HandmadeGunsCore.cfg_ClientCamera_CustomBobEnabled) return;
        float ads = isAdsDown() ? HandmadeGunsCore.cfg_ClientCamera_ADSBobMultiplier : 1.0F;
        float amount = HandmadeGunsCore.cfg_ClientCamera_BobStrength * ads;
        GL11.glTranslatef(MathHelper.sin(bobPhase) * amount * 0.04F, -Math.abs(MathHelper.cos(bobPhase)) * amount * 0.035F, 0.0F);
        GL11.glRotatef(MathHelper.sin(bobPhase * 0.5F) * amount * 1.5F, 0.0F, 0.0F, 1.0F);
        GL11.glRotatef(MathHelper.cos(bobPhase) * amount * 0.8F, 1.0F, 0.0F, 0.0F);
    }

    public static float modifyFov(float baseFov, float partialTicks) {
        if (!HandmadeGunsCore.cfg_ClientCamera_MasterEnabled || !HandmadeGunsCore.cfg_ClientCamera_FovInertiaEnabled) return baseFov;
        Minecraft mc = Minecraft.getMinecraft();
        float target = 0.0F;
        if (mc != null && mc.thePlayer != null && mc.thePlayer.isSprinting()) target += HandmadeGunsCore.cfg_ClientCamera_SprintFovBoost;
        float speed = isAdsDown() ? HandmadeGunsCore.cfg_ClientCamera_ADSFovSpeed : HandmadeGunsCore.cfg_ClientCamera_FovLerpSpeed;
        fovOffset = approach(fovOffset, target, clamp01(speed));
        return baseFov + fovOffset;
    }

    public static void applyHurtCamera(float partialTicks) { applyCameraRotations(); }
    public static void addRecoilShake(float strength) { recoilShake += Math.max(0.0F, strength) * HandmadeGunsCore.cfg_ClientCamera_RecoilShakeMultiplier; }
    public static void addExplosionShake(float strength) { explosionShake += Math.max(0.0F, strength) * HandmadeGunsCore.cfg_ClientCamera_ExplosionShakeMultiplier; }
    public static void addLandingShake(float strength) { landingShake += Math.max(0.0F, strength) * HandmadeGunsCore.cfg_ClientCamera_LandingShakeMultiplier; }
    public static void addDamageShake(float strength) { damageShake += Math.max(0.0F, strength) * HandmadeGunsCore.cfg_ClientCamera_DamageShakeMultiplier; }

    private static void updateShake() {
        float total = HandmadeGunsCore.cfg_ClientCamera_ShakeEnabled ? recoilShake + explosionShake + landingShake + damageShake : 0.0F;
        shakePitch = clamp((RANDOM.nextFloat() - 0.35F) * total, -HandmadeGunsCore.cfg_ClientCamera_MaxShakePitch, HandmadeGunsCore.cfg_ClientCamera_MaxShakePitch);
        shakeYaw = clamp((RANDOM.nextFloat() - 0.5F) * total, -HandmadeGunsCore.cfg_ClientCamera_MaxShakeYaw, HandmadeGunsCore.cfg_ClientCamera_MaxShakeYaw);
        shakeRoll = clamp((RANDOM.nextFloat() - 0.5F) * total, -HandmadeGunsCore.cfg_ClientCamera_MaxShakeRoll, HandmadeGunsCore.cfg_ClientCamera_MaxShakeRoll);
        float decay = clamp01(HandmadeGunsCore.cfg_ClientCamera_ShakeDecaySpeed);
        recoilShake = approach(recoilShake, 0.0F, decay); explosionShake = approach(explosionShake, 0.0F, decay * 0.7F);
        landingShake = approach(landingShake, 0.0F, decay); damageShake = approach(damageShake, 0.0F, decay);
    }

    private static boolean isAdsDown() { Minecraft mc = Minecraft.getMinecraft(); return mc != null && mc.thePlayer != null && HandmadeGunsCore.Key_ADS(mc.thePlayer); }
    private static float approach(float current, float target, float factor) { return current + (target - current) * clamp01(factor); }
    private static float clamp(float v, float min, float max) { return Math.max(min, Math.min(max, v)); }
    private static float clamp01(float v) { return clamp(v, 0.0F, 1.0F); }
}
