package com.lulan.shincolle.client.gui.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import com.lulan.shincolle.entity.BasicEntityShip;
import com.lulan.shincolle.item.BasicEquip;
import com.lulan.shincolle.reference.ID;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**CUSTOM SHIP INVENTORY
 * slot: S0(136,18) S1(136,36) S2(136,54) S3(136,72) S4(136,90) S5(6,108) S6~S23(8,18) 6x3
 * player inventory(44,132) hotbar(44,190)
 * S0~S5 for equip only
 */
public class ContainerShipInventory extends Container {
	
	private BasicEntityShip entity;
	public static final byte SLOTS_PLAYERINV = 24;  //player inventory start id
	public static final byte SLOTS_SHIPINV = 6;		//ship inventory start id
	private int GuiKills, GuiExpCurrent, GuiNumAmmo, GuiNumAmmoHeavy, GuiNumGrudge, 
	            GuiNumAirLight, GuiNumAirHeavy, GuiIsMarried, GuiMorale, GuiInvSize,
	            ButtonMelee, ButtonAmmoLight, ButtonAmmoHeavy, ButtonAirLight, ButtoAirHeavy,
	            FollowMin, FollowMax, FleeHP, TarAI, AuraEffect, OnSightAI, PVPAI, AAAI, ASMAI,
	            TIMEKEEPAI, ShowPage, PICKAI, WpStay;
	
	public ContainerShipInventory(InventoryPlayer invPlayer, BasicEntityShip entity1) {
		int i, j;
		this.entity = entity1;
		
		//ship equip = 0~5
		for(i = 0; i < 6; i++) {
			this.addSlotToContainer(new SlotShipInventory(entity1.getExtProps(), i, 144, 18+i*18));
		}
		
		//ship inventory = 6~24
		for(i = 0; i < 6; i++) {
			for(j = 0; j < 3; j++) {
				this.addSlotToContainer(new SlotShipInventory(entity1.getExtProps(), j+i*3+6, 8+j* 18, 18+i*18));
			}
		}
		
		//player inventory
		for(i = 0; i < 3; i++) {
			for(j = 0; j < 9; j++) {
				this.addSlotToContainer(new Slot(invPlayer, j+i*9+9, 8+j*18, 132+i*18));
			}
		}
		
		//player action bar (hot bar)
		for(i = 0; i < 9; i++) {
			this.addSlotToContainer(new Slot(invPlayer, i, 8+i*18, 190));
		}
	}
	
	@Override
	public boolean canInteractWith(EntityPlayer p_75145_1_) {
		return true;
	}
	
