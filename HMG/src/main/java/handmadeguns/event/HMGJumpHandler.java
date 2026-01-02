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

    // state
    private static final Map<UUID, Boolean> wasJumping = new HashMap<UUID, Boolean>();
    private static final Map<UUID, Integer> groundGrace = new HashMap<UUID, Integer>();
    private static final Map<UUID, Integer> cooldowns = new HashMap<UUID, Integer>();      // active blocking cooldown (counts down after landing)
    private static final Map<UUID, Integer> pendingCooldowns = new HashMap<UUID, Integer>(); // scheduled cooldown to start on landing
    private static final Map<UUID, Boolean> wasOnGround = new HashMap<UUID, Boolean>();

    // parity: whether to cancel when a cooldown finishes (toggles each finish)
    private static final Map<UUID, Boolean> cancelNext = new HashMap<UUID, Boolean>();
    // one-shot arm: if true, the next rising-edge jump will be cancelled (then cleared)
    private static final Map<UUID, Boolean> willCancelNextJump = new HashMap<UUID, Boolean>();

    private static final int BASE_DELAY = 2;   // ticks baseline
    private static final int MAX_EXTRA = 15;   // ticks extra for heavy guns

    // reflection: EntityLivingBase.isJumping
    private static Field isJumpingField;
    static {
        try {
            isJumpingField = EntityLivingBase.class.getDeclaredField("isJumping");
            isJumpingField.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Primary tick: detect rising-edge jumps, schedule pending cooldowns, and handle willCancelNextJump
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        EntityPlayer player = event.player;
        UUID id = player.getUniqueID();

        // ground grace (2 ticks)
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

        // landing detection: pending -> active cooldown on landing
        boolean prevOn = wasOnGround.getOrDefault(id, player.onGround);
        if (!prevOn && player.onGround) {
            Integer pending = pendingCooldowns.remove(id);
            if (pending != null && pending > 0) {
                cooldowns.put(id, pending);
                if (!cancelNext.containsKey(id)) cancelNext.put(id, true);
            }
        }
        wasOnGround.put(id, player.onGround);

        // read isJumping (reflection)
        boolean isJumping = false;
        try { if (isJumpingField != null) isJumping = isJumpingField.getBoolean(player); }
        catch (Exception ignored) {}

        boolean prevJump = wasJumping.getOrDefault(id, false);
        boolean jumpPressed = isJumping && !prevJump;
        wasJumping.put(id, isJumping);

        // If this rising-edge jump is the one-shot cancel, cancel it now
        if (jumpPressed && willCancelNextJump.getOrDefault(id, false)) {
            willCancelNextJump.remove(id);
            // cancel the attempted jump
            player.motionY = 0;
            player.isAirBorne = false;
            return;
        }

        // If there's an active cooldown counting down, allow this jump (we don't cancel mid-air here)
        if (jumpPressed && cooldowns.containsKey(id)) {
            // allow normal jump; cancellation will have been handled on cooldown finish (either armed or immediate)
            return;
        }

        // If it's a normal rising-edge jump (no active cooldown/will-cancel), schedule a pending cooldown
        if (!jumpPressed) return;
        if (!groundGrace.containsKey(id)) return;

        ItemStack held = player.getCurrentEquippedItem();
        if (held == null || !(held.getItem() instanceof HMGItem_Unified_Guns)) return;

        HMGItem_Unified_Guns gun = (HMGItem_Unified_Guns) held.getItem();
        double motion = gun.gunInfo.motion;

        if (motion >= 1.0) return; // no penalty for lightest weapons

        int delay = computeDelay(motion);

        // skip tiny delays so rifles are not impacted
        if (delay <= 1) return;

        pendingCooldowns.put(id, delay);
        if (!cancelNext.containsKey(id)) cancelNext.put(id, true);
    }

    // Secondary tick: countdown active cooldowns; when a cooldown finishes -> toggle parity and either
    // * immediate stunt if the player is currently holding jump (isJumping true), or
    // * arm a one-shot willCancelNextJump to cancel the next rising-edge press.
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

        // cooldown finished
        cooldowns.remove(id);

        // get held item info (if absent, do not stunt)
        ItemStack held = player.getCurrentEquippedItem();
        double motion = 1.0;
        if (held != null && held.getItem() instanceof HMGItem_Unified_Guns) {
            motion = ((HMGItem_Unified_Guns) held.getItem()).gunInfo.motion;
        }

        // decide parity
        boolean shouldCancel = cancelNext.getOrDefault(id, true);
        cancelNext.put(id, !shouldCancel); // toggle parity for next finish

        if (!shouldCancel) {
            // do nothing — next jump allowed
            willCancelNextJump.remove(id);
            return;
        }

        // shouldCancel == true: either stunt now (if player is holding jump) or arm a one-shot for next rising-edge

        // check current jump state via reflection
        boolean isJumpingNow = false;
        try { if (isJumpingField != null) isJumpingNow = isJumpingField.getBoolean(player); }
        catch (Exception ignored) {}

        // only stunt if gun still qualifies (medium/heavy) and player is holding jump
        if (motion < 0.75 && isJumpingNow) {
            // immediate stunt because player is still holding the jump key (same press)
            player.motionY = 0;
            player.isAirBorne = false;
            // ensure no one-shot remains
            willCancelNextJump.remove(id);
        } else {
            // arm a one-shot: cancel the next rising-edge jump press
            willCancelNextJump.put(id, true);
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
