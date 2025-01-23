package handmadeguns.entity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

public class HMGEntityLight extends Entity {
    private Entity sourceEntity;
    private static final int LIGHT_RADIUS = 10;

    public HMGEntityLight(World world, Entity sourceEntity, float range) {
        super(world);
        this.sourceEntity = sourceEntity;
        this.setSize(0.5F, 0.5F);
        this.renderDistanceWeight = 10.0D;
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

        // Update position to follow the source entity
        this.setPosition(sourceEntity.posX, sourceEntity.posY + sourceEntity.getEyeHeight(), sourceEntity.posZ);

        if (sourceEntity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) sourceEntity;
            // Calculate the position where light is emitted
            Vec3 lookVec = player.getLookVec();
            int lightX = MathHelper.floor_double(this.posX + lookVec.xCoord * LIGHT_RADIUS);
            int lightY = MathHelper.floor_double(this.posY + lookVec.yCoord * LIGHT_RADIUS);
            int lightZ = MathHelper.floor_double(this.posZ + lookVec.zCoord * LIGHT_RADIUS);

            // Update light level at the target position
            this.worldObj.setLightValue(EnumSkyBlock.Block, lightX, lightY, lightZ, 15);
        }
    }

    @Override
    public void setDead() {
        super.setDead();

        // Ensure light is removed when the entity is dead
        if (!this.worldObj.isRemote && sourceEntity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) sourceEntity;
            Vec3 lookVec = player.getLookVec();
            int lightX = MathHelper.floor_double(this.posX + lookVec.xCoord * LIGHT_RADIUS);
            int lightY = MathHelper.floor_double(this.posY + lookVec.yCoord * LIGHT_RADIUS);
            int lightZ = MathHelper.floor_double(this.posZ + lookVec.zCoord * LIGHT_RADIUS);

            this.worldObj.setLightValue(EnumSkyBlock.Block, lightX, lightY, lightZ, 0);
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
