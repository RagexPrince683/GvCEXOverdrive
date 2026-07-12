package handmadeguns.gunsmithing;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.List;

public class GunSmithingCraftHandler {

    //gun craft handler
    public static void handleCraft(EntityPlayer player, int recipeIndex) {
        List<GunSmithRecipeRegistry.GunRecipeEntry> list = GunSmithRecipeRegistry.getAll();
        if (list == null || recipeIndex < 0 || recipeIndex >= list.size()) return;
        GunSmithRecipeRegistry.GunRecipeEntry entry = list.get(recipeIndex);
        if (entry == null) return;

        // Verify materials
        for (int reqIndex = 0; reqIndex < getIngredientCount(entry); reqIndex++) {
            ItemStack req = getInput(entry, reqIndex);
            String oreName = getOreInput(entry, reqIndex);
            if (req == null && oreName == null) continue;
            int needed = getRequiredCount(req);
            int owned = countMatchingItems(player, req, oreName);
            if (owned < needed) return; // abort
        }

        // consume
        for (int reqIndex = 0; reqIndex < getIngredientCount(entry); reqIndex++) {
            ItemStack req = getInput(entry, reqIndex);
            String oreName = getOreInput(entry, reqIndex);
            if (req == null && oreName == null) continue;
            consumeMatchingItems(player, req, oreName);
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

        for (int reqIndex = 0; reqIndex < getIngredientCount(entry); reqIndex++) {
            ItemStack req = getInput(entry, reqIndex);
            String oreName = getOreInput(entry, reqIndex);
            if (req == null && oreName == null) continue;

            int needed = getRequiredCount(req);
            int owned = countMatchingItems(player, req, oreName);
            if (owned < needed) {
                System.out.println("[GunSmith] NOT ENOUGH: " +
                        getIngredientName(req, oreName) + " needs " + needed + " owned " + owned);
                return;
            }
        }

        // === CONSUME INPUTS ===
        for (int reqIndex = 0; reqIndex < getIngredientCount(entry); reqIndex++) {
            ItemStack req = getInput(entry, reqIndex);
            String oreName = getOreInput(entry, reqIndex);
            if (req == null && oreName == null) continue;
            consumeMatchingItems(player, req, oreName);
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


    private static int getIngredientCount(GunSmithRecipeRegistry.GunRecipeEntry entry) {
        int inputLength = entry.inputs == null ? 0 : entry.inputs.length;
        int oreLength = entry.oreInputs == null ? 0 : entry.oreInputs.length;
        return Math.max(inputLength, oreLength);
    }

    private static ItemStack getInput(GunSmithRecipeRegistry.GunRecipeEntry entry, int index) {
        if (entry == null || entry.inputs == null || index < 0 || index >= entry.inputs.length) return null;
        return entry.inputs[index];
    }

    private static String getOreInput(GunSmithRecipeRegistry.GunRecipeEntry entry, int index) {
        if (entry == null || entry.oreInputs == null || index < 0 || index >= entry.oreInputs.length) return null;
        return entry.oreInputs[index];
    }

    private static int countMatchingItems(EntityPlayer player, ItemStack req, String oreName) {
        int owned = 0;
        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            ItemStack inv = player.inventory.getStackInSlot(i);
            if (matchesIngredient(inv, req, oreName)) owned += inv.stackSize;
        }
        return owned;
    }

    private static void consumeMatchingItems(EntityPlayer player, ItemStack req, String oreName) {
        int remaining = getRequiredCount(req);
        for (int i = 0; i < player.inventory.getSizeInventory() && remaining > 0; i++) {
            ItemStack inv = player.inventory.getStackInSlot(i);
            if (!matchesIngredient(inv, req, oreName)) continue;

            int take = Math.min(inv.stackSize, remaining);
            inv.stackSize -= take;
            remaining -= take;
            if (inv.stackSize <= 0) player.inventory.setInventorySlotContents(i, null);
        }
    }

    private static boolean matchesIngredient(ItemStack stack, ItemStack req, String oreName) {
        if (stack == null) return false;

        if (oreName != null && !oreName.isEmpty()) {
            List<ItemStack> ores = OreDictionary.getOres(oreName);
            if (ores == null) return false;
            for (ItemStack ore : ores) {
                if (ore != null && OreDictionary.itemMatches(ore, stack, false)) return true;
            }
            return false;
        }

        return req != null && stack.getItem() == req.getItem() && stack.getItemDamage() == req.getItemDamage();
    }

    private static int getRequiredCount(ItemStack req) {
        return req == null ? 1 : req.stackSize;
    }

    private static String getIngredientName(ItemStack req, String oreName) {
        if (oreName != null && !oreName.isEmpty()) return "ore:" + oreName;
        return req == null ? "unknown" : req.getDisplayName();
    }

}
