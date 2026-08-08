package com.combatives.api.camera.entity;

import net.minecraft.entity.Entity;

/** A composable strategy used to select mounted entities. */
public interface EntityMatcher {
    boolean matches(Entity entity);
}
