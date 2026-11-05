/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.Products$P2
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder$Instance
 *  com.mojang.serialization.codecs.RecordCodecBuilder$Mu
 */
package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.datafixers.Products;
import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;

public abstract class FoliagePlacer {
    public static final Codec<FoliagePlacer> CODEC = Registry.FOLIAGE_PLACER_TYPES.byNameCodec().dispatch(FoliagePlacer::type, FoliagePlacerType::codec);
    protected final IntProvider radius;
    protected final IntProvider offset;

    protected static <P extends FoliagePlacer> Products.P2<RecordCodecBuilder.Mu<P>, IntProvider, IntProvider> foliagePlacerParts(RecordCodecBuilder.Instance<P> instance) {
        return instance.group((App)IntProvider.codec(0, 16).fieldOf("radius").forGetter(foliagePlacer -> foliagePlacer.radius), (App)IntProvider.codec(0, 16).fieldOf("offset").forGetter(foliagePlacer -> foliagePlacer.offset));
    }

    public FoliagePlacer(IntProvider intProvider, IntProvider intProvider2) {
        this.radius = intProvider;
        this.offset = intProvider2;
    }

    protected abstract FoliagePlacerType<?> type();

    public void createFoliage(LevelSimulatedReader levelSimulatedReader, BiConsumer<BlockPos, BlockState> biConsumer, Random random, TreeConfiguration treeConfiguration, int n, FoliageAttachment foliageAttachment, int n2, int n3) {
        this.createFoliage(levelSimulatedReader, biConsumer, random, treeConfiguration, n, foliageAttachment, n2, n3, this.offset(random));
    }

    protected abstract void createFoliage(LevelSimulatedReader var1, BiConsumer<BlockPos, BlockState> var2, Random var3, TreeConfiguration var4, int var5, FoliageAttachment var6, int var7, int var8, int var9);

    public abstract int foliageHeight(Random var1, int var2, TreeConfiguration var3);

    public int foliageRadius(Random random, int n) {
        return this.radius.sample(random);
    }

    private int offset(Random random) {
        return this.offset.sample(random);
    }

    protected abstract boolean shouldSkipLocation(Random var1, int var2, int var3, int var4, int var5, boolean var6);

    protected boolean shouldSkipLocationSigned(Random random, int n, int n2, int n3, int n4, boolean bl) {
        int n5;
        int n6;
        if (bl) {
            n6 = Math.min(Math.abs(n), Math.abs(n - 1));
            n5 = Math.min(Math.abs(n3), Math.abs(n3 - 1));
        } else {
            n6 = Math.abs(n);
            n5 = Math.abs(n3);
        }
        return this.shouldSkipLocation(random, n6, n2, n5, n4, bl);
    }

    protected void placeLeavesRow(LevelSimulatedReader levelSimulatedReader, BiConsumer<BlockPos, BlockState> biConsumer, Random random, TreeConfiguration treeConfiguration, BlockPos blockPos, int n, int n2, boolean bl) {
        int n3 = bl ? 1 : 0;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int i = -n; i <= n + n3; ++i) {
            for (int j = -n; j <= n + n3; ++j) {
                if (this.shouldSkipLocationSigned(random, i, n2, j, n, bl)) continue;
                mutableBlockPos.setWithOffset(blockPos, i, n2, j);
                FoliagePlacer.tryPlaceLeaf(levelSimulatedReader, biConsumer, random, treeConfiguration, mutableBlockPos);
            }
        }
    }

    protected static void tryPlaceLeaf(LevelSimulatedReader levelSimulatedReader, BiConsumer<BlockPos, BlockState> biConsumer, Random random, TreeConfiguration treeConfiguration, BlockPos blockPos) {
        if (TreeFeature.validTreePos(levelSimulatedReader, blockPos)) {
            biConsumer.accept(blockPos, treeConfiguration.foliageProvider.getState(random, blockPos));
        }
    }

    public static final class FoliageAttachment {
        private final BlockPos pos;
        private final int radiusOffset;
        private final boolean doubleTrunk;

        public FoliageAttachment(BlockPos blockPos, int n, boolean bl) {
            this.pos = blockPos;
            this.radiusOffset = n;
            this.doubleTrunk = bl;
        }

        public BlockPos pos() {
            return this.pos;
        }

        public int radiusOffset() {
            return this.radiusOffset;
        }

        public boolean doubleTrunk() {
            return this.doubleTrunk;
        }
    }
}

