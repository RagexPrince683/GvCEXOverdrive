package com.lulan.shincolle.tileentity;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import com.lulan.shincolle.crafting.LargeRecipes;
import com.lulan.shincolle.entity.renderentity.EntityRenderVortex;
import com.lulan.shincolle.handler.ConfigHandler;
import com.lulan.shincolle.init.ModItems;
import com.lulan.shincolle.reference.ID;
import com.lulan.shincolle.reference.Reference;
import com.lulan.shincolle.utility.CalcHelper;
import com.lulan.shincolle.utility.TileEntityHelper;

/** Fuel Cost = BaseCost + CostPerMaterial * ( TotalMaterialAmount - minAmount * 4 )
 *  Total Build Time = FuelCost / buildSpeed
 *  MaxBuildTime / MaxFuelCost = 24min / 1382400  (48 fuel per tick)
 *  MinBuildTime / MinFuelCost = 8min / 460800
 * 	MaxMaterial / MaxFuelCost = 1000*4 / 1382400
 *  MinMaterial / MinFuelCost = 100*4 / 460800 = BaseCost(460800) CostPerMaterial(256)
 */
public class TileMultiGrudgeHeavy extends BasicTileMulti implements ITileLiquidFurnace, IFluidHandler {	
	
	//fluid tank
	private static final int TANKCAPA = FluidContainerRegistry.BUCKET_VOLUME;
	private static final Fluid F_LAVA = FluidRegistry.LAVA;
	private FluidTank tank = new FluidTank(new FluidStack(F_LAVA, 0), TANKCAPA);
	
	//furnace
	private int powerConsumed = 0;	//已花費的能量
	private int powerRemained = 0;	//剩餘燃料
	private int powerGoal = 0;		//需要達成的目標能量
	private int buildType = 0;		//type 0:none 1:ship 2:equip 3:ship loop 4: equip loop
	private int invMode = 0;		//物品欄模式 0:收物品 1:放出物品
	private int selectMat = 0;		//物品選擇模式, 用於物品輸出 0:grudge 1:abyss 2:ammo 3:poly
	private boolean isActive;		//是否正在建造中, 此為紀錄isBuilding是否有變化用
	private int[] matsBuild;		//建造材料量
	private int[] matsStock;		//庫存材料量
	public static int buildSpeed = 48;  		//power cost per tick
	public static final int POWERMAX = 1382400; //max power storage
	public static final int SLOTS_NUM = 10;
	public static final int SLOTS_OUT = 0;
	public static final int[] SLOTS_ALL = new int[] {0,2,3,4,5,6,7,8,9}; //slot 1 for fuel

	
	public TileMultiGrudgeHeavy() {
		super();
		
		//0:output 2~10:inventory
		this.slots = new ItemStack[SLOTS_NUM];
		this.isActive = false;
		this.matsBuild = new int[] {0,0,0,0};
		this.matsStock = new int[] {0,0,0,0};
		this.syncTime = 0;
		
		if(ConfigHandler.easyMode) {
			buildSpeed = 480;
		}
	}
	
	//依照輸出入口設定, 決定漏斗等裝置如何輸出入物品到特定slot中
	@Override
	public int[] getAccessibleSlotsFromSide(int side) {
		//type 1/2: large shipyard
		//slot 0:output 2~10:inventory
		//side 0:bottom 1:top 2~5:side
		if(this.structType == 1 || this.structType == 2) {
			return SLOTS_ALL;
		}
		return new int[] {};
	}
	
	//GUI顯示的名稱, 有custom name則用, 不然就用預設名稱
	@Override
	public String getInventoryName() {
		return this.hasCustomInventoryName() ? this.customName : "tile."+Reference.MOD_ID+":BlockLargeShipyard";
	}
	
