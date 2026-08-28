package handmadeguns.client;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import handmadeguns.HMGPacketHandler;
import handmadeguns.network.PacketManualGunPickup;
import handmadeguns.HMGManualGunPickup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import org.lwjgl.input.Keyboard;

import static handmadeguns.HandmadeGunsCore.enableManualGunPickup;
import static handmadeguns.HandmadeGunsCore.manualGunPickupRange;

public class HMGManualGunPickupClientHandler {
    public static final KeyBinding PICKUP_KEY = new KeyBinding("Pickup HMG Gun", Keyboard.KEY_P, "HandmadeGuns");

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (!PICKUP_KEY.isPressed()) return;

        requestPickupForCurrentTarget();
    }

    @SubscribeEvent
    public void onMouseInput(MouseEvent event) {
        if (event.button != 1 || !event.buttonstate) return;

        if (requestPickupForCurrentTarget()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void renderPickupPrompt(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.HOTBAR || !enableManualGunPickup) return;

        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || minecraft.theWorld == null || minecraft.thePlayer == null) return;
        EntityPlayer player = minecraft.thePlayer;
        if (player == null || player.isDead) return;

        EntityItem target = findCurrentTarget(player);
        if (target == null) return;

        ItemStack stack = target.getEntityItem();
        if (stack == null) return;

        int keyCode = PICKUP_KEY.getKeyCode();
        String prompt;
        if (keyCode == Keyboard.KEY_NONE) {
            prompt = "Right Click to pick up " + stack.getDisplayName();
        } else {
            prompt = "Press [" + GameSettings.getKeyDisplayString(keyCode) + "] or Right Click to pick up " + stack.getDisplayName();
        }

        ScaledResolution resolution = new ScaledResolution(minecraft, minecraft.displayWidth, minecraft.displayHeight);
        int x = (resolution.getScaledWidth() - minecraft.fontRenderer.getStringWidth(prompt)) / 2;
        int y = resolution.getScaledHeight() - 59;
        minecraft.fontRenderer.drawStringWithShadow(prompt, x, y, 0xFFFFFF);
    }

    private boolean requestPickupForCurrentTarget() {
        if (!enableManualGunPickup) return false;

        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || minecraft.theWorld == null) return false;

        EntityPlayer player = minecraft.thePlayer;
        if (player == null || player.isDead) return false;

        EntityItem target = findCurrentTarget(player);
        if (target == null) return false;

        HMGPacketHandler.INSTANCE.sendToServer(new PacketManualGunPickup(target.getEntityId()));
        return true;
    }

    private EntityItem findCurrentTarget(EntityPlayer player) {
        return HMGManualGunPickup.getLookedAtGunItem(player, Math.max(0.1D, manualGunPickupRange));
    }
}
