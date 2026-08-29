package com.glowingfederal.combatives.entity.player;

import com.glowingfederal.combatives.entity.EntitySize;
import com.glowingfederal.combatives.entity.Pose;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public interface ICombativesPlayerPose {
    boolean canSwim();
    void updateSwimming();
    boolean getEyesInWaterPlayer();
    float getWaterVision();
    float getPoseWidth();
    float getPoseHeight();
    EffectivePlayerGeometry getEffectiveGeometry();
    EffectivePlayerGeometry getEffectiveGeometry(Pose pose);
    int getGeometryRevision();
    void acceptGeometryRevision(int revision);
    EntitySize getSize(Pose pose);
    void recalculateSize();
    void logGeometry(String heading, String reason);
    boolean isResizingAllowed();
    boolean isActuallySneaking();
    float getStandingEyeHeight(Pose pose, EntitySize size);
    void setPose(Pose pose);
    Pose getPose();
    boolean isPoseClear(Pose pose);
    boolean getShouldBeDead();
    boolean isSwimming();
    boolean isActuallySwimming();
    @SideOnly(Side.CLIENT)
    boolean isVisuallySwimming();
    void setSwimming(boolean swimming);
    float getSwimAnimation(float partialTicks);
    boolean canCrawl();
    boolean isCrawlKeyDown();
    void setCrawlKeyDown(boolean down);
}
