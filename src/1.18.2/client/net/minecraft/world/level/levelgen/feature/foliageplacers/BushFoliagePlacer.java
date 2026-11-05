/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;

public class BushFoliagePlacer
extends BlobFoliagePlacer {
    public static final Codec<BushFoliagePlacer> CODEC = RecordCodecBuilder.create(instance -> BushFoliagePlacer.blobParts(instance).apply((Applicative)instance, BushFoliagePlacer::new));

    public BushFoliagePlacer(IntProvider intProvider, IntProvider intProvider2, int n) {
        super(intProvider, intProvider2, n);
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return FoliagePlacerType.BUSH_FOLIAGE_PLACER;
    }

    @Override
    protected void createFoliage(LevelSimulatedReader levelSimulatedReader, BiConsumer<BlockPos, BlockState> biConsumer, Random random, TreeConfiguration treeConfiguration, int n, FoliagePlacer.FoliageAttachment foliageAttachment, int n2, int n3, int n4) {
        for (int i = n4; i >= n4 - n2; --i) {
            int n5 = n3 + foliageAttachment.radiusOffset() - 1 - i;
            this.placeLeavesRow(levelSimulatedReader, biConsumer, random, treeConfiguration, foliageAttachment.pos(), n5, i, foliageAttachment.doubleTrunk());
        }
    }

    @Override
    protected boolean shouldSkipLocation(Random random, int n, int n2, int n3, int n4, boolean bl) {
        return n == n4 && n3 == n4 && random.nextInt(2) == 0;
    }
}

