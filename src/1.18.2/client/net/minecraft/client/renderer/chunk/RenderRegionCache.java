/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer.chunk;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

public class RenderRegionCache {
    private final Long2ObjectMap<ChunkInfo> chunkInfoCache = new Long2ObjectOpenHashMap();

    @Nullable
    public RenderChunkRegion createRegion(Level level, BlockPos blockPos, BlockPos blockPos2, int n) {
        int n2;
        int n3 = SectionPos.blockToSectionCoord(blockPos.getX() - n);
        int n4 = SectionPos.blockToSectionCoord(blockPos.getZ() - n);
        int n5 = SectionPos.blockToSectionCoord(blockPos2.getX() + n);
        int n6 = SectionPos.blockToSectionCoord(blockPos2.getZ() + n);
        ChunkInfo[][] chunkInfoArray = new ChunkInfo[n5 - n3 + 1][n6 - n4 + 1];
        for (int i = n3; i <= n5; ++i) {
            for (n2 = n4; n2 <= n6; ++n2) {
                chunkInfoArray[i - n3][n2 - n4] = (ChunkInfo)this.chunkInfoCache.computeIfAbsent(ChunkPos.asLong(i, n2), l -> new ChunkInfo(level.getChunk(ChunkPos.getX(l), ChunkPos.getZ(l))));
            }
        }
        if (RenderRegionCache.isAllEmpty(blockPos, blockPos2, n3, n4, chunkInfoArray)) {
            return null;
        }
        RenderChunk[][] renderChunkArray = new RenderChunk[n5 - n3 + 1][n6 - n4 + 1];
        for (n2 = n3; n2 <= n5; ++n2) {
            for (int i = n4; i <= n6; ++i) {
                renderChunkArray[n2 - n3][i - n4] = chunkInfoArray[n2 - n3][i - n4].renderChunk();
            }
        }
        return new RenderChunkRegion(level, n3, n4, renderChunkArray);
    }

    private static boolean isAllEmpty(BlockPos blockPos, BlockPos blockPos2, int n, int n2, ChunkInfo[][] chunkInfoArray) {
        int n3 = SectionPos.blockToSectionCoord(blockPos.getX());
        int n4 = SectionPos.blockToSectionCoord(blockPos.getZ());
        int n5 = SectionPos.blockToSectionCoord(blockPos2.getX());
        int n6 = SectionPos.blockToSectionCoord(blockPos2.getZ());
        for (int i = n3; i <= n5; ++i) {
            for (int j = n4; j <= n6; ++j) {
                LevelChunk levelChunk = chunkInfoArray[i - n][j - n2].chunk();
                if (levelChunk.isYSpaceEmpty(blockPos.getY(), blockPos2.getY())) continue;
                return false;
            }
        }
        return true;
    }

    static final class ChunkInfo {
        private final LevelChunk chunk;
        @Nullable
        private RenderChunk renderChunk;

        ChunkInfo(LevelChunk levelChunk) {
            this.chunk = levelChunk;
        }

        public LevelChunk chunk() {
            return this.chunk;
        }

        public RenderChunk renderChunk() {
            if (this.renderChunk == null) {
                this.renderChunk = new RenderChunk(this.chunk);
            }
            return this.renderChunk;
        }
    }
}

