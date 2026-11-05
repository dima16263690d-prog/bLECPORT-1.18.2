/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
 *  javax.annotation.Nullable
 *  org.apache.commons.lang3.mutable.MutableDouble
 *  org.apache.commons.lang3.mutable.MutableObject
 */
package net.minecraft.world.level.levelgen.blending;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction8;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.material.FluidState;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.apache.commons.lang3.mutable.MutableObject;

public class Blender {
    private static final Blender EMPTY = new Blender(new Long2ObjectOpenHashMap(), new Long2ObjectOpenHashMap()){

        @Override
        public BlendingOutput blendOffsetAndFactor(int n, int n2) {
            return new BlendingOutput(1.0, 0.0);
        }

        @Override
        public double blendDensity(DensityFunction.FunctionContext functionContext, double d) {
            return d;
        }

        @Override
        public BiomeResolver getBiomeResolver(BiomeResolver biomeResolver) {
            return biomeResolver;
        }
    };
    private static final NormalNoise SHIFT_NOISE = NormalNoise.create(new XoroshiroRandomSource(42L), BuiltinRegistries.NOISE.getOrThrow(Noises.SHIFT));
    private static final int HEIGHT_BLENDING_RANGE_CELLS = QuartPos.fromSection(7) - 1;
    private static final int HEIGHT_BLENDING_RANGE_CHUNKS = QuartPos.toSection(HEIGHT_BLENDING_RANGE_CELLS + 3);
    private static final int DENSITY_BLENDING_RANGE_CELLS = 2;
    private static final int DENSITY_BLENDING_RANGE_CHUNKS = QuartPos.toSection(5);
    private static final double OLD_CHUNK_Y_RADIUS = (double)BlendingData.AREA_WITH_OLD_GENERATION.getHeight() / 2.0;
    private static final double OLD_CHUNK_CENTER_Y = (double)BlendingData.AREA_WITH_OLD_GENERATION.getMinBuildHeight() + OLD_CHUNK_Y_RADIUS;
    private static final double OLD_CHUNK_XZ_RADIUS = 8.0;
    private final Long2ObjectOpenHashMap<BlendingData> blendingData;
    private final Long2ObjectOpenHashMap<BlendingData> blendingDataForDensityBlending;

    public static Blender empty() {
        return EMPTY;
    }

    public static Blender of(@Nullable WorldGenRegion worldGenRegion) {
        if (worldGenRegion == null) {
            return EMPTY;
        }
        Long2ObjectOpenHashMap long2ObjectOpenHashMap = new Long2ObjectOpenHashMap();
        Long2ObjectOpenHashMap long2ObjectOpenHashMap2 = new Long2ObjectOpenHashMap();
        ChunkPos chunkPos = worldGenRegion.getCenter();
        for (int i = -HEIGHT_BLENDING_RANGE_CHUNKS; i <= HEIGHT_BLENDING_RANGE_CHUNKS; ++i) {
            for (int j = -HEIGHT_BLENDING_RANGE_CHUNKS; j <= HEIGHT_BLENDING_RANGE_CHUNKS; ++j) {
                int n = chunkPos.x + i;
                int n2 = chunkPos.z + j;
                BlendingData blendingData = BlendingData.getOrUpdateBlendingData(worldGenRegion, n, n2);
                if (blendingData == null) continue;
                long2ObjectOpenHashMap.put(ChunkPos.asLong(n, n2), (Object)blendingData);
                if (i < -DENSITY_BLENDING_RANGE_CHUNKS || i > DENSITY_BLENDING_RANGE_CHUNKS || j < -DENSITY_BLENDING_RANGE_CHUNKS || j > DENSITY_BLENDING_RANGE_CHUNKS) continue;
                long2ObjectOpenHashMap2.put(ChunkPos.asLong(n, n2), (Object)blendingData);
            }
        }
        if (long2ObjectOpenHashMap.isEmpty() && long2ObjectOpenHashMap2.isEmpty()) {
            return EMPTY;
        }
        return new Blender((Long2ObjectOpenHashMap<BlendingData>)long2ObjectOpenHashMap, (Long2ObjectOpenHashMap<BlendingData>)long2ObjectOpenHashMap2);
    }

    Blender(Long2ObjectOpenHashMap<BlendingData> long2ObjectOpenHashMap, Long2ObjectOpenHashMap<BlendingData> long2ObjectOpenHashMap2) {
        this.blendingData = long2ObjectOpenHashMap;
        this.blendingDataForDensityBlending = long2ObjectOpenHashMap2;
    }

