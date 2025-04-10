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

    // Function to draw the box with alpha transparency
    public static void drawRectWithAlpha(int left, int top, int right, int bottom, int color) {
        Tessellator tessellator = Tessellator.instance;

        // Enable blending for transparency effects
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // Render the glowing white border first (before the box)
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F); // Set white color for the glowing effect
        float glowThickness = 2.0f; // Thickness of the border glow

        // Draw the glowing border by making the box slightly larger than the original and drawing it first
        tessellator.startDrawingQuads();
        tessellator.addVertex((double) (left - glowThickness), (double) (bottom + glowThickness), 0.0D);
        tessellator.addVertex((double) (right + glowThickness), (double) (bottom + glowThickness), 0.0D);
        tessellator.addVertex((double) (right + glowThickness), (double) (top - glowThickness), 0.0D);
        tessellator.addVertex((double) (left - glowThickness), (double) (top - glowThickness), 0.0D);
        tessellator.draw();

        // Now render the inner, darker box (the main box)
        // Set the color for the main box (with a darker shade, reduce RGB values)
        GL11.glColor4ub((byte) (color >> 16 & 255), (byte) (color >> 8 & 255), (byte) (color & 255), (byte) (color >> 24 & 255));

        // Draw the inner, darker box (with the same dimensions as before)
        tessellator.startDrawingQuads();
        tessellator.addVertex((double) left, (double) bottom, 0.0D);
        tessellator.addVertex((double) right, (double) bottom, 0.0D);
        tessellator.addVertex((double) right, (double) top, 0.0D);
        tessellator.addVertex((double) left, (double) top, 0.0D);
        tessellator.draw();

        // Re-enable texture rendering and disable blending
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }

    // Function to draw the transparent box (no glowing effect)
    public static void drawTransparentRect(int left, int top, int right, int bottom, int color) {
        Tessellator tessellator = Tessellator.instance;

        // Enable blending for transparency effects
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // Set the color (with transparency)
        GL11.glColor4ub((byte) (color >> 16 & 255), (byte) (color >> 8 & 255), (byte) (color & 255), (byte) (color >> 24 & 255));

        // Draw the box without glowing
        tessellator.startDrawingQuads();
        tessellator.addVertex((double) left, (double) bottom, 0.0D);
        tessellator.addVertex((double) right, (double) bottom, 0.0D);
        tessellator.addVertex((double) right, (double) top, 0.0D);
        tessellator.addVertex((double) left, (double) top, 0.0D);
        tessellator.draw();

        // Re-enable texture rendering and disable blending
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }

    // Main method for rendering the ammo HUD
    public static void renderAmmoHUD(FontRenderer fontrenderer, int screenWidth, int screenHeight, ItemStack gunstack) {
        if (!(gunstack.getItem() instanceof HMGItem_Unified_Guns)) return;
        HMGItem_Unified_Guns gun = (HMGItem_Unified_Guns) gunstack.getItem();

        int current = gun.remain_Bullet(gunstack);
        int reserve = getTotalReserveAmmo(gunstack);
        String currentAmmo = String.format("%03d", current);
        String reserveAmmo = String.format("%03d", reserve);

        int mode = gunstack.getTagCompound().getInteger("HMGMode");
        String modeText = getFireModeText(gun, mode);

        String rpmText = "";
        int burst = gun.getburstCount(mode);
        boolean isSingle = (burst == 1 && gun.gunInfo.needcock);
        if (!isSingle && !gun.gunInfo.rates.isEmpty() && gun.gunInfo.rates.size() > mode && gun.gunInfo.rates.get(mode) > 0) {
            float delayTicks = gun.gunInfo.rates.get(mode);
            int rpm = (int) (1200.0 / delayTicks);
            rpmText = rpm + " RPM";
        }

        int boxHeight = 50;
        int boxWidth = 100;
        int x = screenWidth - boxWidth - 20;
        int y = screenHeight - boxHeight - 10;

        drawRectWithAlpha(x, y, x + boxWidth, y + boxHeight, 0x80000000);

        int textOffsetY = 5;

        // Scaling for current ammo count
        float currentAmmoScale = 2.2F;
        float reserveAmmoScale = 1.0F   ;

        // Scaled current ammo
        float currentAmmoX = x + 6;
        float currentAmmoY = y + textOffsetY;
        renderTextWithGlow(fontrenderer, currentAmmo, currentAmmoX, currentAmmoY, 0xFFFFFF, 0x000000, currentAmmoScale);

        // Width of scaled current ammo
        int currentAmmoWidth = (int) (fontrenderer.getStringWidth(currentAmmo) * currentAmmoScale);

        // Reserve ammo beside current, smaller
        float reserveAmmoX = currentAmmoX + currentAmmoWidth + 4; // +4 for spacing
        renderTextWithGlow(fontrenderer, " / " + reserveAmmo, reserveAmmoX, currentAmmoY + 1, 0xFFFFFF, 0x000000, reserveAmmoScale);

        // Fire mode, aligned under reserve
        renderFireModeWithColor(fontrenderer, modeText, (int) reserveAmmoX - 4, (int) currentAmmoY - 5);

        // RPM text
        if (!rpmText.isEmpty()) {
            int rpmX = x + 6;
            int rpmY = y + boxHeight - 20;
            renderText(fontrenderer, rpmText, rpmX, rpmY, 0x999999, 1.0F);
        }
    }

    // Function to render fire mode text with color
    private static void renderFireModeWithColor(FontRenderer fontrenderer, String modeText, int x, int y) {
        int modeColor = 0xFFFFFF; // Default color (white)

        switch (modeText) {
            case "SAFE":
                modeColor = 0x00FF00; // Green for SAFE
                break;
            case "SEMI":
            case "SINGLE":
                modeColor = 0xFFFF00; // Yellow for SEMI/SINGLE
                break;
            case "AUTO":
            case "BURST":
                modeColor = 0xFF0000; // Red for BURST/AUTO
                break;
        }

        renderTextWithGlow(fontrenderer, "[" + modeText + "]", x + 6.0F, y + 18.0F, modeColor, 0x000000, 1.0F);
    }

    public static void renderTextWithGlow(FontRenderer fontrenderer, String text, float x, float y, int textColor, int glowColor, float scale) {
        // These offsets are scaled inside the GL context now
        renderText(fontrenderer, text, x - 1 * scale, y - 1 * scale, glowColor, scale);
        renderText(fontrenderer, text, x + 1 * scale, y - 1 * scale, glowColor, scale);
        renderText(fontrenderer, text, x - 1 * scale, y + 1 * scale, glowColor, scale);
        renderText(fontrenderer, text, x + 1 * scale, y + 1 * scale, glowColor, scale);
        renderText(fontrenderer, text, x, y, textColor, scale);
    }

    public static void renderText(FontRenderer fontrenderer, String text, float x, float y, int color, float scale) {
        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, 0.0F);
        GL11.glScalef(scale, scale, scale);
        fontrenderer.drawStringWithShadow(text, 0, 0, color); // Draw at (0,0) since position is handled by glTranslate
        GL11.glPopMatrix();
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

        // 2. Check player inventory for matching magazines
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        InventoryPlayer inventory = player.inventory;

        for (int i = 0; i < inventory.mainInventory.length; i++) {
            ItemStack stack = inventory.mainInventory[i];
            if (stack != null && stack.getItem() == currentMagItem) {
                int ammo = (stack.getMaxDamage() - stack.getItemDamage()) * stack.stackSize;
                totalReserveAmmo += ammo;
            }
        }

        return totalReserveAmmo;
    }
}













