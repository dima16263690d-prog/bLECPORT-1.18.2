/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.block.grower;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public abstract class AbstractMegaTreeGrower
extends AbstractTreeGrower {
    @Override
    public boolean growTree(ServerLevel serverLevel, ChunkGenerator chunkGenerator, BlockPos blockPos, BlockState blockState, Random random) {
        for (int i = 0; i >= -1; --i) {
            for (int j = 0; j >= -1; --j) {
                if (!AbstractMegaTreeGrower.isTwoByTwoSapling(blockState, serverLevel, blockPos, i, j)) continue;
                return this.placeMega(serverLevel, chunkGenerator, blockPos, blockState, random, i, j);
            }
        }
        return super.growTree(serverLevel, chunkGenerator, blockPos, blockState, random);
    }

    @Nullable
    protected abstract Holder<? extends ConfiguredFeature<?, ?>> getConfiguredMegaFeature(Random var1);

    public boolean placeMega(ServerLevel serverLevel, ChunkGenerator chunkGenerator, BlockPos blockPos, BlockState blockState, Random random, int n, int n2) {
        Holder<ConfiguredFeature<?, ?>> holder = this.getConfiguredMegaFeature(random);
        if (holder == null) {
            return false;
        }
        ConfiguredFeature<?, ?> configuredFeature = holder.value();
        BlockState blockState2 = Blocks.AIR.defaultBlockState();
        serverLevel.setBlock(blockPos.offset(n, 0, n2), blockState2, 4);
        serverLevel.setBlock(blockPos.offset(n + 1, 0, n2), blockState2, 4);
        serverLevel.setBlock(blockPos.offset(n, 0, n2 + 1), blockState2, 4);
        serverLevel.setBlock(blockPos.offset(n + 1, 0, n2 + 1), blockState2, 4);
        if (configuredFeature.place(serverLevel, chunkGenerator, random, blockPos.offset(n, 0, n2))) {
            return true;
        }
        serverLevel.setBlock(blockPos.offset(n, 0, n2), blockState, 4);
        serverLevel.setBlock(blockPos.offset(n + 1, 0, n2), blockState, 4);
        serverLevel.setBlock(blockPos.offset(n, 0, n2 + 1), blockState, 4);
        serverLevel.setBlock(blockPos.offset(n + 1, 0, n2 + 1), blockState, 4);
        return false;
    }

    public static boolean isTwoByTwoSapling(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, int n, int n2) {
        Block block = blockState.getBlock();
        return blockGetter.getBlockState(blockPos.offset(n, 0, n2)).is(block) && blockGetter.getBlockState(blockPos.offset(n + 1, 0, n2)).is(block) && blockGetter.getBlockState(blockPos.offset(n, 0, n2 + 1)).is(block) && blockGetter.getBlockState(blockPos.offset(n + 1, 0, n2 + 1)).is(block);
    }
}

