package handmadeguns.client.modelLoader;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.Tessellator;

/**
 * Creates Tessellators owned exclusively by HMG's model compilers.
 *
 * Model display-list compilation can be reached from item rendering while another
 * renderer owns the instrumented global Tessellator. Model compilation must not
 * start or finish that renderer's drawing session, so it deliberately uses a
 * separate vanilla-compatible Tessellator instance.
 */
@SideOnly(Side.CLIENT)
public final class HMGModelTessellator
{
    private HMGModelTessellator()
    {
    }

    public static Tessellator create()
    {
        return new Tessellator();
    }
}