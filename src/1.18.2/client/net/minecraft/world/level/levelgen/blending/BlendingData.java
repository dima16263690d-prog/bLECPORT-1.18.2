/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.primitives.Doubles
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.levelgen.blending;

import com.google.common.primitives.Doubles;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.DoubleStream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction8;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;

public class BlendingData {
    private static final double BLENDING_DENSITY_FACTOR = 0.1;
    protected static final LevelHeightAccessor AREA_WITH_OLD_GENERATION = new LevelHeightAccessor(){

        @Override
        public int getHeight() {
            return 256;
        }

        @Override
        public int getMinBuildHeight() {
            return 0;
        }
    };
    protected static final int CELL_WIDTH = 4;
    protected static final int CELL_HEIGHT = 8;
    protected static final int CELL_RATIO = 2;
    private static final int CELLS_PER_SECTION_Y = 2;
    private static final int QUARTS_PER_SECTION = QuartPos.fromBlock(16);
    private static final int CELL_HORIZONTAL_MAX_INDEX_INSIDE = QUARTS_PER_SECTION - 1;
    private static final int CELL_HORIZONTAL_MAX_INDEX_OUTSIDE = QUARTS_PER_SECTION;
    private static final int CELL_COLUMN_INSIDE_COUNT = 2 * CELL_HORIZONTAL_MAX_INDEX_INSIDE + 1;
    private static final int CELL_COLUMN_OUTSIDE_COUNT = 2 * CELL_HORIZONTAL_MAX_INDEX_OUTSIDE + 1;
    private static final int CELL_COLUMN_COUNT = CELL_COLUMN_INSIDE_COUNT + CELL_COLUMN_OUTSIDE_COUNT;
    private static final int CELL_HORIZONTAL_FLOOR_COUNT = QUARTS_PER_SECTION + 1;
    private static final List<Block> SURFACE_BLOCKS = List.of((Object[])new Block[]{Blocks.PODZOL, Blocks.GRAVEL, Blocks.GRASS_BLOCK, Blocks.STONE, Blocks.COARSE_DIRT, Blocks.SAND, Blocks.RED_SAND, Blocks.MYCELIUM, Blocks.SNOW_BLOCK, Blocks.TERRACOTTA, Blocks.DIRT});
    protected static final double NO_VALUE = Double.MAX_VALUE;
    private final boolean oldNoise;
    private boolean hasCalculatedData;
    private final double[] heights;
    private final List<Holder<Biome>> biomes;
    private final transient double[][] densities;
    private final transient double[] floorDensities;
    private static final Codec<double[]> DOUBLE_ARRAY_CODEC = Codec.DOUBLE.listOf().xmap(Doubles::toArray, Doubles::asList);
    public static final Codec<BlendingData> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.BOOL.fieldOf("old_noise").forGetter(BlendingData::oldNoise), (App)DOUBLE_ARRAY_CODEC.optionalFieldOf("heights").forGetter(blendingData -> DoubleStream.of(blendingData.heights).anyMatch(d -> d != Double.MAX_VALUE) ? Optional.of(blendingData.heights) : Optional.empty())).apply((Applicative)instance, BlendingData::new)).comapFlatMap(BlendingData::validateArraySize, Function.identity());

    private static DataResult<BlendingData> validateArraySize(BlendingData blendingData) {
        if (blendingData.heights.length != CELL_COLUMN_COUNT) {
            return DataResult.error((String)("heights has to be of length " + CELL_COLUMN_COUNT));
        }
        return DataResult.success((Object)blendingData);
    }

    private BlendingData(boolean bl, Optional<double[]> optional) {
        this.oldNoise = bl;
        this.heights = optional.orElse(Util.make(new double[CELL_COLUMN_COUNT], dArray -> Arrays.fill(dArray, Double.MAX_VALUE)));
        this.densities = new double[CELL_COLUMN_COUNT][];
        this.floorDensities = new double[CELL_HORIZONTAL_FLOOR_COUNT * CELL_HORIZONTAL_FLOOR_COUNT];
        ObjectArrayList objectArrayList = new ObjectArrayList(CELL_COLUMN_COUNT);
        objectArrayList.size(CELL_COLUMN_COUNT);
        this.biomes = objectArrayList;
    }

    public boolean oldNoise() {
        return this.oldNoise;
    }

    @Nullable
    public static BlendingData getOrUpdateBlendingData(WorldGenRegion worldGenRegion, int n, int n2) {
        ChunkAccess chunkAccess = worldGenRegion.getChunk(n, n2);
        BlendingData blendingData = chunkAccess.getBlendingData();
        if (blendingData == null || !blendingData.oldNoise()) {
            return null;
        }
        blendingData.calculateData(chunkAccess, BlendingData.sideByGenerationAge(worldGenRegion, n, n2, false));
        return blendingData;
    }

    public static Set<Direction8> sideByGenerationAge(WorldGenLevel worldGenLevel, int n, int n2, boolean bl) {
        EnumSet<Direction8> enumSet = EnumSet.noneOf(Direction8.class);
        for (Direction8 direction8 : Direction8.values()) {
            int n3 = n;
            int n4 = n2;
            for (Direction direction : direction8.getDirections()) {
                n3 += direction.getStepX();
                n4 += direction.getStepZ();
            }
            if (worldGenLevel.getChunk(n3, n4).isOldNoiseGeneration() != bl) continue;
            enumSet.add(direction8);
        }
        return enumSet;
    }

    private void calculateData(ChunkAccess chunkAccess, Set<Direction8> set) {
        int n;
        if (this.hasCalculatedData) {
            return;
        }
        Arrays.fill(this.floorDensities, 1.0);
        if (set.contains((Object)Direction8.NORTH) || set.contains((Object)Direction8.WEST) || set.contains((Object)Direction8.NORTH_WEST)) {
            this.addValuesForColumn(BlendingData.getInsideIndex(0, 0), chunkAccess, 0, 0);
        }
        if (set.contains((Object)Direction8.NORTH)) {
            for (n = 1; n < QUARTS_PER_SECTION; ++n) {
                this.addValuesForColumn(BlendingData.getInsideIndex(n, 0), chunkAccess, 4 * n, 0);
            }
        }
        if (set.contains((Object)Direction8.WEST)) {
            for (n = 1; n < QUARTS_PER_SECTION; ++n) {
                this.addValuesForColumn(BlendingData.getInsideIndex(0, n), chunkAccess, 0, 4 * n);
            }
        }
        if (set.contains((Object)Direction8.EAST)) {
            for (n = 1; n < QUARTS_PER_SECTION; ++n) {
                this.addValuesForColumn(BlendingData.getOutsideIndex(CELL_HORIZONTAL_MAX_INDEX_OUTSIDE, n), chunkAccess, 15, 4 * n);
            }
        }
        if (set.contains((Object)Direction8.SOUTH)) {
            for (n = 0; n < QUARTS_PER_SECTION; ++n) {
                this.addValuesForColumn(BlendingData.getOutsideIndex(n, CELL_HORIZONTAL_MAX_INDEX_OUTSIDE), chunkAccess, 4 * n, 15);
            }
        }
        if (set.contains((Object)Direction8.EAST) && set.contains((Object)Direction8.NORTH_EAST)) {
            this.addValuesForColumn(BlendingData.getOutsideIndex(CELL_HORIZONTAL_MAX_INDEX_OUTSIDE, 0), chunkAccess, 15, 0);
        }
        if (set.contains((Object)Direction8.EAST) && set.contains((Object)Direction8.SOUTH) && set.contains((Object)Direction8.SOUTH_EAST)) {
            this.addValuesForColumn(BlendingData.getOutsideIndex(CELL_HORIZONTAL_MAX_INDEX_OUTSIDE, CELL_HORIZONTAL_MAX_INDEX_OUTSIDE), chunkAccess, 15, 15);
        }
        this.hasCalculatedData = true;
    }

    private void addValuesForColumn(int n, ChunkAccess chunkAccess, int n2, int n3) {
        if (this.heights[n] == Double.MAX_VALUE) {
            this.heights[n] = BlendingData.getHeightAtXZ(chunkAccess, n2, n3);
        }
        this.densities[n] = BlendingData.getDensityColumn(chunkAccess, n2, n3, Mth.floor(this.heights[n]));
        this.biomes.set(n, chunkAccess.getNoiseBiome(QuartPos.fromBlock(n2), QuartPos.fromBlock(Mth.floor(this.heights[n])), QuartPos.fromBlock(n3)));
    }

    private static int getHeightAtXZ(ChunkAccess chunkAccess, int n, int n2) {
        int n3 = chunkAccess.hasPrimedHeightmap(Heightmap.Types.WORLD_SURFACE_WG) ? Math.min(chunkAccess.getHeight(Heightmap.Types.WORLD_SURFACE_WG, n, n2) + 1, AREA_WITH_OLD_GENERATION.getMaxBuildHeight()) : AREA_WITH_OLD_GENERATION.getMaxBuildHeight();
        int n4 = AREA_WITH_OLD_GENERATION.getMinBuildHeight();
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(n, n3, n2);
        while (mutableBlockPos.getY() > n4) {
            mutableBlockPos.move(Direction.DOWN);
            if (!SURFACE_BLOCKS.contains(chunkAccess.getBlockState(mutableBlockPos).getBlock())) continue;
            return mutableBlockPos.getY();
        }
        return n4;
    }

    private static double read1(ChunkAccess chunkAccess, BlockPos.MutableBlockPos mutableBlockPos) {
        return BlendingData.isGround(chunkAccess, mutableBlockPos.move(Direction.DOWN)) ? 1.0 : -1.0;
    }

    private static double read7(ChunkAccess chunkAccess, BlockPos.MutableBlockPos mutableBlockPos) {
        double d = 0.0;
        for (int i = 0; i < 7; ++i) {
            d += BlendingData.read1(chunkAccess, mutableBlockPos);
        }
        return d;
    }

    private static double[] getDensityColumn(ChunkAccess chunkAccess, int n, int n2, int n3) {
        double d;
        double d2;
        int n4;
        double[] dArray = new double[BlendingData.cellCountPerColumn()];
        Arrays.fill(dArray, -1.0);
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(n, AREA_WITH_OLD_GENERATION.getMaxBuildHeight(), n2);
        double d3 = BlendingData.read7(chunkAccess, mutableBlockPos);
        for (n4 = dArray.length - 2; n4 >= 0; --n4) {
            d2 = BlendingData.read1(chunkAccess, mutableBlockPos);
            d = BlendingData.read7(chunkAccess, mutableBlockPos);
            dArray[n4] = (d3 + d2 + d) / 15.0;
            d3 = d;
        }
        n4 = Mth.intFloorDiv(n3, 8);
        if (n4 >= 1 && n4 < dArray.length) {
            d2 = ((double)n3 + 0.5) % 8.0 / 8.0;
            d = (1.0 - d2) / d2;
            double d4 = Math.max(d, 1.0) * 0.25;
            dArray[n4] = -d / d4;
            dArray[n4 - 1] = 1.0 / d4;
        }
        return dArray;
    }

    private static boolean isGround(ChunkAccess chunkAccess, BlockPos blockPos) {
        BlockState blockState = chunkAccess.getBlockState(blockPos);
        if (blockState.isAir()) {
            return false;
        }
        if (blockState.is(BlockTags.LEAVES)) {
            return false;
        }
        if (blockState.is(BlockTags.LOGS)) {
            return false;
        }
        if (blockState.is(Blocks.BROWN_MUSHROOM_BLOCK) || blockState.is(Blocks.RED_MUSHROOM_BLOCK)) {
            return false;
        }
        return !blockState.getCollisionShape(chunkAccess, blockPos).isEmpty();
    }

    protected double getHeight(int n, int n2, int n3) {
        if (n == CELL_HORIZONTAL_MAX_INDEX_OUTSIDE || n3 == CELL_HORIZONTAL_MAX_INDEX_OUTSIDE) {
            return this.heights[BlendingData.getOutsideIndex(n, n3)];
        }
        if (n == 0 || n3 == 0) {
            return this.heights[BlendingData.getInsideIndex(n, n3)];
        }
        return Double.MAX_VALUE;
    }

    private static double getDensity(@Nullable double[] dArray, int n) {
        if (dArray == null) {
            return Double.MAX_VALUE;
        }
        int n2 = n - BlendingData.getColumnMinY();
        if (n2 < 0 || n2 >= dArray.length) {
            return Double.MAX_VALUE;
        }
        return dArray[n2] * 0.1;
    }

    protected double getDensity(int n, int n2, int n3) {
        if (n2 == BlendingData.getMinY()) {
            return this.floorDensities[this.getFloorIndex(n, n3)] * 0.1;
        }
        if (n == CELL_HORIZONTAL_MAX_INDEX_OUTSIDE || n3 == CELL_HORIZONTAL_MAX_INDEX_OUTSIDE) {
            return BlendingData.getDensity(this.densities[BlendingData.getOutsideIndex(n, n3)], n2);
        }
        if (n == 0 || n3 == 0) {
            return BlendingData.getDensity(this.densities[BlendingData.getInsideIndex(n, n3)], n2);
        }
        return Double.MAX_VALUE;
    }

    protected void iterateBiomes(int n, int n2, BiomeConsumer biomeConsumer) {
        for (int i = 0; i < this.biomes.size(); ++i) {
            Holder<Biome> holder = this.biomes.get(i);
            if (holder == null) continue;
            biomeConsumer.consume(n + BlendingData.getX(i), n2 + BlendingData.getZ(i), holder);
        }
    }

    protected void iterateHeights(int n, int n2, HeightConsumer heightConsumer) {
        for (int i = 0; i < this.heights.length; ++i) {
            double d = this.heights[i];
            if (d == Double.MAX_VALUE) continue;
            heightConsumer.consume(n + BlendingData.getX(i), n2 + BlendingData.getZ(i), d);
        }
    }

    protected void iterateDensities(int n, int n2, int n3, int n4, DensityConsumer densityConsumer) {
        int n5 = BlendingData.getColumnMinY();
        int n6 = Math.max(0, n3 - n5);
        int n7 = Math.min(BlendingData.cellCountPerColumn(), n4 - n5);
        for (int i = 0; i < this.densities.length; ++i) {
            double[] dArray = this.densities[i];
            if (dArray == null) continue;
            int n8 = n + BlendingData.getX(i);
            int n9 = n2 + BlendingData.getZ(i);
            for (int j = n6; j < n7; ++j) {
                densityConsumer.consume(n8, j + n5, n9, dArray[j] * 0.1);
            }
        }
    }

    private int getFloorIndex(int n, int n2) {
        return n * CELL_HORIZONTAL_FLOOR_COUNT + n2;
    }

    private static int cellCountPerColumn() {
        return AREA_WITH_OLD_GENERATION.getSectionsCount() * 2;
    }

    private static int getColumnMinY() {
        return BlendingData.getMinY() + 1;
    }

    private static int getMinY() {
        return AREA_WITH_OLD_GENERATION.getMinSection() * 2;
    }

    private static int getInsideIndex(int n, int n2) {
        return CELL_HORIZONTAL_MAX_INDEX_INSIDE - n + n2;
    }

    private static int getOutsideIndex(int n, int n2) {
        return CELL_COLUMN_INSIDE_COUNT + n + CELL_HORIZONTAL_MAX_INDEX_OUTSIDE - n2;
    }

    private static int getX(int n) {
        if (n < CELL_COLUMN_INSIDE_COUNT) {
            return BlendingData.zeroIfNegative(CELL_HORIZONTAL_MAX_INDEX_INSIDE - n);
        }
        int n2 = n - CELL_COLUMN_INSIDE_COUNT;
        return CELL_HORIZONTAL_MAX_INDEX_OUTSIDE - BlendingData.zeroIfNegative(CELL_HORIZONTAL_MAX_INDEX_OUTSIDE - n2);
    }

    private static int getZ(int n) {
        if (n < CELL_COLUMN_INSIDE_COUNT) {
            return BlendingData.zeroIfNegative(n - CELL_HORIZONTAL_MAX_INDEX_INSIDE);
        }
        int n2 = n - CELL_COLUMN_INSIDE_COUNT;
        return CELL_HORIZONTAL_MAX_INDEX_OUTSIDE - BlendingData.zeroIfNegative(n2 - CELL_HORIZONTAL_MAX_INDEX_OUTSIDE);
    }

    private static int zeroIfNegative(int n) {
        return n & ~(n >> 31);
    }

    protected static interface BiomeConsumer {
        public void consume(int var1, int var2, Holder<Biome> var3);
    }

    protected static interface HeightConsumer {
        public void consume(int var1, int var2, double var3);
    }

    protected static interface DensityConsumer {
        public void consume(int var1, int var2, int var3, double var4);
    }
}

