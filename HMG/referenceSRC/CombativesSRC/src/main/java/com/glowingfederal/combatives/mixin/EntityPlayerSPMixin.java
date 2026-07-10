package com.glowingfederal.combatives.mixin;

import com.glowingfederal.combatives.client.ICombativesClientPlayerSwimming;
import com.glowingfederal.combatives.client.MovementInputStorage;
import com.glowingfederal.combatives.entity.Pose;
import com.glowingfederal.combatives.entity.player.ICombativesPlayerPose;
import com.glowingfederal.combatives.movement.MovementDiagnostics;
import com.glowingfederal.combatives.network.NetworkHandler;
import com.glowingfederal.combatives.network.message.PacketCrawlKeyState;
import com.glowingfederal.combatives.util.math.AxisAlignedBBSpliterator;
import java.util.stream.StreamSupport;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.potion.Potion;
import net.minecraft.util.MovementInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityPlayerSP.class)
public abstract class EntityPlayerSPMixin implements ICombativesClientPlayerSwimming {
    @Shadow(aliases = "field_71159_c") protected Minecraft mc;
    @Shadow public MovementInput movementInput;
    @Shadow protected int sprintToggleTimer;

    private final MovementInputStorage combatives$movementStorage = new MovementInputStorage();
    private boolean combatives$isCrouching;
    private boolean combatives$lastCrawlJumpExitDown;

    @Inject(method = "isSneaking", at = @At("HEAD"), cancellable = true)
    private void combatives$isSneaking(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(this.combatives$isCrouching);
    }

    @Override
    public boolean isActuallySneaking() {
        return this.movementInput != null && this.movementInput.sneak;
    }

    @Override
    public boolean isForcedDown() {
        EntityPlayerSP self = (EntityPlayerSP) (Object) this;
        ICombativesPlayerPose pose = this.combatives$getPoseState(self);
        if (pose == null) {
            return this.isActuallySneaking();
        }
        return pose.isResizingAllowed() && !self.capabilities.isFlying ? this.combatives$isCrouching || pose.isVisuallySwimming() : this.isActuallySneaking();
    }

    @Override
    public boolean isUsingSwimmingAnimation() {
        return this.movementInput != null && this.isUsingSwimmingAnimation(this.movementInput.moveForward, this.movementInput.moveStrafe);
    }

    @Override
    public boolean isUsingSwimmingAnimation(float moveForward, float moveStrafe) {
        if (this.canSwimClient()) {
            return this.isMovingForward(moveForward, moveStrafe);
        }
        return moveForward >= 0.8F;
    }

    @Override
    public boolean canSwimClient() {
        ICombativesPlayerPose pose = this.combatives$getPoseState((EntityPlayerSP) (Object) this);
        return pose != null && pose.getEyesInWaterPlayer();
    }

    @Override
    public boolean isMovingForward(float moveForward, float moveStrafe) {
        return moveForward > 1.0E-5F;
    }

    @Inject(method = "func_145771_j", at = @At("HEAD"), cancellable = true)
    private void combatives$pushOutOfBlocks(double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        EntityPlayerSP self = (EntityPlayerSP) (Object) this;
        if (!self.noClip) {
            this.combatives$setPlayerOffsetMotion(x, z);
        }
        cir.setReturnValue(false);
    }

    private void combatives$setPlayerOffsetMotion(double x, double z) {
        EntityPlayerSP self = (EntityPlayerSP) (Object) this;
        int blockX = (int) Math.floor(x);
        int blockZ = (int) Math.floor(z);
        if (this.combatives$shouldBlockPushPlayer(blockX, blockZ)) {
            double localX = x - blockX;
            double localZ = z - blockZ;
            double closest = Double.MAX_VALUE;
            int bestX = 0;
            int bestZ = 0;
            int[][] directions = new int[][] { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };
            for (int[] direction : directions) {
                boolean xAxis = direction[0] != 0;
                double distance = direction[0] + direction[1] > 0 ? 1.0D - (xAxis ? localX : localZ) : (xAxis ? localX : localZ);
                if (distance < closest && !this.combatives$shouldBlockPushPlayer(blockX + direction[0], blockZ + direction[1])) {
                    closest = distance;
                    bestX = direction[0];
                    bestZ = direction[1];
                }
            }
            if (bestX != 0 || bestZ != 0) {
                self.motionX = 0.1D * bestX;
                self.motionZ = 0.1D * bestZ;
                MovementDiagnostics.verbose(self, "client exact collision push-out applied for crawl/swim clearance");
            }
        }
    }

