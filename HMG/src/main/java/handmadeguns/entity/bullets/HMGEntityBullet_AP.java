package handmadeguns.entity.bullets;

//import littleMaidMobX.LMM_EntityLittleMaid;
//import littleMaidMobX.LMM_EntityLittleMaidAvatar;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import handmadeguns.HMGMessageKeyPressedC;
import handmadeguns.HMGPacketHandler;
import handmadeguns.HandmadeGunsCore;
import handmadeguns.entity.IFF;
import handmadeguns.entity.I_SPdamageHandle;
import io.netty.buffer.ByteBuf;
import littleMaidMobX.LMM_EntityLittleMaid;
import littleMaidMobX.LMM_EntityLittleMaidAvatar;
import littleMaidMobX.LMM_EntityLittleMaidAvatarMP;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraft.init.Blocks;

import static handmadeguns.HandmadeGunsCore.islmmloaded;

public class HMGEntityBullet_AP extends HMGEntityBulletBase implements IEntityAdditionalSpawnData
{
	private boolean hasPiercedBlock;
	public HMGEntityBullet_AP(World worldIn) {
		super(worldIn);
	}

	public HMGEntityBullet_AP(World worldIn, Entity throwerIn, int damege, float bspeed, float bure, String modelname) {
		super(worldIn, throwerIn, damege, bspeed, bure ,modelname );
	}

	public HMGEntityBullet_AP(World worldIn, double x, double y, double z) {
		super(worldIn, x, y, z);
	}


	@Override
	protected boolean canPenetrateBlock(MovingObjectPosition hit, Block block, int metadata) {
		if (hit == null || hit.hitVec == null || hit.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK || block == null
				|| block.isAir(worldObj, hit.blockX, hit.blockY, hit.blockZ)) return false;
		float resistance = block.getExplosionResistance(this, worldObj, hit.blockX, hit.blockY, hit.blockZ,
				hit.hitVec.xCoord, hit.hitVec.yCoord, hit.hitVec.zCoord);
		float stoneResistance = Blocks.stone.getExplosionResistance(this, worldObj, hit.blockX, hit.blockY, hit.blockZ,
				hit.hitVec.xCoord, hit.hitVec.yCoord, hit.hitVec.zCoord);
		return resistance <= stoneResistance;
	}

	@Override
	protected void onBlockPenetrated(MovingObjectPosition hit, Block block, int metadata) {
		hasPiercedBlock = true;
	}

	@Override
	protected float getImpactDamage() {
		return hasPiercedBlock ? Bdamege : Bdamege * 0.90F;
	}

	@Override
	protected float getBulletGravity() {
		return gra * 0.90F;
	}
	public void writeSpawnData(ByteBuf buffer){
		super.writeSpawnData(buffer);
	}
	public void readSpawnData(ByteBuf additionalData){
		super.readSpawnData(additionalData);
	}
}
