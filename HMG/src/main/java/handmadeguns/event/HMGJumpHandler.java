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

        // ground grace
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

        // detect rising-edge jump
        boolean isJumping = false;
        try { if (isJumpingField != null) isJumping = isJumpingField.getBoolean(player); }
        catch (Exception ignored) {}

        boolean prevJump = wasJumping.getOrDefault(id, false);
        boolean jumpPressed = isJumping && !prevJump;
        wasJumping.put(id, isJumping);

        // one-shot cancel
        if (jumpPressed && willCancelNextJump.getOrDefault(id, false)) {
            willCancelNextJump.remove(id);
            player.motionY = 0;
            player.isAirBorne = false;
            return;
        }

        // if active cooldown, allow normal jump but do not schedule more
        if (jumpPressed && cooldowns.containsKey(id)) return;

        // schedule pending cooldown
        if (!jumpPressed || !groundGrace.containsKey(id)) return;

        ItemStack held = player.getCurrentEquippedItem();
        if (held == null || !(held.getItem() instanceof HMGItem_Unified_Guns)) return;

        HMGItem_Unified_Guns gun = (HMGItem_Unified_Guns) held.getItem();
        double motion = gun.gunInfo.motion;

        if (motion >= 0.95) return; // light weapons skip entirely

        int delay = computeDelay(motion);
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
        cooldowns.remove(id);

        // get gun info
        ItemStack held = player.getCurrentEquippedItem();
        double motion = 1.0;
        if (held != null && held.getItem() instanceof HMGItem_Unified_Guns) {
            motion = ((HMGItem_Unified_Guns) held.getItem()).gunInfo.motion;
        }

        // decide parity
        boolean shouldCancel = cancelNext.getOrDefault(id, true);
        cancelNext.put(id, !shouldCancel);

        if (!shouldCancel) {
            willCancelNextJump.remove(id);
            return;
        }

        // APPLY rules:
        // HEAVY weapons: arm one-shot cancel for next rising-edge jump (do NOT stunt mid-air)
        if (motion <= 0.70) {
            willCancelNextJump.put(id, true); // blocks next jump entirely until cooldown expires
        }
        // MEDIUM: arm one-shot only
        else if (motion <= 0.90) {
            willCancelNextJump.put(id, true);
        }
        // LIGHT: only rising-edge cancel
        else {
            willCancelNextJump.put(id, true);
        }
    }

    // --- compute discrete delay ---
    private static int computeDelay(double motion) {
        motion = MathHelper.clamp_double(motion, 0.0, 1.0);

        if (motion >= 0.95) return 0;
        if (motion >= 0.90) return BASE_DELAY + 1; // very light
        if (motion >= 0.70) return BASE_DELAY + 4; // medium
        return BASE_DELAY + 10; // heavy
    }
}
