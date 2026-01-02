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
    private static final Map<UUID, Boolean> wasJumping = new HashMap<>();
    private static final Map<UUID, Integer> groundGrace = new HashMap<>();
    private static final Map<UUID, Integer> cooldowns = new HashMap<>();
    private static final Map<UUID, Integer> pendingCooldowns = new HashMap<>();
    private static final Map<UUID, Boolean> wasOnGround = new HashMap<>();
    private static final Map<UUID, Boolean> cancelNext = new HashMap<>();
    private static final Map<UUID, Boolean> willCancelNextJump = new HashMap<>();

    // tuned constants
    private static final int BASE_DELAY = 2;
    private static final int MAX_EXTRA = 15;

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

    // --- Primary tick: rising-edge jump detection ---
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

        // landing detection: pending -> active cooldown
        boolean prevOn = wasOnGround.getOrDefault(id, player.onGround);
        if (!prevOn && player.onGround) {
            Integer pending = pendingCooldowns.remove(id);
            if (pending != null && pending > 0) {
                cooldowns.put(id, pending);
                cancelNext.putIfAbsent(id, true);
            }
        }
        wasOnGround.put(id, player.onGround);

        // detect rising-edge jump via reflection
        boolean isJumping = false;
        try { if (isJumpingField != null) isJumping = isJumpingField.getBoolean(player); }
        catch (Exception ignored) {}

        boolean prevJump = wasJumping.getOrDefault(id, false);
        boolean jumpPressed = isJumping && !prevJump;
        wasJumping.put(id, isJumping);

        // If a one-shot cancel is armed, cancel this rising-edge jump now
        if (jumpPressed && willCancelNextJump.getOrDefault(id, false)) {
            willCancelNextJump.remove(id);
            player.motionY = 0;
            player.isAirBorne = false;
            return;
        }

        // If cooldown is active and counting down, allow the jump now (we won't cancel mid-air here)
        if (jumpPressed && cooldowns.containsKey(id)) return;

        // Normal rising-edge jump: schedule pending cooldown (only if gun and delay warrant)
        if (!jumpPressed || !groundGrace.containsKey(id)) return;

        ItemStack held = player.getCurrentEquippedItem();
        if (held == null || !(held.getItem() instanceof HMGItem_Unified_Guns)) return;

        HMGItem_Unified_Guns gun = (HMGItem_Unified_Guns) held.getItem();
        double motion = gun.gunInfo.motion;

        // If motion is very high (>= 0.95), treat as effectively unpenalized — do not schedule.
        if (motion >= 0.95) return;

        int delay = computeDelay(motion);
        // tiny delay => skip (keeps light rifles feeling natural)
        if (delay <= 1) return;

        pendingCooldowns.put(id, delay);
        cancelNext.putIfAbsent(id, true);
    }

    // --- Secondary tick: countdown active cooldowns ---
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

        // read current held gun motion (default to 1.0 — no penalty)
        ItemStack held = player.getCurrentEquippedItem();
        double motion = 1.0;
        if (held != null && held.getItem() instanceof HMGItem_Unified_Guns) {
            motion = ((HMGItem_Unified_Guns) held.getItem()).gunInfo.motion;
        }

        // decide parity (alternate every finish)
        boolean shouldCancel = cancelNext.getOrDefault(id, true);
        cancelNext.put(id, !shouldCancel);

        if (!shouldCancel) {
            // do not arm or stunt; next jump allowed
            willCancelNextJump.remove(id);
            return;
        }

        // check if player is currently holding jump (same keypress)
        boolean isJumpingNow = false;
        try { if (isJumpingField != null) isJumpingNow = isJumpingField.getBoolean(player); }
        catch (Exception ignored) {}

        // APPLY rules:
        // - HEAVY (motion <= 0.70) : immediate stunt if holding; otherwise arm one-shot.
        // - MEDIUM (0.70 < motion <= 0.90) : do NOT stunt immediately; arm one-shot only.
        // - LIGHT (motion > 0.90) : should not reach here because we early-return at motion>=0.95; but safely arm one-shot only.
        if (motion <= 0.70) {
            if (isJumpingNow) {
                // immediate stunt (heavy weapons)
                player.motionY = 0;
                player.isAirBorne = false;
                willCancelNextJump.remove(id);
            } else {
                // arm one-shot cancel for next rising-edge
                willCancelNextJump.put(id, true);
            }
        } else if (motion <= 0.90) {
            // medium weapons: don't stunt mid-air; only arm one-shot
            willCancelNextJump.put(id, true);
        } else {
            // light weapons fallback (unlikely due to earlier check) — only one-shot
            willCancelNextJump.put(id, true);
        }
    }

    // --- compute discrete delay based on motion (simple predictable mapping) ---
    // motion: 0.95 (light) -> no scheduling
    // motion: 0.90..0.95 -> tiny delay
    // motion: 0.70..0.90 -> medium delay
    // motion: <=0.70 -> large delay
    private static int computeDelay(double motion) {
        motion = MathHelper.clamp_double(motion, 0.0, 1.0);

        if (motion >= 0.95) return 0;             // effectively no penalty
        if (motion >= 0.90) return BASE_DELAY + 1; // very light
        if (motion >= 0.70) return BASE_DELAY + 4; // medium
        // heavy
        return BASE_DELAY + 10; // strong effect for heavy weapons
    }
}
