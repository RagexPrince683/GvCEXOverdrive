package handmadeguns.Util;

import handmadevehicle.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import javax.vecmath.Vector3d;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static handmadeguns.HandmadeGunsCore.cfg_ThreadHitCheck_split_length;
import static java.lang.Math.*;
import static net.minecraft.util.MathHelper.floor_double;

public class GunsUtils {
	public static Vec3 getLook(float p_70676_1_, Entity entity)
	{
		float f1;
		float f2;
		float f3;
		float f4;


		if (p_70676_1_ == 1.0F)
		{
			f1 = MathHelper.cos(-(entity instanceof EntityLivingBase ? ((EntityLivingBase)entity).rotationYawHead : entity.rotationYaw) * 0.017453292F - (float)Math.PI);
			f2 = MathHelper.sin(-(entity instanceof EntityLivingBase ? ((EntityLivingBase)entity).rotationYawHead : entity.rotationYaw) * 0.017453292F - (float)Math.PI);
			f3 = -MathHelper.cos(-entity.rotationPitch * 0.017453292F);
			f4 = MathHelper.sin(-entity.rotationPitch * 0.017453292F);
			return Vec3.createVectorHelper((double)(f2 * f3), (double)f4, (double)(f1 * f3));
		}else {
			return null;
		}
	}

	public static Vec3 RotationVector_byAxisVector(Vec3 axis, Vec3 tovec, float angle)
	{
		double axisVectorX = axis.xCoord;
		double axisVectorY = axis.yCoord;
		double axisVectorZ = axis.zCoord;
		double toVectorX = tovec.xCoord;
		double toVectorY = tovec.yCoord;
		double toVectorZ = tovec.zCoord;
		double angleRad = (double)angle / 180.0D * Math.PI;
		double sintheta = Math.sin(angleRad);
		double costheta = Math.cos(angleRad);
		double returnVectorX = (axisVectorX * axisVectorX * (1 - costheta) + costheta)			   * toVectorX + (axisVectorX * axisVectorY * (1 - costheta) - axisVectorZ * sintheta) * toVectorY + (axisVectorZ * axisVectorX * (1 - costheta) + axisVectorY * sintheta) * toVectorZ;
		double returnVectorY = (axisVectorX * axisVectorY * (1 - costheta) + axisVectorZ * sintheta) * toVectorX + (axisVectorY * axisVectorY * (1 - costheta) + costheta)			   * toVectorY + (axisVectorY * axisVectorZ * (1 - costheta) - axisVectorX * sintheta) * toVectorZ;
		double returnVectorZ = (axisVectorZ * axisVectorX * (1 - costheta) - axisVectorY * sintheta) * toVectorX + (axisVectorY * axisVectorZ * (1 - costheta) + axisVectorX * sintheta) * toVectorY + (axisVectorZ * axisVectorZ * (1 - costheta) + costheta)			   * toVectorZ;

		return Vec3.createVectorHelper(returnVectorX, returnVectorY, returnVectorZ);
	}

	public static MovingObjectPosition getmovingobjectPosition_forBlock(World worldObj, Vec3 start, Vec3 end,Block[] forcePenetrateList){
		return getmovingobjectPosition_forBlock(worldObj, start, end,3,forcePenetrateList,null);
	}

