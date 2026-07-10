package com.combatives.api.camera;

import com.glowingfederal.combatives.client.camera.internal.CameraEffectManager;
import java.util.EnumSet;
import java.util.Set;

public final class CombativesCameraAPI {
    public static final int API_VERSION = 1;
    private static final Set<CameraCapability> CAPABILITIES = EnumSet.of(CameraCapability.ROTATION_PITCH, CameraCapability.ROTATION_YAW, CameraCapability.ROTATION_ROLL, CameraCapability.TRANSLATION, CameraCapability.FOV, CameraCapability.POSITIONAL_FALLOFF, CameraCapability.CONTINUOUS_EFFECTS, CameraCapability.PRESET_EFFECTS, CameraCapability.CUSTOM_IMPULSES, CameraCapability.CLIENT_NETWORK_HELPERS);
    private CombativesCameraAPI() {}
    public static int getApiVersion(){return API_VERSION;}
    public static Set<CameraCapability> getCapabilities(){return EnumSet.copyOf(CAPABILITIES);}
    public static boolean isAvailable(){return CameraEffectManager.isAvailable();}
    public static boolean trigger(CameraEffectType type, CameraEffectContext context, float strength){return CameraEffectManager.trigger(type, context, strength);}
    public static boolean submitImpulse(CameraImpulse impulse){return CameraEffectManager.submitImpulse(impulse);}
    public static CameraEffectHandle startContinuousEffect(ContinuousCameraEffect effect){return CameraEffectManager.startContinuousEffect(effect);}
}
