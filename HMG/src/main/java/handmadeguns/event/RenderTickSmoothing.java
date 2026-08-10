package handmadeguns.event;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.FMLLog;
import handmadeguns.HandmadeGunsCore;
import handmadeguns.entity.HMGEntityParticles;
import handmadeguns.client.render.HMGRenderItemGun_U;
import handmadeguns.client.render.HMGRenderItemGun_U_NEW;
import handmadeguns.items.guns.HMGItem_Unified_Guns;
import handmadeguns.compat.HMGRecoilBridge;
import handmadeguns.compat.HMGAimRecoilController;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;

import java.util.Random;

import static handmadeguns.HandmadeGunsCore.HMG_proxy;
import static handmadeguns.HandmadeGunsCore.cfg_Sneak_ByADSKey;
import static handmadeguns.client.render.HMGRenderItemGun_U_NEW.*;
import static handmadeguns.event.HMGEventZoom.currentZoomLevel;
import static handmadevehicle.HMVehicle.HMV_Proxy;
import static org.lwjgl.opengl.EXTFramebufferObject.*;
import static org.lwjgl.opengl.GL11.*;

public class RenderTickSmoothing {

	public static boolean test_ReCreate = false;

	//todo onRenderTickStartでマウス感度を下げ、onRenderTickEndで復帰させればズーム時の照準が楽になるだろう

	public static float backUppedMouseSensitivity = -1;
	private static Item lastWeightDebugItem;



	public static int currentFBO = -1;
	public static int currentRenderBuffer = -1;
	public static int currentTextureBuffer = -1;
	public static int currentStencilBufferID = -1;
	private static final int GL_FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE_EXT = 0x8CD0;
	private static final int GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME_EXT = 0x8CD1;
	private static final int GL_DEPTH_STENCIL_ATTACHMENT_EXT = 0x821A;
	private static final int GL_DEPTH_STENCIL = 0x84F9;
	private static final int GL_DEPTH24_STENCIL8_EXT = 0x88F0;
	private static final int GL_DEPTH32F_STENCIL8 = 0x8CAD;
	private static float pendingRecoilPitch = 0.0f;
	private static float pendingRecoilYaw = 0.0f;
	private static float recoilVelocityPitch = 0.0f;
	private static float recoilVelocityYaw = 0.0f;
	private static final float RECOIL_INITIAL_IMPULSE = 0.65f;
	private static final float RECOIL_SPRING = 0.62f;
	private static final float RECOIL_DAMPING = 0.74f;
	private static final float HORIZONTAL_RECOIL_RATIO = 0.15f;
	private static final Random RECOIL_RANDOM = new Random();
	private static int lastRecoilWeaponKey = 0;
	private static int lastRecoilDimension = Integer.MIN_VALUE;

	public static void addSmoothRecoilPitch(float recoilAmount)
	{
		addSmoothRecoil(recoilAmount);
	}

	public static void addSmoothRecoil(float recoilPitchAmount)
	{
		float horizontalSign = RECOIL_RANDOM.nextBoolean() ? 1.0f : -1.0f;
		float recoilYawAmount = recoilPitchAmount * HORIZONTAL_RECOIL_RATIO * horizontalSign;
		pendingRecoilPitch += recoilPitchAmount;
		pendingRecoilYaw += recoilYawAmount;
	}

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
				ItemStack heldGun = getHeldUnifiedGun();
				boolean weightApplies = canApplyHeldGunWeight(heldGun);
				float weightSensitivityMultiplier = weightApplies ? getHeldGunWeightSensitivityMultiplier(heldGun) : 1.0F;
				float activeZoomMultiplier = currentZoomLevel == 0.0F ? 1.0F : 1.0F / currentZoomLevel;
				if(currentZoomLevel != 1 || weightSensitivityMultiplier != 1.0F) {
					backUppedMouseSensitivity = HMG_proxy.getMCInstance().gameSettings.mouseSensitivity;
					float zoomedSensitivity = backUppedMouseSensitivity * activeZoomMultiplier;
					HMG_proxy.getMCInstance().gameSettings.mouseSensitivity =
							applyCameraGainMultiplier(zoomedSensitivity, weightSensitivityMultiplier);
				}else {
					backUppedMouseSensitivity = -1;
				}
				logWeightSensitivityOnHeldGunChange(heldGun, weightSensitivityMultiplier,
						backUppedMouseSensitivity == -1 ? HMG_proxy.getMCInstance().gameSettings.mouseSensitivity : backUppedMouseSensitivity,
						HMG_proxy.getMCInstance().gameSettings.mouseSensitivity, activeZoomMultiplier, event.phase);
				currentZoomLevel = 1;

				ensureStencilBufferAvailable();

