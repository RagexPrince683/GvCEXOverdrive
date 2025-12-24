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
    public static Item springSet;
    public static Item gunOil;

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

        springSet = new Item()
                .setUnlocalizedName("springSet")
                .setTextureName("handmadeguns:spring_set")
                .setCreativeTab(tabHMGCrafting);

        gunOil = new Item()
                .setUnlocalizedName("gunOil")
                .setTextureName("handmadeguns:gun_oil")
                .setCreativeTab(tabHMGCrafting);

        GameRegistry.registerItem(polymer, "polymer");
        GameRegistry.registerItem(springSet, "springSet");
        GameRegistry.registerItem(gunOil, "gunOil");

        // Polymer
        GameRegistry.addShapelessRecipe(
                new ItemStack(polymer),
                new ItemStack(Items.slime_ball),
                new ItemStack(Items.reeds)
        );

        // Spring Set = any steel ingot
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(springSet),
                " S ",
                " S ",
                " S ",
                'S', "ingotSteel"
        ));

        // Gun Oil
        GameRegistry.addShapelessRecipe(
                new ItemStack(gunOil),
                new ItemStack(Items.glass_bottle),
                new ItemStack(Items.wheat_seeds),
                new ItemStack(Items.coal)
        );
    }
}

