package handmadeguns;

import handmadeguns.items.guns.HMGItem_Unified_Guns;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

import java.util.List;

import static handmadeguns.HandmadeGunsCore.manualGunPickupOnlyGuns;
import static handmadeguns.HandmadeGunsCore.manualGunPickupRange;
import static handmadeguns.HandmadeGunsCore.manualGunPickupRequiresLineOfSight;

public class HMGManualGunPickup {

    public static boolean isManualPickupStack(ItemStack stack) {
        if (stack == null || stack.getItem() == null) return false;
        if (manualGunPickupOnlyGuns) return stack.getItem() instanceof HMGItem_Unified_Guns;

        Item item = stack.getItem();
        return item instanceof HMGItem_Unified_Guns || item.getClass().getName().startsWith("handmadeguns.");
    }

    public static boolean isManualPickupEntity(EntityItem entityItem) {
        return entityItem != null && !entityItem.isDead && isManualPickupStack(entityItem.getEntityItem());
    }

    public static boolean isHMGGunStack(ItemStack stack) {
        return stack != null && stack.getItem() instanceof HMGItem_Unified_Guns;
    }

    public static EntityItem getLookedAtGunItem(EntityPlayer player, double range) {
        if (player == null || player.worldObj == null) return null;
        Vec3 start = Vec3.createVectorHelper(player.posX, player.posY + player.getEyeHeight(), player.posZ);
        Vec3 look = player.getLook(1.0F);
        Vec3 end = start.addVector(look.xCoord * range, look.yCoord * range, look.zCoord * range);

        AxisAlignedBB search = player.boundingBox.addCoord(look.xCoord * range, look.yCoord * range, look.zCoord * range).expand(1.0D, 1.0D, 1.0D);
        List list = player.worldObj.getEntitiesWithinAABB(EntityItem.class, search);
        EntityItem closest = null;
        double closestDistance = range * range;

        for (Object object : list) {
            EntityItem entityItem = (EntityItem) object;
            if (!isManualPickupEntity(entityItem)) continue;

            AxisAlignedBB box = entityItem.boundingBox.expand(0.25D, 0.25D, 0.25D);
            MovingObjectPosition hit = box.calculateIntercept(start, end);
            if (hit == null) continue;

            double distance = start.squareDistanceTo(hit.hitVec);
            if (distance < closestDistance) {
                closest = entityItem;
                closestDistance = distance;
            }
        }

        return closest;
    }

    public static boolean canServerPickup(EntityPlayer player, EntityItem entityItem) {
        if (player == null || entityItem == null || player.isDead || entityItem.isDead) return false;
        if (player.worldObj == null || entityItem.worldObj != player.worldObj) return false;
        if (!isManualPickupEntity(entityItem)) return false;

        double range = Math.max(0.1D, manualGunPickupRange);
        double maxDistanceSq = range * range;
        if (player.getDistanceSqToEntity(entityItem) > maxDistanceSq) return false;

        EntityItem lookedAt = getLookedAtGunItem(player, range);
        if (lookedAt != entityItem) return false;

        if (manualGunPickupRequiresLineOfSight) {
            Vec3 start = Vec3.createVectorHelper(player.posX, player.posY + player.getEyeHeight(), player.posZ);
            Vec3 target = Vec3.createVectorHelper(entityItem.posX, entityItem.posY + entityItem.height * 0.5D, entityItem.posZ);
            MovingObjectPosition blockHit = player.worldObj.rayTraceBlocks(start, target);
            if (blockHit != null) return false;
        }

        return true;
    }

    public static boolean pickup(EntityPlayer player, EntityItem entityItem) {
        if (!canServerPickup(player, entityItem)) return false;

        ItemStack entityStack = entityItem.getEntityItem();
        if (entityStack == null || entityStack.stackSize <= 0) return false;

        if (trySwapHeldGun(player, entityItem, entityStack)) return true;

        ItemStack insertStack = entityStack.copy();
        int originalSize = insertStack.stackSize;
        boolean fullyInserted = player.inventory.addItemStackToInventory(insertStack);
        int remaining = insertStack.stackSize;
        int pickedUp = originalSize - remaining;

        if (pickedUp <= 0) return false;

        player.inventory.markDirty();
        entityItem.playSound("random.pop", 0.2F, ((entityItem.worldObj.rand.nextFloat() - entityItem.worldObj.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
        player.onItemPickup(entityItem, pickedUp);

        if (fullyInserted || remaining <= 0) {
            entityItem.setDead();
        } else {
            entityStack.stackSize = remaining;
        }

        return true;
    }

    private static boolean trySwapHeldGun(EntityPlayer player, EntityItem entityItem, ItemStack entityStack) {
        ItemStack heldStack = player.getHeldItem();
        if (!isHMGGunStack(heldStack) || !isHMGGunStack(entityStack)) return false;

        int currentSlot = player.inventory.currentItem;
        if (currentSlot < 0 || currentSlot >= player.inventory.mainInventory.length) return false;

        ItemStack pickedUpStack = entityStack.copy();
        ItemStack droppedStack = heldStack.copy();
        player.inventory.mainInventory[currentSlot] = pickedUpStack;
        player.inventory.markDirty();
        if (player.inventoryContainer != null) {
            player.inventoryContainer.detectAndSendChanges();
        }

        entityItem.playSound("random.pop", 0.2F, ((entityItem.worldObj.rand.nextFloat() - entityItem.worldObj.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
        player.onItemPickup(entityItem, pickedUpStack.stackSize);
        entityItem.setEntityItemStack(droppedStack);
        entityItem.delayBeforeCanPickup = 10;
        entityItem.age = 0;
        entityItem.motionX = 0.0D;
        entityItem.motionY = 0.0D;
        entityItem.motionZ = 0.0D;
        return true;
    }
}
