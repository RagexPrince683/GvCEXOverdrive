package handmadeguns.items;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockOre;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;

public class HGMetalBlocks {

    public static Block copperOre;
    public static Block aluminumOre;

    public static void init() {
        // Copper Ore
        copperOre = new BlockOre()
                .setBlockName("copperOre")
                .setBlockTextureName("handmadeguns:copper_ore")
                .setHardness(3.0F)
                .setResistance(5.0F);
        GameRegistry.registerBlock(copperOre, "copperOre");

        // Aluminum Ore
        aluminumOre = new BlockOre()
                .setBlockName("aluminumOre")
                .setBlockTextureName("handmadeguns:aluminum_ore")
                .setHardness(3.0F)
                .setResistance(5.0F);
        GameRegistry.registerBlock(aluminumOre, "aluminumOre");

        GameRegistry.addSmelting(HGMetalBlocks.copperOre, new ItemStack(HGMetalItems.copperIngot), 0.7f);
        GameRegistry.addSmelting(HGMetalBlocks.aluminumOre, new ItemStack(HGMetalItems.aluminumIngot), 0.7f);
    }
}

