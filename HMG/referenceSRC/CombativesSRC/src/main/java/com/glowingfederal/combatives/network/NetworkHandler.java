package com.glowingfederal.combatives.network;

import com.glowingfederal.combatives.Combatives;
import com.glowingfederal.combatives.network.message.PacketCrawlKeyState;
import com.glowingfederal.combatives.network.message.PacketPlayerPoseC2S;
import com.glowingfederal.combatives.network.message.PacketPlayerPoseS2C;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

public final class NetworkHandler {
    private static final int PACKET_CRAWL_KEY_STATE = 0;
    private static final int PACKET_PLAYER_POSE_C2S = 1;
    private static final int PACKET_PLAYER_POSE_S2C = 2;

    public static SimpleNetworkWrapper channel;

    private NetworkHandler() {
    }

    public static void register() {
        if (channel != null) {
            Combatives.logger.info("Combatives network channel already initialized: {}", Combatives.MOD_ID);
            return;
        }

        channel = NetworkRegistry.INSTANCE.newSimpleChannel(Combatives.MOD_ID);
        Combatives.logger.info("Combatives network channel initialized: {}", Combatives.MOD_ID);

        channel.registerMessage(PacketCrawlKeyState.Handler.class, PacketCrawlKeyState.class, PACKET_CRAWL_KEY_STATE, Side.SERVER);
        Combatives.logger.info("Combatives crawl packet registered for SERVER with discriminator {}", PACKET_CRAWL_KEY_STATE);

        channel.registerMessage(PacketPlayerPoseC2S.Handler.class, PacketPlayerPoseC2S.class, PACKET_PLAYER_POSE_C2S, Side.SERVER);
        channel.registerMessage(PacketPlayerPoseS2C.Handler.class, PacketPlayerPoseS2C.class, PACKET_PLAYER_POSE_S2C, Side.CLIENT);
    }
}
