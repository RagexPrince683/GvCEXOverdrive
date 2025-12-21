package handmadeguns.client.render;

import cpw.mods.fml.client.FMLClientHandler;
import handmadeguns.entity.PlacedGunEntity;
import handmadeguns.items.HMGItemAttachment_reddot;
import handmadeguns.items.HMGItemAttachment_scope;
import handmadeguns.items.HMGItemSightBase;
import handmadeguns.HandmadeGunsCore;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import static handmadeguns.HandmadeGunsCore.smooth;
import static net.minecraft.util.MathHelper.wrapAngleTo180_float;

public class PlacedGun_Render extends Render {

    //TODO: UNFUCK THIS ENTIRE GODDAMN CLASS HOLY FUCKING SHIT ITS SO ASS
    @Override
    public void doRender(Entity p_76986_1_, double p_76986_2_, double p_76986_4_, double p_76986_6_, float p_76986_8_, float p_76986_9_) {
        doRender((PlacedGunEntity)p_76986_1_,p_76986_2_,p_76986_4_,p_76986_6_,p_76986_8_,p_76986_9_);
    }

    public void doRender(PlacedGunEntity entity, double p_180551_2_, double p_180551_4_, double p_180551_6_, float p_180551_8_, float p_180551_9_) {
        float lastBrightnessX = OpenGlHelper.lastBrightnessX;
        float lastBrightnessY = OpenGlHelper.lastBrightnessY;

        // ADS visibility checks (keeps original behavior but safe against NPEs)
        if (entity.riddenByEntity == FMLClientHandler.instance().getClientPlayerEntity() && entity.gunStack != null && entity.gunItem != null) {
            ItemStack itemstackSight = getSightSafe(entity.gunStack, entity.gunItem);

            if (HandmadeGunsCore.Key_ADS(entity.riddenByEntity) && (entity.riddenByEntity == null || !entity.riddenByEntity.isSprinting())) {
                if (itemstackSight != null && itemstackSight.getItem() instanceof HMGItemSightBase) {
                    HMGItemSightBase sight = (HMGItemSightBase) itemstackSight.getItem();
                    if (sight.scopeonly) {
                        return;
                    } else if (itemstackSight.getItem() instanceof HMGItemAttachment_reddot) {
                        if (!entity.gunItem.gunInfo.zoomrer) {
                            return;
                        }
                    } else if (itemstackSight.getItem() instanceof HMGItemAttachment_scope) {
                        if (!entity.gunItem.gunInfo.zoomres) {
                            return;
                        }
                    }
                } else {
                    if (!entity.gunItem.gunInfo.zoomren) {
                        return;
                    }
                }
            }
        }

        GL11.glPushMatrix();
        GL11.glTranslatef((float) p_180551_2_, (float) p_180551_4_ + entity.gunyoffset, (float) p_180551_6_);

        if (entity.gunStack != null) {
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            IItemRenderer gunrender = MinecraftForgeClient.getItemRenderer(entity.gunStack, IItemRenderer.ItemRenderType.EQUIPPED);

            if (gunrender instanceof HMGRenderItemGun_U_NEW) {
                // keep original rotations/flag behaviour exactly
                GL11.glRotatef(-(entity.rotationYaw), 0.0F, 1.0F, 0.0F);

                HMGRenderItemGun_U_NEW.isPlacedGun = true;
                HMGRenderItemGun_U_NEW.turretYaw = wrapAngleTo180_float(entity.rotationYaw - (entity.prevrotationYawGun + (entity.rotationYawGun - entity.prevrotationYawGun) * smooth));
                HMGRenderItemGun_U_NEW.turretPitch = wrapAngleTo180_float((entity.prevRotationPitch + wrapAngleTo180_float(entity.rotationPitch - entity.prevRotationPitch) * smooth));

                GL11.glScalef(0.5f, 0.5f, 0.5f);
                try {
                    gunrender.renderItem(IItemRenderer.ItemRenderType.ENTITY, entity.gunStack);
                } catch (Throwable t) {
                    // fail-safe: avoid breaking the game if a custom renderer throws
                    t.printStackTrace();
                } finally {
                    HMGRenderItemGun_U_NEW.isPlacedGun = false;
                }
            } else if (gunrender instanceof HMGRenderItemGun_U) {
                // base は matbase を利用してそれらしく描画可能
                GL11.glRotatef(-(entity.prevrotationYawGun + (entity.rotationYawGun - entity.prevrotationYawGun) * smooth), 0.0F, 1.0F, 0.0F);
                GL11.glRotatef(-(-entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * smooth), 1.0F, 0.0F, 0.0F);
                GL11.glScalef(0.5f, 0.5f, 0.5f);
                try {
                    gunrender.renderItem(IItemRenderer.ItemRenderType.ENTITY, entity.gunStack);
                } catch (Throwable t) {
                    // fail-safe: avoid breaking the game if a custom renderer throws
                    t.printStackTrace();
                }
            }

            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        }

        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)lastBrightnessX, (float)lastBrightnessY);
        GL11.glPopMatrix();
    }

    /**
     * Safely attempt to retrieve sight (slot 1) from the gun stack's NBT.
     * If NBT is missing, attempt to let the gunItem initialize tags (original behaviour),
     * then re-check. Returns null when no sight found or on any missing data.
     */
    private ItemStack getSightSafe(ItemStack gunStack, net.minecraft.item.Item gunItemGeneric) {
        if (gunStack == null) return null;

        NBTTagCompound nbt = gunStack.getTagCompound();
        if (nbt == null) {
            // original code calls checkTags on gunItem; only call if the item actually has that method.
            try {
                // try calling checkTags(Object) via the expected gunItem type if available
                // many of your gunItem classes implement checkTags(ItemStack), so keep the original behavior:
                if (gunItemGeneric instanceof handmadeguns.items.guns.HMGItem_Unified_Guns) {
                    ((handmadeguns.items.guns.HMGItem_Unified_Guns) gunItemGeneric).checkTags(gunStack);
                } else {
                    // fallback: attempt reflection if your actual item class is different
                    // avoid throwing if method not present
                    try {
                        java.lang.reflect.Method m = gunItemGeneric.getClass().getMethod("checkTags", ItemStack.class);
                        m.invoke(gunItemGeneric, gunStack);
                    } catch (NoSuchMethodException ignored) {
                        // nothing we can do; continue
                    }
                }
            } catch (Throwable ignored) {
                // ignore any errors from checkTags
            }
            nbt = gunStack.getTagCompound();
        }

        if (nbt == null) return null;

        // original code used nbt.getTag("Items") and casted to NBTTagList; guard against null
        if (!nbt.hasKey("Items")) return null;
        NBTTagList tags = (NBTTagList) nbt.getTag("Items");
        if (tags == null) return null;

        for (int i = 0; i < tags.tagCount(); i++) {
            NBTTagCompound tagCompound = tags.getCompoundTagAt(i);
            if (tagCompound == null) continue;
            int slot = tagCompound.getByte("Slot");
            if (slot == 1) { // sight slot
                try {
                    return ItemStack.loadItemStackFromNBT(tagCompound);
                } catch (Throwable ignored) {
                    // if load fails, continue searching
                }
            }
        }
        return null;
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity p_110775_1_) {
        return null;
    }
}
