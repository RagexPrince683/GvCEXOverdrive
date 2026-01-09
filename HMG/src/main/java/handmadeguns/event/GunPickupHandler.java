package handmadeguns.event;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import handmadeguns.items.guns.HMGItem_Unified_Guns;
import net.minecraftforge.common.util.FakePlayer;

import static handmadeguns.HandmadeGunsCore.MAXGUNSINV;

public class GunPickupHandler {

    private static final int MAX_GUNS = MAXGUNSINV;
    private static final int MAX_GRENADE = 3;

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {

        if (event.phase != TickEvent.Phase.END) return;
        if (event.player.worldObj.isRemote) return;

        EntityPlayer player = event.player;

        // ignore creative
        if (player.capabilities.isCreativeMode) return;
        if (player instanceof FakePlayer) return;


        int gunCount = 0;
        int grenadeCount = 0;

        // ---- COUNT ----
        for (ItemStack stack : player.inventory.mainInventory) {
            if (stack == null) continue;
            if (!(stack.getItem() instanceof HMGItem_Unified_Guns)) continue;

            HMGItem_Unified_Guns gun = (HMGItem_Unified_Guns) stack.getItem();

            if (gun.gunInfo != null && gun.gunInfo.grenade) {
                grenadeCount += stack.stackSize;
            } else {
                gunCount += stack.stackSize;
            }
        }

        // ---- EARLY EXIT ----
        if (gunCount <= MAX_GUNS && grenadeCount <= MAX_GRENADE) return;

        // ---- ENFORCE GRENADE LIMIT ----
        for (int i = 0; i < player.inventory.mainInventory.length && grenadeCount > MAX_GRENADE; i++) {
            ItemStack stack = player.inventory.mainInventory[i];
            if (stack == null) continue;
            if (!(stack.getItem() instanceof HMGItem_Unified_Guns)) continue;

            HMGItem_Unified_Guns gun = (HMGItem_Unified_Guns) stack.getItem();
            if (gun.gunInfo == null || !gun.gunInfo.grenade) continue;

            while (stack.stackSize > 0 && grenadeCount > MAX_GRENADE) {
                ItemStack drop = stack.splitStack(1);
                grenadeCount--;
                player.dropPlayerItemWithRandomChoice(drop, false);
            }

            if (stack.stackSize <= 0) {
                player.inventory.mainInventory[i] = null;
            }
        }

        // ---- ENFORCE NORMAL GUN LIMIT ----
        for (int i = 0; i < player.inventory.mainInventory.length && gunCount > MAX_GUNS; i++) {
            ItemStack stack = player.inventory.mainInventory[i];
            if (stack == null) continue;
            if (!(stack.getItem() instanceof HMGItem_Unified_Guns)) continue;

            HMGItem_Unified_Guns gun = (HMGItem_Unified_Guns) stack.getItem();
            if (gun.gunInfo != null && gun.gunInfo.grenade) continue;

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
