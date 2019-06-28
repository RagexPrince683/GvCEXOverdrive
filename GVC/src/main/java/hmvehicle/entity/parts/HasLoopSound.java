package hmvehicle.entity.parts;

import hmvehicle.entity.parts.logics.IbaseLogic;

public interface HasLoopSound {
	IbaseLogic getBaseLogic();
	default void yourSoundIsremain(){
		getBaseLogic().yourSoundIsremain();
	}
}