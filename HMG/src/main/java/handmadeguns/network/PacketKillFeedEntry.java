package handmadeguns.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;

import static cpw.mods.fml.common.network.ByteBufUtils.readItemStack;
import static cpw.mods.fml.common.network.ByteBufUtils.readUTF8String;
import static cpw.mods.fml.common.network.ByteBufUtils.writeItemStack;
import static cpw.mods.fml.common.network.ByteBufUtils.writeUTF8String;

public class PacketKillFeedEntry implements IMessage {
    public String attackerName;
    public String victimName;
    public ItemStack weaponStack;

    public PacketKillFeedEntry() {
    }

    public PacketKillFeedEntry(String attackerName, String victimName, ItemStack weaponStack) {
        this.attackerName = attackerName;
        this.victimName = victimName;
        this.weaponStack = weaponStack;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        attackerName = readUTF8String(buf);
        victimName = readUTF8String(buf);
        weaponStack = readItemStack(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        writeUTF8String(buf, attackerName);
        writeUTF8String(buf, victimName);
        writeItemStack(buf, weaponStack);
    }
}
