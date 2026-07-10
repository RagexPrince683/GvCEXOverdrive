package com.glowingfederal.combatives.client.render;

import com.glowingfederal.combatives.entity.Pose;
import com.glowingfederal.combatives.entity.player.ICombativesPlayerPose;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

public final class CombativesVisualPoseHelper {
    private CombativesVisualPoseHelper() {
    }

    public static boolean isVisuallySwimmingOrCrawling(Entity entity) {
        return entity instanceof EntityPlayer && isVisuallySwimmingOrCrawling((EntityPlayer) entity);
    }

    public static boolean isVisuallySwimmingOrCrawling(EntityPlayer player) {
        if (!(player instanceof ICombativesPlayerPose)) {
            return false;
        }
        ICombativesPlayerPose pose = (ICombativesPlayerPose) player;
        return pose.isSwimming() || pose.isCrawlKeyDown() || pose.getPose() == Pose.SWIMMING;
    }

    public static float getVisualSwimAnimation(EntityPlayer player, float partialTicks) {
        if (!(player instanceof ICombativesPlayerPose)) {
            return 0.0F;
        }
        ICombativesPlayerPose pose = (ICombativesPlayerPose) player;
        float animation = pose.getSwimAnimation(partialTicks);
        return isVisuallySwimmingOrCrawling(player) && animation <= 0.0F ? 1.0F : animation;
    }

    public static String describe(EntityPlayer player) {
        if (!(player instanceof ICombativesPlayerPose)) {
            return "poseState=unavailable";
        }
        ICombativesPlayerPose pose = (ICombativesPlayerPose) player;
        return "crawl=" + pose.isCrawlKeyDown() + " swim=" + pose.isSwimming() + " pose=" + pose.getPose();
    }
}
