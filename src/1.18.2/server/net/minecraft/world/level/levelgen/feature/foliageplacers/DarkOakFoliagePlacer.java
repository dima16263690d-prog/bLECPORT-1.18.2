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

public class DarkOakFoliagePlacer
extends FoliagePlacer {
    public static final Codec<DarkOakFoliagePlacer> CODEC = RecordCodecBuilder.create(instance -> DarkOakFoliagePlacer.foliagePlacerParts(instance).apply((Applicative)instance, DarkOakFoliagePlacer::new));

    public DarkOakFoliagePlacer(IntProvider intProvider, IntProvider intProvider2) {
        super(intProvider, intProvider2);
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return FoliagePlacerType.DARK_OAK_FOLIAGE_PLACER;
    }

    @Override
    protected void createFoliage(LevelSimulatedReader levelSimulatedReader, BiConsumer<BlockPos, BlockState> biConsumer, Random random, TreeConfiguration treeConfiguration, int n, FoliagePlacer.FoliageAttachment foliageAttachment, int n2, int n3, int n4) {
        BlockPos blockPos = foliageAttachment.pos().above(n4);
        boolean bl = foliageAttachment.doubleTrunk();
        if (bl) {
            this.placeLeavesRow(levelSimulatedReader, biConsumer, random, treeConfiguration, blockPos, n3 + 2, -1, bl);
            this.placeLeavesRow(levelSimulatedReader, biConsumer, random, treeConfiguration, blockPos, n3 + 3, 0, bl);
            this.placeLeavesRow(levelSimulatedReader, biConsumer, random, treeConfiguration, blockPos, n3 + 2, 1, bl);
            if (random.nextBoolean()) {
                this.placeLeavesRow(levelSimulatedReader, biConsumer, random, treeConfiguration, blockPos, n3, 2, bl);
            }
        } else {
            this.placeLeavesRow(levelSimulatedReader, biConsumer, random, treeConfiguration, blockPos, n3 + 2, -1, bl);
            this.placeLeavesRow(levelSimulatedReader, biConsumer, random, treeConfiguration, blockPos, n3 + 1, 0, bl);
        }
    }

    @Override
    public int foliageHeight(Random random, int n, TreeConfiguration treeConfiguration) {
        return 4;
    }

    @Override
    protected boolean shouldSkipLocationSigned(Random random, int n, int n2, int n3, int n4, boolean bl) {
        if (!(n2 != 0 || !bl || n != -n4 && n < n4 || n3 != -n4 && n3 < n4)) {
            return true;
        }
        return super.shouldSkipLocationSigned(random, n, n2, n3, n4, bl);
    }

    @Override
    protected boolean shouldSkipLocation(Random random, int n, int n2, int n3, int n4, boolean bl) {
        if (n2 == -1 && !bl) {
            return n == n4 && n3 == n4;
        }
        if (n2 == 1) {
            return n + n3 > n4 * 2 - 2;
        }
        return false;
    }
}

