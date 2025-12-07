package handmadeguns.gunsmithing;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class GunSmithingCraftHandler {

    //gun craft handler
    public static void handleCraft(EntityPlayer player, int recipeIndex) {
        List<GunSmithRecipeRegistry.GunRecipeEntry> list = GunSmithRecipeRegistry.getAll();
        if (list == null || recipeIndex < 0 || recipeIndex >= list.size()) return;
        GunSmithRecipeRegistry.GunRecipeEntry entry = list.get(recipeIndex);
        if (entry == null) return;

        // Verify materials
        for (ItemStack req : entry.inputs) {
            if (req == null) continue;
            int owned = 0;
            for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
                ItemStack inv = player.inventory.getStackInSlot(i);
                if (inv != null && inv.getItem() == req.getItem() && inv.getItemDamage() == req.getItemDamage()) {
                    owned += inv.stackSize;
                }
            }
            if (owned < req.stackSize) return; // abort
        }

        // consume
        for (ItemStack req : entry.inputs) {
            if (req == null) continue;
            int remaining = req.stackSize;
            for (int i = 0; i < player.inventory.getSizeInventory() && remaining > 0; i++) {
                ItemStack inv = player.inventory.getStackInSlot(i);
                if (inv != null && inv.getItem() == req.getItem() && inv.getItemDamage() == req.getItemDamage()) {
                    int take = Math.min(inv.stackSize, remaining);
                    inv.stackSize -= take;
                    remaining -= take;
                    if (inv.stackSize <= 0) player.inventory.setInventorySlotContents(i, null);
                }
            }
        }

        // give result
        player.inventory.addItemStackToInventory(entry.result.copy());
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

        // === VALIDATE INPUTS ===
        if (entry.inputs == null) {
            System.out.println("[GunSmith] Ammo recipe has no inputs!");
            return;
        }

        for (ItemStack req : entry.inputs) {
            if (req == null) continue;

            int owned = 0;
            for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
                ItemStack slot = player.inventory.getStackInSlot(i);
                if (slot != null &&
                        slot.getItem() == req.getItem() &&
                        slot.getItemDamage() == req.getItemDamage()) {

                    owned += slot.stackSize;
                }
            }

            if (owned < req.stackSize) {
                System.out.println("[GunSmith] NOT ENOUGH: " +
                        req.getDisplayName() + " needs " + req.stackSize + " owned " + owned);
                return;
            }
        }

        // === CONSUME INPUTS ===
        for (ItemStack req : entry.inputs) {
            if (req == null) continue;

            int remaining = req.stackSize;

            for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
                ItemStack slot = player.inventory.getStackInSlot(i);
                if (slot == null) continue;

                if (slot.getItem() == req.getItem() &&
                        slot.getItemDamage() == req.getItemDamage()) {

                    int remove = Math.min(remaining, slot.stackSize);
                    slot.stackSize -= remove;
                    remaining -= remove;

                    if (slot.stackSize <= 0) {
                        player.inventory.setInventorySlotContents(i, null);
                    }

                    if (remaining <= 0) break;
                }
            }
        }

        // === GIVE RESULT ===
        ItemStack resultCopy = entry.result.copy();
        boolean added = player.inventory.addItemStackToInventory(resultCopy);

        if (!added) {
            // If inventory full, drop it in the world
            player.dropPlayerItemWithRandomChoice(resultCopy, false);
        }

        player.inventoryContainer.detectAndSendChanges();

        System.out.println("[GunSmith] CRAFTED: " + entry.result.getDisplayName());
    }


}
