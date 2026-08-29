package com.glowingfederal.combatives.util.math;

public final class MathHelperNew {
    private MathHelperNew() {
    }

    public static float lerp(float partialTicks, float start, float end) {
        return start + partialTicks * (end - start);
    }
}
