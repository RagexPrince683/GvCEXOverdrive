package handmadeguns.gunsmithing;

import net.minecraft.tileentity.TileEntity;

public class GunSmithTableTileEntity extends TileEntity {

    public int getBlockMetadata() {
        //if(super.blockMetadata == -1) {
        //    super.blockMetadata = super.worldObj.getBlockMetadata(super.xCoord, super.yCoord, super.zCoord);
        //}

        return super.blockMetadata;
    }


}
