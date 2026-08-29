package com.glowingfederal.combatives.entity.player;

import java.util.EnumMap;
import java.util.Map;

import com.glowingfederal.combatives.entity.Pose;

/** The single table of physical player dimensions and gameplay eye heights. */
public final class PlayerGeometryResolver {
    private static final Map<Pose, EffectivePlayerGeometry> GEOMETRY =
            new EnumMap<Pose, EffectivePlayerGeometry>(Pose.class);

    static {
        put(Pose.STANDING, 0.6F, 1.8F, 1.62F);
        put(Pose.CROUCHING, 0.6F, 1.5F, 1.54F);
        put(Pose.SWIMMING, 0.6F, 0.6F, 0.28F);
        put(Pose.FALL_FLYING, 0.6F, 0.6F, 0.28F);
        put(Pose.SPIN_ATTACK, 0.6F, 0.6F, 0.28F);
        put(Pose.SLEEPING, 0.2F, 0.2F, 0.2F);
        put(Pose.DYING, 0.2F, 0.2F, 0.2F);
    }

    private PlayerGeometryResolver() { }

    private static void put(Pose pose, float width, float height, float eye) {
        GEOMETRY.put(pose, new EffectivePlayerGeometry(pose, width, height, eye));
    }

    public static EffectivePlayerGeometry resolve(Pose pose) {
        EffectivePlayerGeometry result = GEOMETRY.get(pose);
        return result == null ? GEOMETRY.get(Pose.STANDING) : result;
    }
}
