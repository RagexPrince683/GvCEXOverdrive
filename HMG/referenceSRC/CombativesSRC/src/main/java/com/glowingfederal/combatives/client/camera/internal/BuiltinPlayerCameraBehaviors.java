package com.glowingfederal.combatives.client.camera.internal;

import com.combatives.api.camera.CameraDecayType;
import com.combatives.api.camera.CameraImpulse;
import com.combatives.api.camera.CameraPriority;
import com.combatives.api.camera.CameraStackingMode;
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
import net.minecraft.client.entity.EntityPlayerSP;
import com.glowingfederal.combatives.entity.Pose;
import com.glowingfederal.combatives.entity.player.ICombativesPlayerPose;

/** Conservative, generic local-player consumers of the shared entity motion sample. */
public final class BuiltinPlayerCameraBehaviors {
    private static boolean registered;
    private BuiltinPlayerCameraBehaviors() {}

    public static synchronized void register() {
        if (registered) return;
        EntityBehaviorMetadata metadata = new EntityBehaviorMetadata("combatives", Collections.<String, String>emptyMap());
        EntityCameraBehaviorRegistry.register("combatives:player_landing", 40, metadata, EntityMatchers.assignableClass(EntityPlayerSP.class), factory(0));
        EntityCameraBehaviorRegistry.register("combatives:player_collision", 30, metadata, EntityMatchers.assignableClass(EntityPlayerSP.class), factory(1));
        EntityCameraBehaviorRegistry.register("combatives:player_freefall", 20, metadata, EntityMatchers.assignableClass(EntityPlayerSP.class), factory(2));
        EntityCameraBehaviorRegistry.register("combatives:player_inertia", 10, metadata, EntityMatchers.assignableClass(EntityPlayerSP.class), factory(3));
        EntityCameraBehaviorRegistry.register("combatives:player_crawl", 15, metadata, EntityMatchers.assignableClass(EntityPlayerSP.class), factory(4));
        registered = true;
    }

    private static EntityCameraBehaviorFactory factory(final int kind) {
        return new EntityCameraBehaviorFactory() { public EntityCameraBehavior create() {
            return kind == 0 ? new Landing() : kind == 1 ? new Collision() : kind == 2 ? new Freefall() : kind == 3 ? new Inertia() : new Crawl();
        }};
    }

    private static final class Crawl extends Base {
        private static final CameraImpulse CYCLE_POS=CameraImpulse.builder("combatives:crawl_cycle_pos").rotation(0.34F,0,0).translation(0,0.014F,-0.018F).duration(0.1F).priority(CameraPriority.BACKGROUND).build();
        private static final CameraImpulse CYCLE_NEG=CameraImpulse.builder("combatives:crawl_cycle_neg").rotation(-0.34F,0,0).translation(0,-0.014F,0.018F).duration(0.1F).priority(CameraPriority.BACKGROUND).build();
        private static final CameraImpulse POSTURE=CameraImpulse.builder("combatives:crawl_posture").rotation(0.65F,0,0).translation(0,-0.035F,-0.012F).duration(0.1F).priority(CameraPriority.BACKGROUND).build();
        private static final CameraImpulse PULL=CameraImpulse.builder("combatives:crawl_pull").translation(0,-0.012F,-0.018F).duration(0.16F).attackTime(0.035F).priority(CameraPriority.BACKGROUND).build();
        private float blend,phase,motionWeight; private int cycle;
        void reset(){blend=phase=motionWeight=0;cycle=0;}
        public void onTick(MountCameraContext c,CameraEffectSink sink){
            EntityPlayerSP p=player(c);EntityMotionSample m=c.getMotion();if(p==null||m.isDiscontinuity()){reset();return;}
            boolean crawling=false;
            ICombativesPlayerPose pose=null;
            if(p instanceof ICombativesPlayerPose){
                pose=(ICombativesPlayerPose)p;
                // isActuallySwimming() means "uses the prone pose" in this port and is true for
                // land crawling too.  The authoritative swim flag plus water state distinguish it.
                crawling=pose.getPose()==Pose.SWIMMING&&!pose.isSwimming()&&!p.isInWater();
            }
            float ticks=Math.max(3F,CombativesConfig.crawlTransitionMillis/50F),target=crawling&&CombativesConfig.enableCrawlCamera?1F:0F;
            blend=approach(blend,target,1F/ticks);
            float speed=clamp(m.getHorizontalSpeed()/0.16D,0,1);
            motionWeight+=(speed-motionWeight)*(speed>motionWeight?0.32F:0.22F);
            phase+=0.43F*motionWeight*blend;
            int now=(int)(phase/(float)Math.PI);
            if(now!=cycle&&blend>0.8F&&motionWeight>0.12F){sink.emitImpulse(PULL);cycle=now;}
            EntityCameraBehaviorDiagnostics.crawl(crawling,pose!=null&&pose.isSwimming(),p.isInWater(),pose==null?"unavailable":pose.getPose(),blend,motionWeight,phase,0,0,false);
        }
        public void onRender(MountCameraContext c,CameraEffectSink sink){
            if(blend<=0.001F)return;
            float amp=clamp(blend*CombativesConfig.crawlCameraAmplitude,0,1);
            boolean postureAccepted=sink.emitFrame(POSTURE,amp);
            float wave=(float)Math.sin(phase+c.getPartialTicks()*0.43F*motionWeight);
            float cycleStrength=Math.abs(wave)*amp*motionWeight;
            boolean cycleAccepted=cycleStrength>0.001F&&sink.emitFrame(wave>=0?CYCLE_POS:CYCLE_NEG,cycleStrength);
            EntityCameraBehaviorDiagnostics.crawl(true,false,false,Pose.SWIMMING,blend,motionWeight,phase,wave,cycleStrength,postureAccepted||cycleAccepted);
        }
        private static float approach(float v,float target,float step){return v<target?Math.min(target,v+step):Math.max(target,v-step);}
    }

