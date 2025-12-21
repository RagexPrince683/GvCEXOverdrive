package handmadeguns.event;

import cpw.mods.fml.common.eventhandler.EventPriority;
import handmadeguns.HandmadeGunsCore;
import handmadeguns.entity.PlacedGunEntity;
import handmadeguns.items.*;
import handmadeguns.items.guns.*;
import handmadeguns.event.AmmoHUDRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.*;
import org.lwjgl.opengl.GL11;


import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.client.GuiIngameForge;
//import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import org.lwjgl.util.glu.Project;

import javax.script.Invocable;
import javax.script.ScriptException;
import javax.vecmath.Vector3d;

import java.util.Iterator;

import static handmadeguns.HandmadeGunsCore.HMG_proxy;
import static handmadeguns.client.render.HMGRenderItemGun_U_NEW.*;
import static handmadeguns.items.guns.HMGItem_Unified_Guns.computeMoveSpeed_WithoutGunModifier;
import static handmadevehicle.Utils.*;
import static handmadevehicle.events.HMVRenderSomeEvent.entityCurrentPos;
import static handmadevehicle.render.RenderVehicle.partialTicks;
import static net.minecraft.client.gui.Gui.icons;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;

public class HMGEventZoom {
	static boolean updated = false;

	// track last camera zoom we forced and whether we forced zoom/reset
	private double hmg_lastForcedCameraZoom = 1.0d;
	private boolean hmg_zoomLockedByUs = false;
	private boolean hmg_blendEnabledByUs = false;

	// public HMGItemGunBase gunbase;

	public static float currentZoomLevel = 1;
	public boolean zoomtype;
	public Item itemss;
	public boolean needreset = false;
	public boolean slot;
	public int targetEntityID = -1;
	ResourceLocation crosstex = new ResourceLocation("handmadeguns:textures/items/crosshair.png");
	ResourceLocation pointer = new ResourceLocation("handmadeguns:textures/entity/laser.png");
	public ItemStack previtemstack;
	public static boolean isSlowdowned;
	//1tick遅れて低速化が有効になるためフラグを3つ使う
	public static boolean isSlowdowned2;
	public static boolean isSlowdowned3;
	private double premotion;


	//@SideOnly(Side.CLIENT)
	//@SubscribeEvent
	//public void renderfov(FOVUpdateEvent event)
	//{
	//	EntityPlayer entityPlayer = event.entity;
//
	//	// --- Movement-based FOV (compute once) ---
	//	IAttributeInstance iattributeinstance = entityPlayer.getEntityAttribute(SharedMonsterAttributes.movementSpeed);
	//	iattributeinstance = entityPlayer.getEntityAttribute(SharedMonsterAttributes.movementSpeed);
	//	double value = computeMoveSpeed_WithoutGunModifier((ModifiableAttributeInstance) iattributeinstance);
//
	//	float baseFov = 1.0F;
	//	if (entityPlayer.capabilities.isFlying)
	//	{
	//		baseFov *= 1.1F;
	//	}
//
	//	baseFov = (float)((double)baseFov * ((value / (double)entityPlayer.capabilities.getWalkSpeed() + 1.0D) / 2.0D));
//
	//	if (entityPlayer.capabilities.getWalkSpeed() == 0.0F || Float.isNaN(baseFov) || Float.isInfinite(baseFov))
	//	{
	//		baseFov = 1.0F;
	//	}
//
	//	if (entityPlayer.isUsingItem() && entityPlayer.getItemInUse() != null && entityPlayer.getItemInUse().getItem() == Items.bow)
	//	{
	//		int i = entityPlayer.getItemInUseDuration();
	//		float f1 = (float)i / 20.0F;
//
	//		if (f1 > 1.0F)
	//		{
	//			f1 = 1.0F;
	//		}
	//		else
	//		{
	//			f1 *= f1;
	//		}
//
	//		baseFov *= 1.0F - f1 * 0.15F;
	//	}
//
	//	// Start with baseFov as computed
	//	float finalFov = baseFov;
//
	//	// --- Handle gun / scope zoom (apply once) ---
	//	ItemStack itemstack = entityPlayer.getCurrentEquippedItem();
	//	Entity ridingEntity = entityPlayer.ridingEntity;
	//	if (ridingEntity instanceof PlacedGunEntity)
	//	{
	//		PlacedGunEntity pge = (PlacedGunEntity) ridingEntity;
	//		if (pge.gunStack != null && pge.gunStack.getItem() instanceof HMGItem_Unified_Guns)
	//		{
	//			itemstack = pge.gunStack;
	//		}
	//	}
//
	//	float zoomFactor = 1.0F;
	//	float newZoomLevel = 1.0F;
//
	//	if (itemstack != null && itemstack.getItem() instanceof HMGItem_Unified_Guns)
	//	{
	//		HMGItem_Unified_Guns gunbase = (HMGItem_Unified_Guns) itemstack.getItem();
//
	//		// Only apply scope zoom when ADS is active and previous ADS state is true
	//		if (firstPerson_ADSState && prevADSState)
	//		{
	//			// if the player is sprinting, skip scope zoom to prevent oscillation
	//			if (!entityPlayer.isSprinting())
	//			{
	//				// Guard NBT/tag access
	//				ItemStack itemstackSight = null;
	//				if (itemstack.hasTagCompound() && itemstack.getTagCompound().hasKey("Items"))
	//				{
	//					NBTTagList tags = (NBTTagList) itemstack.getTagCompound().getTag("Items");
	//					if (tags != null)
	//					{
	//						// read into a small array safely
	//						ItemStack[] items = new ItemStack[6];
	//						int loopCount = Math.min(tags.tagCount(), 7); // avoid index issues
	//						for (int i = 0; i < loopCount; i++)
	//						{
	//							NBTTagCompound tagCompound = tags.getCompoundTagAt(i);
	//							int slot = tagCompound.getByte("Slot");
	//							if (slot >= 0 && slot < items.length)
	//							{
	//								items[slot] = ItemStack.loadItemStackFromNBT(tagCompound);
	//							}
	//						}
	//						itemstackSight = items[1];
	//					}
	//				}
//
	//				// choose zoom factor based on sight type
	//				if (itemstackSight != null)
	//				{
	//					if (itemstackSight.getItem() instanceof HMGItemAttachment_reddot)
	//					{
	//						if (gunbase.gunInfo.canobj && gunbase.gunInfo.zoomrer && !isentitysprinting(entityPlayer))
	//						{
	//							zoomFactor = gunbase.gunInfo.scopezoomred;
	//							newZoomLevel = gunbase.gunInfo.scopezoomred;
	//						}
	//					}
	//					else if (itemstackSight.getItem() instanceof HMGItemAttachment_scope && !isentitysprinting(entityPlayer))
	//					{
	//						if (gunbase.gunInfo.canobj && gunbase.gunInfo.zoomres && !isentitysprinting(entityPlayer))
	//						{
	//							zoomFactor = gunbase.gunInfo.scopezoomscope;
	//							newZoomLevel = gunbase.gunInfo.scopezoomscope;
	//						}
	//					}
	//					else if (itemstackSight.getItem() instanceof HMGItemSightBase && !isentitysprinting(entityPlayer))
	//					{
	//						if (gunbase.gunInfo.canobj && !((HMGItemSightBase) itemstackSight.getItem()).scopeonly && !isentitysprinting(entityPlayer))
	//						{
	//							zoomFactor = ((HMGItemSightBase) itemstackSight.getItem()).zoomlevel;
	//							newZoomLevel = ((HMGItemSightBase) itemstackSight.getItem()).zoomlevel;
	//						}
	//					}
	//					else
	//					{
	//						if (gunbase.gunInfo.canobj && gunbase.gunInfo.zoomren && !isentitysprinting(entityPlayer))
	//						{
	//							zoomFactor = gunbase.gunInfo.scopezoombase;
	//							newZoomLevel = gunbase.gunInfo.scopezoombase;
	//						}
	//					}
	//				}
	//				else
	//				{
	//					if (gunbase.gunInfo.canobj && gunbase.gunInfo.zoomren && !isentitysprinting(entityPlayer))
	//					{
	//						zoomFactor = gunbase.gunInfo.scopezoombase;
	//						newZoomLevel = gunbase.gunInfo.scopezoombase;
	//					}
	//				}
	//			} // end !isSprinting()
	//		} // end ADS check
//
	//		// when flying, compensate (keep old behaviour)
	//		if (entityPlayer.capabilities.isFlying)
	//		{
	//			finalFov /= 1.1F;
	//		}
	//	} // end itemstack != null
//
	//	// apply zoom factor once (protect against division by zero)
	//	if (zoomFactor <= 0.00001F) zoomFactor = 1.0F;
	//	finalFov = finalFov / zoomFactor;
//
	//	// update event once
	//	event.newfov = finalFov;
//
	//	// update currentZoomLevel only when changed
	//	if (newZoomLevel != currentZoomLevel)
	//	{
	//		currentZoomLevel = newZoomLevel;
	//	}
	//}

