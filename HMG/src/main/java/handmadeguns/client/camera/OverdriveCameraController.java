package handmadeguns.client.camera;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import handmadeguns.HandmadeGunsCore;
import handmadeguns.camera.CameraConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ExplosionEvent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

@SideOnly(Side.CLIENT)
public final class OverdriveCameraController {
    private static final OverdriveCameraController INSTANCE = new OverdriveCameraController();
    private static final Random RANDOM = new Random();

    private final List<ShakeImpulse> impulses = new ArrayList<ShakeImpulse>();
    private boolean registered;
    private boolean initializedRotation;
    private boolean wasOnGround = true;
    private float smoothedYaw;
    private float smoothedPitch;
    private float yawOffset;
    private float pitchOffset;
    private float motionPitch;
    private float roll;
    private float bobPhase;
    private float bobPitch;
    private float bobRoll;
    private float bobTranslate;
    private float shakeYaw;
    private float shakePitch;
    private float shakeRoll;
    private float fovCurrent = -1.0F;
    private double lastHorizontalSpeed;
    private double lastMotionY;

    private OverdriveCameraController() {
    }

    public static void registerForgeEvents() {
        if (INSTANCE.registered) return;
        INSTANCE.registered = true;
        MinecraftForge.EVENT_BUS.register(INSTANCE);
    }

    public static void update(float partialTicks) {
        INSTANCE.updateInternal(partialTicks);
    }

    public static void applyCameraTransforms(float partialTicks) {
        INSTANCE.applyCameraTransformsInternal(partialTicks);
    }

    public static boolean applyCustomBobbing(float partialTicks) {
        return INSTANCE.applyCustomBobbingInternal(partialTicks);
    }

    public static float modifyFov(float fov, float partialTicks, boolean useFovSetting) {
        return INSTANCE.modifyFovInternal(fov);
    }

    public static boolean applyHurtCameraEffect(float partialTicks) {
        return INSTANCE.applyHurtCameraEffectInternal(partialTicks);
    }

    public static void addRecoilShake(float strength) {
        INSTANCE.addImpulse(strength * CameraConfig.recoilShakeMultiplier, 0.72F, -1.0F, 0.35F, 0.45F);
    }

    public static void addExplosionShake(float strength) {
        INSTANCE.addImpulse(strength * CameraConfig.explosionShakeMultiplier, 0.86F, 0.7F, 0.8F, 0.9F);
    }

    public static void addLandingShake(float strength) {
        INSTANCE.addImpulse(strength * CameraConfig.landingShakeMultiplier, 0.78F, 0.9F, 0.25F, 0.6F);
    }

