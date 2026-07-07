package handmadeguns.client.camera;

import handmadeguns.HandmadeGunsCore;
import handmadeguns.entity.PlacedGunEntity;
import handmadeguns.items.HMGItemAttachment_reddot;
import handmadeguns.items.HMGItemAttachment_scope;
import handmadeguns.items.HMGItemSightBase;
import handmadeguns.items.guns.HMGItem_Unified_Guns;
import handmadeguns.event.HMGEventZoom;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

public final class OverdriveCameraController {
    private static final float ROTATION_SMOOTHING = 0.18F;
    private static final float MAX_YAW_OFFSET = 3.0F;
    private static final float MAX_PITCH_OFFSET = 3.0F;
    private static final float MAX_ROLL = 4.0F;
    private static final float MAX_MOVEMENT_PITCH = 3.0F;
    private static final float MOVEMENT_DEADZONE = 0.03F;
    private static final float MOVEMENT_TRANSITION_SPEED = 0.16F;
    private static final float MOVEMENT_ACCEL_LIMIT = 0.10F;
    private static final float LEAN_ACCELERATION = 0.055F;
    private static final float LEAN_DAMPING = 0.78F;
    private static final float LEAN_MAX_VELOCITY = 0.22F;
    private static final float DIRECTION_TRANSITION_HALF_LIFE = 5.5F;
    private static final float MAX_OFFSET_CHANGE = 0.45F;
    private static final float STEP_BOB_STRENGTH = 0.72F;
    private static final float STEP_BOB_SPEED = 11.5F;
    private static final float SPRINT_STEP_MULTIPLIER = 1.35F;
    private static final float ADS_BOB_MULTIPLIER = 0.20F;
    private static final float FOV_LERP_SPEED = 0.12F;
    private static final float SPRINT_FOV_BOOST = 3.0F;
    private static final float ADS_FOV_SPEED = 0.22F;
    private static final float SCOPE_ZOOM_IN_SPEED = 0.18F;
    private static final float SCOPE_ZOOM_OUT_SPEED = 0.13F;
    private static final float SCOPE_ZOOM_OVERSHOOT = 0.018F;
    private static final float SCOPE_BREATH_PITCH = 0.11F;
    private static final float SCOPE_BREATH_YAW = 0.075F;
    private static final float MAX_SHAKE_PITCH = 7.0F;
    private static final float MAX_SHAKE_YAW = 4.0F;
    private static final float MAX_SHAKE_ROLL = 6.0F;
    private static final float SHAKE_DECAY_SPEED = 0.18F;
    private static final float RECOIL_FIRST_SHOT_MULTIPLIER = 1.45F;
    private static final float RECOIL_SUSTAINED_MULTIPLIER = 0.23F;
    private static final float RECOIL_HORIZONTAL_WANDER = 0.42F;
    private static final float RECOIL_PATTERN_STRENGTH = 0.28F;
    private static final float RECOIL_CAMERA_PUNCH = 0.16F;
    private static final float RECOIL_RECOVERY_DELAY_TICKS = 3.0F;
    private static final float RECOIL_RECOVERY_SPEED = 0.22F;
    private static final float RECOIL_MAX_ACCUMULATED_PITCH = 9.0F;
    private static final float RECOIL_MAX_ACCUMULATED_YAW = 3.0F;
    private static final float EXPLOSION_SHAKE_MULTIPLIER = 1.18F;
    private static final float EXPLOSION_INITIAL_PUNCH = 1.35F;
    private static final float LANDING_SHAKE_MULTIPLIER = 0.8F;
    private static final float DAMAGE_SHAKE_MULTIPLIER = 0.7F;

