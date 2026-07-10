package com.glowingfederal.combatives.mixin;

import java.util.EnumMap;
import java.util.Map;

import com.glowingfederal.combatives.entity.EntitySize;
import com.glowingfederal.combatives.entity.Pose;
import com.glowingfederal.combatives.entity.player.ICombativesPlayerPose;
import com.glowingfederal.combatives.movement.ICombativesMovementState;
import com.glowingfederal.combatives.movement.MovementController;
import com.glowingfederal.combatives.movement.MovementDiagnostics;
import com.glowingfederal.combatives.movement.MovementSnapshot;
import com.glowingfederal.combatives.network.PoseSync;
import net.minecraft.entity.player.EntityPlayerMP;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fluids.IFluidBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityPlayer.class)
public abstract class EntityPlayerMixin extends EntityLivingBase implements ICombativesPlayerPose, ICombativesMovementState {
    private static final int POSE_WATCHER_ID = 28;
    private static final EntitySize STANDING_SIZE = EntitySize.flexible(0.6F, 1.8F);
    private static final Map<Pose, EntitySize> SIZE_BY_POSE = new EnumMap<Pose, EntitySize>(Pose.class);

    static {
        SIZE_BY_POSE.put(Pose.STANDING, STANDING_SIZE);
        SIZE_BY_POSE.put(Pose.SLEEPING, EntitySize.fixed(0.2F, 0.2F));
        SIZE_BY_POSE.put(Pose.FALL_FLYING, EntitySize.flexible(0.6F, 0.6F));
        SIZE_BY_POSE.put(Pose.SWIMMING, EntitySize.flexible(0.6F, 0.6F));
        SIZE_BY_POSE.put(Pose.SPIN_ATTACK, EntitySize.flexible(0.6F, 0.6F));
        SIZE_BY_POSE.put(Pose.CROUCHING, EntitySize.flexible(0.6F, 1.5F));
        SIZE_BY_POSE.put(Pose.DYING, EntitySize.fixed(0.2F, 0.2F));
    }

    @Shadow public PlayerCapabilities capabilities;
    @Shadow(remap = false) public float eyeHeight;
    @Shadow(aliases = "func_71000_j") public abstract void addMovementStat(double x, double y, double z);

    private boolean eyesInWater;
    private boolean eyesInWaterPlayer;
    private EntitySize combativesSize;
    private float combativesEyeHeight;
    private float previousEyeHeight;
    private float swimAnimation;
    private float lastSwimAnimation;
    private float timeUnderwater;
    private Pose lastLoggedPose = Pose.STANDING;
    private boolean lastLoggedSwimming;
    private boolean crawlKeyDown;
    private Pose combativesPose = Pose.STANDING;
    private boolean combativesPoseWatcherReady;
    private MovementSnapshot combativesMovementSnapshot = MovementSnapshot.EMPTY;

