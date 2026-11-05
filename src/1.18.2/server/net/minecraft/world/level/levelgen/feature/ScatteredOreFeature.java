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
import net.minecraft.world.level.levelgen.feature.OreFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

public class ScatteredOreFeature
extends Feature<OreConfiguration> {
    private static final int MAX_DIST_FROM_ORIGIN = 7;

    ScatteredOreFeature(Codec<OreConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<OreConfiguration> featurePlaceContext) {
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        Random random = featurePlaceContext.random();
        OreConfiguration oreConfiguration = featurePlaceContext.config();
        BlockPos blockPos = featurePlaceContext.origin();
        int n = random.nextInt(oreConfiguration.size + 1);
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        block0: for (int i = 0; i < n; ++i) {
            this.offsetTargetPos(mutableBlockPos, random, blockPos, Math.min(i, 7));
            BlockState blockState = worldGenLevel.getBlockState(mutableBlockPos);
            for (OreConfiguration.TargetBlockState targetBlockState : oreConfiguration.targetStates) {
                if (!OreFeature.canPlaceOre(blockState, worldGenLevel::getBlockState, random, oreConfiguration, targetBlockState, mutableBlockPos)) continue;
                worldGenLevel.setBlock(mutableBlockPos, targetBlockState.state, 2);
                continue block0;
            }
        }
        return true;
    }

    private void offsetTargetPos(BlockPos.MutableBlockPos mutableBlockPos, Random random, BlockPos blockPos, int n) {
        int n2 = this.getRandomPlacementInOneAxisRelativeToOrigin(random, n);
        int n3 = this.getRandomPlacementInOneAxisRelativeToOrigin(random, n);
        int n4 = this.getRandomPlacementInOneAxisRelativeToOrigin(random, n);
        mutableBlockPos.setWithOffset(blockPos, n2, n3, n4);
    }

    private int getRandomPlacementInOneAxisRelativeToOrigin(Random random, int n) {
        return Math.round((random.nextFloat() - random.nextFloat()) * (float)n);
    }
}

