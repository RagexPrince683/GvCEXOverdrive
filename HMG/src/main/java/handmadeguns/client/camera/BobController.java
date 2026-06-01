package handmadeguns.client.camera;

import handmadeguns.HandmadeGunsCore;
import handmadeguns.camera.CameraConfig;
import net.minecraft.client.entity.EntityClientPlayerMP;

public class BobController {
    private float phase;
    private float prevPitch;
    private float pitch;
    private float prevRoll;
    private float roll;

    public void reset() {
        phase = 0.0F;
        prevPitch = pitch = 0.0F;
        prevRoll = roll = 0.0F;
    }

    public void update(EntityClientPlayerMP player) {
        prevPitch = pitch;
        prevRoll = roll;

        if (!CameraConfig.masterEnabled || !CameraConfig.bobEnabled || player == null) {
            pitch = CameraMath.approach(pitch, 0.0F, 0.25F);
            roll = CameraMath.approach(roll, 0.0F, 0.25F);
            return;
        }

        double horizontalSpeed = Math.sqrt(player.motionX * player.motionX + player.motionZ * player.motionZ);
        float movement = CameraMath.clamp((float) horizontalSpeed * 5.5F, 0.0F, 1.0F);
        float multiplier = player.isSprinting() ? CameraConfig.bobSprintMultiplier : 1.0F;
        if (HandmadeGunsCore.Key_ADS(player)) {
            multiplier *= CameraConfig.bobAdsMultiplier;
        }

        phase += CameraConfig.bobSpeed * (0.25F + movement) * multiplier;
        float strength = CameraConfig.bobStrength * movement * multiplier;

        // Additive only: Forge 1.7.10 does not expose a safe event to replace vanilla view bobbing
        // without reaching into EntityRenderer or user game settings, so this remains a subtle overlay.
        float targetPitch = (float) Math.sin(phase * 2.0F) * 0.35F * strength;
        float targetRoll = (float) Math.cos(phase) * 0.22F * strength;
        pitch = CameraMath.approach(pitch, targetPitch, 0.2F);
        roll = CameraMath.approach(roll, targetRoll, 0.2F);
    }

    public float getPitch(float partialTicks) {
        return CameraMath.lerp(prevPitch, pitch, partialTicks);
    }

    public float getRoll(float partialTicks) {
        return CameraMath.lerp(prevRoll, roll, partialTicks);
    }
}
