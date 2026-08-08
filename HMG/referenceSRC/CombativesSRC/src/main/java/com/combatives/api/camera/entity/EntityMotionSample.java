package com.combatives.api.camera.entity;

/** Immutable tick observation, with a render-interpolated position and orientation. */
public final class EntityMotionSample {
    public static final EntityMotionSample EMPTY = new EntityMotionSample(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, true, 0, 0, 0, 0, 0, 0, 0, 0);
    private final long tick;
    private final double x, y, z, velocityX, velocityY, velocityZ, previousVelocityX, previousVelocityY, previousVelocityZ;
    private final double accelerationX, accelerationY, accelerationZ, previousAccelerationX, previousAccelerationY, previousAccelerationZ;
    private final float yaw, pitch, angularYaw, angularPitch;
    private final boolean discontinuity;
    private final double forwardVelocity, lateralVelocity, verticalVelocity;
    private final double forwardAcceleration, lateralAcceleration, verticalAcceleration, horizontalSpeed, totalSpeed;

    /** Legacy world-space constructor retained for API compatibility. */
    public EntityMotionSample(long tick, double x, double y, double z, double velocityX, double velocityY, double velocityZ,
            double previousVelocityX, double previousVelocityY, double previousVelocityZ, double accelerationX, double accelerationY,
            double accelerationZ, double previousAccelerationX, double previousAccelerationY, double previousAccelerationZ,
            float yaw, float pitch, float angularYaw, float angularPitch, boolean discontinuity) {
        this(tick,x,y,z,velocityX,velocityY,velocityZ,previousVelocityX,previousVelocityY,previousVelocityZ,accelerationX,accelerationY,
            accelerationZ,previousAccelerationX,previousAccelerationY,previousAccelerationZ,yaw,pitch,angularYaw,angularPitch,discontinuity,
            0,0,velocityY,0,0,accelerationY,Math.sqrt(velocityX*velocityX+velocityZ*velocityZ),Math.sqrt(velocityX*velocityX+velocityY*velocityY+velocityZ*velocityZ));
    }
    public EntityMotionSample(long tick, double x, double y, double z, double velocityX, double velocityY, double velocityZ,
            double previousVelocityX, double previousVelocityY, double previousVelocityZ, double accelerationX, double accelerationY,
            double accelerationZ, double previousAccelerationX, double previousAccelerationY, double previousAccelerationZ,
            float yaw, float pitch, float angularYaw, float angularPitch, boolean discontinuity,
            double forwardVelocity, double lateralVelocity, double verticalVelocity, double forwardAcceleration,
            double lateralAcceleration, double verticalAcceleration, double horizontalSpeed, double totalSpeed) {
        this.tick=tick; this.x=x; this.y=y; this.z=z; this.velocityX=velocityX; this.velocityY=velocityY; this.velocityZ=velocityZ;
        this.previousVelocityX=previousVelocityX; this.previousVelocityY=previousVelocityY; this.previousVelocityZ=previousVelocityZ;
        this.accelerationX=accelerationX; this.accelerationY=accelerationY; this.accelerationZ=accelerationZ;
        this.previousAccelerationX=previousAccelerationX; this.previousAccelerationY=previousAccelerationY; this.previousAccelerationZ=previousAccelerationZ;
        this.yaw=yaw; this.pitch=pitch; this.angularYaw=angularYaw; this.angularPitch=angularPitch; this.discontinuity=discontinuity;
        this.forwardVelocity=forwardVelocity; this.lateralVelocity=lateralVelocity; this.verticalVelocity=verticalVelocity;
        this.forwardAcceleration=forwardAcceleration; this.lateralAcceleration=lateralAcceleration; this.verticalAcceleration=verticalAcceleration;
        this.horizontalSpeed=horizontalSpeed; this.totalSpeed=totalSpeed;
    }
    public long getTick() { return tick; } public double getX(){return x;} public double getY(){return y;} public double getZ(){return z;}
    public double getVelocityX(){return velocityX;} public double getVelocityY(){return velocityY;} public double getVelocityZ(){return velocityZ;}
    public double getPreviousVelocityX(){return previousVelocityX;} public double getPreviousVelocityY(){return previousVelocityY;} public double getPreviousVelocityZ(){return previousVelocityZ;}
    public double getAccelerationX(){return accelerationX;} public double getAccelerationY(){return accelerationY;} public double getAccelerationZ(){return accelerationZ;}
    public double getPreviousAccelerationX(){return previousAccelerationX;} public double getPreviousAccelerationY(){return previousAccelerationY;} public double getPreviousAccelerationZ(){return previousAccelerationZ;}
    public float getYaw(){return yaw;} public float getPitch(){return pitch;} public float getAngularYaw(){return angularYaw;} public float getAngularPitch(){return angularPitch;}
    public boolean isDiscontinuity(){return discontinuity;}
    public double getForwardVelocity(){return forwardVelocity;} public double getLateralVelocity(){return lateralVelocity;} public double getVerticalVelocity(){return verticalVelocity;}
    public double getForwardAcceleration(){return forwardAcceleration;} public double getLateralAcceleration(){return lateralAcceleration;} public double getVerticalAcceleration(){return verticalAcceleration;}
    public double getHorizontalSpeed(){return horizontalSpeed;} public double getTotalSpeed(){return totalSpeed;}
    public float getYawRate(){return angularYaw;} public float getPitchRate(){return angularPitch;}
}
