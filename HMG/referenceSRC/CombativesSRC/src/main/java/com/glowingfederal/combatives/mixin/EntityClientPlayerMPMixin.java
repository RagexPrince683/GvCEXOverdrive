package com.glowingfederal.combatives.mixin;

import com.glowingfederal.combatives.client.ICombativesClientPlayerSwimming;
import net.minecraft.client.entity.EntityClientPlayerMP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityClientPlayerMP.class)
public abstract class EntityClientPlayerMPMixin {
    @Redirect(method = "sendMotionUpdates", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityClientPlayerMP;isSneaking()Z"))
    private boolean combatives$sendActualSneaking(EntityClientPlayerMP player) {
        return player instanceof ICombativesClientPlayerSwimming
            ? ((ICombativesClientPlayerSwimming) player).isActuallySneaking()
            : player.isSneaking();
    }
}
