package handmadeguns.client.render;


import cpw.mods.fml.client.FMLClientHandler;
import handmadeguns.event.RenderTickSmoothing;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelCustom;
import org.lwjgl.opengl.GL11;

import javax.script.Invocable;
import javax.script.ScriptException;

import static org.lwjgl.opengl.EXTFramebufferObject.*;
import static org.lwjgl.opengl.GL11.*;

public abstract class PartsRender {
	public IModelCustom model;
	public ResourceLocation texture;
	public float gunPartsScale = 1;
	public int pass;
	

	public abstract void partSidentification(Object... data);

	public static final FrameBuffer FBO = FrameBuffer.create();

	public void part_Render(HMGGunParts parts, GunState state, float flame, int remainbullets, HMGGunParts_Motion_PosAndRotation OffsetAndRotation){
		FMLClientHandler.instance().getWorldClient().theProfiler.startSection("partRender");
//		System.out.println("" + glGetFramebufferAttachmentParameteriEXT(GL_FRAMEBUFFER_EXT,GL_STENCIL_ATTACHMENT_EXT,GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME_EXT));

//		FrameBuffer.defaultFBOID = glGetInteger(GL_FRAMEBUFFER_BINDING_EXT);
//		FrameBuffer.defaultFBO_DepthID = glGetInteger(GL_RENDERBUFFER_BINDING_EXT);
//		System.out.println("debug" + FrameBuffer.defaultFBOID);


//		if(glGetFramebufferAttachmentParameteriEXT(GL_FRAMEBUFFER_EXT,GL_STENCIL_ATTACHMENT_EXT,GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME_EXT) == 0) {
//			System.out.println("attach_Stencil to " + glGetInteger(GL_RENDERBUFFER_BINDING_EXT));
//			System.out.println("attach_Stencil to " + glGetInteger(GL_TEXTURE_BINDING_BUFFER_EXT));
//			int width = Display.getWidth();
//			int height = Display.getHeight();
//			glBindRenderbufferEXT(GL_RENDERBUFFER_EXT, glGetInteger(GL_RENDERBUFFER_BINDING_EXT));
//			glRenderbufferStorageEXT(GL_RENDERBUFFER_EXT, GL_DEPTH24_STENCIL8, width, height);
//			OpenGlHelper.func_153186_a(OpenGlHelper.field_153199_f, org.lwjgl.opengl.EXTPackedDepthStencil.GL_DEPTH24_STENCIL8_EXT, width, height);
//			glFramebufferRenderbufferEXT(GL_FRAMEBUFFER_EXT, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER_EXT, glGetInteger(GL_RENDERBUFFER_BINDING_EXT));
//		}
		if(!parts.initialized){
			GL11.glColorMask(false,false,false,false);
			GL11.glDepthMask(true);
			model.renderPart(parts.partsname);
			parts.currentGroup_parts = ((IModelCustom_HMG)model).renderPart_getInstance();

			model.renderPart(parts.partsname_reticlePlate);
			parts.currentGroup_reticlePlate = ((IModelCustom_HMG)model).renderPart_getInstance();

			model.renderPart(parts.partsname_reticle);
			parts.currentGroup_reticle = ((IModelCustom_HMG)model).renderPart_getInstance();

			model.renderPart(parts.partsname_light);
			parts.currentGroup_light = ((IModelCustom_HMG)model).renderPart_getInstance();


			parts.initialized = true;
		}

		HMGGunParts_Motion_PosAndRotation rotationCenterAndRotation = parts.getRenderinfCenter();
		if(OffsetAndRotation != null && !OffsetAndRotation.renderOnOff)return;
		GL11.glPushMatrix();
		transformParts(rotationCenterAndRotation,parts.getRenderinfDefault_offset(),parts);
		if(OffsetAndRotation != null)transformParts(rotationCenterAndRotation,OffsetAndRotation,parts);


		FMLClientHandler.instance().getWorldClient().theProfiler.startSection("renderPart");
		if(!partModel_render(parts, state, flame, remainbullets, OffsetAndRotation))
			partSidentification(parts.childs,state,flame,remainbullets);
		FMLClientHandler.instance().getWorldClient().theProfiler.endSection();

		GL11.glPopMatrix();
		FMLClientHandler.instance().getWorldClient().theProfiler.endSection();

	}

