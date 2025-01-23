package handmadeguns.event;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import handmadeguns.entity.PlacedGunEntity;
import handmadeguns.entity.bullets.HMGEntityBulletBase;
import handmadeguns.items.guns.HMGItem_Unified_Guns;
import handmadevehicle.entity.EntityVehicle;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import static net.minecraft.util.DamageSource.inWall;
import java.util.WeakHashMap;

public class HMGLivingUpdateEvent {
    private final WeakHashMap<EntityLivingBase, Boolean> headshotProcessed = new WeakHashMap<>();


    @SubscribeEvent
    public void onEntityHurt(LivingHurtEvent event) {
        if (!(event.source.getSourceOfDamage() instanceof Entity)) return;

        // Get the entity being attacked and the attacker
        EntityLivingBase entity = event.entityLiving;
        Entity attacker = event.source.getSourceOfDamage();

        // Ensure damage is only applied once per headshot
        if (headshotProcessed.getOrDefault(entity, false)) return;

        // Check if it's a headshot
        if (isHeadshot(entity, attacker)) {
            event.ammount *= 2.0F; // Double the damage for headshots
            entity.worldObj.playSoundAtEntity(entity, "random.orb", 1.0F, 1.0F); // Headshot sound

            // Mark this entity as having processed headshot damage
            headshotProcessed.put(entity, true);
        }
    }

    @SubscribeEvent
    public void onEntityUpdate(LivingEvent.LivingUpdateEvent event) {
        // Reset the headshotProcessed flag after the entity updates
        if (event.entity instanceof EntityLivingBase) {
            EntityLivingBase entity = (EntityLivingBase) event.entity;
            if (headshotProcessed.containsKey(entity)) {
                headshotProcessed.put(entity, false);
            }
        }
    }

    private boolean isHeadshot(EntityLivingBase entity, Entity attacker) {
        // Check for null or invalid input
        if (entity == null || attacker == null) {
            return false;
        }

        // Calculate the head region of the target
        double headHeight = entity.height * 0.25; // Top 25% of the entity is the head
        double headStartY = entity.posY + entity.height - headHeight;

        // Calculate the attacker's look vector
        Vec3 attackerLookVec = Vec3.createVectorHelper(
                -Math.sin(Math.toRadians(attacker.rotationYaw)) * Math.cos(Math.toRadians(attacker.rotationPitch)),
                -Math.sin(Math.toRadians(attacker.rotationPitch)),
                Math.cos(Math.toRadians(attacker.rotationYaw)) * Math.cos(Math.toRadians(attacker.rotationPitch))
        );

        // Calculate the attacker's position, accounting for eye height
        Vec3 attackerPosition = Vec3.createVectorHelper(
                attacker.posX,
                attacker.posY + attacker.getEyeHeight(),
                attacker.posZ
        );

        // Ensure the entity's bounding box is valid
        AxisAlignedBB entityBox = entity.boundingBox;
        if (entityBox == null) {
            return false;
        }

        // Perform the ray trace
        Vec3 rayEnd = attackerPosition.addVector(
                attackerLookVec.xCoord * 10, // Extend 10 blocks forward
                attackerLookVec.yCoord * 10,
                attackerLookVec.zCoord * 10
        );
        MovingObjectPosition hit = entityBox.calculateIntercept(attackerPosition, rayEnd);

        // Check if the ray hit the head region
        if (hit != null && hit.hitVec != null) {
            return hit.hitVec.yCoord >= headStartY;
        }

        //probably should return true?
        //return false;
        return true;
    }

    @SubscribeEvent
    public void canupdate(EntityEvent.CanUpdate event){
        if(event.entity instanceof HMGEntityBulletBase && !((HMGEntityBulletBase) event.entity).chunkLoaderBullet){
            event.entity.setDead();
        }
        if(event.entity instanceof EntityVehicle && ((EntityVehicle) event.entity).canDespawn){
            event.entity.setDead();
        }
    }
    @SubscribeEvent
    public void canupdate(LivingEvent.LivingUpdateEvent event){
        event.entity.worldObj.MAX_ENTITY_RADIUS = 40;
    }

    @SubscribeEvent
    public void entitydamaged(LivingHurtEvent event)
    {
        EntityLivingBase entity = event.entityLiving;

        if ((entity != null && entity.ridingEntity instanceof PlacedGunEntity)) {
            HMGItem_Unified_Guns item_unified_guns = ((PlacedGunEntity) entity.ridingEntity).
                    gunItem;
            if(item_unified_guns != null) {
                if(item_unified_guns.gunInfo.turretMaxHP != -1){
                    event.ammount = 0;
                    event.setCanceled(true);
                }else if(event.source == inWall){
                    event.ammount = 0;
                    event.setCanceled(true);
                    entity.hurtTime = 0;
                }
            }
        }
    }
    @SubscribeEvent
    public void livingAttackEvent(LivingAttackEvent event){
        EntityLivingBase entity = event.entityLiving;

        if ((entity != null && entity.ridingEntity instanceof PlacedGunEntity)) {
            HMGItem_Unified_Guns item_unified_guns = ((PlacedGunEntity) entity.ridingEntity).
                    gunItem;
            if(item_unified_guns != null) {
                if(item_unified_guns.gunInfo.turretMaxHP != -1){
                    event.setCanceled(true);
                }else if(event.source == inWall){
                    event.setCanceled(true);
                    entity.hurtTime = 0;
                }
            }
        }
    }
}
