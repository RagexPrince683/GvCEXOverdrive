package com.glowingfederal.combatives.config;

public final class CombativesConfigDefaults {
    public static final boolean ENABLE_COMBATIVES_CAMERA = true;
    public static final boolean ENABLE_PROCEDURAL_BOB = true;
    public static final boolean ENABLE_MOVEMENT_LEAN = true;
    public static final boolean ENABLE_MOVEMENT_FOV = true;
    public static final boolean ENABLE_CAMERA_ROTATIONS = true;
    public static final boolean ENABLE_CAMERA_SHAKE = true;
    public static final float MAX_CAMERA_YAW_DEGREES = 4.0F;
    public static final boolean ENABLE_MOUSE_DELTA_CLAMP = false;
    public static final int MAX_MOUSE_DELTA = 80;
    public static final boolean ENABLE_LANDING_CAMERA_FEEDBACK = true;
    public static final double LANDING_FEEDBACK_STRENGTH = 1.0D;
    public static final boolean ENABLE_PLAYER_FREEFALL_CAMERA = true;
    public static final double PLAYER_FREEFALL_CAMERA_STRENGTH = 1.0D;
    public static final boolean ENABLE_PLAYER_INERTIA_CAMERA = true;
    public static final double PLAYER_INERTIA_CAMERA_STRENGTH = 1.0D;
    public static final boolean ENABLE_PLAYER_COLLISION_CAMERA = true;
    public static final double PLAYER_COLLISION_CAMERA_STRENGTH = 1.0D;
    public static final boolean ENABLE_EXPLOSION_CAMERA_FEEDBACK = true;
    public static final double EXPLOSION_FEEDBACK_STRENGTH = 1.0D;
    public static final boolean ENABLE_HORSE_CAMERA = true;
    public static final double HORSE_CAMERA_AMPLITUDE = 1.0D;
    public static final double HORSE_TERRAIN_IMPULSE = 1.0D;
    public static final double HORSE_LANDING = 1.0D;
    public static final double HORSE_TURNING_ROLL = 1.0D;
    public static final boolean ENABLE_CRAWL_CAMERA = false;
    public static final double CRAWL_CAMERA_AMPLITUDE = 1.0D;
    public static final int CRAWL_TRANSITION_MILLIS = 200;
    public static final boolean ENABLE_MPM_HITBOX_SCALING = true;
    public static final boolean DEBUG = false;
    public static final boolean VERBOSE_DEBUG = false;

    private CombativesConfigDefaults() {
    }
}
