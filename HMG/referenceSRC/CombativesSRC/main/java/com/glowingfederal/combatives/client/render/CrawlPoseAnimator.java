package com.glowingfederal.combatives.client.render;

import com.glowingfederal.combatives.util.math.MathHelperNew;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.util.MathHelper;

/** Applies a restrained, alternating prone crawl to the vanilla rigid-limb biped. */
public final class CrawlPoseAnimator {
    private static final float PI = (float) Math.PI;
    private static final float PRONE_HEAD_COUNTER_PITCH = -(PI * 0.40F);
    private static final float REST_ARM_SWEEP = 0.52F;
    private static final float ARM_PULL_SWEEP = 0.30F;
    private static final float ARM_REACH_PITCH = 0.20F;
    private static final float LEVEL_LEG_PITCH = 0.0F;
    private static final float LEG_DRIVE = 0.28F;
    private static final float LEG_SPREAD = 0.10F;

    private CrawlPoseAnimator() {
    }

    /**
     * Uses vanilla's render-interpolated limb phase and movement amplitude.
     * The rigid arms cannot show an elbow, so a short shoulder sweep suggests
     * a forearm-supported pull without the broad overhead swimming stroke.
     */
    public static void apply(ModelBiped model, float limbSwing, float limbSwingAmount, float poseBlend) {
        float movement = MathHelper.clamp_float(limbSwingAmount, 0.0F, 1.0F);
        float leftPull = MathHelper.sin(limbSwing * 0.6662F);
        float rightPull = -leftPull;

        /*
         * RenderPlayer pitches the complete model from upright to prone.  A
         * vanilla zero-pitch head therefore looks into the floor unless its
         * local pitch cancels that crawl-only body rotation.  Counter it with
         * the same blend so the remaining angle is the player's look pitch;
         * do not move the shared model origin or gameplay entity upward.
         */
        model.bipedHead.rotateAngleX += PRONE_HEAD_COUNTER_PITCH * poseBlend;
        model.bipedHeadwear.rotateAngleX = model.bipedHead.rotateAngleX;

        model.bipedLeftArm.rotateAngleX = blend(model.bipedLeftArm.rotateAngleX,
            -ARM_REACH_PITCH + ARM_REACH_PITCH * movement * leftPull, poseBlend);
        model.bipedRightArm.rotateAngleX = blend(model.bipedRightArm.rotateAngleX,
            -ARM_REACH_PITCH + ARM_REACH_PITCH * movement * rightPull, poseBlend);
        model.bipedLeftArm.rotateAngleY = blendAngle(model.bipedLeftArm.rotateAngleY, PI, poseBlend);
        model.bipedRightArm.rotateAngleY = blend(model.bipedRightArm.rotateAngleY, PI, poseBlend);
        model.bipedLeftArm.rotateAngleZ = blendAngle(model.bipedLeftArm.rotateAngleZ,
            PI + REST_ARM_SWEEP + ARM_PULL_SWEEP * movement * leftPull, poseBlend);
        model.bipedRightArm.rotateAngleZ = blend(model.bipedRightArm.rotateAngleZ,
            PI - REST_ARM_SWEEP + ARM_PULL_SWEEP * movement * rightPull, poseBlend);

        // Each arm pulls with the opposite leg: left arm/right leg, then vice versa.
        model.bipedLeftLeg.rotateAngleX = blend(model.bipedLeftLeg.rotateAngleX,
            LEVEL_LEG_PITCH + LEG_DRIVE * movement * rightPull, poseBlend);
        model.bipedRightLeg.rotateAngleX = blend(model.bipedRightLeg.rotateAngleX,
            LEVEL_LEG_PITCH + LEG_DRIVE * movement * leftPull, poseBlend);
        model.bipedLeftLeg.rotateAngleZ = blend(model.bipedLeftLeg.rotateAngleZ, LEG_SPREAD, poseBlend);
        model.bipedRightLeg.rotateAngleZ = blend(model.bipedRightLeg.rotateAngleZ, -LEG_SPREAD, poseBlend);
    }

    private static float blend(float current, float target, float amount) {
        return MathHelperNew.lerp(amount, current, target);
    }

    private static float blendAngle(float current, float target, float amount) {
        float delta = (target - current) % (PI * 2.0F);
        if (delta < -PI) delta += PI * 2.0F;
        if (delta >= PI) delta -= PI * 2.0F;
        return current + amount * delta;
    }
}
