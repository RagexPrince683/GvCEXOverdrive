package com.glowingfederal.combatives.movement;

public final class MovementSnapshot {
    public static final MovementSnapshot EMPTY =
            new MovementSnapshot(
                    0.0, 0.0,
                    0.0, 0.0,
                    0.0, 0.0,
                    0.0, 0.0,
                    0.0, 0.0,   // wishX, wishZ
                    false, false, false, false,
                    false, false, false, false,
                    0.0, 0.0
            );

    public final double velocityX;
    public final double velocityZ;
    public final double previousVelocityX;
    public final double previousVelocityZ;
    public final double accelerationX;
    public final double accelerationZ;
    public final double speed;
    public final double speed01;
    public final double wishX;
    public final double wishZ;
    public final boolean grounded;
    public final boolean sprinting;
    public final boolean sneaking;
    public final boolean crawling;
    public final boolean swimming;
    public final boolean underwater;
    public final boolean airborne;
    public final boolean landingImpact;
    public final double turnAmount;
    public final double turnRate;

    public MovementSnapshot(double velocityX, double velocityZ, double previousVelocityX, double previousVelocityZ, double accelerationX, double accelerationZ,
            double speed, double speed01, double wishX, double wishZ, boolean grounded, boolean sprinting, boolean sneaking, boolean crawling,
            boolean swimming, boolean underwater, boolean airborne, boolean landingImpact, double turnAmount, double turnRate) {
        this.velocityX = velocityX;
        this.velocityZ = velocityZ;
        this.previousVelocityX = previousVelocityX;
        this.previousVelocityZ = previousVelocityZ;
        this.accelerationX = accelerationX;
        this.accelerationZ = accelerationZ;
        this.speed = speed;
        this.speed01 = speed01;
        this.wishX = wishX;
        this.wishZ = wishZ;
        this.grounded = grounded;
        this.sprinting = sprinting;
        this.sneaking = sneaking;
        this.crawling = crawling;
        this.swimming = swimming;
        this.underwater = underwater;
        this.airborne = airborne;
        this.landingImpact = landingImpact;
        this.turnAmount = turnAmount;
        this.turnRate = turnRate;
    }
}
