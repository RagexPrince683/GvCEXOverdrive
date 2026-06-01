package handmadeguns.client.camera;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import handmadeguns.camera.CameraConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ExplosionEvent;

@SideOnly(Side.CLIENT)
public class CameraSystem {
    public static final CameraSystem INSTANCE = new CameraSystem();

    private final RotationSmoother rotationSmoother = new RotationSmoother();
    private final MotionTilt motionTilt = new MotionTilt();
    private final FOVController fovController = new FOVController();
    private final BobController bobController = new BobController();
    private final ShakeManager shakeManager = new ShakeManager();
    private boolean registered;
    private boolean wasOnGround = true;
    private double lastMotionY;
    private boolean renderOffsetsApplied;
    private EntityClientPlayerMP renderOffsetPlayer;
    private float backupRotationYaw;
    private float backupPrevRotationYaw;
    private float backupRotationPitch;
    private float backupPrevRotationPitch;

    private CameraSystem() {
    }

    public void register() {
        if (registered) return;
        registered = true;
        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance().bus().register(this);
    }

    public static void addRecoilShake(float strength) {
        INSTANCE.shakeManager.addRecoilShake(strength);
    }

    public static void addExplosionShake(float strength) {
        INSTANCE.shakeManager.addExplosionShake(strength);
    }

    public static void addLandingShake(float strength) {
        INSTANCE.shakeManager.addLandingShake(strength);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        Minecraft mc = Minecraft.getMinecraft();
        EntityClientPlayerMP player = mc.thePlayer;
        if (player == null || mc.theWorld == null) {
            reset();
            return;
        }

        updateViewOffsetState(player);
        motionTilt.update(player);
        bobController.update(player);
        shakeManager.update();
        handleLandingShake(player);
    }

    private void updateViewOffsetState(EntityClientPlayerMP player) {
        rotationSmoother.update(player, 1.0F);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRenderTickStart(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            applyRenderOnlyOffsets(event.renderTickTime);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRenderTickEnd(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            restoreRenderOnlyOffsets();
        }
    }

    /**
     * Forge 1.7.10 has no CameraSetup event. To get real first-person camera feel without replacing
     * EntityRenderer or patching Angelica/OptiFine classes, we borrow the render-view player's yaw and
     * pitch only for the active render tick, then restore them at RenderTickEvent.END. This is still
     * isolated to the client render path: no packets are sent, no server movement changes, and no
     * persistent aim state is kept. True camera roll cannot be applied safely here without an
     * EntityRenderer ASM injection, so roll-producing controllers are folded into a tiny yaw sway.
     */
    private void applyRenderOnlyOffsets(float partialTicks) {
        restoreRenderOnlyOffsets();

        Minecraft mc = Minecraft.getMinecraft();
        if (!CameraConfig.masterEnabled || mc == null || mc.thePlayer == null || mc.theWorld == null) return;
        if (mc.gameSettings == null || mc.gameSettings.thirdPersonView != 0) return;
        if (mc.renderViewEntity != mc.thePlayer) return;

        EntityClientPlayerMP player = mc.thePlayer;
        renderOffsetPlayer = player;
        backupRotationYaw = player.rotationYaw;
        backupPrevRotationYaw = player.prevRotationYaw;
        backupRotationPitch = player.rotationPitch;
        backupPrevRotationPitch = player.prevRotationPitch;

        float rollAsYawSway = (motionTilt.getRoll(partialTicks)
                + bobController.getRoll(partialTicks)
                + shakeManager.getRoll(partialTicks)) * 0.15F;
        float yawOffset = rotationSmoother.getYawOffset(partialTicks)
                + shakeManager.getYaw(partialTicks)
                + rollAsYawSway;
        float pitchOffset = rotationSmoother.getPitchOffset(partialTicks)
                + motionTilt.getPitch(partialTicks)
                + bobController.getPitch(partialTicks)
                + shakeManager.getPitch(partialTicks);

        player.rotationYaw += yawOffset;
        player.prevRotationYaw += yawOffset;
        player.rotationPitch = CameraMath.clamp(player.rotationPitch + pitchOffset, -90.0F, 90.0F);
        player.prevRotationPitch = CameraMath.clamp(player.prevRotationPitch + pitchOffset, -90.0F, 90.0F);
        renderOffsetsApplied = true;
    }

    private void restoreRenderOnlyOffsets() {
        if (!renderOffsetsApplied || renderOffsetPlayer == null) return;
        renderOffsetPlayer.rotationYaw = backupRotationYaw;
        renderOffsetPlayer.prevRotationYaw = backupPrevRotationYaw;
        renderOffsetPlayer.rotationPitch = backupRotationPitch;
        renderOffsetPlayer.prevRotationPitch = backupPrevRotationPitch;
        renderOffsetsApplied = false;
        renderOffsetPlayer = null;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onFovUpdate(FOVUpdateEvent event) {
        if (event.entity instanceof EntityClientPlayerMP) {
            // Adjust the final FOV value provided by Forge instead of storing into game settings or EntityRenderer.
            fovController.onFovUpdate(event, (EntityClientPlayerMP) event.entity);
        }
    }

    @SubscribeEvent
    public void onExplosionDetonate(ExplosionEvent.Detonate event) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityClientPlayerMP player = mc.thePlayer;
        World world = event.world;
        if (player == null || world == null || !world.isRemote) return;

        double dx = event.explosion.explosionX - player.posX;
        double dy = event.explosion.explosionY - player.posY;
        double dz = event.explosion.explosionZ - player.posZ;
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        double radius = Math.max(4.0D, event.explosion.explosionSize * 5.0D);
        if (dist <= radius) {
            addExplosionShake((float) ((1.0D - dist / radius) * event.explosion.explosionSize));
        }
    }

    private void handleLandingShake(EntityClientPlayerMP player) {
        if (!wasOnGround && player.onGround && lastMotionY < -0.55D) {
            addLandingShake((float) Math.min(2.4D, (-lastMotionY - 0.45D) * 1.4D));
        }
        wasOnGround = player.onGround;
        lastMotionY = player.motionY;
    }

    private void reset() {
        restoreRenderOnlyOffsets();
        rotationSmoother.reset();
        motionTilt.reset();
        fovController.reset();
        bobController.reset();
        shakeManager.reset();
        wasOnGround = true;
        lastMotionY = 0.0D;
    }
}
