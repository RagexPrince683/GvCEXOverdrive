/*    */ package backtools.common.core;
/*    */ 
/*    */ import backtools.common.BackTools;
/*    */ import cpw.mods.fml.common.eventhandler.SubscribeEvent;
/*    */ import cpw.mods.fml.common.network.FMLNetworkEvent;
/*    */ import cpw.mods.fml.relauncher.Side;
/*    */ import cpw.mods.fml.relauncher.SideOnly;
/*    */ import ichun.client.render.RendererHelper;
/*    */ import morph.api.Api;
/*    */ import net.minecraft.entity.EntityLivingBase;
/*    */ import net.minecraft.item.ItemStack;
/*    */ import net.minecraftforge.client.event.RenderPlayerEvent;
/*    */ import org.lwjgl.opengl.GL11;
/*    */ 
/*    */ 
/*    */ 
/*    */ @SideOnly(Side.CLIENT)
/*    */ public class EventHandler
/*    */ {
/*    */   @SubscribeEvent
/*    */   public void onPlayerRender(RenderPlayerEvent.Specials.Post event) {
/* 22 */     if (BackTools.hasMorphMod) {
/*    */       
/* 24 */       EntityLivingBase ent = Api.getMorphEntity(event.entityPlayer.func_70005_c_(), true);
/* 25 */       if (ent != null && !(ent instanceof net.minecraft.entity.player.EntityPlayer)) {
/*    */         return;
/*    */       }
/*    */     } 
/*    */     
/* 30 */     if (!event.entityPlayer.func_82238_cc() && !event.entityPlayer.func_82150_aj()) {
/*    */       
/* 32 */       ItemStack is = (ItemStack)BackTools.tickHandlerClient.playerTool.get(event.entityPlayer.func_70005_c_());
/*    */       
/* 34 */       ItemStack heldItem = event.entityPlayer.func_70694_bm();
/* 35 */       if (heldItem != null) {
/*    */         
/* 37 */         ItemStack is1 = heldItem.func_77946_l();
/* 38 */         is1.func_77964_b(0);
/* 39 */         heldItem = is1;
/*    */       } 
/*    */       
/* 42 */       if (is != null && !ItemStack.func_77989_b(is, heldItem)) {
/*    */         
/* 44 */         GL11.glPushMatrix();
/*    */         
/* 46 */         GL11.glTranslatef(0.0F, 0.35F, 0.16F);
/*    */         
/* 48 */         if (event.entityPlayer.field_71071_by.func_70440_f(2) != null)
/*    */         {
/* 50 */           GL11.glTranslatef(0.0F, event.entityPlayer.func_70093_af() ? -0.1F : 0.0F, event.entityPlayer.func_70093_af() ? 0.025F : 0.06F);
/*    */         }
/* 52 */         if (event.entityPlayer.func_70093_af()) {
/*    */           
/* 54 */           GL11.glRotatef(28.8F, 1.0F, 0.0F, 0.0F);
/* 55 */           GL11.glTranslatef(0.0F, 0.0F, 0.18F);
/*    */         } 
/*    */         
/* 58 */         GL11.glRotatef(BackTools.getOrientation(is.func_77973_b().getClass()) * -90.0F, 0.0F, 0.0F, 1.0F);
/*    */         
/* 60 */         GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
/*    */         
/* 62 */         GL11.glEnable(3042);
/* 63 */         GL11.glBlendFunc(770, 771);
/*    */         
/* 65 */         RendererHelper.renderItemIn3d(is);
/*    */         
/* 67 */         GL11.glDisable(3042);
/*    */         
/* 69 */         GL11.glPopMatrix();
/*    */       } 
/*    */     } 
/*    */   }
/*    */ 
/*    */   
/*    */   @SubscribeEvent
/*    */   public void onClientConnect(FMLNetworkEvent.ClientConnectedToServerEvent event) {
/* 77 */     onClientConnection();
/*    */   }
/*    */ 
/*    */   
/*    */   @SubscribeEvent
/*    */   public void onClientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
/* 83 */     onClientConnection();
/*    */   }
/*    */ 
/*    */   
/*    */   public void onClientConnection() {
/* 88 */     BackTools.tickHandlerClient.currentTool.clear();
/* 89 */     BackTools.tickHandlerClient.playerTool.clear();
/*    */   }
/*    */ }


/* Location:              C:\Users\zealot\Downloads\BackTools-4.0.0.jar!\backtools\common\core\EventHandler.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */