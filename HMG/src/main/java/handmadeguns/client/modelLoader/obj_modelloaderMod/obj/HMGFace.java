package handmadeguns.client.modelLoader.obj_modelloaderMod.obj;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import handmadeguns.HandmadeGunsCore;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.Vec3;

import javax.vecmath.Vector3d;

import static java.lang.Math.abs;

public class HMGFace
{
	public HMGVertex[] vertices;
	public HMGVertex[] HMGVertexNormals;
	public HMGVertex faceNormal;
	public HMGTextureCoordinate[] HMGTextureCoordinates;

	@SideOnly(Side.CLIENT)
	public void addFaceForRender(Tessellator tessellator)
	{
		addFaceForRender(tessellator, 0.0005F);
	}

	@SideOnly(Side.CLIENT)
	public void addFaceForRender(Tessellator tessellator, float textureOffset)
	{
		if (faceNormal == null)
		{
			faceNormal = this.calculateFaceNormal();
		}

		tessellator.setNormal(faceNormal.x, faceNormal.y, faceNormal.z);

		float averageU = 0F;
		float averageV = 0F;

		if ((HMGTextureCoordinates != null) && (HMGTextureCoordinates.length > 0))
		{
			for (int i = 0; i < HMGTextureCoordinates.length; ++i)
			{
				averageU += HMGTextureCoordinates[i].u;
				averageV += HMGTextureCoordinates[i].v;
			}

			averageU = averageU / HMGTextureCoordinates.length;
			averageV = averageV / HMGTextureCoordinates.length;
		}

		float offsetU, offsetV;

		for (int i = 0; i < vertices.length; ++i)
		{

			if ((HMGTextureCoordinates != null) && (HMGTextureCoordinates.length > 0))
			{
				offsetU = textureOffset;
				offsetV = textureOffset;

				if (HMGTextureCoordinates[i].u > averageU)
				{
					offsetU = -offsetU;
				}
				if (HMGTextureCoordinates[i].v > averageV)
				{
					offsetV = -offsetV;
				}

				if(this.HMGVertexNormals !=null && i<this.HMGVertexNormals.length)
				{
					tessellator.setNormal(this.HMGVertexNormals[i].x, this.HMGVertexNormals[i].y, this.HMGVertexNormals[i].z);
				}

				tessellator.addVertexWithUV(vertices[i].x, vertices[i].y, vertices[i].z, HMGTextureCoordinates[i].u + offsetU + HandmadeGunsCore.textureOffsetU, HMGTextureCoordinates[i].v + offsetV + HandmadeGunsCore.textureOffsetV);
			}
			else
			{
				tessellator.addVertex(vertices[i].x, vertices[i].y, vertices[i].z);
			}
		}
	}

	public HMGVertex calculateFaceNormal()
	{
		Vec3 v1 = Vec3.createVectorHelper(vertices[1].x - vertices[0].x, vertices[1].y - vertices[0].y, vertices[1].z - vertices[0].z);
		Vec3 v2 = Vec3.createVectorHelper(vertices[2].x - vertices[0].x, vertices[2].y - vertices[0].y, vertices[2].z - vertices[0].z);
		Vec3 normalVector = null;

		normalVector = v1.crossProduct(v2).normalize();

		return new HMGVertex((float) normalVector.xCoord, (float) normalVector.yCoord, (float) normalVector.zCoord);
	}

	Vector3d vertices_0 = null;
	Vector3d vertices_1 = null;
	Vector3d vertices_2 = null;
	Vector3d relative0_1;
	Vector3d relative0_2;
	Vector3d normal0_1;
	Vector3d relative1_0;
	Vector3d relative1_2;
	Vector3d normal1_2;
	Vector3d relative2_0;
	Vector3d relative2_1;
	Vector3d normal2_0;
	Vector3d relative0_Hit;
	Vector3d relative1_Hit;
	Vector3d relative2_Hit;

	double dist_01_2;
	double dist_12_0;
	double dist_20_1;

	public Vector3d faceNormal_Javax = null;

