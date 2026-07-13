package handmadeguns.gunsmithing;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

/**
 * Development-only verification helpers for Gun Smithing Table ingredient matching.
 * These methods are not called during normal gameplay because they register test ore
 * dictionary entries and use Java assertions for local validation.
 */
public final class GunTableRecipeVerificationHelper {
    private GunTableRecipeVerificationHelper() {}

    public static void verifyCopperOreDictionaryBehavior(ItemStack firstCopper, ItemStack secondCopper,
                                                         ItemStack wrongMetadataCopper) {
        assert firstCopper != null;
        assert secondCopper != null;

        OreDictionary.registerOre("ingotCopper", firstCopper.copy());
        OreDictionary.registerOre("ingotCopper", secondCopper.copy());

        ExactStackIngredient exact = new ExactStackIngredient(firstCopper.copy());
        assert exact.matches(firstCopper.copy());
        assert !exact.matches(secondCopper.copy());

        OreDictionaryIngredient oneCopper = new OreDictionaryIngredient("ingotCopper", 1);
        assert oneCopper.matches(firstCopper.copy());
        assert oneCopper.matches(secondCopper.copy());

        OreDictionaryIngredient fiveCopper = new OreDictionaryIngredient("ingotCopper", 5);
        assert canAllocate(new ItemStack[] { copyWithSize(firstCopper, 2), copyWithSize(secondCopper, 3) },
                new GunTableIngredient[] { fiveCopper });
        assert !canAllocate(new ItemStack[] { copyWithSize(firstCopper, 2), copyWithSize(secondCopper, 2) },
                new GunTableIngredient[] { fiveCopper });

        OreDictionaryIngredient lateOre = new OreDictionaryIngredient("ingotCopperLateVerification", 1);
        assert !lateOre.matches(firstCopper.copy());
        OreDictionary.registerOre("ingotCopperLateVerification", firstCopper.copy());
        assert lateOre.matches(firstCopper.copy());

        assert canAllocate(new ItemStack[] { copyWithSize(firstCopper, 2) },
                new GunTableIngredient[] { new ExactStackIngredient(copyWithSize(firstCopper, 1)), oneCopper });
        assert !canAllocate(new ItemStack[] { copyWithSize(firstCopper, 1) },
                new GunTableIngredient[] { new ExactStackIngredient(copyWithSize(firstCopper, 1)), oneCopper });

        if (wrongMetadataCopper != null) {
            OreDictionary.registerOre("ingotCopperMetaSpecificVerification", firstCopper.copy());
            OreDictionaryIngredient metaSpecific = new OreDictionaryIngredient("ingotCopperMetaSpecificVerification", 1);
            assert metaSpecific.matches(firstCopper.copy());
            assert !metaSpecific.matches(wrongMetadataCopper.copy());
        }
    }

    public static boolean canAllocate(ItemStack[] inventory, GunTableIngredient[] ingredients) {
        int[] remaining = new int[inventory == null ? 0 : inventory.length];
        for (int i = 0; i < remaining.length; i++) {
            remaining[i] = inventory[i] == null ? 0 : inventory[i].stackSize;
        }

        GunTableIngredient[] ordered = copyIngredients(ingredients);
        sortExactBeforeOre(ordered);
        for (GunTableIngredient ingredient : ordered) {
            if (ingredient == null) continue;
            int needed = ingredient.getRequiredAmount();
            for (int slot = 0; slot < remaining.length && needed > 0; slot++) {
                if (remaining[slot] <= 0 || !ingredient.matches(inventory[slot])) continue;
                int reserved = Math.min(remaining[slot], needed);
                remaining[slot] -= reserved;
                needed -= reserved;
            }
            if (needed > 0) return false;
        }
        return true;
    }

    private static GunTableIngredient[] copyIngredients(GunTableIngredient[] ingredients) {
        if (ingredients == null) return new GunTableIngredient[0];
        GunTableIngredient[] copy = new GunTableIngredient[ingredients.length];
        System.arraycopy(ingredients, 0, copy, 0, ingredients.length);
        return copy;
    }

    private static void sortExactBeforeOre(GunTableIngredient[] ingredients) {
        for (int i = 0; i < ingredients.length; i++) {
            for (int j = i + 1; j < ingredients.length; j++) {
                if (ingredients[i] != null && ingredients[j] != null
                        && ingredients[i].isOreDictionary() && !ingredients[j].isOreDictionary()) {
                    GunTableIngredient tmp = ingredients[i];
                    ingredients[i] = ingredients[j];
                    ingredients[j] = tmp;
                }
            }
        }
    }

    private static ItemStack copyWithSize(ItemStack source, int size) {
        ItemStack copy = source.copy();
        copy.stackSize = size;
        return copy;
    }
}
