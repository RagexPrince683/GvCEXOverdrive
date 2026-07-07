package handmadeguns.client.camera;

import handmadeguns.HandmadeGunsCore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

public final class OverdriveCameraController {
    private static float yawOffset, pitchOffset, rollOffset, movementPitch, bobPhase, fovOffset;
    private static float shakePitch, shakeYaw, shakeRoll;
    private static float recoilShake, explosionShake, landingShake, damageShake;
    private static float smoothedForward, smoothedStrafe, smoothedVertical;
    private static float shakeTime, explosionPhase;
    private static double lastMotionY;
    private static boolean wasOnGround = true;
    private static int lastHurtTime;

    private OverdriveCameraController() {}

    public static void initClient() { reset(); }

    public static void reset() {
        yawOffset = pitchOffset = rollOffset = movementPitch = bobPhase = fovOffset = 0.0F;
        shakePitch = shakeYaw = shakeRoll = 0.0F;
        recoilShake = explosionShake = landingShake = damageShake = 0.0F;
        smoothedForward = smoothedStrafe = smoothedVertical = 0.0F;
        shakeTime = explosionPhase = 0.0F;
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
            yawOffset = approachLimited(yawOffset, clamp(-yawDelta * 0.35F, -HandmadeGunsCore.cfg_ClientCamera_MaxYawOffset, HandmadeGunsCore.cfg_ClientCamera_MaxYawOffset), smooth, HandmadeGunsCore.cfg_ClientCamera_MaxOffsetChange);
            pitchOffset = approachLimited(pitchOffset, clamp(-pitchDelta * 0.25F, -HandmadeGunsCore.cfg_ClientCamera_MaxPitchOffset, HandmadeGunsCore.cfg_ClientCamera_MaxPitchOffset), smooth, HandmadeGunsCore.cfg_ClientCamera_MaxOffsetChange);
        } else {
            yawOffset = approachLimited(yawOffset, 0.0F, 0.3F, HandmadeGunsCore.cfg_ClientCamera_MaxOffsetChange);
            pitchOffset = approachLimited(pitchOffset, 0.0F, 0.3F, HandmadeGunsCore.cfg_ClientCamera_MaxOffsetChange);
        }

        double interpX = player.prevPosX + (player.posX - player.prevPosX) * partialTicks;
        double interpY = player.prevPosY + (player.posY - player.prevPosY) * partialTicks;
        double interpZ = player.prevPosZ + (player.posZ - player.prevPosZ) * partialTicks;
        double prevInterpX = player.lastTickPosX + (player.prevPosX - player.lastTickPosX) * partialTicks;
        double prevInterpY = player.lastTickPosY + (player.prevPosY - player.lastTickPosY) * partialTicks;
        double prevInterpZ = player.lastTickPosZ + (player.prevPosZ - player.lastTickPosZ) * partialTicks;
        double dx = interpX - prevInterpX;
        double dy = interpY - prevInterpY;
        double dz = interpZ - prevInterpZ;

        float inputForward = 0.0F;
        float inputStrafe = 0.0F;
        if (player instanceof EntityPlayerSP && ((EntityPlayerSP) player).movementInput != null) {
            inputForward = ((EntityPlayerSP) player).movementInput.moveForward;
            inputStrafe = ((EntityPlayerSP) player).movementInput.moveStrafe;
        } else {
            inputForward = MathHelper.sin(player.rotationYaw * 0.017453292F) * (float) dx - MathHelper.cos(player.rotationYaw * 0.017453292F) * (float) dz;
            inputStrafe = MathHelper.cos(player.rotationYaw * 0.017453292F) * (float) dx + MathHelper.sin(player.rotationYaw * 0.017453292F) * (float) dz;
        }
        inputForward = applyDeadzone(inputForward, HandmadeGunsCore.cfg_ClientCamera_MovementDeadzone);
        inputStrafe = applyDeadzone(inputStrafe, HandmadeGunsCore.cfg_ClientCamera_MovementDeadzone);
        float vertical = applyDeadzone((float) dy, HandmadeGunsCore.cfg_ClientCamera_MovementDeadzone * 0.25F);
        smoothedForward = approachLimited(smoothedForward, inputForward, HandmadeGunsCore.cfg_ClientCamera_MotionInputSmoothing, 0.12F);
        smoothedStrafe = approachLimited(smoothedStrafe, inputStrafe, HandmadeGunsCore.cfg_ClientCamera_MotionInputSmoothing, 0.12F);
        smoothedVertical = approachLimited(smoothedVertical, vertical, HandmadeGunsCore.cfg_ClientCamera_MotionInputSmoothing, 0.04F);

