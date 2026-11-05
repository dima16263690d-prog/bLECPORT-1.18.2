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

public class SpruceFoliagePlacer
extends FoliagePlacer {
    public static final Codec<SpruceFoliagePlacer> CODEC = RecordCodecBuilder.create(instance -> SpruceFoliagePlacer.foliagePlacerParts(instance).and((App)IntProvider.codec(0, 24).fieldOf("trunk_height").forGetter(spruceFoliagePlacer -> spruceFoliagePlacer.trunkHeight)).apply((Applicative)instance, SpruceFoliagePlacer::new));
    private final IntProvider trunkHeight;

    public SpruceFoliagePlacer(IntProvider intProvider, IntProvider intProvider2, IntProvider intProvider3) {
        super(intProvider, intProvider2);
        this.trunkHeight = intProvider3;
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return FoliagePlacerType.SPRUCE_FOLIAGE_PLACER;
    }

    @Override
    protected void createFoliage(LevelSimulatedReader levelSimulatedReader, BiConsumer<BlockPos, BlockState> biConsumer, Random random, TreeConfiguration treeConfiguration, int n, FoliagePlacer.FoliageAttachment foliageAttachment, int n2, int n3, int n4) {
        BlockPos blockPos = foliageAttachment.pos();
        int n5 = random.nextInt(2);
        int n6 = 1;
        int n7 = 0;
        for (int i = n4; i >= -n2; --i) {
            this.placeLeavesRow(levelSimulatedReader, biConsumer, random, treeConfiguration, blockPos, n5, i, foliageAttachment.doubleTrunk());
            if (n5 >= n6) {
                n5 = n7;
                n7 = 1;
                n6 = Math.min(n6 + 1, n3 + foliageAttachment.radiusOffset());
                continue;
            }
            ++n5;
        }
    }

    @Override
    public int foliageHeight(Random random, int n, TreeConfiguration treeConfiguration) {
        return Math.max(4, n - this.trunkHeight.sample(random));
    }

    @Override
    protected boolean shouldSkipLocation(Random random, int n, int n2, int n3, int n4, boolean bl) {
        return n == n4 && n3 == n4 && n4 > 0;
    }
}

