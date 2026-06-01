package handmadeguns.entity.bullets;


import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import handmadeguns.network.PacketSpawnParticle;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

import static handmadeguns.HandmadeGunsCore.HMG_proxy;

public class HMGEntityBulletRocket extends HMGEntityBulletExprode implements IEntityAdditionalSpawnData
{
	public HMGEntityBulletRocket(World worldIn) {
		super(worldIn);
	}
	public HMGEntityBulletRocket(World worldIn, Entity throwerIn, int damege, float bspeed, float bure) {
		super(worldIn, throwerIn, damege, bspeed, bure);
		this.bulletTypeName = "byfrou01_Rocket";
		this.canbounce = false;
		this.bouncerate = 0.1f;
		
	}

	@Override
	public void explode(double x, double y, double z, float level, boolean candestroy, float exl) {
		super.explode(x, y, z, level, candestroy, exl);
		this.setDead();
	}

	public HMGEntityBulletRocket(World worldIn, Entity throwerIn, int damege, float bspeed, float bure, float exl, boolean canex) {
		this(worldIn, throwerIn, damege, bspeed, bure);
		exlevel = exl;
		this.canex = canex;
		this.canbounce = false;
		this.bouncerate = 0.1f;
	}
	public HMGEntityBulletRocket(World worldIn, Entity throwerIn, int damege, float bspeed, float bure, float exl, boolean canex, String modelname) {
		super(worldIn, throwerIn, damege, bspeed, bure,exl,canex, modelname);
		exlevel = exl;
		this.canex = canex;
		this.canbounce = false;
		this.bouncerate = 0.1f;
	}
	public HMGEntityBulletRocket(World worldIn, double x, double y, double z) {
		super(worldIn, x, y, z);
	}

	public void onUpdate(){
		super.onUpdate();
		if(worldObj.isRemote && smoketexture == null){
			PacketSpawnParticle packet = new PacketSpawnParticle(posX, posY, posZ, -this.motionX / 8,
					-this.motionY / 8,
					-this.motionZ / 8, 1);
			packet.scale = smokeWidth;
			packet.fuse = smoketime;
			if (smokeglow) packet.id += 100;
			HMG_proxy.spawnParticles(packet);
		}
	}
}
