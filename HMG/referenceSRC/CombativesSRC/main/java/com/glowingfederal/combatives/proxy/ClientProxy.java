package com.glowingfederal.combatives.proxy;

import com.glowingfederal.combatives.Combatives;
import com.glowingfederal.combatives.client.ClientMovementInputHandler;
import com.glowingfederal.combatives.client.CombativesKeyBindings;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        Combatives.logger.info("Combatives ClientProxy preInit executing");
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        CombativesKeyBindings.register();
        FMLCommonHandler.instance().bus().register(new ClientMovementInputHandler());
        Combatives.logger.info("Combatives ClientProxy initialized");
    }
}
