package handmadeguns.gui;
 
import handmadeguns.client.render.HMGRenderItemGun_U;
import handmadeguns.client.render.HMGRenderItemGun_U_NEW;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import static net.minecraft.util.MathHelper.wrapAngleTo180_float;

public class HMGGuiInventoryItem extends GuiContainer
{
    static int test;
    //private static final ResourceLocation texture = new ResourceLocation("textures/gui/container/generic_54.png");
    private static final ResourceLocation texture = new ResourceLocation("handmadeguns:textures/gui/AR.png");
 
    public HMGGuiInventoryItem(InventoryPlayer inventoryPlayer, ItemStack itemstack)
    {
        super(new HMGContainerInventoryItem(inventoryPlayer, itemstack));
        this.ySize = 222;
    }

	/*
        ChestとかInventoryとか文字を描画する
     */
    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        // text (unchanged)
        this.fontRendererObj.drawString("Attachments", 8, 6, 4210752);
        this.fontRendererObj.drawString("Inventory", 8, this.ySize - 96 + 2, 4210752);
        this.fontRendererObj.drawString("Sight/Support/Muzzle/Under/SP bullets", 8, 24, 4210752);

        int centerX = this.guiLeft + this.xSize / 2;
        int centerY = this.guiTop + this.ySize / 2 + 30; // small vertical offset if desired
        int scale = 60; // same idea as before

        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
        GL11.glPushMatrix();

        // translate to the point we want and scale *after* translating like vanilla does
        GL11.glTranslatef((float)centerX, (float)centerY, 50.0F);
        GL11.glScalef((float)(-scale), (float)scale, (float)scale);

        // orientation same as vanilla drawEntityOnScreen
        GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
        GL11.glRotatef(135.0F, 0.0F, 1.0F, 0.0F);

        RenderHelper.enableStandardItemLighting();
        GL11.glRotatef(-135.0F, 0.0F, 1.0F, 0.0F);

        // temporarily force view to stable angle and keep a copy to restore
        float prevViewY = RenderManager.instance.playerViewY;
        RenderManager.instance.playerViewY = 180.0F;

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_CULL_FACE);

        ItemStack currentItem = ((HMGContainerInventoryItem)inventorySlots).inventory.currentItem;
        if (currentItem != null && currentItem.getTagCompound() != null)
        {
            // rebuild attachment NBT
            NBTTagList tagList = new NBTTagList();
            for (int i = 0; i < ((HMGContainerInventoryItem)inventorySlots).inventory.items.length; i++)
            {
                ItemStack stack = ((HMGContainerInventoryItem)inventorySlots).inventory.items[i];
                if (stack != null)
                {
                    NBTTagCompound compound = new NBTTagCompound();
                    compound.setByte("Slot", (byte)i);
                    stack.writeToNBT(compound);
                    tagList.appendTag(compound);
                }
            }
            currentItem.getTagCompound().setTag("Items", tagList);

            GL11.glEnable(GL12.GL_RESCALE_NORMAL);

            IItemRenderer gunrender = MinecraftForgeClient.getItemRenderer(
                    currentItem, IItemRenderer.ItemRenderType.EQUIPPED);

            if (gunrender instanceof HMGRenderItemGun_U_NEW ||
                    gunrender instanceof HMGRenderItemGun_U)
            {
                // render at origin (we already translated/scaled to where we want)
                gunrender.renderItem(IItemRenderer.ItemRenderType.ENTITY, currentItem);
            }

            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        }

        // restore
        RenderManager.instance.playerViewY = prevViewY;
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        GL11.glPopMatrix();
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);

        OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }


    /*
        背景の描画
     */
    @Override
    protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_)
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(texture);
        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(k, l, 0, 0, this.xSize, this.ySize);
    }
}