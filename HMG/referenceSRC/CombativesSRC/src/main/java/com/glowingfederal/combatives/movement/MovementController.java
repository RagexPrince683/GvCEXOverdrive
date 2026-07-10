package com.glowingfederal.combatives.movement;

import com.glowingfederal.combatives.entity.Pose;
import com.glowingfederal.combatives.entity.player.ICombativesPlayerPose;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;

public final class MovementController {
    private MovementController() {
    }

    public static boolean shouldBypass(EntityPlayer player) {
        return shouldBypassUnsafe(player) || player.isInWater() || isCustomCrawlOrSwim(player);
    }

    public static boolean shouldBypassUnsafe(EntityPlayer player) {
        return player == null || player.noClip || player.isRiding() || player.isPlayerSleeping() || player.getHealth() <= 0.0F
            || player.isOnLadder() || player.capabilities.isFlying;
    }

    public static boolean isCustomCrawlOrSwim(EntityPlayer player) {
        if (!(player instanceof ICombativesPlayerPose)) {
            return false;
        }
        ICombativesPlayerPose pose = (ICombativesPlayerPose) player;
        return pose.isSwimming() || pose.getPose() == Pose.SWIMMING;
    }

    public static MovementResult shape(EntityPlayer player, float strafe, float forward, float yaw, double currentX, double currentZ, double vanillaTargetX, double vanillaTargetZ) {
        MovementProfile profile = selectProfile(player);
        boolean hasInput = strafe * strafe + forward * forward > 1.0E-4F;
        double wishX = 0.0D;
        double wishZ = 0.0D;
        if (hasInput) {
            double inputLength = MathHelper.sqrt_float(strafe * strafe + forward * forward);
            double normalizedStrafe = strafe / inputLength;
            double normalizedForward = forward / inputLength;
            double sin = MathHelper.sin(yaw * (float) Math.PI / 180.0F);
            double cos = MathHelper.cos(yaw * (float) Math.PI / 180.0F);
            wishX = normalizedStrafe * cos - normalizedForward * sin;
            wishZ = normalizedForward * cos + normalizedStrafe * sin;
        }

        double targetX = vanillaTargetX;
        double targetZ = vanillaTargetZ;
        if (profile.maxSpeedMultiplier != 1.0D) {
            targetX *= profile.maxSpeedMultiplier;
            targetZ *= profile.maxSpeedMultiplier;
        }

        double deltaX = targetX - currentX;
        double deltaZ = targetZ - currentZ;
        double accel = hasInput ? profile.acceleration : profile.deceleration;
        if (!player.onGround) {
            accel *= profile.airControlMultiplier;
        }
        double turnAmount = getTurnAmount(currentX, currentZ, targetX, targetZ);
        if (turnAmount > 0.35D) {
            accel = Math.min(accel, profile.turnAcceleration);
        }

        double deltaLength = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        if (deltaLength > accel && deltaLength > 1.0E-8D) {
            double scale = accel / deltaLength;
            deltaX *= scale;
            deltaZ *= scale;
        }

        double resultX = currentX + deltaX;
        double resultZ = currentZ + deltaZ;
        if (!hasInput) {
            resultX *= profile.frictionDrag;
            resultZ *= profile.frictionDrag;
        }

        MovementSnapshot previous = player instanceof ICombativesMovementState ? ((ICombativesMovementState) player).getCombativesMovementSnapshot() : MovementSnapshot.EMPTY;
        double speed = Math.sqrt(resultX * resultX + resultZ * resultZ);
        double targetSpeed = Math.max(1.0E-8D, Math.sqrt(targetX * targetX + targetZ * targetZ));
        double previousWishDot = previous.wishX * wishX + previous.wishZ * wishZ;
        double previousWishLen = Math.sqrt(previous.wishX * previous.wishX + previous.wishZ * previous.wishZ);
        double wishLen = Math.sqrt(wishX * wishX + wishZ * wishZ);
        double turnRate = previousWishLen > 1.0E-8D && wishLen > 1.0E-8D ? 1.0D - clamp(previousWishDot / (previousWishLen * wishLen), -1.0D, 1.0D) : 0.0D;
        boolean grounded = player.onGround;
        boolean swimming = player instanceof ICombativesPlayerPose && ((ICombativesPlayerPose) player).isSwimming();
        boolean crawling = player instanceof ICombativesPlayerPose && ((ICombativesPlayerPose) player).getPose() == Pose.SWIMMING && !swimming;
        boolean underwater = player.isInWater();
        MovementSnapshot snapshot = new MovementSnapshot(resultX, resultZ, currentX, currentZ, resultX - currentX, resultZ - currentZ, speed,
            clamp(speed / targetSpeed, 0.0D, 1.0D), wishX, wishZ, grounded, player.isSprinting(), player.isSneaking(), crawling, swimming,
            underwater, !grounded, !previous.grounded && grounded && player.fallDistance > 0.0F, turnAmount, turnRate);
        return new MovementResult(resultX, resultZ, snapshot);
    }

    public static MovementProfile selectProfile(EntityPlayer player) {
        if (!player.onGround) return MovementProfile.AIRBORNE;
        if (player instanceof ICombativesPlayerPose) {
            ICombativesPlayerPose pose = (ICombativesPlayerPose) player;
            if (pose.isSwimming()) return MovementProfile.SWIMMING;
            if (pose.getPose() == Pose.SWIMMING) return MovementProfile.CRAWLING;
        }
        if (player.isSneaking()) return MovementProfile.SNEAKING;
        if (player.isSprinting()) return MovementProfile.SPRINTING;
        return MovementProfile.STANDING;
    }

    private static double getTurnAmount(double currentX, double currentZ, double targetX, double targetZ) {
        double currentLength = Math.sqrt(currentX * currentX + currentZ * currentZ);
        double targetLength = Math.sqrt(targetX * targetX + targetZ * targetZ);
        if (currentLength < 1.0E-8D || targetLength < 1.0E-8D) return 0.0D;
        return 1.0D - clamp((currentX * targetX + currentZ * targetZ) / (currentLength * targetLength), -1.0D, 1.0D);
    }

    private static double clamp(double value, double min, double max) {
        return value < min ? min : value > max ? max : value;
    }

    public static final class MovementResult {
        public final double motionX;
        public final double motionZ;
        public final MovementSnapshot snapshot;

        private MovementResult(double motionX, double motionZ, MovementSnapshot snapshot) {
            this.motionX = motionX;
            this.motionZ = motionZ;
            this.snapshot = snapshot;
        }
    }
}
