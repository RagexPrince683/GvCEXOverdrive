package handmadeguns.gunsmithing;

import cpw.mods.fml.common.registry.GameRegistry;
import handmadeguns.HandmadeGunsCore;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.block.Block;
import net.minecraftforge.oredict.OreDictionary;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Authoritative, common-side registry for every Gun Smithing Table recipe. */
public final class GunSmithRecipeRegistry {
    private static final List<GunSmithRecipe> RECIPES = new ArrayList<GunSmithRecipe>();

    private GunSmithRecipeRegistry() {}

    public static synchronized void register(GunSmithRecipe recipe) {
        if (recipe != null) RECIPES.add(recipe);
    }

    public static void register(ItemStack output, GunTableIngredient[] ingredients,
                                GunSmithRecipeCategory category) {
        if (output != null) register(new GunSmithRecipe(output, ingredients, category));
    }

    public static void register(ItemStack output, GunSmithRecipeCategory category, ItemStack... inputs) {
        register(output, exactIngredients(inputs), category);
    }

    /** Compatibility adapter for old pack-facing callers; gun is the historical default. */
    @Deprecated
    public static void register(ItemStack output, ItemStack... inputs) {
        register(output, GunSmithRecipeCategory.GUNS, inputs);
    }

    /** Compatibility adapter for old pack-facing callers; gun is the historical default. */
    @Deprecated
    public static void register(ItemStack output, GunTableIngredient[] ingredients) {
        register(output, ingredients, GunSmithRecipeCategory.GUNS);
    }

    /** Compatibility adapter for the former parallel exact/ore arrays. */
    @Deprecated
    public static void register(ItemStack output, ItemStack[] inputs, String[] oreInputs) {
        int length = Math.max(inputs == null ? 0 : inputs.length, oreInputs == null ? 0 : oreInputs.length);
        GunTableIngredient[] ingredients = new GunTableIngredient[Math.min(length, GunSmithRecipe.SLOT_COUNT)];
        for (int i = 0; i < ingredients.length; i++) {
            ItemStack input = inputs != null && i < inputs.length ? inputs[i] : null;
            String ore = oreInputs != null && i < oreInputs.length ? oreInputs[i] : null;
            if (ore != null && !ore.trim().isEmpty()) {
                ingredients[i] = new OreDictionaryIngredient(ore.trim(), input == null ? 1 : Math.max(1, input.stackSize));
            } else if (input != null) {
                ingredients[i] = new ExactStackIngredient(input);
            }
        }
        register(output, ingredients, GunSmithRecipeCategory.GUNS);
    }

    public static synchronized List<GunSmithRecipe> getAll() {
        return Collections.unmodifiableList(new ArrayList<GunSmithRecipe>(RECIPES));
    }

    public static synchronized List<GunSmithRecipe> getRecipes(GunSmithRecipeCategory category) {
        List<GunSmithRecipe> found = new ArrayList<GunSmithRecipe>();
        for (GunSmithRecipe recipe : RECIPES) if (recipe.getCategory() == category) found.add(recipe);
        return Collections.unmodifiableList(found);
    }

    public static List<GunSmithRecipe> getGunRecipes() { return getRecipes(GunSmithRecipeCategory.GUNS); }
    public static List<GunSmithRecipe> getAmmoRecipes() { return getRecipes(GunSmithRecipeCategory.AMMO); }

    public static synchronized List<GunSmithRecipe> findByOutput(ItemStack output) {
        List<GunSmithRecipe> found = new ArrayList<GunSmithRecipe>();
        if (output != null) for (GunSmithRecipe recipe : RECIPES) {
            ItemStack candidate = recipe.getOutput();
            if (OreDictionary.itemMatches(candidate, output, false)
                    && ItemStack.areItemStackTagsEqual(candidate, output)) found.add(recipe);
        }
        return Collections.unmodifiableList(found);
    }

    public static synchronized List<GunSmithRecipe> findByIngredient(ItemStack stack) {
        List<GunSmithRecipe> found = new ArrayList<GunSmithRecipe>();
        if (stack != null) for (GunSmithRecipe recipe : RECIPES) {
            for (GunTableIngredient ingredient : recipe.getIngredients()) {
                if (ingredient != null && ingredient.matches(stack)) { found.add(recipe); break; }
            }
        }
        return Collections.unmodifiableList(found);
    }

