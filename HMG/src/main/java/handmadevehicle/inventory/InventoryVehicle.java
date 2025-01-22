package handmadevehicle.inventory;

import handmadevehicle.entity.parts.logics.BaseLogic;
import handmadevehicle.entity.parts.turrets.TurretObj;
import handmadevehicle.network.HMVPacketHandler;
import handmadevehicle.network.packets.HMVPacketSyncInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class InventoryVehicle implements IInventory {
	public ItemStack[] items;
	public BaseLogic baseLogic;
	public boolean needSync = true;


	public InventoryVehicle(BaseLogic baseLogic)
	{
		this.baseLogic = baseLogic;
		
		//currentItem = inventoryPlayer.getCurrentItem();
		//currentItem = stack;
		
		//InventorySize
		//items = new ItemStack[baseLogic.prefab_vehicle.weaponSlotNum+baseLogic.prefab_vehicle.cargoSlotNum];
	}
	
	@Override
	public int getSizeInventory()
	{
		return items.length;
	}
	
	@Override
	public ItemStack getStackInSlot(int slot)
	{
		return items[slot];
	}
	
	@Override
	public ItemStack decrStackSize(int p_70298_1_, int p_70298_2_)
	{
		if (this.items[p_70298_1_] != null)
		{
			ItemStack itemstack;
			
			if (this.items[p_70298_1_].stackSize <= p_70298_2_)
			{
				itemstack = this.items[p_70298_1_];
				this.items[p_70298_1_] = null;
				this.markDirty();
				return itemstack;
			}
			else
			{
				itemstack = this.items[p_70298_1_].splitStack(p_70298_2_);
				
				if (this.items[p_70298_1_].stackSize == 0)
				{
					this.items[p_70298_1_] = null;
				}
				
				this.markDirty();
				return itemstack;
			}
		}
		return null;
	}
	
	@Override
	public ItemStack getStackInSlotOnClosing(int p_70304_1_)
	{
		if (this.items[p_70304_1_] != null)
		{
			ItemStack itemstack = this.items[p_70304_1_];
			this.items[p_70304_1_] = null;
			return itemstack;
		}
		return null;
	}
	
	@Override
	public void setInventorySlotContents(int p_70299_1_, ItemStack p_70299_2_)
	{
		this.items[p_70299_1_] = p_70299_2_;
		if (p_70299_2_ != null && p_70299_2_.stackSize > this.getInventoryStackLimit(p_70299_1_)) {
			p_70299_2_.stackSize = this.getInventoryStackLimit(p_70299_1_);
		}

		this.markDirty();
	}
	
	@Override
	public String getInventoryName()
	{
		return "InventoryItem";
	}
	
	@Override
	public boolean hasCustomInventoryName()
	{
		return false;
	}
	
	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}

	public int getInventoryStackLimit(int slotID)
	{
		//if(slotID<baseLogic.prefab_vehicle.weaponSlotNum){
		//	return 1;
		//}
		return 64;
	}
	
	@Override
	public void markDirty() {
		needSync = true;
	}
	
	@Override
	public boolean isUseableByPlayer(EntityPlayer p_70300_1_)
	{
		return true;
	}
	
	@Override
	public void openInventory()
	{

	}
	
	@Override
	public void closeInventory()
	{
		baseLogic.saveToTag(baseLogic.mc_Entity.getEntityData());
		baseLogic.readFromTag(baseLogic.mc_Entity.getEntityData());
		HMVPacketHandler.INSTANCE.sendToAll(new HMVPacketSyncInventory(baseLogic.mc_Entity));
	}
	@Override
	public boolean isItemValidForSlot(int slotID, ItemStack checkingStack)
	{
		//if(slotID<baseLogic.prefab_vehicle.weaponSlotNum && checkingStack != null){
		//	TurretObj turretObj = baseLogic.allturrets[baseLogic.prefab_vehicle.weaponSlot_linkedTurretID.get(slotID)];
		//	if(!turretObj.prefab_turret.canReloadAirBone && !baseLogic.mc_Entity.onGround)
		//		return false;
		//	String itemName = checkingStack.getUnlocalizedName();
		//	if(baseLogic.prefab_vehicle.weaponSlot_linkedTurret_StackWhiteList.get(slotID) == null)return true;
//		//	System.out.println("itemName " + itemName);
		//	for(String whiteList: baseLogic.prefab_vehicle.weaponSlot_linkedTurret_StackWhiteList.get(slotID)) {
//		//		System.out.println("whiteList " + whiteList);
		//		if("item.".concat(whiteList).equals(itemName)){
		//			return true;
		//		}
		//	}
		//	return false;
		//}
		return true;
	}
}
