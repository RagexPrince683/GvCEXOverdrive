package handmadeguns.items;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class FireTemp {
	public int power;
	public int fuse;
	public float speed;
	public String model = null;
	public float exlevel;
	public boolean destroyBlock;
	public boolean canDoorBreach;
	
	public double knockback;
	public double knockbackY;
	public float  bouncerate;
	public float  bouncelimit;
	public float  resistance;
	public float  acceleration;
	public float  gra;
	public float spread;
	public int pellet;
	public double bulletStability;
	public float damageRange;
	public float resistanceInWater;
	public boolean canbounce;
	public int accelerationDelay;
	public int accelerationFuse;
	public int bulletType;
	public FireTemp(){}
	public FireTemp(GunInfo gunInfo){
		this.power = gunInfo.power;
		this.fuse = gunInfo.fuse;
		this.speed = gunInfo.speed;
		this.exlevel = gunInfo.ex;
		this.destroyBlock = gunInfo.destroyBlock;
		
		this.knockback = gunInfo.knockback;
		this.knockbackY = gunInfo.knockbackY;
		this.bouncerate = gunInfo.bouncerate;
		this.bouncelimit = gunInfo.bouncelimit;
		this.resistance = gunInfo.resistance;
		this.acceleration = gunInfo.acceleration;
		this.gra = gunInfo.gravity;
		this.spread = gunInfo.spread_setting;
		this.pellet = Math.max(1, gunInfo.pellet);
		this.bulletStability = gunInfo.bulletStability;
		this.damageRange = gunInfo.damagerange;
		this.resistanceInWater = gunInfo.resistanceinWater;
		this.canbounce = gunInfo.canbounce;
		this.accelerationDelay = gunInfo.accelerationDelay;
		this.accelerationFuse = gunInfo.accelerationFuse;
		this.bulletType = -1;
	}
	public void applyMagOption(ItemStack ammunitionStack){
		if(ammunitionStack != null) applyMagOption(ammunitionStack.getItem());
	}
	public void applyMagOption(Item item){
		if(item instanceof HMGItemCustomMagazine) applyMagOption((HMGItemCustomMagazine)item);
	}
	public void applyMagOption(HMGItemCustomMagazine magazine){
		// Absolute values use gun-TXT units; legacy multipliers intentionally run second.
		if(magazine.powerOverride != null)this.power = magazine.powerOverride;
		if(magazine.speedOverride != null && Float.isFinite(magazine.speedOverride))this.speed = magazine.speedOverride;
		this.power *= magazine.damagemodify;
		this.speed *= magazine.speedmodify;
		if(magazine.fuseOverride != null)this.fuse = magazine.fuseOverride;
		else if(magazine.fuse != -1)this.fuse = magazine.fuse;
		if(magazine.explosionlevel != -1)this.exlevel = magazine.explosionlevel;
		//todo this is causing mch vehicles to take way less damage than they should from anti tank weaponry
		// specifically explosive weaponry because the math for MCH is wrong for explosion calcs but also the logic for this is weak
		//
		if(magazine.blockDestroyOverride != null)this.destroyBlock = magazine.blockDestroyOverride;
		else this.destroyBlock &= magazine.blockdestroyex;
		if(magazine.canDoorBreachOverride != null)this.canDoorBreach = magazine.canDoorBreachOverride;
		if(magazine.bulletmodel != null)this.model = magazine.bulletmodel;
		
		if(Double.isFinite(magazine.knockback))this.knockback = magazine.knockback;
		if(Double.isFinite(magazine.knockbackY))this.knockbackY = magazine.knockbackY;
		if(Float.isFinite(magazine.bouncerate))this.bouncerate = magazine.bouncerate;
		if(Float.isFinite(magazine.bouncelimit))this.bouncelimit = magazine.bouncelimit;
		if(Float.isFinite(magazine.resistance))this.resistance = magazine.resistance;
		if(Float.isFinite(magazine.acceleration))this.acceleration = magazine.acceleration;
		if(Float.isFinite(magazine.gra))this.gra = magazine.gra;
		if(magazine.spreadOverride != null && Float.isFinite(magazine.spreadOverride))this.spread = magazine.spreadOverride;
		if(magazine.pelletOverride != null)this.pellet = Math.max(1, magazine.pelletOverride);
		if(magazine.bulletStabilityOverride != null && Double.isFinite(magazine.bulletStabilityOverride))this.bulletStability = magazine.bulletStabilityOverride;
		if(magazine.damageRangeOverride != null && Float.isFinite(magazine.damageRangeOverride))this.damageRange = magazine.damageRangeOverride;
		if(magazine.resistanceInWaterOverride != null && Float.isFinite(magazine.resistanceInWaterOverride))this.resistanceInWater = magazine.resistanceInWaterOverride;
		if(magazine.canBounceOverride != null)this.canbounce = magazine.canBounceOverride;
		if(magazine.accelerationDelayOverride != null)this.accelerationDelay = magazine.accelerationDelayOverride;
		if(magazine.accelerationFuseOverride != null)this.accelerationFuse = magazine.accelerationFuseOverride;
		if(magazine.bullettype != -1)this.bulletType = magazine.bullettype;
	}
}