    public BlendingOutput blendOffsetAndFactor(int n, int n2) {
        int n3;
        int n4 = QuartPos.fromBlock(n);
        double d = this.getBlendingDataValue(n4, 0, n3 = QuartPos.fromBlock(n2), BlendingData::getHeight);
        if (d != Double.MAX_VALUE) {
            return new BlendingOutput(0.0, Blender.heightToOffset(d));
        }
        MutableDouble mutableDouble = new MutableDouble(0.0);
        MutableDouble mutableDouble2 = new MutableDouble(0.0);
        MutableDouble mutableDouble3 = new MutableDouble(Double.POSITIVE_INFINITY);
        this.blendingData.forEach((l, blendingData) -> blendingData.iterateHeights(QuartPos.fromSection(ChunkPos.getX(l)), QuartPos.fromSection(ChunkPos.getZ(l)), (n3, n4, d) -> {
            double d2 = Mth.length(n4 - n3, n3 - n4);
            if (d2 > (double)HEIGHT_BLENDING_RANGE_CELLS) {
                return;
            }
            if (d2 < mutableDouble3.doubleValue()) {
                mutableDouble3.setValue(d2);
            }
            double d3 = 1.0 / (d2 * d2 * d2 * d2);
            mutableDouble2.add(d * d3);
            mutableDouble.add(d3);
        }));
        if (mutableDouble3.doubleValue() == Double.POSITIVE_INFINITY) {
            return new BlendingOutput(1.0, 0.0);
        }
        double d2 = mutableDouble2.doubleValue() / mutableDouble.doubleValue();
        double d3 = Mth.clamp(mutableDouble3.doubleValue() / (double)(HEIGHT_BLENDING_RANGE_CELLS + 1), 0.0, 1.0);
        d3 = 3.0 * d3 * d3 - 2.0 * d3 * d3 * d3;
        return new BlendingOutput(d3, Blender.heightToOffset(d2));
    }

    private static double heightToOffset(double d) {
        double d2 = 1.0;
        double d3 = d + 0.5;
        double d4 = Mth.positiveModulo(d3, 8.0);
        return 1.0 * (32.0 * (d3 - 128.0) - 3.0 * (d3 - 120.0) * d4 + 3.0 * d4 * d4) / (128.0 * (32.0 - 3.0 * d4));
    }

    public double blendDensity(DensityFunction.FunctionContext functionContext, double d) {
        int n;
        int n2;
        int n3 = QuartPos.fromBlock(functionContext.blockX());
        double d2 = this.getBlendingDataValue(n3, n2 = functionContext.blockY() / 8, n = QuartPos.fromBlock(functionContext.blockZ()), BlendingData::getDensity);
        if (d2 != Double.MAX_VALUE) {
            return d2;
        }
        MutableDouble mutableDouble = new MutableDouble(0.0);
        MutableDouble mutableDouble2 = new MutableDouble(0.0);
        MutableDouble mutableDouble3 = new MutableDouble(Double.POSITIVE_INFINITY);
        this.blendingDataForDensityBlending.forEach((l, blendingData) -> blendingData.iterateDensities(QuartPos.fromSection(ChunkPos.getX(l)), QuartPos.fromSection(ChunkPos.getZ(l)), n2 - 1, n2 + 1, (n4, n5, n6, d) -> {
            double d2 = Mth.length(n3 - n4, (n2 - n5) * 2, n - n6);
            if (d2 > 2.0) {
                return;
            }
            if (d2 < mutableDouble3.doubleValue()) {
                mutableDouble3.setValue(d2);
            }
            double d3 = 1.0 / (d2 * d2 * d2 * d2);
            mutableDouble2.add(d * d3);
            mutableDouble.add(d3);
        }));
        if (mutableDouble3.doubleValue() == Double.POSITIVE_INFINITY) {
            return d;
        }
        double d3 = mutableDouble2.doubleValue() / mutableDouble.doubleValue();
        double d4 = Mth.clamp(mutableDouble3.doubleValue() / 3.0, 0.0, 1.0);
        return Mth.lerp(d4, d3, d);
    }

