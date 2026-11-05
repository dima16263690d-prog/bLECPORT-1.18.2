/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.levelgen.structure.placement;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;

public interface StructurePlacement {
    public static final Codec<StructurePlacement> CODEC = Registry.STRUCTURE_PLACEMENT_TYPE.byNameCodec().dispatch(StructurePlacement::type, StructurePlacementType::codec);

    public boolean isFeatureChunk(ChunkGenerator var1, long var2, int var4, int var5);

    public StructurePlacementType<?> type();
}