    private static float yawOffset, pitchOffset, rollOffset, movementPitch, bobPhase, fovOffset, scopeZoomOffset, scopeBreathPhase;
    private static float leanRollVelocity, leanPitchVelocity;
    private static float shakePitch, shakeYaw, shakeRoll;
    private static float recoilShake, explosionShake, landingShake, damageShake;
    private static float smoothedForward, smoothedStrafe, smoothedVertical;
    private static float shakeTime, explosionPhase, smoothedSpeed, sprintBlend;
    private static float recoilPitch, recoilYaw, recoilVisualPitch, recoilVisualYaw, recoilVisualRoll;
    private static int recoilBurstShots;
    private static float recoilCooldown;
    private static int recoilSeed = 0x51F15EED;
    private static double lastMotionY;
    private static boolean wasOnGround = true;
    private static int lastHurtTime;

    private OverdriveCameraController() {}

    public static void initClient() { reset(); }

    public static void reset() {
        yawOffset = pitchOffset = rollOffset = movementPitch = bobPhase = fovOffset = scopeZoomOffset = scopeBreathPhase = 0.0F;
        leanRollVelocity = leanPitchVelocity = 0.0F;
        shakePitch = shakeYaw = shakeRoll = 0.0F;
        recoilShake = explosionShake = landingShake = damageShake = 0.0F;
        smoothedForward = smoothedStrafe = smoothedVertical = smoothedSpeed = sprintBlend = 0.0F;
        shakeTime = explosionPhase = 0.0F;
        recoilPitch = recoilYaw = recoilVisualPitch = recoilVisualYaw = recoilVisualRoll = 0.0F;
        recoilBurstShots = 0;
        recoilCooldown = 0.0F;
        lastMotionY = 0.0D;
        wasOnGround = true;
        lastHurtTime = 0;
    }

