package handmadeguns.gunsmithing;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

import java.util.List;

public class GunSmithingTableGui extends GuiScreen {

    private int selectedIndex = -1;

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        List<GunSmithRecipeRegistry.GunRecipeEntry> recipes =
                GunSmithRecipeRegistry.getAll();

        // ✅ TITLE
        drawCenteredString(fontRendererObj, "GunSmith Table", width / 2, 8, 0xFFFFFF);

        int listX = 20;
        int listY = 30;

        // ✅ LEFT SIDE — GUN LIST
        for (int i = 0; i < recipes.size(); i++) {
            ItemStack gun = recipes.get(i).result;
            String name = gun.getDisplayName();

            int y = listY + i * 12;

            // Highlight selected
            if (i == selectedIndex) {
                drawRect(listX - 2, y - 2, listX + 120, y + 10, 0x8800AAFF);
            }

            fontRendererObj.drawString(name, listX, y, 0xFFFFFF);
        }

        // ✅ RIGHT SIDE — RECIPE PREVIEW
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

                int x = previewX + (i % 3) * 20;
                int y = previewY + (i / 3) * 20;

                itemRender.renderItemIntoGUI(fontRendererObj,
                        mc.getTextureManager(), stack, x, y);
            }

            RenderHelper.disableStandardItemLighting();
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {

        int listX = 20;
        int listY = 30;

        int index = (mouseY - listY) / 12;

        if (mouseX >= listX && mouseX <= listX + 120) {
            if (index >= 0 && index < GunSmithRecipeRegistry.getAll().size()) {
                selectedIndex = index;
            }
        }

        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
