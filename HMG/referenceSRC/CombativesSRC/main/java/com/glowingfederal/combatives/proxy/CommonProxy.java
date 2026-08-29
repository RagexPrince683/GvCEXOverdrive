package com.glowingfederal.combatives.proxy;

import com.glowingfederal.combatives.loading.CombativesCorePlugin;
import com.glowingfederal.combatives.network.NetworkHandler;
import com.glowingfederal.combatives.network.PoseSyncEvents;
import com.glowingfederal.combatives.Combatives;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.common.MinecraftForge;

public class CommonProxy {
    public void preInit(FMLPreInitializationEvent event) {
        Combatives.logger.info("Combatives common pose mixin config expected from early loader: {}", CombativesCorePlugin.COMMON_MIXIN_CONFIG);
        NetworkHandler.register();
    }

    public void init(FMLInitializationEvent event) {
        PoseSyncEvents poseSyncEvents = new PoseSyncEvents();
        FMLCommonHandler.instance().bus().register(poseSyncEvents);
        MinecraftForge.EVENT_BUS.register(poseSyncEvents);
    }
}
