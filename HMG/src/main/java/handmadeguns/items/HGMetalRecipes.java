package handmadeguns.items;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import handmadeguns.compat.HGMetalCompat;

public class HGMetalRecipes {

    public static void init() {

        // === Existing fallback steel logic ===
        if (!HGMetalCompat.externalSteelExists()) {

            // Iron Infused Coal
            GameRegistry.addShapelessRecipe(
                    new ItemStack(HGMetalItems.ironInfusedCoal, 1),
                    new ItemStack(Items.iron_ingot, 1),
                    new ItemStack(Items.coal, 1)
            );

            // Smelt to steel
            GameRegistry.addSmelting(
                    HGMetalItems.ironInfusedCoal,
                    new ItemStack(HGMetalItems.steelIngot, 1),
                    0.3f
            );
        }

        // === Cross-mod conversion recipes ===
        addOreConversions("ingotSteel", HGMetalItems.steelIngot);
        addOreConversions("ingotCopper", HGMetalItems.copperIngot);
        addOreConversions("ingotAluminum", HGMetalItems.aluminumIngot);
    }

    private static void addOreConversions(String oreName, Item targetItem) {
        for (ItemStack oreStack : OreDictionary.getOres(oreName)) {
            if (oreStack != null && !oreStack.getItem().equals(targetItem)) {
                // Add shapeless recipes both ways
                GameRegistry.addShapelessRecipe(
                        new ItemStack(targetItem, 1, 0),
                        oreStack.copy()
                );
                GameRegistry.addShapelessRecipe(
                        oreStack.copy(),
                        new ItemStack(targetItem, 1, 0)
                );
            }
        }
    }
}
