/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Queues
 *  com.mojang.logging.LogUtils
 *  org.apache.commons.lang3.mutable.MutableObject
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.levelgen.structure.pools;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.pools.EmptyPoolElement;
import net.minecraft.world.level.levelgen.structure.pools.JigsawJunction;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.mutable.MutableObject;
import org.slf4j.Logger;

public class JigsawPlacement {
    static final Logger LOGGER = LogUtils.getLogger();

    public static Optional<PieceGenerator<JigsawConfiguration>> addPieces(PieceGeneratorSupplier.Context<JigsawConfiguration> context2, PieceFactory pieceFactory, BlockPos blockPos, boolean bl, boolean bl2) {
        WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(0L));
        worldgenRandom.setLargeFeatureSeed(context2.seed(), context2.chunkPos().x, context2.chunkPos().z);
        RegistryAccess registryAccess = context2.registryAccess();
        JigsawConfiguration jigsawConfiguration = context2.config();
        ChunkGenerator chunkGenerator = context2.chunkGenerator();
        StructureManager structureManager = context2.structureManager();
        LevelHeightAccessor levelHeightAccessor = context2.heightAccessor();
        Predicate<Holder<Biome>> predicate = context2.validBiome();
        StructureFeature.bootstrap();
        Registry<StructureTemplatePool> registry = registryAccess.registryOrThrow(Registry.TEMPLATE_POOL_REGISTRY);
        Rotation rotation = Rotation.getRandom(worldgenRandom);
        StructureTemplatePool structureTemplatePool = jigsawConfiguration.startPool().value();
        StructurePoolElement structurePoolElement = structureTemplatePool.getRandomTemplate(worldgenRandom);
        if (structurePoolElement == EmptyPoolElement.INSTANCE) {
            return Optional.empty();
        }
        PoolElementStructurePiece poolElementStructurePiece = pieceFactory.create(structureManager, structurePoolElement, blockPos, structurePoolElement.getGroundLevelDelta(), rotation, structurePoolElement.getBoundingBox(structureManager, blockPos, rotation));
        BoundingBox boundingBox = poolElementStructurePiece.getBoundingBox();
        int n = (boundingBox.maxX() + boundingBox.minX()) / 2;
        int n2 = (boundingBox.maxZ() + boundingBox.minZ()) / 2;
        int n3 = bl2 ? blockPos.getY() + chunkGenerator.getFirstFreeHeight(n, n2, Heightmap.Types.WORLD_SURFACE_WG, levelHeightAccessor) : blockPos.getY();
        if (!predicate.test(chunkGenerator.getNoiseBiome(QuartPos.fromBlock(n), QuartPos.fromBlock(n3), QuartPos.fromBlock(n2)))) {
            return Optional.empty();
        }
        int n4 = boundingBox.minY() + poolElementStructurePiece.getGroundLevelDelta();
        poolElementStructurePiece.move(0, n3 - n4, 0);
        return Optional.of((structurePiecesBuilder, context) -> {
            ArrayList arrayList = Lists.newArrayList();
            arrayList.add(poolElementStructurePiece);
            if (jigsawConfiguration.maxDepth() <= 0) {
                return;
            }
            int n4 = 80;
            AABB aABB = new AABB(n - 80, n3 - 80, n2 - 80, n + 80 + 1, n3 + 80 + 1, n2 + 80 + 1);
            Placer placer = new Placer(registry, jigsawConfiguration.maxDepth(), pieceFactory, chunkGenerator, structureManager, arrayList, worldgenRandom);
            placer.placing.addLast(new PieceState(poolElementStructurePiece, (MutableObject<VoxelShape>)new MutableObject((Object)Shapes.join(Shapes.create(aABB), Shapes.create(AABB.of(boundingBox)), BooleanOp.ONLY_FIRST)), 0));
            while (!placer.placing.isEmpty()) {
                PieceState pieceState = placer.placing.removeFirst();
                placer.tryPlacingChildren(pieceState.piece, pieceState.free, pieceState.depth, bl, levelHeightAccessor);
            }
            arrayList.forEach(structurePiecesBuilder::addPiece);
        });
    }

    public static void addPieces(RegistryAccess registryAccess, PoolElementStructurePiece poolElementStructurePiece, int n, PieceFactory pieceFactory, ChunkGenerator chunkGenerator, StructureManager structureManager, List<? super PoolElementStructurePiece> list, Random random, LevelHeightAccessor levelHeightAccessor) {
        Registry<StructureTemplatePool> registry = registryAccess.registryOrThrow(Registry.TEMPLATE_POOL_REGISTRY);
        Placer placer = new Placer(registry, n, pieceFactory, chunkGenerator, structureManager, list, random);
        placer.placing.addLast(new PieceState(poolElementStructurePiece, (MutableObject<VoxelShape>)new MutableObject((Object)Shapes.INFINITY), 0));
        while (!placer.placing.isEmpty()) {
            PieceState pieceState = placer.placing.removeFirst();
            placer.tryPlacingChildren(pieceState.piece, pieceState.free, pieceState.depth, false, levelHeightAccessor);
        }
    }

    public static interface PieceFactory {
        public PoolElementStructurePiece create(StructureManager var1, StructurePoolElement var2, BlockPos var3, int var4, Rotation var5, BoundingBox var6);
    }

    static final class Placer {
        private final Registry<StructureTemplatePool> pools;
        private final int maxDepth;
        private final PieceFactory factory;
        private final ChunkGenerator chunkGenerator;
        private final StructureManager structureManager;
        private final List<? super PoolElementStructurePiece> pieces;
        private final Random random;
        final Deque<PieceState> placing = Queues.newArrayDeque();

        Placer(Registry<StructureTemplatePool> registry, int n, PieceFactory pieceFactory, ChunkGenerator chunkGenerator, StructureManager structureManager, List<? super PoolElementStructurePiece> list, Random random) {
            this.pools = registry;
            this.maxDepth = n;
            this.factory = pieceFactory;
            this.chunkGenerator = chunkGenerator;
            this.structureManager = structureManager;
            this.pieces = list;
            this.random = random;
        }

        void tryPlacingChildren(PoolElementStructurePiece poolElementStructurePiece, MutableObject<VoxelShape> mutableObject, int n, boolean bl, LevelHeightAccessor levelHeightAccessor) {
            StructurePoolElement structurePoolElement = poolElementStructurePiece.getElement();
            BlockPos blockPos = poolElementStructurePiece.getPosition();
            Rotation rotation = poolElementStructurePiece.getRotation();
            StructureTemplatePool.Projection projection = structurePoolElement.getProjection();
            boolean bl2 = projection == StructureTemplatePool.Projection.RIGID;
            MutableObject<VoxelShape> mutableObject2 = new MutableObject<VoxelShape>();
            BoundingBox boundingBox = poolElementStructurePiece.getBoundingBox();
            int n2 = boundingBox.minY();
            block0: for (StructureTemplate.StructureBlockInfo structureBlockInfo2 : structurePoolElement.getShuffledJigsawBlocks(this.structureManager, blockPos, rotation, this.random)) {
                StructurePoolElement structurePoolElement2;
                MutableObject<VoxelShape> mutableObject3;
                Direction direction = JigsawBlock.getFrontFacing(structureBlockInfo2.state);
                BlockPos blockPos2 = structureBlockInfo2.pos;
                BlockPos blockPos3 = blockPos2.relative(direction);
                int n3 = blockPos2.getY() - n2;
                int n4 = -1;
                ResourceLocation resourceLocation = new ResourceLocation(structureBlockInfo2.nbt.getString("pool"));
                Optional<StructureTemplatePool> optional = this.pools.getOptional(resourceLocation);
                if (!optional.isPresent() || optional.get().size() == 0 && !Objects.equals(resourceLocation, Pools.EMPTY.location())) {
                    LOGGER.warn("Empty or non-existent pool: {}", (Object)resourceLocation);
                    continue;
                }
                ResourceLocation resourceLocation2 = optional.get().getFallback();
                Optional<StructureTemplatePool> optional2 = this.pools.getOptional(resourceLocation2);
                if (!optional2.isPresent() || optional2.get().size() == 0 && !Objects.equals(resourceLocation2, Pools.EMPTY.location())) {
                    LOGGER.warn("Empty or non-existent fallback pool: {}", (Object)resourceLocation2);
                    continue;
                }
                boolean bl3 = boundingBox.isInside(blockPos3);
                if (bl3) {
                    mutableObject3 = mutableObject2;
                    if (mutableObject2.getValue() == null) {
                        mutableObject2.setValue((Object)Shapes.create(AABB.of(boundingBox)));
                    }
                } else {
                    mutableObject3 = mutableObject;
                }
                ArrayList arrayList = Lists.newArrayList();
                if (n != this.maxDepth) {
                    arrayList.addAll(optional.get().getShuffledTemplates(this.random));
                }
                arrayList.addAll(optional2.get().getShuffledTemplates(this.random));
                Iterator iterator = arrayList.iterator();
                while (iterator.hasNext() && (structurePoolElement2 = (StructurePoolElement)iterator.next()) != EmptyPoolElement.INSTANCE) {
                    for (Rotation rotation2 : Rotation.getShuffled(this.random)) {
                        List<StructureTemplate.StructureBlockInfo> list = structurePoolElement2.getShuffledJigsawBlocks(this.structureManager, BlockPos.ZERO, rotation2, this.random);
                        BoundingBox boundingBox2 = structurePoolElement2.getBoundingBox(this.structureManager, BlockPos.ZERO, rotation2);
                        int n5 = !bl || boundingBox2.getYSpan() > 16 ? 0 : list.stream().mapToInt(structureBlockInfo -> {
                            if (!boundingBox2.isInside(structureBlockInfo.pos.relative(JigsawBlock.getFrontFacing(structureBlockInfo.state)))) {
                                return 0;
                            }
                            ResourceLocation resourceLocation = new ResourceLocation(structureBlockInfo.nbt.getString("pool"));
                            Optional<StructureTemplatePool> optional = this.pools.getOptional(resourceLocation);
                            Optional<Integer> optional2 = optional.flatMap(structureTemplatePool -> this.pools.getOptional(structureTemplatePool.getFallback()));
                            int n = optional.map(structureTemplatePool -> structureTemplatePool.getMaxSize(this.structureManager)).orElse(0);
                            int n2 = optional2.map(structureTemplatePool -> structureTemplatePool.getMaxSize(this.structureManager)).orElse(0);
                            return Math.max(n, n2);
                        }).max().orElse(0);
                        for (StructureTemplate.StructureBlockInfo structureBlockInfo3 : list) {
                            int n6;
                            int n7;
                            int n8;
                            if (!JigsawBlock.canAttach(structureBlockInfo2, structureBlockInfo3)) continue;
                            BlockPos blockPos4 = structureBlockInfo3.pos;
                            BlockPos blockPos5 = blockPos3.subtract(blockPos4);
                            BoundingBox boundingBox3 = structurePoolElement2.getBoundingBox(this.structureManager, blockPos5, rotation2);
                            int n9 = boundingBox3.minY();
                            StructureTemplatePool.Projection projection2 = structurePoolElement2.getProjection();
                            boolean bl4 = projection2 == StructureTemplatePool.Projection.RIGID;
                            int n10 = blockPos4.getY();
                            int n11 = n3 - n10 + JigsawBlock.getFrontFacing(structureBlockInfo2.state).getStepY();
                            if (bl2 && bl4) {
                                n8 = n2 + n11;
                            } else {
                                if (n4 == -1) {
                                    n4 = this.chunkGenerator.getFirstFreeHeight(blockPos2.getX(), blockPos2.getZ(), Heightmap.Types.WORLD_SURFACE_WG, levelHeightAccessor);
                                }
                                n8 = n4 - n10;
                            }
                            int n12 = n8 - n9;
                            BoundingBox boundingBox4 = boundingBox3.moved(0, n12, 0);
                            BlockPos blockPos6 = blockPos5.offset(0, n12, 0);
                            if (n5 > 0) {
                                n7 = Math.max(n5 + 1, boundingBox4.maxY() - boundingBox4.minY());
                                boundingBox4.encapsulate(new BlockPos(boundingBox4.minX(), boundingBox4.minY() + n7, boundingBox4.minZ()));
                            }
                            if (Shapes.joinIsNotEmpty((VoxelShape)mutableObject3.getValue(), Shapes.create(AABB.of(boundingBox4).deflate(0.25)), BooleanOp.ONLY_SECOND)) continue;
                            mutableObject3.setValue((Object)Shapes.joinUnoptimized((VoxelShape)mutableObject3.getValue(), Shapes.create(AABB.of(boundingBox4)), BooleanOp.ONLY_FIRST));
                            n7 = poolElementStructurePiece.getGroundLevelDelta();
                            int n13 = bl4 ? n7 - n11 : structurePoolElement2.getGroundLevelDelta();
                            PoolElementStructurePiece poolElementStructurePiece2 = this.factory.create(this.structureManager, structurePoolElement2, blockPos6, n13, rotation2, boundingBox4);
                            if (bl2) {
                                n6 = n2 + n3;
                            } else if (bl4) {
                                n6 = n8 + n10;
                            } else {
                                if (n4 == -1) {
                                    n4 = this.chunkGenerator.getFirstFreeHeight(blockPos2.getX(), blockPos2.getZ(), Heightmap.Types.WORLD_SURFACE_WG, levelHeightAccessor);
                                }
                                n6 = n4 + n11 / 2;
                            }
                            poolElementStructurePiece.addJunction(new JigsawJunction(blockPos3.getX(), n6 - n3 + n7, blockPos3.getZ(), n11, projection2));
                            poolElementStructurePiece2.addJunction(new JigsawJunction(blockPos2.getX(), n6 - n10 + n13, blockPos2.getZ(), -n11, projection));
                            this.pieces.add(poolElementStructurePiece2);
                            if (n + 1 > this.maxDepth) continue block0;
                            this.placing.addLast(new PieceState(poolElementStructurePiece2, mutableObject3, n + 1));
                            continue block0;
                        }
                    }
                }
            }
        }
    }

    static final class PieceState {
        final PoolElementStructurePiece piece;
        final MutableObject<VoxelShape> free;
        final int depth;

        PieceState(PoolElementStructurePiece poolElementStructurePiece, MutableObject<VoxelShape> mutableObject, int n) {
            this.piece = poolElementStructurePiece;
            this.free = mutableObject;
            this.depth = n;
        }
    }
}

