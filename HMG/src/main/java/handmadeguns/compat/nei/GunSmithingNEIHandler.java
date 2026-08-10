package handmadeguns.compat.nei;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import handmadeguns.gunsmithing.GunSmithRecipe;
import handmadeguns.gunsmithing.GunSmithRecipeRegistry;
import handmadeguns.gunsmithing.GunTableIngredient;
import handmadeguns.gunsmithing.OreDictionaryIngredient;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.List;

/** Client-only NEI view over the live canonical registry. */
@SideOnly(Side.CLIENT)
public class GunSmithingNEIHandler extends TemplateRecipeHandler {
    public static final String OVERLAY_ID = "handmadeguns.gunsmithing";

    public String getRecipeName() { return "Gun Smithing Table"; }
    public String getGuiTexture() { return "minecraft:textures/gui/container/crafting_table.png"; }
    public String getOverlayIdentifier() { return OVERLAY_ID; }

    public void loadCraftingRecipes(String outputId, Object... results) {
        if (OVERLAY_ID.equals(outputId)) {
            arecipes.clear();
            addRecipes(GunSmithRecipeRegistry.getAll());
        }
        else super.loadCraftingRecipes(outputId, results);
    }

    public void loadCraftingRecipes(ItemStack result) {
        // Isolate every NEI R-key query so a failed lookup cannot expose an older result.
        arecipes.clear();
        addRecipes(GunSmithRecipeRegistry.findByOutput(result));
    }

    public void loadUsageRecipes(ItemStack ingredient) {
        arecipes.clear();
        addRecipes(GunSmithRecipeRegistry.findByIngredient(ingredient));
    }

    private void addRecipes(List<GunSmithRecipe> recipes) {
        for (GunSmithRecipe recipe : recipes) arecipes.add(new CachedGunSmithRecipe(recipe));
    }

    public final class CachedGunSmithRecipe extends CachedRecipe {
        private final List<PositionedStack> ingredients = new ArrayList<PositionedStack>();
        private final PositionedStack result;

        CachedGunSmithRecipe(GunSmithRecipe recipe) {
            GunTableIngredient[] slots = recipe.getIngredients();
            for (int i = 0; i < slots.length; i++) {
                GunTableIngredient ingredient = slots[i];
                if (ingredient == null) continue;
                Object display = ingredient.getDisplayStack();
                if (ingredient instanceof OreDictionaryIngredient) {
                    ArrayList<ItemStack> alternatives = new ArrayList<ItemStack>();
                    for (ItemStack ore : OreDictionary.getOres(
                            ((OreDictionaryIngredient) ingredient).getOreName())) {
                        if (ore != null) {
                            ItemStack copy = ore.copy();
                            copy.stackSize = ingredient.getRequiredAmount();
                            alternatives.add(copy);
                        }
                    }
                    if (!alternatives.isEmpty()) {
                        display = alternatives.toArray(new ItemStack[alternatives.size()]);
                    }
                }
                if (display != null) ingredients.add(new PositionedStack(display,
                        25 + (i % 3) * 18, 6 + (i / 3) * 18, true));
            }
            result = new PositionedStack(recipe.getOutput(), 119, 24);
        }

        public List<PositionedStack> getIngredients() {
            return getCycledIngredients(cycleticks / 20, ingredients);
        }

        public PositionedStack getResult() { return result; }
    }
}