    public static void update(Minecraft mc, float partialTicks) {
        if (!HandmadeGunsCore.cfg_ClientCamera_MasterEnabled || mc == null || mc.theWorld == null || mc.thePlayer == null) {
            reset();
            return;
        }
        AngelicaCameraCompat.apply(mc);
        EntityLivingBase player = mc.thePlayer;
        float smooth = ROTATION_SMOOTHING;
        if (HandmadeGunsCore.cfg_ClientCamera_RotationSmoothingEnabled) {
            float yawDelta = MathHelper.wrapAngleTo180_float(player.rotationYaw - player.prevRotationYaw);
            float pitchDelta = player.rotationPitch - player.prevRotationPitch;
            yawOffset = approachLimited(yawOffset, clamp(-yawDelta * 0.35F, -MAX_YAW_OFFSET, MAX_YAW_OFFSET), smooth, MAX_OFFSET_CHANGE);
            pitchOffset = approachLimited(pitchOffset, clamp(-pitchDelta * 0.25F, -MAX_PITCH_OFFSET, MAX_PITCH_OFFSET), smooth, MAX_OFFSET_CHANGE);
        } else {
            yawOffset = approachLimited(yawOffset, 0.0F, 0.3F, MAX_OFFSET_CHANGE);
            pitchOffset = approachLimited(pitchOffset, 0.0F, 0.3F, MAX_OFFSET_CHANGE);
        }

        double interpX = player.prevPosX + (player.posX - player.prevPosX) * partialTicks;
        double interpY = player.prevPosY + (player.posY - player.prevPosY) * partialTicks;
        double interpZ = player.prevPosZ + (player.posZ - player.prevPosZ) * partialTicks;
        double prevInterpX = player.lastTickPosX + (player.prevPosX - player.lastTickPosX) * partialTicks;
        double prevInterpY = player.lastTickPosY + (player.prevPosY - player.lastTickPosY) * partialTicks;
        double prevInterpZ = player.lastTickPosZ + (player.prevPosZ - player.lastTickPosZ) * partialTicks;
        double dx = interpX - prevInterpX;
        double dy = interpY - prevInterpY;
        double dz = interpZ - prevInterpZ;

        float inputForward = 0.0F;
        float inputStrafe = 0.0F;
        if (player instanceof EntityPlayerSP && ((EntityPlayerSP) player).movementInput != null) {
            inputForward = ((EntityPlayerSP) player).movementInput.moveForward;
            inputStrafe = ((EntityPlayerSP) player).movementInput.moveStrafe;
        } else {
            inputForward = MathHelper.sin(player.rotationYaw * 0.017453292F) * (float) dx - MathHelper.cos(player.rotationYaw * 0.017453292F) * (float) dz;
            inputStrafe = MathHelper.cos(player.rotationYaw * 0.017453292F) * (float) dx + MathHelper.sin(player.rotationYaw * 0.017453292F) * (float) dz;
        }
        inputForward = applyDeadzone(inputForward, MOVEMENT_DEADZONE);
        inputStrafe = applyDeadzone(inputStrafe, MOVEMENT_DEADZONE);
        float vertical = applyDeadzone((float) dy, MOVEMENT_DEADZONE * 0.25F);
        float directionFactor = halfLifeFactor(DIRECTION_TRANSITION_HALF_LIFE);
        smoothedForward = approachLimited(smoothedForward, inputForward, directionFactor, MOVEMENT_ACCEL_LIMIT);
        smoothedStrafe = approachLimited(smoothedStrafe, inputStrafe, directionFactor, MOVEMENT_ACCEL_LIMIT);
        smoothedVertical = approachLimited(smoothedVertical, vertical, MOVEMENT_TRANSITION_SPEED, 0.04F);

        if (HandmadeGunsCore.cfg_ClientCamera_MotionTiltEnabled) {
            float targetRoll = clamp(-smoothedStrafe * MAX_ROLL, -MAX_ROLL, MAX_ROLL);
            float targetPitch = clamp(-smoothedForward * MAX_MOVEMENT_PITCH - smoothedVertical * 24.0F, -MAX_MOVEMENT_PITCH, MAX_MOVEMENT_PITCH);
            rollOffset = springToward(rollOffset, targetRoll, true);
            movementPitch = springToward(movementPitch, targetPitch, false);
        } else {
            rollOffset = approachLimited(rollOffset, 0.0F, 0.3F, MAX_OFFSET_CHANGE);
            movementPitch = approachLimited(movementPitch, 0.0F, 0.3F, MAX_OFFSET_CHANGE);
        }

        float speed = MathHelper.sqrt_double(dx * dx + dz * dz);
        smoothedSpeed = approachLimited(smoothedSpeed, speed, 0.28F, 0.025F);
        sprintBlend = approachLimited(sprintBlend, player.isSprinting() ? 1.0F : 0.0F, 0.14F, 0.08F);
        float inputMagnitude = clamp01(MathHelper.sqrt_float(smoothedForward * smoothedForward + smoothedStrafe * smoothedStrafe));
        bobPhase += smoothedSpeed * STEP_BOB_SPEED * (1.0F + (SPRINT_STEP_MULTIPLIER - 1.0F) * sprintBlend) * (0.35F + inputMagnitude * 0.65F);
        scopeBreathPhase += 0.045F + smoothedSpeed * 0.035F;
        updateRecoilRecovery();
        if (!wasOnGround && player.onGround && lastMotionY < -0.35D) addLandingShake((float) Math.min(1.5D, -lastMotionY));
        wasOnGround = player.onGround;
        if (player.hurtTime > lastHurtTime) addDamageShake(1.0F);
        lastHurtTime = player.hurtTime;
        lastMotionY = player.motionY;
        updateShake(partialTicks);
    }

