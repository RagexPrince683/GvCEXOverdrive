package com.glowingfederal.combatives.client.camera.internal;

import com.combatives.api.camera.CameraEffectHandle;
import com.combatives.api.camera.CameraImpulse;
import com.combatives.api.camera.ContinuousCameraEffect;
import com.combatives.api.camera.entity.CameraEffectSink;

/** Thin validated adapter; CameraEffectManager retains all accumulation and clamp ownership. */
public final class EntityCameraEffectSink implements CameraEffectSink {
    public static final EntityCameraEffectSink INSTANCE = new EntityCameraEffectSink();
    private EntityCameraEffectSink() {}
    public boolean emitFrame(CameraImpulse intent, float strength) { return CameraEffectManager.submitFrameContribution(intent, strength); }
    public boolean emitImpulse(CameraImpulse intent) { return CameraEffectManager.submitImpulse(intent); }
    public CameraEffectHandle beginContinuous(ContinuousCameraEffect intent) { return CameraEffectManager.startContinuousEffect(intent); }
    public boolean contribute(CameraImpulse contribution, float strength) { return emitFrame(contribution, strength); }
    public boolean submitImpulse(CameraImpulse impulse) { return emitImpulse(impulse); }
    public CameraEffectHandle startContinuous(ContinuousCameraEffect effect) { return beginContinuous(effect); }
}
