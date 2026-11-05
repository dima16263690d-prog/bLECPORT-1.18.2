/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.levelgen;

import com.google.common.collect.Sets;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.TerrainShaper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.Beardifier;
import net.minecraft.world.level.levelgen.BelowZeroRetrogen;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.SurfaceSystem;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public final class NoiseBasedChunkGenerator
extends ChunkGenerator {
    public static final Codec<NoiseBasedChunkGenerator> CODEC = RecordCodecBuilder.create(instance -> NoiseBasedChunkGenerator.commonCodec(instance).and(instance.group((App)RegistryOps.retrieveRegistry(Registry.NOISE_REGISTRY).forGetter(noiseBasedChunkGenerator -> noiseBasedChunkGenerator.noises), (App)BiomeSource.CODEC.fieldOf("biome_source").forGetter(noiseBasedChunkGenerator -> noiseBasedChunkGenerator.biomeSource), (App)Codec.LONG.fieldOf("seed").stable().forGetter(noiseBasedChunkGenerator -> noiseBasedChunkGenerator.seed), (App)NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(noiseBasedChunkGenerator -> noiseBasedChunkGenerator.settings))).apply((Applicative)instance, instance.stable(NoiseBasedChunkGenerator::new)));
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState[] EMPTY_COLUMN = new BlockState[0];
    protected final BlockState defaultBlock;
    private final Registry<NormalNoise.NoiseParameters> noises;
    private final long seed;
    protected final Holder<NoiseGeneratorSettings> settings;
    private final NoiseRouter router;
    private final Climate.Sampler sampler;
    private final SurfaceSystem surfaceSystem;
    private final Aquifer.FluidPicker globalFluidPicker;

    public NoiseBasedChunkGenerator(Registry<StructureSet> registry, Registry<NormalNoise.NoiseParameters> registry2, BiomeSource biomeSource, long l, Holder<NoiseGeneratorSettings> holder) {
        this(registry, registry2, biomeSource, biomeSource, l, holder);
    }

    private NoiseBasedChunkGenerator(Registry<StructureSet> registry, Registry<NormalNoise.NoiseParameters> registry2, BiomeSource biomeSource, BiomeSource biomeSource2, long l, Holder<NoiseGeneratorSettings> holder) {
        super(registry, Optional.empty(), biomeSource, biomeSource2, l);
        this.noises = registry2;
        this.seed = l;
        this.settings = holder;
        NoiseGeneratorSettings noiseGeneratorSettings = this.settings.value();
        this.defaultBlock = noiseGeneratorSettings.defaultBlock();
        NoiseSettings noiseSettings = noiseGeneratorSettings.noiseSettings();
        this.router = noiseGeneratorSettings.createNoiseRouter(registry2, l);
        this.sampler = new Climate.Sampler(this.router.temperature(), this.router.humidity(), this.router.continents(), this.router.erosion(), this.router.depth(), this.router.ridges(), this.router.spawnTarget());
        Aquifer.FluidStatus fluidStatus = new Aquifer.FluidStatus(-54, Blocks.LAVA.defaultBlockState());
        int n = noiseGeneratorSettings.seaLevel();
        Aquifer.FluidStatus fluidStatus2 = new Aquifer.FluidStatus(n, noiseGeneratorSettings.defaultFluid());
        Aquifer.FluidStatus fluidStatus3 = new Aquifer.FluidStatus(noiseSettings.minY() - 1, Blocks.AIR.defaultBlockState());
        this.globalFluidPicker = (n2, n3, n4) -> {
            if (n3 < Math.min(-54, n)) {
                return fluidStatus;
            }
            return fluidStatus2;
        };
        this.surfaceSystem = new SurfaceSystem(registry2, this.defaultBlock, n, l, noiseGeneratorSettings.getRandomSource());
    }

    @Override
    public CompletableFuture<ChunkAccess> createBiomes(Registry<Biome> registry, Executor executor, Blender blender, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess) {
        return CompletableFuture.supplyAsync(Util.wrapThreadWithTaskName("init_biomes", () -> {
            this.doCreateBiomes(blender, structureFeatureManager, chunkAccess);
            return chunkAccess;
        }), Util.backgroundExecutor());
    }

    private void doCreateBiomes(Blender blender, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess) {
        NoiseChunk noiseChunk = chunkAccess.getOrCreateNoiseChunk(this.router, () -> new Beardifier(structureFeatureManager, chunkAccess), this.settings.value(), this.globalFluidPicker, blender);
        BiomeResolver biomeResolver = BelowZeroRetrogen.getBiomeResolver(blender.getBiomeResolver(this.runtimeBiomeSource), chunkAccess);
        chunkAccess.fillBiomesFromNoise(biomeResolver, noiseChunk.cachedClimateSampler(this.router));
    }

    @VisibleForDebug
    public NoiseRouter router() {
        return this.router;
    }

    @Override
    public Climate.Sampler climateSampler() {
        return this.sampler;
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public ChunkGenerator withSeed(long l) {
        return new NoiseBasedChunkGenerator((Registry<StructureSet>)this.structureSets, this.noises, this.biomeSource.withSeed(l), l, this.settings);
    }

    public boolean stable(long l, ResourceKey<NoiseGeneratorSettings> resourceKey) {
        return this.seed == l && this.settings.is(resourceKey);
    }

    @Override
    public int getBaseHeight(int n, int n2, Heightmap.Types types, LevelHeightAccessor levelHeightAccessor) {
        NoiseSettings noiseSettings = this.settings.value().noiseSettings();
        int n3 = Math.max(noiseSettings.minY(), levelHeightAccessor.getMinBuildHeight());
        int n4 = Math.min(noiseSettings.minY() + noiseSettings.height(), levelHeightAccessor.getMaxBuildHeight());
        int n5 = Mth.intFloorDiv(n3, noiseSettings.getCellHeight());
        int n6 = Mth.intFloorDiv(n4 - n3, noiseSettings.getCellHeight());
        if (n6 <= 0) {
            return levelHeightAccessor.getMinBuildHeight();
        }
        return this.iterateNoiseColumn(n, n2, null, types.isOpaque(), n5, n6).orElse(levelHeightAccessor.getMinBuildHeight());
    }

    @Override
    public NoiseColumn getBaseColumn(int n, int n2, LevelHeightAccessor levelHeightAccessor) {
        NoiseSettings noiseSettings = this.settings.value().noiseSettings();
        int n3 = Math.max(noiseSettings.minY(), levelHeightAccessor.getMinBuildHeight());
        int n4 = Math.min(noiseSettings.minY() + noiseSettings.height(), levelHeightAccessor.getMaxBuildHeight());
        int n5 = Mth.intFloorDiv(n3, noiseSettings.getCellHeight());
        int n6 = Mth.intFloorDiv(n4 - n3, noiseSettings.getCellHeight());
        if (n6 <= 0) {
            return new NoiseColumn(n3, EMPTY_COLUMN);
        }
        BlockState[] blockStateArray = new BlockState[n6 * noiseSettings.getCellHeight()];
        this.iterateNoiseColumn(n, n2, blockStateArray, null, n5, n6);
        return new NoiseColumn(n3, blockStateArray);
    }

    @Override
    public void addDebugScreenInfo(List<String> list, BlockPos blockPos) {
        DecimalFormat decimalFormat = new DecimalFormat("0.000");
        DensityFunction.SinglePointContext singlePointContext = new DensityFunction.SinglePointContext(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        double d = this.router.ridges().compute(singlePointContext);
        list.add("NoiseRouter T: " + decimalFormat.format(this.router.temperature().compute(singlePointContext)) + " H: " + decimalFormat.format(this.router.humidity().compute(singlePointContext)) + " C: " + decimalFormat.format(this.router.continents().compute(singlePointContext)) + " E: " + decimalFormat.format(this.router.erosion().compute(singlePointContext)) + " D: " + decimalFormat.format(this.router.depth().compute(singlePointContext)) + " W: " + decimalFormat.format(d) + " PV: " + decimalFormat.format(TerrainShaper.peaksAndValleys((float)d)) + " AS: " + decimalFormat.format(this.router.initialDensityWithoutJaggedness().compute(singlePointContext)) + " N: " + decimalFormat.format(this.router.finalDensity().compute(singlePointContext)));
    }

    private OptionalInt iterateNoiseColumn(int n, int n2, @Nullable BlockState[] blockStateArray, @Nullable Predicate<BlockState> predicate, int n3, int n4) {
        NoiseSettings noiseSettings = this.settings.value().noiseSettings();
        int n5 = noiseSettings.getCellWidth();
        int n6 = noiseSettings.getCellHeight();
        int n7 = Math.floorDiv(n, n5);
        int n8 = Math.floorDiv(n2, n5);
        int n9 = Math.floorMod(n, n5);
        int n10 = Math.floorMod(n2, n5);
        int n11 = n7 * n5;
        int n12 = n8 * n5;
        double d = (double)n9 / (double)n5;
        double d2 = (double)n10 / (double)n5;
        NoiseChunk noiseChunk = NoiseChunk.forColumn(n11, n12, n3, n4, this.router, this.settings.value(), this.globalFluidPicker);
        noiseChunk.initializeForFirstCellX();
        noiseChunk.advanceCellX(0);
        for (int i = n4 - 1; i >= 0; --i) {
            noiseChunk.selectCellYZ(i, 0);
            for (int j = n6 - 1; j >= 0; --j) {
                BlockState blockState;
                int n13 = (n3 + i) * n6 + j;
                double d3 = (double)j / (double)n6;
                noiseChunk.updateForY(n13, d3);
                noiseChunk.updateForX(n, d);
                noiseChunk.updateForZ(n2, d2);
                BlockState blockState2 = noiseChunk.getInterpolatedState();
                BlockState blockState3 = blockState = blockState2 == null ? this.defaultBlock : blockState2;
                if (blockStateArray != null) {
                    int n14 = i * n6 + j;
                    blockStateArray[n14] = blockState;
                }
                if (predicate == null || !predicate.test(blockState)) continue;
                noiseChunk.stopInterpolation();
                return OptionalInt.of(n13 + 1);
            }
        }
        noiseChunk.stopInterpolation();
        return OptionalInt.empty();
    }

    @Override
    public void buildSurface(WorldGenRegion worldGenRegion, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess) {
        if (SharedConstants.debugVoidTerrain(chunkAccess.getPos())) {
            return;
        }
        WorldGenerationContext worldGenerationContext = new WorldGenerationContext(this, worldGenRegion);
        NoiseGeneratorSettings noiseGeneratorSettings = this.settings.value();
        NoiseChunk noiseChunk = chunkAccess.getOrCreateNoiseChunk(this.router, () -> new Beardifier(structureFeatureManager, chunkAccess), noiseGeneratorSettings, this.globalFluidPicker, Blender.of(worldGenRegion));
        this.surfaceSystem.buildSurface(worldGenRegion.getBiomeManager(), worldGenRegion.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY), noiseGeneratorSettings.useLegacyRandomSource(), worldGenerationContext, chunkAccess, noiseChunk, noiseGeneratorSettings.surfaceRule());
    }

    @Override
    public void applyCarvers(WorldGenRegion worldGenRegion, long l, BiomeManager biomeManager, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess, GenerationStep.Carving carving) {
        BiomeManager biomeManager2 = biomeManager.withDifferentSource((n, n2, n3) -> this.biomeSource.getNoiseBiome(n, n2, n3, this.climateSampler()));
        WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(RandomSupport.seedUniquifier()));
        int n4 = 8;
        ChunkPos chunkPos = chunkAccess.getPos();
        NoiseChunk noiseChunk = chunkAccess.getOrCreateNoiseChunk(this.router, () -> new Beardifier(structureFeatureManager, chunkAccess), this.settings.value(), this.globalFluidPicker, Blender.of(worldGenRegion));
        Aquifer aquifer = noiseChunk.aquifer();
        CarvingContext carvingContext = new CarvingContext(this, worldGenRegion.registryAccess(), chunkAccess.getHeightAccessorForGeneration(), noiseChunk);
        CarvingMask carvingMask = ((ProtoChunk)chunkAccess).getOrCreateCarvingMask(carving);
        for (int i = -8; i <= 8; ++i) {
            for (int j = -8; j <= 8; ++j) {
                ChunkPos chunkPos2 = new ChunkPos(chunkPos.x + i, chunkPos.z + j);
                ChunkAccess chunkAccess2 = worldGenRegion.getChunk(chunkPos2.x, chunkPos2.z);
                BiomeGenerationSettings biomeGenerationSettings = chunkAccess2.carverBiome(() -> this.biomeSource.getNoiseBiome(QuartPos.fromBlock(chunkPos2.getMinBlockX()), 0, QuartPos.fromBlock(chunkPos2.getMinBlockZ()), this.climateSampler())).value().getGenerationSettings();
                Iterable<Holder<ConfiguredWorldCarver<?>>> iterable = biomeGenerationSettings.getCarvers(carving);
                int n5 = 0;
                for (Holder<ConfiguredWorldCarver<?>> holder : iterable) {
                    ConfiguredWorldCarver<?> configuredWorldCarver = holder.value();
                    worldgenRandom.setLargeFeatureSeed(l + (long)n5, chunkPos2.x, chunkPos2.z);
                    if (configuredWorldCarver.isStartChunk(worldgenRandom)) {
                        configuredWorldCarver.carve(carvingContext, chunkAccess, biomeManager2::getBiome, worldgenRandom, aquifer, chunkPos2, carvingMask);
                    }
                    ++n5;
                }
            }
        }
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess2) {
        NoiseSettings noiseSettings = this.settings.value().noiseSettings();
        LevelHeightAccessor levelHeightAccessor = chunkAccess2.getHeightAccessorForGeneration();
        int n = Math.max(noiseSettings.minY(), levelHeightAccessor.getMinBuildHeight());
        int n2 = Math.min(noiseSettings.minY() + noiseSettings.height(), levelHeightAccessor.getMaxBuildHeight());
        int n3 = Mth.intFloorDiv(n, noiseSettings.getCellHeight());
        int n4 = Mth.intFloorDiv(n2 - n, noiseSettings.getCellHeight());
        if (n4 <= 0) {
            return CompletableFuture.completedFuture(chunkAccess2);
        }
        int n5 = chunkAccess2.getSectionIndex(n4 * noiseSettings.getCellHeight() - 1 + n);
        int n6 = chunkAccess2.getSectionIndex(n);
        HashSet hashSet = Sets.newHashSet();
        for (int i = n5; i >= n6; --i) {
            LevelChunkSection levelChunkSection = chunkAccess2.getSection(i);
            levelChunkSection.acquire();
            hashSet.add(levelChunkSection);
        }
        return CompletableFuture.supplyAsync(Util.wrapThreadWithTaskName("wgen_fill_noise", () -> this.doFill(blender, structureFeatureManager, chunkAccess2, n3, n4)), Util.backgroundExecutor()).whenCompleteAsync((chunkAccess, throwable) -> {
            for (LevelChunkSection levelChunkSection : hashSet) {
                levelChunkSection.release();
            }
        }, executor);
    }

    private ChunkAccess doFill(Blender blender, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess, int n, int n2) {
        NoiseGeneratorSettings noiseGeneratorSettings = this.settings.value();
        NoiseChunk noiseChunk = chunkAccess.getOrCreateNoiseChunk(this.router, () -> new Beardifier(structureFeatureManager, chunkAccess), noiseGeneratorSettings, this.globalFluidPicker, blender);
        Heightmap heightmap = chunkAccess.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap heightmap2 = chunkAccess.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        ChunkPos chunkPos = chunkAccess.getPos();
        int n3 = chunkPos.getMinBlockX();
        int n4 = chunkPos.getMinBlockZ();
        Aquifer aquifer = noiseChunk.aquifer();
        noiseChunk.initializeForFirstCellX();
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        NoiseSettings noiseSettings = noiseGeneratorSettings.noiseSettings();
        int n5 = noiseSettings.getCellWidth();
        int n6 = noiseSettings.getCellHeight();
        int n7 = 16 / n5;
        int n8 = 16 / n5;
        for (int i = 0; i < n7; ++i) {
            noiseChunk.advanceCellX(i);
            for (int j = 0; j < n8; ++j) {
                LevelChunkSection levelChunkSection = chunkAccess.getSection(chunkAccess.getSectionsCount() - 1);
                for (int k = n2 - 1; k >= 0; --k) {
                    noiseChunk.selectCellYZ(k, j);
                    for (int i2 = n6 - 1; i2 >= 0; --i2) {
                        int n9 = (n + k) * n6 + i2;
                        int n10 = n9 & 0xF;
                        int n11 = chunkAccess.getSectionIndex(n9);
                        if (chunkAccess.getSectionIndex(levelChunkSection.bottomBlockY()) != n11) {
                            levelChunkSection = chunkAccess.getSection(n11);
                        }
                        double d = (double)i2 / (double)n6;
                        noiseChunk.updateForY(n9, d);
                        for (int i3 = 0; i3 < n5; ++i3) {
                            int n12 = n3 + i * n5 + i3;
                            int n13 = n12 & 0xF;
                            double d2 = (double)i3 / (double)n5;
                            noiseChunk.updateForX(n12, d2);
                            for (int i4 = 0; i4 < n5; ++i4) {
                                int n14 = n4 + j * n5 + i4;
                                int n15 = n14 & 0xF;
                                double d3 = (double)i4 / (double)n5;
                                noiseChunk.updateForZ(n14, d3);
                                BlockState blockState = noiseChunk.getInterpolatedState();
                                if (blockState == null) {
                                    blockState = this.defaultBlock;
                                }
                                if ((blockState = this.debugPreliminarySurfaceLevel(noiseChunk, n12, n9, n14, blockState)) == AIR || SharedConstants.debugVoidTerrain(chunkAccess.getPos())) continue;
                                if (blockState.getLightEmission() != 0 && chunkAccess instanceof ProtoChunk) {
                                    mutableBlockPos.set(n12, n9, n14);
                                    ((ProtoChunk)chunkAccess).addLight(mutableBlockPos);
                                }
                                levelChunkSection.setBlockState(n13, n10, n15, blockState, false);
                                heightmap.update(n13, n9, n15, blockState);
                                heightmap2.update(n13, n9, n15, blockState);
                                if (!aquifer.shouldScheduleFluidUpdate() || blockState.getFluidState().isEmpty()) continue;
                                mutableBlockPos.set(n12, n9, n14);
                                chunkAccess.markPosForPostprocessing(mutableBlockPos);
                            }
                        }
                    }
                }
            }
            noiseChunk.swapSlices();
        }
        noiseChunk.stopInterpolation();
        return chunkAccess;
    }

    private BlockState debugPreliminarySurfaceLevel(NoiseChunk noiseChunk, int n, int n2, int n3, BlockState blockState) {
        return blockState;
    }

    @Override
    public int getGenDepth() {
        return this.settings.value().noiseSettings().height();
    }

    @Override
    public int getSeaLevel() {
        return this.settings.value().seaLevel();
    }

    @Override
    public int getMinY() {
        return this.settings.value().noiseSettings().minY();
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion worldGenRegion) {
        if (this.settings.value().disableMobGeneration()) {
            return;
        }
        ChunkPos chunkPos = worldGenRegion.getCenter();
        Holder<Biome> holder = worldGenRegion.getBiome(chunkPos.getWorldPosition().atY(worldGenRegion.getMaxBuildHeight() - 1));
        WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(RandomSupport.seedUniquifier()));
        worldgenRandom.setDecorationSeed(worldGenRegion.getSeed(), chunkPos.getMinBlockX(), chunkPos.getMinBlockZ());
        NaturalSpawner.spawnMobsForChunkGeneration(worldGenRegion, holder, chunkPos, worldgenRandom);
    }

    @Deprecated
    public Optional<BlockState> topMaterial(CarvingContext carvingContext, Function<BlockPos, Holder<Biome>> function, ChunkAccess chunkAccess, NoiseChunk noiseChunk, BlockPos blockPos, boolean bl) {
        return this.surfaceSystem.topMaterial(this.settings.value().surfaceRule(), carvingContext, function, chunkAccess, noiseChunk, blockPos, bl);
    }
}

