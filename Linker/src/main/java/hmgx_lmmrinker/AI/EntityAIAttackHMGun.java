package hmgx_lmmrinker.AI;

import handmadeguns.items.GunInfo;
import handmadeguns.items.guns.HMGItem_Unified_Guns;
import handmadevehicle.SlowPathFinder.WorldForPathfind;
import handmadevehicle.entity.EntityVehicle;
import littleMaidMobX.*;
import mmmlibx.lib.MMM_Helper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import static java.lang.Math.abs;
import static java.lang.Math.atan2;
import static java.lang.Math.toDegrees;
import static net.minecraft.realms.RealmsMth.ceil;
import static net.minecraft.util.MathHelper.wrapAngleTo180_float;

public class EntityAIAttackHMGun extends EntityAIBase implements LMM_IEntityAI {

    protected boolean fEnable;

    protected LMM_EntityLittleMaid fMaid;
    protected EntityPlayer fAvatar;
    protected LMM_InventoryLittleMaid fInventory;
    protected LMM_SwingStatus swingState;
    protected World worldObj;
    public WorldForPathfind worldForPathfind;
    protected EntityLivingBase fTarget;
    protected int fForget;

    protected int fTargetDamegeCounter;

    protected float fTargetHealth;

    protected int fTargetSearchDir;


    public EntityAIAttackHMGun(LMM_EntityLittleMaid pEntityLittleMaid) {
        fMaid = pEntityLittleMaid;
        fAvatar = pEntityLittleMaid.maidAvatar;
        fInventory = pEntityLittleMaid.maidInventory;
        swingState = pEntityLittleMaid.getSwingStatusDominant();
        worldObj = pEntityLittleMaid.worldObj;
        fEnable = true;
        fTargetDamegeCounter = 0;
        fTargetHealth = 0;
        fTargetSearchDir = 0;
        worldForPathfind = new WorldForPathfind(worldObj);
        setMutexBits(3);
    }

    public LMM_IEntityLittleMaidAvatarBase getAvatarIF()
    {
        return (LMM_IEntityLittleMaidAvatarBase)fAvatar;
    }

    @Override
    public boolean shouldExecute() {
        EntityLivingBase entityliving = fMaid.getAttackTarget();
        if (!fEnable || entityliving == null || entityliving.isDead) {
            fMaid.setAttackTarget(null);
            fMaid.setTarget(null);
            if (entityliving != null) {
                fMaid.getNavigator().clearPathEntity();
            }
            fTarget = null;
            fAvatar.stopUsingItem();
            return false;
        } else {
            fTarget = entityliving;
            return true;
        }
    }

    @Override
    public void startExecuting() {
        super.startExecuting();
        fMaid.playSound(fMaid.isBloodsuck() ? LMM_EnumSound.findTarget_B : LMM_EnumSound.findTarget_N, false);
        swingState = fMaid.getSwingStatusDominant();
    }

    @Override
    public boolean continueExecuting() {
        return shouldExecute() || (fTarget != null && !fMaid.getNavigator().noPath());
    }

    @Override
    public void resetTask() {
        fTarget = null;
    }

    public int getNextDir()
    {
        int now = fTargetSearchDir;
        int next = 1 + fMaid.getRNG().nextInt(2);
        if(now==next)
        {
            //	next = 1 + fMaid.getRNG().nextInt(3);
        }
        LMM_LittleMaidMobX.Debug("getNextDir() = " + next +" : "+ (next==3? "FORWARD": next==1? "RIGHT": "LEFT"));
        return next;
    }

