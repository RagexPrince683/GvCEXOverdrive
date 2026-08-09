package handmadeguns.items;

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
    private boolean overlayAvailable = true;

    public HMGItemGunSkin(String skinId, String texture, Collection<String> targets) {
        this.skinId = skinId;
        this.overlayTexture = HMGGunSkinRegistry.textureLocation(texture);
        this.targets = new LinkedHashSet<String>(targets);
        setMaxStackSize(64);
    }

    public String getSkinId() { return skinId; }
    public ResourceLocation getOverlayTexture() { return overlayAvailable ? overlayTexture : null; }
    public void setOverlayAvailable(boolean available) { this.overlayAvailable = available; }
    public boolean supports(String gunId) {
        if (gunId == null) return false;
        if (targets.contains(gunId)) return true;
        int separator = gunId.indexOf(':');
        return separator >= 0 && targets.contains(gunId.substring(separator + 1));
    }
}
