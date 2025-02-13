package handmadeguns.gui;

import handmadeguns.inventory.ContainerHolder;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class HMGGuiInventoryItemMountnew extends GuiContainer
{
    private static final ResourceLocation field_147017_u = new ResourceLocation("textures/gui/container/generic_54.png");
    private IInventory upperChestInventory;
    private IInventory lowerChestInventory;
    /** window height is calculated with these values; the more rows, the heigher */
    private int inventoryRows;
    private int inventoryamari;
    private static final String __OBFID = "CL_00000749";

    public HMGGuiInventoryItemMountnew(IInventory p_i1083_1_, IInventory p_i1083_2_)
    {
        super(new ContainerHolder(p_i1083_1_, p_i1083_2_));
        this.upperChestInventory = p_i1083_1_;
        this.lowerChestInventory = p_i1083_2_;
        this.allowUserInput = false;
        short short1 = 222;
        int i = short1 - 108;
        this.inventoryRows = p_i1083_2_.getSizeInventory() / 9;
        inventoryamari = p_i1083_2_.getSizeInventory()%9;
        this.ySize = i + (this.inventoryRows+ (inventoryamari != 0?1:0)) * 18;
    }

    /**
     * Draw the foreground layer for the GuiContainer (everything in front of the items)
     */
    protected void drawGuiContainerForegroundLayer(int p_146979_1_, int p_146979_2_)
    {
        this.fontRendererObj.drawString(this.lowerChestInventory.hasCustomInventoryName() ? this.lowerChestInventory.getInventoryName() : I18n.format(this.lowerChestInventory.getInventoryName(), new Object[0]), 8, 6, 4210752);
        this.fontRendererObj.drawString(this.upperChestInventory.hasCustomInventoryName() ? this.upperChestInventory.getInventoryName() : I18n.format(this.upperChestInventory.getInventoryName(), new Object[0]), 8, this.ySize - 96 + 2, 4210752);
    }

    protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_)
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(field_147017_u);
        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(k, l, 0, 0, this.xSize, this.inventoryRows * 18 + 17);
        if(inventoryamari != 0){
            this.drawTexturedModalRect(k, l + this.inventoryRows * 18 + 17, 0, 3, this.xSize, this.inventoryRows * 18 + 30);
            this.drawTexturedModalRect(k, l + this.inventoryRows * 18 + 31, 0, 3, this.xSize, this.inventoryRows * 18 + 35);
            this.drawTexturedModalRect(k, l + this.inventoryRows * 18 + 17, 0, 17, 7 + inventoryamari * 18, this.inventoryRows * 18 + 35);
        }
        this.drawTexturedModalRect(k, l + (this.inventoryRows+ (inventoryamari != 0?1:0)) * 18  + 17, 0, 126, this.xSize, 96);
    }
}