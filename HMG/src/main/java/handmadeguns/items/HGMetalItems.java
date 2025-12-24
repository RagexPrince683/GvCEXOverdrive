package handmadeguns.items;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import handmadeguns.compat.HGMetalCompat;


public class HGMetalItems {

    public static Item steelIngot;
    public static Item copperIngot;
    public static Item aluminumIngot;

    // Steel fallback production
    public static Item ironInfusedCoal;

    public static void init(CreativeTabs tab) {

        steelIngot = new Item()
                .setUnlocalizedName("steelIngot")
                .setTextureName("handmadeguns:steel_ingot")
                .setCreativeTab(tab);

        copperIngot = new Item()
                .setUnlocalizedName("copperIngot")
                .setTextureName("handmadeguns:copper_ingot")
                .setCreativeTab(tab);

        aluminumIngot = new Item()
                .setUnlocalizedName("aluminumIngot")
                .setTextureName("handmadeguns:aluminum_ingot")
                .setCreativeTab(tab);

        ironInfusedCoal = new Item()
                .setUnlocalizedName("ironInfusedCoal")
                .setTextureName("handmadeguns:iron_infused_coal")
                .setCreativeTab(tab);

        GameRegistry.registerItem(steelIngot, "steelIngot");
        GameRegistry.registerItem(copperIngot, "copperIngot");
        GameRegistry.registerItem(aluminumIngot, "aluminumIngot");
        GameRegistry.registerItem(ironInfusedCoal, "ironInfusedCoal");

        // === OREDICT REGISTRATION ===
        OreDictionary.registerOre("ingotSteel", steelIngot);
        OreDictionary.registerOre("ingotCopper", copperIngot);
        OreDictionary.registerOre("ingotAluminum", aluminumIngot);
    }
}


