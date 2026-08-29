package com.glowingfederal.combatives.server;

import com.glowingfederal.combatives.Combatives;
import com.glowingfederal.combatives.config.CombativesConfig;
import com.glowingfederal.combatives.entity.player.ICombativesPlayerPose;
import com.glowingfederal.combatives.interaction.InteractionRay;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.MovingObjectPosition;

/** Server-authoritative resolution for interaction packets which carry client coordinates. */
public final class ServerInteractionTarget {
    private ServerInteractionTarget() { }

    public static MovingObjectPosition resolveDigStart(
            EntityPlayerMP player,
            C07PacketPlayerDigging packet) {

        // 1.7.10 C07 digging status:
        // 0 = start digging
        // 1 = cancel digging
        // 2 = finish digging
        // 3 = drop item stack
        // 4 = drop item
        // 5 = release use item
        if (!(player instanceof ICombativesPlayerPose)
                || packet.func_149506_g() != 0) {
            return null;
        }

        InteractionRay ray = InteractionRay.authoritative(player);

        MovingObjectPosition hit = ray.traceBlocks(
                player,
                player.theItemInWorldManager.getBlockReachDistance());

        if (CombativesConfig.verboseMovementDebug && Combatives.logger != null) {
            Combatives.logger.info(
                    "SERVER INTERACTION RAY player={} tick={} geometryRevision={} pose={} "
                            + "position=[{},{},{}] aabb={} width={} height={} anchor={} "
                            + "yaw={} pitch={} origin={} direction={} reach={} rayBlock={} "
                            + "packetBlock=[{},{},{}] agreement={}",
                    player.getCommandSenderName(),
                    player.ticksExisted,
                    ray.geometryRevision,
                    ((ICombativesPlayerPose) player).getPose(),
                    player.posX,
                    player.posY,
                    player.posZ,
                    player.boundingBox,
                    player.width,
                    player.height,
                    ((ICombativesPlayerPose) player).getEffectiveGeometry().eyeAboveMinY,
                    player.rotationYaw,
                    player.rotationPitch,
                    ray.origin,
                    ray.direction,
                    player.theItemInWorldManager.getBlockReachDistance(),
                    format(hit),
                    packet.func_149505_c(),
                    packet.func_149503_d(),
                    packet.func_149502_e(),
                    agrees(hit, packet));
        }

        return hit;
    }

    public static MovingObjectPosition resolveBlockUse(EntityPlayerMP player,
            C08PacketPlayerBlockPlacement packet) {
        if (!(player instanceof ICombativesPlayerPose) || packet.func_149576_c() < 0) return null;
        InteractionRay ray = InteractionRay.authoritative(player);
        MovingObjectPosition hit = ray.traceBlocks(player,
                player.theItemInWorldManager.getBlockReachDistance());
        if (CombativesConfig.verboseMovementDebug && Combatives.logger != null) {
            Combatives.logger.info("SERVER USE RAY player={} tick={} geometryRevision={} pose={} origin={} direction={} reach={} rayBlock={} packetBlock=[{},{},{}]",
                    player.getCommandSenderName(), player.ticksExisted, ray.geometryRevision,
                    ((ICombativesPlayerPose) player).getPose(), ray.origin, ray.direction,
                    player.theItemInWorldManager.getBlockReachDistance(), format(hit),
                    packet.func_149576_c(), packet.func_149571_d(), packet.func_149570_e());
        }
        return hit != null && hit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK ? hit : null;
    }

    private static boolean agrees(MovingObjectPosition hit, C07PacketPlayerDigging packet) {
        return hit != null && hit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK
                && hit.blockX == packet.func_149505_c() && hit.blockY == packet.func_149503_d()
                && hit.blockZ == packet.func_149502_e();
    }

    private static String format(MovingObjectPosition hit) {
        return hit == null ? "null" : "[" + hit.blockX + "," + hit.blockY + "," + hit.blockZ + "]";
    }
}
