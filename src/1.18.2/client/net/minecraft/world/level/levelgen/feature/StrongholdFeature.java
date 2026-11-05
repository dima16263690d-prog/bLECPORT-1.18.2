/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.StrongholdPieces;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class StrongholdFeature
extends StructureFeature<NoneFeatureConfiguration> {
    public StrongholdFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec, PieceGeneratorSupplier.simple(StrongholdFeature::checkLocation, StrongholdFeature::generatePieces));
    }

    private static boolean checkLocation(PieceGeneratorSupplier.Context<NoneFeatureConfiguration> context) {
        return true;
    }

    private static void generatePieces(StructurePiecesBuilder structurePiecesBuilder, PieceGenerator.Context<NoneFeatureConfiguration> context) {
        StrongholdPieces.StartPiece startPiece;
        int n = 0;
        do {
            structurePiecesBuilder.clear();
            context.random().setLargeFeatureSeed(context.seed() + (long)n++, context.chunkPos().x, context.chunkPos().z);
            StrongholdPieces.resetPieces();
            startPiece = new StrongholdPieces.StartPiece(context.random(), context.chunkPos().getBlockX(2), context.chunkPos().getBlockZ(2));
            structurePiecesBuilder.addPiece(startPiece);
            startPiece.addChildren(startPiece, structurePiecesBuilder, context.random());
            List<StructurePiece> list = startPiece.pendingChildren;
            while (!list.isEmpty()) {
                int n2 = context.random().nextInt(list.size());
                StructurePiece structurePiece = list.remove(n2);
                structurePiece.addChildren(startPiece, structurePiecesBuilder, context.random());
            }
            structurePiecesBuilder.moveBelowSeaLevel(context.chunkGenerator().getSeaLevel(), context.chunkGenerator().getMinY(), context.random(), 10);
        } while (structurePiecesBuilder.isEmpty() || startPiece.portalRoomPiece == null);
    }
}

