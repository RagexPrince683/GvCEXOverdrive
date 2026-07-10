package com.glowingfederal.combatives.movement;

public enum MovementProfile {
    STANDING(1.0D, 0.180D, 0.240D, 0.240D, 0.930D, 1.0D),
    SPRINTING(1.0D, 0.150D, 0.210D, 0.220D, 0.945D, 1.0D),
    SNEAKING(1.0D, 0.060D, 0.075D, 0.080D, 0.975D, 1.0D),
    CRAWLING(0.95D, 0.024D, 0.038D, 0.050D, 0.965D, 1.0D),
    SWIMMING(1.0D, 0.026D, 0.032D, 0.045D, 0.940D, 1.0D),
    AIRBORNE(1.0D, 0.022D, 0.010D, 0.038D, 0.997D, 0.65D);

    public final double maxSpeedMultiplier;
    public final double acceleration;
    public final double deceleration;
    public final double turnAcceleration;
    public final double frictionDrag;
    public final double airControlMultiplier;

    MovementProfile(double maxSpeedMultiplier, double acceleration, double deceleration, double turnAcceleration, double frictionDrag, double airControlMultiplier) {
        this.maxSpeedMultiplier = maxSpeedMultiplier;
        this.acceleration = acceleration;
        this.deceleration = deceleration;
        this.turnAcceleration = turnAcceleration;
        this.frictionDrag = frictionDrag;
        this.airControlMultiplier = airControlMultiplier;
    }
}
