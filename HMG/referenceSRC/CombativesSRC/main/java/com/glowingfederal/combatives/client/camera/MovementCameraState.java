package com.glowingfederal.combatives.client.camera;

import com.glowingfederal.combatives.Combatives;
import com.glowingfederal.combatives.config.CombativesConfig;
import com.glowingfederal.combatives.entity.Pose;
import com.glowingfederal.combatives.entity.player.ICombativesPlayerPose;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.MovementInput;

public final class MovementCameraState {
    private static final float INPUT_DEADZONE = 0.04F;
    private static final float SPEED_DEADZONE = 0.003F;
    private static final float INPUT_SMOOTHING = 0.22F;
    private static final float SPEED_SMOOTHING = 0.18F;

    private float forward;
    private float strafe;
    private float speed;
    private float walkPhase;
    private float vanillaCameraYaw;
    private float vanillaCameraPitch;
    private boolean crawling;
    private boolean swimming;
    private boolean sneaking;
    private boolean sprinting;
    private boolean grounded;
    private boolean landed;
    private float landingStrength;
    private boolean wasGrounded = true;
    private double previousMotionY;
    private float previousFallDistance;
    private int lastLandingSampleTick = Integer.MIN_VALUE;

    public void update(EntityPlayerSP player, float partialTicks) {
        MovementInput input = player.movementInput;
        float targetForward = input == null ? 0.0F : applyDeadzone(input.moveForward, INPUT_DEADZONE);
        float targetStrafe = input == null ? 0.0F : applyDeadzone(input.moveStrafe, INPUT_DEADZONE);
        float horizontalSpeed = (float) Math.sqrt(player.motionX * player.motionX + player.motionZ * player.motionZ);
        if (horizontalSpeed < SPEED_DEADZONE) horizontalSpeed = 0.0F;

        this.forward += (targetForward - this.forward) * INPUT_SMOOTHING;
        this.strafe += (targetStrafe - this.strafe) * INPUT_SMOOTHING;
        this.speed += (horizontalSpeed - this.speed) * SPEED_SMOOTHING;

        float walkedDelta = player.distanceWalkedModified - player.prevDistanceWalkedModified;
        this.walkPhase = -(player.distanceWalkedModified + walkedDelta * partialTicks);
        this.vanillaCameraYaw = player.prevCameraYaw + (player.cameraYaw - player.prevCameraYaw) * partialTicks;
        this.vanillaCameraPitch = player.prevCameraPitch + (player.cameraPitch - player.prevCameraPitch) * partialTicks;

        this.grounded = player.onGround;
        this.sneaking = player.isSneaking();
        this.sprinting = player.isSprinting();
        this.swimming = false;
        this.crawling = false;
        if (player instanceof ICombativesPlayerPose) {
            ICombativesPlayerPose pose = (ICombativesPlayerPose) player;
            this.swimming = pose.isSwimming() || pose.isActuallySwimming();
            this.crawling = !this.swimming && pose.getPose() == Pose.SWIMMING;
        }
        if (player.ticksExisted != this.lastLandingSampleTick) {
            boolean wasAirborne = !this.wasGrounded;
            boolean realLanding = wasAirborne && player.onGround;
            float severity = clamp((this.previousFallDistance - 0.75F) / 5.0F, 0.0F, 1.0F);
            this.landed = CombativesConfig.enableCombativesCamera && CombativesConfig.enableLandingCameraFeedback && realLanding && severity > 0.0F;
            this.landingStrength = this.landed ? severity : 0.0F;

            if (Combatives.logger != null && CombativesConfig.verboseCameraDebug && !player.onGround) {
                Combatives.logger.info("Combatives landing airborne tracking: tick={}, motionY={}, fallDistance={}, previousMotionY={}, previousFallDistance={}", player.ticksExisted, player.motionY, player.fallDistance, this.previousMotionY, this.previousFallDistance);
            }
            if (Combatives.logger != null && CombativesConfig.verboseCameraDebug && realLanding && !this.landed) {
                Combatives.logger.info("Combatives landing impulse rejected: reason=below_threshold_or_disabled, previousFallDistance={}, severity={}, enableCombativesCamera={}, enableLandingCameraFeedback={}", this.previousFallDistance, severity, CombativesConfig.enableCombativesCamera, CombativesConfig.enableLandingCameraFeedback);
            }

            this.wasGrounded = player.onGround;
            this.previousMotionY = player.motionY;
            this.previousFallDistance = player.fallDistance;
            this.lastLandingSampleTick = player.ticksExisted;
        } else {
            this.landed = false;
            this.landingStrength = 0.0F;
        }
    }

    private static float applyDeadzone(float value, float deadzone) {
        return Math.abs(value) < deadzone ? 0.0F : value;
    }

    private static float clamp(float value, float min, float max) {
        return value < min ? min : value > max ? max : value;
    }

    public float getForward() { return forward; }
    public float getStrafe() { return strafe; }
    public float getSpeed() { return speed; }
    public float getWalkPhase() { return walkPhase; }
    public float getCameraYaw() { return vanillaCameraYaw; }
    public float getCameraPitch() { return vanillaCameraPitch; }
    public boolean isCrawling() { return crawling; }
    public boolean isSwimming() { return swimming; }
    public boolean isSneaking() { return sneaking; }
    public boolean isSprinting() { return sprinting; }
    public boolean isGrounded() { return grounded; }
    public boolean hasLanded() { return landed; }
    public float getLandingStrength() { return landingStrength; }
}
