package com.glowingfederal.combatives.mixin;

import com.glowingfederal.combatives.entity.player.ICombativesPlayerPose;
import com.glowingfederal.combatives.movement.MovementDiagnostics;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RendererLivingEntity.class)
public abstract class RendererLivingEntityMixin {
    @Inject(method = "passSpecialRender", at = @At("HEAD"), cancellable = true)
    private void combatives$hideCrawlSpecials(EntityLivingBase entity, double x, double y, double z, CallbackInfo ci) {
        this.combatives$debugAndCancelCrawlNameplate("RendererLivingEntity#passSpecialRender", entity, x * x + y * y + z * z, ci);
    }

    @Inject(method = "func_96449_a(Lnet/minecraft/entity/EntityLivingBase;DDDLjava/lang/String;FD)V", at = @At("HEAD"), cancellable = true)
    private void combatives$hideCrawlLivingLabel(EntityLivingBase entity, double x, double y, double z, String name, float scale, double distance, CallbackInfo ci) {
        this.combatives$debugAndCancelCrawlNameplate("RendererLivingEntity#func_96449_a", entity, distance, ci);
    }

    private void combatives$debugAndCancelCrawlNameplate(String hook, EntityLivingBase entity, double distance, CallbackInfo ci) {
        if (!(entity instanceof EntityPlayer)) {
            MovementDiagnostics.verbose(hook + " nameplate hook entity=" + entity.getClass().getName() + " player=false distance=" + distance + " cancelAttempt=false");
            return;
        }

        EntityPlayer player = (EntityPlayer) entity;
        boolean crawl = entity instanceof ICombativesPlayerPose && ((ICombativesPlayerPose) entity).isCrawlKeyDown();
        boolean swim = entity instanceof ICombativesPlayerPose && ((ICombativesPlayerPose) entity).isSwimming();
        String pose = entity instanceof ICombativesPlayerPose ? String.valueOf(((ICombativesPlayerPose) entity).getPose()) : "unknown";
        MovementDiagnostics.verbose(player, hook + " nameplate hook entity=" + entity.getClass().getName() + " crawl=" + crawl + " swim=" + swim + " pose=" + pose + " distance=" + distance + " cancelAttempt=" + crawl);
        if (crawl) {
            ci.cancel();
        }
    }
}
