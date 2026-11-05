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
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;

public class AcaciaFoliagePlacer
extends FoliagePlacer {
    public static final Codec<AcaciaFoliagePlacer> CODEC = RecordCodecBuilder.create(instance -> AcaciaFoliagePlacer.foliagePlacerParts(instance).apply((Applicative)instance, AcaciaFoliagePlacer::new));

    public AcaciaFoliagePlacer(IntProvider intProvider, IntProvider intProvider2) {
        super(intProvider, intProvider2);
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return FoliagePlacerType.ACACIA_FOLIAGE_PLACER;
    }

    @Override
    protected void createFoliage(LevelSimulatedReader levelSimulatedReader, BiConsumer<BlockPos, BlockState> biConsumer, Random random, TreeConfiguration treeConfiguration, int n, FoliagePlacer.FoliageAttachment foliageAttachment, int n2, int n3, int n4) {
        boolean bl = foliageAttachment.doubleTrunk();
        BlockPos blockPos = foliageAttachment.pos().above(n4);
        this.placeLeavesRow(levelSimulatedReader, biConsumer, random, treeConfiguration, blockPos, n3 + foliageAttachment.radiusOffset(), -1 - n2, bl);
        this.placeLeavesRow(levelSimulatedReader, biConsumer, random, treeConfiguration, blockPos, n3 - 1, -n2, bl);
        this.placeLeavesRow(levelSimulatedReader, biConsumer, random, treeConfiguration, blockPos, n3 + foliageAttachment.radiusOffset() - 1, 0, bl);
    }

    @Override
    public int foliageHeight(Random random, int n, TreeConfiguration treeConfiguration) {
        return 0;
    }

    @Override
    protected boolean shouldSkipLocation(Random random, int n, int n2, int n3, int n4, boolean bl) {
        if (n2 == 0) {
            return (n > 1 || n3 > 1) && n != 0 && n3 != 0;
        }
        return n == n4 && n3 == n4 && n4 > 0;
    }
}

