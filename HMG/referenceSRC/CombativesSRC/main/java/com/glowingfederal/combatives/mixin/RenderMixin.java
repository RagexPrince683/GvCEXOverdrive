package com.glowingfederal.combatives.mixin;

import com.glowingfederal.combatives.entity.player.ICombativesPlayerPose;
import com.glowingfederal.combatives.movement.MovementDiagnostics;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Render.class)
public abstract class RenderMixin {
    @Inject(method = "func_147906_a", at = @At("HEAD"), cancellable = true)
    private void combatives$hideCrawlBaseLabel(Entity entity, String name, double x, double y, double z, int maxDistance, CallbackInfo ci) {
        if (!(entity instanceof EntityPlayer)) {
            MovementDiagnostics.verbose("Render#func_147906_a nameplate hook entity=" + entity.getClass().getName() + " player=false distance=" + maxDistance + " cancelAttempt=false");
            return;
        }

        EntityPlayer player = (EntityPlayer) entity;
        boolean crawl = entity instanceof ICombativesPlayerPose && ((ICombativesPlayerPose) entity).isCrawlKeyDown();
        boolean swim = entity instanceof ICombativesPlayerPose && ((ICombativesPlayerPose) entity).isSwimming();
        String pose = entity instanceof ICombativesPlayerPose ? String.valueOf(((ICombativesPlayerPose) entity).getPose()) : "unknown";
        MovementDiagnostics.verbose(player, "Render#func_147906_a nameplate hook entity=" + entity.getClass().getName() + " crawl=" + crawl + " swim=" + swim + " pose=" + pose + " distance=" + maxDistance + " cancelAttempt=" + crawl);
        if (crawl) {
            ci.cancel();
        }
    }
}
