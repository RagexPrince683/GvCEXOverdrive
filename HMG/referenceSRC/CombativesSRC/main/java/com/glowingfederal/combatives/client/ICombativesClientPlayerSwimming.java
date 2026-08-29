package com.glowingfederal.combatives.client;

public interface ICombativesClientPlayerSwimming {
    boolean isActuallySneaking();
    boolean isForcedDown();
    boolean isUsingSwimmingAnimation();
    boolean isUsingSwimmingAnimation(float moveForward, float moveStrafe);
    boolean canSwimClient();
    boolean isMovingForward(float moveForward, float moveStrafe);
}
