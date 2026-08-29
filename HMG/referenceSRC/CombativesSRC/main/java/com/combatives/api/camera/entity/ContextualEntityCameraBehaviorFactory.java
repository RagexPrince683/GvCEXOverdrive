package com.combatives.api.camera.entity;

/** Opt-in factory extension that receives shared resources and provider identity without global lookups. */
public interface ContextualEntityCameraBehaviorFactory extends EntityCameraBehaviorFactory {
    EntityCameraBehavior create(EntityBehaviorEnvironment environment, EntityBehaviorProviderInfo provider);
}
