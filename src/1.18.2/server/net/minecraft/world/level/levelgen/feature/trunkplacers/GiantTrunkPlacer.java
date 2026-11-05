/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

public class GiantTrunkPlacer
extends TrunkPlacer {
    public static final Codec<GiantTrunkPlacer> CODEC = RecordCodecBuilder.create(instance -> GiantTrunkPlacer.trunkPlacerParts(instance).apply((Applicative)instance, GiantTrunkPlacer::new));

    public GiantTrunkPlacer(int n, int n2, int n3) {
        super(n, n2, n3);
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return TrunkPlacerType.GIANT_TRUNK_PLACER;
    }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedReader levelSimulatedReader, BiConsumer<BlockPos, BlockState> biConsumer, Random random, int n, BlockPos blockPos, TreeConfiguration treeConfiguration) {
        BlockPos blockPos2 = blockPos.below();
        GiantTrunkPlacer.setDirtAt(levelSimulatedReader, biConsumer, random, blockPos2, treeConfiguration);
        GiantTrunkPlacer.setDirtAt(levelSimulatedReader, biConsumer, random, blockPos2.east(), treeConfiguration);
        GiantTrunkPlacer.setDirtAt(levelSimulatedReader, biConsumer, random, blockPos2.south(), treeConfiguration);
        GiantTrunkPlacer.setDirtAt(levelSimulatedReader, biConsumer, random, blockPos2.south().east(), treeConfiguration);
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int i = 0; i < n; ++i) {
            GiantTrunkPlacer.placeLogIfFreeWithOffset(levelSimulatedReader, biConsumer, random, mutableBlockPos, treeConfiguration, blockPos, 0, i, 0);
            if (i >= n - 1) continue;
            GiantTrunkPlacer.placeLogIfFreeWithOffset(levelSimulatedReader, biConsumer, random, mutableBlockPos, treeConfiguration, blockPos, 1, i, 0);
            GiantTrunkPlacer.placeLogIfFreeWithOffset(levelSimulatedReader, biConsumer, random, mutableBlockPos, treeConfiguration, blockPos, 1, i, 1);
            GiantTrunkPlacer.placeLogIfFreeWithOffset(levelSimulatedReader, biConsumer, random, mutableBlockPos, treeConfiguration, blockPos, 0, i, 1);
        }
        return ImmutableList.of((Object)new FoliagePlacer.FoliageAttachment(blockPos.above(n), 0, true));
    }

    private static void placeLogIfFreeWithOffset(LevelSimulatedReader levelSimulatedReader, BiConsumer<BlockPos, BlockState> biConsumer, Random random, BlockPos.MutableBlockPos mutableBlockPos, TreeConfiguration treeConfiguration, BlockPos blockPos, int n, int n2, int n3) {
        mutableBlockPos.setWithOffset(blockPos, n, n2, n3);
        GiantTrunkPlacer.placeLogIfFree(levelSimulatedReader, biConsumer, random, mutableBlockPos, treeConfiguration);
    }
}

