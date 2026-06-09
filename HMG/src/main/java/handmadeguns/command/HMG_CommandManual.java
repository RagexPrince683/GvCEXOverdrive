package handmadeguns.command;

import handmadeguns.HandmadeGunsCore;
import handmadeguns.guide.HMGGuideIntegration;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentTranslation;

public class HMG_CommandManual extends CommandBase {
    @Override
    public String getCommandName() {
        return "hmgmanual";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/hmgmanual";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (!HandmadeGunsCore.enableHMGGuideBook) {
            sender.addChatMessage(new ChatComponentTranslation("hmg.guide.command.disabled"));
        } else if (!HMGGuideIntegration.isGuideApiLoaded()) {
            sender.addChatMessage(new ChatComponentTranslation("hmg.guide.command.missing"));
        } else if (HMGGuideIntegration.isRegistered()) {
            sender.addChatMessage(new ChatComponentTranslation("hmg.guide.command.registered"));
        } else if (HMGGuideIntegration.isFailed()) {
            sender.addChatMessage(new ChatComponentTranslation("hmg.guide.command.failed"));
        } else {
            sender.addChatMessage(new ChatComponentTranslation("hmg.guide.command.pending"));
        }
    }
}