	public static MovingObjectPosition getmovingobjectPosition_forBlock(World worldObj, Vec3 start, Vec3 end){
		return getmovingobjectPosition_forBlock(worldObj, start, end,3,null,null);
	}
	public static int penerateCnt;
	public static MovingObjectPosition getmovingobjectPosition_forBlock(World worldObj, final Vec3 start, final Vec3 end,int penerateCnt_in , Block[] forcePenetrateList,Material[] forcePenetrateList2){
		penerateCnt = penerateCnt_in;
		MovingObjectPosition errorVal = new MovingObjectPosition((int)(start.xCoord),(int)(start.yCoord),(int)(start.zCoord),0,start);

		if (!Double.isNaN(start.xCoord) && !Double.isNaN(start.yCoord) && !Double.isNaN(start.zCoord))
		{
			if (!Double.isNaN(end.xCoord) && !Double.isNaN(end.yCoord) && !Double.isNaN(end.zCoord))
			{
				return checkBlockAndCheckHit(worldObj, start,end,forcePenetrateList,forcePenetrateList2);
			}
			else
			{
				return errorVal;
			}
		}
		else
		{
			return errorVal;
		}
	}
	//	public static MovingObjectPosition checkBlockAndCheckHit(
//			final World worldObj,
//			final Vec3 start,
//			final Vec3 end){
//		int endX = floor_double(end.xCoord);
//		int endY = floor_double(end.yCoord);
//		int endZ = floor_double(end.zCoord);
//		int startX = floor_double(start.xCoord);
//		int startY = floor_double(start.yCoord);
//		int startZ = floor_double(start.zCoord);
//		int cnt = 0;
//
//		while (startX != endX || startY != endY || startZ != endZ) {
//
//			boolean needXCheck = true;
//			boolean needYCheck = true;
//			boolean needZCheck = true;
//			double nextX = 999.0D;
//			double nextY = 999.0D;
//			double nextZ = 999.0D;
//
//			if (endX > startX) {
//				nextX = (double) startX + 1.0D;
//			} else if (endX < startX) {
//				nextX = (double) startX + 0.0D;
//			} else {
//				needXCheck = false;
//			}
//
//			if (endY > startY) {
//				nextY = (double) startY + 1.0D;
//			} else if (endY < startY) {
//				nextY = (double) startY + 0.0D;
//			} else {
//				needYCheck = false;
//			}
//
//			if (endZ > startZ) {
//				nextZ = (double) startZ + 1.0D;
//			} else if (endZ < startZ) {
//				nextZ = (double) startZ + 0.0D;
//			} else {
//				needZCheck = false;
//			}
//
//			double moveScaleX = 999.0D;
//			double moveScaleZ = 999.0D;
//			double moveScaleY = 999.0D;
//			Vector3d checkLineVec = new Vector3d(
//					end.xCoord - start.xCoord,
//					end.yCoord - start.yCoord,
//					end.zCoord - start.zCoord);
//
//
//			if (needXCheck) {
//				moveScaleX = (nextX - start.xCoord) / checkLineVec.x;
//			}
//
//			if (needYCheck) {
//				moveScaleZ = (nextY - start.yCoord) / checkLineVec.y;
//			}
//
//			if (needZCheck) {
//				moveScaleY = (nextZ - start.zCoord) / checkLineVec.z;
//			}
//			byte b0;//移動方向
//
//			//探索点の移動がもっとも小さい軸に沿って探索基準ベクトルを移動させる
//			if (moveScaleX < moveScaleZ && moveScaleX < moveScaleY) {
//				if (endX > startX) {
//					b0 = 4;
//				} else {
//					b0 = 5;
//				}
//
//				start.xCoord = nextX;
//				start.yCoord += checkLineVec.y * moveScaleX;
//				start.zCoord += checkLineVec.z * moveScaleX;
//			} else if (moveScaleZ < moveScaleY) {
//				if (endY > startY) {
//					b0 = 0;
//				} else {
//					b0 = 1;
//				}
//
//				start.xCoord += checkLineVec.x * moveScaleZ;
//				start.yCoord = nextY;
//				start.zCoord += checkLineVec.z * moveScaleZ;
//			} else {
//				if (endZ > startZ) {
//					b0 = 2;
//				} else {
//					b0 = 3;
//				}
//
//				start.xCoord += checkLineVec.x * moveScaleY;
//				start.yCoord += checkLineVec.y * moveScaleY;
//				start.zCoord = nextZ;
//			}
//
//			Vec3 vec32 = Vec3.createVectorHelper(start.xCoord, start.yCoord, start.zCoord);
//			startX = (int) (vec32.xCoord = floor_double(start.xCoord));
//
//			if (b0 == 5) {
//				--startX;
//				++vec32.xCoord;
//			}
//
//			startY = (int) (vec32.yCoord = floor_double(start.yCoord));
//
//			if (b0 == 1) {
//				--startY;
//				++vec32.yCoord;
//			}
//
//			startZ = (int) (vec32.zCoord = floor_double(start.zCoord));
//
//			if (b0 == 3) {
//				--startZ;
//				++vec32.zCoord;
//			}
//
//			Block block1 = worldObj.getBlock(startX, startY, startZ);
//			int l1 = worldObj.getBlockMetadata(startX, startY, startZ);
//
//			if (isCollidableBlock(block1) && block1.getCollisionBoundingBoxFromPool(worldObj, startX, startY, startZ) != null) {
//				if (block1.canCollideCheck(l1, false)) {
//					//ブロックに当たる箇所があるかチェック
//					//同時に当たる点を確認
//					MovingObjectPosition movingobjectposition1 = block1.collisionRayTrace(worldObj, startX, startY, startZ, start, end);
//
//					if (movingobjectposition1 != null) {
//						return movingobjectposition1;
//					}
//				}
////						else
////						{
//////							movingobjectposition2 = new MovingObjectPosition(startX, startY, startZ, b0, start, false);
////						}
//			}
//		}
//
//		return null;
//	}

