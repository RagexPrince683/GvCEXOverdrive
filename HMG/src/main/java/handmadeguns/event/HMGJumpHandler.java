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
        if (event.phase != TickEvent.Phase.START) return;

        EntityPlayer player = event.player;
        UUID id = player.getUniqueID();

        boolean isJumping = false;
        try {
            isJumping = isJumpingField.getBoolean(player);
        } catch (Exception ignored) {}

        boolean prevJumping = wasJumping.containsKey(id) && wasJumping.get(id);

        // Detect rising edge: not jumping last tick → jumping now
        boolean jumpPressedThisTick = isJumping && !prevJumping;

        wasJumping.put(id, isJumping);

        // Tick down cooldown
        Integer cd = cooldowns.get(id);
        if (cd != null) {
            if (--cd <= 0) cooldowns.remove(id);
            else cooldowns.put(id, cd);
        }

        if (!jumpPressedThisTick || !player.onGround) return;

        ItemStack held = player.getCurrentEquippedItem();
        if (held == null) return;
        if (!(held.getItem() instanceof HMGItem_Unified_Guns)) return;

        HMGItem_Unified_Guns gun = (HMGItem_Unified_Guns) held.getItem();
        double motion = gun.gunInfo.motion;

        if (motion >= 1.0) return;

        int delay = computeDelay(motion);

        // If cooldown active → BLOCK THIS JUMP
        if (cooldowns.containsKey(id)) {
            player.motionY = 0;
            player.isAirBorne = false;
            return;
        }

        // Start cooldown immediately on jump press
        cooldowns.put(id, delay);
    }


    private static int computeDelay(double motion) {
        double penalty = MathHelper.clamp_double(1.0 - motion, 0.0, 1.0);
        return BASE_DELAY + (int)(penalty * MAX_EXTRA);
    }
}
