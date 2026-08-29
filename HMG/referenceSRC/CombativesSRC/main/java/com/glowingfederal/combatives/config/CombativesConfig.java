package com.glowingfederal.combatives.config;

import java.io.File;

import com.glowingfederal.combatives.build.BuildInfo;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Logger;

public final class CombativesConfig {
    private static final String CATEGORY_DEBUG = "debug";
    private static final String CATEGORY_CAMERA = "camera";
    private static final String CATEGORY_COMPATIBILITY = "compatibility";

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
    public static boolean enablePlayerFreefallCamera = CombativesConfigDefaults.ENABLE_PLAYER_FREEFALL_CAMERA;
    public static double playerFreefallCameraStrength = CombativesConfigDefaults.PLAYER_FREEFALL_CAMERA_STRENGTH;
    public static boolean enablePlayerInertiaCamera = CombativesConfigDefaults.ENABLE_PLAYER_INERTIA_CAMERA;
    public static double playerInertiaCameraStrength = CombativesConfigDefaults.PLAYER_INERTIA_CAMERA_STRENGTH;
    public static boolean enablePlayerCollisionCamera = CombativesConfigDefaults.ENABLE_PLAYER_COLLISION_CAMERA;
    public static double playerCollisionCameraStrength = CombativesConfigDefaults.PLAYER_COLLISION_CAMERA_STRENGTH;
    public static boolean enableExplosionCameraFeedback = CombativesConfigDefaults.ENABLE_EXPLOSION_CAMERA_FEEDBACK;
    public static double explosionFeedbackStrength = CombativesConfigDefaults.EXPLOSION_FEEDBACK_STRENGTH;
    public static boolean enableHorseCamera = CombativesConfigDefaults.ENABLE_HORSE_CAMERA;
    public static double horseCameraAmplitude = CombativesConfigDefaults.HORSE_CAMERA_AMPLITUDE;
    public static double horseTerrainImpulse = CombativesConfigDefaults.HORSE_TERRAIN_IMPULSE;
    public static double horseLanding = CombativesConfigDefaults.HORSE_LANDING;
    public static double horseTurningRoll = CombativesConfigDefaults.HORSE_TURNING_ROLL;
    public static boolean enableCrawlCamera = CombativesConfigDefaults.ENABLE_CRAWL_CAMERA;
    public static double crawlCameraAmplitude = CombativesConfigDefaults.CRAWL_CAMERA_AMPLITUDE;
    public static int crawlTransitionMillis = CombativesConfigDefaults.CRAWL_TRANSITION_MILLIS;
    public static boolean debugMovement = CombativesConfigDefaults.DEBUG;
    public static boolean verboseMovementDebug = CombativesConfigDefaults.VERBOSE_DEBUG;
    public static boolean debugCamera = CombativesConfigDefaults.DEBUG;
    public static boolean verboseCameraDebug = CombativesConfigDefaults.VERBOSE_DEBUG;
    public static boolean debugMpmPov = CombativesConfigDefaults.DEBUG;
    public static boolean enableMpmHitboxScaling = CombativesConfigDefaults.ENABLE_MPM_HITBOX_SCALING;

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
        enablePlayerFreefallCamera = CombativesConfigDefaults.ENABLE_PLAYER_FREEFALL_CAMERA;
        playerFreefallCameraStrength = CombativesConfigDefaults.PLAYER_FREEFALL_CAMERA_STRENGTH;
        enablePlayerInertiaCamera = CombativesConfigDefaults.ENABLE_PLAYER_INERTIA_CAMERA;
        playerInertiaCameraStrength = CombativesConfigDefaults.PLAYER_INERTIA_CAMERA_STRENGTH;
        enablePlayerCollisionCamera = CombativesConfigDefaults.ENABLE_PLAYER_COLLISION_CAMERA;
        playerCollisionCameraStrength = CombativesConfigDefaults.PLAYER_COLLISION_CAMERA_STRENGTH;
        enableExplosionCameraFeedback = CombativesConfigDefaults.ENABLE_EXPLOSION_CAMERA_FEEDBACK;
        explosionFeedbackStrength = CombativesConfigDefaults.EXPLOSION_FEEDBACK_STRENGTH;
        enableHorseCamera = CombativesConfigDefaults.ENABLE_HORSE_CAMERA;
        horseCameraAmplitude = CombativesConfigDefaults.HORSE_CAMERA_AMPLITUDE;
        horseTerrainImpulse = CombativesConfigDefaults.HORSE_TERRAIN_IMPULSE;
        horseLanding = CombativesConfigDefaults.HORSE_LANDING;
        horseTurningRoll = CombativesConfigDefaults.HORSE_TURNING_ROLL;
        enableCrawlCamera = CombativesConfigDefaults.ENABLE_CRAWL_CAMERA;
        crawlCameraAmplitude = CombativesConfigDefaults.CRAWL_CAMERA_AMPLITUDE;
        crawlTransitionMillis = CombativesConfigDefaults.CRAWL_TRANSITION_MILLIS;
        enableMpmHitboxScaling = CombativesConfigDefaults.ENABLE_MPM_HITBOX_SCALING;
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
        debugMpmPov = debug;
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
        enablePlayerFreefallCamera = config.getBoolean("enablePlayerFreefallCamera", CATEGORY_CAMERA, enablePlayerFreefallCamera, "Enable subtle sustained player freefall anticipation.");
        playerFreefallCameraStrength = config.getFloat("playerFreefallCameraStrength", CATEGORY_CAMERA, (float)playerFreefallCameraStrength, 0F, 4F, "Strength of player freefall feedback.");
        enablePlayerInertiaCamera = config.getBoolean("enablePlayerInertiaCamera", CATEGORY_CAMERA, enablePlayerInertiaCamera, "Enable conservative motion-sampled player inertia.");
        playerInertiaCameraStrength = config.getFloat("playerInertiaCameraStrength", CATEGORY_CAMERA, (float)playerInertiaCameraStrength, 0F, 4F, "Strength of player inertia feedback.");
        enablePlayerCollisionCamera = config.getBoolean("enablePlayerCollisionCamera", CATEGORY_CAMERA, enablePlayerCollisionCamera, "Enable meaningful player momentum-loss impacts.");
        playerCollisionCameraStrength = config.getFloat("playerCollisionCameraStrength", CATEGORY_CAMERA, (float)playerCollisionCameraStrength, 0F, 4F, "Strength of player collision feedback.");
        enableExplosionCameraFeedback = config.getBoolean("enableExplosionCameraFeedback", CATEGORY_CAMERA, enableExplosionCameraFeedback, "Enable visual-only low-frequency explosion camera feedback near client explosions.");
        explosionFeedbackStrength = config.getFloat("explosionFeedbackStrength", CATEGORY_CAMERA, (float) explosionFeedbackStrength, 0.0F, 4.0F, "Multiplier for visual-only explosion camera feedback strength.");
        enableHorseCamera = config.getBoolean("enableHorseCamera", CATEGORY_CAMERA, enableHorseCamera, "Enable continuous, motion-sampled first-person riding feedback for registered horse mounts.");
        horseCameraAmplitude = config.getFloat("horseCameraAmplitude", CATEGORY_CAMERA, (float)horseCameraAmplitude, 0F, 3F, "Multiplier for horse gait bob, pitch, and fore/aft travel.");
        horseTerrainImpulse = config.getFloat("horseTerrainImpulse", CATEGORY_CAMERA, (float)horseTerrainImpulse, 0F, 3F, "Multiplier for subtle horse terrain-compression impulses.");
        horseLanding = config.getFloat("horseLanding", CATEGORY_CAMERA, (float)horseLanding, 0F, 3F, "Multiplier for horse jump landing compression through the shared impulse pipeline.");
        horseTurningRoll = config.getFloat("horseTurningRoll", CATEGORY_CAMERA, (float)horseTurningRoll, 0F, 3F, "Multiplier for damped horse turning roll (hard-limited to two degrees before shared saturation).");
        enableCrawlCamera = config.getBoolean("enableCrawlCamera", CATEGORY_CAMERA, enableCrawlCamera, "Enable restrained continuous crawling motion and crawl transitions.");
        crawlCameraAmplitude = config.getFloat("crawlCameraAmplitude", CATEGORY_CAMERA, (float)crawlCameraAmplitude, 0F, 3F, "Multiplier for crawl-cycle movement and pull impulses.");
        crawlTransitionMillis = config.getInt("crawlTransitionMillis", CATEGORY_CAMERA, crawlTransitionMillis, 150, 250, "Monotonic crawl enter/exit camera blend duration in milliseconds.");
        enableMpmHitboxScaling = config.getBoolean("enableMpmHitboxScaling", CATEGORY_COMPATIBILITY,
                enableMpmHitboxScaling, "Scale player collision width and height by MorePlayerModels+'s synchronized whole-model size. Independent of camera compatibility.");
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
        debugMpmPov = config.getBoolean(
            "debugMpmPov",
            CATEGORY_DEBUG,
            debugMpmPov,
            "Enable one focused MPM camera/targeting ownership sample every five seconds."
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
        logger.info("Combatives config: player motion camera freefall={}/{}, inertia={}/{}, collision={}/{}", enablePlayerFreefallCamera, playerFreefallCameraStrength, enablePlayerInertiaCamera, playerInertiaCameraStrength, enablePlayerCollisionCamera, playerCollisionCameraStrength);
        logger.info("Combatives config: enableExplosionCameraFeedback={}", enableExplosionCameraFeedback);
        logger.info("Combatives config: explosionFeedbackStrength={}", explosionFeedbackStrength);
        logger.info("Combatives config: horse camera={}/{}, terrain={}, landing={}, turning={}", enableHorseCamera, horseCameraAmplitude, horseTerrainImpulse, horseLanding, horseTurningRoll);
        logger.info("Combatives config: crawl camera={}/{}, transitionMillis={}", enableCrawlCamera, crawlCameraAmplitude, crawlTransitionMillis);
        logger.info("Combatives config: debugMovement={}", debugMovement);
        logger.info("Combatives config: verboseMovementDebug={}", verboseMovementDebug);
        logger.info("Combatives config: debugCamera={}", debugCamera);
        logger.info("Combatives config: verboseCameraDebug={}", verboseCameraDebug);
        logger.info("Combatives config: debugMpmPov={}", debugMpmPov);
        logger.info("Combatives config: enableMpmHitboxScaling={}", enableMpmHitboxScaling);
    }
}
