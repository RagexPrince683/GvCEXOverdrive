package handmadeguns.camera;

import net.minecraftforge.common.config.Configuration;

/** Client camera-feel configuration. Read on both sides, but consumed only by client event handlers. */
public final class CameraConfig {
    private static final String CATEGORY = "ClientCamera";

    public static boolean masterEnabled = true;

    public static boolean rotationSmoothingEnabled = true;
    public static float smoothingStrength = 0.35F;
    public static float maxYawOffset = 4.0F;
    public static float maxPitchOffset = 3.0F;

    public static boolean motionTiltEnabled = true;
    public static float motionTiltMaxRoll = 4.0F;
    public static float motionTiltMaxPitchOffset = 2.0F;
    public static float motionTiltReturnSpeed = 0.18F;

    public static boolean bobEnabled = true;
    public static float bobStrength = 0.45F;
    public static float bobSpeed = 0.72F;
    public static float bobSprintMultiplier = 1.35F;
    public static float bobAdsMultiplier = 0.45F;

    public static boolean fovEnabled = true;
    public static float fovLerpSpeed = 0.22F;
    public static float sprintFovBoost = 0.04F;
    public static float adsFovSpeed = 0.34F;

    public static boolean shakeEnabled = true;
    public static float maxShakePitch = 3.5F;
    public static float maxShakeYaw = 2.0F;
    public static float maxShakeRoll = 3.0F;
    public static float recoilShakeMultiplier = 0.18F;
    public static float explosionShakeMultiplier = 1.0F;
    public static float landingShakeMultiplier = 1.0F;

    private CameraConfig() {
    }

    public static void load(Configuration config) {
        masterEnabled = config.get(CATEGORY, "enabled", masterEnabled,
                "Master switch for the low-conflict, client-only first-person camera feel system.").getBoolean(masterEnabled);

        rotationSmoothingEnabled = config.get(CATEGORY, "rotationSmoothing.enabled", rotationSmoothingEnabled).getBoolean(rotationSmoothingEnabled);
        smoothingStrength = (float) config.get(CATEGORY, "rotationSmoothing.smoothingStrength", smoothingStrength,
                "Higher values follow mouse input faster; lower values add more visual inertia only.").getDouble(smoothingStrength);
        maxYawOffset = (float) config.get(CATEGORY, "rotationSmoothing.maxYawOffset", maxYawOffset).getDouble(maxYawOffset);
        maxPitchOffset = (float) config.get(CATEGORY, "rotationSmoothing.maxPitchOffset", maxPitchOffset).getDouble(maxPitchOffset);

        motionTiltEnabled = config.get(CATEGORY, "motionTilt.enabled", motionTiltEnabled).getBoolean(motionTiltEnabled);
        motionTiltMaxRoll = (float) config.get(CATEGORY, "motionTilt.maxRoll", motionTiltMaxRoll).getDouble(motionTiltMaxRoll);
        motionTiltMaxPitchOffset = (float) config.get(CATEGORY, "motionTilt.maxPitchOffset", motionTiltMaxPitchOffset).getDouble(motionTiltMaxPitchOffset);
        motionTiltReturnSpeed = (float) config.get(CATEGORY, "motionTilt.returnSpeed", motionTiltReturnSpeed).getDouble(motionTiltReturnSpeed);

        bobEnabled = config.get(CATEGORY, "bob.enabled", bobEnabled).getBoolean(bobEnabled);
        bobStrength = (float) config.get(CATEGORY, "bob.bobStrength", bobStrength).getDouble(bobStrength);
        bobSpeed = (float) config.get(CATEGORY, "bob.bobSpeed", bobSpeed).getDouble(bobSpeed);
        bobSprintMultiplier = (float) config.get(CATEGORY, "bob.sprintMultiplier", bobSprintMultiplier).getDouble(bobSprintMultiplier);
        bobAdsMultiplier = (float) config.get(CATEGORY, "bob.adsMultiplier", bobAdsMultiplier).getDouble(bobAdsMultiplier);

        fovEnabled = config.get(CATEGORY, "fov.enabled", fovEnabled).getBoolean(fovEnabled);
        fovLerpSpeed = (float) config.get(CATEGORY, "fov.fovLerpSpeed", fovLerpSpeed).getDouble(fovLerpSpeed);
        sprintFovBoost = (float) config.get(CATEGORY, "fov.sprintFovBoost", sprintFovBoost).getDouble(sprintFovBoost);
        adsFovSpeed = (float) config.get(CATEGORY, "fov.adsFovSpeed", adsFovSpeed).getDouble(adsFovSpeed);

        shakeEnabled = config.get(CATEGORY, "shake.enabled", shakeEnabled).getBoolean(shakeEnabled);
        maxShakePitch = (float) config.get(CATEGORY, "shake.maxPitch", maxShakePitch).getDouble(maxShakePitch);
        maxShakeYaw = (float) config.get(CATEGORY, "shake.maxYaw", maxShakeYaw).getDouble(maxShakeYaw);
        maxShakeRoll = (float) config.get(CATEGORY, "shake.maxRoll", maxShakeRoll).getDouble(maxShakeRoll);
        recoilShakeMultiplier = (float) config.get(CATEGORY, "shake.recoilMultiplier", recoilShakeMultiplier).getDouble(recoilShakeMultiplier);
        explosionShakeMultiplier = (float) config.get(CATEGORY, "shake.explosionMultiplier", explosionShakeMultiplier).getDouble(explosionShakeMultiplier);
        landingShakeMultiplier = (float) config.get(CATEGORY, "shake.landingMultiplier", landingShakeMultiplier).getDouble(landingShakeMultiplier);
    }
}
