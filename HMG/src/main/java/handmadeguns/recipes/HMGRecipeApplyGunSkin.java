package handmadeguns.recipes;

import handmadeguns.HMGGunSkinRegistry;
import handmadeguns.items.HMGItemGunSkin;
import handmadeguns.items.guns.HMGItem_Unified_Guns;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/** One shapeless recipe for every compatible pack-defined gun/skin pair. */
public class HMGRecipeApplyGunSkin implements IRecipe {
    public boolean matches(InventoryCrafting inventory, World world) { return result(inventory) != null; }
    public ItemStack getCraftingResult(InventoryCrafting inventory) { return result(inventory); }
    public int getRecipeSize() { return 2; }
    public ItemStack getRecipeOutput() { return null; }

    private ItemStack result(InventoryCrafting inventory) {
        ItemStack gun = null;
        HMGItemGunSkin skin = null;
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack == null) continue;
            if (stack.getItem() instanceof HMGItem_Unified_Guns && gun == null) gun = stack;
            else if (stack.getItem() instanceof HMGItemGunSkin && skin == null) skin = (HMGItemGunSkin) stack.getItem();
            else return null;
        }
        if (gun == null || skin == null || !skin.isValid() || !skin.supports(gun.getItem())) return null;
        ItemStack output = gun.copy();
        if (output.getTagCompound() == null) output.setTagCompound(new NBTTagCompound());
        output.getTagCompound().setString(HMGGunSkinRegistry.NBT_KEY, skin.getSkinId());
        output.stackSize = 1;
        return output;
    }
}
