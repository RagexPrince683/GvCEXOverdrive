package handmadeguns.compat.nei;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;
import handmadeguns.gunsmithing.GunSmithRecipeRegistry;
import handmadeguns.gunsmithing.GunTableIngredient;
import handmadeguns.gunsmithing.OreDictionaryIngredient;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.List;

public class GunSmithingNEIHandler extends TemplateRecipeHandler {
    public static final String OVERLAY_ID = "handmadeguns.gunsmithing";

    public String getRecipeName() { return "Gun Smithing Table"; }

    public String getGuiTexture() { return "textures/gui/container/crafting_table.png"; }

    public String getOverlayIdentifier() { return OVERLAY_ID; }

    public void loadCraftingRecipes(String outputId, Object... results) {
        if (OVERLAY_ID.equals(outputId)) addAllRecipes();
        else super.loadCraftingRecipes(outputId, results);
    }

    public void loadCraftingRecipes(ItemStack result) {
        for (GunSmithRecipeRegistry.GunRecipeEntry recipe : GunSmithRecipeRegistry.getAll()) {
            if (recipe != null && recipe.result != null && stacksMatch(recipe.result, result)) {
                arecipes.add(new CachedGunSmithRecipe(recipe));
            }
        }
    }

    public void loadUsageRecipes(ItemStack ingredient) {
        for (GunSmithRecipeRegistry.GunRecipeEntry recipe : GunSmithRecipeRegistry.getAll()) {
            if (recipe == null || recipe.ingredients == null) continue;
            for (GunTableIngredient required : recipe.ingredients) {
                if (required != null && required.matches(ingredient)) {
                    arecipes.add(new CachedGunSmithRecipe(recipe));
                    break;
                }
            }
        }
    }

    private void addAllRecipes() {
        for (GunSmithRecipeRegistry.GunRecipeEntry recipe : GunSmithRecipeRegistry.getAll()) {
            if (recipe != null && recipe.result != null) arecipes.add(new CachedGunSmithRecipe(recipe));
        }
    }

    private static boolean stacksMatch(ItemStack expected, ItemStack candidate) {
        return expected != null && candidate != null && OreDictionary.itemMatches(expected, candidate, false);
    }

    public final class CachedGunSmithRecipe extends CachedRecipe {
        private final ArrayList<PositionedStack> ingredients = new ArrayList<PositionedStack>();
        private final PositionedStack result;

        CachedGunSmithRecipe(GunSmithRecipeRegistry.GunRecipeEntry recipe) {
            for (int i = 0; i < recipe.ingredients.length; i++) {
                GunTableIngredient ingredient = recipe.ingredients[i];
                if (ingredient == null) continue;
                Object display = ingredient.getDisplayStack();
                if (ingredient instanceof OreDictionaryIngredient) {
                    List<ItemStack> alternatives = OreDictionary.getOres(
                            ((OreDictionaryIngredient) ingredient).getOreName());
                    ArrayList<ItemStack> copies = new ArrayList<ItemStack>();
                    for (ItemStack alternative : alternatives) {
                        if (alternative == null) continue;
                        ItemStack copy = alternative.copy();
                        copy.stackSize = ingredient.getRequiredAmount();
                        copies.add(copy);
                    }
                    if (!copies.isEmpty()) display = copies;
                }
                if (display != null) ingredients.add(new PositionedStack(display, 25 + (i % 3) * 18,
                        6 + (i / 3) * 18, true));
            }
            result = new PositionedStack(recipe.result.copy(), 119, 24);
        }

        public List<PositionedStack> getIngredients() {
            return getCycledIngredients(cycleticks / 20, ingredients);
        }

        public PositionedStack getResult() { return result; }
    }
}
