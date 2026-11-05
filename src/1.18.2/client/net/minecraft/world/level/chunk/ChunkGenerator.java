/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Stopwatch
 *  com.google.common.base.Ticker
 *  com.mojang.datafixers.Products$P1
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder$Instance
 *  com.mojang.serialization.codecs.RecordCodecBuilder$Mu
 *  it.unimi.dsi.fastutil.ints.IntArraySet
 *  it.unimi.dsi.fastutil.ints.IntSet
 *  it.unimi.dsi.fastutil.longs.LongSet
 *  it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap
 *  it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.objects.ObjectArraySet
 *  javax.annotation.Nullable
 *  org.apache.commons.lang3.mutable.MutableBoolean
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.chunk;

import com.google.common.base.Stopwatch;
import com.google.common.base.Ticker;
import com.mojang.datafixers.Products;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureCheckResult;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.slf4j.Logger;

public abstract class ChunkGenerator
implements BiomeManager.NoiseBiomeSource {
    private static final Logger LOGGER;
    public static final Codec<ChunkGenerator> CODEC;
    protected final Registry<StructureSet> structureSets;
    protected final BiomeSource biomeSource;
    protected final BiomeSource runtimeBiomeSource;
    protected final Optional<HolderSet<StructureSet>> structureOverrides;
    private final Map<ConfiguredStructureFeature<?, ?>, List<StructurePlacement>> placementsForFeature = new Object2ObjectOpenHashMap();
    private final Map<ConcentricRingsStructurePlacement, CompletableFuture<List<ChunkPos>>> ringPositions = new Object2ObjectArrayMap();
    private boolean hasGeneratedPositions;
    @Deprecated
    private final long ringPlacementSeed;

    protected static final <T extends ChunkGenerator> Products.P1<RecordCodecBuilder.Mu<T>, Registry<StructureSet>> commonCodec(RecordCodecBuilder.Instance<T> instance) {
        return instance.group((App)RegistryOps.retrieveRegistry(Registry.STRUCTURE_SET_REGISTRY).forGetter(chunkGenerator -> chunkGenerator.structureSets));
    }

    public ChunkGenerator(Registry<StructureSet> registry, Optional<HolderSet<StructureSet>> optional, BiomeSource biomeSource) {
        this(registry, optional, biomeSource, biomeSource, 0L);
    }

    public ChunkGenerator(Registry<StructureSet> registry, Optional<HolderSet<StructureSet>> optional, BiomeSource biomeSource, BiomeSource biomeSource2, long l) {
        this.structureSets = registry;
        this.biomeSource = biomeSource;
        this.runtimeBiomeSource = biomeSource2;
        this.structureOverrides = optional;
        this.ringPlacementSeed = l;
    }

    public Stream<Holder<StructureSet>> possibleStructureSets() {
        if (this.structureOverrides.isPresent()) {
            return this.structureOverrides.get().stream();
        }
        return this.structureSets.holders().map(Holder::hackyErase);
    }

    private void generatePositions() {
        Set<Holder<Biome>> set = this.runtimeBiomeSource.possibleBiomes();
        this.possibleStructureSets().forEach(holder -> {
            StructureSet structureSet = (StructureSet)((Object)((Object)holder.value()));
            for (StructureSet.StructureSelectionEntry object2 : structureSet.structures()) {
                this.placementsForFeature.computeIfAbsent(object2.structure().value(), configuredStructureFeature -> new ArrayList()).add(structureSet.placement());
            }
            StructurePlacement structurePlacement = structureSet.placement();
            if (structurePlacement instanceof ConcentricRingsStructurePlacement) {
                ConcentricRingsStructurePlacement concentricRingsStructurePlacement = (ConcentricRingsStructurePlacement)structurePlacement;
                if (structureSet.structures().stream().anyMatch(structureSelectionEntry -> structureSelectionEntry.generatesInMatchingBiome(set::contains))) {
                    this.ringPositions.put(concentricRingsStructurePlacement, this.generateRingPositions((Holder<StructureSet>)holder, concentricRingsStructurePlacement));
                }
            }
        });
    }

    private CompletableFuture<List<ChunkPos>> generateRingPositions(Holder<StructureSet> holder, ConcentricRingsStructurePlacement concentricRingsStructurePlacement) {
        if (concentricRingsStructurePlacement.count() == 0) {
            return CompletableFuture.completedFuture(List.of());
        }
        return CompletableFuture.supplyAsync(Util.wrapThreadWithTaskName("placement calculation", () -> {
            Stopwatch stopwatch = Stopwatch.createStarted((Ticker)Util.TICKER);
            ArrayList<ChunkPos> arrayList = new ArrayList<ChunkPos>();
            Set set = ((StructureSet)((Object)((Object)holder.value()))).structures().stream().flatMap(structureSelectionEntry -> structureSelectionEntry.structure().value().biomes().stream()).collect(Collectors.toSet());
            int n = concentricRingsStructurePlacement.distance();
            int n2 = concentricRingsStructurePlacement.count();
            int n3 = concentricRingsStructurePlacement.spread();
            Random random = new Random();
            random.setSeed(this.ringPlacementSeed);
            double d = random.nextDouble() * Math.PI * 2.0;
            int n4 = 0;
            int n5 = 0;
            for (int i = 0; i < n2; ++i) {
                double d2 = (double)(4 * n + n * n5 * 6) + (random.nextDouble() - 0.5) * ((double)n * 2.5);
                int n6 = (int)Math.round(Math.cos(d) * d2);
                int n7 = (int)Math.round(Math.sin(d) * d2);
                Pair<BlockPos, Holder<Biome>> pair = this.biomeSource.findBiomeHorizontal(SectionPos.sectionToBlockCoord(n6, 8), 0, SectionPos.sectionToBlockCoord(n7, 8), 112, set::contains, random, this.climateSampler());
                if (pair != null) {
                    BlockPos blockPos = (BlockPos)pair.getFirst();
                    n6 = SectionPos.blockToSectionCoord(blockPos.getX());
                    n7 = SectionPos.blockToSectionCoord(blockPos.getZ());
                }
                arrayList.add(new ChunkPos(n6, n7));
                d += Math.PI * 2 / (double)n3;
                if (++n4 != n3) continue;
                n4 = 0;
                n3 += 2 * n3 / (++n5 + 1);
                n3 = Math.min(n3, n2 - i);
                d += random.nextDouble() * Math.PI * 2.0;
            }
            double d3 = (double)stopwatch.stop().elapsed(TimeUnit.MILLISECONDS) / 1000.0;
            LOGGER.debug("Calculation for {} took {}s", (Object)holder, (Object)d3);
            return arrayList;
        }), Util.backgroundExecutor());
    }

    protected abstract Codec<? extends ChunkGenerator> codec();

    public Optional<ResourceKey<Codec<? extends ChunkGenerator>>> getTypeNameForDataFixer() {
        return Registry.CHUNK_GENERATOR.getResourceKey(this.codec());
    }

    public abstract ChunkGenerator withSeed(long var1);

    public CompletableFuture<ChunkAccess> createBiomes(Registry<Biome> registry, Executor executor, Blender blender, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess) {
        return CompletableFuture.supplyAsync(Util.wrapThreadWithTaskName("init_biomes", () -> {
            chunkAccess.fillBiomesFromNoise(this.runtimeBiomeSource::getNoiseBiome, this.climateSampler());
            return chunkAccess;
        }), Util.backgroundExecutor());
    }

    public abstract Climate.Sampler climateSampler();

    @Override
    public Holder<Biome> getNoiseBiome(int n, int n2, int n3) {
        return this.getBiomeSource().getNoiseBiome(n, n2, n3, this.climateSampler());
    }

    public abstract void applyCarvers(WorldGenRegion var1, long var2, BiomeManager var4, StructureFeatureManager var5, ChunkAccess var6, GenerationStep.Carving var7);

    @Nullable
    public Pair<BlockPos, Holder<ConfiguredStructureFeature<?, ?>>> findNearestMapFeature(ServerLevel serverLevel, HolderSet<ConfiguredStructureFeature<?, ?>> holderSet, BlockPos blockPos, int n, boolean bl) {
        Object object;
        Set set = holderSet.stream().flatMap(holder -> ((ConfiguredStructureFeature)holder.value()).biomes().stream()).collect(Collectors.toSet());
        if (set.isEmpty()) {
            return null;
        }
        Set<Holder<Biome>> set2 = this.runtimeBiomeSource.possibleBiomes();
        if (Collections.disjoint(set2, set)) {
            return null;
        }
        Pair<BlockPos, Holder<ConfiguredStructureFeature<?, ?>>> pair = null;
        double d = Double.MAX_VALUE;
        Object2ObjectArrayMap object2ObjectArrayMap = new Object2ObjectArrayMap();
        for (Holder object2 : holderSet) {
            if (set2.stream().noneMatch(((ConfiguredStructureFeature)object2.value()).biomes()::contains)) continue;
            for (StructurePlacement structurePlacement2 : this.getPlacementsForFeature(object2)) {
                object2ObjectArrayMap.computeIfAbsent(structurePlacement2, structurePlacement -> new ObjectArraySet()).add(object2);
            }
        }
        ArrayList arrayList = new ArrayList(object2ObjectArrayMap.size());
        for (Map.Entry entry : object2ObjectArrayMap.entrySet()) {
            StructurePlacement structurePlacement2;
            structurePlacement2 = (StructurePlacement)entry.getKey();
            if (structurePlacement2 instanceof ConcentricRingsStructurePlacement) {
                ConcentricRingsStructurePlacement concentricRingsStructurePlacement = (ConcentricRingsStructurePlacement)structurePlacement2;
                object = this.getNearestGeneratedStructure(blockPos, concentricRingsStructurePlacement);
                double d2 = blockPos.distSqr((Vec3i)object);
                if (!(d2 < d)) continue;
                d = d2;
                pair = Pair.of((Object)object, (Object)((Holder)((Set)entry.getValue()).iterator().next()));
                continue;
            }
            if (!(structurePlacement2 instanceof RandomSpreadStructurePlacement)) continue;
            arrayList.add(entry);
        }
        if (!arrayList.isEmpty()) {
            int n2 = SectionPos.blockToSectionCoord(blockPos.getX());
            int n3 = SectionPos.blockToSectionCoord(blockPos.getZ());
            for (int i = 0; i <= n; ++i) {
                boolean bl2 = false;
                object = arrayList.iterator();
                while (object.hasNext()) {
                    Map.Entry entry = (Map.Entry)object.next();
                    RandomSpreadStructurePlacement randomSpreadStructurePlacement = (RandomSpreadStructurePlacement)entry.getKey();
                    Pair<BlockPos, Holder<ConfiguredStructureFeature<?, ?>>> pair2 = ChunkGenerator.getNearestGeneratedStructure((Set)entry.getValue(), serverLevel, serverLevel.structureFeatureManager(), n2, n3, i, bl, serverLevel.getSeed(), randomSpreadStructurePlacement);
                    if (pair2 == null) continue;
                    bl2 = true;
                    double d3 = blockPos.distSqr((Vec3i)pair2.getFirst());
                    if (!(d3 < d)) continue;
                    d = d3;
                    pair = pair2;
                }
                if (!bl2) continue;
                return pair;
            }
        }
        return pair;
    }

    @Nullable
    private BlockPos getNearestGeneratedStructure(BlockPos blockPos, ConcentricRingsStructurePlacement concentricRingsStructurePlacement) {
        List<ChunkPos> list = this.getRingPositionsFor(concentricRingsStructurePlacement);
        if (list == null) {
            throw new IllegalStateException("Somehow tried to find structures for a placement that doesn't exist");
        }
        BlockPos blockPos2 = null;
        double d = Double.MAX_VALUE;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (ChunkPos chunkPos : list) {
            mutableBlockPos.set(SectionPos.sectionToBlockCoord(chunkPos.x, 8), 32, SectionPos.sectionToBlockCoord(chunkPos.z, 8));
            double d2 = mutableBlockPos.distSqr(blockPos);
            if (blockPos2 == null) {
                blockPos2 = new BlockPos(mutableBlockPos);
                d = d2;
                continue;
            }
            if (!(d2 < d)) continue;
            blockPos2 = new BlockPos(mutableBlockPos);
            d = d2;
        }
        return blockPos2;
    }

    @Nullable
    private static Pair<BlockPos, Holder<ConfiguredStructureFeature<?, ?>>> getNearestGeneratedStructure(Set<Holder<ConfiguredStructureFeature<?, ?>>> set, LevelReader levelReader, StructureFeatureManager structureFeatureManager, int n, int n2, int n3, boolean bl, long l, RandomSpreadStructurePlacement randomSpreadStructurePlacement) {
        int n4 = randomSpreadStructurePlacement.spacing();
        for (int i = -n3; i <= n3; ++i) {
            boolean bl2 = i == -n3 || i == n3;
            for (int j = -n3; j <= n3; ++j) {
                boolean bl3;
                boolean bl4 = bl3 = j == -n3 || j == n3;
                if (!bl2 && !bl3) continue;
                int n5 = n + n4 * i;
                int n6 = n2 + n4 * j;
                ChunkPos chunkPos = randomSpreadStructurePlacement.getPotentialFeatureChunk(l, n5, n6);
                for (Holder<ConfiguredStructureFeature<?, ?>> holder : set) {
                    StructureCheckResult structureCheckResult = structureFeatureManager.checkStructurePresence(chunkPos, holder.value(), bl);
                    if (structureCheckResult == StructureCheckResult.START_NOT_PRESENT) continue;
                    if (!bl && structureCheckResult == StructureCheckResult.START_PRESENT) {
                        return Pair.of((Object)StructureFeature.getLocatePos(randomSpreadStructurePlacement, chunkPos), holder);
                    }
                    ChunkAccess chunkAccess = levelReader.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.STRUCTURE_STARTS);
                    StructureStart structureStart = structureFeatureManager.getStartForFeature(SectionPos.bottomOf(chunkAccess), holder.value(), chunkAccess);
                    if (structureStart == null || !structureStart.isValid()) continue;
                    if (bl && structureStart.canBeReferenced()) {
                        structureFeatureManager.addReference(structureStart);
                        return Pair.of((Object)StructureFeature.getLocatePos(randomSpreadStructurePlacement, structureStart.getChunkPos()), holder);
                    }
                    if (bl) continue;
                    return Pair.of((Object)StructureFeature.getLocatePos(randomSpreadStructurePlacement, structureStart.getChunkPos()), holder);
                }
            }
        }
        return null;
    }

    public void applyBiomeDecoration(WorldGenLevel worldGenLevel, ChunkAccess chunkAccess, StructureFeatureManager structureFeatureManager) {
        ChunkPos chunkPos = chunkAccess.getPos();
        if (SharedConstants.debugVoidTerrain(chunkPos)) {
            return;
        }
        SectionPos sectionPos = SectionPos.of(chunkPos, worldGenLevel.getMinSection());
        BlockPos blockPos = sectionPos.origin();
        Registry<ConfiguredStructureFeature<?, ?>> registry = worldGenLevel.registryAccess().registryOrThrow(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY);
        Map<Integer, List<ConfiguredStructureFeature>> map = registry.stream().collect(Collectors.groupingBy(configuredStructureFeature -> ((StructureFeature)configuredStructureFeature.feature).step().ordinal()));
        List<BiomeSource.StepFeatureData> list = this.biomeSource.featuresPerStep();
        WorldgenRandom worldgenRandom = new WorldgenRandom(new XoroshiroRandomSource(RandomSupport.seedUniquifier()));
        long l = worldgenRandom.setDecorationSeed(worldGenLevel.getSeed(), blockPos.getX(), blockPos.getZ());
        ObjectArraySet objectArraySet = new ObjectArraySet();
        if (this instanceof FlatLevelSource) {
            this.biomeSource.possibleBiomes().stream().map(Holder::value).forEach(((Set)objectArraySet)::add);
        } else {
            ChunkPos.rangeClosed(sectionPos.chunk(), 1).forEach(arg_0 -> ChunkGenerator.lambda$applyBiomeDecoration$11(worldGenLevel, (Set)objectArraySet, arg_0));
            objectArraySet.retainAll(this.biomeSource.possibleBiomes().stream().map(Holder::value).collect(Collectors.toSet()));
        }
        int n = list.size();
        try {
            Registry<PlacedFeature> registry2 = worldGenLevel.registryAccess().registryOrThrow(Registry.PLACED_FEATURE_REGISTRY);
            int n2 = Math.max(GenerationStep.Decoration.values().length, n);
            for (int i = 0; i < n2; ++i) {
                Object object;
                Object object22;
                IntArraySet intArraySet;
                int n3 = 0;
                if (structureFeatureManager.shouldGenerateFeatures()) {
                    intArraySet = map.getOrDefault(i, Collections.emptyList());
                    for (Object object22 : intArraySet) {
                        worldgenRandom.setFeatureSeed(l, n3, i);
                        object = () -> ChunkGenerator.lambda$applyBiomeDecoration$12(registry, (ConfiguredStructureFeature)object22);
                        try {
                            worldGenLevel.setCurrentlyGenerating((Supplier<String>)object);
                            structureFeatureManager.startsForFeature(sectionPos, (ConfiguredStructureFeature<?, ?>)object22).forEach(structureStart -> structureStart.placeInChunk(worldGenLevel, structureFeatureManager, this, worldgenRandom, ChunkGenerator.getWritableArea(chunkAccess), chunkPos));
                        }
                        catch (Exception exception) {
                            CrashReport crashReport = CrashReport.forThrowable(exception, "Feature placement");
                            crashReport.addCategory("Feature").setDetail("Description", ((Supplier)object)::get);
                            throw new ReportedException(crashReport);
                        }
                        ++n3;
                    }
                }
                if (i >= n) continue;
                intArraySet = new IntArraySet();
                for (Object object22 : objectArraySet) {
                    object = ((Biome)object22).getGenerationSettings().features();
                    if (i >= object.size()) continue;
                    HolderSet holderSet = (HolderSet)object.get(i);
                    BiomeSource.StepFeatureData stepFeatureData = list.get(i);
                    holderSet.stream().map(Holder::value).forEach(arg_0 -> ChunkGenerator.lambda$applyBiomeDecoration$14((IntSet)intArraySet, stepFeatureData, arg_0));
                }
                int n4 = intArraySet.size();
                object22 = intArraySet.toIntArray();
                Arrays.sort((int[])object22);
                object = list.get(i);
                for (int j = 0; j < n4; ++j) {
                    Object object3 = object22[j];
                    PlacedFeature placedFeature = ((BiomeSource.StepFeatureData)((Object)object)).features().get((int)object3);
                    Supplier<String> supplier = () -> registry2.getResourceKey(placedFeature).map(Object::toString).orElseGet(placedFeature::toString);
                    worldgenRandom.setFeatureSeed(l, (int)object3, i);
                    try {
                        worldGenLevel.setCurrentlyGenerating(supplier);
                        placedFeature.placeWithBiomeCheck(worldGenLevel, this, worldgenRandom, blockPos);
                        continue;
                    }
                    catch (Exception exception) {
                        CrashReport crashReport = CrashReport.forThrowable(exception, "Feature placement");
                        crashReport.addCategory("Feature").setDetail("Description", supplier::get);
                        throw new ReportedException(crashReport);
                    }
                }
            }
            worldGenLevel.setCurrentlyGenerating(null);
        }
        catch (Exception exception) {
            CrashReport crashReport = CrashReport.forThrowable(exception, "Biome decoration");
            crashReport.addCategory("Generation").setDetail("CenterX", chunkPos.x).setDetail("CenterZ", chunkPos.z).setDetail("Seed", l);
            throw new ReportedException(crashReport);
        }
    }

    public boolean hasFeatureChunkInRange(ResourceKey<StructureSet> resourceKey, long l, int n, int n2, int n3) {
        StructureSet structureSet = this.structureSets.get(resourceKey);
        if (structureSet == null) {
            return false;
        }
        StructurePlacement structurePlacement = structureSet.placement();
        for (int i = n - n3; i <= n + n3; ++i) {
            for (int j = n2 - n3; j <= n2 + n3; ++j) {
                if (!structurePlacement.isFeatureChunk(this, l, i, j)) continue;
                return true;
            }
        }
        return false;
    }

    private static BoundingBox getWritableArea(ChunkAccess chunkAccess) {
        ChunkPos chunkPos = chunkAccess.getPos();
        int n = chunkPos.getMinBlockX();
        int n2 = chunkPos.getMinBlockZ();
        LevelHeightAccessor levelHeightAccessor = chunkAccess.getHeightAccessorForGeneration();
        int n3 = levelHeightAccessor.getMinBuildHeight() + 1;
        int n4 = levelHeightAccessor.getMaxBuildHeight() - 1;
        return new BoundingBox(n, n3, n2, n + 15, n4, n2 + 15);
    }

    public abstract void buildSurface(WorldGenRegion var1, StructureFeatureManager var2, ChunkAccess var3);

    public abstract void spawnOriginalMobs(WorldGenRegion var1);

    public int getSpawnHeight(LevelHeightAccessor levelHeightAccessor) {
        return 64;
    }

    public BiomeSource getBiomeSource() {
        return this.runtimeBiomeSource;
    }

    public abstract int getGenDepth();

    public WeightedRandomList<MobSpawnSettings.SpawnerData> getMobsAt(Holder<Biome> holder, StructureFeatureManager structureFeatureManager, MobCategory mobCategory, BlockPos blockPos) {
        Map<ConfiguredStructureFeature<?, ?>, LongSet> map = structureFeatureManager.getAllStructuresAt(blockPos);
        for (Map.Entry<ConfiguredStructureFeature<?, ?>, LongSet> entry : map.entrySet()) {
            ConfiguredStructureFeature<?, ?> configuredStructureFeature = entry.getKey();
            StructureSpawnOverride structureSpawnOverride = configuredStructureFeature.spawnOverrides.get(mobCategory);
            if (structureSpawnOverride == null) continue;
            MutableBoolean mutableBoolean = new MutableBoolean(false);
            Predicate<StructureStart> predicate = structureSpawnOverride.boundingBox() == StructureSpawnOverride.BoundingBoxType.PIECE ? structureStart -> structureFeatureManager.structureHasPieceAt(blockPos, (StructureStart)structureStart) : structureStart -> structureStart.getBoundingBox().isInside(blockPos);
            structureFeatureManager.fillStartsForFeature(configuredStructureFeature, entry.getValue(), structureStart -> {
                if (mutableBoolean.isFalse() && predicate.test((StructureStart)structureStart)) {
                    mutableBoolean.setTrue();
                }
            });
            if (!mutableBoolean.isTrue()) continue;
            return structureSpawnOverride.spawns();
        }
        return holder.value().getMobSettings().getMobs(mobCategory);
    }

    public static Stream<ConfiguredStructureFeature<?, ?>> allConfigurations(Registry<ConfiguredStructureFeature<?, ?>> registry, StructureFeature<?> structureFeature) {
        return registry.stream().filter(configuredStructureFeature -> configuredStructureFeature.feature == structureFeature);
    }

    public void createStructures(RegistryAccess registryAccess, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess, StructureManager structureManager, long l) {
        ChunkPos chunkPos = chunkAccess.getPos();
        SectionPos sectionPos = SectionPos.bottomOf(chunkAccess);
        this.possibleStructureSets().forEach(holder -> {
            Object object2;
            StructurePlacement structurePlacement = ((StructureSet)((Object)((Object)holder.value()))).placement();
            List<StructureSet.StructureSelectionEntry> list = ((StructureSet)((Object)((Object)holder.value()))).structures();
            for (Object object2 : list) {
                StructureStart structureStart = structureFeatureManager.getStartForFeature(sectionPos, object2.structure().value(), chunkAccess);
                if (structureStart == null || !structureStart.isValid()) continue;
                return;
            }
            if (!structurePlacement.isFeatureChunk(this, l, chunkPos.x, chunkPos.z)) {
                return;
            }
            if (list.size() == 1) {
                this.tryGenerateStructure(list.get(0), structureFeatureManager, registryAccess, structureManager, l, chunkAccess, chunkPos, sectionPos);
                return;
            }
            ArrayList arrayList = new ArrayList(list.size());
            arrayList.addAll(list);
            object2 = new WorldgenRandom(new LegacyRandomSource(0L));
            ((WorldgenRandom)object2).setLargeFeatureSeed(l, chunkPos.x, chunkPos.z);
            int n = 0;
            Iterator iterator = arrayList.iterator();
            while (iterator.hasNext()) {
                StructureSet.StructureSelectionEntry structureSelectionEntry = (StructureSet.StructureSelectionEntry)((Object)((Object)iterator.next()));
                n += structureSelectionEntry.weight();
            }
            while (!arrayList.isEmpty()) {
                StructureSet.StructureSelectionEntry structureSelectionEntry;
                int n2 = ((Random)object2).nextInt(n);
                int n3 = 0;
                Object object3 = arrayList.iterator();
                while (object3.hasNext() && (n2 -= (structureSelectionEntry = (StructureSet.StructureSelectionEntry)((Object)((Object)object3.next()))).weight()) >= 0) {
                    ++n3;
                }
                object3 = (StructureSet.StructureSelectionEntry)((Object)((Object)arrayList.get(n3)));
                if (this.tryGenerateStructure((StructureSet.StructureSelectionEntry)((Object)((Object)object3)), structureFeatureManager, registryAccess, structureManager, l, chunkAccess, chunkPos, sectionPos)) {
                    return;
                }
                arrayList.remove(n3);
                n -= ((StructureSet.StructureSelectionEntry)((Object)((Object)object3))).weight();
            }
        });
    }

    private boolean tryGenerateStructure(StructureSet.StructureSelectionEntry structureSelectionEntry, StructureFeatureManager structureFeatureManager, RegistryAccess registryAccess, StructureManager structureManager, long l, ChunkAccess chunkAccess, ChunkPos chunkPos, SectionPos sectionPos) {
        HolderSet<Biome> holderSet;
        Predicate<Holder<Biome>> predicate;
        int n;
        ConfiguredStructureFeature<?, ?> configuredStructureFeature = structureSelectionEntry.structure().value();
        StructureStart structureStart = configuredStructureFeature.generate(registryAccess, this, this.biomeSource, structureManager, l, chunkPos, n = ChunkGenerator.fetchReferences(structureFeatureManager, chunkAccess, sectionPos, configuredStructureFeature), chunkAccess, predicate = arg_0 -> this.lambda$tryGenerateStructure$21(holderSet = configuredStructureFeature.biomes(), arg_0));
        if (structureStart.isValid()) {
            structureFeatureManager.setStartForFeature(sectionPos, configuredStructureFeature, structureStart, chunkAccess);
            return true;
        }
        return false;
    }

    private static int fetchReferences(StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess, SectionPos sectionPos, ConfiguredStructureFeature<?, ?> configuredStructureFeature) {
        StructureStart structureStart = structureFeatureManager.getStartForFeature(sectionPos, configuredStructureFeature, chunkAccess);
        return structureStart != null ? structureStart.getReferences() : 0;
    }

    protected Holder<Biome> adjustBiome(Holder<Biome> holder) {
        return holder;
    }

    public void createReferences(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess) {
        int n = 8;
        ChunkPos chunkPos = chunkAccess.getPos();
        int n2 = chunkPos.x;
        int n3 = chunkPos.z;
        int n4 = chunkPos.getMinBlockX();
        int n5 = chunkPos.getMinBlockZ();
        SectionPos sectionPos = SectionPos.bottomOf(chunkAccess);
        for (int i = n2 - 8; i <= n2 + 8; ++i) {
            for (int j = n3 - 8; j <= n3 + 8; ++j) {
                long l = ChunkPos.asLong(i, j);
                for (StructureStart structureStart : worldGenLevel.getChunk(i, j).getAllStarts().values()) {
                    try {
                        if (!structureStart.isValid() || !structureStart.getBoundingBox().intersects(n4, n5, n4 + 15, n5 + 15)) continue;
                        structureFeatureManager.addReferenceForFeature(sectionPos, structureStart.getFeature(), l, chunkAccess);
                        DebugPackets.sendStructurePacket(worldGenLevel, structureStart);
                    }
                    catch (Exception exception) {
                        CrashReport crashReport = CrashReport.forThrowable(exception, "Generating structure reference");
                        CrashReportCategory crashReportCategory = crashReport.addCategory("Structure");
                        Optional<Registry<ConfiguredStructureFeature<?, ?>>> optional = worldGenLevel.registryAccess().registry(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY);
                        crashReportCategory.setDetail("Id", () -> optional.map(registry -> registry.getKey(structureStart.getFeature()).toString()).orElse("UNKNOWN"));
                        crashReportCategory.setDetail("Name", () -> Registry.STRUCTURE_FEATURE.getKey((StructureFeature<?>)structureStart.getFeature().feature).toString());
                        crashReportCategory.setDetail("Class", () -> structureStart.getFeature().getClass().getCanonicalName());
                        throw new ReportedException(crashReport);
                    }
                }
            }
        }
    }

    public abstract CompletableFuture<ChunkAccess> fillFromNoise(Executor var1, Blender var2, StructureFeatureManager var3, ChunkAccess var4);

    public abstract int getSeaLevel();

    public abstract int getMinY();

    public abstract int getBaseHeight(int var1, int var2, Heightmap.Types var3, LevelHeightAccessor var4);

    public abstract NoiseColumn getBaseColumn(int var1, int var2, LevelHeightAccessor var3);

    public int getFirstFreeHeight(int n, int n2, Heightmap.Types types, LevelHeightAccessor levelHeightAccessor) {
        return this.getBaseHeight(n, n2, types, levelHeightAccessor);
    }

    public int getFirstOccupiedHeight(int n, int n2, Heightmap.Types types, LevelHeightAccessor levelHeightAccessor) {
        return this.getBaseHeight(n, n2, types, levelHeightAccessor) - 1;
    }

    public void ensureStructuresGenerated() {
        if (!this.hasGeneratedPositions) {
            this.generatePositions();
            this.hasGeneratedPositions = true;
        }
    }

    @Nullable
    public List<ChunkPos> getRingPositionsFor(ConcentricRingsStructurePlacement concentricRingsStructurePlacement) {
        this.ensureStructuresGenerated();
        CompletableFuture<List<ChunkPos>> completableFuture = this.ringPositions.get(concentricRingsStructurePlacement);
        return completableFuture != null ? completableFuture.join() : null;
    }

    private List<StructurePlacement> getPlacementsForFeature(Holder<ConfiguredStructureFeature<?, ?>> holder) {
        this.ensureStructuresGenerated();
        return this.placementsForFeature.getOrDefault(holder.value(), List.of());
    }

    public abstract void addDebugScreenInfo(List<String> var1, BlockPos var2);

    private /* synthetic */ boolean lambda$tryGenerateStructure$21(HolderSet holderSet, Holder holder) {
        return holderSet.contains(this.adjustBiome(holder));
    }

    private static /* synthetic */ void lambda$applyBiomeDecoration$14(IntSet intSet, BiomeSource.StepFeatureData stepFeatureData, PlacedFeature placedFeature) {
        intSet.add(stepFeatureData.indexMapping().applyAsInt(placedFeature));
    }

    private static /* synthetic */ String lambda$applyBiomeDecoration$12(Registry registry, ConfiguredStructureFeature configuredStructureFeature) {
        return registry.getResourceKey(configuredStructureFeature).map(Object::toString).orElseGet(configuredStructureFeature::toString);
    }

    private static /* synthetic */ void lambda$applyBiomeDecoration$11(WorldGenLevel worldGenLevel, Set set, ChunkPos chunkPos) {
        ChunkAccess chunkAccess = worldGenLevel.getChunk(chunkPos.x, chunkPos.z);
        for (LevelChunkSection levelChunkSection : chunkAccess.getSections()) {
            levelChunkSection.getBiomes().getAll(holder -> set.add((Biome)holder.value()));
        }
    }

    static {
        Registry.register(Registry.CHUNK_GENERATOR, "noise", NoiseBasedChunkGenerator.CODEC);
        Registry.register(Registry.CHUNK_GENERATOR, "flat", FlatLevelSource.CODEC);
        Registry.register(Registry.CHUNK_GENERATOR, "debug", DebugLevelSource.CODEC);
        LOGGER = LogUtils.getLogger();
        CODEC = Registry.CHUNK_GENERATOR.byNameCodec().dispatchStable(ChunkGenerator::codec, Function.identity());
    }
}

