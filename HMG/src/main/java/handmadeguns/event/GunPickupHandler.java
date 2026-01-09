package handmadeguns.event;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import handmadeguns.items.guns.HMGItem_Unified_Guns;
import net.minecraftforge.common.util.FakePlayer;

import static handmadeguns.HandmadeGunsCore.MAXGUNSINV;

public class GunPickupHandler {

    //todo config option also creative players ignored
    private static final int MAX_GUNS = MAXGUNSINV;

    private static final int MAX_GRENADE = 3;
    //if item nbt data has grenade use max_grenade logic instead

    //if gunInfo.grenade we do not use the MAX_GUNS flag


    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player.worldObj.isRemote) return;


        EntityPlayer player = event.player;

        if (player instanceof FakePlayer) return;
        if (player.capabilities.isCreativeMode) return; // <-- THIS LINE

        // DEBUG — you should see this spam in the server log
        //System.out.println("[HG] PlayerTick firing for " + player.getCommandSenderName());

        int gunCount = 0;

        for (ItemStack stack : player.inventory.mainInventory) {
            if (stack != null && stack.getItem() instanceof HMGItem_Unified_Guns) {
                gunCount += stack.stackSize;
            }
        }

        if (gunCount <= MAX_GUNS) return;

        for (int i = 0; i < player.inventory.mainInventory.length && gunCount > MAX_GUNS; i++) {
            ItemStack stack = player.inventory.mainInventory[i];
            if (stack == null) continue;
            if (!(stack.getItem() instanceof HMGItem_Unified_Guns)) continue;

            while (stack.stackSize > 0 && gunCount > MAX_GUNS) {
                ItemStack drop = stack.splitStack(1);
                gunCount--;
                player.dropPlayerItemWithRandomChoice(drop, false);
            }

            if (stack.stackSize <= 0) {
                player.inventory.mainInventory[i] = null;
            }
        }

        player.inventory.markDirty();
    }
}
