package handmadeguns.mqoLoader_server;

import handmadeguns.StackTracer;
import handmadeguns.client.modelLoader.obj_modelloaderMod.obj.HMGFace;
import handmadeguns.client.modelLoader.obj_modelloaderMod.obj.HMGGroupObject;
import handmadeguns.client.modelLoader.obj_modelloaderMod.obj.HMGVertex;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

import static java.lang.Integer.parseInt;
import static java.lang.Math.toRadians;

public class MQO_MetasequoiaObjectForCommon
{
	public ArrayList<HMGVertex>		vertices		= new ArrayList<HMGVertex>();
	public ArrayList<HMGGroupObject>	groupObjects	= new ArrayList<HMGGroupObject>();
	private String						fileName;
	private int							vertexNum = 0;
	private int							faceNum = 0;

	public float	min  =  1000000;
	public float	minX =  1000000;
	public float	minY =  1000000;
	public float	minZ =  1000000;

	public float	max  = -1000000;
	public float	maxX = -1000000;
	public float	maxY = -1000000;
	public float	maxZ = -1000000;

	public float	size  = 0;
	public float	sizeX = 0;
	public float	sizeY = 0;
	public float	sizeZ = 0;

	public boolean endLoad = false;
	ExecutorService es;

	public MQO_MetasequoiaObjectForCommon(InputStream inputStream)
	{

		loadObjModel(inputStream);
	}

	public HMGGroupObject getPart(String partName)
	{
		try {
			for (HMGGroupObject groupObject : groupObjects) {
				if (partName.equals(groupObject.name)) {
					return groupObject;
				}
			}
		}catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}






	private void loadObjModel(InputStream inputStream) throws StackTracer
	{
		BufferedReader reader = null;

		String currentLine = null;
		int lineCount = 0;

		try
		{
			reader = new BufferedReader(new InputStreamReader(inputStream));

			while ((currentLine = reader.readLine()) != null)
			{
				lineCount++;
				currentLine = currentLine.replaceAll("\\s+", " ").trim();

				// オブジェクトを探す
				if(isValidGroupObjectLine(currentLine))
				{
					HMGGroupObject group = parseGroupObject(currentLine, lineCount);
					System.out.println("" + group);
					if(group == null)
					{
						continue;
					}
					group.faces = new ArrayList<HMGFace>();

					group.glDrawingMode = -1;

					this.vertices.clear();
					int vertexNum = 0;

					boolean mirror = false;

					double  facet   = Math.cos(59.5 * 3.1415926535 / 180.0);
					boolean shading = false;

					// シェーディングの設定と頂点数読み込み
					while ((currentLine = reader.readLine()) != null)
					{
						lineCount++;
						currentLine = currentLine.replaceAll("\\s+", " ").trim();

						if(currentLine.equals("mirror 1"))
						{
							mirror = true;
						}
						if(currentLine.equals("shading 1"))
						{
							shading = true;
						}

						String s[] = currentLine.split(" ");
						if(s.length==2 && s[0].equals("facet"))
						{
							facet   = Math.cos(toRadians(Double.parseDouble(s[1])));
						}

						if(isValidVertexLine(currentLine))
						{
							vertexNum = Integer.valueOf(currentLine.split(" ")[1]);
							break;
						}
					}

					// 頂点読み込み
					if(vertexNum > 0)
					{
						while ((currentLine = reader.readLine()) != null)
						{
							lineCount++;
							currentLine = currentLine.replaceAll("\\s+", " ").trim();

							String s[] = currentLine.split(" ");
							if(s.length == 3)
							{
								HMGVertex v = new HMGVertex(
										Float.valueOf(s[0]) / 100,
										Float.valueOf(s[1]) / 100,
										Float.valueOf(s[2]) / 100);

								if(v.x < this.minX) this.minX = v.x;
								if(v.y < this.minY) this.minY = v.y;
								if(v.z < this.minZ) this.minZ = v.z;
								if(v.x > this.maxX) this.maxX = v.x;
								if(v.y > this.maxY) this.maxY = v.y;
								if(v.z > this.maxZ) this.maxZ = v.z;

								this.vertices.add(v);

								vertexNum--;

								if(vertexNum <= 0)
								{
									break;
								}
							}
							else if(s.length > 0)
							{
								throw new StackTracer("format error : "+this.fileName+" : line="+lineCount);
							}
						}

						int faceNum = 0;
						// 面数読み込み
						while ((currentLine = reader.readLine()) != null)
						{
							lineCount++;
							currentLine = currentLine.replaceAll("\\s+", " ").trim();

							if(isValidFaceLine(currentLine))
							{
								faceNum = Integer.valueOf(currentLine.split(" ")[1]);
								break;
							}
						}

						if(faceNum > 0)
						{
							while ((currentLine = reader.readLine()) != null)
							{
								lineCount++;
								currentLine = currentLine.replaceAll("\\s+", " ").trim();

								String s[] = currentLine.split(" ");
								if(s.length > 2)
								{
									if(Integer.valueOf(s[0]) >= 3)
									{
										MQO_Face_Common[] faces = parseFace(currentLine, lineCount, mirror);
										for(MQO_Face_Common face : faces)
										{
											group.faces.add(face);
										}
									}
									faceNum--;
									if(faceNum <= 0)
									{
										break;
									}
								}
								else if(s.length > 2 && Integer.valueOf(s[0])!=3)
								{
									throw new StackTracer("found face is not triangle : "+this.fileName+" : line="+lineCount);
								}
							}

//							calcVerticesNormal(group, shading, facet);
						}
					}
					this.vertices.clear();
					groupObjects.add(group);
				}else if(isValidMaterialLine(currentLine)){
				}
			}
		}
		catch (IOException e)
		{
			throw new StackTracer("IO Exception reading model format : "+this.fileName, e);
		}
		finally
		{
			if(this.minX < this.min)	this.min = this.minX;
			if(this.minY < this.min)	this.min = this.minY;
			if(this.minZ < this.min)	this.min = this.minZ;
			if(this.maxX > this.max)	this.max = this.maxX;
			if(this.maxY > this.max)	this.max = this.maxY;
			if(this.maxZ > this.max)	this.max = this.maxZ;
			this.sizeX = this.maxX - this.minX;
			this.sizeY = this.maxY - this.minY;
			this.sizeZ = this.maxZ - this.minZ;
			this.size  = this.max  - this.min;

			this.vertices = null;
			try
			{
				reader.close();
			}
			catch (IOException e)
			{
				// hush
			}

			try
			{
				inputStream.close();
			}
			catch (IOException e)
			{
				// hush
			}
		}
	}



