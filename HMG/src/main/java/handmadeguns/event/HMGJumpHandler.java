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

    private static final Map<UUID, Boolean> wasJumping = new HashMap<>();
    private static final Map<UUID, Integer> groundGrace = new HashMap<>();
    private static final Map<UUID, Integer> cooldowns = new HashMap<>();
    private static final Map<UUID, Integer> pendingCooldowns = new HashMap<>();
    private static final Map<UUID, Boolean> wasOnGround = new HashMap<>();
    private static final Map<UUID, Boolean> willCancelNextJump = new HashMap<>();

    private static final int BASE_DELAY = 2;

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

        // we need prevOnGround for server-side rising detection; read it BEFORE updating wasOnGround
        boolean prevOn = wasOnGround.getOrDefault(id, player.onGround);

        // ground grace
        if (player.onGround) groundGrace.put(id, 2);
        else {
            Integer g = groundGrace.get(id);
            if (g != null) {
                g--;
                if (g <= 0) groundGrace.remove(id);
                else groundGrace.put(id, g);
            }
        }

        // landing detection: if we just landed, move pending -> active cooldown
        if (!prevOn && player.onGround) {
            Integer pending = pendingCooldowns.remove(id);
            if (pending != null && pending > 0) cooldowns.put(id, pending);
        }
        wasOnGround.put(id, player.onGround);

        // clear jump-state on landing so rising-edge is detected once
        if (player.onGround) {
            wasJumping.put(id, false);
        }

        // detect rising-edge jump
        boolean jumpPressed = false;

        if (player.worldObj.isRemote) {
            // client-side: use isJumping (keyboard)
            boolean isJumping = false;
            try { if (isJumpingField != null) isJumping = isJumpingField.getBoolean(player); }
            catch (Exception ignored) {}
            boolean prevJump = wasJumping.getOrDefault(id, false);
            jumpPressed = isJumping && !prevJump;
            wasJumping.put(id, isJumping);
        } else {
            // server-side: detect the moment player starts rising (prevOnGround && now not on ground && upward motion)
            boolean startedRising = prevOn && !player.onGround && player.motionY > 0.0D;
            boolean prevJump = wasJumping.getOrDefault(id, false);
            jumpPressed = startedRising && !prevJump;
            // mark that we've detected the start of rise until landing clears it
            if (startedRising) wasJumping.put(id, true);
        }

        // block jump if one-shot cancel armed
        if (jumpPressed && willCancelNextJump.getOrDefault(id, false)) {
            willCancelNextJump.remove(id);
            // cancel upward motion on both sides
            player.motionY = 0;
            player.isAirBorne = false;
            player.fallDistance = 0f;
            // server is authoritative; client will get corrected next tick via normal sync
            return;
        }

        // if cooldown active, allow normal jump
        if (jumpPressed && cooldowns.containsKey(id)) return;

        // schedule pending cooldown
        if (!jumpPressed || !groundGrace.containsKey(id)) return;

        ItemStack held = player.getCurrentEquippedItem();
        if (held == null || !(held.getItem() instanceof HMGItem_Unified_Guns)) return;

        HMGItem_Unified_Guns gun = (HMGItem_Unified_Guns) held.getItem();
        double motion = gun.gunInfo.motion;

        if (motion >= 0.95) return; // light weapons skip

        int delay = computeDelay(motion);
        if (delay <= 1) return;

        pendingCooldowns.put(id, delay);

        // HEAVY weapons: arm one-shot only AFTER the first jump completes
        if (motion <= 0.70) {
            willCancelNextJump.put(id, true);
        }
    }

    @SubscribeEvent
    public void onPlayerPostTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        EntityPlayer player = event.player;
        UUID id = player.getUniqueID();

        Integer cd = cooldowns.get(id);
        if (cd == null) return;

        cd--;
        if (cd > 0) {
            cooldowns.put(id, cd);
            return;
        }
        cooldowns.remove(id);
    }

    private static int computeDelay(double motion) {
        motion = MathHelper.clamp_double(motion, 0.0, 1.0);

        if (motion >= 0.95) return 0;
        if (motion >= 0.90) return BASE_DELAY + 1;
        if (motion >= 0.70) return BASE_DELAY + 4;
        return BASE_DELAY + 10;
    }
}
