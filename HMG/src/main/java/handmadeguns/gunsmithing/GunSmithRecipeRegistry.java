package handmadeguns.gunsmithing;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class GunSmithRecipeRegistry {

    public static class GunRecipeEntry {
        public final ItemStack result;
        public final ItemStack[] inputs;

        public GunRecipeEntry(ItemStack result, ItemStack[] inputs) {
            this.result = result;
            this.inputs = inputs;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof GunRecipeEntry)) return false;
            GunRecipeEntry other = (GunRecipeEntry) obj;
            if (result == null || other.result == null) return false;
            // only compare result item + meta (ignore NBT for now)
            return result.getItem() == other.result.getItem() &&
                    result.getItemDamage() == other.result.getItemDamage();
        }

        @Override
        public int hashCode() {
            int hash = 17;
            hash = 31 * hash + (result != null ? result.getItem().hashCode() : 0);
            hash = 31 * hash + (result != null ? result.getItemDamage() : 0);
            return hash;
        }
    }


    public static void registerFromFile(File recipeFile) {
        try {
            parseAndRegisterAddRecipeFile(recipeFile);
        } catch (Exception e) {
            System.out.println("[GunSmith] Failed to parse: " + recipeFile.getName());
            e.printStackTrace();
        }
    }

    private static final List<GunRecipeEntry> RECIPES = new ArrayList<>();

    public static void register(ItemStack result, ItemStack... inputs) {
        if (result == null) return;
        // normalize input array length to 9 (3x3) if needed
        ItemStack[] normalized = inputs;
        if (normalized == null) normalized = new ItemStack[0];
        RECIPES.add(new GunRecipeEntry(result, normalized));
    }

    public static List<GunRecipeEntry> getAll() {
        return RECIPES;
    }

    /**
     * Load all recipes from the given pack directory.
     * This mirrors the behavior of your readPackRecipe: it searches subdirectories,
     * finds files with "addpackrecipe" in their name, sorts them, and parses each.
     *
     * Call this from your main mod postInit (after items have been registered).
     *
     * Example:
     *   GunSmithRecipeRegistry.loadRecipesFromPackDir(new File(modBaseDir, "packs"));
     */

    //this is not used
    public static void loadRecipesFromPackDir(File packdir) {
        if (packdir == null || !packdir.exists() || !packdir.isDirectory()) return;

        File[] packlist = packdir.listFiles();
        if (packlist == null) return;

        Arrays.sort(packlist, new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                return f1.getName().compareTo(f2.getName());
            }
        });

        for (File aPack : packlist) {
            if (!aPack.isDirectory()) continue;

            // find files with "addpackrecipe" in the name (same as your getFileList lookup)
            File[] recipelist = aPack.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().contains("addpackrecipe");
                }
            });

            if (recipelist == null || recipelist.length == 0) continue;

            Arrays.sort(recipelist, new Comparator<File>() {
                @Override
                public int compare(File f1, File f2) {
                    return f1.getName().compareTo(f2.getName());
                }
            });

            for (File recipeFile : recipelist) {
                try {
                    parseAndRegisterAddRecipeFile(recipeFile);
                    System.out.println("[HMG] Loaded recipe: " + recipeFile.getAbsolutePath());
                } catch (Exception e) {
                    System.out.println("[HMG] ERROR: Failed to load recipe: " + recipeFile.getAbsolutePath());
                    e.printStackTrace();
                }
            }
        }
    }


    //  Parse a single recipe file that follows the "AddRecipe/Slot*/CraftItem" format (based on the example you pasted).
    private static void parseAndRegisterAddRecipeFile(File recipeFile) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(recipeFile), "UTF-8"));
        try {
            String line;
            ItemStack[] grid = new ItemStack[9]; // slots 0..8 map to Slot1..Slot9
            ItemStack result = null;
            boolean readingRecipe = false;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                // start marker
                if (line.equalsIgnoreCase("AddRecipe")) {
                    grid = new ItemStack[9];
                    result = null;
                    readingRecipe = true;
                    continue;
                }

                if (!readingRecipe) continue;

                // Slot line, expecting "SlotN,<item-spec>"
                if (line.toLowerCase().startsWith("slot")) {
                    String[] parts = line.split(",", 2);
                    if (parts.length < 2) continue;

                    String slotPart = parts[0].trim(); // e.g. "Slot1"
                    String itemPart = parts[1].trim();

                    // parse slot number (Slot1 -> index 0)
                    int slotIndex;
                    try {
                        slotIndex = Integer.parseInt(slotPart.substring(4)) - 1;
                    } catch (Exception ex) {
                        continue;
                    }
                    if (slotIndex < 0 || slotIndex > 8) continue;

                    ItemStack s = parseSlotItemString(itemPart);
                    grid[slotIndex] = s;
                    continue;
                }

                // Result line: "CraftItem,<modid>:<name>:<meta>:<count>"
                if (line.toLowerCase().startsWith("craftitem")) {
                    String[] parts = line.split(",", 2);
                    if (parts.length < 2) {
                        readingRecipe = false;
                        continue;
                    }
                    result = parseResultWeaponString(parts[1].trim());

                    // register now if result present
                    if (result != null) {
                        // determine inputs array length (trim trailing nulls if you want)
                        register(result, grid);
                    }

                    readingRecipe = false; // finished this block
                    continue;
                }

                // If file contains other blocks (Recipe1 / ItemA etc) we ignore them
            }
        } finally {
            br.close();
        }
    }

    /**
     * Parse slot item strings like:
     *   minecraft:iron_ingot:
     *   modid:itemname:meta:count
     *   minecraft,iron_ingot
     *
     * Returns null if cannot resolve item.
     */
    private static ItemStack parseSlotItemString(String s) {

        if (s == null) return null;
        s = s.trim();
        if (s.isEmpty()) return null;

        // replace comma with colon to support both formats
        s = s.replace(',', ':');

        // remove trailing colons (some lines end with :)
        while (s.endsWith(":")) s = s.substring(0, s.length() - 1);

        String[] p = s.split(":");
        if (p.length < 2) return null;

        String modid = p[0].trim();
        String name = p[1].trim();
        int meta = 0;
        int count = 1;

        if (p.length >= 3) {
            // try parse meta or count depending on number of parts
            try {
                meta = Integer.parseInt(p[2].trim());
            } catch (NumberFormatException ignored) {}
        }
        if (p.length >= 4) {
            try {
                count = Integer.parseInt(p[3].trim());
            } catch (NumberFormatException ignored) {}
        }

        Item item = GameRegistry.findItem(modid, name);
        if (item == null) {
            // try fallback: maybe name and mod are reversed? (rare)
            // don't spam errors, just return null
            return null;
        }

        return new ItemStack(item, Math.max(1, count), meta);
    }

    /**
     * Parse the CraftItem result format used in your example:
     *   HandmadeGuns:AK47:0:1  -> modid:itemname:meta:count
     */
    private static ItemStack parseResultWeaponString(String s) {
        if (s == null) return null;
        s = s.trim();
        if (s.isEmpty()) return null;

        String[] p = s.split(":");
        // allow either mod:item:meta:count or mod:item (defaults)
        if (p.length < 2) return null;

        String modid = p[0].trim();
        String name = p[1].trim();
        int meta = 0;
        int count = 1;

        if (p.length >= 3) {
            try {
                meta = Integer.parseInt(p[2].trim());
            } catch (NumberFormatException ignored) {}
        }
        if (p.length >= 4) {
            try {
                count = Integer.parseInt(p[3].trim());
            } catch (NumberFormatException ignored) {}
        }

        Item item = GameRegistry.findItem(modid, name);
        if (item == null) {
            return null;
        }

        return new ItemStack(item, Math.max(1, count), meta);
    }
}
