/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.Lists
 *  com.mojang.datafixers.util.Either
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 *  it.unimi.dsi.fastutil.ints.IntList
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.chunk;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import net.minecraft.util.profiling.jfr.callback.ProfiledDuration;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.BelowZeroRetrogen;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class ChunkStatus {
    public static final int MAX_STRUCTURE_DISTANCE = 8;
    private static final EnumSet<Heightmap.Types> PRE_FEATURES = EnumSet.of(Heightmap.Types.OCEAN_FLOOR_WG, Heightmap.Types.WORLD_SURFACE_WG);
    public static final EnumSet<Heightmap.Types> POST_FEATURES = EnumSet.of(Heightmap.Types.OCEAN_FLOOR, Heightmap.Types.WORLD_SURFACE, Heightmap.Types.MOTION_BLOCKING, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES);
    private static final LoadingTask PASSTHROUGH_LOAD_TASK = (chunkStatus, serverLevel, structureManager, threadedLevelLightEngine, function, chunkAccess) -> {
        if (chunkAccess instanceof ProtoChunk) {
            ProtoChunk protoChunk = (ProtoChunk)chunkAccess;
            if (!chunkAccess.getStatus().isOrAfter(chunkStatus)) {
                protoChunk.setStatus(chunkStatus);
            }
        }
        return CompletableFuture.completedFuture(Either.left((Object)chunkAccess));
    };
    public static final ChunkStatus EMPTY = ChunkStatus.registerSimple("empty", null, -1, PRE_FEATURES, ChunkType.PROTOCHUNK, (chunkStatus, serverLevel, chunkGenerator, list, chunkAccess) -> {});
    public static final ChunkStatus STRUCTURE_STARTS = ChunkStatus.register("structure_starts", EMPTY, 0, PRE_FEATURES, ChunkType.PROTOCHUNK, (chunkStatus, executor, serverLevel, chunkGenerator, structureManager, threadedLevelLightEngine, function, list, chunkAccess, bl) -> {
        if (!chunkAccess.getStatus().isOrAfter(chunkStatus)) {
            if (serverLevel.getServer().getWorldData().worldGenSettings().generateFeatures()) {
                chunkGenerator.createStructures(serverLevel.registryAccess(), serverLevel.structureFeatureManager(), chunkAccess, structureManager, serverLevel.getSeed());
            }
            if (chunkAccess instanceof ProtoChunk) {
                ProtoChunk protoChunk = (ProtoChunk)chunkAccess;
                protoChunk.setStatus(chunkStatus);
            }
            serverLevel.onStructureStartsAvailable(chunkAccess);
        }
        return CompletableFuture.completedFuture(Either.left((Object)chunkAccess));
    }, (chunkStatus, serverLevel, structureManager, threadedLevelLightEngine, function, chunkAccess) -> {
        if (!chunkAccess.getStatus().isOrAfter(chunkStatus)) {
            if (chunkAccess instanceof ProtoChunk) {
                ProtoChunk protoChunk = (ProtoChunk)chunkAccess;
                protoChunk.setStatus(chunkStatus);
            }
            serverLevel.onStructureStartsAvailable(chunkAccess);
        }
        return CompletableFuture.completedFuture(Either.left((Object)chunkAccess));
    });
    public static final ChunkStatus STRUCTURE_REFERENCES = ChunkStatus.registerSimple("structure_references", STRUCTURE_STARTS, 8, PRE_FEATURES, ChunkType.PROTOCHUNK, (chunkStatus, serverLevel, chunkGenerator, list, chunkAccess) -> {
        WorldGenRegion worldGenRegion = new WorldGenRegion(serverLevel, list, chunkStatus, -1);
        chunkGenerator.createReferences(worldGenRegion, serverLevel.structureFeatureManager().forWorldGenRegion(worldGenRegion), chunkAccess);
    });
    public static final ChunkStatus BIOMES = ChunkStatus.register("biomes", STRUCTURE_REFERENCES, 8, PRE_FEATURES, ChunkType.PROTOCHUNK, (chunkStatus, executor, serverLevel, chunkGenerator, structureManager, threadedLevelLightEngine, function, list, chunkAccess2, bl) -> {
        if (bl || !chunkAccess2.getStatus().isOrAfter(chunkStatus)) {
            WorldGenRegion worldGenRegion = new WorldGenRegion(serverLevel, list, chunkStatus, -1);
            return chunkGenerator.createBiomes(serverLevel.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY), executor, Blender.of(worldGenRegion), serverLevel.structureFeatureManager().forWorldGenRegion(worldGenRegion), chunkAccess2).thenApply(chunkAccess -> {
                if (chunkAccess instanceof ProtoChunk) {
                    ((ProtoChunk)chunkAccess).setStatus(chunkStatus);
                }
                return Either.left((Object)chunkAccess);
            });
        }
        return CompletableFuture.completedFuture(Either.left((Object)chunkAccess2));
    });
    public static final ChunkStatus NOISE = ChunkStatus.register("noise", BIOMES, 8, PRE_FEATURES, ChunkType.PROTOCHUNK, (chunkStatus, executor, serverLevel, chunkGenerator, structureManager, threadedLevelLightEngine, function, list, chunkAccess2, bl) -> {
        if (bl || !chunkAccess2.getStatus().isOrAfter(chunkStatus)) {
            WorldGenRegion worldGenRegion = new WorldGenRegion(serverLevel, list, chunkStatus, 0);
            return chunkGenerator.fillFromNoise(executor, Blender.of(worldGenRegion), serverLevel.structureFeatureManager().forWorldGenRegion(worldGenRegion), chunkAccess2).thenApply(chunkAccess -> {
                if (chunkAccess instanceof ProtoChunk) {
                    ProtoChunk protoChunk = (ProtoChunk)chunkAccess;
                    BelowZeroRetrogen belowZeroRetrogen = protoChunk.getBelowZeroRetrogen();
                    if (belowZeroRetrogen != null) {
                        BelowZeroRetrogen.replaceOldBedrock(protoChunk);
                        if (belowZeroRetrogen.hasBedrockHoles()) {
                            belowZeroRetrogen.applyBedrockMask(protoChunk);
                        }
                    }
                    protoChunk.setStatus(chunkStatus);
                }
                return Either.left((Object)chunkAccess);
            });
        }
        return CompletableFuture.completedFuture(Either.left((Object)chunkAccess2));
    });
    public static final ChunkStatus SURFACE = ChunkStatus.registerSimple("surface", NOISE, 8, PRE_FEATURES, ChunkType.PROTOCHUNK, (chunkStatus, serverLevel, chunkGenerator, list, chunkAccess) -> {
        WorldGenRegion worldGenRegion = new WorldGenRegion(serverLevel, list, chunkStatus, 0);
        chunkGenerator.buildSurface(worldGenRegion, serverLevel.structureFeatureManager().forWorldGenRegion(worldGenRegion), chunkAccess);
    });
    public static final ChunkStatus CARVERS = ChunkStatus.registerSimple("carvers", SURFACE, 8, PRE_FEATURES, ChunkType.PROTOCHUNK, (chunkStatus, serverLevel, chunkGenerator, list, chunkAccess) -> {
        WorldGenRegion worldGenRegion = new WorldGenRegion(serverLevel, list, chunkStatus, 0);
        if (chunkAccess instanceof ProtoChunk) {
            ProtoChunk protoChunk = (ProtoChunk)chunkAccess;
            Blender.addAroundOldChunksCarvingMaskFilter(worldGenRegion, protoChunk);
        }
        chunkGenerator.applyCarvers(worldGenRegion, serverLevel.getSeed(), serverLevel.getBiomeManager(), serverLevel.structureFeatureManager().forWorldGenRegion(worldGenRegion), chunkAccess, GenerationStep.Carving.AIR);
    });
    public static final ChunkStatus LIQUID_CARVERS = ChunkStatus.registerSimple("liquid_carvers", CARVERS, 8, POST_FEATURES, ChunkType.PROTOCHUNK, (chunkStatus, serverLevel, chunkGenerator, list, chunkAccess) -> {});
    public static final ChunkStatus FEATURES = ChunkStatus.register("features", LIQUID_CARVERS, 8, POST_FEATURES, ChunkType.PROTOCHUNK, (chunkStatus, executor, serverLevel, chunkGenerator, structureManager, threadedLevelLightEngine, function, list, chunkAccess, bl) -> {
        ProtoChunk protoChunk = (ProtoChunk)chunkAccess;
        protoChunk.setLightEngine(threadedLevelLightEngine);
        if (bl || !chunkAccess.getStatus().isOrAfter(chunkStatus)) {
            Heightmap.primeHeightmaps(chunkAccess, EnumSet.of(Heightmap.Types.MOTION_BLOCKING, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Heightmap.Types.OCEAN_FLOOR, Heightmap.Types.WORLD_SURFACE));
            WorldGenRegion worldGenRegion = new WorldGenRegion(serverLevel, list, chunkStatus, 1);
            chunkGenerator.applyBiomeDecoration(worldGenRegion, chunkAccess, serverLevel.structureFeatureManager().forWorldGenRegion(worldGenRegion));
            Blender.generateBorderTicks(worldGenRegion, chunkAccess);
            protoChunk.setStatus(chunkStatus);
        }
        return CompletableFuture.completedFuture(Either.left((Object)chunkAccess));
    });
    public static final ChunkStatus LIGHT = ChunkStatus.register("light", FEATURES, 1, POST_FEATURES, ChunkType.PROTOCHUNK, (chunkStatus, executor, serverLevel, chunkGenerator, structureManager, threadedLevelLightEngine, function, list, chunkAccess, bl) -> ChunkStatus.lightChunk(chunkStatus, threadedLevelLightEngine, chunkAccess), (chunkStatus, serverLevel, structureManager, threadedLevelLightEngine, function, chunkAccess) -> ChunkStatus.lightChunk(chunkStatus, threadedLevelLightEngine, chunkAccess));
    public static final ChunkStatus SPAWN = ChunkStatus.registerSimple("spawn", LIGHT, 0, POST_FEATURES, ChunkType.PROTOCHUNK, (chunkStatus, serverLevel, chunkGenerator, list, chunkAccess) -> {
        if (!chunkAccess.isUpgrading()) {
            chunkGenerator.spawnOriginalMobs(new WorldGenRegion(serverLevel, list, chunkStatus, -1));
        }
    });
    public static final ChunkStatus HEIGHTMAPS = ChunkStatus.registerSimple("heightmaps", SPAWN, 0, POST_FEATURES, ChunkType.PROTOCHUNK, (chunkStatus, serverLevel, chunkGenerator, list, chunkAccess) -> {});
    public static final ChunkStatus FULL = ChunkStatus.register("full", HEIGHTMAPS, 0, POST_FEATURES, ChunkType.LEVELCHUNK, (chunkStatus, executor, serverLevel, chunkGenerator, structureManager, threadedLevelLightEngine, function, list, chunkAccess, bl) -> (CompletableFuture)function.apply(chunkAccess), (chunkStatus, serverLevel, structureManager, threadedLevelLightEngine, function, chunkAccess) -> (CompletableFuture)function.apply(chunkAccess));
    private static final List<ChunkStatus> STATUS_BY_RANGE = ImmutableList.of((Object)FULL, (Object)FEATURES, (Object)LIQUID_CARVERS, (Object)BIOMES, (Object)STRUCTURE_STARTS, (Object)STRUCTURE_STARTS, (Object)STRUCTURE_STARTS, (Object)STRUCTURE_STARTS, (Object)STRUCTURE_STARTS, (Object)STRUCTURE_STARTS, (Object)STRUCTURE_STARTS, (Object)STRUCTURE_STARTS, (Object[])new ChunkStatus[0]);
    private static final IntList RANGE_BY_STATUS = (IntList)Util.make(new IntArrayList(ChunkStatus.getStatusList().size()), intArrayList -> {
        int n = 0;
        for (int i = ChunkStatus.getStatusList().size() - 1; i >= 0; --i) {
            while (n + 1 < STATUS_BY_RANGE.size() && i <= STATUS_BY_RANGE.get(n + 1).getIndex()) {
                ++n;
            }
            intArrayList.add(0, n);
        }
    });
    private final String name;
    private final int index;
    private final ChunkStatus parent;
    private final GenerationTask generationTask;
    private final LoadingTask loadingTask;
    private final int range;
    private final ChunkType chunkType;
    private final EnumSet<Heightmap.Types> heightmapsAfter;

    private static CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> lightChunk(ChunkStatus chunkStatus, ThreadedLevelLightEngine threadedLevelLightEngine, ChunkAccess chunkAccess) {
        boolean bl = ChunkStatus.isLighted(chunkStatus, chunkAccess);
        if (!chunkAccess.getStatus().isOrAfter(chunkStatus)) {
            ((ProtoChunk)chunkAccess).setStatus(chunkStatus);
        }
        return threadedLevelLightEngine.lightChunk(chunkAccess, bl).thenApply(Either::left);
    }

    private static ChunkStatus registerSimple(String string, @Nullable ChunkStatus chunkStatus, int n, EnumSet<Heightmap.Types> enumSet, ChunkType chunkType, SimpleGenerationTask simpleGenerationTask) {
        return ChunkStatus.register(string, chunkStatus, n, enumSet, chunkType, simpleGenerationTask);
    }

    private static ChunkStatus register(String string, @Nullable ChunkStatus chunkStatus, int n, EnumSet<Heightmap.Types> enumSet, ChunkType chunkType, GenerationTask generationTask) {
        return ChunkStatus.register(string, chunkStatus, n, enumSet, chunkType, generationTask, PASSTHROUGH_LOAD_TASK);
    }

    private static ChunkStatus register(String string, @Nullable ChunkStatus chunkStatus, int n, EnumSet<Heightmap.Types> enumSet, ChunkType chunkType, GenerationTask generationTask, LoadingTask loadingTask) {
        return Registry.register(Registry.CHUNK_STATUS, string, new ChunkStatus(string, chunkStatus, n, enumSet, chunkType, generationTask, loadingTask));
    }

    public static List<ChunkStatus> getStatusList() {
        ChunkStatus chunkStatus;
        ArrayList arrayList = Lists.newArrayList();
        for (chunkStatus = FULL; chunkStatus.getParent() != chunkStatus; chunkStatus = chunkStatus.getParent()) {
            arrayList.add(chunkStatus);
        }
        arrayList.add(chunkStatus);
        Collections.reverse(arrayList);
        return arrayList;
    }

    private static boolean isLighted(ChunkStatus chunkStatus, ChunkAccess chunkAccess) {
        return chunkAccess.getStatus().isOrAfter(chunkStatus) && chunkAccess.isLightCorrect();
    }

    public static ChunkStatus getStatusAroundFullChunk(int n) {
        if (n >= STATUS_BY_RANGE.size()) {
            return EMPTY;
        }
        if (n < 0) {
            return FULL;
        }
        return STATUS_BY_RANGE.get(n);
    }

    public static int maxDistance() {
        return STATUS_BY_RANGE.size();
    }

    public static int getDistance(ChunkStatus chunkStatus) {
        return RANGE_BY_STATUS.getInt(chunkStatus.getIndex());
    }

    ChunkStatus(String string, @Nullable ChunkStatus chunkStatus, int n, EnumSet<Heightmap.Types> enumSet, ChunkType chunkType, GenerationTask generationTask, LoadingTask loadingTask) {
        this.name = string;
        this.parent = chunkStatus == null ? this : chunkStatus;
        this.generationTask = generationTask;
        this.loadingTask = loadingTask;
        this.range = n;
        this.chunkType = chunkType;
        this.heightmapsAfter = enumSet;
        this.index = chunkStatus == null ? 0 : chunkStatus.getIndex() + 1;
    }

    public int getIndex() {
        return this.index;
    }

    public String getName() {
        return this.name;
    }

    public ChunkStatus getParent() {
        return this.parent;
    }

    public CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> generate(Executor executor, ServerLevel serverLevel, ChunkGenerator chunkGenerator, StructureManager structureManager, ThreadedLevelLightEngine threadedLevelLightEngine, Function<ChunkAccess, CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> function, List<ChunkAccess> list, boolean bl) {
        ChunkAccess chunkAccess = list.get(list.size() / 2);
        ProfiledDuration profiledDuration = JvmProfiler.INSTANCE.onChunkGenerate(chunkAccess.getPos(), serverLevel.dimension(), this.name);
        CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completableFuture = this.generationTask.doWork(this, executor, serverLevel, chunkGenerator, structureManager, threadedLevelLightEngine, function, list, chunkAccess, bl);
        return profiledDuration != null ? completableFuture.thenApply(either -> {
            profiledDuration.finish();
            return either;
        }) : completableFuture;
    }

    public CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> load(ServerLevel serverLevel, StructureManager structureManager, ThreadedLevelLightEngine threadedLevelLightEngine, Function<ChunkAccess, CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> function, ChunkAccess chunkAccess) {
        return this.loadingTask.doWork(this, serverLevel, structureManager, threadedLevelLightEngine, function, chunkAccess);
    }

    public int getRange() {
        return this.range;
    }

    public ChunkType getChunkType() {
        return this.chunkType;
    }

    public static ChunkStatus byName(String string) {
        return Registry.CHUNK_STATUS.get(ResourceLocation.tryParse(string));
    }

    public EnumSet<Heightmap.Types> heightmapsAfter() {
        return this.heightmapsAfter;
    }

    public boolean isOrAfter(ChunkStatus chunkStatus) {
        return this.getIndex() >= chunkStatus.getIndex();
    }

    public String toString() {
        return Registry.CHUNK_STATUS.getKey(this).toString();
    }

    public static enum ChunkType {
        PROTOCHUNK,
        LEVELCHUNK;

    }

    static interface GenerationTask {
        public CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> doWork(ChunkStatus var1, Executor var2, ServerLevel var3, ChunkGenerator var4, StructureManager var5, ThreadedLevelLightEngine var6, Function<ChunkAccess, CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> var7, List<ChunkAccess> var8, ChunkAccess var9, boolean var10);
    }

    static interface LoadingTask {
        public CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> doWork(ChunkStatus var1, ServerLevel var2, StructureManager var3, ThreadedLevelLightEngine var4, Function<ChunkAccess, CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> var5, ChunkAccess var6);
    }

    static interface SimpleGenerationTask
    extends GenerationTask {
        @Override
        default public CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> doWork(ChunkStatus chunkStatus, Executor executor, ServerLevel serverLevel, ChunkGenerator chunkGenerator, StructureManager structureManager, ThreadedLevelLightEngine threadedLevelLightEngine, Function<ChunkAccess, CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> function, List<ChunkAccess> list, ChunkAccess chunkAccess, boolean bl) {
            if (bl || !chunkAccess.getStatus().isOrAfter(chunkStatus)) {
                this.doWork(chunkStatus, serverLevel, chunkGenerator, list, chunkAccess);
                if (chunkAccess instanceof ProtoChunk) {
                    ProtoChunk protoChunk = (ProtoChunk)chunkAccess;
                    protoChunk.setStatus(chunkStatus);
                }
            }
            return CompletableFuture.completedFuture(Either.left((Object)chunkAccess));
        }

        public void doWork(ChunkStatus var1, ServerLevel var2, ChunkGenerator var3, List<ChunkAccess> var4, ChunkAccess var5);
    }
}

