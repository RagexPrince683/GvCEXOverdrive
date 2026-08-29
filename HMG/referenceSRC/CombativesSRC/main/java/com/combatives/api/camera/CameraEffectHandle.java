package com.combatives.api.camera;

public interface CameraEffectHandle {
    boolean isActive();
    void setStrength(float strength);
    void setPosition(double x, double y, double z);
    void setEnabled(boolean enabled);
    void stop();
}
