package com.glowingfederal.combatives.mixin;

import com.glowingfederal.combatives.Combatives;
import com.glowingfederal.combatives.client.camera.CameraController;
import com.glowingfederal.combatives.client.camera.AuthoritativeViewRay;
import com.glowingfederal.combatives.client.camera.TargetingDiagnostics;
import com.glowingfederal.combatives.config.CombativesConfig;
import com.glowingfederal.combatives.compat.mcheli.MCHeliCameraCompat;
import com.glowingfederal.combatives.entity.Pose;
import com.glowingfederal.combatives.entity.player.ICombativesPlayerPose;
import com.glowingfederal.combatives.entity.player.EffectivePlayerGeometry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
    private float combatives$eyeHeight;
    private float combatives$previousEyeHeight;
    private float combatives$entityEyeHeight;
    private float combatives$partialTicks;
    private Pose combatives$lastLoggedPose;
    private boolean combatives$lastLoggedLowPose;
    private Entity combatives$lastLoggedMount;
    private double combatives$lastBaseCameraY = Double.NaN;

    @Inject(method = "getMouseOver", at = @At("HEAD"))
    private void combatives$diagnoseTargetingOrigin(float partialTicks, CallbackInfo ci) {
        TargetingDiagnostics.beforeTargeting(this, partialTicks);
        Entity view = Minecraft.getMinecraft().renderViewEntity;
        if (view instanceof EntityLivingBase) {
            AuthoritativeViewRay.beginTargeting((EntityLivingBase) view, partialTicks);
        }
    }

    @Inject(method = "getMouseOver", at = @At("RETURN"))
    private void combatives$diagnoseTargetingResult(float partialTicks, CallbackInfo ci) {
        TargetingDiagnostics.afterTargeting();
        AuthoritativeViewRay.endTargeting();
    }

    @Redirect(method = "getMouseOver", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;getPosition(F)Lnet/minecraft/util/Vec3;"))
    private net.minecraft.util.Vec3 combatives$captureConsumedTargetOrigin(EntityLivingBase entity, float partialTicks) {
        net.minecraft.util.Vec3 origin = AuthoritativeViewRay.origin(entity, entity.getPosition(partialTicks));
        TargetingDiagnostics.captureActualTargetOrigin(entity, partialTicks, origin);
        return origin;
    }

    @Redirect(method = "getMouseOver", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;getLook(F)Lnet/minecraft/util/Vec3;"))
    private net.minecraft.util.Vec3 combatives$captureConsumedTargetLook(EntityLivingBase entity, float partialTicks) {
        net.minecraft.util.Vec3 look = AuthoritativeViewRay.direction(entity, entity.getLook(partialTicks));
        TargetingDiagnostics.captureActualTargetLook(look);
        return look;
    }

    @Inject(method = "orientCamera", at = @At("HEAD"))
    private void combatives$capturePartialTicks(float partialTicks, CallbackInfo ci) {
        this.combatives$partialTicks = partialTicks;
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        if (player != null) {
            CameraController.INSTANCE.update(mc, player, partialTicks);
        } else {
            CameraController.INSTANCE.reset();
        }
    }

    @Inject(method = "orientCamera", at = @At("TAIL"))
    private void combatives$applyCameraTransforms(float partialTicks, CallbackInfo ci) {
        CameraController.INSTANCE.applyTransforms(partialTicks);
        Entity entity = Minecraft.getMinecraft().renderViewEntity;
        if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            double interpolatedPosY = player.prevPosY + (player.posY - player.prevPosY) * (double) partialTicks;
            double cameraY = interpolatedPosY - this.combatives$entityEyeHeight;
            AuthoritativeViewRay.captureCameraBase(player, partialTicks, cameraY);
            TargetingDiagnostics.logRenderedCamera(player, partialTicks, cameraY);
        }
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

        EntityPlayer player = (EntityPlayer) entity;
        float calculatedPoseOffset = this.combatives$getPoseCameraOffset(player, eyeHeight);
        boolean mcheliCamera = MCHeliCameraCompat.ownsCamera(player);
        float selectedCameraOffset = player.isRiding() || mcheliCamera ? eyeHeight : calculatedPoseOffset;
        this.combatives$entityEyeHeight = selectedCameraOffset;

        this.combatives$eyeHeight = selectedCameraOffset;
        this.combatives$previousEyeHeight = selectedCameraOffset;
        this.combatives$logCameraOrigin(player, eyeHeight, calculatedPoseOffset, selectedCameraOffset);
        TargetingDiagnostics.captureActualCameraOrigin(player, this.combatives$partialTicks, selectedCameraOffset);
        return selectedCameraOffset;
    }

    private float combatives$getPoseCameraOffset(EntityPlayer player, float vanillaCameraOffset) {
        if (!(player instanceof ICombativesPlayerPose)) {
            return vanillaCameraOffset;
        }

        EffectivePlayerGeometry geometry =
                ((ICombativesPlayerPose) player).getEffectiveGeometry();

        double interpolatedPosY =
                player.prevPosY
                        + (player.posY - player.prevPosY)
                        * (double) this.combatives$partialTicks;

        double interpolatedMinY =
                interpolatedPosY
                        + (player.boundingBox.minY - player.posY);

        return (float) (
                interpolatedPosY
                        - (interpolatedMinY + geometry.eyeAboveMinY)
        );
    }

    private float combatives$getLowPoseCameraEyeHeight(EntityPlayer player, float vanillaEyeHeight) {
        if (player.height <= 0.61F) {
            return 0.0F;
        }
        return player.getEyeHeight() > 0.0F ? player.getEyeHeight() : vanillaEyeHeight;
    }

    private boolean combatives$isVanillaBaselinePose(EntityPlayer player) {
        if (!(player instanceof ICombativesPlayerPose)) {
            return true;
        }
        ICombativesPlayerPose pose = (ICombativesPlayerPose) player;
        return pose.getPose() == Pose.STANDING && !pose.isSwimming() && !pose.isCrawlKeyDown() && !pose.isActuallySwimming();
    }

    private boolean combatives$isLowPose(EntityPlayer player) {
        if (!(player instanceof ICombativesPlayerPose)) {
            return false;
        }
        ICombativesPlayerPose pose = (ICombativesPlayerPose) player;
        return pose.getPose() == Pose.SWIMMING || pose.isSwimming() || pose.isCrawlKeyDown() || pose.isActuallySwimming();
    }

    private void combatives$logCameraOrigin(EntityPlayer player, float incomingCameraOffset,
                                             float calculatedPoseOffset, float selectedCameraOffset) {
        if (!CombativesConfig.debugCamera || Combatives.logger == null) {
            return;
        }
        Pose pose = player instanceof ICombativesPlayerPose ? ((ICombativesPlayerPose) player).getPose() : Pose.STANDING;
        boolean lowPose = this.combatives$isLowPose(player);
        double interpolatedPosY = player.prevPosY + (player.posY - player.prevPosY) * (double) this.combatives$partialTicks;
        double vanillaBaseCameraY = interpolatedPosY - incomingCameraOffset;
        double baseCameraY = interpolatedPosY - selectedCameraOffset;
        float proceduralTranslationY = CameraController.INSTANCE.getLastTranslationY();
        double finalCameraY = baseCameraY + proceduralTranslationY;
        boolean ownershipChanged = this.combatives$lastLoggedMount != player.ridingEntity;
        boolean poseChanged = this.combatives$lastLoggedPose != pose || this.combatives$lastLoggedLowPose != lowPose;

        if (poseChanged || ownershipChanged) {
            Combatives.logger.info(
                    "Combatives camera origin: riderClass={} ridingEntityClass={} pose={} partialTicks={} posY={} interpolatedPosY={} boundingBoxMinY={} yOffset={} getEyeHeight={} effectiveEyeAboveMinY={} incomingCameraOffset={} calculatedPoseOffset={} selectedCameraOffset={} owner={} baseCameraY={} proceduralTranslationY={} finalCameraY={}",
                    player.getClass().getName(),
                    player.ridingEntity == null ? "none" : player.ridingEntity.getClass().getName(),
                    pose,
                    this.combatives$partialTicks,
                    player.posY,
                    interpolatedPosY,
                    player.boundingBox.minY,
                    player.yOffset,
                    player.getEyeHeight(),
                    player instanceof ICombativesPlayerPose
                            ? ((ICombativesPlayerPose) player).getEffectiveGeometry().eyeAboveMinY
                            : Float.NaN,
                    incomingCameraOffset,
                    calculatedPoseOffset,
                    selectedCameraOffset,
                    MCHeliCameraCompat.ownsCamera(player) ? "MCHELI"
                            : player.isRiding() ? "MOUNT" : "COMBATIVES_POSE",
                    baseCameraY,
                    proceduralTranslationY,
                    finalCameraY
            );
            if (!Double.isNaN(this.combatives$lastBaseCameraY) && Math.abs(baseCameraY - this.combatives$lastBaseCameraY) < 1.0E-4D) {
                Combatives.logger.warn("Combatives camera origin warning: pose changed but base camera Y did not change; previousBaseCameraY={} currentBaseCameraY={} previousPose={} currentPose={}", this.combatives$lastBaseCameraY, baseCameraY, this.combatives$lastLoggedPose, pose);
            }
            this.combatives$lastLoggedPose = pose;
            this.combatives$lastLoggedLowPose = lowPose;
            this.combatives$lastLoggedMount = player.ridingEntity;
            this.combatives$lastBaseCameraY = baseCameraY;
        }

        if (this.combatives$isVanillaBaselinePose(player) && Math.abs(baseCameraY - vanillaBaseCameraY) > 1.0E-4D) {
            Combatives.logger.warn("Combatives camera origin warning: STANDING base camera Y differs from incoming value; incomingBaseCameraY={} combativesBaseCameraY={} incomingOffset={} selectedOffset={}", vanillaBaseCameraY, baseCameraY, incomingCameraOffset, selectedCameraOffset);
        }
    }

    @Inject(method = "updateRenderer", at = @At("TAIL"))
    private void combatives$interpolateEyeHeight(CallbackInfo ci) {
        this.combatives$previousEyeHeight = this.combatives$eyeHeight;
        this.combatives$eyeHeight += (this.combatives$entityEyeHeight - this.combatives$eyeHeight) * 0.5F;
    }
}
