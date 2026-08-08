package com.combatives.api.camera.entity;

import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

/** Stateful physical observation for one arbitrary entity; independent of player movement shaping. */
public final class EntityMotionSampler {
    private static final double TELEPORT_DISTANCE_SQUARED = 64.0D;
    private Entity entity;
    private long tick = Long.MIN_VALUE;
    private double x,y,z,vx,vy,vz,ax,ay,az,previousVx,previousVy,previousVz,previousAx,previousAy,previousAz;
    private float yaw,pitch,angularYaw,angularPitch;
    private boolean discontinuity = true;

    public void sampleTick(Entity observed, long clientTick) {
        if (observed == null) { reset(); return; }
        if (observed == entity && clientTick == tick) return;
        boolean first = observed != entity || tick == Long.MIN_VALUE;
        double dx=first?0:observed.posX-x, dy=first?0:observed.posY-y, dz=first?0:observed.posZ-z;
        long elapsed=first?1:Math.max(1L, clientTick-tick);
        discontinuity = first || dx*dx+dy*dy+dz*dz > TELEPORT_DISTANCE_SQUARED || elapsed > 1L;
        previousVx=vx; previousVy=vy; previousVz=vz; previousAx=ax; previousAy=ay; previousAz=az;
        if (discontinuity) { vx=vy=vz=ax=ay=az=previousVx=previousVy=previousVz=previousAx=previousAy=previousAz=0; angularYaw=angularPitch=0; }
        else { vx=dx/elapsed; vy=dy/elapsed; vz=dz/elapsed; ax=(vx-previousVx)/elapsed; ay=(vy-previousVy)/elapsed; az=(vz-previousVz)/elapsed; angularYaw=MathHelper.wrapAngleTo180_float(observed.rotationYaw-yaw)/elapsed; angularPitch=MathHelper.wrapAngleTo180_float(observed.rotationPitch-pitch)/elapsed; }
        entity=observed; tick=clientTick; x=observed.posX; y=observed.posY; z=observed.posZ; yaw=observed.rotationYaw; pitch=observed.rotationPitch;
    }
    public EntityMotionSample render(float partialTicks) {
        if (entity == null) return EntityMotionSample.EMPTY;
        float p=Math.max(0,Math.min(1,partialTicks));
        double rx=entity.prevPosX+(entity.posX-entity.prevPosX)*p, ry=entity.prevPosY+(entity.posY-entity.prevPosY)*p, rz=entity.prevPosZ+(entity.posZ-entity.prevPosZ)*p;
        float ryaw=entity.prevRotationYaw+MathHelper.wrapAngleTo180_float(entity.rotationYaw-entity.prevRotationYaw)*p;
        float rpitch=entity.prevRotationPitch+(entity.rotationPitch-entity.prevRotationPitch)*p;
        double radians=Math.toRadians(ryaw), sin=Math.sin(radians), cos=Math.cos(radians);
        double forward=-sin*vx+cos*vz, lateral=cos*vx+sin*vz;
        double forwardAcceleration=-sin*ax+cos*az, lateralAcceleration=cos*ax+sin*az;
        return new EntityMotionSample(tick,rx,ry,rz,vx,vy,vz,previousVx,previousVy,previousVz,ax,ay,az,previousAx,previousAy,previousAz,
            ryaw,rpitch,angularYaw,angularPitch,discontinuity,forward,lateral,vy,forwardAcceleration,lateralAcceleration,ay,
            Math.sqrt(vx*vx+vz*vz),Math.sqrt(vx*vx+vy*vy+vz*vz));
    }
    public void reset() { entity=null; tick=Long.MIN_VALUE; discontinuity=true; }
}
