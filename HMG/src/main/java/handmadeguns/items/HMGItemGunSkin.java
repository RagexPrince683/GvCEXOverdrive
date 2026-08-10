package handmadeguns.items;

import handmadeguns.HMGGunSkinRegistry;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

/** A pack-defined, ordinary inventory item which applies a texture overlay to guns. */
public class HMGItemGunSkin extends Item {
    private final String skinId;
    private final ResourceLocation overlayTexture;

    public HMGItemGunSkin(String skinId, String texture) {
        this.skinId = skinId;
        this.overlayTexture = HMGGunSkinRegistry.textureLocation(texture);
        setMaxStackSize(64);
    }

    public String getSkinId() { return skinId; }
    public ResourceLocation getOverlayTexture() { return overlayTexture; }
    public boolean isValid() { return skinId != null && skinId.length() > 0 && overlayTexture != null; }

}
