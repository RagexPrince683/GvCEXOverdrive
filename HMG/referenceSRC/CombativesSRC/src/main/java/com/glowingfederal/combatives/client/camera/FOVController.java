package com.glowingfederal.combatives.client.camera;

public final class FOVController {
    private float modifier;
    public void update(MovementCameraState state) {
        float target = 0.0F;
        if (state.isSprinting()) target += Math.min(state.getSpeed() * 0.16F, 0.035F);
        if (state.isSwimming()) target += Math.min(state.getSpeed() * 0.12F, 0.025F);
        modifier += (target - modifier) * 0.12F;
    }
    public void reset() { modifier = 0.0F; }
    public float getModifier() { return modifier; }
}
