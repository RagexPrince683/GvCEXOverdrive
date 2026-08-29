package com.glowingfederal.combatives.mixin;

import com.glowingfederal.combatives.entity.EntitySize;
import com.glowingfederal.combatives.entity.Pose;
import com.glowingfederal.combatives.entity.player.ICombativesPlayerPose;
import com.glowingfederal.combatives.entity.player.EffectivePlayerGeometry;
import com.glowingfederal.combatives.entity.player.PlayerGeometryResolver;
import com.glowingfederal.combatives.entity.player.PlayerStepHeight;
import com.glowingfederal.combatives.compat.mpm.MpmCompatibility;
import com.glowingfederal.combatives.movement.ICombativesMovementState;
import com.glowingfederal.combatives.movement.MovementController;
import com.glowingfederal.combatives.movement.MovementDiagnostics;
import com.glowingfederal.combatives.movement.MovementSnapshot;
import com.glowingfederal.combatives.network.PoseSync;
import com.glowingfederal.combatives.network.PlayerGeometrySync;
import net.minecraft.entity.player.EntityPlayerMP;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
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
    private static final EntitySize STANDING_SIZE = EntitySize.flexible(0.6F, 1.8F);

    @Shadow public PlayerCapabilities capabilities;
    @Shadow(remap = false) public float eyeHeight;
    @Shadow(aliases = "func_71000_j") public abstract void addMovementStat(double x, double y, double z);

    private boolean eyesInWater;
    private boolean eyesInWaterPlayer;
    private EntitySize combativesSize;
    private float combativesEyeHeight;
    private float combativesLegacyEyeHeight;
    private float previousEyeHeight;
    private float swimAnimation;
    private float lastSwimAnimation;
    private float timeUnderwater;
    private Pose lastLoggedPose = Pose.STANDING;
    private boolean lastLoggedSwimming;
    private boolean crawlKeyDown;
    private Pose combativesPose = Pose.STANDING;
    private Pose combativesAppliedPose = Pose.STANDING;
    private EffectivePlayerGeometry combativesAppliedGeometry;
    private int combativesGeometryRevision;
    private int combativesLastStepHeightWarningTick = -200;
    private MovementSnapshot combativesMovementSnapshot = MovementSnapshot.EMPTY;
    private Entity combativesLastRidingEntity;
    private Entity combativesDismountedEntity;
    private boolean combativesDismountHandoff;
    private int combativesLastMountWaitLogTick = -20;
    private int combativesLastMpmRawSize = MpmCompatibility.DEFAULT_RAW_SIZE;
    private float combativesLastMpmScale = 1.0F;
    private String combativesLastMpmDisguise;

    public EntityPlayerMixin(World world) {
        super(world);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void combatives$constructed(CallbackInfo ci) {
        this.combativesSize = STANDING_SIZE;
        this.combativesEyeHeight = this.getEyeHeight(Pose.STANDING, this.combativesSize);
        this.combativesLegacyEyeHeight = (float) (this.boundingBox.minY
                + this.combativesEyeHeight - this.posY);
        this.combativesPose = Pose.STANDING;
        this.combativesAppliedGeometry = PlayerGeometryResolver.resolve(Pose.STANDING);
        PlayerStepHeight.restoreVanillaStepHeight(this.getPlayer(), "EntityPlayer<init>");
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
    public void onEntityUpdate() {
        super.onEntityUpdate();
        if (this.isInWater()) {
            this.timeUnderwater = MathHelper.clamp_float(this.timeUnderwater + 1, 0, 600);
        } else if (this.timeUnderwater > 0) {
            this.timeUnderwater = MathHelper.clamp_float(this.timeUnderwater - 10, 0, 600);
        }
        this.eyesInWater = this.isInsideOfMaterial(Material.water);
        this.updateSwimming();
        this.recalculateSize();
        if (!this.worldObj.isRemote && this.getPlayer() instanceof EntityPlayerMP) {
            PlayerGeometrySync.sampleAndBroadcast((EntityPlayerMP) this.getPlayer());
        }
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
    @Override public EffectivePlayerGeometry getEffectiveGeometry() {
        return this.combativesAppliedGeometry == null
                ? PlayerGeometryResolver.resolve(this.combativesAppliedPose)
                : this.combativesAppliedGeometry;
    }
    @Override public EffectivePlayerGeometry getEffectiveGeometry(Pose pose) { return this.combatives$resolveGeometry(pose); }
    @Override public int getGeometryRevision() { return this.combativesGeometryRevision; }
    @Override public void acceptGeometryRevision(int revision) {
        if (revision > this.combativesGeometryRevision) this.combativesGeometryRevision = revision;
    }
    private EffectivePlayerGeometry combatives$resolveGeometry(Pose pose) {
        MpmCompatibility.Geometry geometry = MpmCompatibility.resolve(this.getPlayer());
        this.combativesLastMpmRawSize = geometry.rawSize;
        this.combativesLastMpmScale = geometry.heightScale;
        this.combativesLastMpmDisguise = geometry.disguiseClass;
        EffectivePlayerGeometry base = PlayerGeometryResolver.resolve(pose);
        return new EffectivePlayerGeometry(pose, base.width * geometry.widthScale,
                base.height * geometry.heightScale, base.eyeAboveMinY * geometry.eyeScale);
    }
    @Override public EntitySize getSize(Pose pose) {
        EffectivePlayerGeometry geometry = this.getEffectiveGeometry(pose);
        return new EntitySize(geometry.width, geometry.height, pose == Pose.SLEEPING || pose == Pose.DYING);
    }

    @Override
    public void recalculateSize() {
        EntitySize oldSize = this.combativesSize == null ? STANDING_SIZE : this.combativesSize;
        EffectivePlayerGeometry requestedGeometry = this.getEffectiveGeometry(this.getPose());
        EntitySize newSize = new EntitySize(requestedGeometry.width, requestedGeometry.height,
                this.getPose() == Pose.SLEEPING || this.getPose() == Pose.DYING);
        boolean accepted = this.isResizingAllowed();
        if (accepted && (newSize.width > oldSize.width || newSize.height > oldSize.height)) {
            EffectivePlayerGeometry requested = new EffectivePlayerGeometry(this.getPose(), newSize.width,
                    newSize.height, 0.0F);
            accepted = this.worldObj.getCollidingBoundingBoxes(this,
                    requested.clearanceBox(this.posX, this.boundingBox.minY, this.posZ)).isEmpty();
        }
        if (accepted) {
            boolean changed = oldSize.width != newSize.width || oldSize.height != newSize.height || this.width != newSize.width || this.height != newSize.height;
            this.recalculateSize(oldSize, newSize);
            this.width = newSize.width;
            this.height = newSize.height;
            if (changed) {
                MovementDiagnostics.verbose(this.getPlayer(), "bounding box recalculated for " + this.getPose() + " size=" + newSize.width + "x" + newSize.height);
                EffectivePlayerGeometry posture = PlayerGeometryResolver.resolve(this.getPose());
                MovementDiagnostics.verbose(this.getPlayer(), "MPM hitbox player=" + this.getCommandSenderName()
                        + " mpmRawSize=" + this.combativesLastMpmRawSize + " mpmResolvedScale=" + this.combativesLastMpmScale
                        + " mpmDisguise=" + this.combativesLastMpmDisguise
                        + " baseWidth=0.6 baseHeight=1.8 posture=" + this.getPose()
                        + " postureBaseWidth=" + posture.width + " postureBaseHeight=" + posture.height
                        + " finalWidth=" + newSize.width + " finalHeight=" + newSize.height
                        + " boundingBox.minY=" + this.boundingBox.minY + " boundingBox.maxY=" + this.boundingBox.maxY);
            }
            this.combativesSize = newSize;
            boolean geometryChanged = this.combativesAppliedGeometry == null
                    || this.combativesAppliedGeometry.pose != requestedGeometry.pose
                    || Float.compare(this.combativesAppliedGeometry.width, requestedGeometry.width) != 0
                    || Float.compare(this.combativesAppliedGeometry.height, requestedGeometry.height) != 0
                    || Float.compare(this.combativesAppliedGeometry.eyeAboveMinY, requestedGeometry.eyeAboveMinY) != 0;
            this.combativesAppliedPose = this.getPose();
            this.combativesAppliedGeometry = requestedGeometry;
            if (geometryChanged && !this.worldObj.isRemote) this.combativesGeometryRevision++;
            this.recalculateEyeHeight();
        } else {
            MovementDiagnostics.verbose(this.getPlayer(), "resize rejected for " + this.getPose()
                    + " requested=" + newSize.width + "x" + newSize.height
                    + " actual=" + this.width + "x" + this.height + " box=" + this.boundingBox
                    + "; retaining applied geometry=" + oldSize.width + "x" + oldSize.height);
        }
        this.combatives$warnUnexpectedStepHeight("recalculateSize");
    }

    private void recalculateSize(EntitySize oldSize, EntitySize newSize) {
        double floorY = this.boundingBox.minY;
        double half = newSize.width / 2.0D;
        this.boundingBox.setBB(AxisAlignedBB.getBoundingBox(this.posX - half, floorY, this.posZ - half,
                this.posX + half, floorY + newSize.height, this.posZ + half));
        /* Entity#setPosition and moveEntity define the legacy 1.7.10 anchor as
         * minY = posY - yOffset + ySize. Keeping only the old AABB floor made
         * the next multiplayer position/correction packet reconstruct a
         * different floor. Preserve the physical floor and move every vertical
         * position sample together so interpolation and packet stance retain
         * that vanilla invariant on both logical sides. */
        double anchoredPosY = floorY + this.yOffset - this.ySize;
        double deltaY = anchoredPosY - this.posY;
        this.posY = anchoredPosY;
        this.prevPosY += deltaY;
        this.lastTickPosY += deltaY;
    }

    private void recalculateEyeHeight() {
        EffectivePlayerGeometry geometry = this.getEffectiveGeometry();
        Pose pose = geometry.pose;
        this.combativesEyeHeight = geometry.eyeAboveMinY;
        /* Cache the conversion while the entity is in its genuine physical
         * coordinate state. Compatibility renderers such as MPM temporarily
         * mutate posY around ray construction without moving the AABB; deriving
         * this value from live posY inside getEyeHeight would cancel that ray
         * transformation. */
        this.combativesLegacyEyeHeight = (float) (this.boundingBox.minY
                + this.combativesEyeHeight - this.posY);
        this.previousEyeHeight = this.eyeHeight;
        MovementDiagnostics.verbose(this.getPlayer(), "eye height recalculated for " + pose + ": " + this.combativesEyeHeight);
    }

    /**
     * Vanilla chooses the bed exit only after it has restored the standing
     * player size.  This state has to be committed before wakeUpPlayer reaches
     * that calculation: waiting for the next player tick leaves
     * combativesSize/appliedGeometry describing the bed-sized body and makes
     * isResizingAllowed reject the vanilla 0.6 x 1.8 size as foreign.
     *
     * The wake transition deliberately bypasses ordinary expansion clearance.
     * The player is still at the bed here; vanilla is about to choose a clear
     * exit using the restored height, and its own setSize has the same forced
     * semantics.
     */
    private void combatives$restoreStandingForWake() {
        this.crawlKeyDown = false;
        this.combatives$setSwimming(false, "wake-up");
        this.setPose(Pose.STANDING);

        EntitySize oldSize = this.combativesSize == null ? STANDING_SIZE : this.combativesSize;
        EffectivePlayerGeometry standing = this.combatives$resolveGeometry(Pose.STANDING);
        EntitySize newSize = new EntitySize(standing.width, standing.height, false);
        this.recalculateSize(oldSize, newSize);
        this.width = newSize.width;
        this.height = newSize.height;
        this.combativesSize = newSize;
        this.combativesAppliedPose = Pose.STANDING;
        this.combativesAppliedGeometry = standing;
        if (!this.worldObj.isRemote) this.combativesGeometryRevision++;
        this.recalculateEyeHeight();
    }

    @Override
    public void logGeometry(String heading, String reason) {
        if (!MovementDiagnostics.isVerboseEnabled()) return;
        MpmCompatibility.Geometry mpm = this.worldObj.isRemote
                ? MpmCompatibility.resolve(this.getPlayer()) : MpmCompatibility.resolveLocal(this.getPlayer());
        EffectivePlayerGeometry geometry = this.getEffectiveGeometry();
        MovementDiagnostics.verbose(this.getPlayer(), heading + " reason=" + reason
                + " side=" + (this.worldObj.isRemote ? "CLIENT" : "SERVER")
                + " tick=" + this.ticksExisted + " geometryRevision=" + this.combativesGeometryRevision
                + " pos=[" + this.posX + "," + this.posY + "," + this.posZ + "]"
                + " prevPos=[" + this.prevPosX + "," + this.prevPosY + "," + this.prevPosZ + "]"
                + " box=[" + this.boundingBox.minY + "," + this.boundingBox.maxY + "]"
                + " boxWidth=" + (this.boundingBox.maxX - this.boundingBox.minX)
                + " boxHeight=" + (this.boundingBox.maxY - this.boundingBox.minY)
                + " entitySize=" + this.width + "x" + this.height
                + " yOffset=" + this.yOffset + " ySize=" + this.ySize
                + " getEyeHeight=" + this.getEyeHeight() + " physicalEyeOffset=" + geometry.eyeAboveMinY
                + " pose=" + this.getPose() + " sneaking=" + this.isSneaking()
                + " swimming=" + this.isSwimming() + " crawling=" + this.crawlKeyDown
                + " mpmRawSize=" + mpm.rawSize + " mpmScale=[" + mpm.widthScale + ","
                + mpm.heightScale + "," + mpm.eyeScale + "] mpmFromData=" + mpm.fromMpm
                + " mpmDisguise=" + mpm.disguiseClass);
    }

    @Override
    public boolean isResizingAllowed() {
        float delta = 0.025F;
        AxisAlignedBB box = this.boundingBox;
        if (this.width < delta || this.height < delta || box.maxX - box.minX < delta || box.maxY - box.minY < delta) return true;
        EntitySize expected = this.combativesSize == null ? STANDING_SIZE : this.combativesSize;
        return Math.abs(this.width / expected.width - 1.0F) < delta && Math.abs(this.height / expected.height - 1.0F) < delta
            && Math.abs((box.maxX - box.minX) / expected.width - 1.0F) < delta && Math.abs((box.maxY - box.minY) / expected.height - 1.0F) < delta;
    }

    private float getEyeHeight(Pose pose, EntitySize size) { return pose == Pose.SLEEPING || pose == Pose.DYING ? 0.2F : this.getStandingEyeHeight(pose, size); }
    @Override public boolean isActuallySneaking() { return this.isSneaking(); }
    @Override public float getStandingEyeHeight(Pose pose, EntitySize size) {
        return this.getEffectiveGeometry(pose).eyeAboveMinY;
    }

    @Override public void setPose(Pose pose) {
        pose = pose == null ? Pose.STANDING : pose;
        Pose old = this.getPose();
        if (old != pose) {
            MovementDiagnostics.verbose(this.getPlayer(), "setPose " + old + " -> " + pose + " via " + this.combatives$getPoseCaller());
        }
        this.combativesPose = pose;
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
        return this.combativesPose == null ? Pose.STANDING : this.combativesPose;
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
        boolean changed = this.crawlKeyDown != down;
        if (changed) {
            MovementDiagnostics.debug(this.getPlayer(), "crawl request " + (down ? "accepted" : "released"));
        }
        this.crawlKeyDown = down;
        if (changed && this.worldObj != null && this.worldObj.isRemote) {
            if (down) {
                this.setPose(Pose.SWIMMING);
            } else if (!this.isSwimming() && this.getPose() == Pose.SWIMMING && this.isPoseClear(Pose.STANDING)) {
                this.setPose(Pose.STANDING);
            }
            this.recalculateSize();
        }
    }

    @Inject(method = "getEyeHeight", at = @At("HEAD"), cancellable = true)
    private void combatives$getEyeHeight(CallbackInfoReturnable<Float> cir) {
        if (this.combativesEyeHeight > 0.0F) {
            /*
             * 1.7.10 Entity position is not the AABB floor. resetPositionToBB
             * reconstructs posY as minY + yOffset - ySize, and the vanilla
             * targeting path adds getEyeHeight() to that legacy position.
             * Pose geometry stores the modern, useful "above minY" value, so
             * use the conversion cached with the applied physical pose rather
             * than counting the legacy position offset a second time or
             * reacting to a renderer's temporary position mutation.
             */
            cir.setReturnValue(this.combativesLegacyEyeHeight);
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
        this.combatives$updateMountLifecycle();
        this.updatePose();
        this.combatives$warnUnexpectedStepHeight("post-player-tick");
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

        if (this.isRiding()) {
            // A rider overlapping its mount is normal. Entity collision must not
            // keep Combatives' prone box alive across the vanilla mount lifecycle.
            if (this.crawlKeyDown) {
                this.setCrawlKeyDown(false);
            }
            this.combatives$setSwimming(false, "mounted");
            this.combatives$selectPose(Pose.STANDING);
            return;
        }

        if (this.combativesDismountHandoff) {
            if (!this.isPoseClear(Pose.STANDING)) {
                // Preserve vanilla's full player box while vanilla or the mount
                // resolves its exit position. Shrinking here lets a player slide
                // into vehicle collision that a vanilla-sized rider cannot enter.
                this.combatives$selectPose(Pose.STANDING);
                if (this.ticksExisted - this.combativesLastMountWaitLogTick >= 20) {
                    this.combativesLastMountWaitLogTick = this.ticksExisted;
                    this.combatives$logMountState("dismount handoff waiting for standing clearance", this.combativesDismountedEntity);
                }
                return;
            }
            this.combativesDismountHandoff = false;
            this.combatives$logMountState("dismount handoff complete", this.combativesDismountedEntity);
            this.combativesDismountedEntity = null;
        }

        if (this.capabilities.isFlying || this.isOnLadder()) {
            if (this.crawlKeyDown) {
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
        } else if (this.isActuallySneaking() && !this.capabilities.isFlying && (this.onGround || !this.isInWater()) && !this.isOnLadder()) {
            pose = Pose.CROUCHING;
        } else if (this.isPoseClear(Pose.STANDING)) {
            if (!this.worldObj.isRemote) {
                this.removePotionEffect(Potion.moveSlowdown.id);
                this.removePotionEffect(Potion.digSlowdown.id);
            }
            pose = Pose.STANDING;
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

    private void combatives$updateMountLifecycle() {
        Entity current = this.ridingEntity;
        if (current == this.combativesLastRidingEntity) {
            return;
        }
        if (this.combativesLastRidingEntity != null && current == null) {
            this.combativesDismountedEntity = this.combativesLastRidingEntity;
            this.combativesDismountHandoff = true;
            this.combatives$logMountState("dismount detected", this.combativesDismountedEntity);
        } else if (current != null) {
            this.combativesDismountHandoff = false;
            this.combativesDismountedEntity = null;
            this.combatives$logMountState(this.combativesLastRidingEntity == null ? "mount detected" : "riding entity changed", current);
        }
        this.combativesLastRidingEntity = current;
    }

    private void combatives$logMountState(String event, Entity mount) {
        if (!MovementDiagnostics.isVerboseEnabled()) {
            return;
        }
        AxisAlignedBB playerBox = this.boundingBox;
        AxisAlignedBB mountBox = mount == null ? null : mount.boundingBox;
        int collisions = this.worldObj.getCollidingBoundingBoxes(this, this.getBoundingBox(Pose.STANDING)).size();
        MovementDiagnostics.verbose(this.getPlayer(), event
            + " side=" + (this.worldObj.isRemote ? "client" : "server")
            + " tick=" + this.ticksExisted
            + " playerId=" + this.getEntityId()
            + " mountId=" + (mount == null ? "none" : mount.getEntityId())
            + " mountClass=" + (mount == null ? "none" : mount.getClass().getName())
            + " riding=" + (this.ridingEntity != null)
            + " pose=" + this.getPose()
            + " crawlRequest=" + this.crawlKeyDown
            + " size=" + this.width + "x" + this.height
            + " pos=" + this.posX + "," + this.posY + "," + this.posZ
            + " playerAABB=" + playerBox
            + " mountAABB=" + mountBox
            + " standingCollisions=" + collisions
            + " intersectsMount=" + (playerBox != null && mountBox != null && playerBox.intersectsWith(mountBox)));
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
        if (poseChanged) {
            MovementDiagnostics.verbose(this.getPlayer(), "pose selected " + (this.worldObj.isRemote ? "client" : "server") + ": " + pose);
            if (!this.worldObj.isRemote && this.getPlayer() instanceof EntityPlayerMP) {
                PoseSync.broadcastAuthoritativePose((EntityPlayerMP) this.getPlayer(), true);
            }
        }
        this.lastLoggedPose = pose;
        this.recalculateSize();
    }

    private void combatives$warnUnexpectedStepHeight(String source) {
        if (this.ticksExisted - this.combativesLastStepHeightWarningTick < 100) {
            return;
        }
        float before = this.stepHeight;
        PlayerStepHeight.warnIfUnexpected(this.getPlayer(), source);
        if (before != PlayerStepHeight.VANILLA_PLAYER_STEP_HEIGHT) {
            this.combativesLastStepHeightWarningTick = this.ticksExisted;
        }
    }

    private AxisAlignedBB getBoundingBox(Pose pose) {
        return this.getEffectiveGeometry(pose).clearanceBox(this.posX, this.boundingBox.minY, this.posZ);
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
            MovementDiagnostics.verbose(this.getPlayer(), "ordinary water travel delegated to vanilla: motionY=" + this.motionY + " onGround=" + this.onGround + " collidedH=" + this.isCollidedHorizontally + " collidedV=" + this.isCollidedVertically + " crawl=" + this.isCrawlKeyDown() + " swim=" + this.isSwimming());
            super.moveEntityWithHeading(strafe, forward);
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
        this.recalculateSize();
    }

    @Inject(method = "wakeUpPlayer", at = @At("HEAD"))
    private void combatives$beforeWakeUp(boolean immediately, boolean updateWorldFlag, boolean setSpawn,
            CallbackInfo ci) {
        this.combatives$restoreStandingForWake();
    }

    @Inject(method = "wakeUpPlayer", at = @At("RETURN"))
    private void combatives$afterWakeUp(boolean immediately, boolean updateWorldFlag, boolean setSpawn,
            CallbackInfo ci) {
        // resetHeight and the exit setPosition run after the HEAD hook.  Cache
        // the legacy eye conversion from their final standing anchor.
        this.recalculateEyeHeight();
        this.logGeometry("wake-up complete", "vanilla exit placement finished");
    }

    private EntityPlayer getPlayer() { return (EntityPlayer)(Object)this; }
}
