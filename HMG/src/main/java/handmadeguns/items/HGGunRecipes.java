package handmadeguns.items;

import cpw.mods.fml.common.registry.GameRegistry;
import handmadeguns.items.HGBaseItems;
import handmadeguns.items.HGGunItems;
import net.minecraft.init.Items;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

public class HGGunRecipes {

    public static void init() {

        // ===== CORE GUN PARTS =====

        // Firing Pin (steel + iron)
        GameRegistry.addRecipe(new ItemStack(HGGunItems.firingPin, 2),
                " S ",
                " I ",
                " S ",
                'S', HGBaseItems.steelIngot,
                'I', Items.iron_ingot
        );

        // Trigger Assembly (steel + redstone for sear/trigger interaction)
        GameRegistry.addRecipe(new ItemStack(HGGunItems.triggerAssembly),
                " S ",
                "RIR",
                " S ",
                'S', HGBaseItems.steelIngot,
                'I', Items.iron_ingot,
                'R', Items.redstone
        );


        // ===== PISTOL PARTS =====

        GameRegistry.addRecipe(new ItemStack(HGGunItems.pistolSlide),
                "SSS",
                " S ",
                "   ",
                'S', HGBaseItems.steelIngot
        );

        GameRegistry.addRecipe(new ItemStack(HGGunItems.pistolBarrelKit),
                "   ",
                " S ",
                " I ",
                'S', HGBaseItems.steelIngot,
                'I', Items.iron_ingot
        );

        GameRegistry.addRecipe(new ItemStack(HGGunItems.pistolFrameInsert),
                " P ",
                "PSP",
                " P ",
                'P', HGBaseItems.polymer,
                'S', HGBaseItems.steelIngot
        );


        // ===== SMG PARTS =====

        GameRegistry.addRecipe(new ItemStack(HGGunItems.smgReceiverBlock),
                "SSS",
                "SI ",
                "SSS",
                'S', HGBaseItems.steelIngot,
                'I', Items.iron_ingot
        );

        GameRegistry.addRecipe(new ItemStack(HGGunItems.smgBarrelKit),
                "   ",
                " S ",
                " S ",
                'S', HGBaseItems.steelIngot
        );

        GameRegistry.addRecipe(new ItemStack(HGGunItems.smgBoltAssembly),
                "SS ",
                " S ",
                " S ",
                'S', HGBaseItems.steelIngot
        );


        // ===== AR FAMILY =====

        GameRegistry.addRecipe(new ItemStack(HGGunItems.arUpper),
                "SAS",
                "S S",
                "SSS",
                'S', HGBaseItems.steelIngot,
                'A', HGBaseItems.aluminumIngot
        );

        GameRegistry.addRecipe(new ItemStack(HGGunItems.arLower),
                "A A",
                "AAA",
                "ASA",
                'A', HGBaseItems.aluminumIngot,
                'S', HGBaseItems.steelIngot
        );

        GameRegistry.addRecipe(new ItemStack(HGGunItems.arBoltCarrierGroup),
                "SSS",
                " S ",
                " S ",
                'S', HGBaseItems.steelIngot
        );

        GameRegistry.addRecipe(new ItemStack(HGGunItems.arBarrelKit),
                "   ",
                " A ",
                " S ",
                'A', HGBaseItems.aluminumIngot,
                'S', HGBaseItems.steelIngot
        );


        // ===== DMR FAMILY =====

        GameRegistry.addRecipe(new ItemStack(HGGunItems.dmrHeavyBarrel),
                "   ",
                " S ",
                " S ",
                'S', HGBaseItems.steelIngot
        );

        GameRegistry.addRecipe(new ItemStack(HGGunItems.dmrPrecisionLower),
                "SAS",
                "R R",
                "SAS",
                'S', HGBaseItems.steelIngot,
                'A', HGBaseItems.aluminumIngot,
                'R', Items.redstone
        );


        // ===== LMG FAMILY =====

        GameRegistry.addRecipe(new ItemStack(HGGunItems.lmgCarrier),
                "SSS",
                "SSS",
                " S ",
                'S', HGBaseItems.steelIngot
        );

        GameRegistry.addRecipe(new ItemStack(HGGunItems.lmgHeavyBarrel),
                "   ",
                " S ",
                " S ",
                'S', HGBaseItems.steelIngot
        );

        GameRegistry.addRecipe(new ItemStack(HGGunItems.lmgFeedBlock),
                "ISI",
                "S S",
                "ISI",
                'S', HGBaseItems.steelIngot,
                'I', Items.iron_ingot
        );


        // ===== LAUNCHER / RPG PARTS =====

        GameRegistry.addRecipe(new ItemStack(HGGunItems.launcherTube),
                "III",
                "I I",
                "III",
                'I', Items.iron_ingot
        );

        GameRegistry.addRecipe(new ItemStack(HGGunItems.launcherFiringAssembly),
                "SRS",
                " I ",
                "S S",
                'S', HGBaseItems.steelIngot,
                'R', Items.redstone,
                'I', Items.iron_ingot
        );

        GameRegistry.addRecipe(new ItemStack(HGGunItems.launcherGripFrame),
                " A ",
                "ASA",
                " A ",
                'A', HGBaseItems.aluminumIngot,
                'S', HGBaseItems.steelIngot
        );


        // ===== UNIVERSAL MOUNTS / MODULES =====

        GameRegistry.addRecipe(new ItemStack(HGGunItems.stockMount),
                "S S",
                "SSS",
                "S S",
                'S', HGBaseItems.steelIngot
        );

        GameRegistry.addRecipe(new ItemStack(HGGunItems.opticMount),
                " A ",
                "ASA",
                " A ",
                'A', HGBaseItems.aluminumIngot,
                'S', HGBaseItems.steelIngot
        );

        GameRegistry.addRecipe(new ItemStack(HGGunItems.gasSystemModule),
                " S ",
                "SRS",
                " S ",
                'S', HGBaseItems.steelIngot,
                'R', Items.redstone
        );
    }
}