	/**使container支援shift點物品的動作
	 * shift點人物背包中的物品->判定物品類型送到指定格子, 點container中的物品->送到人物背包
	 * mergeItemStack: parm: item,start slot,end slot(此格不判定放入),是否由hotbar開始判定
	 * slot id: 0~4:equip  5~22:ship inventory 
	 *          23~49:player inventory  50~58:hot bar
	 *          
	 * Click: slot 0~5   (Equip)   -> put in slot 6~59 (ShipInv & Player)
	 *        slot 6~23  (ShipInv) -> if equip -> slot 0~5 (Equip)
	 *                             -> if other -> slot 24~59 (Player)
	 *        slot 24~59 (Player)  -> if equip -> slot 0~5 (Equip)
	 *        					   -> if other -> slot 6~23 (ShipInv)
	 *        
	 * Equip slot check in SlotShipInventory.class 
	 */
	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotid) {
        ItemStack itemstack = null;
        Slot slot = (Slot)this.inventorySlots.get(slotid);
        boolean isEquip = false;
        int slotsEnd = SLOTS_PLAYERINV + 36;

        if(slot != null && slot.getHasStack()) { 			//若slot有東西
            ItemStack itemstack1 = slot.getStack();			//itemstack1取得該slot物品
            itemstack = itemstack1.copy();					//itemstack複製一份itemstack1
            
            if(itemstack1.getItem() instanceof BasicEquip) isEquip = true;	//判定是否為equip

            if(slotid < SLOTS_SHIPINV) {  		//click equip slot
            	if(!this.mergeItemStack(itemstack1, SLOTS_SHIPINV, slotsEnd, true)) { //take out equip
                	return null;
                }	
                slot.onSlotChange(itemstack1, itemstack); //若物品成功搬動過, 則呼叫slot change事件
            }
            else {					//slot is ship or player inventory (5~58)
            	if(slotid < SLOTS_PLAYERINV) {	//if ship inventory (0~23)
            		if(isEquip) {	//把equip塞進slot 0~4, 塞不下則放player inventory (24~58)
            			if(!this.mergeItemStack(itemstack1, 0, SLOTS_SHIPINV, false)) {
                			if(!this.mergeItemStack(itemstack1, SLOTS_PLAYERINV, slotsEnd, true)) {
                				return null;
                			}			
                        }
            		}  
            		else {			//non-equip, put into player inventory (23~58)
            			if(!this.mergeItemStack(itemstack1, SLOTS_PLAYERINV, slotsEnd, true)) {
            				return null;
            			}
            		}
            	}
            	else {				//if player inventory (23~58)
            		if(isEquip) {	//把equip塞進slot 0~4, 塞不下則放ship inventory (5~22)
            			if(!this.mergeItemStack(itemstack1, 0, SLOTS_SHIPINV, false)) {
                			if(!this.mergeItemStack(itemstack1, SLOTS_SHIPINV, SLOTS_PLAYERINV, true)) {
                				return null;
                			}			
                        }
            		} 
            		else {			//non-equip, put into ship inventory (5~22)
            			if(!this.mergeItemStack(itemstack1, SLOTS_SHIPINV, SLOTS_PLAYERINV, false)) {
            				return null;
            			}
            		}
            	}
            }

            //如果物品都放完了, 則設成null清空該物品
            if (itemstack1.stackSize <= 0) {
                slot.putStack((ItemStack)null);
            }
            else { //還沒放完, 先跑一次slot update
                slot.onSlotChanged();
            }

            //如果itemstack的數量跟原先的數量相同, 表示都不能移動物品
            if (itemstack1.stackSize == itemstack.stackSize) {
                return null;
            }
            //最後再發送一次slot update
            slot.onPickupFromSlot(player, itemstack1);
        }
        return itemstack;	//物品移動完成, 回傳剩下的物品
    }
	
	//發送更新gui進度條更新, 比detectAndSendChanges還要優先(在此放置init方法等)
	@Override
	public void addCraftingToCrafters (ICrafting crafting) {
		super.addCraftingToCrafters(crafting);
		crafting.sendProgressBarUpdate(this, 1, this.entity.getStateMinor(ID.M.ExpCurrent));
		crafting.sendProgressBarUpdate(this, 2, this.entity.getStateMinor(ID.M.NumAmmoLight));
		crafting.sendProgressBarUpdate(this, 3, this.entity.getStateMinor(ID.M.NumAmmoHeavy));
		crafting.sendProgressBarUpdate(this, 5, this.entity.getStateMinor(ID.M.NumAirLight));
		crafting.sendProgressBarUpdate(this, 6, this.entity.getStateMinor(ID.M.NumAirHeavy));
		crafting.sendProgressBarUpdate(this, 7, this.entity.getStateFlagI(ID.F.UseMelee));
		crafting.sendProgressBarUpdate(this, 8, this.entity.getStateFlagI(ID.F.UseAmmoLight));
		crafting.sendProgressBarUpdate(this, 9, this.entity.getStateFlagI(ID.F.UseAmmoHeavy));
		crafting.sendProgressBarUpdate(this, 10, this.entity.getStateFlagI(ID.F.UseAirLight));
		crafting.sendProgressBarUpdate(this, 11, this.entity.getStateFlagI(ID.F.UseAirHeavy));
		crafting.sendProgressBarUpdate(this, 12, this.entity.getStateFlagI(ID.F.IsMarried));
		crafting.sendProgressBarUpdate(this, 13, this.entity.getStateMinor(ID.M.FollowMin));
		crafting.sendProgressBarUpdate(this, 14, this.entity.getStateMinor(ID.M.FollowMax));
		crafting.sendProgressBarUpdate(this, 15, this.entity.getStateMinor(ID.M.FleeHP));
		crafting.sendProgressBarUpdate(this, 16, this.entity.getStateFlagI(ID.F.PassiveAI));
		crafting.sendProgressBarUpdate(this, 17, this.entity.getStateFlagI(ID.F.UseRingEffect));
		crafting.sendProgressBarUpdate(this, 18, this.entity.getStateFlagI(ID.F.OnSightChase));
		crafting.sendProgressBarUpdate(this, 19, this.entity.getStateFlagI(ID.F.PVPFirst));
		crafting.sendProgressBarUpdate(this, 20, this.entity.getStateFlagI(ID.F.AntiAir));
		crafting.sendProgressBarUpdate(this, 21, this.entity.getStateFlagI(ID.F.AntiSS));
		crafting.sendProgressBarUpdate(this, 22, this.entity.getStateFlagI(ID.F.TimeKeeper));
		crafting.sendProgressBarUpdate(this, 23, this.entity.getStateMinor(ID.M.Morale));
		crafting.sendProgressBarUpdate(this, 24, this.entity.getStateMinor(ID.M.InvSize));
		crafting.sendProgressBarUpdate(this, 25, this.entity.getStateFlagI(ID.F.PickItem));
		crafting.sendProgressBarUpdate(this, 26, this.entity.getStateMinor(ID.M.WpStay));
	}
	
	//偵測數值是否改變, 有改變時發送更新(此為server端偵測)
	@Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();
		int getValue;
		float getValueF;
		
		if(this.GuiKills != this.entity.getStateMinor(ID.M.Kills) ||
		   this.GuiNumGrudge != this.entity.getStateMinor(ID.M.NumGrudge) ||
		   this.ShowPage != this.entity.getExtProps().getInventoryPage()) {
			//send sync packet
			this.entity.sendGUISyncPacket();
			
			//update value
			this.GuiKills = this.entity.getStateMinor(ID.M.Kills);
			this.GuiNumGrudge = this.entity.getStateMinor(ID.M.NumGrudge);
			this.ShowPage = this.entity.getExtProps().getInventoryPage();
		}
		
        for(Object crafter : this.crafters) {
            ICrafting icrafting = (ICrafting) crafter;
  
            getValue = this.entity.getStateMinor(ID.M.ExpCurrent);
            if(this.GuiExpCurrent != getValue) {
                icrafting.sendProgressBarUpdate(this, 1, getValue);
                this.GuiExpCurrent = getValue;
            }
            getValue = this.entity.getStateMinor(ID.M.NumAmmoLight);
            if(this.GuiNumAmmo != getValue) {
                icrafting.sendProgressBarUpdate(this, 2, getValue);
                this.GuiNumAmmo = getValue;
            }
            getValue = this.entity.getStateMinor(ID.M.NumAmmoHeavy);
            if(this.GuiNumAmmoHeavy != getValue) {
                icrafting.sendProgressBarUpdate(this, 3, getValue);
                this.GuiNumAmmoHeavy = getValue;
            }
            getValue = this.entity.getStateMinor(ID.M.NumAirLight);
            if(this.GuiNumAirLight != getValue) {
                icrafting.sendProgressBarUpdate(this, 5, getValue);
                this.GuiNumAirLight = getValue;
            }
            getValue = this.entity.getStateMinor(ID.M.NumAirHeavy);
            if(this.GuiNumAirHeavy != getValue) {
                icrafting.sendProgressBarUpdate(this, 6, getValue);
                this.GuiNumAirHeavy = getValue;
            }
            getValue = this.entity.getStateFlagI(ID.F.UseMelee);
            if(this.ButtonMelee != getValue) {
                icrafting.sendProgressBarUpdate(this, 7, getValue);
                this.ButtonMelee = getValue;
            }
            getValue = this.entity.getStateFlagI(ID.F.UseAmmoLight);
            if(this.ButtonAmmoLight != getValue) {
                icrafting.sendProgressBarUpdate(this, 8, getValue);
                this.ButtonAmmoLight = getValue;
            }
            getValue = this.entity.getStateFlagI(ID.F.UseAmmoHeavy);
            if(this.ButtonAmmoHeavy != getValue) {
                icrafting.sendProgressBarUpdate(this, 9, getValue);
                this.ButtonAmmoHeavy = getValue;
            }
            getValue = this.entity.getStateFlagI(ID.F.UseAirLight);
            if(this.ButtonAirLight != getValue) {
                icrafting.sendProgressBarUpdate(this, 10, getValue);
                this.ButtonAirLight = getValue;
            }
            getValue = this.entity.getStateFlagI(ID.F.UseAirHeavy);
            if(this.ButtoAirHeavy != getValue) {
                icrafting.sendProgressBarUpdate(this, 11, getValue);
                this.ButtoAirHeavy = getValue;
            }
            getValue = this.entity.getStateFlagI(ID.F.IsMarried);
            if(this.GuiIsMarried != getValue) {
                icrafting.sendProgressBarUpdate(this, 12, getValue);
                this.GuiIsMarried = getValue;
            }
            getValue = this.entity.getStateMinor(ID.M.FollowMin);
            if(this.FollowMin != getValue) {
                icrafting.sendProgressBarUpdate(this, 13, getValue);
                this.FollowMin = getValue;
            }
            getValue = this.entity.getStateMinor(ID.M.FollowMax);
            if(this.FollowMax != getValue) {
                icrafting.sendProgressBarUpdate(this, 14, getValue);
                this.FollowMax = getValue;
            }
            getValue = this.entity.getStateMinor(ID.M.FleeHP);
            if(this.FleeHP != getValue) {
                icrafting.sendProgressBarUpdate(this, 15, getValue);
                this.FleeHP = getValue;
            }
            getValue = this.entity.getStateFlagI(ID.F.PassiveAI);
            if(this.TarAI != getValue) {
                icrafting.sendProgressBarUpdate(this, 16, getValue);
                this.TarAI = getValue;
            }
            getValue = this.entity.getStateFlagI(ID.F.UseRingEffect);
            if(this.AuraEffect != getValue) {
                icrafting.sendProgressBarUpdate(this, 17, getValue);
                this.AuraEffect = getValue;
            }
            getValue = this.entity.getStateFlagI(ID.F.OnSightChase);
            if(this.OnSightAI != getValue) {
                icrafting.sendProgressBarUpdate(this, 18, getValue);
                this.OnSightAI = getValue;
            }
            getValue = this.entity.getStateFlagI(ID.F.PVPFirst);
            if(this.PVPAI != getValue) {
                icrafting.sendProgressBarUpdate(this, 19, getValue);
                this.PVPAI = getValue;
            }
            getValue = this.entity.getStateFlagI(ID.F.AntiAir);
            if(this.AAAI != getValue) {
                icrafting.sendProgressBarUpdate(this, 20, getValue);
                this.AAAI = getValue;
            }
            getValue = this.entity.getStateFlagI(ID.F.AntiSS);
            if(this.ASMAI != getValue) {
                icrafting.sendProgressBarUpdate(this, 21, getValue);
                this.ASMAI = getValue;
            }
            getValue = this.entity.getStateFlagI(ID.F.TimeKeeper);
            if(this.TIMEKEEPAI != getValue) {
                icrafting.sendProgressBarUpdate(this, 22, getValue);
                this.TIMEKEEPAI = getValue;
            }
            getValue = this.entity.getStateMinor(ID.M.Morale);
            if(this.GuiMorale != getValue) {
                icrafting.sendProgressBarUpdate(this, 23, getValue);
                this.GuiMorale = getValue;
            }
            getValue = this.entity.getStateMinor(ID.M.InvSize);
            if(this.GuiInvSize != getValue) {
                icrafting.sendProgressBarUpdate(this, 24, getValue);
                this.GuiInvSize = getValue;
            }
            getValue = this.entity.getStateFlagI(ID.F.PickItem);
            if(this.PICKAI != getValue) {
                icrafting.sendProgressBarUpdate(this, 25, getValue);
                this.PICKAI = getValue;
            }
            getValue = this.entity.getStateMinor(ID.M.WpStay);
            if(this.WpStay != getValue) {
                icrafting.sendProgressBarUpdate(this, 26, getValue);
                this.WpStay = getValue;
            }
        }
    }
	
	//client端container接收新值
	@Override
	@SideOnly(Side.CLIENT)
    public void updateProgressBar(int valueType, int updatedValue) {     
		switch(valueType) {
		case 1:
			this.entity.setStateMinor(ID.M.ExpCurrent, updatedValue);
			break;
		case 2:
			this.entity.setStateMinor(ID.M.NumAmmoLight, updatedValue);
			break;
		case 3:
			this.entity.setStateMinor(ID.M.NumAmmoHeavy, updatedValue);
			break;
		case 5:
			this.entity.setStateMinor(ID.M.NumAirLight, updatedValue);
			break;
		case 6:
			this.entity.setStateMinor(ID.M.NumAirHeavy, updatedValue);
			break;
		case 7:
			this.entity.setEntityFlagI(ID.F.UseMelee, updatedValue);
			break;
		case 8:
			this.entity.setEntityFlagI(ID.F.UseAmmoLight, updatedValue);
			break;
		case 9:
			this.entity.setEntityFlagI(ID.F.UseAmmoHeavy, updatedValue);
			break;
		case 10:
			this.entity.setEntityFlagI(ID.F.UseAirLight, updatedValue);
			break;
		case 11:
			this.entity.setEntityFlagI(ID.F.UseAirHeavy, updatedValue);
			break;
		case 12:
			this.entity.setEntityFlagI(ID.F.IsMarried, updatedValue);
			break;
		case 13:
			this.entity.setStateMinor(ID.M.FollowMin, updatedValue);
			break;
		case 14:
			this.entity.setStateMinor(ID.M.FollowMax, updatedValue);
			break;
		case 15:
			this.entity.setStateMinor(ID.M.FleeHP, updatedValue);
			break;
		case 16:
			this.entity.setEntityFlagI(ID.F.PassiveAI, updatedValue);
			break;
		case 17:
			this.entity.setEntityFlagI(ID.F.UseRingEffect, updatedValue);
			break;
		case 18:
			this.entity.setEntityFlagI(ID.F.OnSightChase, updatedValue);
			break;
		case 19:
			this.entity.setEntityFlagI(ID.F.PVPFirst, updatedValue);
			break;
		case 20:
			this.entity.setEntityFlagI(ID.F.AntiAir, updatedValue);
			break;
		case 21:
			this.entity.setEntityFlagI(ID.F.AntiSS, updatedValue);
			break;
		case 22:
			this.entity.setEntityFlagI(ID.F.TimeKeeper, updatedValue);
			break;
		case 23:
			this.entity.setStateMinor(ID.M.Morale, updatedValue);
			break;
		case 24:
			this.entity.setStateMinor(ID.M.InvSize, updatedValue);
			break;
		case 25:
			this.entity.setEntityFlagI(ID.F.PickItem, updatedValue);
			break;
		case 26:
			this.entity.setStateMinor(ID.M.WpStay, updatedValue);
			break;
		}
    }

}
