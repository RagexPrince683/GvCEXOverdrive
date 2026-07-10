package com.glowingfederal.combatives.network.message;

import com.glowingfederal.combatives.entity.Pose;
import com.glowingfederal.combatives.network.PoseSync;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;

public class PacketPlayerPoseC2S implements IMessage {
    private Pose pose;
    private boolean swimming;
    private boolean crawlKeyDown;

    public PacketPlayerPoseC2S() {
    }

    public PacketPlayerPoseC2S(Pose pose, boolean swimming, boolean crawlKeyDown) {
        this.pose = pose;
        this.swimming = swimming;
        this.crawlKeyDown = crawlKeyDown;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int id = buf.readByte();
        this.pose = id >= 0 && id < Pose.values().length ? Pose.values()[id] : Pose.STANDING;
        this.swimming = buf.readBoolean();
        this.crawlKeyDown = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(this.pose == null ? Pose.STANDING.ordinal() : this.pose.ordinal());
        buf.writeBoolean(this.swimming);
        buf.writeBoolean(this.crawlKeyDown);
    }

    public static class Handler implements IMessageHandler<PacketPlayerPoseC2S, IMessage> {
        @Override
        public IMessage onMessage(PacketPlayerPoseC2S message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            PoseSync.applyAuthoritativePose(player, message.pose, message.swimming, message.crawlKeyDown, "client");
            return null;
        }
    }
}
