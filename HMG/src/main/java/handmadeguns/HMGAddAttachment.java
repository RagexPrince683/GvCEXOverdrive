package handmadeguns;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import handmadeguns.items.*;
import handmadeguns.client.render.HMGRenderItemCustom;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;

public class HMGAddAttachment
{
	public static List Attach = new ArrayList();
	public static List<Item> Magazines = new ArrayList<Item>();

	public static void load(File configfile, boolean isClient, File file1)
	{
		String GunName = null;
		String Namegun = null;
		int kazu = 1;
		String texture = "null";
		String hud = null;
		String ads = "null";
		float zoom = -1;
		boolean isnightvision = false;
		boolean textureOnly = false;
		float zoomads = 1;
		float damagemodify = 1;
		float slowdownrate = 1;
		float speedmodify = 1;


		int fuse = -1;
		boolean needset_fuse = false;
		boolean flame = false;
		boolean needset_flame = false;
		boolean explosion = false;
		boolean needset_explosion = false;
		boolean blockdestroyex = false;
		boolean needset_blockdestroyex = false;
		float explosionlevel = 1;
		boolean needset_explosionlevel = false;
		String bulletmodel = "default";
		boolean needset_bulletmodel = false;
		float gunoffset[] = new float[3];
		float gunrotation[] = new float[3];
		boolean needgunoffset = false;

		boolean canobj = false;
		String  objmodel = "null";
		String objtexture = "null";
		Item itema = null;
		Item itemb = null;
		Item itemc = null;
		Item itemd = null;
		Item iteme = null;
		Item itemf = null;
		Item itemg = null;
		Item itemh = null;
		Item itemi = null;
		String re1 = "abc";
		String re2 = "def";
		String re3 = "ghi";


		float reduceRecoilLevel = 1f;
		float reduceRecoilLevel_ADS = 1f;
		float reduceSpreadLevel = 1f;
		float reduceSpreadLevel_ADS = 1f;
		boolean isbase = false;
		String tabname = null;
		try {
			File file = file1;
			//File file = new File(configfile,"hmg_handmadeguns.txt");
			if (checkBeforeReadfile(file))
			{

				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file),"Shift-JIS"));
				String str;
				while((str = br.readLine()) != null){  // 1行ずつ読み込む
					//System.out.println(str);
					String[] type = str.split(",");

					int guntype = 0;






					if (type.length != 0)
					{//1
						switch (type[0]) {
							case "Texture":
								texture = type[1];
								break;
							case "Stack":
								kazu = Integer.parseInt(type[1]);
								break;
							case "Name":
								Namegun = type[1];
								break;
							case "ScopeTexture":
								hud = type[1];
								break;
							case "Zoom":
								zoom = Float.parseFloat(type[1]);
								isnightvision = Boolean.parseBoolean(type[1]);
								break;
							case "ZoomRender":
								textureOnly = Boolean.parseBoolean(type[1]);
								break;
							case "ScopeOnly":
								textureOnly = Boolean.parseBoolean(type[1]);
								break;
							case "Model":
								canobj = Boolean.parseBoolean(type[1]);
								break;
							case "ObjModel":
								objmodel = type[1];
								break;
							case "ObjTexture":
								objtexture = type[1];
								break;
							case "ReduceRecoilLevel":
								reduceRecoilLevel = Float.parseFloat(type[1]);
								break;
							case "AntiRecoil":
								reduceRecoilLevel = Float.parseFloat(type[1]);
								break;
							case "AntiRecoil_ADS":
								reduceRecoilLevel_ADS = Float.parseFloat(type[1]);
								break;
							case "AntiBure":
								reduceSpreadLevel = Float.parseFloat(type[1]);
								break;
							case "AntiBure_ADS":
								reduceSpreadLevel_ADS = Float.parseFloat(type[1]);
								break;
							case "isBase":
								isbase = Boolean.parseBoolean(type[1]);
								break;
							case "Slowdown":
								slowdownrate = Float.parseFloat(type[1]);
								break;
							case "CenterPoint":
								for (int i = 0; i < 3; i++)
									gunoffset[i] = Float.parseFloat(type[i + 1]);
								needgunoffset = true;
								break;
							case "Tabname":
								tabname = type[1];
								break;
							case "GunRotation":
								for (int i = 0; i < 3; i++)
									gunrotation[i] = Float.parseFloat(type[i + 1]);
								needgunoffset = true;
								break;
						}
						Item newitem = null;
						if(type[0].equals("Model_Sight")){
							GunName = type[1];
							newitem	= new HMGItemSightBase().setUnlocalizedName(GunName)
									.setTextureName("handmadeguns:"+ texture).setCreativeTab(HandmadeGunsCore.tabhmg);
							((HMGItemSightBase)newitem).needgunoffset = needgunoffset;
							((HMGItemSightBase)newitem).gunoffset = gunoffset;
							((HMGItemSightBase)newitem).gunrotation = gunrotation;
							((HMGItemSightBase)newitem).zoomlevel = zoom;
							((HMGItemSightBase)newitem).isnightvision = isnightvision;

							if(hud != null)((HMGItemSightBase)newitem).scopetexture = new ResourceLocation("handmadeguns:textures/misc/" + hud);
							((HMGItemSightBase)newitem).scopeonly = textureOnly;
							if(Namegun != null){
								LanguageRegistry.instance().addNameForObject(newitem, "jp_JP", Namegun);
								LanguageRegistry.instance().addNameForObject(newitem, "en_US", GunName);
							}else{
								LanguageRegistry.instance().addNameForObject(newitem, "en_US", GunName);
							}
							System.out.println("" + GunName);
							Attach.add(newitem);
						}else if(type[0].equals("RedDot")){
							GunName = type[1];
							newitem	= new HMGItemAttachment_reddot().setUnlocalizedName(GunName)
									.setTextureName("handmadeguns:"+ texture).setCreativeTab(HandmadeGunsCore.tabhmg);
							((HMGItemSightBase)newitem).zoomlevel = zoom;
							((HMGItemSightBase)newitem).isnightvision = isnightvision;
							if(hud != null)((HMGItemSightBase)newitem).scopetexture = new ResourceLocation("handmadeguns:textures/misc/" + hud);
							if(Namegun != null){
								LanguageRegistry.instance().addNameForObject(newitem, "jp_JP", Namegun);
								LanguageRegistry.instance().addNameForObject(newitem, "en_US", GunName);
							}else{
								LanguageRegistry.instance().addNameForObject(newitem, "en_US", GunName);
							}
							System.out.println("" + GunName);
							Attach.add(newitem);
						}else if(type[0].equals("SCOPE")){
							GunName = type[1];
							newitem	= new HMGItemAttachment_scope().setUnlocalizedName(GunName)
									.setTextureName("handmadeguns:"+texture).setCreativeTab(HandmadeGunsCore.tabhmg);
							((HMGItemSightBase)newitem).zoomlevel = zoom;
							((HMGItemSightBase)newitem).isnightvision = isnightvision;
							if(hud != null)((HMGItemSightBase)newitem).scopetexture = new ResourceLocation("handmadeguns:textures/misc/" + hud);
							if(Namegun != null){
								LanguageRegistry.instance().addNameForObject(newitem, "jp_JP", Namegun);
								LanguageRegistry.instance().addNameForObject(newitem, "en_US", GunName);
							}else{
								LanguageRegistry.instance().addNameForObject(newitem, "en_US", GunName);
							}
							System.out.println("" + GunName);
							Attach.add(newitem);
						}else if(type[0].equals("Suppressor")){
							GunName = type[1];
							newitem	= new HMGItemAttachment_Suppressor().setUnlocalizedName(GunName)
									.setTextureName("handmadeguns:"+texture).setCreativeTab(HandmadeGunsCore.tabhmg);
							if(Namegun != null){
								LanguageRegistry.instance().addNameForObject(newitem, "jp_JP", Namegun);
								LanguageRegistry.instance().addNameForObject(newitem, "en_US", GunName);
							}else{
								LanguageRegistry.instance().addNameForObject(newitem, "en_US", GunName);
							}
							System.out.println("" + GunName);
							Attach.add(newitem);
						}else if(type[0].equals("Laser")){
							GunName = type[1];
							newitem	= new HMGItemAttachment_laser().setUnlocalizedName(GunName)
									.setTextureName("handmadeguns:"+texture).setCreativeTab(HandmadeGunsCore.tabhmg);
							if(Namegun != null){
								LanguageRegistry.instance().addNameForObject(newitem, "jp_JP", Namegun);
								LanguageRegistry.instance().addNameForObject(newitem, "en_US", GunName);
							}else{
								LanguageRegistry.instance().addNameForObject(newitem, "en_US", GunName);
							}
							System.out.println("" + GunName);
							Attach.add(newitem);
						}else if(type[0].equals("Model_Laser")){
							GunName = type[1];
							newitem	= new HMGItemAttachment_laser().setUnlocalizedName(GunName)
									.setTextureName("handmadeguns:"+texture).setCreativeTab(HandmadeGunsCore.tabhmg);
							if(Namegun != null){
								LanguageRegistry.instance().addNameForObject(newitem, "jp_JP", Namegun);
								LanguageRegistry.instance().addNameForObject(newitem, "en_US", GunName);
							}else{
								LanguageRegistry.instance().addNameForObject(newitem, "en_US", GunName);
							}
							System.out.println("" + GunName);
							Attach.add(newitem);
						}else if(type[0].equals("Right")){
							GunName = type[1];
							newitem	= new HMGItemAttachment_light().setUnlocalizedName(GunName)
									.setTextureName("handmadeguns:"+texture).setCreativeTab(HandmadeGunsCore.tabhmg);
							if(Namegun != null){
								LanguageRegistry.instance().addNameForObject(newitem, "jp_JP", Namegun);
								LanguageRegistry.instance().addNameForObject(newitem, "en_US", GunName);
							}else{
								LanguageRegistry.instance().addNameForObject(newitem, "en_US", GunName);
							}
							System.out.println("" + GunName);
							Attach.add(newitem);
						}else if(type[0].equals("Light")){
							GunName = type[1];
							newitem	= new HMGItemAttachment_light().setUnlocalizedName(GunName)
									.setTextureName("handmadeguns:"+texture).setCreativeTab(HandmadeGunsCore.tabhmg);
							if(Namegun != null){
								LanguageRegistry.instance().addNameForObject(newitem, "jp_JP", Namegun);
								LanguageRegistry.instance().addNameForObject(newitem, "en_US", GunName);
							}else{
								LanguageRegistry.instance().addNameForObject(newitem, "en_US", GunName);
							}
							System.out.println("" + GunName);
							Attach.add(newitem);
						}else if(type[0].equals("Model_Light")){
							GunName = type[1];
							newitem	= new HMGItemAttachment_light().setUnlocalizedName(GunName)
									.setTextureName("handmadeguns:"+texture).setCreativeTab(HandmadeGunsCore.tabhmg);
							if(Namegun != null){
								LanguageRegistry.instance().addNameForObject(newitem, "jp_JP", Namegun);
								LanguageRegistry.instance().addNameForObject(newitem, "en_US", GunName);
							}else{
								LanguageRegistry.instance().addNameForObject(newitem, "en_US", GunName);
							}
							System.out.println("" + GunName);
							Attach.add(newitem);
						}else if(type[0].equals("Grip")){
							GunName = type[1];
							newitem	= new HMGItemAttachment_grip().setUnlocalizedName(GunName)
									.setTextureName("handmadeguns:"+texture).setCreativeTab(HandmadeGunsCore.tabhmg);
							((HMGItemAttachment_grip)newitem).reduceRecoilLevel = reduceRecoilLevel;
							((HMGItemAttachment_grip)newitem).reduceRecoilLevel_ADS= reduceRecoilLevel_ADS;
							((HMGItemAttachment_grip)newitem).reduceSpreadLevel= reduceSpreadLevel;
							((HMGItemAttachment_grip)newitem).reduceSpreadLevel_ADS= reduceSpreadLevel_ADS;
							((HMGItemAttachmentBase)newitem).slowdownrate = slowdownrate;
							if(Namegun != null){
								LanguageRegistry.instance().addNameForObject(newitem, "jp_JP", Namegun);
								LanguageRegistry.instance().addNameForObject(newitem, "en_US", GunName);
							}else{
								LanguageRegistry.instance().addNameForObject(newitem, "en_US", GunName);
							}
							System.out.println("" + GunName);
							Attach.add(newitem);
						}else if(type[0].equals("Model_Grip")){
							GunName = type[1];
							newitem	= new HMGItemAttachment_grip().setUnlocalizedName(GunName)
									.setTextureName("handmadeguns:"+texture).setCreativeTab(HandmadeGunsCore.tabhmg);
							((HMGItemAttachment_grip)newitem).reduceRecoilLevel = reduceRecoilLevel;
							((HMGItemAttachment_grip)newitem).reduceRecoilLevel_ADS= reduceRecoilLevel_ADS;
							((HMGItemAttachment_grip)newitem).reduceSpreadLevel = reduceSpreadLevel;
							((HMGItemAttachment_grip)newitem).reduceSpreadLevel_ADS= reduceSpreadLevel_ADS;
							((HMGItemAttachment_grip)newitem).isbase= isbase;
							((HMGItemAttachmentBase)newitem).slowdownrate = slowdownrate;
							if(Namegun != null){
								LanguageRegistry.instance().addNameForObject(newitem, "jp_JP", Namegun);
								LanguageRegistry.instance().addNameForObject(newitem, "en_US", GunName);
							}else{
								LanguageRegistry.instance().addNameForObject(newitem, "en_US", GunName);
							}
							System.out.println("" + GunName);
							Attach.add(newitem);
						}
						else if(type[0].equals("Magazine")){
							GunName = type[1];
							newitem	= new HMGItemBullet().setUnlocalizedName(GunName).setMaxStackSize(kazu)
									.setTextureName("handmadeguns:"+texture);
							if(Namegun != null){
								LanguageRegistry.instance().addNameForObject(newitem, "jp_JP", Namegun);
								LanguageRegistry.instance().addNameForObject(newitem, "en_US", GunName);
							}else{
								LanguageRegistry.instance().addNameForObject(newitem, "en_US", GunName);
							}
							System.out.println("" + GunName);
							Magazines.add(newitem);
						}
						else if(type[0].equals("BulletAP")){
							GunName = type[1];
							newitem	= new HMGItemBullet_AP().setUnlocalizedName(GunName).setMaxStackSize(kazu)
									.setTextureName("handmadeguns:"+texture);
							((HMGItemBulletBase)newitem).damagemodify = damagemodify;
							((HMGItemBulletBase)newitem).speedmodify = speedmodify;
							((HMGItemBulletBase)newitem).slowdownrate = slowdownrate;

							if(needset_fuse)((HMGItemBulletBase)newitem).fuse = fuse;
							if(needset_flame)((HMGItemBulletBase)newitem).flame = flame;
							if(needset_explosion)((HMGItemBulletBase)newitem).explosion = explosion;
							if(needset_blockdestroyex)((HMGItemBulletBase)newitem).blockdestroyex = blockdestroyex;
							if(needset_explosionlevel)((HMGItemBulletBase)newitem).explosionlevel = explosionlevel;
							if(needset_bulletmodel)((HMGItemBulletBase)newitem).bulletmodel = bulletmodel;
							if(Namegun != null){
								LanguageRegistry.instance().addNameForObject(newitem, "jp_JP", Namegun);
								LanguageRegistry.instance().addNameForObject(newitem, "en_US", GunName);
							}else{
								LanguageRegistry.instance().addNameForObject(newitem, "en_US", GunName);
							}
						}
						else if(type[0].equals("BulletAT")){
							GunName = type[1];
							newitem	= new HMGItemBullet_AT().setUnlocalizedName(GunName).setMaxStackSize(kazu)
									.setTextureName("handmadeguns:"+texture);
							((HMGItemBulletBase)newitem).damagemodify = damagemodify;
							((HMGItemBulletBase)newitem).speedmodify = speedmodify;
							((HMGItemBulletBase)newitem).slowdownrate = slowdownrate;

							if(needset_fuse)((HMGItemBulletBase)newitem).fuse = fuse;
							if(needset_flame)((HMGItemBulletBase)newitem).flame = flame;
							if(needset_explosion)((HMGItemBulletBase)newitem).explosion = explosion;
							if(needset_blockdestroyex)((HMGItemBulletBase)newitem).blockdestroyex = blockdestroyex;
							if(needset_explosionlevel)((HMGItemBulletBase)newitem).explosionlevel = explosionlevel;
							if(needset_bulletmodel)((HMGItemBulletBase)newitem).bulletmodel = bulletmodel;
							if(Namegun != null){
								LanguageRegistry.instance().addNameForObject(newitem, "jp_JP", Namegun);
								LanguageRegistry.instance().addNameForObject(newitem, "en_US", GunName);
							}else{
								LanguageRegistry.instance().addNameForObject(newitem, "en_US", GunName);
							}
						}
						else if(type[0].equals("BulletDart")){
							GunName = type[1];
							newitem	= new HMGItemBullet_AP().setUnlocalizedName(GunName).setMaxStackSize(kazu)
									.setTextureName("handmadeguns:"+texture);
							((HMGItemBulletBase)newitem).damagemodify = damagemodify;
							((HMGItemBulletBase)newitem).speedmodify = speedmodify;
							((HMGItemBulletBase)newitem).slowdownrate = slowdownrate;

							if(needset_fuse)((HMGItemBulletBase)newitem).fuse = fuse;
							if(needset_flame)((HMGItemBulletBase)newitem).flame = flame;
							if(needset_explosion)((HMGItemBulletBase)newitem).explosion = explosion;
							if(needset_blockdestroyex)((HMGItemBulletBase)newitem).blockdestroyex = blockdestroyex;
							if(needset_explosionlevel)((HMGItemBulletBase)newitem).explosionlevel = explosionlevel;
							if(needset_bulletmodel)((HMGItemBulletBase)newitem).bulletmodel = bulletmodel;
							if(Namegun != null){
								LanguageRegistry.instance().addNameForObject(newitem, "jp_JP", Namegun);
								LanguageRegistry.instance().addNameForObject(newitem, "en_US", GunName);
							}else{
								LanguageRegistry.instance().addNameForObject(newitem, "en_US", GunName);
							}
						}
						else if(type[0].equals("BulletFrag")){
							GunName = type[1];
							newitem	= new HMGItemBullet_Frag().setUnlocalizedName(GunName).setMaxStackSize(kazu)
									.setTextureName("handmadeguns:"+texture);
							((HMGItemBulletBase)newitem).damagemodify = damagemodify;
							((HMGItemBulletBase)newitem).speedmodify = speedmodify;
							((HMGItemBulletBase)newitem).slowdownrate = slowdownrate;

							if(needset_fuse)((HMGItemBulletBase)newitem).fuse = fuse;
							if(needset_flame)((HMGItemBulletBase)newitem).flame = flame;
							if(needset_explosion)((HMGItemBulletBase)newitem).explosion = explosion;
							if(needset_blockdestroyex)((HMGItemBulletBase)newitem).blockdestroyex = blockdestroyex;
							if(needset_explosionlevel)((HMGItemBulletBase)newitem).explosionlevel = explosionlevel;
							if(needset_bulletmodel)((HMGItemBulletBase)newitem).bulletmodel = bulletmodel;
							if(Namegun != null){
								LanguageRegistry.instance().addNameForObject(newitem, "jp_JP", Namegun);
								LanguageRegistry.instance().addNameForObject(newitem, "en_US", GunName);
							}else{
								LanguageRegistry.instance().addNameForObject(newitem, "en_US", GunName);
							}
							//Namegun = null;
						}
						else if(type[0].equals("BulletTE")){
							GunName = type[1];
							//	Name = type[2];
							newitem	= new HMGItemBullet_TE(texture).setUnlocalizedName(GunName).setMaxStackSize(kazu)
									//.setTextureName("minecraft:"+"mods" + File.separatorChar + "handmadeguns/attachment/texture/"+texture)
									.setTextureName("handmadeguns:"+texture);
							((HMGItemBulletBase)newitem).damagemodify = damagemodify;
							((HMGItemBulletBase)newitem).speedmodify = speedmodify;
							((HMGItemBulletBase)newitem).slowdownrate = slowdownrate;

							if(needset_fuse)((HMGItemBulletBase)newitem).fuse = fuse;
							if(needset_flame)((HMGItemBulletBase)newitem).flame = flame;
							if(needset_explosion)((HMGItemBulletBase)newitem).explosion = explosion;
							if(needset_blockdestroyex)((HMGItemBulletBase)newitem).blockdestroyex = blockdestroyex;
							if(needset_explosionlevel)((HMGItemBulletBase)newitem).explosionlevel = explosionlevel;
							if(needset_bulletmodel)((HMGItemBulletBase)newitem).bulletmodel = bulletmodel;
							if(Namegun != null){
								LanguageRegistry.instance().addNameForObject(newitem, "jp_JP", Namegun);
								LanguageRegistry.instance().addNameForObject(newitem, "en_US", GunName);
							}else{
								LanguageRegistry.instance().addNameForObject(newitem, "en_US", GunName);
							}
							//Namegun = null;
						}

						if (newitem != null) {
							try {
								if (canobj && isClient) {
									System.out.println("" + objmodel);
									IModelCustom attach = AdvancedModelLoader
											.loadModel(new ResourceLocation("handmadeguns:textures/model/" + objmodel));
									ResourceLocation attachtexture = new ResourceLocation("handmadeguns:textures/model/" + objtexture);
									MinecraftForgeClient.registerItemRenderer(newitem, new HMGRenderItemCustom(attach, attachtexture));
								}
							}catch (Exception e){
								e.printStackTrace();
							}
							newitem.setCreativeTab(HandmadeGunsCore.tabhmg);
							GameRegistry.registerItem(newitem, GunName);
						}













						if(type[0].equals("addRecipe")){
							Item additem = GameRegistry.findItem("HandmadeGuns", type[1]);
							if(additem != null) {
								int kazu1 = Integer.parseInt(type[2]);
								re1 = type[3];
								re2 = type[4];
								re3 = type[5];
								int ia = Integer.parseInt(type[6]);
								int ib = Integer.parseInt(type[7]);
								int ic = Integer.parseInt(type[8]);
								int id = Integer.parseInt(type[9]);
								int ie = Integer.parseInt(type[10]);
								int ief = Integer.parseInt(type[11]);
								int ig = Integer.parseInt(type[12]);
								int ih = Integer.parseInt(type[13]);
								int ii = Integer.parseInt(type[14]);


								itema = Item.getItemById(ia);
								itemb = Item.getItemById(ib);
								itemc = Item.getItemById(ic);
								itemd = Item.getItemById(id);
								iteme = Item.getItemById(ie);
								itemf = Item.getItemById(ief);
								itemg = Item.getItemById(ig);
								itemh = Item.getItemById(ih);
								itemi = Item.getItemById(ii);


								GameRegistry.addRecipe(new ItemStack(additem, kazu1),
										re1,
										re2,
										re3,
										'a', itema,
										'b', itemb,
										'c', itemc,
										'd', itemd,
										'e', iteme,
										'f', itemf,
										'g', itemg,
										'h', itemh,
										'i', itemi
								);
								itema = null;
								itemb = null;
								itemc = null;
								itemd = null;
								iteme = null;
								itemf = null;
								itemg = null;
								itemh = null;
								itemi = null;
							}


						}
						else if(type[0].equals("addSmelting")){
							Item additem = GameRegistry.findItem("HandmadeGuns", type[1]);
							if(additem != null) {
								float xp = Float.parseFloat(type[2]);

								int ia = Integer.parseInt(type[3]);
								itema = Item.getItemById(ia);


								if(itema != null && additem != null)
								GameRegistry.addSmelting(itema, new ItemStack(additem), xp);
								itema = null;
							}
						}


						if(type[0].equals("Recipe1")){
							re1 = type[1];
						}
						if(type[0].equals("Recipe2")){
							re2 = type[1];
						}
						if(type[0].equals("Recipe3")){
							re3 = type[1];
						}
						if(type[0].equals("ItemA") && !type[1].equals("null")){
							itema = GameRegistry.findItem(type[1], type[2]);
						}
						if(type[0].equals("ItemB") && !type[1].equals("null")){
							itemb = GameRegistry.findItem(type[1], type[2]);
						}
						if(type[0].equals("ItemC") && !type[1].equals("null")){
							itemc = GameRegistry.findItem(type[1], type[2]);
						}
						if(type[0].equals("ItemD") && !type[1].equals("null")){
							itemd = GameRegistry.findItem(type[1], type[2]);
						}
						if(type[0].equals("ItemE") && !type[1].equals("null")){
							iteme = GameRegistry.findItem(type[1], type[2]);
						}
						if(type[0].equals("ItemF") && !type[1].equals("null")){
							itemf = GameRegistry.findItem(type[1], type[2]);
						}
						if(type[0].equals("ItemG") && !type[1].equals("null")){
							itemg = GameRegistry.findItem(type[1], type[2]);
						}
						if(type[0].equals("ItemH") && !type[1].equals("null")){
							itemh = GameRegistry.findItem(type[1], type[2]);
						}
						if(type[0].equals("ItemI") && !type[1].equals("null")){
							itemi = GameRegistry.findItem(type[1], type[2]);
						}
						if(type[0].equals("addNewRecipe")){
							Item additem = GameRegistry.findItem(type[1], type[2]);
							int kazu1 = Integer.parseInt(type[3]);
							GameRegistry.addRecipe(new ItemStack(additem, kazu1),
									re1,
									re2,
									re3,
									'a',itema,
									'b',itemb,
									'c',itemc,
									'd',itemd,
									'e',iteme,
									'f',itemf,
									'g',itemg,
									'h',itemh,
									'i',itemi
							);

							itema =null;
							itemb =null;
							itemc =null;
							itemd =null;
							iteme =null;
							itemf =null;
							itemg =null;
							itemh =null;
							itemi =null;
						}



					}//1





				}
				br.close();  // ファイルを閉じる
			}
			else
			{

			}
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private static boolean checkBeforeReadfile(File file){
		if (file.exists()){
			if (file.isFile() && file.canRead()){
				return true;
			}
		}

		return false;
	}
}
