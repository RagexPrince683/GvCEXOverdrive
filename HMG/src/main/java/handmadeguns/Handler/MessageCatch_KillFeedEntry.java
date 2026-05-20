package handmadeguns.Handler;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import handmadeguns.network.PacketKillFeedEntry;

public class MessageCatch_KillFeedEntry implements IMessageHandler<PacketKillFeedEntry, IMessage> {
    @Override
    public IMessage onMessage(PacketKillFeedEntry message, MessageContext ctx) {
        handmadeguns.HandmadeGunsCore.HMG_proxy.addKillFeedEntry(message.attackerName, message.victimName, message.weaponStack);
        return null;
    }
}
