package com.glowingfederal.combatives.client.camera.internal;

import com.combatives.api.camera.CameraEffectHandle;
import com.combatives.api.camera.CameraImpulse;
import com.combatives.api.camera.ContinuousCameraEffect;
import com.combatives.api.camera.entity.CameraEffectSink;
import com.combatives.api.camera.entity.EntityBehaviorRegistration;

/** Provider-scoped diagnostic decorator around the single manager adapter. */
final class ProviderCameraEffectSink implements CameraEffectSink {
    private final EntityBehaviorRegistration registration;
    ProviderCameraEffectSink(EntityBehaviorRegistration registration) { this.registration=registration; }
    public boolean emitFrame(CameraImpulse intent,float strength){boolean accepted=EntityCameraEffectSink.INSTANCE.emitFrame(intent,strength);EntityCameraBehaviorDiagnostics.effect(registration,"frame",intent,accepted);return accepted;}
    public boolean emitImpulse(CameraImpulse intent){boolean accepted=EntityCameraEffectSink.INSTANCE.emitImpulse(intent);EntityCameraBehaviorDiagnostics.effect(registration,"impulse",intent,accepted);return accepted;}
    public CameraEffectHandle beginContinuous(ContinuousCameraEffect intent){CameraEffectHandle handle=EntityCameraEffectSink.INSTANCE.beginContinuous(intent);EntityCameraBehaviorDiagnostics.effect(registration,"continuous",intent==null?null:intent.getImpulse(),handle!=null);return handle;}
    public boolean contribute(CameraImpulse intent,float strength){return emitFrame(intent,strength);} public boolean submitImpulse(CameraImpulse intent){return emitImpulse(intent);} public CameraEffectHandle startContinuous(ContinuousCameraEffect intent){return beginContinuous(intent);}
}
