package handmadeguns;

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

    public static String gunId(ItemStack gun) {
        GameRegistry.UniqueIdentifier id = GameRegistry.findUniqueIdentifierFor(gun.getItem());
        return id == null ? null : id.modId + ":" + id.name;
    }

    public static ResourceLocation appliedTexture(ItemStack gun) {
        if (gun == null || gun.getTagCompound() == null || !gun.getTagCompound().hasKey(NBT_KEY)) return null;
        HMGItemGunSkin skin = get(gun.getTagCompound().getString(NBT_KEY));
        return skin != null && skin.supports(gunId(gun)) ? skin.getOverlayTexture() : null;
    }

    public static ResourceLocation textureLocation(String value) {
        if (value == null || value.trim().length() == 0) return null;
        String path = value.trim();
        if (path.indexOf(':') >= 0) return HMGGunMaker.getCachedResourceLocation(path);
        if (!path.endsWith(".png")) path += ".png";
        if (!path.startsWith("textures/")) path = "textures/model/" + path;
        return HMGGunMaker.getCachedResourceLocation("handmadeguns:" + path);
    }
}
