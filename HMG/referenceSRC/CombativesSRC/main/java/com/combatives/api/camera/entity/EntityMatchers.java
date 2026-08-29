package com.combatives.api.camera.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;

public final class EntityMatchers {
    private EntityMatchers() {}

    public static EntityMatcher exactClass(final Class<? extends Entity> type) {
        if (type == null) throw new IllegalArgumentException("type");
        return new EntityMatcher() { public boolean matches(Entity entity) { return entity != null && entity.getClass() == type; } };
    }

    public static EntityMatcher assignableClass(final Class<? extends Entity> type) {
        if (type == null) throw new IllegalArgumentException("type");
        return new EntityMatcher() { public boolean matches(Entity entity) { return entity != null && type.isAssignableFrom(entity.getClass()); } };
    }

    public static EntityMatcher registryId(final String registryId) {
        if (registryId == null || registryId.length() == 0) throw new IllegalArgumentException("registryId");
        return new EntityMatcher() { public boolean matches(Entity entity) { return entity != null && registryId.equals(EntityList.getEntityString(entity)); } };
    }

    /** Predicates are simply custom matcher implementations; this name documents that extension point. */
    public static EntityMatcher predicate(EntityMatcher predicate) {
        if (predicate == null) throw new IllegalArgumentException("predicate");
        return predicate;
    }
}
