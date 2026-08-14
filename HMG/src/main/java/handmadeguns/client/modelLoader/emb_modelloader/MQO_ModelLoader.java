package handmadeguns.client.modelLoader.emb_modelloader;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelCustom;
import net.minecraftforge.client.model.IModelCustomLoader;
import net.minecraftforge.client.model.ModelFormatException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MQO_ModelLoader implements IModelCustomLoader
{
    private static final Map<String, IModelCustom> MODEL_CACHE = new ConcurrentHashMap<String, IModelCustom>();

    @Override
    public String getType()
    {
        return "Metasequoia model";
    }

    private static final String[] types = { "mqo" };

    @Override
    public String[] getSuffixes()
    {
        return types;
    }

	public static void clearModelCache()
	{
		for (IModelCustom model : MODEL_CACHE.values())
		{
			if (model instanceof MQO_MetasequoiaObject)
			{
				((MQO_MetasequoiaObject) model).releaseDisplayLists();
			}
		}
		MODEL_CACHE.clear();
	}

    public IModelCustom loadInstance(ResourceLocation resource) throws ModelFormatException
    {
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
                cached = new MQO_MetasequoiaObject(resource);
                MODEL_CACHE.put(key, cached);
            }
            return cached;
        }
    }
}
