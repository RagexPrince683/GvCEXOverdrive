package handmadeguns.event;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import handmadeguns.HandmadeGunsCore;
import handmadeguns.entity.PlacedGunEntity;
import handmadeguns.items.HMGItemAttachment_reddot;
import handmadeguns.items.HMGItemAttachment_scope;
import handmadeguns.items.HMGItemSightBase;
import handmadeguns.items.guns.HMGItem_Unified_Guns;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.client.event.FOVUpdateEvent;

@SideOnly(Side.CLIENT)
public class HMGFovHandler {

    @SubscribeEvent
    public void onFovUpdate(FOVUpdateEvent event) {
        HMGEventZoom.currentZoomLevel = 1.0F;
        if (!(event.entity instanceof EntityPlayer)) return;

        EntityPlayer player = (EntityPlayer) event.entity;
        ItemStack held = player.getHeldItem();
        if (player.ridingEntity instanceof PlacedGunEntity) {
            ItemStack placedGun = ((PlacedGunEntity) player.ridingEntity).gunStack;
            if (placedGun != null && placedGun.getItem() instanceof HMGItem_Unified_Guns) {
                held = placedGun;
            }
        }

        if (held == null || !(held.getItem() instanceof HMGItem_Unified_Guns)) return;

        HMGItem_Unified_Guns gun = (HMGItem_Unified_Guns) held.getItem();

        // ADS only
        if (!HandmadeGunsCore.Key_ADS(player)) return;

        // Sprinting cancels zoom
        if (player.isSprinting()) return;

        float zoom = resolveZoom(gun, findSightInSlotOne(held));
        HMGEventZoom.currentZoomLevel = zoom;

        event.newfov /= zoom;
    }

    private static ItemStack findSightInSlotOne(ItemStack gunStack) {
        if (!gunStack.hasTagCompound()) return null;

        NBTBase itemsTag = gunStack.getTagCompound().getTag("Items");
        if (!(itemsTag instanceof NBTTagList)) return null;

        NBTTagList tags = (NBTTagList) itemsTag;
        for (int i = 0; i < tags.tagCount(); i++) {
            NBTTagCompound entry = tags.getCompoundTagAt(i);
            if (entry != null && entry.getByte("Slot") == 1) {
                try {
                    return ItemStack.loadItemStackFromNBT(entry);
                } catch (RuntimeException ignored) {
                    return null;
                }
            }
        }
        return null;
    }

    private static float resolveZoom(HMGItem_Unified_Guns gun, ItemStack sightStack) {
        float zoom = gun.gunInfo.scopezoombase;
        if (sightStack != null && sightStack.getItem() != null) {
            Item sight = sightStack.getItem();
            if (sight instanceof HMGItemAttachment_reddot) {
                zoom = gun.gunInfo.scopezoomred;
            } else if (sight instanceof HMGItemAttachment_scope) {
                zoom = gun.gunInfo.scopezoomscope;
            } else if (sight instanceof HMGItemSightBase) {
                zoom = ((HMGItemSightBase) sight).zoomlevel;
            }
        }
        return isValidZoom(zoom) ? zoom : 1.0F;
    }

    private static boolean isValidZoom(float zoom) {
        return zoom > 0.0F && !Float.isNaN(zoom) && !Float.isInfinite(zoom);
    }
}
