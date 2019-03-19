package com.lulan.shincolle.client.model;

import com.lulan.shincolle.entity.IShipEmotion;

public interface IModelEmotionAdv extends IModelEmotion {

	public void setFaceNormal(IShipEmotion ent);
	public void setFaceBlink0(IShipEmotion ent);
	public void setFaceBlink1(IShipEmotion ent);
	public void setFaceCry(IShipEmotion ent);
	public void setFaceAttack(IShipEmotion ent);
	public void setFaceDamaged(IShipEmotion ent);
	public void setFaceHungry(IShipEmotion ent);
	public void setFaceAngry(IShipEmotion ent);
	public void setFaceScorn(IShipEmotion ent);
	public void setFaceBored(IShipEmotion ent);
	public void setFaceShy(IShipEmotion ent);
	public void setFaceHappy(IShipEmotion ent);
	
	
}
