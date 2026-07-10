package com.glowingfederal.combatives.mixin;

import com.glowingfederal.combatives.client.camera.CameraController;
import com.glowingfederal.combatives.config.CombativesConfig;
import com.glowingfederal.combatives.util.math.MathHelperNew;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
    private float combatives$eyeHeight;
    private float combatives$previousEyeHeight;
    private float combatives$entityEyeHeight;
    private float combatives$partialTicks;

    @Inject(method = "orientCamera", at = @At("HEAD"))
    private void combatives$capturePartialTicks(float partialTicks, CallbackInfo ci) {
        this.combatives$partialTicks = partialTicks;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer instanceof EntityPlayerSP) {
            CameraController.INSTANCE.update(mc, (EntityPlayerSP) mc.thePlayer, partialTicks);
        } else {
            CameraController.INSTANCE.reset();
        }
    }

    @Inject(method = "orientCamera", at = @At("TAIL"))
    private void combatives$applyCameraTransforms(float partialTicks, CallbackInfo ci) {
        CameraController.INSTANCE.applyTransforms(partialTicks);
    }

    @Inject(
            method = "renderHand",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/ItemRenderer;renderItemInFirstPerson(F)V"
            )
    )
    private void combatives$applyHandBobbing(float partialTicks, int pass, CallbackInfo ci) {
        CameraController.INSTANCE.applyHandTransforms(partialTicks);
    }

    @Inject(method = "setupViewBobbing", at = @At("HEAD"), cancellable = true)
    private void combatives$cancelVanillaViewBobbing(float partialTicks, CallbackInfo ci) {
        if (CombativesConfig.enableCombativesCamera && CombativesConfig.enableProceduralBob) {
            ci.cancel();
        }
    }

    @Inject(method = "getFOVModifier", at = @At("RETURN"), cancellable = true)
    private void combatives$applyMovementFov(float partialTicks, boolean useFOVSetting, CallbackInfoReturnable<Float> cir) {
        if (CombativesConfig.enableCombativesCamera && CombativesConfig.enableMovementFov) {
            cir.setReturnValue(cir.getReturnValue() * (1.0F + CameraController.INSTANCE.getFovModifier()));
        }
    }

    @ModifyVariable(
            method = "orientCamera",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/entity/EntityLivingBase;prevPosX:D",
                    ordinal = 0
            ),
            ordinal = 1
    )
    private float combatives$getInterpolatedEyeHeight(float eyeHeight) {
        Entity entity = Minecraft.getMinecraft().renderViewEntity;

        if (!(entity instanceof EntityPlayer)) {
            return eyeHeight;
        }

        this.combatives$entityEyeHeight = ((EntityPlayer) entity).getEyeHeight();

        return MathHelperNew.lerp(
                this.combatives$partialTicks,
                this.combatives$previousEyeHeight,
                this.combatives$eyeHeight
        );
    }

    @Inject(method = "updateRenderer", at = @At("TAIL"))
    private void combatives$interpolateEyeHeight(CallbackInfo ci) {
        this.combatives$previousEyeHeight = this.combatives$eyeHeight;
        this.combatives$eyeHeight += (this.combatives$entityEyeHeight - this.combatives$eyeHeight) * 0.5F;
    }
}
