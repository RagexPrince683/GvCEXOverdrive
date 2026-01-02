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

    // state maps
    private static final Map<UUID, Boolean> wasJumping = new HashMap<UUID, Boolean>();
    private static final Map<UUID, Integer> groundGrace = new HashMap<UUID, Integer>();
    private static final Map<UUID, Integer> cooldowns = new HashMap<UUID, Integer>();      // active blocking cooldown (counts down after landing)
    private static final Map<UUID, Integer> pendingCooldowns = new HashMap<UUID, Integer>(); // scheduled cooldown to start on landing
    private static final Map<UUID, Boolean> wasOnGround = new HashMap<UUID, Boolean>();

    // parity: whether to cancel when the cooldown finishes and arms the next-jump cancel
    private static final Map<UUID, Boolean> cancelNext = new HashMap<UUID, Boolean>();
    // one-shot arm: if true, the next rising-edge jump will be cancelled (then cleared)
    private static final Map<UUID, Boolean> willCancelNextJump = new HashMap<UUID, Boolean>();

    private static final int BASE_DELAY = 2;   // ticks baseline
    private static final int MAX_EXTRA = 15;   // ticks extra for heavy guns

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

    /* Main per-tick handler: detect rising-edge jumps, schedule pending cooldowns, and cancel a jump
       if willCancelNextJump is armed. Uses END phase so isJumping/onGround are stable. */
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        EntityPlayer player = event.player;
        UUID id = player.getUniqueID();

        /* ground grace (2 ticks) to avoid edge issues */
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

        /* landing detection: pending -> active cooldown on landing */
        boolean prevOn = wasOnGround.getOrDefault(id, player.onGround);
        if (!prevOn && player.onGround) {
            Integer pending = pendingCooldowns.remove(id);
            if (pending != null && pending > 0) {
                cooldowns.put(id, pending);
                // ensure parity exists but DO NOT overwrite if already present
                if (!cancelNext.containsKey(id)) cancelNext.put(id, true);
            }
        }
        wasOnGround.put(id, player.onGround);

        /* read isJumping via reflection */
        boolean isJumping = false;
        try {
            if (isJumpingField != null) isJumping = isJumpingField.getBoolean(player);
        } catch (Exception ignored) {}

        boolean prevJump = wasJumping.getOrDefault(id, false);
        boolean jumpPressed = isJumping && !prevJump;
        wasJumping.put(id, isJumping);

        /* tick down active blocking cooldowns (these count down after landing). Handled in onPlayerPostTick as well, but we keep ticks here for robustness. */
        Integer cd = cooldowns.get(id);
        if (cd != null) {
            // don't remove/tick here; onPlayerPostTick will do the authoritative tick and arm willCancelNextJump.
            // keep this minimal so we don't double-tick if postTick also ticks.
        }

        /* If not a rising-edge jump, nothing else to do here */
        if (!jumpPressed) return;

        /* require recent ground contact */
        if (!groundGrace.containsKey(id)) return;

        /* If the one-shot arm is set, CANCEL this rising-edge jump and clear the arm.
           This makes the cancellation happen on the NEXT jump after the cooldown finished (not mid-air). */
        if (willCancelNextJump.getOrDefault(id, false)) {
            willCancelNextJump.remove(id);
            // original effect: block the jump
            player.motionY = 0;
            player.isAirBorne = false;
            return;
        }

        /* If there's still an active cooldown counting down, allow the jump (do not cancel mid-air).
           We only arm a cancel when cooldown finishes (handled in post tick). */
        if (cooldowns.containsKey(id)) {
            return; // allow normal jump while cooldown is counting down
        }

        /* No active cooldown and no will-cancel: this is a normal jump
           → schedule pending cooldown to be activated when the player lands. */
        ItemStack held = player.getCurrentEquippedItem();
        if (held == null || !(held.getItem() instanceof HMGItem_Unified_Guns)) return;

        HMGItem_Unified_Guns gun = (HMGItem_Unified_Guns) held.getItem();
        double motion = gun.gunInfo.motion;

        // no penalty for fully-light weapons
        if (motion >= 1.0) return;

        int delay = computeDelay(motion);

        // Skip tiny delays to preserve rifle feel
        if (delay <= 1) return;

        pendingCooldowns.put(id, delay);
        // ensure parity exists but do not overwrite existing parity
        if (!cancelNext.containsKey(id)) cancelNext.put(id, true);
    }

    /* Secondary tick: countdown active cooldowns and, when they finish, ARM the one-shot cancel
       according to parity (cancelNext), then toggle parity. This ensures cancellation is applied
       on the next rising-edge jump, not immediately mid-air. */
    @SubscribeEvent
    public void onPlayerPostTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        EntityPlayer player = event.player;
        UUID id = player.getUniqueID();

        Integer cd = cooldowns.get(id);
        if (cd == null) return;

        // tick down
        cd--;
        if (cd > 0) {
            cooldowns.put(id, cd);
            return;
        }

        // cooldown finished: remove and ARM or not based on parity
        cooldowns.remove(id);

        boolean shouldCancel = cancelNext.getOrDefault(id, true);
        // toggle parity for next time
        cancelNext.put(id, !shouldCancel);

        if (shouldCancel) {
            // ARM a one-shot that will cancel the *next* jump press (rising-edge)
            willCancelNextJump.put(id, true);
        } else {
            // do not arm — next jump is allowed normally
            willCancelNextJump.remove(id);
        }
    }

    // computeDelay tuned to soften light guns while keeping heavy guns punishing
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

        if (motion >= 0.85 && result > 2) result = 2;
        return result;
    }
}
