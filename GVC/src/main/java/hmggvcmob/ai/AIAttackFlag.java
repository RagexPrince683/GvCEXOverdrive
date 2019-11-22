package hmggvcmob.ai;

import hmggvcmob.IflagBattler;
import handmadevehicle.SlowPathFinder.WorldForPathfind;
import hmggvcmob.tile.TileEntityFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;

import javax.vecmath.Vector3d;
import java.util.Random;

public class AIAttackFlag extends EntityAIBase{
    IflagBattler flagBattler;
    EntityLiving flagBattlerBody;
    private WorldForPathfind worldForPathfind;
    TileEntityFlag targetFlag;
    int repathcntcool = 0;
    @Override
    public boolean shouldExecute() {
        //todo �D��x�͏e����艺�ł��悵�A������������܂ł̋������������5�u���b�N�܂ŋ߂Â�����n���A��苗���܂ő���
        IflagBattler platoonLeader;
        if((platoonLeader = flagBattler.getPlatoonLeader()) != null){
            if(((Entity) platoonLeader).isDead){
                flagBattler.setState((byte) -1);//�s����������
                flagBattler.joinPlatoon(null);
                return false;
            }
            int[] targetPos = platoonLeader.getTargetCampPosition();
            TileEntity temp = flagBattlerBody.worldObj.getTileEntity(targetPos[0], targetPos[1], targetPos[2]);
            if(temp instanceof TileEntityFlag) {
                targetFlag = (TileEntityFlag) temp;
            }
            if(targetFlag != null) {

                flagBattler.setTargetCampPosition(targetPos);
                double meToTDist = flagBattlerBody.getDistance(targetPos[0], targetPos[1], targetPos[2]);
                double leaderToTDist = ((Entity) platoonLeader).getDistance(targetPos[0], targetPos[1], targetPos[2]);

                if (flagBattler.isThisFriendCamp(targetFlag.campObj)) {
                    if (platoonLeader == flagBattler) {//������������
                        flagBattler.setState((byte) 2);//���őҋ@
                    } else {
                        if (meToTDist  < 2) {
                            flagBattler.setState((byte) 3);//�����[�I�E�o����I
                        } else if (meToTDist > 10) {
                            flagBattler.setState((byte) 0);//���āI���̈ʒu��ۂāI
                        }
                    }
                } else {
                    if (platoonLeader == flagBattler) {//������������
                        flagBattler.setState((byte) 2);//�s�����I�������ɍs���I
                    } else {
                        if (meToTDist + 2 > leaderToTDist) {
                            flagBattler.setState((byte) 1);//�s�����I�������ɍs���I
                        } else if (meToTDist + 10 < leaderToTDist) {
                            flagBattler.setState((byte) 0);//�҂āI���̈ʒu��ۂāI
                        }
                    }
                }
                if(flagBattlerBody.getAttackTarget() == null){
                    flagBattlerBody.setAttackTarget(((EntityLiving)platoonLeader).getAttackTarget());
                }
                return true;
            }else {
                flagBattler.joinPlatoon(null);
            }
        }
        flagBattler.setState((byte) -1);//�s����������
        return false;
    }

    public void resetTask() {
        flagBattler.setState((byte) -1);
    }
    public AIAttackFlag(EntityLiving entityLiving,IflagBattler flagBattler,WorldForPathfind worldForPathfind){
        this.flagBattlerBody = entityLiving;
        this.flagBattler = flagBattler;
        this.worldForPathfind = worldForPathfind;
    }
    Random random = new Random();
    int changeMoveModeCNT = 0;
    boolean moveRound = false;
    boolean roundDir = random.nextBoolean();

    @Override
    public void updateTask(){
        //�e��AI�������ĂȂ����͂������őO�i������

        if(changeMoveModeCNT<0){
            moveRound = random.nextBoolean();
            roundDir = random.nextBoolean();
            if(moveRound)
                changeMoveModeCNT = random.nextInt(40);
            else
                changeMoveModeCNT = 200 + random.nextInt(200);
        }
        repathcntcool--;
        if(repathcntcool < 0) {
            repathcntcool = random.nextInt(40);
            flagBattlerBody.getNavigator().clearPathEntity();

            byte state = flagBattler.getState();
            int[] targetPos = flagBattler.getTargetCampPosition();

            if(flagBattler.getPlatoonLeader() == flagBattler){
                flagBattlerBody.getLookHelper().setLookPosition(targetPos[0], targetPos[1], targetPos[2],90,90);
            }

            double meToTDist = flagBattlerBody.getDistanceSq(targetPos[0], targetPos[1], targetPos[2]);
            double meToLeaderDist = flagBattlerBody.getDistanceSqToEntity((Entity) flagBattler.getPlatoonLeader());

            {
                Vector3d reCurToTargetPosition;
                if (meToLeaderDist > 256) {
                    reCurToTargetPosition = new Vector3d(((Entity) flagBattler.getPlatoonLeader()).posX,
                            ((Entity) flagBattler.getPlatoonLeader()).posY,
                            ((Entity) flagBattler.getPlatoonLeader()).posZ);
                    moveRound = false;
                    flagBattler.setState((byte) 0);
                } else {
                    reCurToTargetPosition = new Vector3d(targetPos[0], targetPos[1], targetPos[2]);
                }
                Vector3d moveToVec = new Vector3d(flagBattlerBody.posX, flagBattlerBody.posY, flagBattlerBody.posZ);
                Vector3d reCurToTargetPosition_copy = new Vector3d(reCurToTargetPosition);
                reCurToTargetPosition.sub(moveToVec);
                double speed = 0;

                if (meToTDist > 5) {
                    if (state == 1) {
                        speed = 1;
                    } else if (state == 2) {
                        speed = 0.6;
                    }
                } else if (state == 3) {
                    speed = -1;
                }
                if (moveRound) {
                    reCurToTargetPosition.x = reCurToTargetPosition_copy.z * -1;
                    reCurToTargetPosition.z = reCurToTargetPosition_copy.x;
                    if (roundDir){
                        reCurToTargetPosition.x *= -1;
                        reCurToTargetPosition.z *= -1;
                    }
                    reCurToTargetPosition_copy.scale(speed);
                    reCurToTargetPosition.add(reCurToTargetPosition_copy);
                    speed = 1;
                } else {
                    reCurToTargetPosition.scale(speed != 0 ? 1 : 0);
                }
                moveToVec.add(reCurToTargetPosition);

                flagBattlerBody.getNavigator().setPath(worldForPathfind.getEntityPathToXYZ(flagBattlerBody,
                        MathHelper.floor_double(moveToVec.x),
                        MathHelper.floor_double(moveToVec.y),
                        MathHelper.floor_double(moveToVec.z),
                        80, true, false, false, true), speed);
            }
        }
    }

}
