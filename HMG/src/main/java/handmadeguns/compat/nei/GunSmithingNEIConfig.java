package handmadeguns.compat.nei;

import codechicken.nei.api.IConfigureNEI;
import codechicken.nei.recipe.GuiCraftingRecipe;
import codechicken.nei.recipe.GuiUsageRecipe;
import handmadeguns.HandmadeGunsCore;

/** Loaded by NEI's optional plugin discovery; it is never referenced by common HMG code. */
public class GunSmithingNEIConfig implements IConfigureNEI {
    public void loadConfig() {
        GunSmithingNEIHandler crafting = new GunSmithingNEIHandler();
        GunSmithingNEIHandler usage = new GunSmithingNEIHandler();
        GuiCraftingRecipe.craftinghandlers.add(crafting);
        GuiUsageRecipe.usagehandlers.add(usage);
    }

    public String getName() {
        return "Handmade Guns Gun Smithing Table";
    }

    public String getVersion() {
        return HandmadeGunsCore.class.getPackage().getImplementationVersion();
    }
}
