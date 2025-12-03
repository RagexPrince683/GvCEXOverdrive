package handmadeguns.gunsmithing;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Mouse;

import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.EnumChatFormatting;

import java.util.List;

public class GunSmithingTableGui extends GuiScreen {

    // === PAGE SYSTEM ===
    private static final int PAGE_GUNS = 0;
    private static final int PAGE_AMMO = 1;
    private int currentPage = PAGE_GUNS;

    private GuiButton gunsTab;
    private GuiButton ammoTab;

    // === AMMO CACHE ===
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

    private EntityPlayer player;

    private GuiButton craftButton;

    private GuiTextField searchBox;
    private List<GunSmithRecipeRegistry.GunRecipeEntry> filteredRecipes;


    public GunSmithingTableGui(EntityPlayer player) {
        this.player = player;
    }

    private void updateSearchResults() {
        String q = searchBox.getText().toLowerCase();
        filteredRecipes = new java.util.ArrayList<GunSmithRecipeRegistry.GunRecipeEntry>();

        List<GunSmithRecipeRegistry.GunRecipeEntry> source;

        if (currentPage == PAGE_GUNS) {
            source = GunSmithRecipeRegistry.getAll();
        } else {
            if (ammoRecipes == null) {
                ammoRecipes = buildAmmoRecipes();
            }
            source = ammoRecipes;
        }

        for (GunSmithRecipeRegistry.GunRecipeEntry e : source) {
            if (e.result.getDisplayName().toLowerCase().contains(q)) {
                filteredRecipes.add(e);
            }
        }

        scrollOffset = 0;
        selectedIndex = -1;
    }



    private boolean canCraft(GunSmithRecipeRegistry.GunRecipeEntry entry) {
        if (entry == null) return false;

        for (ItemStack req : entry.inputs) {
            if (req == null) continue;

            int owned = countInInventory(req);
            if (owned < req.stackSize)
                return false;
        }
        return true;
    }

    private void drawScrollbar(int totalEntries) {
        int maxVisible = LIST_HEIGHT / ENTRY_HEIGHT;
        if (totalEntries <= maxVisible) return;

        int maxScroll = totalEntries - maxVisible;

        // Scrollbar background
        drawRect(
                SCROLLBAR_X,
                SCROLLBAR_Y,
                SCROLLBAR_X + SCROLLBAR_WIDTH,
                SCROLLBAR_Y + SCROLLBAR_HEIGHT,
                0xFF333333
        );

        // Thumb size
        int thumbHeight = Math.max(12,
                (int) ((float) maxVisible / totalEntries * SCROLLBAR_HEIGHT));

        int thumbY = SCROLLBAR_Y +
                (int) ((float) scrollOffset / maxScroll * (SCROLLBAR_HEIGHT - thumbHeight));

        // Thumb
        drawRect(
                SCROLLBAR_X,
                thumbY,
                SCROLLBAR_X + SCROLLBAR_WIDTH,
                thumbY + thumbHeight,
                0xFFAAAAAA
        );
    }


    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        // Draw tabs
        int tabY = height - 40;
        int gunsColor = (currentPage == PAGE_GUNS) ? 0xFFAAAAAA : 0xFF555555;
        int ammoColor = (currentPage == PAGE_AMMO) ? 0xFFAAAAAA : 0xFF555555;

        drawRect(20, tabY, 20 + 60, tabY + 20, gunsColor);
        drawCenteredString(fontRendererObj, "Guns", 20 + 30, tabY + 6, 0x000000);

        drawRect(85, tabY, 85 + 60, tabY + 20, ammoColor);
        drawCenteredString(fontRendererObj, "Ammo", 85 + 30, tabY + 6, 0x000000);


        drawDefaultBackground();

        searchBox.drawTextBox();


        //List<GunSmithRecipeRegistry.GunRecipeEntry> recipes =
        //        GunSmithRecipeRegistry.getAll();
        List<GunSmithRecipeRegistry.GunRecipeEntry> recipes = filteredRecipes;


        String title = (currentPage == PAGE_GUNS) ? "Gun Smithing Table" : "Ammo Crafting";
        drawCenteredString(fontRendererObj, title, width / 2, 8, 0xFFFFFF);


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

        drawScrollbar(recipes.size());


        if (selectedIndex < 0 || selectedIndex >= recipes.size()) return;


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

                // ✅ HOVER TOOLTIP
                if (mouseX >= x && mouseX <= x + 16 && mouseY >= y && mouseY <= y + 16) {
                    java.util.List list = stack.getTooltip(player, false);
                    for (int t = 0; t < list.size(); t++) {
                        if (t == 0)
                            list.set(t, EnumChatFormatting.WHITE + (String) list.get(t));
                        else
                            list.set(t, EnumChatFormatting.GRAY + (String) list.get(t));
                    }
                    drawHoveringText(list, mouseX, mouseY, fontRendererObj);
                }

