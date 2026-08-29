package com.glowingfederal.combatives.client.camera;

public final class LeanController {
    private float roll, pitch, rollVelocity, pitchVelocity;
    public void update(MovementCameraState state) {
        float scale = state.isCrawling() || state.isSwimming() ? 0.45F : state.isSneaking() ? 0.65F : 1.0F;
        spring(state.getStrafe() * -1.35F * scale, state.getForward() * -0.45F * scale);
    }
    private void spring(float targetRoll, float targetPitch) {
        rollVelocity += (targetRoll - roll) * 0.08F; rollVelocity *= 0.72F; roll += rollVelocity;
        pitchVelocity += (targetPitch - pitch) * 0.07F; pitchVelocity *= 0.74F; pitch += pitchVelocity;
    }
    public void reset() { roll = pitch = rollVelocity = pitchVelocity = 0.0F; }
    public float getRoll() { return roll; }
    public float getPitch() { return pitch; }
}