    private boolean combatives$shouldBlockPushPlayer(int x, int z) {
        EntityPlayerSP self = (EntityPlayerSP) (Object) this;
        AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(x, self.boundingBox.minY, z, x + 1.0D, self.boundingBox.maxY, z + 1.0D);
        return !this.combatives$isAxisAlignedBBNotClear(self.worldObj, self, aabb.expand(-1.0E-7D, -1.0E-7D, -1.0E-7D));
    }

    private boolean combatives$isAxisAlignedBBNotClear(net.minecraft.world.World world, Entity entity, AxisAlignedBB aabb) {
        return !StreamSupport.stream(new AxisAlignedBBSpliterator(world, entity, aabb), false).findAny().isPresent();
    }

    @Inject(method = "onLivingUpdate", at = @At("HEAD"))
    private void combatives$onLivingUpdateHead(CallbackInfo ci) {
        if (this.movementInput == null) {
            return;
        }
        this.combatives$updateSprintToggleTimer();
        this.combatives$movementStorage.copyFrom(this.movementInput);
        this.combatives$movementStorage.isSprinting = ((EntityPlayerSP) (Object) this).isSprinting();
        this.combatives$movementStorage.isFlying = ((EntityPlayerSP) (Object) this).capabilities.isFlying;
        this.combatives$handleCrawlJumpExit();
    }


    private void combatives$handleCrawlJumpExit() {
        EntityPlayerSP self = (EntityPlayerSP) (Object) this;
        ICombativesPlayerPose pose = this.combatives$getPoseState(self);
        if (pose == null || !pose.isCrawlKeyDown()) {
            this.combatives$lastCrawlJumpExitDown = false;
            return;
        }

        boolean jumpDown = this.movementInput.jump;
        this.movementInput.jump = false;
        if (!jumpDown) {
            this.combatives$lastCrawlJumpExitDown = false;
            return;
        }
        if (this.combatives$lastCrawlJumpExitDown) {
            return;
        }
        this.combatives$lastCrawlJumpExitDown = true;

        if (!pose.isPoseClear(Pose.STANDING)) {
            MovementDiagnostics.debug(self, "crawl jump exit blocked: standing clearance unavailable");
            return;
        }

        pose.setCrawlKeyDown(false);
        MovementDiagnostics.debug(self, "crawl jump requested crawl exit");
        if (NetworkHandler.channel == null) {
            MovementDiagnostics.warn(self, "crawl jump exit packet skipped because network channel is not initialized");
            return;
        }
        NetworkHandler.channel.sendToServer(new PacketCrawlKeyState());
    }

    private void combatives$updateSprintToggleTimer() {
        if (this.movementInput.sneak) {
            this.sprintToggleTimer = 0;
        }
        this.combatives$movementStorage.sprintToggleTimer = this.sprintToggleTimer;
        if (this.combatives$movementStorage.sprintToggleTimer > 0) {
            --this.combatives$movementStorage.sprintToggleTimer;
        }
        if (((EntityPlayerSP) (Object) this).isUsingItem() && !((EntityPlayerSP) (Object) this).isRiding()) {
            this.combatives$movementStorage.sprintToggleTimer = 0;
        }
    }

    @Inject(method = "onLivingUpdate", at = @At("TAIL"))
    private void combatives$onLivingUpdateTail(CallbackInfo ci) {
        EntityPlayerSP self = (EntityPlayerSP) (Object) this;
        ICombativesPlayerPose pose = this.combatives$getPoseState(self);
        if (pose == null || this.movementInput == null) {
            return;
        }

        this.combatives$updatePlayerMoveState();
        this.combatives$isCrouching = this.combatives$isCrouching(!pose.isPoseClear(Pose.STANDING));

        if (((EntityPlayerSP) (Object) this).isSprinting() != this.combatives$movementStorage.isSprinting && (((EntityPlayerSP) (Object) this).isInWater() || pose.isSwimming())) {
            MovementDiagnostics.debug(self, "client restored Combatives water sprint state");
            ((EntityPlayerSP) (Object) this).setSprinting(this.combatives$movementStorage.isSprinting);
        }

        boolean isSaturated = (float) self.getFoodStats().getFoodLevel() > 6.0F || self.capabilities.allowFlying;
        this.combatives$startSprinting(isSaturated);
        this.combatives$stopSprinting(isSaturated);
        this.combatives$handleWaterSneaking();
    }

    private ICombativesPlayerPose combatives$getPoseState(EntityPlayerSP self) {
        return self instanceof ICombativesPlayerPose ? (ICombativesPlayerPose) self : null;
    }

