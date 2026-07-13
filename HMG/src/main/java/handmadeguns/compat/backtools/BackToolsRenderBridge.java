package handmadeguns.compat.backtools;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderPlayerEvent;
import org.lwjgl.opengl.GL11;

import java.lang.reflect.Field;
import java.util.Map;

@SideOnly(Side.CLIENT)
public final class BackToolsRenderBridge {
    private Field backToolsPlayerToolField;
    private Object backToolsTickHandler;
    private boolean reflectionResolved;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void renderHmgBackItem(RenderPlayerEvent.Specials.Post event) {
        if (event == null || event.entityPlayer == null || event.entityPlayer.isPlayerSleeping() || event.entityPlayer.isInvisible()) return;
        EntityPlayer player = event.entityPlayer;
        ItemStack rememberedBackStack = getRememberedBackStack(player);
        if (rememberedBackStack == null) return;

        rememberedBackStack = rememberedBackStack.copy();
        ItemStack heldStack = player.getHeldItem();
        boolean sameAsHeld = isSameDisplayedStack(rememberedBackStack, heldStack);
        BackItemRenderCompat.logCandidate(player, rememberedBackStack, heldStack, sameAsHeld);
        if (sameAsHeld) return;

        GL11.glPushMatrix();
        try {
            applyBackToolsTransform(player);
            BackItemRenderCompat.renderCustomBackItem(player, rememberedBackStack, event.partialRenderTick);
        } finally {
            GL11.glPopMatrix();
        }
    }

    private void applyBackToolsTransform(EntityPlayer player) {
        GL11.glTranslatef(0.0F, 0.35F, 0.16F);
        if (player.inventory.armorItemInSlot(2) != null) {
            GL11.glTranslatef(0.0F, player.isSneaking() ? -0.1F : 0.0F, player.isSneaking() ? 0.025F : 0.06F);
        }
        if (player.isSneaking()) {
            GL11.glRotatef(28.8F, 1.0F, 0.0F, 0.0F);
            GL11.glTranslatef(0.0F, 0.0F, 0.18F);
        }
        GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
    }

    private static boolean isSameDisplayedStack(ItemStack a, ItemStack b) {
        if (a == null || b == null) return false;
        return a.getItem() == b.getItem()
                && a.getItemDamage() == b.getItemDamage()
                && ItemStack.areItemStackTagsEqual(a, b);
    }

    private ItemStack getRememberedBackStack(EntityPlayer player) {
        Map map = getBackToolsPlayerToolMap();
        return map == null ? null : (ItemStack) map.get(player.getCommandSenderName());
    }

    private Map getBackToolsPlayerToolMap() {
        try {
            if (!reflectionResolved) {
                Class backTools = Class.forName("backtools.common.BackTools");
                Field tickHandlerField = backTools.getField("tickHandlerClient");
                backToolsTickHandler = tickHandlerField.get(null);
                if (backToolsTickHandler == null) return null;
                backToolsPlayerToolField = backToolsTickHandler.getClass().getField("playerTool");
                reflectionResolved = true;
            }
            return backToolsTickHandler == null || backToolsPlayerToolField == null ? null : (Map) backToolsPlayerToolField.get(backToolsTickHandler);
        } catch (ClassNotFoundException ignored) {
            reflectionResolved = true;
            return null;
        } catch (Throwable ignored) {
            return null;
        }
    }
}