	//todo onRenderTickStartでマウス感度を下げ、onRenderTickEndで復帰させればズーム時の照準が楽になるだろう
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void displayHUD(RenderGameOverlayEvent.Post event) {
		if (event.type == RenderGameOverlayEvent.ElementType.ALL) {
			GL11.glEnable(GL_BLEND);
			GL11.glEnable(GL11.GL_ALPHA_TEST);
			GL11.glAlphaFunc(GL11.GL_GREATER, 0);
			Minecraft minecraft = FMLClientHandler.instance().getClient();
			ScaledResolution scaledresolution = new ScaledResolution(minecraft, minecraft.displayWidth,
					minecraft.displayHeight);
			float screenWidth = scaledresolution.getScaledWidth();
			float screenHeight = scaledresolution.getScaledHeight();
			// Entity entity = minecraft.pointedEntity;
			EntityPlayer entityplayer = minecraft.thePlayer;
			// EntityPlayer entityplayer = event.player;




			ItemStack gunstack = ((entityplayer)).getCurrentEquippedItem();

			Entity ridingEntity = entityplayer.ridingEntity;
			if(ridingEntity instanceof PlacedGunEntity) {
				if (((PlacedGunEntity) ridingEntity).gunStack != null && ((PlacedGunEntity) ridingEntity).gunStack.getItem() instanceof HMGItem_Unified_Guns) {
					gunstack = ((PlacedGunEntity) ridingEntity).gunStack;
				}
			}
			if (gunstack != previtemstack) {
				needreset = true;
			}
			// FontRenderer fontrenderer = minecraft.fontRenderer;
			// minecraft.entityRenderer.setupOverlayRendering();
			// OpenGlHelper.
			GL11.glEnable(GL11.GL_BLEND);
			if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
				if (gunstack != null && gunstack.getItem() instanceof HMGItem_Unified_Guns) {
					HMGItem_Unified_Guns gunItem = (HMGItem_Unified_Guns) gunstack.getItem();
					String ads = gunItem.gunInfo.adstexture;
					String adsr = gunItem.gunInfo.adstexturer;
					String adss = gunItem.gunInfo.adstextures;
					((HMGItem_Unified_Guns) gunstack.getItem()).checkTags(gunstack);
					NBTTagCompound nbt = gunstack.getTagCompound();
					//String ads = nbt.getString("adstexture");
					EntityRenderer entityrender = minecraft.entityRenderer;
					ItemRenderer itemrender = entityrender.itemRenderer;
					//itemrender.
					targetEntityID = -1;
					boolean recoiled = nbt.getBoolean("Recoiled");
					if (nbt.getBoolean("islockedentity")) {
						targetEntityID = nbt.getInteger("TGT");
					}
					float spreadDiffusion = nbt.getFloat("Diffusion");
					float bure = gunItem.gunInfo.spread_setting;
					bure *= firstPerson_ADSState && prevADSState ? gunItem.gunInfo.ads_spread_cof : 1;
					bure += gunItem.gunInfo.spread_setting * spreadDiffusion;
					((HMGItem_Unified_Guns) gunstack.getItem()).checkTags(gunstack);
					ItemStack[] items = new ItemStack[6];
					ItemStack itemstackSight = null;
					NBTTagList tags = (NBTTagList) gunstack.getTagCompound().getTag("Items");
					if (tags != null) {
						for (int i1 = 0; i1 < 7; i1++)//133
						{
							NBTTagCompound tagCompound = tags.getCompoundTagAt(i1);
							int slot = tagCompound.getByte("Slot");
							if (slot >= 0 && slot < items.length) {
								items[slot] = ItemStack.loadItemStackFromNBT(tagCompound);
//                    if(items[slot] != null) {
//                        System.out.println(""+ screenWidth + "" + items[slot].getItem().getUnlocalizedName());
//                    }
							}
						}
					}
					itemstackSight = items[1];


					setUp3DView(minecraft,event.partialTicks);
					GL11.glPushMatrix();
					{
						GL11.glRotatef(0, 1.0F, 0.0F, 0.0F);
						GL11.glRotatef(180, 0.0F, 1.0F, 0.0F);


						boolean skipAfter = false;
						if(gunItem.gunInfo.script != null) {
							try {
								skipAfter = (boolean) ((Invocable)gunItem.gunInfo.script).invokeFunction("GUI_rendering_3D", this);
							} catch (NoSuchMethodException | ScriptException e) {
								e.printStackTrace();
							}
						}
						if(!skipAfter) {
							// this.modelArmor.aimedBow = true;

							//if (entityplayer.isSneaking())

							//TODO problematic code block...???
							if (firstPerson_ADSState && prevADSState) {
								if (itemstackSight != null) {
									if (itemstackSight.getItem() instanceof HMGItemAttachment_reddot) {
										if (!gunItem.gunInfo.canobj || !gunItem.gunInfo.zoomrer && !isentitysprinting(entityplayer)) {
											ObfuscationReflectionHelper.setPrivateValue(EntityRenderer.class, minecraft.entityRenderer,
													gunItem.gunInfo.scopezoomred, "cameraZoom", "field_78503_V");
											currentZoomLevel = gunItem.gunInfo.scopezoomred;
											needreset = true;
										}
										if (gunItem.gunInfo.zoomrert) {
											renderPumpkinBlur(minecraft, adsr);
										}
									} else if (itemstackSight.getItem() instanceof HMGItemAttachment_scope) {
										if (!gunItem.gunInfo.canobj || !gunItem.gunInfo.zoomres && !isentitysprinting(entityplayer)) {
											ObfuscationReflectionHelper.setPrivateValue(EntityRenderer.class, minecraft.entityRenderer,
													gunItem.gunInfo.scopezoomscope, "cameraZoom", "field_78503_V");
											currentZoomLevel = gunItem.gunInfo.scopezoomscope;
											needreset = true;
										}
										if (gunItem.gunInfo.zoomrest) {
											renderPumpkinBlur(minecraft, adss);
										}
									} else if (itemstackSight.getItem() instanceof HMGItemSightBase) {
										if (!gunItem.gunInfo.canobj || ((HMGItemSightBase) itemstackSight.getItem()).scopeonly && !isentitysprinting(entityplayer)) {
											ObfuscationReflectionHelper.setPrivateValue(EntityRenderer.class, minecraft.entityRenderer,
													((HMGItemSightBase) itemstackSight.getItem()).zoomlevel, "cameraZoom", "field_78503_V");
											currentZoomLevel = ((HMGItemSightBase) itemstackSight.getItem()).zoomlevel;
											needreset = true;
										}
										if (((HMGItemSightBase) itemstackSight.getItem()).scopetexture != null) {
											renderPumpkinBlur(minecraft, ((HMGItemSightBase) itemstackSight.getItem()).scopetexture);
										}
									}
								} else {
									if (!gunItem.gunInfo.canobj || !gunItem.gunInfo.zoomren && !isentitysprinting(entityplayer)) {
										ObfuscationReflectionHelper.setPrivateValue(EntityRenderer.class, minecraft.entityRenderer,
												gunItem.gunInfo.scopezoombase, "cameraZoom", "field_78503_V");
										currentZoomLevel = gunItem.gunInfo.scopezoombase;
										needreset = true;
									}
									if (gunItem.gunInfo.zoomrent) {
										renderPumpkinBlur(minecraft, ads);
									}
								}
								if (gunItem.gunInfo.renderMCcross) {
									GuiIngameForge.renderCrosshairs = true;
								} else {
									GuiIngameForge.renderCrosshairs = false;
									GL11.glEnable(GL11.GL_BLEND);
								}
								if (gunItem.gunInfo.renderHMGcross && spreadDiffusion > gunItem.gunInfo.spreadDiffusionmin)
									this.renderCrossHair(minecraft, scaledresolution.getScaledWidth(), scaledresolution.getScaledHeight(), bure);
							}
							//end of problematic code block
							else {
								// GuiIngameForge.renderCrosshairs = true;
								if (gunItem.gunInfo.renderMCcross) {
									GuiIngameForge.renderCrosshairs = true;
								} else {
									GuiIngameForge.renderCrosshairs = false;
									// enable blend only once (avoid repeatedly calling glEnable)
									if (!hmg_blendEnabledByUs) {
										GL11.glEnable(GL11.GL_BLEND);
										hmg_blendEnabledByUs = true;
									}
								}

								if (gunItem.gunInfo.renderHMGcross)
									this.renderCrossHair(minecraft, scaledresolution.getScaledWidth(), scaledresolution.getScaledHeight(), bure);

								// decide if we should reset cameraZoom to 1.0 (no valid sight / zoom not allowed)
								boolean shouldResetZoom = false;

								if (itemstackSight != null) {
									if (itemstackSight.getItem() instanceof HMGItemAttachment_reddot) {
										if (!gunItem.gunInfo.canobj || !gunItem.gunInfo.zoomrer) shouldResetZoom = true;
									} else if (itemstackSight.getItem() instanceof HMGItemAttachment_scope) {
										if (!gunItem.gunInfo.canobj || !gunItem.gunInfo.zoomres) shouldResetZoom = true;
									} else if (itemstackSight.getItem() instanceof HMGItemSightBase) {
										if (!gunItem.gunInfo.canobj || ((HMGItemSightBase) itemstackSight.getItem()).scopeonly) shouldResetZoom = true;
									}
								} else {
									if (!gunItem.gunInfo.canobj || !gunItem.gunInfo.zoomren) shouldResetZoom = true;
								}

								// Only change the EntityRenderer.cameraZoom via reflection when it actually differs from what we want.
								try {
									// read current zoom (field name args to handle obfuscation)
									Double currentZoom = ObfuscationReflectionHelper.getPrivateValue(EntityRenderer.class, minecraft.entityRenderer,
											"cameraZoom", "field_78503_V");
									if (currentZoom == null) currentZoom = 1.0d;

									double desiredZoom = shouldResetZoom ? 1.0d : currentZoom; // here we only *reset* to 1.0 as original block did

									// If we want to reset and value differs, or if we previously forced a zoom and now want to reset,
									// then update. This prevents repeatedly setting the same value every tick.
									if (Math.abs(currentZoom - desiredZoom) > 1e-6) {
										ObfuscationReflectionHelper.setPrivateValue(EntityRenderer.class, minecraft.entityRenderer, desiredZoom,
												"cameraZoom", "field_78503_V");
										hmg_lastForcedCameraZoom = desiredZoom;
										hmg_zoomLockedByUs = desiredZoom != 1.0d;
									} else {
										// keep tracking that we haven't changed anything
										hmg_lastForcedCameraZoom = currentZoom;
									}

									// keep your currentZoomLevel consistent with what you forced (only update when reset)
									if (shouldResetZoom) {
										currentZoomLevel = 1;
									}
								} catch (Exception ex) {
									// don't spam stack traces every tick; log once or ignore silently
									// Fallback: attempt a safe set without reading if reflection read failed
									try {
										ObfuscationReflectionHelper.setPrivateValue(EntityRenderer.class, minecraft.entityRenderer, 1.0d,
												"cameraZoom", "field_78503_V");
										hmg_lastForcedCameraZoom = 1.0d;
										hmg_zoomLockedByUs = false;
										currentZoomLevel = 1;
									} catch (Exception ignore) {
										// give up quietly to avoid spam
									}
								}

								// NOTE: we purposely do NOT set cameraZoom repeatedly while it's already the desired value.
								// This prevents conflicts with vanilla sprint FOV changes and stops the zoom-in/out spam.
							}
							if (gunItem.gunInfo.canlock && nbt != null) {
								//todo 3Dにしたので根本から作り直し
								Vector3d vecToLockTargetPos = null;
								float currentRotationYaw = (minecraft.renderViewEntity.prevRotationYawHead + (minecraft.renderViewEntity.rotationYawHead - minecraft.renderViewEntity.prevRotationYawHead) * partialTicks);

								float currentRotationPit = (minecraft.renderViewEntity.prevRotationPitch + (minecraft.renderViewEntity.rotationPitch - minecraft.renderViewEntity.prevRotationPitch) * partialTicks);
								if (nbt.getBoolean("islockedentity")) {
									Entity TGT = entityplayer.worldObj.getEntityByID(nbt.getInteger("TGT"));
									if (TGT != null) {


										vecToLockTargetPos = new Vector3d();
										vecToLockTargetPos.add(entityCurrentPos(TGT));
										vecToLockTargetPos.sub(entityCurrentPos(minecraft.renderViewEntity));
										vecToLockTargetPos.normalize()
										;
										vecToLockTargetPos.normalize();


										if (gunItem.gunInfo.displayPredict) {
											Vector3d PredictedTargetPos =
													LinePrediction(new Vector3d(
																	minecraft.renderViewEntity.posX,
																	minecraft.renderViewEntity.posY,
																	minecraft.renderViewEntity.posZ),
															new Vector3d(TGT.posX, TGT.posY, TGT.posZ),
															new Vector3d(
																	TGT.motionX - minecraft.renderViewEntity.motionX,
																	TGT.motionY - minecraft.renderViewEntity.motionY,
																	TGT.motionZ - minecraft.renderViewEntity.motionZ),
															gunItem.getTerminalspeed());

											Vector3d vecToLockTarget_PredictPos = new Vector3d(PredictedTargetPos.x - minecraft.renderViewEntity.posX
													, PredictedTargetPos.y - minecraft.renderViewEntity.posY - minecraft.renderViewEntity.getEyeHeight()
													, PredictedTargetPos.z - minecraft.renderViewEntity.posZ
											);
											vecToLockTarget_PredictPos.normalize();
											RotateVectorAroundY(vecToLockTarget_PredictPos, currentRotationYaw);
											RotateVectorAroundX(vecToLockTarget_PredictPos, currentRotationPit);
											renderLockOnMarker(minecraft, gunItem.gunInfo.predictMarker, vecToLockTarget_PredictPos);
										}
									}
								} else if (nbt.getBoolean("islockedblock")) {
									vecToLockTargetPos = new Vector3d();
									vecToLockTargetPos.set(nbt.getDouble("LockedPosX"), nbt.getDouble("LockedPosY"), nbt.getDouble("LockedPosZ"));
									vecToLockTargetPos.sub(new Vector3d(new double[]{entityplayer.posX, entityplayer.posY, entityplayer.posZ}));
									vecToLockTargetPos.normalize();
								}
								if (vecToLockTargetPos != null) {
									RotateVectorAroundY(vecToLockTargetPos, currentRotationYaw);
									RotateVectorAroundX(vecToLockTargetPos, currentRotationPit);
									renderLockOnMarker(minecraft, gunItem.gunInfo.lockOnMarker, vecToLockTargetPos);
								}
							}


							// GuiIngameForge.renderCrosshairs = true;

							this.zoomtype = true;


							//event.
							//EntityRenderer.
							//minecraft.entityRenderer.
							//	entityplayer.eyeHeight = 10F;
							//minecraft.entityRenderer.

							//	GL11.glEnable(GL11.GL_BLEND);
						}
					}
					GL11.glPopMatrix();
					setUp2DView(minecraft);
					// Get screen dimensions
					screenWidth = scaledresolution.getScaledWidth();
					screenHeight = scaledresolution.getScaledHeight();

					if (tags != null) {
						NBTTagCompound tagCompound = tags.getCompoundTagAt(5);
						ItemStack temp = ItemStack.loadItemStackFromNBT(tagCompound);
						if (temp != null) itemss = temp.getItem();
					}

					FontRenderer fontrenderer = minecraft.fontRenderer;
					GL11.glPushMatrix();
					GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
					GL11.glEnable(GL11.GL_BLEND);
					GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

					boolean skipAfter = false;

					if (gunItem.gunInfo.script != null) {
						try {
							skipAfter = (boolean) ((Invocable) gunItem.gunInfo.script).invokeFunction("GUI_rendering_2D", this, gunItem, gunstack);
						} catch (NoSuchMethodException | ScriptException e) {
							e.printStackTrace();
						}
					}

					GuiIngame g = minecraft.ingameGUI;
					if (!skipAfter) {
						// Keeping your existing offsets and positions
						int boxHeight = 5; // Space for magazine info
						int boxWidth = 65; // Adjusted width for the box (fits the icons)
						int iconOffsetX = 6; // Offset for magazine icon X
						int iconOffsetY = -15; // Offset for magazine icon Y
						int x = (int) screenWidth - boxWidth - 40;
						int y = (int) screenHeight - boxHeight - 110; // Position above the ammo HUD

						// First render the icon to ensure it stays on top of the background
						if (gunItem.get_selectingMagazine(gunstack) != null && gunItem.getcurrentMagazine(gunstack) != gunItem.get_selectingMagazine(gunstack)) {
							int stacksize = 0;
							int selectedMagazineY = y - 45; // Position the selected magazine box above the current one

							minecraft.getTextureManager().bindTexture(TextureMap.locationItemsTexture);
							// Draw the selected magazine icon first (ensure it stays on top)
							g.drawTexturedModelRectFromIcon(x + iconOffsetX, selectedMagazineY + iconOffsetY, gunItem.get_selectingMagazine(gunstack).getIconFromDamage(0), 16, 16);

							// Check how many of the selected magazine are in the player's inventory
							for (int is = 0; is < 36; ++is) {
								InventoryPlayer playerInv = entityplayer.inventory;
								ItemStack itemi = playerInv.getStackInSlot(is);
								if (itemi != null && itemi.getItem() == gunItem.get_selectingMagazine(gunstack)) {
									stacksize += itemi.stackSize;
								}
							}

							// Render the background box for the selected magazine (darker inner box)
							AmmoHUDRenderer.drawTransparentRect(x, selectedMagazineY - 35, x + boxWidth, selectedMagazineY + boxHeight, 0x80000000);

							// Render the "x" stack size text for the selected magazine
							String d2 = String.format("%1$3d", stacksize);
							AmmoHUDRenderer.renderTextWithGlow(fontrenderer, "x" + d2, x + iconOffsetX + 30, selectedMagazineY + iconOffsetY, 0xFFFFFF, 0x000000, 1.0f);
							AmmoHUDRenderer.renderTextWithGlow(fontrenderer, "Next", x + iconOffsetX, selectedMagazineY + iconOffsetY - 10, 0xFFFFFF, 0x000000, 1.0f);
						}

						// Then render the current magazine box and icon
						if (gunItem.getcurrentMagazine(gunstack) != null) {
							int stacksize = 0;
							minecraft.getTextureManager().bindTexture(TextureMap.locationItemsTexture);
							// Draw the current magazine icon
							g.drawTexturedModelRectFromIcon(x + iconOffsetX, y + iconOffsetY, gunItem.getcurrentMagazine(gunstack).getIconFromDamage(0), 16, 16);

							// Check how many of the current magazine are in the player's inventory
							for (int is = 0; is < 36; ++is) {
								InventoryPlayer playerInv = entityplayer.inventory;
								ItemStack itemi = playerInv.getStackInSlot(is);
								if (itemi != null && itemi.getItem() == gunItem.getcurrentMagazine(gunstack)) {
									stacksize += itemi.stackSize;
								}
							}

							// Render the background box for the current magazine (this happens if the selected box was not drawn)
							AmmoHUDRenderer.drawTransparentRect(x, y - 35, x + boxWidth, y + boxHeight, 0x80000000);

							// Render the "x" stack size text for the current magazine
							String d2 = String.format("%1$3d", stacksize);
							AmmoHUDRenderer.renderTextWithGlow(fontrenderer, "x" + d2, x + iconOffsetX + 30, y + iconOffsetY, 0xFFFFFF, 0x000000, 1.0f);

							// Render the "Current" label above the current magazine icon
							AmmoHUDRenderer.renderTextWithGlow(fontrenderer, "Current", x + iconOffsetX, y + iconOffsetY - 10, 0xFFFFFF, 0x000000, 1.0f);
						}

						// Render the Ammo HUD (ammo count for the gun and reserve)
						if (gunstack != null && gunstack.getItem() instanceof HMGItem_Unified_Guns) {
							AmmoHUDRenderer.renderAmmoHUD(fontrenderer, (int) screenWidth, (int) screenHeight, gunstack);
						}
					}

					GL11.glPopMatrix();
					GL11.glPopAttrib();

				} else {
					if (needreset) {
						ObfuscationReflectionHelper.setPrivateValue(EntityRenderer.class, minecraft.entityRenderer,
								1.0d, "cameraZoom", "field_78503_V");
						currentZoomLevel = 1;
						needreset = false;
					}
					if (this.zoomtype) {
						GuiIngameForge.renderCrosshairs = true;
					/*if (HandmadeGunsCore.cfg_ZoomRender == true) {
						minecraft.gameSettings.fovSetting = HandmadeGunsCore.cfg_FOV;
						ObfuscationReflectionHelper.setPrivateValue(EntityRenderer.class, minecraft.entityRenderer,
								1.0D, "cameraZoom", "field_78503_V");
					} else {
						ObfuscationReflectionHelper.setPrivateValue(EntityRenderer.class, minecraft.entityRenderer,
								1.0D, "cameraZoom", "field_78503_V");
					}*/
						this.zoomtype = false;
					}

				}
			}

			GL11.glPushMatrix();
			GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
			{
				GL11.glEnable(GL11.GL_BLEND);
				GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
				NBTTagCompound nbts = entityplayer.getEntityData();
				int nb = nbts.getInteger("hitentity");
				if (nb >= 1) {
					GuiIngame g = minecraft.ingameGUI;
					minecraft.renderEngine.bindTexture(new ResourceLocation("handmadeguns:textures/items/hit.png"));
					GL11.glTranslatef(0.5F, 0F, 0F);
					GL11.glScalef(0.0625f, 0.0625f, 1);
					g.drawTexturedModalRect((scaledresolution.getScaledWidth() / 2 - 8) * 16,
							(scaledresolution.getScaledHeight() / 2 - 8) * 16, 0, 0, 256, 256);
					nbts.setInteger("hitentity", nb - 1);
				}
			}
			GL11.glPopAttrib();
			GL11.glPopMatrix();
			previtemstack = gunstack;
		}

