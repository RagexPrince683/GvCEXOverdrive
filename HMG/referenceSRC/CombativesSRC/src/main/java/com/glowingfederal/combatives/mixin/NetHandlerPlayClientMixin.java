package com.glowingfederal.combatives.mixin;

import com.glowingfederal.combatives.client.camera.CameraController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.S27PacketExplosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayClient.class)
public abstract class NetHandlerPlayClientMixin {
    @Inject(method = "func_147283_a", at = @At("TAIL"))
    private void combatives$addExplosionCameraFeedback(S27PacketExplosion packet, CallbackInfo ci) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc == null ? null : mc.thePlayer;
        if (player == null) {
            return;
        }

        CameraController.INSTANCE.addExplosionFeedback(
            player,
            packet.func_149148_f(),
            packet.func_149143_g(),
            packet.func_149145_h(),
            packet.func_149146_i()
        );
    }
}
