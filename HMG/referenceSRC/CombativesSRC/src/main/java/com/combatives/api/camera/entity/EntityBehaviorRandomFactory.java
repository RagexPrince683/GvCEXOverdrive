package com.combatives.api.camera.entity;

import java.util.Random;

/** Creates repeatable, independently-owned random streams rather than sharing mutable random state. */
public final class EntityBehaviorRandomFactory {
    private final long worldSeed;
    public EntityBehaviorRandomFactory(long worldSeed) { this.worldSeed = worldSeed; }
    public Random create(String registrationId, long salt) {
        long id = registrationId == null ? 0L : registrationId.hashCode();
        return new Random(worldSeed ^ (id * 0x9E3779B97F4A7C15L) ^ salt);
    }
}
