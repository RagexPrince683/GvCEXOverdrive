package handmadeguns.client.render;

import cpw.mods.fml.client.FMLClientHandler;

import java.util.ArrayList;

public class HMGGunParts_Motions {
    public ArrayList<HMGGunParts_Motion> motions = new ArrayList<HMGGunParts_Motion>();

    public HMGGunParts_Motion[] motionArray;

    public void addmotion(HMGGunParts_Motion motion){
        motions.add(motion);
    }
    public HMGGunParts_Motion_PosAndRotation getpartsMotion(float flame){
        FMLClientHandler.instance().getWorldClient().theProfiler.startSection("RenderVehicle_SearchMotion");
        if(motionArray == null){
            init();
        }
        HMGGunParts_Motion motionkey = this.getmotionobject(flame);
        FMLClientHandler.instance().getWorldClient().theProfiler.endSection();
        if(motionkey != null) return motionkey.posAndRotation(flame);
        return null;
    }

    public void init(){
        motionArray = new HMGGunParts_Motion[motions.size()];
        int cnt = 0;
        for(HMGGunParts_Motion motion : motions){
            motionArray[cnt] = motion;
            cnt ++;
        }
        motions = null;
    }

    public int currentMotionID = 0;
    public HMGGunParts_Motion getmotionobject(float flame){
        currentMotionID = 0;
        int cnt = 0;
        for(HMGGunParts_Motion motion : motionArray){
            if(((cnt == 0 && motion.startflame <= flame) || motion.startflame < flame) && flame <= motion.endflame){
                currentMotionID = cnt;
                return motion;
            }
            cnt++;
        }
        return null;
    }
}
