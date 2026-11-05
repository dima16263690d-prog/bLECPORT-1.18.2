/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.level;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;

public class ColumnPos {
    private static final long COORD_BITS = 32L;
    private static final long COORD_MASK = 0xFFFFFFFFL;
    private static final int HASH_A = 1664525;
    private static final int HASH_C = 1013904223;
    private static final int HASH_Z_XOR = -559038737;
    public final int x;
    public final int z;

    public ColumnPos(int n, int n2) {
        this.x = n;
        this.z = n2;
    }

    public ColumnPos(BlockPos blockPos) {
        this.x = blockPos.getX();
        this.z = blockPos.getZ();
    }

    public ChunkPos toChunkPos() {
        return new ChunkPos(SectionPos.blockToSectionCoord(this.x), SectionPos.blockToSectionCoord(this.z));
    }

    public long toLong() {
        return ColumnPos.asLong(this.x, this.z);
    }

    public static long asLong(int n, int n2) {
        return (long)n & 0xFFFFFFFFL | ((long)n2 & 0xFFFFFFFFL) << 32;
    }

    public String toString() {
        return "[" + this.x + ", " + this.z + "]";
    }

    public int hashCode() {
        int n = 1664525 * this.x + 1013904223;
        int n2 = 1664525 * (this.z ^ 0xDEADBEEF) + 1013904223;
        return n ^ n2;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof ColumnPos) {
            ColumnPos columnPos = (ColumnPos)object;
            return this.x == columnPos.x && this.z == columnPos.z;
        }
        return false;
    }
}