		Minecraft.getMinecraft().renderEngine.bindTexture(icons);
	}


	@SideOnly(Side.CLIENT)
	protected void renderBullet(FontRenderer fontrenderer, int i, int j, ItemStack itemstack) {
		String sss = "null";
		String l2 = "null";
		if (itemstack != null && itemstack.getItem() instanceof HMGItem_Unified_Guns) {
			HMGItem_Unified_Guns gunbase = (HMGItem_Unified_Guns) itemstack.getItem();
			ItemStack[] items = new ItemStack[6];
			NBTTagList tags = (NBTTagList)  itemstack.getTagCompound().getTag("Items");
			if(tags != null) {
				for (int l = 0; l < 7; l++)//133
				{
					NBTTagCompound tagCompound = tags.getCompoundTagAt(l);
					int slot = tagCompound.getByte("Slot");
					if (slot >= 0 && slot < items.length) {
						items[slot] = ItemStack.loadItemStackFromNBT(tagCompound);
//                    if(items[slot] != null) {
//                        System.out.println(""+ i + "" + items[slot].getItem().getUnlocalizedName());
//                    }
					}
				}
			}
			switch (gunbase.gunInfo.guntype) {
				case 4:
				case 0:
					sss = "normal";
					break;
				case 1:
					sss = "buckshot  " + gunbase.gunInfo.pellet + " pellet";
					break;
				case 2:
					sss = "grenade";
					break;
				case 3:
					sss = "rocket";
					break;
			}
			ItemStack itemstacka = items[5];
			if(itemstacka !=null){
				if(itemstacka.getItem() instanceof HMGItemBullet_AP){
					switch (gunbase.gunInfo.guntype) {
						case 0:
						case 4:
							sss = "AP";
							break;
						case 1:
							sss = "AP buckshot  " + gunbase.gunInfo.pellet + " pellet";
							break;
						case 2:
							sss = "grenade";
							break;
						case 3:
							sss = "rocket";
							break;
					}
				}else if(itemstacka.getItem() instanceof HMGItemBullet_AT){
					switch (gunbase.gunInfo.guntype) {
						case 0:
						case 4:
							sss = "Anesthesia";
							break;
						case 1:
							sss = "Anesthesia";
							break;
						case 2:
							sss = "grenade";
							break;
						case 3:
							sss = "rocket";
							break;
					}
				}else if(itemstacka.getItem() instanceof HMGItemBullet_Frag){
					switch (gunbase.gunInfo.guntype) {
						case 0:
						case 4:
							sss = "Frag";
							break;
						case 1:
							sss = "Frag";
							break;
						case 2:
							sss = "grenade";
							break;
						case 3:
							sss = "rocket";
							break;
					}
				}else if(itemstacka.getItem() instanceof HMGItemBullet_TE){
					switch (gunbase.gunInfo.guntype) {
						case 0:
						case 4:
							sss = "normal";
							break;
						case 1:
							sss = "buckshot  " + gunbase.gunInfo.pellet + " pellet";
							break;
						case 2:
							sss = "incendiary";
							break;
						case 3:
							sss = "incendiary";
							break;
					}
				}
			}
			int mode = itemstack.getTagCompound().getInteger("HMGMode");
			int bursts = gunbase.getburstCount(mode);
			if(bursts == -1){
				sss += " : full ";
			}else if(bursts == 0) {
				sss += " : safe ";
			}else if(bursts == 1){
				if(gunbase.gunInfo.needcock) {
					sss += " : one shot";
				}else{
					sss += " : semi";
				}
			}else {
				sss += " : " + gunbase.getburstCount(mode) + "burst ";
			}
			if(!gunbase.gunInfo.rates.isEmpty() && gunbase.gunInfo.rates.size()>mode) {
				if(gunbase.gunInfo.needcock) {
					sss += " : cocking time " + gunbase.gunInfo.cocktime;
				}else{
					sss += " : rate " + (int)(1200/gunbase.gunInfo.rates.get(mode));
				}
			}
			try {
				if (gunbase.gunInfo.elevationOffsets_info != null && itemstack.getTagCompound().getInteger("currentElevation")>=0 && itemstack.getTagCompound().getInteger("currentElevation")<gunbase.gunInfo.elevationOffsets_info.size())
					l2 = " Zero " + gunbase.gunInfo.elevationOffsets_info.get(itemstack.getTagCompound().getInteger("currentElevation"));
			}catch (Exception e){
				e.printStackTrace();
			}
		}

		fontrenderer.drawStringWithShadow(sss, i - fontrenderer.getStringWidth(sss)-5, j - fontrenderer.FONT_HEIGHT*3, 0xFFFFFF);
		fontrenderer.drawStringWithShadow(l2, i - fontrenderer.getStringWidth(sss)-5, j - fontrenderer.FONT_HEIGHT, 0xFFFFFF);
	}


	public static void renderPumpkinBlur(Minecraft minecraft,ResourceLocation adsr)
	{
		GL11.glPushMatrix();
		GL11.glDisable(GL_DEPTH_TEST);
		GL11.glEnable(GL_BLEND);
		GL11.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

//		GL11.glTranslatef((float)scaledresolution.getScaledWidth() / 2, (float)scaledresolution.getScaledHeight() / 2, 0.0F);
//		IAttributeInstance iattributeinstance = minecraft.thePlayer.getEntityAttribute(SharedMonsterAttributes.movementSpeed);
//		double f = ((iattributeinstance.getAttributeValue() / (double)minecraft.thePlayer.capabilities.getWalkSpeed() + 1.0D) / 2.0D);
//		double anti_fov = (0.81915204428D/*sin(55)*/ / sin(Math.toRadians(minecraft.gameSettings.fovSetting * f / 2.0F)));
//		anti_fov = anti_fov*anti_fov;
//		GL11.glScalef((float)anti_fov, (float)anti_fov, 1);
//		GL11.glTranslatef(-(float)scaledresolution.getScaledWidth() / 2, -(float)scaledresolution.getScaledHeight() / 2, 0.0F);
		minecraft.getTextureManager().bindTexture(adsr);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		float width = 40;
		float height = 20;
		float xoffset = -width/2f;
		float yoffset = -height/2f;

		for(int x = -1; x < 2 ;x++) for(int y = -1; y < 2 ;y++) {
			tessellator.addVertexWithUV(xoffset + width * x, yoffset + height * (y + 1)			, 10.0D, x < 0 ? 0 : x > 0 ? 1 : 1.0D, y < 0 ? 0 : y > 0 ? 1 : 0.0D);
			tessellator.addVertexWithUV(xoffset + width * (x + 1), yoffset + height * (y + 1)	, 10.0D, x < 0 ? 0 : x > 0 ? 1 : 0.0D, y < 0 ? 0 : y > 0 ? 1 : 0.0D);
			tessellator.addVertexWithUV(xoffset + width * (x + 1), yoffset + height * y			, 10.0D, x < 0 ? 0 : x > 0 ? 1 : 0.0D, y < 0 ? 0 : y > 0 ? 1 : 1.0D);
			tessellator.addVertexWithUV(xoffset + width * x, yoffset + height * y				, 10.0D, x < 0 ? 0 : x > 0 ? 1 : 1.0D, y < 0 ? 0 : y > 0 ? 1 : 1.0D);
		}
//		tessellator.addVertexWithUV(x + xoffset, y + yoffset + height*2			, 0.0D, 0.0D, 1.0D);
//		tessellator.addVertexWithUV(x + xoffset + width, y + yoffset + height*2	, 0.0D, 1.0D, 1.0D);
//		tessellator.addVertexWithUV(x + xoffset + width, y + yoffset + height		, 0.0D, 1.0D, 0.0D);
//		tessellator.addVertexWithUV(x + xoffset, y + yoffset + height				, 0.0D, 0.0D, 0.0D);
//
//		tessellator.addVertexWithUV(x + xoffset, y + yoffset						, 0.0D, 0.0D, 1.0D);
//		tessellator.addVertexWithUV(x + xoffset + width, y + yoffset				, 0.0D, 1.0D, 1.0D);
//		tessellator.addVertexWithUV(x + xoffset + width, y + yoffset - height		, 0.0D, 1.0D, 0.0D);
//		tessellator.addVertexWithUV(x + xoffset, y + yoffset - height				, 0.0D, 0.0D, 0.0D);

		tessellator.draw();
//		GL11.glScalef((float)(1/anti_fov), (float)(1/anti_fov), 1);
		GL11.glPopMatrix();
		GL11.glEnable(GL_DEPTH_TEST);
		GL11.glEnable(GL_ALPHA_TEST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
	}
	public static void renderPumpkinBlur(Minecraft minecraft, String adss)
	{
		renderPumpkinBlur(minecraft,new ResourceLocation(adss));
	}

	@SideOnly(Side.CLIENT)
	public static void renderLockOnMarker(Minecraft minecraft, ResourceLocation adsr,Vector3d markerPos)
	{
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		GL11.glPushMatrix();
		GL11.glDisable(2929);
		GL11.glDisable(3008);
		GL11.glEnable(3042);
		GL11.glBlendFunc(770, 771);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

//		GL11.glTranslatef((float)scaledresolution.getScaledWidth() / 2, (float)scaledresolution.getScaledHeight() / 2, 0.0F);
//		IAttributeInstance iattributeinstance = minecraft.thePlayer.getEntityAttribute(SharedMonsterAttributes.movementSpeed);
//		double currentFov = minecraft.gameSettings.fovSetting * ((iattributeinstance.getAttributeValue() / (double)minecraft.thePlayer.capabilities.getWalkSpeed() + 1.0D) / 2.0D);
//		double anti_fov = (0.81915204428/*sin(55)*/ / sin(Math.toRadians(currentFov / 2.0F)));
//		anti_fov *= anti_fov;
//		GL11.glScalef((float)anti_fov, (float)anti_fov, 1);
//		GL11.glTranslatef(-(float)scaledresolution.getScaledWidth() / 2, -(float)scaledresolution.getScaledHeight() / 2, 0.0F);//左上に戻す
		//中央のままにしておく
		GL11.glRotatef(-(float)ObfuscationReflectionHelper.getPrivateValue(EntityRenderer.class, minecraft.entityRenderer, "camRoll", "R", "field_78495_O"),0,0,1);
		GL11.glTranslatef((float)markerPos.x,(float)markerPos.y,(float)markerPos.z);
		GL11.glRotatef((float)ObfuscationReflectionHelper.getPrivateValue(EntityRenderer.class, minecraft.entityRenderer, "camRoll", "R", "field_78495_O"),0,0,1);
		minecraft.getTextureManager().bindTexture(adsr);
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV(-0.05, 0.05 , 0.0D, 1.0D, 0.0D);
		tessellator.addVertexWithUV(0.05 , 0.05 , 0.0D, 0.0D, 0.0D);
		tessellator.addVertexWithUV(0.05 , -0.05, 0.0D, 0.0D, 1.0D);
		tessellator.addVertexWithUV(-0.05, -0.05, 0.0D, 1.0D, 1.0D);

		tessellator.draw();
//		GL11.glScalef((float)(1/anti_fov), (float)(1/anti_fov), 1);
		GL11.glPopMatrix();
		GL11.glEnable(2929);
		GL11.glEnable(3008);
	}

	@SideOnly(Side.CLIENT)
	protected void renderCrossHair(Minecraft minecraft, int i, int j, float bure) {
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glPushMatrix();
		GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		GL11.glDepthMask(false);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.5F); // Adjust alpha for transparency (50% transparent)
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glAlphaFunc(GL11.GL_GREATER, 0);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA); // Standard transparency blend
		minecraft.getTextureManager().bindTexture(crosstex);

		double x = bure * 2d / 10d;
		double y = -1d / 10d;
		double widthx = 16d / 10d;
		double widthy = 2d / 10d;

		for (int cnt = 0; cnt < 4; cnt++) {
			Tessellator tessellator = Tessellator.instance;
			tessellator.startDrawingQuads();
			tessellator.addVertexWithUV(x + 0, y + widthy, 20.0D, 0.0D, 1.0D);
			tessellator.addVertexWithUV(x + widthx, y + widthy, 20.0D, 1.0D, 1.0D);
			tessellator.addVertexWithUV(x + widthx, y + 0, 20.0D, 1.0D, 0.0D);
			tessellator.addVertexWithUV(x + 0, y + 0, 20.0D, 0.0D, 0.0D);
			tessellator.draw();
			GL11.glRotatef(90, 0, 0, 1);
		}

