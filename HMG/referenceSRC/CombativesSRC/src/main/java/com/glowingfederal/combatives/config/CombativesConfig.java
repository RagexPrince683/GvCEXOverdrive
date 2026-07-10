package com.glowingfederal.combatives.config;

import java.io.File;

import com.glowingfederal.combatives.build.BuildInfo;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Logger;

public final class CombativesConfig {
    private static final String CATEGORY_DEBUG = "debug";
    private static final String CATEGORY_CAMERA = "camera";

    public static boolean enableCombativesCamera = CombativesConfigDefaults.ENABLE_COMBATIVES_CAMERA;
    public static boolean enableProceduralBob = CombativesConfigDefaults.ENABLE_PROCEDURAL_BOB;
    public static boolean enableMovementLean = CombativesConfigDefaults.ENABLE_MOVEMENT_LEAN;
    public static boolean enableMovementFov = CombativesConfigDefaults.ENABLE_MOVEMENT_FOV;
    public static boolean enableCameraRotations = CombativesConfigDefaults.ENABLE_CAMERA_ROTATIONS;
    public static boolean enableCameraShake = CombativesConfigDefaults.ENABLE_CAMERA_SHAKE;
    public static float maxCameraYawDegrees = CombativesConfigDefaults.MAX_CAMERA_YAW_DEGREES;
    public static boolean enableMouseDeltaClamp = CombativesConfigDefaults.ENABLE_MOUSE_DELTA_CLAMP;
    public static int maxMouseDelta = CombativesConfigDefaults.MAX_MOUSE_DELTA;
    public static boolean enableLandingCameraFeedback = CombativesConfigDefaults.ENABLE_LANDING_CAMERA_FEEDBACK;
    public static double landingFeedbackStrength = CombativesConfigDefaults.LANDING_FEEDBACK_STRENGTH;
    public static boolean enableExplosionCameraFeedback = CombativesConfigDefaults.ENABLE_EXPLOSION_CAMERA_FEEDBACK;
    public static double explosionFeedbackStrength = CombativesConfigDefaults.EXPLOSION_FEEDBACK_STRENGTH;
    public static boolean debugMovement = CombativesConfigDefaults.DEBUG;
    public static boolean verboseMovementDebug = CombativesConfigDefaults.VERBOSE_DEBUG;
    public static boolean debugCamera = CombativesConfigDefaults.DEBUG;
    public static boolean verboseCameraDebug = CombativesConfigDefaults.VERBOSE_DEBUG;

    private CombativesConfig() {
    }

    private static void applyCanonicalGameplayDefaults() {
        enableCombativesCamera = CombativesConfigDefaults.ENABLE_COMBATIVES_CAMERA;
        enableProceduralBob = CombativesConfigDefaults.ENABLE_PROCEDURAL_BOB;
        enableMovementLean = CombativesConfigDefaults.ENABLE_MOVEMENT_LEAN;
        enableMovementFov = CombativesConfigDefaults.ENABLE_MOVEMENT_FOV;
        enableCameraRotations = CombativesConfigDefaults.ENABLE_CAMERA_ROTATIONS;
        enableCameraShake = CombativesConfigDefaults.ENABLE_CAMERA_SHAKE;
        maxCameraYawDegrees = CombativesConfigDefaults.MAX_CAMERA_YAW_DEGREES;
        enableMouseDeltaClamp = CombativesConfigDefaults.ENABLE_MOUSE_DELTA_CLAMP;
        maxMouseDelta = CombativesConfigDefaults.MAX_MOUSE_DELTA;
        enableLandingCameraFeedback = CombativesConfigDefaults.ENABLE_LANDING_CAMERA_FEEDBACK;
        landingFeedbackStrength = CombativesConfigDefaults.LANDING_FEEDBACK_STRENGTH;
        enableExplosionCameraFeedback = CombativesConfigDefaults.ENABLE_EXPLOSION_CAMERA_FEEDBACK;
        explosionFeedbackStrength = CombativesConfigDefaults.EXPLOSION_FEEDBACK_STRENGTH;
    }

    private static File fairplayConfigFile(File suggestedConfigFile) {
        File parent = suggestedConfigFile.getParentFile();
        return new File(parent == null ? new File(".") : parent, "Combatives-Fairplay.cfg");
    }

    private static void loadFairplay(File suggestedConfigFile) {
        applyCanonicalGameplayDefaults();

        Configuration config = new Configuration(fairplayConfigFile(suggestedConfigFile));
        config.load();
        boolean debug = config.getBoolean("debug", CATEGORY_DEBUG, CombativesConfigDefaults.DEBUG, "Enable general Combatives movement and camera diagnostics.");
        boolean verboseDebug = config.getBoolean("verboseDebug", CATEGORY_DEBUG, CombativesConfigDefaults.VERBOSE_DEBUG, "Enable verbose per-frame/per-tick Combatives movement and camera diagnostics. This implies debug output.");
        debugMovement = debug;
        debugCamera = debug;
        verboseMovementDebug = verboseDebug;
        verboseCameraDebug = verboseDebug;

        if (config.hasChanged()) {
            config.save();
        }
    }