	public void transformParts(HMGGunParts_Motion_PosAndRotation rotationCenterAndRotation,
	                           HMGGunParts_Motion_PosAndRotation posAndRotation,
	                           HMGGunParts parts){
		if (posAndRotation != null) {
			GL11.glTranslatef(posAndRotation.posX * gunPartsScale, posAndRotation.posY * gunPartsScale, posAndRotation.posZ * gunPartsScale);
			if(parts.rotateTypeIsVector){
				GL11.glTranslatef(rotationCenterAndRotation.posX * gunPartsScale, rotationCenterAndRotation.posY * gunPartsScale, rotationCenterAndRotation.posZ * gunPartsScale);
				GL11.glRotatef(posAndRotation.rotationX, rotationCenterAndRotation.rotateVec.x, rotationCenterAndRotation.rotateVec.y, rotationCenterAndRotation.rotateVec.z);
				GL11.glTranslatef(-rotationCenterAndRotation.posX * gunPartsScale, -rotationCenterAndRotation.posY * gunPartsScale, -rotationCenterAndRotation.posZ * gunPartsScale);
			}else {
				GL11.glTranslatef(rotationCenterAndRotation.posX * gunPartsScale, rotationCenterAndRotation.posY * gunPartsScale, rotationCenterAndRotation.posZ * gunPartsScale);
				GL11.glRotatef(posAndRotation.rotationY, 0, 1, 0);
				GL11.glRotatef(posAndRotation.rotationX, 1, 0, 0);
				GL11.glRotatef(posAndRotation.rotationZ, 0, 0, 1);
				GL11.glTranslatef(-rotationCenterAndRotation.posX * gunPartsScale, -rotationCenterAndRotation.posY * gunPartsScale, -rotationCenterAndRotation.posZ * gunPartsScale);
			}
		}
	}

	public boolean partModel_render(HMGGunParts parts, GunState state, float flame, int remainbullets, HMGGunParts_Motion_PosAndRotation OffsetAndRotation){
		HMGGunParts_Motion_PosAndRotation rotationCenterAndRotation = parts.getRenderinfCenter();
		boolean skip = false;
		if (parts.isbullet) {
			if (state == GunState.Recoil) {
				renderParts_Bullet(parts, flame / 10.0F, remainbullets, rotationCenterAndRotation);
			} else {
				renderParts_Bullet(parts, flame, remainbullets, rotationCenterAndRotation);
			}
		} else {
			if (this.pass == 1) {
				GL11.glEnable(GL_BLEND);
				GL11.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
				GL11.glDepthMask(false);
				GL11.glAlphaFunc(GL_LESS, 1.0f);
			} else {
				GL11.glDepthMask(true);
				GL11.glAlphaFunc(GL_EQUAL, 1.0F);
			}
			try {
				if(parts.script_global!=null)skip = (boolean) ((Invocable)parts.script_global).invokeFunction("ModelUpdate_Pre",this,parts);
			} catch (ScriptException | NoSuchMethodException e) {
				e.printStackTrace();
			}
			float lastBrightnessX = OpenGlHelper.lastBrightnessX;
			float lastBrightnessY = OpenGlHelper.lastBrightnessY;
			if(!skip) {
				parts.currentGroup_parts.render();
				OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
				parts.currentGroup_light.render();
				OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) lastBrightnessX, (float) lastBrightnessY);
			}
			try {
				if(parts.script_global!=null)((Invocable)parts.script_global).invokeFunction("ModelUpdate_Post",this);
			} catch (ScriptException | NoSuchMethodException e) {
				e.printStackTrace();
			}
			if (parts.reticleAndPlate && this.pass == 1) {
//				FBO.start();
//				FBO.attachMotherDepth();
//				GL11.glPushMatrix();

//				System.out.println("debug");

				RenderTickSmoothing.currentFBO = glGetInteger(GL_FRAMEBUFFER_BINDING_EXT);
//		System.out.println("currentFBO " + RenderTickSmoothing.currentFBO);
				RenderTickSmoothing.currentRenderBuffer = glGetInteger(GL_RENDERBUFFER_BINDING_EXT);
//		System.out.println("currentRenderBuffer " + RenderTickSmoothing.currentRenderBuffer);
				RenderTickSmoothing.currentTextureBuffer = glGetFramebufferAttachmentParameteriEXT(GL_FRAMEBUFFER_EXT,GL_DEPTH_ATTACHMENT_EXT,GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME_EXT);;
//		System.out.println("currentTextureBuffer " + RenderTickSmoothing.currentTextureBuffer);
				RenderTickSmoothing.currentStencilBufferID = glGetFramebufferAttachmentParameteriEXT(GL_FRAMEBUFFER_EXT,GL_STENCIL_ATTACHMENT_EXT,GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME_EXT);
//		System.out.println("currentStencilState " + RenderTickSmoothing.currentStencilState);

				FMLClientHandler.instance().getClient().getTextureManager().bindTexture(this.texture);
				GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
				GL11.glClear(GL_STENCIL_BUFFER_BIT);
				GL11.glEnable(GL_STENCIL_TEST);
				GL11.glStencilMask(1);
				GL11.glStencilFunc(GL_ALWAYS, 1, -1);
				GL11.glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE);
				GL11.glColorMask(false, false, false, false);
				GL11.glEnable(GL_BLEND);
				GL11.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
				GL11.glDepthMask(false);
				GL11.glAlphaFunc(GL_GREATER, 0.0F);
//				this.model.renderPart(parts.partsname_reticlePlate);
				parts.currentGroup_reticlePlate.render();


