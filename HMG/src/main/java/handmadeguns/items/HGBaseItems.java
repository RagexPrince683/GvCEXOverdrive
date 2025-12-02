package handmadeguns.items;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.init.Items;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class HGBaseItems {

    public static Item steelIngot;
    public static Item aluminumIngot;
    public static Item polymerChunk;
    public static Item springSet;
    public static Item ironInfusedCoal;
    public static Item gunOil;

    public static void init() {

        // === ITEM REGISTRATION ===
        steelIngot = new Item().setUnlocalizedName("steelIngot").setTextureName("handmadeguns:steel_ingot");
        aluminumIngot = new Item().setUnlocalizedName("aluminumIngot").setTextureName("handmadeguns:aluminum_ingot");
        polymerChunk = new Item().setUnlocalizedName("polymerChunk").setTextureName("handmadeguns:polymer_chunk");
        springSet = new Item().setUnlocalizedName("springSet").setTextureName("handmadeguns:spring_set");
        ironInfusedCoal = new Item().setUnlocalizedName("ironInfusedCoal").setTextureName("handmadeguns:iron_infused_coal");
        gunOil = new Item().setUnlocalizedName("gunOil").setTextureName("handmadeguns:gun_oil");

        GameRegistry.registerItem(steelIngot, "steelIngot");
        GameRegistry.registerItem(aluminumIngot, "aluminumIngot");
        GameRegistry.registerItem(polymerChunk, "polymerChunk");
        GameRegistry.registerItem(springSet, "springSet");
        GameRegistry.registerItem(ironInfusedCoal, "ironInfusedCoal");
        GameRegistry.registerItem(gunOil, "gunOil");

        // === RECIPES ===

        // Iron Infused Coal = iron + coal in a crafting table
        GameRegistry.addShapelessRecipe(new ItemStack(ironInfusedCoal),
                new ItemStack(Items.iron_ingot),
                new ItemStack(Items.coal));

        // Steel Ingot = smelt iron infused coal
        GameRegistry.addSmelting(ironInfusedCoal, new ItemStack(steelIngot), 0.3f);

        // Aluminum Ingot = 1 iron block + 1 steel ingot + 1 coal
        GameRegistry.addShapelessRecipe(new ItemStack(aluminumIngot),
                new ItemStack(Blocks.iron_block),
                new ItemStack(steelIngot),
                new ItemStack(Items.coal));

        // Polymer Chunk = plastic + sugar cane
        // Plastic = slimeball, realistic enough for now
        GameRegistry.addShapelessRecipe(new ItemStack(polymerChunk),
                new ItemStack(Items.slime_ball),
                new ItemStack(Items.reeds));

        // Spring Set = steel ingot arranged in shape
        GameRegistry.addRecipe(new ItemStack(springSet),
                " S ",
                " S ",
                " S ",
                'S', steelIngot);

        // Gun Oil = bottle + seeds + coal (carbon + oil precursor)
        GameRegistry.addShapelessRecipe(new ItemStack(gunOil),
                new ItemStack(Items.glass_bottle),
                new ItemStack(Items.wheat_seeds),
                new ItemStack(Items.coal));
    }
}