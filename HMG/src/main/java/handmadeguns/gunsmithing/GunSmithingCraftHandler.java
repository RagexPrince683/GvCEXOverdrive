package handmadeguns.gunsmithing;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.List;

public class GunSmithingCraftHandler {

    public static void handleCraft(EntityPlayer player, int recipeIndex) {

        List<GunSmithRecipeRegistry.GunRecipeEntry> list =
                GunSmithRecipeRegistry.getAll();

        if (recipeIndex < 0 || recipeIndex >= list.size()) return;

        GunSmithRecipeRegistry.GunRecipeEntry entry = list.get(recipeIndex);

        // ✅ VERIFY MATERIALS
        for (ItemStack req : entry.inputs) {
            if (req == null) continue;

            int owned = 0;
            for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
                ItemStack inv = player.inventory.getStackInSlot(i);
                if (inv != null &&
                        inv.getItem() == req.getItem() &&
                        inv.getItemDamage() == req.getItemDamage()) {

                    owned += inv.stackSize;
                }
            }

            if (owned < req.stackSize)
                return; // abort if player no longer has materials
        }

        // ✅ REMOVE MATERIALS
        for (ItemStack req : entry.inputs) {
            if (req == null) continue;

            int remaining = req.stackSize;

            for (int i = 0; i < player.inventory.getSizeInventory() && remaining > 0; i++) {
                ItemStack inv = player.inventory.getStackInSlot(i);

                if (inv != null &&
                        inv.getItem() == req.getItem() &&
                        inv.getItemDamage() == req.getItemDamage()) {

                    int take = Math.min(inv.stackSize, remaining);
                    inv.stackSize -= take;
                    remaining -= take;

                    if (inv.stackSize <= 0)
                        player.inventory.setInventorySlotContents(i, null);
                }
            }
        }

        // ✅ GIVE RESULT
        player.inventory.addItemStackToInventory(entry.result.copy());
    }
}
