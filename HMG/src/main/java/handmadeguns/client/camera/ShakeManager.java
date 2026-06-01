package handmadeguns.client.camera;

import handmadeguns.camera.CameraConfig;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class ShakeManager {
    private final Random random = new Random();
    private final List<ShakeImpulse> impulses = new ArrayList<ShakeImpulse>();
    private float prevPitch;
    private float pitch;
    private float prevYaw;
    private float yaw;
    private float prevRoll;
    private float roll;

    public void reset() {
        impulses.clear();
        prevPitch = pitch = 0.0F;
        prevYaw = yaw = 0.0F;
        prevRoll = roll = 0.0F;
    }

    public void update() {
        prevPitch = pitch;
        prevYaw = yaw;
        prevRoll = roll;

        if (!CameraConfig.masterEnabled || !CameraConfig.shakeEnabled) {
            pitch = CameraMath.approach(pitch, 0.0F, 0.4F);
            yaw = CameraMath.approach(yaw, 0.0F, 0.4F);
            roll = CameraMath.approach(roll, 0.0F, 0.4F);
            impulses.clear();
            return;
        }

        float targetPitch = 0.0F;
        float targetYaw = 0.0F;
        float targetRoll = 0.0F;
        Iterator<ShakeImpulse> iterator = impulses.iterator();
        while (iterator.hasNext()) {
            ShakeImpulse impulse = iterator.next();
            targetPitch += impulse.pitch * impulse.life;
            targetYaw += impulse.yaw * impulse.life;
            targetRoll += impulse.roll * impulse.life;
            impulse.life *= impulse.decay;
            if (impulse.life < 0.015F) {
                iterator.remove();
            }
        }

        pitch = CameraMath.clamp(targetPitch, -CameraConfig.maxShakePitch, CameraConfig.maxShakePitch);
        yaw = CameraMath.clamp(targetYaw, -CameraConfig.maxShakeYaw, CameraConfig.maxShakeYaw);
        roll = CameraMath.clamp(targetRoll, -CameraConfig.maxShakeRoll, CameraConfig.maxShakeRoll);
    }

    public void addRecoilShake(float strength) {
        addImpulse(strength * CameraConfig.recoilShakeMultiplier, 0.72F, -1.0F, 0.35F, 0.45F);
    }

    public void addExplosionShake(float strength) {
        addImpulse(strength * CameraConfig.explosionShakeMultiplier, 0.86F, 0.7F, 0.8F, 0.9F);
    }

    public void addLandingShake(float strength) {
        addImpulse(strength * CameraConfig.landingShakeMultiplier, 0.78F, 0.9F, 0.25F, 0.6F);
    }

    private void addImpulse(float strength, float decay, float pitchBias, float yawScale, float rollScale) {
        if (!CameraConfig.masterEnabled || !CameraConfig.shakeEnabled || strength <= 0.0F) return;
        strength = CameraMath.clamp(strength, 0.0F, 6.0F);
        float pitchImpulse = pitchBias * strength;
        float yawImpulse = (random.nextFloat() * 2.0F - 1.0F) * strength * yawScale;
        float rollImpulse = (random.nextFloat() * 2.0F - 1.0F) * strength * rollScale;
        impulses.add(new ShakeImpulse(pitchImpulse, yawImpulse, rollImpulse, CameraMath.clamp(decay, 0.1F, 0.98F)));
    }

    public float getPitch(float partialTicks) {
        return CameraMath.lerp(prevPitch, pitch, partialTicks);
    }

    public float getYaw(float partialTicks) {
        return CameraMath.lerp(prevYaw, yaw, partialTicks);
    }

    public float getRoll(float partialTicks) {
        return CameraMath.lerp(prevRoll, roll, partialTicks);
    }

    private static class ShakeImpulse {
        private final float pitch;
        private final float yaw;
        private final float roll;
        private final float decay;
        private float life = 1.0F;

        private ShakeImpulse(float pitch, float yaw, float roll, float decay) {
            this.pitch = pitch;
            this.yaw = yaw;
            this.roll = roll;
            this.decay = decay;
        }
    }
}
