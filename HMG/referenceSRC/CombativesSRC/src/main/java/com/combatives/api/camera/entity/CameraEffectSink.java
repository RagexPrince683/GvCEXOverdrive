package com.combatives.api.camera.entity;

import com.combatives.api.camera.CameraEffectHandle;
import com.combatives.api.camera.CameraImpulse;
import com.combatives.api.camera.ContinuousCameraEffect;

/** Camera intent emitted by a provider; no accumulator or renderer implementation is exposed. */
public interface CameraEffectSink {
    boolean emitFrame(CameraImpulse intent, float strength);
    boolean emitImpulse(CameraImpulse intent);
    CameraEffectHandle beginContinuous(ContinuousCameraEffect intent);
    /** @deprecated Use {@link #emitFrame(CameraImpulse, float)}. */
    boolean contribute(CameraImpulse contribution, float strength);
    /** @deprecated Use {@link #emitImpulse(CameraImpulse)}. */
    boolean submitImpulse(CameraImpulse impulse);
    /** @deprecated Use {@link #beginContinuous(ContinuousCameraEffect)}. */
    CameraEffectHandle startContinuous(ContinuousCameraEffect effect);
}
