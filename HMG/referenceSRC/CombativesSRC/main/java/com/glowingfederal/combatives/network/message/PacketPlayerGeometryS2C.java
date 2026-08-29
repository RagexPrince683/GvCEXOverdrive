package com.glowingfederal.combatives.network.message;

import com.glowingfederal.combatives.compat.mpm.MpmCompatibility;
import com.glowingfederal.combatives.entity.player.ICombativesPlayerPose;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

public class PacketPlayerGeometryS2C implements IMessage {
    private int entityId, rawSize, revision;
    private float widthScale, heightScale, eyeScale;
    private boolean fromMpm;
    public PacketPlayerGeometryS2C() { }
    public PacketPlayerGeometryS2C(int entityId, int revision, MpmCompatibility.Geometry geometry) {
        this.entityId = entityId; this.rawSize = geometry.rawSize;
        this.revision = revision;
        this.widthScale = geometry.widthScale; this.heightScale = geometry.heightScale;
        this.eyeScale = geometry.eyeScale; this.fromMpm = geometry.fromMpm;
    }
    @Override public void fromBytes(ByteBuf buf) {
        entityId = buf.readInt(); revision = buf.readInt(); rawSize = buf.readInt(); widthScale = buf.readFloat();
        heightScale = buf.readFloat(); eyeScale = buf.readFloat(); fromMpm = buf.readBoolean();
    }
    @Override public void toBytes(ByteBuf buf) {
        buf.writeInt(entityId); buf.writeInt(revision); buf.writeInt(rawSize); buf.writeFloat(widthScale);
        buf.writeFloat(heightScale); buf.writeFloat(eyeScale); buf.writeBoolean(fromMpm);
    }
    public static class Handler implements IMessageHandler<PacketPlayerGeometryS2C, IMessage> {
        @Override @SideOnly(Side.CLIENT)
        public IMessage onMessage(PacketPlayerGeometryS2C message, MessageContext ctx) {
            Entity entity = Minecraft.getMinecraft().theWorld == null ? null
                    : Minecraft.getMinecraft().theWorld.getEntityByID(message.entityId);
            if (entity instanceof EntityPlayer && entity instanceof ICombativesPlayerPose) {
                EntityPlayer player = (EntityPlayer) entity;
                MpmCompatibility.applyServerGeometry(player, new MpmCompatibility.Geometry(message.rawSize,
                        message.widthScale, message.heightScale, message.eyeScale, message.fromMpm, null));
                ICombativesPlayerPose state = (ICombativesPlayerPose) player;
                state.acceptGeometryRevision(message.revision);
                state.recalculateSize();
                state.logGeometry("CLIENT PLAYER GEOMETRY", "server geometry packet");
            }
            return null;
        }
    }
}
