package hmggvcmob.ai;

import handmadevehicle.SlowPathFinder.ModifiedPathNavigater;
import handmadevehicle.SlowPathFinder.PathPoint_slow;
import handmadevehicle.SlowPathFinder.WorldForPathfind;
import handmadevehicle.Utils;
import handmadevehicle.entity.EntityDummy_rider;
import handmadevehicle.entity.EntityVehicle;
import handmadevehicle.entity.parts.logics.BaseLogic;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;

import static handmadevehicle.Utils.*;
import static java.lang.Math.abs;

public class AIDriveTank {
//	public EntityVehicle vehicle;
//	public boolean Limited_turning_turret = false;//�ԗ��ݒ�Ɠ���������@�ǂ�����Č��o���悤�B�蓮�ł�������
//	public boolean combatSituation = false;
//	public EntityLiving driverBody;
//
//	public WorldForPathfind worldForPathfind;
//
//
//	public AIDriveTank(EntityLiving driverBody,EntityVehicle entityVehicle,WorldForPathfind worldForPathfind){
//		this.driverBody = driverBody;
//		this.vehicle = entityVehicle;
//		this.worldForPathfind = worldForPathfind;
//	}
//
//	@Override
//	public boolean shouldExecute() {
//		if(driverBody.ridingEntity instanceof EntityDummy_rider){
//			vehicle = (EntityVehicle) ((EntityDummy_rider) driverBody.ridingEntity).linkedBaseLogic.mc_Entity;
//		}
//		if(vehicle != null && !vehicle.isDead && vehicle.getBaseLogic().ispilot(driverBody)){
//			return true;
//		}else {
//			vehicle = null;
//		}
//		return false;
//	}
//
//
//	@Override
//	public void resetTask() {
//		if(vehicle != null){
//			System.out.println("debug");
//			vehicle.getNavigator().clearPathEntity();
//		}
//		combatSituation = false;
//		super.resetTask();
//	}
//	int pathSetCool = 20;
//	@Override
//	public void updateTask() {
//		if(driverBody.ridingEntity instanceof EntityDummy_rider && ((EntityDummy_rider) driverBody.ridingEntity).linkedSeatID == ((EntityDummy_rider) driverBody.ridingEntity).linkedBaseLogic.getpilotseatid()) {
//			if (driverBody.getNavigator().getPath() != null) {
//				double[] driverLastPosition = driverLastPosition();
//				if ((!isTargetSynced() || pathSetCool <= 0 || vehicle.getNavigator().getPath() == null) && driverLastPosition != null) {
//					PathEntity current = worldForPathfind.getEntityPathToXYZ(vehicle,
//							MathHelper.floor_double(driverLastPosition[0]),
//							MathHelper.floor_double(driverLastPosition[1]),
//							MathHelper.floor_double(driverLastPosition[2]),
//							60, false, false, false, false);
//					if (worldForPathfind.slowPathfinder != null && !worldForPathfind.slowPathfinder.isserchingpath && current != null)
//						vehicle.getNavigator().setPath(current,
//								driverBody.getNavigator() instanceof ModifiedPathNavigater ? ((ModifiedPathNavigater) driverBody.getNavigator()).getSpeed() : 1);
//
////					System.out.println("" + driverBody);
//
////					System.out.println("" + driverBody.getNavigator().getPath());
//
////					System.out.println("" + vehicle.getNavigator().getPath());
//				}
//
//				pathSetCool = 5;
//			} else {
//				vehicle.getNavigator().clearPathEntity();
//			}
//			pathSetCool--;
//		}
////
////
////
////
////
////
////
////
////
////
////
////
////
////		if(vehicle.getBaseLogic().ispilot(driverBody) && !driverBody.worldObj.isRemote) {
////			BaseLogic baseLogic = vehicle.getBaseLogic();
////			double[] nextPos = nextPosition();
////			baseLogic.setControl_throttle_down(false);
////			baseLogic.setControl_throttle_up(false);
////			baseLogic.setControl_yaw_Right(false);
////			baseLogic.setControl_yaw_Left(false);
////			baseLogic.setControl_Space(false);
////			baseLogic.setControl_brake(false);
////			double[] rollXYZ = Utils.eulerfromQuat(Utils.(baseLogic.rotationmotion));
////			double yawing = -rollXYZ[1]*10;
////			if (nextPos != null) {
////				Vector3d vecToNextPos = new Vector3d(nextPos);
//////				System.out.println("vecToNextPos global	" + vecToNextPos);
////				vecToNextPos.sub(new Vector3d(vehicle.posX, vehicle.posY, vehicle.posZ));//���Έʒu
////				vecToNextPos.scale(vehicle.getNavigator().getSpeed());
////				double dist = vecToNextPos.lengthSquared();
////
////				double[] lastPos = lastPosition();
////				if(lastPos != null){
////					Vector3d vecToLastPos = new Vector3d(lastPos);
////					vecToLastPos.sub(new Vector3d(vehicle.posX, vehicle.posY, vehicle.posZ));//���Έʒu
////					dist = vecToLastPos.lengthSquared();
////				}
////
////				getVector_local_inRotatedObj(vecToNextPos,vecToNextPos,baseLogic.bodyRot);
////				vecToNextPos.normalize();
//////				System.out.println("vecToNextPos local	" + vecToNextPos);
//////				System.out.println("localMotionVec		" + baseLogic.localMotionVec);
////				if(vehicle.onGround || vehicle.getBaseLogic().ishittingWater()) {
////					if (baseLogic.prefab_vehicle.forced_rudder_effect_OnGround > 0) {
////						baseLogic.setControl_throttle_up(true);
////						if (vecToNextPos.x < 0) {
////							if(vecToNextPos.x < yawing)
////								baseLogic.setControl_yaw_Right(true);
////						} else if (vecToNextPos.x > 0) {
////							if(vecToNextPos.x > yawing)
////								baseLogic.setControl_yaw_Left(true);
////						}
////						if(abs(vecToNextPos.x)>0.2){
////							baseLogic.setControl_brake(true);
////						}
////						//���M�n����
////					} else if (baseLogic.localMotionVec.z < 0) {//�O�i��
////						if (vecToNextPos.x < 0) {
////							if(vecToNextPos.x < yawing)
////								baseLogic.setControl_yaw_Right(true);
////						} else if (vecToNextPos.x > 0) {
////							if(vecToNextPos.x > yawing)
////								baseLogic.setControl_yaw_Left(true);
////						}
////					} else {
////						if (vecToNextPos.x < 0) {
////							if(vecToNextPos.x < yawing)
////								baseLogic.setControl_yaw_Right(true);
////						} else if (vecToNextPos.x > 0) {
////							if(vecToNextPos.x > yawing)
////								baseLogic.setControl_yaw_Left(true);
////						}
////					}
////				}
////				if (vecToNextPos.z < 0 || Limited_turning_turret || (combatSituation && vehicle.getNavigator().getSpeed() >0)) {
////					boolean flag_onCourse = false;
////					if (vecToNextPos.x > -0.3 && vecToNextPos.x < 0.3){
////						if (dist > 16) {
////							flag_onCourse = true;
////						}
////					}
////
////					if (flag_onCourse) {
////						baseLogic.setControl_throttle_up(true);
////					} else {
////						if (baseLogic.throttle > baseLogic.prefab_vehicle.throttle_Max / 2) {
////							baseLogic.setControl_throttle_down(true);
////						}else {
////							baseLogic.setControl_throttle_up(true);
////						}
////					}
////				} else {
////					boolean flag_onCourse = false;
////					if (vecToNextPos.x > -0.3 && vecToNextPos.x < 0.3){
////						if (dist > 16) {
////							flag_onCourse = true;
////						}
////					}
////					if (flag_onCourse) {
////						baseLogic.setControl_throttle_down(true);
////					} else {
////						if (-baseLogic.throttle < baseLogic.prefab_vehicle.throttle_min / 2) {
////							baseLogic.setControl_throttle_up(true);
////						}else {
////							baseLogic.setControl_throttle_down(true);
////						}
////					}
//////					if (vecToNextPos.x < -1) {
//////						baseLogic.setControl_yaw_Right(true);
//////						baseLogic.setControl_brake(true);
//////					}
//////					else if (vecToNextPos.x > 1) {
//////						baseLogic.setControl_yaw_Left(true);
//////						baseLogic.setControl_brake(true);
//////					}
//////					else
//////						baseLogic.setControl_throttle_down(true);
////				}
////				combatSituation = false;
////			} else {
////				baseLogic.setControl_Space(true);
////				baseLogic.setControl_brake(true);
////			}
////		}
//	}
//	public double[] nextPosition(){
//		PathEntity currentPath = vehicle.getNavigator().getPath();
//		if(currentPath != null && !currentPath.isFinished()){
//			Vec3 nextPos = currentPath.getPosition(vehicle);
//			return new double[]{nextPos.xCoord - (double)((int)(vehicle.width + 1.0F)) * 0.5D,nextPos.yCoord,nextPos.zCoord - (double)((int)(vehicle.width + 1.0F)) * 0.5D};
//		}
//		return null;
//	}
//	public double[] lastPosition(){
//		PathEntity currentPath = vehicle.getNavigator().getPath();
//		if(currentPath != null && !currentPath.isFinished()){
//			PathPoint nextPos = currentPath.getFinalPathPoint();
//			return new double[]{nextPos.xCoord,nextPos.yCoord,nextPos.zCoord};
//		}
//		return null;
//	}
//	public double[] driverLastPosition(){
//		PathEntity currentPath = driverBody.getNavigator().getPath();
//		if(currentPath != null && !currentPath.isFinished()){
//			PathPoint nextPos = currentPath.getFinalPathPoint();
//			return new double[]{nextPos.xCoord,nextPos.yCoord,nextPos.zCoord};
//		}
//		return null;
//	}
//
//	public boolean isTargetSynced(){
//		double[] vehicleTarget = lastPosition();
//		double[] driverLastPosition = driverLastPosition();
//		if(driverLastPosition == null)return true;
//		if(vehicleTarget == null)return false;
//		return vehicleTarget[0] == driverLastPosition[0] && vehicleTarget[1] == driverLastPosition[1] && vehicleTarget[2] == driverLastPosition[2];
//	}
}
