package handmadeguns.client.camera;

import handmadeguns.camera.CameraConfig;
import net.minecraft.client.entity.EntityClientPlayerMP;

public class RotationSmoother {
    private boolean initialized;
    private float smoothedYaw;
    private float smoothedPitch;
    private float prevYawOffset;
    private float currentYawOffset;
    private float prevPitchOffset;
    private float currentPitchOffset;

    public void reset() {
        initialized = false;
        prevYawOffset = currentYawOffset = 0.0F;
        prevPitchOffset = currentPitchOffset = 0.0F;
    }

    public void update(EntityClientPlayerMP player, float partialTicks) {
        prevYawOffset = currentYawOffset;
        prevPitchOffset = currentPitchOffset;

        if (!CameraConfig.masterEnabled || !CameraConfig.rotationSmoothingEnabled || player == null) {
            currentYawOffset = CameraMath.approach(currentYawOffset, 0.0F, 0.5F);
            currentPitchOffset = CameraMath.approach(currentPitchOffset, 0.0F, 0.5F);
            return;
        }

        float renderYaw = player.prevRotationYaw + CameraMath.wrapDegrees(player.rotationYaw - player.prevRotationYaw) * partialTicks;
        float renderPitch = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * partialTicks;

        if (!initialized) {
            smoothedYaw = renderYaw;
            smoothedPitch = renderPitch;
            initialized = true;
        }

        smoothedYaw += CameraMath.wrapDegrees(renderYaw - smoothedYaw) * CameraMath.clamp(CameraConfig.smoothingStrength, 0.01F, 1.0F);
        smoothedPitch += (renderPitch - smoothedPitch) * CameraMath.clamp(CameraConfig.smoothingStrength, 0.01F, 1.0F);

        currentYawOffset = CameraMath.clamp(CameraMath.wrapDegrees(smoothedYaw - renderYaw), -CameraConfig.maxYawOffset, CameraConfig.maxYawOffset);
        currentPitchOffset = CameraMath.clamp(smoothedPitch - renderPitch, -CameraConfig.maxPitchOffset, CameraConfig.maxPitchOffset);
    }

    public float getYawOffset(float partialTicks) {
        return CameraMath.lerp(prevYawOffset, currentYawOffset, partialTicks);
    }

    public float getPitchOffset(float partialTicks) {
        return CameraMath.lerp(prevPitchOffset, currentPitchOffset, partialTicks);
    }
}