    private double getBlendingDataValue(int n, int n2, int n3, CellValueGetter cellValueGetter) {
        int n4 = QuartPos.toSection(n);
        int n5 = QuartPos.toSection(n3);
        boolean bl = (n & 3) == 0;
        boolean bl2 = (n3 & 3) == 0;
        double d = this.getBlendingDataValue(cellValueGetter, n4, n5, n, n2, n3);
        if (d == Double.MAX_VALUE) {
            if (bl && bl2) {
                d = this.getBlendingDataValue(cellValueGetter, n4 - 1, n5 - 1, n, n2, n3);
            }
            if (d == Double.MAX_VALUE) {
                if (bl) {
                    d = this.getBlendingDataValue(cellValueGetter, n4 - 1, n5, n, n2, n3);
                }
                if (d == Double.MAX_VALUE && bl2) {
                    d = this.getBlendingDataValue(cellValueGetter, n4, n5 - 1, n, n2, n3);
                }
            }
        }
        return d;
    }

    private double getBlendingDataValue(CellValueGetter cellValueGetter, int n, int n2, int n3, int n4, int n5) {
        BlendingData blendingData = (BlendingData)this.blendingData.get(ChunkPos.asLong(n, n2));
        if (blendingData != null) {
            return cellValueGetter.get(blendingData, n3 - QuartPos.fromSection(n), n4, n5 - QuartPos.fromSection(n2));
        }
        return Double.MAX_VALUE;
    }

    public BiomeResolver getBiomeResolver(BiomeResolver biomeResolver) {
        return (n, n2, n3, sampler) -> {
            Holder<Biome> holder = this.blendBiome(n, n3);
            if (holder == null) {
                return biomeResolver.getNoiseBiome(n, n2, n3, sampler);
            }
            return holder;
        };
    }

    @Nullable
    private Holder<Biome> blendBiome(int n, int n2) {
        double d = (double)n + SHIFT_NOISE.getValue(n, 0.0, n2) * 12.0;
        double d2 = (double)n2 + SHIFT_NOISE.getValue(n2, n, 0.0) * 12.0;
        MutableDouble mutableDouble = new MutableDouble(Double.POSITIVE_INFINITY);
        MutableObject mutableObject = new MutableObject();
        this.blendingData.forEach((l, blendingData) -> blendingData.iterateBiomes(QuartPos.fromSection(ChunkPos.getX(l)), QuartPos.fromSection(ChunkPos.getZ(l)), (n, n2, holder) -> {
            double d3 = Mth.length(d - (double)n, d2 - (double)n2);
            if (d3 > (double)HEIGHT_BLENDING_RANGE_CELLS) {
                return;
            }
            if (d3 < mutableDouble.doubleValue()) {
                mutableObject.setValue((Object)holder);
                mutableDouble.setValue(d3);
            }
        }));
        if (mutableDouble.doubleValue() == Double.POSITIVE_INFINITY) {
            return null;
        }
        double d3 = Mth.clamp(mutableDouble.doubleValue() / (double)(HEIGHT_BLENDING_RANGE_CELLS + 1), 0.0, 1.0);
        if (d3 > 0.5) {
            return null;
        }
        return (Holder)mutableObject.getValue();
    }

