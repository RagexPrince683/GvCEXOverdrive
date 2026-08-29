package com.combatives.api.camera.entity;

import net.minecraft.util.MathHelper;

/** Stateless calculations shared by integrations. */
public final class EntityBehaviorHelpers {
    public float wrapDegrees(float degrees) { return MathHelper.wrapAngleTo180_float(degrees); }
    public double clamp(double value, double minimum, double maximum) { return Math.max(minimum, Math.min(maximum, value)); }
}
