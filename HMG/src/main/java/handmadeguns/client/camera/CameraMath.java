package handmadeguns.client.camera;

import net.minecraft.util.MathHelper;

final class CameraMath {
    private CameraMath() {
    }

    static float clamp(float value, float min, float max) {
        return value < min ? min : (value > max ? max : value);
    }

    static float lerp(float from, float to, float amount) {
        amount = clamp(amount, 0.0F, 1.0F);
        return from + (to - from) * amount;
    }

    static float approach(float current, float target, float amount) {
        return lerp(current, target, amount);
    }

    static float wrapDegrees(float degrees) {
        return MathHelper.wrapAngleTo180_float(degrees);
    }
}
