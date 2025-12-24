package handmadeguns.compat;

import handmadeguns.items.HGMetalItems;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraft.item.ItemStack;

public class HGMetalCompat {

    public static boolean externalSteelExists() {
        for (ItemStack stack : OreDictionary.getOres("ingotSteel")) {
            if (stack != null && stack.getItem() != HGMetalItems.steelIngot) {
                return true;
            }
        }
        return false;
    }
    public static boolean externalCopperExists() {
        return !OreDictionary.getOres("oreCopper").isEmpty();
    }

    public static boolean externalAluminumExists() {
        return !OreDictionary.getOres("oreAluminum").isEmpty()
                || !OreDictionary.getOres("oreAluminium").isEmpty();
    }
}

