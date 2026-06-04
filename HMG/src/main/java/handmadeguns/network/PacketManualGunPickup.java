package handmadeguns.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

public class PacketManualGunPickup implements IMessage {
    public int entityId;

    public PacketManualGunPickup() {
    }

    public PacketManualGunPickup(int entityId) {
        this.entityId = entityId;
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeInt(entityId);
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        entityId = buffer.readInt();
    }
}