    private void combatives$updatePlayerMoveState() {
        if (!this.movementInput.sneak && this.isForcedDown()) {
            this.movementInput.moveStrafe *= 0.3F;
            this.movementInput.moveForward *= 0.3F;
            MovementDiagnostics.verbose((EntityPlayerSP) (Object) this, "client movement slowed for forced crawl/swim pose");
        }
        if (this.movementInput.sneak && !this.isForcedDown()) {
            this.movementInput.moveStrafe /= 0.3F;
            this.movementInput.moveForward /= 0.3F;
        }
    }

    private boolean combatives$isCrouching(boolean cantStand) {
        EntityPlayerSP self = (EntityPlayerSP) (Object) this;
        ICombativesPlayerPose pose = this.combatives$getPoseState(self);
        if (pose == null) {
            return false;
        }
        if ((!this.combatives$movementStorage.isFlying || !cantStand) && !pose.isSwimming() && (self.onGround || !((EntityPlayerSP) (Object) this).isInWater())) {
            if (!self.isOnLadder() && (pose.isPoseClear(Pose.CROUCHING) || self.noClip)) {
                return this.movementInput.sneak || pose.isResizingAllowed() && !self.isPlayerSleeping() && cantStand;
            }
        }
        return false;
    }

    private void combatives$startSprinting(boolean isSaturated) {
        boolean wasSneaking = this.combatives$movementStorage.sneak;
        boolean wasSwimmingMove = this.isUsingSwimmingAnimation(this.combatives$movementStorage.moveForward, this.combatives$movementStorage.moveStrafe);
        boolean sprintEnvironment = ((EntityPlayerSP) (Object) this).onGround || this.canSwimClient() || this.combatives$movementStorage.isFlying;
        boolean sprintKeyDown = this.mc != null && this.mc.gameSettings.keyBindSprint.getIsKeyPressed();
        if (sprintEnvironment && !wasSneaking && !wasSwimmingMove && this.isUsingSwimmingAnimation() && !((EntityPlayerSP) (Object) this).isSprinting()
            && isSaturated && !((EntityPlayerSP) (Object) this).isPotionActive(Potion.blindness)) {
            if (this.combatives$movementStorage.sprintToggleTimer <= 0 && !sprintKeyDown) {
                this.sprintToggleTimer = 7;
            } else {
                MovementDiagnostics.debug((EntityPlayerSP) (Object) this, "client started Combatives swim sprint");
                ((EntityPlayerSP) (Object) this).setSprinting(true);
            }
        }
        if (!((EntityPlayerSP) (Object) this).isSprinting() && (!((EntityPlayerSP) (Object) this).isInWater() || this.canSwimClient()) && this.isUsingSwimmingAnimation()
            && isSaturated && !((EntityPlayerSP) (Object) this).isPotionActive(Potion.blindness) && sprintKeyDown) {
            MovementDiagnostics.debug((EntityPlayerSP) (Object) this, "client started Combatives swim sprint from sprint key");
            ((EntityPlayerSP) (Object) this).setSprinting(true);
        }
    }

    private void combatives$stopSprinting(boolean isSaturated) {
        if (!((EntityPlayerSP) (Object) this).isSprinting()) {
            return;
        }
        EntityPlayerSP self = (EntityPlayerSP) (Object) this;
        ICombativesPlayerPose pose = this.combatives$getPoseState(self);
        if (pose == null) {
            return;
        }
        boolean notMoving = !this.isMovingForward(this.movementInput.moveForward, this.movementInput.moveStrafe) || !isSaturated;
        boolean collided = notMoving || ((EntityPlayerSP) (Object) this).isInWater() && !this.canSwimClient() && !this.combatives$movementStorage.isFlying;
        if (pose.isSwimming()) {
            if (!this.movementInput.sneak && notMoving || !((EntityPlayerSP) (Object) this).isInWater()) {
                MovementDiagnostics.debug(self, !((EntityPlayerSP) (Object) this).isInWater() ? "swimming cancelled: left water" : "swimming cancelled: movement/saturation stopped");
                ((EntityPlayerSP) (Object) this).setSprinting(false);
            }
        } else if (collided) {
            ((EntityPlayerSP) (Object) this).setSprinting(false);
        }
    }

    private void combatives$handleWaterSneaking() {
        EntityPlayerSP self = (EntityPlayerSP) (Object) this;
        if (((EntityPlayerSP) (Object) this).isInWater() && this.movementInput.sneak && !self.capabilities.isFlying) {
            self.motionY -= 0.03999999910593033D * self.getEntityAttribute(net.minecraft.entity.SharedMonsterAttributes.movementSpeed).getAttributeValue();
        }
    }
}
