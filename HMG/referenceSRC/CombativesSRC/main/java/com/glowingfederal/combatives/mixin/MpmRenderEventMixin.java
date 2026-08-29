package com.glowingfederal.combatives.mixin;

import com.glowingfederal.combatives.compat.mpm.MpmCompatibility;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderHandEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Restores vanilla's first-person item/arm pass after MPM suppresses entity disguises. */
@Pseudo
@Mixin(targets = "noppes.mpm.client.RenderEvent", remap = false)
public abstract class MpmRenderEventMixin {
    @Inject(method = "hand(Lnet/minecraftforge/client/event/RenderHandEvent;)V", at = @At("RETURN"), require = 0, remap = false)
    private void combatives$restoreDisguiseHand(RenderHandEvent event, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (event.isCanceled() && minecraft.thePlayer != null
                && MpmCompatibility.hasEntityDisguise(minecraft.thePlayer)) {
            event.setCanceled(false);
        }
    }
}
