/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public class HeightmapPlacement
extends PlacementModifier {
    public static final Codec<HeightmapPlacement> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Heightmap.Types.CODEC.fieldOf("heightmap").forGetter(heightmapPlacement -> heightmapPlacement.heightmap)).apply((Applicative)instance, HeightmapPlacement::new));
    private final Heightmap.Types heightmap;

    private HeightmapPlacement(Heightmap.Types types) {
        this.heightmap = types;
    }

    public static HeightmapPlacement onHeightmap(Heightmap.Types types) {
        return new HeightmapPlacement(types);
    }

    @Override
    public Stream<BlockPos> getPositions(PlacementContext placementContext, Random random, BlockPos blockPos) {
        int n;
        int n2 = blockPos.getX();
        int n3 = placementContext.getHeight(this.heightmap, n2, n = blockPos.getZ());
        if (n3 > placementContext.getMinBuildHeight()) {
            return Stream.of(new BlockPos(n2, n3, n));
        }
        return Stream.of(new BlockPos[0]);
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifierType.HEIGHTMAP;
    }
}

