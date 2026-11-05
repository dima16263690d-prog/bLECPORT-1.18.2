/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.google.common.collect.Iterables
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Queues
 *  com.google.common.collect.Sets
 *  com.mojang.datafixers.DataFixer
 *  com.mojang.datafixers.util.Either
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.longs.Long2ByteMap
 *  it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap
 *  it.unimi.dsi.fastutil.longs.Long2LongMap
 *  it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap$Entry
 *  it.unimi.dsi.fastutil.longs.LongIterator
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  it.unimi.dsi.fastutil.longs.LongSet
 *  it.unimi.dsi.fastutil.objects.ObjectIterator
 *  javax.annotation.Nullable
 *  org.apache.commons.lang3.mutable.MutableBoolean
 *  org.apache.commons.lang3.mutable.MutableObject
 *  org.slf4j.Logger
 */
package net.minecraft.server.level;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheCenterPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkTaskPriorityQueue;
import net.minecraft.server.level.ChunkTaskPriorityQueueSorter;
import net.minecraft.server.level.PlayerMap;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.level.TickingTracker;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.util.CsvOutput;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.util.thread.ProcessorHandle;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.entity.ChunkStatusUpdateListener;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableObject;
import org.slf4j.Logger;

public class ChunkMap
extends ChunkStorage
implements ChunkHolder.PlayerProvider {
    private static final byte CHUNK_TYPE_REPLACEABLE = -1;
    private static final byte CHUNK_TYPE_UNKNOWN = 0;
    private static final byte CHUNK_TYPE_FULL = 1;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int CHUNK_SAVED_PER_TICK = 200;
    private static final int CHUNK_SAVED_EAGERLY_PER_TICK = 20;
    private static final int EAGER_CHUNK_SAVE_COOLDOWN_IN_MILLIS = 10000;
    private static final int MIN_VIEW_DISTANCE = 3;
    public static final int MAX_VIEW_DISTANCE = 33;
    public static final int MAX_CHUNK_DISTANCE = 33 + ChunkStatus.maxDistance();
    public static final int FORCED_TICKET_LEVEL = 31;
    private final Long2ObjectLinkedOpenHashMap<ChunkHolder> updatingChunkMap = new Long2ObjectLinkedOpenHashMap();
    private volatile Long2ObjectLinkedOpenHashMap<ChunkHolder> visibleChunkMap = this.updatingChunkMap.clone();
    private final Long2ObjectLinkedOpenHashMap<ChunkHolder> pendingUnloads = new Long2ObjectLinkedOpenHashMap();
    private final LongSet entitiesInLevel = new LongOpenHashSet();
    final ServerLevel level;
    private final ThreadedLevelLightEngine lightEngine;
    private final BlockableEventLoop<Runnable> mainThreadExecutor;
    private ChunkGenerator generator;
    private final Supplier<DimensionDataStorage> overworldDataStorage;
    private final PoiManager poiManager;
    final LongSet toDrop = new LongOpenHashSet();
    private boolean modified;
    private final ChunkTaskPriorityQueueSorter queueSorter;
    private final ProcessorHandle<ChunkTaskPriorityQueueSorter.Message<Runnable>> worldgenMailbox;
    private final ProcessorHandle<ChunkTaskPriorityQueueSorter.Message<Runnable>> mainThreadMailbox;
    private final ChunkProgressListener progressListener;
    private final ChunkStatusUpdateListener chunkStatusListener;
    private final DistanceManager distanceManager;
    private final AtomicInteger tickingGenerated = new AtomicInteger();
    private final StructureManager structureManager;
    private final String storageName;
    private final PlayerMap playerMap = new PlayerMap();
    private final Int2ObjectMap<TrackedEntity> entityMap = new Int2ObjectOpenHashMap();
    private final Long2ByteMap chunkTypeCache = new Long2ByteOpenHashMap();
    private final Long2LongMap chunkSaveCooldowns = new Long2LongOpenHashMap();
    private final Queue<Runnable> unloadQueue = Queues.newConcurrentLinkedQueue();
    int viewDistance;

    public ChunkMap(ServerLevel serverLevel, LevelStorageSource.LevelStorageAccess levelStorageAccess, DataFixer dataFixer, StructureManager structureManager, Executor executor, BlockableEventLoop<Runnable> blockableEventLoop, LightChunkGetter lightChunkGetter, ChunkGenerator chunkGenerator, ChunkProgressListener chunkProgressListener, ChunkStatusUpdateListener chunkStatusUpdateListener, Supplier<DimensionDataStorage> supplier, int n, boolean bl) {
        super(levelStorageAccess.getDimensionPath(serverLevel.dimension()).resolve("region"), dataFixer, bl);
        this.structureManager = structureManager;
        Path path = levelStorageAccess.getDimensionPath(serverLevel.dimension());
        this.storageName = path.getFileName().toString();
        this.level = serverLevel;
        this.generator = chunkGenerator;
        this.mainThreadExecutor = blockableEventLoop;
        ProcessorMailbox<Runnable> processorMailbox = ProcessorMailbox.create(executor, "worldgen");
        ProcessorHandle<Runnable> processorHandle = ProcessorHandle.of("main", blockableEventLoop::tell);
        this.progressListener = chunkProgressListener;
        this.chunkStatusListener = chunkStatusUpdateListener;
        ProcessorMailbox<Runnable> processorMailbox2 = ProcessorMailbox.create(executor, "light");
        this.queueSorter = new ChunkTaskPriorityQueueSorter((List<ProcessorHandle<?>>)ImmutableList.of(processorMailbox, processorHandle, processorMailbox2), executor, Integer.MAX_VALUE);
        this.worldgenMailbox = this.queueSorter.getProcessor(processorMailbox, false);
        this.mainThreadMailbox = this.queueSorter.getProcessor(processorHandle, false);
        this.lightEngine = new ThreadedLevelLightEngine(lightChunkGetter, this, this.level.dimensionType().hasSkyLight(), processorMailbox2, this.queueSorter.getProcessor(processorMailbox2, false));
        this.distanceManager = new DistanceManager(executor, blockableEventLoop);
        this.overworldDataStorage = supplier;
        this.poiManager = new PoiManager(path.resolve("poi"), dataFixer, bl, serverLevel);
        this.setViewDistance(n);
    }

    protected ChunkGenerator generator() {
        return this.generator;
    }

    public void debugReloadGenerator() {
        DataResult dataResult = ChunkGenerator.CODEC.encodeStart((DynamicOps)JsonOps.INSTANCE, (Object)this.generator);
        DataResult dataResult2 = dataResult.flatMap(jsonElement -> ChunkGenerator.CODEC.parse((DynamicOps)JsonOps.INSTANCE, jsonElement));
        dataResult2.result().ifPresent(chunkGenerator -> {
            this.generator = chunkGenerator;
        });
    }

    private static double euclideanDistanceSquared(ChunkPos chunkPos, Entity entity) {
        double d = SectionPos.sectionToBlockCoord(chunkPos.x, 8);
        double d2 = SectionPos.sectionToBlockCoord(chunkPos.z, 8);
        double d3 = d - entity.getX();
        double d4 = d2 - entity.getZ();
        return d3 * d3 + d4 * d4;
    }

    public static boolean isChunkInRange(int n, int n2, int n3, int n4, int n5) {
        int n6;
        int n7;
        int n8 = Math.max(0, Math.abs(n - n3) - 1);
        int n9 = Math.max(0, Math.abs(n2 - n4) - 1);
        long l = Math.max(0, Math.max(n8, n9) - 1);
        long l2 = Math.min(n8, n9);
        long l3 = l2 * l2 + l * l;
        return l3 <= (long)(n7 = (n6 = n5 - 1) * n6);
    }

    private static boolean isChunkOnRangeBorder(int n, int n2, int n3, int n4, int n5) {
        if (!ChunkMap.isChunkInRange(n, n2, n3, n4, n5)) {
            return false;
        }
        if (!ChunkMap.isChunkInRange(n + 1, n2, n3, n4, n5)) {
            return true;
        }
        if (!ChunkMap.isChunkInRange(n, n2 + 1, n3, n4, n5)) {
            return true;
        }
        if (!ChunkMap.isChunkInRange(n - 1, n2, n3, n4, n5)) {
            return true;
        }
        return !ChunkMap.isChunkInRange(n, n2 - 1, n3, n4, n5);
    }

    protected ThreadedLevelLightEngine getLightEngine() {
        return this.lightEngine;
    }

    @Nullable
    protected ChunkHolder getUpdatingChunkIfPresent(long l) {
        return (ChunkHolder)this.updatingChunkMap.get(l);
    }

    @Nullable
    protected ChunkHolder getVisibleChunkIfPresent(long l) {
        return (ChunkHolder)this.visibleChunkMap.get(l);
    }

    protected IntSupplier getChunkQueueLevel(long l) {
        return () -> {
            ChunkHolder chunkHolder = this.getVisibleChunkIfPresent(l);
            if (chunkHolder == null) {
                return ChunkTaskPriorityQueue.PRIORITY_LEVEL_COUNT - 1;
            }
            return Math.min(chunkHolder.getQueueLevel(), ChunkTaskPriorityQueue.PRIORITY_LEVEL_COUNT - 1);
        };
    }

    public String getChunkDebugData(ChunkPos chunkPos) {
        ChunkHolder chunkHolder = this.getVisibleChunkIfPresent(chunkPos.toLong());
        if (chunkHolder == null) {
            return "null";
        }
        String string = chunkHolder.getTicketLevel() + "\n";
        ChunkStatus chunkStatus = chunkHolder.getLastAvailableStatus();
        ChunkAccess chunkAccess = chunkHolder.getLastAvailable();
        if (chunkStatus != null) {
            string = string + "St: \u00a7" + chunkStatus.getIndex() + chunkStatus + "\u00a7r\n";
        }
        if (chunkAccess != null) {
            string = string + "Ch: \u00a7" + chunkAccess.getStatus().getIndex() + chunkAccess.getStatus() + "\u00a7r\n";
        }
        ChunkHolder.FullChunkStatus fullChunkStatus = chunkHolder.getFullStatus();
        string = string + "\u00a7" + fullChunkStatus.ordinal() + fullChunkStatus;
        return string + "\u00a7r";
    }

    private CompletableFuture<Either<List<ChunkAccess>, ChunkHolder.ChunkLoadingFailure>> getChunkRangeFuture(ChunkPos chunkPos, final int n, IntFunction<ChunkStatus> intFunction) {
        ArrayList<CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> arrayList = new ArrayList<CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>>();
        ArrayList<ChunkHolder> arrayList2 = new ArrayList<ChunkHolder>();
        int n2 = chunkPos.x;
        int n3 = chunkPos.z;
        for (int i = -n; i <= n; ++i) {
            for (int j = -n; j <= n; ++j) {
                int n4 = Math.max(Math.abs(j), Math.abs(i));
                final ChunkPos object = new ChunkPos(n2 + j, n3 + i);
                long l = object.toLong();
                ChunkHolder chunkHolder = this.getUpdatingChunkIfPresent(l);
                if (chunkHolder == null) {
                    return CompletableFuture.completedFuture(Either.right((Object)new ChunkHolder.ChunkLoadingFailure(){

                        public String toString() {
                            return "Unloaded " + object;
                        }
                    }));
                }
                ChunkStatus chunkStatus = intFunction.apply(n4);
                CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completableFuture = chunkHolder.getOrScheduleFuture(chunkStatus, this);
                arrayList2.add(chunkHolder);
                arrayList.add(completableFuture);
            }
        }
        CompletableFuture completableFuture = Util.sequence(arrayList);
        CompletionStage completionStage = completableFuture.thenApply(list -> {
            ArrayList arrayList = Lists.newArrayList();
            int n4 = 0;
            for (final Either either : list) {
                if (either == null) {
                    throw this.debugFuturesAndCreateReportedException(new IllegalStateException("At least one of the chunk futures were null"), "n/a");
                }
                Optional optional = either.left();
                if (!optional.isPresent()) {
                    final int n5 = n4;
                    return Either.right((Object)new ChunkHolder.ChunkLoadingFailure(){

                        public String toString() {
                            return "Unloaded " + new ChunkPos(n2 + n5 % (n * 2 + 1), n3 + n5 / (n * 2 + 1)) + " " + either.right().get();
                        }
                    });
                }
                arrayList.add((ChunkAccess)optional.get());
                ++n4;
            }
            return Either.left((Object)arrayList);
        });
        for (ChunkHolder chunkHolder : arrayList2) {
            chunkHolder.addSaveDependency("getChunkRangeFuture " + chunkPos + " " + n, (CompletableFuture<?>)completionStage);
        }
        return completionStage;
    }

    public ReportedException debugFuturesAndCreateReportedException(IllegalStateException illegalStateException, String string) {
        StringBuilder stringBuilder = new StringBuilder();
        Consumer<ChunkHolder> consumer = chunkHolder -> chunkHolder.getAllFutures().forEach(pair -> {
            ChunkStatus chunkStatus = (ChunkStatus)pair.getFirst();
            CompletableFuture completableFuture = (CompletableFuture)pair.getSecond();
            if (completableFuture != null && completableFuture.isDone() && completableFuture.join() == null) {
                stringBuilder.append(chunkHolder.getPos()).append(" - status: ").append(chunkStatus).append(" future: ").append(completableFuture).append(System.lineSeparator());
            }
        });
        stringBuilder.append("Updating:").append(System.lineSeparator());
        this.updatingChunkMap.values().forEach(consumer);
        stringBuilder.append("Visible:").append(System.lineSeparator());
        this.visibleChunkMap.values().forEach(consumer);
        CrashReport crashReport = CrashReport.forThrowable(illegalStateException, "Chunk loading");
        CrashReportCategory crashReportCategory = crashReport.addCategory("Chunk loading");
        crashReportCategory.setDetail("Details", string);
        crashReportCategory.setDetail("Futures", stringBuilder);
        return new ReportedException(crashReport);
    }

    public CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> prepareEntityTickingChunk(ChunkPos chunkPos) {
        return this.getChunkRangeFuture(chunkPos, 2, n -> ChunkStatus.FULL).thenApplyAsync(either -> either.mapLeft(list -> (LevelChunk)list.get(list.size() / 2)), (Executor)this.mainThreadExecutor);
    }

    @Nullable
    ChunkHolder updateChunkScheduling(long l, int n, @Nullable ChunkHolder chunkHolder, int n2) {
        if (n2 > MAX_CHUNK_DISTANCE && n > MAX_CHUNK_DISTANCE) {
            return chunkHolder;
        }
        if (chunkHolder != null) {
            chunkHolder.setTicketLevel(n);
        }
        if (chunkHolder != null) {
            if (n > MAX_CHUNK_DISTANCE) {
                this.toDrop.add(l);
            } else {
                this.toDrop.remove(l);
            }
        }
        if (n <= MAX_CHUNK_DISTANCE && chunkHolder == null) {
            chunkHolder = (ChunkHolder)this.pendingUnloads.remove(l);
            if (chunkHolder != null) {
                chunkHolder.setTicketLevel(n);
            } else {
                chunkHolder = new ChunkHolder(new ChunkPos(l), n, this.level, this.lightEngine, this.queueSorter, this);
            }
            this.updatingChunkMap.put(l, (Object)chunkHolder);
            this.modified = true;
        }
        return chunkHolder;
    }

    @Override
    public void close() throws IOException {
        try {
            this.queueSorter.close();
            this.poiManager.close();
        }
        finally {
            super.close();
        }
    }

    protected void saveAllChunks(boolean bl) {
        if (bl) {
            List list = this.visibleChunkMap.values().stream().filter(ChunkHolder::wasAccessibleSinceLastSave).peek(ChunkHolder::refreshAccessibility).collect(Collectors.toList());
            MutableBoolean mutableBoolean = new MutableBoolean();
            do {
                mutableBoolean.setFalse();
                list.stream().map(chunkHolder -> {
                    CompletableFuture<ChunkAccess> completableFuture;
                    do {
                        completableFuture = chunkHolder.getChunkToSave();
                        this.mainThreadExecutor.managedBlock(completableFuture::isDone);
                    } while (completableFuture != chunkHolder.getChunkToSave());
                    return completableFuture.join();
                }).filter(chunkAccess -> chunkAccess instanceof ImposterProtoChunk || chunkAccess instanceof LevelChunk).filter(this::save).forEach(chunkAccess -> mutableBoolean.setTrue());
            } while (mutableBoolean.isTrue());
            this.processUnloads(() -> true);
            this.flushWorker();
        } else {
            this.visibleChunkMap.values().forEach(this::saveChunkIfNeeded);
        }
    }

    protected void tick(BooleanSupplier booleanSupplier) {
        ProfilerFiller profilerFiller = this.level.getProfiler();
        profilerFiller.push("poi");
        this.poiManager.tick(booleanSupplier);
        profilerFiller.popPush("chunk_unload");
        if (!this.level.noSave()) {
            this.processUnloads(booleanSupplier);
        }
        profilerFiller.pop();
    }

    public boolean hasWork() {
        return this.lightEngine.hasLightWork() || !this.pendingUnloads.isEmpty() || !this.updatingChunkMap.isEmpty() || this.poiManager.hasWork() || !this.toDrop.isEmpty() || !this.unloadQueue.isEmpty() || this.queueSorter.hasWork() || this.distanceManager.hasTickets();
    }

    private void processUnloads(BooleanSupplier booleanSupplier) {
        Runnable runnable;
        LongIterator longIterator = this.toDrop.iterator();
        int n = 0;
        while (longIterator.hasNext() && (booleanSupplier.getAsBoolean() || n < 200 || this.toDrop.size() > 2000)) {
            long l = longIterator.nextLong();
            ChunkHolder chunkHolder = (ChunkHolder)this.updatingChunkMap.remove(l);
            if (chunkHolder != null) {
                this.pendingUnloads.put(l, (Object)chunkHolder);
                this.modified = true;
                ++n;
                this.scheduleUnload(l, chunkHolder);
            }
            longIterator.remove();
        }
        for (int i = Math.max(0, this.unloadQueue.size() - 2000); (booleanSupplier.getAsBoolean() || i > 0) && (runnable = this.unloadQueue.poll()) != null; --i) {
            runnable.run();
        }
        int n2 = 0;
        ObjectIterator objectIterator = this.visibleChunkMap.values().iterator();
        while (n2 < 20 && booleanSupplier.getAsBoolean() && objectIterator.hasNext()) {
            if (!this.saveChunkIfNeeded((ChunkHolder)objectIterator.next())) continue;
            ++n2;
        }
    }

    private void scheduleUnload(long l, ChunkHolder chunkHolder) {
        CompletableFuture<ChunkAccess> completableFuture = chunkHolder.getChunkToSave();
        ((CompletableFuture)completableFuture.thenAcceptAsync(chunkAccess -> {
            CompletableFuture<ChunkAccess> completableFuture2 = chunkHolder.getChunkToSave();
            if (completableFuture2 != completableFuture) {
                this.scheduleUnload(l, chunkHolder);
                return;
            }
            if (this.pendingUnloads.remove(l, (Object)chunkHolder) && chunkAccess != null) {
                if (chunkAccess instanceof LevelChunk) {
                    ((LevelChunk)chunkAccess).setLoaded(false);
                }
                this.save((ChunkAccess)chunkAccess);
                if (this.entitiesInLevel.remove(l) && chunkAccess instanceof LevelChunk) {
                    LevelChunk levelChunk = (LevelChunk)chunkAccess;
                    this.level.unload(levelChunk);
                }
                this.lightEngine.updateChunkStatus(chunkAccess.getPos());
                this.lightEngine.tryScheduleUpdate();
                this.progressListener.onStatusChange(chunkAccess.getPos(), null);
                this.chunkSaveCooldowns.remove(chunkAccess.getPos().toLong());
            }
        }, this.unloadQueue::add)).whenComplete((void_, throwable) -> {
            if (throwable != null) {
                LOGGER.error("Failed to save chunk {}", (Object)chunkHolder.getPos(), throwable);
            }
        });
    }

    protected boolean promoteChunkMap() {
        if (!this.modified) {
            return false;
        }
        this.visibleChunkMap = this.updatingChunkMap.clone();
        this.modified = false;
        return true;
    }

    public CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> schedule(ChunkHolder chunkHolder, ChunkStatus chunkStatus) {
        Optional optional;
        ChunkPos chunkPos = chunkHolder.getPos();
        if (chunkStatus == ChunkStatus.EMPTY) {
            return this.scheduleChunkLoad(chunkPos);
        }
        if (chunkStatus == ChunkStatus.LIGHT) {
            this.distanceManager.addTicket(TicketType.LIGHT, chunkPos, 33 + ChunkStatus.getDistance(ChunkStatus.LIGHT), chunkPos);
        }
        if ((optional = chunkHolder.getOrScheduleFuture(chunkStatus.getParent(), this).getNow(ChunkHolder.UNLOADED_CHUNK).left()).isPresent() && ((ChunkAccess)optional.get()).getStatus().isOrAfter(chunkStatus)) {
            CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completableFuture = chunkStatus.load(this.level, this.structureManager, this.lightEngine, chunkAccess -> this.protoChunkToFullChunk(chunkHolder), (ChunkAccess)optional.get());
            this.progressListener.onStatusChange(chunkPos, chunkStatus);
            return completableFuture;
        }
        return this.scheduleChunkGeneration(chunkHolder, chunkStatus);
    }

    private CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> scheduleChunkLoad(ChunkPos chunkPos) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                this.level.getProfiler().incrementCounter("chunkLoad");
                CompoundTag compoundTag = this.readChunk(chunkPos);
                if (compoundTag != null) {
                    boolean bl = compoundTag.contains("Status", 8);
                    if (bl) {
                        ProtoChunk protoChunk = ChunkSerializer.read(this.level, this.poiManager, chunkPos, compoundTag);
                        this.markPosition(chunkPos, ((ChunkAccess)protoChunk).getStatus().getChunkType());
                        return Either.left((Object)protoChunk);
                    }
                    LOGGER.error("Chunk file at {} is missing level data, skipping", (Object)chunkPos);
                }
            }
            catch (ReportedException reportedException) {
                Throwable throwable = reportedException.getCause();
                if (throwable instanceof IOException) {
                    LOGGER.error("Couldn't load chunk {}", (Object)chunkPos, (Object)throwable);
                }
                this.markPositionReplaceable(chunkPos);
                throw reportedException;
            }
            catch (Exception exception) {
                LOGGER.error("Couldn't load chunk {}", (Object)chunkPos, (Object)exception);
            }
            this.markPositionReplaceable(chunkPos);
            return Either.left((Object)new ProtoChunk(chunkPos, UpgradeData.EMPTY, this.level, this.level.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY), null));
        }, this.mainThreadExecutor);
    }

    private void markPositionReplaceable(ChunkPos chunkPos) {
        this.chunkTypeCache.put(chunkPos.toLong(), (byte)-1);
    }

    private byte markPosition(ChunkPos chunkPos, ChunkStatus.ChunkType chunkType) {
        return this.chunkTypeCache.put(chunkPos.toLong(), chunkType == ChunkStatus.ChunkType.PROTOCHUNK ? (byte)-1 : 1);
    }

    private CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> scheduleChunkGeneration(ChunkHolder chunkHolder, ChunkStatus chunkStatus) {
        ChunkPos chunkPos = chunkHolder.getPos();
        CompletableFuture<Either<List<ChunkAccess>, ChunkHolder.ChunkLoadingFailure>> completableFuture = this.getChunkRangeFuture(chunkPos, chunkStatus.getRange(), n -> this.getDependencyStatus(chunkStatus, n));
        this.level.getProfiler().incrementCounter(() -> "chunkGenerate " + chunkStatus.getName());
        Executor executor = runnable -> this.worldgenMailbox.tell(ChunkTaskPriorityQueueSorter.message(chunkHolder, runnable));
        return completableFuture.thenComposeAsync(either -> (CompletionStage)either.map(list -> {
            try {
                CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completableFuture = chunkStatus.generate(executor, this.level, this.generator, this.structureManager, this.lightEngine, chunkAccess -> this.protoChunkToFullChunk(chunkHolder), (List<ChunkAccess>)list, false);
                this.progressListener.onStatusChange(chunkPos, chunkStatus);
                return completableFuture;
            }
            catch (Exception exception) {
                exception.getStackTrace();
                CrashReport crashReport = CrashReport.forThrowable(exception, "Exception generating new chunk");
                CrashReportCategory crashReportCategory = crashReport.addCategory("Chunk to be generated");
                crashReportCategory.setDetail("Location", String.format("%d,%d", chunkPos.x, chunkPos.z));
                crashReportCategory.setDetail("Position hash", ChunkPos.asLong(chunkPos.x, chunkPos.z));
                crashReportCategory.setDetail("Generator", this.generator);
                this.mainThreadExecutor.execute(() -> {
                    throw new ReportedException(crashReport);
                });
                throw new ReportedException(crashReport);
            }
        }, chunkLoadingFailure -> {
            this.releaseLightTicket(chunkPos);
            return CompletableFuture.completedFuture(Either.right((Object)chunkLoadingFailure));
        }), executor);
    }

    protected void releaseLightTicket(ChunkPos chunkPos) {
        this.mainThreadExecutor.tell(Util.name(() -> this.distanceManager.removeTicket(TicketType.LIGHT, chunkPos, 33 + ChunkStatus.getDistance(ChunkStatus.LIGHT), chunkPos), () -> "release light ticket " + chunkPos));
    }

    private ChunkStatus getDependencyStatus(ChunkStatus chunkStatus, int n) {
        ChunkStatus chunkStatus2 = n == 0 ? chunkStatus.getParent() : ChunkStatus.getStatusAroundFullChunk(ChunkStatus.getDistance(chunkStatus) + n);
        return chunkStatus2;
    }

    private static void postLoadProtoChunk(ServerLevel serverLevel, List<CompoundTag> list) {
        if (!list.isEmpty()) {
            serverLevel.addWorldGenChunkEntities(EntityType.loadEntitiesRecursive(list, serverLevel));
        }
    }

    private CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> protoChunkToFullChunk(ChunkHolder chunkHolder) {
        CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completableFuture = chunkHolder.getFutureIfPresentUnchecked(ChunkStatus.FULL.getParent());
        return completableFuture.thenApplyAsync(either -> {
            ChunkStatus chunkStatus = ChunkHolder.getStatus(chunkHolder.getTicketLevel());
            if (!chunkStatus.isOrAfter(ChunkStatus.FULL)) {
                return ChunkHolder.UNLOADED_CHUNK;
            }
            return either.mapLeft(chunkAccess -> {
                LevelChunk levelChunk2;
                ChunkPos chunkPos = chunkHolder.getPos();
                ProtoChunk protoChunk = (ProtoChunk)chunkAccess;
                if (protoChunk instanceof ImposterProtoChunk) {
                    levelChunk2 = ((ImposterProtoChunk)protoChunk).getWrapped();
                } else {
                    levelChunk2 = new LevelChunk(this.level, protoChunk, levelChunk -> ChunkMap.postLoadProtoChunk(this.level, protoChunk.getEntities()));
                    chunkHolder.replaceProtoChunk(new ImposterProtoChunk(levelChunk2, false));
                }
                levelChunk2.setFullStatus(() -> ChunkHolder.getFullChunkStatus(chunkHolder.getTicketLevel()));
                levelChunk2.runPostLoad();
                if (this.entitiesInLevel.add(chunkPos.toLong())) {
                    levelChunk2.setLoaded(true);
                    levelChunk2.registerAllBlockEntitiesAfterLevelLoad();
                    levelChunk2.registerTickContainerInLevel(this.level);
                }
                return levelChunk2;
            });
        }, runnable -> this.mainThreadMailbox.tell(ChunkTaskPriorityQueueSorter.message(runnable, chunkHolder.getPos().toLong(), chunkHolder::getTicketLevel)));
    }

    public CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> prepareTickingChunk(ChunkHolder chunkHolder) {
        ChunkPos chunkPos = chunkHolder.getPos();
        CompletableFuture<Either<List<ChunkAccess>, ChunkHolder.ChunkLoadingFailure>> completableFuture = this.getChunkRangeFuture(chunkPos, 1, n -> ChunkStatus.FULL);
        CompletionStage completionStage = ((CompletableFuture)completableFuture.thenApplyAsync(either -> either.mapLeft(list -> (LevelChunk)list.get(list.size() / 2)), runnable -> this.mainThreadMailbox.tell(ChunkTaskPriorityQueueSorter.message(chunkHolder, runnable)))).thenApplyAsync(either -> either.ifLeft(levelChunk -> {
            levelChunk.postProcessGeneration();
            this.level.startTickingChunk((LevelChunk)levelChunk);
        }), (Executor)this.mainThreadExecutor);
        ((CompletableFuture)completionStage).thenAcceptAsync(either -> either.ifLeft(levelChunk -> {
            this.tickingGenerated.getAndIncrement();
            MutableObject mutableObject = new MutableObject();
            this.getPlayers(chunkPos, false).forEach(serverPlayer -> this.playerLoadedChunk((ServerPlayer)serverPlayer, (MutableObject<ClientboundLevelChunkWithLightPacket>)mutableObject, (LevelChunk)levelChunk));
        }), runnable -> this.mainThreadMailbox.tell(ChunkTaskPriorityQueueSorter.message(chunkHolder, runnable)));
        return completionStage;
    }

    public CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> prepareAccessibleChunk(ChunkHolder chunkHolder) {
        return this.getChunkRangeFuture(chunkHolder.getPos(), 1, ChunkStatus::getStatusAroundFullChunk).thenApplyAsync(either -> either.mapLeft(list -> {
            LevelChunk levelChunk = (LevelChunk)list.get(list.size() / 2);
            return levelChunk;
        }), runnable -> this.mainThreadMailbox.tell(ChunkTaskPriorityQueueSorter.message(chunkHolder, runnable)));
    }

    public int getTickingGenerated() {
        return this.tickingGenerated.get();
    }

    private boolean saveChunkIfNeeded(ChunkHolder chunkHolder) {
        if (!chunkHolder.wasAccessibleSinceLastSave()) {
            return false;
        }
        ChunkAccess chunkAccess = chunkHolder.getChunkToSave().getNow(null);
        if (chunkAccess instanceof ImposterProtoChunk || chunkAccess instanceof LevelChunk) {
            long l = chunkAccess.getPos().toLong();
            long l2 = this.chunkSaveCooldowns.getOrDefault(l, -1L);
            long l3 = System.currentTimeMillis();
            if (l3 < l2) {
                return false;
            }
            boolean bl = this.save(chunkAccess);
            chunkHolder.refreshAccessibility();
            if (bl) {
                this.chunkSaveCooldowns.put(l, l3 + 10000L);
            }
            return bl;
        }
        return false;
    }

    private boolean save(ChunkAccess chunkAccess) {
        this.poiManager.flush(chunkAccess.getPos());
        if (!chunkAccess.isUnsaved()) {
            return false;
        }
        chunkAccess.setUnsaved(false);
        ChunkPos chunkPos = chunkAccess.getPos();
        try {
            ChunkStatus chunkStatus = chunkAccess.getStatus();
            if (chunkStatus.getChunkType() != ChunkStatus.ChunkType.LEVELCHUNK) {
                if (this.isExistingChunkFull(chunkPos)) {
                    return false;
                }
                if (chunkStatus == ChunkStatus.EMPTY && chunkAccess.getAllStarts().values().stream().noneMatch(StructureStart::isValid)) {
                    return false;
                }
            }
            this.level.getProfiler().incrementCounter("chunkSave");
            CompoundTag compoundTag = ChunkSerializer.write(this.level, chunkAccess);
            this.write(chunkPos, compoundTag);
            this.markPosition(chunkPos, chunkStatus.getChunkType());
            return true;
        }
        catch (Exception exception) {
            LOGGER.error("Failed to save chunk {},{}", new Object[]{chunkPos.x, chunkPos.z, exception});
            return false;
        }
    }

    private boolean isExistingChunkFull(ChunkPos chunkPos) {
        CompoundTag compoundTag;
        byte by = this.chunkTypeCache.get(chunkPos.toLong());
        if (by != 0) {
            return by == 1;
        }
        try {
            compoundTag = this.readChunk(chunkPos);
            if (compoundTag == null) {
                this.markPositionReplaceable(chunkPos);
                return false;
            }
        }
        catch (Exception exception) {
            LOGGER.error("Failed to read chunk {}", (Object)chunkPos, (Object)exception);
            this.markPositionReplaceable(chunkPos);
            return false;
        }
        ChunkStatus.ChunkType chunkType = ChunkSerializer.getChunkTypeFromTag(compoundTag);
        return this.markPosition(chunkPos, chunkType) == 1;
    }

    protected void setViewDistance(int n) {
        int n2 = Mth.clamp(n + 1, 3, 33);
        if (n2 != this.viewDistance) {
            int n3 = this.viewDistance;
            this.viewDistance = n2;
            this.distanceManager.updatePlayerTickets(this.viewDistance + 1);
            for (ChunkHolder chunkHolder : this.updatingChunkMap.values()) {
                ChunkPos chunkPos = chunkHolder.getPos();
                MutableObject mutableObject = new MutableObject();
                this.getPlayers(chunkPos, false).forEach(serverPlayer -> {
                    SectionPos sectionPos = serverPlayer.getLastSectionPos();
                    boolean bl = ChunkMap.isChunkInRange(chunkPos.x, chunkPos.z, sectionPos.x(), sectionPos.z(), n3);
                    boolean bl2 = ChunkMap.isChunkInRange(chunkPos.x, chunkPos.z, sectionPos.x(), sectionPos.z(), this.viewDistance);
                    this.updateChunkTracking((ServerPlayer)serverPlayer, chunkPos, (MutableObject<ClientboundLevelChunkWithLightPacket>)mutableObject, bl, bl2);
                });
            }
        }
    }

    protected void updateChunkTracking(ServerPlayer serverPlayer, ChunkPos chunkPos, MutableObject<ClientboundLevelChunkWithLightPacket> mutableObject, boolean bl, boolean bl2) {
        ChunkHolder chunkHolder;
        if (serverPlayer.level != this.level) {
            return;
        }
        if (bl2 && !bl && (chunkHolder = this.getVisibleChunkIfPresent(chunkPos.toLong())) != null) {
            LevelChunk levelChunk = chunkHolder.getTickingChunk();
            if (levelChunk != null) {
                this.playerLoadedChunk(serverPlayer, mutableObject, levelChunk);
            }
            DebugPackets.sendPoiPacketsForChunk(this.level, chunkPos);
        }
        if (!bl2 && bl) {
            serverPlayer.untrackChunk(chunkPos);
        }
    }

    public int size() {
        return this.visibleChunkMap.size();
    }

    public net.minecraft.server.level.DistanceManager getDistanceManager() {
        return this.distanceManager;
    }

    protected Iterable<ChunkHolder> getChunks() {
        return Iterables.unmodifiableIterable((Iterable)this.visibleChunkMap.values());
    }

    void dumpChunks(Writer writer) throws IOException {
        CsvOutput csvOutput = CsvOutput.builder().addColumn("x").addColumn("z").addColumn("level").addColumn("in_memory").addColumn("status").addColumn("full_status").addColumn("accessible_ready").addColumn("ticking_ready").addColumn("entity_ticking_ready").addColumn("ticket").addColumn("spawning").addColumn("block_entity_count").addColumn("ticking_ticket").addColumn("ticking_level").addColumn("block_ticks").addColumn("fluid_ticks").build(writer);
        TickingTracker tickingTracker = this.distanceManager.tickingTracker();
        for (Long2ObjectMap.Entry entry : this.visibleChunkMap.long2ObjectEntrySet()) {
            long l = entry.getLongKey();
            ChunkPos chunkPos = new ChunkPos(l);
            ChunkHolder chunkHolder = (ChunkHolder)entry.getValue();
            Optional<ChunkAccess> optional = Optional.ofNullable(chunkHolder.getLastAvailable());
            Optional<Object> optional2 = optional.flatMap(chunkAccess -> chunkAccess instanceof LevelChunk ? Optional.of((LevelChunk)chunkAccess) : Optional.empty());
            csvOutput.writeRow(chunkPos.x, chunkPos.z, chunkHolder.getTicketLevel(), optional.isPresent(), optional.map(ChunkAccess::getStatus).orElse(null), optional2.map(LevelChunk::getFullStatus).orElse(null), ChunkMap.printFuture(chunkHolder.getFullChunkFuture()), ChunkMap.printFuture(chunkHolder.getTickingChunkFuture()), ChunkMap.printFuture(chunkHolder.getEntityTickingChunkFuture()), this.distanceManager.getTicketDebugString(l), this.anyPlayerCloseEnoughForSpawning(chunkPos), optional2.map(levelChunk -> levelChunk.getBlockEntities().size()).orElse(0), tickingTracker.getTicketDebugString(l), tickingTracker.getLevel(l), optional2.map(levelChunk -> levelChunk.getBlockTicks().count()).orElse(0), optional2.map(levelChunk -> levelChunk.getFluidTicks().count()).orElse(0));
        }
    }

    private static String printFuture(CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> completableFuture) {
        try {
            Either either = completableFuture.getNow(null);
            if (either != null) {
                return (String)either.map(levelChunk -> "done", chunkLoadingFailure -> "unloaded");
            }
            return "not completed";
        }
        catch (CompletionException completionException) {
            return "failed " + completionException.getCause().getMessage();
        }
        catch (CancellationException cancellationException) {
            return "cancelled";
        }
    }

    @Nullable
    private CompoundTag readChunk(ChunkPos chunkPos) throws IOException {
        CompoundTag compoundTag = this.read(chunkPos);
        if (compoundTag == null) {
            return null;
        }
        return this.upgradeChunkTag(this.level.dimension(), this.overworldDataStorage, compoundTag, this.generator.getTypeNameForDataFixer());
    }

    boolean anyPlayerCloseEnoughForSpawning(ChunkPos chunkPos) {
        long l = chunkPos.toLong();
        if (!this.distanceManager.hasPlayersNearby(l)) {
            return false;
        }
        for (ServerPlayer serverPlayer : this.playerMap.getPlayers(l)) {
            if (!this.playerIsCloseEnoughForSpawning(serverPlayer, chunkPos)) continue;
            return true;
        }
        return false;
    }

    public List<ServerPlayer> getPlayersCloseForSpawning(ChunkPos chunkPos) {
        long l = chunkPos.toLong();
        if (!this.distanceManager.hasPlayersNearby(l)) {
            return List.of();
        }
        ImmutableList.Builder builder = ImmutableList.builder();
        for (ServerPlayer serverPlayer : this.playerMap.getPlayers(l)) {
            if (!this.playerIsCloseEnoughForSpawning(serverPlayer, chunkPos)) continue;
            builder.add((Object)serverPlayer);
        }
        return builder.build();
    }

    private boolean playerIsCloseEnoughForSpawning(ServerPlayer serverPlayer, ChunkPos chunkPos) {
        if (serverPlayer.isSpectator()) {
            return false;
        }
        double d = ChunkMap.euclideanDistanceSquared(chunkPos, serverPlayer);
        return d < 16384.0;
    }

    private boolean skipPlayer(ServerPlayer serverPlayer) {
        return serverPlayer.isSpectator() && !this.level.getGameRules().getBoolean(GameRules.RULE_SPECTATORSGENERATECHUNKS);
    }

    void updatePlayerStatus(ServerPlayer serverPlayer, boolean bl) {
        boolean bl2 = this.skipPlayer(serverPlayer);
        boolean bl3 = this.playerMap.ignoredOrUnknown(serverPlayer);
        int n = SectionPos.blockToSectionCoord(serverPlayer.getBlockX());
        int n2 = SectionPos.blockToSectionCoord(serverPlayer.getBlockZ());
        if (bl) {
            this.playerMap.addPlayer(ChunkPos.asLong(n, n2), serverPlayer, bl2);
            this.updatePlayerPos(serverPlayer);
            if (!bl2) {
                this.distanceManager.addPlayer(SectionPos.of(serverPlayer), serverPlayer);
            }
        } else {
            SectionPos sectionPos = serverPlayer.getLastSectionPos();
            this.playerMap.removePlayer(sectionPos.chunk().toLong(), serverPlayer);
            if (!bl3) {
                this.distanceManager.removePlayer(sectionPos, serverPlayer);
            }
        }
        for (int i = n - this.viewDistance - 1; i <= n + this.viewDistance + 1; ++i) {
            for (int j = n2 - this.viewDistance - 1; j <= n2 + this.viewDistance + 1; ++j) {
                if (!ChunkMap.isChunkInRange(i, j, n, n2, this.viewDistance)) continue;
                ChunkPos chunkPos = new ChunkPos(i, j);
                this.updateChunkTracking(serverPlayer, chunkPos, (MutableObject<ClientboundLevelChunkWithLightPacket>)new MutableObject(), !bl, bl);
            }
        }
    }

    private SectionPos updatePlayerPos(ServerPlayer serverPlayer) {
        SectionPos sectionPos = SectionPos.of(serverPlayer);
        serverPlayer.setLastSectionPos(sectionPos);
        serverPlayer.connection.send(new ClientboundSetChunkCacheCenterPacket(sectionPos.x(), sectionPos.z()));
        return sectionPos;
    }

    public void move(ServerPlayer serverPlayer) {
        boolean bl;
        for (TrackedEntity trackedEntity : this.entityMap.values()) {
            if (trackedEntity.entity == serverPlayer) {
                trackedEntity.updatePlayers(this.level.players());
                continue;
            }
            trackedEntity.updatePlayer(serverPlayer);
        }
        int n = SectionPos.blockToSectionCoord(serverPlayer.getBlockX());
        int n2 = SectionPos.blockToSectionCoord(serverPlayer.getBlockZ());
        SectionPos sectionPos = serverPlayer.getLastSectionPos();
        SectionPos sectionPos2 = SectionPos.of(serverPlayer);
        long l = sectionPos.chunk().toLong();
        long l2 = sectionPos2.chunk().toLong();
        boolean bl2 = this.playerMap.ignored(serverPlayer);
        boolean bl3 = this.skipPlayer(serverPlayer);
        boolean bl4 = bl = sectionPos.asLong() != sectionPos2.asLong();
        if (bl || bl2 != bl3) {
            this.updatePlayerPos(serverPlayer);
            if (!bl2) {
                this.distanceManager.removePlayer(sectionPos, serverPlayer);
            }
            if (!bl3) {
                this.distanceManager.addPlayer(sectionPos2, serverPlayer);
            }
            if (!bl2 && bl3) {
                this.playerMap.ignorePlayer(serverPlayer);
            }
            if (bl2 && !bl3) {
                this.playerMap.unIgnorePlayer(serverPlayer);
            }
            if (l != l2) {
                this.playerMap.updatePlayer(l, l2, serverPlayer);
            }
        }
        int n3 = sectionPos.x();
        int n4 = sectionPos.z();
        if (Math.abs(n3 - n) <= this.viewDistance * 2 && Math.abs(n4 - n2) <= this.viewDistance * 2) {
            int n5 = Math.min(n, n3) - this.viewDistance - 1;
            int n6 = Math.min(n2, n4) - this.viewDistance - 1;
            int n7 = Math.max(n, n3) + this.viewDistance + 1;
            int n8 = Math.max(n2, n4) + this.viewDistance + 1;
            for (int i = n5; i <= n7; ++i) {
                for (int j = n6; j <= n8; ++j) {
                    boolean bl5 = ChunkMap.isChunkInRange(i, j, n3, n4, this.viewDistance);
                    boolean bl6 = ChunkMap.isChunkInRange(i, j, n, n2, this.viewDistance);
                    this.updateChunkTracking(serverPlayer, new ChunkPos(i, j), (MutableObject<ClientboundLevelChunkWithLightPacket>)new MutableObject(), bl5, bl6);
                }
            }
        } else {
            boolean bl7;
            boolean bl8;
            int n9;
            int n10;
            for (n10 = n3 - this.viewDistance - 1; n10 <= n3 + this.viewDistance + 1; ++n10) {
                for (n9 = n4 - this.viewDistance - 1; n9 <= n4 + this.viewDistance + 1; ++n9) {
                    if (!ChunkMap.isChunkInRange(n10, n9, n3, n4, this.viewDistance)) continue;
                    bl8 = true;
                    bl7 = false;
                    this.updateChunkTracking(serverPlayer, new ChunkPos(n10, n9), (MutableObject<ClientboundLevelChunkWithLightPacket>)new MutableObject(), true, false);
                }
            }
            for (n10 = n - this.viewDistance - 1; n10 <= n + this.viewDistance + 1; ++n10) {
                for (n9 = n2 - this.viewDistance - 1; n9 <= n2 + this.viewDistance + 1; ++n9) {
                    if (!ChunkMap.isChunkInRange(n10, n9, n, n2, this.viewDistance)) continue;
                    bl8 = false;
                    bl7 = true;
                    this.updateChunkTracking(serverPlayer, new ChunkPos(n10, n9), (MutableObject<ClientboundLevelChunkWithLightPacket>)new MutableObject(), false, true);
                }
            }
        }
    }

    @Override
    public List<ServerPlayer> getPlayers(ChunkPos chunkPos, boolean bl) {
        Set<ServerPlayer> set = this.playerMap.getPlayers(chunkPos.toLong());
        ImmutableList.Builder builder = ImmutableList.builder();
        for (ServerPlayer serverPlayer : set) {
            SectionPos sectionPos = serverPlayer.getLastSectionPos();
            if ((!bl || !ChunkMap.isChunkOnRangeBorder(chunkPos.x, chunkPos.z, sectionPos.x(), sectionPos.z(), this.viewDistance)) && (bl || !ChunkMap.isChunkInRange(chunkPos.x, chunkPos.z, sectionPos.x(), sectionPos.z(), this.viewDistance))) continue;
            builder.add((Object)serverPlayer);
        }
        return builder.build();
    }

    protected void addEntity(Entity entity) {
        if (entity instanceof EnderDragonPart) {
            return;
        }
        EntityType<?> entityType = entity.getType();
        int n = entityType.clientTrackingRange() * 16;
        if (n == 0) {
            return;
        }
        int n2 = entityType.updateInterval();
        if (this.entityMap.containsKey(entity.getId())) {
            throw Util.pauseInIde(new IllegalStateException("Entity is already tracked!"));
        }
        TrackedEntity trackedEntity = new TrackedEntity(entity, n, n2, entityType.trackDeltas());
        this.entityMap.put(entity.getId(), (Object)trackedEntity);
        trackedEntity.updatePlayers(this.level.players());
        if (entity instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)entity;
            this.updatePlayerStatus(serverPlayer, true);
            for (TrackedEntity trackedEntity2 : this.entityMap.values()) {
                if (trackedEntity2.entity == serverPlayer) continue;
                trackedEntity2.updatePlayer(serverPlayer);
            }
        }
    }

    protected void removeEntity(Entity entity) {
        Object object;
        if (entity instanceof ServerPlayer) {
            object = (ServerPlayer)entity;
            this.updatePlayerStatus((ServerPlayer)object, false);
            for (TrackedEntity trackedEntity : this.entityMap.values()) {
                trackedEntity.removePlayer((ServerPlayer)object);
            }
        }
        if ((object = (TrackedEntity)this.entityMap.remove(entity.getId())) != null) {
            ((TrackedEntity)object).broadcastRemoved();
        }
    }

    protected void tick() {
        ArrayList arrayList = Lists.newArrayList();
        List<ServerPlayer> list = this.level.players();
        for (TrackedEntity trackedEntity : this.entityMap.values()) {
            boolean bl;
            SectionPos sectionPos = trackedEntity.lastSectionPos;
            SectionPos sectionPos2 = SectionPos.of(trackedEntity.entity);
            boolean bl2 = bl = !Objects.equals(sectionPos, sectionPos2);
            if (bl) {
                trackedEntity.updatePlayers(list);
                Entity entity = trackedEntity.entity;
                if (entity instanceof ServerPlayer) {
                    arrayList.add((ServerPlayer)entity);
                }
                trackedEntity.lastSectionPos = sectionPos2;
            }
            if (!bl && !this.distanceManager.inEntityTickingRange(sectionPos2.chunk().toLong())) continue;
            trackedEntity.serverEntity.sendChanges();
        }
        if (!arrayList.isEmpty()) {
            for (TrackedEntity trackedEntity : this.entityMap.values()) {
                trackedEntity.updatePlayers(arrayList);
            }
        }
    }

    public void broadcast(Entity entity, Packet<?> packet) {
        TrackedEntity trackedEntity = (TrackedEntity)this.entityMap.get(entity.getId());
        if (trackedEntity != null) {
            trackedEntity.broadcast(packet);
        }
    }

    protected void broadcastAndSend(Entity entity, Packet<?> packet) {
        TrackedEntity trackedEntity = (TrackedEntity)this.entityMap.get(entity.getId());
        if (trackedEntity != null) {
            trackedEntity.broadcastAndSend(packet);
        }
    }

    private void playerLoadedChunk(ServerPlayer serverPlayer, MutableObject<ClientboundLevelChunkWithLightPacket> mutableObject, LevelChunk levelChunk) {
        if (mutableObject.getValue() == null) {
            mutableObject.setValue((Object)new ClientboundLevelChunkWithLightPacket(levelChunk, this.lightEngine, null, null, true));
        }
        serverPlayer.trackChunk(levelChunk.getPos(), (Packet)mutableObject.getValue());
        DebugPackets.sendPoiPacketsForChunk(this.level, levelChunk.getPos());
        ArrayList arrayList = Lists.newArrayList();
        ArrayList arrayList2 = Lists.newArrayList();
        for (Object object : this.entityMap.values()) {
            Entity entity = ((TrackedEntity)object).entity;
            if (entity == serverPlayer || !entity.chunkPosition().equals(levelChunk.getPos())) continue;
            ((TrackedEntity)object).updatePlayer(serverPlayer);
            if (entity instanceof Mob && ((Mob)entity).getLeashHolder() != null) {
                arrayList.add(entity);
            }
            if (entity.getPassengers().isEmpty()) continue;
            arrayList2.add(entity);
        }
        if (!arrayList.isEmpty()) {
            for (Object object : arrayList) {
                serverPlayer.connection.send(new ClientboundSetEntityLinkPacket((Entity)object, ((Mob)object).getLeashHolder()));
            }
        }
        if (!arrayList2.isEmpty()) {
            for (Object object : arrayList2) {
                serverPlayer.connection.send(new ClientboundSetPassengersPacket((Entity)object));
            }
        }
    }

    protected PoiManager getPoiManager() {
        return this.poiManager;
    }

    public String getStorageName() {
        return this.storageName;
    }

    void onFullChunkStatusChange(ChunkPos chunkPos, ChunkHolder.FullChunkStatus fullChunkStatus) {
        this.chunkStatusListener.onChunkStatusChange(chunkPos, fullChunkStatus);
    }

    class DistanceManager
    extends net.minecraft.server.level.DistanceManager {
        protected DistanceManager(Executor executor, Executor executor2) {
            super(executor, executor2);
        }

        @Override
        protected boolean isChunkToRemove(long l) {
            return ChunkMap.this.toDrop.contains(l);
        }

        @Override
        @Nullable
        protected ChunkHolder getChunk(long l) {
            return ChunkMap.this.getUpdatingChunkIfPresent(l);
        }

        @Override
        @Nullable
        protected ChunkHolder updateChunkScheduling(long l, int n, @Nullable ChunkHolder chunkHolder, int n2) {
            return ChunkMap.this.updateChunkScheduling(l, n, chunkHolder, n2);
        }
    }

    class TrackedEntity {
        final ServerEntity serverEntity;
        final Entity entity;
        private final int range;
        SectionPos lastSectionPos;
        private final Set<ServerPlayerConnection> seenBy = Sets.newIdentityHashSet();

        public TrackedEntity(Entity entity, int n, int n2, boolean bl) {
            this.serverEntity = new ServerEntity(ChunkMap.this.level, entity, n2, bl, this::broadcast);
            this.entity = entity;
            this.range = n;
            this.lastSectionPos = SectionPos.of(entity);
        }

        public boolean equals(Object object) {
            if (object instanceof TrackedEntity) {
                return ((TrackedEntity)object).entity.getId() == this.entity.getId();
            }
            return false;
        }

        public int hashCode() {
            return this.entity.getId();
        }

        public void broadcast(Packet<?> packet) {
            for (ServerPlayerConnection serverPlayerConnection : this.seenBy) {
                serverPlayerConnection.send(packet);
            }
        }

        public void broadcastAndSend(Packet<?> packet) {
            this.broadcast(packet);
            if (this.entity instanceof ServerPlayer) {
                ((ServerPlayer)this.entity).connection.send(packet);
            }
        }

        public void broadcastRemoved() {
            for (ServerPlayerConnection serverPlayerConnection : this.seenBy) {
                this.serverEntity.removePairing(serverPlayerConnection.getPlayer());
            }
        }

        public void removePlayer(ServerPlayer serverPlayer) {
            if (this.seenBy.remove(serverPlayer.connection)) {
                this.serverEntity.removePairing(serverPlayer);
            }
        }

        public void updatePlayer(ServerPlayer serverPlayer) {
            boolean bl;
            if (serverPlayer == this.entity) {
                return;
            }
            Vec3 vec3 = serverPlayer.position().subtract(this.serverEntity.sentPos());
            double d = vec3.x * vec3.x + vec3.z * vec3.z;
            double d2 = Math.min(this.getEffectiveRange(), (ChunkMap.this.viewDistance - 1) * 16);
            double d3 = d2 * d2;
            boolean bl2 = bl = d <= d3 && this.entity.broadcastToPlayer(serverPlayer);
            if (bl) {
                if (this.seenBy.add(serverPlayer.connection)) {
                    this.serverEntity.addPairing(serverPlayer);
                }
            } else if (this.seenBy.remove(serverPlayer.connection)) {
                this.serverEntity.removePairing(serverPlayer);
            }
        }

        private int scaledRange(int n) {
            return ChunkMap.this.level.getServer().getScaledTrackingDistance(n);
        }

        private int getEffectiveRange() {
            int n = this.range;
            for (Entity entity : this.entity.getIndirectPassengers()) {
                int n2 = entity.getType().clientTrackingRange() * 16;
                if (n2 <= n) continue;
                n = n2;
            }
            return this.scaledRange(n);
        }

        public void updatePlayers(List<ServerPlayer> list) {
            for (ServerPlayer serverPlayer : list) {
                this.updatePlayer(serverPlayer);
            }
        }
    }
}

