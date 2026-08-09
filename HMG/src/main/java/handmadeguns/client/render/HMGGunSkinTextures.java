package handmadeguns.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/** Client-side validation performed only after content-pack resources are refreshed. */
final class HMGGunSkinTextures {
    private static final Map<ResourceLocation, Boolean> AVAILABILITY = new HashMap<ResourceLocation, Boolean>();

    private HMGGunSkinTextures() {}

    static ResourceLocation available(ResourceLocation texture) {
        if (texture == null) return null;
        Boolean available = AVAILABILITY.get(texture);
        if (available == null) {
            InputStream stream = null;
            try {
                stream = Minecraft.getMinecraft().getResourceManager().getResource(texture).getInputStream();
                available = Boolean.TRUE;
            } catch (IOException missing) {
                available = Boolean.FALSE;
            } finally {
                if (stream != null) try { stream.close(); } catch (IOException ignored) {}
            }
            AVAILABILITY.put(texture, available);
        }
        return available.booleanValue() ? texture : null;
    }
}
