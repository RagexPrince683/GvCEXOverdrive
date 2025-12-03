package handmadeguns.gunsmithing;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Mouse;

import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.List;

public class GunSmithingTableGui extends GuiScreen {

    // === PAGE SYSTEM ===
    private static final int PAGE_GUNS = 0;
    private static final int PAGE_AMMO = 1;
    private int currentPage = PAGE_GUNS;

    private GuiButton gunsTab;
    private GuiButton ammoTab;

    // === AMMO CACHE (client side display only) ===
    private List<GunSmithRecipeRegistry.GunRecipeEntry> ammoRecipes;

    private int selectedIndex = -1;
    private int scrollOffset = 0;
    private static final int LIST_X = 20;
    private static final int LIST_Y = 30;
    private static final int LIST_HEIGHT = 140;
    private static final int ENTRY_HEIGHT = 12;

    // === Scrollbar ===
    private boolean draggingScroll = false;

    private static final int SCROLLBAR_X = 165;
    private static final int SCROLLBAR_Y = LIST_Y;
    private static final int SCROLLBAR_HEIGHT = LIST_HEIGHT;
    private static final int SCROLLBAR_WIDTH = 8;

    private final EntityPlayer player;

    private GuiButton craftButton;

    private GuiTextField searchBox;
    private List<GunSmithRecipeRegistry.GunRecipeEntry> filteredRecipes;


    public GunSmithingTableGui(EntityPlayer player) {
        this.player = player;
        this.currentPage = PAGE_GUNS;
    }

    // ---------- Build / filter recipes ----------

    private void updateSearchResults() {
        String q = (searchBox == null) ? "" : searchBox.getText().toLowerCase();
        filteredRecipes = new ArrayList<GunSmithRecipeRegistry.GunRecipeEntry>();

        List<GunSmithRecipeRegistry.GunRecipeEntry> source;

        if (currentPage == PAGE_GUNS) {
            source = GunSmithRecipeRegistry.getAll();
        } else {
            if (ammoRecipes == null) {
                ammoRecipes = buildAmmoRecipes();
            }
            source = ammoRecipes;
        }

        if (source == null) return;

        for (GunSmithRecipeRegistry.GunRecipeEntry e : source) {
            if (e == null || e.result == null) continue;

            // defensive: get display name safely
            String name;
            try {
                name = e.result.getDisplayName();
            } catch (Throwable t) {
                continue;
            }
            if (name == null) continue;

            if (q.isEmpty() || name.toLowerCase().contains(q)) {
                filteredRecipes.add(e);
            }
        }

        scrollOffset = 0;
        selectedIndex = -1;
    }

    private boolean canCraft(GunSmithRecipeRegistry.GunRecipeEntry entry) {
        if (entry == null || entry.inputs == null) return false;

        for (ItemStack req : entry.inputs) {
            if (req == null) continue;
            int owned = countInInventory(req);
            if (owned < req.stackSize) return false;
        }
        return true;
    }

    private void drawScrollbar(int totalEntries) {
        int maxVisible = LIST_HEIGHT / ENTRY_HEIGHT;
        if (totalEntries <= maxVisible) return;

        int maxScroll = totalEntries - maxVisible;

        drawRect(
                SCROLLBAR_X,
                SCROLLBAR_Y,
                SCROLLBAR_X + SCROLLBAR_WIDTH,
                SCROLLBAR_Y + SCROLLBAR_HEIGHT,
                0xFF333333
        );

        int thumbHeight = Math.max(12,
                (int) ((float) maxVisible / totalEntries * SCROLLBAR_HEIGHT));

        int thumbY;
        if (maxScroll <= 0) {
            thumbY = SCROLLBAR_Y;
        } else {
            thumbY = SCROLLBAR_Y +
                    (int) ((float) scrollOffset / maxScroll * (SCROLLBAR_HEIGHT - thumbHeight));
        }

        drawRect(
                SCROLLBAR_X,
                thumbY,
                SCROLLBAR_X + SCROLLBAR_WIDTH,
                thumbY + thumbHeight,
                0xFFAAAAAA
        );
    }

    // ---------- UI rendering ----------
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        // tabs (draw after background)
        int tabY = height - 40;
        int gunsColor = (currentPage == PAGE_GUNS) ? 0xFFAAAAAA : 0xFF555555;
        int ammoColor = (currentPage == PAGE_AMMO) ? 0xFFAAAAAA : 0xFF555555;

        drawRect(20, tabY, 20 + 60, tabY + 20, gunsColor);
        drawCenteredString(fontRendererObj, "Guns", 20 + 30, tabY + 6, 0x000000);

        drawRect(85, tabY, 85 + 60, tabY + 20, ammoColor);
        drawCenteredString(fontRendererObj, "Ammo", 85 + 30, tabY + 6, 0x000000);

        if (searchBox != null) searchBox.drawTextBox();
        if (filteredRecipes == null) updateSearchResults();