	private MQO_Face_Common[] parseFace(String line, int lineCount, boolean mirror)
	{
		String s[] = line.split("[ VU)(M]+");
		// Format
		// 3 V(0 2 1) M(0) UV(0.30158 0.75859 0.32219 0.75859 0.28098 0.75859)
		// ↓
		// 3	0 2 1	0	0.30158 0.75859	0.32219 0.75859	0.28098 0.75859

		int vnum = Integer.valueOf(s[0]);
		if(vnum!=3 && vnum!=4)
		{
			return new MQO_Face_Common[]{};
		}

		if(vnum == 3)
		{
			MQO_Face_Common face = new MQO_Face_Common();
			face.verticesID = new int[]
				{
					Integer.valueOf(s[3]),
					Integer.valueOf(s[2]),
					Integer.valueOf(s[1]),
				};

			face.vertices = new HMGVertex[]{
					this.vertices.get(face.verticesID[0]),
					this.vertices.get(face.verticesID[1]),
					this.vertices.get(face.verticesID[2]),
			};
			face.faceNormal = face.calculateFaceNormal();

			return new MQO_Face_Common[]{ face };
		}
		else
		{
			MQO_Face_Common face1 = new MQO_Face_Common();
			face1.verticesID = new int[]
					{
						Integer.valueOf(s[3]),
						Integer.valueOf(s[2]),
						Integer.valueOf(s[1]),
					};

			face1.vertices = new HMGVertex[]{
					this.vertices.get(face1.verticesID[0]),
					this.vertices.get(face1.verticesID[1]),
					this.vertices.get(face1.verticesID[2]),
			};

			face1.faceNormal = face1.calculateFaceNormal();


			MQO_Face_Common face2 = new MQO_Face_Common();
			face2.verticesID = new int[]
					{
						Integer.valueOf(s[4]),
						Integer.valueOf(s[3]),
						Integer.valueOf(s[1]),
					};

			face2.vertices = new HMGVertex[]{
					this.vertices.get(face2.verticesID[0]),
					this.vertices.get(face2.verticesID[1]),
					this.vertices.get(face2.verticesID[2]),
			};
			face2.faceNormal = face2.calculateFaceNormal();

			return new MQO_Face_Common[]{ face1, face2 };
		}
	}

	// オブジェクトの開始行かどうか判別
	private static boolean isValidGroupObjectLine(String line)
	{
		// Object "obj4" {
		String[] s = line.split(" ");

		if(s.length < 2 || !s[0].equals("Object"))
		{
			return false;
		}

		if(s[1].length()<4 || s[1].charAt(0)!='"')
		{
			return false;
		}

		return true;
	}
	private static boolean isValidMaterialLine(String line)
	{
		// Object "obj4" {
		String[] s = line.split(" ");

		if(s.length < 2 || !s[0].equals("Material"))
		{
			return false;
		}

		return true;
	}
	private HMGGroupObject parseGroupObject(String line, int lineCount) throws StackTracer
	{
		HMGGroupObject group = null;

		String s[] = line.split(" ");
		String trimmedLine = s[1].substring(1, s[1].length()-1);

		if (trimmedLine.length() > 0)
		{
			group = new HMGGroupObject(trimmedLine,-1);
		}

		return group;
	}

	// 頂点の開始行かどうか判別
	private static boolean isValidVertexLine(String line)
	{
		String s[] = line.split(" ");

		if(!s[0].equals("vertex")) return false;

		return true;
	}

	/***
	 * Verifies that the given line from the model file is a valid face of any of the possible face formats
	 * @param line the line being validated
	 * @return true if the line is a valid face that matches any of the valid face formats, false otherwise
	 */
	private static boolean isValidFaceLine(String line)
	{
		String s[] = line.split(" ");

		if(!s[0].equals("face")) return false;

		return true;
	}
	public String toString(){
		return fileName;
	}
}
