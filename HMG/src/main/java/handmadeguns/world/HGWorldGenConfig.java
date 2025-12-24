package handmadeguns.world;

import handmadeguns.compat.HGMetalCompat;

public class HGWorldGenConfig {

    public static boolean generateCopper;
    public static boolean generateAluminum;

    public static void init() {
        generateCopper = !HGMetalCompat.externalCopperExists();
        generateAluminum = !HGMetalCompat.externalAluminumExists();
    }
}
