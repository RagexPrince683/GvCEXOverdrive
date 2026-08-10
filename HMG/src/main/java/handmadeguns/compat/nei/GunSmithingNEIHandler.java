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
        for (GunSmithRecipeRegistry.GunRecipeEntry recipe : getVisibleRecipes()) {
            if (recipe != null && recipe.result != null && stacksMatch(recipe.result, result)) {
                arecipes.add(new CachedGunSmithRecipe(recipe));
            }
        }
    }

    public void loadUsageRecipes(ItemStack ingredient) {
        for (GunSmithRecipeRegistry.GunRecipeEntry recipe : getVisibleRecipes()) {
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
        for (GunSmithRecipeRegistry.GunRecipeEntry recipe : getVisibleRecipes()) {
            if (recipe != null && recipe.result != null) arecipes.add(new CachedGunSmithRecipe(recipe));
        }
    }

    /**
     * Mirrors both recipe pages exposed by {@code GunSmithingTableGui}. Recipes
     * discovered by the ammo registry from CraftingManager deliberately remain
     * normal crafting recipes as well as appearing in this handler.
     */
    private static List<GunSmithRecipeRegistry.GunRecipeEntry> getVisibleRecipes() {
        ArrayList<GunSmithRecipeRegistry.GunRecipeEntry> recipes =
                new ArrayList<GunSmithRecipeRegistry.GunRecipeEntry>();
        addUniqueRecipes(recipes, GunSmithRecipeRegistry.getAll());
        addUniqueRecipes(recipes, GunSmithRecipeRegistry.getCombinedAmmoRecipes());
        return recipes;
    }

    private static void addUniqueRecipes(List<GunSmithRecipeRegistry.GunRecipeEntry> recipes,
                                         List<GunSmithRecipeRegistry.GunRecipeEntry> additions) {
        if (additions == null) return;
        for (GunSmithRecipeRegistry.GunRecipeEntry addition : additions) {
            if (addition == null || addition.result == null) continue;
            boolean duplicate = false;
            for (GunSmithRecipeRegistry.GunRecipeEntry recipe : recipes) {
                if (sameRecipe(recipe, addition)) {
                    duplicate = true;
                    break;
                }
            }
            if (!duplicate) recipes.add(addition);
        }
    }

    private static boolean sameRecipe(GunSmithRecipeRegistry.GunRecipeEntry first,
                                      GunSmithRecipeRegistry.GunRecipeEntry second) {
        if (!sameStack(first.result, second.result)) return false;
        GunTableIngredient[] firstIngredients = first.ingredients;
        GunTableIngredient[] secondIngredients = second.ingredients;
        if (firstIngredients == null || secondIngredients == null) {
            return firstIngredients == secondIngredients;
        }
        if (firstIngredients.length != secondIngredients.length) return false;
        for (int i = 0; i < firstIngredients.length; i++) {
            if (!sameIngredient(firstIngredients[i], secondIngredients[i])) return false;
        }
        return true;
    }

    private static boolean sameIngredient(GunTableIngredient first, GunTableIngredient second) {
        if (first == null || second == null) return first == second;
        if (first.getClass() != second.getClass()
                || first.getRequiredAmount() != second.getRequiredAmount()) return false;
        if (first instanceof OreDictionaryIngredient) {
            return ((OreDictionaryIngredient) first).getOreName().equals(
                    ((OreDictionaryIngredient) second).getOreName());
        }
        return sameStack(first.getDisplayStack(), second.getDisplayStack());
    }

    private static boolean sameStack(ItemStack first, ItemStack second) {
        return first != null && second != null
                && first.getItem() == second.getItem()
                && first.getItemDamage() == second.getItemDamage()
                && first.stackSize == second.stackSize
                && ItemStack.areItemStackTagsEqual(first, second);
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