			break;
			case END :
				if(backUppedMouseSensitivity != -1) {
					HMG_proxy.getMCInstance().gameSettings.mouseSensitivity = backUppedMouseSensitivity;
				}
				break;
		}
	}

	private static ItemStack getHeldUnifiedGun()
	{
		if (HMG_proxy.getMCInstance().thePlayer == null) return null;
		ItemStack held = HMG_proxy.getMCInstance().thePlayer.getCurrentEquippedItem();
		return held != null && held.getItem() instanceof HMGItem_Unified_Guns ? held : null;
	}

	private static boolean canApplyHeldGunWeight(ItemStack held)
	{
		return held != null && HMG_proxy.getMCInstance().currentScreen == null
				&& HMG_proxy.getMCInstance().thePlayer.ridingEntity == null;
	}

	private static float getHeldGunWeightSensitivityMultiplier(ItemStack held)
	{
		if (held == null) return 1.0F;
		if (HandmadeGunsCore.enableWeightSensitivityDiagnostics
				&& HandmadeGunsCore.forceWeightSensitivityDiagnostic) return 0.10F;
		return ((HMGItem_Unified_Guns) held.getItem()).gunInfo.getWeightSensitivityMultiplier();
	}

	/** Convert a desired camera-gain multiplier back to Minecraft's nonlinear sensitivity slider. */
	static float applyCameraGainMultiplier(float sensitivity, float cameraMultiplier)
	{
		if (cameraMultiplier == 1.0F) return sensitivity;
		double base = sensitivity * 0.6D + 0.2D;
		double adjusted = Math.cbrt(base * base * base * Math.max(0.0F, cameraMultiplier));
		return (float) Math.max(0.0D, Math.min(1.0D, (adjusted - 0.2D) / 0.6D));
	}

	private static void logWeightSensitivityOnHeldGunChange(ItemStack held, float multiplier,
			float originalSensitivity, float appliedSensitivity, float zoomMultiplier, TickEvent.Phase phase)
	{
		if (!HandmadeGunsCore.enableWeightSensitivityDiagnostics) return;
		Item heldItem = held == null ? null : held.getItem();
		if (heldItem == lastWeightDebugItem) return;
		lastWeightDebugItem = heldItem;
		if (!(heldItem instanceof HMGItem_Unified_Guns)) {
			System.out.println("[HMG weight sensitivity] held gun: none");
			return;
		}
		HMGItem_Unified_Guns gun = (HMGItem_Unified_Guns) heldItem;
		Object registryName = Item.itemRegistry.getNameForObject(heldItem);
		boolean riding = HMG_proxy.getMCInstance().thePlayer.ridingEntity != null;
		String message = String.format("[HMG weight sensitivity] registry=%s, unlocalized=%s, source=%s, weightConfigured=%s, Weight=%s, multiplier=%.4f, before=%.4f, during=%.4f, zoomMultiplier=%.4f, phase=%s, riding=%s",
				registryName, heldItem.getUnlocalizedName(), gun.gunInfo.sourceGunDefinition,
				gun.gunInfo.weightConfigured, Double.toString(gun.gunInfo.weight), multiplier,
				originalSensitivity, appliedSensitivity, zoomMultiplier, phase, riding);
		System.out.println(message);
		System.err.println(message);
		FMLLog.info(message);
		if (HandmadeGunsCore.showWeightSensitivityDiagnosticsOnScreen) {
			HMG_proxy.getMCInstance().ingameGUI.getChatGUI().printChatMessage(new ChatComponentText(message));
		}
	}

	//todo: figure out why some guns decide to continue to be in sprint state while firing
	/**
	 * Ensure the framebuffer currently used by HMG reticle rendering has a stencil attachment.
	 *
	 * <p>The old path reallocated whatever depth texture happened to be attached as
	 * {@code GL_DEPTH_STENCIL}. That mirrors the kind of global framebuffer mutation older
	 * renderer stacks tolerated, but it is unsafe with Angelica/Iris because those renderers track and
	 * reuse the main depth texture. Angelica's framebuffer code instead treats stencil as part of
	 * an already-combined depth/stencil attachment and binds that attachment atomically. We follow
	 * that model here: reuse an existing combined depth-stencil texture when present, otherwise
	 * leave the framebuffer untouched and let the caller avoid stencil-only rendering.
	 */
	public static boolean ensureStencilBufferAvailable()
	{
		if (currentFBO == -1) {
			return false;
		}

		int previousFramebuffer = glGetInteger(GL_FRAMEBUFFER_BINDING_EXT);
		int previousTexture = glGetInteger(GL_TEXTURE_BINDING_2D);
		try {
			glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, currentFBO);

			int stencilAttachment = getFramebufferAttachmentName(GL_STENCIL_ATTACHMENT_EXT);
			if (stencilAttachment != 0) {
				currentStencilBufferID = stencilAttachment;
				return true;
			}

			int depthAttachment = getFramebufferAttachmentName(GL_DEPTH_ATTACHMENT_EXT);
			int depthAttachmentType = glGetFramebufferAttachmentParameteriEXT(
					GL_FRAMEBUFFER_EXT,
					GL_DEPTH_ATTACHMENT_EXT,
					GL_FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE_EXT);
			if (depthAttachment <= 0 || depthAttachmentType != GL_TEXTURE) {
				currentStencilBufferID = 0;
				return false;
			}

			glBindTexture(GL_TEXTURE_2D, depthAttachment);
			int internalFormat = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_INTERNAL_FORMAT);
			if (!isCombinedDepthStencilFormat(internalFormat)) {
				currentStencilBufferID = 0;
				return false;
			}

			glFramebufferTexture2DEXT(
					GL_FRAMEBUFFER_EXT,
					GL_DEPTH_STENCIL_ATTACHMENT_EXT,
					GL_TEXTURE_2D,
					depthAttachment,
					0);

			if (glCheckFramebufferStatusEXT(GL_FRAMEBUFFER_EXT) != GL_FRAMEBUFFER_COMPLETE_EXT) {
				glFramebufferTexture2DEXT(
						GL_FRAMEBUFFER_EXT,
						GL_DEPTH_STENCIL_ATTACHMENT_EXT,
						GL_TEXTURE_2D,
						0,
						0);
				currentStencilBufferID = 0;
				return false;
			}

			currentStencilBufferID = getFramebufferAttachmentName(GL_STENCIL_ATTACHMENT_EXT);
			return currentStencilBufferID != 0;
		}
		finally {
			glBindTexture(GL_TEXTURE_2D, previousTexture);
			glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, previousFramebuffer);
		}
	}

	private static int getFramebufferAttachmentName(int attachment)
	{
		return glGetFramebufferAttachmentParameteriEXT(
				GL_FRAMEBUFFER_EXT,
				attachment,
				GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME_EXT);
	}

	private static boolean isCombinedDepthStencilFormat(int internalFormat)
	{
		return internalFormat == GL_DEPTH_STENCIL
				|| internalFormat == GL_DEPTH24_STENCIL8_EXT
				|| internalFormat == GL_DEPTH32F_STENCIL8;
	}

	@SubscribeEvent
	public void clientTickEvent(TickEvent.ClientTickEvent event)
	{
		if (event.phase != TickEvent.Phase.START) return;

		if (HMG_proxy.getEntityPlayerInstance() == null) return;
		EntityPlayer entityPlayer = HMG_proxy.getEntityPlayerInstance();
		ItemStack held = entityPlayer.getCurrentEquippedItem();
		resetCombativesRecoilStateIfNeeded(entityPlayer, held);
		HMGAimRecoilController.onClientTick(entityPlayer);
		applySmoothRecoil(entityPlayer);

		// --------------------------------------------------
		// Reload state
		// --------------------------------------------------
		prevReloadState = firstPerson_ReloadState;
		firstPerson_ReloadState = false;

		if (held != null && held.getItem() instanceof HMGItem_Unified_Guns && held.hasTagCompound())
		{
			((HMGItem_Unified_Guns) held.getItem()).checkTags(held);
			NBTTagCompound tag = held.getTagCompound();
			if (tag != null && tag.hasKey("IsReloading"))
			{
				firstPerson_ReloadState = tag.getBoolean("IsReloading");
			}
		}
		// --------------------------------------------------
		// Firing / trigger state (authoritative)
		// --------------------------------------------------
		boolean isTriggered = false;

		if (held != null && held.getItem() instanceof HMGItem_Unified_Guns && held.hasTagCompound())
		{
			NBTTagCompound tag = held.getTagCompound();
			if (tag != null && tag.hasKey("IsTriggered"))
			{
				isTriggered = tag.getBoolean("IsTriggered");
			}
		}
		HMGRecoilBridge.onClientTick(entityPlayer, held, isTriggered, firstPerson_ReloadState);

		// --------------------------------------------------
		// Sprint state (HARD gated by reload + trigger)
		// --------------------------------------------------
		prevSprintState = firstPerson_SprintState;

		if (!firstPerson_ReloadState
				&& !isTriggered
				&& held != null
				&& held.getItem() instanceof HMGItem_Unified_Guns
				&& held.hasTagCompound()
				&& entityPlayer != null)
		{
			NBTTagCompound nbt = held.getTagCompound();
			if (nbt != null)
			{
				firstPerson_SprintState =
						isentitysprinting(entityPlayer) && !nbt.getBoolean("IsTriggered");
			}
			else
			{
				firstPerson_SprintState = false;
			}
		}
		else
		{
			firstPerson_SprintState = false;

			//// prevent sticky sprint while firing or reloading
			////this fires when it shouldn't?
			//if (entityPlayer != null && entityPlayer.isSprinting() && entityPlayer.getHeldItem() != null
			//		&& entityPlayer.getHeldItem().getItem() instanceof HMGItem_Unified_Guns ); //&& entityPlayer.getHeldItem() instanceof HMGItem_Unified_Guns can't be done because incompatible types
			//{
			//	entityPlayer.setSprinting(false);
			//}
			//just don't
		}


		// --------------------------------------------------
		// ADS state (TOGGLE — edge triggered)
		// --------------------------------------------------
		prevADSState = firstPerson_ADSState;

		boolean desiredADS = false;

		// ADS allowed unless reloading (trigger does NOT force ADS off)
		if (!firstPerson_ReloadState)
		{
			desiredADS = HandmadeGunsCore.Key_ADS(entityPlayer);
		}

		firstPerson_ADSState = desiredADS;

		// Toggle zoom ONLY on press edge
		if (firstPerson_ADSState && !prevADSState)
		{
			HMV_Proxy.zoomclick();
		}

		// --------------------------------------------------
		// Optional: sneak while ADS (no sprint forcing)
		// --------------------------------------------------
		//if (firstPerson_ADSState && cfg_Sneak_ByADSKey)
		//{
		//	if (entityPlayer.ridingEntity == null
		//			&& held != null
		//			&& held.getItem() instanceof HMGItem_Unified_Guns
		//			&& !isentitysprinting(entityPlayer))
		//	{
		//		if (entityPlayer instanceof EntityClientPlayerMP)
		//		{
		//			((EntityClientPlayerMP) entityPlayer).movementInput.sneak = true;
		//		}
		//	}
		//}
	}

	private void resetCombativesRecoilStateIfNeeded(EntityPlayer entityPlayer, ItemStack held)
	{
		int dimension = entityPlayer.worldObj == null ? Integer.MIN_VALUE : entityPlayer.worldObj.provider.dimensionId;
		int weaponKey = held == null || held.getItem() == null ? 0 : System.identityHashCode(held.getItem()) * 31 + held.getItemDamage();
		if (!entityPlayer.isEntityAlive() || dimension != lastRecoilDimension || weaponKey != lastRecoilWeaponKey) {
			HMGRecoilBridge.resetWeaponState();
			if (!entityPlayer.isEntityAlive() || dimension != lastRecoilDimension) {
				HMGAimRecoilController.reset("player-death-or-dimension-change");
			} else if (weaponKey != lastRecoilWeaponKey) {
				HMGAimRecoilController.resetWeaponBuildUp();
			}
			lastRecoilDimension = dimension;
			lastRecoilWeaponKey = weaponKey;
		}
	}

	public static void clearPendingLegacyRecoil()
	{
		pendingRecoilPitch = 0.0f;
		pendingRecoilYaw = 0.0f;
		recoilVelocityPitch = 0.0f;
		recoilVelocityYaw = 0.0f;
	}

	private void applySmoothRecoil(EntityPlayer entityPlayer)
	{
		if (entityPlayer == null) return;
		if (pendingRecoilPitch == 0.0f && recoilVelocityPitch == 0.0f
				&& pendingRecoilYaw == 0.0f && recoilVelocityYaw == 0.0f) return;

		float immediatePitch = pendingRecoilPitch * RECOIL_INITIAL_IMPULSE;
		float immediateYaw = pendingRecoilYaw * RECOIL_INITIAL_IMPULSE;
		float delayedPitch = pendingRecoilPitch - immediatePitch;
		float delayedYaw = pendingRecoilYaw - immediateYaw;

		recoilVelocityPitch += delayedPitch;
		recoilVelocityYaw += delayedYaw;
		pendingRecoilPitch = 0.0f;
		pendingRecoilYaw = 0.0f;

		float recoilStepPitch = immediatePitch + recoilVelocityPitch * RECOIL_SPRING;
		float recoilStepYaw = immediateYaw + recoilVelocityYaw * RECOIL_SPRING;
		recoilVelocityPitch *= RECOIL_DAMPING;
		recoilVelocityYaw *= RECOIL_DAMPING;

		if (Math.abs(recoilVelocityPitch) < 0.001f) {
			recoilVelocityPitch = 0.0f;
		}
		if (Math.abs(recoilVelocityYaw) < 0.001f) {
			recoilVelocityYaw = 0.0f;
		}

		entityPlayer.rotationPitch -= recoilStepPitch;
		entityPlayer.rotationYaw += recoilStepYaw;
	}


}
