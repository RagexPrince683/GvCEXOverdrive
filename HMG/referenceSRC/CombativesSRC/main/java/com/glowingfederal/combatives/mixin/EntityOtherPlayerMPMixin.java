package com.glowingfederal.combatives.mixin;

import net.minecraft.client.entity.EntityOtherPlayerMP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityOtherPlayerMP.class)
public abstract class EntityOtherPlayerMPMixin {
    @Inject(method = "onUpdate", at = @At("TAIL"))
    private void combatives$applyRemoteYOffset(CallbackInfo ci) {
        EntityOtherPlayerMP player = (EntityOtherPlayerMP) (Object) this;
        player.yOffset = 0.0F;
    }
}
