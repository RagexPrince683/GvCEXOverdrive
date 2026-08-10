package handmadeguns;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.registry.GameRegistry;
import handmadeguns.items.HMGItemGunSkin;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;

/** In-memory skin index populated once by the content-pack attachment loader. */
public final class HMGGunSkinRegistry {
    public static final String NBT_KEY = "GunSkin";
    private static final Map<String, HMGItemGunSkin> SKINS = new LinkedHashMap<String, HMGItemGunSkin>();

    private HMGGunSkinRegistry() {}

    public static void register(HMGItemGunSkin skin) { SKINS.put(skin.getSkinId(), skin); }
    public static HMGItemGunSkin get(String id) { return id == null ? null : SKINS.get(id); }

    /** Run once after pack registration; malformed targets warn without disabling other skins. */
    public static void validateTargets() {
        for (HMGItemGunSkin skin : SKINS.values()) {
            for (String target : skin.getTargets()) {
                boolean matched = false;
                for (Object candidate : HMGGunMaker.Guns) {
                    if (candidate instanceof Item && targetMatches((Item)candidate, target)) {
                        matched = true;
                        break;
                    }
                }
                if (!matched) FMLLog.warning("[HMG] Gun skin '%s' has invalid SkinTarget '%s'; expected GunName or %s:GunName",
                        skin.getSkinId(), target, HandmadeGunsCore.MOD_ID);
            }
        }
    }

    private static boolean targetMatches(Item gun, String rawTarget) {
        GameRegistry.UniqueIdentifier actual = GameRegistry.findUniqueIdentifierFor(gun);
        if (actual == null || rawTarget == null) return false;
        String target = rawTarget.trim();
        int separator = target.indexOf(':');
        if (separator < 0) return actual.name.equals(target);
        String targetMod = target.substring(0, separator).trim();
        String targetName = target.substring(separator + 1).trim();
        return actual.name.equals(targetName) && (actual.modId.equals(targetMod)
                || (targetMod.equalsIgnoreCase(HandmadeGunsCore.MOD_ID)
                && actual.modId.equalsIgnoreCase(HandmadeGunsCore.MOD_ID)));
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
