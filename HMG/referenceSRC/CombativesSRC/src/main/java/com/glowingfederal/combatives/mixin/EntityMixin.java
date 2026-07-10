package com.glowingfederal.combatives.mixin;

import com.glowingfederal.combatives.entity.Pose;
import com.glowingfederal.combatives.entity.player.ICombativesPlayerPose;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Redirect(method = "moveEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isSneaking()Z"))
    private boolean combatives$useActualSneakForMovement(Entity entity) {
        if (entity instanceof ICombativesPlayerPose) {
            return ((ICombativesPlayerPose) entity).isActuallySneaking();
        }
        return entity.isSneaking();
    }

    @ModifyConstant(method = "handleWaterMovement", constant = @Constant(doubleValue = -0.4000000059604645D))
    private double combatives$adjustSwimmingWaterProbe(double original) {
        return this instanceof ICombativesPlayerPose && ((ICombativesPlayerPose) this).getPose() == Pose.SWIMMING
            ? -0.2500000059604645D
            : original;
    }
}
