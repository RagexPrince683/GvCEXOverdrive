package com.glowingfederal.combatives.client.camera.internal;

import com.combatives.api.camera.CameraImpulse;
import com.combatives.api.camera.entity.EntityBehaviorRegistration;
import com.combatives.api.camera.entity.EntityMotionSample;
import com.glowingfederal.combatives.Combatives;
import com.glowingfederal.combatives.config.CombativesConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;

final class EntityCameraBehaviorDiagnostics {
    private EntityCameraBehaviorDiagnostics() {}
    static void matches(Entity mount, java.util.List<EntityBehaviorRegistration> matches) {
        if (!CombativesConfig.verboseCameraDebug) return;
        StringBuilder order=new StringBuilder(); for(EntityBehaviorRegistration r:matches){if(order.length()>0)order.append(", ");order.append(r.getId()).append('@').append(r.getPriority());}
        Combatives.logger.info("Entity camera providers matched mount={} order=[{}]", mount(mount), order);
    }
    static void lifecycle(String event, EntityBehaviorRegistration r, Entity mount) {
        if (!CombativesConfig.debugCamera) return;
        Combatives.logger.info("Entity camera provider {} id={} priority={} owner={} mount={}",event,r.getId(),r.getPriority(),r.getMetadata().getOwningMod(),mount(mount));
    }
    static long begin() { return CombativesConfig.verboseCameraDebug ? System.nanoTime() : 0L; }
    static void execution(String callback, EntityBehaviorRegistration r, Entity mount, long started) {
        if (started == 0L) return;
        Combatives.logger.info("Entity camera provider execution callback={} id={} owner={} mount={} durationNs={}",callback,r.getId(),r.getMetadata().getOwningMod(),mount(mount),System.nanoTime()-started);
    }
    static void effect(EntityBehaviorRegistration r,String kind,CameraImpulse effect,boolean accepted) {
        if (!CombativesConfig.verboseCameraDebug) return;
        Combatives.logger.info("Entity camera provider emitted id={} owner={} kind={} effect={} accepted={}",r.getId(),r.getMetadata().getOwningMod(),kind,effect==null?"null":effect.getEffectId(),accepted);
    }
    static void motionEvent(String provider,String values) {
        if (!CombativesConfig.debugCamera) return;
        Combatives.logger.info("Player motion camera event provider={} {}",provider,values);
    }
    static void landing(String phase,float compression,float target,float velocity,float roll) {
        if (!CombativesConfig.verboseCameraDebug || Math.abs(compression) <= 0.001F) return;
        Combatives.logger.info("Player landing phase={} compression={} springTarget={} springVelocity={} unevenRoll={}",phase,compression,target,velocity,roll);
    }
    static void inertia(double rawForward,double rawLateral,double forward,double lateral,double turn,float contribution,boolean ascent,int takeoffBlend,float composition) {
        if (!CombativesConfig.verboseCameraDebug || contribution <= 0.008F) return;
        Combatives.logger.info("Player inertia contribution={} rawAcceleration=({},{}) filteredAcceleration=({},{}) turnLag={} ascent={} takeoffBlendSamples={} composition={}",contribution,rawForward,rawLateral,forward,lateral,turn,ascent,takeoffBlend,composition);
    }
    static void motionSample(String provider,EntityMotionSample m) {
        if (!CombativesConfig.verboseCameraDebug) return;
        Combatives.logger.info("Player motion sample provider={} velocity=({},{},{}) local=({},{},{}) acceleration=({},{},{}) localAcceleration=({},{},{}) horizontalSpeed={} yawRate={} discontinuity={}",
            provider,m.getVelocityX(),m.getVelocityY(),m.getVelocityZ(),m.getForwardVelocity(),m.getLateralVelocity(),m.getVerticalVelocity(),
            m.getAccelerationX(),m.getAccelerationY(),m.getAccelerationZ(),m.getForwardAcceleration(),m.getLateralAcceleration(),m.getVerticalAcceleration(),m.getHorizontalSpeed(),m.getYawRate(),m.isDiscontinuity());
    }
    static void crawl(boolean detected, boolean swimFlag, boolean inWater, Object pose, float blend, float speed, float phase, float wave, float strength, boolean submitted) {
        if (!CombativesConfig.verboseCameraDebug) return;
        Combatives.logger.info("Crawl camera pipeline detected={} swimFlag={} inWater={} pose={} transitionWeight={} speed={} phase={} wave={} frameStrength={} sinkSubmitted={}",
            detected,swimFlag,inWater,pose,blend,speed,phase,wave,strength,submitted);
    }
    static void horse(float speed,float acceleration,float stride,float amplitude,float cadence,float loading,float roll) {
        if (!CombativesConfig.verboseCameraDebug) return;
        Combatives.logger.info("Horse camera pipeline speed={} acceleration={} stridePhase={} amplitude={} cadence={} loading={} roll={}",speed,acceleration,stride,amplitude,cadence,loading,roll);
    }
    private static String mount(Entity entity){if(entity==null)return "none";String id=EntityList.getEntityString(entity);return (id==null?entity.getClass().getName():id)+"#"+entity.getEntityId();}
}
