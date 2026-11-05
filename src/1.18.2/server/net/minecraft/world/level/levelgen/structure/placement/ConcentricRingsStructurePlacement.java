/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.structure.placement;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;

public record ConcentricRingsStructurePlacement(int distance, int spread, int count) implements StructurePlacement
{
    public static final Codec<ConcentricRingsStructurePlacement> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.intRange((int)0, (int)1023).fieldOf("distance").forGetter(ConcentricRingsStructurePlacement::distance), (App)Codec.intRange((int)0, (int)1023).fieldOf("spread").forGetter(ConcentricRingsStructurePlacement::spread), (App)Codec.intRange((int)1, (int)4095).fieldOf("count").forGetter(ConcentricRingsStructurePlacement::count)).apply((Applicative)instance, ConcentricRingsStructurePlacement::new));

    @Override
    public boolean isFeatureChunk(ChunkGenerator chunkGenerator, long l, int n, int n2) {
        List<ChunkPos> list = chunkGenerator.getRingPositionsFor(this);
        if (list == null) {
            return false;
        }
        return list.contains(new ChunkPos(n, n2));
    }

    @Override
    public StructurePlacementType<?> type() {
        return StructurePlacementType.CONCENTRIC_RINGS;
    }
}

