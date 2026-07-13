/*    */ package backtools.client.thread;
/*    */ 
/*    */ import backtools.common.BackTools;
/*    */ import cpw.mods.fml.relauncher.Side;
/*    */ import cpw.mods.fml.relauncher.SideOnly;
/*    */ import java.net.URL;
/*    */ import java.net.URLConnection;
/*    */ import javax.xml.parsers.DocumentBuilder;
/*    */ import javax.xml.parsers.DocumentBuilderFactory;
/*    */ import org.w3c.dom.Document;
/*    */ import org.w3c.dom.Element;
/*    */ import org.w3c.dom.Node;
/*    */ import org.w3c.dom.NodeList;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ @SideOnly(Side.CLIENT)
/*    */ public class ThreadCheckModSupport
/*    */   extends Thread
/*    */ {
/*    */   public ThreadCheckModSupport() {
/* 25 */     setName("Back Tools Resource download thread");
/* 26 */     setDaemon(true);
/*    */   }
/*    */ 
/*    */   
/*    */   public void run() {
/* 31 */     boolean newFile = false;
/*    */     
/*    */     try {
/* 34 */       URL var1 = new URL("http://new.creeperrepo.net/ichun/static/backtools.xml");
/* 35 */       DocumentBuilderFactory var2 = DocumentBuilderFactory.newInstance();
/* 36 */       DocumentBuilder var3 = var2.newDocumentBuilder();
/* 37 */       URLConnection con = var1.openConnection();
/* 38 */       con.setConnectTimeout(60000);
/* 39 */       con.setReadTimeout(60000);
/* 40 */       Document var4 = var3.parse(con.getInputStream());
/* 41 */       NodeList var5 = var4.getElementsByTagName("File");
/*    */       
/* 43 */       for (int var6 = 0; var6 < 2; var6++)
/*    */       {
/* 45 */         for (int var7 = 0; var7 < var5.getLength(); var7++)
/*    */         {
/* 47 */           Node var8 = var5.item(var7);
/*    */           
/* 49 */           if (var8.getNodeType() == 1)
/*    */           {
/* 51 */             Element var9 = (Element)var8;
/* 52 */             String path = var9.getElementsByTagName("Path").item(0).getChildNodes().item(0).getNodeValue();
/* 53 */             int index = path.indexOf("/");
/* 54 */             if (index != -1)
/*    */             {
/* 56 */               path = path.substring(0, index);
/*    */             }
/* 58 */             String name = var9.getElementsByTagName("Name").item(0).getChildNodes().item(0).getNodeValue();
/*    */             
/*    */             try {
/* 61 */               Class<?> clz = Class.forName(path);
/* 62 */               BackTools.orientationMap.put(clz, Integer.valueOf(Integer.parseInt(name)));
/*    */             }
/* 64 */             catch (Exception e) {}
/*    */           
/*    */           }
/*    */         
/*    */         }
/*    */       
/*    */       }
/*    */     
/*    */     }
/* 73 */     catch (Exception e) {
/*    */       
/* 75 */       e.printStackTrace();
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\Users\zealot\Downloads\BackTools-4.0.0.jar!\backtools\client\thread\ThreadCheckModSupport.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */