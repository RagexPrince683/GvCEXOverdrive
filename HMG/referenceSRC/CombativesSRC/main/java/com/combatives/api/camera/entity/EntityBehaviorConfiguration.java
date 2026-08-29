package com.combatives.api.camera.entity;

/** Read-only configuration view; unknown keys return the caller's fallback. */
public interface EntityBehaviorConfiguration {
    boolean getBoolean(String key, boolean fallback);
    int getInt(String key, int fallback);
    double getDouble(String key, double fallback);
    String getString(String key, String fallback);
}
