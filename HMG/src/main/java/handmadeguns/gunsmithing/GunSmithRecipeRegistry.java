package handmadeguns.gunsmithing;

import net.minecraft.item.ItemStack;
import java.util.ArrayList;
import java.util.List;

public class GunSmithRecipeRegistry {

    public static class GunRecipeEntry {
        public final ItemStack result;
        public final ItemStack[] inputs;

        public GunRecipeEntry(ItemStack result, ItemStack[] inputs) {
            this.result = result;
            this.inputs = inputs;
        }
    }

    private static final List<GunRecipeEntry> RECIPES = new ArrayList<>();

    public static void register(ItemStack result, ItemStack... inputs) {
        RECIPES.add(new GunRecipeEntry(result, inputs));
    }

    //TODO: integrate recipes from THIS logic, currently our register method is unused:

    /***
     * public void readPackRecipe(File packdir){
     *
     * 		File[] packlist = packdir.listFiles();
     * 		if(packlist == null) return;
     *
     * 		Arrays.sort(packlist, new Comparator<File>() {
     * 			public int compare(File file1, File file2){
     * 				return file1.getName().compareTo(file2.getName());
     *                        }* 		});
     *
     * 		for (File aPacklist : packlist) {
     * 			if (aPacklist.isDirectory()) {
     *
     * 				File[] recipelist = getFileList(aPacklist, "addpackrecipe");
     *
     * 				if(recipelist != null && recipelist.length > 0){
     *
     * 					Arrays.sort(recipelist, new Comparator<File>(){
     * 						public int compare(File file1, File file2){
     * 							return file1.getName().compareTo(file2.getName());
     *                        }
     *                    });
     *
     * 					for(int count = 0; count < recipelist.length; count++){
     *
     * 						File recipeFile = recipelist[count];
     *
     * 						try {
     * 							HMGGunMaker.addRecipe(recipeFile);
     * 							System.out.println("[HMG] Loaded recipe: " + recipeFile.getAbsolutePath());
     *
     *                        } catch (Exception e) {
     * 							System.out.println("[HMG] ERROR: Failed to load recipe: " + recipeFile.getAbsolutePath());
     * 							e.printStackTrace(); // full crash reason in console
     *                        }
     *                    }
     *                }
     *            }
     *        }
     *    }
     *
     */

    public static List<GunRecipeEntry> getAll() {
        return RECIPES;
    }
}