        if (HandmadeGunsCore.cfg_ClientCamera_MotionTiltEnabled) {
            rollOffset = approachLimited(rollOffset, clamp(-smoothedStrafe * HandmadeGunsCore.cfg_ClientCamera_MaxRoll, -HandmadeGunsCore.cfg_ClientCamera_MaxRoll, HandmadeGunsCore.cfg_ClientCamera_MaxRoll), clamp01(HandmadeGunsCore.cfg_ClientCamera_TiltReturnSpeed), HandmadeGunsCore.cfg_ClientCamera_MaxOffsetChange);
            float targetPitch = clamp(-smoothedForward * HandmadeGunsCore.cfg_ClientCamera_MaxMovementPitch - smoothedVertical * 24.0F, -HandmadeGunsCore.cfg_ClientCamera_MaxMovementPitch, HandmadeGunsCore.cfg_ClientCamera_MaxMovementPitch);
            movementPitch = approachLimited(movementPitch, targetPitch, clamp01(HandmadeGunsCore.cfg_ClientCamera_TiltReturnSpeed), HandmadeGunsCore.cfg_ClientCamera_MaxOffsetChange);
        } else {
            rollOffset = approachLimited(rollOffset, 0.0F, 0.3F, HandmadeGunsCore.cfg_ClientCamera_MaxOffsetChange);
            movementPitch = approachLimited(movementPitch, 0.0F, 0.3F, HandmadeGunsCore.cfg_ClientCamera_MaxOffsetChange);
        }

        float speed = MathHelper.sqrt_double(dx * dx + dz * dz);
        bobPhase += speed * HandmadeGunsCore.cfg_ClientCamera_BobSpeed * (player.isSprinting() ? HandmadeGunsCore.cfg_ClientCamera_SprintBobMultiplier : 1.0F);
        if (!wasOnGround && player.onGround && lastMotionY < -0.35D) addLandingShake((float) Math.min(1.5D, -lastMotionY));
        wasOnGround = player.onGround;
        if (player.hurtTime > lastHurtTime) addDamageShake(1.0F);
        lastHurtTime = player.hurtTime;
        lastMotionY = player.motionY;
        updateShake(partialTicks);
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

    private static void updateShake(float partialTicks) {
        float frequency = Math.max(0.1F, HandmadeGunsCore.cfg_ClientCamera_ShakeFrequency);
        shakeTime += 0.05F * frequency;
        explosionPhase += 0.035F * frequency;

        float trauma = HandmadeGunsCore.cfg_ClientCamera_ShakeEnabled ? recoilShake + landingShake + damageShake : 0.0F;
        float impulse = HandmadeGunsCore.cfg_ClientCamera_ShakeEnabled ? explosionShake : 0.0F;
        float targetPitch = clamp(MathHelper.sin(shakeTime * 2.10F) * trauma * 0.70F - MathHelper.sin(explosionPhase) * impulse, -HandmadeGunsCore.cfg_ClientCamera_MaxShakePitch, HandmadeGunsCore.cfg_ClientCamera_MaxShakePitch);
        float targetYaw = clamp(MathHelper.sin(shakeTime * 1.37F + 1.7F) * trauma * 0.45F + MathHelper.sin(explosionPhase * 0.83F + 0.6F) * impulse * 0.55F, -HandmadeGunsCore.cfg_ClientCamera_MaxShakeYaw, HandmadeGunsCore.cfg_ClientCamera_MaxShakeYaw);
        float targetRoll = clamp(MathHelper.sin(shakeTime * 1.73F + 2.4F) * trauma * 0.55F + MathHelper.sin(explosionPhase * 0.71F + 1.2F) * impulse * 0.70F, -HandmadeGunsCore.cfg_ClientCamera_MaxShakeRoll, HandmadeGunsCore.cfg_ClientCamera_MaxShakeRoll);
        shakePitch = approachLimited(shakePitch, targetPitch, 0.35F, HandmadeGunsCore.cfg_ClientCamera_MaxOffsetChange * 1.5F);
        shakeYaw = approachLimited(shakeYaw, targetYaw, 0.35F, HandmadeGunsCore.cfg_ClientCamera_MaxOffsetChange * 1.5F);
        shakeRoll = approachLimited(shakeRoll, targetRoll, 0.35F, HandmadeGunsCore.cfg_ClientCamera_MaxOffsetChange * 1.5F);

        float decay = clamp01(HandmadeGunsCore.cfg_ClientCamera_ShakeDecaySpeed);
        recoilShake = approach(recoilShake, 0.0F, decay);
        explosionShake = approach(explosionShake, 0.0F, decay * 0.45F);
        landingShake = approach(landingShake, 0.0F, decay * 0.75F);
        damageShake = approach(damageShake, 0.0F, decay);
    }

    private static boolean isAdsDown() { Minecraft mc = Minecraft.getMinecraft(); return mc != null && mc.thePlayer != null && HandmadeGunsCore.Key_ADS(mc.thePlayer); }
    private static float applyDeadzone(float value, float deadzone) { return Math.abs(value) < deadzone ? 0.0F : value; }
    private static float approach(float current, float target, float factor) { return current + (target - current) * clamp01(factor); }
    private static float approachLimited(float current, float target, float factor, float maxStep) { return current + clamp((target - current) * clamp01(factor), -Math.max(0.001F, maxStep), Math.max(0.001F, maxStep)); }
    private static float clamp(float v, float min, float max) { return Math.max(min, Math.min(max, v)); }
    private static float clamp01(float v) { return clamp(v, 0.0F, 1.0F); }
}
