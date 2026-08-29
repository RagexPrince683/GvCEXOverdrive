package com.combatives.api.camera.entity;

/** A stateful, per-mount provider. Implementations must not modify rendering or rider rotation. */
public interface EntityCameraBehavior {
    void onAttach(MountCameraContext context, CameraEffectSink sink);
    void onTick(MountCameraContext context, CameraEffectSink sink);
    void onRender(MountCameraContext context, CameraEffectSink sink);
    void onDetach(MountCameraContext context, CameraEffectSink sink);
}
