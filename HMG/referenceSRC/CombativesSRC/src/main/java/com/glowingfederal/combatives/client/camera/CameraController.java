package com.glowingfederal.combatives.client.camera;

import com.glowingfederal.combatives.Combatives;
import com.glowingfederal.combatives.config.CombativesConfig;
import com.glowingfederal.combatives.client.camera.internal.CameraEffectManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import org.lwjgl.opengl.GL11;

public final class CameraController {
    public static final CameraController INSTANCE = new CameraController();

    private final MovementCameraState movement = new MovementCameraState();
    private final LeanController lean = new LeanController();
    private final BobController bob = new BobController();
    private final FOVController fov = new FOVController();
    private final ShakeController shake = new ShakeController();

    private static final float MAX_CAMERA_PITCH_DEGREES = 6.0F;
    private static final float MAX_CAMERA_ROLL_DEGREES = 5.0F;
    private static final float MAX_AMBIENT_X_OFFSET = 0.04F;
    private static final float MAX_AMBIENT_Y_OFFSET = 0.06F;
    private static final float MAX_IMPACT_PITCH_DEGREES = 8.0F;
    private static final float MAX_IMPACT_ROLL_DEGREES = 4.5F;
    private static final float MAX_IMPACT_X_OFFSET = 0.16F;
    private static final float MAX_IMPACT_Y_OFFSET = 0.2F;
    private static final float MAX_IMPACT_Z_OFFSET = 0.16F;

    private float leanRoll, leanPitch, bobVertical, bobSway, bobPitch, bobRoll, shakeVertical, shakeForward, shakeLateral, shakePitch, shakeRoll, fovModifier;

    private CameraController() {}

    public void update(Minecraft mc, EntityPlayerSP player, float partialTicks) {
        if (!CombativesConfig.enableCombativesCamera || mc == null || player == null) { reset(); return; }
        movement.update(player, partialTicks);
        if (CombativesConfig.enableCameraShake && CombativesConfig.enableLandingCameraFeedback && movement.hasLanded()) shake.addLandingImpulse(movement.getLandingStrength(), movement.getStrafe(), movement.getSpeed());
        if (CombativesConfig.enableMovementLean) lean.update(movement); else lean.reset();
        if (CombativesConfig.enableProceduralBob) bob.update(movement); else bob.reset();
        if (CombativesConfig.enableMovementFov) fov.update(movement); else fov.reset();
        if (CombativesConfig.enableCameraShake) shake.update(movement, partialTicks); else shake.reset();
        CameraEffectManager.update(player);
        leanRoll = lean.getRoll(); leanPitch = lean.getPitch(); bobVertical = bob.getVertical(); bobSway = bob.getSway(); bobPitch = bob.getPitch(); bobRoll = bob.getRoll();
        shakeVertical = shake.getVertical(); shakeForward = shake.getForward(); shakeLateral = shake.getLateral(); shakePitch = shake.getPitch(); shakeRoll = shake.getRoll(); fovModifier = fov.getModifier();
    }

