package com.glowingfederal.combatives.entity;

public final class EntitySize {
    public final float width;
    public final float height;
    public final boolean fixed;

    public EntitySize(float width, float height, boolean fixed) {
        this.width = width;
        this.height = height;
        this.fixed = fixed;
    }

    public static EntitySize flexible(float width, float height) {
        return new EntitySize(width, height, false);
    }

    public static EntitySize fixed(float width, float height) {
        return new EntitySize(width, height, true);
    }
}
