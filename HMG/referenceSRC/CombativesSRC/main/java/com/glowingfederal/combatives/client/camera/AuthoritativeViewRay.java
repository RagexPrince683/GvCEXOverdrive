package com.glowingfederal.combatives.client.camera;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import com.glowingfederal.combatives.entity.player.ICombativesPlayerPose;
import com.glowingfederal.combatives.interaction.InteractionRay;
import com.glowingfederal.combatives.compat.mcheli.MCHeliCameraCompat;

/**
 * The non-presentational, first-person ray represented by the center pixel.
 *
 * <p>The camera height is recorded where {@code orientCamera} selects its base
 * origin, before Combatives' shake/bob/lean transforms.  It is stored relative
 * to the entity's interpolated bounding-box floor.  That anchor deliberately
 * survives renderers which temporarily translate the legacy position samples
 * around {@code getMouseOver} without moving the physical bounding box.</p>
 */
public final class AuthoritativeViewRay {
    private static Entity cameraEntity;
    private static double cameraAboveMinY;
    private static boolean hasCameraSample;
    private static EntityLivingBase targetEntity;
    private static Vec3 targetOrigin;
    private static Vec3 targetDirection;

    private AuthoritativeViewRay() { }

    public static void captureCameraBase(Entity entity, float partialTicks, double cameraY) {
        double interpolatedY = interpolate(entity.prevPosY, entity.posY, partialTicks);
        double interpolatedMinY = interpolatedY + entity.boundingBox.minY - entity.posY;
        cameraEntity = entity;
        cameraAboveMinY = cameraY - interpolatedMinY;
        hasCameraSample = true;
    }

    public static void beginTargeting(EntityLivingBase entity, float partialTicks) {
        clearTarget();
        Minecraft mc = Minecraft.getMinecraft();
        if (entity != mc.renderViewEntity || mc.gameSettings.thirdPersonView != 0 || entity.isPlayerSleeping()) {
            return;
        }

        // A vehicle may pair its orientCamera offset with its own targeting
        // mutations. Do not replace either half of that mounted-camera contract.
        if (entity.isRiding() || MCHeliCameraCompat.ownsCamera(entity)) {
            return;
        }

        if (entity instanceof EntityPlayer && entity instanceof ICombativesPlayerPose) {
            InteractionRay ray = InteractionRay.interpolated((EntityPlayer) entity, partialTicks);
            targetEntity = entity;
            targetOrigin = ray.origin;
            targetDirection = ray.direction;
            return;
        }
        double x = interpolate(entity.prevPosX, entity.posX, partialTicks);
        double interpolatedY = interpolate(entity.prevPosY, entity.posY, partialTicks);
        double interpolatedMinY = interpolatedY + entity.boundingBox.minY - entity.posY;
        double z = interpolate(entity.prevPosZ, entity.posZ, partialTicks);
        double aboveMinY;
        if (hasCameraSample && entity == cameraEntity) {
            aboveMinY = cameraAboveMinY;
        } else {
            return;
        }
        targetEntity = entity;
        targetOrigin = Vec3.createVectorHelper(x, interpolatedMinY + aboveMinY, z);
        // Deliberately excludes CameraController's presentation-only rotations.
        targetDirection = entity.getLook(partialTicks);
    }

    public static Vec3 origin(EntityLivingBase entity, Vec3 vanilla) {
        return entity == targetEntity && targetOrigin != null ? targetOrigin : vanilla;
    }

    public static Vec3 direction(EntityLivingBase entity, Vec3 vanilla) {
        return entity == targetEntity && targetDirection != null ? targetDirection : vanilla;
    }

    public static Vec3 currentOrigin() {
        return targetOrigin;
    }

    public static Vec3 currentDirection() {
        return targetDirection;
    }

    public static void endTargeting() {
        clearTarget();
    }

    private static void clearTarget() {
        targetEntity = null;
        targetOrigin = null;
        targetDirection = null;
    }

    private static double interpolate(double previous, double current, float partialTicks) {
        return previous + (current - previous) * (double) partialTicks;
    }
}
