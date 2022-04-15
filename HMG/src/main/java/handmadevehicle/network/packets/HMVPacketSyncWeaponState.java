package handmadevehicle.network.packets;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import handmadevehicle.entity.parts.logics.BaseLogic;
import io.netty.buffer.ByteBuf;

public class HMVPacketSyncWeaponState implements IMessage {
	int targetEntityID;



	public HMVPacketSyncWeaponState(){

	}
	@Override
	public void fromBytes(ByteBuf buf) {
		targetEntityID = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(targetEntityID);
	}
}
