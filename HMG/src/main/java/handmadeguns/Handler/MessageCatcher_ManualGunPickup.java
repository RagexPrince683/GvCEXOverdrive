package handmadeguns.Handler;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import handmadeguns.network.PacketManualGunPickup;
import handmadeguns.util.HMGManualGunPickup;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;

import static handmadeguns.HandmadeGunsCore.enableManualGunPickup;

public class MessageCatcher_ManualGunPickup implements IMessageHandler<PacketManualGunPickup, IMessage> {
    @Override
    public IMessage onMessage(PacketManualGunPickup message, MessageContext ctx) {
        if (!enableManualGunPickup || ctx == null || ctx.getServerHandler() == null) return null;

        EntityPlayerMP player = ctx.getServerHandler().playerEntity;
        if (player == null || player.worldObj == null || player.isDead) return null;

        Entity entity = player.worldObj.getEntityByID(message.entityId);
        if (!(entity instanceof EntityItem)) return null;

        HMGManualGunPickup.pickup(player, (EntityItem) entity);
        return null;
    }
}
