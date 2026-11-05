/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.google.common.collect.Sets
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  it.unimi.dsi.fastutil.longs.LongSet
 *  it.unimi.dsi.fastutil.shorts.ShortArrayList
 *  it.unimi.dsi.fastutil.shorts.ShortList
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.chunk;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.FeatureAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.gameevent.GameEventDispatcher;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.BelowZeroRetrogen;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.ticks.SerializableTickContainer;
import net.minecraft.world.ticks.TickContainerAccess;
import org.slf4j.Logger;

public abstract class ChunkAccess
implements BlockGetter,
BiomeManager.NoiseBiomeSource,
FeatureAccess {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final LongSet EMPTY_REFERENCE_SET = new LongOpenHashSet();
    protected final ShortList[] postProcessing;
    protected volatile boolean unsaved;
    private volatile boolean isLightCorrect;
    protected final ChunkPos chunkPos;
    private long inhabitedTime;
    @Nullable
    @Deprecated
    private Holder<Biome> carverBiome;
    @Nullable
    protected NoiseChunk noiseChunk;
    protected final UpgradeData upgradeData;
    @Nullable
    protected BlendingData blendingData;
    protected final Map<Heightmap.Types, Heightmap> heightmaps = Maps.newEnumMap(Heightmap.Types.class);
    private final Map<ConfiguredStructureFeature<?, ?>, StructureStart> structureStarts = Maps.newHashMap();
    private final Map<ConfiguredStructureFeature<?, ?>, LongSet> structuresRefences = Maps.newHashMap();
    protected final Map<BlockPos, CompoundTag> pendingBlockEntities = Maps.newHashMap();
    protected final Map<BlockPos, BlockEntity> blockEntities = Maps.newHashMap();
    protected final LevelHeightAccessor levelHeightAccessor;
    protected final LevelChunkSection[] sections;

    public ChunkAccess(ChunkPos chunkPos, UpgradeData upgradeData, LevelHeightAccessor levelHeightAccessor, Registry<Biome> registry, long l, @Nullable LevelChunkSection[] levelChunkSectionArray, @Nullable BlendingData blendingData) {
        this.chunkPos = chunkPos;
        this.upgradeData = upgradeData;
        this.levelHeightAccessor = levelHeightAccessor;
        this.sections = new LevelChunkSection[levelHeightAccessor.getSectionsCount()];
        this.inhabitedTime = l;
        this.postProcessing = new ShortList[levelHeightAccessor.getSectionsCount()];
        this.blendingData = blendingData;
        if (levelChunkSectionArray != null) {
            if (this.sections.length == levelChunkSectionArray.length) {
                System.arraycopy(levelChunkSectionArray, 0, this.sections, 0, this.sections.length);
            } else {
                LOGGER.warn("Could not set level chunk sections, array length is {} instead of {}", (Object)levelChunkSectionArray.length, (Object)this.sections.length);
            }
        }
        ChunkAccess.replaceMissingSections(levelHeightAccessor, registry, this.sections);
    }

    private static void replaceMissingSections(LevelHeightAccessor levelHeightAccessor, Registry<Biome> registry, LevelChunkSection[] levelChunkSectionArray) {
        for (int i = 0; i < levelChunkSectionArray.length; ++i) {
            if (levelChunkSectionArray[i] != null) continue;
            levelChunkSectionArray[i] = new LevelChunkSection(levelHeightAccessor.getSectionYFromSectionIndex(i), registry);
        }
    }

    public GameEventDispatcher getEventDispatcher(int n) {
        return GameEventDispatcher.NOOP;
    }

    @Nullable
    public abstract BlockState setBlockState(BlockPos var1, BlockState var2, boolean var3);

    public abstract void setBlockEntity(BlockEntity var1);

    public abstract void addEntity(Entity var1);

    @Nullable
    public LevelChunkSection getHighestSection() {
        LevelChunkSection[] levelChunkSectionArray = this.getSections();
        for (int i = levelChunkSectionArray.length - 1; i >= 0; --i) {
            LevelChunkSection levelChunkSection = levelChunkSectionArray[i];
            if (levelChunkSection.hasOnlyAir()) continue;
            return levelChunkSection;
        }
        return null;
    }

    public int getHighestSectionPosition() {
        LevelChunkSection levelChunkSection = this.getHighestSection();
        return levelChunkSection == null ? this.getMinBuildHeight() : levelChunkSection.bottomBlockY();
    }

    public Set<BlockPos> getBlockEntitiesPos() {
        HashSet hashSet = Sets.newHashSet(this.pendingBlockEntities.keySet());
        hashSet.addAll(this.blockEntities.keySet());
        return hashSet;
    }

    public LevelChunkSection[] getSections() {
        return this.sections;
    }

    public LevelChunkSection getSection(int n) {
        return this.getSections()[n];
    }

    public Collection<Map.Entry<Heightmap.Types, Heightmap>> getHeightmaps() {
        return Collections.unmodifiableSet(this.heightmaps.entrySet());
    }

    public void setHeightmap(Heightmap.Types types, long[] lArray) {
        this.getOrCreateHeightmapUnprimed(types).setRawData(this, types, lArray);
    }

    public Heightmap getOrCreateHeightmapUnprimed(Heightmap.Types types2) {
        return this.heightmaps.computeIfAbsent(types2, types -> new Heightmap(this, (Heightmap.Types)types));
    }

    public boolean hasPrimedHeightmap(Heightmap.Types types) {
        return this.heightmaps.get(types) != null;
    }

    public int getHeight(Heightmap.Types types, int n, int n2) {
        Heightmap heightmap = this.heightmaps.get(types);
        if (heightmap == null) {
            if (SharedConstants.IS_RUNNING_IN_IDE && this instanceof LevelChunk) {
                LOGGER.error("Unprimed heightmap: " + types + " " + n + " " + n2);
            }
            Heightmap.primeHeightmaps(this, EnumSet.of(types));
            heightmap = this.heightmaps.get(types);
        }
        return heightmap.getFirstAvailable(n & 0xF, n2 & 0xF) - 1;
    }

    public ChunkPos getPos() {
        return this.chunkPos;
    }

    @Override
    @Nullable
    public StructureStart getStartForFeature(ConfiguredStructureFeature<?, ?> configuredStructureFeature) {
        return this.structureStarts.get(configuredStructureFeature);
    }

    @Override
    public void setStartForFeature(ConfiguredStructureFeature<?, ?> configuredStructureFeature, StructureStart structureStart) {
        this.structureStarts.put(configuredStructureFeature, structureStart);
        this.unsaved = true;
    }

    public Map<ConfiguredStructureFeature<?, ?>, StructureStart> getAllStarts() {
        return Collections.unmodifiableMap(this.structureStarts);
    }

    public void setAllStarts(Map<ConfiguredStructureFeature<?, ?>, StructureStart> map) {
        this.structureStarts.clear();
        this.structureStarts.putAll(map);
        this.unsaved = true;
    }

    @Override
    public LongSet getReferencesForFeature(ConfiguredStructureFeature<?, ?> configuredStructureFeature) {
        return this.structuresRefences.getOrDefault(configuredStructureFeature, EMPTY_REFERENCE_SET);
    }

    @Override
    public void addReferenceForFeature(ConfiguredStructureFeature<?, ?> configuredStructureFeature2, long l) {
        this.structuresRefences.computeIfAbsent(configuredStructureFeature2, configuredStructureFeature -> new LongOpenHashSet()).add(l);
        this.unsaved = true;
    }

    @Override
    public Map<ConfiguredStructureFeature<?, ?>, LongSet> getAllReferences() {
        return Collections.unmodifiableMap(this.structuresRefences);
    }

    @Override
    public void setAllReferences(Map<ConfiguredStructureFeature<?, ?>, LongSet> map) {
        this.structuresRefences.clear();
        this.structuresRefences.putAll(map);
        this.unsaved = true;
    }

    public boolean isYSpaceEmpty(int n, int n2) {
        if (n < this.getMinBuildHeight()) {
            n = this.getMinBuildHeight();
        }
        if (n2 >= this.getMaxBuildHeight()) {
            n2 = this.getMaxBuildHeight() - 1;
        }
        for (int i = n; i <= n2; i += 16) {
            if (this.getSection(this.getSectionIndex(i)).hasOnlyAir()) continue;
            return false;
        }
        return true;
    }

    public void setUnsaved(boolean bl) {
        this.unsaved = bl;
    }

    public boolean isUnsaved() {
        return this.unsaved;
    }

    public abstract ChunkStatus getStatus();

    public abstract void removeBlockEntity(BlockPos var1);

    public void markPosForPostprocessing(BlockPos blockPos) {
        LOGGER.warn("Trying to mark a block for PostProcessing @ {}, but this operation is not supported.", (Object)blockPos);
    }

    public ShortList[] getPostProcessing() {
        return this.postProcessing;
    }

    public void addPackedPostProcess(short s, int n) {
        ChunkAccess.getOrCreateOffsetList(this.getPostProcessing(), n).add(s);
    }

    public void setBlockEntityNbt(CompoundTag compoundTag) {
        this.pendingBlockEntities.put(BlockEntity.getPosFromTag(compoundTag), compoundTag);
    }

    @Nullable
    public CompoundTag getBlockEntityNbt(BlockPos blockPos) {
        return this.pendingBlockEntities.get(blockPos);
    }

    @Nullable
    public abstract CompoundTag getBlockEntityNbtForSaving(BlockPos var1);

    public abstract Stream<BlockPos> getLights();

    public abstract TickContainerAccess<Block> getBlockTicks();

    public abstract TickContainerAccess<Fluid> getFluidTicks();

    public abstract TicksToSave getTicksForSerialization();

    public UpgradeData getUpgradeData() {
        return this.upgradeData;
    }

    public boolean isOldNoiseGeneration() {
        return this.blendingData != null && this.blendingData.oldNoise();
    }

    @Nullable
    public BlendingData getBlendingData() {
        return this.blendingData;
    }

    public void setBlendingData(BlendingData blendingData) {
        this.blendingData = blendingData;
    }

    public long getInhabitedTime() {
        return this.inhabitedTime;
    }

    public void incrementInhabitedTime(long l) {
        this.inhabitedTime += l;
    }

    public void setInhabitedTime(long l) {
        this.inhabitedTime = l;
    }

    public static ShortList getOrCreateOffsetList(ShortList[] shortListArray, int n) {
        if (shortListArray[n] == null) {
            shortListArray[n] = new ShortArrayList();
        }
        return shortListArray[n];
    }

    public boolean isLightCorrect() {
        return this.isLightCorrect;
    }

    public void setLightCorrect(boolean bl) {
        this.isLightCorrect = bl;
        this.setUnsaved(true);
    }

    @Override
    public int getMinBuildHeight() {
        return this.levelHeightAccessor.getMinBuildHeight();
    }

    @Override
    public int getHeight() {
        return this.levelHeightAccessor.getHeight();
    }

    public NoiseChunk getOrCreateNoiseChunk(NoiseRouter noiseRouter, Supplier<DensityFunctions.BeardifierOrMarker> supplier, NoiseGeneratorSettings noiseGeneratorSettings, Aquifer.FluidPicker fluidPicker, Blender blender) {
        if (this.noiseChunk == null) {
            this.noiseChunk = NoiseChunk.forChunk(this, noiseRouter, supplier, noiseGeneratorSettings, fluidPicker, blender);
        }
        return this.noiseChunk;
    }

    @Deprecated
    public Holder<Biome> carverBiome(Supplier<Holder<Biome>> supplier) {
        if (this.carverBiome == null) {
            this.carverBiome = supplier.get();
        }
        return this.carverBiome;
    }

    @Override
    public Holder<Biome> getNoiseBiome(int n, int n2, int n3) {
        try {
            int n4 = QuartPos.fromBlock(this.getMinBuildHeight());
            int n5 = n4 + QuartPos.fromBlock(this.getHeight()) - 1;
            int n6 = Mth.clamp(n2, n4, n5);
            int n7 = this.getSectionIndex(QuartPos.toBlock(n6));
            return this.sections[n7].getNoiseBiome(n & 3, n6 & 3, n3 & 3);
        }
        catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.forThrowable(throwable, "Getting biome");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Biome being got");
            crashReportCategory.setDetail("Location", () -> CrashReportCategory.formatLocation((LevelHeightAccessor)this, n, n2, n3));
            throw new ReportedException(crashReport);
        }
    }

    public void fillBiomesFromNoise(BiomeResolver biomeResolver, Climate.Sampler sampler) {
        ChunkPos chunkPos = this.getPos();
        int n = QuartPos.fromBlock(chunkPos.getMinBlockX());
        int n2 = QuartPos.fromBlock(chunkPos.getMinBlockZ());
        LevelHeightAccessor levelHeightAccessor = this.getHeightAccessorForGeneration();
        for (int i = levelHeightAccessor.getMinSection(); i < levelHeightAccessor.getMaxSection(); ++i) {
            LevelChunkSection levelChunkSection = this.getSection(this.getSectionIndexFromSectionY(i));
            levelChunkSection.fillBiomesFromNoise(biomeResolver, sampler, n, n2);
        }
    }

    public boolean hasAnyStructureReferences() {
        return !this.getAllReferences().isEmpty();
    }

    @Nullable
    public BelowZeroRetrogen getBelowZeroRetrogen() {
        return null;
    }

    public boolean isUpgrading() {
        return this.getBelowZeroRetrogen() != null;
    }

    public LevelHeightAccessor getHeightAccessorForGeneration() {
        return this;
    }

    public record TicksToSave(SerializableTickContainer<Block> blocks, SerializableTickContainer<Fluid> fluids) {
    }
}

