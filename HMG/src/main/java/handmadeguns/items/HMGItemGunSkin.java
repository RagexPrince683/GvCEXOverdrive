package handmadeguns.items;

import cpw.mods.fml.common.registry.GameRegistry;
import handmadeguns.HMGGunSkinRegistry;
import handmadeguns.HandmadeGunsCore;
import handmadeguns.items.guns.HMGItem_Unified_Guns;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/** A pack-defined, ordinary inventory item which applies a texture overlay to guns. */
public class HMGItemGunSkin extends Item {
    private final String skinId;
    private final ResourceLocation overlayTexture;
    private final Set<String> targets;

    public HMGItemGunSkin(String skinId, String texture, Collection<String> targets) {
        this.skinId = skinId;
        this.overlayTexture = HMGGunSkinRegistry.textureLocation(texture);
        this.targets = new LinkedHashSet<String>();
        if (targets != null) for (String target : targets) {
            if (target != null && target.trim().length() > 0) this.targets.add(target.trim());
        }
        setMaxStackSize(64);
    }

    public String getSkinId() { return skinId; }
    public ResourceLocation getOverlayTexture() { return overlayTexture; }
    public Collection<String> getTargets() { return new LinkedHashSet<String>(targets); }
    public boolean isValid() { return skinId != null && skinId.length() > 0 && overlayTexture != null && !targets.isEmpty(); }

    /** Compare the gun's registered identity directly, never mutable stack state. */
    public boolean supports(Item gunItem) {
        if (!(gunItem instanceof HMGItem_Unified_Guns)) return false;
        GameRegistry.UniqueIdentifier actual = GameRegistry.findUniqueIdentifierFor(gunItem);
        if (actual == null) return false;
        for (String rawTarget : targets) {
            if (rawTarget == null) continue;
            String target = rawTarget.trim();
            if (target.length() == 0) continue;
            int separator = target.indexOf(':');
            if (separator < 0) {
                if (actual.name.equals(target)) return true;
                continue;
            }
            String targetMod = target.substring(0, separator).trim();
            String targetName = target.substring(separator + 1).trim();
            if (!actual.name.equals(targetName)) continue;
            if (actual.modId.equals(targetMod)) return true;
            if (targetMod.equalsIgnoreCase(HandmadeGunsCore.MOD_ID)
                    && actual.modId.equalsIgnoreCase(HandmadeGunsCore.MOD_ID)) return true;
        }
        return false;
    }

}
