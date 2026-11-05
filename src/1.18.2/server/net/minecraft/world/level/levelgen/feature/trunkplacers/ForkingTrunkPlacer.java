/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.Random;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

public class ForkingTrunkPlacer
extends TrunkPlacer {
    public static final Codec<ForkingTrunkPlacer> CODEC = RecordCodecBuilder.create(instance -> ForkingTrunkPlacer.trunkPlacerParts(instance).apply((Applicative)instance, ForkingTrunkPlacer::new));

    public ForkingTrunkPlacer(int n, int n2, int n3) {
        super(n, n2, n3);
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return TrunkPlacerType.FORKING_TRUNK_PLACER;
    }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedReader levelSimulatedReader, BiConsumer<BlockPos, BlockState> biConsumer, Random random, int n, BlockPos blockPos, TreeConfiguration treeConfiguration) {
        int n2;
        ForkingTrunkPlacer.setDirtAt(levelSimulatedReader, biConsumer, random, blockPos.below(), treeConfiguration);
        ArrayList arrayList = Lists.newArrayList();
        Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        int n3 = n - random.nextInt(4) - 1;
        int n4 = 3 - random.nextInt(3);
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        int n5 = blockPos.getX();
        int n6 = blockPos.getZ();
        OptionalInt optionalInt = OptionalInt.empty();
        for (int i = 0; i < n; ++i) {
            n2 = blockPos.getY() + i;
            if (i >= n3 && n4 > 0) {
                n5 += direction.getStepX();
                n6 += direction.getStepZ();
                --n4;
            }
            if (!ForkingTrunkPlacer.placeLog(levelSimulatedReader, biConsumer, random, mutableBlockPos.set(n5, n2, n6), treeConfiguration)) continue;
            optionalInt = OptionalInt.of(n2 + 1);
        }
        if (optionalInt.isPresent()) {
            arrayList.add(new FoliagePlacer.FoliageAttachment(new BlockPos(n5, optionalInt.getAsInt(), n6), 1, false));
        }
        n5 = blockPos.getX();
        n6 = blockPos.getZ();
        Direction direction2 = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        if (direction2 != direction) {
            n2 = n3 - random.nextInt(2) - 1;
            int n7 = 1 + random.nextInt(3);
            optionalInt = OptionalInt.empty();
            for (int i = n2; i < n && n7 > 0; ++i, --n7) {
                if (i < 1) continue;
                int n8 = blockPos.getY() + i;
                if (!ForkingTrunkPlacer.placeLog(levelSimulatedReader, biConsumer, random, mutableBlockPos.set(n5 += direction2.getStepX(), n8, n6 += direction2.getStepZ()), treeConfiguration)) continue;
                optionalInt = OptionalInt.of(n8 + 1);
            }
            if (optionalInt.isPresent()) {
                arrayList.add(new FoliagePlacer.FoliageAttachment(new BlockPos(n5, optionalInt.getAsInt(), n6), 0, false));
            }
        }
        return arrayList;
    }
}