    public static void applyCameraRotations() {
        if (!HandmadeGunsCore.cfg_ClientCamera_MasterEnabled) return;
        float[] breath = getScopeBreathing();
        GL11.glRotatef(clamp(pitchOffset + movementPitch + recoilVisualPitch + shakePitch + breath[0], -MAX_PITCH_OFFSET - MAX_SHAKE_PITCH - RECOIL_MAX_ACCUMULATED_PITCH, MAX_PITCH_OFFSET + MAX_SHAKE_PITCH + RECOIL_MAX_ACCUMULATED_PITCH), 1.0F, 0.0F, 0.0F);
        GL11.glRotatef(clamp(yawOffset + recoilVisualYaw + shakeYaw + breath[1], -MAX_YAW_OFFSET - MAX_SHAKE_YAW - RECOIL_MAX_ACCUMULATED_YAW, MAX_YAW_OFFSET + MAX_SHAKE_YAW + RECOIL_MAX_ACCUMULATED_YAW), 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(clamp(rollOffset + recoilVisualRoll + shakeRoll, -MAX_ROLL - MAX_SHAKE_ROLL, MAX_ROLL + MAX_SHAKE_ROLL), 0.0F, 0.0F, 1.0F);
    }

    public static void applyCustomBob(float partialTicks) {
        if (!HandmadeGunsCore.cfg_ClientCamera_MasterEnabled || !HandmadeGunsCore.cfg_ClientCamera_CustomBobEnabled) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || !(mc.renderViewEntity instanceof EntityPlayerSP)) return;

        EntityPlayerSP player = (EntityPlayerSP) mc.renderViewEntity;
        float walkedDelta = player.distanceWalkedModified - player.prevDistanceWalkedModified;
        float walkPhase = -(player.distanceWalkedModified + walkedDelta * partialTicks);
        float cameraYaw = player.prevCameraYaw + (player.cameraYaw - player.prevCameraYaw) * partialTicks;
        float cameraPitch = player.prevCameraPitch + (player.cameraPitch - player.prevCameraPitch) * partialTicks;
        float adsScale = isAdsDown() ? ADS_BOB_MULTIPLIER : 1.0F;

        // Angelica/NotFine does not invent a new waveform: its bob modes gate vanilla
        // setupViewBobbing between camera and hand rendering.  Keep HMG as the owner
        // of the transform, but reproduce that vanilla/Angelica waveform here so the
        // camera feels like the reference without re-enabling Angelica bobbing.
        cameraYaw *= adsScale;
        cameraPitch *= adsScale;
        GL11.glTranslatef(MathHelper.sin(walkPhase * (float)Math.PI) * cameraYaw * 0.5F, -Math.abs(MathHelper.cos(walkPhase * (float)Math.PI) * cameraYaw), 0.0F);
        GL11.glRotatef(MathHelper.sin(walkPhase * (float)Math.PI) * cameraYaw * 3.0F, 0.0F, 0.0F, 1.0F);
        GL11.glRotatef(Math.abs(MathHelper.cos(walkPhase * (float)Math.PI - 0.2F) * cameraYaw) * 5.0F, 1.0F, 0.0F, 0.0F);
        GL11.glRotatef(cameraPitch, 1.0F, 0.0F, 0.0F);
    }

    public static float modifyFov(float baseFov, float partialTicks) {
        if (!HandmadeGunsCore.cfg_ClientCamera_MasterEnabled || !HandmadeGunsCore.cfg_ClientCamera_FovInertiaEnabled) return baseFov;
        Minecraft mc = Minecraft.getMinecraft();
        float target = 0.0F;
        if (mc != null && mc.thePlayer != null && mc.thePlayer.isSprinting() && getScopeZoomFactor(mc) <= 1.0001F) target += SPRINT_FOV_BOOST;
        float zoom = getScopeZoomFactor(mc);
        HMGEventZoom.currentZoomLevel = zoom;
        float zoomTarget = zoom > 1.0001F ? baseFov / zoom - baseFov : 0.0F;
        float speed = zoomTarget < scopeZoomOffset ? SCOPE_ZOOM_IN_SPEED : SCOPE_ZOOM_OUT_SPEED;
        scopeZoomOffset = approach(scopeZoomOffset, zoomTarget, clamp01(speed));
        if (zoomTarget < 0.0F && Math.abs(scopeZoomOffset - zoomTarget) < 0.7F) {
            scopeZoomOffset += MathHelper.sin(scopeBreathPhase * 0.7F) * SCOPE_ZOOM_OVERSHOOT;
        }
        float speedFov = isAdsDown() ? ADS_FOV_SPEED : FOV_LERP_SPEED;
        fovOffset = approach(fovOffset, target, clamp01(speedFov));
        return baseFov + fovOffset + scopeZoomOffset;
    }

