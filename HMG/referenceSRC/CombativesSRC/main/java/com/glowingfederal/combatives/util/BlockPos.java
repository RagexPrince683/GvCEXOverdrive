package com.glowingfederal.combatives.util;

import net.minecraft.util.MathHelper;

public class BlockPos {
    protected int x;
    protected int y;
    protected int z;

    public BlockPos(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public BlockPos(double x, double y, double z) {
        this(MathHelper.floor_double(x), MathHelper.floor_double(y), MathHelper.floor_double(z));
    }

    public int getX() { return this.x; }
    public int getY() { return this.y; }
    public int getZ() { return this.z; }

    public static class MutableBlockPos extends BlockPos {
        public MutableBlockPos(int x, int y, int z) {
            super(x, y, z);
        }

        public MutableBlockPos setPos(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
            return this;
        }
    }

    public static final class PooledMutableBlockPos extends MutableBlockPos {
        private PooledMutableBlockPos(int x, int y, int z) {
            super(x, y, z);
        }

        public static PooledMutableBlockPos retain() {
            return new PooledMutableBlockPos(0, 0, 0);
        }

        public void release() {
        }

        @Override
        public PooledMutableBlockPos setPos(int x, int y, int z) {
            super.setPos(x, y, z);
            return this;
        }
    }
}
