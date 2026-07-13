/*    */ package backtools.client.core;
/*    */ 
/*    */ import backtools.common.BackTools;
/*    */ import cpw.mods.fml.common.eventhandler.SubscribeEvent;
/*    */ import cpw.mods.fml.common.gameevent.TickEvent;
/*    */ import cpw.mods.fml.relauncher.Side;
/*    */ import java.util.HashMap;
/*    */ import net.minecraft.block.Block;
/*    */ import net.minecraft.entity.player.EntityPlayer;
/*    */ import net.minecraft.init.Blocks;
/*    */ import net.minecraft.item.ItemStack;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class TickHandlerClient
/*    */ {
/*    */   @SubscribeEvent
/*    */   public void playerTick(TickEvent.PlayerTickEvent event) {
/* 21 */     if (event.side == Side.CLIENT && event.phase == TickEvent.Phase.END) {
/*    */       
/* 23 */       EntityPlayer player = event.player;
/*    */       
/* 25 */       ItemStack heldItem = player.func_70694_bm();
/* 26 */       if (heldItem != this.currentTool.get(player.func_70005_c_()) && ((heldItem != null && Block.func_149634_a(heldItem.func_77973_b()) == Blocks.field_150350_a) || heldItem == null)) {
/*    */         
/* 28 */         if (this.currentTool.get(player.func_70005_c_()) != null && (((ItemStack)this.currentTool.get(player.func_70005_c_())).func_77973_b().func_77662_d() || ((ItemStack)this.currentTool.get(player.func_70005_c_())).func_77973_b() instanceof net.minecraft.item.ItemBow) && !BackTools.blacklist.contains(((ItemStack)this.currentTool.get(player.func_70005_c_())).func_77973_b())) {
/*    */           
/* 30 */           ItemStack is = ((ItemStack)this.currentTool.get(player.func_70005_c_())).func_77946_l();
/* 31 */           is.func_77964_b(0);
/* 32 */           ItemStack prevTool = this.playerTool.get(player.func_70005_c_());
/* 33 */           boolean equal = ItemStack.func_77989_b(prevTool, is);
/* 34 */           if (prevTool == null || !equal)
/*    */           {
/* 36 */             this.playerTool.put(player.func_70005_c_(), is);
/*    */           }
/*    */         } 
/* 39 */         this.currentTool.put(player.func_70005_c_(), heldItem);
/*    */       } 
/*    */     } 
/*    */   }
/*    */   
/* 44 */   public HashMap<String, ItemStack> playerTool = new HashMap<String, ItemStack>();
/* 45 */   public HashMap<String, ItemStack> currentTool = new HashMap<String, ItemStack>();
/*    */ }


/* Location:              C:\Users\zealot\Downloads\BackTools-4.0.0.jar!\backtools\client\core\TickHandlerClient.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */