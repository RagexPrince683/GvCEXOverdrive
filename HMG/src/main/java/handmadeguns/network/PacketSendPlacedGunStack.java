package handmadeguns.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import static cpw.mods.fml.common.network.ByteBufUtils.*;

public class PacketSendPlacedGunStack implements IMessage {

	public int shooterID = -1;
	public ItemStack stack;
	public PacketSendPlacedGunStack(){
	}
	public PacketSendPlacedGunStack(int targetID ,ItemStack stack){
		shooterID = targetID;
		this.stack = stack;
	}
	@Override
	public void fromBytes(ByteBuf buf) {
		shooterID = buf.readInt();

		stack = readItemStack(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(shooterID);

		writeItemStack(buf,stack);
	}
}
