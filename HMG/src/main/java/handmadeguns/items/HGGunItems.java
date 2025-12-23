package handmadeguns.items;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;
import net.minecraft.creativetab.CreativeTabs;

public class HGGunItems {

    // === Core gun parts ===
    public static Item firingPin;
    public static Item triggerAssembly;

    // === Weapon-family / category parts ===
    // Pistol
    public static Item pistolSlide;
    public static Item pistolBarrelKit;
    public static Item pistolFrameInsert;

    // SMG
    public static Item smgReceiverBlock;
    public static Item smgBarrelKit;
    public static Item smgBoltAssembly;

    // AR (assault rifle / carbine)
    public static Item arUpper;
    public static Item arLower;
    public static Item arBarrelKit;
    public static Item arBoltCarrierGroup;

    // DMR
    public static Item dmrHeavyBarrel;
    public static Item dmrPrecisionLower;

    // LMG
    public static Item lmgCarrier;
    public static Item lmgHeavyBarrel;
    public static Item lmgFeedBlock;

    // Launcher / RPG
    public static Item launcherTube;
    public static Item launcherFiringAssembly;
    public static Item launcherStabilizerHousing;

    // === Universal mounts / modules ===
    public static Item stockMount;
    public static Item opticMount;
    public static Item gasSystemModule;

    // reuse the creative tab defined in HGBaseItems
    private static final CreativeTabs TAB = HGBaseItems.tabHMGCrafting;

