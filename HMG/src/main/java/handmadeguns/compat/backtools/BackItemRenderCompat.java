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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

@SideOnly(Side.CLIENT)
public final class BackItemRenderCompat {
    private static final float BACK_GUN_SCALE = 0.32F;
    private static final float BACK_GUN_X = -0.10F;
    private static final float BACK_GUN_Y = -0.18F;
    private static final float BACK_GUN_Z = 0.03F;
    private static final float BACK_GUN_ALIGN_X = 0.0F;
    private static final float BACK_GUN_ALIGN_Y = 90.0F;
    private static final float BACK_GUN_DIRECTION_Z = -105.0F;
    private static final long DEBUG_THROTTLE_MS = 5000L;
    private static final Map<String, String> LAST_DEBUG_STATE = new HashMap<String, String>();
    private static final Map<String, Long> LAST_DEBUG_TIME = new HashMap<String, Long>();
    private static final Map<String, Integer> FRAME_RENDER_COUNTS = new HashMap<String, Integer>();
    private static final FloatBuffer COLOR_BUFFER = BufferUtils.createFloatBuffer(16);

    private BackItemRenderCompat() {}

    public static boolean renderCustomBackItem(EntityPlayer player, ItemStack rememberedBackStack, float partialTicks) {
        if (player == null || rememberedBackStack == null || !(rememberedBackStack.getItem() instanceof HMGItem_Unified_Guns)) {
            logState(player, rememberedBackStack, null, false, false, null, "ENTITY", false, "not_hmg", 0);
            return false;
        }

        ItemStack renderStack = rememberedBackStack.copy();
        IItemRenderer renderer;
        try {
            renderer = MinecraftForgeClient.getItemRenderer(renderStack, IItemRenderer.ItemRenderType.EQUIPPED);
        } catch (Throwable t) {
            logState(player, renderStack, null, true, false, null, "ENTITY", false, "renderer_lookup_failed:" + t.getClass().getName(), System.identityHashCode(renderStack));
            return false;
        }

        if (!(renderer instanceof HMGRenderItemGun_U_NEW)) {
            logState(player, renderStack, null, true, renderer != null, renderer, "ENTITY", false, "unsupported_renderer", System.identityHashCode(renderStack));
            return false;
        }
        if (!renderer.handleRenderType(renderStack, IItemRenderer.ItemRenderType.ENTITY)) {
            logState(player, renderStack, null, true, true, renderer, "ENTITY", false, "entity_type_rejected", System.identityHashCode(renderStack));
            return false;
        }

        GLStateSnapshot glState = GLStateSnapshot.capture();
        HMGStaticSnapshot hmgState = HMGStaticSnapshot.capture();
        boolean rendered = false;
        try {
            ((HMGItem_Unified_Guns) renderStack.getItem()).checkTags(renderStack);
            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
            GL11.glPushMatrix();
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDepthMask(true);
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            RenderHelper.enableStandardItemLighting();
            applyBackGunCorrection();
            synchronized (HMGRenderItemGun_U_NEW.class) {
                renderer.renderItem(IItemRenderer.ItemRenderType.ENTITY, renderStack, player, partialTicks);
            }
            rendered = true;
            int count = incrementFrameRenderCount(player, renderStack);
            logState(player, renderStack, null, true, true, renderer, "ENTITY", false, "rendered count=" + count, System.identityHashCode(renderStack));
            return true;
        } catch (Throwable t) {
            logState(player, renderStack, null, true, true, renderer, "ENTITY", false, "render_failed:" + t.getClass().getName(), System.identityHashCode(renderStack));
            return false;
        } finally {
            try { RenderHelper.disableStandardItemLighting(); } catch (Throwable ignored) {}
            try { GL11.glPopMatrix(); } catch (Throwable ignored) {}
            try { GL11.glPopAttrib(); } catch (Throwable ignored) {}
            hmgState.restore();
            glState.restore();
            if (!rendered) GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    public static void logCandidate(EntityPlayer player, ItemStack remembered, ItemStack held, boolean sameAsHeld) {
        logState(player, remembered, held, remembered != null && remembered.getItem() instanceof HMGItem_Unified_Guns,
                false, null, "ENTITY", false, "candidate sameAsHeld=" + sameAsHeld, remembered == null ? 0 : System.identityHashCode(remembered));
    }

    private static void applyBackGunCorrection() {
        GL11.glTranslatef(BACK_GUN_X, BACK_GUN_Y, BACK_GUN_Z);
        // HMG ENTITY renders use the model's long/barrel axis in the local Z-depth direction.
        // Y alignment rolls that axis into the player's back plane; Z direction then makes a barrel-down diagonal.
        GL11.glRotatef(BACK_GUN_ALIGN_X, 1.0F, 0.0F, 0.0F);
        GL11.glRotatef(BACK_GUN_ALIGN_Y, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(BACK_GUN_DIRECTION_Z, 0.0F, 0.0F, 1.0F);
        GL11.glScalef(BACK_GUN_SCALE, BACK_GUN_SCALE, BACK_GUN_SCALE);
    }

    private static int incrementFrameRenderCount(EntityPlayer player, ItemStack stack) {
        String key = (player == null ? -1 : player.getEntityId()) + ":" + stackId(stack) + ":" + (player == null ? 0 : player.ticksExisted);
        Integer count = FRAME_RENDER_COUNTS.get(key);
        int next = count == null ? 1 : count.intValue() + 1;
        FRAME_RENDER_COUNTS.put(key, Integer.valueOf(next));
        if (FRAME_RENDER_COUNTS.size() > 128) FRAME_RENDER_COUNTS.clear();
        return next;
    }

    private static void logState(EntityPlayer player, ItemStack remembered, ItemStack held, boolean hmg, boolean foundRenderer, IItemRenderer renderer, String renderType, boolean legacySuppressed, String result, int stackIdentity) {
        String playerKey = player == null ? "unknown" : player.getCommandSenderName() + "#" + player.getEntityId();
        String rendererName = renderer == null ? "none" : renderer.getClass().getName();
        String rememberedId = stackId(remembered);
        String heldId = stackId(held);
        String state = rememberedId + '|' + heldId + '|' + hmg + '|' + foundRenderer + '|' + rendererName + '|' + renderType + '|' + legacySuppressed + '|' + result + '|' + stackIdentity;
        long now = System.currentTimeMillis();
        Long last = LAST_DEBUG_TIME.get(playerKey);
        if (state.equals(LAST_DEBUG_STATE.get(playerKey)) && last != null && now - last.longValue() < DEBUG_THROTTLE_MS) return;
        LAST_DEBUG_STATE.put(playerKey, state);
        LAST_DEBUG_TIME.put(playerKey, Long.valueOf(now));
        System.out.println("HandmadeGuns-BackToolsCompat player=" + playerKey + " remembered=" + rememberedId + " held=" + heldId + " hmg=" + hmg + " rendererFound=" + foundRenderer + " renderer=" + rendererName + " renderType=" + renderType + " customRendered=" + result.startsWith("rendered") + " legacySuppressed=" + legacySuppressed + " stackIdentity=" + stackIdentity + " result=" + result);
    }

    private static String stackId(ItemStack stack) {
        if (stack == null || stack.getItem() == null) return "none";
        String name = String.valueOf(GameRegistry.findUniqueIdentifierFor(stack.getItem()));
        NBTTagCompound tag = stack.getTagCompound();
        return name + "@" + stack.getItemDamage() + "#" + (tag == null ? 0 : tag.toString().hashCode());
    }

    private static final class HMGStaticSnapshot {
        private final NBTTagCompound nbt;
        private final ItemStack[] items;
        private final HMGItem_Unified_Guns gunItem;
        private final boolean isPlacedGun;
        private final float turretYaw;
        private final float turretPitch;

        private HMGStaticSnapshot() {
            this.nbt = HMGRenderItemGun_U_NEW.nbt;
            this.items = HMGRenderItemGun_U_NEW.items == null ? null : HMGRenderItemGun_U_NEW.items.clone();
            this.gunItem = HMGRenderItemGun_U_NEW.gunitem;
            this.isPlacedGun = HMGRenderItemGun_U_NEW.isPlacedGun;
            this.turretYaw = HMGRenderItemGun_U_NEW.turretYaw;
            this.turretPitch = HMGRenderItemGun_U_NEW.turretPitch;
        }

        private static HMGStaticSnapshot capture() {
            synchronized (HMGRenderItemGun_U_NEW.class) {
                return new HMGStaticSnapshot();
            }
        }

        private void restore() {
            synchronized (HMGRenderItemGun_U_NEW.class) {
                HMGRenderItemGun_U_NEW.nbt = nbt;
                if (items != null && HMGRenderItemGun_U_NEW.items != null) {
                    System.arraycopy(items, 0, HMGRenderItemGun_U_NEW.items, 0, Math.min(items.length, HMGRenderItemGun_U_NEW.items.length));
                }
                HMGRenderItemGun_U_NEW.gunitem = gunItem;
                HMGRenderItemGun_U_NEW.isPlacedGun = isPlacedGun;
                HMGRenderItemGun_U_NEW.turretYaw = turretYaw;
                HMGRenderItemGun_U_NEW.turretPitch = turretPitch;
            }
        }
    }

    private static final class GLStateSnapshot {
        private final boolean cullEnabled;
        private final boolean blendEnabled;
        private final boolean lightingEnabled;
        private final boolean depthEnabled;
        private final boolean rescaleEnabled;
        private final boolean depthMask;
        private final int texture;
        private final int matrixMode;
        private final int blendSrc;
        private final int blendDst;
        private final float[] color = new float[4];
        private final float lightmapX;
        private final float lightmapY;

        private GLStateSnapshot() {
            this.cullEnabled = GL11.glIsEnabled(GL11.GL_CULL_FACE);
            this.blendEnabled = GL11.glIsEnabled(GL11.GL_BLEND);
            this.lightingEnabled = GL11.glIsEnabled(GL11.GL_LIGHTING);
            this.depthEnabled = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
            this.rescaleEnabled = GL11.glIsEnabled(GL12.GL_RESCALE_NORMAL);
            this.depthMask = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
            this.texture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
            this.matrixMode = GL11.glGetInteger(GL11.GL_MATRIX_MODE);
            this.blendSrc = GL11.glGetInteger(GL11.GL_BLEND_SRC);
            this.blendDst = GL11.glGetInteger(GL11.GL_BLEND_DST);
            COLOR_BUFFER.clear();
            GL11.glGetFloat(GL11.GL_CURRENT_COLOR, COLOR_BUFFER);
            this.color[0] = COLOR_BUFFER.get(0);
            this.color[1] = COLOR_BUFFER.get(1);
            this.color[2] = COLOR_BUFFER.get(2);
            this.color[3] = COLOR_BUFFER.get(3);
            this.lightmapX = OpenGlHelper.lastBrightnessX;
            this.lightmapY = OpenGlHelper.lastBrightnessY;
        }

        private static GLStateSnapshot capture() {
            return new GLStateSnapshot();
        }

        private void restore() {
            setEnabled(GL11.GL_CULL_FACE, cullEnabled);
            setEnabled(GL11.GL_BLEND, blendEnabled);
            setEnabled(GL11.GL_LIGHTING, lightingEnabled);
            setEnabled(GL11.GL_DEPTH_TEST, depthEnabled);
            setEnabled(GL12.GL_RESCALE_NORMAL, rescaleEnabled);
            GL11.glDepthMask(depthMask);
            GL11.glBlendFunc(blendSrc, blendDst);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
            GL11.glMatrixMode(matrixMode);
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lightmapX, lightmapY);
            GL11.glColor4f(color[0], color[1], color[2], color[3]);
        }

        private static void setEnabled(int cap, boolean enabled) {
            if (enabled) GL11.glEnable(cap); else GL11.glDisable(cap);
        }
    }
}
