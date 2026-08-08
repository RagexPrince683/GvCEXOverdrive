package com.combatives.api.camera.entity;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Immutable, diagnostic metadata supplied by the registering mod. */
public final class EntityBehaviorMetadata {
    public static final EntityBehaviorMetadata EMPTY = new EntityBehaviorMetadata("unknown", Collections.<String, String>emptyMap());
    private final String owningMod;
    private final Map<String, String> attributes;

    public EntityBehaviorMetadata(String owningMod, Map<String, String> attributes) {
        if (owningMod == null || owningMod.length() == 0) throw new IllegalArgumentException("owningMod");
        this.owningMod = owningMod;
        this.attributes = Collections.unmodifiableMap(new LinkedHashMap<String, String>(attributes == null ? Collections.<String, String>emptyMap() : attributes));
    }
    public String getOwningMod() { return owningMod; }
    public Map<String, String> getAttributes() { return attributes; }
    public String get(String key) { return attributes.get(key); }
}
