package handmadeguns;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import handmadeguns.entity.bullets.HMGEntityBulletBase;
import net.minecraft.entity.Entity;
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
        if (event.phase != TickEvent.Phase.END) return;

        EntityPlayer player = event.player;
        World world = player.worldObj;

        if (world.isRemote) {
            detectBulletWhizz(player, world);
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

        for (HMGEntityBulletBase bullet : bullets) {
            //test
            // Skip bullets fired by this player
            Entity shooter = bullet.getThrower(); // or bullet.shootingEntity if that's the actual field
            if (shooter == player) continue;

            int bulletId = bullet.getEntityId();
            if (!trackedBullets.contains(bulletId)) {
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
            }
        }

        // Cleanup stale bullets
        trackedBullets.removeIf(id -> world.getEntityByID(id) == null);
    }
}