	static Vector3d startVec = new Vector3d();
	static Vector3d end__Vec = new Vector3d();
	static Vector3d checkLineVec = new Vector3d();
	static ExecutorService exec = Executors.newWorkStealingPool();
	static AtomicInteger ThreadCounter = new AtomicInteger();
	public static MovingObjectPosition checkBlockAndCheckHit(
			final World worldObj,
			final Vec3 start,
			final Vec3 end,
			final Block[] forcePenetrateList) {
		return checkBlockAndCheckHit(worldObj,start,end,forcePenetrateList,null);
	}
	public static MovingObjectPosition checkBlockAndCheckHit(
			final World worldObj,
			final Vec3 start,
			final Vec3 end,
			final Block[] forcePenetrateList,final Material[] forcePenetrateList2){
		int endX = floor_double(end.xCoord);
		int endY = floor_double(end.yCoord);
		int endZ = floor_double(end.zCoord);
		int startX = floor_double(start.xCoord);
		int startY = floor_double(start.yCoord);
		int startZ = floor_double(start.zCoord);

		startVec.set(start.xCoord,
				start.yCoord,
				start.zCoord);
		end__Vec.set(end.xCoord,
				end.yCoord,
				end.zCoord);

		checkLineVec.set(
				end.xCoord - start.xCoord,
				end.yCoord - start.yCoord,
				end.zCoord - start.zCoord);
//		long startTime = System.nanoTime();
		if(true /*!cfg_ThreadHitCheck || checkLineVec.length() < 160**/){
//			System.out.println("debug");

			//			long endTime = ;

//			System.out.println("Serial   EndTime " + (System.nanoTime() - startTime));
			return checkBlocks_serial(worldObj,startX,startY,startZ,endX,endY,endZ,end,start,forcePenetrateList,forcePenetrateList2);
		}else
			{
			int loopNum = -1;
//			int splitLength = cfg_ThreadHitCheck_split_length;
			double length = checkLineVec.length();
			loopNum = floor_double(length)/cfg_ThreadHitCheck_split_length + 1;
//			if (abs(checkLineVec.x) > abs(checkLineVec.y) && abs(checkLineVec.x) > abs(checkLineVec.z)) {
//				oneLoopCheckVec.scale(splitLength / abs(checkLineVec.x));
//				oneLoopCheckVec.x = round(oneLoopCheckVec.x);
//				loopNum = abs(floor_double(end__Vec.x - startVec.x))/splitLength + 1;
//			} else if (abs(checkLineVec.y) > abs(checkLineVec.x) && abs(checkLineVec.y) > abs(checkLineVec.z)) {
//				oneLoopCheckVec.scale(1 / abs(checkLineVec.y));
//				oneLoopCheckVec.y = round(oneLoopCheckVec.y);
//				loopNum = abs(floor_double(end__Vec.y - startVec.y))/splitLength + 1;
//			} else if (abs(checkLineVec.z) > abs(checkLineVec.x) && abs(checkLineVec.z) > abs(checkLineVec.y)) {
//				oneLoopCheckVec.scale(1 / abs(checkLineVec.z));
//				oneLoopCheckVec.z = round(oneLoopCheckVec.z);
//				loopNum = abs(floor_double(end__Vec.z - startVec.z))/splitLength + 1;
//			}
			final MovingObjectPosition_andCounter returnValue = new MovingObjectPosition_andCounter(null, loopNum);

//			ExecutorService checkerThread = Executors.newFixedThreadPool(loopNum);
//			System.out.println("ThreadOpeningTime" + (System.nanoTime() - startTime));
			;
			List<Future<MovingObjectPosition>> returnVal = new ArrayList<>(loopNum);
			for (int cnt = 0; cnt < loopNum; cnt++) {
//				System.out.println("Thread Starting" + cnt + "\t" + (System.nanoTime() - startTime));
				A_checkerThread a_checkerThread = new A_checkerThread(
						cnt, loopNum,  worldObj);
				//					System.out.println("thread-id:" + Thread.currentThread().getId());
//				ThreadCounter.incrementAndGet();
//				exec.submit(a_checkerThread);
				returnVal.add(exec.submit(a_checkerThread));
//				System.out.println("Thread Started " + cnt + "\t" + (System.nanoTime() - startTime));
//				checkerThread.execute(a_checkerThread);
			}
			MovingObjectPosition movingObjectPosition = null;
			try {
				for (Future<MovingObjectPosition> future : returnVal) {
					movingObjectPosition = future.get();
					if(movingObjectPosition != null)break;
				}
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}

//			System.out.println("ParallelsEndTime  " + (System.nanoTime() - startTime));

			return movingObjectPosition;
//			oneLoopCheckVec.set(checkLineVec);
//
//			int loopNum = -1;
//
////			splitLength = cfg_ThreadHitCheck_split_length;
//
//			double length = oneLoopCheckVec.length();
//
//			oneLoopCheckVec.normalize();
//			oneLoopCheckVec.scale(cfg_ThreadHitCheck_split_length);
//			loopNum = floor_double(length)/cfg_ThreadHitCheck_split_length + 1;
//
////			if (abs(checkLineVec.x) > abs(checkLineVec.y) && abs(checkLineVec.x) > abs(checkLineVec.z)) {
////				oneLoopCheckVec.scale(splitLength / abs(checkLineVec.x));
////				oneLoopCheckVec.x = round(oneLoopCheckVec.x);
////
////				loopNum = abs(floor_double(end__Vec.x - startVec.x))/splitLength + 1;
////			} else if (abs(checkLineVec.y) > abs(checkLineVec.x) && abs(checkLineVec.y) > abs(checkLineVec.z)) {
////				oneLoopCheckVec.scale(1 / abs(checkLineVec.y));
////				oneLoopCheckVec.y = round(oneLoopCheckVec.y);
////
////				loopNum = abs(floor_double(end__Vec.y - startVec.y))/splitLength + 1;
////			} else if (abs(checkLineVec.z) > abs(checkLineVec.x) && abs(checkLineVec.z) > abs(checkLineVec.y)) {
////				oneLoopCheckVec.scale(1 / abs(checkLineVec.z));
////				oneLoopCheckVec.z = round(oneLoopCheckVec.z);
////
////				loopNum = abs(floor_double(end__Vec.z - startVec.z))/splitLength + 1;
////			}
//			final MovingObjectPosition_andCounter returnValue = new MovingObjectPosition_andCounter(null, loopNum);
////			startedNum.set(0);
////			ended__Num.set(0);
//
//			ExecutorService checkerThread = Executors.newWorkStealingPool(loopNum);
//			for (int cnt = 0; cnt < loopNum; cnt++) {
//
////				startedNum.getAndIncrement();
//
//				int finalCnt = cnt;
//				int finalLoopNum = loopNum;
//				checkerThread.execute(() -> {
//
//
//					try {
//						Vector3d currentStartVec = new Vector3d(startVec);
//						Vector3d currentEnd__Vec = new Vector3d(startVec);
//						currentStartVec.scaleAdd(finalCnt, oneLoopCheckVec, startVec);
//						if(finalCnt + 1 == finalLoopNum){
//							currentEnd__Vec.set(end__Vec);
//						}else {
//							currentEnd__Vec.scaleAdd(finalCnt + 1, oneLoopCheckVec, startVec);
//						}
//
//						int[] startBlockPos = new int[]{
//								floor_double(currentStartVec.x),
//								floor_double(currentStartVec.y),
//								floor_double(currentStartVec.z)
//						};
//						int[] end__BlockPos = new int[]{
//								floor_double(currentEnd__Vec.x),
//								floor_double(currentEnd__Vec.y),
//								floor_double(currentEnd__Vec.z)
//						};
//
//
////				System.out.println("start" + currentStartVec);
////				System.out.println("start" + currentEnd__Vec);
////
////				System.out.println("start" + startBlockPos[0] + " , " + startBlockPos[1] + " , " + startBlockPos[2]);
////				System.out.println("end  " + end__BlockPos[0] + " , " + end__BlockPos[1] + " , " + end__BlockPos[2]);
//						int cnt2 = 0;
//						synchronized (returnValue) {
//							if (returnValue.cnt > finalCnt) {
//								Block block1 = worldObj.getBlock(startBlockPos[0], startBlockPos[1], startBlockPos[2]);
//								int l1 = worldObj.getBlockMetadata(startBlockPos[0], startBlockPos[1], startBlockPos[2]);
//								if (isCollidableBlock(block1) && block1.getCollisionBoundingBoxFromPool(worldObj, startBlockPos[0], startBlockPos[1], startBlockPos[2]) != null) {
//									if (block1.canCollideCheck(l1, false)) {
//										//ブロックに当たる箇所があるかチェック
//										//同時に当たる点を確認
//										MovingObjectPosition movingobjectposition1 = block1.collisionRayTrace(worldObj, startBlockPos[0], startBlockPos[1], startBlockPos[2], start, end);
//
//										if (movingobjectposition1 != null) {
//											returnValue.overWrite(movingobjectposition1, finalCnt);
//										}
//									}
//
//								}
//							}
//						}
//
//						while (startBlockPos[0] != end__BlockPos[0] || startBlockPos[1] != end__BlockPos[1] || startBlockPos[2] != end__BlockPos[2]) {
//							cnt2++;
//							if (cnt2 == cfg_ThreadHitCheck_split_length*4) {
//								break;
//							}
//							boolean needXCheck = true;
//							boolean needYCheck = true;
//							boolean needZCheck = true;
//							double nextX = 0;
//							double nextY = 0;
//							double nextZ = 0;
//							checkLineVec.set(
//									end__BlockPos[0] - startBlockPos[0],
//									end__BlockPos[1] - startBlockPos[1],
//									end__BlockPos[2] - startBlockPos[2]);
//
//							if (checkLineVec.x > 0) {
//								nextX = (double) startBlockPos[0] + 1.0D;
//							} else if (checkLineVec.x < 0) {
//								nextX = (double) startBlockPos[0] + 0.0D;
//							} else {
//								needXCheck = false;
//							}
//
//							if (checkLineVec.y > 0) {
//								nextY = (double) startBlockPos[1] + 1.0D;
//							} else if (checkLineVec.y < 0) {
//								nextY = (double) startBlockPos[1] + 0.0D;
//							} else {
//								needYCheck = false;
//							}
//
//							if (checkLineVec.z > 0) {
//								nextZ = (double) startBlockPos[2] + 1.0D;
//							} else if (checkLineVec.z < 0) {
//								nextZ = (double) startBlockPos[2] + 0.0D;
//							} else {
//								needZCheck = false;
//							}
//
//							double moveScaleX = 999.0D;
//							double moveScaleZ = 999.0D;
//							double moveScaleY = 999.0D;
//							checkLineVec.set(
//									currentEnd__Vec.x - currentStartVec.x,
//									currentEnd__Vec.y - currentStartVec.y,
//									currentEnd__Vec.z - currentStartVec.z);
//
//
//							if (needXCheck) {
//								moveScaleX = (nextX - currentStartVec.x) / checkLineVec.x;
//							}
//
//							if (needYCheck) {
//								moveScaleZ = (nextY - currentStartVec.y) / checkLineVec.y;
//							}
//
//							if (needZCheck) {
//								moveScaleY = (nextZ - currentStartVec.z) / checkLineVec.z;
//							}
//
//							byte b0;
//							if (moveScaleX < moveScaleZ && moveScaleX < moveScaleY) {
//								if (endX > startBlockPos[0]) {
//									b0 = 4;
//								} else {
//									b0 = 5;
//								}
//
//								currentStartVec.x = nextX;
//								currentStartVec.y += checkLineVec.y * moveScaleX;
//								currentStartVec.z += checkLineVec.z * moveScaleX;
//							} else if (moveScaleZ < moveScaleY) {
//								if (endY > startBlockPos[1]) {
//									b0 = 0;
//								} else {
//									b0 = 1;
//								}
//
//								currentStartVec.x += checkLineVec.x * moveScaleZ;
//								currentStartVec.y = nextY;
//								currentStartVec.z += checkLineVec.z * moveScaleZ;
//							} else {
//								if (endZ > startBlockPos[2]) {
//									b0 = 2;
//								} else {
//									b0 = 3;
//								}
//
//								currentStartVec.x += checkLineVec.x * moveScaleY;
//								currentStartVec.y += checkLineVec.y * moveScaleY;
//								currentStartVec.z = nextZ;
//							}
//
//							Vec3 vec32 = Vec3.createVectorHelper(currentStartVec.x, currentStartVec.y, currentStartVec.z);
//							startBlockPos[0] = (int) (vec32.xCoord = floor_double(currentStartVec.x));
//
//							if (b0 == 5) {
//								--startBlockPos[0];
//								++vec32.xCoord;
//							}
//
//							startBlockPos[1] = (int) (vec32.yCoord = floor_double(currentStartVec.y));
//
//							if (b0 == 1) {
//								--startBlockPos[1];
//								++vec32.yCoord;
//							}
//
//							startBlockPos[2] = (int) (vec32.zCoord = floor_double(currentStartVec.z));
//							if (b0 == 3) {
//								--startBlockPos[2];
//								++vec32.zCoord;
//							}
//
//							synchronized (returnValue) {
//								if (returnValue.cnt > finalCnt) {
//									Block block1 = worldObj.getBlock(startBlockPos[0], startBlockPos[1], startBlockPos[2]);
//									int l1 = worldObj.getBlockMetadata(startBlockPos[0], startBlockPos[1], startBlockPos[2]);
//									if (isCollidableBlock(block1) && block1.getCollisionBoundingBoxFromPool(worldObj, startBlockPos[0], startBlockPos[1], startBlockPos[2]) != null) {
//										if (block1.canCollideCheck(l1, false)) {
//											//ブロックに当たる箇所があるかチェック
//											//同時に当たる点を確認
//											MovingObjectPosition movingobjectposition1 = block1.collisionRayTrace(worldObj, startBlockPos[0], startBlockPos[1], startBlockPos[2], start, end);
//
//											if (movingobjectposition1 != null) {
//												returnValue.overWrite(movingobjectposition1, finalCnt);
//											}
//										}
//
//									}
//								}
//							}
////					System.out.println("start" + startBlockPos[0] + " , " + startBlockPos[1] + " , " + startBlockPos[2]);
////					System.out.println("end  " + end__BlockPos[0] + " , " + end__BlockPos[1] + " , " + end__BlockPos[2]);
//						}
////						ended__Num.incrementAndGet();
//					} catch (Throwable e) {
//						e.printStackTrace();
//					}
//				});
//			}
//			try {
//				checkerThread.shutdown();
//				checkerThread.awaitTermination(4,TimeUnit.SECONDS);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//
////		System.out.println("debug");
//
//
//			long endTime = System.nanoTime();
//
//
//			System.out.println("" + (endTime - startTime));
//			return returnValue.value;
		}

	}

