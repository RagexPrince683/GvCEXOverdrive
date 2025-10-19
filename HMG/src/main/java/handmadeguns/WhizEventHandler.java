package handmadeguns;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import handmadeguns.entity.bullets.HMGEntityBulletBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import java.util.*;

public class WhizEventHandler {
    public WhizEventHandler() {
        //System.out.println("[Whiz Debug] WhizEventHandler constructed");

    }

    private final Map<UUID, Set<Integer>> playerWhizzedBullets = new HashMap<>();

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        // Basic event fire check
        //System.out.println("[WHIZZ] onPlayerTick FIRED for: " + event.player.getCommandSenderName() + " | Phase: " + event.phase);

        if (event.phase != TickEvent.Phase.END) return;

        EntityPlayer player = event.player;
        World world = player.worldObj;

        if (world.isRemote) {
            System.out.println("[WHIZZ] Running bullet detection on CLIENT for " + player.getCommandSenderName());
            detectBulletWhizz(player, world);
        } else {
            //System.out.println("[WHIZZ] Ignoring SERVER side");
        }
    }

    private void detectBulletWhizz(EntityPlayer player, World world) {
        double radius = 10.0D;

        // Track map
        Set<Integer> trackedBullets = playerWhizzedBullets.computeIfAbsent(player.getUniqueID(), k -> new HashSet<>());

        // Get bullets nearby
        List<HMGEntityBulletBase> bullets = world.getEntitiesWithinAABB(
                HMGEntityBulletBase.class,
                player.boundingBox.expand(radius, radius, radius)
        );

        //System.out.println("[WHIZZ] Found " + bullets.size() + " bullets near " + player.getCommandSenderName());

        for (HMGEntityBulletBase bullet : bullets) {
            int bulletId = bullet.getEntityId();

            if (!trackedBullets.contains(bulletId)) {
                //System.out.println("[WHIZZ] Bullet " + bulletId + " triggered whizz sound at " + bullet.posX + "," + bullet.posY + "," + bullet.posZ);

                world.playSound(
                        bullet.posX,
                        bullet.posY,
                        bullet.posZ,
                        "handmadeguns:handmadeguns.bulletflyby",
                        5.0F,
                        1.0F,
                        false
                );

                trackedBullets.add(bulletId);
            } else {
                // Optional: useful to verify the set is working correctly
                //System.out.println("[WHIZZ] Bullet " + bulletId + " already triggered before, skipping.");
            }
        }

        // Cleanup bullets no longer in world
        int before = trackedBullets.size();
        trackedBullets.removeIf(id -> world.getEntityByID(id) == null);
        int after = trackedBullets.size();

        if (before != after) {
            //System.out.println("[WHIZZ] Cleaned up " + (before - after) + " stale bullet IDs for " + player.getCommandSenderName());
        }
    }
}