//		tessellator.startDrawingQuads();
//		tessellator.addVertexWithUV(x + 0   ,y +widthy, 10.0D, 0.0D, 1.0D);
//		tessellator.addVertexWithUV(x+widthx,y +widthy, 10.0D, 1.0D, 1.0D);
//		tessellator.addVertexWithUV(x+widthx,y + 0, 10.0D, 1.0D, 0.0D);
//		tessellator.addVertexWithUV(x + 0   ,y + 0, 10.0D, 0.0D, 0.0D);
//		tessellator.draw();
//		GL11.glRotatef(90,0,0,1);
//		tessellator.startDrawingQuads();
//		tessellator.addVertexWithUV(x + 0   ,y +widthy, 10.0D, 0.0D, 1.0D);
//		tessellator.addVertexWithUV(x+widthx,y +widthy, 10.0D, 1.0D, 1.0D);
//		tessellator.addVertexWithUV(x+widthx,y + 0, 10.0D, 1.0D, 0.0D);
//		tessellator.addVertexWithUV(x + 0   ,y + 0, 10.0D, 0.0D, 0.0D);
//		tessellator.draw();
//		GL11.glRotatef(90,0,0,1);
//		tessellator.startDrawingQuads();
//		tessellator.addVertexWithUV(x + 0   ,y+widthy, 10.0D, 0.0D, 1.0D);
//		tessellator.addVertexWithUV(x+widthx,y +widthy, 10.0D, 1.0D, 1.0D);
//		tessellator.addVertexWithUV(x+widthx,y + 0, 10.0D, 1.0D, 0.0D);
//		tessellator.addVertexWithUV(x + 0   ,y + 0, 10.0D, 0.0D, 0.0D);
//		tessellator.draw();

		GL11.glPopMatrix();
		GL11.glPopAttrib();
		GL11.glDepthMask(true);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		bind(Gui.icons);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
	}
	private void bind(ResourceLocation res)
	{
		Minecraft.getMinecraft().getTextureManager().bindTexture(res);
	}

	public static void setUp3DView(Minecraft minecraft,float partialTicks){
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		Project.gluPerspective(HMG_proxy.getFOVModifier(minecraft,partialTicks,true), (float)minecraft.displayWidth / (float)minecraft.displayHeight, 0.01F, FMLClientHandler.instance().getClient().gameSettings.renderDistanceChunks * 16 * 2.0F);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
	}
	public static void setUp2DView(Minecraft minecraft){
		GL11.glViewport(0, 0, minecraft.displayWidth, minecraft.displayHeight);
		ScaledResolution scaledresolution = new ScaledResolution(minecraft, minecraft.displayWidth, minecraft.displayHeight);
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0.0D, scaledresolution.getScaledWidth_double(), scaledresolution.getScaledHeight_double(), 0.0D, 1000.0D, 3000.0D);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		GL11.glTranslatef(0.0F, 0.0F, -2000.0F);
	}



	//@SideOnly(Side.CLIENT)
	//@SubscribeEvent(priority = EventPriority.LOWEST) // run last so we override vanilla sprint FOV
	//public void renderfovOverride(FOVUpdateEvent event)
	//{
	//	EntityPlayer entityPlayer = event.entity;
	//	if (entityPlayer == null) return;
//
	//	// --- compute a clean base FOV (don't rely on event.newfov which other handlers may have modified) ---
	//	IAttributeInstance attr = entityPlayer.getEntityAttribute(SharedMonsterAttributes.movementSpeed);
	//	double moveSpeed = computeMoveSpeed_WithoutGunModifier((ModifiableAttributeInstance) attr);
	//	float baseFov = 1.0F;
	//	if (entityPlayer.capabilities.isFlying) baseFov *= 1.1F;
	//	float walkSpeed = entityPlayer.capabilities.getWalkSpeed();
	//	baseFov = (float)((double)baseFov * ((moveSpeed / (double)walkSpeed + 1.0D) / 2.0D));
	//	if (walkSpeed == 0.0F || Float.isNaN(baseFov) || Float.isInfinite(baseFov)) baseFov = 1.0F;
//
	//	// bow slowdown
	//	if (entityPlayer.isUsingItem() && entityPlayer.getItemInUse() != null && entityPlayer.getItemInUse().getItem() == Items.bow)
	//	{
	//		int i = entityPlayer.getItemInUseDuration();
	//		float f1 = (float)i / 20.0F;
	//		if (f1 > 1.0F) f1 = 1.0F;
	//		else f1 *= f1;
	//		baseFov *= 1.0F - f1 * 0.15F;
	//	}
