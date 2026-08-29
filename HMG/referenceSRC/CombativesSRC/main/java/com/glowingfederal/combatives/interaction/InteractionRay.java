package com.glowingfederal.combatives.interaction;

import com.glowingfederal.combatives.entity.player.EffectivePlayerGeometry;
import com.glowingfederal.combatives.entity.player.ICombativesPlayerPose;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

/**
 * The common-side definition of gameplay aim.  The anchor is always measured
 * from the accepted physical AABB floor; legacy entity eye/offset fields and
 * renderer translations are intentionally not inputs.
 */
public final class InteractionRay {
    public final Vec3 origin;
    public final Vec3 direction;
    public final int geometryRevision;

    private InteractionRay(Vec3 origin, Vec3 direction, int geometryRevision) {
        this.origin = origin;
        this.direction = direction;
        this.geometryRevision = geometryRevision;
    }

    /** Tick-authoritative ray used by server interaction validation. */
    public static InteractionRay authoritative(EntityPlayer player) {
        return create(player, player.posX, player.boundingBox.minY, player.posZ,
                player.rotationYaw, player.rotationPitch);
    }

    /** Same geometry semantics with position/orientation interpolation for rendering. */
    public static InteractionRay interpolated(EntityPlayer player, float partialTicks) {
        double x = interpolate(player.prevPosX, player.posX, partialTicks);
        double z = interpolate(player.prevPosZ, player.posZ, partialTicks);
        double positionY = interpolate(player.prevPosY, player.posY, partialTicks);
        double floorY = positionY + player.boundingBox.minY - player.posY;
        float yaw = interpolateRotation(player.prevRotationYaw, player.rotationYaw, partialTicks);
        float pitch = player.prevRotationPitch
                + (player.rotationPitch - player.prevRotationPitch) * partialTicks;
        return create(player, x, floorY, z, yaw, pitch);
    }

    private static InteractionRay create(EntityPlayer player, double x, double floorY,
            double z, float yaw, float pitch) {
        ICombativesPlayerPose state = (ICombativesPlayerPose) player;
        EffectivePlayerGeometry geometry = state.getEffectiveGeometry();
        float yawRadians = -yaw * 0.017453292F - (float) Math.PI;
        float pitchRadians = -pitch * 0.017453292F;
        float horizontal = -MathHelper.cos(pitchRadians);
        Vec3 direction = Vec3.createVectorHelper(MathHelper.sin(yawRadians) * horizontal,
                MathHelper.sin(pitchRadians), MathHelper.cos(yawRadians) * horizontal);
        return new InteractionRay(Vec3.createVectorHelper(x, floorY + geometry.eyeAboveMinY, z),
                direction, state.getGeometryRevision());
    }

    public Vec3 end(double reach) {
        return this.origin.addVector(this.direction.xCoord * reach,
                this.direction.yCoord * reach, this.direction.zCoord * reach);
    }

    public MovingObjectPosition traceBlocks(EntityLivingBase player, double reach) {
        return player.worldObj.rayTraceBlocks(this.origin, end(reach), false);
    }

    private static double interpolate(double previous, double current, float partialTicks) {
        return previous + (current - previous) * partialTicks;
    }

    private static float interpolateRotation(float previous, float current, float partialTicks) {
        return previous + MathHelper.wrapAngleTo180_float(current - previous) * partialTicks;
    }
}
