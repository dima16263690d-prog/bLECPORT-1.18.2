/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.DiskConfiguration;

public class BaseDiskFeature
extends Feature<DiskConfiguration> {
    public BaseDiskFeature(Codec<DiskConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<DiskConfiguration> featurePlaceContext) {
        DiskConfiguration diskConfiguration = featurePlaceContext.config();
        BlockPos blockPos = featurePlaceContext.origin();
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        boolean bl = false;
        int n = blockPos.getY();
        int n2 = n + diskConfiguration.halfHeight();
        int n3 = n - diskConfiguration.halfHeight() - 1;
        boolean bl2 = diskConfiguration.state().getBlock() instanceof FallingBlock;
        int n4 = diskConfiguration.radius().sample(featurePlaceContext.random());
        for (int i = blockPos.getX() - n4; i <= blockPos.getX() + n4; ++i) {
            for (int j = blockPos.getZ() - n4; j <= blockPos.getZ() + n4; ++j) {
                int n5;
                int n6 = i - blockPos.getX();
                if (n6 * n6 + (n5 = j - blockPos.getZ()) * n5 > n4 * n4) continue;
                boolean bl3 = false;
                for (int k = n2; k >= n3; --k) {
                    BlockPos blockPos2 = new BlockPos(i, k, j);
                    BlockState blockState = worldGenLevel.getBlockState(blockPos2);
                    Block block = blockState.getBlock();
                    boolean bl4 = false;
                    if (k > n3) {
                        for (BlockState blockState2 : diskConfiguration.targets()) {
                            if (!blockState2.is(block)) continue;
                            worldGenLevel.setBlock(blockPos2, diskConfiguration.state(), 2);
                            this.markAboveForPostProcessing(worldGenLevel, blockPos2);
                            bl = true;
                            bl4 = true;
                            break;
                        }
                    }
                    if (bl2 && bl3 && blockState.isAir()) {
                        BlockState blockState3 = diskConfiguration.state().is(Blocks.RED_SAND) ? Blocks.RED_SANDSTONE.defaultBlockState() : Blocks.SANDSTONE.defaultBlockState();
                        worldGenLevel.setBlock(new BlockPos(i, k + 1, j), blockState3, 2);
                    }
                    bl3 = bl4;
                }
            }
        }
        return bl;
    }
}

