package handmadeguns.client;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.Entity;
import net.minecraftforge.client.event.RenderLivingEvent;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Tracks whether the current living-entity render belongs to CustomNPC+ without
 * linking HMG against CustomNPC+ classes.
 */
public final class HMGCustomNpcRenderCompat {
	private static final String CUSTOM_NPC_BASE_CLASS = "noppes.npcs.entity.EntityNPCInterface";
	private static final ThreadLocal<Deque<Boolean>> RENDER_CONTEXT = new ThreadLocal<Deque<Boolean>>() {
		@Override
		protected Deque<Boolean> initialValue() {
			return new ArrayDeque<Boolean>();
		}
	};

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onRenderLivingPre(RenderLivingEvent.Pre event) {
		RENDER_CONTEXT.get().push(isCustomNpc(event.entity));
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onRenderLivingPost(RenderLivingEvent.Post event) {
		Deque<Boolean> context = RENDER_CONTEXT.get();
		if (!context.isEmpty()) {
			context.pop();
		}
		if (context.isEmpty()) {
			RENDER_CONTEXT.remove();
		}
	}

	public static boolean isRenderingCustomNpc() {
		Deque<Boolean> context = RENDER_CONTEXT.get();
		return !context.isEmpty() && context.peek();
	}

	private static boolean isCustomNpc(Entity entity) {
		for (Class<?> type = entity == null ? null : entity.getClass(); type != null; type = type.getSuperclass()) {
			if (CUSTOM_NPC_BASE_CLASS.equals(type.getName())) {
				return true;
			}
		}
		return false;
	}
}
