/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.google.common.collect.Lists
 *  com.mojang.serialization.Codec
 *  it.unimi.dsi.fastutil.longs.Long2IntMap
 *  it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.levelgen;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.NoiseRouterData;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.OreVeinifier;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.material.MaterialRuleList;

public class NoiseChunk
implements DensityFunction.ContextProvider,
DensityFunction.FunctionContext {
    private final NoiseSettings noiseSettings;
    final int cellCountXZ;
    final int cellCountY;
    final int cellNoiseMinY;
    private final int firstCellX;
    private final int firstCellZ;
    final int firstNoiseX;
    final int firstNoiseZ;
    final List<NoiseInterpolator> interpolators;
    final List<CacheAllInCell> cellCaches;
    private final Map<DensityFunction, DensityFunction> wrapped = new HashMap<DensityFunction, DensityFunction>();
    private final Long2IntMap preliminarySurfaceLevel = new Long2IntOpenHashMap();
    private final Aquifer aquifer;
    private final DensityFunction initialDensityNoJaggedness;
    private final BlockStateFiller blockStateRule;
    private final Blender blender;
    private final FlatCache blendAlpha;
    private final FlatCache blendOffset;
    private final DensityFunctions.BeardifierOrMarker beardifier;
    private long lastBlendingDataPos = ChunkPos.INVALID_CHUNK_POS;
    private Blender.BlendingOutput lastBlendingOutput = new Blender.BlendingOutput(1.0, 0.0);
    final int noiseSizeXZ;
    final int cellWidth;
    final int cellHeight;
    boolean interpolating;
    boolean fillingCell;
    private int cellStartBlockX;
    int cellStartBlockY;
    private int cellStartBlockZ;
    int inCellX;
    int inCellY;
    int inCellZ;
    long interpolationCounter;
    long arrayInterpolationCounter;
    int arrayIndex;
    private final DensityFunction.ContextProvider sliceFillingContextProvider = new DensityFunction.ContextProvider(){

        @Override
        public DensityFunction.FunctionContext forIndex(int n) {
            NoiseChunk.this.cellStartBlockY = (n + NoiseChunk.this.cellNoiseMinY) * NoiseChunk.this.cellHeight;
            ++NoiseChunk.this.interpolationCounter;
            NoiseChunk.this.inCellY = 0;
            NoiseChunk.this.arrayIndex = n;
            return NoiseChunk.this;
        }

        @Override
        public void fillAllDirectly(double[] dArray, DensityFunction densityFunction) {
            for (int i = 0; i < NoiseChunk.this.cellCountY + 1; ++i) {
                NoiseChunk.this.cellStartBlockY = (i + NoiseChunk.this.cellNoiseMinY) * NoiseChunk.this.cellHeight;
                ++NoiseChunk.this.interpolationCounter;
                NoiseChunk.this.inCellY = 0;
                NoiseChunk.this.arrayIndex = i;
                dArray[i] = densityFunction.compute(NoiseChunk.this);
            }
        }
    };

    public static NoiseChunk forChunk(ChunkAccess chunkAccess, NoiseRouter noiseRouter, Supplier<DensityFunctions.BeardifierOrMarker> supplier, NoiseGeneratorSettings noiseGeneratorSettings, Aquifer.FluidPicker fluidPicker, Blender blender) {
        ChunkPos chunkPos = chunkAccess.getPos();
        NoiseSettings noiseSettings = noiseGeneratorSettings.noiseSettings();
        int n = Math.max(noiseSettings.minY(), chunkAccess.getMinBuildHeight());
        int n2 = Math.min(noiseSettings.minY() + noiseSettings.height(), chunkAccess.getMaxBuildHeight());
        int n3 = Mth.intFloorDiv(n, noiseSettings.getCellHeight());
        int n4 = Mth.intFloorDiv(n2 - n, noiseSettings.getCellHeight());
        return new NoiseChunk(16 / noiseSettings.getCellWidth(), n4, n3, noiseRouter, chunkPos.getMinBlockX(), chunkPos.getMinBlockZ(), supplier.get(), noiseGeneratorSettings, fluidPicker, blender);
    }

    public static NoiseChunk forColumn(int n, int n2, int n3, int n4, NoiseRouter noiseRouter, NoiseGeneratorSettings noiseGeneratorSettings, Aquifer.FluidPicker fluidPicker) {
        return new NoiseChunk(1, n4, n3, noiseRouter, n, n2, DensityFunctions.BeardifierMarker.INSTANCE, noiseGeneratorSettings, fluidPicker, Blender.empty());
    }

    private NoiseChunk(int n, int n2, int n3, NoiseRouter noiseRouter, int n4, int n5, DensityFunctions.BeardifierOrMarker beardifierOrMarker, NoiseGeneratorSettings noiseGeneratorSettings, Aquifer.FluidPicker fluidPicker, Blender blender) {
        int n6;
        int n7;
        this.noiseSettings = noiseGeneratorSettings.noiseSettings();
        this.cellCountXZ = n;
        this.cellCountY = n2;
        this.cellNoiseMinY = n3;
        this.cellWidth = this.noiseSettings.getCellWidth();
        this.cellHeight = this.noiseSettings.getCellHeight();
        this.firstCellX = Math.floorDiv(n4, this.cellWidth);
        this.firstCellZ = Math.floorDiv(n5, this.cellWidth);
        this.interpolators = Lists.newArrayList();
        this.cellCaches = Lists.newArrayList();
        this.firstNoiseX = QuartPos.fromBlock(n4);
        this.firstNoiseZ = QuartPos.fromBlock(n5);
        this.noiseSizeXZ = QuartPos.fromBlock(n * this.cellWidth);
        this.blender = blender;
        this.beardifier = beardifierOrMarker;
        this.blendAlpha = new FlatCache(new BlendAlpha(), false);
        this.blendOffset = new FlatCache(new BlendOffset(), false);
        for (n7 = 0; n7 <= this.noiseSizeXZ; ++n7) {
            n6 = this.firstNoiseX + n7;
            int n8 = QuartPos.toBlock(n6);
            for (int i = 0; i <= this.noiseSizeXZ; ++i) {
                int n9 = this.firstNoiseZ + i;
                int n10 = QuartPos.toBlock(n9);
                Blender.BlendingOutput blendingOutput = blender.blendOffsetAndFactor(n8, n10);
                this.blendAlpha.values[n7][i] = blendingOutput.alpha();
                this.blendOffset.values[n7][i] = blendingOutput.blendingOffset();
            }
        }
        if (!noiseGeneratorSettings.isAquifersEnabled()) {
            this.aquifer = Aquifer.createDisabled(fluidPicker);
        } else {
            n7 = SectionPos.blockToSectionCoord(n4);
            n6 = SectionPos.blockToSectionCoord(n5);
            this.aquifer = Aquifer.create(this, new ChunkPos(n7, n6), noiseRouter.barrierNoise(), noiseRouter.fluidLevelFloodednessNoise(), noiseRouter.fluidLevelSpreadNoise(), noiseRouter.lavaNoise(), noiseRouter.aquiferPositionalRandomFactory(), n3 * this.cellHeight, n2 * this.cellHeight, fluidPicker);
        }
        ImmutableList.Builder builder = ImmutableList.builder();
        DensityFunction densityFunction = DensityFunctions.cacheAllInCell(DensityFunctions.add(noiseRouter.finalDensity(), DensityFunctions.BeardifierMarker.INSTANCE)).mapAll(this::wrap);
        builder.add(functionContext -> this.aquifer.computeSubstance(functionContext, densityFunction.compute(functionContext)));
        if (noiseGeneratorSettings.oreVeinsEnabled()) {
            builder.add((Object)OreVeinifier.create(noiseRouter.veinToggle().mapAll(this::wrap), noiseRouter.veinRidged().mapAll(this::wrap), noiseRouter.veinGap().mapAll(this::wrap), noiseRouter.oreVeinsPositionalRandomFactory()));
        }
        this.blockStateRule = new MaterialRuleList((List<BlockStateFiller>)builder.build());
        this.initialDensityNoJaggedness = noiseRouter.initialDensityWithoutJaggedness().mapAll(this::wrap);
    }

    protected Climate.Sampler cachedClimateSampler(NoiseRouter noiseRouter) {
        return new Climate.Sampler(noiseRouter.temperature().mapAll(this::wrap), noiseRouter.humidity().mapAll(this::wrap), noiseRouter.continents().mapAll(this::wrap), noiseRouter.erosion().mapAll(this::wrap), noiseRouter.depth().mapAll(this::wrap), noiseRouter.ridges().mapAll(this::wrap), noiseRouter.spawnTarget());
    }

    @Nullable
    protected BlockState getInterpolatedState() {
        return this.blockStateRule.calculate(this);
    }

    @Override
    public int blockX() {
        return this.cellStartBlockX + this.inCellX;
    }

    @Override
    public int blockY() {
        return this.cellStartBlockY + this.inCellY;
    }

    @Override
    public int blockZ() {
        return this.cellStartBlockZ + this.inCellZ;
    }

    public int preliminarySurfaceLevel(int n, int n2) {
        return this.preliminarySurfaceLevel.computeIfAbsent(ChunkPos.asLong(QuartPos.fromBlock(n), QuartPos.fromBlock(n2)), this::computePreliminarySurfaceLevel);
    }

    private int computePreliminarySurfaceLevel(long l) {
        int n = ChunkPos.getX(l);
        int n2 = ChunkPos.getZ(l);
        return (int)NoiseRouterData.computePreliminarySurfaceLevelScanning(this.noiseSettings, this.initialDensityNoJaggedness, QuartPos.toBlock(n), QuartPos.toBlock(n2));
    }

    @Override
    public Blender getBlender() {
        return this.blender;
    }

    private void fillSlice(boolean bl, int n) {
        this.cellStartBlockX = n * this.cellWidth;
        this.inCellX = 0;
        for (int i = 0; i < this.cellCountXZ + 1; ++i) {
            int n2 = this.firstCellZ + i;
            this.cellStartBlockZ = n2 * this.cellWidth;
            this.inCellZ = 0;
            ++this.arrayInterpolationCounter;
            for (NoiseInterpolator noiseInterpolator : this.interpolators) {
                double[] dArray = (bl ? noiseInterpolator.slice0 : noiseInterpolator.slice1)[i];
                noiseInterpolator.fillArray(dArray, this.sliceFillingContextProvider);
            }
        }
        ++this.arrayInterpolationCounter;
    }

    public void initializeForFirstCellX() {
        if (this.interpolating) {
            throw new IllegalStateException("Staring interpolation twice");
        }
        this.interpolating = true;
        this.interpolationCounter = 0L;
        this.fillSlice(true, this.firstCellX);
    }

    public void advanceCellX(int n) {
        this.fillSlice(false, this.firstCellX + n + 1);
        this.cellStartBlockX = (this.firstCellX + n) * this.cellWidth;
    }

    @Override
    public NoiseChunk forIndex(int n) {
        int n2 = Math.floorMod(n, this.cellWidth);
        int n3 = Math.floorDiv(n, this.cellWidth);
        int n4 = Math.floorMod(n3, this.cellWidth);
        int n5 = this.cellHeight - 1 - Math.floorDiv(n3, this.cellWidth);
        this.inCellX = n4;
        this.inCellY = n5;
        this.inCellZ = n2;
        this.arrayIndex = n;
        return this;
    }

    @Override
    public void fillAllDirectly(double[] dArray, DensityFunction densityFunction) {
        this.arrayIndex = 0;
        for (int i = this.cellHeight - 1; i >= 0; --i) {
            this.inCellY = i;
            for (int j = 0; j < this.cellWidth; ++j) {
                this.inCellX = j;
                int n = 0;
                while (n < this.cellWidth) {
                    this.inCellZ = n++;
                    dArray[this.arrayIndex++] = densityFunction.compute(this);
                }
            }
        }
    }

    public void selectCellYZ(int n, int n2) {
        this.interpolators.forEach(noiseInterpolator -> noiseInterpolator.selectCellYZ(n, n2));
        this.fillingCell = true;
        this.cellStartBlockY = (n + this.cellNoiseMinY) * this.cellHeight;
        this.cellStartBlockZ = (this.firstCellZ + n2) * this.cellWidth;
        ++this.arrayInterpolationCounter;
        for (CacheAllInCell cacheAllInCell : this.cellCaches) {
            cacheAllInCell.noiseFiller.fillArray(cacheAllInCell.values, this);
        }
        ++this.arrayInterpolationCounter;
        this.fillingCell = false;
    }

    public void updateForY(int n, double d) {
        this.inCellY = n - this.cellStartBlockY;
        this.interpolators.forEach(noiseInterpolator -> noiseInterpolator.updateForY(d));
    }

    public void updateForX(int n, double d) {
        this.inCellX = n - this.cellStartBlockX;
        this.interpolators.forEach(noiseInterpolator -> noiseInterpolator.updateForX(d));
    }

    public void updateForZ(int n, double d) {
        this.inCellZ = n - this.cellStartBlockZ;
        ++this.interpolationCounter;
        this.interpolators.forEach(noiseInterpolator -> noiseInterpolator.updateForZ(d));
    }

    public void stopInterpolation() {
        if (!this.interpolating) {
            throw new IllegalStateException("Staring interpolation twice");
        }
        this.interpolating = false;
    }

    public void swapSlices() {
        this.interpolators.forEach(NoiseInterpolator::swapSlices);
    }

    public Aquifer aquifer() {
        return this.aquifer;
    }

    Blender.BlendingOutput getOrComputeBlendingOutput(int n, int n2) {
        Blender.BlendingOutput blendingOutput;
        long l = ChunkPos.asLong(n, n2);
        if (this.lastBlendingDataPos == l) {
            return this.lastBlendingOutput;
        }
        this.lastBlendingDataPos = l;
        this.lastBlendingOutput = blendingOutput = this.blender.blendOffsetAndFactor(n, n2);
        return blendingOutput;
    }

    protected DensityFunction wrap(DensityFunction densityFunction) {
        return this.wrapped.computeIfAbsent(densityFunction, this::wrapNew);
    }

    private DensityFunction wrapNew(DensityFunction densityFunction) {
        if (densityFunction instanceof DensityFunctions.Marker) {
            DensityFunctions.Marker marker = (DensityFunctions.Marker)densityFunction;
            return switch (marker.type()) {
                default -> throw new IncompatibleClassChangeError();
                case DensityFunctions.Marker.Type.Interpolated -> new NoiseInterpolator(marker.wrapped());
                case DensityFunctions.Marker.Type.FlatCache -> new FlatCache(marker.wrapped(), true);
                case DensityFunctions.Marker.Type.Cache2D -> new Cache2D(marker.wrapped());
                case DensityFunctions.Marker.Type.CacheOnce -> new CacheOnce(marker.wrapped());
                case DensityFunctions.Marker.Type.CacheAllInCell -> new CacheAllInCell(marker.wrapped());
            };
        }
        if (this.blender != Blender.empty()) {
            if (densityFunction == DensityFunctions.BlendAlpha.INSTANCE) {
                return this.blendAlpha;
            }
            if (densityFunction == DensityFunctions.BlendOffset.INSTANCE) {
                return this.blendOffset;
            }
        }
        if (densityFunction == DensityFunctions.BeardifierMarker.INSTANCE) {
            return this.beardifier;
        }
        if (densityFunction instanceof DensityFunctions.HolderHolder) {
            DensityFunctions.HolderHolder holderHolder = (DensityFunctions.HolderHolder)densityFunction;
            return holderHolder.function().value();
        }
        return densityFunction;
    }

    @Override
    public /* synthetic */ DensityFunction.FunctionContext forIndex(int n) {
        return this.forIndex(n);
    }

    class FlatCache
    implements DensityFunctions.MarkerOrMarked,
    NoiseChunkDensityFunction {
        private final DensityFunction noiseFiller;
        final double[][] values;

        FlatCache(DensityFunction densityFunction, boolean bl) {
            this.noiseFiller = densityFunction;
            this.values = new double[NoiseChunk.this.noiseSizeXZ + 1][NoiseChunk.this.noiseSizeXZ + 1];
            if (bl) {
                for (int i = 0; i <= NoiseChunk.this.noiseSizeXZ; ++i) {
                    int n = NoiseChunk.this.firstNoiseX + i;
                    int n2 = QuartPos.toBlock(n);
                    for (int j = 0; j <= NoiseChunk.this.noiseSizeXZ; ++j) {
                        int n3 = NoiseChunk.this.firstNoiseZ + j;
                        int n4 = QuartPos.toBlock(n3);
                        this.values[i][j] = densityFunction.compute(new DensityFunction.SinglePointContext(n2, 0, n4));
                    }
                }
            }
        }

        @Override
        public double compute(DensityFunction.FunctionContext functionContext) {
            int n = QuartPos.fromBlock(functionContext.blockX());
            int n2 = QuartPos.fromBlock(functionContext.blockZ());
            int n3 = n - NoiseChunk.this.firstNoiseX;
            int n4 = n2 - NoiseChunk.this.firstNoiseZ;
            int n5 = this.values.length;
            if (n3 >= 0 && n4 >= 0 && n3 < n5 && n4 < n5) {
                return this.values[n3][n4];
            }
            return this.noiseFiller.compute(functionContext);
        }

        @Override
        public void fillArray(double[] dArray, DensityFunction.ContextProvider contextProvider) {
            contextProvider.fillAllDirectly(dArray, this);
        }

        @Override
        public DensityFunction wrapped() {
            return this.noiseFiller;
        }

        @Override
        public DensityFunctions.Marker.Type type() {
            return DensityFunctions.Marker.Type.FlatCache;
        }
    }

    class BlendAlpha
    implements NoiseChunkDensityFunction {
        BlendAlpha() {
        }

        @Override
        public DensityFunction wrapped() {
            return DensityFunctions.BlendAlpha.INSTANCE;
        }

        @Override
        public double compute(DensityFunction.FunctionContext functionContext) {
            return NoiseChunk.this.getOrComputeBlendingOutput(functionContext.blockX(), functionContext.blockZ()).alpha();
        }

        @Override
        public void fillArray(double[] dArray, DensityFunction.ContextProvider contextProvider) {
            contextProvider.fillAllDirectly(dArray, this);
        }

        @Override
        public double minValue() {
            return 0.0;
        }

        @Override
        public double maxValue() {
            return 1.0;
        }

        @Override
        public Codec<? extends DensityFunction> codec() {
            return DensityFunctions.BlendAlpha.CODEC;
        }
    }

    class BlendOffset
    implements NoiseChunkDensityFunction {
        BlendOffset() {
        }

        @Override
        public DensityFunction wrapped() {
            return DensityFunctions.BlendOffset.INSTANCE;
        }

        @Override
        public double compute(DensityFunction.FunctionContext functionContext) {
            return NoiseChunk.this.getOrComputeBlendingOutput(functionContext.blockX(), functionContext.blockZ()).blendingOffset();
        }

        @Override
        public void fillArray(double[] dArray, DensityFunction.ContextProvider contextProvider) {
            contextProvider.fillAllDirectly(dArray, this);
        }

        @Override
        public double minValue() {
            return Double.NEGATIVE_INFINITY;
        }

        @Override
        public double maxValue() {
            return Double.POSITIVE_INFINITY;
        }

        @Override
        public Codec<? extends DensityFunction> codec() {
            return DensityFunctions.BlendOffset.CODEC;
        }
    }

    @FunctionalInterface
    public static interface BlockStateFiller {
        @Nullable
        public BlockState calculate(DensityFunction.FunctionContext var1);
    }

    public class NoiseInterpolator
    implements DensityFunctions.MarkerOrMarked,
    NoiseChunkDensityFunction {
        double[][] slice0;
        double[][] slice1;
        private final DensityFunction noiseFiller;
        private double noise000;
        private double noise001;
        private double noise100;
        private double noise101;
        private double noise010;
        private double noise011;
        private double noise110;
        private double noise111;
        private double valueXZ00;
        private double valueXZ10;
        private double valueXZ01;
        private double valueXZ11;
        private double valueZ0;
        private double valueZ1;
        private double value;

        NoiseInterpolator(DensityFunction densityFunction) {
            this.noiseFiller = densityFunction;
            this.slice0 = this.allocateSlice(NoiseChunk.this.cellCountY, NoiseChunk.this.cellCountXZ);
            this.slice1 = this.allocateSlice(NoiseChunk.this.cellCountY, NoiseChunk.this.cellCountXZ);
            NoiseChunk.this.interpolators.add(this);
        }

        private double[][] allocateSlice(int n, int n2) {
            int n3 = n2 + 1;
            int n4 = n + 1;
            double[][] dArray = new double[n3][n4];
            for (int i = 0; i < n3; ++i) {
                dArray[i] = new double[n4];
            }
            return dArray;
        }

        void selectCellYZ(int n, int n2) {
            this.noise000 = this.slice0[n2][n];
            this.noise001 = this.slice0[n2 + 1][n];
            this.noise100 = this.slice1[n2][n];
            this.noise101 = this.slice1[n2 + 1][n];
            this.noise010 = this.slice0[n2][n + 1];
            this.noise011 = this.slice0[n2 + 1][n + 1];
            this.noise110 = this.slice1[n2][n + 1];
            this.noise111 = this.slice1[n2 + 1][n + 1];
        }

        void updateForY(double d) {
            this.valueXZ00 = Mth.lerp(d, this.noise000, this.noise010);
            this.valueXZ10 = Mth.lerp(d, this.noise100, this.noise110);
            this.valueXZ01 = Mth.lerp(d, this.noise001, this.noise011);
            this.valueXZ11 = Mth.lerp(d, this.noise101, this.noise111);
        }

        void updateForX(double d) {
            this.valueZ0 = Mth.lerp(d, this.valueXZ00, this.valueXZ10);
            this.valueZ1 = Mth.lerp(d, this.valueXZ01, this.valueXZ11);
        }

        void updateForZ(double d) {
            this.value = Mth.lerp(d, this.valueZ0, this.valueZ1);
        }

        @Override
        public double compute(DensityFunction.FunctionContext functionContext) {
            if (functionContext != NoiseChunk.this) {
                return this.noiseFiller.compute(functionContext);
            }
            if (!NoiseChunk.this.interpolating) {
                throw new IllegalStateException("Trying to sample interpolator outside the interpolation loop");
            }
            if (NoiseChunk.this.fillingCell) {
                return Mth.lerp3((double)NoiseChunk.this.inCellX / (double)NoiseChunk.this.cellWidth, (double)NoiseChunk.this.inCellY / (double)NoiseChunk.this.cellHeight, (double)NoiseChunk.this.inCellZ / (double)NoiseChunk.this.cellWidth, this.noise000, this.noise100, this.noise010, this.noise110, this.noise001, this.noise101, this.noise011, this.noise111);
            }
            return this.value;
        }

        @Override
        public void fillArray(double[] dArray, DensityFunction.ContextProvider contextProvider) {
            if (NoiseChunk.this.fillingCell) {
                contextProvider.fillAllDirectly(dArray, this);
                return;
            }
            this.wrapped().fillArray(dArray, contextProvider);
        }

        @Override
        public DensityFunction wrapped() {
            return this.noiseFiller;
        }

        private void swapSlices() {
            double[][] dArray = this.slice0;
            this.slice0 = this.slice1;
            this.slice1 = dArray;
        }

        @Override
        public DensityFunctions.Marker.Type type() {
            return DensityFunctions.Marker.Type.Interpolated;
        }
    }

    class CacheAllInCell
    implements DensityFunctions.MarkerOrMarked,
    NoiseChunkDensityFunction {
        final DensityFunction noiseFiller;
        final double[] values;

        CacheAllInCell(DensityFunction densityFunction) {
            this.noiseFiller = densityFunction;
            this.values = new double[NoiseChunk.this.cellWidth * NoiseChunk.this.cellWidth * NoiseChunk.this.cellHeight];
            NoiseChunk.this.cellCaches.add(this);
        }

        @Override
        public double compute(DensityFunction.FunctionContext functionContext) {
            if (functionContext != NoiseChunk.this) {
                return this.noiseFiller.compute(functionContext);
            }
            if (!NoiseChunk.this.interpolating) {
                throw new IllegalStateException("Trying to sample interpolator outside the interpolation loop");
            }
            int n = NoiseChunk.this.inCellX;
            int n2 = NoiseChunk.this.inCellY;
            int n3 = NoiseChunk.this.inCellZ;
            if (n >= 0 && n2 >= 0 && n3 >= 0 && n < NoiseChunk.this.cellWidth && n2 < NoiseChunk.this.cellHeight && n3 < NoiseChunk.this.cellWidth) {
                return this.values[((NoiseChunk.this.cellHeight - 1 - n2) * NoiseChunk.this.cellWidth + n) * NoiseChunk.this.cellWidth + n3];
            }
            return this.noiseFiller.compute(functionContext);
        }

        @Override
        public void fillArray(double[] dArray, DensityFunction.ContextProvider contextProvider) {
            contextProvider.fillAllDirectly(dArray, this);
        }

        @Override
        public DensityFunction wrapped() {
            return this.noiseFiller;
        }

        @Override
        public DensityFunctions.Marker.Type type() {
            return DensityFunctions.Marker.Type.CacheAllInCell;
        }
    }

    static class Cache2D
    implements DensityFunctions.MarkerOrMarked,
    NoiseChunkDensityFunction {
        private final DensityFunction function;
        private long lastPos2D = ChunkPos.INVALID_CHUNK_POS;
        private double lastValue;

        Cache2D(DensityFunction densityFunction) {
            this.function = densityFunction;
        }

        @Override
        public double compute(DensityFunction.FunctionContext functionContext) {
            double d;
            int n;
            int n2 = functionContext.blockX();
            long l = ChunkPos.asLong(n2, n = functionContext.blockZ());
            if (this.lastPos2D == l) {
                return this.lastValue;
            }
            this.lastPos2D = l;
            this.lastValue = d = this.function.compute(functionContext);
            return d;
        }

        @Override
        public void fillArray(double[] dArray, DensityFunction.ContextProvider contextProvider) {
            this.function.fillArray(dArray, contextProvider);
        }

        @Override
        public DensityFunction wrapped() {
            return this.function;
        }

        @Override
        public DensityFunctions.Marker.Type type() {
            return DensityFunctions.Marker.Type.Cache2D;
        }
    }

    class CacheOnce
    implements DensityFunctions.MarkerOrMarked,
    NoiseChunkDensityFunction {
        private final DensityFunction function;
        private long lastCounter;
        private long lastArrayCounter;
        private double lastValue;
        @Nullable
        private double[] lastArray;

        CacheOnce(DensityFunction densityFunction) {
            this.function = densityFunction;
        }

        @Override
        public double compute(DensityFunction.FunctionContext functionContext) {
            double d;
            if (functionContext != NoiseChunk.this) {
                return this.function.compute(functionContext);
            }
            if (this.lastArray != null && this.lastArrayCounter == NoiseChunk.this.arrayInterpolationCounter) {
                return this.lastArray[NoiseChunk.this.arrayIndex];
            }
            if (this.lastCounter == NoiseChunk.this.interpolationCounter) {
                return this.lastValue;
            }
            this.lastCounter = NoiseChunk.this.interpolationCounter;
            this.lastValue = d = this.function.compute(functionContext);
            return d;
        }

        @Override
        public void fillArray(double[] dArray, DensityFunction.ContextProvider contextProvider) {
            if (this.lastArray != null && this.lastArrayCounter == NoiseChunk.this.arrayInterpolationCounter) {
                System.arraycopy(this.lastArray, 0, dArray, 0, dArray.length);
                return;
            }
            this.wrapped().fillArray(dArray, contextProvider);
            if (this.lastArray != null && this.lastArray.length == dArray.length) {
                System.arraycopy(dArray, 0, this.lastArray, 0, dArray.length);
            } else {
                this.lastArray = (double[])dArray.clone();
            }
            this.lastArrayCounter = NoiseChunk.this.arrayInterpolationCounter;
        }

        @Override
        public DensityFunction wrapped() {
            return this.function;
        }

        @Override
        public DensityFunctions.Marker.Type type() {
            return DensityFunctions.Marker.Type.CacheOnce;
        }
    }

    static interface NoiseChunkDensityFunction
    extends DensityFunction {
        public DensityFunction wrapped();

        @Override
        default public DensityFunction mapAll(DensityFunction.Visitor visitor) {
            return this.wrapped().mapAll(visitor);
        }

        @Override
        default public double minValue() {
            return this.wrapped().minValue();
        }

        @Override
        default public double maxValue() {
            return this.wrapped().maxValue();
        }
    }
}

