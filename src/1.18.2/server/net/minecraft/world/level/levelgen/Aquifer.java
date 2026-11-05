/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  org.apache.commons.lang3.mutable.MutableDouble
 */
package net.minecraft.world.level.levelgen;

import java.util.Arrays;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.RandomSource;
import org.apache.commons.lang3.mutable.MutableDouble;

public interface Aquifer {
    public static Aquifer create(NoiseChunk noiseChunk, ChunkPos chunkPos, DensityFunction densityFunction, DensityFunction densityFunction2, DensityFunction densityFunction3, DensityFunction densityFunction4, PositionalRandomFactory positionalRandomFactory, int n, int n2, FluidPicker fluidPicker) {
        return new NoiseBasedAquifer(noiseChunk, chunkPos, densityFunction, densityFunction2, densityFunction3, densityFunction4, positionalRandomFactory, n, n2, fluidPicker);
    }

    public static Aquifer createDisabled(final FluidPicker fluidPicker) {
        return new Aquifer(){

            @Override
            @Nullable
            public BlockState computeSubstance(DensityFunction.FunctionContext functionContext, double d) {
                if (d > 0.0) {
                    return null;
                }
                return fluidPicker.computeFluid(functionContext.blockX(), functionContext.blockY(), functionContext.blockZ()).at(functionContext.blockY());
            }

            @Override
            public boolean shouldScheduleFluidUpdate() {
                return false;
            }
        };
    }

    @Nullable
    public BlockState computeSubstance(DensityFunction.FunctionContext var1, double var2);

    public boolean shouldScheduleFluidUpdate();

