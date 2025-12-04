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



    /**private static List<GunSmithRecipeRegistry.GunRecipeEntry> buildServerAmmoRecipes() {
        List<GunSmithRecipeRegistry.GunRecipeEntry> out = new ArrayList<GunSmithRecipeRegistry.GunRecipeEntry>();
        List list = net.minecraft.item.crafting.CraftingManager.getInstance().getRecipeList();
        if (list == null) return out;

        for (Object obj : list) {
            try {
                net.minecraft.item.ItemStack result = null;
                net.minecraft.item.ItemStack[] items = new net.minecraft.item.ItemStack[0];

                if (obj instanceof net.minecraft.item.crafting.ShapedRecipes) {
                    net.minecraft.item.crafting.ShapedRecipes r = (net.minecraft.item.crafting.ShapedRecipes) obj;
                    result = r.getRecipeOutput();
                    items = (r.recipeItems == null) ? new net.minecraft.item.ItemStack[0] : r.recipeItems;
                } else if (obj instanceof net.minecraft.item.crafting.ShapelessRecipes) {
                    net.minecraft.item.crafting.ShapelessRecipes r = (net.minecraft.item.crafting.ShapelessRecipes) obj;
                    result = r.getRecipeOutput();
                    // ShapelessRecipes.recipeItems is a List<ItemStack> in 1.7.10
                    if (r.recipeItems != null && r.recipeItems.size() > 0) {
                        @SuppressWarnings("unchecked")
                        java.util.List<net.minecraft.item.ItemStack> listItems = (java.util.List<net.minecraft.item.ItemStack>) r.recipeItems;
                        items = listItems.toArray(new net.minecraft.item.ItemStack[listItems.size()]);
                    } else {
                        items = new net.minecraft.item.ItemStack[0];
                    }
                } else {
                    continue; // skip other recipe types
                }

                if (result == null) continue;
                // defensive unlocalized name / display name checks
                String un = null;
                try { un = result.getUnlocalizedName(); } catch (Throwable ignored) {}
                if (un == null) un = "";
                String lower = un.toLowerCase();
                String display = "";
                try { display = result.getDisplayName().toLowerCase(); } catch (Throwable ignored) {}

                if (!(lower.contains("hmg") || lower.contains("handmade") || lower.contains("ammo") ||
                        lower.contains("bullet") || lower.contains("cartridge") || lower.contains("round") ||
                        display.contains("bullet") || display.contains("ammo") || display.contains("round"))) {
                    continue;
                }

                out.add(new GunSmithRecipeRegistry.GunRecipeEntry(result, items));
            } catch (Throwable t) {
                // do not let one bad recipe crash the server — log and skip it
                t.printStackTrace();
                continue;
            }
        }

        return out;
    }
     **/

}
