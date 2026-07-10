package com.glowingfederal.combatives.build;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class BuildInfo {
    private static final String RESOURCE = "/combatives-build.properties";

    public static final boolean FAIRPLAY_BUILD = loadFairplayFlag();
    public static final String DISPLAY_NAME = FAIRPLAY_BUILD ? "Combatives Fairplay" : "Combatives";

    private BuildInfo() {
    }

    private static boolean loadFairplayFlag() {
        InputStream stream = BuildInfo.class.getResourceAsStream(RESOURCE);
        if (stream == null) {
            return false;
        }

        try {
            Properties properties = new Properties();
            properties.load(stream);
            return Boolean.parseBoolean(properties.getProperty("fairplayBuild", "false"));
        } catch (IOException ignored) {
            return false;
        } finally {
            try {
                stream.close();
            } catch (IOException ignored) {
            }
        }
    }
}
