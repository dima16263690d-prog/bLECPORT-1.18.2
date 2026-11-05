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
import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.EndCityPieces;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;

public class EndCityFeature
extends StructureFeature<NoneFeatureConfiguration> {
    private static final int RANDOM_SALT = 10387313;

    public EndCityFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec, EndCityFeature::pieceGeneratorSupplier);
    }

    private static int getYPositionForFeature(ChunkPos chunkPos, ChunkGenerator chunkGenerator, LevelHeightAccessor levelHeightAccessor) {
        Random random = new Random(chunkPos.x + chunkPos.z * 10387313);
        Rotation rotation = Rotation.getRandom(random);
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
        int n3 = chunkPos.getBlockX(7);
        int n4 = chunkPos.getBlockZ(7);
        int n5 = chunkGenerator.getFirstOccupiedHeight(n3, n4, Heightmap.Types.WORLD_SURFACE_WG, levelHeightAccessor);
        int n6 = chunkGenerator.getFirstOccupiedHeight(n3, n4 + n2, Heightmap.Types.WORLD_SURFACE_WG, levelHeightAccessor);
        int n7 = chunkGenerator.getFirstOccupiedHeight(n3 + n, n4, Heightmap.Types.WORLD_SURFACE_WG, levelHeightAccessor);
        int n8 = chunkGenerator.getFirstOccupiedHeight(n3 + n, n4 + n2, Heightmap.Types.WORLD_SURFACE_WG, levelHeightAccessor);
        return Math.min(Math.min(n5, n6), Math.min(n7, n8));
    }

    private static Optional<PieceGenerator<NoneFeatureConfiguration>> pieceGeneratorSupplier(PieceGeneratorSupplier.Context<NoneFeatureConfiguration> context2) {
        int n = EndCityFeature.getYPositionForFeature(context2.chunkPos(), context2.chunkGenerator(), context2.heightAccessor());
        if (n < 60) {
            return Optional.empty();
        }
        BlockPos blockPos = context2.chunkPos().getMiddleBlockPosition(n);
        if (!context2.validBiome().test(context2.chunkGenerator().getNoiseBiome(QuartPos.fromBlock(blockPos.getX()), QuartPos.fromBlock(blockPos.getY()), QuartPos.fromBlock(blockPos.getZ())))) {
            return Optional.empty();
        }
        return Optional.of((structurePiecesBuilder, context) -> {
            Rotation rotation = Rotation.getRandom(context.random());
            ArrayList arrayList = Lists.newArrayList();
            EndCityPieces.startHouseTower(context.structureManager(), blockPos, rotation, arrayList, context.random());
            arrayList.forEach(structurePiecesBuilder::addPiece);
        });
    }
}

