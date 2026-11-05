/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.datafixers.kinds.App;
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

public class RandomSpreadFoliagePlacer
extends FoliagePlacer {
    public static final Codec<RandomSpreadFoliagePlacer> CODEC = RecordCodecBuilder.create(instance -> RandomSpreadFoliagePlacer.foliagePlacerParts(instance).and(instance.group((App)IntProvider.codec(1, 512).fieldOf("foliage_height").forGetter(randomSpreadFoliagePlacer -> randomSpreadFoliagePlacer.foliageHeight), (App)Codec.intRange((int)0, (int)256).fieldOf("leaf_placement_attempts").forGetter(randomSpreadFoliagePlacer -> randomSpreadFoliagePlacer.leafPlacementAttempts))).apply((Applicative)instance, RandomSpreadFoliagePlacer::new));
    private final IntProvider foliageHeight;
    private final int leafPlacementAttempts;

    public RandomSpreadFoliagePlacer(IntProvider intProvider, IntProvider intProvider2, IntProvider intProvider3, int n) {
        super(intProvider, intProvider2);
        this.foliageHeight = intProvider3;
        this.leafPlacementAttempts = n;
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return FoliagePlacerType.RANDOM_SPREAD_FOLIAGE_PLACER;
    }

    @Override
    protected void createFoliage(LevelSimulatedReader levelSimulatedReader, BiConsumer<BlockPos, BlockState> biConsumer, Random random, TreeConfiguration treeConfiguration, int n, FoliagePlacer.FoliageAttachment foliageAttachment, int n2, int n3, int n4) {
        BlockPos blockPos = foliageAttachment.pos();
        BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
        for (int i = 0; i < this.leafPlacementAttempts; ++i) {
            mutableBlockPos.setWithOffset(blockPos, random.nextInt(n3) - random.nextInt(n3), random.nextInt(n2) - random.nextInt(n2), random.nextInt(n3) - random.nextInt(n3));
            RandomSpreadFoliagePlacer.tryPlaceLeaf(levelSimulatedReader, biConsumer, random, treeConfiguration, mutableBlockPos);
        }
    }

    @Override
    public int foliageHeight(Random random, int n, TreeConfiguration treeConfiguration) {
        return this.foliageHeight.sample(random);
    }

    @Override
    protected boolean shouldSkipLocation(Random random, int n, int n2, int n3, int n4, boolean bl) {
        return false;
    }
}

