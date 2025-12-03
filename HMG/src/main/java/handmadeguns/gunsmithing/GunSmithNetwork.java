package handmadeguns.gunsmithing;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.*;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;

public class GunSmithNetwork {

    public static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel("GunSmithNet");

    public static void init() {
        // gun craft (server)
        CHANNEL.registerMessage(CraftGunMessage.Handler.class, CraftGunMessage.class, 0, Side.SERVER);
        // ammo craft (server)
        CHANNEL.registerMessage(CraftAmmoMessage.Handler.class, CraftAmmoMessage.class, 1, Side.SERVER);
    }

    // client -> server
    public static void sendCraftRequestToServer(int recipeIndex) {
        CHANNEL.sendToServer(new CraftGunMessage(recipeIndex));
    }

    // ammo: client -> server
    public static void sendAmmoCraftRequestToServer(int recipeIndex) {
        CHANNEL.sendToServer(new CraftAmmoMessage(recipeIndex));
    }

    // ---------------- CraftGunMessage ----------------
    public static class CraftGunMessage implements IMessage {
        private int recipeIndex;
        public CraftGunMessage() {}
        public CraftGunMessage(int recipeIndex) { this.recipeIndex = recipeIndex; }

        @Override public void toBytes(ByteBuf buf) { buf.writeInt(recipeIndex); }
        @Override public void fromBytes(ByteBuf buf) { recipeIndex = buf.readInt(); }

        public static class Handler implements IMessageHandler<CraftGunMessage, IMessage> {
            @Override
            public IMessage onMessage(CraftGunMessage msg, MessageContext ctx) {
                EntityPlayerMP player = ctx.getServerHandler().playerEntity;
                GunSmithingCraftHandler.handleCraft(player, msg.recipeIndex);
                return null;
            }
        }
    }

    // ---------------- CraftAmmoMessage ----------------
    public static class CraftAmmoMessage implements IMessage {
        private int recipeIndex;
        public CraftAmmoMessage() {}
        public CraftAmmoMessage(int recipeIndex) { this.recipeIndex = recipeIndex; }

        @Override public void toBytes(ByteBuf buf) { buf.writeInt(recipeIndex); }
        @Override public void fromBytes(ByteBuf buf) { recipeIndex = buf.readInt(); }

        public static class Handler implements IMessageHandler<CraftAmmoMessage, IMessage> {
            @Override
            public IMessage onMessage(CraftAmmoMessage msg, MessageContext ctx) {
                EntityPlayerMP player = ctx.getServerHandler().playerEntity;
                GunSmithingCraftHandler.handleAmmoCraft(player, msg.recipeIndex);
                return null;
            }
        }
    }
}
