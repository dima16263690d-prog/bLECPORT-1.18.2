/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap
 *  javax.annotation.Nullable
 */
package net.minecraft.client.color.block;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.ToIntFunction;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;

public class BlockTintCache {
    private static final int MAX_CACHE_ENTRIES = 256;
    private final ThreadLocal<LatestCacheInfo> latestChunkOnThread = ThreadLocal.withInitial(LatestCacheInfo::new);
    private final Long2ObjectLinkedOpenHashMap<CacheData> cache = new Long2ObjectLinkedOpenHashMap(256, 0.25f);
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final ToIntFunction<BlockPos> source;

    public BlockTintCache(ToIntFunction<BlockPos> toIntFunction) {
        this.source = toIntFunction;
    }

    public int getColor(BlockPos blockPos) {
        int n;
        int n2 = SectionPos.blockToSectionCoord(blockPos.getX());
        int n3 = SectionPos.blockToSectionCoord(blockPos.getZ());
        LatestCacheInfo latestCacheInfo = this.latestChunkOnThread.get();
        if (latestCacheInfo.x != n2 || latestCacheInfo.z != n3 || latestCacheInfo.cache == null) {
            latestCacheInfo.x = n2;
            latestCacheInfo.z = n3;
            latestCacheInfo.cache = this.findOrCreateChunkCache(n2, n3);
        }
        int[] nArray = latestCacheInfo.cache.getLayer(blockPos.getY());
        int n4 = blockPos.getX() & 0xF;
        int n5 = blockPos.getZ() & 0xF;
        int n6 = n5 << 4 | n4;
        int n7 = nArray[n6];
        if (n7 != -1) {
            return n7;
        }
        nArray[n6] = n = this.source.applyAsInt(blockPos);
        return n;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void invalidateForChunk(int n, int n2) {
        try {
            this.lock.writeLock().lock();
            for (int i = -1; i <= 1; ++i) {
                for (int j = -1; j <= 1; ++j) {
                    long l = ChunkPos.asLong(n + i, n2 + j);
                    this.cache.remove(l);
                }
            }
        }
        finally {
            this.lock.writeLock().unlock();
        }
    }

    public void invalidateAll() {
        try {
            this.lock.writeLock().lock();
            this.cache.clear();
        }
        finally {
            this.lock.writeLock().unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private CacheData findOrCreateChunkCache(int n, int n2) {
        CacheData cacheData;
        long l = ChunkPos.asLong(n, n2);
        this.lock.readLock().lock();
        try {
            cacheData = (CacheData)this.cache.get(l);
            if (cacheData != null) {
                CacheData cacheData2 = cacheData;
                return cacheData2;
            }
        }
        finally {
            this.lock.readLock().unlock();
        }
        this.lock.writeLock().lock();
        try {
            cacheData = (CacheData)this.cache.get(l);
            if (cacheData != null) {
                CacheData cacheData3 = cacheData;
                return cacheData3;
            }
            CacheData cacheData4 = new CacheData();
            if (this.cache.size() >= 256) {
                this.cache.removeFirst();
            }
            this.cache.put(l, (Object)cacheData4);
            CacheData cacheData5 = cacheData4;
            return cacheData5;
        }
        finally {
            this.lock.writeLock().unlock();
        }
    }

    static class LatestCacheInfo {
        public int x = Integer.MIN_VALUE;
        public int z = Integer.MIN_VALUE;
        @Nullable
        CacheData cache;

        private LatestCacheInfo() {
        }
    }

    static class CacheData {
        private final Int2ObjectArrayMap<int[]> cache = new Int2ObjectArrayMap(16);
        private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        private static final int BLOCKS_PER_LAYER = Mth.square(16);

        CacheData() {
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public int[] getLayer(int n2) {
            int[] nArray;
            this.lock.readLock().lock();
            try {
                nArray = (int[])this.cache.get(n2);
                if (nArray != null) {
                    int[] nArray2 = nArray;
                    return nArray2;
                }
            }
            finally {
                this.lock.readLock().unlock();
            }
            this.lock.writeLock().lock();
            try {
                nArray = (int[])this.cache.computeIfAbsent(n2, n -> this.allocateLayer());
                return nArray;
            }
            finally {
                this.lock.writeLock().unlock();
            }
        }

        private int[] allocateLayer() {
            int[] nArray = new int[BLOCKS_PER_LAYER];
            Arrays.fill(nArray, -1);
            return nArray;
        }
    }
}