	private static class A_checkerThread implements Callable <MovingObjectPosition>{

		public A_checkerThread(int finalCnt, int finalLoopNum, World worldObj){
			this.currentEnd__Vec = new Vector3d();
			this.currentStartVec = new Vector3d();
			this.finalCnt = finalCnt;
			this.finalLoopNum = finalLoopNum;
			this.worldObj = worldObj;
		}
		Vector3d currentEnd__Vec;
		Vector3d currentStartVec;
		int finalCnt;
		int finalLoopNum;
		World worldObj;

		MovingObjectPosition movingObjectPosition;
		Block[] forcePenetrateList;

		@Override
		public MovingObjectPosition call() throws Exception {
			currentStartVec.interpolate(startVec, end__Vec, (double) finalCnt / finalLoopNum);
			currentEnd__Vec.interpolate(startVec, end__Vec, (double)(finalCnt + 1) / finalLoopNum);
			movingObjectPosition = checkBlocks_serial(worldObj,floor_double(currentStartVec.x),
					floor_double(currentStartVec.y),
					floor_double(currentStartVec.z),floor_double(currentEnd__Vec.x),
					floor_double(currentEnd__Vec.y),
					floor_double(currentEnd__Vec.z), Utils.getMinecraftVecObj(currentEnd__Vec),Utils.getMinecraftVecObj(currentStartVec),forcePenetrateList, null);

			return movingObjectPosition;
		}
	}

