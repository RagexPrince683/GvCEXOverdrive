package handmadeguns.compat.nei;

import codechicken.nei.api.IConfigureNEI;
import codechicken.nei.recipe.GuiCraftingRecipe;
import codechicken.nei.recipe.GuiUsageRecipe;
import handmadeguns.HandmadeGunsCore;
import handmadeguns.gunsmithing.GunSmithRecipeRegistry;

/**
 * NEI 1.7.10 discovers plugins whose simple class name follows NEI*Config.
 * Nothing in common HMG code references this optional, client-loaded entry point.
 */
public final class NEIHandmadeGunsConfig implements IConfigureNEI {
    public void loadConfig() {
        // Keep one live handler rather than taking an initialization-time recipe
        // snapshot: content-pack recipes are registered during HMG post-init.
        GunSmithingNEIHandler handler = new GunSmithingNEIHandler();
        GuiCraftingRecipe.craftinghandlers.add(handler);
        GuiUsageRecipe.usagehandlers.add(handler);
        HandmadeGunsCore.Debug("Loaded NEI Gun Smithing Table handler; current recipes=%s",
                GunSmithRecipeRegistry.getAll().size());
    }

    public String getName() {
        return "Handmade Guns Gun Smithing Table";
    }

    public String getVersion() {
        String version = HandmadeGunsCore.class.getPackage().getImplementationVersion();
        return version == null ? "unknown" : version;
    }
}