    private abstract static class Base implements EntityCameraBehavior {
        public void onAttach(MountCameraContext context, CameraEffectSink sink) { reset(); }
        public void onRender(MountCameraContext context, CameraEffectSink sink) {}
        public void onDetach(MountCameraContext context, CameraEffectSink sink) { reset(); }
        void reset() {}
        EntityPlayerSP player(MountCameraContext c) { return c.getRider() instanceof EntityPlayerSP ? (EntityPlayerSP)c.getRider() : null; }
        static float clamp(double value, double min, double max) { return (float)(value < min ? min : value > max ? max : value); }
    }

    private static final class Landing extends Base {
        private boolean grounded = true;
        private double fastestDescent; private float greatestFallDistance;
        private float compression, compressionVelocity, compressionTarget, rollBias, impactEnergy, presentationStrength;
        private int compressionHold;
        void reset() { grounded = true; fastestDescent = 0; greatestFallDistance = 0; compression=compressionVelocity=compressionTarget=rollBias=impactEnergy=presentationStrength=0;compressionHold=0; }
        public void onTick(MountCameraContext c, CameraEffectSink sink) {
            EntityPlayerSP player=player(c); EntityMotionSample m=c.getMotion();
            if(player==null || m.isDiscontinuity()){reset();grounded=player==null||player.onGround;return;}
            if(!player.onGround) { fastestDescent=Math.min(fastestDescent,m.getVerticalVelocity()); greatestFallDistance=Math.max(greatestFallDistance,player.fallDistance); }
            if(CombativesConfig.enableLandingCameraFeedback && player.onGround && !grounded) {
                // Preserve the last unsupported sample before presentation filtering can erase it.
                // previousVelocityY is especially important on the support tick, where sampled motionY
                // has commonly already become zero.
                double preImpactVelocity=Math.min(fastestDescent,m.getPreviousVelocityY());
                double impactSpeed=Math.max(0,-preImpactVelocity);
                double momentumLoss=Math.max(0,m.getVerticalVelocity()-preImpactVelocity);
                double fall=Math.max(0,greatestFallDistance);
                double speedEnergy=clamp((impactSpeed-0.12D)/0.82D,0,1);
                double impulseEnergy=clamp(momentumLoss/0.82D,0,1);
                double distanceEnergy=1D-Math.exp(-Math.max(0,fall-1D)/7D);
                double runningWeight=clamp((m.getHorizontalSpeed()-0.12D)/0.34D,0,1)*0.08D;
                impactEnergy=clamp(speedEnergy*0.55D+impulseEnergy*0.27D+distanceEnergy*0.18D+runningWeight,0,1);
                if(impactEnergy>0.01F) {
                    // Raw impact energy is captured above; only this presentation value is shaped.
                    presentationStrength=clamp(Math.pow(impactEnergy,1.22D)*CombativesConfig.landingFeedbackStrength,0,1);
                    compressionTarget=Math.max(compressionTarget,presentationStrength);
                    compressionHold=2+(int)(presentationStrength*2.5F);
                    rollBias=clamp(m.getLateralAcceleration()*0.28D,-0.18D,0.18D);
                    EntityCameraBehaviorDiagnostics.motionEvent("landing", "phase=impact energy="+impactEnergy+" presentation="+presentationStrength+" preImpactVelocity="+preImpactVelocity+" momentumLoss="+momentumLoss+" fallDistance="+fall+" target="+compressionTarget);
                }
                fastestDescent=0; greatestFallDistance=0;
            }
            if(compressionHold>0) {
                compressionHold--;
                // Drive into the compression rather than snapping to it, then briefly load there.
                compressionVelocity+=(compressionTarget-compression)*0.46F;
                compressionVelocity*=0.42F;
                compression+=compressionVelocity;
            } else {
                compressionTarget=0;
                // Severity lengthens an over-damped, monotonic recovery without adding a bounce train.
                float recovery=0.13F-0.035F*presentationStrength;
                compressionVelocity+=(-recovery*compression-0.72F*compressionVelocity);
                compression+=compressionVelocity;
                if(compression<0)compression=compressionVelocity=0;
                if(Math.abs(compression)<0.0005F&&Math.abs(compressionVelocity)<0.0005F)compression=compressionVelocity=0;
            }
            compression=clamp(compression,0D,1D);
            grounded=player.onGround;
            EntityCameraBehaviorDiagnostics.landing(compressionHold>0?"compression":"recovery",compression,compressionTarget,compressionVelocity,rollBias);
            EntityCameraBehaviorDiagnostics.motionSample("landing",m);
        }
        public void onRender(MountCameraContext c,CameraEffectSink sink){
            if(Math.abs(compression)>0.001F&&CombativesConfig.enableLandingCameraFeedback)sink.emitFrame(CameraImpulse.builder("combatives:player_landing")
                .rotation(5.2F,0,rollBias*1.35F).translation(0,-0.17F,-0.024F).duration(0.1F)
                .priority(CameraPriority.NORMAL).build(),clamp(compression,0,1));
        }
    }

