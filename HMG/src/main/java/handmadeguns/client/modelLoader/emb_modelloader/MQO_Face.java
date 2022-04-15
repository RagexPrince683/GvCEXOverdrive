package handmadeguns.client.modelLoader.emb_modelloader;

import handmadeguns.HandmadeGunsCore;
import handmadeguns.client.modelLoader.obj_modelloaderMod.obj.HMGFace;
import handmadeguns.client.modelLoader.obj_modelloaderMod.obj.HMGVertex;
import net.minecraft.client.renderer.Tessellator;

import javax.vecmath.Vector3d;

public class MQO_Face extends HMGFace
{
	public int faceMat;
	public int[] verticesID;
	public HMGVertex[] vertexNormals;
	public HMGVertex faceNormal;
	public MQO_TextureCoordinate[] textureCoordinates;

	public MQO_Face copy()
	{
		MQO_Face f = new MQO_Face();

		return f;
	}

	public void addFaceForRender(Tessellator tessellator)
	{
		addFaceForRender(tessellator, 0.000F);
	}

	public void addFaceForRender(Tessellator tessellator, float textureOffset)
	{
		if (faceNormal == null)
		{
			faceNormal = this.calculateFaceNormal();
		}

		tessellator.setNormal(faceNormal.x, faceNormal.y, faceNormal.z);

		float averageU = 0F;
		float averageV = 0F;

		if ((textureCoordinates != null) && (textureCoordinates.length > 0))
		{
			for (int i = 0; i < textureCoordinates.length; ++i)
			{
				averageU += textureCoordinates[i].u;
				averageV += textureCoordinates[i].v;
			}

			averageU = averageU / textureCoordinates.length;
			averageV = averageV / textureCoordinates.length;
		}

		float offsetU, offsetV;

		for (int i = 0; i < vertices.length; ++i)
		{

			if ((textureCoordinates != null) && (textureCoordinates.length > 0))
			{
				offsetU = textureOffset;
				offsetV = textureOffset;

				if (textureCoordinates[i].u > averageU)
				{
					offsetU = -offsetU;
				}
				if (textureCoordinates[i].v > averageV)
				{
					offsetV = -offsetV;
				}

				if(this.vertexNormals!=null && i<this.vertexNormals.length)
				{
					tessellator.setNormal(this.vertexNormals[i].x, this.vertexNormals[i].y, this.vertexNormals[i].z);
				}

				tessellator.addVertexWithUV(vertices[i].x, vertices[i].y, vertices[i].z, textureCoordinates[i].u + offsetU + HandmadeGunsCore.textureOffsetU, textureCoordinates[i].v + offsetV + HandmadeGunsCore.textureOffsetV);
			}
			else
			{
				tessellator.addVertex(vertices[i].x, vertices[i].y, vertices[i].z);
			}
		}
	}

	private HMGVertex oldVertex = null;
	public HMGVertex calculateFaceNormal()
	{
		if(oldVertex == null) {
			Vector3d v1 = new Vector3d(vertices[1].x - vertices[0].x, vertices[1].y - vertices[0].y, vertices[1].z - vertices[0].z);
			Vector3d v2 = new Vector3d(vertices[2].x - vertices[0].x, vertices[2].y - vertices[0].y, vertices[2].z - vertices[0].z);
			Vector3d normalVector = new Vector3d();

			v1.scale(10);
			v2.scale(10);
			v1.normalize();
			v2.normalize();

			normalVector.cross(v1,v2);
			normalVector.normalize();

			return oldVertex = new HMGVertex((float) normalVector.x, (float) normalVector.y, (float) normalVector.z);
		}else {
			return oldVertex;
		}
	}
}
