package handmadeguns.client.modelLoader.obj_modelloaderMod.obj;

import cpw.mods.fml.relauncher.Side;
import handmadeguns.HMGGunMaker;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;

/** Clears cached HMG OBJ models and their VBOs when resource packs reload. */
@SideOnly(Side.CLIENT)
public class HMGObjResourceReloadListener implements IResourceManagerReloadListener {
    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        HMGGunMaker.clearCachedModels();
        HMGObjModelLoader.clearModelCache();
    }
}
