package com.glowingfederal.combatives.client;

import com.glowingfederal.combatives.entity.player.ICombativesPlayerPose;
import com.glowingfederal.combatives.movement.MovementDiagnostics;
import com.glowingfederal.combatives.network.NetworkHandler;
import com.glowingfederal.combatives.network.message.PacketCrawlKeyState;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

public class ClientMovementInputHandler {
    private boolean lastCrawlDown;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || Minecraft.getMinecraft().thePlayer == null || CombativesKeyBindings.crawl == null) {
            return;
        }
        boolean crawlDown = CombativesKeyBindings.crawl.getIsKeyPressed();
        if (crawlDown == this.lastCrawlDown) {
            return;
        }
        this.lastCrawlDown = crawlDown;
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        MovementDiagnostics.verbose(player, "crawl key " + (crawlDown ? "pressed" : "released"));
        if (!crawlDown) {
            MovementDiagnostics.verbose(player, "crawl key released: debounce reset only");
            return;
        }
        if (player instanceof ICombativesPlayerPose) {
            ICombativesPlayerPose pose = (ICombativesPlayerPose) player;
            pose.setCrawlKeyDown(!pose.isCrawlKeyDown());
            MovementDiagnostics.verbose(player, "client predicted crawl toggle: " + pose.isCrawlKeyDown());
        }
        MovementDiagnostics.debug(player, "client sends crawl toggle request");
        if (NetworkHandler.channel == null) {
            MovementDiagnostics.warn(player, "client crawl packet send skipped because network channel is not initialized");
            return;
        }
        NetworkHandler.channel.sendToServer(new PacketCrawlKeyState());
    }
}
