package handmadeguns.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import handmadeguns.items.HMGItemAttachment_light;
import cpw.mods.fml.relauncher.SideOnly;
import cpw.mods.fml.relauncher.Side;

public class HMGEntityLight extends Entity {
    private boolean gunisheld;
    private Entity sourceEntity;
    private int lastLightX = Integer.MIN_VALUE;
    private int lastLightY = Integer.MIN_VALUE;
    private int lastLightZ = Integer.MIN_VALUE;

    public HMGEntityLight(World world, Entity sourceEntity, boolean held) {
        super(world);
        this.sourceEntity = sourceEntity;
        this.setSize(0.5F, 0.5F);
        this.renderDistanceWeight = 10.0D;
        this.gunisheld = held;
    }

    // Add a setter to allow updates
    public void setGunIsHeld(boolean held) {
        this.gunisheld = held;
    }

    /**
     * Determines if the given item stack is a gun with a flashlight attachment.
     *
     * @param itemstack The item stack to check.
     * @return True if the item is a gun with a flashlight, false otherwise.
     */
    private boolean isGunWithFlashlight(ItemStack itemstack) {
        if (itemstack == null) return false;

        try {
            NBTTagCompound tagCompound = itemstack.getTagCompound();
            if (tagCompound != null) {
                NBTTagList tags = (NBTTagList) tagCompound.getTag("Items");
                if (tags != null) {
                    for (int i = 0; i < tags.tagCount(); i++) {
                        NBTTagCompound attachmentTag = tags.getCompoundTagAt(i);
                        int slot = attachmentTag.getByte("Slot");

                        // Check the slot for the flashlight attachment
                        if (slot == 2) { // Assuming slot 2 is for flashlight attachments
                            ItemStack attachment = ItemStack.loadItemStackFromNBT(attachmentTag);
                            if (attachment != null && attachment.getItem() instanceof HMGItemAttachment_light) {
                                return true;
                            }
                        }
                    }
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    protected void entityInit() {
        // No additional data to initialize
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (sourceEntity == null || sourceEntity.isDead) {
            this.setDead();
            return;
        }

        if (sourceEntity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) sourceEntity;

            // Check if the player is holding a gun with a flashlight attachment
            ItemStack heldItem = player.getHeldItem();
            gunisheld = isGunWithFlashlight(heldItem);

            // If no gun with a flashlight is held, remove the light entity
            if (!gunisheld) {
                this.setDead();
                return;
            }

            // Raytrace to calculate light position
            Vec3 start = Vec3.createVectorHelper(player.posX, player.posY + player.getEyeHeight(), player.posZ);
            Vec3 lookVec = player.getLookVec();
            Vec3 end = start.addVector(lookVec.xCoord * 10, lookVec.yCoord * 10, lookVec.zCoord * 10);

            MovingObjectPosition hit = this.worldObj.func_147447_a(start, end, false, true, false);
            if (hit != null) {
                int lightX = hit.blockX;
                int lightY = hit.blockY;
                int lightZ = hit.blockZ;

                // Adjust to the block face
                switch (hit.sideHit) {
                    case 0: lightY -= 1; break;
                    case 1: lightY += 1; break;
                    case 2: lightZ -= 1; break;
                    case 3: lightZ += 1; break;
                    case 4: lightX -= 1; break;
                    case 5: lightX += 1; break;
                }

                updateLightPosition(lightX, lightY, lightZ);
            } else {
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