    public static void generateBorderTicks(WorldGenRegion worldGenRegion, ChunkAccess chunkAccess) {
        ChunkPos chunkPos = chunkAccess.getPos();
        boolean bl = chunkAccess.isOldNoiseGeneration();
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        BlockPos blockPos = new BlockPos(chunkPos.getMinBlockX(), 0, chunkPos.getMinBlockZ());
        int n = BlendingData.AREA_WITH_OLD_GENERATION.getMinBuildHeight();
        int n2 = BlendingData.AREA_WITH_OLD_GENERATION.getMaxBuildHeight() - 1;
        if (bl) {
            for (int i = 0; i < 16; ++i) {
                for (int j = 0; j < 16; ++j) {
                    Blender.generateBorderTick(chunkAccess, mutableBlockPos.setWithOffset(blockPos, i, n - 1, j));
                    Blender.generateBorderTick(chunkAccess, mutableBlockPos.setWithOffset(blockPos, i, n, j));
                    Blender.generateBorderTick(chunkAccess, mutableBlockPos.setWithOffset(blockPos, i, n2, j));
                    Blender.generateBorderTick(chunkAccess, mutableBlockPos.setWithOffset(blockPos, i, n2 + 1, j));
                }
            }
        }
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (worldGenRegion.getChunk(chunkPos.x + direction.getStepX(), chunkPos.z + direction.getStepZ()).isOldNoiseGeneration() == bl) continue;
            int n3 = direction == Direction.EAST ? 15 : 0;
            int n4 = direction == Direction.WEST ? 0 : 15;
            int n5 = direction == Direction.SOUTH ? 15 : 0;
            int n6 = direction == Direction.NORTH ? 0 : 15;
            for (int i = n3; i <= n4; ++i) {
                for (int j = n5; j <= n6; ++j) {
                    int n7 = Math.min(n2, chunkAccess.getHeight(Heightmap.Types.MOTION_BLOCKING, i, j)) + 1;
                    for (int k = n; k < n7; ++k) {
                        Blender.generateBorderTick(chunkAccess, mutableBlockPos.setWithOffset(blockPos, i, k, j));
                    }
                }
            }
        }
    }

    private static void generateBorderTick(ChunkAccess chunkAccess, BlockPos blockPos) {
        FluidState fluidState;
        BlockState blockState = chunkAccess.getBlockState(blockPos);
        if (blockState.is(BlockTags.LEAVES)) {
            chunkAccess.markPosForPostprocessing(blockPos);
        }
        if (!(fluidState = chunkAccess.getFluidState(blockPos)).isEmpty()) {
            chunkAccess.markPosForPostprocessing(blockPos);
        }
    }

    public static void addAroundOldChunksCarvingMaskFilter(WorldGenLevel worldGenLevel, ProtoChunk protoChunk) {
        ChunkPos chunkPos = protoChunk.getPos();
        DistanceGetter distanceGetter = Blender.makeOldChunkDistanceGetter(protoChunk.isOldNoiseGeneration(), BlendingData.sideByGenerationAge(worldGenLevel, chunkPos.x, chunkPos.z, true));
        if (distanceGetter == null) {
            return;
        }
        CarvingMask.Mask mask = (n, n2, n3) -> {
            double d;
            double d2;
            double d3 = (double)n + 0.5 + SHIFT_NOISE.getValue(n, n2, n3) * 4.0;
            return distanceGetter.getDistance(d3, d2 = (double)n2 + 0.5 + SHIFT_NOISE.getValue(n2, n3, n) * 4.0, d = (double)n3 + 0.5 + SHIFT_NOISE.getValue(n3, n, n2) * 4.0) < 4.0;
        };
        Stream.of(GenerationStep.Carving.values()).map(protoChunk::getOrCreateCarvingMask).forEach(carvingMask -> carvingMask.setAdditionalMask(mask));
    }

    @Nullable
    public static DistanceGetter makeOldChunkDistanceGetter(boolean bl, Set<Direction8> set) {
        if (!bl && set.isEmpty()) {
            return null;
        }
        ArrayList arrayList = Lists.newArrayList();
        if (bl) {
            arrayList.add(Blender.makeOffsetOldChunkDistanceGetter(null));
        }
        set.forEach(direction8 -> arrayList.add(Blender.makeOffsetOldChunkDistanceGetter(direction8)));
        return (d, d2, d3) -> {
            double d4 = Double.POSITIVE_INFINITY;
            for (DistanceGetter distanceGetter : arrayList) {
                double d5 = distanceGetter.getDistance(d, d2, d3);
                if (!(d5 < d4)) continue;
                d4 = d5;
            }
            return d4;
        };
    }

    private static DistanceGetter makeOffsetOldChunkDistanceGetter(@Nullable Direction8 direction8) {
        double d = 0.0;
        double d2 = 0.0;
        if (direction8 != null) {
            for (Direction direction : direction8.getDirections()) {
                d += (double)(direction.getStepX() * 16);
                d2 += (double)(direction.getStepZ() * 16);
            }
        }
        double d6 = d;
        double d7 = d2;
        return (d3, d4, d5) -> Blender.distanceToCube(d3 - 8.0 - d6, d4 - OLD_CHUNK_CENTER_Y, d5 - 8.0 - d7, 8.0, OLD_CHUNK_Y_RADIUS, 8.0);
    }

    private static double distanceToCube(double d, double d2, double d3, double d4, double d5, double d6) {
        double d7 = Math.abs(d) - d4;
        double d8 = Math.abs(d2) - d5;
        double d9 = Math.abs(d3) - d6;
        return Mth.length(Math.max(0.0, d7), Math.max(0.0, d8), Math.max(0.0, d9));
    }

    static interface CellValueGetter {
        public double get(BlendingData var1, int var2, int var3, int var4);
    }

    public record BlendingOutput(double alpha, double blendingOffset) {
    }

    public static interface DistanceGetter {
        public double getDistance(double var1, double var3, double var5);
    }
}

