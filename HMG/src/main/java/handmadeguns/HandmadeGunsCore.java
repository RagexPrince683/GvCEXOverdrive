package handmadeguns;







import java.io.*;
import java.lang.reflect.Field;
import java.nio.channels.FileChannel;
import java.util.*;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.*;
import cpw.mods.fml.common.discovery.ContainerType;
import cpw.mods.fml.common.discovery.ModCandidate;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.ReflectionHelper;
import handmadeguns.blocks.HMGBlockMounter;
import handmadeguns.command.HMG_CommandReloadparm;
import handmadeguns.entity.*;
import handmadeguns.entity.bullets.*;
import handmadeguns.event.*;
import handmadeguns.gunsmithing.GunSmithNetwork;
import handmadeguns.gunsmithing.GunSmithRecipeRegistry;
import handmadeguns.gunsmithing.GunSmithTable;
import handmadeguns.gunsmithing.GunSmithTableTileEntity;
import handmadeguns.items.*;
import handmadeguns.items.guns.HMGItem_Unified_Guns;
import handmadeguns.world.HGWorldGen;
import handmadeguns.world.HGWorldGenConfig;
import handmadevehicle.entity.EntityDummy_rider;
import littleMaidMobX.LMM_EntityLittleMaid;
import net.minecraft.block.Block;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.IntHashMap;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelFormatException;
import org.apache.commons.io.FileUtils;


import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent.KeyInputEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import handmadeguns.gui.HMGGuiHandler;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import static handmadeguns.HMGGunMaker.checkBeforeReadfile;


@Mod(
		modid   = "HandmadeGuns",        // MUST be lowercase, no spaces
		name    = "HandMadeGunsOverdrive",
		version = "1.0.0",               // placeholder value OK when useMetadata=true
		useMetadata = true
)



public class HandmadeGunsCore {

	//@Mod.Instance
	//public static HandmadeGunsCore instance2;
	public static float textureOffsetU;//for textureAnimation
	public static float textureOffsetV;
	public static float smooth = 0;
	static Field mcResourcePackRepository;
	static Field repositoryEntries;
	@SidedProxy(clientSide = "handmadeguns.ClientProxyHMG", serverSide = "handmadeguns.CommonSideProxyHMG")
	public static CommonSideProxyHMG HMG_proxy;
	public static final String MOD_ID = "HandmadeGuns";
	@Mod.Instance("HandmadeGuns")
	public static HandmadeGunsCore INSTANCE;
	//public static CommonProxy proxy;


	//public static final KeyBinding Speedreload = new KeyBinding("Key.proceedreload", Keyboard.KEY_R, "GVCGunsPlus");

	public static GunSmithTable blockGunTable;

	public static boolean isDebugMessage = true;
	public static boolean islmmloaded;
	public static boolean isgvcloaded;

	public static boolean cfg_exprotion = true;
	public static boolean cfg_FriendFireLMM;
	public static boolean cfg_FriendFirePlayerToLMM;
	public static boolean cfg_RenderGunSizeLMM;
	public static boolean cfg_RenderGunAttachmentLMM;

	public static boolean cfg_ZoomRender;
	public static int cfg_FOV;

	public static int MAXGUNSINV;

	public static boolean cfg_RenderPlayer;

	public static boolean cfg_canEjectCartridge;
	public static int cfg_Cartridgetime;

	public static boolean cfg_muzzleflash;
	public static boolean cfg_forceunifiedguns = true;

	public static int cfg_ADS_Sneaking;
	public static boolean cfg_ADS_Toggle;
	public static boolean cfg_Avoid_ALL_ConflictKeys;
	public static boolean cfg_Sneak_ByADSKey;
	public static String cfg_Avoid_Hit_Entitys;
	public static boolean cfg_ThreadHitCheck;
	public static int cfg_ThreadHitCheck_split_length;
	public static double cfg_defgravitycof = 0.1;
	public static boolean cfg_blockdestroy;
	public static boolean cfg_Flash;
	public static double cfg_defaultknockback;
	public static double cfg_defaultknockbacky;
	public static boolean cfgRender_useStencil = true;


	public static Item hmg_bullet;
	public static Item hmg_bullet_hg;
	public static Item hmg_bullet_shell;
	public static Item hmg_bullet_rr;
	public static Item hmg_bullet_lmg;

	public static Item hmg_battlepack;


	public static Item hmg_reddot;
	public static Item hmg_scope;
	public static Item hmg_bayonet;
	public static Item hmg_Suppressor;
	public static Item hmg_laser;
	public static Item hmg_right;
	public static Item hmg_grip;

	public static Item hmg_handing;
	public static Item hmg_handing2;

	//public static Item[] guns;
	public static List guns = new ArrayList();

	//protected static final File optionsDir = new File(Minecraft.getMinecraft().mcDataDir,"config" + File.separatorChar + "handmadeguns");



	protected static File configFile;

	public static final CreativeTabs tabhmg = new HMGCreativeTab("HMGTab");
	public static Map<String, CreativeTabs> tabshmg = new HashMap<String, CreativeTabs>();
	//TODO:FIELDS
	public static final String assetsfilepath = "mods" + File.separatorChar + "handmadeguns" + File.separatorChar + "assets" + File.separatorChar + "handmadeguns" + File.separatorChar;
	public static ArrayList<Invocable> scripts = new ArrayList<Invocable>();



	public static void Debug(String pText, Object... pData) {
		if (isDebugMessage) {
			System.out.println(String.format("HandmadeGuns-" + pText, pData));
		}
	}

