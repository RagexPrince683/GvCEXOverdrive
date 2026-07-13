package handmadeguns.gunsmithing;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.List;

public class GunSmithingCraftHandler {

    //gun craft handler
    public static void handleCraft(EntityPlayer player, int recipeIndex) {
        if (player == null) return;
        List<GunSmithRecipeRegistry.GunRecipeEntry> list = GunSmithRecipeRegistry.getAll();
        if (list == null || recipeIndex < 0 || recipeIndex >= list.size()) return;
        GunSmithRecipeRegistry.GunRecipeEntry entry = list.get(recipeIndex);
        if (entry == null || entry.result == null) return;

        GunTableInventoryAllocator.AllocationResult allocation =
                GunTableInventoryAllocator.allocate(player, entry.ingredients);
        if (!allocation.success) return;
        if (!GunTableInventoryAllocator.consume(player, allocation)) return;

        player.inventory.addItemStackToInventory(entry.result.copy());
        player.inventory.markDirty();
        if (player.inventoryContainer != null) player.inventoryContainer.detectAndSendChanges();
    }

    // ---------------- SERVER-SIDE AMMO CRAFT ----------------
    public static void handleAmmoCraft(EntityPlayer player, int recipeIndex) {
        if (player == null) return;

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

        GunTableInventoryAllocator.AllocationResult allocation =
                GunTableInventoryAllocator.allocate(player, entry.ingredients);
        if (!allocation.success) {
            if (allocation.failedIngredient != null) {
                System.out.println("[GunSmith] NOT ENOUGH: " +
                        allocation.failedIngredient.getDisplayName() + " missing " + allocation.missingAmount);
            }
            return;
        }
        if (!GunTableInventoryAllocator.consume(player, allocation)) return;

        // === GIVE RESULT ===
        ItemStack resultCopy = entry.result.copy();
        boolean added = player.inventory.addItemStackToInventory(resultCopy);

        if (!added) {
            // If inventory full, drop it in the world
            player.dropPlayerItemWithRandomChoice(resultCopy, false);
        }

        player.inventory.markDirty();
        if (player.inventoryContainer != null) player.inventoryContainer.detectAndSendChanges();

        System.out.println("[GunSmith] CRAFTED: " + entry.result.getDisplayName());
    }
}
