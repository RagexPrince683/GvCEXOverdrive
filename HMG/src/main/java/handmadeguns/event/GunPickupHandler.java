package handmadeguns.event;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import handmadeguns.items.guns.HMGItem_Unified_Guns;

public class GunPickupHandler {

    private static final int MAX_GUNS = 2;

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player.worldObj.isRemote) return;

        EntityPlayer player = event.player;

        // DEBUG — you should see this spam in the server log
        System.out.println("[HG] PlayerTick firing for " + player.getCommandSenderName());

        int gunCount = 0;

        for (ItemStack stack : player.inventory.mainInventory) {
            if (stack != null && stack.getItem() instanceof HMGItem_Unified_Guns) {
                gunCount += stack.stackSize;
            }
        }

        if (gunCount <= MAX_GUNS) return;

        for (int i = 0; i < player.inventory.mainInventory.length && gunCount > MAX_GUNS; i++) {
            ItemStack stack = player.inventory.mainInventory[i];
            if (stack == null) continue;
            if (!(stack.getItem() instanceof HMGItem_Unified_Guns)) continue;

            while (stack.stackSize > 0 && gunCount > MAX_GUNS) {
                ItemStack drop = stack.splitStack(1);
                gunCount--;
                player.dropPlayerItemWithRandomChoice(drop, false);
            }

            if (stack.stackSize <= 0) {
                player.inventory.mainInventory[i] = null;
            }
        }

        player.inventory.markDirty();
    }
}
