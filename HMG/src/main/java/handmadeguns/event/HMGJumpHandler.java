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

        boolean isRemote = player.worldObj.isRemote;

        boolean isJumping = false;
        try {
            isJumping = isJumpingField.getBoolean(player);
        } catch (Exception e) {
            System.out.println("[HMGJump] REFLECTION FAILED");
            e.printStackTrace();
        }

        Boolean prev = wasJumping.get(id);
        boolean wasJumpingLastTick = prev != null && prev;
        boolean jumpPressedThisTick = isJumping && !wasJumpingLastTick;

        wasJumping.put(id, isJumping);

        System.out.println(
                "[HMGJump] side=" + (isRemote ? "CLIENT" : "SERVER") +
                        " onGround=" + player.onGround +
                        " isJumping=" + isJumping +
                        " prevJumping=" + wasJumpingLastTick +
                        " edge=" + jumpPressedThisTick
        );

        // Tick cooldown
        Integer cd = cooldowns.get(id);
        if (cd != null) {
            cooldowns.put(id, cd - 1);
            if (cd - 1 <= 0) {
                cooldowns.remove(id);
                System.out.println("[HMGJump] cooldown expired");
            }
        }

        if (!jumpPressedThisTick) return;
        if (!player.onGround) {
            System.out.println("[HMGJump] jump edge but NOT on ground");
            return;
        }

        ItemStack held = player.getCurrentEquippedItem();
        if (held == null) {
            System.out.println("[HMGJump] no held item");
            return;
        }

        if (!(held.getItem() instanceof HMGItem_Unified_Guns)) {
            System.out.println("[HMGJump] held item NOT gun");
            return;
        }

        HMGItem_Unified_Guns gun = (HMGItem_Unified_Guns) held.getItem();
        double motion = gun.gunInfo.motion;

        System.out.println("[HMGJump] gun motion=" + motion);

        if (motion >= 1.0) {
            System.out.println("[HMGJump] motion >= 1.0 → ignoring");
            return;
        }

        int delay = computeDelay(motion);
        System.out.println("[HMGJump] computed delay=" + delay);

        if (cooldowns.containsKey(id)) {
            System.out.println("[HMGJump] BLOCKING JUMP");
            player.motionY = 0;
            player.isAirBorne = false;
            return;
        }

        cooldowns.put(id, delay);
        System.out.println("[HMGJump] cooldown started");
    }



    private static int computeDelay(double motion) {
        double penalty = MathHelper.clamp_double(1.0 - motion, 0.0, 1.0);
        return BASE_DELAY + (int)(penalty * MAX_EXTRA);
    }
}