	public static MovingObjectPosition checkBlocks_serial(World worldObj, int startX, int startY, int startZ,
	                                                      int endX, int endY, int endZ, Vec3 end, Vec3 start, final Block[] forcePenetrateList, Material[] forcePenetrateList2){

		Block block;
		int k1;
		synchronized (worldObj.getChunkProvider()){
			block = worldObj.getBlock(startX, startY, startZ);
			k1 = worldObj.getBlockMetadata(startX, startY, startZ);
		}

		if (isCollidableBlock(block,forcePenetrateList,forcePenetrateList2) && block.getCollisionBoundingBoxFromPool(worldObj, startX, startY, startZ) != null && block.canCollideCheck(k1, false))
		{
			//開始地点に無いかチェック

			MovingObjectPosition movingobjectposition = block.collisionRayTrace(worldObj, startX, startY, startZ, start, end);

			if (movingobjectposition != null) {
//				System.out.println("debug");
				return movingobjectposition;
			}
		}
		
		int timer = (int) (abs(checkLineVec.x) + abs(checkLineVec.y) + abs(checkLineVec.z) + 3);//処理するブロックはもっとも多くてもこの量を超えない...ハズ
		if (isCollidableBlock(block,forcePenetrateList,forcePenetrateList2) && block.getCollisionBoundingBoxFromPool(worldObj, startX, startY, startZ) != null)if (block.canCollideCheck(k1, false))
		{
			MovingObjectPosition movingobjectposition = block.collisionRayTrace(worldObj, startX, startY, startZ, start, end);

			if (movingobjectposition != null)
			{
				return movingobjectposition;
			}
		}

		while (timer > 0 && (startX != endX || startY != endY || startZ != endZ)) {
			timer--;
			boolean needXCheck = true;
			boolean needYCheck = true;
			boolean needZCheck = true;
			double nextX = 999.0D;
			double nextY = 999.0D;
			double nextZ = 999.0D;

			if (endX > startX) {
				nextX = (double) startX + 1.0D;
			} else if (endX < startX) {
				nextX = (double) startX + 0.0D;
			} else {
				needXCheck = false;
			}

			if (endY > startY) {
				nextY = (double) startY + 1.0D;
			} else if (endY < startY) {
				nextY = (double) startY + 0.0D;
			} else {
				needYCheck = false;
			}

			if (endZ > startZ) {
				nextZ = (double) startZ + 1.0D;
			} else if (endZ < startZ) {
				nextZ = (double) startZ + 0.0D;
			} else {
				needZCheck = false;
			}

			double moveScaleX = 999.0D;
			double moveScaleZ = 999.0D;
			double moveScaleY = 999.0D;

			checkLineVec.set(
					end.xCoord - start.xCoord,
					end.yCoord - start.yCoord,
					end.zCoord - start.zCoord);


			if (needXCheck) {
				moveScaleX = (nextX - start.xCoord) / checkLineVec.x;
			}

			if (needYCheck) {
				moveScaleZ = (nextY - start.yCoord) / checkLineVec.y;
			}

			if (needZCheck) {
				moveScaleY = (nextZ - start.zCoord) / checkLineVec.z;
			}
			byte b0;//移動方向

			//探索点の移動がもっとも小さい軸に沿って探索基準ベクトルを移動させる
			if (moveScaleX < moveScaleZ && moveScaleX < moveScaleY) {
				if (endX > startX) {
					b0 = 4;
				} else {
					b0 = 5;
				}

				start.xCoord = nextX;
				start.yCoord += checkLineVec.y * moveScaleX;
				start.zCoord += checkLineVec.z * moveScaleX;
			} else if (moveScaleZ < moveScaleY) {
				if (endY > startY) {
					b0 = 0;
				} else {
					b0 = 1;
				}

				start.xCoord += checkLineVec.x * moveScaleZ;
				start.yCoord = nextY;
				start.zCoord += checkLineVec.z * moveScaleZ;
			} else {
				if (endZ > startZ) {
					b0 = 2;
				} else {
					b0 = 3;
				}

				start.xCoord += checkLineVec.x * moveScaleY;
				start.yCoord += checkLineVec.y * moveScaleY;
				start.zCoord = nextZ;
			}

			Vec3 vec32 = Vec3.createVectorHelper(start.xCoord, start.yCoord, start.zCoord);
			startX = (int) (vec32.xCoord = floor_double(start.xCoord));

			if (b0 == 5) {
				--startX;
				++vec32.xCoord;
			}

			startY = (int) (vec32.yCoord = floor_double(start.yCoord));

			if (b0 == 1) {
				--startY;
				++vec32.yCoord;
			}

			startZ = (int) (vec32.zCoord = floor_double(start.zCoord));

			if (b0 == 3) {
				--startZ;
				++vec32.zCoord;
			}

			Block block1;
			int l1;

			synchronized (worldObj.getChunkProvider()){
				block1 = worldObj.getBlock(startX, startY, startZ);
				l1 = worldObj.getBlockMetadata(startX, startY, startZ);
			}
			if (isCollidableBlock(block1,forcePenetrateList,forcePenetrateList2) && block1.getCollisionBoundingBoxFromPool(worldObj, startX, startY, startZ) != null) {
				if (block1.canCollideCheck(l1, false)) {
					//ブロックに当たる箇所があるかチェック
					//同時に当たる点を確認
					MovingObjectPosition movingobjectposition1 = block1.collisionRayTrace(worldObj, startX, startY, startZ, start, end);

					if (movingobjectposition1 != null) {
//							System.out.println("debug");
						return movingobjectposition1;
					}
				}
//						else
//						{
////							movingobjectposition2 = new MovingObjectPosition(startX, startY, startZ, b0, start, false);
//						}
			}
		}
//			System.out.println("debug" + timer);
		return null;
	}

