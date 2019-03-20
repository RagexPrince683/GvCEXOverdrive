package hmgww2.entity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import hmgww2.Nation;
import hmgww2.mod_GVCWW2;
import hmgww2.blocks.BlockRUSFlagBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemSword;
import net.minecraft.world.World;

public class EntityUSSRBase extends EntityBases {
	
	
	public EntityUSSRBase(World par1World) {
		super(par1World);
		this.flag = mod_GVCWW2.b_flag_rus;
		this.flag2 = mod_GVCWW2.b_flag2_rus;
		this.flag3 = mod_GVCWW2.b_flag3_rus;
		this.flag4 = mod_GVCWW2.b_flag2_rus;
	}
	
	@Override
	public Nation getnation() {
		return Nation.USSR;
	}
	
}
