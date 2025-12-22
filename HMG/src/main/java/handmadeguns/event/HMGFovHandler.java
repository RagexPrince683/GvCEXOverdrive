package handmadeguns.event;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import handmadeguns.HandmadeGunsCore;
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
        ItemStack held = player.getHeldItem();
        if (held == null) return;

        if (!(held.getItem() instanceof HMGItem_Unified_Guns)) return;

        HMGItem_Unified_Guns gun = (HMGItem_Unified_Guns) held.getItem();

        // ADS only
        if (!HandmadeGunsCore.Key_ADS(player)) return;

        // Sprinting cancels zoom
        if (player.isSprinting()) return;

        //todo grab the following as well:
        // gunItem.gunInfo.scopezoomscope
        // ((HMGItemSightBase) itemstackSight.getItem()).zoomlevel
        // gunItem.gunInfo.scopezoomred

        // Apply zoom safely
        float zoom = gun.gunInfo.scopezoombase;
        if (zoom <= 0f) return;

        event.newfov /= zoom;
    }
}
