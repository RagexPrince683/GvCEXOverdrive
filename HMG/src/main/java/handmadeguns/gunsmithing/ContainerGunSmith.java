// ContainerGunSmith.java (common code)
package handmadeguns.gunsmithing;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

// Very small container just to satisfy server<->client GUI container requirement.
// You can expand it later to actually hold slots if you need to.
public class ContainerGunSmith extends Container {
    private final InventoryPlayer playerInv;

    public ContainerGunSmith(InventoryPlayer playerInventory) {
        this.playerInv = playerInventory;
        // if you want, add player inventory slots so the GUI can render/use them:
        // for chest-like GUIs you'd add player inventory slots here. For now we don't need custom slots.
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        // Always allow until you implement real range checks
        return true;
    }


    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        return null;
    }
}

