package com.lulan.shincolle.tileentity;

import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;

/** light source block
 *  
 *  type: 0:air, 1:water
 *
 */
public class TileEntityLightBlock extends BasicTileEntity {

	private int type;
	public int life;
	public int tick;
	
	
	public TileEntityLightBlock() {
		super();
		this.type = 0;
		this.life = 0;
		this.tick = 0;
	}
	
	public TileEntityLightBlock(int type, int life) {
		super();
		this.type = type;
		this.life = life;
		this.tick = 0;
	}
	
	//讀取nbt資料
	@Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);	//從nbt讀取方塊的xyz座標
        
        this.type = compound.getInteger("type");
        this.life = compound.getInteger("life");
    }
	
	//將資料寫進nbt
	@Override
	public void writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		
		compound.setInteger("type", this.type);
		compound.setInteger("life", this.life);
	}
	
	@Override
	public void updateEntity() {
		this.tick++;
		
		if(!this.worldObj.isRemote) {
			if(this.tick > this.life) {
				this.worldObj.setBlock(this.xCoord, this.yCoord, this.zCoord, Blocks.air);
				
//				//replace block
//				switch(type) {
//				case 1:   //water
//					this.worldObj.setBlock(this.xCoord, this.yCoord, this.zCoord, Blocks.water);
//					break;
//				default:  //default air
//					this.worldObj.setBlock(this.xCoord, this.yCoord, this.zCoord, Blocks.air);
//					break;
//				}
			}
		}
	}
	
}
