package handmadeguns.event;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import handmadeguns.items.guns.HMGItem_Unified_Guns;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraftforge.event.entity.living.LivingEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HMGJumpHandler {

    private static final Map<UUID, Integer> cooldowns = new HashMap<>();
    private static final Map<UUID, Integer> pendingCooldowns = new HashMap<>();
    private static final Map<UUID, Boolean> willCancelNextJump = new HashMap<>();

    private static final int BASE_DELAY = 2;

    // ======================
    // SERVER AUTHORITATIVE JUMP
    // ======================

    @SubscribeEvent
    public void onJump(LivingEvent.LivingJumpEvent event) {

        if (event.entity.worldObj.isRemote) return;



        if (!(event.entity instanceof EntityPlayer)) return;

        EntityPlayer player = (EntityPlayer) event.entity;
        UUID id = player.getUniqueID();

        ItemStack held = player.getCurrentEquippedItem();

        // Not holding a gun → clear any armed cancel state and exit
        if (held == null || !(held.getItem() instanceof HMGItem_Unified_Guns)) {
            willCancelNextJump.remove(id);
            return;
        }

        HMGItem_Unified_Guns gun = (HMGItem_Unified_Guns) held.getItem();
        double motion = getServerMotion(gun);

        // If cooldown active → do not allow jump?
        if (cooldowns.containsKey(id)) {
            player.motionY = 0;
            player.isAirBorne = false;
            player.fallDistance = 0;
            return;
        }

        // Cancel armed jump (heavy weapons)
        if (willCancelNextJump.remove(id) != null) {
            player.motionY = 0;
            player.isAirBorne = false;
            player.fallDistance = 0;
            return;
        }

        if (motion >= 0.95) return; // light weapons = no penalty

        int delay = computeDelay(motion);
        if (delay <= 0) return;

        // schedule cooldown after landing
        pendingCooldowns.put(id, delay);

        // heavy weapons → cancel the next jump
        if (motion <= 0.70) {
            willCancelNextJump.put(id, true);
        }
    }

    private double getServerMotion(HMGItem_Unified_Guns gun) {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            return 1.0; // client never influences logic
        }
        return gun.gunInfo.motion;
    }


    // ======================
    // LANDING + COOLDOWN TICK
    // ======================

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {

        if (event.phase != TickEvent.Phase.END) return;
        if (event.player.worldObj.isRemote) return;

        EntityPlayer player = event.player;
        UUID id = player.getUniqueID();

        ItemStack held = player.getCurrentEquippedItem();
        if (!(held != null && held.getItem() instanceof HMGItem_Unified_Guns)) return;

        HMGItem_Unified_Guns gun = (HMGItem_Unified_Guns) held.getItem();
        double motion = gun.gunInfo.motion;

        // If cooldown active → hard block jump
        if (cooldowns.containsKey(id)) {

            if (player.motionY > 0) {
                player.motionY = 0;
                player.isAirBorne = false;
                player.fallDistance = 0;
            }
        }

        // Hard clamp heavy weapons
        if (motion <= 0.70 && player.motionY > 0.2) {
            player.motionY = 0.2;
        }
    }

    private static int computeDelay(double motion) {

        motion = MathHelper.clamp_double(motion, 0.0, 1.0);

        if (motion >= 0.95) return 0;
        if (motion >= 0.90) return BASE_DELAY + 1;
        if (motion >= 0.70) return BASE_DELAY + 4;
        return BASE_DELAY + 10;
    }
}
