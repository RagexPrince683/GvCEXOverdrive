package hmggvcmob.entity.util;

import handmadevehicle.SlowPathFinder.WorldForPathfind;
import handmadevehicle.entity.EntityDummy_rider;
import handmadevehicle.entity.EntityVehicle;
import handmadevehicle.entity.parts.logics.BaseLogic;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.util.MathHelper;

import javax.vecmath.Vector3d;
import java.util.Random;

import static hmggvcmob.util.GVCUtil.getDrivingVehicle;
import static hmggvcmob.util.GVCUtil.getRidingEntity;

public class MoveToPositionMng {
	private final Vector3d moveToPos;
	private final EntityLiving movingEntity;
	private final WorldForPathfind worldForPathfind;
	private double movingSpeed;

	private Entity ChaseEntity;//主に航空機用、追跡してるエンティティ

	public MoveToPositionMng(EntityLiving entity,WorldForPathfind worldForPathfind){
		//TODO 経路探索をAI側で行うと煩雑に過ぎるのでこのオブジェクトを介する形に変更する。
		movingEntity = entity;
		moveToPos = new Vector3d(entity.posX,entity.posY,entity.posZ);
		this.worldForPathfind = worldForPathfind;
	}

	public Vector3d getMoveToPos() {//目標位置ベクトルを返す。セット時はこれにset使えばいいね
		return moveToPos;
	}
	public void setMovingSpeed(double speed){
		movingSpeed = speed;
	}

	public double getMovingSpeed() {
		return movingSpeed;
	}

	public void stop(){
		movingSpeed = 0;
		moveToPos.set(movingEntity.posX,movingEntity.posY,movingEntity.posZ);
	}

	public Random random = new Random();
	public int resetPathCool = 20;//若干の間隔を開けてパスを更新する
	public void update(){//TODO 車両に乗ってるときは車両を取得して目標位置を設定する。車長のみ
		BaseLogic driving = getDrivingVehicle(this.movingEntity);
		Entity riding = getRidingEntity(this.movingEntity);

		if(movingEntity.getDistanceSq(moveToPos.x,
				moveToPos.y,
				moveToPos.z) > 40){
			Vector3d standingPos = new Vector3d(movingEntity.posX,
					movingEntity.posY,
					movingEntity.posZ);
			moveToPos.sub(standingPos);
			moveToPos.scale(40/moveToPos.length());
			moveToPos.add(standingPos);
		}

		if (driving != null && driving.prefab_vehicle.T_Land_F_Plane) {
			if(resetPathCool < 0 || driving.mc_Entity.getNavigator().noPath()) {
				PathEntity current = driving.mc_Entity.getNavigator().getPathToXYZ(
						MathHelper.floor_double(moveToPos.x),
						MathHelper.floor_double(moveToPos.y),
						MathHelper.floor_double(moveToPos.z));
				boolean flag = false;
				if (current != null) {
					driving.mc_Entity.getNavigator().setPath(current, movingSpeed);
					flag = true;
				}
				if (movingSpeed == 0) {
					driving.mc_Entity.getNavigator().clearPathEntity();
					flag = true;
				}
				if (flag) resetPathCool = 5 + random.nextInt(10);//平均10tick、一時に大量のmobが位置更新すると負荷がヤバい
			}
		}else if(riding == null){//単独mob
			if(resetPathCool < 0 || movingEntity.getNavigator().noPath()) {
				PathEntity current = worldForPathfind.getEntityPathToXYZ(
						movingEntity,
						MathHelper.floor_double(moveToPos.x),
						MathHelper.floor_double(moveToPos.y),
						MathHelper.floor_double(moveToPos.z),
						80, true, false, false, false);
				boolean flag = false;
				if(current != null){
					movingEntity.getNavigator().setPath(current, movingSpeed);
					flag = true;
				}
				if(movingSpeed == 0){
					movingEntity.getNavigator().clearPathEntity();
					flag = true;
				}
				if(flag)resetPathCool = 5 + random.nextInt(10);//平均10tick、一時に大量のmobが位置更新すると負荷がヤバい
			}
		}
		resetPathCool--;
	}
}
