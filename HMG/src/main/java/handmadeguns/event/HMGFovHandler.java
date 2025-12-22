package handmadeguns.event;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import handmadeguns.HandmadeGunsCore;
import handmadeguns.items.HMGItemSightBase;
import handmadeguns.items.guns.HMGItem_Unified_Guns;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.FOVUpdateEvent;

@SideOnly(Side.CLIENT)
public class HMGFovHandler {

    @SubscribeEvent
    public void onFovUpdate(FOVUpdateEvent event) {
        if (!(event.entity instanceof EntityPlayer)) return;

        EntityPlayer player = (EntityPlayer) event.entity;

        if (!HandmadeGunsCore.Key_ADS(player)) return;
        if (player.isSprinting()) return;

        ItemStack held = player.getHeldItem();
        if (held == null) return;
        if (!(held.getItem() instanceof HMGItem_Unified_Guns)) return;

        HMGItem_Unified_Guns gun = (HMGItem_Unified_Guns) held.getItem();

        float zoom = 1.0f;

        // Base gun zoom
        if (!gun.gunInfo.canobj && gun.gunInfo.scopezoombase > 0f) {
            zoom = gun.gunInfo.scopezoombase;
        }

        // Sight override
        ItemStack sight = gun(held); // however you resolve this
        if (sight != null && sight.getItem() instanceof HMGItemSightBase) {
            HMGItem_Unified_Guns sightItem = (HMGItem_Unified_Guns) sight.getItem();

            if (!gun.gunInfo.canobj || sightItem.scopeonly) {
                if (sightItem.zoomlevel > 0f) {
                    zoom = sightItem.zoomlevel;
                }
            }
        }

        if (zoom > 0f) {
            event.newfov /= zoom;
        }
    }
}