    private static final class Freefall extends Base {
        private int fallingTicks; private float intensity;
        void reset(){fallingTicks=0;intensity=0;}
        public void onTick(MountCameraContext c,CameraEffectSink sink){
            EntityPlayerSP p=player(c);EntityMotionSample m=c.getMotion();if(p==null||m.isDiscontinuity()){reset();return;}
            boolean unsupported=!p.onGround && m.getVerticalVelocity() < -0.27D && m.getVerticalAcceleration() < 0.08D;
            fallingTicks=unsupported?fallingTicks+1:0;
            float speedEnvelope=clamp((-m.getVerticalVelocity()-0.24D)*1.55D,0,1);
            float timeEnvelope=fallingTicks<3?0:clamp((fallingTicks-2)/18D,0,1);
            float target=speedEnvelope*timeEnvelope;
            intensity+=(target-intensity)*(target>intensity?0.14F:0.24F);
            if(CombativesConfig.debugCamera&&((fallingTicks==3)||(fallingTicks==0&&intensity>0.01F)))EntityCameraBehaviorDiagnostics.motionEvent("freefall","active="+(fallingTicks>=3)+" intensity="+intensity+" speedEnvelope="+speedEnvelope+" timeEnvelope="+timeEnvelope);
            EntityCameraBehaviorDiagnostics.motionSample("freefall",m);
        }
        public void onRender(MountCameraContext c,CameraEffectSink sink){
            if(intensity>0.01F&&CombativesConfig.enablePlayerFreefallCamera) sink.emitFrame(CameraImpulse.builder("combatives:player_freefall")
                .rotation(0.72F,0,0).translation(0,-0.052F,0.011F).duration(0.1F).priority(CameraPriority.BACKGROUND).build(),clamp(intensity*CombativesConfig.playerFreefallCameraStrength,0,1));
        }
    }

