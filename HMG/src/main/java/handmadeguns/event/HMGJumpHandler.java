package handmadeguns.event;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import handmadeguns.items.guns.HMGItem_Unified_Guns;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HMGJumpHandler {

    private static final Map<UUID, Boolean> wasJumping = new HashMap<UUID, Boolean>();
    private static final Map<UUID, Integer> groundGrace = new HashMap<UUID, Integer>();
    private static final Map<UUID, Integer> cooldowns = new HashMap<UUID, Integer>();      // active blocking cooldown (blocks jump presses)
    private static final Map<UUID, Integer> pendingCooldowns = new HashMap<UUID, Integer>(); // scheduled cooldown to start on landing
    private static final Map<UUID, Boolean> wasOnGround = new HashMap<UUID, Boolean>();
    private static final Map<UUID, Boolean> cancelNext = new HashMap<UUID, Boolean>(); // parity: whether to cancel when cooldown finishes

    private static final int BASE_DELAY = 2;   // ticks
    private static final int MAX_EXTRA = 15;   // ticks

    // reflection field for EntityLivingBase.isJumping
    private static Field isJumpingField;

    static {
        try {
            isJumpingField = EntityLivingBase.class.getDeclaredField("isJumping");
            isJumpingField.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        EntityPlayer player = event.player;
        UUID id = player.getUniqueID();

        // ground grace for jump detection (keeps behavior stable)
        if (player.onGround) {
            groundGrace.put(id, 2);
        } else {
            Integer g = groundGrace.get(id);
            if (g != null) {
                g--;
                if (g <= 0) groundGrace.remove(id);
                else groundGrace.put(id, g);
            }
        }

        // landing detection (edge: not-on-ground -> onGround)
        boolean prevOn = wasOnGround.getOrDefault(id, player.onGround);
        if (!prevOn && player.onGround) {
            // player just landed — if there's a pending cooldown, activate it now
            Integer pending = pendingCooldowns.remove(id);
            if (pending != null && pending > 0) {
                cooldowns.put(id, pending);
                // initialize parity only if missing (do NOT overwrite existing parity)
                if (!cancelNext.containsKey(id)) cancelNext.put(id, true); // first blocked press will cancel
            }
        }
        wasOnGround.put(id, player.onGround);

        // read isJumping via reflection
        boolean isJumping = false;
        try {
            if (isJumpingField != null) isJumping = isJumpingField.getBoolean(player);
        } catch (Exception ignored) {}

        boolean prevJump = wasJumping.getOrDefault(id, false);
        boolean jumpPressed = isJumping && !prevJump;
        wasJumping.put(id, isJumping);

        // tick down active blocking cooldowns (these block jump presses)
        Integer cd = cooldowns.get(id);
        if (cd != null) {
            cd--;
            if (cd <= 0) {
                cooldowns.remove(id);
            } else {
                cooldowns.put(id, cd);
            }
        }

        // If this press is not a rising-edge jump, bail
        if (!jumpPressed) return;

        // Must have been on ground very recently (avoid mid-air nonsense)
        if (!groundGrace.containsKey(id)) return;

        // If an active cooldown exists now, this press should be blocked (this cancels the NEXT jump)
        if (cooldowns.containsKey(id)) {
            // YOUR ORIGINAL EFFECT — cancel the attempted jump
            player.motionY = 0;
            player.isAirBorne = false;
            return;
        }

        // Otherwise schedule a pending cooldown to start when the player next lands.
        ItemStack held = player.getCurrentEquippedItem();
        if (held == null) return;
        if (!(held.getItem() instanceof HMGItem_Unified_Guns)) return;

        HMGItem_Unified_Guns gun = (HMGItem_Unified_Guns) held.getItem();
        double motion = gun.gunInfo.motion;

        // no penalty for fully-light weapons
        if (motion >= 1.0) return;

        int delay = computeDelay(motion);

        // SKIP tiny delays so light rifles are not affected
        if (delay <= 1) return;

        // schedule pending cooldown; it will be activated when the player next lands
        pendingCooldowns.put(id, delay);
    }

    // This fires continuously per tick and handles the cooldown finishing behavior.
    @SubscribeEvent
    public void onPlayerPostTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        EntityPlayer player = event.player;
        UUID id = player.getUniqueID();

        Integer cd = cooldowns.get(id);
        if (cd == null) return;

        ItemStack held = player.getCurrentEquippedItem();
        if (held == null || !(held.getItem() instanceof HMGItem_Unified_Guns)) {
            // still tick cooldown even if item changed
            cd--;
            if (cd <= 0) cooldowns.remove(id);
            else cooldowns.put(id, cd);
            return;
        }

        HMGItem_Unified_Guns gun = (HMGItem_Unified_Guns) held.getItem();
        double motion = gun.gunInfo.motion;

        // tick down
        cd--;
        if (cd <= 0) {
            cooldowns.remove(id);

            // alternate cancellation every other time
            boolean shouldCancel = cancelNext.getOrDefault(id, true);
            cancelNext.put(id, !shouldCancel); // toggle parity for next time

            // Only heavy/medium guns actually get blocked, and only on alternating finishes
            if (shouldCancel && motion < 0.75) {
                player.motionY = 0;
                player.isAirBorne = false;
            }
        } else {
            cooldowns.put(id, cd);
        }
    }

    // --- tuned computeDelay (keeps your scaled behavior) ---
    private static int computeDelay(double motion) {
        double penalty = MathHelper.clamp_double(1.0 - motion, 0.0, 1.0);

        double scale;
        if (motion >= 0.85) {         // very light weapons
            scale = 0.12;
        } else if (motion >= 0.70) {  // light weapons
            scale = 0.35;
        } else if (motion >= 0.50) {  // medium weapons
            scale = 0.65;
        } else {                      // heavy weapons
            scale = 1.0;
        }

        int extra = (int) Math.round(penalty * MAX_EXTRA * scale);
        int result = BASE_DELAY + extra;

        // floor for ultra-light guns
        if (motion >= 0.85 && result > 2) result = 2;

        return result;
    }
}