                int owned = countInInventory(stack);
                int needed = stack.stackSize;
                boolean missing = owned < needed;

                String txt = owned + "/" + needed;
                int color = missing ? 0xFF5555 : 0x55FF55; // ❌ red / ✅ green

                fontRendererObj.drawString(txt, x, y + 16, color);
            }

            boolean canCraft = canCraft(entry);
            craftButton.enabled = canCraft;

            RenderHelper.disableStandardItemLighting();
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void keyTyped(char c, int key) {
        if (searchBox.textboxKeyTyped(c, key)) {
            updateSearchResults();
        } else {
            super.keyTyped(c, key);
        }
    }

    @Override
    protected void mouseClicked(int x, int y, int btn) {
        searchBox.mouseClicked(x, y, btn);

        // ✅ SCROLLBAR CLICK
        if (x >= SCROLLBAR_X && x <= SCROLLBAR_X + SCROLLBAR_WIDTH &&
                y >= SCROLLBAR_Y && y <= SCROLLBAR_Y + SCROLLBAR_HEIGHT) {

            draggingScroll = true;
            return;
        }

        int index = (y - LIST_Y) / ENTRY_HEIGHT + scrollOffset;
        // Tab click detection
        if (y >= height - 40 && y <= height - 20) {
            if (x >= 20 && x <= 20 + 60) { // Guns tab
                currentPage = PAGE_GUNS;
                updateSearchResults();
                return;
            }
            if (x >= 85 && x <= 85 + 60) { // Ammo tab
                currentPage = PAGE_AMMO;
                updateSearchResults();
                return;
            }
        }


        super.mouseClicked(x, y, btn);
    }

    @Override
    protected void mouseMovedOrUp(int mouseX, int mouseY, int state) {
        if (state == 0) { // Left mouse released
            draggingScroll = false;
        }

        super.mouseMovedOrUp(mouseX, mouseY, state);
    }

    @Override
    protected void mouseClickMove(int x, int y, int btn, long time) {
        if (draggingScroll) {
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

    @Override
    protected void actionPerformed(GuiButton button) {

        // ✅ TAB SWITCHING
        if (button.id == 1) { // Guns
            currentPage = PAGE_GUNS;
            updateSearchResults();
            return;
        }

        if (button.id == 2) { // Ammo
            currentPage = PAGE_AMMO;
            updateSearchResults();
            return;
        }

        // ✅ CRAFT BUTTON
        if (button.id == 0) {
            if (selectedIndex < 0 || selectedIndex >= filteredRecipes.size()) return;

            GunSmithRecipeRegistry.GunRecipeEntry entry = filteredRecipes.get(selectedIndex);
            if (!canCraft(entry)) return;

            if (currentPage == PAGE_GUNS) {
                int realIndex = GunSmithRecipeRegistry.getAll().indexOf(entry);
                if (realIndex >= 0) {
                    GunSmithNetwork.sendCraftRequestToServer(realIndex);
                }
            } else if (currentPage == PAGE_AMMO) {
                craftAmmo(entry);
            }
        }

    }

    private void craftAmmo(GunSmithRecipeRegistry.GunRecipeEntry entry) {
        if (entry == null || !canCraft(entry)) return;

        // Remove required items from player inventory
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

        // Add result to player inventory
        player.inventory.addItemStackToInventory(entry.result.copy());
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
    public void initGui() {
        super.initGui();

        int btnX = width / 2 + 20;
        int btnY = height - 40;

        craftButton = new GuiButton(0, btnX, btnY, 80, 20, "Create");

        gunsTab = new GuiButton(1, 20, height - 40, 60, 20, "Guns");
        ammoTab = new GuiButton(2, 85, height - 40, 60, 20, "Ammo");

        searchBox = new GuiTextField(
                fontRendererObj,
                20,
                15,
                140,
                12
        );

        searchBox.setMaxStringLength(32);
        searchBox.setFocused(false);

        buttonList.clear();
        buttonList.add(craftButton);
        buttonList.add(gunsTab);
        buttonList.add(ammoTab);

        updateSearchResults();
    }


    private List<GunSmithRecipeRegistry.GunRecipeEntry> buildAmmoRecipes() {

        List<GunSmithRecipeRegistry.GunRecipeEntry> out =
                new java.util.ArrayList<GunSmithRecipeRegistry.GunRecipeEntry>();

        List list = net.minecraft.item.crafting.CraftingManager
                .getInstance()
                .getRecipeList();

        for (Object obj : list) {

            if (!(obj instanceof net.minecraft.item.crafting.ShapedRecipes))
                continue;

            net.minecraft.item.crafting.ShapedRecipes r =
                    (net.minecraft.item.crafting.ShapedRecipes) obj;

            ItemStack result = r.getRecipeOutput();
            if (result == null)
                continue;

            out.add(new GunSmithRecipeRegistry.GunRecipeEntry(
                    result,
                    r.recipeItems
            ));
        }

        return out;
    }


    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
