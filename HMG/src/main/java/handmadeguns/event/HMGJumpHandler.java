package handmadeguns.event;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import handmadeguns.items.guns.HMGItem_Unified_Guns;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HMGJumpHandler {

    private static final Map<UUID, Integer> cooldowns = new HashMap<>();
    private static final Map<UUID, Integer> pendingCooldowns = new HashMap<>();
    private static final Map<UUID, Boolean> willCancelNextJump = new HashMap<>();

    // track previous onGround to detect the server-side jump moment
    private static final Map<UUID, Boolean> prevOnGround = new HashMap<>();

    private static final int BASE_DELAY = 2;

    // ======================
    // PLAYER TICK - SERVER AUTHORITATIVE (single place of truth)
    // ======================
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        EntityPlayer player = event.player;

        // only run on server
        if (player.worldObj.isRemote) return;

        UUID id = player.getUniqueID();

        // Read previous onGround state (default true so we treat first tick as grounded)
        boolean prevGround = prevOnGround.getOrDefault(id, true);
        boolean currGround = player.onGround;

        // Activate pending cooldown when player touches ground (landing)
        if (currGround && !prevGround) { // transition: air -> ground (landed)
            Integer pending = pendingCooldowns.remove(id);
            if (pending != null) cooldowns.put(id, pending);
        }

        // Compute and tick cooldowns (server authoritative tick)
        Integer cd = cooldowns.get(id);
        if (cd != null) {
            cd--;
            if (cd <= 0) cooldowns.remove(id);
            else cooldowns.put(id, cd);
        }

        // Remember ground state now (for next tick)
        prevOnGround.put(id, currGround);

        // Only proceed if player is holding a gun
        ItemStack held = player.getCurrentEquippedItem();
        if (held == null || !(held.getItem() instanceof HMGItem_Unified_Guns)) {
            // clear any "willCancelNextJump" for players who no longer hold gun (matches your original)
            willCancelNextJump.remove(id);
            return;
        }

        HMGItem_Unified_Guns gun = (HMGItem_Unified_Guns) held.getItem();
        double motion = getServerMotion(gun); // authoritative server value

        // Detect server-side jump *moment*
        // A jump has happened if we were on ground last tick and now left the ground.
        // Also guard by seeing upward velocity > 0 (to confirm upward movement).
        boolean justJumped = prevGround && !currGround && player.motionY > 0.0D;

        // If player just jumped, enforce rules immediately (server wins)
        if (justJumped) {

            // If there is an armed cancel from previous fire: cancel this jump completely
            if (willCancelNextJump.remove(id) != null) {
                blockAndSyncJump(player);
                // do not schedule cooldown from this canceled jump
                return;
            }

            // If a cooldown is active, block jump entirely
            if (cooldowns.containsKey(id)) {
                blockAndSyncJump(player);
                return;
            }

            // If light weapon → nothing to do
            if (motion >= 0.95) {
                return;
            }

            // For other weights, compute delay and schedule activation on landing
            int delay = computeDelay(motion);
            if (delay > 0) {
                pendingCooldowns.put(id, delay);
            }

            // If heavy weapon: immediately clamp the jump (server authoritative clamp)
            if (motion <= 0.70) {
                // clamp upward velocity to a safe value so client cannot get higher jump
                // choose a conservative clamp; adjust as desired
                double clamp = 0.2D;
                if (player.motionY > clamp) {
                    player.motionY = clamp;
                    player.velocityChanged = true; // mark changed so server sends velocity packet
                }
                // arm next jump cancellation if you still want that behavior
                willCancelNextJump.put(id, true);
            } else {
                // medium weapons: smaller clamp (optional)
                if (motion < 0.95 && motion > 0.70) {
                    double clamp = 0.35D; // example medium clamp
                    if (player.motionY > clamp) {
                        player.motionY = clamp;
                        player.velocityChanged = true;
                    }
                }
            }
        }

        // If you also want to hard-prevent any upward velocity while a cooldown is active
        // (e.g., stop jump if they try to jump while on cooldown via client prediction)
        if (cooldowns.containsKey(id) && player.motionY > 0.0D) {
            // block and sync to enforce server authority
            blockAndSyncJump(player);
        }
    }

    // Forcefully cancel upward motion and ensure client is corrected
    private void blockAndSyncJump(EntityPlayer player) {
        player.motionY = 0.0D;
        player.fallDistance = 0.0F;
        player.velocityChanged = true; // ask server to sync velocity

        // As a stronger fallback (teleport a tiny bit) to guarantee immediate correction:
        // this sends a Position packet and forces client to snap to server position.
        // Keep it small to avoid rubberband annoyance.
        try {
            player.setPositionAndUpdate(player.posX, player.posY - 0.01D, player.posZ);
        } catch (Exception ignored) {
            // setPositionAndUpdate can be noisy in some contexts — velocityChanged was already set.
        }
    }

    // Authoritative source of motion: read from the server-side gun definition/config
    // (Make sure HMGItem_Unified_Guns.gunInfo is loaded from server config at startup)
    private double getServerMotion(HMGItem_Unified_Guns gun) {
        // On the server the gun object and its gunInfo should reflect server configs,
        // so simply return the configured value here.
        return gun.gunInfo.motion;
    }

    private static int computeDelay(double motion) {
        motion = MathHelper.clamp_double(motion, 0.0, 1.0);

        if (motion >= 0.95) return 0;
        if (motion >= 0.90) return BASE_DELAY + 1;
        if (motion >= 0.70) return BASE_DELAY + 4;
        return BASE_DELAY + 10;
    }
}