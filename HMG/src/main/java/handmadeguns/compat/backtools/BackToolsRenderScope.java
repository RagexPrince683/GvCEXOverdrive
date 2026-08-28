package handmadeguns.compat.backtools;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 * Identifies the single item-renderer call owned by the BackTools player render.
 * Item renderers are also used by GUIs (including NEI), so render type and item
 * class are not sufficient evidence that a render belongs to BackTools.
 */
@SideOnly(Side.CLIENT)
final class BackToolsRenderScope {
    private static final ThreadLocal<Context> ACTIVE = new ThreadLocal<Context>();

    private BackToolsRenderScope() {}

    static Token enter(EntityPlayer player, ItemStack stack) {
        Context previous = ACTIVE.get();
        Context context = new Context(player, stack);
        ACTIVE.set(context);
        return new Token(previous, context);
    }

    static boolean claim(EntityPlayer player, ItemStack stack) {
        Context context = ACTIVE.get();
        if (context == null || context.claimed || context.player != player || context.stack != stack) return false;
        context.claimed = true;
        return true;
    }

    static boolean isActive() {
        return ACTIVE.get() != null;
    }

    static EntityPlayer owner() {
        Context context = ACTIVE.get();
        return context == null ? null : context.player;
    }

    static ItemStack stack() {
        Context context = ACTIVE.get();
        return context == null ? null : context.stack;
    }

    private static final class Context {
        private final EntityPlayer player;
        private final ItemStack stack;
        private boolean claimed;

        private Context(EntityPlayer player, ItemStack stack) {
            this.player = player;
            this.stack = stack;
        }
    }

    static final class Token {
        private final Context previous;
        private final Context entered;
        private boolean closed;

        private Token(Context previous, Context entered) {
            this.previous = previous;
            this.entered = entered;
        }

        void close() {
            if (closed) return;
            closed = true;
            if (ACTIVE.get() == entered) {
                if (previous == null) ACTIVE.remove(); else ACTIVE.set(previous);
            }
        }
    }
}
