package handmadeguns.client.render;

import handmadeguns.items.HMGItemAttachmentBase;
import handmadeguns.items.guns.HMGItem_Unified_Guns;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;
import org.lwjgl.opengl.GL11;

/** Shared local-space renderer for model-based attachments in every gun view. */
public final class HMGAttachmentModelRenderer {
    private HMGAttachmentModelRenderer() {}

    public static void renderInstalled(HMGItem_Unified_Guns gun, ItemStack[] slots, int pass,
                                       float gunPartsScale, ResourceLocation gunTexture) {
        if (gun == null || slots == null || !gun.gunInfo.hasAttachmentLocation) return;
        for (int slot = 1; slot < slots.length; slot++) {
            ItemStack stack = slots[slot];
            if (stack == null || !(stack.getItem() instanceof HMGItemAttachmentBase)) continue;
            HMGItemAttachmentBase attachment = (HMGItemAttachmentBase) stack.getItem();
            if (!attachment.has3dModel()) continue;
            IItemRenderer renderer = MinecraftForgeClient.getItemRenderer(stack, IItemRenderer.ItemRenderType.EQUIPPED);
            if (!(renderer instanceof HMGRenderItemCustom)) continue;

            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
            GL11.glPushMatrix();
            try {
                GL11.glTranslatef(gun.gunInfo.attachmentLocationX * gunPartsScale,
                        gun.gunInfo.attachmentLocationY * gunPartsScale,
                        gun.gunInfo.attachmentLocationZ * gunPartsScale);
                // Existing attachment rotations use Y first; the optional scalar follows that convention.
                GL11.glRotatef(gun.gunInfo.attachmentLocationRotation, 0, 1, 0);
                ((HMGRenderItemCustom) renderer).renderaspart(pass);
            } finally {
                GL11.glPopMatrix();
                GL11.glPopAttrib();
            }
            Minecraft.getMinecraft().renderEngine.bindTexture(gunTexture);
        }
    }
}
