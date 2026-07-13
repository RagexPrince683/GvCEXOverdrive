package handmadeguns.gunsmithing;

import handmadeguns.HandmadeGunsCore;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class GunTableInventoryAllocator {
    private GunTableInventoryAllocator() {}

    public static final class Reservation {
        public final int slot;
        public final int amount;
        public final GunTableIngredient ingredient;

        private Reservation(int slot, int amount, GunTableIngredient ingredient) {
            this.slot = slot;
            this.amount = amount;
            this.ingredient = ingredient;
        }
    }

    public static final class AllocationResult {
        public final boolean success;
        public final List<Reservation> reservations;
        public final GunTableIngredient failedIngredient;
        public final int missingAmount;

        private AllocationResult(boolean success, List<Reservation> reservations,
                                 GunTableIngredient failedIngredient, int missingAmount) {
            this.success = success;
            this.reservations = reservations;
            this.failedIngredient = failedIngredient;
            this.missingAmount = missingAmount;
        }
    }

    private static final class OrderedIngredient {
        final int index;
        final GunTableIngredient ingredient;

        OrderedIngredient(int index, GunTableIngredient ingredient) {
            this.index = index;
            this.ingredient = ingredient;
        }
    }

    public static AllocationResult allocate(EntityPlayer player, GunTableIngredient[] ingredients) {
        if (player == null) return new AllocationResult(false, new ArrayList<Reservation>(), null, 0);

        List<OrderedIngredient> ordered = collectOrderedIngredients(ingredients);
        int size = player.inventory.getSizeInventory();
        int[] remaining = new int[size];
        for (int i = 0; i < size; i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            remaining[i] = stack == null ? 0 : stack.stackSize;
        }

        List<Reservation> reservations = new ArrayList<Reservation>();
        for (OrderedIngredient orderedIngredient : ordered) {
            GunTableIngredient ingredient = orderedIngredient.ingredient;
            int needed = ingredient.getRequiredAmount();
            HandmadeGunsCore.Debug("[GunSmith] allocating ingredient %s amount=%d slotIndex=%d", ingredient.getDisplayName(), needed, orderedIngredient.index);

            for (int slot = 0; slot < size && needed > 0; slot++) {
                if (remaining[slot] <= 0) continue;
                ItemStack stack = player.inventory.getStackInSlot(slot);
                boolean matches = ingredient.matches(stack);
                HandmadeGunsCore.Debug("[GunSmith] candidate slot=%d stack=%s remaining=%d ingredient=%s matches=%s", slot, stackToLogString(stack), remaining[slot], ingredient.getDisplayName(), matches);
                if (!matches) continue;

                int reserved = Math.min(remaining[slot], needed);
                remaining[slot] -= reserved;
                needed -= reserved;
                reservations.add(new Reservation(slot, reserved, ingredient));
                HandmadeGunsCore.Debug("[GunSmith] reserved slot=%d amount=%d for %s", slot, reserved, ingredient.getDisplayName());
            }

            if (needed > 0) {
                HandmadeGunsCore.Debug("[GunSmith] allocation failed ingredient=%s missing=%d", ingredient.getDisplayName(), needed);
                return new AllocationResult(false, reservations, ingredient, needed);
            }
        }

        return new AllocationResult(true, reservations, null, 0);
    }

    public static int countMatches(EntityPlayer player, GunTableIngredient ingredient) {
        if (player == null || ingredient == null) return 0;
        int count = 0;
        for (int slot = 0; slot < player.inventory.getSizeInventory(); slot++) {
            ItemStack stack = player.inventory.getStackInSlot(slot);
            if (ingredient.matches(stack)) count += stack.stackSize;
        }
        return count;
    }

    public static boolean canCraft(EntityPlayer player, GunTableIngredient[] ingredients) {
        return allocate(player, ingredients).success;
    }

    public static boolean consume(EntityPlayer player, AllocationResult allocation) {
        if (player == null || allocation == null || !allocation.success) return false;

        for (Reservation reservation : allocation.reservations) {
            ItemStack stack = player.inventory.getStackInSlot(reservation.slot);
            if (stack == null || stack.stackSize < reservation.amount || !reservation.ingredient.matches(stack)) {
                HandmadeGunsCore.Debug("[GunSmith] reservation stale slot=%d amount=%d ingredient=%s", reservation.slot, reservation.amount, reservation.ingredient.getDisplayName());
                return false;
            }
        }

        for (Reservation reservation : allocation.reservations) {
            ItemStack stack = player.inventory.getStackInSlot(reservation.slot);
            stack.stackSize -= reservation.amount;
            HandmadeGunsCore.Debug("[GunSmith] consumed slot=%d amount=%d ingredient=%s", reservation.slot, reservation.amount, reservation.ingredient.getDisplayName());
            if (stack.stackSize <= 0) {
                player.inventory.setInventorySlotContents(reservation.slot, null);
            } else {
                player.inventory.setInventorySlotContents(reservation.slot, stack);
            }
        }

        player.inventory.markDirty();
        if (player.inventoryContainer != null) player.inventoryContainer.detectAndSendChanges();
        return true;
    }

    private static List<OrderedIngredient> collectOrderedIngredients(GunTableIngredient[] ingredients) {
        List<OrderedIngredient> ordered = new ArrayList<OrderedIngredient>();
        if (ingredients == null) return ordered;
        for (int i = 0; i < ingredients.length; i++) {
            if (ingredients[i] != null) ordered.add(new OrderedIngredient(i, ingredients[i]));
        }
        Collections.sort(ordered, new Comparator<OrderedIngredient>() {
            public int compare(OrderedIngredient a, OrderedIngredient b) {
                if (a.ingredient.isOreDictionary() != b.ingredient.isOreDictionary()) {
                    return a.ingredient.isOreDictionary() ? 1 : -1;
                }
                return a.index - b.index;
            }
        });
        return ordered;
    }

    private static String stackToLogString(ItemStack stack) {
        if (stack == null) return "null";
        try {
            return stack.getDisplayName() + " x" + stack.stackSize + " meta=" + stack.getItemDamage();
        } catch (Throwable ignored) {
            return stack.toString();
        }
    }
}