	public static class MovingObjectPosition_andCounter{
		MovingObjectPosition value;
		int cnt;
		public MovingObjectPosition_andCounter(MovingObjectPosition value,int cnt){
			this.value = value;
			this.cnt = cnt;
		}
		public void overWrite(MovingObjectPosition value,int cnt){
			if(cnt < this.cnt) {
				this.value = value;
				this.cnt = cnt;
			}
		}
	}
	public static boolean getMovingObjectPosition_forBlock_CheckEmpty(World worldObj, Vec3 start, Vec3 end, int penerateCnt_in){
		penerateCnt = penerateCnt_in;
		return checkBlockAndCheckHit(worldObj,start,end,null) == null;
	}

	public static boolean getMovingObjectPosition_forBlock_CheckEmpty(World worldObj, Vec3 start, Vec3 end, int penerateCnt_in,final Block[] forcePenetrateList){
		penerateCnt = penerateCnt_in;
		return checkBlockAndCheckHit(worldObj,start,end,null) == null;
	}


	public static boolean isCollidableBlock(Block block){
		return isCollidableBlock(block,null);
	}
	public static boolean isCollidableBlock(Block block,Block[] forcePenetrateList){
		return isCollidableBlock(block,forcePenetrateList,null);
	}
	
	public static boolean isCollidableBlock(Block block,Block[] forcePenetrateList,Material[] forcePenetrateList2){
		return !(
						(block.getMaterial() == Material.air)
								/*||
								(block.getMaterial() == Material.plants)
								||
								(block.getMaterial() == Material.leaves)*///葉と草は透視できんな…
								|| 
								(block.getMaterial() == Material.glass)
								||
								(block.getMaterial() == Material.fire)
								|| 
								(block instanceof BlockLiquid)
								||
								
								((block instanceof BlockFence 
										|| 
										block instanceof BlockFenceGate || block == Blocks.iron_bars) 
										
										&& --penerateCnt>0)/*こっちは貫通回数減少*/) && checkAdditionalAvoidBlock(block,forcePenetrateList,forcePenetrateList2);
	}

	public static boolean checkAdditionalAvoidBlock(Block block,Block[] forcePenetrateList,Material[] forcePenetrateList2){
		if(forcePenetrateList != null) for(Block aBlock : forcePenetrateList){
			if(aBlock == block)return false;
		}
		if(forcePenetrateList2 != null) for(Material aMat : forcePenetrateList2){
			if(aMat == block.getMaterial())return false;
		}
		return true;
	}
}
