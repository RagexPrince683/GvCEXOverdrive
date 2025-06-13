package handmadeguns.entity.bullets;


import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import handmadeguns.items.GunInfo;
import handmadeguns.network.PacketSpawnParticle;
import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

import java.util.List;

import static handmadeguns.HandmadeGunsCore.HMG_proxy;

public class HMGEntityBulletRocket extends HMGEntityBulletExprode implements IEntityAdditionalSpawnData
{
	protected GunInfo gunInfo;
	protected int power;

	//what the actual fuck is this spaghetti nightmare code
	public HMGEntityBulletRocket(World worldIn) {
		super(worldIn);
	}
	public HMGEntityBulletRocket(World world, Entity thrower, GunInfo gunInfo, float speed, float spread, float exl, boolean canex) {
		super(world, thrower, gunInfo.power, speed, spread);
		this.gunInfo = gunInfo;
		this.power = gunInfo.power;
		this.bulletTypeName = "byfrou01_Rocket";
		this.canbounce = false;
		this.bouncerate = 0.1f;
		this.exlevel = exl;
		this.canex = canex;
	}


	@Override
	public void explode(double x, double y, double z, float level, boolean candestroy) {
		if (!worldObj.isRemote) {
			float explosionRadius = 3.5F; // adjust radius

			List<Entity> entities = worldObj.getEntitiesWithinAABBExcludingEntity(this,
					this.boundingBox.expand(explosionRadius, explosionRadius, explosionRadius));

			for (Entity target : entities) {
				if (!target.isDead && target.canBeCollidedWith()) {
					float distance = (float) this.getDistanceToEntity(target);

					// maxDamage based on gunInfo
					float maxDamage = gunInfo != null ? gunInfo.power : this.power;
					float damage = getDamageBasedOnDistance(distance, level, maxDamage);
					DamageSource ds = DamageSource.causeThrownDamage(this, this.thrower);

					target.attackEntityFrom(ds, damage);
				}
			}

			// Do the visual explosion
			worldObj.createExplosion(this, x, y, z, level, candestroy);
		}

		this.setDead();
	}

	private float getDamageBasedOnDistance(float distance, float explosionPower, float maxDamage) {
		float falloffStart = 0.5f;
		float falloffEnd = 3.5f;

		if (distance <= falloffStart) return maxDamage;
		if (distance >= falloffEnd) return 0;

		float falloffRange = falloffEnd - falloffStart;
		float scale = 1.0f - ((distance - falloffStart) / falloffRange);
		return maxDamage * scale;
	}

	//public HMGEntityBulletRocket(World worldIn, Entity throwerIn, int damege, float bspeed, float bure, float exl, boolean canex) {
	//	this(worldIn, throwerIn, damege, bspeed, bure);
	//	exlevel = exl;
	//	this.canex = canex;
	//	this.canbounce = false;
	//	this.bouncerate = 0.1f;
	//}
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
