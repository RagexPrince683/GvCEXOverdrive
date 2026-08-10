package handmadeguns.gunsmithing;

import net.minecraft.item.ItemStack;

/** Immutable canonical recipe used by crafting, the table GUI, and recipe viewers. */
public final class GunSmithRecipe {
    public static final int SLOT_COUNT = 9;

    private final ItemStack output;
    private final GunTableIngredient[] ingredients;
    private final GunSmithRecipeCategory category;

    public GunSmithRecipe(ItemStack output, GunTableIngredient[] ingredients,
                          GunSmithRecipeCategory category) {
        if (output == null || output.getItem() == null) {
            throw new IllegalArgumentException("output must not be null");
        }
        this.output = output.copy();
        this.category = category == null ? GunSmithRecipeCategory.GUNS : category;
        this.ingredients = new GunTableIngredient[SLOT_COUNT];
        if (ingredients != null) {
            System.arraycopy(ingredients, 0, this.ingredients, 0,
                    Math.min(ingredients.length, SLOT_COUNT));
        }
    }

    public ItemStack getOutput() {
        return output.copy();
    }

    public GunTableIngredient[] getIngredients() {
        return ingredients.clone();
    }

    public GunSmithRecipeCategory getCategory() {
        return category;
    }
}
