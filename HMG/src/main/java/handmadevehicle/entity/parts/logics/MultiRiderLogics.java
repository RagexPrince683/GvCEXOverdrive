package handmadevehicle.entity.parts.logics;

import handmadevehicle.entity.parts.SeatObject;
import net.minecraft.entity.Entity;

public interface MultiRiderLogics {
	Entity[] getRiddenEntityList();
	SeatObject[] getRiddenSeatList();
	boolean pickupEntity(Entity p_70085_1_, int StartSeachSeatNum,boolean dir);
	boolean isRidingEntity(Entity entity);
	default void disMountEntity(Entity p_70085_1_){
		if(p_70085_1_ == null)return;
		Entity[] riddenByEntities = getRiddenEntityList();
//		boolean flag = false;
		for (int cnt = 0; cnt < riddenByEntities.length; cnt++) {
			if(riddenByEntities[cnt] == p_70085_1_){
				riddenByEntities[cnt] = null;
				if(p_70085_1_.ridingEntity != null) {
					p_70085_1_.ridingEntity.riddenByEntity = null;
					p_70085_1_.ridingEntity = null;
				}
//				flag = true;
			}
		}
		//		p_70085_1_.mountEntity(this);
	}
	default boolean ispilot(Entity entity){
		if(entity == null)return false;
		return getRiddenEntityList()[getpilotseatid()] == entity;
	}
	default int getpilotseatid(){
		return 0;
	}
}
