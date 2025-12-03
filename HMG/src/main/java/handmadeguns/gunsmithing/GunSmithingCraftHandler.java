package handmadeguns.gunsmithing;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class GunSmithingCraftHandler {

    // existing gun craft handler (unchanged)
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
        // Build ammo recipes server-side with same heuristics
        List<GunSmithRecipeRegistry.GunRecipeEntry> ammoList = buildServerAmmoRecipes();

        if (ammoList == null || recipeIndex < 0 || recipeIndex >= ammoList.size()) return;

        GunSmithRecipeRegistry.GunRecipeEntry entry = ammoList.get(recipeIndex);
        if (entry == null) return;

        // Validate player's inventory
        for (ItemStack req : entry.inputs) {
            if (req == null) continue;
            int owned = 0;
            for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
                ItemStack inv = player.inventory.getStackInSlot(i);
                if (inv != null && inv.getItem() == req.getItem() && inv.getItemDamage() == req.getItemDamage()) {
                    owned += inv.stackSize;
                }
            }
            if (owned < req.stackSize) return; // abort - not enough materials
        }

        // Remove materials
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

        // Give result
        player.inventory.addItemStackToInventory(entry.result.copy());
    }

    private static List<GunSmithRecipeRegistry.GunRecipeEntry> buildServerAmmoRecipes() {
        List<GunSmithRecipeRegistry.GunRecipeEntry> out = new ArrayList<GunSmithRecipeRegistry.GunRecipeEntry>();
        List list = net.minecraft.item.crafting.CraftingManager.getInstance().getRecipeList();
        if (list == null) return out;

        for (Object obj : list) {
            if (!(obj instanceof net.minecraft.item.crafting.ShapedRecipes)) continue;
            net.minecraft.item.crafting.ShapedRecipes r = (net.minecraft.item.crafting.ShapedRecipes) obj;
            ItemStack result = r.getRecipeOutput();
            if (result == null) continue;

            String un = result.getUnlocalizedName();
            if (un == null) continue;
            String lc = un.toLowerCase();
            if (!(lc.contains("hmg") || lc.contains("handmade") || lc.contains("ammo") || lc.contains("bullet") || lc.contains("cartridge") || lc.contains("round"))) {
                continue;
            }

            out.add(new GunSmithRecipeRegistry.GunRecipeEntry(result, r.recipeItems));
        }

        return out;
    }
}
