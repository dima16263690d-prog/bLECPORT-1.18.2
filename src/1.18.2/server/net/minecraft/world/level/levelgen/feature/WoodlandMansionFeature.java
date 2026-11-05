/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.WoodlandMansionPieces;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;

public class WoodlandMansionFeature
extends StructureFeature<NoneFeatureConfiguration> {
    public WoodlandMansionFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec, WoodlandMansionFeature::pieceGeneratorSupplier, WoodlandMansionFeature::afterPlace);
    }

    private static Optional<PieceGenerator<NoneFeatureConfiguration>> pieceGeneratorSupplier(PieceGeneratorSupplier.Context<NoneFeatureConfiguration> context2) {
        WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(0L));
        worldgenRandom.setLargeFeatureSeed(context2.seed(), context2.chunkPos().x, context2.chunkPos().z);
        Rotation rotation = Rotation.getRandom(worldgenRandom);
        int n = 5;
        int n2 = 5;
        if (rotation == Rotation.CLOCKWISE_90) {
            n = -5;
        } else if (rotation == Rotation.CLOCKWISE_180) {
            n = -5;
            n2 = -5;
        } else if (rotation == Rotation.COUNTERCLOCKWISE_90) {
            n2 = -5;
        }
        int n3 = context2.chunkPos().getBlockX(7);
        int n4 = context2.chunkPos().getBlockZ(7);
        int[] nArray = context2.getCornerHeights(n3, n, n4, n2);
        int n5 = Math.min(Math.min(nArray[0], nArray[1]), Math.min(nArray[2], nArray[3]));
        if (n5 < 60) {
            return Optional.empty();
        }
        if (!context2.validBiome().test(context2.chunkGenerator().getNoiseBiome(QuartPos.fromBlock(n3), QuartPos.fromBlock(nArray[0]), QuartPos.fromBlock(n4)))) {
            return Optional.empty();
        }
        BlockPos blockPos = new BlockPos(context2.chunkPos().getMiddleBlockX(), n5 + 1, context2.chunkPos().getMiddleBlockZ());
        return Optional.of((structurePiecesBuilder, context) -> {
            LinkedList linkedList = Lists.newLinkedList();
            WoodlandMansionPieces.generateMansion(context.structureManager(), blockPos, rotation, linkedList, worldgenRandom);
            linkedList.forEach(structurePiecesBuilder::addPiece);
        });
    }

    private static void afterPlace(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, PiecesContainer piecesContainer) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        int n = worldGenLevel.getMinBuildHeight();
        BoundingBox boundingBox2 = piecesContainer.calculateBoundingBox();
        int n2 = boundingBox2.minY();
        for (int i = boundingBox.minX(); i <= boundingBox.maxX(); ++i) {
            block1: for (int j = boundingBox.minZ(); j <= boundingBox.maxZ(); ++j) {
                mutableBlockPos.set(i, n2, j);
                if (worldGenLevel.isEmptyBlock(mutableBlockPos) || !boundingBox2.isInside(mutableBlockPos) || !piecesContainer.isInsidePiece(mutableBlockPos)) continue;
                for (int k = n2 - 1; k > n; --k) {
                    mutableBlockPos.setY(k);
                    if (!worldGenLevel.isEmptyBlock(mutableBlockPos) && !worldGenLevel.getBlockState(mutableBlockPos).getMaterial().isLiquid()) continue block1;
                    worldGenLevel.setBlock(mutableBlockPos, Blocks.COBBLESTONE.defaultBlockState(), 2);
                }
            }
        }
    }
}

