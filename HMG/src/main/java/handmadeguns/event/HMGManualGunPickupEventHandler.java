package handmadeguns.event;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import handmadeguns.HMGManualGunPickup;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;

import static handmadeguns.HandmadeGunsCore.enableManualGunPickup;

public class HMGManualGunPickupEventHandler {

    @SubscribeEvent
    public void onEntityItemPickup(EntityItemPickupEvent event) {
        if (!enableManualGunPickup) return;
        if (event == null || event.item == null) return;
        if (HMGManualGunPickup.isManualPickupEntity(event.item)) {
            event.setCanceled(true);
        }
    }
}