    public static void load(File configFile) {
        if (BuildInfo.FAIRPLAY_BUILD) {
            loadFairplay(configFile);
            return;
        }
        Configuration config = new Configuration(configFile);
        config.load();

        enableCombativesCamera = config.getBoolean("enableCombativesCamera", CATEGORY_CAMERA, enableCombativesCamera, "Enable the client-only Combatives first-person camera controller.");
        enableProceduralBob = config.getBoolean("enableProceduralBob", CATEGORY_CAMERA, enableProceduralBob, "Enable subtle procedural Combatives movement bobbing.");
        enableMovementLean = config.getBoolean("enableMovementLean", CATEGORY_CAMERA, enableMovementLean, "Enable subtle movement-driven camera lean.");
        enableMovementFov = config.getBoolean("enableMovementFov", CATEGORY_CAMERA, enableMovementFov, "Enable subtle movement-driven FOV changes.");
        enableCameraRotations = config.getBoolean("enableCameraRotations", CATEGORY_CAMERA, enableCameraRotations, "Emergency diagnostic toggle: when false, Combatives applies only camera translations and FOV, never pitch or roll rotations.");
        enableCameraShake = config.getBoolean("enableCameraShake", CATEGORY_CAMERA, enableCameraShake, "Enable the Combatives camera shake framework for movement impulses.");
        maxCameraYawDegrees = config.getFloat("maxCameraYawDegrees", CATEGORY_CAMERA, maxCameraYawDegrees, 0.0F, 12.0F, "Hard clamp in degrees for visual-only Combatives yaw offsets. Tuned independently from pitch and roll.");
        enableMouseDeltaClamp = config.getBoolean("enableMouseDeltaClamp", CATEGORY_CAMERA, enableMouseDeltaClamp, "Clamp pathological raw LWJGL mouse deltas before vanilla camera sensitivity scaling consumes them.");
        maxMouseDelta = config.getInt("maxMouseDelta", CATEGORY_CAMERA, maxMouseDelta, 1, 10000, "Maximum absolute raw mouse delta accepted from LWJGL per mouseXYChange call.");
        enableLandingCameraFeedback = config.getBoolean("enableLandingCameraFeedback", CATEGORY_CAMERA, enableLandingCameraFeedback, "Enable visual-only landing camera dip and recovery impulses.");
        landingFeedbackStrength = config.getFloat("landingFeedbackStrength", CATEGORY_CAMERA, (float) landingFeedbackStrength, 0.0F, 4.0F, "Multiplier for visual-only landing camera feedback strength.");
        enableExplosionCameraFeedback = config.getBoolean("enableExplosionCameraFeedback", CATEGORY_CAMERA, enableExplosionCameraFeedback, "Enable visual-only low-frequency explosion camera feedback near client explosions.");
        explosionFeedbackStrength = config.getFloat("explosionFeedbackStrength", CATEGORY_CAMERA, (float) explosionFeedbackStrength, 0.0F, 4.0F, "Multiplier for visual-only explosion camera feedback strength.");
        debugMovement = config.getBoolean(
            "debugMovement",
            CATEGORY_DEBUG,
            debugMovement,
            "Enable general Combatives movement diagnostics for lifecycle events and rejected actions. Per-frame diagnostics remain disabled unless verboseMovementDebug is also enabled."
        );
        verboseMovementDebug = config.getBoolean(
            "verboseMovementDebug",
            CATEGORY_DEBUG,
            verboseMovementDebug,
            "Enable per-frame/per-tick Combatives movement diagnostics. This implies debugMovement output for movement diagnostics."
        );
        debugCamera = config.getBoolean(
            "debugCamera",
            CATEGORY_DEBUG,
            debugCamera,
            "Enable major Combatives camera ownership and state-change diagnostics."
        );
        verboseCameraDebug = config.getBoolean(
            "verboseCameraDebug",
            CATEGORY_DEBUG,
            verboseCameraDebug,
            "Enable throttled per-frame Combatives camera diagnostics."
        );

        if (config.hasChanged()) {
            config.save();
        }
    }

    public static void logLoadedValues(Logger logger) {
        logger.info("Combatives config: enableCombativesCamera={}", enableCombativesCamera);
        logger.info("Combatives config: enableProceduralBob={}", enableProceduralBob);
        logger.info("Combatives config: enableMovementLean={}", enableMovementLean);
        logger.info("Combatives config: enableMovementFov={}", enableMovementFov);
        logger.info("Combatives config: enableCameraRotations={}", enableCameraRotations);
        logger.info("Combatives config: enableCameraShake={}", enableCameraShake);
        logger.info("Combatives config: maxCameraYawDegrees={}", maxCameraYawDegrees);
        logger.info("Combatives config: enableMouseDeltaClamp={}", enableMouseDeltaClamp);
        logger.info("Combatives config: maxMouseDelta={}", maxMouseDelta);
        logger.info("Combatives config: enableLandingCameraFeedback={}", enableLandingCameraFeedback);
        logger.info("Combatives config: landingFeedbackStrength={}", landingFeedbackStrength);
        logger.info("Combatives config: enableExplosionCameraFeedback={}", enableExplosionCameraFeedback);
        logger.info("Combatives config: explosionFeedbackStrength={}", explosionFeedbackStrength);
        logger.info("Combatives config: debugMovement={}", debugMovement);
        logger.info("Combatives config: verboseMovementDebug={}", verboseMovementDebug);
        logger.info("Combatives config: debugCamera={}", debugCamera);
        logger.info("Combatives config: verboseCameraDebug={}", verboseCameraDebug);
    }
}
