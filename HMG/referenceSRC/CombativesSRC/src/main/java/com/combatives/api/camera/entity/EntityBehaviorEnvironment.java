package com.combatives.api.camera.entity;

import net.minecraft.world.World;
import org.apache.logging.log4j.Logger;

/** Immutable resources shared by all providers created for a mount lifecycle. */
public final class EntityBehaviorEnvironment {
    private final World world;
    private final EntityBehaviorConfiguration configuration;
    private final Logger logger;
    private final String apiVersion;
    private final EntityBehaviorHelpers helpers;
    private final EntityBehaviorRandomFactory randomFactory;

    public EntityBehaviorEnvironment(World world, EntityBehaviorConfiguration configuration, Logger logger, String apiVersion,
            EntityBehaviorHelpers helpers, EntityBehaviorRandomFactory randomFactory) {
        if (configuration == null || logger == null || apiVersion == null || helpers == null || randomFactory == null) throw new IllegalArgumentException("environment resource");
        this.world=world; this.configuration=configuration; this.logger=logger; this.apiVersion=apiVersion; this.helpers=helpers; this.randomFactory=randomFactory;
    }
    public World getWorld() { return world; }
    public EntityBehaviorConfiguration getConfiguration() { return configuration; }
    public Logger getLogger() { return logger; }
    public String getApiVersion() { return apiVersion; }
    public EntityBehaviorHelpers getHelpers() { return helpers; }
    public EntityBehaviorRandomFactory getRandomFactory() { return randomFactory; }
}