//
	//	float finalFov = baseFov;
//
	//	// --- compute zoom factor from held item / placed gun (same logic as before) ---
	//	ItemStack held = entityPlayer.getCurrentEquippedItem();
	//	Entity riding = entityPlayer.ridingEntity;
	//	if (riding instanceof PlacedGunEntity)
	//	{
	//		PlacedGunEntity pge = (PlacedGunEntity) riding;
	//		if (pge.gunStack != null && pge.gunStack.getItem() instanceof HMGItem_Unified_Guns)
	//		{
	//			held = pge.gunStack;
	//		}
	//	}
//
	//	float zoomFactor = 1.0f;
	//	float newZoomLevel = currentZoomLevel; // default keep old
//
	//	if (held != null && held.getItem() instanceof HMGItem_Unified_Guns)
	//	{
	//		HMGItem_Unified_Guns gunbase = (HMGItem_Unified_Guns) held.getItem();
//
	//		// Only apply zoom when ADS is active (and not forcing sprint to cancel it)
	//		if (firstPerson_ADSState && !entityPlayer.isSprinting())
	//		{
	//			// read sight safely
	//			ItemStack sight = null;
	//			if (held.hasTagCompound() && held.getTagCompound().hasKey("Items"))
	//			{
	//				NBTTagList tags = (NBTTagList) held.getTagCompound().getTag("Items");
	//				if (tags != null)
	//				{
	//					ItemStack[] items = new ItemStack[6];
	//					int loopCount = Math.min(tags.tagCount(), 7);
	//					for (int i = 0; i < loopCount; i++)
	//					{
	//						NBTTagCompound tagCompound = tags.getCompoundTagAt(i);
	//						int slot = tagCompound.getByte("Slot");
	//						if (slot >= 0 && slot < items.length)
	//						{
	//							items[slot] = ItemStack.loadItemStackFromNBT(tagCompound);
	//						}
	//					}
	//					sight = items[1];
	//				}
	//			}
//
	//			if (sight != null)
	//			{
	//				if (sight.getItem() instanceof HMGItemAttachment_reddot)
	//				{
	//					if (gunbase.gunInfo.canobj && gunbase.gunInfo.zoomrer && !isentitysprinting(entityPlayer)) {
	//						zoomFactor = gunbase.gunInfo.scopezoomred;
	//						newZoomLevel = gunbase.gunInfo.scopezoomred; }
	//				}
	//				else if (sight.getItem() instanceof HMGItemAttachment_scope)
	//				{
	//					if (gunbase.gunInfo.canobj && gunbase.gunInfo.zoomres && !isentitysprinting(entityPlayer)) {
	//						zoomFactor = gunbase.gunInfo.scopezoomscope;
	//						newZoomLevel = gunbase.gunInfo.scopezoomscope;
	//					}
	//				}
	//				else if (sight.getItem() instanceof HMGItemSightBase)
	//				{
	//					if (gunbase.gunInfo.canobj && !((HMGItemSightBase) sight.getItem()).scopeonly && !isentitysprinting(entityPlayer))
	//					{
	//						zoomFactor = ((HMGItemSightBase) sight.getItem()).zoomlevel;
	//						newZoomLevel = ((HMGItemSightBase) sight.getItem()).zoomlevel;
	//					}
	//				}
	//				else
	//				{
	//					if (gunbase.gunInfo.canobj && gunbase.gunInfo.zoomren && !isentitysprinting(entityPlayer)) {
