/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.ReplaceSphereConfiguration;

public class ReplaceBlobsFeature
extends Feature<ReplaceSphereConfiguration> {
    public ReplaceBlobsFeature(Codec<ReplaceSphereConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<ReplaceSphereConfiguration> featurePlaceContext) {
        ReplaceSphereConfiguration replaceSphereConfiguration = featurePlaceContext.config();
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        Random random = featurePlaceContext.random();
        Block block = replaceSphereConfiguration.targetState.getBlock();
        BlockPos blockPos = ReplaceBlobsFeature.findTarget(worldGenLevel, featurePlaceContext.origin().mutable().clamp(Direction.Axis.Y, worldGenLevel.getMinBuildHeight() + 1, worldGenLevel.getMaxBuildHeight() - 1), block);
        if (blockPos == null) {
            return false;
        }
        int n = replaceSphereConfiguration.radius().sample(random);
        int n2 = replaceSphereConfiguration.radius().sample(random);
        int n3 = replaceSphereConfiguration.radius().sample(random);
        int n4 = Math.max(n, Math.max(n2, n3));
        boolean bl = false;
        for (BlockPos blockPos2 : BlockPos.withinManhattan(blockPos, n, n2, n3)) {
            if (blockPos2.distManhattan(blockPos) > n4) break;
            BlockState blockState = worldGenLevel.getBlockState(blockPos2);
            if (!blockState.is(block)) continue;
            this.setBlock(worldGenLevel, blockPos2, replaceSphereConfiguration.replaceState);
            bl = true;
        }
        return bl;
    }

    @Nullable
    private static BlockPos findTarget(LevelAccessor levelAccessor, BlockPos.MutableBlockPos mutableBlockPos, Block block) {
        while (mutableBlockPos.getY() > levelAccessor.getMinBuildHeight() + 1) {
            BlockState blockState = levelAccessor.getBlockState(mutableBlockPos);
            if (blockState.is(block)) {
                return mutableBlockPos;
            }
            mutableBlockPos.move(Direction.DOWN);
        }
        return null;
    }
}

