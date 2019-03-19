package com.lulan.shincolle.client.render.special;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import com.lulan.shincolle.client.model.ModelAbyssMissile;
import com.lulan.shincolle.entity.other.EntityAbyssMissile;
import com.lulan.shincolle.reference.Reference;


@SideOnly(Side.CLIENT)
public class RenderAbyssMissile extends Render {
    
	//貼圖檔路徑
	private static final ResourceLocation entityTexture = new ResourceLocation(Reference.TEXTURES_ENTITY+"EntityAbyssMissile.png");
	private ModelBase model;
	private float entityScale;	//模型大小

    public RenderAbyssMissile(float scale) {   
    	this.model = new ModelAbyssMissile();
    	this.entityScale = scale;
	}
    
    @Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return entityTexture;
	}

    public void doRender(EntityAbyssMissile entity, double offsetX, double offsetY, double offsetZ, float p_76986_8_, float p_76986_9_) {
    	//bind texture
        this.bindEntityTexture(entity);  		//call getEntityTexture
        
        //render start
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_CULL_FACE);	//保證model全部都畫出來, 不是只畫看得到的面
        
        //model position set to center
        GL11.glTranslatef((float)offsetX, (float)offsetY+0.3F, (float)offsetZ);
        
        //apply model scale
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);	//將scale調整模式設為normal      		
        GL11.glScalef(this.entityScale, this.entityScale, this.entityScale);   //調整model大小

        //parm: entity, f依移動速度, f1依移動速度, f2遞增, f3左右角度, f4上下角度, f5(scale)
        this.model.render(entity, 0F, 0F, 0F, entity.rotationYaw, entity.rotationPitch, 0.0625F);
        
        //render end
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPopMatrix();       
    }

    //傳入entity的都轉成abyssmissile
    @Override
	public void doRender(Entity entity, double offsetX, double offsetY, double offsetZ, float p_76986_8_, float p_76986_9_) {
        this.doRender((EntityAbyssMissile)entity, offsetX, offsetY, offsetZ, p_76986_8_, p_76986_9_);
    }
}