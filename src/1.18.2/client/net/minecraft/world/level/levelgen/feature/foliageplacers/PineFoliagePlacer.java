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

public class PineFoliagePlacer
extends FoliagePlacer {
    public static final Codec<PineFoliagePlacer> CODEC = RecordCodecBuilder.create(instance -> PineFoliagePlacer.foliagePlacerParts(instance).and((App)IntProvider.codec(0, 24).fieldOf("height").forGetter(pineFoliagePlacer -> pineFoliagePlacer.height)).apply((Applicative)instance, PineFoliagePlacer::new));
    private final IntProvider height;

    public PineFoliagePlacer(IntProvider intProvider, IntProvider intProvider2, IntProvider intProvider3) {
        super(intProvider, intProvider2);
        this.height = intProvider3;
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return FoliagePlacerType.PINE_FOLIAGE_PLACER;
    }

    @Override
    protected void createFoliage(LevelSimulatedReader levelSimulatedReader, BiConsumer<BlockPos, BlockState> biConsumer, Random random, TreeConfiguration treeConfiguration, int n, FoliagePlacer.FoliageAttachment foliageAttachment, int n2, int n3, int n4) {
        int n5 = 0;
        for (int i = n4; i >= n4 - n2; --i) {
            this.placeLeavesRow(levelSimulatedReader, biConsumer, random, treeConfiguration, foliageAttachment.pos(), n5, i, foliageAttachment.doubleTrunk());
            if (n5 >= 1 && i == n4 - n2 + 1) {
                --n5;
                continue;
            }
            if (n5 >= n3 + foliageAttachment.radiusOffset()) continue;
            ++n5;
        }
    }

    @Override
    public int foliageRadius(Random random, int n) {
        return super.foliageRadius(random, n) + random.nextInt(Math.max(n + 1, 1));
    }

    @Override
    public int foliageHeight(Random random, int n, TreeConfiguration treeConfiguration) {
        return this.height.sample(random);
    }

    @Override
    protected boolean shouldSkipLocation(Random random, int n, int n2, int n3, int n4, boolean bl) {
        return n == n4 && n3 == n4 && n4 > 0;
    }
}

