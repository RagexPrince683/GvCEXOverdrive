package com.combatives.api.camera.entity;

public final class EntityBehaviorRegistration {
    private final String id;
    private final int priority;
    private final long sequence;
    private final EntityBehaviorMetadata metadata;
    private final EntityMatcher matcher;
    private final EntityCameraBehaviorFactory factory;

    /** Legacy direct construction; registry registration is preferred so sequence ordering is meaningful. */
    public EntityBehaviorRegistration(String id, EntityMatcher matcher, EntityCameraBehaviorFactory factory) {
        this(id, 0, Long.MAX_VALUE, EntityBehaviorMetadata.EMPTY, matcher, factory);
    }
    EntityBehaviorRegistration(String id, int priority, long sequence, EntityBehaviorMetadata metadata, EntityMatcher matcher, EntityCameraBehaviorFactory factory) {
        if (id == null || id.length() == 0) throw new IllegalArgumentException("id");
        if (matcher == null) throw new IllegalArgumentException("matcher");
        if (factory == null) throw new IllegalArgumentException("factory");
        this.id = id;
        this.priority = priority;
        this.sequence = sequence;
        this.metadata = metadata == null ? EntityBehaviorMetadata.EMPTY : metadata;
        this.matcher = matcher;
        this.factory = factory;
    }
    public String getId() { return id; }
    public int getPriority() { return priority; }
    public long getRegistrationSequence() { return sequence; }
    public EntityBehaviorMetadata getMetadata() { return metadata; }
    public EntityMatcher getMatcher() { return matcher; }
    public EntityCameraBehaviorFactory getFactory() { return factory; }
}
