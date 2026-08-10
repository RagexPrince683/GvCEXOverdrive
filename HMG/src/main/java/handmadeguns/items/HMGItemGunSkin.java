package handmadeguns.items;

import handmadeguns.HMGGunSkinRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import java.util.List;

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

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean advanced) {
        tooltip.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("hmg.tooltip.gun_skin.apply"));
        tooltip.add(EnumChatFormatting.DARK_GRAY + StatCollector.translateToLocal(isValid()
                ? "hmg.tooltip.gun_skin.compatibility.universal"
                : "hmg.tooltip.gun_skin.invalid"));
    }

}
