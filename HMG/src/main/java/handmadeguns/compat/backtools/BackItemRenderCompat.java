package handmadeguns.compat.backtools;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import handmadeguns.client.render.HMGRenderItemGun_U_NEW;
import handmadeguns.items.guns.HMGItem_Unified_Guns;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.HashMap;
import java.util.Map;

@SideOnly(Side.CLIENT)
public final class BackItemRenderCompat {
    private static final float BACK_GUN_SCALE = 0.32F;
    private static final float BACK_GUN_X = -0.10F;
    private static final float BACK_GUN_Y = -0.18F;
    private static final float BACK_GUN_Z = -0.04F;
    private static final float BACK_GUN_ROTATE_X = 0.0F;
    private static final float BACK_GUN_ROTATE_Y = 0.0F;
    private static final float BACK_GUN_ROTATE_Z = 35.0F;
    private static final long DEBUG_THROTTLE_MS = 5000L;
    private static final Map<String, String> LAST_DEBUG_STATE = new HashMap<String, String>();
    private static final Map<String, Long> LAST_DEBUG_TIME = new HashMap<String, Long>();

    private BackItemRenderCompat() {}

    public static boolean renderCustomBackItem(EntityPlayer player, ItemStack stack, float partialTicks) {
        if (player == null || stack == null || !(stack.getItem() instanceof HMGItem_Unified_Guns)) {
            logState(player, stack, false, false, null, "not_hmg", null);
            return false;
        }

        IItemRenderer renderer;
        try {
            renderer = MinecraftForgeClient.getItemRenderer(stack, IItemRenderer.ItemRenderType.EQUIPPED);
        } catch (Throwable t) {
            logState(player, stack, true, false, null, "renderer_lookup_failed", t);
            return false;
        }

        if (!(renderer instanceof HMGRenderItemGun_U_NEW)) {
            logState(player, stack, true, renderer != null, renderer, "unsupported_renderer", null);
            return false;
        }
        if (!renderer.handleRenderType(stack, IItemRenderer.ItemRenderType.ENTITY)) {
            logState(player, stack, true, true, renderer, "entity_type_rejected", null);
            return false;
        }

        ItemStack renderStack = stack.copy();
        float lastBrightnessX = OpenGlHelper.lastBrightnessX;
        float lastBrightnessY = OpenGlHelper.lastBrightnessY;
        int texture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        try {
            ((HMGItem_Unified_Guns) renderStack.getItem()).checkTags(renderStack);
            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
            GL11.glPushMatrix();
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            RenderHelper.enableStandardItemLighting();
            applyBackGunCorrection();
            renderer.renderItem(IItemRenderer.ItemRenderType.ENTITY, renderStack, player, partialTicks);
            logState(player, stack, true, true, renderer, "rendered", null);
            return true;
        } catch (Throwable t) {
            logState(player, stack, true, true, renderer, "render_failed", t);
            return false;
        } finally {
            try { RenderHelper.disableStandardItemLighting(); } catch (Throwable ignored) {}
            try { GL11.glDisable(GL12.GL_RESCALE_NORMAL); } catch (Throwable ignored) {}
            try { GL11.glPopMatrix(); } catch (Throwable ignored) {}
            try { GL11.glPopAttrib(); } catch (Throwable ignored) {}
            try { GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture); } catch (Throwable ignored) {}
            try { OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastBrightnessX, lastBrightnessY); } catch (Throwable ignored) {}
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glDepthMask(true);
        }
    }

    private static void applyBackGunCorrection() {
        GL11.glTranslatef(BACK_GUN_X, BACK_GUN_Y, BACK_GUN_Z);
        GL11.glRotatef(BACK_GUN_ROTATE_X, 1.0F, 0.0F, 0.0F);
        GL11.glRotatef(BACK_GUN_ROTATE_Y, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(BACK_GUN_ROTATE_Z, 0.0F, 0.0F, 1.0F);
        GL11.glScalef(BACK_GUN_SCALE, BACK_GUN_SCALE, BACK_GUN_SCALE);
    }

    private static void logState(EntityPlayer player, ItemStack stack, boolean hmg, boolean foundRenderer, IItemRenderer renderer, String result, Throwable throwable) {
        String playerKey = player == null ? "unknown" : player.getCommandSenderName() + "#" + player.getEntityId();
        String itemName = stack == null || stack.getItem() == null ? "none" : String.valueOf(GameRegistry.findUniqueIdentifierFor(stack.getItem()));
        String rendererName = renderer == null ? "none" : renderer.getClass().getName();
        String state = itemName + '|' + hmg + '|' + foundRenderer + '|' + rendererName + '|' + result + '|' + (throwable == null ? "" : throwable.getClass().getName());
        long now = System.currentTimeMillis();
        Long last = LAST_DEBUG_TIME.get(playerKey);
        if (state.equals(LAST_DEBUG_STATE.get(playerKey)) && last != null && now - last.longValue() < DEBUG_THROTTLE_MS) return;
        LAST_DEBUG_STATE.put(playerKey, state);
        LAST_DEBUG_TIME.put(playerKey, Long.valueOf(now));
        if (throwable == null) {
            System.out.println("HandmadeGuns-BackToolsCompat player=" + playerKey + " item=" + itemName + " hmg=" + hmg + " rendererFound=" + foundRenderer + " renderer=" + rendererName + " result=" + result);
        } else {
            System.out.println("HandmadeGuns-BackToolsCompat player=" + playerKey + " item=" + itemName + " hmg=" + hmg + " rendererFound=" + foundRenderer + " renderer=" + rendererName + " result=" + result + " error=" + throwable);
        }
    }
}