    public EntityPlayerMixin(World world) {
        super(world);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void combatives$constructed(CallbackInfo ci) {
        this.combativesSize = STANDING_SIZE;
        this.combativesEyeHeight = this.getEyeHeight(Pose.STANDING, this.combativesSize);
        this.combativesPose = Pose.STANDING;
        this.getDataWatcher().addObject(POSE_WATCHER_ID, Pose.STANDING.ordinal());
        this.combativesPoseWatcherReady = true;
    }


    @Override
    public MovementSnapshot getCombativesMovementSnapshot() {
        return this.combativesMovementSnapshot == null ? MovementSnapshot.EMPTY : this.combativesMovementSnapshot;
    }

    @Override
    public void setCombativesMovementSnapshot(MovementSnapshot snapshot) {
        this.combativesMovementSnapshot = snapshot == null ? MovementSnapshot.EMPTY : snapshot;
    }

    @Override
    public void func_145781_i(int key) {
        if (key == POSE_WATCHER_ID && this.worldObj.isRemote && !this.isRiding()) {
            MovementDiagnostics.verbose(this.getPlayer(), "DataWatcher pose changed on client: " + this.getPose());
            this.recalculateEyeHeight();
            this.recalculateSize();
        }
        super.func_145781_i(key);
    }

    @Override
    public void onEntityUpdate() {
        super.onEntityUpdate();
        if (this.isInWater()) {
            this.timeUnderwater = MathHelper.clamp_float(this.timeUnderwater + 1, 0, 600);
        } else if (this.timeUnderwater > 0) {
            this.timeUnderwater = MathHelper.clamp_float(this.timeUnderwater - 10, 0, 600);
        }
        this.eyesInWater = this.isInsideOfMaterial(Material.water);
        this.updateSwimming();
    }

    @Override
    public boolean canSwim() { return this.eyesInWater && this.isInWater(); }

    @Override
    public void updateSwimming() {
        boolean next = !this.capabilities.isFlying && this.isSprinting() && this.isInWater() && !this.isRiding()
            && (this.isSwimming() || this.canSwim());
        if (this.isSwimming() && !next) {
            MovementDiagnostics.debug(this.getPlayer(), this.combatives$getSwimCancelReason());
        }
        if (next != this.isSwimming()) {
            MovementDiagnostics.debug(this.getPlayer(), next ? "entering swim" : "leaving swim");
        }
        this.combatives$setSwimming(next, "updateSwimming");
    }

    @Override
    public boolean getEyesInWaterPlayer() { return this.eyesInWaterPlayer; }

    @Override
    public float getWaterVision() {
        if (!this.isInWater()) return 0.0F;
        if (this.timeUnderwater >= 600.0F) return 1.0F;
        float fadeIn = MathHelper.clamp_float(this.timeUnderwater / 100.0F, 0.0F, 1.0F);
        float longFade = this.timeUnderwater < 100.0F ? 0.0F : MathHelper.clamp_float((this.timeUnderwater - 100.0F) / 500.0F, 0.0F, 1.0F);
        return fadeIn * 0.6F + longFade * 0.4F;
    }

    @Override public float getPoseWidth() { return this.combativesSize.width; }
    @Override public float getPoseHeight() { return this.combativesSize.height; }
    @Override public EntitySize getSize(Pose pose) { return SIZE_BY_POSE.containsKey(pose) ? SIZE_BY_POSE.get(pose) : STANDING_SIZE; }

    @Override
    public void recalculateSize() {
        EntitySize oldSize = this.combativesSize == null ? STANDING_SIZE : this.combativesSize;
        EntitySize newSize = this.getSize(this.getPose());
        if (this.isResizingAllowed()) {
            boolean changed = oldSize.width != newSize.width || oldSize.height != newSize.height || this.width != newSize.width || this.height != newSize.height;
            this.recalculateSize(oldSize, newSize);
            this.width = newSize.width;
            this.height = newSize.height;
            if (changed) {
                MovementDiagnostics.verbose(this.getPlayer(), "bounding box recalculated for " + this.getPose() + " size=" + newSize.width + "x" + newSize.height);
            }
        }
        this.combativesSize = newSize;
    }

    private void recalculateSize(EntitySize oldSize, EntitySize newSize) {
        if (newSize.width < oldSize.width) {
            double half = newSize.width / 2.0D;
            this.boundingBox.setBB(AxisAlignedBB.getBoundingBox(this.posX - half, this.posY, this.posZ - half, this.posX + half, this.posY + newSize.height, this.posZ + half));
        } else {
            AxisAlignedBB box = this.boundingBox;
            this.boundingBox.setBB(AxisAlignedBB.getBoundingBox(box.minX, box.minY, box.minZ, box.minX + newSize.width, box.minY + newSize.height, box.minZ + newSize.width));
            if (newSize.width > oldSize.width && !this.worldObj.isRemote && this.ticksExisted > 0) {
                float distance = oldSize.width - newSize.width;
                this.moveEntity(distance, 0.0D, distance);
            }
        }
    }

    private void recalculateEyeHeight() {
        Pose pose = this.getPose();
        this.combativesEyeHeight = this.getEyeHeight(pose, this.getSize(pose));
        this.previousEyeHeight = this.eyeHeight;
        MovementDiagnostics.verbose(this.getPlayer(), "eye height recalculated for " + pose + ": " + this.combativesEyeHeight);
    }

    @Override
    public boolean isResizingAllowed() {
        float delta = 0.025F;
        AxisAlignedBB box = this.boundingBox;
        if (this.width < delta || this.height < delta || box.maxX - box.minX < delta || box.maxY - box.minY < delta) return true;
        return Math.abs(this.width / this.getPoseWidth() - 1.0F) < delta && Math.abs(this.height / this.getPoseHeight() - 1.0F) < delta
            && Math.abs((box.maxX - box.minX) / this.getPoseWidth() - 1.0F) < delta && Math.abs((box.maxY - box.minY) / this.getPoseHeight() - 1.0F) < delta;
    }

    private float getEyeHeight(Pose pose, EntitySize size) { return pose == Pose.SLEEPING || pose == Pose.DYING ? 0.2F : this.getStandingEyeHeight(pose, size); }
    @Override public boolean isActuallySneaking() { return this.isSneaking(); }
    @Override public float getStandingEyeHeight(Pose pose, EntitySize size) {
        if (pose == Pose.SWIMMING || pose == Pose.FALL_FLYING || pose == Pose.SPIN_ATTACK) return this.eyeHeight;
        if (pose == Pose.CROUCHING) return 0.35F;
        return this.eyeHeight;
    }

    @Override public void setPose(Pose pose) {
        Pose old = this.getPose();
        if (old != pose) {
            MovementDiagnostics.verbose(this.getPlayer(), "setPose " + old + " -> " + pose + " via " + this.combatives$getPoseCaller());
        }
        this.combativesPose = pose;
        if (this.combativesPoseWatcherReady) {
            this.getDataWatcher().updateObject(POSE_WATCHER_ID, pose.ordinal());
        }
    }

    private String combatives$getPoseCaller() {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        for (int i = 2; i < trace.length; i++) {
            String method = trace[i].getMethodName();
            if (!method.equals("setPose") && !method.equals("combatives$getPoseCaller")) {
                return trace[i].getClassName() + "#" + method + ":" + trace[i].getLineNumber();
            }
        }
        return "unknown";
    }
    @Override public Pose getPose() {
        if (!this.combativesPoseWatcherReady) {
            return this.combativesPose == null ? Pose.STANDING : this.combativesPose;
        }
        try {
            int id = this.getDataWatcher().getWatchableObjectInt(POSE_WATCHER_ID);
            this.combativesPose = id >= 0 && id < Pose.values().length ? Pose.values()[id] : Pose.STANDING;
            return this.combativesPose;
        } catch (RuntimeException e) {
            return this.combativesPose == null ? Pose.STANDING : this.combativesPose;
        }
    }
    @Override public boolean isPoseClear(Pose pose) { return this.worldObj.getCollidingBoundingBoxes(this, this.getBoundingBox(pose)).isEmpty(); }
    @Override public boolean getShouldBeDead() { return this.deathTime > 0; }
    @Override public boolean isSwimming() { return !this.capabilities.isFlying && this.getFlag(6); }
    @Override public boolean isActuallySwimming() { return this.getPose() == Pose.SWIMMING || this.getPose() == Pose.FALL_FLYING; }
    @SideOnly(Side.CLIENT) @Override public boolean isVisuallySwimming() { return this.isActuallySwimming() && !this.isInWater(); }
    @Override public void setSwimming(boolean swimming) {
        this.combatives$setSwimming(swimming, this.combatives$getPoseCaller());
    }

    private void combatives$setSwimming(boolean swimming, String reason) {
        boolean old = this.getFlag(6);
        if (old != swimming) {
            MovementDiagnostics.debug(this.getPlayer(), "setSwimming " + old + " -> " + swimming + " via " + reason + ": " + (swimming ? "swim flag changed: entered" : combatives$getSwimCancelReason()));
        }
        this.setFlag(6, swimming);
    }

    private String combatives$getSwimCancelReason() {
        if (this.capabilities.isFlying) return "swimming cancelled: player is flying";
        if (!this.isSprinting()) return "swimming cancelled: player is not sprinting";
        if (!this.isInWater()) return "swimming cancelled: player is not in water";
        if (this.isRiding()) return "swimming cancelled: player is riding";
        if (!this.canSwim()) return "swimming cancelled: eyes are not in water";
        return "swim state exited";
    }
    @Override public float getSwimAnimation(float partialTicks) { return this.lastSwimAnimation + partialTicks * (this.swimAnimation - this.lastSwimAnimation); }
    @Override public boolean canCrawl() { return !this.isRiding() && !this.capabilities.isFlying && !this.isOnLadder() && !this.getShouldBeDead() && !this.isPlayerSleeping(); }
    @Override public boolean isCrawlKeyDown() { return this.canCrawl() && this.crawlKeyDown; }
    @Override public void setCrawlKeyDown(boolean down) {
        if (down && !this.canCrawl()) {
            MovementDiagnostics.warn(this.getPlayer(), "crawl rejected: player state disallows crawling");
            this.crawlKeyDown = false;
            return;
        }
        if (this.crawlKeyDown != down) {
            MovementDiagnostics.debug(this.getPlayer(), "crawl request " + (down ? "accepted" : "released"));
        }
        this.crawlKeyDown = down;
    }

    @Inject(method = "getEyeHeight", at = @At("HEAD"), cancellable = true)
    private void combatives$getEyeHeight(CallbackInfoReturnable<Float> cir) {
        if (this.combativesEyeHeight > 0.0F) {
            cir.setReturnValue(this.combativesEyeHeight);
        }
    }

    @Inject(method = "onUpdate", at = @At(value = "INVOKE", target = "cpw/mods/fml/common/FMLCommonHandler.onPlayerPostTick(Lnet/minecraft/entity/player/EntityPlayer;)V", shift = At.Shift.BEFORE, remap = false))
    private void combatives$prePostTick(CallbackInfo ci) {
        this.lastSwimAnimation = this.swimAnimation;
        this.swimAnimation = this.isActuallySwimming() ? Math.min(1.0F, this.swimAnimation + 0.09F) : Math.max(0.0F, this.swimAnimation - 0.09F);
        this.eyesInWaterPlayer = this.isInsideOfMaterial(Material.water);
    }

    @Inject(method = "onUpdate", at = @At(value = "INVOKE", target = "cpw/mods/fml/common/FMLCommonHandler.onPlayerPostTick(Lnet/minecraft/entity/player/EntityPlayer;)V", shift = At.Shift.AFTER, remap = false))
    private void combatives$postPostTick(CallbackInfo ci) {
        this.updatePose();
        if (this.eyeHeight != this.previousEyeHeight) this.recalculateEyeHeight();
    }

    private void updatePose() {
        if (this.getShouldBeDead()) {
            this.combatives$selectPose(Pose.DYING);
            return;
        }

        if (this.isPlayerSleeping()) {
            this.combatives$selectPose(Pose.SLEEPING);
            return;
        }

        if (this.isRiding() || this.capabilities.isFlying || this.isOnLadder()) {
            if (this.isCrawlKeyDown()) {
                this.setCrawlKeyDown(false);
            }
            this.combatives$selectPose(this.isPoseClear(Pose.STANDING) ? Pose.STANDING : this.getPose());
            return;
        }

        if (!this.isPoseClear(Pose.SWIMMING)) {
            return;
        }

        Pose pose = this.getPose();
        boolean swimActive = this.isSwimming();
        boolean crawlActive = this.isCrawlKeyDown();

        if (swimActive || crawlActive) {
            pose = Pose.SWIMMING;
            if (this.worldObj.isRemote) {
                this.yOffset = 0.28F;
            }
        } else if (this.isActuallySneaking() && !this.capabilities.isFlying && (this.onGround || !this.isInWater()) && !this.isOnLadder()) {
            pose = Pose.CROUCHING;
            if (this.worldObj.isRemote) {
                this.yOffset = 1.62F;
            }
        } else if (this.isPoseClear(Pose.STANDING)) {
            if (!this.worldObj.isRemote) {
                this.removePotionEffect(Potion.moveSlowdown.id);
                this.removePotionEffect(Potion.digSlowdown.id);
            }
            pose = Pose.STANDING;
            if (this.worldObj.isRemote) {
                this.yOffset = 1.62F;
            }
        }

        Pose finalPose;
        if (!this.noClip && !this.isRiding() && this.isResizingAllowed() && !this.isPoseClear(pose)) {
            if (this.isPoseClear(Pose.CROUCHING)) {
                finalPose = Pose.CROUCHING;
            } else {
                finalPose = Pose.SWIMMING;
            }
        } else {
            finalPose = pose;
        }

        if ((swimActive || crawlActive) && finalPose != Pose.SWIMMING && this.isPoseClear(Pose.SWIMMING)) {
            finalPose = Pose.SWIMMING;
        }

        this.combatives$selectPose(finalPose);
    }

    private void combatives$selectPose(Pose pose) {
        Pose current = this.getPose();
        boolean poseChanged = pose != current;
        boolean swimActive = this.isSwimming();
        if (current == Pose.SWIMMING && pose != Pose.SWIMMING && (swimActive || this.isCrawlKeyDown())) {
            MovementDiagnostics.debug(this.getPlayer(), "blocked active low-pose downgrade to " + pose + "; crawl=" + this.isCrawlKeyDown() + " swimming=" + swimActive);
            pose = Pose.SWIMMING;
            poseChanged = pose != current;
        }
        this.lastLoggedSwimming = swimActive;
        this.setPose(pose);
        if (this.worldObj.isRemote) {
            this.yOffset = pose == Pose.SWIMMING ? 0.28F : 1.62F;
        }
        if (poseChanged) {
            MovementDiagnostics.verbose(this.getPlayer(), "pose selected " + (this.worldObj.isRemote ? "client" : "server") + ": " + pose);
            if (!this.worldObj.isRemote && this.getPlayer() instanceof EntityPlayerMP) {
                PoseSync.broadcastAuthoritativePose((EntityPlayerMP) this.getPlayer(), true);
            }
        }
        this.lastLoggedPose = pose;
        this.recalculateSize();
    }

    private AxisAlignedBB getBoundingBox(Pose pose) {
        EntitySize size = this.getSize(pose);
        float half = size.width / 2.0F;
        return AxisAlignedBB.getBoundingBox(this.posX - half, this.posY - this.yOffset + this.ySize, this.posZ - half, this.posX + half, this.posY - this.yOffset + this.ySize + size.height, this.posZ + half);
    }

    @Inject(method = "moveEntityWithHeading", at = @At("HEAD"), cancellable = true)
    private void combatives$moveEntityWithHeading(float strafe, float forward, CallbackInfo ci) {
        double startX = this.posX, startY = this.posY, startZ = this.posZ;
        boolean customSwimming = this.isSwimming() && !this.isRiding();
        boolean customCrawling = this.getPose() == Pose.SWIMMING && !this.isSwimming() && !this.isInWater();
        if (customSwimming) {
            double lookY = this.getLookVec().yCoord;
            double factor = lookY < -0.2D ? 0.085D : 0.06D;
            Block block = this.worldObj.getBlock((int)this.posX, (int)(this.posY + 0.9D), (int)this.posZ);
            if (lookY <= 0.0D || this.isJumping || block instanceof BlockLiquid || block instanceof IFluidBlock) this.motionY += (lookY - this.motionY) * factor;
        }
        double savedMotionY = this.motionY;
        float savedJumpMovement = this.jumpMovementFactor;
        if (this.capabilities.isFlying && !this.isRiding()) this.jumpMovementFactor = this.capabilities.getFlySpeed() * (this.isSprinting() ? 2.0F : 1.0F);
        if (customCrawling && !MovementController.shouldBypassUnsafe(this.getPlayer())) {
            this.combatives$moveCrawlingWithHeading(strafe, forward);
        } else if (!this.capabilities.isFlying && this.isInWater()) {
            float drag = this.isSprinting() ? 0.9F : 0.8F;
            double currentX = this.motionX;
            double currentZ = this.motionZ;
            this.moveFlying(strafe, forward, 0.02F);
            if (!MovementController.shouldBypassUnsafe(this.getPlayer())) {
                MovementController.MovementResult result = MovementController.shape(this.getPlayer(), strafe, forward, this.rotationYaw, currentX, currentZ, this.motionX, this.motionZ);
                this.motionX = result.motionX;
                this.motionZ = result.motionZ;
                this.setCombativesMovementSnapshot(result.snapshot);
            }
            this.moveEntity(this.motionX, this.motionY, this.motionZ);
            if (this.isCollidedHorizontally && this.isOnLadder()) this.motionY = 0.2D;
            this.motionX *= drag;
            this.motionY *= 0.8D;
            this.motionZ *= drag;
            if (!this.isSprinting()) this.motionY -= 0.005D;
            this.updateCombativesLimbSwing();
        } else {
            super.moveEntityWithHeading(strafe, forward);
        }
        if (this.capabilities.isFlying && !this.isRiding()) {
            this.motionY = savedMotionY * 0.6D;
            this.jumpMovementFactor = savedJumpMovement;
            this.fallDistance = 0.0F;
        }
        this.addMovementStat(this.posX - startX, this.posY - startY, this.posZ - startZ);
        ci.cancel();
    }


    private void combatives$moveCrawlingWithHeading(float strafe, float forward) {
        float friction = 0.91F;
        if (this.onGround) {
            friction = this.worldObj.getBlock(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.boundingBox.minY) - 1, MathHelper.floor_double(this.posZ)).slipperiness * 0.91F;
        }
        float groundAcceleration = 0.16277136F / (friction * friction * friction);
        float moveFactor = this.onGround ? this.getAIMoveSpeed() * groundAcceleration : this.jumpMovementFactor;
        double currentX = this.motionX;
        double currentZ = this.motionZ;
        this.moveFlying(strafe, forward, moveFactor);
        MovementController.MovementResult result = MovementController.shape(this.getPlayer(), strafe, forward, this.rotationYaw, currentX, currentZ, this.motionX, this.motionZ);
        this.motionX = result.motionX;
        this.motionZ = result.motionZ;
        this.setCombativesMovementSnapshot(result.snapshot);

        friction = 0.91F;
        if (this.onGround) {
            friction = this.worldObj.getBlock(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.boundingBox.minY) - 1, MathHelper.floor_double(this.posZ)).slipperiness * 0.91F;
        }
        this.moveEntity(this.motionX, this.motionY, this.motionZ);
        this.motionY -= 0.08D;
        this.motionY *= 0.9800000190734863D;
        this.motionX *= friction;
        this.motionZ *= friction;
        this.updateCombativesLimbSwing();
    }

    private void updateCombativesLimbSwing() {
        this.prevLimbSwingAmount = this.limbSwingAmount;
        double dx = this.posX - this.prevPosX;
        double dz = this.posZ - this.prevPosZ;
        float amount = MathHelper.sqrt_double(dx * dx + dz * dz) * 4.0F;
        if (amount > 1.0F) amount = 1.0F;
        this.limbSwingAmount += (amount - this.limbSwingAmount) * 0.4F;
        this.limbSwing += this.limbSwingAmount;
    }

    @Redirect(method = "sleepInBedAt", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;setSize(FF)V"))
    private void combatives$sleepSize(EntityPlayer player, float width, float height) {
        this.setPose(Pose.SLEEPING);
    }

    private EntityPlayer getPlayer() { return (EntityPlayer)(Object)this; }
}
