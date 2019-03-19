package com.lulan.shincolle.client.gui.inventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import com.lulan.shincolle.tileentity.TileEntitySmallShipyard;

public class SlotSmallShipyard extends Slot {
	
	private int slotIndex;
	private TileEntitySmallShipyard tile;

	public SlotSmallShipyard(IInventory entity, int slotIndex, int x, int y) {
		super(entity, slotIndex, x, y);
		this.slotIndex = slotIndex;
		this.tile = (TileEntitySmallShipyard)entity;
	}
	
	//設定每個slot可以放進的物品: 0:grudge 1:abyssium 2:ammo 3:polymetal 4:fuel 5:output
	@Override
	public boolean isItemValid(ItemStack itemstack) {
		return tile.isItemValidForSlot(this.slotIndex, itemstack);
    }

	
}
