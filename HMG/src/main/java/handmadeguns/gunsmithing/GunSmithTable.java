package handmadeguns.gunsmithing;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import handmadeguns.HandmadeGunsCore;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

public class GunSmithTable extends BlockContainer implements ITileEntityProvider {


    public GunSmithTable() {
        super(Material.iron);
        this.setStepSound(Block.soundTypeMetal);
        this.setHardness(0.2F);

    }


    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) {
        if(!world.isRemote) {
            if(!player.isSneaking()) {
                System.out.println("test GunSmithTable OPEN GUI");
                player.openGui(HandmadeGunsCore.instance, 4, world, x, y, z);
            }
            //maybe I'll add something here idk
            //else {
            //
            //}
        }

        return true;
    }

    public TileEntity createNewTileEntity(World world, int a) {
        return new GunSmithTableTileEntity();
    }

    public TileEntity createNewTileEntity(World world) {
        return new GunSmithTableTileEntity();
    }

    public boolean shouldSideBeRendered(IBlockAccess p_149646_1_, int p_149646_2_, int p_149646_3_, int p_149646_4_, int p_149646_5_) {
        return true;
    }

    public boolean renderAsNormalBlock() {
        return false;
    }

    public boolean isOpaqueCube() {
        return false;
    }

    public boolean canHarvestBlock(EntityPlayer player, int meta) {
        return true;
    }

    public boolean canRenderInPass(int pass) {
        return false;
    }

    public int getMobilityFlag() {
        return 1;
    }


    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister par1IconRegister) {
        super.blockIcon = par1IconRegister.registerIcon("hmg:gun_table");
    }

    public void registerIcons(IIconRegister par1IconRegister) {
        super.blockIcon = par1IconRegister.registerIcon("hmg:gun_table");
    }

    public Item getItemDropped(int p_149650_1_, Random p_149650_2_, int p_149650_3_) {
        return Item.getItemFromBlock(HandmadeGunsCore.blockGunTable);
    }

    @SideOnly(Side.CLIENT)
    public Item getItem(World world, int p_149694_2_, int p_149694_3_, int p_149694_4_) {
        return Item.getItemFromBlock(HandmadeGunsCore.blockGunTable);
    }

    protected ItemStack createStackedBlock(int p_149644_1_) {
        return new ItemStack(HandmadeGunsCore.blockGunTable);
    }


}
