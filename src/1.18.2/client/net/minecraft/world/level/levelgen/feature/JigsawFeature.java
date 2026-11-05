/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;

public class JigsawFeature
extends StructureFeature<JigsawConfiguration> {
    public JigsawFeature(Codec<JigsawConfiguration> codec, int n, boolean bl, boolean bl2, Predicate<PieceGeneratorSupplier.Context<JigsawConfiguration>> predicate) {
        super(codec, context -> {
            if (!predicate.test(context)) {
                return Optional.empty();
            }
            BlockPos blockPos = new BlockPos(context.chunkPos().getMinBlockX(), n, context.chunkPos().getMinBlockZ());
            Pools.bootstrap();
            return JigsawPlacement.addPieces(context, PoolElementStructurePiece::new, blockPos, bl, bl2);
        });
    }
}

