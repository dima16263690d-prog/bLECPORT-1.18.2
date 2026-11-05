/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer.chunk;

import javax.annotation.Nullable;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;

public class RenderChunkRegion
implements BlockAndTintGetter {
    private final int centerX;
    private final int centerZ;
    protected final RenderChunk[][] chunks;
    protected final Level level;

    RenderChunkRegion(Level level, int n, int n2, RenderChunk[][] renderChunkArray) {
        this.level = level;
        this.centerX = n;
        this.centerZ = n2;
        this.chunks = renderChunkArray;
    }

    @Override
    public BlockState getBlockState(BlockPos blockPos) {
        int n = SectionPos.blockToSectionCoord(blockPos.getX()) - this.centerX;
        int n2 = SectionPos.blockToSectionCoord(blockPos.getZ()) - this.centerZ;
        return this.chunks[n][n2].getBlockState(blockPos);
    }

    @Override
    public FluidState getFluidState(BlockPos blockPos) {
        int n = SectionPos.blockToSectionCoord(blockPos.getX()) - this.centerX;
        int n2 = SectionPos.blockToSectionCoord(blockPos.getZ()) - this.centerZ;
        return this.chunks[n][n2].getBlockState(blockPos).getFluidState();
    }

    @Override
    public float getShade(Direction direction, boolean bl) {
        return this.level.getShade(direction, bl);
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return this.level.getLightEngine();
    }

    @Override
    @Nullable
    public BlockEntity getBlockEntity(BlockPos blockPos) {
        int n = SectionPos.blockToSectionCoord(blockPos.getX()) - this.centerX;
        int n2 = SectionPos.blockToSectionCoord(blockPos.getZ()) - this.centerZ;
        return this.chunks[n][n2].getBlockEntity(blockPos);
    }

    @Override
    public int getBlockTint(BlockPos blockPos, ColorResolver colorResolver) {
        return this.level.getBlockTint(blockPos, colorResolver);
    }

    @Override
    public int getMinBuildHeight() {
        return this.level.getMinBuildHeight();
    }

    @Override
    public int getHeight() {
        return this.level.getHeight();
    }
}

