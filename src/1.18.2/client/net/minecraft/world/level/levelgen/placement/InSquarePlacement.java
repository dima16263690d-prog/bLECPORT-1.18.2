/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public class InSquarePlacement
extends PlacementModifier {
    private static final InSquarePlacement INSTANCE = new InSquarePlacement();
    public static final Codec<InSquarePlacement> CODEC = Codec.unit(() -> INSTANCE);

    public static InSquarePlacement spread() {
        return INSTANCE;
    }

    @Override
    public Stream<BlockPos> getPositions(PlacementContext placementContext, Random random, BlockPos blockPos) {
        int n = random.nextInt(16) + blockPos.getX();
        int n2 = random.nextInt(16) + blockPos.getZ();
        return Stream.of(new BlockPos(n, blockPos.getY(), n2));
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifierType.IN_SQUARE;
    }
}

