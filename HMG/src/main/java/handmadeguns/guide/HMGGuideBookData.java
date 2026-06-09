package handmadeguns.guide;

import handmadeguns.HandmadeGunsCore;
import handmadeguns.items.HGBaseItems;
import handmadeguns.items.HGGunItems;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Guidebook content model with no Guide-API references.  The optional Guide-API
 * adapter consumes these simple data objects through reflection so the content
 * can later be generated from registered gun/part metadata.
 */
public final class HMGGuideBookData {
    public static final String BOOK_TITLE_KEY = "hmg.guide.book.title";
    public static final String BOOK_WELCOME_KEY = "hmg.guide.book.welcome";
    public static final String BOOK_DISPLAY_KEY = "item.hmg_field_manual.name";

    private HMGGuideBookData() {
    }

    public static List<CategorySpec> createCategories() {
        List<CategorySpec> categories = new ArrayList<CategorySpec>();
        categories.add(category("getting_started", stack(HGBaseItems.polymer),
                entry("overview", text("getting_started.overview"), text("getting_started.crafting")),
                entry("crafting_materials", item("materials.polymer", HGBaseItems.polymer), item("materials.spring_set", HGGunItems.springSet), item("materials.machined", HGGunItems.machinedParts), item("materials.precision", HGGunItems.precisionComponents))));
        categories.add(category("weapon_basics", stack(HandmadeGunsCore.hmg_bullet),
                entry("gun_stats", text("weapon_basics.stats"), generated("weapon_basics.generated")),
                entry("magazines_ammo", item("ammo.rifle", HandmadeGunsCore.hmg_bullet), item("ammo.handgun", HandmadeGunsCore.hmg_bullet_hg), item("ammo.shotgun", HandmadeGunsCore.hmg_bullet_shell), item("ammo.lmg", HandmadeGunsCore.hmg_bullet_lmg), item("ammo.rocket", HandmadeGunsCore.hmg_bullet_rr))));
        categories.add(category("receivers_assemblies", stack(HGGunItems.stampedReceiver),
                entry("receivers", item("receivers.stamped", HGGunItems.stampedReceiver), item("receivers.milled", HGGunItems.milledReceiver), item("receivers.ar_upper", HGGunItems.arUpper), item("receivers.ar_lower", HGGunItems.arLower)),
                entry("assemblies", item("assemblies.blowback", HGGunItems.blowbackBoltAssembly), item("assemblies.rotating", HGGunItems.rotatingBoltAssembly), item("assemblies.heavy", HGGunItems.heavyBoltCarrierAssembly), item("assemblies.bolt_action", HGGunItems.boltActionAssembly), item("assemblies.handgun", HGGunItems.handgunSlideAssembly), item("assemblies.revolver", HGGunItems.revolverMechanismAssembly))));
        categories.add(category("barrels_stocks_attachments", stack(HGGunItems.rifleBarrelKit),
                entry("barrels", item("barrels.light", HGGunItems.lightBarrelKit), item("barrels.rifle", HGGunItems.rifleBarrelKit), item("barrels.heavy", HGGunItems.heavyBarrelKit)),
                entry("stocks_mounts", item("attachments.stock_mount", HGGunItems.stockMount), item("attachments.optic_mount", HGGunItems.opticMount), item("attachments.furniture", HGGunItems.woodGunFurniture), text("attachments.effects"))));
        categories.add(category("ammo_compatibility", stack(HandmadeGunsCore.hmg_bullet),
                entry("ammo_types", text("ammo.compatibility"), item("ammo.rifle", HandmadeGunsCore.hmg_bullet), item("ammo.handgun", HandmadeGunsCore.hmg_bullet_hg), item("ammo.shotgun", HandmadeGunsCore.hmg_bullet_shell), item("ammo.lmg", HandmadeGunsCore.hmg_bullet_lmg), item("ammo.rocket", HandmadeGunsCore.hmg_bullet_rr)),
                entry("special_projectiles", text("ammo.special_projectiles"))));
        categories.add(category("handling", stack(HGGunItems.stockMount),
                entry("recoil_ads", text("handling.recoil_ads"), generated("handling.generated")),
                entry("sprint_setup", text("handling.sprint_setup"), text("handling.attachments"))));
        categories.add(category("explosives_damage", stack(Items.gunpowder),
                entry("explosives", text("explosives.overview"), generated("explosives.generated")),
                entry("damage", text("explosives.damage"))));
        categories.add(category("armor_flans", stack(Items.iron_chestplate),
                entry("armor", text("armor_flans.armor")),
                entry("flans", text("armor_flans.flans"))));
        categories.add(category("gun_pack_creation", stack(HGGunItems.precisionComponents),
                entry("folders", text("gun_pack.folders")),
                entry("metadata", text("gun_pack.metadata"))));
        categories.add(category("server_config_balance", stack(Items.redstone),
                entry("config", generated("server_config.generated"), text("server_config.balance")),
                entry("crafting", text("server_config.crafting"))));
        categories.add(category("troubleshooting", stack(Items.book),
                entry("missing_manual", text("troubleshooting.missing_manual")),
                entry("common", text("troubleshooting.common"))));
        return categories;
    }

    private static CategorySpec category(String id, ItemStack icon, EntrySpec... entries) {
        CategorySpec spec = new CategorySpec(id, icon);
        for (int i = 0; i < entries.length; i++) spec.entries.add(entries[i]);
        return spec;
    }

    private static EntrySpec entry(String id, PageSpec... pages) {
        EntrySpec spec = new EntrySpec(id);
        for (int i = 0; i < pages.length; i++) if (pages[i] != null) spec.pages.add(pages[i]);
        return spec;
    }

    private static PageSpec text(String keySuffix) {
        return PageSpec.text("hmg.guide.page." + keySuffix);
    }

    private static PageSpec item(String keySuffix, Item item) {
        return item == null ? null : PageSpec.item("hmg.guide.page." + keySuffix, new ItemStack(item));
    }

    private static PageSpec generated(String keySuffix) {
        return PageSpec.generated("hmg.guide.page." + keySuffix);
    }

    private static ItemStack stack(Item item) {
        return item == null ? new ItemStack(Items.book) : new ItemStack(item);
    }

    public static final class CategorySpec {
        public final String id;
        public final ItemStack icon;
        public final List<EntrySpec> entries = new ArrayList<EntrySpec>();

        private CategorySpec(String id, ItemStack icon) {
            this.id = id;
            this.icon = icon;
        }

        public String key() {
            return "hmg.guide.category." + id;
        }
    }

    public static final class EntrySpec {
        public final String id;
        public final List<PageSpec> pages = new ArrayList<PageSpec>();

        private EntrySpec(String id) {
            this.id = id;
        }

        public String key(CategorySpec category) {
            return "hmg.guide.entry." + category.id + "." + id;
        }
    }

    public static final class PageSpec {
        public final String key;
        public final ItemStack stack;
        public final boolean generated;

        private PageSpec(String key, ItemStack stack, boolean generated) {
            this.key = key;
            this.stack = stack;
            this.generated = generated;
        }

        private static PageSpec text(String key) {
            return new PageSpec(key, null, false);
        }

        private static PageSpec item(String key, ItemStack stack) {
            return new PageSpec(key, stack, false);
        }

        private static PageSpec generated(String key) {
            return new PageSpec(key, null, true);
        }
    }
}
