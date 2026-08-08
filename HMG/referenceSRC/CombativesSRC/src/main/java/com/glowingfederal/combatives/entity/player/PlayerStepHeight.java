package com.glowingfederal.combatives.entity.player;

import com.glowingfederal.combatives.config.CombativesConfig;
import com.glowingfederal.combatives.entity.Pose;
import com.glowingfederal.combatives.movement.MovementDiagnostics;
import net.minecraft.entity.player.EntityPlayer;

public final class PlayerStepHeight {
    public static final float VANILLA_PLAYER_STEP_HEIGHT = 0.5F;
    private static final float EPSILON = 0.0001F;

    private PlayerStepHeight() {
    }

    public static void restoreVanillaStepHeight(EntityPlayer player, String source) {
        if (player == null) {
            return;
        }
        if (Math.abs(player.stepHeight - VANILLA_PLAYER_STEP_HEIGHT) > EPSILON) {
            float old = player.stepHeight;
            player.stepHeight = VANILLA_PLAYER_STEP_HEIGHT;
            warnIfDevelopment(player, source, old, true);
        }
    }

    public static void warnIfUnexpected(EntityPlayer player, String source) {
        if (player == null || Math.abs(player.stepHeight - VANILLA_PLAYER_STEP_HEIGHT) <= EPSILON || !CombativesConfig.debugMovement) {
            return;
        }
        warnIfDevelopment(player, source, player.stepHeight, false);
    }

    private static void warnIfDevelopment(EntityPlayer player, String source, float observed, boolean restored) {
        if (!CombativesConfig.debugMovement || isIntentionalSpecialState(player)) {
            return;
        }
        String side = player.worldObj == null ? "unknown" : (player.worldObj.isRemote ? "client" : "server");
        String pose = player instanceof ICombativesPlayerPose ? String.valueOf(((ICombativesPlayerPose) player).getPose()) : "unavailable";
        MovementDiagnostics.warn(player, "unexpected player stepHeight=" + observed
            + (restored ? " restoredTo=" + VANILLA_PLAYER_STEP_HEIGHT : " expected=" + VANILLA_PLAYER_STEP_HEIGHT)
            + " side=" + side
            + " class=" + player.getClass().getName()
            + " pose=" + pose
            + " source=" + source
            + " caller=" + likelyCaller());
    }

    private static boolean isIntentionalSpecialState(EntityPlayer player) {
        if (player == null || player.isPlayerSleeping() || player.isRiding() || player.isDead || player.deathTime > 0) {
            return true;
        }
        if (player instanceof ICombativesPlayerPose) {
            Pose pose = ((ICombativesPlayerPose) player).getPose();
            return pose == Pose.SLEEPING || pose == Pose.DYING;
        }
        return false;
    }

    private static String likelyCaller() {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        for (int i = 2; i < trace.length; i++) {
            String className = trace[i].getClassName();
            if (!className.equals(PlayerStepHeight.class.getName())) {
                return className + "#" + trace[i].getMethodName() + ":" + trace[i].getLineNumber();
            }
        }
        return "unknown";
    }
}
