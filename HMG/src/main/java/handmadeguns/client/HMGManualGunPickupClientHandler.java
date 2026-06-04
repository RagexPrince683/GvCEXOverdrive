package handmadeguns.client;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import handmadeguns.HMGPacketHandler;
import handmadeguns.network.PacketManualGunPickup;
import handmadeguns.HMGManualGunPickup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.input.Keyboard;

import static handmadeguns.HandmadeGunsCore.enableManualGunPickup;
import static handmadeguns.HandmadeGunsCore.manualGunPickupRange;

public class HMGManualGunPickupClientHandler {
    public static final KeyBinding PICKUP_KEY = new KeyBinding("Pickup HMG Gun", Keyboard.KEY_P, "HandmadeGuns");

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (!enableManualGunPickup) return;
        if (!PICKUP_KEY.isPressed()) return;

        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || minecraft.theWorld == null) return;
        EntityPlayer player = minecraft.thePlayer;
        if (player == null || player.isDead) return;

        EntityItem target = HMGManualGunPickup.getLookedAtGunItem(player, Math.max(0.1D, manualGunPickupRange));
        if (target != null) {
            HMGPacketHandler.INSTANCE.sendToServer(new PacketManualGunPickup(target.getEntityId()));
        }
    }
}
