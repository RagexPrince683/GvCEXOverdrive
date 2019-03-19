package com.lulan.shincolle.item;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import com.lulan.shincolle.reference.ID;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**meta:
 *    0:  Air Radar Mk.I
 *    1:  Air Radar Mk.II
 *    2:  Surface Radar Mk.I
 *    3:  Surface Radar Mk.II
 *    4:  Abyssal Sonar
 *    5:  Abyssal Air Radar
 *    6:  Abyssal Surface Radar
 *    7:  Abyssal Sonar Mk.II
 *    8:  Abyssal FCS + CIC
 */
public class EquipRadar extends BasicEquip {
	
	IIcon[] icons = new IIcon[1];
	
	
	public EquipRadar() {
		super();
		this.setUnlocalizedName("EquipRadar");
		this.types = 9;
	}
	
	@Override
	public void registerIcons(IIconRegister iconRegister) {	
		icons[0] = iconRegister.registerIcon(this.getUnlocalizedName().substring(this.getUnlocalizedName().indexOf(".")+1));
	}
	
	@Override
	public IIcon getIconFromDamage(int meta) {
	    return icons[0];
	}
	
	@Override
	public int getEquipTypeIDFromMeta(int meta) {
		switch(meta) {
		case 0:
		case 1:
		case 2:
		case 3:
		case 4:
			return ID.EquipType.RADAR_LO;
		case 5:
		case 6:
		case 7:
		case 8:
			return ID.EquipType.RADAR_HI;
		default:
			return 0;
		}
	}
	
	//item glow effect
	@Override
	@SideOnly(Side.CLIENT)
    public boolean hasEffect(ItemStack item, int pass) {
		switch(this.getEquipTypeIDFromMeta(item.getItemDamage())) {
		case ID.EquipType.RADAR_HI:
			return true;
		default:
			return false;
		}
    }
	
	@Override
	public int[] getResourceValue(int meta) {
		switch(this.getEquipTypeIDFromMeta(meta)) {
		case ID.EquipType.RADAR_LO:  //200
			return new int[] {itemRand.nextInt(7) + 12,
	  		  		  		  itemRand.nextInt(6) + 10,
	  		  		  		  itemRand.nextInt(5) + 9,
	  		  		  		  itemRand.nextInt(4) + 7};
		case ID.EquipType.RADAR_HI:  //2000
			return new int[] {itemRand.nextInt(40) + 110,
							  itemRand.nextInt(35) + 90,
							  itemRand.nextInt(30) + 70,
							  itemRand.nextInt(25) + 50};
		default:
			return new int[] {0, 0, 0, 0};
		}
	}
	

}