//
	//						zoomFactor = gunbase.gunInfo.scopezoombase;
	//						newZoomLevel = gunbase.gunInfo.scopezoombase;
	//					}
	//				}
	//			}
	//			else
	//			{
	//				if (gunbase.gunInfo.canobj && gunbase.gunInfo.zoomren && !isentitysprinting(entityPlayer)) {
	//					zoomFactor = gunbase.gunInfo.scopezoombase;
	//					newZoomLevel = gunbase.gunInfo.scopezoombase; }
	//			}
	//		} // end ADS check
//
	//		// keep previous flying compensation behavior if desired
	//		if (entityPlayer.capabilities.isFlying)
	//		{
	//			finalFov /= 1.1F;
	//		}
	//	} // end held check
//
	//	// Protect against division by zero
	//	if (zoomFactor <= 0.00001F) zoomFactor = 1.0F;
//
	//	// --- IMPORTANT: override event.newfov here (this runs last because of LOWEST priority) ---
	//	if (firstPerson_ADSState && prevADSState)
	//	{
	//		event.newfov = finalFov / zoomFactor;
	//		// update currentZoomLevel only if changed
	//		if (newZoomLevel != currentZoomLevel) currentZoomLevel = newZoomLevel;
	//	}
	//	else
	//	{
	//		// not ADS: ensure no leftover zoom remains
	//		// leave event.newfov alone (so vanilla sprint/normal FOV applies)
	//	}
	//}

}
