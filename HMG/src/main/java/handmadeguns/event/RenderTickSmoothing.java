package handmadeguns.event;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import handmadeguns.HandmadeGunsCore;
import handmadeguns.entity.HMGEntityParticles;
import handmadeguns.client.render.HMGRenderItemGun_U;
import handmadeguns.client.render.HMGRenderItemGun_U_NEW;
import handmadeguns.items.guns.HMGItem_Unified_Guns;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.EXTFramebufferObject;

import java.nio.FloatBuffer;

import static handmadeguns.HandmadeGunsCore.HMG_proxy;
import static handmadeguns.HandmadeGunsCore.cfg_Sneak_ByADSKey;
import static handmadeguns.client.render.HMGRenderItemGun_U_NEW.*;
import static handmadeguns.event.HMGEventZoom.currentZoomLevel;
import static handmadevehicle.HMVehicle.HMV_Proxy;
import static org.lwjgl.opengl.ARBFramebufferObject.*;
import static org.lwjgl.opengl.EXTFramebufferObject.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.NVPackedDepthStencil.GL_DEPTH_STENCIL_NV;

public class RenderTickSmoothing {

	public static boolean test_ReCreate = false;

	//todo onRenderTickStartでマウス感度を下げ、onRenderTickEndで復帰させればズーム時の照準が楽になるだろう

	public static float backUppedMouseSensitivity = -1;



