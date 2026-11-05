/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;

public class BlockBlobFeature
extends Feature<BlockStateConfiguration> {
    public BlockBlobFeature(Codec<BlockStateConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<BlockStateConfiguration> featurePlaceContext) {
        BlockState blockState;
        BlockPos blockPos = featurePlaceContext.origin();
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        Random random = featurePlaceContext.random();
        BlockStateConfiguration blockStateConfiguration = featurePlaceContext.config();
        while (blockPos.getY() > worldGenLevel.getMinBuildHeight() + 3 && (worldGenLevel.isEmptyBlock(blockPos.below()) || !BlockBlobFeature.isDirt(blockState = worldGenLevel.getBlockState(blockPos.below())) && !BlockBlobFeature.isStone(blockState))) {
            blockPos = blockPos.below();
        }
        if (blockPos.getY() <= worldGenLevel.getMinBuildHeight() + 3) {
            return false;
        }
        for (int i = 0; i < 3; ++i) {
            int n = random.nextInt(2);
            int n2 = random.nextInt(2);
            int n3 = random.nextInt(2);
            float f = (float)(n + n2 + n3) * 0.333f + 0.5f;
            for (BlockPos blockPos2 : BlockPos.betweenClosed(blockPos.offset(-n, -n2, -n3), blockPos.offset(n, n2, n3))) {
                if (!(blockPos2.distSqr(blockPos) <= (double)(f * f))) continue;
                worldGenLevel.setBlock(blockPos2, blockStateConfiguration.state, 4);
            }
            blockPos = blockPos.offset(-1 + random.nextInt(2), -random.nextInt(2), -1 + random.nextInt(2));
        }
        return true;
    }
}

