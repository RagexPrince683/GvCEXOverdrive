package com.glowingfederal.combatives.network.message;

import com.glowingfederal.combatives.entity.Pose;
import com.glowingfederal.combatives.entity.player.ICombativesPlayerPose;
import com.glowingfederal.combatives.movement.MovementDiagnostics;
import com.glowingfederal.combatives.network.PoseSync;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;

public class PacketCrawlKeyState implements IMessage {
    public PacketCrawlKeyState() {
    }

    /**
     * Compatibility constructor for older call sites; packet semantics are always "toggle requested".
     */
    public PacketCrawlKeyState(boolean ignored) {
    }

    @Override
    public void fromBytes(ByteBuf buf) {
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    public static class Handler implements IMessageHandler<PacketCrawlKeyState, IMessage> {
        @Override
        public IMessage onMessage(PacketCrawlKeyState message, MessageContext ctx) {
            MovementDiagnostics.debug("server received crawl toggle request");
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            if (player == null) {
                MovementDiagnostics.warn("server could not resolve player for crawl toggle request");
                return null;
            }

            MovementDiagnostics.debug(player, "server player resolved for crawl toggle request");
            if (player instanceof ICombativesPlayerPose) {
                ICombativesPlayerPose pose = (ICombativesPlayerPose) player;
                boolean before = pose.isCrawlKeyDown();
                boolean next = !before;
                if (before && !next && !pose.isPoseClear(Pose.STANDING)) {
                    MovementDiagnostics.debug(player, "server crawl exit blocked: standing clearance unavailable");
                    PoseSync.broadcastAuthoritativePose(player, true);
                    return null;
                }
                MovementDiagnostics.debug(player, "server crawl " + before + " -> " + next);
                pose.setCrawlKeyDown(next);
                if (next && pose.isPoseClear(Pose.SWIMMING)) {
                    pose.setPose(Pose.SWIMMING);
                    MovementDiagnostics.debug(player, "crawl toggle selected SWIMMING pose immediately");
                }
                pose.recalculateSize();
                PoseSync.broadcastAuthoritativePose(player, true);
                MovementDiagnostics.debug(player, "server pose after crawl toggle: pose=" + pose.getPose() + " swimming=" + pose.isSwimming() + " crawl=" + pose.isCrawlKeyDown());
            } else {
                MovementDiagnostics.warn(player, "server player does not expose combatives pose state");
            }
            return null;
        }
    }
}
