package com.glowingfederal.combatives.client.camera.internal;

import com.combatives.api.camera.CameraImpulse;
import com.combatives.api.camera.CameraPriority;
import com.combatives.api.camera.entity.CameraEffectSink;
import com.combatives.api.camera.entity.EntityBehaviorMetadata;
import com.combatives.api.camera.entity.EntityCameraBehavior;
import com.combatives.api.camera.entity.EntityCameraBehaviorFactory;
import com.combatives.api.camera.entity.EntityCameraBehaviorRegistry;
import com.combatives.api.camera.entity.EntityMatchers;
import com.combatives.api.camera.entity.EntityMotionSample;
import com.combatives.api.camera.entity.MountCameraContext;
import com.glowingfederal.combatives.config.CombativesConfig;
import java.util.Collections;
import net.minecraft.entity.passive.EntityHorse;

/** Built-in horse registration; other rideables can register the same provider contract independently. */
public final class BuiltinHorseCameraBehavior {
    private static boolean registered;
    private BuiltinHorseCameraBehavior() {}
    public static synchronized void register() {
        if (registered) return;
        EntityBehaviorMetadata metadata = new EntityBehaviorMetadata("combatives", Collections.<String, String>emptyMap());
        EntityCameraBehaviorRegistry.register("combatives:horse_riding", 35, metadata,
            EntityMatchers.assignableClass(EntityHorse.class), new EntityCameraBehaviorFactory() {
                public EntityCameraBehavior create() { return new Horse(); }
            });
        registered = true;
    }

    static final class Horse implements EntityCameraBehavior {
        private static final CameraImpulse RECOVERY = CameraImpulse.builder("combatives:horse_stride_recovery").rotation(-1.2F,0,0).translation(0,0.042F,-0.018F).duration(0.1F).priority(CameraPriority.BACKGROUND).build();
        private static final CameraImpulse LOADING = CameraImpulse.builder("combatives:horse_stride_loading").rotation(1.8F,0,0).translation(0,-0.066F,0.028F).duration(0.1F).priority(CameraPriority.BACKGROUND).build();
        private static final CameraImpulse ACCELERATE = CameraImpulse.builder("combatives:horse_accelerate").rotation(0.55F,0,0).translation(0,-0.008F,0.025F).duration(0.1F).priority(CameraPriority.BACKGROUND).build();
        private static final CameraImpulse DECELERATE = CameraImpulse.builder("combatives:horse_decelerate").rotation(-0.4F,0,0).translation(0,0.005F,-0.02F).duration(0.1F).priority(CameraPriority.BACKGROUND).build();
        private static final CameraImpulse ROLL_LEFT = CameraImpulse.builder("combatives:horse_turn_left").rotation(0,0,-2F).duration(0.1F).priority(CameraPriority.BACKGROUND).build();
        private static final CameraImpulse ROLL_RIGHT = CameraImpulse.builder("combatives:horse_turn_right").rotation(0,0,2F).duration(0.1F).priority(CameraPriority.BACKGROUND).build();
        private static final CameraImpulse TERRAIN = CameraImpulse.builder("combatives:horse_terrain").translation(0,-0.018F,0).rotation(0.2F,0,0).duration(0.13F).attackTime(0.025F).priority(CameraPriority.BACKGROUND).build();
        private static final CameraImpulse LAND = CameraImpulse.builder("combatives:horse_landing").translation(0,-0.09F,-0.018F).rotation(3.2F,0,0).duration(0.32F).attackTime(0.055F).priority(CameraPriority.NORMAL).build();

        private float amplitude, cadence, acceleration, roll, fallbackPhase;
        private boolean grounded = true;
        private double descent;
        private float fall;

        public void onAttach(MountCameraContext context, CameraEffectSink sink) { reset(); }
        public void onDetach(MountCameraContext context, CameraEffectSink sink) { reset(); }
        private void reset() { amplitude=cadence=acceleration=roll=fallbackPhase=0; grounded=true; descent=0; fall=0; }

