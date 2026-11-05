/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.levelgen.structure;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.RangeConfiguration;
import net.minecraft.world.level.levelgen.structure.NetherFossilPieces;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class NetherFossilFeature
extends StructureFeature<RangeConfiguration> {
    public NetherFossilFeature(Codec<RangeConfiguration> codec) {
        super(codec, NetherFossilFeature::pieceGeneratorSupplier);
    }

    private static Optional<PieceGenerator<RangeConfiguration>> pieceGeneratorSupplier(PieceGeneratorSupplier.Context<RangeConfiguration> context) {
        Object object;
        WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(0L));
        worldgenRandom.setLargeFeatureSeed(context.seed(), context.chunkPos().x, context.chunkPos().z);
        int n = context.chunkPos().getMinBlockX() + worldgenRandom.nextInt(16);
        int n2 = context.chunkPos().getMinBlockZ() + worldgenRandom.nextInt(16);
        int n3 = context.chunkGenerator().getSeaLevel();
        WorldGenerationContext worldGenerationContext = new WorldGenerationContext(context.chunkGenerator(), context.heightAccessor());
        int n4 = context.config().height.sample(worldgenRandom, worldGenerationContext);
        NoiseColumn noiseColumn = context.chunkGenerator().getBaseColumn(n, n2, context.heightAccessor());
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(n, n4, n2);
        while (n4 > n3) {
            object = noiseColumn.getBlock(n4);
            BlockState blockState = noiseColumn.getBlock(--n4);
            if (!((BlockBehaviour.BlockStateBase)object).isAir() || !blockState.is(Blocks.SOUL_SAND) && !blockState.isFaceSturdy(EmptyBlockGetter.INSTANCE, mutableBlockPos.setY(n4), Direction.UP)) continue;
            break;
        }
        if (n4 <= n3) {
            return Optional.empty();
        }
        if (!context.validBiome().test(context.chunkGenerator().getNoiseBiome(QuartPos.fromBlock(n), QuartPos.fromBlock(n4), QuartPos.fromBlock(n2)))) {
            return Optional.empty();
        }
        object = new BlockPos(n, n4, n2);
        return Optional.of((arg_0, arg_1) -> NetherFossilFeature.lambda$pieceGeneratorSupplier$0(context, worldgenRandom, (BlockPos)object, arg_0, arg_1));
    }

    private static /* synthetic */ void lambda$pieceGeneratorSupplier$0(PieceGeneratorSupplier.Context context, WorldgenRandom worldgenRandom, BlockPos blockPos, StructurePiecesBuilder structurePiecesBuilder, PieceGenerator.Context context2) {
        NetherFossilPieces.addPieces(context.structureManager(), structurePiecesBuilder, worldgenRandom, blockPos);
    }
}

