package handmadeguns.gunsmithing;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

import java.util.List;

public class GunSmithingCraftHandler {

    //gun craft handler
    public static void handleCraft(EntityPlayer player, int recipeIndex) {
        if (!canHandleRequest(player)) return;
        List<GunSmithRecipeRegistry.GunRecipeEntry> list = GunSmithRecipeRegistry.getAll();
        if (list == null || recipeIndex < 0 || recipeIndex >= list.size()) return;
        GunSmithRecipeRegistry.GunRecipeEntry entry = list.get(recipeIndex);
        if (entry == null || entry.result == null) return;

        craftTransaction(player, entry);
    }

    // ---------------- SERVER-SIDE AMMO CRAFT ----------------
    public static void handleAmmoCraft(EntityPlayer player, int recipeIndex) {
        if (!canHandleRequest(player)) return;

        // Build the SAME combined list as the GUI
        List<GunSmithRecipeRegistry.GunRecipeEntry> ammoList =
                GunSmithRecipeRegistry.getCombinedAmmoRecipes();

        System.out.println("[GunSmith] handleAmmoCraft idx=" + recipeIndex +
                " size=" + (ammoList == null ? 0 : ammoList.size()));

        if (ammoList == null || recipeIndex < 0 || recipeIndex >= ammoList.size()) {
            System.out.println("[GunSmith] INVALID AMMO INDEX " + recipeIndex);
            return;
        }

        GunSmithRecipeRegistry.GunRecipeEntry entry = ammoList.get(recipeIndex);
        if (entry == null || entry.result == null) {
            System.out.println("[GunSmith] NULL AMMO ENTRY AT INDEX " + recipeIndex);
            return;
        }

        if (craftTransaction(player, entry)) {
            System.out.println("[GunSmith] CRAFTED: " + entry.result.getDisplayName());
        }
    }

    private static boolean canHandleRequest(EntityPlayer player) {
        return player != null && !player.worldObj.isRemote && player.openContainer instanceof ContainerGunSmith;
    }

    /**
     * Revalidates, consumes, and delivers under one inventory lock.  The packet only
     * selects a server-owned recipe; no client-provided stack participates here.
     */
    private static boolean craftTransaction(EntityPlayer player,
                                            GunSmithRecipeRegistry.GunRecipeEntry entry) {
        synchronized (player.inventory) {
            GunTableInventoryAllocator.AllocationResult allocation =
                    GunTableInventoryAllocator.allocate(player, entry.ingredients);
            if (!allocation.success || !GunTableInventoryAllocator.consume(player, allocation)) return false;

            ItemStack remainder = entry.result.copy();
            player.inventory.addItemStackToInventory(remainder);
            if (remainder.stackSize > 0) {
                player.dropPlayerItemWithRandomChoice(remainder, false);
            }
            synchronize(player);
            return true;
        }
    }

    private static void synchronize(EntityPlayer player) {
        player.inventory.markDirty();
        if (player.inventoryContainer != null) player.inventoryContainer.detectAndSendChanges();
        if (player.openContainer != null) player.openContainer.detectAndSendChanges();
        if (player instanceof EntityPlayerMP) {
            EntityPlayerMP serverPlayer = (EntityPlayerMP) player;
            // ContainerGunSmith has no slots of its own, so explicitly send the
            // player inventory container as well as detecting the open container.
            serverPlayer.sendContainerToPlayer(serverPlayer.inventoryContainer);
        }
    }
}
