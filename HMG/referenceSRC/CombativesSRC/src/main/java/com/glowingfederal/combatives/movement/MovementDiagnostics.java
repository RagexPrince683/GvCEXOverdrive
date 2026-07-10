package com.glowingfederal.combatives.movement;

import com.glowingfederal.combatives.Combatives;
import com.glowingfederal.combatives.config.CombativesConfig;
import net.minecraft.entity.player.EntityPlayer;

public final class MovementDiagnostics {
    private MovementDiagnostics() {
    }

    public static void logFeatureState() {
        Combatives.logger.info("Combatives modern swimming behavior enabled");
        Combatives.logger.info("Combatives crawling behavior enabled");
    }

    public static void warn(String message) {
        if (Combatives.logger == null) {
            return;
        }
        Combatives.logger.warn("[movement] {}", message);
    }

    public static void warn(EntityPlayer player, String message) {
        if (Combatives.logger == null) {
            return;
        }
        Combatives.logger.warn("[movement] {} for {}", message, player.getCommandSenderName());
    }

    public static void debug(String message) {
        if (!isGeneralEnabled()) {
            return;
        }
        Combatives.logger.info("[movement] {}", message);
    }

    public static void debug(EntityPlayer player, String message) {
        if (!isGeneralEnabled()) {
            return;
        }
        Combatives.logger.info("[movement] {} for {}", message, player.getCommandSenderName());
    }

    public static void verbose(String message) {
        if (!isVerboseEnabled()) {
            return;
        }
        Combatives.logger.info("[movement:verbose] {}", message);
    }

    public static void verbose(EntityPlayer player, String message) {
        if (!isVerboseEnabled()) {
            return;
        }
        Combatives.logger.info("[movement:verbose] {} for {}", message, player.getCommandSenderName());
    }

    private static boolean isGeneralEnabled() {
        return Combatives.logger != null && (CombativesConfig.debugMovement || CombativesConfig.verboseMovementDebug);
    }

    private static boolean isVerboseEnabled() {
        return Combatives.logger != null && CombativesConfig.verboseMovementDebug;
    }
}
