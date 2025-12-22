package handmadeguns.client.render;

import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.model.IModelCustom;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import net.minecraft.util.MathHelper;


import java.nio.FloatBuffer;

import static handmadeguns.HandmadeGunsCore.cfgRender_useStencil;
import static handmadeguns.client.render.PartsRender.FBO;
import static java.lang.Math.abs;
import static org.lwjgl.opengl.GL11.*;

public class HMGRenderItemCustom extends RenderItem implements IItemRenderer {
	private IModelCustom modeling;
	private ResourceLocation texture;
	public static float smoothing;
	public NBTTagCompound nbt;

	public HMGRenderItemCustom(IModelCustom modelgun, ResourceLocation texture) {
		modeling = modelgun;
		this.texture = texture;
	}

	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type) {

		switch (type) {
			// case 1: //entity third person
			case INVENTORY:
			case FIRST_PERSON_MAP:
				return false;
			case EQUIPPED_FIRST_PERSON:
			case ENTITY:
			case EQUIPPED:
				return true;
		}
		return false;
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
		switch (type) {
			// case 1:
			case INVENTORY:
			case FIRST_PERSON_MAP:
				return false;
			case EQUIPPED_FIRST_PERSON:
			case ENTITY:
			case EQUIPPED:
				return true;
		}
		return false;
	}
	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
		GL11.glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		GL11.glColor4f(1, 1, 1, 1F);
		nbt = item.getTagCompound();
		switch (type) {
			case INVENTORY:
				break;
			case EQUIPPED_FIRST_PERSON:
			{
				Minecraft mc = Minecraft.getMinecraft();

				// Player-configured FOV (vanilla default is 95)
				float playerFov = mc.gameSettings.fovSetting;

				// Authored at vanilla 95 FOV
				float fovScale = playerFov / 95.0f;

				// Clamp to avoid insanity
				fovScale = Math.max(0.7F, Math.min(1.3F, fovScale));

				GL11.glPushMatrix();

				// FOV compensation (ONE LINE FIX)
				GL11.glScalef(fovScale, fovScale, fovScale);

				GL11.glRotatef(180F, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(50F, 0.0F, 1.0F, 0.0F);
				GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);

				mc.renderEngine.bindTexture(texture);
				modeling.renderAll();

				GL11.glPopMatrix();
				break;
			}
			case EQUIPPED://thrid
				GL11.glPushMatrix();
				GL11.glRotatef(180F, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(50F, 0.0F, 1.0F, 0.0F);
				GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
				Minecraft.getMinecraft().renderEngine.bindTexture(texture);
				modeling.renderAll();
				GL11.glPopMatrix();//glend1
				break;

			case FIRST_PERSON_MAP:
				break;
		}

		GL11.glDepthMask(true);
		GL11.glDisable(GL_BLEND);
	}

	public void renderaspart() {
		renderaspart(0);
		renderaspart(1);
	}
	public void renderaspart(int pass) {
		glEnable(GL_BLEND);
		if(pass == 1) {
			glEnable(GL_BLEND);
			glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
			GL11.glDepthMask(false);
			glAlphaFunc(GL_LEQUAL, 1);
		}else {
			GL11.glDepthMask(true);
			glAlphaFunc(GL_EQUAL, 1);
		}
		GL11.glColor4f(1, 1, 1, 1F);
		Minecraft.getMinecraft().renderEngine.bindTexture(texture);
		GL11.glScalef(1, 1, 1);
		modeling.renderAllExcept("light");


		RenderHelper.disableStandardItemLighting();
		float lastBrightnessX = OpenGlHelper.lastBrightnessX;
		float lastBrightnessY = OpenGlHelper.lastBrightnessY;
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
		modeling.renderPart("light");
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)lastBrightnessX, (float)lastBrightnessY);
		RenderHelper.enableStandardItemLighting();


		if(cfgRender_useStencil && pass==1){
			//INSERT : �t���[���o�b�t�@�ɕ`��J�n
			//       : �ی���Matrix����w�[��
			//       : �e�̃e�N�X�`������bind
//			FBO.start();
//			GL11.glPushMatrix();
			FMLClientHandler.instance().getClient().getTextureManager().bindTexture(this.texture);
			glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			//INSERT-END

			glClear(GL_STENCIL_BUFFER_BIT);
			glEnable(GL_STENCIL_TEST);
			glStencilMask(1);

			glStencilFunc(
					GL_ALWAYS,   // GLenum func
					1,          // GLint ref
					~0);// GLuint mask
			glStencilOp(
					GL_KEEP,
					GL_KEEP,
					GL_REPLACE);

			GL11.glDepthMask(false);
			glAlphaFunc(GL_ALWAYS, 1);
			glColorMask(
					false,   // GLboolean red
					false,   // GLboolean green
					false,   // GLboolean blue
					false);
			glEnable(GL_BLEND);
			glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
			GL11.glDepthMask(false);
			glAlphaFunc(GL_GREATER, 0);

			modeling.renderPart("plate");

			GL11.glDepthMask(true);
			glAlphaFunc(GL_EQUAL, 1);
			glColorMask(
					true,   // GLboolean red
					true,   // GLboolean green
					true,   // GLboolean blue
					true);

			glDisable(GL_DEPTH_TEST);


			glStencilFunc(
					GL_EQUAL,   // GLenum func
					1,          // GLint ref
					~0);// GLuint mask

			glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
			glAlphaFunc(GL_GREATER, 0);
			GL11.glDepthMask(false);

			GL11.glDepthFunc(GL11.GL_ALWAYS);//�����`��
			GL11.glDisable(GL11.GL_LIGHTING);
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
			modeling.renderPart("reticle_light");
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastBrightnessX, lastBrightnessY);
			modeling.renderPart("reticle");
			GL11.glDepthFunc(GL11.GL_LEQUAL);
			GL11.glDepthMask(true);
			glDisable(GL_STENCIL_TEST);
			glEnable(GL_DEPTH_TEST);