	//是否可以右鍵點開方塊
	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		//由於會有多個tile entity副本, 要先確認座標相同的副本才能使用
		if (worldObj.getTileEntity(xCoord, yCoord, zCoord) != this) {
			return false;
		}
		else {	//確認player要在該tile entity 64格內, 以免超出讀取範圍 or 產生其他不明bug
			return player.getDistanceSq(xCoord+0.5D, yCoord+0.5D, zCoord+0.5D) <= 64;
		}
	}
	
	//讀取nbt資料
	@Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        tank.readFromNBT(compound);
        
        NBTTagList list = compound.getTagList("Items", 10);	//抓nbt tag: Items (此為類型10:TagCompound)
        
        for(int i=0; i<list.tagCount(); i++) {			//將tag列出的所有物品抓出來
            NBTTagCompound item = list.getCompoundTagAt(i);
            byte sid = item.getByte("Slot");
            
            if (sid>=0 && sid<slots.length) {	//讀取nbt紀錄的物品, 生成到各slot中 
            	slots[sid] = ItemStack.loadItemStackFromNBT(item);
            }
        }

        powerConsumed = compound.getInteger("powerConsumed");
        powerRemained = compound.getInteger("powerRemained");
        powerGoal = compound.getInteger("powerGoal");
        buildType = compound.getInteger("buildType");
        invMode = compound.getInteger("invMode");
        selectMat = compound.getInteger("selectMat");
        matsBuild = compound.getIntArray("matsBuild");
        matsStock = compound.getIntArray("matsStock");
    }
	
	//將資料寫進nbt
	@Override
	public void writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		tank.writeToNBT(compound);
		
		NBTTagList list = new NBTTagList();
		compound.setTag("Items", list);
		for(int i=0; i<slots.length; i++) {		//將slots[]資料寫進nbt
			if (slots[i] != null) {
				NBTTagCompound item = new NBTTagCompound();
				item.setByte("Slot", (byte)i);	//在tag: Slot下儲存資料i
				slots[i].writeToNBT(item);		//在tag: Slot下儲存slots[i]資料
				list.appendTag(item);			//增加下一個欄位
			}
		}
			
		compound.setInteger("powerConsumed", powerConsumed);
		compound.setInteger("powerRemained", powerRemained);
		compound.setInteger("powerGoal", powerGoal);
		compound.setInteger("buildType", buildType);
		compound.setInteger("invMode", invMode);
		compound.setInteger("selectMat", selectMat);
		compound.setIntArray("matsBuild", matsBuild);
		compound.setIntArray("matsStock", matsStock);
	}
	
	//判定物品是否能放入該格子, 用於canExtractItem等方法
	//格子用途:0:grudge 1:abyss 2:ammo 3:poly 4:fuel 5:output
	@Override
	public boolean isItemValidForSlot(int slot, ItemStack itemstack) {
		if(slot == 0) {	//output slot
			return false;
		}	
		return true;
	}
	
	//使用管線/漏斗輸出時呼叫, 不適用於手動置入
	@Override
	public boolean canExtractItem(int slot, ItemStack itemstack, int side) {
		return true;
	}
	
	//建造ship方法
	public void buildComplete() {
		//輸入材料數量, 取得build output到slot 5
		switch(this.buildType) {
		default:
		case ID.Build.SHIP:			//build ship
		case ID.Build.SHIP_LOOP:
			slots[0] = LargeRecipes.getBuildResultShip(matsBuild);
			break;
		case ID.Build.EQUIP:		//build equip
		case ID.Build.EQUIP_LOOP:
			slots[0] = LargeRecipes.getBuildResultEquip(matsBuild);
			break;
		}
	}
	
	//判定是否建造中
	public boolean isBuilding() {
		return hasPowerRemained() && canBuild();
	}
	
	//判定是否有燃料
	public boolean hasPowerRemained() {
		return powerRemained > buildSpeed;
	}
	
	//判定是否有建造目標
	public boolean canBuild() {
		return powerGoal > 0 && slots[0] == null;
	}
	
	//方塊的流程進行方法
	//資料必須以markDirty標記block更新, 以及讀寫NBT tag來保存
	@Override
	public void updateEntity() {
		//do not update if no structure
		if(this.getStructType() == 0) return;
		
		boolean sendUpdate = false;	//標紀要block update, 有要更新metadata時設為true
		
		//update goalPower
		if(this.buildType != 0) {
			this.powerGoal = LargeRecipes.calcGoalPower(matsBuild);
		}
		else {
			this.powerGoal = 0;
		}
		
		//server side
		if(!worldObj.isRemote) {
			//add item fuel
			if(TileEntityHelper.decrItemFuel(this)) {
				sendUpdate = true;
			}
			
			//add liquid fuel
			TileEntityHelper.decrLiquidFuel(this);
			
			//inventory mode 0:收入物品 1:放出物品
			if(invMode == 0) {  //ADD MODE
				for(int i = SLOTS_OUT + 1; i < SLOTS_NUM; i++) {
					//add material
					if(LargeRecipes.addMaterialStock(this, slots[i])) {
						slots[i].stackSize--;
						
						if(slots[i].stackSize == 0) {
							slots[i] = null;
						}
						
						sendUpdate = true;
						break;		//新增材料成功, 跳到下個tick
					}
				}
			}
			else {				//RELEASE MODE
				int compressNum = 9;	//output block
				int normalNum = 1;		//output single item
				
				//抽出物品的數量
				if(ConfigHandler.easyMode) {	
					compressNum = 90;
					normalNum = 10;
				}
				
				//放出block or container等壓縮型態
				if(getMatStock(selectMat) >= compressNum) {
					if(LargeRecipes.outputMaterialToSlot(this, selectMat, true)) {
						this.addMatStock(selectMat, -compressNum);
						sendUpdate = true;
					}
				}
				else if(getMatStock(selectMat) >= normalNum) {	//放出單件物品型態
					if(LargeRecipes.outputMaterialToSlot(this, selectMat, false)) {
						this.addMatStock(selectMat, -normalNum);
						sendUpdate = true;
					}
				}
			}

			//判定是否建造中, 每tick進行進度值更新, 若非建造中則重置進度值
			if(this.isBuilding()) {
				this.syncTime++;
				this.powerRemained -= buildSpeed;	//fuel bar --
				this.powerConsumed += buildSpeed;	//build bar ++
				
				//消耗高速建造材料
				for(int i = SLOTS_OUT + 1; i < SLOTS_NUM; i++) {
					if(slots[i] != null && slots[i].getItem() == ModItems.InstantConMat) {
						slots[i].stackSize--;
						this.powerConsumed += 57600;
						
						if(this.slots[i].stackSize == 0) {
							this.slots[i] = null;
						}
						
						sendUpdate = true;
						break;
					}
				}
				
				//sync render entity every 60 ticks if building
				if(this.syncTime > 60) {
					this.sendSyncPacket();
					this.syncTime = 0;
					sendUpdate = true;
				}
				
				//power達標, 建造完成
				if (this.powerConsumed >= this.powerGoal) {
					this.buildComplete();	//建造出成品放到output slot
					this.powerConsumed = 0;
					this.powerGoal = 0;

					//continue build if mode = loop mode
					switch(buildType) {
					default:
					case ID.Build.SHIP:
					case ID.Build.EQUIP:		//reset build type
						this.buildType = ID.Build.NONE;
						//將建造材料清除
						matsBuild[0] = 0;
						matsBuild[1] = 0;
						matsBuild[2] = 0;
						matsBuild[3] = 0;
						break;
					case ID.Build.SHIP_LOOP:	//remain build type
					case ID.Build.EQUIP_LOOP:	//remain build type
						this.setRepeatBuild();
						break;
					}

					sendUpdate = true;
				}
			}			
			
			if(!this.canBuild()) {	//非建造中, 重置build bar
				this.powerConsumed = 0;
			}
			
			//若狀態有改變過, 則發送更新
			if(this.isActive != this.isBuilding()) {
				this.isActive = this.isBuilding();
				
				//set render entity state
				AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(xCoord-1.5D, yCoord-1.5D, zCoord-1.5D, xCoord+1.5D, yCoord+0.5D, zCoord+1.5D);
				List renderEntityList = this.worldObj.getEntitiesWithinAABB(EntityRenderVortex.class, aabb);
				
	            for(int i = 0; i < renderEntityList.size(); i++) { 
	            	((EntityRenderVortex)renderEntityList.get(i)).setIsActive(this.isBuilding());
	            }
	            
	            //sync to client
	            this.sendSyncPacket();
			}
		}
		
//		//mark block update, no use for now (only for block metadata changed)
//		if(sendUpdate) {
//			this.markDirty();
//		}
	}
	
	//set materials for repeat build
	public void setRepeatBuild() {
		//set materials
		for(int i = 0; i < 4; i++) {
			//has enough materials
			if(matsStock[i] >= matsBuild[i]) {
				matsStock[i] -= matsBuild[i];
			}
			//no materials, reset matsBuild
			else {
				matsBuild[i] = 0;
				buildType = ID.Build.NONE;
			}
		}
	}

	//計算fuel存量條
	public int getPowerRemainingScaled(int i) {
		return (powerRemained * i) / POWERMAX;
	}
	
	//計算建造時間 (換算成真實時間)
	public String getBuildTimeString() {
		//剩餘秒數 = (目標能量 - 目前能量) / (每tick增加能量) / 20
		int timeSec = (powerGoal - powerConsumed) / buildSpeed / 20;	//get time (單位: sec)		
		return CalcHelper.getTimeFormated(timeSec);
	}
	
	//getter
	@Override
	public int getPowerConsumed() {
		return powerConsumed;
	}
	@Override
	public int getPowerRemained() {
		return powerRemained;
	}
	@Override
	public int getPowerGoal() {
		return powerGoal;
	}
	@Override
	public int getPowerMax() {
		return POWERMAX;
	}
	public int getBuildType() {
		return buildType;
	}
	public int getInvMode() {
		return invMode;
	}
	public int getSelectMat() {
		return selectMat;
	}
	public int getMatBuild(int id) {
		return matsBuild[id];
	}
	public int getMatStock(int id) {
		return matsStock[id];
	}
	
	//setter
	@Override
	public void setPowerConsumed(int par1) {
		this.powerConsumed = par1;
	}
	@Override
	public void setPowerRemained(int par1) {
		this.powerRemained = par1;
	}
	@Override
	public void setPowerGoal(int par1) {
		this.powerGoal = par1;
	}
	@Override
	public void setPowerMax(int par1) {}
	
	public void setBuildType(int par1) {
		this.buildType = par1;
	}
	public void setInvMode(int par1) {
		this.invMode = par1;
	}
	public void setSelectMat(int par1) {
		this.selectMat = par1;
	}
	public void setMatBuild(int id, int par1) {
		this.matsBuild[id] = par1;
	}
	public void setMatStock(int id, int par1) {
		this.matsStock[id] = par1;
	}
	public void addMatBuild(int id, int par1)  {	//add a number to build
		this.matsBuild[id] += par1;
	}
	public void addMatStock(int id, int par1)  {	//add a number to stock
		this.matsStock[id] += par1;
	}

	//fuel input slot (1~9)
	@Override
	public int getFuelSlotMin() {
		return SLOTS_OUT+1;
	}

	//fuel input slot (1~9)
	@Override
	public int getFuelSlotMax() {
		return SLOTS_NUM-1;
	}

	//only accept LAVA
	@Override
	public int fill(ForgeDirection from, FluidStack fluid, boolean doFill) {
//		//show fill animation
//		if(amount > 0 && doFill) { 
//			waterheight = resource.amount; 
//			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord); 
//		}
		if(TileEntityHelper.checkLiquidIsLava(fluid)) {
			return tank.fill(fluid, doFill);
		}
		
		return 0;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
//		if(resource == null || !resource.isFluidEqual(tank.getFluid())) {
//            return null;
//        }
//        return tank.drain(resource.amount, doDrain);
		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
//		return tank.drain(maxDrain, doDrain);
		return null;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) {
		if(TileEntityHelper.checkLiquidIsLava(fluid)) {
			return true;
		}
		
		return false;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid) {
		return false;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from) {
		return new FluidTankInfo[] { tank.getInfo() };
	}

	@Override
	public int getFluidFuelAmount() {
		return this.tank.getFluidAmount();
	}

	@Override
	public FluidStack drainFluidFuel(int amount) {
		return this.tank.drain(amount, true);
	}

	
}
