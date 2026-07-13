package handmadeguns.gunsmithing;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.List;

public final class OreDictionaryIngredient implements GunTableIngredient {
    private final String oreName;
    private final int requiredAmount;

    public OreDictionaryIngredient(String oreName, int requiredAmount) {
        if (oreName == null || oreName.trim().isEmpty()) throw new IllegalArgumentException("oreName must not be empty");
        if (requiredAmount <= 0) throw new IllegalArgumentException("requiredAmount must be positive");
        this.oreName = oreName.trim();
        this.requiredAmount = requiredAmount;
    }

    public int getRequiredAmount() {
        return requiredAmount;
    }

    public boolean matches(ItemStack candidate) {
        if (candidate == null) return false;
        List<ItemStack> ores = OreDictionary.getOres(oreName);
        if (ores == null || ores.isEmpty()) return false;
        for (ItemStack ore : ores) {
            if (ore != null && OreDictionary.itemMatches(ore, candidate, false)) return true;
        }
        return false;
    }

    public ItemStack getDisplayStack() {
        List<ItemStack> ores = OreDictionary.getOres(oreName);
        if (ores == null || ores.isEmpty()) return null;
        for (ItemStack stack : ores) {
            if (stack != null) {
                ItemStack copy = stack.copy();
                copy.stackSize = requiredAmount;
                return copy;
            }
        }
        return null;
    }

    public String getDisplayName() {
        return "Any " + oreName;
    }

    public boolean isOreDictionary() {
        return true;
    }

    public String getOreName() {
        return oreName;
    }
}