    public static void applyHurtCamera(float partialTicks) { applyCameraRotations(); }
    public static void addRecoilShake(float strength) {
        if (!HandmadeGunsCore.cfg_ClientCamera_MasterEnabled) return;
        float s = Math.max(0.0F, strength);
        if (recoilCooldown <= 0.0F) {
            recoilBurstShots = 0;
            recoilSeed = recoilSeed * 1664525 + 1013904223;
        }
        recoilBurstShots++;
        recoilCooldown = RECOIL_RECOVERY_DELAY_TICKS;
        float first = recoilBurstShots == 1 ? RECOIL_FIRST_SHOT_MULTIPLIER : 1.0F;
        float sustained = 1.0F + Math.min(2.0F, recoilBurstShots * RECOIL_SUSTAINED_MULTIPLIER);
        float ads = isAdsDown() ? 0.62F : 1.0F;
        float crouch = Minecraft.getMinecraft() != null && Minecraft.getMinecraft().thePlayer != null && Minecraft.getMinecraft().thePlayer.isSneaking() ? 0.78F : 1.0F;
        float pattern = MathHelper.sin((recoilSeed + recoilBurstShots * 37) * 0.017453292F) * RECOIL_PATTERN_STRENGTH;
        float wander = (nextRecoilNoise() * 2.0F - 1.0F) * RECOIL_HORIZONTAL_WANDER;
        float pitchKick = s * first * sustained * ads * crouch;
        float yawKick = s * (wander + pattern) * ads * crouch;
        recoilPitch = clamp(recoilPitch + pitchKick, 0.0F, RECOIL_MAX_ACCUMULATED_PITCH);
        recoilYaw = clamp(recoilYaw + yawKick, -RECOIL_MAX_ACCUMULATED_YAW, RECOIL_MAX_ACCUMULATED_YAW);
        recoilVisualPitch = clamp(recoilVisualPitch + pitchKick * RECOIL_CAMERA_PUNCH, -RECOIL_MAX_ACCUMULATED_PITCH, RECOIL_MAX_ACCUMULATED_PITCH);
        recoilVisualYaw = clamp(recoilVisualYaw + yawKick * RECOIL_CAMERA_PUNCH, -RECOIL_MAX_ACCUMULATED_YAW, RECOIL_MAX_ACCUMULATED_YAW);
        recoilVisualRoll = clamp(recoilVisualRoll - yawKick * 0.18F, -MAX_SHAKE_ROLL, MAX_SHAKE_ROLL);
        recoilShake += s * 0.10F;
    }
    public static void addExplosionShake(float strength) {
        float scoped = getScopeZoomFactor(Minecraft.getMinecraft()) > 1.0001F ? 1.28F : 1.0F;
        float s = clamp(Math.max(0.0F, strength) * EXPLOSION_SHAKE_MULTIPLIER * scoped, 0.0F, 5.0F);
        explosionShake = clamp(explosionShake + s, 0.0F, 6.0F);
        shakePitch = clamp(shakePitch - s * EXPLOSION_INITIAL_PUNCH, -MAX_SHAKE_PITCH, MAX_SHAKE_PITCH);
        shakeRoll = clamp(shakeRoll + s * 0.55F, -MAX_SHAKE_ROLL, MAX_SHAKE_ROLL);
    }
    public static void addLandingShake(float strength) { landingShake += Math.max(0.0F, strength) * LANDING_SHAKE_MULTIPLIER; }
    public static void addDamageShake(float strength) { damageShake += Math.max(0.0F, strength) * DAMAGE_SHAKE_MULTIPLIER; }

