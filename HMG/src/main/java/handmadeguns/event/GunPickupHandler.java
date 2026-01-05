package handmadeguns.event;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import handmadeguns.items.guns.HMGItem_Unified_Guns;

public class GunPickupHandler {
    //todo add config option for max and disabled, also dont do for creative players

    @SubscribeEvent
    public void onItemPickup(EntityItemPickupEvent event) {
        EntityPlayer player = event.entityPlayer;
        ItemStack pickedUp = event.item.getEntityItem();

        if (!(pickedUp.getItem() instanceof HMGItem_Unified_Guns))
            return;

        int count = 0;

        for (ItemStack stack : player.inventory.mainInventory) {
            if (stack != null && stack.getItem() == pickedUp.getItem()) {
                count += stack.stackSize;
            }
        }

        if (count >= 2) {
            event.setCanceled(true);

            if (!player.worldObj.isRemote) {
                event.item.setPosition(player.posX, player.posY, player.posZ);
                event.item.motionX = 0;
                event.item.motionY = 0.1;
                event.item.motionZ = 0;
            }
        }
    }
}
