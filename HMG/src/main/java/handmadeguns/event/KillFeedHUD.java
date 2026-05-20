package handmadeguns.event;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@SideOnly(Side.CLIENT)
public class KillFeedHUD {
    private static final List<KillEntry> ENTRIES = new ArrayList<KillEntry>();
    private static final int DISPLAY_TICKS = 300;

    public static void addEntry(String attacker, String victim, ItemStack weapon) {
        ItemStack copy = weapon == null ? null : weapon.copy();
        ENTRIES.add(0, new KillEntry(attacker, victim, copy, DISPLAY_TICKS));
        while (ENTRIES.size() > 5) {
            ENTRIES.remove(ENTRIES.size() - 1);
        }
    }

    public static void render(Minecraft minecraft) {
        if (ENTRIES.isEmpty()) return;

        ScaledResolution scaled = new ScaledResolution(minecraft, minecraft.displayWidth, minecraft.displayHeight);
        FontRenderer font = minecraft.fontRenderer;
        int x = scaled.getScaledWidth() - 170;
        int y = (scaled.getScaledHeight() / 2) - 50;

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        Iterator<KillEntry> it = ENTRIES.iterator();
        int row = 0;
        while (it.hasNext()) {
            KillEntry entry = it.next();
            entry.ticksLeft--;
            if (entry.ticksLeft <= 0) {
                it.remove();
                continue;
            }

            int rowY = y + (row * 20);
            Gui.drawRect(x - 4, rowY - 2, x + 164, rowY + 16, 0x4A000000);
            font.drawStringWithShadow(entry.attacker, x, rowY + 4, 0xFFFFFF);
            if (entry.weapon != null) {

                GL11.glPushMatrix();

                RenderHelper.enableGUIStandardItemLighting();

                GL11.glEnable(GL11.GL_DEPTH_TEST);
                GL11.glDepthMask(true);

                // Move to desired position
                GL11.glTranslatef(x + 82.0F, rowY + 8.0F, 0.0F);

                // Rotate item sideways
                GL11.glRotatef(-90.0F, 0.0F, 0.0F, 1.0F);

                // Center 16x16 item
                GL11.glTranslatef(-8.0F, -8.0F, 0.0F);

                RenderItem.getInstance().renderItemAndEffectIntoGUI(
                        font,
                        minecraft.renderEngine,
                        entry.weapon,
                        0,
                        0
                );

                RenderItem.getInstance().renderItemOverlayIntoGUI(
                        font,
                        minecraft.renderEngine,
                        entry.weapon,
                        0,
                        0
                );

                GL11.glDisable(GL11.GL_DEPTH_TEST);

                RenderHelper.disableStandardItemLighting();

                GL11.glPopMatrix();
            }
            font.drawStringWithShadow(entry.victim, x + 96, rowY + 4, 0xFFFFFF);
            row++;
        }

        GL11.glDepthMask(true);
        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }

    private static class KillEntry {
        final String attacker;
        final String victim;
        final ItemStack weapon;
        int ticksLeft;

        KillEntry(String attacker, String victim, ItemStack weapon, int ticksLeft) {
            this.attacker = attacker;
            this.victim = victim;
            this.weapon = weapon;
            this.ticksLeft = ticksLeft;
        }
    }
}