    private static void updateShake(float partialTicks) {
        float frequency = 0.75F;
        shakeTime += 0.05F * frequency;
        explosionPhase += 0.035F * frequency;

        float trauma = HandmadeGunsCore.cfg_ClientCamera_ShakeEnabled ? recoilShake + landingShake + damageShake : 0.0F;
        float impulse = HandmadeGunsCore.cfg_ClientCamera_ShakeEnabled ? explosionShake : 0.0F;
        float targetPitch = clamp(MathHelper.sin(shakeTime * 2.10F) * trauma * 0.70F - MathHelper.sin(explosionPhase) * impulse * 1.25F, -MAX_SHAKE_PITCH, MAX_SHAKE_PITCH);
        float targetYaw = clamp(MathHelper.sin(shakeTime * 1.37F + 1.7F) * trauma * 0.45F + MathHelper.sin(explosionPhase * 0.83F + 0.6F) * impulse * 0.55F, -MAX_SHAKE_YAW, MAX_SHAKE_YAW);
        float targetRoll = clamp(MathHelper.sin(shakeTime * 1.73F + 2.4F) * trauma * 0.55F + MathHelper.sin(explosionPhase * 0.71F + 1.2F) * impulse * 0.70F, -MAX_SHAKE_ROLL, MAX_SHAKE_ROLL);
        shakePitch = approachLimited(shakePitch, targetPitch, 0.35F, MAX_OFFSET_CHANGE * 1.5F);
        shakeYaw = approachLimited(shakeYaw, targetYaw, 0.35F, MAX_OFFSET_CHANGE * 1.5F);
        shakeRoll = approachLimited(shakeRoll, targetRoll, 0.35F, MAX_OFFSET_CHANGE * 1.5F);

        float decay = clamp01(SHAKE_DECAY_SPEED);
        recoilShake = approach(recoilShake, 0.0F, decay);
        explosionShake = approach(explosionShake, 0.0F, decay * 0.30F);
        landingShake = approach(landingShake, 0.0F, decay * 0.75F);
        damageShake = approach(damageShake, 0.0F, decay);
    }


    private static void updateRecoilRecovery() {
        if (recoilCooldown > 0.0F) {
            recoilCooldown -= 1.0F;
        } else {
            recoilPitch = approach(recoilPitch, 0.0F, RECOIL_RECOVERY_SPEED);
            recoilYaw = approach(recoilYaw, 0.0F, RECOIL_RECOVERY_SPEED);
        }
        recoilVisualPitch = approach(recoilVisualPitch, recoilPitch * 0.20F, RECOIL_RECOVERY_SPEED * 0.65F);
        recoilVisualYaw = approach(recoilVisualYaw, recoilYaw * 0.25F, RECOIL_RECOVERY_SPEED * 0.65F);
        recoilVisualRoll = approach(recoilVisualRoll, 0.0F, RECOIL_RECOVERY_SPEED);
    }

    private static float nextRecoilNoise() {
        recoilSeed = recoilSeed * 1103515245 + 12345;
        return ((recoilSeed >>> 8) & 0xFFFF) / 65535.0F;
    }


    public static boolean isHandlingScopeFov() {
        return HandmadeGunsCore.cfg_ClientCamera_MasterEnabled && HandmadeGunsCore.cfg_ClientCamera_FovInertiaEnabled;
    }

    private static float getScopeZoomFactor(Minecraft mc) {
        if (mc == null || mc.thePlayer == null || !HandmadeGunsCore.Key_ADS(mc.thePlayer)) return 1.0F;
        ItemStack stack = getActiveGunStack(mc.thePlayer);
        if (stack == null || !(stack.getItem() instanceof HMGItem_Unified_Guns)) return 1.0F;
        HMGItem_Unified_Guns gun = (HMGItem_Unified_Guns) stack.getItem();
        if (!gun.gunInfo.canobj || mc.thePlayer.isSprinting()) return 1.0F;
        gun.checkTags(stack);
        ItemStack sight = getAttachment(stack, 1);
        if (sight != null) {
            if (sight.getItem() instanceof HMGItemAttachment_reddot && gun.gunInfo.zoomrer) return validZoom(gun.gunInfo.scopezoomred);
            if (sight.getItem() instanceof HMGItemAttachment_scope && gun.gunInfo.zoomres) return validZoom(gun.gunInfo.scopezoomscope);
            if (sight.getItem() instanceof HMGItemSightBase && !((HMGItemSightBase) sight.getItem()).scopeonly) return validZoom(((HMGItemSightBase) sight.getItem()).zoomlevel);
        }
        return gun.gunInfo.zoomren ? validZoom(gun.gunInfo.scopezoombase) : 1.0F;
    }

