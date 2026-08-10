package handmadeguns;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.registry.GameRegistry;
import handmadeguns.items.HMGItemGunSkin;
import handmadeguns.items.guns.HMGItem_Unified_Guns;
import handmadeguns.recipes.HMGRecipeApplyGunSkin;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;

/** In-memory skin index populated once by the content-pack attachment loader. */
public final class HMGGunSkinRegistry {
    public static final String NBT_KEY = "GunSkin";
    private static final Map<String, HMGItemGunSkin> SKINS = new LinkedHashMap<String, HMGItemGunSkin>();
    private static final Map<String, HMGItem_Unified_Guns> GUNS = new LinkedHashMap<String, HMGItem_Unified_Guns>();

    private HMGGunSkinRegistry() {}

    public static void register(HMGItemGunSkin skin) { SKINS.put(skin.getSkinId(), skin); }
    public static HMGItemGunSkin get(String id) { return id == null ? null : SKINS.get(id); }

    /** Records the final gun object chosen by the pack loader under its declared GunName. */
    public static void registerGun(String gunName, HMGItem_Unified_Guns gun) {
        if (gunName != null && gun != null) GUNS.put(gunName, gun);
    }

    /** Generates one recipe per resolved target after every content pack has loaded. */
    public static void registerCraftingRecipes() {
        int recipeCount = 0;
        for (HMGItemGunSkin skin : SKINS.values()) {
            for (String rawTarget : skin.getTargets()) {
                String gunName = hmgGunName(rawTarget);
                if (gunName == null) {
                    FMLLog.warning("[HMG] Gun skin '%s' targets unknown gun '%s' (only HMG GunName targets are supported)",
                            skin.getSkinId(), rawTarget);
                    continue;
                }
                HMGItem_Unified_Guns gun = GUNS.get(gunName);
                if (gun == null) {
                    FMLLog.warning("[HMG] Gun skin '%s' targets unknown gun '%s'",
                            skin.getSkinId(), gunName);
                    continue;
                }
                GameRegistry.addRecipe(new HMGRecipeApplyGunSkin(gun, skin));
                recipeCount++;
            }
        }
        FMLLog.info("[HMG] Registered %d gun skin recipes for %d gun skins.", recipeCount, SKINS.size());
    }

    private static String hmgGunName(String rawTarget) {
        if (rawTarget == null) return null;
        String target = rawTarget.trim();
        if (target.length() == 0) return null;
        int separator = target.indexOf(':');
        if (separator < 0) return target;
        String targetMod = target.substring(0, separator).trim();
        String targetName = target.substring(separator + 1).trim();
        return targetMod.equalsIgnoreCase(HandmadeGunsCore.MOD_ID) && targetName.length() > 0 ? targetName : null;
    }

    public static String gunId(ItemStack gun) {
        GameRegistry.UniqueIdentifier id = GameRegistry.findUniqueIdentifierFor(gun.getItem());
        return id == null ? null : id.modId + ":" + id.name;
    }

    public static ResourceLocation appliedTexture(ItemStack gun) {
        if (gun == null || gun.getTagCompound() == null || !gun.getTagCompound().hasKey(NBT_KEY)) return null;
        HMGItemGunSkin skin = get(gun.getTagCompound().getString(NBT_KEY));
        return skin != null && skin.isValid() && skin.supports(gun.getItem()) ? skin.getOverlayTexture() : null;
    }

    public static ResourceLocation textureLocation(String value) {
        if (value == null || value.trim().length() == 0) return null;
        String path = value.trim();
        path = path.replace('\\', '/');
        if (path.indexOf(':') >= 0) return new ResourceLocation(path);
        if (!path.endsWith(".png")) path += ".png";
        if (!path.startsWith("textures/")) path = "textures/model/" + path;
        return new ResourceLocation("handmadeguns", path);
    }
}
