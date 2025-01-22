package handmadevehicle.entity.parts;

public interface HasLoopSound {
	default void yourSoundIsremain(String playingSound){
		//((HasBaseLogic)this).getBaseLogic().yourSoundIsremain();
	}
	//default float getsoundPitch(){
	//	return ((HasBaseLogic)this).getBaseLogic().getsoundPitch();
	//}
	default boolean getCanSeeFlag(){
		return false;
	}
	//default String getsound(){
	//	return ((HasBaseLogic)this).getBaseLogic().getsound();
	//}
}