        List<GunSmithRecipeRegistry.GunRecipeEntry> recipes = filteredRecipes;
        if (recipes == null) recipes = new ArrayList<GunSmithRecipeRegistry.GunRecipeEntry>();

        String title = (currentPage == PAGE_GUNS) ? "Gun Smithing Table" : "Ammo Crafting";
        drawCenteredString(fontRendererObj, title, width / 2, 8, 0xFFFFFF);

        int maxVisible = LIST_HEIGHT / ENTRY_HEIGHT;
        int maxScroll = Math.max(0, recipes.size() - maxVisible);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
        int end = Math.min(recipes.size(), scrollOffset + maxVisible);

        // left panel list
        for (int i = scrollOffset; i < end; i++) {
            GunSmithRecipeRegistry.GunRecipeEntry entry = recipes.get(i);
            if (entry == null || entry.result == null) continue;

            String name;
            try {
                name = entry.result.getDisplayName();
            } catch (Throwable t) {
                continue;
            }
            if (name == null) continue;

            int y = LIST_Y + (i - scrollOffset) * ENTRY_HEIGHT;

            if (i == selectedIndex) {
                drawRect(LIST_X - 2, y - 2, LIST_X + 140, y + 10, 0x8800AAFF);
            }

            fontRendererObj.drawString(name, LIST_X, y, 0xFFFFFF);
        }

        drawScrollbar(recipes.size());

        if (selectedIndex < 0 || selectedIndex >= recipes.size()) {
            super.drawScreen(mouseX, mouseY, partialTicks);
            return;
        }

        // right panel
        GunSmithRecipeRegistry.GunRecipeEntry entry = recipes.get(selectedIndex);
        if (entry == null || entry.result == null) {
            super.drawScreen(mouseX, mouseY, partialTicks);
            return;
        }

        int previewX = width / 2 + 20;
        int previewY = 40;

        try {
            drawCenteredString(fontRendererObj, entry.result.getDisplayName(), previewX + 40, 30, 0xFFFFFF);
        } catch (Throwable ignored) {}

        RenderHelper.enableGUIStandardItemLighting();

        if (entry.inputs != null) {
            for (int i = 0; i < entry.inputs.length; i++) {
                ItemStack stack = entry.inputs[i];
                if (stack == null) continue;

                int x = previewX + (i % 3) * 22;
                int y = previewY + (i / 3) * 22;

                try {
                    itemRender.renderItemIntoGUI(fontRendererObj, mc.getTextureManager(), stack, x, y);
                } catch (Throwable ignored) {}

                if (mouseX >= x && mouseX <= x + 16 && mouseY >= y && mouseY <= y + 16) {
                    List list = stack.getTooltip(player, false);
                    for (int t = 0; t < list.size(); t++) {
                        if (t == 0) list.set(t, EnumChatFormatting.WHITE + (String) list.get(t));
                        else list.set(t, EnumChatFormatting.GRAY + (String) list.get(t));
                    }
                    drawHoveringText(list, mouseX, mouseY, fontRendererObj);
                }

                int owned = countInInventory(stack);
                int needed = stack.stackSize;
                boolean missing = owned < needed;

                String txt = owned + "/" + needed;
                int color = missing ? 0xFF5555 : 0x55FF55;
                fontRendererObj.drawString(txt, x, y + 16, color);
            }
        }

        if (craftButton != null) craftButton.enabled = canCraft(entry);

        RenderHelper.disableStandardItemLighting();

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void keyTyped(char c, int key) {
        if (searchBox != null && searchBox.textboxKeyTyped(c, key)) {
            updateSearchResults();
        } else {
            super.keyTyped(c, key);
        }
    }

    @Override
    protected void mouseClicked(int x, int y, int btn) {
        // tab clicks first
        if (y >= height - 40 && y <= height - 20) {
            if (x >= 20 && x <= 20 + 60) {
                if (currentPage != PAGE_GUNS) {
                    currentPage = PAGE_GUNS;
                    updateSearchResults();
                }
                return;
            }
            if (x >= 85 && x <= 85 + 60) {
                if (currentPage != PAGE_AMMO) {
                    currentPage = PAGE_AMMO;
                    updateSearchResults();
                }
                return;
            }
        }

        if (searchBox != null) searchBox.mouseClicked(x, y, btn);

        // scrollbar
        if (x >= SCROLLBAR_X && x <= SCROLLBAR_X + SCROLLBAR_WIDTH
                && y >= SCROLLBAR_Y && y <= SCROLLBAR_Y + SCROLLBAR_HEIGHT) {
            draggingScroll = true;
            return;
        }

        if (filteredRecipes != null) {
            int index = (y - LIST_Y) / ENTRY_HEIGHT + scrollOffset;
            if (x >= LIST_X && x <= LIST_X + 140) {
                if (index >= 0 && index < filteredRecipes.size()) selectedIndex = index;
            }
        }

        super.mouseClicked(x, y, btn);
    }