    private int coolingPrg = 0;
    @Override
    public void updateTask() {

        double backupPosX = fMaid.posX;
        double backupPosZ = fMaid.posZ;

        if(fMaid.ridingEntity instanceof EntityPlayer)
        {
            double dtx = fTarget.posX - fMaid.posX;
            double dtz = fTarget.posZ - fMaid.posZ;
            double distTarget = MathHelper.sqrt_double(dtx*dtx + dtz*dtz);
            fMaid.posX += dtx / distTarget * 1.0;	// 1m �ڕW�ɋ߂Â���
            fMaid.posZ += dtz / distTarget * 1.0;	// 1m �ڕW�ɋ߂Â���
        }

        double DIST = 80;
        double lrange = DIST * DIST;
        double ldist = fMaid.getDistanceSqToEntity(fTarget);
        boolean lsee = fMaid.getEntitySenses().canSee(fTarget);


        if (lsee) {
            fForget = 0;
        } else {
            fForget++;
        }



        fMaid.getLookHelper().setLookPositionWithEntity(fTarget, 90F, 90F);

        if (ldist < lrange) {

            double atx = fTarget.posX - fMaid.posX;
            double aty = fTarget.posY - fMaid.posY;
            double atz = fTarget.posZ - fMaid.posZ;
            if (fTarget.isEntityAlive()) {


                if(fTarget.getHealth() != fTargetHealth)
                {
                    fTargetHealth = fTarget.getHealth();
                    fTargetDamegeCounter = 0;
                    fTargetSearchDir = 0;
                }
                else
                {
                    if(fTargetDamegeCounter == 0)
                    {
                        fTargetSearchDir = getNextDir();
                    }
                    fTargetDamegeCounter++;
                }

                ItemStack litemstack = fMaid.getCurrentEquippedItem();

                double atl = atx * atx + aty * aty + atz * atz;
                double il = -1D;
                double milsq = 10D;
                Entity masterEntity = fMaid.getMaidMasterEntity();
                if (masterEntity != null && !fMaid.isPlaying()) {

                    double amx = masterEntity.posX - fMaid.posX;
                    double amy = masterEntity.posY - fMaid.posY;//-2D
                    double amz = masterEntity.posZ - fMaid.posZ;


                    il = (amx * atx + amy * aty + amz * atz) / atl;


                    double mix = (fMaid.posX + il * atx) - masterEntity.posX;
                    double miy = (fMaid.posY + il * aty) - masterEntity.posY;// + 2D;
                    double miz = (fMaid.posZ + il * atz) - masterEntity.posZ;

                    milsq = mix * mix + miy * miy + miz * miz;
//					mod_LMM_littleMaidMob.Debug("il:%f, milsq:%f", il, milsq);
                }


                if(!(milsq > 3D || il < 0D))
                {

                    if(fTargetSearchDir == 0)
                    {
                        fTargetSearchDir = getNextDir();
                    }
                }
                int li;
                if (litemstack != null && (litemstack.getItem() instanceof HMGItem_Unified_Guns) && (litemstack.getMaxDamage()!= litemstack.getItemDamage())) {
                    fMaid.mstatAimeBow = true;
                    getAvatarIF().getValueVectorFire(atx, aty, atz, atl);

                    boolean lcanattack = true;
                    boolean ldotarget = true;
                    double tpr = Math.sqrt(atl);
                    Entity lentity = MMM_Helper.getRayTraceEntity(fMaid.maidAvatar, tpr + 1.0F, 1.0F, 1.0F);
                    Item helmid = !fMaid.isMaskedMaid() ? null : fInventory.armorInventory[3].getItem();
                    if (helmid == Items.diamond_helmet || helmid == Items.golden_helmet) {

                        if (lentity != null && fMaid.getIFF(lentity)) {
                            lcanattack = false;
//							mod_LMM_littleMaidMob.Debug("ID:%d-friendly fire to ID:%d.", fMaid.entityId, lentity.entityId);
                        }
                    }
                    if (lentity ==null||lentity == fTarget||lentity instanceof EntityVehicle) {
                        ldotarget = true;
                    }
                    else if(fMaid.getIFF(lentity))
                    {

                        ldotarget = false;
                    }
                    lcanattack &= (milsq > 3D || il < 0D);
                    lcanattack &= ldotarget;
                    lcanattack &= lsee;
                    if ((!lcanattack || fTargetDamegeCounter > 40)) {
                        double tpx = fMaid.posX;
                        double tpy = fMaid.posY;
                        double tpz = fMaid.posZ;
                        tpy += aty;
//						double tpr = Math.sqrt(atl) * 0.5D;
                        tpr = Math.sqrt(atx * atx + atz * atz);
                        if (fTargetSearchDir==1) {
                            tpx += atx*2 / tpr;
                            tpz += atz*2 / tpr;
                            tpx += (atz*5 / tpr );
                            tpz -= (atx*5 / tpr );
                        } else if (fTargetSearchDir==2) {
                            tpx += atx*2 / tpr;
                            tpz += atz*2 / tpr;
                            tpx -= (atz*5 / tpr );
                            tpz += (atx*5 / tpr );
                        } else if(fTargetSearchDir==3) {
                            tpx += atx;
                            tpz += atz;
                        } else if(fTargetSearchDir==0) {
                        }
                        PathEntity pathEntity = worldForPathfind.getEntityPathToXYZ(fMaid,(int)tpx, (int)tpy, (int)tpz,30,true,true,true,true);
                        if(pathEntity != null)fMaid.getNavigator().setPath(pathEntity,1.0);
                    }else {
                        fMaid.getNavigator().clearPathEntity();
                    }

                    if(lcanattack) {
                        fMaid.rotationYaw = wrapAngleTo180_float(fMaid.rotationYaw);
                        float targetrote = wrapAngleTo180_float((float) -toDegrees(atan2(fTarget.posX - fMaid.posX, fTarget.posZ - fMaid.posZ)));
//					System.out.println(" " + vec3.xCoord + " , " +  vec3.zCoord);
                        float Angulardifference = wrapAngleTo180_float(fMaid.rotationYaw - targetrote);
                        if(abs(Angulardifference)<90){
                            if (litemstack.getItem() instanceof HMGItem_Unified_Guns) {
                                if ((fMaid.getSwingStatusDominant().getItemInUseCount() <= 0) && coolingPrg < 0) {
                                    GunInfo gunInfo = ((HMGItem_Unified_Guns) litemstack.getItem()).gunInfo;
                                    float dif = ((HMGItem_Unified_Guns) litemstack.getItem()).getDiffusion(litemstack);
                                    if(dif > gunInfo.spreadDiffusionMax/4){
                                        coolingPrg = ceil((dif - gunInfo.spreadDiffusionmin)/gunInfo.spreadDiffusionReduceRate) + 10;
                                    }
                                    if (((HMGItem_Unified_Guns) litemstack.getItem()).getburstCount(litemstack.getTagCompound().getInteger("HMGMode")) == -1) {
                                        fMaid.setSwing(30, LMM_EnumSound.sighting);
                                        swingState.setItemInUse(litemstack, 20, fMaid);
                                        LMM_LittleMaidMobX.Debug("id:%d redygun.", fMaid.getEntityId());
                                    } else {
                                        LMM_LittleMaidMobX.Debug("id:%d shoot.", fMaid.getEntityId());
                                        fAvatar.stopUsingItem();
                                        if (litemstack.getTagCompound() != null)
                                            litemstack.getTagCompound().setBoolean("TriggerBacked", false);
                                        fMaid.setSwing(20, LMM_EnumSound.shoot);
                                        swingState.setItemInUse(litemstack, 20, fMaid);
                                    }
                                }
                                coolingPrg--;
                            }
                        }else {
                            fAvatar.stopUsingItem();
                            swingState.clearItemInUse(fMaid);
                        }
                    }else {
                        if(fTargetSearchDir == 0)
                            fTargetSearchDir = getNextDir();
                        fAvatar.stopUsingItem();
                        swingState.clearItemInUse(fMaid);
                    }
//            		maidAvatarEntity.setValueRotation();
                    getAvatarIF().setValueVector();
                    if (litemstack.stackSize <= 0) {
                        fMaid.destroyCurrentEquippedItem();
                        fMaid.getNextEquipItem();
                    } else {
                        fInventory.setInventoryCurrentSlotContents(litemstack);
                    }
                }
            }
        } else {
            fTargetDamegeCounter = 0;
            if (fMaid.getNavigator().noPath()) {
                LMM_LittleMaidMobX.Debug("id:%d Target range out.", fMaid.getEntityId());
                fMaid.setAttackTarget(null);
            }
            if (fMaid.isWeaponFullAuto() && getAvatarIF().getIsItemTrigger()) {
                fAvatar.stopUsingItem();
            } else {
                fAvatar.clearItemInUse();
            }

        }



        fMaid.posX = backupPosX;
        fMaid.posZ = backupPosZ;
    }

    @Override
    public void setEnable(boolean pFlag) {
        fEnable = pFlag;
    }

    @Override
    public boolean getEnable() {
        return fEnable;
    }

}