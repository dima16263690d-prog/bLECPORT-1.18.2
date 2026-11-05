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
import net.minecraft.util.Mth;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;

public class MegaPineFoliagePlacer
extends FoliagePlacer {
    public static final Codec<MegaPineFoliagePlacer> CODEC = RecordCodecBuilder.create(instance -> MegaPineFoliagePlacer.foliagePlacerParts(instance).and((App)IntProvider.codec(0, 24).fieldOf("crown_height").forGetter(megaPineFoliagePlacer -> megaPineFoliagePlacer.crownHeight)).apply((Applicative)instance, MegaPineFoliagePlacer::new));
    private final IntProvider crownHeight;

    public MegaPineFoliagePlacer(IntProvider intProvider, IntProvider intProvider2, IntProvider intProvider3) {
        super(intProvider, intProvider2);
        this.crownHeight = intProvider3;
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return FoliagePlacerType.MEGA_PINE_FOLIAGE_PLACER;
    }

    @Override
    protected void createFoliage(LevelSimulatedReader levelSimulatedReader, BiConsumer<BlockPos, BlockState> biConsumer, Random random, TreeConfiguration treeConfiguration, int n, FoliagePlacer.FoliageAttachment foliageAttachment, int n2, int n3, int n4) {
        BlockPos blockPos = foliageAttachment.pos();
        int n5 = 0;
        for (int i = blockPos.getY() - n2 + n4; i <= blockPos.getY() + n4; ++i) {
            int n6 = blockPos.getY() - i;
            int n7 = n3 + foliageAttachment.radiusOffset() + Mth.floor((float)n6 / (float)n2 * 3.5f);
            int n8 = n6 > 0 && n7 == n5 && (i & 1) == 0 ? n7 + 1 : n7;
            this.placeLeavesRow(levelSimulatedReader, biConsumer, random, treeConfiguration, new BlockPos(blockPos.getX(), i, blockPos.getZ()), n8, 0, foliageAttachment.doubleTrunk());
            n5 = n7;
        }
    }

    @Override
    public int foliageHeight(Random random, int n, TreeConfiguration treeConfiguration) {
        return this.crownHeight.sample(random);
    }

    @Override
    protected boolean shouldSkipLocation(Random random, int n, int n2, int n3, int n4, boolean bl) {
        if (n + n3 >= 7) {
            return true;
        }
        return n * n + n3 * n3 > n4 * n4;
    }
}

