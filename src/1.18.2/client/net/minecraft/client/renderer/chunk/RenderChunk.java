/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer.chunk;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.levelgen.DebugLevelSource;

class RenderChunk {
    private final Map<BlockPos, BlockEntity> blockEntities;
    @Nullable
    private final List<PalettedContainer<BlockState>> sections;
    private final boolean debug;
    private final LevelChunk wrapped;

    RenderChunk(LevelChunk levelChunk) {
        this.wrapped = levelChunk;
        this.debug = levelChunk.getLevel().isDebug();
        this.blockEntities = ImmutableMap.copyOf(levelChunk.getBlockEntities());
        if (levelChunk instanceof EmptyLevelChunk) {
            this.sections = null;
        } else {
            LevelChunkSection[] levelChunkSectionArray = levelChunk.getSections();
            this.sections = new ArrayList<PalettedContainer<BlockState>>(levelChunkSectionArray.length);
            for (LevelChunkSection levelChunkSection : levelChunkSectionArray) {
                this.sections.add(levelChunkSection.hasOnlyAir() ? null : levelChunkSection.getStates().copy());
            }
        }
    }

    @Nullable
    public BlockEntity getBlockEntity(BlockPos blockPos) {
        return this.blockEntities.get(blockPos);
    }

    public BlockState getBlockState(BlockPos blockPos) {
        int n = blockPos.getX();
        int n2 = blockPos.getY();
        int n3 = blockPos.getZ();
        if (this.debug) {
            BlockState blockState = null;
            if (n2 == 60) {
                blockState = Blocks.BARRIER.defaultBlockState();
            }
            if (n2 == 70) {
                blockState = DebugLevelSource.getBlockStateFor(n, n3);
            }
            return blockState == null ? Blocks.AIR.defaultBlockState() : blockState;
        }
        if (this.sections == null) {
            return Blocks.AIR.defaultBlockState();
        }
        try {
            PalettedContainer<BlockState> palettedContainer;
            int n4 = this.wrapped.getSectionIndex(n2);
            if (n4 >= 0 && n4 < this.sections.size() && (palettedContainer = this.sections.get(n4)) != null) {
                return palettedContainer.get(n & 0xF, n2 & 0xF, n3 & 0xF);
            }
            return Blocks.AIR.defaultBlockState();
        }
        catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.forThrowable(throwable, "Getting block state");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Block being got");
            crashReportCategory.setDetail("Location", () -> CrashReportCategory.formatLocation((LevelHeightAccessor)this.wrapped, n, n2, n3));
            throw new ReportedException(crashReport);
        }
    }
}

