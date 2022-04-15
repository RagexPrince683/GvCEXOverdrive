package hmggvcmob.ai.newai;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;

import javax.vecmath.Vector3d;
import java.util.Random;

public abstract class AIAttack extends EntityAIBase {
	protected final EntityLiving shooter;
	protected AIAttackManager aiAttackManager;
	protected Random rnd = new Random();

	public AIAttack(EntityLiving shooter,AIAttackManager aiAttackManager){
		this.shooter = shooter;
		this.aiAttackManager = aiAttackManager;
	}

	public abstract void movePosition();
	public abstract void aimTarget();
	public abstract void fireWeapon();
}