	public static int currentFBO = -1;
	public static int currentRenderBuffer = -1;
	public static int currentTextureBuffer = -1;
	public static int currentStencilBufferID = -1;

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void renderTick(TickEvent.RenderTickEvent event)
	{
		switch(event.phase)
		{
			case START :
				if(event.renderTickTime<1)
					HMGRenderItemGun_U.smoothing = event.renderTickTime;
				HMGRenderItemGun_U_NEW.smoothing = event.renderTickTime;
				HMGEntityParticles.particaltick = event.renderTickTime;
				HandmadeGunsCore.smooth = event.renderTickTime;
				if(currentZoomLevel != 1) {
					backUppedMouseSensitivity = HMG_proxy.getMCInstance().gameSettings.mouseSensitivity;
					HMG_proxy.getMCInstance().gameSettings.mouseSensitivity /= currentZoomLevel;
				}else {
					backUppedMouseSensitivity = -1;
				}
				currentZoomLevel = 1;

//				System.out.println("currentFBO " + glGetInteger(GL_FRAMEBUFFER_BINDING_EXT));
				//StencilBufferとDepthをアタッチ
//				if(FrameBuffer.defaultFBOID != -1 && glGetFramebufferAttachmentParameteriEXT(GL_FRAMEBUFFER_EXT,GL_STENCIL_ATTACHMENT_EXT,GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME_EXT) == 0) {
//					int width = Display.getWidth();
//					int height = Display.getHeight();
//					int prevFrame = glGetInteger(GL_FRAMEBUFFER_BINDING_EXT);
//					int prevRenderBuffer = glGetInteger(GL_RENDERBUFFER_BINDING_EXT);
//					glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, FrameBuffer.defaultFBOID);
////                        System.out.println("debug" + FMLClientHandler.instance().getClient().getFramebuffer().depthBuffer);
////					System.out.println("debug" + defaultFBO_DepthID);
//
////					glRenderbufferStorageEXT(GL_RENDERBUFFER_EXT, GL_DEPTH24_STENCIL8, width, height);
////                        glFramebufferRenderbufferEXT(GL_FRAMEBUFFER_EXT, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER_EXT, depthID);
//
//					glBindRenderbufferEXT(GL_RENDERBUFFER_EXT, defaultFBO_DepthID);
//					OpenGlHelper.func_153186_a(OpenGlHelper.field_153199_f, org.lwjgl.opengl.EXTPackedDepthStencil.GL_DEPTH24_STENCIL8_EXT, width, height);
//					OpenGlHelper.func_153190_b(OpenGlHelper.field_153198_e, org.lwjgl.opengl.EXTFramebufferObject.GL_DEPTH_ATTACHMENT_EXT, OpenGlHelper.field_153199_f, defaultFBO_DepthID);
//					OpenGlHelper.func_153190_b(OpenGlHelper.field_153198_e, org.lwjgl.opengl.EXTFramebufferObject.GL_STENCIL_ATTACHMENT_EXT, OpenGlHelper.field_153199_f, defaultFBO_DepthID);
//
//					prevDefID = FrameBuffer.defaultFBOID;
//					glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, prevFrame);
//					glBindRenderbufferEXT(GL_RENDERBUFFER_EXT, prevRenderBuffer);
//				}
				if(currentFBO != -1 && currentStencilBufferID == 0) {
					int width = Display.getWidth();
					int height = Display.getHeight();
					int prevFrame = glGetInteger(GL_FRAMEBUFFER_BINDING_EXT);
					int prevRenderBuffer = glGetInteger(GL_RENDERBUFFER_BINDING_EXT);
					int prevTexture = glGetFramebufferAttachmentParameteriEXT(GL_FRAMEBUFFER_EXT,GL_DEPTH_ATTACHMENT_EXT,GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME_EXT);

					glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, currentFBO);
					System.out.println("attach_Stencil to " + currentFBO);
//					System.out.println("FboInfo " + glGetFramebufferAttachmentParameteri( GL_RENDERBUFFER, GL_DEPTH_STENCIL_ATTACHMENT,GL_FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE));
//					System.out.println("FboInfo " + glGetFramebufferAttachmentParameteri( GL_RENDERBUFFER, GL_DEPTH_STENCIL_ATTACHMENT,GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME));
//					System.out.println("FboInfo " + glGetFramebufferAttachmentParameteri( GL_RENDERBUFFER, GL_DEPTH_STENCIL_ATTACHMENT,GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_LEVEL));
//					System.out.println("FboInfo " + glGetFramebufferAttachmentParameteri( GL_RENDERBUFFER, GL_DEPTH_STENCIL_ATTACHMENT,GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_CUBE_MAP_FACE));
//					System.out.println("FboInfo " + glGetFramebufferAttachmentParameteri( GL_RENDERBUFFER, GL_DEPTH_STENCIL_ATTACHMENT,GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_LAYER));
//					System.out.println("FboInfo " + glGetFramebufferAttachmentParameteri( GL_RENDERBUFFER, GL_DEPTH_STENCIL_ATTACHMENT,GL_FRAMEBUFFER_ATTACHMENT_COLOR_ENCODING));
//					System.out.println("FboInfo " + glGetFramebufferAttachmentParameteri( GL_RENDERBUFFER, GL_DEPTH_STENCIL_ATTACHMENT,GL_FRAMEBUFFER_ATTACHMENT_COMPONENT_TYPE));
//					System.out.println("FboInfo " + glGetFramebufferAttachmentParameteri( GL_RENDERBUFFER, GL_DEPTH_STENCIL_ATTACHMENT,GL_FRAMEBUFFER_ATTACHMENT_RED_SIZE));
//					System.out.println("FboInfo " + glGetFramebufferAttachmentParameteri( GL_RENDERBUFFER, GL_DEPTH_STENCIL_ATTACHMENT,GL_FRAMEBUFFER_ATTACHMENT_GREEN_SIZE));
//					System.out.println("FboInfo " + glGetFramebufferAttachmentParameteri( GL_RENDERBUFFER, GL_DEPTH_STENCIL_ATTACHMENT,GL_FRAMEBUFFER_ATTACHMENT_BLUE_SIZE));
//					System.out.println("FboInfo " + glGetFramebufferAttachmentParameteri( GL_RENDERBUFFER, GL_DEPTH_STENCIL_ATTACHMENT,GL_FRAMEBUFFER_ATTACHMENT_ALPHA_SIZE));
//					System.out.println("FboInfo " + glGetFramebufferAttachmentParameteri( GL_RENDERBUFFER, GL_DEPTH_STENCIL_ATTACHMENT,GL_FRAMEBUFFER_ATTACHMENT_DEPTH_SIZE));
//					System.out.println("FboInfo " + glGetFramebufferAttachmentParameteri( GL_RENDERBUFFER, GL_DEPTH_STENCIL_ATTACHMENT,GL_FRAMEBUFFER_ATTACHMENT_STENCIL_SIZE));
//                        System.out.println("debug" + FMLClientHandler.instance().getClient().getFramebuffer().depthBuffer);
//					System.out.println("debug" + defaultFBO_DepthID);

//					glRenderbufferStorageEXT(GL_RENDERBUFFER_EXT, GL_DEPTH24_STENCIL8, width, height);
//                        glFramebufferRenderbufferEXT(GL_FRAMEBUFFER_EXT, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER_EXT, depthID);

					if(currentTextureBuffer > 0) {
						System.out.println("" + currentTextureBuffer);
						glBindTexture(GL_TEXTURE_2D,currentTextureBuffer);
						int prevWidth = glGetRenderbufferParameteri( GL_RENDERBUFFER, GL_RENDERBUFFER_WIDTH);
						int prevHeight = glGetRenderbufferParameteri( GL_RENDERBUFFER, GL_RENDERBUFFER_HEIGHT);
//						System.out.println("prevFormat " + glGetRenderbufferParameteri( GL_RENDERBUFFER, GL_RENDERBUFFER_INTERNAL_FORMAT));
//						System.out.println("prevFormat " + glGetRenderbufferParameteri( GL_RENDERBUFFER, GL_RENDERBUFFER_RED_SIZE));
//						System.out.println("prevFormat " + glGetRenderbufferParameteri( GL_RENDERBUFFER, GL_RENDERBUFFER_GREEN_SIZE));
//						System.out.println("prevFormat " + glGetRenderbufferParameteri( GL_RENDERBUFFER, GL_RENDERBUFFER_BLUE_SIZE));
//						System.out.println("prevFormat " + glGetRenderbufferParameteri( GL_RENDERBUFFER, GL_RENDERBUFFER_ALPHA_SIZE));
//						System.out.println("prevFormat " + glGetRenderbufferParameteri( GL_RENDERBUFFER, GL_RENDERBUFFER_DEPTH_SIZE));
//						System.out.println("prevFormat " + glGetRenderbufferParameteri( GL_RENDERBUFFER, GL_RENDERBUFFER_STENCIL_SIZE));
//						System.out.println("prevFormat " + glGetRenderbufferParameteri( GL_RENDERBUFFER, GL_RENDERBUFFER_SAMPLES));

						if(width == prevWidth && height == prevHeight) {
//							System.out.println("prevWidth " + prevHeight);
//							System.out.println("prevWidth " + prevWidth);
							glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_STENCIL, prevWidth, prevHeight, 0, GL_DEPTH_STENCIL, GL_UNSIGNED_INT_24_8, (FloatBuffer) null);
							glFramebufferTexture2DEXT(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT_EXT, GL_TEXTURE_2D, currentTextureBuffer, 0);
							glFramebufferTexture2DEXT(GL_FRAMEBUFFER, GL_STENCIL_ATTACHMENT_EXT, GL_TEXTURE_2D, currentTextureBuffer, 0);
						}
					}
					int status = EXTFramebufferObject.glCheckFramebufferStatusEXT(GL_FRAMEBUFFER);
					if (status != GL_FRAMEBUFFER_COMPLETE) {
						System.out.println("ERROR");
					}

//					if(currentRenderBuffer > 0) {
//						System.out.println("" + currentRenderBuffer);
//						glBindRenderbufferEXT(GL_RENDERBUFFER_EXT, currentRenderBuffer);
//						OpenGlHelper.func_153186_a(OpenGlHelper.field_153199_f, org.lwjgl.opengl.EXTPackedDepthStencil.GL_DEPTH24_STENCIL8_EXT, width, height);
//						OpenGlHelper.func_153190_b(OpenGlHelper.field_153198_e, org.lwjgl.opengl.EXTFramebufferObject.GL_DEPTH_ATTACHMENT_EXT, OpenGlHelper.field_153199_f, currentRenderBuffer);
//						OpenGlHelper.func_153190_b(OpenGlHelper.field_153198_e, org.lwjgl.opengl.EXTFramebufferObject.GL_STENCIL_ATTACHMENT_EXT, OpenGlHelper.field_153199_f, currentRenderBuffer);
//					}
					if (status != GL_FRAMEBUFFER_COMPLETE) {
						System.out.println("ERROR");
					}

					{
						RenderTickSmoothing.currentStencilBufferID = glGetFramebufferAttachmentParameteriEXT(GL_FRAMEBUFFER_EXT,GL_STENCIL_ATTACHMENT_EXT,GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME_EXT);
					}

					glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, prevFrame);
					glBindRenderbufferEXT(GL_RENDERBUFFER_EXT, prevRenderBuffer);
					glBindTexture(GL_TEXTURE_2D,prevTexture);

				}
			break;
			case END :
				if(backUppedMouseSensitivity != -1) {
					HMG_proxy.getMCInstance().gameSettings.mouseSensitivity = backUppedMouseSensitivity;
				}
				break;
		}
	}
	@SubscribeEvent
	public void clientTickEvent(TickEvent.ClientTickEvent event)
	{
		switch(event.phase)
		{
			case START :
				if(HMG_proxy.getEntityPlayerInstance() != null) {
					EntityPlayer entityPlayer = HMG_proxy.getEntityPlayerInstance();
					prevReloadState = firstPerson_ReloadState;
					if(entityPlayer.getCurrentEquippedItem() != null &&
							entityPlayer.getHeldItem().getItem() instanceof HMGItem_Unified_Guns){
						((HMGItem_Unified_Guns) entityPlayer.getHeldItem().getItem()).checkTags(entityPlayer.getHeldItem());
						firstPerson_ReloadState = entityPlayer.getHeldItem().getTagCompound().getBoolean("IsReloading");
					}
					prevSprintState = firstPerson_SprintState;
					if(!firstPerson_ReloadState)firstPerson_SprintState = isentitysprinting(entityPlayer);
					else firstPerson_SprintState = false;

//                    System.out.println("debug" + firstPerson_ADSState);
					prevADSState = firstPerson_ADSState;
					if(!firstPerson_SprintState)
						firstPerson_ADSState = HandmadeGunsCore.Key_ADS((entityPlayer));
					else
						firstPerson_ADSState = false;
//                    System.out.println("debug" + firstPerson_ADSState);

					if(firstPerson_ADSState && cfg_Sneak_ByADSKey){
						if(HMG_proxy.getEntityPlayerInstance() == entityPlayer &&
								entityPlayer.ridingEntity == null &&
								((EntityPlayer) entityPlayer).getHeldItem() != null &&
								((EntityPlayer) entityPlayer).getHeldItem().getItem() instanceof HMGItem_Unified_Guns &&
								!isentitysprinting(HMG_proxy.getEntityPlayerInstance()))
							((EntityClientPlayerMP) HMG_proxy.getEntityPlayerInstance()).movementInput.sneak = true;
					}

					HMV_Proxy.zoomclick();
				}
				break;
			case END :
				break;
		}
	}
}
