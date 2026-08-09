package handmadeguns.items;

import cpw.mods.fml.common.registry.GameRegistry;
import handmadeguns.HMGGunSkinRegistry;
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
        this.targets = new LinkedHashSet<String>(targets);
        setMaxStackSize(64);
    }

    public String getSkinId() { return skinId; }
    public ResourceLocation getOverlayTexture() { return overlayTexture; }
    public boolean isValid() { return skinId != null && skinId.length() > 0 && overlayTexture != null && !targets.isEmpty(); }

    /** Resolve exact Forge registry names and compare the shared Item identity, never stack state. */
    public boolean supports(Item gunItem) {
        if (gunItem == null) return false;
        for (String target : targets) {
            int separator = target.indexOf(':');
            if (separator <= 0 || separator == target.length() - 1) continue;
            Item targetItem = GameRegistry.findItem(target.substring(0, separator), target.substring(separator + 1));
            if (targetItem == gunItem) return true;
        }
        return false;
    }

}
