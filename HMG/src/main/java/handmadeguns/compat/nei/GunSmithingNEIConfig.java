package handmadeguns.compat.nei;

import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;
import codechicken.nei.recipe.GuiCraftingRecipe;
import codechicken.nei.recipe.GuiUsageRecipe;
import handmadeguns.HandmadeGunsCore;
import net.minecraft.item.ItemStack;

/** Loaded by NEI's optional plugin discovery; it is never referenced by common HMG code. */
public class GunSmithingNEIConfig implements IConfigureNEI {
    public void loadConfig() {
        GunSmithingNEIHandler crafting = new GunSmithingNEIHandler();
        GunSmithingNEIHandler usage = new GunSmithingNEIHandler();
        GuiCraftingRecipe.craftinghandlers.add(crafting);
        GuiUsageRecipe.usagehandlers.add(usage);
        API.addRecipeCatalyst(new ItemStack(HandmadeGunsCore.blockGunTable), GunSmithingNEIHandler.OVERLAY_ID);
    }

    public String getName() {
        return "Handmade Guns Gun Smithing Table";
    }

    public String getVersion() {
        return HandmadeGunsCore.class.getPackage().getImplementationVersion();
    }
}
