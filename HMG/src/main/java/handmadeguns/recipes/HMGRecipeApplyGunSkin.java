package handmadeguns.recipes;

import handmadeguns.HMGGunSkinRegistry;
import handmadeguns.items.HMGItemGunSkin;
import handmadeguns.items.guns.HMGItem_Unified_Guns;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/** A shapeless recipe bound to one exact pack-defined gun/skin pair. */
public class HMGRecipeApplyGunSkin implements IRecipe {
    private final HMGItem_Unified_Guns targetGun;
    private final HMGItemGunSkin skinItem;
    private final ItemStack recipeOutput;

    public HMGRecipeApplyGunSkin(HMGItem_Unified_Guns targetGun, HMGItemGunSkin skinItem) {
        this.targetGun = targetGun;
        this.skinItem = skinItem;
        this.recipeOutput = applySkin(new ItemStack(targetGun));
    }

    public boolean matches(InventoryCrafting inventory, World world) { return result(inventory) != null; }
    public ItemStack getCraftingResult(InventoryCrafting inventory) { return result(inventory); }
    public int getRecipeSize() { return 2; }
    public ItemStack getRecipeOutput() { return recipeOutput.copy(); }

    private ItemStack result(InventoryCrafting inventory) {
        ItemStack gun = null;
        boolean foundSkin = false;
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack == null) continue;
            if (stack.getItem() == targetGun && gun == null) gun = stack;
            else if (stack.getItem() == skinItem && !foundSkin) foundSkin = true;
            else return null;
        }
        if (gun == null || !foundSkin) return null;
        return applySkin(gun);
    }

    private ItemStack applySkin(ItemStack gun) {
        ItemStack output = gun.copy();
        if (output.getTagCompound() == null) output.setTagCompound(new NBTTagCompound());
        output.getTagCompound().setString(HMGGunSkinRegistry.NBT_KEY, skinItem.getSkinId());
        output.stackSize = 1;
        return output;
    }
}
