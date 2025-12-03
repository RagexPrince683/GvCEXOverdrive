package handmadeguns.gunsmithing;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.*;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;

public class GunSmithNetwork {

    public static final SimpleNetworkWrapper CHANNEL =
            NetworkRegistry.INSTANCE.newSimpleChannel("GunSmithNet");

    // ✅ Call ONCE in init()
    public static void init() {
        CHANNEL.registerMessage(
                CraftGunMessage.Handler.class,
                CraftGunMessage.class,
                0,
                Side.SERVER
        );
    }

    // ✅ GUI → Server
    public static void sendCraftRequestToServer(int recipeIndex) {
        CHANNEL.sendToServer(new CraftGunMessage(recipeIndex));
    }

    // ============================================================
    // ✅ PACKET
    // ============================================================

    public static class CraftGunMessage implements IMessage {

        private int recipeIndex;

        public CraftGunMessage() {}

        public CraftGunMessage(int recipeIndex) {
            this.recipeIndex = recipeIndex;
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeInt(recipeIndex);
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            recipeIndex = buf.readInt();
        }

        // ========================================================
        // ✅ SERVER HANDLER — 1.7.10 CORRECT
        // ========================================================

        public static class Handler implements IMessageHandler<CraftGunMessage, IMessage> {

            @Override
            public IMessage onMessage(CraftGunMessage msg, MessageContext ctx) {

                // ✅ DIRECT EXECUTION — THIS IS CORRECT FOR 1.7.10
                EntityPlayerMP player = ctx.getServerHandler().playerEntity;

                GunSmithingCraftHandler.handleCraft(player, msg.recipeIndex);

                return null;
            }
        }
    }
}
