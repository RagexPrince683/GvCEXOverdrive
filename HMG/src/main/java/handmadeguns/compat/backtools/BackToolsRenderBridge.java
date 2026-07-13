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
import java.util.HashMap;
import java.util.Map;

@SideOnly(Side.CLIENT)
public final class BackToolsRenderBridge {
    private final Map<String, ItemStack> suppressedBackToolsItems = new HashMap<String, ItemStack>();
    private Field backToolsPlayerToolField;
    private Object backToolsTickHandler;
    private boolean reflectionResolved;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void renderHmgBackItem(RenderPlayerEvent.Specials.Post event) {
        if (event == null || event.entityPlayer == null || event.entityPlayer.isPlayerSleeping() || event.entityPlayer.isInvisible()) return;
        ItemStack stack = getBackToolsStack(event.entityPlayer);
        if (stack == null) return;
        ItemStack held = normalizeHeldStack(event.entityPlayer.getHeldItem());
        if (ItemStack.areItemStacksEqual(stack, held)) return;

        GL11.glPushMatrix();
        try {
            applyBackToolsTransform(event.entityPlayer);
            if (BackItemRenderCompat.renderCustomBackItem(event.entityPlayer, stack, event.partialRenderTick)) {
                suppressBackToolsLegacyRender(event.entityPlayer, stack);
            }
        } finally {
            GL11.glPopMatrix();
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void restoreBackToolsState(RenderPlayerEvent.Specials.Post event) {
        if (event == null || event.entityPlayer == null) return;
        String key = event.entityPlayer.getCommandSenderName();
        ItemStack stack = suppressedBackToolsItems.remove(key);
        if (stack != null) putBackToolsStack(key, stack);
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

    private ItemStack normalizeHeldStack(ItemStack heldItem) {
        if (heldItem == null) return null;
        ItemStack copy = heldItem.copy();
        copy.setItemDamage(0);
        return copy;
    }

    private ItemStack getBackToolsStack(EntityPlayer player) {
        Map map = getBackToolsPlayerToolMap();
        return map == null ? null : (ItemStack) map.get(player.getCommandSenderName());
    }

    private void suppressBackToolsLegacyRender(EntityPlayer player, ItemStack originalStack) {
        String key = player.getCommandSenderName();
        if (!suppressedBackToolsItems.containsKey(key)) suppressedBackToolsItems.put(key, originalStack);
        putBackToolsStack(key, normalizeHeldStack(player.getHeldItem()));
    }

    private void putBackToolsStack(String playerName, ItemStack stack) {
        Map map = getBackToolsPlayerToolMap();
        if (map == null) return;
        if (stack == null) map.remove(playerName); else map.put(playerName, stack);
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
