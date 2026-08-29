package com.glowingfederal.combatives.network;

import java.util.Map;
import java.util.WeakHashMap;
import com.glowingfederal.combatives.compat.mpm.MpmCompatibility;
import com.glowingfederal.combatives.entity.player.ICombativesPlayerPose;
import com.glowingfederal.combatives.entity.player.EffectivePlayerGeometry;
import com.glowingfederal.combatives.entity.player.PlayerGeometryResolver;
import com.glowingfederal.combatives.network.message.PacketPlayerGeometryS2C;
import cpw.mods.fml.common.network.NetworkRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;

/** Server ownership and distribution of gameplay-relevant MPM player geometry. */
public final class PlayerGeometrySync {
    private static final Map<EntityPlayerMP, MpmCompatibility.Geometry> lastSent =
            new WeakHashMap<EntityPlayerMP, MpmCompatibility.Geometry>();
    private PlayerGeometrySync() { }

    public static void sampleAndBroadcast(EntityPlayerMP player) {
        MpmCompatibility.Geometry current = acceptedGeometry(player);
        MpmCompatibility.Geometry previous = lastSent.get(player);
        if (same(previous, current)) {
            if (player.ticksExisted % 100 == 0 && player instanceof ICombativesPlayerPose) {
                ((ICombativesPlayerPose) player).logGeometry("SERVER PLAYER GEOMETRY", "periodic sample");
            }
            return;
        }
        lastSent.put(player, current);
        broadcast(player, current, true, previous == null ? "login/entity construction" : "MPM model change");
    }

    public static void sendTo(EntityPlayerMP observer, Entity source) {
        if (!(source instanceof EntityPlayerMP) || NetworkHandler.channel == null) return;
        EntityPlayerMP player = (EntityPlayerMP) source;
        ICombativesPlayerPose state = (ICombativesPlayerPose) player;
        NetworkHandler.channel.sendTo(new PacketPlayerGeometryS2C(player.getEntityId(),
                state.getGeometryRevision(), acceptedGeometry(player)), observer);
    }

    private static void broadcast(EntityPlayerMP player, MpmCompatibility.Geometry geometry,
            boolean includeSelf, String reason) {
        if (NetworkHandler.channel == null) return;
        int revision = ((ICombativesPlayerPose) player).getGeometryRevision();
        PacketPlayerGeometryS2C packet = new PacketPlayerGeometryS2C(player.getEntityId(), revision, geometry);
        NetworkHandler.channel.sendToAllAround(packet, new NetworkRegistry.TargetPoint(
                player.dimension, player.posX, player.posY, player.posZ, 512.0D));
        if (includeSelf) NetworkHandler.channel.sendTo(packet, player);
        if (player instanceof ICombativesPlayerPose) {
            ((ICombativesPlayerPose) player).logGeometry("SERVER PLAYER GEOMETRY", reason);
        }
    }

    private static boolean same(MpmCompatibility.Geometry a, MpmCompatibility.Geometry b) {
        return a != null && b != null && a.rawSize == b.rawSize
                && Float.compare(a.widthScale, b.widthScale) == 0
                && Float.compare(a.heightScale, b.heightScale) == 0
                && Float.compare(a.eyeScale, b.eyeScale) == 0 && a.fromMpm == b.fromMpm
                && (a.disguiseClass == null ? b.disguiseClass == null : a.disguiseClass.equals(b.disguiseClass));
    }

    private static MpmCompatibility.Geometry acceptedGeometry(EntityPlayerMP player) {
        MpmCompatibility.Geometry source = MpmCompatibility.resolveLocal(player);
        if (!(player instanceof ICombativesPlayerPose)) return source;
        EffectivePlayerGeometry applied = ((ICombativesPlayerPose) player).getEffectiveGeometry();
        EffectivePlayerGeometry base = PlayerGeometryResolver.resolve(applied.pose);
        return new MpmCompatibility.Geometry(source.rawSize,
                applied.width / base.width, applied.height / base.height,
                applied.eyeAboveMinY / base.eyeAboveMinY, source.fromMpm, source.disguiseClass);
    }
}
