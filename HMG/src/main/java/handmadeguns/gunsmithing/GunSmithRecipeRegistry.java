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

    private static final List<GunRecipeEntry> AMMO_RECIPES = new ArrayList<>();


    public static class GunRecipeEntry {
        public final ItemStack result;
        public final ItemStack[] inputs;

        public GunRecipeEntry(ItemStack result, ItemStack[] inputs) {
            this.result = result;
            this.inputs = inputs;
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
     * Register an ammo-type crafting recipe so the GUI can list it.
     * result and inputs are stored as copies for safety.
     */
    public static void registerAmmoRecipe(ItemStack result, ItemStack[] inputs) {
        if (result == null) return;
        ItemStack resCopy = result.copy();
        ItemStack[] normalized = inputs == null ? new ItemStack[0] : inputs.clone();
        // clone inner stacks defensively
        for (int i = 0; i < normalized.length; i++) {
            if (normalized[i] != null) normalized[i] = normalized[i].copy();
        }
        AMMO_RECIPES.add(new GunRecipeEntry(resCopy, normalized));
    }


    /** Return a copy of the registered ammo recipes (so callers can't mutate internal list). */
    public static List<GunRecipeEntry> getAmmoRecipes() {
        return new ArrayList<GunRecipeEntry>(AMMO_RECIPES);
    }


    /** Clears ammo recipes (call before reloading packs if you ever support reload). */
    //not needed
    public static void clearAmmoRecipes() {
        AMMO_RECIPES.clear();
    }

    /**
     * Build a combined ammo recipe list:
     *  - Start with registered ammo recipes (AMMO_RECIPES)
     *  - Then append ammo-like recipes discovered via CraftingManager (shaped + shapeless)
     *  - Avoid duplicate RESULT item+meta entries (so same result doesn't appear twice)
     *
     * Returns defensive copies (so callers can mutate safely).
     */
    // Helper: build counts map and sample map from an ItemStack[] (condenses repeated slots)
    private static java.util.Map<String, Integer> buildCountMap(net.minecraft.item.ItemStack[] arr,
                                                                java.util.Map<String, net.minecraft.item.ItemStack> sampleMap) {
        java.util.Map<String, Integer> counts = new java.util.HashMap<String, Integer>();
        if (arr == null) return counts;
        for (net.minecraft.item.ItemStack s : arr) {
            if (s == null) continue;
            String key;
            try {
                String nbt = (s.hasTagCompound() && s.getTagCompound() != null) ? s.getTagCompound().toString() : "";
                int id = 0;
                try {
                    id = net.minecraft.item.Item.getIdFromItem(s.getItem());
                } catch (Throwable tt) {
                    // fallback to unlocalized name if getIdFromItem isn't available
                }
                if (id != 0) {
                    key = id + ":" + s.getItemDamage() + ":" + nbt;
                } else {
                    key = (s.getItem() == null ? "null" : s.getItem().getUnlocalizedName()) + ":" + s.getItemDamage() + ":" + nbt;
                }
            } catch (Throwable t) {
                // very defensive fallback
                key = (s.getItem() == null ? "null" : s.getItem().toString()) + ":" + s.getItemDamage();
            }
            int prev = counts.containsKey(key) ? counts.get(key) : 0;
            counts.put(key, prev + s.stackSize);
            if (!sampleMap.containsKey(key)) {
                try {
                    sampleMap.put(key, s.copy());
                } catch (Throwable ignored) {
                    sampleMap.put(key, s);
                }
            }
        }
        return counts;
    }

    // Helper: make an ItemStack[] from counts + sampleMap (sets stackSize to aggregated count)
    //no longer needed?
    //private static net.minecraft.item.ItemStack[] makeInputsFromMaps(java.util.Map<String, Integer> counts,
    //                                                                 java.util.Map<String, net.minecraft.item.ItemStack> sampleMap) {
    //    java.util.List<net.minecraft.item.ItemStack> list = new java.util.ArrayList<net.minecraft.item.ItemStack>();
    //    for (java.util.Map.Entry<String, Integer> e : counts.entrySet()) {
    //        String key = e.getKey();
    //        int cnt = e.getValue();
    //        net.minecraft.item.ItemStack sample = sampleMap.get(key);
    //        if (sample == null) continue;
    //        net.minecraft.item.ItemStack s = sample.copy();
    //        s.stackSize = cnt;
    //        list.add(s);
    //    }
    //    return list.toArray(new net.minecraft.item.ItemStack[list.size()]);
    //}

    // helper: count non-null slots in a 3x3 grid
    private static int countFilledSlots(net.minecraft.item.ItemStack[] grid) {
        if (grid == null) return 0;
        int c = 0;
        for (net.minecraft.item.ItemStack s : grid) if (s != null) c++;
        return c;
    }

    public static List<GunRecipeEntry> getCombinedAmmoRecipes() {
        List<GunRecipeEntry> out = new ArrayList<GunRecipeEntry>();

        // 1) Add registered ammo recipes FIRST (already 3x3)
        List<GunRecipeEntry> reg = getAmmoRecipes();
        if (reg != null) {
            for (GunRecipeEntry e : reg) {
                if (e == null || e.result == null) continue;

                // copy into a strict 3x3 grid
                net.minecraft.item.ItemStack[] grid = new net.minecraft.item.ItemStack[9];
                if (e.inputs != null) {
                    for (int i = 0; i < Math.min(9, e.inputs.length); i++) {
                        if (e.inputs[i] != null) grid[i] = e.inputs[i].copy();
                    }
                }

                out.add(new GunRecipeEntry(e.result.copy(), grid));
            }
        }

        // 2) Scan CraftingManager
        List rawList = net.minecraft.item.crafting.CraftingManager.getInstance().getRecipeList();
        if (rawList == null) return out;

        for (Object obj : rawList) {
            try {
                net.minecraft.item.ItemStack result = null;
                net.minecraft.item.ItemStack[] grid = new net.minecraft.item.ItemStack[9];

                if (obj instanceof net.minecraft.item.crafting.ShapedRecipes) {
                    net.minecraft.item.crafting.ShapedRecipes r =
                            (net.minecraft.item.crafting.ShapedRecipes) obj;

                    result = r.getRecipeOutput();
                    if (result == null) continue;

                    net.minecraft.item.ItemStack[] src = r.recipeItems;
                    if (src != null) {
                        // place recipe items into the 3x3 grid starting at index 0 (top-left),
                        // copying at most 9 slots. This preserves shape placement as used elsewhere.
                        for (int i = 0; i < Math.min(9, src.length); i++) {
                            if (src[i] != null) grid[i] = src[i].copy();
                        }
                    }

                } else if (obj instanceof net.minecraft.item.crafting.ShapelessRecipes) {
                    // Shapeless -> lay items left-to-right like vanilla preview
                    net.minecraft.item.crafting.ShapelessRecipes r =
                            (net.minecraft.item.crafting.ShapelessRecipes) obj;

                    result = r.getRecipeOutput();
                    if (result == null) continue;

                    int idx = 0;
                    for (Object o : r.recipeItems) {
                        if (idx >= 9) break;
                        if (o instanceof net.minecraft.item.ItemStack) {
                            grid[idx++] = ((net.minecraft.item.ItemStack) o).copy();
                        }
                    }
                } else {
                    continue; // skip other recipe types
                }

                net.minecraft.item.Item it = result.getItem();
                if (it == null) continue;

                // Ammo heuristic (same as you had)
                String name = "";
                try { name = result.getDisplayName().toLowerCase(); } catch (Throwable ignored) {}
                String un = "";
                try { un = it.getUnlocalizedName().toLowerCase(); } catch (Throwable ignored) {}

                if (!(name.contains("ammo") || name.contains("bullet") || name.contains("round")
                        || un.contains("ammo") || un.contains("bullet") || un.contains("round")
                        || un.contains("hmg") || un.contains("handmade"))) {
                    continue;
                }

                // 3) Avoid duplicate RESULT item+meta BUT prefer the recipe that fills more grid slots
                int foundIndex = -1;
                for (int i = 0; i < out.size(); i++) {
                    GunRecipeEntry e = out.get(i);
                    if (e == null || e.result == null) continue;
                    try {
                        if (e.result.getItem() == result.getItem()
                                && e.result.getItemDamage() == result.getItemDamage()) {
                            foundIndex = i;
                            break;
                        }
                    } catch (Throwable ignored) {}
                }

                if (foundIndex != -1) {
                    // Decide whether to replace existing entry with this one
                    GunRecipeEntry existing = out.get(foundIndex);
                    int existingFilled = countFilledSlots(existing.inputs);
                    int newFilled = countFilledSlots(grid);

                    // Replace only if the new recipe fills strictly more slots (so shaped 3x3 wins)
                    if (newFilled > existingFilled) {
                        out.set(foundIndex, new GunRecipeEntry(result.copy(), grid));
                    }
                    // otherwise keep existing (do not add a second entry)
                    continue;
                }

                // Not found yet -> add the new recipe (defensive copy already done)
                out.add(new GunRecipeEntry(result.copy(), grid));

            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        return out;
    }






    //  Parse a single recipe file that follows the "AddRecipe/Slot*/CraftItem" format (based on the example provided).
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
