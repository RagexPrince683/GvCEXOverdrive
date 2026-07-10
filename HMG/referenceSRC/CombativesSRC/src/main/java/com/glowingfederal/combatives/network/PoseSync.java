package com.glowingfederal.combatives.network;

import com.glowingfederal.combatives.entity.EntitySize;
import com.glowingfederal.combatives.entity.Pose;
import com.glowingfederal.combatives.entity.player.ICombativesPlayerPose;
import com.glowingfederal.combatives.movement.MovementDiagnostics;
import com.glowingfederal.combatives.network.message.PacketPlayerPoseS2C;
import cpw.mods.fml.common.network.NetworkRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

public final class PoseSync {
    private PoseSync() {
    }

    public static void applyAuthoritativePose(EntityPlayer player, Pose pose, boolean swimming, boolean crawlKeyDown, String source) {
        if (!(player instanceof ICombativesPlayerPose)) {
            return;
        }
        ICombativesPlayerPose state = (ICombativesPlayerPose) player;
        Pose oldPose = state.getPose();
        boolean oldSwimming = state.isSwimming();
        boolean oldCrawlKeyDown = state.isCrawlKeyDown();
        boolean clientPrediction = "client".equals(source) && !player.worldObj.isRemote;
        boolean effectiveCrawlKeyDown = clientPrediction ? oldCrawlKeyDown : crawlKeyDown;
        boolean activeLowPose = (oldSwimming || oldCrawlKeyDown) && state.isPoseClear(Pose.SWIMMING);
        if (clientPrediction) {
            if (pose != Pose.SWIMMING && activeLowPose) {
                MovementDiagnostics.debug(player, "ignored client pose cancellation " + oldPose + " -> " + pose + "; preserving SWIMMING crawl=" + oldCrawlKeyDown + " swimming=" + oldSwimming);
            } else {
                MovementDiagnostics.debug(player, "ignored client pose prediction " + pose + "; server remains authoritative pose=" + oldPose + " crawl=" + oldCrawlKeyDown + " swimming=" + oldSwimming);
            }
            pose = activeLowPose ? Pose.SWIMMING : oldPose;
            swimming = oldSwimming;
            effectiveCrawlKeyDown = oldCrawlKeyDown;
        }
        state.setCrawlKeyDown(effectiveCrawlKeyDown);
        state.setSwimming(swimming);
        state.setPose(pose);
        state.recalculateSize();
        if (player.worldObj.isRemote) {
            player.func_145781_i(28);
            MovementDiagnostics.verbose(player, "authoritative pose marked render dirty from " + source + ": dataWatcherPose=" + state.getPose());
        }
        if (oldPose != pose || oldSwimming != swimming || oldCrawlKeyDown != effectiveCrawlKeyDown) {
            MovementDiagnostics.debug(player, "authoritative pose applied from " + source + ": " + pose + " swimming=" + swimming + " crawl=" + effectiveCrawlKeyDown);
        }
    }

    public static void broadcastAuthoritativePose(EntityPlayerMP player, boolean includeSelf) {
        if (!(player instanceof ICombativesPlayerPose) || NetworkHandler.channel == null) {
            return;
        }
        ICombativesPlayerPose state = (ICombativesPlayerPose) player;
        PacketPlayerPoseS2C packet = new PacketPlayerPoseS2C(player.getEntityId(), state.getPose(), state.isSwimming(), state.isCrawlKeyDown());
        EntitySize size = state.getSize(state.getPose());
        NetworkHandler.channel.sendToAllAround(packet, new NetworkRegistry.TargetPoint(player.dimension, player.posX, player.posY, player.posZ, 512.0D));
        if (includeSelf) {
            NetworkHandler.channel.sendTo(packet, player);
        }
        MovementDiagnostics.verbose(player, "broadcast authoritative pose " + state.getPose() + " size=" + size.width + "x" + size.height);
    }

    public static void sendAuthoritativePose(EntityPlayerMP target, Entity source) {
        if (!(source instanceof EntityPlayer) || !(source instanceof ICombativesPlayerPose) || NetworkHandler.channel == null) {
            return;
        }
        EntityPlayer player = (EntityPlayer) source;
        ICombativesPlayerPose state = (ICombativesPlayerPose) source;
        NetworkHandler.channel.sendTo(new PacketPlayerPoseS2C(player.getEntityId(), state.getPose(), state.isSwimming(), state.isCrawlKeyDown()), target);
        MovementDiagnostics.verbose(player, "sent authoritative pose to tracker " + target.getCommandSenderName());
    }
}
