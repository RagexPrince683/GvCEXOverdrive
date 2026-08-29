package com.glowingfederal.combatives.mixin;

import com.glowingfederal.combatives.server.ServerInteractionTarget;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.MovingObjectPosition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Makes START_DESTROY_BLOCK consume the server's copy of the shared ray. */
@Mixin(NetHandlerPlayServer.class)
public abstract class NetHandlerPlayServerMixin {
    @Shadow public EntityPlayerMP playerEntity;
    @Unique private MovingObjectPosition combatives$digTarget;
    @Unique private MovingObjectPosition combatives$useTarget;

    @Inject(method = "processPlayerDigging", at = @At("HEAD"))
    private void combatives$resolveDigTarget(C07PacketPlayerDigging packet, CallbackInfo ci) {
        this.combatives$digTarget = ServerInteractionTarget.resolveDigStart(this.playerEntity, packet);
    }

    @Inject(method = "processPlayerDigging", at = @At("RETURN"))
    private void combatives$clearDigTarget(C07PacketPlayerDigging packet, CallbackInfo ci) {
        this.combatives$digTarget = null;
    }

    @Redirect(method = "processPlayerDigging", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/play/client/C07PacketPlayerDigging;func_149505_c()I"))
    private int combatives$authoritativeX(C07PacketPlayerDigging packet) {
        return this.combatives$digTarget == null ? packet.func_149505_c() : this.combatives$digTarget.blockX;
    }

    @Redirect(method = "processPlayerDigging", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/play/client/C07PacketPlayerDigging;func_149503_d()I"))
    private int combatives$authoritativeY(C07PacketPlayerDigging packet) {
        return this.combatives$digTarget == null ? packet.func_149503_d() : this.combatives$digTarget.blockY;
    }

    @Redirect(method = "processPlayerDigging", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/play/client/C07PacketPlayerDigging;func_149502_e()I"))
    private int combatives$authoritativeZ(C07PacketPlayerDigging packet) {
        return this.combatives$digTarget == null ? packet.func_149502_e() : this.combatives$digTarget.blockZ;
    }

    @Inject(method = "processPlayerBlockPlacement", at = @At("HEAD"))
    private void combatives$resolveUseTarget(C08PacketPlayerBlockPlacement packet, CallbackInfo ci) {
        this.combatives$useTarget = ServerInteractionTarget.resolveBlockUse(this.playerEntity, packet);
    }

    @Inject(method = "processPlayerBlockPlacement", at = @At("RETURN"))
    private void combatives$clearUseTarget(C08PacketPlayerBlockPlacement packet, CallbackInfo ci) {
        this.combatives$useTarget = null;
    }

    @Redirect(method = "processPlayerBlockPlacement", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/play/client/C08PacketPlayerBlockPlacement;func_149576_c()I"))
    private int combatives$authoritativeUseX(C08PacketPlayerBlockPlacement packet) {
        return this.combatives$useTarget == null ? packet.func_149576_c() : this.combatives$useTarget.blockX;
    }

    @Redirect(method = "processPlayerBlockPlacement", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/play/client/C08PacketPlayerBlockPlacement;func_149571_d()I"))
    private int combatives$authoritativeUseY(C08PacketPlayerBlockPlacement packet) {
        return this.combatives$useTarget == null ? packet.func_149571_d() : this.combatives$useTarget.blockY;
    }

    @Redirect(method = "processPlayerBlockPlacement", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/play/client/C08PacketPlayerBlockPlacement;func_149570_e()I"))
    private int combatives$authoritativeUseZ(C08PacketPlayerBlockPlacement packet) {
        return this.combatives$useTarget == null ? packet.func_149570_e() : this.combatives$useTarget.blockZ;
    }

    @Redirect(method = "processPlayerBlockPlacement", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/play/client/C08PacketPlayerBlockPlacement;func_149568_f()I"))
    private int combatives$authoritativeUseFace(C08PacketPlayerBlockPlacement packet) {
        return this.combatives$useTarget == null ? packet.func_149568_f() : this.combatives$useTarget.sideHit;
    }

    @Redirect(method = "processPlayerBlockPlacement", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/play/client/C08PacketPlayerBlockPlacement;func_149573_h()F"))
    private float combatives$authoritativeUseHitX(C08PacketPlayerBlockPlacement packet) {
        return this.combatives$useTarget == null ? packet.func_149573_h()
                : (float) (this.combatives$useTarget.hitVec.xCoord - this.combatives$useTarget.blockX);
    }

    @Redirect(method = "processPlayerBlockPlacement", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/play/client/C08PacketPlayerBlockPlacement;func_149569_i()F"))
    private float combatives$authoritativeUseHitY(C08PacketPlayerBlockPlacement packet) {
        return this.combatives$useTarget == null ? packet.func_149569_i()
                : (float) (this.combatives$useTarget.hitVec.yCoord - this.combatives$useTarget.blockY);
    }

    @Redirect(method = "processPlayerBlockPlacement", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/play/client/C08PacketPlayerBlockPlacement;func_149575_j()F"))
    private float combatives$authoritativeUseHitZ(C08PacketPlayerBlockPlacement packet) {
        return this.combatives$useTarget == null ? packet.func_149575_j()
                : (float) (this.combatives$useTarget.hitVec.zCoord - this.combatives$useTarget.blockZ);
    }
}
