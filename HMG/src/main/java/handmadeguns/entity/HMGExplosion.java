package handmadeguns.entity;

import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
//import mcheli.aircraft.MCH_EntityAircraft;
//are you fucking retarded?
import java.util.List;

public class HMGExplosion extends Explosion {

	private final World worldObj;

	public HMGExplosion(World world, Entity exploder, double x, double y, double z, float size) {
		super(world, exploder, x, y, z, size);
		this.worldObj = world;
	}

	@Override
	public void doExplosionA() {
		// Skip vanilla block damage
	}

	@Override
	public void doExplosionB(boolean doParticles) {
		float radius = this.explosionSize * 2.0F;
		AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(
				explosionX - radius, explosionY - radius, explosionZ - radius,
				explosionX + radius, explosionY + radius, explosionZ + radius
		);

		List<Entity> entities = worldObj.getEntitiesWithinAABB(Entity.class, aabb);

		for (Entity entity : entities) {
			if (entity == this.exploder) continue;

			double dx = entity.posX - explosionX;
			double dy = entity.posY + (double) entity.getEyeHeight() - explosionY;
			double dz = entity.posZ - explosionZ;
			double distSq = dx * dx + dy * dy + dz * dz;

			if (distSq > radius * radius) continue;

			double distance = Math.sqrt(distSq);
			double exposure = (1.0 - distance / radius);

			float damage = (this.explosionSize * 100F) * (float) exposure; // tune multiplier

			// Force explosion damage handling in MCH_EntityAircraft
			DamageSource ds = DamageSource.setExplosionSource(this);
			try {
				entity.attackEntityFrom(ds, damage);
			} catch (Exception ex) {
				ex.printStackTrace(); // don't crash the server
			}
		}
	}
}
