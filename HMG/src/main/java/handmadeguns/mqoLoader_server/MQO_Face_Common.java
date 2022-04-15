package handmadeguns.mqoLoader_server;

import handmadeguns.client.modelLoader.obj_modelloaderMod.obj.HMGFace;
import handmadeguns.client.modelLoader.obj_modelloaderMod.obj.HMGVertex;

import javax.vecmath.Vector3d;

public class MQO_Face_Common extends HMGFace
{
	public int[] verticesID;

	public MQO_Face_Common copy()
	{
		MQO_Face_Common f = new MQO_Face_Common();

		return f;
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
