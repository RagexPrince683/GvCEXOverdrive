package handmadeguns.items;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class HGGunRecipes {

    public static void init() {

        // ===== CORE GUN PARTS =====

        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(HGGunItems.firingPin, 2),
                " S ",
                " I ",
                " S ",
                'S', "ingotSteel",
                'I', "ingotIron"
        ));

        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(HGGunItems.triggerAssembly),
                " S ",
                "RIR",
                " S ",
                'S', "ingotSteel",
                'I', "ingotIron",
                'R', Items.redstone
        ));


        // ===== PISTOL PARTS =====

        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(HGGunItems.pistolSlide),
                "SSS",
                " S ",
                "   ",
                'S', "ingotSteel"
        ));

        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(HGGunItems.pistolBarrelKit),
                "   ",
                " S ",
                " I ",
                'S', "ingotSteel",
                'I', "ingotIron"
        ));

        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(HGGunItems.pistolFrameInsert),
                " P ",
                "PSP",
                " P ",
                'P', HGBaseItems.polymer,
                'S', "ingotSteel"
        ));


        // ===== SMG PARTS =====

        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(HGGunItems.smgReceiverBlock),
                "SSS",
                "SI ",
                "SSS",
                'S', "ingotSteel",
                'I', "ingotIron"
        ));

        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(HGGunItems.smgBarrelKit),
                "   ",
                " S ",
                " S ",
                'S', "ingotSteel"
        ));

        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(HGGunItems.smgBoltAssembly),
                "SS ",
                " S ",
                " S ",
                'S', "ingotSteel"
        ));


        // ===== AR FAMILY =====

        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(HGGunItems.arUpper),
                "SAS",
                "S S",
                "SSS",
                'S', "ingotSteel",
                'A', "ingotAluminum"
        ));

        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(HGGunItems.arLower),
                "A A",
                "AAA",
                "ASA",
                'A', "ingotAluminum",
                'S', "ingotSteel"
        ));

        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(HGGunItems.arBoltCarrierGroup),
                "SSS",
                " S ",
                " S ",
                'S', "ingotSteel"
        ));

        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(HGGunItems.arBarrelKit),
                "   ",
                " A ",
                " S ",
                'A', "ingotAluminum",
                'S', "ingotSteel"
        ));


        // ===== DMR FAMILY =====

        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(HGGunItems.dmrHeavyBarrel),
                "   ",
                " S ",
                " S ",
                'S', "ingotSteel"
        ));

        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(HGGunItems.dmrPrecisionLower),
                "SAS",
                "R R",
                "SAS",
                'S', "ingotSteel",
                'A', "ingotAluminum",
                'R', Items.redstone
        ));


        // ===== LMG FAMILY =====

        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(HGGunItems.lmgCarrier),
                "SSS",
                "SSS",
                " S ",
                'S', "ingotSteel"
        ));

        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(HGGunItems.lmgHeavyBarrel),
                "   ",
                " S ",
                " S ",
                'S', "ingotSteel"
        ));

        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(HGGunItems.lmgFeedBlock),
                "ISI",
                "S S",
                "ISI",
                'S', "ingotSteel",
                'I', "ingotIron"
        ));


        // ===== LAUNCHER / RPG PARTS =====

        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(HGGunItems.launcherTube),
                "III",
                "I I",
                "III",
                'I', "ingotIron"
        ));

        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(HGGunItems.launcherFiringAssembly),
                "SRS",
                " I ",
                "S S",
                'S', "ingotSteel",
                'R', Items.redstone,
                'I', "ingotIron"
        ));

        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(HGGunItems.launcherGripFrame),
                " A ",
                "ASA",
                " A ",
                'A', "ingotAluminum",
                'S', "ingotSteel"
        ));


        // ===== UNIVERSAL MOUNTS / MODULES =====

        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(HGGunItems.stockMount),
                "S S",
                "SSS",
                "S S",
                'S', "ingotSteel"
        ));

        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(HGGunItems.opticMount),
                " A ",
                "ASA",
                " A ",
                'A', "ingotAluminum",
                'S', "ingotSteel"
        ));

        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(HGGunItems.gasSystemModule),
                " S ",
                "SRS",
                " S ",
                'S', "ingotSteel",
                'R', Items.redstone
        ));
    }
}

