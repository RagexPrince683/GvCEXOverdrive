package handmadeguns.client.modelLoader.obj_modelloaderMod.obj;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import handmadeguns.HandmadeGunsCore;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GLContext;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Tracks client-side VBO mesh allocations for HMG OBJ models.
 *
 * VBOs are OpenGL objects owned by the current client context.  They must be
 * created and deleted on the render/client thread while a valid GL context is
 * current; OBJ parsing can happen on the existing background loader thread, but
 * this cache intentionally compiles lazily from render() instead of during parse.
 */
@SideOnly(Side.CLIENT)
public final class HMGVboModelCache {
    private static final Set<HMGVboMeshGroup> MESHES = Collections.synchronizedSet(new HashSet<HMGVboMeshGroup>());
    private static boolean loggedEnabled;
    private static boolean loggedUnsupported;

    private HMGVboModelCache() {
    }

    public static boolean isEnabled() {
        if (!HandmadeGunsCore.enableVBOModelRendering) {
            return false;
        }

        try {
            boolean supported = GLContext.getCapabilities() != null && GLContext.getCapabilities().OpenGL15;
            if (supported) {
                if (!loggedEnabled) {
                    System.out.println("HandmadeGuns-OBJ VBO rendering enabled");
                    loggedEnabled = true;
                }
                return true;
            }
        } catch (Throwable ignored) {
            // No current GL context yet, or LWJGL could not report capabilities.
        }

        if (!loggedUnsupported) {
            System.out.println("HandmadeGuns-OBJ VBO rendering unavailable; using legacy OBJ display-list rendering");
            loggedUnsupported = true;
        }
        return false;
    }

    static void register(HMGVboMeshGroup mesh) {
        MESHES.add(mesh);
    }

    static void unregister(HMGVboMeshGroup mesh) {
        MESHES.remove(mesh);
    }

    /**
     * Releases all tracked VBOs.  Call from resource reload/model-cache clear or
     * client shutdown while Minecraft's render context is still valid.
     */
    public static void releaseAll() {
        HMGVboMeshGroup[] meshes;
        synchronized (MESHES) {
            meshes = MESHES.toArray(new HMGVboMeshGroup[MESHES.size()]);
        }

        int released = 0;
        for (HMGVboMeshGroup mesh : meshes) {
            if (mesh.release()) {
                released++;
            }
        }

        if (released > 0) {
            System.out.println("HandmadeGuns-released " + released + " OBJ VBO buffer(s)");
        }
    }

    static void bindArrayBuffer(int bufferId) {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, bufferId);
    }

    static void deleteBuffer(int bufferId) {
        GL15.glDeleteBuffers(bufferId);
    }

    static void resetClientState() {
        bindArrayBuffer(0);
        GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
        GL11.glDisableClientState(GL11.GL_NORMAL_ARRAY);
        GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
    }
}
