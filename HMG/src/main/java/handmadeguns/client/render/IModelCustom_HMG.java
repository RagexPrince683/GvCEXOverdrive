package handmadeguns.client.render;

import cpw.mods.fml.relauncher.SideOnly;
import handmadeguns.obj_modelloaderMod.obj.HMGGroupObject;
import net.minecraftforge.client.model.IModelCustom;

import java.util.concurrent.ExecutorService;

import static cpw.mods.fml.relauncher.Side.CLIENT;

public interface IModelCustom_HMG extends IModelCustom {
	@SideOnly(CLIENT)
	HMGGroupObject renderPart_getInstance();
	@SideOnly(CLIENT)
	boolean isReady();
	@SideOnly(CLIENT)
	ExecutorService getLoadThread();
}
