package handmadeguns.gunsmithing;

import net.minecraft.tileentity.TileEntity;

public class GunSmithTableTileEntity extends TileEntity {

    public int getBlockMetadata() {
        if(super.blockMetadata == -1) {
            super.blockMetadata = super.worldObj.getBlockMetadata(super.xCoord, super.yCoord, super.zCoord);
            //MCH_Lib.DbgLog(super.worldObj, "MCH_DraftingTableTileEntity.getBlockMetadata : %d(0x%08X)", new Object[]{Integer.valueOf(super.blockMetadata), Integer.valueOf(super.blockMetadata)});
        }

        return super.blockMetadata;
    }


}
