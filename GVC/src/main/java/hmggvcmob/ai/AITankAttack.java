package hmggvcmob.ai;

import hmggvcmob.SlowPathFinder.WorldForPathfind;
import hmggvcmob.entity.IRideableTank;
import hmggvcmob.entity.ITank;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;

import java.util.Random;

public class AITankAttack extends EntityAIBase {
    private EntityLiving Tank_body;//戦車
    private handmadeguns.entity.IFF IFF;//戦車
    private ITank Tank_SPdata;
    private int attackTime = 0;
    public WorldForPathfind worldForPathfind;
    private EntityLivingBase target;//ターゲット
    private Random rnd;
    private int forget = 0;//忘れるまで
    private boolean fEnable = true;
    private int aimcnt;
    private float maxrenge;
    private float minrenge;
    private boolean dir;
    private boolean movearound;

    public boolean noLineCheck_subfire;

    private int mgBurstRoundMax = 40;
    private int mgBurstRound;
    private int mgBurstRoundCnt;
    private int mgBurstCoolMax = 40;
    private int mgBurstCool;
    private int mgBurstCoolCnt;

    private double lastTargetX;
    private double lastTargetY;
    private double lastTargetZ;


    public AITankAttack(EntityLiving guerrilla,float maxrenge,float minrenge){
        this.Tank_body = guerrilla;
        IFF = (handmadeguns.entity.IFF) guerrilla;
        if(guerrilla instanceof ITank)
            this.Tank_SPdata = (ITank) guerrilla;
        rnd = new Random();
        this.maxrenge = maxrenge;
        this.minrenge = minrenge;
        worldForPathfind = new WorldForPathfind(guerrilla.worldObj);
    }

    public AITankAttack(EntityLiving guerrilla,float maxrenge,float minrenge,int mgBurstRoundMax,int mgBurstCoolMax){
        this(guerrilla,maxrenge,minrenge);
        this.mgBurstRoundMax = mgBurstRoundMax;
        this.mgBurstCoolMax = mgBurstCoolMax;
    }
    @Override
    public boolean shouldExecute() {
        attackTime--;
        EntityLivingBase entityliving = Tank_body.getAttackTarget();
        boolean ismanual = (Tank_body instanceof IRideableTank) && !((IRideableTank) Tank_body).standalone();
        if (ismanual || !fEnable || entityliving == null || entityliving.isDead||forget > 1200) {
            target = null;
            Tank_body.setAttackTarget(null);
            forget = 0;
//            System.out.println("debug");
            return false;
        } else {
            target = entityliving;
            return true;
        }
    }
    public void resetTask() {
        Tank_body.setAttackTarget(null);
    }

    @Override
    public void updateTask() {

        Tank_body.getNavigator().clearPathEntity();
        if (Tank_body.getDistanceSqToEntity(target) > maxrenge || Tank_body.getEntitySenses().canSee(target)) {
            Tank_body.getNavigator().setPath(worldForPathfind.getEntityPathToXYZ(Tank_body, (int) target.posX, (int) target.posY, (int) target.posZ, 80f, true, false, false, true), 1.2);
        } else if (Tank_body.getDistanceSqToEntity(target) < minrenge) {
            Tank_body.getNavigator().setPath(worldForPathfind.getEntityPathToXYZ(Tank_body, (int) target.posX, (int) target.posY, (int) target.posZ, 80f, true, false, false, true), -0.75);
        }else if(movearound){
            if(dir)Tank_body.getNavigator().setPath(worldForPathfind.getEntityPathToXYZ(Tank_body, (int)(Tank_body.posX + target.posZ - Tank_body.posZ), (int) target.posY, (int)(Tank_body.posZ - target.posX + Tank_body.posX), 80f, true, false, false, true), 1.2);
            else Tank_body.getNavigator().setPath(worldForPathfind.getEntityPathToXYZ(Tank_body, (int)(Tank_body.posX - target.posZ + Tank_body.posZ), (int) target.posY, (int)(Tank_body.posZ + target.posX - Tank_body.posX), 80f, true, false, false, true), 1.2);
        }else if(Tank_body.getDistanceSqToEntity(target) < minrenge + 100){
            Tank_body.getNavigator().setPath(worldForPathfind.getEntityPathToXYZ(Tank_body, (int) target.posX, (int) target.posY, (int) target.posZ, 80f, true, false, false, true), -0.75);
        }
        if(rnd.nextInt(100) == 1){
            dir = rnd.nextBoolean();
            movearound = rnd.nextBoolean();
        }
        if(Tank_body.getEntitySenses().canSee(target)){
            lastTargetX = target.posX;
            lastTargetY = target.posY + target.getEyeHeight();
            lastTargetZ = target.posZ;
        }

//        List list = Tank_body.worldObj.getEntitiesWithinAABBExcludingEntity(Tank_body, Tank_body.boundingBox.expand(0.20000000298023224D, 0.0D, 0.20000000298023224D));
//        if (list != null && !list.isEmpty()) {
//            for (int i = 0; i < list.size(); ++i) {
//                Entity colliedentity = (Entity) list.get(i);
//                colliedentity.attackEntityFrom(DamageSource.causeMobDamage(Tank_body),2);
//            }
//        }
//        System.out.println("debug");
        boolean aimed = Tank_body.getEntitySenses().canSee(target) ? Tank_SPdata.getBaseLogic().aimMainTurret_toTarget(target):Tank_SPdata.getBaseLogic().aimMainTurret_toPos(lastTargetX,lastTargetY,lastTargetZ);
        if(mgBurstRoundCnt < mgBurstRound){
            if(Tank_body.getEntitySenses().canSee(target) || noLineCheck_subfire)Tank_SPdata.subFire(target);
            mgBurstRoundCnt++;
        }else {
            if(mgBurstCoolCnt > mgBurstCool){
                mgBurstCool = rnd.nextInt(mgBurstCoolMax);
                mgBurstRound = rnd.nextInt(mgBurstRoundMax);
                mgBurstCoolCnt= 0;
                mgBurstRoundCnt = 0;
            }
            mgBurstCoolCnt++;
        }
        if(aimed) {
            aimcnt++;
            if (aimcnt > 30) {
                Tank_SPdata.mainFire(target);
            }
        }else {
            aimcnt = 0;
        }
    }
    public void setEnable(boolean Value){
        fEnable = Value;
    }
}
