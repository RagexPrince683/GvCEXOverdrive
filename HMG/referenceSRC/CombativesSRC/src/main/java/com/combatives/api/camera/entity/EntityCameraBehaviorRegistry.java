package com.combatives.api.camera.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Comparator;
import net.minecraft.entity.Entity;

/** Runtime multi-provider registry with deterministic priority/ID/sequence ordering. */
public final class EntityCameraBehaviorRegistry {
    private static final List<EntityBehaviorRegistration> REGISTRATIONS = new ArrayList<EntityBehaviorRegistration>();
    private static long nextSequence;
    private EntityCameraBehaviorRegistry() {}

    public static synchronized EntityBehaviorRegistration register(String id, EntityMatcher matcher, EntityCameraBehaviorFactory factory) {
        return register(id, 0, EntityBehaviorMetadata.EMPTY, matcher, factory);
    }
    public static synchronized EntityBehaviorRegistration register(String id, int priority, EntityBehaviorMetadata metadata, EntityMatcher matcher, EntityCameraBehaviorFactory factory) {
        EntityBehaviorRegistration registration = new EntityBehaviorRegistration(id, priority, nextSequence++, metadata, matcher, factory);
        REGISTRATIONS.add(registration);
        return registration;
    }
    public static synchronized boolean unregister(EntityBehaviorRegistration registration) { return REGISTRATIONS.remove(registration); }
    public static synchronized List<EntityBehaviorRegistration> registrations() {
        return ordered(REGISTRATIONS);
    }
    public static synchronized List<EntityBehaviorRegistration> matching(Entity entity) {
        List<EntityBehaviorRegistration> result = new ArrayList<EntityBehaviorRegistration>();
        for (EntityBehaviorRegistration registration : REGISTRATIONS) {
            if (registration.getMatcher().matches(entity)) result.add(registration);
        }
        return ordered(result);
    }
    private static List<EntityBehaviorRegistration> ordered(List<EntityBehaviorRegistration> source) {
        List<EntityBehaviorRegistration> result = new ArrayList<EntityBehaviorRegistration>(source);
        Collections.sort(result, new Comparator<EntityBehaviorRegistration>() {
            public int compare(EntityBehaviorRegistration a, EntityBehaviorRegistration b) {
                if (a.getPriority() != b.getPriority()) return a.getPriority() < b.getPriority() ? 1 : -1;
                int id = a.getId().compareTo(b.getId());
                if (id != 0) return id;
                return a.getRegistrationSequence() < b.getRegistrationSequence() ? -1 : (a.getRegistrationSequence() == b.getRegistrationSequence() ? 0 : 1);
            }
        });
        return Collections.unmodifiableList(result);
    }
}
