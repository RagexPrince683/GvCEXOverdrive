package handmadeguns.recipes;

import handmadeguns.HMGGunSkinRegistry;
import handmadeguns.items.HMGItemGunSkin;
import handmadeguns.items.guns.HMGItem_Unified_Guns;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

/** Shapeless recipe which removes a gun's selected skin without changing its other state. */
public class HMGRecipeRemoveGunSkin implements IRecipe {
    public boolean matches(InventoryCrafting inventory, World world) { return result(inventory) != null; }
    public ItemStack getCraftingResult(InventoryCrafting inventory) { return result(inventory); }
    public int getRecipeSize() { return 2; }
    public ItemStack getRecipeOutput() { return null; }

    private ItemStack result(InventoryCrafting inventory) {
        ItemStack gun = null;
        ItemStack removalIngredient = null;
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack == null) continue;
            if (isHMGGun(stack) && gun == null) gun = stack;
            else if (removalIngredient == null) removalIngredient = stack;
            else return null;
        }
        if (gun == null || removalIngredient == null || !hasAppliedSkin(gun)) return null;

        String appliedSkinId = gun.getTagCompound().getString(HMGGunSkinRegistry.NBT_KEY);
        if (!isDye(removalIngredient) && !isAppliedSkin(removalIngredient, appliedSkinId)) return null;

        ItemStack output = gun.copy();
        NBTTagCompound outputTags = output.getTagCompound();
        outputTags.removeTag(HMGGunSkinRegistry.NBT_KEY);
        output.stackSize = 1;
        return output;
    }

    private boolean isHMGGun(ItemStack stack) {
        return stack.getItem() instanceof HMGItem_Unified_Guns;
    }

    private boolean hasAppliedSkin(ItemStack gun) {
        NBTTagCompound tags = gun.getTagCompound();
        return tags != null && tags.hasKey(HMGGunSkinRegistry.NBT_KEY)
                && tags.getString(HMGGunSkinRegistry.NBT_KEY).length() > 0;
    }

    private boolean isAppliedSkin(ItemStack stack, String appliedSkinId) {
        return stack.getItem() instanceof HMGItemGunSkin
                && appliedSkinId.equals(((HMGItemGunSkin) stack.getItem()).getSkinId());
    }

    private boolean isDye(ItemStack stack) {
        if (stack.getItem() == Items.dye) return true;
        int[] oreIds = OreDictionary.getOreIDs(stack);
        for (int oreId : oreIds) {
            String oreName = OreDictionary.getOreName(oreId);
            if (oreName != null && oreName.startsWith("dye")) return true;
        }
        return false;
    }
}
