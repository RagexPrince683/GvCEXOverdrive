package com.glowingfederal.combatives.client.camera.internal;

import com.combatives.api.camera.CameraEffectHandle;
import com.combatives.api.camera.CameraImpulse;
import com.combatives.api.camera.CameraStackingMode;
import com.combatives.api.camera.ContinuousCameraEffect;
import com.glowingfederal.combatives.config.CombativesConfig;

/** Development-only assertions for auditing each camera API channel before render transforms are applied. */
public final class CameraEffectManagerDevelopmentAssertions {
    private CameraEffectManagerDevelopmentAssertions() {}

    public static void runAll() {
        boolean oldEnabled = CombativesConfig.enableCombativesCamera;
        float oldYawClamp = CombativesConfig.maxCameraYawDegrees;
        try {
            CombativesConfig.enableCombativesCamera = true;
            CombativesConfig.maxCameraYawDegrees = 4.0F;
            assertChannel("pitch", impulse("dev:pitch").rotation(2, 0, 0).build(), 'p');
            assertChannel("yaw", impulse("dev:yaw").rotation(0, 2, 0).build(), 'y');
            assertChannel("roll", impulse("dev:roll").rotation(0, 0, 2).build(), 'r');
            assertChannel("x", impulse("dev:x").translation(0.05F, 0, 0).build(), 'x');
            assertChannel("y", impulse("dev:y_axis").translation(0, 0.05F, 0).build(), 'v');
            assertChannel("z", impulse("dev:z").translation(0, 0, 0.05F).build(), 'z');
            assertChannel("fov", impulse("dev:fov").fov(2).build(), 'f');
            assertYaw("combined pitch/yaw/roll", impulse("dev:combined").rotation(1, 2, 3).build(), 0.1F, 4.0F);
            assertYaw("positional directional yaw", impulse("dev:positional_yaw").position(1, 2, 3).rotation(0, 2, 0).translation(0.05F, 0, 0).build(), 0.1F, 4.0F);
            assertYaw("stacked yaw impulses", impulse("dev:stack_a").rotation(0, 3, 0).build(), 0.1F, 4.0F, impulse("dev:stack_b").rotation(0, 3, 0).build());
            assertYaw("REPLACE_SAME_ID yaw", impulse("dev:replace").rotation(0, 1, 0).stackingMode(CameraStackingMode.REPLACE_SAME_ID).build(), 0.1F, 4.0F, impulse("dev:replace").rotation(0, 3, 0).stackingMode(CameraStackingMode.REPLACE_SAME_ID).build());
            assertYaw("REFRESH_SAME_ID yaw", impulse("dev:refresh").rotation(0, 2, 0).stackingMode(CameraStackingMode.REFRESH_SAME_ID).build(), 0.1F, 4.0F, impulse("dev:refresh").rotation(0, 4, 0).stackingMode(CameraStackingMode.REFRESH_SAME_ID).build());
            CameraEffectManager.reset();
            CameraEffectHandle handle = CameraEffectManager.startContinuousEffectForDevelopmentTest(ContinuousCameraEffect.builder(impulse("dev:continuous").rotation(0, 2, 0).build()).strength(1).build());
            CameraEffectManager.update(null);
            assertTrue("continuous yaw", handle.isActive() && CameraEffectManager.getYaw() > 0.1F);
            assertYaw("yaw saturation", impulse("dev:saturation").rotation(0, 100, 0).build(), 0.1F, 4.0F);
            assertYaw("yaw clamp", impulse("dev:clamp").rotation(0, 1000, 0).build(), 0.1F, 4.0F);
            CameraEffectManager.reset();
            assertTrue("NaN yaw rejection", !CameraEffectManager.submitImpulseForDevelopmentTest(impulse("dev:nan").rotation(0, Float.NaN, 0).build()));
            CombativesConfig.enableCombativesCamera = false;
            assertTrue("camera-disabled rejection", !CameraEffectManager.submitImpulse(impulse("dev:disabled").rotation(0, 1, 0).build()));
        } finally {
            CombativesConfig.enableCombativesCamera = oldEnabled;
            CombativesConfig.maxCameraYawDegrees = oldYawClamp;
            CameraEffectManager.reset();
        }
    }

    private static CameraImpulse.Builder impulse(String id) { return CameraImpulse.builder(id).duration(0.35F); }
    private static void assertYaw(String name, CameraImpulse first, float min, float max, CameraImpulse... more) { CameraEffectManager.reset(); assertTrue(name, CameraEffectManager.submitImpulseForDevelopmentTest(first)); for (CameraImpulse i : more) CameraEffectManager.submitImpulseForDevelopmentTest(i); CameraEffectManager.update(null); assertTrue(name, CameraEffectManager.getYaw() >= min && CameraEffectManager.getYaw() <= max); }
    private static void assertChannel(String name, CameraImpulse impulse, char channel) { CameraEffectManager.reset(); assertTrue(name, CameraEffectManager.submitImpulseForDevelopmentTest(impulse)); CameraEffectManager.update(null); float value = channel=='p'?CameraEffectManager.getPitch():channel=='y'?CameraEffectManager.getYaw():channel=='r'?CameraEffectManager.getRoll():channel=='x'?CameraEffectManager.getX():channel=='v'?CameraEffectManager.getY():channel=='z'?CameraEffectManager.getZ():CameraEffectManager.getFov(); assertTrue(name, value > 0.0F); }
    private static void assertTrue(String name, boolean value) { if (!value) throw new AssertionError("CameraEffectManager development assertion failed: " + name); }
}
