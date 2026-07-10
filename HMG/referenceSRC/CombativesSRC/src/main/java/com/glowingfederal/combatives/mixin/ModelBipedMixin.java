package com.glowingfederal.combatives.mixin;

import com.glowingfederal.combatives.client.model.ICombativesModelBipedSwimming;
import com.glowingfederal.combatives.client.render.CombativesVisualPoseHelper;
import com.glowingfederal.combatives.entity.player.ICombativesPlayerPose;
import com.glowingfederal.combatives.movement.MovementDiagnostics;
import com.glowingfederal.combatives.util.math.MathHelperNew;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelBiped.class)
public abstract class ModelBipedMixin extends ModelBase implements ICombativesModelBipedSwimming {
    @Shadow public ModelRenderer bipedHead;
    @Shadow public ModelRenderer bipedRightArm;
    @Shadow public ModelRenderer bipedLeftArm;
    @Shadow public ModelRenderer bipedRightLeg;
    @Shadow public ModelRenderer bipedLeftLeg;

    @Unique private float combatives$swimAnimation;

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelBiped;setRotationAngles(FFFFFFLnet/minecraft/entity/Entity;)V"))
    private void combatives$setRotationAngles(ModelBiped model, float limbSwing, float limbSwingAmount, float ageInTicks,
        float netHeadYaw, float headPitch, float scaleFactor, Entity entity) {
        if (entity instanceof ICombativesPlayerPose && this.combatives$getSwimAnimationFor(entity) > 0.0F) {
            ICombativesPlayerPose pose = (ICombativesPlayerPose) entity;
            float swimAnimation = this.combatives$getSwimAnimationFor(entity);
            if (pose.isActuallySwimming()) {
                headPitch = this.combatives$rotLerpRad(swimAnimation, this.bipedHead.rotateAngleX, -((float) Math.PI / 4.0F)) / 0.017453292F;
            } else {
                headPitch = this.combatives$rotLerpRad(swimAnimation, this.bipedHead.rotateAngleX, headPitch * ((float) Math.PI / 180.0F)) / 0.017453292F;
            }
        }
        model.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entity);
    }

    @Inject(method = "setRotationAngles", at = @At("TAIL"), cancellable = true)
    private void combatives$setRotationAnglesPost(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
        float headPitch, float scaleFactor, Entity entity, CallbackInfo ci) {
        float swimAnimation = this.combatives$getSwimAnimationFor(entity);
        if (swimAnimation <= 0.0F) {
            return;
        }
        if (entity instanceof EntityPlayer && entity instanceof ICombativesPlayerPose) {
            ICombativesPlayerPose pose = (ICombativesPlayerPose) entity;
            MovementDiagnostics.verbose((EntityPlayer) entity, "Combatives crawl/swim model hook fired: crawl=" + pose.isCrawlKeyDown() + " swim=" + pose.isSwimming() + " pose=" + pose.getPose() + " animation=" + this.combatives$swimAnimation);
        }
        float cycle = limbSwing % 26.0F;
        float armBlend = this.onGround > 0.0F ? 0.0F : swimAnimation;
        if (cycle < 14.0F) {
            this.bipedLeftArm.rotateAngleX = this.combatives$rotLerpRad(armBlend, this.bipedLeftArm.rotateAngleX, 0.0F);
            this.bipedRightArm.rotateAngleX = MathHelperNew.lerp(armBlend, this.bipedRightArm.rotateAngleX, 0.0F);
            this.bipedLeftArm.rotateAngleY = this.combatives$rotLerpRad(armBlend, this.bipedLeftArm.rotateAngleY, (float) Math.PI);
            this.bipedRightArm.rotateAngleY = MathHelperNew.lerp(armBlend, this.bipedRightArm.rotateAngleY, (float) Math.PI);
            this.bipedLeftArm.rotateAngleZ = this.combatives$rotLerpRad(armBlend, this.bipedLeftArm.rotateAngleZ, (float) Math.PI + 1.8707964F * this.combatives$getArmAngleSq(cycle) / this.combatives$getArmAngleSq(14.0F));
            this.bipedRightArm.rotateAngleZ = MathHelperNew.lerp(armBlend, this.bipedRightArm.rotateAngleZ, (float) Math.PI - 1.8707964F * this.combatives$getArmAngleSq(cycle) / this.combatives$getArmAngleSq(14.0F));
        } else if (cycle < 22.0F) {
            float progress = (cycle - 14.0F) / 8.0F;
            this.bipedLeftArm.rotateAngleX = this.combatives$rotLerpRad(armBlend, this.bipedLeftArm.rotateAngleX, ((float) Math.PI / 2.0F) * progress);
            this.bipedRightArm.rotateAngleX = MathHelperNew.lerp(armBlend, this.bipedRightArm.rotateAngleX, ((float) Math.PI / 2.0F) * progress);
            this.bipedLeftArm.rotateAngleY = this.combatives$rotLerpRad(armBlend, this.bipedLeftArm.rotateAngleY, (float) Math.PI);
            this.bipedRightArm.rotateAngleY = MathHelperNew.lerp(armBlend, this.bipedRightArm.rotateAngleY, (float) Math.PI);
            this.bipedLeftArm.rotateAngleZ = this.combatives$rotLerpRad(armBlend, this.bipedLeftArm.rotateAngleZ, 5.012389F - 1.8707964F * progress);
            this.bipedRightArm.rotateAngleZ = MathHelperNew.lerp(armBlend, this.bipedRightArm.rotateAngleZ, 1.2707963F + 1.8707964F * progress);
        } else {
            float progress = (cycle - 22.0F) / 4.0F;
            this.bipedLeftArm.rotateAngleX = this.combatives$rotLerpRad(armBlend, this.bipedLeftArm.rotateAngleX, ((float) Math.PI / 2.0F) - ((float) Math.PI / 2.0F) * progress);
            this.bipedRightArm.rotateAngleX = MathHelperNew.lerp(armBlend, this.bipedRightArm.rotateAngleX, ((float) Math.PI / 2.0F) - ((float) Math.PI / 2.0F) * progress);
            this.bipedLeftArm.rotateAngleY = this.combatives$rotLerpRad(armBlend, this.bipedLeftArm.rotateAngleY, (float) Math.PI);
            this.bipedRightArm.rotateAngleY = MathHelperNew.lerp(armBlend, this.bipedRightArm.rotateAngleY, (float) Math.PI);
            this.bipedLeftArm.rotateAngleZ = this.combatives$rotLerpRad(armBlend, this.bipedLeftArm.rotateAngleZ, (float) Math.PI);
            this.bipedRightArm.rotateAngleZ = MathHelperNew.lerp(armBlend, this.bipedRightArm.rotateAngleZ, (float) Math.PI);
        }
        this.bipedLeftLeg.rotateAngleX = MathHelperNew.lerp(swimAnimation, this.bipedLeftLeg.rotateAngleX, 0.3F * MathHelper.cos(limbSwing * 0.33333334F + (float) Math.PI));
        this.bipedRightLeg.rotateAngleX = MathHelperNew.lerp(swimAnimation, this.bipedRightLeg.rotateAngleX, 0.3F * MathHelper.cos(limbSwing * 0.33333334F));
        ci.cancel();
    }

    @Override
    public void setLivingAnimations(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float partialTicks) {
        if (entity instanceof ICombativesPlayerPose) {
            this.combatives$swimAnimation = entity instanceof EntityPlayer ? CombativesVisualPoseHelper.getVisualSwimAnimation((EntityPlayer) entity, partialTicks) : ((ICombativesPlayerPose) entity).getSwimAnimation(partialTicks);
        }
        super.setLivingAnimations(entity, limbSwing, limbSwingAmount, partialTicks);
    }

    @Unique private float combatives$getSwimAnimationFor(Entity entity) {
        if (entity instanceof EntityPlayer) {
            return CombativesVisualPoseHelper.getVisualSwimAnimation((EntityPlayer) entity, 1.0F);
        }
        return this.combatives$swimAnimation;
    }

    @Unique private float combatives$getArmAngleSq(float limbSwing) {
        return -65.0F * limbSwing + limbSwing * limbSwing;
    }

    @Unique private float combatives$rotLerpRad(float angle, float maxAngle, float target) {
        float f = (target - maxAngle) % ((float) Math.PI * 2.0F);
        if (f < -(float) Math.PI) f += ((float) Math.PI * 2.0F);
        if (f >= (float) Math.PI) f -= ((float) Math.PI * 2.0F);
        return maxAngle + angle * f;
    }

    @Override
    public void setSwimAnimation(float swimAnimation) {
        this.combatives$swimAnimation = swimAnimation;
    }
}
