package com.lulan.shincolle.block;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.lulan.shincolle.init.ModBlocks;
import com.lulan.shincolle.item.BasicEntityItem;
import com.lulan.shincolle.tileentity.TileMultiGrudgeHeavy;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockGrudgeHeavy extends BasicBlockMulti {
	public BlockGrudgeHeavy() {
		super(Material.sand);
		this.setBlockName("BlockGrudgeHeavy");
		this.setHarvestLevel("shovel", 0);
	    this.setHardness(3F);
	    this.setLightLevel(1F);
	    this.setStepSound(soundTypeSand);
	    this.setResistance(600F);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileMultiGrudgeHeavy();
	}
		
	//禁止該方塊產生掉落物, 所有掉落物都改在breakBlock生成
	@Override
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
		ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
		return ret;	//直接回傳空的array (不能傳null會噴出NPE)
    }
	
	//方塊放置時, 將物品的mats數量取出存到tile的nbt中
	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack itemstack) {
		TileEntity tile = world.getTileEntity(x, y, z);
		
		if(tile != null && tile instanceof TileMultiGrudgeHeavy) {
			//將mats資料存到matStock中
			if(itemstack.hasTagCompound()) {
				TileMultiGrudgeHeavy tile2 = (TileMultiGrudgeHeavy) tile;
				NBTTagCompound nbt = itemstack.getTagCompound();
				int[] mats = nbt.getIntArray("mats");
				int fuel = nbt.getInteger("fuel");
		        
				tile2.setMatStock(0, mats[0]);
				tile2.setMatStock(1, mats[1]);
				tile2.setMatStock(2, mats[2]);
				tile2.setMatStock(3, mats[3]);
				tile2.setPowerRemained(fuel);
			}
		}
	}
	
	//打掉方塊後, 掉落其內容物
	//heavy grudge打掉時, 會把matBuild跟matStock存在item的nbt中
	//注意tile會在這邊消滅掉, 所以getDrops呼叫時已經抓不到tile, 任何tile資料要留下的都要在此方法做完
	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
		TileEntity getTile = world.getTileEntity(x, y, z);
		TileMultiGrudgeHeavy tile = null;
		
		//確認抓到的是grudge heavy, 以防意外
		if(getTile instanceof TileMultiGrudgeHeavy) {
			tile = (TileMultiGrudgeHeavy)getTile;
		}
		
		if(tile != null) {
			//掃描全部slot內容物, 然後做成entity掉落出來
			for(int i = 0; i < tile.getSizeInventory(); i++) {
				ItemStack itemstack = tile.getStackInSlot(i);

				if(itemstack != null) {
					//設定要隨機噴出的range
					float f = world.rand.nextFloat() * 0.8F + 0.1F;
					float f1 = world.rand.nextFloat() * 0.8F + 0.1F;
					float f2 = world.rand.nextFloat() * 0.8F + 0.1F;

					while(itemstack.stackSize > 0) {
						int j = world.rand.nextInt(21) + 10;
						//如果物品超過一個隨機數量, 會分更多疊噴出
						if(j > itemstack.stackSize) {  
							j = itemstack.stackSize;
						}

						itemstack.stackSize -= j;
						//將item做成entity, 生成到世界上
						EntityItem item = new EntityItem(world, x + f, y + f1, z + f2, new ItemStack(itemstack.getItem(), j, itemstack.getItemDamage()));
						//如果有NBT tag, 也要複製到物品上
						if(itemstack.hasTagCompound()) {
							item.getEntityItem().setTagCompound((NBTTagCompound)itemstack.getTagCompound().copy());
						}
					world.spawnEntityInWorld(item);	//生成item entity
					}
				}
			}
			
			//掃描matBuild跟matStock是否有存值, 有的話轉存到block item上並生成到world中
			BasicEntityItem item = new BasicEntityItem(world, x, y+0.5D, z, new ItemStack(ModBlocks.BlockGrudgeHeavy, 1 ,0));
			NBTTagCompound nbt = new NBTTagCompound();
			
			int[] mats = new int[4];
			mats[0] = tile.getMatBuild(0) + tile.getMatStock(0);
			mats[1] = tile.getMatBuild(1) + tile.getMatStock(1);
			mats[2] = tile.getMatBuild(2) + tile.getMatStock(2);
			mats[3] = tile.getMatBuild(3) + tile.getMatStock(3);		
			
			//save nbt
			nbt.setIntArray("mats", mats);
			nbt.setInteger("fuel", tile.getPowerRemained());
			item.getEntityItem().setTagCompound(nbt);	//將nbt存到entity item中
			
			//spawn entity item
			world.spawnEntityInWorld(item);				//生成item entity
			world.func_147453_f(x, y, z, block);		//alert block changed
		}
		
		//呼叫原先的breakBlock, 會把tile entity移除掉
		super.breakBlock(world, x, y, z, block, meta);
	}
	
	//隨機發出傳送門音效
	@Override
	@SideOnly(Side.CLIENT)
    public void randomDisplayTick(World p_149734_1_, int p_149734_2_, int p_149734_3_, int p_149734_4_, Random p_149734_5_) {
		//play portal sound
		if (p_149734_5_.nextInt(50) == 0) {
            p_149734_1_.playSound(p_149734_2_ + 0.5D, p_149734_3_ + 0.5D, p_149734_4_ + 0.5D, "portal.portal", 0.5F, p_149734_5_.nextFloat() * 0.4F + 0.8F, false);
        }
    }


}
