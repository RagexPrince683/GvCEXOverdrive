package handmadeguns.event;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import handmadeguns.items.guns.HMGItem_Unified_Guns;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

// Register this with MinecraftForge.EVENT_BUS.register(new HMGJumpHandler());

public class HMGJumpHandler {

    // base delay in ticks (20 ticks == 1 second). Tune this to taste.
    private static final int BASE_JUMP_DELAY_TICKS = 10;

    @SubscribeEvent
    public void onLivingJump(LivingEvent.LivingJumpEvent event) {
        if (!(event.entityLiving instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) event.entityLiving;

        // Only enforce when player is holding an HMG gun
        ItemStack held = player.getCurrentEquippedItem();
        if (held == null || !(held.getItem() instanceof HMGItem_Unified_Guns)) return;

        HMGItem_Unified_Guns gun = (HMGItem_Unified_Guns) held.getItem();

        // ensure tags are present / up to date (your checkTags pattern)
        try {
            gun.checkTags(held);
        } catch (Throwable ignored) {}

        NBTTagCompound tag = held.hasTagCompound() ? held.getTagCompound() : null;
        if (tag == null) return; // no tag -> no special handling

        // Pull the motion multiplier from the gun (fall back to 1.0)
        double motionMult = 1.0;
        if (gun.gunInfo != null) motionMult = gun.gunInfo.motion;

        // If motion is <= 1.0 we don't impose extra delay
        if (motionMult <= 1.0) return;

        // Compute required cooldown in ticks
        int requiredTicks = (int) Math.round(BASE_JUMP_DELAY_TICKS * motionMult);

        // Store last jump tick on the player's entity data
        // Use a namespaced key to avoid collisions
        NBTTagCompound pdata = player.getEntityData();
        final String KEY = "HMG_lastJumpTick";

        long now = player.worldObj.getTotalWorldTime();
        long last = pdata.hasKey(KEY) ? pdata.getLong(KEY) : -100000L;

        if (now - last < requiredTicks) {
            // Too soon: cancel the jump by zeroing vertical motion and preventing airborne flag
            player.motionY = 0.0D;
            player.fallDistance = 0.0F;
            // best-effort to stop client-side visual: server is authoritative; client will be corrected
            if (player instanceof EntityPlayerMP) {
                // Optionally you can send a velocity packet here; usually not required.
            }
            // Prevent further processing (nothing else to do)
            return;
        }

        // Accept the jump and record the time
        pdata.setLong(KEY, now);
    }
}
