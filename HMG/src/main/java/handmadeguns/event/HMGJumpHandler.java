package handmadeguns.event;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import handmadeguns.items.guns.HMGItem_Unified_Guns;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HMGJumpHandler {

    /** minimal delay even for light guns (ticks) */
    private static final int BASE_JUMP_DELAY = 5;

    /** additional delay at motion = 0.0 */
    private static final int MAX_EXTRA_DELAY = 15;

    /** per-player cooldown */
    private final Map<UUID, Integer> jumpCooldown = new HashMap<>();

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        EntityPlayer player = event.player;
        UUID id = player.getUniqueID();

        // tick cooldown
        if (jumpCooldown.containsKey(id)) {
            int t = jumpCooldown.get(id) - 1;
            if (t <= 0) jumpCooldown.remove(id);
            else jumpCooldown.put(id, t);
        }

        ItemStack held = player.getCurrentEquippedItem();
        if (held == null) return;
        if (!(held.getItem() instanceof HMGItem_Unified_Guns)) return;

        HMGItem_Unified_Guns gun = (HMGItem_Unified_Guns) held.getItem();
        double motion = gun.gunInfo.motion;

        // no penalty for normal/light guns
        if (motion >= 1.0) return;

        // if cooldown active, kill jump
        if (jumpCooldown.containsKey(id)) {
            if (player.motionY > 0) {
                player.motionY = 0;
                player.isAirBorne = false;
            }
            return;
        }

        // detect jump edge (ground → upward motion)
        if (player.onGround && player.motionY > 0) {
            int delay = computeJumpDelay(motion);
            jumpCooldown.put(id, delay);
        }
    }

    private int computeJumpDelay(double motion) {
        // motion: 1.0 = no penalty, 0.6 = heavy
        double penalty = MathHelper.clamp_double(1.0 - motion, 0.0, 1.0);
        return BASE_JUMP_DELAY + (int) (penalty * MAX_EXTRA_DELAY);
    }
}
