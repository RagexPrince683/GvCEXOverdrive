package com.combatives.api.camera.entity;

/** Stable identity handed directly to a contextual provider factory. */
public final class EntityBehaviorProviderInfo {
    private final String registrationId;
    private final int priority;
    private final long registrationSequence;
    private final EntityBehaviorMetadata metadata;

    public EntityBehaviorProviderInfo(EntityBehaviorRegistration registration) {
        registrationId = registration.getId(); priority = registration.getPriority();
        registrationSequence = registration.getRegistrationSequence(); metadata = registration.getMetadata();
    }
    public String getRegistrationId() { return registrationId; }
    public int getPriority() { return priority; }
    public long getRegistrationSequence() { return registrationSequence; }
    public EntityBehaviorMetadata getMetadata() { return metadata; }
}
