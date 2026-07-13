package handmadeguns.gunsmithing;

import net.minecraft.item.ItemStack;

public interface GunTableIngredient {
    int getRequiredAmount();

    boolean matches(ItemStack stack);

    ItemStack getDisplayStack();

    String getDisplayName();

    boolean isOreDictionary();
}
