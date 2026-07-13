package handmadeguns.gunsmithing;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public final class ExactStackIngredient implements GunTableIngredient {
    private final ItemStack stack;

    public ExactStackIngredient(ItemStack stack) {
        if (stack == null) throw new IllegalArgumentException("stack must not be null");
        this.stack = stack.copy();
    }

    public int getRequiredAmount() {
        return stack.stackSize;
    }

    public boolean matches(ItemStack candidate) {
        if (candidate == null || stack.getItem() == null) return false;
        return OreDictionary.itemMatches(stack, candidate, false);
    }

    public ItemStack getDisplayStack() {
        return stack.copy();
    }

    public String getDisplayName() {
        try {
            return stack.getDisplayName();
        } catch (Throwable ignored) {
            return stack.toString();
        }
    }

    public boolean isOreDictionary() {
        return false;
    }

    public ItemStack getExactStack() {
        return stack.copy();
    }
}