//			//INSET : Matrix����w�֕���
//			//      : FBO����e�N�X�`��ID���擾
//			//      : ��ʂɏo�͂ł���悤��Matrix��ۑ���������
//			//      : �e�N�X�`����ViwerPort�ɏo��
//			//      : �ۑ�����Matrix���Ăі߂�
//			GL11.glPopMatrix();
//			int tex = FBO.end();
//
//			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
//
//			glPushMatrix();
//			glDisable(GL_DEPTH_TEST);
//
//			glMatrixMode(GL_PROJECTION);
//			FloatBuffer projectionMatrix = BufferUtils.createFloatBuffer(16);
//			glGetFloat(GL_PROJECTION_MATRIX, projectionMatrix);
//			glLoadIdentity();
//
//			glMatrixMode(GL_MODELVIEW);
//			FloatBuffer modelViewMatrix = BufferUtils.createFloatBuffer(16);
//			glGetFloat(GL_PROJECTION_MATRIX, modelViewMatrix);
//			glLoadIdentity();
//
//			glOrtho(0,1,1,0,-1,1);
//			glDisable(GL_CULL_FACE);
//			glBindTexture(GL_TEXTURE_2D, tex);
//			glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
//			glBegin(GL_QUADS);
//			glTexCoord2d(0.0D, 1.0D);glVertex2d(0.0D, 0.0D);
//			glTexCoord2d(0.0D, 0.0D);glVertex2d(0.0D, 1.0D);
//			glTexCoord2d(1.0D, 0.0D);glVertex2d(1.0D, 1.0D);
//			glTexCoord2d(1.0D, 1.0D);glVertex2d(1.0D, 0.0D);
//			glEnd();
//			glEnable(GL_CULL_FACE);
//
//			glMatrixMode(GL_PROJECTION);
//			glLoadMatrix(projectionMatrix);
//			glMatrixMode(GL_MODELVIEW);
//			glLoadMatrix(modelViewMatrix);
//			glPopMatrix();
//			//INSERT-END
			FMLClientHandler.instance().getClient().getTextureManager().bindTexture(this.texture);
			GL11.glDepthFunc(GL11.GL_LEQUAL);
			glEnable(GL_DEPTH_TEST);
			if(pass == 1) {
				glEnable(GL_BLEND);
				glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
				GL11.glDepthMask(false);
				glAlphaFunc(GL_LEQUAL, 1);
			}else {
				GL11.glDepthMask(true);
				glAlphaFunc(GL_EQUAL, 1);
			}
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)lastBrightnessX, (float)lastBrightnessY);
			GL11.glEnable(GL11.GL_LIGHTING);
		}
		if(pass == 1) {
			glEnable(GL_BLEND);
			glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
			GL11.glDepthMask(false);
			glAlphaFunc(GL_LEQUAL, 1);
		}else {
			GL11.glDepthMask(true);
			glAlphaFunc(GL_EQUAL, 1);
		}

		GL11.glDepthMask(true);
	}
}