    private void updateInternal(float partialTicks) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!CameraConfig.masterEnabled || mc == null || mc.thePlayer == null || mc.theWorld == null) {
            resetSoft();
            return;
        }

        EntityClientPlayerMP player = mc.thePlayer;
        updateRotationSmoothing(player, partialTicks);
        updateMotionTilt(player);
        updateBob(player);
        updateShake();
        updateLandingShake(player);
    }

    private void updateRotationSmoothing(EntityClientPlayerMP player, float partialTicks) {
        if (!CameraConfig.rotationSmoothingEnabled) {
            yawOffset = approach(yawOffset, 0.0F, 0.5F);
            pitchOffset = approach(pitchOffset, 0.0F, 0.5F);
            return;
        }

        float renderYaw = player.prevRotationYaw + wrapDegrees(player.rotationYaw - player.prevRotationYaw) * partialTicks;
        float renderPitch = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * partialTicks;
        if (!initializedRotation) {
            smoothedYaw = renderYaw;
            smoothedPitch = renderPitch;
            initializedRotation = true;
        }

        float follow = clamp(CameraConfig.smoothingStrength, 0.01F, 1.0F);
        smoothedYaw += wrapDegrees(renderYaw - smoothedYaw) * follow;
        smoothedPitch += (renderPitch - smoothedPitch) * follow;
        yawOffset = clamp(wrapDegrees(smoothedYaw - renderYaw), -CameraConfig.maxYawOffset, CameraConfig.maxYawOffset);
        pitchOffset = clamp(smoothedPitch - renderPitch, -CameraConfig.maxPitchOffset, CameraConfig.maxPitchOffset);
    }

    private void updateMotionTilt(EntityClientPlayerMP player) {
        if (!CameraConfig.motionTiltEnabled) {
            roll = approach(roll, 0.0F, CameraConfig.motionTiltReturnSpeed);
            motionPitch = approach(motionPitch, 0.0F, CameraConfig.motionTiltReturnSpeed);
            return;
        }

        double horizontalSpeed = Math.sqrt(player.motionX * player.motionX + player.motionZ * player.motionZ);
        float yawRad = player.rotationYaw * 0.017453292F;
        double strafe = player.motionX * Math.cos(yawRad) + player.motionZ * Math.sin(yawRad);
        double accel = horizontalSpeed - lastHorizontalSpeed;
        lastHorizontalSpeed = horizontalSpeed;

        float targetRoll = clamp((float) (-strafe * 28.0D), -CameraConfig.motionTiltMaxRoll, CameraConfig.motionTiltMaxRoll);
        float targetPitch = (float) (-(horizontalSpeed * 2.2D) - accel * 18.0D);
        if (player.isSprinting()) targetPitch -= 0.55F;
        if (!player.onGround) targetPitch += clamp((float) (-player.motionY * 2.2D), -1.2F, 1.2F);
        targetPitch = clamp(targetPitch, -CameraConfig.motionTiltMaxPitchOffset, CameraConfig.motionTiltMaxPitchOffset);

        float speed = clamp(CameraConfig.motionTiltReturnSpeed, 0.01F, 1.0F);
        roll = approach(roll, targetRoll, speed);
        motionPitch = approach(motionPitch, targetPitch, speed);
    }

    private void updateBob(EntityClientPlayerMP player) {
        if (!CameraConfig.bobEnabled) {
            bobPitch = approach(bobPitch, 0.0F, 0.25F);
            bobRoll = approach(bobRoll, 0.0F, 0.25F);
            bobTranslate = approach(bobTranslate, 0.0F, 0.25F);
            return;
        }

        double horizontalSpeed = Math.sqrt(player.motionX * player.motionX + player.motionZ * player.motionZ);
        float movement = clamp((float) horizontalSpeed * 5.5F, 0.0F, 1.0F);
        float multiplier = player.isSprinting() ? CameraConfig.bobSprintMultiplier : 1.0F;
        if (HandmadeGunsCore.Key_ADS(player)) multiplier *= CameraConfig.bobAdsMultiplier;
        bobPhase += CameraConfig.bobSpeed * (0.25F + movement) * multiplier;
        float strength = CameraConfig.bobStrength * movement * multiplier;
        bobPitch = approach(bobPitch, (float) Math.sin(bobPhase * 2.0F) * 0.35F * strength, 0.2F);
        bobRoll = approach(bobRoll, (float) Math.cos(bobPhase) * 0.22F * strength, 0.2F);
        bobTranslate = approach(bobTranslate, Math.abs((float) Math.sin(bobPhase)) * 0.035F * strength, 0.2F);
    }

    private void updateShake() {
        if (!CameraConfig.shakeEnabled) {
            impulses.clear();
            shakeYaw = approach(shakeYaw, 0.0F, 0.4F);
            shakePitch = approach(shakePitch, 0.0F, 0.4F);
            shakeRoll = approach(shakeRoll, 0.0F, 0.4F);
            return;
        }

        float targetYaw = 0.0F;
        float targetPitch = 0.0F;
        float targetRoll = 0.0F;
        Iterator<ShakeImpulse> iterator = impulses.iterator();
        while (iterator.hasNext()) {
            ShakeImpulse impulse = iterator.next();
            targetPitch += impulse.pitch * impulse.life;
            targetYaw += impulse.yaw * impulse.life;
            targetRoll += impulse.roll * impulse.life;
            impulse.life *= impulse.decay;
            if (impulse.life < 0.015F) iterator.remove();
        }
        shakePitch = clamp(targetPitch, -CameraConfig.maxShakePitch, CameraConfig.maxShakePitch);
        shakeYaw = clamp(targetYaw, -CameraConfig.maxShakeYaw, CameraConfig.maxShakeYaw);
        shakeRoll = clamp(targetRoll, -CameraConfig.maxShakeRoll, CameraConfig.maxShakeRoll);
    }

    private void updateLandingShake(EntityClientPlayerMP player) {
        if (!wasOnGround && player.onGround && lastMotionY < -0.55D) {
            addLandingShake((float) Math.min(2.4D, (-lastMotionY - 0.45D) * 1.4D));
        }
        wasOnGround = player.onGround;
        lastMotionY = player.motionY;
    }

    private void applyCameraTransformsInternal(float partialTicks) {
        if (!CameraConfig.masterEnabled) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.gameSettings == null || mc.gameSettings.thirdPersonView != 0) return;

        float finalYaw = yawOffset + shakeYaw;
        float finalPitch = pitchOffset + motionPitch + bobPitch + shakePitch;
        float finalRoll = roll + bobRoll + shakeRoll;
        finalYaw = clamp(finalYaw, -CameraConfig.maxYawOffset - CameraConfig.maxShakeYaw, CameraConfig.maxYawOffset + CameraConfig.maxShakeYaw);
        finalPitch = clamp(finalPitch, -CameraConfig.maxPitchOffset - CameraConfig.motionTiltMaxPitchOffset - CameraConfig.maxShakePitch,
                CameraConfig.maxPitchOffset + CameraConfig.motionTiltMaxPitchOffset + CameraConfig.maxShakePitch);
        finalRoll = clamp(finalRoll, -CameraConfig.motionTiltMaxRoll - CameraConfig.maxShakeRoll, CameraConfig.motionTiltMaxRoll + CameraConfig.maxShakeRoll);

        GL11.glRotatef(finalRoll, 0.0F, 0.0F, 1.0F);
        GL11.glRotatef(finalPitch, 1.0F, 0.0F, 0.0F);
        GL11.glRotatef(finalYaw, 0.0F, 1.0F, 0.0F);
    }

    private boolean applyCustomBobbingInternal(float partialTicks) {
        if (!CameraConfig.masterEnabled || !CameraConfig.bobEnabled || !CameraConfig.bobReplaceVanilla) return false;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.gameSettings == null || mc.gameSettings.thirdPersonView != 0) return false;

        GL11.glTranslatef(0.0F, -bobTranslate, 0.0F);
        GL11.glRotatef(bobPitch, 1.0F, 0.0F, 0.0F);
        GL11.glRotatef(bobRoll, 0.0F, 0.0F, 1.0F);
        return true;
    }

    private float modifyFovInternal(float fov) {
        if (!CameraConfig.masterEnabled || !CameraConfig.fovEnabled) {
            fovCurrent = -1.0F;
            return fov;
        }
        Minecraft mc = Minecraft.getMinecraft();
        EntityClientPlayerMP player = mc == null ? null : mc.thePlayer;
        float target = fov;
        if (player != null && player.isSprinting()) target += CameraConfig.sprintFovBoost * 70.0F;
        if (fovCurrent < 0.0F) fovCurrent = target;
        float speed = player != null && HandmadeGunsCore.Key_ADS(player) ? CameraConfig.adsFovSpeed : CameraConfig.fovLerpSpeed;
        fovCurrent = approach(fovCurrent, target, clamp(speed, 0.01F, 1.0F));
        return fovCurrent;
    }

    private boolean applyHurtCameraEffectInternal(float partialTicks) {
        if (!CameraConfig.masterEnabled || !CameraConfig.hurtEffectEnabled) return false;
        Minecraft mc = Minecraft.getMinecraft();
        EntityLivingBase view = mc == null ? null : mc.renderViewEntity;
        if (view == null || view.hurtTime <= 0) return false;

        float hurt = (float) view.hurtTime - partialTicks;
        float amount = clamp(hurt / Math.max(1.0F, (float) view.maxHurtTime), 0.0F, 1.0F);
        float angle = (float) Math.sin(amount * amount * Math.PI) * CameraConfig.hurtEffectStrength;
        GL11.glRotatef(angle, 0.0F, 0.0F, 1.0F);
        if (CameraConfig.hurtEffectAddsShake && view.hurtTime == view.maxHurtTime) {
            addImpulse(CameraConfig.hurtShakeStrength, 0.76F, 0.7F, 0.45F, 0.85F);
        }
        return CameraConfig.hurtReplaceVanilla;
    }

    @SubscribeEvent
    public void onExplosionDetonate(ExplosionEvent.Detonate event) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityClientPlayerMP player = mc == null ? null : mc.thePlayer;
        World world = event.world;
        if (player == null || world == null || !world.isRemote) return;
        double dx = event.explosion.explosionX - player.posX;
        double dy = event.explosion.explosionY - player.posY;
        double dz = event.explosion.explosionZ - player.posZ;
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        double radius = Math.max(4.0D, event.explosion.explosionSize * 5.0D);
        if (dist <= radius) addExplosionShake((float) ((1.0D - dist / radius) * event.explosion.explosionSize));
    }

    private void addImpulse(float strength, float decay, float pitchBias, float yawScale, float rollScale) {
        if (!CameraConfig.masterEnabled || !CameraConfig.shakeEnabled || strength <= 0.0F) return;
        strength = clamp(strength, 0.0F, 6.0F);
        impulses.add(new ShakeImpulse(pitchBias * strength,
                (RANDOM.nextFloat() * 2.0F - 1.0F) * strength * yawScale,
                (RANDOM.nextFloat() * 2.0F - 1.0F) * strength * rollScale,
                clamp(decay, 0.1F, 0.98F)));
    }

    private void resetSoft() {
        initializedRotation = false;
        yawOffset = approach(yawOffset, 0.0F, 0.5F);
        pitchOffset = approach(pitchOffset, 0.0F, 0.5F);
        motionPitch = approach(motionPitch, 0.0F, 0.5F);
        roll = approach(roll, 0.0F, 0.5F);
        bobPitch = approach(bobPitch, 0.0F, 0.5F);
        bobRoll = approach(bobRoll, 0.0F, 0.5F);
        shakeYaw = approach(shakeYaw, 0.0F, 0.5F);
        shakePitch = approach(shakePitch, 0.0F, 0.5F);
        shakeRoll = approach(shakeRoll, 0.0F, 0.5F);
    }

    private static float clamp(float value, float min, float max) {
        return value < min ? min : (value > max ? max : value);
    }

    private static float approach(float current, float target, float amount) {
        amount = clamp(amount, 0.0F, 1.0F);
        return current + (target - current) * amount;
    }

    private static float wrapDegrees(float value) {
        while (value >= 180.0F) value -= 360.0F;
        while (value < -180.0F) value += 360.0F;
        return value;
    }

    private static class ShakeImpulse {
        private final float pitch;
        private final float yaw;
        private final float roll;
        private final float decay;
        private float life = 1.0F;

        private ShakeImpulse(float pitch, float yaw, float roll, float decay) {
            this.pitch = pitch;
            this.yaw = yaw;
            this.roll = roll;
            this.decay = decay;
        }
    }
}
