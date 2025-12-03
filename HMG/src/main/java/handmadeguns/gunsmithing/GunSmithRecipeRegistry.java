package handmadeguns.gunsmithing;

import net.minecraft.item.ItemStack;
import java.util.ArrayList;
import java.util.List;

public class GunSmithRecipeRegistry {

    public static class GunRecipeEntry {
        public final ItemStack result;
        public final ItemStack[] inputs;

        public GunRecipeEntry(ItemStack result, ItemStack[] inputs) {
            this.result = result;
            this.inputs = inputs;
        }
    }

    private static final List<GunRecipeEntry> RECIPES = new ArrayList<>();

    public static void register(ItemStack result, ItemStack... inputs) {
        RECIPES.add(new GunRecipeEntry(result, inputs));
    }

    //TODO: integrate recipes from HandmadeGunsCore readPackRecipe method

    public static List<GunRecipeEntry> getAll() {
        return RECIPES;
    }
}