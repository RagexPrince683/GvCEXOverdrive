package com.glowingfederal.combatives.util.math;

import com.glowingfederal.combatives.util.BlockPos;
import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class AxisAlignedBBSpliterator extends Spliterators.AbstractSpliterator<AxisAlignedBB> {
    private final Entity entity;
    private final AxisAlignedBB aabb;
    private final CubeCoordinateIterator cubeCoordinateIterator;
    private final World world;
    private final BiPredicate<Block, BlockPos> statePositionPredicate;

    public AxisAlignedBBSpliterator(World world, Entity entity, AxisAlignedBB aabb) {
        this(world, entity, aabb, new BiPredicate<Block, BlockPos>() {
            @Override public boolean test(Block block, BlockPos pos) { return true; }
        });
    }

    public AxisAlignedBBSpliterator(World world, Entity entity, AxisAlignedBB aabb, BiPredicate<Block, BlockPos> statePositionPredicate) {
        super(Long.MAX_VALUE, Spliterator.NONNULL | Spliterator.IMMUTABLE);
        this.world = world;
        this.entity = entity;
        this.aabb = aabb;
        this.statePositionPredicate = statePositionPredicate;
        int startX = MathHelper.floor_double(aabb.minX - 1.0E-7D) - 1;
        int endX = MathHelper.floor_double(aabb.maxX + 1.0E-7D) + 1;
        int startY = MathHelper.floor_double(aabb.minY - 1.0E-7D) - 1;
        int endY = MathHelper.floor_double(aabb.maxY + 1.0E-7D) + 1;
        int startZ = MathHelper.floor_double(aabb.minZ - 1.0E-7D) - 1;
        int endZ = MathHelper.floor_double(aabb.maxZ + 1.0E-7D) + 1;
        this.cubeCoordinateIterator = new CubeCoordinateIterator(startX, startY, startZ, endX, endY, endZ);
    }

    @Override
    public boolean tryAdvance(Consumer<? super AxisAlignedBB> consumer) {
        BlockPos.PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain();
        while (this.cubeCoordinateIterator.hasNext()) {
            int x = this.cubeCoordinateIterator.getX();
            int y = this.cubeCoordinateIterator.getY();
            int z = this.cubeCoordinateIterator.getZ();
            int boundaries = this.cubeCoordinateIterator.numBoundariesTouched();
            if (boundaries == 3) continue;
            pos.setPos(x, y, z);
            if (!this.world.blockExists(x, y, z)) continue;
            Block block = this.world.getBlock(x, y, z);
            if (!this.statePositionPredicate.test(block, pos) || boundaries == 2 && block != Blocks.piston_extension) continue;
            List<AxisAlignedBB> boxes = new ArrayList<AxisAlignedBB>();
            block.addCollisionBoxesToList(this.world, x, y, z, this.aabb, boxes, this.entity);
            if (!boxes.isEmpty()) {
                consumer.accept(boxes.get(0));
                pos.release();
                return true;
            }
        }
        pos.release();
        return false;
    }
}
