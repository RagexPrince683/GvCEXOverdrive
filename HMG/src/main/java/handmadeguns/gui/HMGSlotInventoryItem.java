package handmadeguns.gui;
 
import handmadeguns.items.*;
import handmadeguns.items.guns.HMGItem_Unified_Guns;
import handmadeguns.items.guns.HMGXItemGun_Sword;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class HMGSlotInventoryItem extends Slot
{
    public ItemStack gun = null;
    private final int attachmentSlot;

    public HMGSlotInventoryItem(IInventory p_i1824_1_, int p_i1824_2_, int p_i1824_3_, int p_i1824_4_) {
        super(p_i1824_1_, p_i1824_2_, p_i1824_3_, p_i1824_4_);
        this.attachmentSlot = -1;
    }
    public HMGSlotInventoryItem(IInventory p_i1824_1_, int p_i1824_2_, int p_i1824_3_, int p_i1824_4_,ItemStack gun) {
        super(p_i1824_1_, p_i1824_2_, p_i1824_3_, p_i1824_4_);
        this.gun = gun;
        this.attachmentSlot = p_i1824_2_;
    }
 
    /*
        このアイテムは動かせない、つかめないようにする。
     */
    @Override
    public boolean canTakeStack(EntityPlayer p_82869_1_)
    {

    	/*if(!(getHasStack() && getStack().getItem()instanceof HMGItemGun_SG)){
    		return !(getHasStack() && getStack().getItem()instanceof HMGItemGunBase);
    	}else if(!(getHasStack() && getStack().getItem()instanceof HMGItemGun_SG)){
    		return !(getHasStack() && getStack().getItem()instanceof HMGItemGunBase);
    	}else{
    		return !(getHasStack() && getStack().getItem()instanceof HMGItemGunBase);
    	}*/
    	return (getStack() != p_82869_1_.getHeldItem());
        
    }
    @Override
    public boolean isItemValid(ItemStack itemStack){
        if (itemStack == null || itemStack.getItem() == null) {
            return false;
        }

        // Player-inventory slots use this class too, but are not attachment destinations.
        if (attachmentSlot < 1 || attachmentSlot > 5) {
            return super.isItemValid(itemStack);
        }

        if (!canvalidthisItemtoSlot(itemStack, attachmentSlot)) {
            return false;
        }

        if (gun != null && gun.getItem() instanceof HMGItem_Unified_Guns) {
            HMGItem_Unified_Guns gunItem = (HMGItem_Unified_Guns) gun.getItem();
            if (gunItem.gunInfo.hasAttachRestriction) {
                for (String allowedAttachment : gunItem.gunInfo.attachwhitelist) {
                    if ("item.".concat(allowedAttachment).equals(itemStack.getItem().getUnlocalizedName())) {
                        return true;
                    }
                }
                return false;
            }
        }

        return true;
    }
    boolean canvalidthisItemtoSlot(ItemStack itemStack,int slot){
        if (itemStack == null || itemStack.getItem() == null) {
            return false;
        }
        switch (slot){
            case 0:
                return false;
            case 1:
                return itemStack.getItem() instanceof HMGItemSightBase;
            case 2:
                return itemStack.getItem() instanceof HMGItemAttachment_laser || itemStack.getItem() instanceof HMGItemAttachment_light;
            case 3:
                return itemStack.getItem() instanceof HMGItemAttachment_Suppressor
                        || itemStack.getItem() instanceof HMGItemAttachment_Muzzle;
            case 4:
                return itemStack.getItem() instanceof HMGItemAttachment_grip || itemStack.getItem() instanceof HMGItem_Unified_Guns || itemStack.getItem() instanceof HMGXItemGun_Sword;
            case 5:
                return itemStack.getItem() instanceof HMGItemBullet_AP
                        || itemStack.getItem() instanceof HMGItemBullet_AT
                        || itemStack.getItem() instanceof HMGItemBullet_Frag
                        || itemStack.getItem() instanceof HMGItemBullet_TE;
        }
        return false;
    }
}
