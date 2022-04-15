package handmadeguns.client.render;

import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.renderer.OpenGlHelper;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.EXTFramebufferObject;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.ARBFramebufferObject.*;
import static org.lwjgl.opengl.EXTFramebufferBlit.*;
import static org.lwjgl.opengl.EXTFramebufferObject.*;
import static org.lwjgl.opengl.GL11.*;

public class FrameBuffer{
	public static int defaultFBOID = -1;
	public static int defaultFBO_DepthID = -1;
	private int listIndex = -1;
	private List<Integer> fboList = new ArrayList<Integer>();
	private List<Integer> texList = new ArrayList<Integer>();
	private List<Integer> stencilList = new ArrayList<Integer>();//depth兼ねてるのでこの命名はおかしい

	public static FrameBuffer create(){
		return new FrameBuffer();
	}

	private int add(){
		int fboID;
		this.fboList.add(fboID = glGenFramebuffersEXT());
		this.texList.add(glGenTextures());
		this.stencilList.add(glGenRenderbuffersEXT());

		return fboID;
	}

	int currentFBOID = -1;

	public void start(){
		int fboID;
		int texID;
		//int depthID;
		int stencilID;


		//標準のfboIDを取得
		if(this.listIndex == -1)this.defaultFBOID = glGetInteger(GL_FRAMEBUFFER_BINDING_EXT);//defaultFBOID = FMLClientHandler.instance().getClient().func_147110_a().field_147616_f;

		//FBOおよびアタッチするTextureとBufferを取得
		if(++this.listIndex >= this.fboList.size()){
			this.add();
		}


		//すでに作られたFBOなどを使いまわす処理
		fboID     = this.fboList.get(this.listIndex);
		texID     = this.texList.get(this.listIndex);
		//depthID   = this.depthList.get(this.listIndex);
		stencilID = this.stencilList.get(this.listIndex);
		currentFBOID = fboID;
		//レンダリング先をFBOに設定
		glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, fboID);

		int width = Display.getWidth();
		int height = Display.getHeight();
		//カラー用のテクスチャをアタッチ
		glBindTexture(GL_TEXTURE_2D, texID);
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_INT, (java.nio.ByteBuffer)null);
		glFramebufferTexture2DEXT(GL_FRAMEBUFFER_EXT, GL_COLOR_ATTACHMENT0_EXT, GL_TEXTURE_2D, texID, 0);

		//StencilBufferとDepthをアタッチ
		glBindRenderbufferEXT(GL_RENDERBUFFER_EXT, stencilID);
//		glRenderbufferStorageEXT(GL_RENDERBUFFER_EXT, GL_DEPTH24_STENCIL8, width, height);
//		glFramebufferRenderbufferEXT(GL_FRAMEBUFFER_EXT, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER_EXT, stencilID);


		OpenGlHelper.func_153186_a(OpenGlHelper.field_153199_f, org.lwjgl.opengl.EXTPackedDepthStencil.GL_DEPTH24_STENCIL8_EXT, width, height);
		OpenGlHelper.func_153190_b(OpenGlHelper.field_153198_e, org.lwjgl.opengl.EXTFramebufferObject.GL_DEPTH_ATTACHMENT_EXT, OpenGlHelper.field_153199_f, stencilID);
		OpenGlHelper.func_153190_b(OpenGlHelper.field_153198_e, org.lwjgl.opengl.EXTFramebufferObject.GL_STENCIL_ATTACHMENT_EXT, OpenGlHelper.field_153199_f, stencilID);

		glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
		glClear(GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT);

	}

	public void attachMotherDepth(){

		int width = Display.getWidth();
		int height = Display.getHeight();
		glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);
		glBindFramebufferEXT(GL_READ_FRAMEBUFFER_EXT, defaultFBOID);
		glBindFramebufferEXT(GL_DRAW_FRAMEBUFFER_EXT, currentFBOID);
		glBlitFramebufferEXT(0, 0, width, height, 0, 0, width, height,
				GL_DEPTH_BUFFER_BIT, GL_NEAREST);
		glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, currentFBOID);
	}

	public int end(){
		int fboID = -1;
		int texID = -1;

		if(this.listIndex >= 0){
			texID = this.texList.get(this.listIndex);
		}

		if(--this.listIndex >= 0){
			fboID = this.fboList.get(this.listIndex);
		}else{
			fboID = this.defaultFBOID;//FMLClientHandler.instance().getClient().func_147110_a().field_147616_f;
			this.listIndex = -1;
		}

		glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, fboID);
		return texID;
	}
}