package handmadeguns.client.modelLoader.obj_modelloaderMod.obj;



import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import javax.vecmath.Vector3d;
import java.util.ArrayList;

import static handmadevehicle.Utils.getDistanceSq;

public class HMGGroupObject
{
	public String name;
	public ArrayList<HMGFace> faces = new ArrayList<HMGFace>();
	public int glDrawingMode;
	private int displayList = -1;

	public HMGGroupObject()
	{
		this("");
	}

	public HMGGroupObject(String name)
	{
		this(name, -1);
	}

    public HMGGroupObject(String name, int glDrawingMode)
    {
        this.name = name;
        this.glDrawingMode = glDrawingMode;
    }

	@SideOnly(Side.CLIENT)
    public void initDisplay(){
        this.displayList = net.minecraft.client.renderer.GLAllocation.generateDisplayLists(1);
	    org.lwjgl.opengl.GL11.glNewList(this.displayList, org.lwjgl.opengl.GL11.GL_COMPILE);
	    net.minecraft.client.renderer.Tessellator tessellator = net.minecraft.client.renderer.Tessellator.instance;
        tessellator.startDrawing(glDrawingMode);
        render(tessellator);
        tessellator.draw();

	    org.lwjgl.opengl.GL11.glEndList();
    }

	@SideOnly(Side.CLIENT)
    public void render()
    {
        if(displayList == -1)initDisplay();
        else if(displayList != 0) org.lwjgl.opengl.GL11.glCallList(this.displayList);
        else initDisplay();
    }

    @SideOnly(Side.CLIENT)
    private void render(net.minecraft.client.renderer.Tessellator tessellator)
    {
        if (faces.size() > 0)
        {
            for (HMGFace HMGFace : faces)
            {
                HMGFace.addFaceForRender(tessellator);
            }
        }
    }

	public HMGFace hitCheck(Vector3d start, Vector3d end,Vector3d returnVec){

		double distToHit = getDistanceSq(start,end);

		HMGFace hitFace = null;
		Vector3d hitVec = null;
		for (HMGFace HMGFace : faces)
		{
			Vector3d TemphitVec = HMGFace.hitCheck(start,end);
			if(TemphitVec != null){
				double tempDist = getDistanceSq(start, TemphitVec);
				if(tempDist < distToHit){
					hitFace = HMGFace;
					distToHit = tempDist;
					hitVec = TemphitVec;
				}
			}
		}
		if(hitVec != null)returnVec.set(hitVec);
		return hitFace;
	}
}