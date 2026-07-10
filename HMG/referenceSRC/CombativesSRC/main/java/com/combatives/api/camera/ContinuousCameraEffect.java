package com.combatives.api.camera;

public final class ContinuousCameraEffect {
    private final CameraImpulse impulse; private final float strength;
    private ContinuousCameraEffect(Builder b){impulse=b.impulse; strength=b.strength;}
    public static Builder builder(CameraImpulse impulse){return new Builder(impulse);} public CameraImpulse getImpulse(){return impulse;} public float getStrength(){return strength;}
    public static final class Builder { private final CameraImpulse impulse; private float strength=1.0F; private Builder(CameraImpulse impulse){this.impulse=impulse;} public Builder strength(float strength){this.strength=strength;return this;} public ContinuousCameraEffect build(){return new ContinuousCameraEffect(this);} }
}
