package handmadeguns.entity.bullets;


import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import handmadeguns.network.PacketSpawnParticle;
import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

import java.util.List;

import static handmadeguns.HandmadeGunsCore.HMG_proxy;

public class HMGEntityBulletRocket extends HMGEntityBulletExprode implements IEntityAdditionalSpawnData
{
	//what the actual fuck is this spaghetti nightmare code
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
	public void explode(double x, double y, double z, float level, boolean candestroy) {
		System.out.println("explode fired");
		if (!worldObj.isRemote) {
			// Handle damage to nearby entities
			List<Entity> entities = worldObj.getEntitiesWithinAABBExcludingEntity(this,
					this.boundingBox.expand(3.5D, 3.5D, 3.5D)); // ~7x7x7 damage radius (adjustable)
			//todo expand size to be the actual explosion size value

			for (Entity target : entities) {
				if (!target.isDead && target.canBeCollidedWith()) {
					float distance = (float) this.getDistanceToEntity(target);
					//float damage = this.getDamageBasedOnDistance(distance, level); // Your method (below)
					//todo get the fucking stupid ass fucking gunpower info or whatever the fuck we were working
					// with before the big ass error of death happened
					System.out.println("new explosion bullshit" + Bdamege + "bdamage" + this.thrower);
					//damage = this.whateverthefuck
					DamageSource ds = DamageSource.causeThrownDamage(this, this.thrower);

					// hopefully does the base damage that the projectile does as opposed to relying on the stupid fucking explosion value
					target.attackEntityFrom(ds, Bdamege);
					//holy shit this is a nightmare to work with
				}
			}

			// removed if statement
			//if (canex) {
				worldObj.createExplosion(this, x, y, z, level, candestroy); // handles both visual and terrain
			//}
		}

		this.setDead();
	}

	//private float getDamageBasedOnDistance(float distance, float explosionPower) {
	//	//float explosionPower is never used
	//	float maxDamage = (float)this.damage; // dip my balls in texas road house butter
	//	float falloffStart = 1.5f;
	//	float falloffEnd = 3.5f;
//
	//	if (distance <= falloffStart) return maxDamage;
	//	if (distance >= falloffEnd) return 0;
//
	//	float falloffRange = falloffEnd - falloffStart;
	//	float scale = 1.0f - ((distance - falloffStart) / falloffRange);
	//	return maxDamage * scale;
	//}
	//DOES NOT WORK, CAUSED A LOT OF CRASHES AND ISSUES

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
