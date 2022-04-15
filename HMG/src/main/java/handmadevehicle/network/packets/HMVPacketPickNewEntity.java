package handmadevehicle.network.packets;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;

public class HMVPacketPickNewEntity implements IMessage {
	public int pickingEntityID;
	public int[] pickedEntityIDs = null;
	
	
	public HMVPacketPickNewEntity(){
	
	}
	
	public HMVPacketPickNewEntity(int pickingEntityID,int[] pickedEntityIDs){
		this();
		this.pickingEntityID = pickingEntityID;
		this.pickedEntityIDs = pickedEntityIDs;
	}
	public HMVPacketPickNewEntity(int pickingEntityID,Entity[] pickedEntities){
		this();
		this.pickingEntityID = pickingEntityID;
		int[] pickedEntityIDs = new int[pickedEntities.length];
		int cnt = 0;
		for(Entity entity:pickedEntities){
			if(entity != null)pickedEntityIDs[cnt] = entity.getEntityId();
			else pickedEntityIDs[cnt] = -1;
			cnt++;
		}
		this.pickedEntityIDs = pickedEntityIDs;
	}
	
	
	@Override
	public void fromBytes(ByteBuf buf) {
		pickingEntityID = buf.readInt();
		int length = buf.readInt();
		if(length != -1) {
			pickedEntityIDs = new int[length];
			for (int id = 0; id < length; id++)
				pickedEntityIDs[id] = buf.readInt();
		}
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(pickingEntityID);
		if(pickedEntityIDs != null) {
			buf.writeInt(pickedEntityIDs.length);
			for (int id : pickedEntityIDs)
				buf.writeInt(id);
		}else
			buf.writeInt(-1);
	}
}
