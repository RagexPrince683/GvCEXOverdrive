package com.glowingfederal.combatives.client.camera.internal;

import com.combatives.api.camera.entity.*;
import com.glowingfederal.combatives.Combatives;
import com.glowingfederal.combatives.config.CombativesConfig;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;

/** Owns client provider instances and lifecycle, but never interprets motion or renders camera output. */
public final class EntityCameraBehaviorManager {
    public static final EntityCameraBehaviorManager INSTANCE = new EntityCameraBehaviorManager();
    private static final String API_VERSION = "1";
    private final EntityMotionSampler sampler = new EntityMotionSampler();
    private Map<EntityBehaviorRegistration, ActiveProvider> active = new LinkedHashMap<EntityBehaviorRegistration, ActiveProvider>();
    private Entity mount, previousMount;
    private EntityBehaviorEnvironment environment;
    private long lastTick = Long.MIN_VALUE;
    private EntityCameraBehaviorManager() {}

    public void update(EntityPlayerSP rider, float partialTicks) {
        // The observed camera entity is the local player while unmounted and the mount while
        // mounted. This keeps the sampler/provider path generic without running two samplers.
        Entity current=rider==null?null:(rider.ridingEntity==null?rider:rider.ridingEntity); long tick=rider==null?0:rider.ticksExisted; Entity previous=mount;
        MountTransition transition=transition(previous,current);
        if(current!=mount){previousMount=previous;MountCameraContext detach=context(rider,previous,previous,null,transition,tick,partialTicks);detachAll(detach);sampler.reset();mount=current;lastTick=Long.MIN_VALUE;environment=current==null?null:environment(rider);}
        if(mount==null)return;
        sampler.sampleTick(mount,tick);
        MountCameraContext context=context(rider,mount,previousMount,current,transition,tick,partialTicks);
        reconcile(context);
        if(tick!=lastTick){for(ActiveProvider provider:active.values())invoke("tick",provider,context);lastTick=tick;}
        for(ActiveProvider provider:active.values())invoke("render",provider,context);
    }

    private EntityBehaviorEnvironment environment(EntityPlayerSP rider) {
        final Map<String,String> values=new java.util.HashMap<String,String>();
        values.put("camera.enabled",String.valueOf(CombativesConfig.enableCombativesCamera));
        values.put("camera.diagnostics",String.valueOf(CombativesConfig.debugCamera));
        EntityBehaviorConfiguration configuration=new EntityBehaviorConfiguration(){
            public boolean getBoolean(String key,boolean fallback){String v=values.get(key);return v==null?fallback:Boolean.parseBoolean(v);}
            public int getInt(String key,int fallback){try{String v=values.get(key);return v==null?fallback:Integer.parseInt(v);}catch(NumberFormatException e){return fallback;}}
            public double getDouble(String key,double fallback){try{String v=values.get(key);return v==null?fallback:Double.parseDouble(v);}catch(NumberFormatException e){return fallback;}}
            public String getString(String key,String fallback){String v=values.get(key);return v==null?fallback:v;}
        };
        long seed=rider.worldObj==null?0L:rider.worldObj.getSeed();
        return new EntityBehaviorEnvironment(rider.worldObj,configuration,Combatives.logger,API_VERSION,new EntityBehaviorHelpers(),new EntityBehaviorRandomFactory(seed));
    }
    private MountCameraContext context(EntityPlayerSP rider,Entity sampled,Entity previous,Entity current,MountTransition transition,long tick,float partialTicks){return new MountCameraContext(rider,current,previous,transition,tick,partialTicks,sampled==null?EntityMotionSample.EMPTY:sampler.render(partialTicks));}
    private void reconcile(MountCameraContext context){
        List<EntityBehaviorRegistration> matches=EntityCameraBehaviorRegistry.matching(mount); EntityCameraBehaviorDiagnostics.matches(mount,matches);
        for(Map.Entry<EntityBehaviorRegistration,ActiveProvider> entry:active.entrySet())if(!matches.contains(entry.getKey()))invoke("detach",entry.getValue(),context);
        Map<EntityBehaviorRegistration,ActiveProvider> ordered=new LinkedHashMap<EntityBehaviorRegistration,ActiveProvider>();
        for(EntityBehaviorRegistration registration:matches){
            ActiveProvider provider=active.get(registration);
            if(provider==null){EntityCameraBehaviorFactory factory=registration.getFactory();EntityCameraBehavior behavior=factory instanceof ContextualEntityCameraBehaviorFactory?((ContextualEntityCameraBehaviorFactory)factory).create(environment,new EntityBehaviorProviderInfo(registration)):factory.create();if(behavior!=null){provider=new ActiveProvider(registration,behavior);invoke("attach",provider,context);}}
            if(provider!=null)ordered.put(registration,provider);
        }
        active=ordered;
    }
    private void invoke(String callback,ActiveProvider provider,MountCameraContext context){
        if("attach".equals(callback)||"detach".equals(callback))EntityCameraBehaviorDiagnostics.lifecycle(callback,provider.registration,context.getCurrentMount()==null?context.getPreviousMount():context.getCurrentMount());
        long start=EntityCameraBehaviorDiagnostics.begin();
        if("attach".equals(callback))provider.behavior.onAttach(context,provider.sink);else if("tick".equals(callback))provider.behavior.onTick(context,provider.sink);else if("render".equals(callback))provider.behavior.onRender(context,provider.sink);else provider.behavior.onDetach(context,provider.sink);
        EntityCameraBehaviorDiagnostics.execution(callback,provider.registration,context.getCurrentMount(),start);
    }
    private void detachAll(MountCameraContext context){for(ActiveProvider provider:active.values())invoke("detach",provider,context);active.clear();}
    private static MountTransition transition(Entity oldMount,Entity newMount){if(oldMount==newMount)return MountTransition.NONE;if(oldMount==null)return MountTransition.ATTACHED;if(newMount==null)return MountTransition.DETACHED;return MountTransition.CHANGED;}
    public void reset(EntityPlayerSP rider){MountCameraContext context=context(rider,mount,mount,null,MountTransition.DETACHED,rider==null?0:rider.ticksExisted,0);detachAll(context);previousMount=mount;mount=null;environment=null;lastTick=Long.MIN_VALUE;sampler.reset();}
    private static final class ActiveProvider { final EntityBehaviorRegistration registration;final EntityCameraBehavior behavior;final CameraEffectSink sink;ActiveProvider(EntityBehaviorRegistration registration,EntityCameraBehavior behavior){this.registration=registration;this.behavior=behavior;sink=new ProviderCameraEffectSink(registration);} }
}
