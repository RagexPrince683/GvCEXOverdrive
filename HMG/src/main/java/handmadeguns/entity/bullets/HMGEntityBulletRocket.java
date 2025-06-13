package handmadeguns.entity.bullets;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import handmadeguns.items.GunInfo;
import handmadeguns.network.PacketSpawnParticle;
import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

import java.util.List;

import static handmadeguns.HandmadeGunsCore.HMG_proxy;

public class HMGEntityBulletRocket extends HMGEntityBulletExprode implements IEntityAdditionalSpawnData {

	protected GunInfo gunInfo;

	public HMGEntityBulletRocket(World world) {
		super(world);
	}

	public HMGEntityBulletRocket(World world, Entity thrower, GunInfo gunInfo, float speed, float spread, float exLevel, boolean canDestroy) {
		super(world, thrower, gunInfo.power, speed, spread);
		this.gunInfo = gunInfo;
		this.exlevel = exLevel;
		this.canex = canDestroy;
		this.bulletTypeName = "byfrou01_Rocket";
		this.canbounce = false;
		this.bouncerate = 0.1f;
	}

	public HMGEntityBulletRocket(World world, Entity thrower, int rawDamage, float speed, float spread, float exLevel, boolean canDestroy, String modelName) {
		super(world, thrower, rawDamage, speed, spread, exLevel, canDestroy, modelName);
		this.exlevel = exLevel;
		this.canex = canDestroy;
		this.canbounce = false;
		this.bouncerate = 0.1f;
	}

	public HMGEntityBulletRocket(World world, double x, double y, double z) {
		super(world, x, y, z);
	}

	@Override
	public void explode(double x, double y, double z, float level, boolean canDestroy) {
		if (!worldObj.isRemote) {
			System.out.println("[HMG] === EXPLOSION START ===");
			System.out.printf("[HMG] Location: %.2f, %.2f, %.2f%n", x, y, z);
			System.out.println("[HMG] Explosion Level: " + level + ", CanDestroy: " + canDestroy);
			System.out.println("[HMG] gunInfo: " + (gunInfo != null ? gunInfo.toString() : "null"));

			float explosionRadius = 3.5F;
			float maxDamage = gunInfo != null ? gunInfo.power : this.exlevel * 100; // fallback damage

			List<Entity> entities = worldObj.getEntitiesWithinAABBExcludingEntity(this,
					this.boundingBox.expand(explosionRadius, explosionRadius, explosionRadius));
			System.out.println("[HMG] Affected Entities: " + entities.size());

			for (Entity target : entities) {
				if (!target.isDead && target.canBeCollidedWith()) {
					float distance = (float) this.getDistanceToEntity(target);
					float damage = getDamageBasedOnDistance(distance, explosionRadius, maxDamage);
					DamageSource ds = DamageSource.causeThrownDamage(this, this.thrower);

					System.out.println("[HMG] Target: " + target.getClass().getSimpleName());
					System.out.printf("  > Distance: %.2f%n", distance);
					System.out.printf("  > Damage Applied: %.2f%n", damage);

					target.attackEntityFrom(ds, damage);
				}
			}

			System.out.println("[HMG] Creating explosion effect...");
			worldObj.createExplosion(this, x, y, z, level, canDestroy);
			System.out.println("[HMG] === EXPLOSION END ===");
		}

		this.setDead();
	}

	private float getDamageBasedOnDistance(float distance, float maxDistance, float maxDamage) {
		float falloffStart = 0.5f;
		float falloffEnd = maxDistance;

		if (distance <= falloffStart) return maxDamage;
		if (distance >= falloffEnd) return 0;

		float falloffRange = falloffEnd - falloffStart;
		float scale = 1.0f - ((distance - falloffStart) / falloffRange);
		return maxDamage * scale;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		if (worldObj.isRemote && smoketexture == null) {
			PacketSpawnParticle packet = new PacketSpawnParticle(posX, posY, posZ,
					-this.motionX / 8, -this.motionY / 8, -this.motionZ / 8, 1);
			packet.scale = smokeWidth;
			packet.fuse = smoketime;
			if (smokeglow) packet.id += 100;
			HMG_proxy.spawnParticles(packet);
		}
	}
}
