package hmgww2.items;

import hmgww2.entity.*;
import hmgww2.entity.planes.*;
import hmgww2.mod_GVCWW2;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Facing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class ItemSpwanEntity extends Item {
	public int mob;

	public ItemSpwanEntity(int i) {
		super();
		this.mob = i;
		this.maxStackSize = 64;
	}

	public void SpawnEntity(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, World par3World, int par4, int par5, int par6) {
		switch (this.mob) {
			case 0: {

				int var12 = MathHelper.floor_double((double) (par2EntityPlayer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
				EntityJPN_S entityskeleton = new EntityJPN_S(par3World);
				entityskeleton.setLocationAndAngles(par4 + 0.5, par5 + 1, par6 + 0.5, var12, 0.0F);
				par3World.spawnEntityInWorld(entityskeleton);
				break;
			}
			case 1: {

				int var12 = MathHelper.floor_double((double) (par2EntityPlayer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
				EntityJPN_S entityskeleton = new EntityJPN_S(par3World);
				entityskeleton.setLocationAndAngles(par4 + 0.5, par5 + 1, par6 + 0.5, var12, 0.0F);
				entityskeleton.addRandomArmor();
				entityskeleton.setCurrentItemOrArmor(4, new ItemStack(mod_GVCWW2.armor_jpn));
				par3World.spawnEntityInWorld(entityskeleton);
				//entityskeleton.mountEntity(entityskeleton1);
				break;
			}
			case 2: {

				int var12 = MathHelper.floor_double((double) (par2EntityPlayer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
				EntityJPN_Tank entityskeleton = new EntityJPN_Tank(par3World);
				entityskeleton.setLocationAndAngles(par4 + 0.5, par5 + 1, par6 + 0.5, var12, 0.0F);
				entityskeleton.setMobMode(1);
				par3World.spawnEntityInWorld(entityskeleton);
				//entityskeleton.mountEntity(entityskeleton1);
				break;
			}
			case 3: {

				int var12 = MathHelper.floor_double((double) (par2EntityPlayer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
				EntityJPN_Fighter entityskeleton = new EntityJPN_Fighter(par3World);
				entityskeleton.setLocationAndAngles(par4 + 0.5, par5, par6 + 0.5, var12, 0.0F);
				entityskeleton.setMobMode(1);
				par3World.spawnEntityInWorld(entityskeleton);
				//entityskeleton.mountEntity(entityskeleton1);
				break;
			}
			case 4: {

				int var12 = MathHelper.floor_double((double) (par2EntityPlayer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
				EntityJPN_TankAA entityskeleton = new EntityJPN_TankAA(par3World);
				entityskeleton.setLocationAndAngles(par4 + 0.5, par5 + 1, par6 + 0.5, var12, 0.0F);
				entityskeleton.setMobMode(1);
				par3World.spawnEntityInWorld(entityskeleton);
				//entityskeleton.mountEntity(entityskeleton1);
				break;
			}
			case 5: {

				int var12 = MathHelper.floor_double((double) (par2EntityPlayer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
				EntityJPN_TankSPG entityskeleton = new EntityJPN_TankSPG(par3World);
				entityskeleton.setLocationAndAngles(par4 + 0.5, par5 + 1, par6 + 0.5, var12, 0.0F);
				entityskeleton.setMobMode(1);
				par3World.spawnEntityInWorld(entityskeleton);
				//entityskeleton.mountEntity(entityskeleton1);
				break;
			}
			case 6: {

				int var12 = MathHelper.floor_double((double) (par2EntityPlayer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
				EntityJPN_FighterA entityskeleton = new EntityJPN_FighterA(par3World);
				entityskeleton.setLocationAndAngles(par4 + 0.5, par5, par6 + 0.5, var12, 0.0F);
				entityskeleton.setMobMode(1);
				par3World.spawnEntityInWorld(entityskeleton);
				//entityskeleton.mountEntity(entityskeleton1);
				break;
			}
			case 7: {

				int var12 = MathHelper.floor_double((double) (par2EntityPlayer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
				EntityJPN_ShipB entityskeleton = new EntityJPN_ShipB(par3World);
				entityskeleton.setLocationAndAngles(par4 + 0.5, par5 + 1, par6 + 0.5, var12, 0.0F);
				entityskeleton.setMobMode(1);
				par3World.spawnEntityInWorld(entityskeleton);
				//entityskeleton.mountEntity(entityskeleton1);
				break;
			}
			case 8: {

				int var12 = MathHelper.floor_double((double) (par2EntityPlayer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
				EntityJPN_ShipD entityskeleton = new EntityJPN_ShipD(par3World);
				entityskeleton.setLocationAndAngles(par4 + 0.5, par5 + 1, par6 + 0.5, var12, 0.0F);
				entityskeleton.setMobMode(1);
				par3World.spawnEntityInWorld(entityskeleton);
				//entityskeleton.mountEntity(entityskeleton1);
				break;
			}
			case 21: {

				int var12 = MathHelper.floor_double((double) (par2EntityPlayer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
				EntityUSA_S entityskeleton = new EntityUSA_S(par3World);
				entityskeleton.setLocationAndAngles(par4 + 0.5, par5 + 1, par6 + 0.5, var12, 0.0F);
				entityskeleton.addRandomArmor();
				entityskeleton.setCurrentItemOrArmor(4, new ItemStack(mod_GVCWW2.armor_usa));
				par3World.spawnEntityInWorld(entityskeleton);
				//entityskeleton.mountEntity(entityskeleton1);
				break;
			}
			case 22: {

				int var12 = MathHelper.floor_double((double) (par2EntityPlayer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
				EntityUSA_Tank entityskeleton = new EntityUSA_Tank(par3World);
				entityskeleton.setLocationAndAngles(par4 + 0.5, par5 + 1, par6 + 0.5, var12, 0.0F);
				entityskeleton.setMobMode(1);
				par3World.spawnEntityInWorld(entityskeleton);
				//entityskeleton.mountEntity(entityskeleton1);
				break;
			}
			case 23: {

				int var12 = MathHelper.floor_double((double) (par2EntityPlayer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
				EntityUSA_Fighter entityskeleton = new EntityUSA_Fighter(par3World);
				entityskeleton.setLocationAndAngles(par4 + 0.5, par5, par6 + 0.5, var12, 0.0F);
				entityskeleton.setMobMode(1);
				par3World.spawnEntityInWorld(entityskeleton);
				//entityskeleton.mountEntity(entityskeleton1);
				break;
			}
			case 24: {

				int var12 = MathHelper.floor_double((double) (par2EntityPlayer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
				EntityUSA_TankAA entityskeleton = new EntityUSA_TankAA(par3World);
				entityskeleton.setLocationAndAngles(par4 + 0.5, par5 + 1, par6 + 0.5, var12, 0.0F);
				entityskeleton.setMobMode(1);
				par3World.spawnEntityInWorld(entityskeleton);
				//entityskeleton.mountEntity(entityskeleton1);
				break;
			}
			case 25: {

				int var12 = MathHelper.floor_double((double) (par2EntityPlayer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
				EntityUSA_TankSPG entityskeleton = new EntityUSA_TankSPG(par3World);
				entityskeleton.setLocationAndAngles(par4 + 0.5, par5 + 1, par6 + 0.5, var12, 0.0F);
				entityskeleton.setMobMode(1);
				par3World.spawnEntityInWorld(entityskeleton);
				//entityskeleton.mountEntity(entityskeleton1);
				break;
			}
			case 26: {

				int var12 = MathHelper.floor_double((double) (par2EntityPlayer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
				EntityUSA_FighterA entityskeleton = new EntityUSA_FighterA(par3World);
				entityskeleton.setLocationAndAngles(par4 + 0.5, par5, par6 + 0.5, var12, 0.0F);
				entityskeleton.setMobMode(1);
				par3World.spawnEntityInWorld(entityskeleton);
				//entityskeleton.mountEntity(entityskeleton1);
				break;
			}
			case 27: {

				int var12 = MathHelper.floor_double((double) (par2EntityPlayer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
				EntityUSA_ShipB entityskeleton = new EntityUSA_ShipB(par3World);
				entityskeleton.setLocationAndAngles(par4 + 0.5, par5 + 1, par6 + 0.5, var12, 0.0F);
				entityskeleton.setMobMode(1);
				par3World.spawnEntityInWorld(entityskeleton);
				//entityskeleton.mountEntity(entityskeleton1);
				break;
			}
			case 28: {

				int var12 = MathHelper.floor_double((double) (par2EntityPlayer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
				EntityUSA_ShipD entityskeleton = new EntityUSA_ShipD(par3World);
				entityskeleton.setLocationAndAngles(par4 + 0.5, par5 + 1, par6 + 0.5, var12, 0.0F);
				entityskeleton.setMobMode(1);
				par3World.spawnEntityInWorld(entityskeleton);
				//entityskeleton.mountEntity(entityskeleton1);
				break;
			}
			case 41: {

				int var12 = MathHelper.floor_double((double) (par2EntityPlayer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
				EntityGER_S entityskeleton = new EntityGER_S(par3World);
				entityskeleton.setLocationAndAngles(par4 + 0.5, par5 + 1, par6 + 0.5, var12, 0.0F);
				entityskeleton.addRandomArmor();
				entityskeleton.setCurrentItemOrArmor(4, new ItemStack(mod_GVCWW2.armor_ger));
				par3World.spawnEntityInWorld(entityskeleton);
				//entityskeleton.mountEntity(entityskeleton1);
				break;
			}
			case 42: {

				int var12 = MathHelper.floor_double((double) (par2EntityPlayer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
				EntityGER_Tank entityskeleton = new EntityGER_Tank(par3World);
				entityskeleton.setLocationAndAngles(par4 + 0.5, par5 + 1, par6 + 0.5, var12, 0.0F);
				entityskeleton.setMobMode(1);
				par3World.spawnEntityInWorld(entityskeleton);
				//entityskeleton.mountEntity(entityskeleton1);
				break;
			}
			case 43: {

				int var12 = MathHelper.floor_double((double) (par2EntityPlayer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
				EntityGER_Fighter entityskeleton = new EntityGER_Fighter(par3World);
				entityskeleton.setLocationAndAngles(par4 + 0.5, par5, par6 + 0.5, var12, 0.0F);
				entityskeleton.setMobMode(1);
				par3World.spawnEntityInWorld(entityskeleton);
				//entityskeleton.mountEntity(entityskeleton1);
				break;
			}
			case 44: {

				int var12 = MathHelper.floor_double((double) (par2EntityPlayer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
				EntityGER_TankAA entityskeleton = new EntityGER_TankAA(par3World);
				entityskeleton.setLocationAndAngles(par4 + 0.5, par5 + 1, par6 + 0.5, var12, 0.0F);
				entityskeleton.setMobMode(1);
				par3World.spawnEntityInWorld(entityskeleton);
				//entityskeleton.mountEntity(entityskeleton1);
				break;
			}
			case 45: {

				int var12 = MathHelper.floor_double((double) (par2EntityPlayer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
				EntityGER_TankSPG entityskeleton = new EntityGER_TankSPG(par3World);
				entityskeleton.setLocationAndAngles(par4 + 0.5, par5 + 1, par6 + 0.5, var12, 0.0F);
				entityskeleton.setMobMode(1);
				par3World.spawnEntityInWorld(entityskeleton);
				//entityskeleton.mountEntity(entityskeleton1);
				break;
			}
			case 46: {

				int var12 = MathHelper.floor_double((double) (par2EntityPlayer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
				EntityGER_FighterA entityskeleton = new EntityGER_FighterA(par3World);
				entityskeleton.setLocationAndAngles(par4 + 0.5, par5, par6 + 0.5, var12, 0.0F);
				entityskeleton.setMobMode(1);
				par3World.spawnEntityInWorld(entityskeleton);
				//entityskeleton.mountEntity(entityskeleton1);
				break;
			}
			case 47: {

				int var12 = MathHelper.floor_double((double) (par2EntityPlayer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
				EntityGER_TankH entityskeleton = new EntityGER_TankH(par3World);
				entityskeleton.setLocationAndAngles(par4 + 0.5, par5 + 1, par6 + 0.5, var12, 0.0F);
				entityskeleton.setMobMode(1);
				par3World.spawnEntityInWorld(entityskeleton);
				//entityskeleton.mountEntity(entityskeleton1);
				break;
			}
			case 48: {

				int var12 = MathHelper.floor_double((double) (par2EntityPlayer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
				EntityGER_ShipSUB entityskeleton = new EntityGER_ShipSUB(par3World);
				entityskeleton.setLocationAndAngles(par4 + 0.5, par5 + 1, par6 + 0.5, var12, 0.0F);
				entityskeleton.setMobMode(1);
				par3World.spawnEntityInWorld(entityskeleton);
				//entityskeleton.mountEntity(entityskeleton1);
				break;
			}
			case 61: {

				int var12 = MathHelper.floor_double((double) (par2EntityPlayer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
				EntityUSSR_S entityskeleton = new EntityUSSR_S(par3World);
				entityskeleton.setLocationAndAngles(par4 + 0.5, par5 + 1, par6 + 0.5, var12, 0.0F);
				entityskeleton.addRandomArmor();
				entityskeleton.setCurrentItemOrArmor(4, new ItemStack(mod_GVCWW2.armor_rus));
				par3World.spawnEntityInWorld(entityskeleton);
				//entityskeleton.mountEntity(entityskeleton1);
				break;
			}
			case 62: {

				int var12 = MathHelper.floor_double((double) (par2EntityPlayer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
				EntityUSSR_Tank entityskeleton = new EntityUSSR_Tank(par3World);
				entityskeleton.setLocationAndAngles(par4 + 0.5, par5 + 1, par6 + 0.5, var12, 0.0F);
				entityskeleton.setMobMode(1);
				par3World.spawnEntityInWorld(entityskeleton);
				//entityskeleton.mountEntity(entityskeleton1);
				break;
			}
			case 63: {

				int var12 = MathHelper.floor_double((double) (par2EntityPlayer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
				EntityUSSR_Fighter entityskeleton = new EntityUSSR_Fighter(par3World);
				entityskeleton.setLocationAndAngles(par4 + 0.5, par5, par6 + 0.5, var12, 0.0F);
				entityskeleton.setMobMode(0);
				entityskeleton.onGround = false;
				entityskeleton.motionX = entityskeleton.motionY = entityskeleton.motionZ = 0;
				par3World.spawnEntityInWorld(entityskeleton);
				//entityskeleton.mountEntity(entityskeleton1);
				break;
			}
			case 64: {

				int var12 = MathHelper.floor_double((double) (par2EntityPlayer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
				EntityUSSR_TankAA entityskeleton = new EntityUSSR_TankAA(par3World);
				entityskeleton.setLocationAndAngles(par4 + 0.5, par5 + 1, par6 + 0.5, var12, 0.0F);
				entityskeleton.setMobMode(1);
				par3World.spawnEntityInWorld(entityskeleton);
				//entityskeleton.mountEntity(entityskeleton1);
				break;
			}
			case 65: {

				int var12 = MathHelper.floor_double((double) (par2EntityPlayer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
				EntityUSSR_TankSPG entityskeleton = new EntityUSSR_TankSPG(par3World);
				entityskeleton.setLocationAndAngles(par4 + 0.5, par5 + 1, par6 + 0.5, var12, 0.0F);
				entityskeleton.setMobMode(1);
				par3World.spawnEntityInWorld(entityskeleton);
				//entityskeleton.mountEntity(entityskeleton1);
				break;
			}
			case 66: {

				int var12 = MathHelper.floor_double((double) (par2EntityPlayer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
				EntityUSSR_FighterA entityskeleton = new EntityUSSR_FighterA(par3World);
				entityskeleton.setLocationAndAngles(par4 + 0.5, par5, par6 + 0.5, var12, 0.0F);
				entityskeleton.setMobMode(0);
				par3World.spawnEntityInWorld(entityskeleton);
				//entityskeleton.mountEntity(entityskeleton1);
				break;
			}
			case 67: {

				int var12 = MathHelper.floor_double((double) (par2EntityPlayer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
				EntityUSSR_TankH entityskeleton = new EntityUSSR_TankH(par3World);
				entityskeleton.setLocationAndAngles(par4 + 0.5, par5 + 1, par6 + 0.5, var12, 0.0F);
				entityskeleton.setMobMode(1);
				par3World.spawnEntityInWorld(entityskeleton);
				//entityskeleton.mountEntity(entityskeleton1);
				break;
			}
		}
	}

	public boolean onItemUse(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, World par3World, int par4, int par5, int par6, int par7, float par8, float par9, float par10) {
		if (par3World.isRemote) {
			return true;
		} else {
			Block block = par3World.getBlock(par4, par5, par6);
			par4 += Facing.offsetsXForSide[par7];
			par5 += Facing.offsetsYForSide[par7];
			par6 += Facing.offsetsZForSide[par7];
			double d0 = 0.0D;

			if (par7 == 1 && block.getRenderType() == 11) {
				d0 = 0.5D;
			}
			{
				this.SpawnEntity(par1ItemStack, par2EntityPlayer, par3World, par4, par5, par6);

				if (!par2EntityPlayer.capabilities.isCreativeMode) {
					--par1ItemStack.stackSize;
				}
			}

			return true;
		}
	}

	public ItemStack onItemRightClick(ItemStack p_77659_1_, World p_77659_2_, EntityPlayer p_77659_3_) {
		if (p_77659_2_.isRemote) {
			return p_77659_1_;
		} else {
			MovingObjectPosition movingobjectposition = this.getMovingObjectPositionFromPlayer(p_77659_2_, p_77659_3_, true);

			if (movingobjectposition == null) {
				return p_77659_1_;
			} else {
				if (movingobjectposition.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
					int i = movingobjectposition.blockX;
					int j = movingobjectposition.blockY;
					int k = movingobjectposition.blockZ;

					if (!p_77659_2_.canMineBlock(p_77659_3_, i, j, k)) {
						return p_77659_1_;
					}

					if (!p_77659_3_.canPlayerEdit(i, j, k, movingobjectposition.sideHit, p_77659_1_)) {
						return p_77659_1_;
					}

					if (p_77659_2_.getBlock(i, j, k) instanceof BlockLiquid) {
						{
							this.SpawnEntity(p_77659_1_, p_77659_3_, p_77659_2_, i, j, k);

							if (!p_77659_3_.capabilities.isCreativeMode) {
								--p_77659_1_.stackSize;
							}
						}
					}
				}

				return p_77659_1_;
			}
		}
	}
}