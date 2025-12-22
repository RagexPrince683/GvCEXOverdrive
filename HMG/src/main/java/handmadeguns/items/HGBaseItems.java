package handmadeguns.items;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;


public class HGBaseItems {

    // ===================
    // ITEMS
    // ===================
    public static Item steelIngot;
    public static Item copperIngot;
    public static Item aluminumIngot;
    public static Item polymer;
    public static Item springSet;
    public static Item ironInfusedCoal;
    public static Item gunOil;

    // ===================
    // CREATIVE TAB
    // ===================
    public static final CreativeTabs tabHMGCrafting = new CreativeTabs("HMGCrafting") {
        @Override
        public Item getTabIconItem() {
            return steelIngot; // Icon of the tab
        }
    };

    public static void init() {

        // === ITEM REGISTRATION ===
        steelIngot = new Item()
                .setUnlocalizedName("steelIngot")
                .setTextureName("handmadeguns:steel_ingot")
                .setCreativeTab(tabHMGCrafting);

        copperIngot = new Item()
                .setUnlocalizedName("copperIngot")
                .setTextureName("handmadeguns:copper_ingot")
                .setCreativeTab(tabHMGCrafting);

        aluminumIngot = new Item()
                .setUnlocalizedName("aluminumIngot")
                .setTextureName("handmadeguns:aluminum_ingot")
                .setCreativeTab(tabHMGCrafting);

        polymer = new Item()
                .setUnlocalizedName("polymer")
                .setTextureName("handmadeguns:polymer")
                .setCreativeTab(tabHMGCrafting);

        springSet = new Item()
                .setUnlocalizedName("springSet")
                .setTextureName("handmadeguns:spring_set")
                .setCreativeTab(tabHMGCrafting);

        ironInfusedCoal = new Item()
                .setUnlocalizedName("ironInfusedCoal")
                .setTextureName("handmadeguns:iron_infused_coal")
                .setCreativeTab(tabHMGCrafting);

        gunOil = new Item()
                .setUnlocalizedName("gunOil")
                .setTextureName("handmadeguns:gun_oil")
                .setCreativeTab(tabHMGCrafting);

        GameRegistry.registerItem(steelIngot, "steelIngot");
        GameRegistry.registerItem(copperIngot, "copperIngot");
        GameRegistry.registerItem(aluminumIngot, "aluminumIngot");
        GameRegistry.registerItem(polymer, "polymer");
        GameRegistry.registerItem(springSet, "springSet");
        GameRegistry.registerItem(ironInfusedCoal, "ironInfusedCoal");
        GameRegistry.registerItem(gunOil, "gunOil");


        // === ORE DICTIONARY REGISTRATION ===
        OreDictionary.registerOre("ingotSteel", steelIngot);
        OreDictionary.registerOre("ingotAluminum", aluminumIngot);
        OreDictionary.registerOre("ingotCopper", copperIngot);

        // === RECIPES ===

        // Iron Infused Coal = iron + coal
        GameRegistry.addShapelessRecipe(new ItemStack(ironInfusedCoal),
                new ItemStack(Items.iron_ingot),
                new ItemStack(Items.coal));

        // Steel Ingot = smelt infused coal
        GameRegistry.addSmelting(ironInfusedCoal, new ItemStack(steelIngot), 0.3f);

        // Aluminum Ingot
        GameRegistry.addShapelessRecipe(new ItemStack(aluminumIngot),
                new ItemStack(Blocks.iron_block),
                new ItemStack(steelIngot),
                new ItemStack(Items.coal));

        // Polymer Chunk
        GameRegistry.addShapelessRecipe(new ItemStack(polymer),
                new ItemStack(Items.slime_ball),
                new ItemStack(Items.reeds));

        // Spring Set = vertical steel
        GameRegistry.addRecipe(new ItemStack(springSet),
                " S ",
                " S ",
                " S ",
                'S', steelIngot);

        // Gun Oil = bottle + seeds + coal
        GameRegistry.addShapelessRecipe(new ItemStack(gunOil),
                new ItemStack(Items.glass_bottle),
                new ItemStack(Items.wheat_seeds),
                new ItemStack(Items.coal));
    }
}
