package hmggvcmob.entity;

import handmadevehicle.entity.parts.Hasmode;
import handmadevehicle.entity.parts.Modes;
import hmggvcmob.entity.util.EntityAndPos;
import hmggvcmob.entity.util.PlatoonOBJ;
import hmggvcmob.entity.util.PlatoonInfoData;

public interface IPlatoonable extends Hasmode {
	void makePlatoon();//分隊に周囲のエンティティを追加
	void makePlatoon_OnLoading();//読み込み時、周囲のエンティティを分隊に追加
	void enlistPlatoon(boolean force);
	void setPlatoon(PlatoonOBJ entities);//分隊員に分隊オブジェクトを渡す

	PlatoonOBJ getPlatoon();//分隊オブジェクトを返す
	default IPlatoonable getPlatoonLeader(){//分隊長を返す
		return (IPlatoonable)getPlatoon().leader.entity;
	}

	void setPosObj(EntityAndPos entityAndPos);

	default boolean isPlatoonLeader(){
		if(getPlatoon() != null && getPlatoon().leader != null) return getPlatoon().leader.entity == this;
		return false;
	}
	default boolean isFree(){
		return getPlatoon() == null;
	}


	default void changePlatoonOrder(Modes order){//分隊命令の変更
		getPlatoon().platoonMode = order;
	}


	default PlatoonInfoData getPlatoonMemberInfo(){
		return null;
	}
}
