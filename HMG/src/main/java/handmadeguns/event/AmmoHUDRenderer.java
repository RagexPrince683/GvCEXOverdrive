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

        // Get the current ammo, magazine size, and total reserve ammo
        int current = gun.remain_Bullet(gunstack);  // Current ammo in the gun
        int reserve = getTotalReserveAmmo(gunstack); // Reserve ammo from inventory

        // Calculate fire mode text (Safe, Semi, Auto, Burst)
        int mode = gunstack.getTagCompound().getInteger("HMGMode");
        String modeText = getFireModeText(gun, mode);

        // Draw hud box
        int boxHeight = 40;
        int boxWidth = 120;  // Box width
        int x = screenWidth - boxWidth - 10;
        int y = screenHeight - boxHeight - 10;

        // Draw the background box with adjusted height
        drawRectWithAlpha(x, y, x + boxWidth, y + boxHeight, 0x80000000);

        // Format ammo string (current ammo / reserve ammo)
        String ammoString = current + " / " + reserve;
        fontrenderer.drawStringWithShadow(ammoString, x + 6, y + 6, 0xFFFFFF);
        fontrenderer.drawStringWithShadow(modeText, x + 6, y + 20, 0xCCCCCC);
    }

    // Function to get the fire mode text (Safe, Semi, Auto, Burst)
    private static String getFireModeText(HMGItem_Unified_Guns gun, int mode) {
        int burst = gun.getburstCount(mode);
        if (burst == 0) return "SAFE";
        else if (burst == 1 && gun.gunInfo.needcock) return "ONE";
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










