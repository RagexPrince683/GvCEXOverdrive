package com.glowingfederal.combatives.loading;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.gtnewhorizon.gtnhmixins.ILateMixinLoader;
import com.gtnewhorizon.gtnhmixins.LateMixin;
import cpw.mods.fml.relauncher.FMLLaunchHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Loads integrations which target optional mod classes after FML has discovered mods. */
@LateMixin
@SuppressWarnings("unused")
public final class CombativesLateMixins implements ILateMixinLoader {
    private static final String CONFIG = "mixins.combatives.mpm.late.json";
    private static final String MPM_MOD_ID = "moreplayermodels";
    private static final Logger LOGGER = LogManager.getLogger("Combatives");

    @Override
    public String getMixinConfig() {
        return CONFIG;
    }

    @Override
    public List<String> getMixins(Set<String> loadedMods) {
        if (!FMLLaunchHandler.side().isClient()) {
            LOGGER.debug("Skipping client-only MorePlayerModels+ compatibility on the dedicated server");
            return Collections.emptyList();
        }
        if (!loadedMods.contains(MPM_MOD_ID)) {
            LOGGER.debug("MorePlayerModels+ is not installed; its optional targeting hook is disabled");
            return Collections.emptyList();
        }

        LOGGER.info("MorePlayerModels+ detected; using the renderer-independent authoritative view ray");
        return Collections.emptyList();
    }
}
