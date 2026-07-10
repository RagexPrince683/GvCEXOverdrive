package com.glowingfederal.combatives.mixin;

import com.glowingfederal.combatives.client.model.ICombativesModelBipedSwimming;
import com.glowingfederal.combatives.entity.player.ICombativesPlayerPose;
import com.glowingfederal.combatives.movement.MovementDiagnostics;
import com.glowingfederal.combatives.util.math.MathHelperNew;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderPlayer.class)
public abstract class RenderPlayerMixin extends RendererLivingEntity {
    private static final float CRAWL_GROUNDING_Y = -0.10F;

    private boolean combatives$loggedLocalCrawlGrounding;
    private boolean combatives$loggedRemoteCrawlGrounding;

    public RenderPlayerMixin(ModelBase model, float shadowSize) {
        super(model, shadowSize);
    }

    @Inject(method = "renderFirstPersonArm", at = @At("HEAD"))
    private void combatives$resetFirstPersonSwimAnimation(EntityPlayer player, CallbackInfo ci) {
        ModelBiped modelPlayer = (ModelBiped) this.mainModel;
        ((ICombativesModelBipedSwimming) modelPlayer).setSwimAnimation(0.0F);
    }

    @Inject(method = "rotateCorpse(Lnet/minecraft/client/entity/AbstractClientPlayer;FFF)V", at = @At("TAIL"))
    private void combatives$applyCombativesSwimRotations(AbstractClientPlayer player, float p_77043_2_, float rotationYaw, float partialTicks, CallbackInfo ci) {
        if (player instanceof ICombativesPlayerPose) {
            ICombativesPlayerPose pose = (ICombativesPlayerPose) player;
            float animation = pose.getSwimAnimation(partialTicks);
            if (animation > 0.0F || pose.isActuallySwimming()) {
                MovementDiagnostics.verbose(player, "Combatives crawl/swim render hook fired: crawl=" + pose.isCrawlKeyDown() + " swim=" + pose.isSwimming() + " pose=" + pose.getPose() + " animation=" + animation);
            }
            float targetPitch = player.isInWater() ? -90.0F - player.rotationPitch : -90.0F;
            float rotation = MathHelperNew.lerp(animation, 0.0F, targetPitch);
            GL11.glRotatef(rotation, 1.0F, 0.0F, 0.0F);

            boolean landCrawl = pose.isCrawlKeyDown() && !player.isInWater();
            float translateY = pose.isActuallySwimming() ? -1.0F : 0.0F;
            float translateZ = pose.isActuallySwimming() ? 0.3F : 0.0F;
            if (landCrawl && pose.isActuallySwimming()) {
                translateY += CRAWL_GROUNDING_Y;
            }

            if (pose.isCrawlKeyDown()) {
                boolean localPlayer = Minecraft.getMinecraft().thePlayer == player;
                if ((localPlayer && !this.combatives$loggedLocalCrawlGrounding) || (!localPlayer && !this.combatives$loggedRemoteCrawlGrounding)) {
                    double interpolatedY = player.prevPosY + (player.posY - player.prevPosY) * partialTicks;
                    MovementDiagnostics.verbose(player, "crawl render grounding: isLocalPlayer=" + localPlayer
                        + " posY=" + player.posY + " prevPosY=" + player.prevPosY + " lastTickPosY=" + player.lastTickPosY
                        + " interpolatedRenderY=" + interpolatedY + " partialTicks=" + partialTicks
                        + " yOffset=" + player.yOffset + " ySize=" + player.ySize + " width=" + player.width + " height=" + player.height
                        + " pose=" + pose.getPose() + " crawl=" + pose.isCrawlKeyDown() + " swim=" + pose.isSwimming()
                        + " actuallySwimming=" + pose.isActuallySwimming() + " landCrawl=" + landCrawl
                        + " baseTranslateY=" + (pose.isActuallySwimming() ? -1.0F : 0.0F) + " groundingY=" + (landCrawl ? CRAWL_GROUNDING_Y : 0.0F)
                        + " finalTranslateY=" + translateY + " translateZ=" + translateZ);
                    if (localPlayer) {
                        this.combatives$loggedLocalCrawlGrounding = true;
                    } else {
                        this.combatives$loggedRemoteCrawlGrounding = true;
                    }
                }
            }

            if (pose.isActuallySwimming()) {
                GL11.glTranslatef(0.0F, translateY, translateZ);
            }
        }
    }

    @Inject(method = "func_96449_a(Lnet/minecraft/client/entity/AbstractClientPlayer;DDDLjava/lang/String;FD)V", at = @At("HEAD"), cancellable = true)
    private void combatives$hideCrawlPlayerLabel(AbstractClientPlayer player, double x, double y, double z, String name, float scale, double distance, CallbackInfo ci) {
        this.combatives$debugAndCancelCrawlPlayerNameplate("RenderPlayer#func_96449_a(AbstractClientPlayer)", player, distance, ci);
    }

    @Inject(method = "func_96449_a(Lnet/minecraft/entity/EntityLivingBase;DDDLjava/lang/String;FD)V", at = @At("HEAD"), cancellable = true)
    private void combatives$hideCrawlPlayerLabelBridge(EntityLivingBase entity, double x, double y, double z, String name, float scale, double distance, CallbackInfo ci) {
        if (entity instanceof EntityPlayer) {
            this.combatives$debugAndCancelCrawlPlayerNameplate("RenderPlayer#func_96449_a(EntityLivingBase)", (EntityPlayer) entity, distance, ci);
        }
    }

    private void combatives$debugAndCancelCrawlPlayerNameplate(String hook, EntityPlayer player, double distance, CallbackInfo ci) {
        boolean crawl = player instanceof ICombativesPlayerPose && ((ICombativesPlayerPose) player).isCrawlKeyDown();
        boolean swim = player instanceof ICombativesPlayerPose && ((ICombativesPlayerPose) player).isSwimming();
        String pose = player instanceof ICombativesPlayerPose ? String.valueOf(((ICombativesPlayerPose) player).getPose()) : "unknown";
        MovementDiagnostics.verbose(player, hook + " nameplate hook entity=" + player.getClass().getName() + " crawl=" + crawl + " swim=" + swim + " pose=" + pose + " distance=" + distance + " cancelAttempt=" + crawl);
        if (crawl) {
            ci.cancel();
        }
    }

}
