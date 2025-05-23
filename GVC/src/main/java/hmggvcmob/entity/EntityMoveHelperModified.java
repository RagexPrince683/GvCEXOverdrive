package hmggvcmob.entity;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;

import static java.lang.Math.abs;

public class EntityMoveHelperModified extends EntityMoveHelper
{
	/** The EntityLiving that is being moved */
	private EntityLiving entity;
	private double posX;
	private double posY;
	private double posZ;
	/** The speed at which the entity should move */
	private double speed;
	private boolean update;
	private static final String __OBFID = "CL_00001573";

	public EntityMoveHelperModified(EntityLiving p_i1614_1_)
	{
		super(p_i1614_1_);
		this.entity = p_i1614_1_;
		this.posX = p_i1614_1_.posX;
		this.posY = p_i1614_1_.posY;
		this.posZ = p_i1614_1_.posZ;
	}

	public boolean isUpdating()
	{
		return this.update;
	}

	public double getSpeed()
	{
		return this.speed;
	}

	/**
	 * Sets the speed and location to move to
	 */
	public void setMoveTo(double p_75642_1_, double p_75642_3_, double p_75642_5_, double p_75642_7_)
	{
		if(this.entity.onGround || this.entity.isInWater()) {
			this.posX = p_75642_1_;
			this.posY = p_75642_3_;
			this.posZ = p_75642_5_;
			this.speed = p_75642_7_;
			this.update = true;
		}
	}

	public void onUpdateMoveHelper()
	{
		this.entity.setMoveForward(0.0F);

		if (this.update || !(this.entity.onGround || this.entity.isInWater()))
		{
			this.update = false;
			int i = MathHelper.floor_double(this.entity.boundingBox.minY);

			int i2 = MathHelper.floor_double(this.posX);
			int j = MathHelper.floor_double(this.posY);
			int k = MathHelper.floor_double(this.posZ);

			double d0 = this.posX - this.entity.posX;
			double d1 = this.posZ - this.entity.posZ;
			double d2 = this.posY - (double)i;
			double d3 = d0 * d0 + d2 * d2 + d1 * d1;

			if (d3 >= 0)
			{
				this.entity.rotationYaw = (float)(Math.atan2(d1, d0) * 180.0D / Math.PI) - 90.0F;
				this.entity.setAIMoveSpeed((float)(this.speed * this.entity.getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue()));

				if (d2 >= this.entity.stepHeight && abs(d0) < (entity.width + 0.4) && abs(d1) < (entity.width + 0.4))
				{
					if(entity.worldObj.getBlock(i2,j-1,k)!=null && entity.worldObj.getBlock(i2,j-1,k).getCollisionBoundingBoxFromPool(entity.worldObj, i2,j-1,k) != null && entity.worldObj.getBlock(i2,j-1,k) != Blocks.tallgrass)this.entity.getJumpHelper().setJumping();
				}
			}
		}
	}

	/**
	 * Limits the given angle to a upper and lower limit.
	 */
	private float limitAngle(float p_75639_1_, float p_75639_2_, float p_75639_3_)
	{
		float f3 = MathHelper.wrapAngleTo180_float(p_75639_2_ - p_75639_1_);

		if (f3 > p_75639_3_)
		{
			f3 = p_75639_3_;
		}

		if (f3 < -p_75639_3_)
		{
			f3 = -p_75639_3_;
		}

		return p_75639_1_ + f3;
	}
}
