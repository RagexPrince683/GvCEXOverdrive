package com.lulan.shincolle.item;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

/**meta:
 * 0: abyssium ingot
 * 1: polymetallic nodules
 */
public class AbyssMetal extends BasicItem implements IShipResourceItem, IShipFoodItem {	
	
	byte types = 2;
	IIcon[] icons = new IIcon[2];
	
	
	public AbyssMetal() {
		super();
		this.setUnlocalizedName("AbyssMetal");
		this.setHasSubtypes(true);
	}
	
	@Override
	public void getSubItems(Item item, CreativeTabs tab, List list) {
		for (int i=0; i<types; i++) {
			list.add(new ItemStack(item, 1, i));
		}
	}
	
	@Override
	public void registerIcons(IIconRegister iconRegister) {	
		icons[0] = iconRegister.registerIcon(this.getUnlocalizedName().substring(this.getUnlocalizedName().indexOf(".")+1)+"0");
		icons[1] = iconRegister.registerIcon(this.getUnlocalizedName().substring(this.getUnlocalizedName().indexOf(".")+1)+"1");
	}
	
	@Override
	public IIcon getIconFromDamage(int meta) {
		if(meta >= types) meta = types - 1;
	    return this.icons[meta];
	}

	@Override
	public int[] getResourceValue(int meta) {
		if(meta == 0) {
			return new int[] {0, 1, 0, 0};
		}
		else {
			return new int[] {0, 0, 0, 1};
		}
	}

	@Override
	public float getFoodValue(int meta) {
		return 30F;
	}

	@Override
	public float getSaturationValue(int meta) {
		return 0.8F;
	}

	@Override
	public int getSpecialEffect(int meta) {
		if (meta == 0)
		{
			return 2;
		}
		
		return 4;
	}

	
}
