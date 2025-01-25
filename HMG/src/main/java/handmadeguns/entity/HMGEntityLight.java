package handmadeguns.entity;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

public class HMGEntityLight extends Entity {
    //light range isn't actually the range of the light, its just a placeholder value to make sure we are holding a gun with a light.
    private boolean gunisheld; // Remove 'final' to allow updates
    private Entity sourceEntity;
    private int lastLightX = Integer.MIN_VALUE;
    private int lastLightY = Integer.MIN_VALUE;
    private int lastLightZ = Integer.MIN_VALUE;

    public HMGEntityLight(World world, Entity sourceEntity, boolean held) {
        super(world);
        this.sourceEntity = sourceEntity;
        this.setSize(0.5F, 0.5F);
        this.renderDistanceWeight = 10.0D;
        this.gunisheld = held; // Store the gun as being held
    }

    // Add a setter to allow updates
    public void setGunIsHeld(boolean held) {
        this.gunisheld = held;
    }



    @Override
    protected void entityInit() {
        // No additional data to initialize
    }

    @Override
    public void onUpdate() {

        if (gunisheld != true) { // Only process logic if holding gun
            return;
        }

        super.onUpdate();

        if (sourceEntity == null || sourceEntity.isDead) {
            this.setDead();
            return;
        }

        // Update position to follow the source entity
        this.setPosition(sourceEntity.posX, sourceEntity.posY + sourceEntity.getEyeHeight(), sourceEntity.posZ);

        if (sourceEntity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) sourceEntity;

            // Raytrace to find the exact position of the light
            Vec3 start = Vec3.createVectorHelper(player.posX, player.posY + player.getEyeHeight(), player.posZ);
            Vec3 lookVec = player.getLookVec();
            Vec3 end = start.addVector(lookVec.xCoord * 10, lookVec.yCoord * 10, lookVec.zCoord * 10); // Maximum distance of 10 blocks

            //raytraceblocks for the ancient dark arts of forge 1.7.10
            MovingObjectPosition hit = this.worldObj.func_147447_a(start, end, false, true, false);
            if (hit != null) {
                // Light position is where the ray hits a block
                int lightX = hit.blockX;
                int lightY = hit.blockY;
                int lightZ = hit.blockZ;

                // Adjust light position to the face of the block
                switch (hit.sideHit) {
                    case 0: lightY -= 1; break; // Bottom
                    case 1: lightY += 1; break; // Top
                    case 2: lightZ -= 1; break; // North
                    case 3: lightZ += 1; break; // South
                    case 4: lightX -= 1; break; // West
                    case 5: lightX += 1; break; // East
                }

                updateLightPosition(lightX, lightY, lightZ);
            } else {
                // If no block is hit, use the max distance
                int lightX = MathHelper.floor_double(end.xCoord);
                int lightY = MathHelper.floor_double(end.yCoord);
                int lightZ = MathHelper.floor_double(end.zCoord);
                updateLightPosition(lightX, lightY, lightZ);
            }
        }
    }

    private void updateLightPosition(int lightX, int lightY, int lightZ) {
        // If the light position changes, reset the old light
        if (lightX != lastLightX || lightY != lastLightY || lightZ != lastLightZ) {
            if (lastLightX != Integer.MIN_VALUE) {
                this.worldObj.setLightValue(EnumSkyBlock.Block, lastLightX, lastLightY, lastLightZ, 0);
            }

            // Update light at the new position
            this.worldObj.setLightValue(EnumSkyBlock.Block, lightX, lightY, lightZ, 15);
            lastLightX = lightX;
            lastLightY = lightY;
            lastLightZ = lightZ;
        }
    }

    @Override
    public void setDead() {

        if (gunisheld != true) { // Skip logic if gun is not held
            return;
        }

        super.setDead();

        // Ensure the light is removed when the entity is dead
        if (!this.worldObj.isRemote && lastLightX != Integer.MIN_VALUE) {
            this.worldObj.setLightValue(EnumSkyBlock.Block, lastLightX, lastLightY, lastLightZ, 0);
        }
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound tagCompound) {
        // No persistent data needed
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound tagCompound) {
        // No persistent data needed
    }

    @SideOnly(Side.CLIENT)
    @Override
    public int getBrightnessForRender(float partialTickTime) {
        return 15728880; // Maximum brightness
    }

    @SideOnly(Side.CLIENT)
    @Override
    public float getBrightness(float partialTickTime) {
        return 1.0F; // Maximum brightness
    }
}