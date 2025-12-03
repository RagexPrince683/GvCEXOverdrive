package handmadeguns.gunsmithing;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Mouse;

import java.util.List;

public class GunSmithingTableGui extends GuiScreen {

    private int selectedIndex = -1;
    private int scrollOffset = 0;

    private static final int LIST_X = 20;
    private static final int LIST_Y = 30;
    private static final int LIST_HEIGHT = 140;
    private static final int ENTRY_HEIGHT = 12;

    private EntityPlayer player;

    public GunSmithingTableGui(EntityPlayer player) {
        this.player = player;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        List<GunSmithRecipeRegistry.GunRecipeEntry> recipes =
                GunSmithRecipeRegistry.getAll();

        drawCenteredString(fontRendererObj, "GunSmith Table", width / 2, 8, 0xFFFFFF);

        // ✅ SCROLL HANDLING
        int maxVisible = LIST_HEIGHT / ENTRY_HEIGHT;
        int maxScroll = Math.max(0, recipes.size() - maxVisible);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));

        int end = Math.min(recipes.size(), scrollOffset + maxVisible);

        // ✅ LEFT PANEL — RECIPE LIST
        for (int i = scrollOffset; i < end; i++) {
            ItemStack gun = recipes.get(i).result;
            String name = gun.getDisplayName();

            int y = LIST_Y + (i - scrollOffset) * ENTRY_HEIGHT;

            if (i == selectedIndex) {
                drawRect(LIST_X - 2, y - 2, LIST_X + 140, y + 10, 0x8800AAFF);
            }

            fontRendererObj.drawString(name, LIST_X, y, 0xFFFFFF);
        }

        // ✅ RIGHT PANEL — RECIPE + INVENTORY CHECK
        if (selectedIndex >= 0 && selectedIndex < recipes.size()) {

            GunSmithRecipeRegistry.GunRecipeEntry entry =
                    recipes.get(selectedIndex);

            int previewX = width / 2 + 20;
            int previewY = 40;

            drawCenteredString(fontRendererObj,
                    entry.result.getDisplayName(),
                    previewX + 40, 30, 0xFFFFFF);

            RenderHelper.enableGUIStandardItemLighting();

            for (int i = 0; i < entry.inputs.length; i++) {
                ItemStack stack = entry.inputs[i];
                if (stack == null) continue;

                int x = previewX + (i % 3) * 22;
                int y = previewY + (i / 3) * 22;

                itemRender.renderItemIntoGUI(fontRendererObj,
                        mc.getTextureManager(), stack, x, y);

                int owned = countInInventory(stack);
                int needed = stack.stackSize;
                boolean missing = owned < needed;

                String txt = owned + "/" + needed;
                int color = missing ? 0xFF5555 : 0x55FF55; // ❌ red / ✅ green

                fontRendererObj.drawString(txt, x, y + 16, color);
            }

            RenderHelper.disableStandardItemLighting();
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    // ✅ MOUSE CLICK SELECTION
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {

        int index = (mouseY - LIST_Y) / ENTRY_HEIGHT + scrollOffset;

        if (mouseX >= LIST_X && mouseX <= LIST_X + 140) {
            if (index >= 0 && index < GunSmithRecipeRegistry.getAll().size()) {
                selectedIndex = index;
            }
        }

        super.mouseClicked(mouseX, mouseY, button);
    }

    // ✅ SCROLL WHEEL SUPPORT
    @Override
    public void handleMouseInput() {
        super.handleMouseInput();

        int wheel = Mouse.getDWheel();
        if (wheel > 0)
            scrollOffset--;
        else if (wheel < 0)
            scrollOffset++;
    }

    // ✅ INVENTORY COUNT CHECK (B + C)
    private int countInInventory(ItemStack target) {
        if (target == null || player == null)
            return 0;

        int count = 0;

        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            ItemStack inv = player.inventory.getStackInSlot(i);

            if (inv != null &&
                    inv.getItem() == target.getItem() &&
                    inv.getItemDamage() == target.getItemDamage()) {

                count += inv.stackSize;
            }
        }

        return count;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
