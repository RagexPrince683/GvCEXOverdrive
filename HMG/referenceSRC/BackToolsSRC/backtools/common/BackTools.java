/*     */ package backtools.common;
/*     */ 
/*     */ import backtools.client.core.TickHandlerClient;
/*     */ import backtools.client.thread.ThreadCheckModSupport;
/*     */ import backtools.common.core.EventHandler;
/*     */ import cpw.mods.fml.common.FMLCommonHandler;
/*     */ import cpw.mods.fml.common.Mod;
/*     */ import cpw.mods.fml.common.Mod.EventHandler;
/*     */ import cpw.mods.fml.common.Mod.Instance;
/*     */ import cpw.mods.fml.common.event.FMLInitializationEvent;
/*     */ import cpw.mods.fml.common.event.FMLInterModComms;
/*     */ import cpw.mods.fml.common.event.FMLPostInitializationEvent;
/*     */ import cpw.mods.fml.common.event.FMLPreInitializationEvent;
/*     */ import ichun.common.core.updateChecker.ModVersionChecker;
/*     */ import ichun.common.core.updateChecker.ModVersionInfo;
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import net.minecraft.item.Item;
/*     */ import net.minecraft.item.ItemBow;
/*     */ import net.minecraft.item.ItemSword;
/*     */ import net.minecraftforge.common.MinecraftForge;
/*     */ import org.apache.logging.log4j.Level;
/*     */ import org.apache.logging.log4j.LogManager;
/*     */ import org.apache.logging.log4j.Logger;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ @Mod(modid = "BackTools", name = "BackTools", version = "4.0.0", dependencies = "required-after:iChunUtil@[4.0.0,)")
/*     */ public class BackTools
/*     */ {
/*     */   public static final String version = "4.0.0";
/*  42 */   private static final Logger logger = LogManager.getLogger("BackTools");
/*     */   
/*     */   @Instance("BackTools")
/*     */   public static BackTools instance;
/*     */   
/*     */   public static TickHandlerClient tickHandlerClient;
/*     */   
/*     */   public static boolean hasMorphMod;
/*     */   
/*  51 */   public static HashMap<Class, Integer> orientationMap = new HashMap<Class<?>, Integer>();
/*     */   
/*  53 */   public static ArrayList<Item> blacklist = new ArrayList<Item>();
/*     */ 
/*     */   
/*     */   @EventHandler
/*     */   public void preLoad(FMLPreInitializationEvent event) {
/*  58 */     if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
/*     */       
/*  60 */       console("You're loading Back Tools on a server! This is a client-only mod!", true);
/*     */       
/*     */       return;
/*     */     } 
/*  64 */     EventHandler handler = new EventHandler();
/*  65 */     FMLCommonHandler.instance().bus().register(handler);
/*  66 */     MinecraftForge.EVENT_BUS.register(handler);
/*     */     
/*  68 */     ModVersionChecker.register_iChunMod(new ModVersionInfo("BackTools", "1.7.10", "4.0.0", true));
/*     */   }
/*     */ 
/*     */   
/*     */   @EventHandler
/*     */   public void load(FMLInitializationEvent event) {
/*  74 */     if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
/*     */       
/*  76 */       tickHandlerClient = new TickHandlerClient();
/*  77 */       FMLCommonHandler.instance().bus().register(tickHandlerClient);
/*     */       
/*  79 */       orientationMap.put(ItemSword.class, Integer.valueOf(2));
/*  80 */       orientationMap.put(ItemBow.class, Integer.valueOf(1));
/*     */       
/*  82 */       (new ThreadCheckModSupport()).start();
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   @EventHandler
/*     */   public void postLoad(FMLPostInitializationEvent event) {
/*  89 */     if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
/*     */       
/*     */       try {
/*     */         
/*  93 */         Class<?> clz = Class.forName("morph.common.Morph");
/*  94 */         hasMorphMod = true;
/*     */       }
/*  96 */       catch (Exception e) {}
/*     */     }
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   @EventHandler
/*     */   public void onIMCMessage(FMLInterModComms.IMCEvent event) {
/* 105 */     for (FMLInterModComms.IMCMessage message : event.getMessages()) {
/*     */       
/* 107 */       if (message.key.equalsIgnoreCase("blacklist") && message.isItemStackMessage())
/*     */       {
/* 109 */         if (!blacklist.contains(message.getItemStackValue().func_77973_b())) {
/*     */           
/* 111 */           blacklist.add(message.getItemStackValue().func_77973_b());
/* 112 */           console("Registered " + message.getItemStackValue().func_77973_b().toString() + " to Item blacklist", false);
/*     */         } 
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public static int getOrientation(Class<Item> clz) {
/*     */     try {
/* 122 */       Integer i = orientationMap.get(clz);
/* 123 */       if (i == null && clz != Item.class)
/*     */       {
/* 125 */         return getOrientation(clz.getSuperclass());
/*     */       }
/*     */ 
/*     */       
/* 129 */       return i.intValue();
/*     */     
/*     */     }
/* 132 */     catch (Exception e) {
/*     */ 
/*     */       
/* 135 */       return 0;
/*     */     } 
/*     */   }
/*     */   
/*     */   public static void console(String s, boolean warning) {
/* 140 */     StringBuilder sb = new StringBuilder();
/* 141 */     logger.log(warning ? Level.WARN : Level.INFO, sb.append("[").append("4.0.0").append("] ").append(s).toString());
/*     */   }
/*     */ }


/* Location:              C:\Users\zealot\Downloads\BackTools-4.0.0.jar!\backtools\common\BackTools.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */