/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Objects;
import java.util.Set;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.OceanMonumentPieces;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class OceanMonumentFeature
extends StructureFeature<NoneFeatureConfiguration> {
    public OceanMonumentFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec, PieceGeneratorSupplier.simple(OceanMonumentFeature::checkLocation, OceanMonumentFeature::generatePieces));
    }

    private static boolean checkLocation(PieceGeneratorSupplier.Context<NoneFeatureConfiguration> context) {
        int n = context.chunkPos().getBlockX(9);
        int n2 = context.chunkPos().getBlockZ(9);
        Set<Holder<Biome>> set = context.biomeSource().getBiomesWithin(n, context.chunkGenerator().getSeaLevel(), n2, 29, context.chunkGenerator().climateSampler());
        for (Holder<Biome> holder : set) {
            if (Biome.getBiomeCategory(holder) == Biome.BiomeCategory.OCEAN || Biome.getBiomeCategory(holder) == Biome.BiomeCategory.RIVER) continue;
            return false;
        }
        return context.validBiomeOnTop(Heightmap.Types.OCEAN_FLOOR_WG);
    }

    private static StructurePiece createTopPiece(ChunkPos chunkPos, WorldgenRandom worldgenRandom) {
        int n = chunkPos.getMinBlockX() - 29;
        int n2 = chunkPos.getMinBlockZ() - 29;
        Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(worldgenRandom);
        return new OceanMonumentPieces.MonumentBuilding(worldgenRandom, n, n2, direction);
    }

    private static void generatePieces(StructurePiecesBuilder structurePiecesBuilder, PieceGenerator.Context<NoneFeatureConfiguration> context) {
        structurePiecesBuilder.addPiece(OceanMonumentFeature.createTopPiece(context.chunkPos(), context.random()));
    }

    public static PiecesContainer regeneratePiecesAfterLoad(ChunkPos chunkPos, long l, PiecesContainer piecesContainer) {
        if (piecesContainer.isEmpty()) {
            return piecesContainer;
        }
        WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(RandomSupport.seedUniquifier()));
        worldgenRandom.setLargeFeatureSeed(l, chunkPos.x, chunkPos.z);
        StructurePiece structurePiece = piecesContainer.pieces().get(0);
        BoundingBox boundingBox = structurePiece.getBoundingBox();
        int n = boundingBox.minX();
        int n2 = boundingBox.minZ();
        Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(worldgenRandom);
        Direction direction2 = (Direction)Objects.requireNonNullElse((Object)structurePiece.getOrientation(), (Object)direction);
        OceanMonumentPieces.MonumentBuilding monumentBuilding = new OceanMonumentPieces.MonumentBuilding(worldgenRandom, n, n2, direction2);
        StructurePiecesBuilder structurePiecesBuilder = new StructurePiecesBuilder();
        structurePiecesBuilder.addPiece(monumentBuilding);
        return structurePiecesBuilder.build();
    }
}