    public static void init() {
        // === Core gun parts ===
        firingPin = new Item()
                .setUnlocalizedName("firingPin")
                .setTextureName("handmadeguns:firing_pin")
                .setCreativeTab(TAB);
        triggerAssembly = new Item()
                .setUnlocalizedName("triggerAssembly")
                .setTextureName("handmadeguns:trigger_assembly")
                .setCreativeTab(TAB);

        // === Pistol family ===
        pistolSlide = new Item()
                .setUnlocalizedName("pistolSlide")
                .setTextureName("handmadeguns:pistol_slide")
                .setCreativeTab(TAB);
        pistolBarrelKit = new Item()
                .setUnlocalizedName("pistolBarrelKit")
                .setTextureName("handmadeguns:pistol_barrel_kit")
                .setCreativeTab(TAB);
        pistolFrameInsert = new Item()
                .setUnlocalizedName("pistolFrameInsert")
                .setTextureName("handmadeguns:pistol_frame_insert")
                .setCreativeTab(TAB);

        // === SMG family ===
        smgReceiverBlock = new Item()
                .setUnlocalizedName("smgReceiverBlock")
                .setTextureName("handmadeguns:smg_receiver_block")
                .setCreativeTab(TAB);
        smgBarrelKit = new Item()
                .setUnlocalizedName("smgBarrelKit")
                .setTextureName("handmadeguns:smg_barrel_kit")
                .setCreativeTab(TAB);
        smgBoltAssembly = new Item()
                .setUnlocalizedName("smgBoltAssembly")
                .setTextureName("handmadeguns:smg_bolt_assembly")
                .setCreativeTab(TAB);

        // === AR family ===
        arUpper = new Item()
                .setUnlocalizedName("arUpper")
                .setTextureName("handmadeguns:ar_upper")
                .setCreativeTab(TAB);
        arLower = new Item()
                .setUnlocalizedName("arLower")
                .setTextureName("handmadeguns:ar_lower")
                .setCreativeTab(TAB);
        arBoltCarrierGroup = new Item()
                .setUnlocalizedName("arBoltCarrierGroup")
                .setTextureName("handmadeguns:ar_bolt_carrier_group")
                .setCreativeTab(TAB);
        arBarrelKit = new Item()
                .setUnlocalizedName("arBarrelKit")
                .setTextureName("handmadeguns:ar_barrel_kit")
                .setCreativeTab(TAB);

        // === DMR family ===
        dmrHeavyBarrel = new Item()
                .setUnlocalizedName("dmrHeavyBarrel")
                .setTextureName("handmadeguns:dmr_heavy_barrel")
                .setCreativeTab(TAB);
        dmrPrecisionLower = new Item()
                .setUnlocalizedName("dmrPrecisionLower")
                .setTextureName("handmadeguns:dmr_precision_lower")
                .setCreativeTab(TAB);

        // === LMG family ===
        lmgCarrier = new Item()
                .setUnlocalizedName("lmgCarrier")
                .setTextureName("handmadeguns:lmg_carrier")
                .setCreativeTab(TAB);
        lmgHeavyBarrel = new Item()
                .setUnlocalizedName("lmgHeavyBarrel")
                .setTextureName("handmadeguns:lmg_heavy_barrel")
                .setCreativeTab(TAB);
        lmgFeedBlock = new Item()
                .setUnlocalizedName("lmgFeedBlock")
                .setTextureName("handmadeguns:lmg_feed_block")
                .setCreativeTab(TAB);

        // === Launcher / RPG family ===
        launcherTube = new Item()
                .setUnlocalizedName("launcherTube")
                .setTextureName("handmadeguns:launcher_tube")
                .setCreativeTab(TAB);
        launcherFiringAssembly = new Item()
                .setUnlocalizedName("launcherFiringAssembly")
                .setTextureName("handmadeguns:launcher_firing_assembly")
                .setCreativeTab(TAB);
        launcherStabilizerHousing = new Item()
                .setUnlocalizedName("launcherStabilizerHousing")
                .setTextureName("handmadeguns:launcher_stabilizer_housing")
                .setCreativeTab(TAB);

        // === Universal mounts / modules ===
        stockMount = new Item()
                .setUnlocalizedName("stockMount")
                .setTextureName("handmadeguns:stock_mount")
                .setCreativeTab(TAB);
        opticMount = new Item()
                .setUnlocalizedName("opticMount")
                .setTextureName("handmadeguns:optic_mount")
                .setCreativeTab(TAB);
        gasSystemModule = new Item()
                .setUnlocalizedName("gasSystemModule")
                .setTextureName("handmadeguns:gas_system_module")
                .setCreativeTab(TAB);

        // === Registration ===
        GameRegistry.registerItem(arBoltCarrierGroup, "arBoltCarrierGroup");
        GameRegistry.registerItem(firingPin, "firingPin");
        GameRegistry.registerItem(triggerAssembly, "triggerAssembly");

        GameRegistry.registerItem(pistolSlide, "pistolSlide");
        GameRegistry.registerItem(pistolBarrelKit, "pistolBarrelKit");
        GameRegistry.registerItem(pistolFrameInsert, "pistolFrameInsert");

        GameRegistry.registerItem(smgReceiverBlock, "smgReceiverBlock");
        GameRegistry.registerItem(smgBarrelKit, "smgBarrelKit");
        GameRegistry.registerItem(smgBoltAssembly, "smgBoltAssembly");

        GameRegistry.registerItem(arUpper, "arUpper");
        GameRegistry.registerItem(arLower, "arLower");
        GameRegistry.registerItem(arBarrelKit, "arBarrelKit");

        GameRegistry.registerItem(dmrHeavyBarrel, "dmrHeavyBarrel");
        GameRegistry.registerItem(dmrPrecisionLower, "dmrPrecisionLower");

        GameRegistry.registerItem(lmgCarrier, "lmgCarrier");
        GameRegistry.registerItem(lmgHeavyBarrel, "lmgHeavyBarrel");
        GameRegistry.registerItem(lmgFeedBlock, "lmgFeedBlock");

        GameRegistry.registerItem(launcherTube, "launcherTube");
        GameRegistry.registerItem(launcherFiringAssembly, "launcherFiringAssembly");
        GameRegistry.registerItem(launcherStabilizerHousing, "launcherStabilizerHousing");

        GameRegistry.registerItem(stockMount, "stockMount");
        GameRegistry.registerItem(opticMount, "opticMount");
        GameRegistry.registerItem(gasSystemModule, "gasSystemModule");
    }
}


