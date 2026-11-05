/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Queues
 *  com.google.common.collect.Sets
 *  com.google.common.primitives.Doubles
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.objects.ObjectArraySet
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.renderer.chunk;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.google.common.primitives.Doubles;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.client.renderer.chunk.RenderRegionCache;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.client.renderer.chunk.VisibilitySet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class ChunkRenderDispatcher {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_WORKERS_32_BIT = 4;
    private static final VertexFormat VERTEX_FORMAT = DefaultVertexFormat.BLOCK;
    private static final int MAX_HIGH_PRIORITY_QUOTA = 2;
    private final PriorityBlockingQueue<RenderChunk.ChunkCompileTask> toBatchHighPriority = Queues.newPriorityBlockingQueue();
    private final Queue<RenderChunk.ChunkCompileTask> toBatchLowPriority = Queues.newLinkedBlockingDeque();
    private int highPriorityQuota = 2;
    private final Queue<ChunkBufferBuilderPack> freeBuffers;
    private final Queue<Runnable> toUpload = Queues.newConcurrentLinkedQueue();
    private volatile int toBatchCount;
    private volatile int freeBufferCount;
    final ChunkBufferBuilderPack fixedBuffers;
    private final ProcessorMailbox<Runnable> mailbox;
    private final Executor executor;
    ClientLevel level;
    final LevelRenderer renderer;
    private Vec3 camera = Vec3.ZERO;

    public ChunkRenderDispatcher(ClientLevel clientLevel, LevelRenderer levelRenderer, Executor executor, boolean bl, ChunkBufferBuilderPack chunkBufferBuilderPack) {
        this.level = clientLevel;
        this.renderer = levelRenderer;
        int n = Math.max(1, (int)((double)Runtime.getRuntime().maxMemory() * 0.3) / (RenderType.chunkBufferLayers().stream().mapToInt(RenderType::bufferSize).sum() * 4) - 1);
        int n2 = Runtime.getRuntime().availableProcessors();
        int n3 = bl ? n2 : Math.min(n2, 4);
        int n4 = Math.max(1, Math.min(n3, n));
        this.fixedBuffers = chunkBufferBuilderPack;
        ArrayList arrayList = Lists.newArrayListWithExpectedSize((int)n4);
        try {
            for (int i = 0; i < n4; ++i) {
                arrayList.add(new ChunkBufferBuilderPack());
            }
        }
        catch (OutOfMemoryError outOfMemoryError) {
            LOGGER.warn("Allocated only {}/{} buffers", (Object)arrayList.size(), (Object)n4);
            int n5 = Math.min(arrayList.size() * 2 / 3, arrayList.size() - 1);
            for (int i = 0; i < n5; ++i) {
                arrayList.remove(arrayList.size() - 1);
            }
            System.gc();
        }
        this.freeBuffers = Queues.newArrayDeque((Iterable)arrayList);
        this.freeBufferCount = this.freeBuffers.size();
        this.executor = executor;
        this.mailbox = ProcessorMailbox.create(executor, "Chunk Renderer");
        this.mailbox.tell(this::runTask);
    }

    public void setLevel(ClientLevel clientLevel) {
        this.level = clientLevel;
    }

    private void runTask() {
        if (this.freeBuffers.isEmpty()) {
            return;
        }
        RenderChunk.ChunkCompileTask chunkCompileTask = this.pollTask();
        if (chunkCompileTask == null) {
            return;
        }
        ChunkBufferBuilderPack chunkBufferBuilderPack = this.freeBuffers.poll();
        this.toBatchCount = this.toBatchHighPriority.size() + this.toBatchLowPriority.size();
        this.freeBufferCount = this.freeBuffers.size();
        ((CompletableFuture)CompletableFuture.supplyAsync(Util.wrapThreadWithTaskName(chunkCompileTask.name(), () -> chunkCompileTask.doTask(chunkBufferBuilderPack)), this.executor).thenCompose(completableFuture -> completableFuture)).whenComplete((chunkTaskResult, throwable) -> {
            if (throwable != null) {
                CrashReport crashReport = CrashReport.forThrowable(throwable, "Batching chunks");
                Minecraft.getInstance().delayCrash(() -> Minecraft.getInstance().fillReport(crashReport));
                return;
            }
            this.mailbox.tell(() -> {
                if (chunkTaskResult == ChunkTaskResult.SUCCESSFUL) {
                    chunkBufferBuilderPack.clearAll();
                } else {
                    chunkBufferBuilderPack.discardAll();
                }
                this.freeBuffers.add(chunkBufferBuilderPack);
                this.freeBufferCount = this.freeBuffers.size();
                this.runTask();
            });
        });
    }

    @Nullable
    private RenderChunk.ChunkCompileTask pollTask() {
        RenderChunk.ChunkCompileTask chunkCompileTask;
        if (this.highPriorityQuota <= 0 && (chunkCompileTask = this.toBatchLowPriority.poll()) != null) {
            this.highPriorityQuota = 2;
            return chunkCompileTask;
        }
        chunkCompileTask = this.toBatchHighPriority.poll();
        if (chunkCompileTask != null) {
            --this.highPriorityQuota;
            return chunkCompileTask;
        }
        this.highPriorityQuota = 2;
        return this.toBatchLowPriority.poll();
    }

    public String getStats() {
        return String.format("pC: %03d, pU: %02d, aB: %02d", this.toBatchCount, this.toUpload.size(), this.freeBufferCount);
    }

    public int getToBatchCount() {
        return this.toBatchCount;
    }

    public int getToUpload() {
        return this.toUpload.size();
    }

    public int getFreeBufferCount() {
        return this.freeBufferCount;
    }

    public void setCamera(Vec3 vec3) {
        this.camera = vec3;
    }

    public Vec3 getCameraPosition() {
        return this.camera;
    }

    public void uploadAllPendingUploads() {
        Runnable runnable;
        while ((runnable = this.toUpload.poll()) != null) {
            runnable.run();
        }
    }

    public void rebuildChunkSync(RenderChunk renderChunk, RenderRegionCache renderRegionCache) {
        renderChunk.compileSync(renderRegionCache);
    }

    public void blockUntilClear() {
        this.clearBatchQueue();
    }

    public void schedule(RenderChunk.ChunkCompileTask chunkCompileTask) {
        this.mailbox.tell(() -> {
            if (chunkCompileTask.isHighPriority) {
                this.toBatchHighPriority.offer(chunkCompileTask);
            } else {
                this.toBatchLowPriority.offer(chunkCompileTask);
            }
            this.toBatchCount = this.toBatchHighPriority.size() + this.toBatchLowPriority.size();
            this.runTask();
        });
    }

    public CompletableFuture<Void> uploadChunkLayer(BufferBuilder bufferBuilder, VertexBuffer vertexBuffer) {
        return CompletableFuture.runAsync(() -> {}, this.toUpload::add).thenCompose(void_ -> this.doUploadChunkLayer(bufferBuilder, vertexBuffer));
    }

    private CompletableFuture<Void> doUploadChunkLayer(BufferBuilder bufferBuilder, VertexBuffer vertexBuffer) {
        return vertexBuffer.uploadLater(bufferBuilder);
    }

    private void clearBatchQueue() {
        RenderChunk.ChunkCompileTask chunkCompileTask;
        while (!this.toBatchHighPriority.isEmpty()) {
            chunkCompileTask = this.toBatchHighPriority.poll();
            if (chunkCompileTask == null) continue;
            chunkCompileTask.cancel();
        }
        while (!this.toBatchLowPriority.isEmpty()) {
            chunkCompileTask = this.toBatchLowPriority.poll();
            if (chunkCompileTask == null) continue;
            chunkCompileTask.cancel();
        }
        this.toBatchCount = 0;
    }

    public boolean isQueueEmpty() {
        return this.toBatchCount == 0 && this.toUpload.isEmpty();
    }

    public void dispose() {
        this.clearBatchQueue();
        this.mailbox.close();
        this.freeBuffers.clear();
    }

    public class RenderChunk {
        public static final int SIZE = 16;
        public final int index;
        public final AtomicReference<CompiledChunk> compiled = new AtomicReference<CompiledChunk>(CompiledChunk.UNCOMPILED);
        final AtomicInteger initialCompilationCancelCount = new AtomicInteger(0);
        @Nullable
        private RebuildTask lastRebuildTask;
        @Nullable
        private ResortTransparencyTask lastResortTransparencyTask;
        private final Set<BlockEntity> globalBlockEntities = Sets.newHashSet();
        private final Map<RenderType, VertexBuffer> buffers = RenderType.chunkBufferLayers().stream().collect(Collectors.toMap(renderType -> renderType, renderType -> new VertexBuffer()));
        private AABB bb;
        private boolean dirty = true;
        final BlockPos.MutableBlockPos origin = new BlockPos.MutableBlockPos(-1, -1, -1);
        private final BlockPos.MutableBlockPos[] relativeOrigins = Util.make(new BlockPos.MutableBlockPos[6], mutableBlockPosArray -> {
            for (int i = 0; i < ((BlockPos.MutableBlockPos[])mutableBlockPosArray).length; ++i) {
                mutableBlockPosArray[i] = new BlockPos.MutableBlockPos();
            }
        });
        private boolean playerChanged;

        public RenderChunk(int n, int n2, int n3, int n4) {
            this.index = n;
            this.setOrigin(n2, n3, n4);
        }

        private boolean doesChunkExistAt(BlockPos blockPos) {
            return ChunkRenderDispatcher.this.level.getChunk(SectionPos.blockToSectionCoord(blockPos.getX()), SectionPos.blockToSectionCoord(blockPos.getZ()), ChunkStatus.FULL, false) != null;
        }

        public boolean hasAllNeighbors() {
            int n = 24;
            if (this.getDistToPlayerSqr() > 576.0) {
                return this.doesChunkExistAt(this.relativeOrigins[Direction.WEST.ordinal()]) && this.doesChunkExistAt(this.relativeOrigins[Direction.NORTH.ordinal()]) && this.doesChunkExistAt(this.relativeOrigins[Direction.EAST.ordinal()]) && this.doesChunkExistAt(this.relativeOrigins[Direction.SOUTH.ordinal()]);
            }
            return true;
        }

        public AABB getBoundingBox() {
            return this.bb;
        }

        public VertexBuffer getBuffer(RenderType renderType) {
            return this.buffers.get(renderType);
        }

        public void setOrigin(int n, int n2, int n3) {
            this.reset();
            this.origin.set(n, n2, n3);
            this.bb = new AABB(n, n2, n3, n + 16, n2 + 16, n3 + 16);
            for (Direction direction : Direction.values()) {
                this.relativeOrigins[direction.ordinal()].set(this.origin).move(direction, 16);
            }
        }

        protected double getDistToPlayerSqr() {
            Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
            double d = this.bb.minX + 8.0 - camera.getPosition().x;
            double d2 = this.bb.minY + 8.0 - camera.getPosition().y;
            double d3 = this.bb.minZ + 8.0 - camera.getPosition().z;
            return d * d + d2 * d2 + d3 * d3;
        }

        void beginLayer(BufferBuilder bufferBuilder) {
            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
        }

        public CompiledChunk getCompiledChunk() {
            return this.compiled.get();
        }

        private void reset() {
            this.cancelTasks();
            this.compiled.set(CompiledChunk.UNCOMPILED);
            this.dirty = true;
        }

        public void releaseBuffers() {
            this.reset();
            this.buffers.values().forEach(VertexBuffer::close);
        }

        public BlockPos getOrigin() {
            return this.origin;
        }

        public void setDirty(boolean bl) {
            boolean bl2 = this.dirty;
            this.dirty = true;
            this.playerChanged = bl | (bl2 && this.playerChanged);
        }

        public void setNotDirty() {
            this.dirty = false;
            this.playerChanged = false;
        }

        public boolean isDirty() {
            return this.dirty;
        }

        public boolean isDirtyFromPlayer() {
            return this.dirty && this.playerChanged;
        }

        public BlockPos getRelativeOrigin(Direction direction) {
            return this.relativeOrigins[direction.ordinal()];
        }

        public boolean resortTransparency(RenderType renderType, ChunkRenderDispatcher chunkRenderDispatcher) {
            CompiledChunk compiledChunk = this.getCompiledChunk();
            if (this.lastResortTransparencyTask != null) {
                this.lastResortTransparencyTask.cancel();
            }
            if (!compiledChunk.hasLayer.contains(renderType)) {
                return false;
            }
            this.lastResortTransparencyTask = new ResortTransparencyTask(this.getDistToPlayerSqr(), compiledChunk);
            chunkRenderDispatcher.schedule(this.lastResortTransparencyTask);
            return true;
        }

        protected boolean cancelTasks() {
            boolean bl = false;
            if (this.lastRebuildTask != null) {
                this.lastRebuildTask.cancel();
                this.lastRebuildTask = null;
                bl = true;
            }
            if (this.lastResortTransparencyTask != null) {
                this.lastResortTransparencyTask.cancel();
                this.lastResortTransparencyTask = null;
            }
            return bl;
        }

        public ChunkCompileTask createCompileTask(RenderRegionCache renderRegionCache) {
            boolean bl;
            boolean bl2 = this.cancelTasks();
            BlockPos blockPos = this.origin.immutable();
            boolean bl3 = true;
            RenderChunkRegion renderChunkRegion = renderRegionCache.createRegion(ChunkRenderDispatcher.this.level, blockPos.offset(-1, -1, -1), blockPos.offset(16, 16, 16), 1);
            boolean bl4 = bl = this.compiled.get() == CompiledChunk.UNCOMPILED;
            if (bl && bl2) {
                this.initialCompilationCancelCount.incrementAndGet();
            }
            this.lastRebuildTask = new RebuildTask(this.getDistToPlayerSqr(), renderChunkRegion, !bl || this.initialCompilationCancelCount.get() > 2);
            return this.lastRebuildTask;
        }

        public void rebuildChunkAsync(ChunkRenderDispatcher chunkRenderDispatcher, RenderRegionCache renderRegionCache) {
            ChunkCompileTask chunkCompileTask = this.createCompileTask(renderRegionCache);
            chunkRenderDispatcher.schedule(chunkCompileTask);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        void updateGlobalBlockEntities(Set<BlockEntity> set) {
            HashSet hashSet;
            HashSet hashSet2 = Sets.newHashSet(set);
            Set<BlockEntity> set2 = this.globalBlockEntities;
            synchronized (set2) {
                hashSet = Sets.newHashSet(this.globalBlockEntities);
                hashSet2.removeAll(this.globalBlockEntities);
                hashSet.removeAll(set);
                this.globalBlockEntities.clear();
                this.globalBlockEntities.addAll(set);
            }
            ChunkRenderDispatcher.this.renderer.updateGlobalBlockEntities(hashSet, hashSet2);
        }

        public void compileSync(RenderRegionCache renderRegionCache) {
            ChunkCompileTask chunkCompileTask = this.createCompileTask(renderRegionCache);
            chunkCompileTask.doTask(ChunkRenderDispatcher.this.fixedBuffers);
        }

        class ResortTransparencyTask
        extends ChunkCompileTask {
            private final CompiledChunk compiledChunk;

            public ResortTransparencyTask(double d, CompiledChunk compiledChunk) {
                super(d, true);
                this.compiledChunk = compiledChunk;
            }

            @Override
            protected String name() {
                return "rend_chk_sort";
            }

            @Override
            public CompletableFuture<ChunkTaskResult> doTask(ChunkBufferBuilderPack chunkBufferBuilderPack) {
                if (this.isCancelled.get()) {
                    return CompletableFuture.completedFuture(ChunkTaskResult.CANCELLED);
                }
                if (!RenderChunk.this.hasAllNeighbors()) {
                    this.isCancelled.set(true);
                    return CompletableFuture.completedFuture(ChunkTaskResult.CANCELLED);
                }
                if (this.isCancelled.get()) {
                    return CompletableFuture.completedFuture(ChunkTaskResult.CANCELLED);
                }
                Vec3 vec3 = ChunkRenderDispatcher.this.getCameraPosition();
                float f = (float)vec3.x;
                float f2 = (float)vec3.y;
                float f3 = (float)vec3.z;
                BufferBuilder.SortState sortState = this.compiledChunk.transparencyState;
                if (sortState == null || !this.compiledChunk.hasBlocks.contains(RenderType.translucent())) {
                    return CompletableFuture.completedFuture(ChunkTaskResult.CANCELLED);
                }
                BufferBuilder bufferBuilder = chunkBufferBuilderPack.builder(RenderType.translucent());
                RenderChunk.this.beginLayer(bufferBuilder);
                bufferBuilder.restoreSortState(sortState);
                bufferBuilder.setQuadSortOrigin(f - (float)RenderChunk.this.origin.getX(), f2 - (float)RenderChunk.this.origin.getY(), f3 - (float)RenderChunk.this.origin.getZ());
                this.compiledChunk.transparencyState = bufferBuilder.getSortState();
                bufferBuilder.end();
                if (this.isCancelled.get()) {
                    return CompletableFuture.completedFuture(ChunkTaskResult.CANCELLED);
                }
                CompletionStage completionStage = ChunkRenderDispatcher.this.uploadChunkLayer(chunkBufferBuilderPack.builder(RenderType.translucent()), RenderChunk.this.getBuffer(RenderType.translucent())).thenApply(void_ -> ChunkTaskResult.CANCELLED);
                return ((CompletableFuture)completionStage).handle((chunkTaskResult, throwable) -> {
                    if (throwable != null && !(throwable instanceof CancellationException) && !(throwable instanceof InterruptedException)) {
                        CrashReport crashReport = CrashReport.forThrowable(throwable, "Rendering chunk");
                        Minecraft.getInstance().delayCrash(() -> crashReport);
                    }
                    return this.isCancelled.get() ? ChunkTaskResult.CANCELLED : ChunkTaskResult.SUCCESSFUL;
                });
            }

            @Override
            public void cancel() {
                this.isCancelled.set(true);
            }
        }

        abstract class ChunkCompileTask
        implements Comparable<ChunkCompileTask> {
            protected final double distAtCreation;
            protected final AtomicBoolean isCancelled = new AtomicBoolean(false);
            protected final boolean isHighPriority;

            public ChunkCompileTask(double d, boolean bl) {
                this.distAtCreation = d;
                this.isHighPriority = bl;
            }

            public abstract CompletableFuture<ChunkTaskResult> doTask(ChunkBufferBuilderPack var1);

            public abstract void cancel();

            protected abstract String name();

            @Override
            public int compareTo(ChunkCompileTask chunkCompileTask) {
                return Doubles.compare((double)this.distAtCreation, (double)chunkCompileTask.distAtCreation);
            }

            @Override
            public /* synthetic */ int compareTo(Object object) {
                return this.compareTo((ChunkCompileTask)object);
            }
        }

        class RebuildTask
        extends ChunkCompileTask {
            @Nullable
            protected RenderChunkRegion region;

            public RebuildTask(@Nullable double d, RenderChunkRegion renderChunkRegion, boolean bl) {
                super(d, bl);
                this.region = renderChunkRegion;
            }

            @Override
            protected String name() {
                return "rend_chk_rebuild";
            }

            @Override
            public CompletableFuture<ChunkTaskResult> doTask(ChunkBufferBuilderPack chunkBufferBuilderPack) {
                if (this.isCancelled.get()) {
                    return CompletableFuture.completedFuture(ChunkTaskResult.CANCELLED);
                }
                if (!RenderChunk.this.hasAllNeighbors()) {
                    this.region = null;
                    RenderChunk.this.setDirty(false);
                    this.isCancelled.set(true);
                    return CompletableFuture.completedFuture(ChunkTaskResult.CANCELLED);
                }
                if (this.isCancelled.get()) {
                    return CompletableFuture.completedFuture(ChunkTaskResult.CANCELLED);
                }
                Vec3 vec3 = ChunkRenderDispatcher.this.getCameraPosition();
                float f = (float)vec3.x;
                float f2 = (float)vec3.y;
                float f3 = (float)vec3.z;
                CompiledChunk compiledChunk = new CompiledChunk();
                Set<BlockEntity> set = this.compile(f, f2, f3, compiledChunk, chunkBufferBuilderPack);
                RenderChunk.this.updateGlobalBlockEntities(set);
                if (this.isCancelled.get()) {
                    return CompletableFuture.completedFuture(ChunkTaskResult.CANCELLED);
                }
                ArrayList arrayList = Lists.newArrayList();
                compiledChunk.hasLayer.forEach(renderType -> arrayList.add(ChunkRenderDispatcher.this.uploadChunkLayer(chunkBufferBuilderPack.builder((RenderType)renderType), RenderChunk.this.getBuffer((RenderType)renderType))));
                return Util.sequenceFailFast(arrayList).handle((list, throwable) -> {
                    if (throwable != null && !(throwable instanceof CancellationException) && !(throwable instanceof InterruptedException)) {
                        CrashReport crashReport = CrashReport.forThrowable(throwable, "Rendering chunk");
                        Minecraft.getInstance().delayCrash(() -> crashReport);
                    }
                    if (this.isCancelled.get()) {
                        return ChunkTaskResult.CANCELLED;
                    }
                    RenderChunk.this.compiled.set(compiledChunk);
                    RenderChunk.this.initialCompilationCancelCount.set(0);
                    ChunkRenderDispatcher.this.renderer.addRecentlyCompiledChunk(RenderChunk.this);
                    return ChunkTaskResult.SUCCESSFUL;
                });
            }

            private Set<BlockEntity> compile(float f, float f2, float f3, CompiledChunk compiledChunk, ChunkBufferBuilderPack chunkBufferBuilderPack) {
                boolean bl = true;
                BlockPos blockPos = RenderChunk.this.origin.immutable();
                BlockPos blockPos2 = blockPos.offset(15, 15, 15);
                VisGraph visGraph = new VisGraph();
                HashSet hashSet = Sets.newHashSet();
                RenderChunkRegion renderChunkRegion = this.region;
                this.region = null;
                PoseStack poseStack = new PoseStack();
                if (renderChunkRegion != null) {
                    ModelBlockRenderer.enableCaching();
                    Random random = new Random();
                    BlockRenderDispatcher blockRenderDispatcher = Minecraft.getInstance().getBlockRenderer();
                    for (BlockPos blockPos3 : BlockPos.betweenClosed(blockPos, blockPos2)) {
                        BufferBuilder bufferBuilder;
                        RenderType renderType;
                        FluidState fluidState;
                        Object object;
                        BlockState blockState = renderChunkRegion.getBlockState(blockPos3);
                        if (blockState.isSolidRender(renderChunkRegion, blockPos3)) {
                            visGraph.setOpaque(blockPos3);
                        }
                        if (blockState.hasBlockEntity() && (object = renderChunkRegion.getBlockEntity(blockPos3)) != null) {
                            this.handleBlockEntity(compiledChunk, hashSet, object);
                        }
                        if (!(fluidState = ((BlockBehaviour.BlockStateBase)(object = renderChunkRegion.getBlockState(blockPos3))).getFluidState()).isEmpty()) {
                            renderType = ItemBlockRenderTypes.getRenderLayer(fluidState);
                            bufferBuilder = chunkBufferBuilderPack.builder(renderType);
                            if (compiledChunk.hasLayer.add(renderType)) {
                                RenderChunk.this.beginLayer(bufferBuilder);
                            }
                            if (blockRenderDispatcher.renderLiquid(blockPos3, renderChunkRegion, bufferBuilder, (BlockState)object, fluidState)) {
                                compiledChunk.isCompletelyEmpty = false;
                                compiledChunk.hasBlocks.add(renderType);
                            }
                        }
                        if (blockState.getRenderShape() == RenderShape.INVISIBLE) continue;
                        renderType = ItemBlockRenderTypes.getChunkRenderType(blockState);
                        bufferBuilder = chunkBufferBuilderPack.builder(renderType);
                        if (compiledChunk.hasLayer.add(renderType)) {
                            RenderChunk.this.beginLayer(bufferBuilder);
                        }
                        poseStack.pushPose();
                        poseStack.translate(blockPos3.getX() & 0xF, blockPos3.getY() & 0xF, blockPos3.getZ() & 0xF);
                        if (blockRenderDispatcher.renderBatched(blockState, blockPos3, renderChunkRegion, poseStack, bufferBuilder, true, random)) {
                            compiledChunk.isCompletelyEmpty = false;
                            compiledChunk.hasBlocks.add(renderType);
                        }
                        poseStack.popPose();
                    }
                    if (compiledChunk.hasBlocks.contains(RenderType.translucent())) {
                        BufferBuilder bufferBuilder = chunkBufferBuilderPack.builder(RenderType.translucent());
                        bufferBuilder.setQuadSortOrigin(f - (float)blockPos.getX(), f2 - (float)blockPos.getY(), f3 - (float)blockPos.getZ());
                        compiledChunk.transparencyState = bufferBuilder.getSortState();
                    }
                    compiledChunk.hasLayer.stream().map(chunkBufferBuilderPack::builder).forEach(BufferBuilder::end);
                    ModelBlockRenderer.clearCache();
                }
                compiledChunk.visibilitySet = visGraph.resolve();
                return hashSet;
            }

            private <E extends BlockEntity> void handleBlockEntity(CompiledChunk compiledChunk, Set<BlockEntity> set, E e) {
                BlockEntityRenderer<E> blockEntityRenderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(e);
                if (blockEntityRenderer != null) {
                    compiledChunk.renderableBlockEntities.add(e);
                    if (blockEntityRenderer.shouldRenderOffScreen(e)) {
                        set.add(e);
                    }
                }
            }

            @Override
            public void cancel() {
                this.region = null;
                if (this.isCancelled.compareAndSet(false, true)) {
                    RenderChunk.this.setDirty(false);
                }
            }
        }
    }

    static enum ChunkTaskResult {
        SUCCESSFUL,
        CANCELLED;

    }

    public static class CompiledChunk {
        public static final CompiledChunk UNCOMPILED = new CompiledChunk(){

            @Override
            public boolean facesCanSeeEachother(Direction direction, Direction direction2) {
                return false;
            }
        };
        final Set<RenderType> hasBlocks = new ObjectArraySet();
        final Set<RenderType> hasLayer = new ObjectArraySet();
        boolean isCompletelyEmpty = true;
        final List<BlockEntity> renderableBlockEntities = Lists.newArrayList();
        VisibilitySet visibilitySet = new VisibilitySet();
        @Nullable
        BufferBuilder.SortState transparencyState;

        public boolean hasNoRenderableLayers() {
            return this.isCompletelyEmpty;
        }

        public boolean isEmpty(RenderType renderType) {
            return !this.hasBlocks.contains(renderType);
        }

        public List<BlockEntity> getRenderableBlockEntities() {
            return this.renderableBlockEntities;
        }

        public boolean facesCanSeeEachother(Direction direction, Direction direction2) {
            return this.visibilitySet.visibilityBetween(direction, direction2);
        }
    }
}