    public void applyTransforms(float partialTicks) {
        if (!CombativesConfig.enableCombativesCamera) return;

        float bobScale = 1.0F - clamp(shake.getBobSuppression(), 0.0F, 0.8F);
        float ambientX = clamp(bobSway * bobScale, -MAX_AMBIENT_X_OFFSET, MAX_AMBIENT_X_OFFSET);
        float ambientY = clamp(bobVertical * bobScale, -MAX_AMBIENT_Y_OFFSET, MAX_AMBIENT_Y_OFFSET);
        float impactX = clamp(shakeLateral + CameraEffectManager.getX(), -MAX_IMPACT_X_OFFSET, MAX_IMPACT_X_OFFSET);
        float impactY = clamp(shakeVertical + CameraEffectManager.getY(), -MAX_IMPACT_Y_OFFSET, MAX_IMPACT_Y_OFFSET);
        float impactZ = clamp(shakeForward + CameraEffectManager.getZ(), -MAX_IMPACT_Z_OFFSET, MAX_IMPACT_Z_OFFSET);
        float xOffset = ambientX + impactX;
        float yOffset = ambientY + impactY;
        float zOffset = impactZ;
        GL11.glTranslatef(xOffset, yOffset, zOffset);

        float pitch = 0.0F;
        float yaw = 0.0F;
        float roll = 0.0F;
        if (CombativesConfig.enableCameraRotations) {
            float ambientPitch = clamp(bobPitch * bobScale + leanPitch, -MAX_CAMERA_PITCH_DEGREES, MAX_CAMERA_PITCH_DEGREES);
            float ambientRoll = clamp(bobRoll * bobScale + leanRoll, -MAX_CAMERA_ROLL_DEGREES, MAX_CAMERA_ROLL_DEGREES);
            yaw = clamp(CameraEffectManager.getYaw(), -CombativesConfig.maxCameraYawDegrees, CombativesConfig.maxCameraYawDegrees);
            pitch = ambientPitch + clamp(shakePitch + CameraEffectManager.getPitch(), -MAX_IMPACT_PITCH_DEGREES, MAX_IMPACT_PITCH_DEGREES);
            roll = ambientRoll + clamp(shakeRoll + CameraEffectManager.getRoll(), -MAX_IMPACT_ROLL_DEGREES, MAX_IMPACT_ROLL_DEGREES);
            GL11.glRotatef(pitch, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(yaw, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(roll, 0.0F, 0.0F, 1.0F);
        }
        if (Combatives.logger != null && CombativesConfig.verboseCameraDebug) Combatives.logger.info("Combatives camera final render transform pitch={} yaw={} roll={} translation=({},{},{}) fov={}", pitch, yaw, roll, xOffset, yOffset, zOffset, getFovModifier());
    }

    public void applyHandTransforms(float partialTicks) {
        if (!CombativesConfig.enableCombativesCamera || !CombativesConfig.enableProceduralBob) return;
        applyVanillaStyleBob();
    }

    private void applyVanillaStyleBob() {
        float xOffset = clamp(bobSway, -MAX_AMBIENT_X_OFFSET, MAX_AMBIENT_X_OFFSET);
        float yOffset = clamp(bobVertical, -MAX_AMBIENT_Y_OFFSET, MAX_AMBIENT_Y_OFFSET);
        float zOffset = clamp(0.0F, -MAX_IMPACT_Z_OFFSET, MAX_IMPACT_Z_OFFSET);
        GL11.glTranslatef(xOffset, yOffset, zOffset);
        if (!CombativesConfig.enableCameraRotations) return;
        GL11.glRotatef(clamp(bobPitch, -MAX_CAMERA_PITCH_DEGREES, MAX_CAMERA_PITCH_DEGREES), 1.0F, 0.0F, 0.0F);
        GL11.glRotatef(clamp(bobRoll, -MAX_CAMERA_ROLL_DEGREES, MAX_CAMERA_ROLL_DEGREES), 0.0F, 0.0F, 1.0F);
    }



    private static float clamp(float value, float min, float max) {
        return value < min ? min : value > max ? max : value;
    }

    public void reset() { lean.reset(); bob.reset(); fov.reset(); shake.reset(); CameraEffectManager.reset(); leanRoll = leanPitch = bobVertical = bobSway = bobPitch = bobRoll = shakeVertical = shakeForward = shakeLateral = shakePitch = shakeRoll = fovModifier = 0.0F; }

    public void addExplosionFeedback(EntityPlayerSP player, double x, double y, double z, float strength) {
        if (!CombativesConfig.enableCombativesCamera || !CombativesConfig.enableCameraShake || !CombativesConfig.enableExplosionCameraFeedback || player == null) {
            if (Combatives.logger != null && CombativesConfig.debugCamera) Combatives.logger.info("Combatives explosion feedback rejected: enableCombativesCamera={}, enableCameraShake={}, enableExplosionCameraFeedback={}, hasPlayer={}", CombativesConfig.enableCombativesCamera, CombativesConfig.enableCameraShake, CombativesConfig.enableExplosionCameraFeedback, player != null);
            return;
        }
        double dx = player.posX - x;
        double dy = player.posY + player.getEyeHeight() - y;
        double dz = player.posZ - z;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        float radius = Math.max(8.0F, strength * 4.0F);
        float distanceFalloff = clamp(1.0F - (float) (distance / radius), 0.0F, 1.0F);
        float strengthFactor = clamp(strength / 4.0F, 0.0F, 2.0F);
        float response = (float) Math.pow(clamp(strengthFactor * distanceFalloff, 0.0F, 1.0F), 0.45F);
        if (response <= 0.0F) {
            if (Combatives.logger != null && CombativesConfig.verboseCameraDebug) Combatives.logger.info("Combatives explosion impulse rejected: reason=outside_radius_or_zero_response, distance={}, radius={}, strength={}", distance, radius, strength);
            return;
        }
        float yawRad = (float) Math.toRadians(player.rotationYaw);
        double forwardX = -Math.sin(yawRad);
        double forwardZ = Math.cos(yawRad);
        double rightX = forwardZ;
        double rightZ = -forwardX;
        double invDistance = 1.0D / Math.max(0.001D, distance);
        double dirX = dx * invDistance;
        double dirY = dy * invDistance;
        double dirZ = dz * invDistance;
        float localForward = clamp((float) (dirX * forwardX + dirZ * forwardZ), -1.0F, 1.0F);
        float localRight = clamp((float) (dirX * rightX + dirZ * rightZ), -1.0F, 1.0F);
        float localVertical = clamp((float) dirY, -1.0F, 1.0F);
        shake.addExplosionImpulse(response, localForward, localRight, localVertical);
    }
    public float getFovModifier() { return fovModifier + CameraEffectManager.getFov() * 0.01F; }
}