    private static float validZoom(float zoom) { return zoom > 0.0001F ? zoom : 1.0F; }

    private static ItemStack getActiveGunStack(EntityLivingBase player) {
        if (player.ridingEntity instanceof PlacedGunEntity) return ((PlacedGunEntity) player.ridingEntity).gunStack;
        return player.getHeldItem();
    }

    private static ItemStack getAttachment(ItemStack stack, int wantedSlot) {
        if (stack == null || !stack.hasTagCompound() || !stack.getTagCompound().hasKey("Items")) return null;
        NBTTagList tags = (NBTTagList) stack.getTagCompound().getTag("Items");
        if (tags == null) return null;
        for (int i = 0; i < tags.tagCount(); i++) {
            NBTTagCompound tag = tags.getCompoundTagAt(i);
            if (tag != null && tag.getByte("Slot") == wantedSlot) return ItemStack.loadItemStackFromNBT(tag);
        }
        return null;
    }

    private static float[] getScopeBreathing() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.gameSettings == null || mc.gameSettings.thirdPersonView != 0 || mc.thePlayer.isDead || mc.thePlayer.getHealth() <= 0.0F) return new float[] {0.0F, 0.0F};
        if (getScopeZoomFactor(mc) <= 1.0001F || isWeaponStabilized(mc.thePlayer)) return new float[] {0.0F, 0.0F};
        float crouch = mc.thePlayer.isSneaking() ? 0.45F : 1.0F;
        return new float[] {MathHelper.sin(scopeBreathPhase) * SCOPE_BREATH_PITCH * crouch, MathHelper.sin(scopeBreathPhase * 0.73F + 1.1F) * SCOPE_BREATH_YAW * crouch};
    }

    private static boolean isWeaponStabilized(EntityLivingBase player) {
        if (player == null || player.ridingEntity instanceof PlacedGunEntity) return true;
        ItemStack stack = getActiveGunStack(player);
        if (stack == null || !stack.hasTagCompound()) return false;
        NBTTagCompound nbt = stack.getTagCompound();
        return nbt.getBoolean("HMGfixed") || nbt.getBoolean("set_up") || nbt.getBoolean("isset_up") || nbt.getBoolean("bipodfixed");
    }

    private static float springToward(float current, float target, boolean roll) {
        float velocity = roll ? leanRollVelocity : leanPitchVelocity;
        velocity = clamp((velocity + (target - current) * LEAN_ACCELERATION) * LEAN_DAMPING, -LEAN_MAX_VELOCITY, LEAN_MAX_VELOCITY);
        float next = current + velocity;
        if (roll) leanRollVelocity = velocity; else leanPitchVelocity = velocity;
        return clamp(next, roll ? -MAX_ROLL : -MAX_MOVEMENT_PITCH, roll ? MAX_ROLL : MAX_MOVEMENT_PITCH);
    }

    private static float halfLifeFactor(float halfLifeTicks) {
        return 1.0F - (float)Math.pow(0.5D, 1.0D / Math.max(0.001F, halfLifeTicks));
    }

    private static boolean isAdsDown() { Minecraft mc = Minecraft.getMinecraft(); return mc != null && mc.thePlayer != null && HandmadeGunsCore.Key_ADS(mc.thePlayer); }
    private static float applyDeadzone(float value, float deadzone) { return Math.abs(value) < deadzone ? 0.0F : value; }
    private static float approach(float current, float target, float factor) { return current + (target - current) * clamp01(factor); }
    private static float approachLimited(float current, float target, float factor, float maxStep) { return current + clamp((target - current) * clamp01(factor), -Math.max(0.001F, maxStep), Math.max(0.001F, maxStep)); }
    private static float clamp(float v, float min, float max) { return Math.max(min, Math.min(max, v)); }
    private static float clamp01(float v) { return clamp(v, 0.0F, 1.0F); }
}
