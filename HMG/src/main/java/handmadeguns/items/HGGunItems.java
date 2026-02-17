package handmadeguns.items;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;
import net.minecraft.creativetab.CreativeTabs;

public class HGGunItems {

    // === CORE MECHANISMS ===
    public static Item firingPin;
    public static Item triggerAssembly;
    public static Item springSet;

    // === MANUFACTURING TIERS ===
    public static Item machinedParts;        // mid-tier machining
    public static Item precisionComponents;  // high-tier precision

    // === RECEIVERS (by manufacturing type) ===
    public static Item stampedReceiver; // WW2 / stamped guns / SMGs / AK-type
    public static Item milledReceiver;  // modern milled rifles / DMRs

    // === FUNCTIONAL CORE ASSEMBLIES ===
    public static Item blowbackBoltAssembly;     // SMG / simple systems
    public static Item rotatingBoltAssembly;     // rifle pattern
    public static Item heavyBoltCarrierAssembly;         // LMG / high-mass systems
    public static Item boltActionAssembly;       // bolt-action rifles

    // === HANDGUN SYSTEMS ===
    public static Item handgunSlideAssembly;         // semi-auto pistol slide systems
    public static Item revolverMechanismAssembly;    // revolver cylinder/timing system

    // === BARREL ASSEMBLIES ===
    public static Item lightBarrelKit;
    public static Item rifleBarrelKit;
    public static Item heavyBarrelKit;

    // === PLATFORM-SPECIFIC (MODULAR SYSTEMS) ===
    public static Item arUpper;
    public static Item arLower;

    // === FEED & OPERATING MODULES ===
    public static Item gasSystemModule;
    public static Item beltFeedModule;
    public static Item pumpLeverActionModule; // pump / lever / manual cycling systems

    // === LAUNCHER SYSTEMS ===
    public static Item launcherTube;
    public static Item launcherFiringAssembly;
    public static Item launcherGripFrame;

    // === MOUNTS / INTERFACES ===
    public static Item stockMount;
    public static Item opticMount;

    private static final CreativeTabs TAB = HGBaseItems.tabHMGCrafting;

    public static void init() {

        // ===== CORE =====
        firingPin = part("firingPin", "firing_pin");
        triggerAssembly = part("triggerAssembly", "trigger_assembly");
        springSet = part("springSet", "spring_set");

        // ===== MANUFACTURING TIERS =====
        machinedParts = part("machinedParts", "machined_parts");
        precisionComponents = part("precisionComponents", "precision_components");

        // ===== RECEIVERS =====
        stampedReceiver = part("stampedReceiver", "stamped_receiver");
        milledReceiver = part("milledReceiver", "milled_receiver");

        // ===== CORE ASSEMBLIES =====
        blowbackBoltAssembly = part("blowbackBoltAssembly", "blowback_bolt");
        rotatingBoltAssembly = part("rotatingBoltAssembly", "rotating_bolt");
        heavyBoltCarrierAssembly = part("heavyBoltCarrierAssembly", "heavy_bolt_carrier");
        boltActionAssembly = part("boltActionAssembly", "bolt_action");

        // ===== HANDGUN SYSTEMS =====
        handgunSlideAssembly = part("handgunSlideAssembly", "handgun_slide");
        revolverMechanismAssembly = part("revolverMechanismAssembly", "revolver_mechanism");

        // ===== BARRELS =====
        lightBarrelKit = part("lightBarrelKit", "light_barrel_kit");
        rifleBarrelKit = part("rifleBarrelKit", "rifle_barrel_kit");
        heavyBarrelKit = part("heavyBarrelKit", "heavy_barrel_kit");

        // ===== AR PLATFORM =====
        arUpper = part("arUpper", "ar_upper");
        arLower = part("arLower", "ar_lower");

        // ===== FEED & OPERATING MODULES =====
        gasSystemModule = part("gasSystemModule", "gas_system_module");
        beltFeedModule = part("beltFeedModule", "belt_feed_module");
        pumpLeverActionModule = part("pumpLeverActionModule", "pump_lever_action_module");

        // ===== LAUNCHER SYSTEMS =====
        launcherTube = part("launcherTube", "launcher_tube");
        launcherFiringAssembly = part("launcherFiringAssembly", "launcher_firing");
        launcherGripFrame = part("launcherGripFrame", "launcher_grip");

        // ===== INTERFACES =====
        stockMount = part("stockMount", "stock_mount");
        opticMount = part("opticMount", "optic_mount");
    }

    private static Item part(String name, String tex) {
        Item i = new Item()
                .setUnlocalizedName(name)
                .setTextureName("handmadeguns:" + tex)
                .setCreativeTab(TAB);
        GameRegistry.registerItem(i, name);
        return i;
    }
}





