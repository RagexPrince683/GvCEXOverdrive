package handmadeguns.world;

import cpw.mods.fml.common.IWorldGenerator;
import handmadeguns.items.HGMetalBlocks;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.feature.WorldGenMinable;

import java.util.Random;

public class HGWorldGen implements IWorldGenerator {

    @Override
    public void generate(Random rand, int chunkX, int chunkZ, World world,
                         IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {

        if (world.provider.dimensionId != 0) return;

        int x = chunkX * 16;
        int z = chunkZ * 16;

        if (HGWorldGenConfig.generateCopper) {
            generateCopper(rand, x, z, world);
        }

        if (HGWorldGenConfig.generateAluminum) {
            generateAluminum(rand, x, z, world);
        }
    }

    private void generateCopper(Random rand, int x, int z, World world) {
        int veinsPerChunk = 12;
        int veinSize = 9;
        int minY = 20;
        int maxY = 64;

        for (int i = 0; i < veinsPerChunk; i++) {
            int y = minY + rand.nextInt(maxY - minY);

            new WorldGenMinable(HGMetalBlocks.copperOre, veinSize).generate(
                    world,
                    rand,
                    x + rand.nextInt(16),
                    y,
                    z + rand.nextInt(16)
            );
        }
    }

    private void generateAluminum(Random rand, int x, int z, World world) {
        int veinsPerChunk = 9;
        int veinSize = 7;
        int minY = 32;
        int maxY = 96;

        for (int i = 0; i < veinsPerChunk; i++) {
            int y = minY + rand.nextInt(maxY - minY);

            new WorldGenMinable(HGMetalBlocks.aluminumOre, veinSize).generate(
                    world,
                    rand,
                    x + rand.nextInt(16),
                    y,
                    z + rand.nextInt(16)
            );
        }
    }
}
