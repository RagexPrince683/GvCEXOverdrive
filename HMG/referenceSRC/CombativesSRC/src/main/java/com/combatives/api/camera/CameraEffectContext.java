package com.combatives.api.camera;

import net.minecraft.entity.Entity;

public final class CameraEffectContext {
    private final Entity sourceEntity;
    private final boolean hasPosition;
    private final double x, y, z;
    private final float radius;
    private final Long seed;

    private CameraEffectContext(Entity sourceEntity, boolean hasPosition, double x, double y, double z, float radius, Long seed) {
        this.sourceEntity = sourceEntity; this.hasPosition = hasPosition; this.x = x; this.y = y; this.z = z; this.radius = radius; this.seed = seed;
    }

    public static CameraEffectContext none() { return new Builder().build(); }
    public static Builder builder() { return new Builder(); }
    public Entity getSourceEntity() { return sourceEntity; }
    public boolean hasPosition() { return hasPosition; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public float getRadius() { return radius; }
    public boolean hasSeed() { return seed != null; }
    public long getSeed() { return seed == null ? 0L : seed.longValue(); }

    public static final class Builder {
        private Entity sourceEntity; private boolean hasPosition; private double x, y, z; private float radius; private Long seed;
        public Builder sourceEntity(Entity entity) { this.sourceEntity = entity; return this; }
        public Builder position(double x, double y, double z) { this.hasPosition = true; this.x = x; this.y = y; this.z = z; return this; }
        public Builder radius(float radius) { this.radius = radius; return this; }
        public Builder seed(long seed) { this.seed = Long.valueOf(seed); return this; }
        public CameraEffectContext build() { return new CameraEffectContext(sourceEntity, hasPosition, x, y, z, radius, seed); }
    }
}
