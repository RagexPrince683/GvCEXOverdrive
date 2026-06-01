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

    /**
     * Forge 1.7.10 does not expose EntityViewRenderEvent.CameraSetup. That hook exists in later
     * Forge versions, but adding a typed handler for it breaks 1.7.10 compilation. The visual
     * yaw/pitch/roll controllers therefore stay isolated and stateful here, but their world-camera
     * application remains a deliberate no-op unless a future non-coremod 1.7.10-safe hook is added.
     *
     * Do not emulate CameraSetup by temporarily writing player.rotationYaw/rotationPitch or replacing
     * EntityRenderer/renderViewEntity: those options would affect aim/ray tracing or conflict with
     * OptiFine/shader render paths. FOV inertia remains active because FOVUpdateEvent is available.
     */
    private void updateViewOffsetState(EntityClientPlayerMP player) {
        rotationSmoother.update(player, 1.0F);
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
        rotationSmoother.reset();
        motionTilt.reset();
        fovController.reset();
        bobController.reset();
        shakeManager.reset();
        wasOnGround = true;
        lastMotionY = 0.0D;
    }
}