				GL11.glColorMask(true, true, true, true);
				GL11.glStencilFunc(GL_EQUAL, 1, -1);
				GL11.glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
				GL11.glAlphaFunc(GL_GREATER, 0.0F);
				GL11.glDepthMask(false);
				GL11.glDepthFunc(GL_ALWAYS);
				GL11.glDisable(GL_LIGHTING);
				GL11.glDisable(GL_DEPTH_TEST);
				OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
				parts.currentGroup_reticle.render();
				if (parts.reticleChild != null) {
					pass = 0;
					partSidentification(parts.reticleChild, state, flame, Integer.valueOf(remainbullets));
					pass = 1;
					partSidentification(parts.reticleChild, state, flame, Integer.valueOf(remainbullets));
				}
				GL11.glAlphaFunc(GL_GREATER, 0.0F);

				GL11.glDisable(GL_LIGHTING);
				GL11.glDisable(GL_STENCIL_TEST);
//				GL11.glPopMatrix();
//				int tex = FBO.end();
//				OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
//				GL11.glPushMatrix();
//				GL11.glDisable(GL_DEPTH_TEST);
//				GL11.glMatrixMode(GL_PROJECTION);
//				FloatBuffer projectionMatrix = BufferUtils.createFloatBuffer(16);
//				GL11.glGetFloat(GL_PROJECTION_MATRIX, projectionMatrix);
//				GL11.glLoadIdentity();
//				GL11.glMatrixMode(GL_MODELVIEW);
//				FloatBuffer modelViewMatrix = BufferUtils.createFloatBuffer(16);
//				GL11.glGetFloat(GL_PROJECTION_MATRIX, modelViewMatrix);
//				GL11.glLoadIdentity();
//				GL11.glOrtho(0.0D, 1.0D, 1.0D, 0.0D, -1.0D, 1.0D);
//				GL11.glDisable(GL_CULL_FACE);
//				GL11.glBindTexture(GL_TEXTURE_2D, tex);
//				GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
//				GL11.glBegin(GL_QUADS);
//				GL11.glTexCoord2d(0.0D, 1.0D);
//				GL11.glVertex2d(0.0D, 0.0D);
//				GL11.glTexCoord2d(0.0D, 0.0D);
//				GL11.glVertex2d(0.0D, 1.0D);
//				GL11.glTexCoord2d(1.0D, 0.0D);
//				GL11.glVertex2d(1.0D, 1.0D);
//				GL11.glTexCoord2d(1.0D, 1.0D);
//				GL11.glVertex2d(1.0D, 0.0D);
//				GL11.glEnd();
//				GL11.glMatrixMode(GL_PROJECTION);
//				GL11.glLoadMatrix(projectionMatrix);
//				GL11.glMatrixMode(GL_MODELVIEW);
//				GL11.glLoadMatrix(modelViewMatrix);
//				GL11.glPopMatrix();


