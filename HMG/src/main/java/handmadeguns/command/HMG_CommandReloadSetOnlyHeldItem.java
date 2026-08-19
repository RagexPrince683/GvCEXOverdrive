package handmadeguns.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;

import static handmadeguns.HandmadeGunsCore.HMG_proxy;

public class HMG_CommandReloadSetOnlyHeldItem extends CommandBase {
    @Override
    public String getCommandName() {
        return "reloadsetonlyhelditem";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/reloadsetonlyhelditem";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] arguments) {
        if (!(sender instanceof EntityPlayer)) {
            sender.addChatMessage(new ChatComponentText("This command requires a player."));
            return;
        }
        ItemStack heldItem = ((EntityPlayer) sender).getHeldItem();
        if (heldItem == null) {
            sender.addChatMessage(new ChatComponentText("No held item to reload."));
            return;
        }
        if (HMG_proxy.reloadHeldItemModel(heldItem)) {
            sender.addChatMessage(new ChatComponentText("Reloaded the held item's model."));
        } else {
            sender.addChatMessage(new ChatComponentText("The held item has no reloadable HMG model."));
        }
    }
}
