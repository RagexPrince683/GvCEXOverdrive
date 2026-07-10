package com.glowingfederal.combatives.network;

import com.glowingfederal.combatives.entity.player.ICombativesPlayerPose;
import com.glowingfederal.combatives.movement.MovementDiagnostics;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.StartTracking;

public class PoseSyncEvents {
    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.entity instanceof EntityPlayerMP) {
            this.logPoseStateCheck((EntityPlayer) event.entity, "server login/runtime");
        } else if (event.entity instanceof EntityPlayer && event.world.isRemote) {
            this.logPoseStateCheck((EntityPlayer) event.entity, "client login/runtime");
        }
    }

    @SubscribeEvent
    public void onStartTracking(StartTracking event) {
        if (event.entityPlayer instanceof EntityPlayerMP) {
            PoseSync.sendAuthoritativePose((EntityPlayerMP) event.entityPlayer, event.target);
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        this.logPoseStateCheck(event.player, "server login");
        if (event.player instanceof EntityPlayerMP) {
            PoseSync.broadcastAuthoritativePose((EntityPlayerMP) event.player, true);
        }
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            PoseSync.broadcastAuthoritativePose((EntityPlayerMP) event.player, true);
        }
    }

    @SubscribeEvent
    public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            PoseSync.broadcastAuthoritativePose((EntityPlayerMP) event.player, true);
        }
    }

    private void logPoseStateCheck(EntityPlayer player, String source) {
        if (player == null) {
            MovementDiagnostics.verbose(source + " pose-state check skipped because player is null");
            return;
        }
        MovementDiagnostics.verbose(player, source + " pose-state check: instanceof ICombativesPlayerPose=" + (player instanceof ICombativesPlayerPose));
    }
}