    @Override
    protected void mouseMovedOrUp(int mouseX, int mouseY, int state) {
        if (state == 0) draggingScroll = false;
        super.mouseMovedOrUp(mouseX, mouseY, state);
    }

    @Override
    protected void mouseClickMove(int x, int y, int btn, long time) {
        if (draggingScroll && filteredRecipes != null) {
            int total = filteredRecipes.size();
            int maxVisible = LIST_HEIGHT / ENTRY_HEIGHT;
            if (total > maxVisible) {
                int maxScroll = total - maxVisible;
                float pct = (float) (y - SCROLLBAR_Y) / SCROLLBAR_HEIGHT;
                pct = Math.max(0F, Math.min(1F, pct));
                scrollOffset = (int) (pct * maxScroll);
            }
        }
        super.mouseClickMove(x, y, btn, time);
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
        int wheel = Mouse.getDWheel();
        if (wheel > 0) scrollOffset--;
        else if (wheel < 0) scrollOffset++;
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button == null) return;

        if (button.id == 1) {
            if (currentPage != PAGE_GUNS) {
                currentPage = PAGE_GUNS;
                updateSearchResults();
            }
            return;
        }
        if (button.id == 2) {
            if (currentPage != PAGE_AMMO) {
                currentPage = PAGE_AMMO;
                updateSearchResults();
            }
            return;
        }

        if (button.id == 0) {
            if (filteredRecipes == null) return;
            if (selectedIndex < 0 || selectedIndex >= filteredRecipes.size()) return;
            GunSmithRecipeRegistry.GunRecipeEntry entry = filteredRecipes.get(selectedIndex);
            if (entry == null) return;
            if (!canCraft(entry)) return;

            if (currentPage == PAGE_GUNS) {
                int realIndex = GunSmithRecipeRegistry.getAll().indexOf(entry);
                if (realIndex >= 0) GunSmithNetwork.sendCraftRequestToServer(realIndex);
            } else {
                // send ammo craft packet (server validates and crafts)
                // send the selectedIndex relative to ammoRecipes (server will rebuild the same list)
                if (ammoRecipes == null) ammoRecipes = buildAmmoRecipes();
                int idx = (ammoRecipes == null) ? -1 : ammoRecipes.indexOf(entry);
                if (idx >= 0) GunSmithNetwork.sendAmmoCraftRequestToServer(idx);
            }
        }
    }

    // client-side fallback removal/give (should not be used for SMP)
    @SuppressWarnings("unused")
    private void craftAmmoClientSide(GunSmithRecipeRegistry.GunRecipeEntry entry) {
        if (entry == null || !canCraft(entry)) return;
        if (entry.inputs != null) {
            for (ItemStack req : entry.inputs) {
                if (req == null) continue;
                int remaining = req.stackSize;
                for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
                    ItemStack slot = player.inventory.getStackInSlot(i);
                    if (slot == null) continue;
                    if (slot.getItem() == req.getItem() && slot.getItemDamage() == req.getItemDamage()) {
                        int toRemove = Math.min(remaining, slot.stackSize);
                        slot.stackSize -= toRemove;
                        remaining -= toRemove;
                        if (slot.stackSize <= 0) player.inventory.setInventorySlotContents(i, null);
                        if (remaining <= 0) break;
                    }
                }
            }
        }
        if (entry.result != null) player.inventory.addItemStackToInventory(entry.result.copy());
    }

    // ---------- Build ammo recipes (from HMG registry) ----------
    private List<GunSmithRecipeRegistry.GunRecipeEntry> buildAmmoRecipes() {
        // Return a defensive copy from the registry
        List<GunSmithRecipeRegistry.GunRecipeEntry> list = GunSmithRecipeRegistry.getAmmoRecipes();
        return list == null ? new java.util.ArrayList<GunSmithRecipeRegistry.GunRecipeEntry>() : list;
    }

    private int countInInventory(ItemStack target) {
        if (target == null || player == null) return 0;
        int count = 0;
        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            ItemStack inv = player.inventory.getStackInSlot(i);
            if (inv != null && inv.getItem() == target.getItem() && inv.getItemDamage() == target.getItemDamage()) {
                count += inv.stackSize;
            }
        }
        return count;
    }

    @Override
    public void initGui() {
        super.initGui();

        int btnX = width / 2 + 20;
        int btnY = height - 40;

        craftButton = new GuiButton(0, btnX, btnY, 80, 20, "Create");
        gunsTab = new GuiButton(1, 20, height - 40, 60, 20, "Guns");
        ammoTab = new GuiButton(2, 85, height - 40, 60, 20, "Ammo");

        searchBox = new GuiTextField(fontRendererObj, 20, 15, 140, 12);
        searchBox.setMaxStringLength(32);
        searchBox.setFocused(false);

        buttonList.clear();
        buttonList.add(craftButton);
        buttonList.add(gunsTab);
        buttonList.add(ammoTab);

        updateSearchResults();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
