package com.glowingfederal.combatives.mixin;

import com.glowingfederal.combatives.Combatives;
import com.glowingfederal.combatives.config.CombativesConfig;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntitySetAnglesDiagnosticsMixin {
    private static final float COMBATIVES_LOOK_YAW_SPIKE_THRESHOLD = 90.0F;
    private static final float COMBATIVES_LOOK_PITCH_SPIKE_THRESHOLD = 45.0F;

    @Shadow public float rotationYaw;
    @Shadow public float rotationPitch;
    @Shadow public int ticksExisted;

    private float combatives$lookBeforeYaw;
    private float combatives$lookBeforePitch;
    private float combatives$lookRawYawDelta;
    private float combatives$lookRawPitchDelta;
    private float combatives$lookTickStartYaw;
    private float combatives$lookTickStartPitch;
    private int combatives$lookTick = Integer.MIN_VALUE;

    @Inject(method = "setAngles", at = @At("HEAD"))
    private void combatives$captureLookMutation(float yawDelta, float pitchDelta, CallbackInfo ci) {
        if (!((Object) this instanceof EntityPlayerSP)) {
            return;
        }
        this.combatives$lookBeforeYaw = this.rotationYaw;
        this.combatives$lookBeforePitch = this.rotationPitch;
        this.combatives$lookRawYawDelta = yawDelta;
        this.combatives$lookRawPitchDelta = pitchDelta;
        if (this.combatives$lookTick != this.ticksExisted) {
            this.combatives$lookTick = this.ticksExisted;
            this.combatives$lookTickStartYaw = this.rotationYaw;
            this.combatives$lookTickStartPitch = this.rotationPitch;
        }
    }

    @Inject(method = "setAngles", at = @At("RETURN"))
    private void combatives$logLookMutation(float yawDelta, float pitchDelta, CallbackInfo ci) {
        if (!((Object) this instanceof EntityPlayerSP)) {
            return;
        }
        float callYawDelta = this.rotationYaw - this.combatives$lookBeforeYaw;
        float callPitchDelta = this.rotationPitch - this.combatives$lookBeforePitch;
        float tickYawDelta = this.rotationYaw - this.combatives$lookTickStartYaw;
        float tickPitchDelta = this.rotationPitch - this.combatives$lookTickStartPitch;
        boolean spike = Math.abs(tickYawDelta) > COMBATIVES_LOOK_YAW_SPIKE_THRESHOLD || Math.abs(tickPitchDelta) > COMBATIVES_LOOK_PITCH_SPIKE_THRESHOLD;

        if (Combatives.logger == null || (!CombativesConfig.debugCamera && !spike)) {
            return;
        }

        Combatives.logger.info(
            "Combatives look mutation: tick={}, rawYawDelta={}, rawPitchDelta={}, beforeYaw={}, beforePitch={}, afterYaw={}, afterPitch={}, callYawDelta={}, callPitchDelta={}, tickYawDelta={}, tickPitchDelta={}",
            this.ticksExisted,
            this.combatives$lookRawYawDelta,
            this.combatives$lookRawPitchDelta,
            this.combatives$lookBeforeYaw,
            this.combatives$lookBeforePitch,
            this.rotationYaw,
            this.rotationPitch,
            callYawDelta,
            callPitchDelta,
            tickYawDelta,
            tickPitchDelta
        );

        if (spike) {
            Combatives.logger.warn(
                "Combatives look spike: tick={}, rawYawDelta={}, rawPitchDelta={}, callYawDelta={}, callPitchDelta={}, tickYawDelta={}, tickPitchDelta={}, stack={}",
                this.ticksExisted,
                this.combatives$lookRawYawDelta,
                this.combatives$lookRawPitchDelta,
                callYawDelta,
                callPitchDelta,
                tickYawDelta,
                tickPitchDelta,
                combatives$partialStack()
            );
        }
    }

    private static String combatives$partialStack() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        StringBuilder builder = new StringBuilder();
        int appended = 0;
        for (int i = 3; i < stack.length && appended < 8; i++) {
            if (builder.length() > 0) {
                builder.append(" <- ");
            }
            builder.append(stack[i].getClassName()).append('#').append(stack[i].getMethodName()).append(':').append(stack[i].getLineNumber());
            appended++;
        }
        return builder.toString();
    }
}
