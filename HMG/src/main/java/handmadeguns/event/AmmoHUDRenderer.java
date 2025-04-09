package handmadeguns.event;

import handmadeguns.items.guns.HMGItem_Unified_Guns;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.renderer.Tessellator;

public class AmmoHUDRenderer {

    public static void renderAmmoHUD(FontRenderer fontrenderer, int screenWidth, int screenHeight, ItemStack gunstack) {
        if (!(gunstack.getItem() instanceof HMGItem_Unified_Guns)) return;
        HMGItem_Unified_Guns gun = (HMGItem_Unified_Guns) gunstack.getItem();

        // Ammo data
        int current = gun.remain_Bullet(gunstack);
        int reserve = getTotalReserveAmmo(gunstack);

        // Format ammo counts with 3 digits (000 to 999)
        String currentAmmo = String.format("%03d", current);
        String reserveAmmo = String.format("%03d", reserve);

        // Fire mode
        int mode = gunstack.getTagCompound().getInteger("HMGMode");
        String modeText = getFireModeText(gun, mode);

        // Proper RPM logic from mod behavior
        String rpmText = "";
        int burst = gun.getburstCount(mode);
        boolean isSingle = (burst == 1 && gun.gunInfo.needcock);

        if (!isSingle && !gun.gunInfo.rates.isEmpty() && gun.gunInfo.rates.size() > mode && gun.gunInfo.rates.get(mode) > 0) {
            float delayTicks = gun.gunInfo.rates.get(mode);
            int rpm = (int) (1200.0 / delayTicks); // 60s * 20 ticks = 1200
            rpmText = rpm + " RPM";
        }

        // Box size
        int boxHeight = 45;
        int boxWidth = 120;
        int x = screenWidth - boxWidth - 10;
        int y = screenHeight - boxHeight - 10;

        drawRectWithAlpha(x, y, x + boxWidth, y + boxHeight, 0x80000000);

        // Render current ammo without brackets
        renderText(fontrenderer, currentAmmo, x + 6, y + 6, 0xFFFFFF);

        // Calculate the new width after rendering current ammo (no scaling applied)
        int currentAmmoWidth = fontrenderer.getStringWidth(currentAmmo);

        // Reserve ammo count (now slightly below current ammo, to the right of it)
        renderText(fontrenderer, "/ " + reserveAmmo, x + 6 + currentAmmoWidth, y + 6, 0xBBBBBB);

        // Fire mode (slightly to the left)
        renderText(fontrenderer, "[" + modeText + "]", x + 6 + currentAmmoWidth - 4, y + 18, 0xCCCCCC);

        // RPM - Now static inside the box, not shifting
        if (!rpmText.isEmpty()) {
            int rpmX = x + 6; // Fixed X position within the box
            int rpmY = y + boxHeight - 15; // Adjust Y for a static position inside the box
            renderText(fontrenderer, rpmText, rpmX, rpmY, 0x999999); // RPM text rendered at fixed position inside the box
        }
    }

    // Function to render text with shadow (helper method)
    private static void renderText(FontRenderer fontrenderer, String text, int x, int y, int color) {
        fontrenderer.drawStringWithShadow(text, x, y, color);
    }

    // Function to get the fire mode text (Safe, Semi, Auto, Burst)
    private static String getFireModeText(HMGItem_Unified_Guns gun, int mode) {
        int burst = gun.getburstCount(mode);
        if (burst == 0) return "SAFE";
        else if (burst == 1 && gun.gunInfo.needcock) return "SINGLE";
        else if (burst == 1) return "SEMI";
        else if (burst == -1) return "AUTO";
        else return burst + "BURST";
    }

    // Function to calculate total reserve ammo from player's inventory
    public static int getTotalReserveAmmo(ItemStack gunStack) {
        if (gunStack == null || !(gunStack.getItem() instanceof HMGItem_Unified_Guns)) return 0;

        HMGItem_Unified_Guns gun = (HMGItem_Unified_Guns) gunStack.getItem();
        Item currentMagItem = gun.getcurrentMagazine(gunStack);
        if (currentMagItem == null) return 0;

        int totalReserveAmmo = 0;

        // 1. Check internal loaded magazines
        ItemStack[] loadedMags = gun.get_loadedMagazineStack(gunStack);
        for (ItemStack mag : loadedMags) {
            if (mag != null && mag.getItem() == currentMagItem) {
                int ammo = (mag.getMaxDamage() - mag.getItemDamage()) * mag.stackSize;
                totalReserveAmmo += ammo;
            }
        }

        // 2. Check player inventory for more mags of this type
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        InventoryPlayer inv = player.inventory;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack != null && stack.getItem() == currentMagItem) {
                int ammo = (stack.getMaxDamage() - stack.getItemDamage()) * stack.stackSize;
                totalReserveAmmo += ammo;
            }
        }

        return totalReserveAmmo;
    }

    // Function to draw a box with alpha transparency
    public static void drawRectWithAlpha(int left, int top, int right, int bottom, int color) {
        Tessellator tessellator = Tessellator.instance;
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4ub((byte) (color >> 16 & 255), (byte) (color >> 8 & 255), (byte) (color & 255), (byte) (color >> 24 & 255));
        tessellator.startDrawingQuads();
        tessellator.addVertex((double) left, (double) bottom, 0.0D);
        tessellator.addVertex((double) right, (double) bottom, 0.0D);
        tessellator.addVertex((double) right, (double) top, 0.0D);
        tessellator.addVertex((double) left, (double) top, 0.0D);
        tessellator.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }
}











