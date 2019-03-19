package com.lulan.shincolle.client.render.block;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.IItemRenderer;

@SideOnly(Side.CLIENT)
public class RenderSmallShipyardItem implements IItemRenderer  {
	
	TileEntitySpecialRenderer tesr;
	private static TileEntity entity;
	
	public RenderSmallShipyardItem(TileEntitySpecialRenderer tesr, TileEntity entity) {
		this.tesr = tesr;
		RenderSmallShipyardItem.entity = entity;	
		RenderSmallShipyardItem.entity.blockMetadata = -2;	//distinguish itemblock(-2)/block(0~7)/non-init state(-1)
	}

	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type) {
		return true;
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
		return true;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
		//type:ENTITY=丟在地上, EQUIPPED=拿在手上, EQUIPPED_FIRST_PERSON=拿在手上第一人稱
		//     INVENTORY=在物品欄中, FIRST_PERSON_MAP=地圖類型物品
		if(type == ItemRenderType.ENTITY) {
			GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
		}
		if(type == ItemRenderType.EQUIPPED_FIRST_PERSON) {
			GL11.glTranslatef(0F, 0.3F, 0F);
		}
		//畫出model
		this.tesr.renderTileEntityAt(RenderSmallShipyardItem.entity, 0D, 0D, 0D, 0F);

	}

}
