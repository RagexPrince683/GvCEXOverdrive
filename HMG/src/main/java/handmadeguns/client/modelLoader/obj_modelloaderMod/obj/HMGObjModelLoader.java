package handmadeguns.client.modelLoader.obj_modelloaderMod.obj;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;
import net.minecraftforge.client.model.IModelCustomLoader;
import net.minecraftforge.client.model.ModelFormatException;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HMGObjModelLoader implements IModelCustomLoader
{
    private static final Map<String, IModelCustom> MODEL_CACHE = new ConcurrentHashMap<String, IModelCustom>();

    @Override
    public String getType()
    {
        return "OBJ model";
    }

    private static final String[] types = { "obj" };
    @Override
    public String[] getSuffixes()
    {
        return types;
    }

    public static void clearModelCache()
    {
        for (IModelCustom model : MODEL_CACHE.values())
        {
            if (model instanceof HMGWavefrontObject)
            {
                ((HMGWavefrontObject) model).releaseVbos();
            }
        }
        MODEL_CACHE.clear();
        HMGVboModelCache.releaseAll();
    }

    public static IModelCustom loadHMGModel(ResourceLocation resource) throws ModelFormatException
    {
        if (!resource.getResourcePath().toLowerCase(Locale.ROOT).endsWith(".obj"))
        {
            return AdvancedModelLoader.loadModel(resource);
        }

        String key = resource.toString();
        IModelCustom cached = MODEL_CACHE.get(key);
        if (cached != null)
        {
            return cached;
        }

        synchronized (MODEL_CACHE)
        {
            cached = MODEL_CACHE.get(key);
            if (cached == null)
            {
                cached = new HMGWavefrontObject(resource);
                MODEL_CACHE.put(key, cached);
            }
            return cached;
        }
    }

    @Override
    public IModelCustom loadInstance(ResourceLocation resource) throws ModelFormatException
    {
        return loadHMGModel(resource);
    }
}
