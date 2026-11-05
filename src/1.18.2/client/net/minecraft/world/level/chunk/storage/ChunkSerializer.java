/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  it.unimi.dsi.fastutil.longs.LongSet
 *  it.unimi.dsi.fastutil.shorts.ShortList
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.chunk.storage;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.BelowZeroRetrogen;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraft.world.ticks.ProtoChunkTicks;
import org.slf4j.Logger;

public class ChunkSerializer {
    private static final Codec<PalettedContainer<BlockState>> BLOCK_STATE_CODEC = PalettedContainer.codec(Block.BLOCK_STATE_REGISTRY, BlockState.CODEC, PalettedContainer.Strategy.SECTION_STATES, Blocks.AIR.defaultBlockState());
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String TAG_UPGRADE_DATA = "UpgradeData";
    private static final String BLOCK_TICKS_TAG = "block_ticks";
    private static final String FLUID_TICKS_TAG = "fluid_ticks";

    public static ProtoChunk read(ServerLevel serverLevel, PoiManager poiManager, ChunkPos chunkPos, CompoundTag compoundTag) {
        Object object;
        Object object3;
        EnumSet<Heightmap.Types> enumSet;
        PalettedContainer<Object> palettedContainer;
        Object object4;
        ChunkPos chunkPos2 = new ChunkPos(compoundTag.getInt("xPos"), compoundTag.getInt("zPos"));
        if (!Objects.equals(chunkPos, chunkPos2)) {
            LOGGER.error("Chunk file at {} is in the wrong location; relocating. (Expected {}, got {})", new Object[]{chunkPos, chunkPos, chunkPos2});
        }
        UpgradeData upgradeData = compoundTag.contains(TAG_UPGRADE_DATA, 10) ? new UpgradeData(compoundTag.getCompound(TAG_UPGRADE_DATA), serverLevel) : UpgradeData.EMPTY;
        boolean bl = compoundTag.getBoolean("isLightOn");
        ListTag listTag = compoundTag.getList("sections", 10);
        int n = serverLevel.getSectionsCount();
        LevelChunkSection[] levelChunkSectionArray = new LevelChunkSection[n];
        boolean bl2 = serverLevel.dimensionType().hasSkyLight();
        ServerChunkCache serverChunkCache = serverLevel.getChunkSource();
        LevelLightEngine levelLightEngine = ((ChunkSource)serverChunkCache).getLightEngine();
        if (bl) {
            levelLightEngine.retainData(chunkPos, true);
        }
        Registry<Biome> registry = serverLevel.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);
        Codec<PalettedContainer<Holder<Biome>>> codec = ChunkSerializer.makeBiomeCodec(registry);
        for (int i = 0; i < listTag.size(); ++i) {
            CompoundTag compoundTag2 = listTag.getCompound(i);
            byte by = compoundTag2.getByte("Y");
            int n2 = serverLevel.getSectionIndexFromSectionY(by);
            if (n2 >= 0 && n2 < levelChunkSectionArray.length) {
                object4 = compoundTag2.contains("block_states", 10) ? (PalettedContainer)BLOCK_STATE_CODEC.parse((DynamicOps)NbtOps.INSTANCE, (Object)compoundTag2.getCompound("block_states")).promotePartial(string -> ChunkSerializer.logErrors(chunkPos, by, string)).getOrThrow(false, arg_0 -> ((Logger)LOGGER).error(arg_0)) : new PalettedContainer(Block.BLOCK_STATE_REGISTRY, Blocks.AIR.defaultBlockState(), PalettedContainer.Strategy.SECTION_STATES);
                palettedContainer = compoundTag2.contains("biomes", 10) ? (PalettedContainer)codec.parse((DynamicOps)NbtOps.INSTANCE, (Object)compoundTag2.getCompound("biomes")).promotePartial(string -> ChunkSerializer.logErrors(chunkPos, by, string)).getOrThrow(false, arg_0 -> ((Logger)LOGGER).error(arg_0)) : new PalettedContainer<Holder<Biome>>(registry.asHolderIdMap(), registry.getHolderOrThrow(Biomes.PLAINS), PalettedContainer.Strategy.SECTION_BIOMES);
                enumSet = new LevelChunkSection(by, (PalettedContainer<BlockState>)object4, (PalettedContainer<Holder<Biome>>)palettedContainer);
                levelChunkSectionArray[n2] = enumSet;
                poiManager.checkConsistencyWithBlocks(chunkPos, (LevelChunkSection)((Object)enumSet));
            }
            if (!bl) continue;
            if (compoundTag2.contains("BlockLight", 7)) {
                levelLightEngine.queueSectionData(LightLayer.BLOCK, SectionPos.of(chunkPos, by), new DataLayer(compoundTag2.getByteArray("BlockLight")), true);
            }
            if (!bl2 || !compoundTag2.contains("SkyLight", 7)) continue;
            levelLightEngine.queueSectionData(LightLayer.SKY, SectionPos.of(chunkPos, by), new DataLayer(compoundTag2.getByteArray("SkyLight")), true);
        }
        long l = compoundTag.getLong("InhabitedTime");
        ChunkStatus.ChunkType chunkType = ChunkSerializer.getChunkTypeFromTag(compoundTag);
        BlendingData blendingData = compoundTag.contains("blending_data", 10) ? (BlendingData)BlendingData.CODEC.parse(new Dynamic((DynamicOps)NbtOps.INSTANCE, (Object)compoundTag.getCompound("blending_data"))).resultOrPartial(arg_0 -> ((Logger)LOGGER).error(arg_0)).orElse(null) : null;
        if (chunkType == ChunkStatus.ChunkType.LEVELCHUNK) {
            palettedContainer = LevelChunkTicks.load(compoundTag.getList(BLOCK_TICKS_TAG, 10), string -> Registry.BLOCK.getOptional(ResourceLocation.tryParse(string)), chunkPos);
            enumSet = LevelChunkTicks.load(compoundTag.getList(FLUID_TICKS_TAG, 10), string -> Registry.FLUID.getOptional(ResourceLocation.tryParse(string)), chunkPos);
            object4 = new LevelChunk(serverLevel.getLevel(), chunkPos, upgradeData, (LevelChunkTicks<Block>)((Object)palettedContainer), (LevelChunkTicks<Fluid>)((Object)enumSet), l, levelChunkSectionArray, ChunkSerializer.postLoadChunk(serverLevel, compoundTag), blendingData);
        } else {
            boolean bl3;
            palettedContainer = ProtoChunkTicks.load(compoundTag.getList(BLOCK_TICKS_TAG, 10), string -> Registry.BLOCK.getOptional(ResourceLocation.tryParse(string)), chunkPos);
            enumSet = ProtoChunkTicks.load(compoundTag.getList(FLUID_TICKS_TAG, 10), string -> Registry.FLUID.getOptional(ResourceLocation.tryParse(string)), chunkPos);
            object4 = object3 = new ProtoChunk(chunkPos, upgradeData, levelChunkSectionArray, (ProtoChunkTicks<Block>)((Object)palettedContainer), (ProtoChunkTicks<Fluid>)((Object)enumSet), serverLevel, registry, blendingData);
            ((ChunkAccess)object4).setInhabitedTime(l);
            if (compoundTag.contains("below_zero_retrogen", 10)) {
                BelowZeroRetrogen.CODEC.parse(new Dynamic((DynamicOps)NbtOps.INSTANCE, (Object)compoundTag.getCompound("below_zero_retrogen"))).resultOrPartial(arg_0 -> ((Logger)LOGGER).error(arg_0)).ifPresent(((ProtoChunk)object3)::setBelowZeroRetrogen);
            }
            ChunkStatus object22 = ChunkStatus.byName(compoundTag.getString("Status"));
            ((ProtoChunk)object3).setStatus(object22);
            if (object22.isOrAfter(ChunkStatus.FEATURES)) {
                ((ProtoChunk)object3).setLightEngine(levelLightEngine);
            }
            object = ((ProtoChunk)object3).getBelowZeroRetrogen();
            boolean bl4 = bl3 = object22.isOrAfter(ChunkStatus.LIGHT) || object != null && ((BelowZeroRetrogen)object).targetStatus().isOrAfter(ChunkStatus.LIGHT);
            if (!bl && bl3) {
                for (BlockPos blockPos : BlockPos.betweenClosed(chunkPos.getMinBlockX(), serverLevel.getMinBuildHeight(), chunkPos.getMinBlockZ(), chunkPos.getMaxBlockX(), serverLevel.getMaxBuildHeight() - 1, chunkPos.getMaxBlockZ())) {
                    if (object4.getBlockState(blockPos).getLightEmission() == 0) continue;
                    ((ProtoChunk)object3).addLight(blockPos);
                }
            }
        }
        ((ChunkAccess)object4).setLightCorrect(bl);
        palettedContainer = compoundTag.getCompound("Heightmaps");
        enumSet = EnumSet.noneOf(Heightmap.Types.class);
        for (Heightmap.Types types : ((ChunkAccess)object4).getStatus().heightmapsAfter()) {
            object = types.getSerializationKey();
            if (((CompoundTag)((Object)palettedContainer)).contains((String)object, 12)) {
                ((ChunkAccess)object4).setHeightmap(types, ((CompoundTag)((Object)palettedContainer)).getLongArray((String)object));
                continue;
            }
            enumSet.add(types);
        }
        Heightmap.primeHeightmaps((ChunkAccess)object4, enumSet);
        object3 = compoundTag.getCompound("structures");
        ((ChunkAccess)object4).setAllStarts(ChunkSerializer.unpackStructureStart(StructurePieceSerializationContext.fromLevel(serverLevel), (CompoundTag)object3, serverLevel.getSeed()));
        ((ChunkAccess)object4).setAllReferences(ChunkSerializer.unpackStructureReferences(serverLevel.registryAccess(), chunkPos, (CompoundTag)object3));
        if (compoundTag.getBoolean("shouldSave")) {
            ((ChunkAccess)object4).setUnsaved(true);
        }
        ListTag listTag2 = compoundTag.getList("PostProcessing", 9);
        for (int i = 0; i < listTag2.size(); ++i) {
            ListTag listTag3 = listTag2.getList(i);
            for (int j = 0; j < listTag3.size(); ++j) {
                ((ChunkAccess)object4).addPackedPostProcess(listTag3.getShort(j), i);
            }
        }
        if (chunkType == ChunkStatus.ChunkType.LEVELCHUNK) {
            return new ImposterProtoChunk((LevelChunk)object4, false);
        }
        ProtoChunk protoChunk = (ProtoChunk)object4;
        ListTag listTag4 = compoundTag.getList("entities", 10);
        for (int i = 0; i < listTag4.size(); ++i) {
            protoChunk.addEntity(listTag4.getCompound(i));
        }
        ListTag listTag5 = compoundTag.getList("block_entities", 10);
        for (int i = 0; i < listTag5.size(); ++i) {
            CompoundTag compoundTag2 = listTag5.getCompound(i);
            ((ChunkAccess)object4).setBlockEntityNbt(compoundTag2);
        }
        ListTag listTag6 = compoundTag.getList("Lights", 9);
        for (int i = 0; i < listTag6.size(); ++i) {
            ListTag listTag7 = listTag6.getList(i);
            for (int j = 0; j < listTag7.size(); ++j) {
                protoChunk.addLight(listTag7.getShort(j), i);
            }
        }
        CompoundTag compoundTag3 = compoundTag.getCompound("CarvingMasks");
        for (String string2 : compoundTag3.getAllKeys()) {
            GenerationStep.Carving carving = GenerationStep.Carving.valueOf(string2);
            protoChunk.setCarvingMask(carving, new CarvingMask(compoundTag3.getLongArray(string2), ((ChunkAccess)object4).getMinBuildHeight()));
        }
        return protoChunk;
    }

    private static void logErrors(ChunkPos chunkPos, int n, String string) {
        LOGGER.error("Recoverable errors when loading section [" + chunkPos.x + ", " + n + ", " + chunkPos.z + "]: " + string);
    }

    private static Codec<PalettedContainer<Holder<Biome>>> makeBiomeCodec(Registry<Biome> registry) {
        return PalettedContainer.codec(registry.asHolderIdMap(), registry.holderByNameCodec(), PalettedContainer.Strategy.SECTION_BIOMES, registry.getHolderOrThrow(Biomes.PLAINS));
    }

    public static CompoundTag write(ServerLevel serverLevel, ChunkAccess chunkAccess) {
        Object object;
        UpgradeData upgradeData;
        BelowZeroRetrogen belowZeroRetrogen;
        ChunkPos chunkPos = chunkAccess.getPos();
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putInt("DataVersion", SharedConstants.getCurrentVersion().getWorldVersion());
        compoundTag.putInt("xPos", chunkPos.x);
        compoundTag.putInt("yPos", chunkAccess.getMinSection());
        compoundTag.putInt("zPos", chunkPos.z);
        compoundTag.putLong("LastUpdate", serverLevel.getGameTime());
        compoundTag.putLong("InhabitedTime", chunkAccess.getInhabitedTime());
        compoundTag.putString("Status", chunkAccess.getStatus().getName());
        BlendingData blendingData = chunkAccess.getBlendingData();
        if (blendingData != null) {
            BlendingData.CODEC.encodeStart((DynamicOps)NbtOps.INSTANCE, (Object)blendingData).resultOrPartial(arg_0 -> ((Logger)LOGGER).error(arg_0)).ifPresent(tag -> compoundTag.put("blending_data", (Tag)tag));
        }
        if ((belowZeroRetrogen = chunkAccess.getBelowZeroRetrogen()) != null) {
            BelowZeroRetrogen.CODEC.encodeStart((DynamicOps)NbtOps.INSTANCE, (Object)belowZeroRetrogen).resultOrPartial(arg_0 -> ((Logger)LOGGER).error(arg_0)).ifPresent(tag -> compoundTag.put("below_zero_retrogen", (Tag)tag));
        }
        if (!(upgradeData = chunkAccess.getUpgradeData()).isEmpty()) {
            compoundTag.put(TAG_UPGRADE_DATA, upgradeData.write());
        }
        LevelChunkSection[] levelChunkSectionArray = chunkAccess.getSections();
        ListTag listTag = new ListTag();
        ThreadedLevelLightEngine threadedLevelLightEngine = serverLevel.getChunkSource().getLightEngine();
        Registry<Biome> registry = serverLevel.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);
        Codec<PalettedContainer<Holder<Biome>>> codec = ChunkSerializer.makeBiomeCodec(registry);
        boolean bl = chunkAccess.isLightCorrect();
        for (int i = threadedLevelLightEngine.getMinLightSection(); i < threadedLevelLightEngine.getMaxLightSection(); ++i) {
            int n = chunkAccess.getSectionIndexFromSectionY(i);
            boolean bl2 = n >= 0 && n < levelChunkSectionArray.length;
            DataLayer object22 = threadedLevelLightEngine.getLayerListener(LightLayer.BLOCK).getDataLayerData(SectionPos.of(chunkPos, i));
            GenerationStep.Carving[] carvingArray = threadedLevelLightEngine.getLayerListener(LightLayer.SKY).getDataLayerData(SectionPos.of(chunkPos, i));
            if (!bl2 && object22 == null && carvingArray == null) continue;
            CompoundTag compoundTag2 = new CompoundTag();
            if (bl2) {
                LevelChunkSection levelChunkSection = levelChunkSectionArray[n];
                compoundTag2.put("block_states", (Tag)BLOCK_STATE_CODEC.encodeStart((DynamicOps)NbtOps.INSTANCE, levelChunkSection.getStates()).getOrThrow(false, arg_0 -> ((Logger)LOGGER).error(arg_0)));
                compoundTag2.put("biomes", (Tag)codec.encodeStart((DynamicOps)NbtOps.INSTANCE, levelChunkSection.getBiomes()).getOrThrow(false, arg_0 -> ((Logger)LOGGER).error(arg_0)));
            }
            if (object22 != null && !object22.isEmpty()) {
                compoundTag2.putByteArray("BlockLight", object22.getData());
            }
            if (carvingArray != null && !carvingArray.isEmpty()) {
                compoundTag2.putByteArray("SkyLight", carvingArray.getData());
            }
            if (compoundTag2.isEmpty()) continue;
            compoundTag2.putByte("Y", (byte)i);
            listTag.add(compoundTag2);
        }
        compoundTag.put("sections", listTag);
        if (bl) {
            compoundTag.putBoolean("isLightOn", true);
        }
        ListTag listTag2 = new ListTag();
        for (BlockPos blockPos : chunkAccess.getBlockEntitiesPos()) {
            CompoundTag compoundTag3 = chunkAccess.getBlockEntityNbtForSaving(blockPos);
            if (compoundTag3 == null) continue;
            listTag2.add(compoundTag3);
        }
        compoundTag.put("block_entities", listTag2);
        if (chunkAccess.getStatus().getChunkType() == ChunkStatus.ChunkType.PROTOCHUNK) {
            object = (ProtoChunk)chunkAccess;
            ListTag listTag3 = new ListTag();
            listTag3.addAll(((ProtoChunk)object).getEntities());
            compoundTag.put("entities", listTag3);
            compoundTag.put("Lights", ChunkSerializer.packOffsets(((ProtoChunk)object).getPackedLights()));
            CompoundTag compoundTag4 = new CompoundTag();
            for (GenerationStep.Carving carving : GenerationStep.Carving.values()) {
                CarvingMask carvingMask = ((ProtoChunk)object).getCarvingMask(carving);
                if (carvingMask == null) continue;
                compoundTag4.putLongArray(carving.toString(), carvingMask.toArray());
            }
            compoundTag.put("CarvingMasks", compoundTag4);
        }
        ChunkSerializer.saveTicks(serverLevel, compoundTag, chunkAccess.getTicksForSerialization());
        compoundTag.put("PostProcessing", ChunkSerializer.packOffsets(chunkAccess.getPostProcessing()));
        object = new CompoundTag();
        for (Map.Entry<Heightmap.Types, Heightmap> entry : chunkAccess.getHeightmaps()) {
            if (!chunkAccess.getStatus().heightmapsAfter().contains(entry.getKey())) continue;
            ((CompoundTag)object).put(entry.getKey().getSerializationKey(), new LongArrayTag(entry.getValue().getRawData()));
        }
        compoundTag.put("Heightmaps", (Tag)object);
        compoundTag.put("structures", ChunkSerializer.packStructureData(StructurePieceSerializationContext.fromLevel(serverLevel), chunkPos, chunkAccess.getAllStarts(), chunkAccess.getAllReferences()));
        return compoundTag;
    }

    private static void saveTicks(ServerLevel serverLevel, CompoundTag compoundTag, ChunkAccess.TicksToSave ticksToSave) {
        long l = serverLevel.getLevelData().getGameTime();
        compoundTag.put(BLOCK_TICKS_TAG, ticksToSave.blocks().save(l, block -> Registry.BLOCK.getKey((Block)block).toString()));
        compoundTag.put(FLUID_TICKS_TAG, ticksToSave.fluids().save(l, fluid -> Registry.FLUID.getKey((Fluid)fluid).toString()));
    }

    public static ChunkStatus.ChunkType getChunkTypeFromTag(@Nullable CompoundTag compoundTag) {
        if (compoundTag != null) {
            return ChunkStatus.byName(compoundTag.getString("Status")).getChunkType();
        }
        return ChunkStatus.ChunkType.PROTOCHUNK;
    }

    @Nullable
    private static LevelChunk.PostLoadProcessor postLoadChunk(ServerLevel serverLevel, CompoundTag compoundTag) {
        ListTag listTag = ChunkSerializer.getListOfCompoundsOrNull(compoundTag, "entities");
        ListTag listTag2 = ChunkSerializer.getListOfCompoundsOrNull(compoundTag, "block_entities");
        if (listTag == null && listTag2 == null) {
            return null;
        }
        return levelChunk -> {
            if (listTag != null) {
                serverLevel.addLegacyChunkEntities(EntityType.loadEntitiesRecursive(listTag, serverLevel));
            }
            if (listTag2 != null) {
                for (int i = 0; i < listTag2.size(); ++i) {
                    CompoundTag compoundTag = listTag2.getCompound(i);
                    boolean bl = compoundTag.getBoolean("keepPacked");
                    if (bl) {
                        levelChunk.setBlockEntityNbt(compoundTag);
                        continue;
                    }
                    BlockPos blockPos = BlockEntity.getPosFromTag(compoundTag);
                    BlockEntity blockEntity = BlockEntity.loadStatic(blockPos, levelChunk.getBlockState(blockPos), compoundTag);
                    if (blockEntity == null) continue;
                    levelChunk.setBlockEntity(blockEntity);
                }
            }
        };
    }

    @Nullable
    private static ListTag getListOfCompoundsOrNull(CompoundTag compoundTag, String string) {
        ListTag listTag = compoundTag.getList(string, 10);
        return listTag.isEmpty() ? null : listTag;
    }

    private static CompoundTag packStructureData(StructurePieceSerializationContext structurePieceSerializationContext, ChunkPos chunkPos, Map<ConfiguredStructureFeature<?, ?>, StructureStart> map, Map<ConfiguredStructureFeature<?, ?>, LongSet> map2) {
        CompoundTag compoundTag = new CompoundTag();
        CompoundTag compoundTag2 = new CompoundTag();
        Registry<ConfiguredStructureFeature<?, ?>> registry = structurePieceSerializationContext.registryAccess().registryOrThrow(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY);
        for (Map.Entry<ConfiguredStructureFeature<?, ?>, StructureStart> object : map.entrySet()) {
            ResourceLocation resourceLocation = registry.getKey(object.getKey());
            compoundTag2.put(resourceLocation.toString(), object.getValue().createTag(structurePieceSerializationContext, chunkPos));
        }
        compoundTag.put("starts", compoundTag2);
        CompoundTag compoundTag3 = new CompoundTag();
        for (Map.Entry<ConfiguredStructureFeature<?, ?>, LongSet> entry : map2.entrySet()) {
            if (entry.getValue().isEmpty()) continue;
            ResourceLocation resourceLocation = registry.getKey(entry.getKey());
            compoundTag3.put(resourceLocation.toString(), new LongArrayTag(entry.getValue()));
        }
        compoundTag.put("References", compoundTag3);
        return compoundTag;
    }

    private static Map<ConfiguredStructureFeature<?, ?>, StructureStart> unpackStructureStart(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag, long l) {
        HashMap hashMap = Maps.newHashMap();
        Registry<ConfiguredStructureFeature<?, ?>> registry = structurePieceSerializationContext.registryAccess().registryOrThrow(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY);
        CompoundTag compoundTag2 = compoundTag.getCompound("starts");
        for (String string : compoundTag2.getAllKeys()) {
            ResourceLocation resourceLocation = ResourceLocation.tryParse(string);
            ConfiguredStructureFeature<?, ?> configuredStructureFeature = registry.get(resourceLocation);
            if (configuredStructureFeature == null) {
                LOGGER.error("Unknown structure start: {}", (Object)resourceLocation);
                continue;
            }
            StructureStart structureStart = StructureFeature.loadStaticStart(structurePieceSerializationContext, compoundTag2.getCompound(string), l);
            if (structureStart == null) continue;
            hashMap.put(configuredStructureFeature, structureStart);
        }
        return hashMap;
    }

    private static Map<ConfiguredStructureFeature<?, ?>, LongSet> unpackStructureReferences(RegistryAccess registryAccess, ChunkPos chunkPos, CompoundTag compoundTag) {
        HashMap hashMap = Maps.newHashMap();
        Registry<ConfiguredStructureFeature<?, ?>> registry = registryAccess.registryOrThrow(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY);
        CompoundTag compoundTag2 = compoundTag.getCompound("References");
        for (String string : compoundTag2.getAllKeys()) {
            ResourceLocation resourceLocation = ResourceLocation.tryParse(string);
            ConfiguredStructureFeature<?, ?> configuredStructureFeature = registry.get(resourceLocation);
            if (configuredStructureFeature == null) {
                LOGGER.warn("Found reference to unknown structure '{}' in chunk {}, discarding", (Object)resourceLocation, (Object)chunkPos);
                continue;
            }
            long[] lArray = compoundTag2.getLongArray(string);
            if (lArray.length == 0) continue;
            hashMap.put(configuredStructureFeature, new LongOpenHashSet(Arrays.stream(lArray).filter(l -> {
                ChunkPos chunkPos2 = new ChunkPos(l);
                if (chunkPos2.getChessboardDistance(chunkPos) > 8) {
                    LOGGER.warn("Found invalid structure reference [ {} @ {} ] for chunk {}.", new Object[]{resourceLocation, chunkPos2, chunkPos});
                    return false;
                }
                return true;
            }).toArray()));
        }
        return hashMap;
    }

    public static ListTag packOffsets(ShortList[] shortListArray) {
        ListTag listTag = new ListTag();
        for (ShortList shortList : shortListArray) {
            ListTag listTag2 = new ListTag();
            if (shortList != null) {
                for (Short s : shortList) {
                    listTag2.add(ShortTag.valueOf(s));
                }
            }
            listTag.add(listTag2);
        }
        return listTag;
    }
}

