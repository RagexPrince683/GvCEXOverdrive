package handmadeguns.gunsmithing;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import handmadeguns.client.render.HMGRenderItemGun_U;
import handmadeguns.client.render.HMGRenderItemGun_U_NEW;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;
import org.lwjgl.input.Mouse;

import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class GunSmithingTableGui extends GuiContainer {
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
    private static final int LIST_X = 20;       // offset inside GUI
    private static final int LIST_Y = 30;
    private static final int LIST_HEIGHT = 140;
    private static final int ENTRY_HEIGHT = 12;

    // === Scrollbar ===
    private boolean draggingScroll = false;

    private static final int SCROLLBAR_X = 165; // offset inside GUI
    private static final int SCROLLBAR_Y = LIST_Y;
    private static final int SCROLLBAR_HEIGHT = LIST_HEIGHT;
    private static final int SCROLLBAR_WIDTH = 8;

    private final EntityPlayer player;

    private GuiButton craftButton;

    private GuiTextField searchBox;
    private List<GunSmithRecipeRegistry.GunRecipeEntry> filteredRecipes;

    // choose GUI size large enough for list + preview
    public GunSmithingTableGui(EntityPlayer player) {
        super(new ContainerGunSmith(player.inventory)); // adjust if your container constructor differs
        this.player = player;
        this.currentPage = PAGE_GUNS;

        // set a GUI size that fits your layout (adjust if needed)
        this.xSize = 330;
        this.ySize = 220;
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

        // draw track
        drawRect(
                this.guiLeft + SCROLLBAR_X,
                this.guiTop + SCROLLBAR_Y,
                this.guiLeft + SCROLLBAR_X + SCROLLBAR_WIDTH,
                this.guiTop + SCROLLBAR_Y + SCROLLBAR_HEIGHT,
                0xFF333333
        );

        int thumbHeight = Math.max(12,
                (int) ((float) maxVisible / totalEntries * SCROLLBAR_HEIGHT));

        int thumbY;
        if (maxScroll <= 0) {
            thumbY = this.guiTop + SCROLLBAR_Y;
        } else {
            thumbY = this.guiTop + SCROLLBAR_Y +
                    (int) ((float) scrollOffset / maxScroll * (SCROLLBAR_HEIGHT - thumbHeight));
        }

        // draw thumb
        drawRect(
                this.guiLeft + SCROLLBAR_X,
                thumbY,
                this.guiLeft + SCROLLBAR_X + SCROLLBAR_WIDTH,
                thumbY + thumbHeight,
                0xFFAAAAAA
        );
    }

    // ---------- UI rendering ----------
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // draw background and container (slots, background) first
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);

        // then draw our overlay UI on top, using guiLeft/guiTop as origin
        if (searchBox != null) searchBox.drawTextBox();
        if (filteredRecipes == null) updateSearchResults();

        List<GunSmithRecipeRegistry.GunRecipeEntry> recipes = filteredRecipes;
        if (recipes == null) recipes = new ArrayList<GunSmithRecipeRegistry.GunRecipeEntry>();

        String title = (currentPage == PAGE_GUNS) ? "Gun Smithing Table" : "Ammo Crafting";
        drawCenteredString(fontRendererObj, title, this.guiLeft + this.xSize / 2, this.guiTop + 8, 0xFFFFFF);

        // tabs (draw after header)
        int tabY = this.guiTop + this.ySize - 40;
        int gunsColor = (currentPage == PAGE_GUNS) ? 0xFFAAAAAA : 0xFF555555;
        int ammoColor = (currentPage == PAGE_AMMO) ? 0xFFAAAAAA : 0xFF555555;

        drawRect(this.guiLeft + 20, tabY, this.guiLeft + 20 + 60, tabY + 20, gunsColor);
        drawCenteredString(fontRendererObj, "Guns", this.guiLeft + 20 + 30, tabY + 6, 0x000000);

        drawRect(this.guiLeft + 85, tabY, this.guiLeft + 85 + 60, tabY + 20, ammoColor);
        drawCenteredString(fontRendererObj, "Ammo", this.guiLeft + 85 + 30, tabY + 6, 0x000000);

        // left list
        int maxVisible = LIST_HEIGHT / ENTRY_HEIGHT;
        int maxScroll = Math.max(0, recipes.size() - maxVisible);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
        int end = Math.min(recipes.size(), scrollOffset + maxVisible);

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

            int y = this.guiTop + LIST_Y + (i - scrollOffset) * ENTRY_HEIGHT;

            if (i == selectedIndex) {
                drawRect(this.guiLeft + LIST_X - 2, y - 2, this.guiLeft + LIST_X + 140, y + 10, 0x8800AAFF);
            }

            fontRendererObj.drawString(name, this.guiLeft + LIST_X, y, 0xFFFFFF);
        }

        drawScrollbar(recipes.size());

        if (selectedIndex < 0 || selectedIndex >= recipes.size()) {
            return; // nothing selected — we've already drawn container and list
        }

        // right panel preview
        GunSmithRecipeRegistry.GunRecipeEntry entry = recipes.get(selectedIndex);
        if (entry == null || entry.result == null) {
            return;
        }

        int previewX = this.guiLeft + this.xSize / 2 + 20;
        int previewY = this.guiTop + 40;

        try {
            drawCenteredString(fontRendererObj, entry.result.getDisplayName(), previewX + 40, this.guiTop + 30, 0xFFFFFF);
        } catch (Throwable ignored) {}

        // --- draw 2D input icons (unchanged) ---
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

                // tooltip
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

        // --- 3D gun preview (ONLY if this item has the gun renderer) ---
        try {
            // copy so we don't mutate the original recipe ItemStack
            ItemStack previewStack = entry.result.copy();

            // ensure NBT exists (some renderers expect it)
            if (previewStack.getTagCompound() == null) {
                previewStack.setTagCompound(new net.minecraft.nbt.NBTTagCompound());
            }

            // find the EQUIPPED renderer for this stack
            IItemRenderer gunRenderer = MinecraftForgeClient.getItemRenderer(previewStack, IItemRenderer.ItemRenderType.EQUIPPED);

            // only render if it's one of our custom gun renderers (so "only for guns")
            if (gunRenderer instanceof HMGRenderItemGun_U || gunRenderer instanceof HMGRenderItemGun_U_NEW) {
                // calculate the center for the model inside the preview area
                int modelCenterX = previewX + 40; // same x used for display name centering
                int modelCenterY = previewY + 80; // tune vertically to taste

                float modelScale = 40.0F; // tune scale to taste (smaller than full GUI preview)

                GL11.glPushMatrix();
                GL11.glEnable(GL11.GL_COLOR_MATERIAL);

                // translate to screen coords (drawScreen uses screen-space coordinates)
                GL11.glTranslatef((float) modelCenterX, (float) modelCenterY, 50.0F);

                // apply scale/flip to match your other renders (keeps orientation you used earlier)
                GL11.glScalef(-modelScale, modelScale, modelScale);

                // keep your rotations exactly as before
                GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
                GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
                GL11.glRotatef(135.0F, 0.0F, 1.0F, 0.0F);
                RenderHelper.enableStandardItemLighting();
                GL11.glRotatef(-135.0F, 0.0F, 1.0F, 0.0F);

                // stabilize and render
                float prevViewY = net.minecraft.client.renderer.entity.RenderManager.instance.playerViewY;
                net.minecraft.client.renderer.entity.RenderManager.instance.playerViewY = 180.0F;

                GL11.glEnable(GL11.GL_DEPTH_TEST);
                GL11.glDepthFunc(GL11.GL_LEQUAL);
                GL11.glDepthMask(true);
                GL11.glEnable(GL11.GL_CULL_FACE);

                GL11.glEnable(GL12.GL_RESCALE_NORMAL);
                gunRenderer.renderItem(IItemRenderer.ItemRenderType.ENTITY, previewStack);
                GL11.glDisable(GL12.GL_RESCALE_NORMAL);

                // restore
                net.minecraft.client.renderer.entity.RenderManager.instance.playerViewY = prevViewY;
                GL11.glDisable(GL11.GL_DEPTH_TEST);

                GL11.glPopMatrix();
                RenderHelper.disableStandardItemLighting();
            }
        } catch (Throwable t) {
            // rendering failed — do not break the GUI. Log for debug.
            System.out.println("[GunSmith] 3D preview render failed: " + t.getMessage());
            t.printStackTrace();
        }

        if (craftButton != null) craftButton.enabled = canCraft(entry);

        RenderHelper.disableStandardItemLighting();
    }


    @Override
    protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
        // draw background color
        drawRect(this.guiLeft, this.guiTop, this.guiLeft + this.xSize, this.guiTop + this.ySize, 0xFF222222);
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
        // translate tab hitboxes and list hitboxes to screen coords using guiLeft/guiTop
        if (y >= this.guiTop + this.ySize - 40 && y <= this.guiTop + this.ySize - 20) {
            if (x >= this.guiLeft + 20 && x <= this.guiLeft + 20 + 60) {
                if (currentPage != PAGE_GUNS) {
                    currentPage = PAGE_GUNS;
                    updateSearchResults();
                }
                return;
            }
            if (x >= this.guiLeft + 85 && x <= this.guiLeft + 85 + 60) {
                if (currentPage != PAGE_AMMO) {
                    currentPage = PAGE_AMMO;
                    updateSearchResults();
                }
                return;
            }
        }

        if (searchBox != null) searchBox.mouseClicked(x, y, btn);

        // scrollbar
        if (x >= this.guiLeft + SCROLLBAR_X && x <= this.guiLeft + SCROLLBAR_X + SCROLLBAR_WIDTH
                && y >= this.guiTop + SCROLLBAR_Y && y <= this.guiTop + SCROLLBAR_Y + SCROLLBAR_HEIGHT) {
            draggingScroll = true;
            return;
        }

        if (filteredRecipes != null) {
            int index = (y - (this.guiTop + LIST_Y)) / ENTRY_HEIGHT + scrollOffset;
            if (x >= this.guiLeft + LIST_X && x <= this.guiLeft + LIST_X + 140) {
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
                float pct = (float) (y - (this.guiTop + SCROLLBAR_Y)) / SCROLLBAR_HEIGHT;
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

        if (button.id == 1) { // guns tab
            if (currentPage != PAGE_GUNS) {
                currentPage = PAGE_GUNS;
                updateSearchResults();
            }
            return;
        }
        if (button.id == 2) { // ammo tab
            if (currentPage != PAGE_AMMO) {
                currentPage = PAGE_AMMO;
                updateSearchResults();
            }
            return;
        }

        if (button.id == 0) { // craft
            if (filteredRecipes == null) return;
            if (selectedIndex < 0 || selectedIndex >= filteredRecipes.size()) return;
            GunSmithRecipeRegistry.GunRecipeEntry entry = filteredRecipes.get(selectedIndex);
            if (entry == null) return;
            if (!canCraft(entry)) return;

            if (currentPage == PAGE_GUNS) {
                int realIndex = GunSmithRecipeRegistry.getAll().indexOf(entry);
                if (realIndex >= 0) GunSmithNetwork.sendCraftRequestToServer(realIndex);
                return;
            }

            // ========== AMMO PATH ==========
            if (ammoRecipes == null) ammoRecipes = buildAmmoRecipes();
            if (ammoRecipes == null || ammoRecipes.isEmpty()) {
                player.addChatMessage(new net.minecraft.util.ChatComponentText(
                        EnumChatFormatting.RED + "No ammo recipes available."));
                return;
            }

            // 1) try identity lookup (fast)
            int idx = -1;
            for (int i = 0; i < ammoRecipes.size(); i++) {
                if (ammoRecipes.get(i) == entry) {
                    idx = i;
                    break;
                }
            }

            // 2) strict match by result+inputs
            if (idx < 0) {
                for (int i = 0; i < ammoRecipes.size(); i++) {
                    GunSmithRecipeRegistry.GunRecipeEntry cand = ammoRecipes.get(i);
                    if (cand == null) continue;
                    if (entriesMatch(entry, cand)) {
                        idx = i;
                        break;
                    }
                }
            }

            // 3) match by result item+meta only
            if (idx < 0) {
                ItemStack target = entry.result;
                if (target != null) {
                    for (int i = 0; i < ammoRecipes.size(); i++) {
                        GunSmithRecipeRegistry.GunRecipeEntry cand = ammoRecipes.get(i);
                        if (cand == null || cand.result == null) continue;
                        if (cand.result.getItem() == target.getItem()
                                && cand.result.getItemDamage() == target.getItemDamage()) {
                            idx = i;
                            break;
                        }
                    }
                }
            }

            if (idx >= 0) {
                GunSmithNetwork.sendAmmoCraftRequestToServer(idx);
            } else {
                player.addChatMessage(new net.minecraft.util.ChatComponentText(
                        EnumChatFormatting.RED + "Failed to locate ammo recipe to craft."));
                System.out.println("[GunSmith] actionPerformed: failed to find ammo index for selected recipe: " +
                        (entry.result == null ? "null" : entry.result.getDisplayName()));
            }
        }
    }

    // ---------- Build ammo recipes safely (client) ----------
    private List<GunSmithRecipeRegistry.GunRecipeEntry> buildAmmoRecipes() {
        List<GunSmithRecipeRegistry.GunRecipeEntry> list = GunSmithRecipeRegistry.getCombinedAmmoRecipes();
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

    private boolean entriesMatch(GunSmithRecipeRegistry.GunRecipeEntry a, GunSmithRecipeRegistry.GunRecipeEntry b) {
        if (a == null || b == null) return false;
        if (a.result == null || b.result == null) return false;

        try {
            if (a.result.getItem() != b.result.getItem()) return false;
            if (a.result.getItemDamage() != b.result.getItemDamage()) return false;
            if (a.result.stackSize != b.result.stackSize) return false;
        } catch (Throwable ignored) {
            return false;
        }

        ItemStack[] inA = a.inputs == null ? new ItemStack[0] : a.inputs;
        ItemStack[] inB = b.inputs == null ? new ItemStack[0] : b.inputs;

        if (inA.length != inB.length) return false;

        for (int i = 0; i < inA.length; i++) {
            ItemStack sa = inA[i];
            ItemStack sb = inB[i];
            if (sa == null && sb == null) continue;
            if (sa == null || sb == null) return false;
            if (sa.getItem() != sb.getItem()) return false;
            if (sa.getItemDamage() != sb.getItemDamage()) return false;
            if (sa.stackSize != sb.stackSize) return false;
        }

        return true;
    }

    @Override
    public void initGui() {
        super.initGui();

        // create controls positioned relative to guiLeft/guiTop
        int btnX = this.guiLeft + this.xSize / 2 + 20;
        int btnY = this.guiTop + this.ySize - 40;

        craftButton = new GuiButton(0, btnX, btnY, 80, 20, "Create");
        gunsTab = new GuiButton(1, this.guiLeft + 20, this.guiTop + this.ySize - 40, 60, 20, "Guns");
        ammoTab = new GuiButton(2, this.guiLeft + 85, this.guiTop + this.ySize - 40, 60, 20, "Ammo");

        searchBox = new GuiTextField(fontRendererObj, this.guiLeft + 20, this.guiTop + 15, 140, 12);
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
