package handmadevehicle.network.packets;

//import handmadeguns.Util.EntityLinkedPos_Motion;
import handmadevehicle.entity.parts.turrets.TurretObj;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;

import static cpw.mods.fml.common.network.ByteBufUtils.*;
import static java.lang.Math.abs;

public class TurretSyncData {
	public float yaw;
	public float pitch;
	public NBTTagCompound gunState;
	public int gunDamaged;
	public int gunStackSize;
	public int replaceStackTime = 0;
	public int childAndBrotherNum = 0;

	public int targetID = -1;

	//public EntityLinkedPos_Motion targetPosition;

	public Vec3 lockedBlockPos = null;

	public TurretSyncData[] childData;
	public TurretSyncData(TurretObj turretObj){
		yaw = (float) turretObj.turretrotationYaw;
		pitch = (float) turretObj.turretrotationPitch;
		replaceStackTime = turretObj.replaceCoolDown;
		turretObj.getDummyStackTag();
		if(turretObj.gunStack != null) {
			gunState = turretObj.getDummyStackTag();
			gunDamaged = turretObj.gunStack.getItemDamage();
			gunStackSize = turretObj.gunStack.stackSize;
		}else {
			gunStackSize = -1;
		}

		lockedBlockPos = turretObj.lockedBlockPos;
//		System.out.println("" + lockedBlockPos);

		if(turretObj.target != null){
			targetID = turretObj.target.getEntityId();
			//targetPosition = new EntityLinkedPos_Motion(turretObj.target,-1);
		}

		childData = new TurretSyncData[turretObj.getChilds().size() + turretObj.getChildsOnBarrel().size() + turretObj.getBrothers().size()];
		int id = 0;
		for(TurretObj aturretObj : turretObj.getChilds()){
			childData[id] = new TurretSyncData(aturretObj);
			id++;
		}
		for(TurretObj aturretObj : turretObj.getChildsOnBarrel()){
			childData[id] = new TurretSyncData(aturretObj);
			id++;
		}
		for(TurretObj aturretObj : turretObj.getBrothers()){
			childData[id] = new TurretSyncData(aturretObj);
			id++;
		}
		childAndBrotherNum = id;
	}
	public void setTurretData(TurretObj target){
		target.targetTurretrotationYaw = yaw;
		target.targetTurretrotationPitch = pitch;
		target.replaceCoolDown = replaceStackTime;
		if(target.gunStack != null) {
			target.gunStack.setTagCompound(gunState);
			target.gunStack.setItemDamage(gunDamaged);
			target.gunStack.stackSize = gunStackSize;
		}
		target.lockedBlockPos = lockedBlockPos;
		target.target = target.motherEntity.worldObj.getEntityByID(targetID);
		//target.targetPos = targetPosition;

		int id = 0;
		for(TurretObj aturretObj : target.getChilds()){
			childData[id].setTurretData(aturretObj);
			id++;
		}
		for(TurretObj aturretObj : target.getChildsOnBarrel()){
			childData[id].setTurretData(aturretObj);
			id++;
		}
		for(TurretObj aturretObj : target.getBrothers()){
			childData[id].setTurretData(aturretObj);
			id++;
		}
	}
	
	public TurretSyncData(ByteBuf buf) {
		yaw = buf.readFloat();
		pitch = buf.readFloat();
		gunState = readTag(buf);
		gunDamaged = buf.readInt();
		replaceStackTime = buf.readInt();
		gunStackSize = buf.readInt();
		childAndBrotherNum = buf.readInt();

		targetID = buf.readInt();

		//if(targetID != -1){
		//	targetPosition = new EntityLinkedPos_Motion();
		//	targetPosition.fromBytes(buf);
		//}

		if(buf.readBoolean()){
			lockedBlockPos = Vec3.createVectorHelper(buf.readDouble(),buf.readDouble(),buf.readDouble());
		}


		childData = new TurretSyncData[childAndBrotherNum];
		for(int id = 0;id < childAndBrotherNum ; id ++){
			childData[id] = new TurretSyncData(buf);
		}
	}
	
	public void toBytes(ByteBuf buf) {
		buf.writeFloat(yaw);
		buf.writeFloat(pitch);
		writeTag(buf,gunState);
		buf.writeInt(gunDamaged);
		buf.writeInt(replaceStackTime);
		buf.writeInt(gunStackSize);
		buf.writeInt(childAndBrotherNum);

		buf.writeInt(targetID);

		if(targetID != -1){
			//targetPosition.toBytes(buf);
		}

		boolean flag = lockedBlockPos != null;
		buf.writeBoolean(flag);

		if(flag){
			buf.writeDouble(lockedBlockPos.xCoord);
			buf.writeDouble(lockedBlockPos.yCoord);
			buf.writeDouble(lockedBlockPos.zCoord);
		}
		for(TurretSyncData syncData:childData){
			syncData.toBytes(buf);
		}
	}
}
