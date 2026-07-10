package com.glowingfederal.combatives.mixin;

import com.glowingfederal.combatives.client.ICombativesClientPlayerSwimming;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerControllerMP.class)
public abstract class PlayerControllerMPMixin {
    @Redirect(method = "onPlayerRightClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;isSneaking()Z"))
    private boolean combatives$useActualSneakForRightClick(EntityPlayer player) {
        return player instanceof ICombativesClientPlayerSwimming
            ? ((ICombativesClientPlayerSwimming) player).isActuallySneaking()
            : player.isSneaking();
    }
}
