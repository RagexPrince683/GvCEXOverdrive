package handmadeguns.guide;

import cpw.mods.fml.common.Loader;
import handmadeguns.HandmadeGunsCore;

/** Entry point for optional Guide-API integration. Contains no Guide-API imports. */
public final class HMGGuideIntegration {
    private static boolean registered;
    private static boolean failed;

    private HMGGuideIntegration() {
    }

    public static void registerIfAvailable() {
        if (!HandmadeGunsCore.enableHMGGuideBook) {
            log("Guide-API manual registration disabled by config.");
            return;
        }
        if (!Loader.isModLoaded("guideapi")) {
            log("Guide-API not loaded; skipping HMG Field Manual registration. Install Guide-API for the full in-game manual, or use /hmgmanual for help.");
            return;
        }
        if (registered) {
            log("Guide-API manual already registered; skipping duplicate registration.");
            return;
        }
        try {
            int categories = HMGGuideReflectionRegistrar.register();
            registered = true;
            failed = false;
            log("Registered HMG Field Manual with Guide-API (categories=" + categories + ").");
        } catch (Throwable t) {
            failed = true;
            log("Failed to register HMG Field Manual with Guide-API: " + t.getClass().getName() + ": " + t.getMessage());
            t.printStackTrace();
        }
    }

    public static boolean isGuideApiLoaded() {
        return Loader.isModLoaded("guideapi");
    }

    public static boolean isRegistered() {
        return registered;
    }

    public static boolean isFailed() {
        return failed;
    }

    public static void log(String message) {
        System.out.println("[HMG][GuideAPI] " + message);
    }
}