        public void onTick(MountCameraContext context, CameraEffectSink sink) {
            EntityMotionSample motion=context.getMotion();
            EntityHorse horse=context.getMount() instanceof EntityHorse?(EntityHorse)context.getMount():null;
            if(horse==null||motion.isDiscontinuity()||!CombativesConfig.enableHorseCamera){reset();grounded=horse==null||horse.onGround;return;}
            float speed=clamp(motion.getHorizontalSpeed(),0,0.65), normalized=smooth(speed,0.005F,0.58F);
            float targetAmplitude=0.018F+0.982F*normalized*normalized;
            float targetCadence=0.08F+1.34F*(float)Math.sqrt(normalized);
            amplitude+=(targetAmplitude-amplitude)*0.18F; cadence+=(targetCadence-cadence)*0.16F; fallbackPhase+=cadence*0.31F;
            float targetAcceleration=clamp(motion.getForwardAcceleration()*5.5D,-1,1);
            acceleration+=(targetAcceleration-acceleration)*0.2F;
            float targetRoll=clamp(motion.getYawRate()/14F,-1,1); roll+=(targetRoll-roll)*0.18F;
            if(!horse.onGround){descent=Math.min(descent,motion.getVerticalVelocity());fall=Math.max(fall,horse.fallDistance);}
            else if(!grounded){float energy=landingEnergy(descent,motion.getPreviousVelocityY(),fall);if(energy>0.03F)sink.emitImpulse(scaleLanding(energy));descent=0;fall=0;}
            if(horse.onGround&&grounded&&Math.abs(motion.getVerticalAcceleration())>0.12D&&Math.abs(motion.getVerticalVelocity())<0.24D){float strength=clamp(CombativesConfig.horseTerrainImpulse,0,1);if(strength>0)sink.emitImpulse(scaleTerrain(strength));}
            grounded=horse.onGround;
        }

        public void onRender(MountCameraContext context, CameraEffectSink sink) {
            if(!CombativesConfig.enableHorseCamera)return;
            EntityHorse horse=context.getMount() instanceof EntityHorse?(EntityHorse)context.getMount():null;if(horse==null)return;
            float partial=context.getPartialTicks();
            float limbAmount=horse.prevLimbSwingAmount+(horse.limbSwingAmount-horse.prevLimbSwingAmount)*partial;
            float limbPhase=horse.limbSwing-horse.limbSwingAmount*(1F-partial);
            float stridePhase=limbAmount>0.015F?limbPhase*0.92F:fallbackPhase;
            float wave=(float)Math.sin(stridePhase), loading=wave<0?-wave:0, recovery=wave>0?wave:0;
            float multiplier=clamp(CombativesConfig.horseCameraAmplitude,0,3), limbEnvelope=clamp(limbAmount*3.2F,0,1), strength=amplitude*multiplier*limbEnvelope;
            float harmonic=0.18F*Math.abs((float)Math.sin(stridePhase*2F));
            if(loading>0)sink.emitFrame(LOADING,clamp((loading+harmonic)*strength,0,1));
            else if(recovery>0)sink.emitFrame(RECOVERY,clamp(recovery*strength,0,1));
            float accelStrength=clamp(Math.abs(acceleration)*multiplier,0,1);if(accelStrength>0.001F)sink.emitFrame(acceleration>=0?ACCELERATE:DECELERATE,accelStrength);
            float rollStrength=clamp(Math.abs(roll)*CombativesConfig.horseTurningRoll,0,1);if(rollStrength>0.001F)sink.emitFrame(roll>=0?ROLL_RIGHT:ROLL_LEFT,rollStrength);
            EntityCameraBehaviorDiagnostics.horse((float)context.getMotion().getHorizontalSpeed(),acceleration,stridePhase,amplitude,cadence,loading,roll);
        }

        private CameraImpulse scaleTerrain(float strength){return CameraImpulse.builder("combatives:horse_terrain").translation(0,TERRAIN.getTranslateY()*strength,0).rotation(TERRAIN.getPitch()*strength,0,0).duration(TERRAIN.getDuration()).attackTime(TERRAIN.getAttackTime()).priority(CameraPriority.BACKGROUND).build();}
        private CameraImpulse scaleLanding(float energy){float strength=clamp(energy*CombativesConfig.horseLanding,0,1);return CameraImpulse.builder("combatives:horse_landing").translation(0,LAND.getTranslateY()*strength,LAND.getTranslateZ()*strength).rotation(LAND.getPitch()*strength,0,0).duration(LAND.getDuration()).attackTime(LAND.getAttackTime()).priority(CameraPriority.NORMAL).build();}
        static float landingEnergy(double descent,double previous,float fall){double impact=Math.max(0,-Math.min(descent,previous));return clamp((impact-0.12D)/0.82D*0.72D+(1D-Math.exp(-Math.max(0,fall-1)/7D))*0.28D,0,1);}
        static float smooth(float value,float low,float high){float t=clamp((value-low)/(high-low),0,1);return t*t*(3-2*t);}
        static float clamp(double value,double low,double high){return(float)(value<low?low:value>high?high:value);}
    }
}
