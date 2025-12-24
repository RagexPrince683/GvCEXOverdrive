package handmadeguns.items;

import handmadeguns.compat.HGMetalCompat;
import handmadeguns.items.HGMetalItems;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class HGMetalRecipes {

    public static void init() {

        if (!HGMetalCompat.externalSteelExists()) {

            // Iron Infused Coal
            GameRegistry.addShapelessRecipe(
                    new ItemStack(HGMetalItems.ironInfusedCoal),
                    new ItemStack(Items.iron_ingot),
                    new ItemStack(Items.coal)
            );

            // Smelt to steel
            GameRegistry.addSmelting(
                    HGMetalItems.ironInfusedCoal,
                    new ItemStack(HGMetalItems.steelIngot),
                    0.3f
            );
        }
    }
}