    public static class NoiseBasedAquifer
    implements Aquifer {
        private static final int X_RANGE = 10;
        private static final int Y_RANGE = 9;
        private static final int Z_RANGE = 10;
        private static final int X_SEPARATION = 6;
        private static final int Y_SEPARATION = 3;
        private static final int Z_SEPARATION = 6;
        private static final int X_SPACING = 16;
        private static final int Y_SPACING = 12;
        private static final int Z_SPACING = 16;
        private static final int MAX_REASONABLE_DISTANCE_TO_AQUIFER_CENTER = 11;
        private static final double FLOWING_UPDATE_SIMULARITY = NoiseBasedAquifer.similarity(Mth.square(10), Mth.square(12));
        private final NoiseChunk noiseChunk;
        private final DensityFunction barrierNoise;
        private final DensityFunction fluidLevelFloodednessNoise;
        private final DensityFunction fluidLevelSpreadNoise;
        private final DensityFunction lavaNoise;
        private final PositionalRandomFactory positionalRandomFactory;
        private final FluidStatus[] aquiferCache;
        private final long[] aquiferLocationCache;
        private final FluidPicker globalFluidPicker;
        private boolean shouldScheduleFluidUpdate;
        private final int minGridX;
        private final int minGridY;
        private final int minGridZ;
        private final int gridSizeX;
        private final int gridSizeZ;
        private static final int[][] SURFACE_SAMPLING_OFFSETS_IN_CHUNKS = new int[][]{{-2, -1}, {-1, -1}, {0, -1}, {1, -1}, {-3, 0}, {-2, 0}, {-1, 0}, {0, 0}, {1, 0}, {-2, 1}, {-1, 1}, {0, 1}, {1, 1}};

        NoiseBasedAquifer(NoiseChunk noiseChunk, ChunkPos chunkPos, DensityFunction densityFunction, DensityFunction densityFunction2, DensityFunction densityFunction3, DensityFunction densityFunction4, PositionalRandomFactory positionalRandomFactory, int n, int n2, FluidPicker fluidPicker) {
            this.noiseChunk = noiseChunk;
            this.barrierNoise = densityFunction;
            this.fluidLevelFloodednessNoise = densityFunction2;
            this.fluidLevelSpreadNoise = densityFunction3;
            this.lavaNoise = densityFunction4;
            this.positionalRandomFactory = positionalRandomFactory;
            this.minGridX = this.gridX(chunkPos.getMinBlockX()) - 1;
            this.globalFluidPicker = fluidPicker;
            int n3 = this.gridX(chunkPos.getMaxBlockX()) + 1;
            this.gridSizeX = n3 - this.minGridX + 1;
            this.minGridY = this.gridY(n) - 1;
            int n4 = this.gridY(n + n2) + 1;
            int n5 = n4 - this.minGridY + 1;
            this.minGridZ = this.gridZ(chunkPos.getMinBlockZ()) - 1;
            int n6 = this.gridZ(chunkPos.getMaxBlockZ()) + 1;
            this.gridSizeZ = n6 - this.minGridZ + 1;
            int n7 = this.gridSizeX * n5 * this.gridSizeZ;
            this.aquiferCache = new FluidStatus[n7];
            this.aquiferLocationCache = new long[n7];
            Arrays.fill(this.aquiferLocationCache, Long.MAX_VALUE);
        }

        private int getIndex(int n, int n2, int n3) {
            int n4 = n - this.minGridX;
            int n5 = n2 - this.minGridY;
            int n6 = n3 - this.minGridZ;
            return (n5 * this.gridSizeZ + n6) * this.gridSizeX + n4;
        }

        @Override
        @Nullable
        public BlockState computeSubstance(DensityFunction.FunctionContext functionContext, double d) {
            double d2;
            double d3;
            BlockState blockState;
            int n = functionContext.blockX();
            int n2 = functionContext.blockY();
            int n3 = functionContext.blockZ();
            if (d > 0.0) {
                this.shouldScheduleFluidUpdate = false;
                return null;
            }
            FluidStatus fluidStatus = this.globalFluidPicker.computeFluid(n, n2, n3);
            if (fluidStatus.at(n2).is(Blocks.LAVA)) {
                this.shouldScheduleFluidUpdate = false;
                return Blocks.LAVA.defaultBlockState();
            }
            int n4 = Math.floorDiv(n - 5, 16);
            int n5 = Math.floorDiv(n2 + 1, 12);
            int n6 = Math.floorDiv(n3 - 5, 16);
            int n7 = Integer.MAX_VALUE;
            int n8 = Integer.MAX_VALUE;
            int n9 = Integer.MAX_VALUE;
            long l = 0L;
            long l2 = 0L;
            long l3 = 0L;
            for (int i = 0; i <= 1; ++i) {
                for (int j = -1; j <= 1; ++j) {
                    for (int k = 0; k <= 1; ++k) {
                        long l4;
                        int n10 = n4 + i;
                        int n11 = n5 + j;
                        int n12 = n6 + k;
                        int n13 = this.getIndex(n10, n11, n12);
                        long l5 = this.aquiferLocationCache[n13];
                        if (l5 != Long.MAX_VALUE) {
                            l4 = l5;
                        } else {
                            RandomSource randomSource = this.positionalRandomFactory.at(n10, n11, n12);
                            this.aquiferLocationCache[n13] = l4 = BlockPos.asLong(n10 * 16 + randomSource.nextInt(10), n11 * 12 + randomSource.nextInt(9), n12 * 16 + randomSource.nextInt(10));
                        }
                        int n14 = BlockPos.getX(l4) - n;
                        int n15 = BlockPos.getY(l4) - n2;
                        int n16 = BlockPos.getZ(l4) - n3;
                        int n17 = n14 * n14 + n15 * n15 + n16 * n16;
                        if (n7 >= n17) {
                            l3 = l2;
                            l2 = l;
                            l = l4;
                            n9 = n8;
                            n8 = n7;
                            n7 = n17;
                            continue;
                        }
                        if (n8 >= n17) {
                            l3 = l2;
                            l2 = l4;
                            n9 = n8;
                            n8 = n17;
                            continue;
                        }
                        if (n9 < n17) continue;
                        l3 = l4;
                        n9 = n17;
                    }
                }
            }
            FluidStatus fluidStatus2 = this.getAquiferStatus(l);
            double d4 = NoiseBasedAquifer.similarity(n7, n8);
            BlockState blockState2 = blockState = fluidStatus2.at(n2);
            if (d4 <= 0.0) {
                this.shouldScheduleFluidUpdate = d4 >= FLOWING_UPDATE_SIMULARITY;
                return blockState2;
            }
            if (blockState.is(Blocks.WATER) && this.globalFluidPicker.computeFluid(n, n2 - 1, n3).at(n2 - 1).is(Blocks.LAVA)) {
                this.shouldScheduleFluidUpdate = true;
                return blockState2;
            }
            MutableDouble mutableDouble = new MutableDouble(Double.NaN);
            FluidStatus fluidStatus3 = this.getAquiferStatus(l2);
            double d5 = d4 * this.calculatePressure(functionContext, mutableDouble, fluidStatus2, fluidStatus3);
            if (d + d5 > 0.0) {
                this.shouldScheduleFluidUpdate = false;
                return null;
            }
            FluidStatus fluidStatus4 = this.getAquiferStatus(l3);
            double d6 = NoiseBasedAquifer.similarity(n7, n9);
            if (d6 > 0.0 && d + (d3 = d4 * d6 * this.calculatePressure(functionContext, mutableDouble, fluidStatus2, fluidStatus4)) > 0.0) {
                this.shouldScheduleFluidUpdate = false;
                return null;
            }
            double d7 = NoiseBasedAquifer.similarity(n8, n9);
            if (d7 > 0.0 && d + (d2 = d4 * d7 * this.calculatePressure(functionContext, mutableDouble, fluidStatus3, fluidStatus4)) > 0.0) {
                this.shouldScheduleFluidUpdate = false;
                return null;
            }
            this.shouldScheduleFluidUpdate = true;
            return blockState2;
        }

        @Override
        public boolean shouldScheduleFluidUpdate() {
            return this.shouldScheduleFluidUpdate;
        }

        private static double similarity(int n, int n2) {
            double d = 25.0;
            return 1.0 - (double)Math.abs(n2 - n) / 25.0;
        }

        private double calculatePressure(DensityFunction.FunctionContext functionContext, MutableDouble mutableDouble, FluidStatus fluidStatus, FluidStatus fluidStatus2) {
            double d;
            double d2;
            int n = functionContext.blockY();
            BlockState blockState = fluidStatus.at(n);
            BlockState blockState2 = fluidStatus2.at(n);
            if (blockState.is(Blocks.LAVA) && blockState2.is(Blocks.WATER) || blockState.is(Blocks.WATER) && blockState2.is(Blocks.LAVA)) {
                return 2.0;
            }
            int n2 = Math.abs(fluidStatus.fluidLevel - fluidStatus2.fluidLevel);
            if (n2 == 0) {
                return 0.0;
            }
            double d3 = 0.5 * (double)(fluidStatus.fluidLevel + fluidStatus2.fluidLevel);
            double d4 = (double)n + 0.5 - d3;
            double d5 = (double)n2 / 2.0;
            double d6 = 0.0;
            double d7 = 2.5;
            double d8 = 1.5;
            double d9 = 3.0;
            double d10 = 10.0;
            double d11 = 3.0;
            double d12 = d5 - Math.abs(d4);
            double d13 = d4 > 0.0 ? ((d2 = 0.0 + d12) > 0.0 ? d2 / 1.5 : d2 / 2.5) : ((d2 = 3.0 + d12) > 0.0 ? d2 / 3.0 : d2 / 10.0);
            d2 = 2.0;
            if (d13 < -2.0 || d13 > 2.0) {
                d = 0.0;
            } else {
                double d14 = mutableDouble.getValue();
                if (Double.isNaN(d14)) {
                    double d15 = this.barrierNoise.compute(functionContext);
                    mutableDouble.setValue(d15);
                    d = d15;
                } else {
                    d = d14;
                }
            }
            return 2.0 * (d + d13);
        }

        private int gridX(int n) {
            return Math.floorDiv(n, 16);
        }

        private int gridY(int n) {
            return Math.floorDiv(n, 12);
        }

        private int gridZ(int n) {
            return Math.floorDiv(n, 16);
        }

        private FluidStatus getAquiferStatus(long l) {
            FluidStatus fluidStatus;
            int n;
            int n2;
            int n3 = BlockPos.getX(l);
            int n4 = BlockPos.getY(l);
            int n5 = BlockPos.getZ(l);
            int n6 = this.gridX(n3);
            int n7 = this.getIndex(n6, n2 = this.gridY(n4), n = this.gridZ(n5));
            FluidStatus fluidStatus2 = this.aquiferCache[n7];
            if (fluidStatus2 != null) {
                return fluidStatus2;
            }
            this.aquiferCache[n7] = fluidStatus = this.computeFluid(n3, n4, n5);
            return fluidStatus;
        }

        private FluidStatus computeFluid(int n, int n2, int n3) {
            double d;
            FluidStatus fluidStatus = this.globalFluidPicker.computeFluid(n, n2, n3);
            int n4 = Integer.MAX_VALUE;
            int n5 = n2 + 12;
            int n6 = n2 - 12;
            boolean bl = false;
            for (int[] nArray : SURFACE_SAMPLING_OFFSETS_IN_CHUNKS) {
                FluidStatus fluidStatus2;
                boolean bl2;
                boolean bl3;
                int n7 = n + SectionPos.sectionToBlockCoord(nArray[0]);
                int n8 = n3 + SectionPos.sectionToBlockCoord(nArray[1]);
                int n9 = this.noiseChunk.preliminarySurfaceLevel(n7, n8);
                int n10 = n9 + 8;
                boolean bl4 = bl3 = nArray[0] == 0 && nArray[1] == 0;
                if (bl3 && n6 > n10) {
                    return fluidStatus;
                }
                boolean bl5 = bl2 = n5 > n10;
                if ((bl2 || bl3) && !(fluidStatus2 = this.globalFluidPicker.computeFluid(n7, n10, n8)).at(n10).isAir()) {
                    if (bl3) {
                        bl = true;
                    }
                    if (bl2) {
                        return fluidStatus2;
                    }
                }
                n4 = Math.min(n4, n9);
            }
            int n11 = n4 + 8 - n2;
            int n12 = 64;
            double d2 = bl ? Mth.clampedMap((double)n11, 0.0, 64.0, 1.0, 0.0) : 0.0;
            double d3 = Mth.clamp(this.fluidLevelFloodednessNoise.compute(new DensityFunction.SinglePointContext(n, n2, n3)), -1.0, 1.0);
            if (d3 > (d = Mth.map(d2, 1.0, 0.0, -0.3, 0.8))) {
                return fluidStatus;
            }
            double d4 = Mth.map(d2, 1.0, 0.0, -0.8, 0.4);
            if (d3 <= d4) {
                return new FluidStatus(DimensionType.WAY_BELOW_MIN_Y, fluidStatus.fluidType);
            }
            int n13 = 16;
            int n14 = 40;
            int n15 = Math.floorDiv(n, 16);
            int n16 = Math.floorDiv(n2, 40);
            int n17 = Math.floorDiv(n3, 16);
            int n18 = n16 * 40 + 20;
            int n19 = 10;
            double d5 = this.fluidLevelSpreadNoise.compute(new DensityFunction.SinglePointContext(n15, n16, n17)) * 10.0;
            int n20 = Mth.quantize(d5, 3);
            int n21 = n18 + n20;
            int n22 = Math.min(n4, n21);
            if (n21 <= -10) {
                int n23;
                int n24;
                int n25 = 64;
                int n26 = 40;
                int n27 = Math.floorDiv(n, 64);
                double d6 = this.lavaNoise.compute(new DensityFunction.SinglePointContext(n27, n24 = Math.floorDiv(n2, 40), n23 = Math.floorDiv(n3, 64)));
                if (Math.abs(d6) > 0.3) {
                    return new FluidStatus(n22, Blocks.LAVA.defaultBlockState());
                }
            }
            return new FluidStatus(n22, fluidStatus.fluidType);
        }
    }

    public static interface FluidPicker {
        public FluidStatus computeFluid(int var1, int var2, int var3);
    }

    public static final class FluidStatus {
        final int fluidLevel;
        final BlockState fluidType;

        public FluidStatus(int n, BlockState blockState) {
            this.fluidLevel = n;
            this.fluidType = blockState;
        }

        public BlockState at(int n) {
            return n < this.fluidLevel ? this.fluidType : Blocks.AIR.defaultBlockState();
        }
    }
}

