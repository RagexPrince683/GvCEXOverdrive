
package hmgww2.render;


import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;


public class RenderUSA_S extends RenderBiped {

	private ResourceLocation skeletonTextures = new ResourceLocation("hmgww2:textures/mob/usa/USA_S.png");


	public RenderUSA_S() {

		super(new ModelSoldier(), 0.5F);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityLiving par1EntityLiving) {
		return this.skeletonTextures;

	}
}