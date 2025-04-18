
package hmggvcmob.render;

 
import hmggvcmob.GVCMobPlus;
import hmggvcmob.render.model.GVCModelGuerrilla;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.entity.EntityLiving; 
import net.minecraft.util.ResourceLocation; 

 

public class GVCRenderSoldierRPG extends RenderBiped 
{ 
	
	private static final ResourceLocation skeletonTextures = new ResourceLocation("gvcmob:textures/mob/soliderrpg.png");
	private static final ResourceLocation skeletonTextures2 = new ResourceLocation("gvcmob:textures/mob/guerrillarpg.png"); 

 
 	public GVCRenderSoldierRPG() 
	{ 
 		
 		super(new GVCModelGuerrilla(), 0.5F);
	} 
 
 
 	
 	@Override 
 	protected ResourceLocation getEntityTexture(EntityLiving par1EntityLiving) 
 	{ 
 		if(GVCMobPlus.cfg_modeGorC){
 			return this.skeletonTextures; 
 		}else{
 			return this.skeletonTextures2; 
 		}
 	} 
 } 

 