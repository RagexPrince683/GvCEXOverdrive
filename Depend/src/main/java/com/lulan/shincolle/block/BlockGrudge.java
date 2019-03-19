package com.lulan.shincolle.block;

import net.minecraft.block.material.Material;

public class BlockGrudge extends BasicBlock {
	public BlockGrudge() {
		super(Material.sand);
		this.setBlockName("BlockGrudge");
		this.setHarvestLevel("shovel", 0);
	    this.setHardness(1F);
	    this.setLightLevel(1F);
	    this.setStepSound(soundTypeSand);
	    this.setResistance(200F);
	}
	
	
}
