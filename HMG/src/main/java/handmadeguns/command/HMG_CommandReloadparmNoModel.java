package handmadeguns.command;

import net.minecraft.command.ICommandSender;

/** Reloads gun pack settings while retaining all currently cached models. */
public class HMG_CommandReloadparmNoModel extends HMG_CommandReloadparm {

    @Override
    public String getCommandName() {
        return "reloadsettingsnomodel";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] arguments) {
        System.out.println("" + sender);
		reloadPackSettings();
    }
}