	public Vector3d hitCheck(final Vector3d start, final Vector3d end)
	{
		try {
			if (vertices_0 == null || vertices_1 == null || vertices_2 == null) {
				vertices_0 = new Vector3d(vertices[0].x, vertices[0].y, vertices[0].z);
				vertices_1 = new Vector3d(vertices[1].x, vertices[1].y, vertices[1].z);
				vertices_2 = new Vector3d(vertices[2].x, vertices[2].y, vertices[2].z);

				faceNormal = this.calculateFaceNormal();
				faceNormal_Javax = new Vector3d(faceNormal.x, faceNormal.y, faceNormal.z);

				relative0_1 = new Vector3d();
				relative0_2 = new Vector3d();
				normal0_1 = new Vector3d();
				relative1_0 = new Vector3d();
				relative1_2 = new Vector3d();
				normal1_2 = new Vector3d();
				relative2_0 = new Vector3d();
				relative2_1 = new Vector3d();
				normal2_0 = new Vector3d();
				relative0_Hit = new Vector3d();
				relative1_Hit = new Vector3d();
				relative2_Hit = new Vector3d();


				relative0_1.sub(vertices_1, vertices_0);
				relative0_2.sub(vertices_2, vertices_0);

				relative1_0.sub(vertices_0, vertices_1);
				relative1_2.sub(vertices_2, vertices_1);

				relative2_0.sub(vertices_0, vertices_2);
				relative2_1.sub(vertices_1, vertices_2);

				normal0_1.cross(relative0_1, faceNormal_Javax);
				normal0_1.normalize();

				normal1_2.cross(relative1_2, faceNormal_Javax);
				normal1_2.normalize();

				normal2_0.cross(relative2_0, faceNormal_Javax);
				normal2_0.normalize();

				dist_01_2 = normal0_1.dot(relative0_2);
				dist_12_0 = normal1_2.dot(relative1_0);
				dist_20_1 = normal2_0.dot(relative2_1);
			}

//		System.out.println("debug Start  " + start);
//		System.out.println("debug End    " + end);
//		System.out.println("debug Vertex1" + vertices_0);
//		System.out.println("debug Vertex2" + vertices_1);
//		System.out.println("debug Vertex3" + vertices_2);


			Vector3d centerPoint = new Vector3d(vertices_0);
			centerPoint.add(vertices_1);
			centerPoint.add(vertices_2);
			centerPoint.scale(1d / 3d);

			Vector3d start_center = new Vector3d(centerPoint);
			start_center.sub(start);
			Vector3d end___center = new Vector3d(centerPoint);
			end___center.sub(end);
			if (start_center.dot(faceNormal_Javax) * end___center.dot(faceNormal_Javax) <= 0) {
				Vector3d start_To_End = new Vector3d();
				start_To_End.sub(end, start);
				start_To_End.scale(faceNormal_Javax.dot(start_center) / faceNormal_Javax.dot(start_To_End));
				start_To_End.add(start);

				relative0_Hit.sub(start_To_End, vertices_0);
				relative1_Hit.sub(start_To_End, vertices_1);
				relative2_Hit.sub(start_To_End, vertices_2);

				double dist_01_hit = normal0_1.dot(relative0_Hit);
				double dist_12_hit = normal1_2.dot(relative1_Hit);
				double dist_20_hit = normal2_0.dot(relative2_Hit);


				if(dist_01_hit * dist_01_2 > 0 &&
						dist_12_hit * dist_12_0 > 0 &&
						dist_20_hit * dist_20_1 > 0){
					return start_To_End;
				}else
					return null;
			} else {
				return null;
			}
//		Vector3d relativeStart = new Vector3d();
//		Vector3d relativeEnd = new Vector3d();
//
//
//		Vector3d vertices_2 = new Vector3d(vertices[0].x,vertices[0].y,vertices[0].z);
//		double startToHitVecLength = faceNormal_Javax.dot(relativeStart);
//
//		Vector3d hitVec = new Vector3d();
//		hitVec.sub(relativeEnd,relativeStart);
//		hitVec.normalize();
//		hitVec.scale(startToHitVecLength);
//		hitVec.add(relativeStart);
//		double xChecker = (hitVec.x - relativeA.x)/(relativeB.x - relativeA.x);
//		boolean xCeck = xChecker<1 && xChecker>0;
//		double yChecker = (hitVec.y - relativeA.y)/(relativeB.y - relativeA.y);
//		boolean yCeck = yChecker<1 && yChecker>0;
//		double zChecker = (hitVec.z - relativeA.z)/(relativeB.z - relativeA.z);
//		boolean zCeck = zChecker<1 && zChecker>0;
//
//		currentHitVec = hitVec;
//		return xCeck && yCeck && zCeck;
		}catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}
}