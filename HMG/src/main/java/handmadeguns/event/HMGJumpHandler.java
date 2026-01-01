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

    private static final int BASE_DELAY = 5;   // ticks
    private static final int MAX_EXTRA = 15;   // ticks

    private static final Map<UUID, Integer> cooldowns = new HashMap<>();

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
        EntityPlayer player = event.player;

        if (event.phase != TickEvent.Phase.START) return;

        UUID id = player.getUniqueID();

        boolean isJumping = false;
        try {
            if (isJumpingField != null) {
                isJumping = isJumpingField.getBoolean(player);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Detect jump press (server + client)
        if (player.onGround && isJumping) {

            // cooldown active → cancel jump
            if (cooldowns.containsKey(id)) {
                player.motionY = 0;
                player.isAirBorne = false;
                return;
            }

            ItemStack held = player.getCurrentEquippedItem();
            if (held == null) return;
            if (!(held.getItem() instanceof HMGItem_Unified_Guns)) return;

            HMGItem_Unified_Guns gun = (HMGItem_Unified_Guns) held.getItem();
            double motion = gun.gunInfo.motion;

            if (motion >= 1.0) return;

            int delay = computeDelay(motion);
            cooldowns.put(id, delay);
        }

        // Tick down cooldowns
        if (cooldowns.containsKey(id)) {
            int remaining = cooldowns.get(id) - 1;
            if (remaining <= 0) {
                cooldowns.remove(id);
            } else {
                cooldowns.put(id, remaining);
            }
        }
    }

    private static int computeDelay(double motion) {
        double penalty = MathHelper.clamp_double(1.0 - motion, 0.0, 1.0);
        return BASE_DELAY + (int)(penalty * MAX_EXTRA);
    }
}
