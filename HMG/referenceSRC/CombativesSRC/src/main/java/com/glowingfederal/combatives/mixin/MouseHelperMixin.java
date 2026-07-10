package com.glowingfederal.combatives.mixin;

import com.glowingfederal.combatives.Combatives;
import com.glowingfederal.combatives.config.CombativesConfig;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MouseHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHelper.class)
public abstract class MouseHelperMixin {
    private static long combatives$lastMouseClampWarningMillis;

    @Shadow public int deltaX;
    @Shadow public int deltaY;

    @Inject(method = "mouseXYChange", at = @At("TAIL"))
    private void combatives$clampRawMouseDeltas(CallbackInfo ci) {
        if (!CombativesConfig.enableMouseDeltaClamp) {
            return;
        }

        int cap = Math.max(1, CombativesConfig.maxMouseDelta);
        int originalX = this.deltaX;
        int originalY = this.deltaY;
        int clampedX = MathHelper.clamp_int(originalX, -cap, cap);
        int clampedY = MathHelper.clamp_int(originalY, -cap, cap);

        if (originalX == clampedX && originalY == clampedY) {
            return;
        }

        this.deltaX = clampedX;
        this.deltaY = clampedY;

        int maxAbsOriginal = Math.max(Math.abs(originalX), Math.abs(originalY));
        boolean drastic = maxAbsOriginal >= cap * 4;
        long now = System.currentTimeMillis();
        boolean throttled = now - combatives$lastMouseClampWarningMillis >= 1000L;
        if (Combatives.logger != null && (CombativesConfig.verboseCameraDebug || (drastic && CombativesConfig.debugCamera && throttled))) {
            combatives$lastMouseClampWarningMillis = now;
            Combatives.logger.warn(
                "Combatives clamped abnormal raw mouse delta: deltaX={} -> {}, deltaY={} -> {}, cap={}",
                originalX,
                clampedX,
                originalY,
                clampedY,
                cap
            );
        }
    }
}
