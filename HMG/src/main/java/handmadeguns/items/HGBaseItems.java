package handmadeguns.items;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class HGBaseItems {

    // ===================
    // ITEMS
    // ===================
    public static Item polymer;

    // ===================
    // CREATIVE TAB
    // ===================
    public static final CreativeTabs tabHMGCrafting = new CreativeTabs("HMGCrafting") {
        @Override
        public Item getTabIconItem() {
            return polymer;
        }
    };

    public static void init() {

        polymer = new Item()
                .setUnlocalizedName("polymer")
                .setTextureName("handmadeguns:polymer")
                .setCreativeTab(tabHMGCrafting);



        GameRegistry.registerItem(polymer, "polymer");

        // Polymer
        GameRegistry.addShapelessRecipe(
                new ItemStack(polymer),
                new ItemStack(Items.slime_ball),
                new ItemStack(Items.reeds)
        );
    }
}