	//@net.minecraftforge.fml.common.Mod.EventHandler
	@EventHandler
	public void preInit(FMLPreInitializationEvent pEvent) {
		MinecraftForge.EVENT_BUS.register(this);

		//WhizEventHandler whizHandler = new WhizEventHandler();
		//MinecraftForge.EVENT_BUS.register(whizHandler);
		//FMLCommonHandler.instance().bus().register(whizHandler);
		configFile = pEvent.getSuggestedConfigurationFile();
		Configuration lconf = new Configuration(configFile);
		lconf.load();

		//GameRegistry.registerTileEntity(GunSmithTableTileEntity.class, "GunSmithTableTileEntity");


		cfg_FriendFireLMM	= lconf.get("LMM", "cfg_FriendFireLMM", true).getBoolean(true);
		cfg_FriendFirePlayerToLMM	= lconf.get("LMM", "cfg_FriendFirePlayerToLMM", true).getBoolean(true);
		cfg_RenderGunSizeLMM	= lconf.get("LMM", "cfg_RenderGunSizeLMM", false).getBoolean(false);
		cfg_RenderGunAttachmentLMM	= lconf.get("LMM", "cfg_RenderGunAttachmentLMM", false).getBoolean(false);
		cfg_ZoomRender	= lconf.get("Render", "cfg_ZoomRender", true).getBoolean(true);
		cfg_FOV	= lconf.get("Render", "cfg_FOV", 95).getInt(95);

		MAXGUNSINV	= lconf.get("Gun", "MAXGUNSINV", 2).getInt(2);

		//TODO: multiply all render offset crap by the player's actual FOV setting. That way we aren't locking people to a specific FOV for proper display.
		// I made ALL MY GUNS USING 95 FUCKING FOV
		// update: this literally has ZERO usages. I don't even know why it's in the mod as a config option.
		cfg_RenderPlayer	= lconf.get("Render", "cfg_RenderPlayer", false).getBoolean(false);
		cfgRender_useStencil = lconf.get("Render", "cfg_useStencil", false).getBoolean(false);
		cfg_canEjectCartridge	= lconf.get("Cartridge", "cfg_canEjectCartridge", true).getBoolean(true);
		cfg_Cartridgetime	= lconf.get("Cartridge", "cfg_Cartridgetime", 200).getInt(200);
		cfg_muzzleflash	= lconf.get("Gun", "cfg_MuzzleFlash", true).getBoolean(true);
		cfg_ADS_Sneaking	= lconf.get("Gun", "cfg_ADS_Sneaking",  0).getInt(0);
		cfg_ADS_Toggle	= lconf.get("Gun", "cfg_ADS_Key_Toggle",  true).getBoolean(true);
		cfg_Sneak_ByADSKey	= lconf.get("Gun", "cfg_Sneak_ByADSKey",  false).getBoolean(false);
		cfg_Avoid_ALL_ConflictKeys	= lconf.get("Gun", "cfg_Avoid_ALL_ConflictKeys",  true).getBoolean(true);
		cfg_blockdestroy = lconf.get("Gun", "cfg_blockdestroy",  true).getBoolean(true);
		cfg_Avoid_Hit_Entitys = lconf.getString("Gun", "cfg_AvoidHit",  "","");
		cfg_ThreadHitCheck = lconf.get("Gun", "cfg_ThreadHitCheck", true).getBoolean(true);
		cfg_ThreadHitCheck_split_length = lconf.get("Gun", "cfg_ThreadHitCheck_split_length", 10).getInt(10);
		cfg_Flash	= lconf.get("Render", "cfg_Flash", true).getBoolean(true);
		cfg_defaultknockback = lconf.get("Gun", "cfg_KnockBack", 0.05).getDouble(0.05);
		cfg_defaultknockbacky = lconf.get("Gun", "cfg_KnockBackY", 0.01).getDouble(0.01);

		lconf.save();

		hmg_bullet	= new HMGItemBullet().setUnlocalizedName("bulletbase_hmg").setTextureName("handmadeguns:base")
		;
		GameRegistry.registerItem(hmg_bullet, "bulletbase_hmg");


		hmg_handing	= new ItemHangingEntityHMG(0).setUnlocalizedName("hmg_handing")
				//.setTextureName("item_frame")
				.setTextureName("handmadeguns:GunRack")
				.setCreativeTab(tabhmg);
		GameRegistry.registerItem(hmg_handing, "hmg_handing");
		hmg_handing2	= new ItemHangingEntityHMG(1).setUnlocalizedName("hmg_handing2")
				.setTextureName("handmadeguns:GunRack2")
				.setCreativeTab(tabhmg);
		GameRegistry.registerItem(hmg_handing2, "hmg_handing2");


		HMGPacketHandler.init();
		HGBaseItems.init();
		HGMetalItems.init(HGBaseItems.tabHMGCrafting);
		HGGunItems.init();
		HGMetalBlocks.init();

	    /*
		 * int power
		 * float speed
		 * float bure
		 * double recoil
		 * int proceedreload
		 * float bayonet
		 * float zoom
		 */
	    /*if(pEvent.getSide().isClient()){
	    	//File optionsDir = new File(Minecraft.getMinecraft().mcDataDir,"config" + File.separatorChar + "handmadeguns");
	    	File optionsDir = new File(Minecraft.getMinecraft().mcDataDir,"mods" + File.separatorChar + "handmadeguns");
	    if (!optionsDir.exists()) {
	        optionsDir.mkdirs();
	      }
	    }*/
		// ResourceLocation aa = new ResourceLocation("handmadeguns").getResourceDomain();
		FMLCommonHandler.instance().bus().register(this);
		HMG_proxy.setuprender();
		File packdir_normal = new File(HMG_proxy.ProxyFile(), "handmadeguns_Packs");
		packdir_normal.mkdirs();
		readPackResource(packdir_normal,pEvent.getSide().isClient());

		String filepath = "mods/handmadeguns/addgun";
		File packdir_old = new File(HMG_proxy.ProxyFile(), filepath);
		readPackResource(packdir_old,pEvent.getSide().isClient());

		readPack(packdir_normal,pEvent.getSide().isClient());
		readPack(packdir_old,pEvent.getSide().isClient());

		//TODO:INJECT_FUNCTION
		File[] packlist = packdir_normal.listFiles();
		Arrays.sort(packlist, new Comparator<File>() {
			public int compare(File file1, File file2){
				return file1.getName().compareTo(file2.getName());
			}
		});
		for (File aPacklist : packlist) {
			if (aPacklist.isDirectory()) {
				File[] addscripts = getFileList(aPacklist, "addscripts");
				if (addscripts != null && addscripts.length > 0) {
					for (File aScript : addscripts) {
						System.out.println("debug" + aScript);
						try {
							ScriptEngine script = (new ScriptEngineManager(null)).getEngineByName("js");
							try {
								if (script.toString().contains("Nashorn")) {
									script.eval("load(\"nashorn:mozilla_compat.js\");");
								}
								script.eval(new FileReader(aScript));
								try {
									((Invocable) script).invokeFunction("preInit", pEvent);
								} catch (ScriptException e) {
									e.printStackTrace();
								} catch (NoSuchMethodException e) {
									e.printStackTrace();
								}
								scripts.add((Invocable) script);
							} catch (ScriptException e) {
								throw new RuntimeException("Script exec error", e);
							}
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}
					}
				}
				File[] addscripts_2 = getFileList(aPacklist, "scripts");
				if (addscripts_2 != null && addscripts_2.length > 0) {
					for (File aScript : addscripts_2) {
						System.out.println("debug" + aScript);
						try {
							ScriptEngine script = (new ScriptEngineManager(null)).getEngineByName("js");
							try {
								if (script.toString().contains("Nashorn")) {
									script.eval("load(\"nashorn:mozilla_compat.js\");");
								}
								script.eval(new FileReader(aScript));
								try {
									((Invocable) script).invokeFunction("preInit", pEvent);
								} catch (ScriptException e) {
									e.printStackTrace();
								} catch (NoSuchMethodException e) {
									e.printStackTrace();
								}
								scripts.add((Invocable) script);
							} catch (ScriptException e) {
								throw new RuntimeException("Script exec error", e);
							}
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}
					}
				}
				//fileSetup(filelist1[pack], "addbattlepack", "battlepacks");
			}
		}
		//END

		//hopefully will fix model loading issues on first start (it randomly doesnt load models on first start sometimes)
		HMG_proxy.setUpModels();



	}
	public void readPackResource(File packdir,boolean isClient){
		File[] packlist = packdir.listFiles();
		if(packlist == null)return;
		Arrays.sort(packlist, new Comparator<File>() {
			public int compare(File file1, File file2){
				return file1.getName().compareTo(file2.getName());
			}
		});
		for (File apack : packlist) {
			if (apack.isDirectory()) {
				String assetsdirstring = apack.getName() + File.separatorChar + "assets" + File.separatorChar + "handmadeguns" + File.separatorChar;
				File diremodel = new File(apack, "addmodel");
				File[] filemodel = diremodel.listFiles();
				if(filemodel != null) {
					for (int ii = 0; ii < filemodel.length; ii++) {
						if (filemodel[ii].isFile()) {
							File directory111 = new File(packdir, assetsdirstring +
									"textures" + File.separatorChar + "model" + File.separatorChar + filemodel[ii].getName());
							try {
								FileUtils.copyFile(filemodel[ii], directory111);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				}
				File diretexture = new File(apack, "addtexture");
				File[] filetexture = diretexture.listFiles();
				if(filetexture != null) {
					for (int ii = 0; ii < filetexture.length; ii++) {
						if (filetexture[ii].isFile()) {
							File directory111 = new File(packdir, assetsdirstring +
									"textures" + File.separatorChar + "items" + File.separatorChar + filetexture[ii].getName());
							try {
								FileUtils.copyFile(filetexture[ii], directory111);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				}
				File diresighttexture = new File(apack, "addsighttex");
				File[] filesighttexture = diresighttexture.listFiles();
				if (filesighttexture != null) {
					for (int ii = 0; ii < filesighttexture.length; ii++) {
						if (filesighttexture[ii].isFile()) {
							File directory111 = new File(packdir,assetsdirstring +
									"textures" + File.separatorChar + "misc" + File.separatorChar + filesighttexture[ii].getName());
							try {
								FileUtils.copyFile(filesighttexture[ii], directory111);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				}
				File diresound = new File(apack, "addsounds");
				File[] filesound = diresound.listFiles();
				if (filesound != null) {
					for (int ii = 0; ii < filesound.length; ii++) {
						if (filesound[ii].isFile()) {
							File directory111 = new File(packdir,assetsdirstring +
									"sounds" + File.separatorChar + filesound[ii].getName());
							try {
								FileUtils.copyFile(filesound[ii], directory111);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}/**/
				}
				HMGAddSounds.load(new File(packdir,assetsdirstring +
						"sounds"),new File(packdir,assetsdirstring));
			}
		}

		for (File file : packdir.listFiles())
		{
			if (file.isDirectory())
			{
				try
				{
					if(isClient) {
						HashMap<String, Object> map = new HashMap<String, Object>();
						map.put("modid", "HandmadeGuns");
						map.put("name", "HandmadeGuns");
						map.put("version", "1");
						FMLModContainer container = new FMLModContainer("handmadeguns.HandmadeGunsCore", new ModCandidate(file, file, file.isDirectory() ? ContainerType.DIR : ContainerType.JAR), map);
						container.bindMetadata(MetadataCollection.from(null, ""));
						FMLClientHandler.instance().addModAsResource(container);
					}
				} catch (Exception e)
				{
					System.out.println("Failed to load resource " + file.getName());
					e.printStackTrace();
				}
				// Add the directory to the content pack list
				System.out.println("Loaded content pack resource : " + file.getName());
			}
		}
		if(isClient){
			Minecraft.getMinecraft().refreshResources();
		}
	}

	public void readPack(File packdir,boolean isClient){
		File[] packlist = packdir.listFiles();
		if(packlist == null)return;
		Arrays.sort(packlist, new Comparator<File>() {
			public int compare(File file1, File file2){
				return file1.getName().compareTo(file2.getName());
			}
		});

		{

			Arrays.sort(packlist);
			for (File apack : packlist) {
				if (apack.isDirectory()) {
					File direTab = new File(apack, "addTab");
					File[] filetab = direTab.listFiles();
					if (filetab != null) {
						Arrays.sort(filetab, new Comparator<File>() {
							public int compare(File file1, File file2) {
								return file1.getName().compareTo(file2.getName());
							}
						});
						for (int ii = 0; ii < filetab.length; ii++) {
							if (filetab[ii].isFile()) {
								HMGAddTabs.load(isClient, filetab[ii]);
							}
						}
					}
					File direattach = new File(apack, "attachment");
					File[] fileattach = direattach.listFiles();
					if (fileattach != null) {
						Arrays.sort(fileattach, new Comparator<File>() {
							public int compare(File file1, File file2) {
								return file1.getName().compareTo(file2.getName());
							}
						});
						for (int ii = 0; ii < fileattach.length; ii++) {
							if (fileattach[ii].isFile()) {
								HMGAddAttachment.load(isClient, fileattach[ii]);
							}
						}
					}
					File diremag = new File(apack, "magazines");
					File[] filelistmag = diremag.listFiles();
					if (filelistmag != null) {
						Arrays.sort(filelistmag, new Comparator<File>() {
							public int compare(File file1, File file2) {
								return file1.getName().compareTo(file2.getName());
							}
						});
						for (int ii = 0; ii < filelistmag.length; ii++) {
							if (filelistmag[ii].isFile()) {
								try {
									HMGAddmagazine.load(isClient, filelistmag[ii]);
								} catch (ModelFormatException e) {
									e.printStackTrace();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}
					}

					File direbullet = new File(apack, "bullets");
					File[] filebullet = direbullet.listFiles();
					if (filebullet != null) {
						for (int ii = 0; ii < filebullet.length; ii++) {
							if (filebullet[ii].isFile()) {
								try {
									HMGAddBullets.load(isClient, filebullet[ii]);
								} catch (ModelFormatException e) {
									e.printStackTrace();
								}
							}
						}
					}
					File direjs = new File(apack, "addscripts");
					File[] filejs = direjs.listFiles();
					if (filejs != null) {
						for (int ii = 0; ii < filejs.length; ii++) {
							if (filejs[ii].isFile()) {
								File directory111 = new File(HMG_proxy.ProxyFile(), "mods" + File.separatorChar + "handmadeguns"
										+ File.separatorChar + "assets" + File.separatorChar + "handmadeguns" + File.separatorChar +
										"scripts" + File.separatorChar + filejs[ii].getName());
//							File in = new File("C:\\temp\\in.txt");
//							File out = new File("C:\\temp\\out.txt");
								try {
									FileUtils.copyFile(filejs[ii], directory111);
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}
					}
					File packAdditionalSettings = new File(apack, "additionalSettings.txt");
					HMGGunMaker.damageCof = 1;
					HMGGunMaker.speedCof = 1;
					if (packAdditionalSettings.isFile() && checkBeforeReadfile(packAdditionalSettings)) {
						try {
							BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(packAdditionalSettings), "Shift-JIS"));

							String str;
							while ((str = br.readLine()) != null) { // 1行ずつ読み込む
								String[] key = str.split(",");
								switch (key[0]) {
									case "damageCof":
										HMGGunMaker.damageCof = Float.parseFloat(key[1]);
									case "speedCof":
										HMGGunMaker.speedCof = Float.parseFloat(key[1]);

								}
							}
							br.close(); // ファイルを閉じる
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					File diregun = new File(apack, "guns");
					File[] filegun = diregun.listFiles();
					Arrays.sort(filegun, new Comparator<File>() {
						public int compare(File file1, File file2) {
							return file1.getName().compareTo(file2.getName());
						}
					});
					for (int ii = 0; ii < filegun.length; ii++) {
						if (filegun[ii].isFile()) {
							try {
								new HMGGunMaker().load(isClient, filegun[ii]);
							} catch (ModelFormatException e) {
								e.printStackTrace();
							}
						}
					}
				}

			}
		}
	}

	public static void copyFile(File in, File out) throws IOException {
		@SuppressWarnings("resource")
		FileChannel inChannel = new FileInputStream(in).getChannel();
		@SuppressWarnings("resource")
		FileChannel outChannel = new FileOutputStream(out).getChannel();
		try {
			inChannel.transferTo(0, inChannel.size(),outChannel);
		}
		catch (IOException e) {
			throw e;
		}
		finally {
			if (inChannel != null) inChannel.close();
			if (outChannel != null) outChannel.close();
		}
	}


	@EventHandler
	public void init(FMLInitializationEvent pEvent) {

		HGGunRecipes.init();
		GameRegistry.registerWorldGenerator(new HGWorldGen(), 0);

		int D = Short.MAX_VALUE;

		blockGunTable = new GunSmithTable();
		blockGunTable.setBlockName("gun_table");
		blockGunTable.setBlockTextureName("handmadeguns:gun_table");

		GameRegistry.registerBlock(blockGunTable, "gun_table");
		/*
		GameRegistry.addRecipe(new ItemStack(hmg_bullet_hg, 2),
				"i ",
				" g",
				'i', Items.iron_ingot,
				'g', Items.gunpowder
			);
		GameRegistry.addRecipe(new ItemStack(hmg_bullet, 2),
				"ii",
				"ig",
				'i', Items.iron_ingot,
				'g', Items.gunpowder
			);
		GameRegistry.addRecipe(new ItemStack(hmg_bullet_shell, 2),
				"ip",
				"pg",
				'i', Items.iron_ingot,
				'p', Items.paper,
				'g', Items.gunpowder
			);
		GameRegistry.addRecipe(new ItemStack(hmg_bullet_lmg, 2),
				"iig",
				"iig",
				'i', Items.iron_ingot,
				'g', Items.gunpowder
			);
		GameRegistry.addRecipe(new ItemStack(hmg_bullet_rr, 2),
				"gig",
				"gig",
				'i', Items.iron_ingot,
				'g', Items.gunpowder
			);
		*/

		GameRegistry.addRecipe(new ItemStack(blockGunTable, 1),
				"gig",
				"gxg",
				'i', Blocks.iron_block,
				'x', Blocks.crafting_table,
				'g', Items.iron_ingot
		);

		//GameRegistry.addRecipe(new ItemStack(hmg_handing, 1),
		//		"  s",
		//		" ss",
		//		"bbb",
		//		's', Items.stick,
		//		'b', new ItemStack(Blocks.wooden_slab, 1, D)
		//);
		//GameRegistry.addRecipe(new ItemStack(hmg_handing2, 1),
		//		" b",
		//		"sb",
		//		" b",
		//		's', Items.stick,
		//		'b', new ItemStack(Blocks.wooden_slab, 1, D)
		//);
		//buggy things

		WhizEventHandler whizHandler = new WhizEventHandler();
		FMLCommonHandler.instance().bus().register(whizHandler);
		MinecraftForge.EVENT_BUS.register(whizHandler);


//		EntityRegistry.registerModEntity(EntityItemFrameHMG.class, "ItemFrameHMG", 200, this, 128, 5, true);
		EntityRegistry.registerModEntity(HMGEntityFallingBlockModified.class, "HMGEntityFallingBlockModified", 200, this, 128, 5, true);
		EntityRegistry.registerModEntity(HMGEntityItemMount.class, "HMGEntityItemMount", 201, this, 128, 5, true);
		EntityRegistry.registerModEntity(HMGEntityItemMount2.class, "HMGEntityItemMount2", 202, this, 128, 5, true);

		//EntityRegistry.instance()ry.registerModEntity(HGEntityBullet.class, "BulletHG", 150, this, 128, 5, true);
		EntityRegistry.registerModEntity(HMGEntityBullet.class, "Bullet_HMG", 260, this, 65536, 5, false);
		EntityRegistry.registerModEntity(HMGEntityBulletRocket.class, "BulletRPG_HMG", 261, this, 65536, 5, false);
		EntityRegistry.registerModEntity(HMGEntityBulletExprode.class, "BulletGrenade_HMG", 262, this, 65536, 5, false);
		EntityRegistry.registerModEntity(HMGEntityBulletTorp.class, "BulletTorp_HMG", 262, this, 65536, 5, false);
		EntityRegistry.registerModEntity(HMGEntityLight.class, "Right_HMG", 263, this, 128, 5, true);
		EntityRegistry.registerModEntity(HMGEntityLight2.class, "Right2_HMG", 264, this, 128, 5, false);
		EntityRegistry.registerModEntity(HMGEntityLaser.class, "Laser_HMG", 265, this, 128, 5, false);

		EntityRegistry.registerModEntity(HMGEntityBullet_AP.class, "Bullet_AP_HMG", 270, this, 4096, 5, false);
		EntityRegistry.registerModEntity(HMGEntityBullet_Frag.class, "Bullet_Frag_HMG", 271, this, 4096, 5, false);
		EntityRegistry.registerModEntity(HMGEntityBullet_TE.class, "Bullet_TE_HMG", 272, this, 4096, 5, false);
		EntityRegistry.registerModEntity(HMGEntityBullet_AT.class, "Bullet_AT_HMG", 273, this, 4096, 5, false);
		EntityRegistry.registerModEntity(HMGEntityBullet_HE.class, "Bullet_HE_HMG", 274, this, 4096, 5, false);
		EntityRegistry.registerModEntity(HMGEntityBullet_Flame.class, "Bullet_Flame_HMG", 275, this, 4096, 5, false);

		EntityRegistry.registerModEntity(HMGEntityBulletCartridge.class, "BulletCartridge_HMG", 255, this, 128, 5, true);
		EntityRegistry.registerModEntity(PlacedGunEntity.class, "PlacedGun", 253, this, 65536, 1, true);

		NetworkRegistry.INSTANCE.registerGuiHandler(HandmadeGunsCore.INSTANCE, new HMGGuiHandler());


		GunSmithNetwork.init();

		//blockGunTable = new GunSmithTable()
		//		.setBlockName("gun_table")
		//		.setBlockTextureName("handmadeguns:gun_table");

		//GameRegistry.registerBlock(blockGunTable, "gun_table");


		Block mounter = new HMGBlockMounter(1).setBlockName("ItemHolder").setBlockTextureName("handmadeguns:camp");
		GameRegistry.registerBlock(mounter, "ItemHolder");

		if(pEvent.getSide().isClient())MinecraftForge.EVENT_BUS.register(new HMGEventZoom());

		HMGLivingUpdateEvent hmgLivingUpdateEvent = new HMGLivingUpdateEvent();
		FMLCommonHandler.instance().bus().register(hmgLivingUpdateEvent);
		MinecraftForge.EVENT_BUS.register(hmgLivingUpdateEvent);

		RenderTickSmoothing renderTickSmoothing = new RenderTickSmoothing();
		FMLCommonHandler.instance().bus().register(renderTickSmoothing);
		MinecraftForge.EVENT_BUS.register(renderTickSmoothing);

		//I don't know if this is needed. I don't know how this fucking mod works. I don't know why this isn't working. OH MY GOD JUST FUCKING WORK
		//NetworkRegistry.INSTANCE.registerGuiHandler(this, new HMGGuiHandler());

		FMLCommonHandler.instance().bus().register(this);
		//if(pEvent.getSide().isClient())
		{
			FMLCommonHandler.instance().bus().register(new HMGJumpHandler());

			FMLCommonHandler.instance().bus().register(new GunPickupHandler());
			MinecraftForge.EVENT_BUS.register(new GunPickupHandler());

			MinecraftForge.EVENT_BUS.register(new WhizEventHandler());
			MinecraftForge.EVENT_BUS.register(new LivingEventHooks());
		}

		for(int count = 0 ; count < scripts.size() ; count++){
			System.out.println("Output : "+(Invocable)scripts.get(count));
			try{
				((Invocable)scripts.get(count)).invokeFunction("init", pEvent);
			}catch(ScriptException e){
				e.printStackTrace();
			}catch(NoSuchMethodException e){
				e.printStackTrace();
			}
		}
		//TODO:END_INJECT_FUNCTION--------------------------------------------------------------------------------------------------------------------------------


		HMG_proxy.registerSomething();
		HMG_proxy.registerTileEntity();
		HMG_proxy.InitRendering();
		HMG_proxy.getEntityPlayerInstance();
	}

	//public static void registerBlocks() {

	//}


	static Field keyBind_pressed;
	static Field keyBind_pressTime;
	static Field keyBind_hash;
	@SubscribeEvent
	public void KeyHandlingEvent(KeyInputEvent event)
	{
		checkConflict(Keyboard.getEventKey());
	}
	@SubscribeEvent
	public void MouseHandlingEvent(InputEvent.MouseInputEvent event)
	{
		checkConflict(Mouse.getEventButton() - 100);
	}
	public static ArrayList<KeyBinding> TrackedKeyBinding_Vanilla;

	public void checkConflict(int checkKey){

		if(TrackedKeyBinding_Vanilla == null){
			TrackedKeyBinding_Vanilla = new ArrayList<KeyBinding>();//
			TrackedKeyBinding_Vanilla.add(HMG_proxy.getMCInstance().gameSettings.keyBindAttack);
			TrackedKeyBinding_Vanilla.add(HMG_proxy.getMCInstance().gameSettings.keyBindUseItem);
			TrackedKeyBinding_Vanilla.add(HMG_proxy.getMCInstance().gameSettings.keyBindForward);
			TrackedKeyBinding_Vanilla.add(HMG_proxy.getMCInstance().gameSettings.keyBindLeft);
			TrackedKeyBinding_Vanilla.add(HMG_proxy.getMCInstance().gameSettings.keyBindBack);
			TrackedKeyBinding_Vanilla.add(HMG_proxy.getMCInstance().gameSettings.keyBindRight);
			TrackedKeyBinding_Vanilla.add(HMG_proxy.getMCInstance().gameSettings.keyBindJump);
			TrackedKeyBinding_Vanilla.add(HMG_proxy.getMCInstance().gameSettings.keyBindSneak);
			TrackedKeyBinding_Vanilla.add(HMG_proxy.getMCInstance().gameSettings.keyBindDrop);
			TrackedKeyBinding_Vanilla.add(HMG_proxy.getMCInstance().gameSettings.keyBindInventory);
			TrackedKeyBinding_Vanilla.add(HMG_proxy.getMCInstance().gameSettings.keyBindChat);
			TrackedKeyBinding_Vanilla.add(HMG_proxy.getMCInstance().gameSettings.keyBindPlayerList);
			TrackedKeyBinding_Vanilla.add(HMG_proxy.getMCInstance().gameSettings.keyBindPickBlock);
			TrackedKeyBinding_Vanilla.add(HMG_proxy.getMCInstance().gameSettings.keyBindCommand);
			TrackedKeyBinding_Vanilla.add(HMG_proxy.getMCInstance().gameSettings.keyBindScreenshot);
			TrackedKeyBinding_Vanilla.add(HMG_proxy.getMCInstance().gameSettings.keyBindTogglePerspective);
			TrackedKeyBinding_Vanilla.add(HMG_proxy.getMCInstance().gameSettings.keyBindSmoothCamera);
			TrackedKeyBinding_Vanilla.add(HMG_proxy.getMCInstance().gameSettings.keyBindSprint);
			TrackedKeyBinding_Vanilla.add(HMG_proxy.getMCInstance().gameSettings.field_152396_an);
			TrackedKeyBinding_Vanilla.add(HMG_proxy.getMCInstance().gameSettings.field_152397_ao);
			TrackedKeyBinding_Vanilla.add(HMG_proxy.getMCInstance().gameSettings.field_152398_ap);
			TrackedKeyBinding_Vanilla.add(HMG_proxy.getMCInstance().gameSettings.field_152399_aq);
			TrackedKeyBinding_Vanilla.add(HMG_proxy.getMCInstance().gameSettings.field_152395_am);
			TrackedKeyBinding_Vanilla.add(HMG_proxy.getMCInstance().gameSettings.keyBindsHotbar[0]);
			TrackedKeyBinding_Vanilla.add(HMG_proxy.getMCInstance().gameSettings.keyBindsHotbar[1]);
			TrackedKeyBinding_Vanilla.add(HMG_proxy.getMCInstance().gameSettings.keyBindsHotbar[2]);
			TrackedKeyBinding_Vanilla.add(HMG_proxy.getMCInstance().gameSettings.keyBindsHotbar[3]);
			TrackedKeyBinding_Vanilla.add(HMG_proxy.getMCInstance().gameSettings.keyBindsHotbar[4]);
			TrackedKeyBinding_Vanilla.add(HMG_proxy.getMCInstance().gameSettings.keyBindsHotbar[5]);
			TrackedKeyBinding_Vanilla.add(HMG_proxy.getMCInstance().gameSettings.keyBindsHotbar[6]);
			TrackedKeyBinding_Vanilla.add(HMG_proxy.getMCInstance().gameSettings.keyBindsHotbar[7]);
			TrackedKeyBinding_Vanilla.add(HMG_proxy.getMCInstance().gameSettings.keyBindsHotbar[8]);
		}


		{
			KeyBinding[] keys = null;
			if(cfg_Avoid_ALL_ConflictKeys){
				keys = Minecraft.getMinecraft().gameSettings.keyBindings;
			}else {
				keys = new KeyBinding[KeyBinding_mod.TrackedKeyBinding.size() + TrackedKeyBinding_Vanilla.size()];
				int cnt = 0;
				for(KeyBinding keyBinding : TrackedKeyBinding_Vanilla){
					keys[cnt] = keyBinding;
					cnt++;
				}
				for(KeyBinding_mod keyBinding : KeyBinding_mod.TrackedKeyBinding){
					keys[cnt] = keyBinding.keyBinding;
					cnt++;
				}
			}
			if(keyBind_pressed == null){
				keyBind_pressed = ReflectionHelper.findField(KeyBinding.class, "field_74513_e","pressed");
			}
			if(keyBind_pressTime == null){
				keyBind_pressTime = ReflectionHelper.findField(KeyBinding.class, "field_151474_i","pressTime");
			}
			if(keyBind_hash == null){
				keyBind_hash = ReflectionHelper.findField(KeyBinding.class, "field_74514_b","hash");
			}
			try {
				IntHashMap hash = (IntHashMap) keyBind_hash.get(null);
				KeyBinding conflictCheckKey = (KeyBinding) hash.lookup(checkKey);
				if(conflictCheckKey != null) {
					for (Object o : keys) {
						KeyBinding keyBinding = (KeyBinding) o;
						if (keyBinding != conflictCheckKey && keyBinding.getKeyCode() == conflictCheckKey.getKeyCode()) {
							keyBind_pressed.setBoolean(keyBinding, conflictCheckKey.getIsKeyPressed());
							if(conflictCheckKey.getIsKeyPressed())keyBind_pressTime.setInt(keyBinding, keyBind_pressTime.getInt(keyBinding)+1);
//							System.out.println("engaged_Conflict\n" + keyBinding.getKeyDescription() +"\n" + conflictCheckKey.getKeyDescription());
						}
					}
				}
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	public static boolean Key_ADS(Entity entity){
		if(entity == null)return false;
		if(entity.ridingEntity instanceof EntityDummy_rider)return true;
		if(islmmloaded && entity instanceof LMM_EntityLittleMaid){
			return true;
		}else
		if(entity instanceof EntityPlayer){
			if(((EntityPlayer) entity).getHeldItem() != null
					&& ((EntityPlayer) entity).getHeldItem().getItem() instanceof HMGItem_Unified_Guns
					&& ((HMGItem_Unified_Guns) ((EntityPlayer) entity).getHeldItem().getItem()).gunInfo.needcock
					&& ((EntityPlayer) entity).getHeldItem().getTagCompound() != null
					&& !((EntityPlayer) entity).getHeldItem().getTagCompound().getBoolean("Cocking")){
				return false;
			}
			boolean flag;
			if(cfg_ADS_Sneaking == 1){
				flag = HMG_proxy.ADSClick();
			}else if(cfg_ADS_Sneaking == 2) {
				flag = entity.isSneaking();
			}else{
				ItemStack held = ((EntityPlayer) entity).getHeldItem();

				// Firearm-only condition
				boolean holdingGun = held != null && held.getItem() instanceof HMGItem_Unified_Guns;

				if (holdingGun) {
					flag = HMG_proxy.ADSClick() || entity.isSneaking();
				} else {
					// Not holding a firearm → no ADS
					flag = false;
				}
			}
//			System.out.println("debug" + HMG_proxy.ADSclick());

//			try {
//				throw new StackTracer("debug");
//			}catch (Exception e){
//				e.printStackTrace();
//			}

			return flag;
		}else if(entity instanceof PlacedGunEntity){
			return true;
		}else{
			return entity.isSneaking();
		}
	}







	public class LivingEventHooks
	{
		public LivingEventHooks() {}

		@SideOnly(Side.CLIENT)
		@SubscribeEvent
		public void renderLiving(RenderPlayerEvent.Pre event)
		{
			ItemStack itemstack = event.entityPlayer.getCurrentEquippedItem();
			RenderPlayer renderplayer = event.renderer;
			if(itemstack != null && (itemstack.getItem() instanceof HMGItem_Unified_Guns) && itemstack.hasTagCompound()){
				if(itemstack.getTagCompound().getBoolean("set_up")) {
					renderplayer.modelArmor.aimedBow = renderplayer.modelArmorChestplate.aimedBow = renderplayer.modelBipedMain.aimedBow = true;
				}else {
					renderplayer.modelBipedMain.heldItemRight = renderplayer.modelArmor.heldItemRight = 4;
				}
			}
			if(event.entityPlayer.ridingEntity instanceof PlacedGunEntity){
				renderplayer.modelArmor.aimedBow = renderplayer.modelArmorChestplate.aimedBow = renderplayer.modelBipedMain.aimedBow = true;
			}
		}
		int knife = 0;
		@SubscribeEvent
		public void entitylving(TickEvent e){
			EntityPlayer entityplayer = HMG_proxy.getEntityPlayerInstance();

			knife = 0;
		}

	}
	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		HGMetalRecipes.init();
		HGWorldGenConfig.init();
//		System.out.println("debug");
		islmmloaded = Loader.isModLoaded("lmmx");
		isgvcloaded = Loader.isModLoaded("GVCMob");

		//TODO:INJCT
		//AddScript
		for(int count = 0 ; count < scripts.size() ; count++){
			System.out.println("Output : "+(Invocable)scripts.get(count));
			try{
				((Invocable)scripts.get(count)).invokeFunction("postInit", event);
			}catch(ScriptException e){
				e.printStackTrace();
			}catch(NoSuchMethodException e){
				e.printStackTrace();
			}
		}
		//END


		//TODO:INJECT_FUNCTION
		//AddRecipe
		readPackRecipe(new File(HMG_proxy.ProxyFile(), "handmadeguns_Packs"));
		String filepath = "mods/handmadeguns/addgun";
		readPackRecipe(new File(HMG_proxy.ProxyFile(), filepath));
		HMG_proxy.setUpModels();
	}

	//Recipe loading
	public void readPackRecipe(File packdir){

		File[] packlist = packdir.listFiles();
		if(packlist == null) return;

		Arrays.sort(packlist, new Comparator<File>() {
			public int compare(File file1, File file2){
				return file1.getName().compareTo(file2.getName());
			}
		});

		for (File aPacklist : packlist) {
			if (aPacklist.isDirectory()) {

				File[] recipelist = getFileList(aPacklist, "addpackrecipe");

				if(recipelist != null && recipelist.length > 0){

					Arrays.sort(recipelist, new Comparator<File>(){
						public int compare(File file1, File file2){
							return file1.getName().compareTo(file2.getName());
						}
					});

					for(int count = 0; count < recipelist.length; count++){

						File recipeFile = recipelist[count];

						try {
							HMGGunMaker.addRecipe(recipeFile); // original crafting system
							GunSmithRecipeRegistry.registerFromFile(recipeFile); // GUI system
							System.out.println("[HMG] Loaded recipe: " + recipeFile.getAbsolutePath());

						} catch (Exception e) {
							System.out.println("[HMG] ERROR: Failed to load recipe: " + recipeFile.getAbsolutePath());
							e.printStackTrace(); // full crash reason in console
						}
					}
				}
			}
		}
	}


	@EventHandler
	public void serverStarting(FMLServerStartingEvent event){
		HMG_CommandReloadparm hmg_commandReloadparm = new HMG_CommandReloadparm();
		event.registerServerCommand(hmg_commandReloadparm);
		net.minecraftforge.client.ClientCommandHandler.instance.registerCommand(hmg_commandReloadparm);
	}

	//TODO:INJ
	public static String pathConverter(String path){
		return path.replaceAll("( // | \\ )", String.valueOf(File.separatorChar));
	}

	public static void fileSetup(File file, String path0, String path1){
		File folder0 = new File(file, path0);
		File[] file0 = folder0.listFiles();
		if(file0 != null){
			for (int var1 = 0 ; var1 < file0.length ; var1++){
				if (file0[var1].isFile()){
					File copypath = new File(HMG_proxy.ProxyFile(), pathConverter(assetsfilepath + path1 + File.separatorChar + file0[var1].getName()));
					try {
						FileUtils.copyFile(file0[var1], copypath);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public static File[] getFileList(File file, String path0){
		File folder0 = new File(file, path0);
		File[] file0 = folder0.listFiles();
		return file0;
	}
	//END

}



