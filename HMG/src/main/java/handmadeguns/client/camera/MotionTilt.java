package handmadeguns.client.camera;

import handmadeguns.camera.CameraConfig;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.util.MathHelper;

public class MotionTilt {
    private float prevRoll;
    private float roll;
    private float prevPitch;
    private float pitch;
    private double lastHorizontalSpeed;

    public void reset() {
        prevRoll = roll = 0.0F;
        prevPitch = pitch = 0.0F;
        lastHorizontalSpeed = 0.0D;
    }

    public void update(EntityClientPlayerMP player) {
        prevRoll = roll;
        prevPitch = pitch;

        if (!CameraConfig.masterEnabled || !CameraConfig.motionTiltEnabled || player == null) {
            roll = CameraMath.approach(roll, 0.0F, CameraConfig.motionTiltReturnSpeed);
            pitch = CameraMath.approach(pitch, 0.0F, CameraConfig.motionTiltReturnSpeed);
            return;
        }

        double horizontalSpeed = Math.sqrt(player.motionX * player.motionX + player.motionZ * player.motionZ);
        float yawRad = player.rotationYaw * 0.017453292F;
        double rightX = MathHelper.cos(yawRad);
        double rightZ = MathHelper.sin(yawRad);
        double strafe = player.motionX * rightX + player.motionZ * rightZ;
        double acceleration = horizontalSpeed - lastHorizontalSpeed;
        lastHorizontalSpeed = horizontalSpeed;

        float targetRoll = (float) (-strafe * 28.0D);
        targetRoll = CameraMath.clamp(targetRoll, -CameraConfig.motionTiltMaxRoll, CameraConfig.motionTiltMaxRoll);

        float targetPitch = (float) (-(horizontalSpeed * 2.2D) - acceleration * 18.0D);
        if (player.isSprinting()) {
            targetPitch -= 0.55F;
        }
        if (!player.onGround) {
            targetPitch += CameraMath.clamp((float) (-player.motionY * 2.2D), -1.2F, 1.2F);
        }
        targetPitch = CameraMath.clamp(targetPitch, -CameraConfig.motionTiltMaxPitchOffset, CameraConfig.motionTiltMaxPitchOffset);

        float speed = CameraMath.clamp(CameraConfig.motionTiltReturnSpeed, 0.01F, 1.0F);
        roll = CameraMath.approach(roll, targetRoll, speed);
        pitch = CameraMath.approach(pitch, targetPitch, speed);
    }

    public float getRoll(float partialTicks) {
        return CameraMath.lerp(prevRoll, roll, partialTicks);
    }

    public float getPitch(float partialTicks) {
        return CameraMath.lerp(prevPitch, pitch, partialTicks);
    }
}
