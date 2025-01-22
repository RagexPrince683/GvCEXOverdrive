package handmadevehicle.Items;

import handmadevehicle.AddNewVehicle;
import handmadevehicle.entity.EntityVehicle;
import handmadevehicle.entity.parts.turrets.WeaponCategory;
import handmadevehicle.entity.prefab.Prefab_AttachedWeapon;
import handmadevehicle.entity.prefab.Prefab_Seat;
//import handmadevehicle.entity.prefab.Prefab_Vehicle_Base;
import handmadevehicle.entity.prefab.Prefab_WeaponCategory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import java.util.List;

import static handmadevehicle.HMVehicle.tabHMV;

public class ItemVehicle extends Item {
	
	public String dataName ;
	//Prefab_Vehicle_Base infos;
	
	public ItemVehicle(String name)
	{
		super();
		this.setTextureName(name);
		this.dataName = name;
		this.maxStackSize = 64;
		//infos = AddNewVehicle.seachInfo(dataName);
	}

	public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List par3List, boolean par4){
		//par3List.add(EnumChatFormatting.GREEN + "Vehicle HP" + infos.maxhealth);
		//if(infos.prefab_attachedWeapons_all != null){
		//	par3List.add(EnumChatFormatting.RED + "Vehicle Weapons");
		//	for (Prefab_AttachedWeapon weapon : infos.prefab_attachedWeapons_all) {
		//		if(weapon.linkedGunStackID == -1){
		//			StringBuilder weaponAmmo = new StringBuilder(", ammunition : ");
//
		//			if (weapon.prefab_turret.gunInfo.magazine != null) {
		//				for (Item magazine : weapon.prefab_turret.gunInfo.magazine)
		//					if(magazine != null)weaponAmmo.append(magazine.getItemStackDisplayName(new ItemStack(magazine)));
		//			}
		//			String turretName = weapon.prefab_turret.turretName;
		//			if(turretName == null){
		//				if(weapon.prefab_turret.attachedItem != null)turretName = weapon.prefab_turret.attachedItem.getItemStackDisplayName(new ItemStack(weapon.prefab_turret.attachedItem));
		//			}
		//			par3List.add(EnumChatFormatting.WHITE + turretName + weaponAmmo.toString());
		//		} else if(weapon.prefab_turret.gunStackwhitelist != null){
		//			StringBuilder validWeapons = new StringBuilder(EnumChatFormatting.WHITE + "weapon Mount");
		//			for(String a_validWeapon : weapon.prefab_turret.gunStackwhitelist){
		//				validWeapons.append(" : ").append(a_validWeapon);
		//			}
		//			par3List.add(validWeapons.toString());
		//		}
//
		//	}
		//}
		//par3List.add(EnumChatFormatting.BLUE + "Seats : " + infos.prefab_seats.length);
		int weaponSeatNum = 0;
		//for(Prefab_Seat prefab_seat : infos.prefab_seats){
		//	if(prefab_seat.subid != -1 || prefab_seat.mainid != null)weaponSeatNum++;
		//}
		if(weaponSeatNum != 0)par3List.add(EnumChatFormatting.WHITE + "└WeaponSeat: " + weaponSeatNum);
		//if(infos.draft < infos.molded_depth)par3List.add(EnumChatFormatting.AQUA + "Amphibious");
	}
	public boolean onItemUse(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, World par3World, int par4, int par5, int par6, int par7, float par8, float par9, float par10) {
		if (par3World.isRemote) {
			return true;
		} else if (par7 != 1) {
			return false;
		} else {
//			par2EntityPlayer.addStat(power_of_number, 1);
			
			int var12 = MathHelper.floor_double((par2EntityPlayer.rotationYaw - 22.5)*8 / 360.0F)*45 + 45;
			EntityVehicle bespawningEntity = new EntityVehicle(par3World,dataName);
			
			bespawningEntity.setLocationAndAngles(par4 + 0.5, par5+1, par6 + 0.5, var12 , 0.0F);
			par3World.spawnEntityInWorld(bespawningEntity);
			--par1ItemStack.stackSize;
			return true;
		}
	}
}