    private static final class Inertia extends Base {
        private double forward,lateral,turnLag; private float contribution,compositionWeight;
        private boolean grounded=true; private int takeoffBlend,landingBlend;
        void reset(){forward=lateral=turnLag=contribution=0;compositionWeight=1;grounded=true;takeoffBlend=landingBlend=0;}
        public void onTick(MountCameraContext c,CameraEffectSink sink){
            EntityMotionSample m=c.getMotion();if(m.isDiscontinuity()||!CombativesConfig.enablePlayerInertiaCamera){reset();return;}
            EntityPlayerSP p=player(c);boolean onGround=p==null||p.onGround;boolean ascending=!onGround&&m.getVerticalVelocity()>0.04D;
            if(grounded&&ascending)takeoffBlend=4;
            if(!grounded&&onGround)landingBlend=3;
            double rawForward=m.getForwardAcceleration(),rawLateral=m.getLateralAcceleration();
            if(takeoffBlend>0){
                // A jump impulse changes the position-derived horizontal acceleration for one or two
                // samples.  Preserve the pre-jump momentum and slew toward airborne input instead of
                // interpreting that sampling transient as a camera impulse.
                forward=slew(forward,rawForward,0.045D,0.22D);
                lateral=slew(lateral,rawLateral,0.045D,0.22D);
                takeoffBlend--;
            } else {forward=rawForward;lateral=rawLateral;}
            turnLag=m.getYawRate()*m.getHorizontalSpeed()/18D;
            compositionWeight=landingBlend>0?0.35F+(3-landingBlend)*0.325F:1F;if(landingBlend>0)landingBlend--;
            contribution=clamp(Math.max(Math.abs(forward)*2.8D,Math.max(Math.abs(lateral)*2.2D,Math.abs(turnLag)))*compositionWeight,0,1);
            grounded=onGround;
            EntityCameraBehaviorDiagnostics.inertia(rawForward,rawLateral,forward,lateral,turnLag,contribution,ascending,takeoffBlend,compositionWeight);
            EntityCameraBehaviorDiagnostics.motionSample("inertia",m);
        }
        private static double slew(double filtered,double raw,double limit,double alpha){double delta=(raw-filtered)*alpha;return filtered+(delta<-limit?-limit:delta>limit?limit:delta);}
        public void onRender(MountCameraContext c,CameraEffectSink sink){
            if(contribution>0.008F)sink.emitFrame(CameraImpulse.builder("combatives:player_inertia")
                .rotation(clamp(-forward*4.2D,-1.05D,1.05D),clamp(-turnLag*0.24D,-0.28D,0.28D),clamp(-lateral*2.0D-turnLag*0.22D,-0.65D,0.65D))
                .translation(clamp(-lateral*0.012D,-0.012D,0.012D),0,clamp(forward*0.018D,-0.018D,0.018D)).duration(0.1F).priority(CameraPriority.BACKGROUND).build(),clamp(CombativesConfig.playerInertiaCameraStrength*compositionWeight,0,1));
        }
    }

    private static final class Collision extends Base {
        private int cooldown;
        public void onTick(MountCameraContext c,CameraEffectSink sink){
            EntityMotionSample m=c.getMotion();if(cooldown>0)cooldown--;if(m.isDiscontinuity()||!CombativesConfig.enablePlayerCollisionCamera)return;
            double previousSpeed=Math.sqrt(m.getPreviousVelocityX()*m.getPreviousVelocityX()+m.getPreviousVelocityZ()*m.getPreviousVelocityZ());
            double loss=previousSpeed-m.getHorizontalSpeed();
            double impulse=Math.sqrt(m.getAccelerationX()*m.getAccelerationX()+m.getAccelerationZ()*m.getAccelerationZ());
            if(cooldown==0&&previousSpeed>0.18D&&loss>0.105D&&impulse>0.13D){
                float severity=clamp((loss-0.08D)*2.7D+impulse*0.65D,0,1);float strength=clamp(severity*CombativesConfig.playerCollisionCameraStrength,0,1);
                float forward=clamp(m.getForwardAcceleration()*-3.2D,-1,1),side=clamp(m.getLateralAcceleration()*-3.2D,-1,1);
                sink.emitImpulse(CameraImpulse.builder("combatives:player_collision").sourceEntity(player(c))
                    .rotation(2.1F*forward*strength,0,1.5F*side*strength).translation(0.025F*side*strength,0,0.045F*forward*strength)
                    .duration(0.2F).attackTime(0.02F).decayType(CameraDecayType.SMOOTH).priority(CameraPriority.NORMAL).stackingMode(CameraStackingMode.REFRESH_SAME_ID).build());
                cooldown=5;EntityCameraBehaviorDiagnostics.motionEvent("collision","severity="+severity+" speedLoss="+loss+" acceleration="+impulse+" direction=("+forward+","+side+")");
            }
            EntityCameraBehaviorDiagnostics.motionSample("collision",m);
        }
    }
}