				FMLClientHandler.instance().getClient().getTextureManager().bindTexture(this.texture);
				OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastBrightnessX, lastBrightnessY);

				GL11.glEnable(GL_CULL_FACE);
				GL11.glEnable(GL_LIGHTING);
				GL11.glEnable(GL_DEPTH_TEST);
				GL11.glDepthMask(true);
				GL11.glDepthFunc(GL_LEQUAL);
			}
		}
		return skip;
	}

	public void renderParts_Bullet(HMGGunParts parts, float flame, int remainbullets, HMGGunParts_Motion_PosAndRotation rotationCenterAndRotation) {
		if (parts.isavatar) {
			if (parts.isbelt) {
				for (int i = 0; i < parts.Maximum_number_of_bullets && i < remainbullets; i++) {
					HMGGunParts_Motion_PosAndRotation bulletoffset = parts.getBulletposition(i - flame);
					if (bulletoffset != null && bulletoffset.renderOnOff) {
						GL11.glPushMatrix();
						transformParts(rotationCenterAndRotation, bulletoffset, parts);
						parts.currentGroup_parts.render();
						parts.currentGroup_parts.render();
						float lastBrightnessX = OpenGlHelper.lastBrightnessX;
						float lastBrightnessY = OpenGlHelper.lastBrightnessY;
						OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
						parts.currentGroup_light.render();
						OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastBrightnessX, lastBrightnessY);
						GL11.glPopMatrix();
					}
				}
			} else {
				HMGGunParts_Motion_PosAndRotation bulletoffset = parts.getRenderinfOfBullet();
				if (bulletoffset != null && bulletoffset.renderOnOff) {
					GL11.glTranslatef(bulletoffset.posX * -flame * this.gunPartsScale, bulletoffset.posY * -flame * this.gunPartsScale, bulletoffset.posZ * -flame * this.gunPartsScale);
					GL11.glTranslatef(rotationCenterAndRotation.posX * this.gunPartsScale, rotationCenterAndRotation.posY * this.gunPartsScale, rotationCenterAndRotation.posZ * this.gunPartsScale);
					GL11.glRotatef(bulletoffset.rotationY * -flame, 0.0F, 1.0F, 0.0F);
					GL11.glRotatef(bulletoffset.rotationX * -flame, 1.0F, 0.0F, 0.0F);
					GL11.glRotatef(bulletoffset.rotationZ * -flame, 0.0F, 0.0F, 1.0F);
					GL11.glTranslatef(-rotationCenterAndRotation.posX * this.gunPartsScale, -rotationCenterAndRotation.posY * this.gunPartsScale, -rotationCenterAndRotation.posZ * this.gunPartsScale);
					for (int i = 0; i < parts.Maximum_number_of_bullets && i < remainbullets; i++) {
						transformParts(rotationCenterAndRotation, bulletoffset, parts);
						parts.currentGroup_parts.render();
						float lastBrightnessX = OpenGlHelper.lastBrightnessX;
						float lastBrightnessY = OpenGlHelper.lastBrightnessY;
						OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
						parts.currentGroup_light.render();
						OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastBrightnessX, lastBrightnessY);
					}
				}
			}
		} else if (parts.isbelt) {
			HMGGunParts_Motion_PosAndRotation bulletoffset = parts.getBulletposition(remainbullets + flame);
			if (bulletoffset != null && bulletoffset.renderOnOff) {
				GL11.glPushMatrix();
				transformParts(rotationCenterAndRotation, bulletoffset, parts);
				parts.currentGroup_parts.render();
				float lastBrightnessX = OpenGlHelper.lastBrightnessX;
				float lastBrightnessY = OpenGlHelper.lastBrightnessY;
				OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
				parts.currentGroup_light.render();
				OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastBrightnessX, lastBrightnessY);
				GL11.glPopMatrix();
			}
		} else {
			HMGGunParts_Motion_PosAndRotation bulletoffset = parts.getRenderinfOfBullet();
			if (bulletoffset != null && bulletoffset.renderOnOff) {
				GL11.glTranslatef(bulletoffset.posX * flame * this.gunPartsScale, bulletoffset.posY * flame * this.gunPartsScale, bulletoffset.posZ * flame * this.gunPartsScale);
				GL11.glTranslatef(rotationCenterAndRotation.posX * this.gunPartsScale, rotationCenterAndRotation.posY * this.gunPartsScale, rotationCenterAndRotation.posZ * this.gunPartsScale);
				GL11.glRotatef(bulletoffset.rotationY * flame, 0.0F, 1.0F, 0.0F);
				GL11.glRotatef(bulletoffset.rotationX * flame, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(bulletoffset.rotationZ * flame, 0.0F, 0.0F, 1.0F);
				GL11.glTranslatef(-rotationCenterAndRotation.posX * this.gunPartsScale, -rotationCenterAndRotation.posY * this.gunPartsScale, -rotationCenterAndRotation.posZ * this.gunPartsScale);
				for (int i = 0; i < parts.Maximum_number_of_bullets && i < remainbullets; i++)
					transformParts(rotationCenterAndRotation, bulletoffset, parts);
				parts.currentGroup_parts.render();
				float lastBrightnessX = OpenGlHelper.lastBrightnessX;
				float lastBrightnessY = OpenGlHelper.lastBrightnessY;
				OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
				parts.currentGroup_light.render();
				OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastBrightnessX, lastBrightnessY);
			}
		}
	}
}