package com.combatives.api.camera.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

/** Immutable generic mount/rider state passed consistently to every provider callback. */
public final class MountCameraContext {
    private final EntityPlayer rider; private final Entity mount, previousMount; private final MountTransition transition;
    private final long clientTick; private final float partialTicks; private final EntityMotionSample motion;
    public MountCameraContext(EntityPlayer rider, Entity mount, Entity previousMount, MountTransition transition, long clientTick, float partialTicks, EntityMotionSample motion) {
        this.rider=rider; this.mount=mount; this.previousMount=previousMount; this.transition=transition; this.clientTick=clientTick; this.partialTicks=partialTicks; this.motion=motion;
    }
    public EntityPlayer getRider(){return rider;} public Entity getMount(){return mount;} public Entity getCurrentMount(){return mount;} public Entity getPreviousMount(){return previousMount;}
    public MountTransition getTransition(){return transition;} public long getClientTick(){return clientTick;} public float getPartialTicks(){return partialTicks;} public EntityMotionSample getMotion(){return motion;}
}
