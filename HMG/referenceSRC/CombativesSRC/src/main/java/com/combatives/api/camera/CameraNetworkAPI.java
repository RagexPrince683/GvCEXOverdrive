package com.combatives.api.camera;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;

public final class CameraNetworkAPI {
    private CameraNetworkAPI() {}
    public static boolean sendEffectToPlayer(EntityPlayerMP player, CameraEffectType type, CameraEffectContext context, float strength) { return false; }
    public static boolean sendEffectAround(World world, double x, double y, double z, double radius, CameraEffectType type, CameraEffectContext context, float strength) { return false; }
}
