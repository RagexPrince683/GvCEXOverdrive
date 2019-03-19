package com.lulan.shincolle.item;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import com.lulan.shincolle.reference.ID;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**meta:
 *    0:  5-Inch Single Cannon
 *    1:  6-Inch Single Cannon
 *    2:  5-Inch Twin Cannon
 *    3:  6-Inch Twin Rapid-Fire Cannon
 *    4:  5-Inch Twin Dual Purpose Cannon
 *    5:  12.5-Inch Twin Secondary Cannon
 *    6:  14-Inch Twin Cannon
 *    7:  16-Inch Twin Cannon
 *    8:  20-Inch Twin Cannon
 *    9:  8-Inch Triple Cannon
 *    10: 16-Inch Triple Cannon
 *    11: 15-Inch Fortress Gun
 */
public class EquipCannon extends BasicEquip {
	
	IIcon[] icons = new IIcon[3];
	
	
	public EquipCannon() {
		super();
		this.setUnlocalizedName("EquipCannon");
		this.types = 12;	//single = 2, twin = 7, triple = 3
	}
	
	@Override
	public void registerIcons(IIconRegister iconRegister) {	
		icons[0] = iconRegister.registerIcon(this.getUnlocalizedName().substring(this.getUnlocalizedName().indexOf(".")+1)+"0");
		icons[1] = iconRegister.registerIcon(this.getUnlocalizedName().substring(this.getUnlocalizedName().indexOf(".")+1)+"1");
		icons[2] = iconRegister.registerIcon(this.getUnlocalizedName().substring(this.getUnlocalizedName().indexOf(".")+1)+"2");
	}
	
	@Override
	public int getEquipTypeIDFromMeta(int meta) {
		switch(meta) {
		case 0:
		case 1:
			return ID.EquipType.CANNON_SI;
		case 2:
		case 3:
		case 4:
		case 5:
			return ID.EquipType.CANNON_TW_LO;
		case 6:
		case 7:
		case 8:
			return ID.EquipType.CANNON_TW_HI;
		case 9:
		case 10:
		case 11:
			return ID.EquipType.CANNON_TR;
		default:
			return 0;
		}
	}
	
	@Override
	public IIcon getIconFromDamage(int meta) {
		switch(this.getEquipTypeIDFromMeta(meta)) {
		case ID.EquipType.CANNON_SI:
			return this.icons[0];	//single cannon
		case ID.EquipType.CANNON_TW_LO:
		case ID.EquipType.CANNON_TW_HI:
			return this.icons[1];	//twin cannon
		case ID.EquipType.CANNON_TR:
			return this.icons[2];	//triple cannon
		default:
			return this.icons[0];
		}
	}
	
	//item glow effect
	@Override
	@SideOnly(Side.CLIENT)
    public boolean hasEffect(ItemStack item, int pass) {
		switch(this.getEquipTypeIDFromMeta(item.getItemDamage())) {
		case ID.EquipType.CANNON_TW_HI:
		case ID.EquipType.CANNON_TR:
			return true;
		}
		
        return false;
    }
	
	@Override
	public int[] getResourceValue(int meta) {
		switch(this.getEquipTypeIDFromMeta(meta)) {
		case ID.EquipType.CANNON_SI:	 //128
			return new int[] {itemRand.nextInt(4) + 5,
	  		  		  		  itemRand.nextInt(4) + 5,
	  		  		  		  itemRand.nextInt(5) + 11,
	  		  		  		  itemRand.nextInt(3) + 3};
		case ID.EquipType.CANNON_TW_LO:  //320
			return new int[] {itemRand.nextInt(7) + 10,
							  itemRand.nextInt(7) + 10,
							  itemRand.nextInt(8) + 16,
							  itemRand.nextInt(6) + 6};
		case ID.EquipType.CANNON_TW_HI:  //1600
			return new int[] {itemRand.nextInt(10) + 50,
							  itemRand.nextInt(15) + 70,
							  itemRand.nextInt(35) + 90,
							  itemRand.nextInt(20) + 80};
		case ID.EquipType.CANNON_TR:	 //4400
			return new int[] {itemRand.nextInt(60) + 170,
			  		  		  itemRand.nextInt(70) + 210,
			  		  		  itemRand.nextInt(80) + 250,
			  		  		  itemRand.nextInt(50) + 130};
		default:
			return new int[] {0, 0, 0, 0};
		}
	}
	

}
