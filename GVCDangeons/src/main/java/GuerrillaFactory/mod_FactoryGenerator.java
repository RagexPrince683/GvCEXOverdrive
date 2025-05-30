package GuerrillaFactory;

//import DungeonGeneratorBase.DungeonData;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraftforge.common.MinecraftForge;

import java.io.*;
import java.util.ArrayList;

//import static DungeonGeneratorBase.DungeonData.loadDungeon;
//import static DungeonGeneratorBase.mod_DungeonGeneratorBase.dangeondatapath;
//import static DungeonGeneratorBase.mod_DungeonGeneratorBase.datafile;

@Mod(
        modid	= "GuerrillaFactoryGenerator",
        name	= "GuerrillaFactoryGenerator",
        version	= "1.7.x-srg-1",
        dependencies = "required-after:GVCMob"
)
public class mod_FactoryGenerator {
    public static File directory1;
    @Mod.EventHandler
    public void init_(FMLInitializationEvent pEvent) {
        String path = "assets/gfactory/Factory.gvcdg";
        //InputStream entry = datafile.getInputStream(datafile.getEntry(path));
//            File file = new File(path);
        // BufferedReader reader = new BufferedReader(new InputStreamReader(entry));
        // loadDangeonData(reader);
        // entry.close();
        MapGenStructureIO.registerStructure(StructuresStartDungeonFactory.class, "DGStructureStart_Factory");
        MapGenStructureIO.func_143031_a(ComponentFactory.class, "DGDungeon_Factory");
        GenerateEventHandler generateEventHandler = new GenerateEventHandler();
        MinecraftForge.EVENT_BUS.register(generateEventHandler);
        MinecraftForge.TERRAIN_GEN_BUS.register(generateEventHandler);

    }

    int minx = -1;
    int miny = -1;
    int minz = -1;
    int maxx = -1;
    int maxy = -1;
    int maxz = -1;

    //static ArrayList<DungeonData> dungeonData = new ArrayList<DungeonData>();

    public void loadDangeonData(BufferedReader file){
        //dungeonData.add(loadDungeon(file));
    }

    private static boolean checkBeforeReadfile(File file) {
        if (file.exists()) {
            if (file.isFile() && file.canRead()) {
                return true;
            }
        }

        return false;
    }
}
