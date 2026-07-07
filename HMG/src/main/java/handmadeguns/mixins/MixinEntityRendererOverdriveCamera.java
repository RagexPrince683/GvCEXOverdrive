package handmadeguns.mixins;

import handmadeguns.HandmadeGunsCore;
import handmadeguns.client.camera.OverdriveCameraController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRendererOverdriveCamera {
    @Shadow private Minecraft mc;

    @Inject(method = "updateCameraAndRender(F)V", at = @At("HEAD"))
    private void hmgOverdrive$updateCamera(float partialTicks, CallbackInfo ci) {
        OverdriveCameraController.update(this.mc, partialTicks);
    }

    @Inject(method = "orientCamera(F)V", at = @At("RETURN"))
    private void hmgOverdrive$orientCamera(float partialTicks, CallbackInfo ci) {
        OverdriveCameraController.applyCameraRotations();
    }

    @Inject(method = "setupViewBobbing(F)V", at = @At("HEAD"), cancellable = true)
    private void hmgOverdrive$replaceBobbing(float partialTicks, CallbackInfo ci) {
        if (HandmadeGunsCore.cfg_ClientCamera_MasterEnabled
                && HandmadeGunsCore.cfg_ClientCamera_CustomBobEnabled
                && HandmadeGunsCore.cfg_ClientCamera_ReplaceVanillaBob) {
            OverdriveCameraController.applyCustomBob(partialTicks);
            ci.cancel();
        }
    }

    @Inject(method = "setupViewBobbing(F)V", at = @At("RETURN"))
    private void hmgOverdrive$additiveBobbing(float partialTicks, CallbackInfo ci) {
        if (HandmadeGunsCore.cfg_ClientCamera_MasterEnabled
                && HandmadeGunsCore.cfg_ClientCamera_CustomBobEnabled
                && !HandmadeGunsCore.cfg_ClientCamera_ReplaceVanillaBob) {
            OverdriveCameraController.applyCustomBob(partialTicks);
        }
    }

    @Inject(method = "getFOVModifier(FZ)F", at = @At("RETURN"), cancellable = true)
    private void hmgOverdrive$modifyFov(float partialTicks, boolean useFOVSetting, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(OverdriveCameraController.modifyFov(cir.getReturnValue(), partialTicks));
    }

    @Inject(method = "hurtCameraEffect(F)V", at = @At("HEAD"), cancellable = true)
    private void hmgOverdrive$replaceHurt(float partialTicks, CallbackInfo ci) {
        if (HandmadeGunsCore.cfg_ClientCamera_MasterEnabled && HandmadeGunsCore.cfg_ClientCamera_CustomHurtCameraEnabled && HandmadeGunsCore.cfg_ClientCamera_ReplaceVanillaHurtCamera) {
            OverdriveCameraController.applyHurtCamera(partialTicks);
            ci.cancel();
        }
    }

    @Inject(method = "hurtCameraEffect(F)V", at = @At("RETURN"))
    private void hmgOverdrive$supplementHurt(float partialTicks, CallbackInfo ci) {
        if (HandmadeGunsCore.cfg_ClientCamera_MasterEnabled && HandmadeGunsCore.cfg_ClientCamera_CustomHurtCameraEnabled && !HandmadeGunsCore.cfg_ClientCamera_ReplaceVanillaHurtCamera) {
            OverdriveCameraController.applyHurtCamera(partialTicks);
        }
    }
}