    public static void registerFromFile(File recipeFile) {
        try {
            parseAndRegisterAddRecipeFile(recipeFile);
        } catch (IOException e) {
            HandmadeGunsCore.Debug("[GunSmith] Failed to parse %s: %s", recipeFile.getName(), e.getMessage());
        }
    }

    private static GunTableIngredient[] exactIngredients(ItemStack[] inputs) {
        GunTableIngredient[] ingredients = new GunTableIngredient[GunSmithRecipe.SLOT_COUNT];
        if (inputs != null) for (int i = 0; i < Math.min(inputs.length, ingredients.length); i++) {
            if (inputs[i] != null) ingredients[i] = new ExactStackIngredient(inputs[i]);
        }
        return ingredients;
    }

    private static void parseAndRegisterAddRecipeFile(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        try {
            String line;
            GunTableIngredient[] ingredients = new GunTableIngredient[GunSmithRecipe.SLOT_COUNT];
            boolean reading = false;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                if (line.equalsIgnoreCase("AddRecipe")) {
                    ingredients = new GunTableIngredient[GunSmithRecipe.SLOT_COUNT];
                    reading = true;
                } else if (reading && line.toLowerCase().startsWith("slot")) {
                    String[] parts = line.split(",", 2);
                    if (parts.length == 2) try {
                        int slot = Integer.parseInt(parts[0].trim().substring(4)) - 1;
                        if (slot >= 0 && slot < ingredients.length) ingredients[slot] = parseIngredient(parts[1], file, slot);
                    } catch (NumberFormatException ignored) { }
                } else if (reading && line.toLowerCase().startsWith("craftitem")) {
                    String[] parts = line.split(",", 2);
                    ItemStack output = parts.length == 2 ? parseStack(parts[1]) : null;
                    if (output != null) register(output, ingredients, GunSmithRecipeCategory.GUNS);
                    reading = false;
                }
            }
        } finally { reader.close(); }
    }

    private static GunTableIngredient parseIngredient(String value, File file, int slot) {
        String text = value.trim();
        String lower = text.toLowerCase();
        int prefix = lower.startsWith("ore:") ? 4 : lower.startsWith("oredict:") ? 8
                : lower.startsWith("oredictionary:") ? 14 : 0;
        if (prefix > 0) {
            String spec = text.substring(prefix).trim();
            int amount = 1;
            int separator = spec.lastIndexOf(':');
            if (separator >= 0) try {
                amount = Integer.parseInt(spec.substring(separator + 1).trim());
                spec = spec.substring(0, separator).trim();
            } catch (NumberFormatException e) {
                reject(file, slot, "invalid Ore Dictionary count"); return null;
            }
            if (!spec.isEmpty() && amount > 0) return new OreDictionaryIngredient(spec, amount);
            reject(file, slot, "invalid Ore Dictionary ingredient"); return null;
        }
        ItemStack stack = parseStack(text);
        if (stack == null) reject(file, slot, "could not resolve " + text);
        return stack == null ? null : new ExactStackIngredient(stack);
    }

    private static ItemStack parseStack(String value) {
        String text = value.trim().replace(',', ':');
        while (text.endsWith(":")) text = text.substring(0, text.length() - 1);
        String[] parts = text.split(":");
        if (parts.length < 2 || parts.length > 4) return null;
        int meta = 0, count = 1;
        try {
            if (parts.length > 2) meta = Integer.parseInt(parts[2].trim());
            if (parts.length > 3) count = Integer.parseInt(parts[3].trim());
        } catch (NumberFormatException e) { return null; }
        String modId = parts[0].trim();
        String name = parts[1].trim();
        Item item = GameRegistry.findItem(modId, name);
        if (item == null) {
            Block block = GameRegistry.findBlock(modId, name);
            if (block != null) item = Item.getItemFromBlock(block);
        }
        return item == null || count <= 0 ? null : new ItemStack(item, count, meta);
    }

    private static void reject(File file, int slot, String reason) {
        HandmadeGunsCore.Debug("[GunSmith] Rejecting %s Slot%d: %s", file.getName(), slot + 1, reason);
    }
}
