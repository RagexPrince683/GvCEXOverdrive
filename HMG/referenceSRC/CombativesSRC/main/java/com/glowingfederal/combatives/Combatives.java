package com.glowingfederal.combatives;

import com.glowingfederal.combatives.build.BuildInfo;
import com.glowingfederal.combatives.config.CombativesConfig;
import com.glowingfederal.combatives.movement.MovementDiagnostics;
import com.glowingfederal.combatives.proxy.CommonProxy;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(
    modid = Combatives.MOD_ID,
    name = Combatives.MOD_NAME,
    version = Combatives.VERSION,
    acceptedMinecraftVersions = "[1.7.10]"
)
public class Combatives {
    public static final String MOD_ID = "combatives";
    public static final String MOD_NAME = "Combatives";
    public static final String VERSION = "1.0.0";

    @Mod.Instance(MOD_ID)
    public static Combatives instance;

    @SidedProxy(
        clientSide = "com.glowingfederal.combatives.proxy.ClientProxy",
        serverSide = "com.glowingfederal.combatives.proxy.CommonProxy"
    )
    public static CommonProxy proxy;

    public static Logger logger;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        CombativesConfig.load(event.getSuggestedConfigurationFile());
        if (BuildInfo.FAIRPLAY_BUILD) {
            logger.info("Combatives Fairplay mode is active; gameplay and camera settings are locked to canonical defaults.");
        }
        CombativesConfig.logLoadedValues(logger);
        MovementDiagnostics.logFeatureState();
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }
}
