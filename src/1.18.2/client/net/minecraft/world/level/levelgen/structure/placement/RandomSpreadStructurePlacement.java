/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.structure.placement;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Vec3i;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;

public record RandomSpreadStructurePlacement(int spacing, int separation, RandomSpreadType spreadType, int salt, Vec3i locateOffset) implements StructurePlacement
{
    public static final Codec<RandomSpreadStructurePlacement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.intRange((int)0, (int)4096).fieldOf("spacing").forGetter(RandomSpreadStructurePlacement::spacing), (App)Codec.intRange((int)0, (int)4096).fieldOf("separation").forGetter(RandomSpreadStructurePlacement::separation), (App)RandomSpreadType.CODEC.optionalFieldOf("spread_type", (Object)RandomSpreadType.LINEAR).forGetter(RandomSpreadStructurePlacement::spreadType), (App)ExtraCodecs.NON_NEGATIVE_INT.fieldOf("salt").forGetter(RandomSpreadStructurePlacement::salt), (App)Vec3i.offsetCodec(16).optionalFieldOf("locate_offset", (Object)Vec3i.ZERO).forGetter(RandomSpreadStructurePlacement::locateOffset)).apply((Applicative)instance, RandomSpreadStructurePlacement::new)).flatXmap(randomSpreadStructurePlacement -> {
        if (randomSpreadStructurePlacement.spacing <= randomSpreadStructurePlacement.separation) {
            return DataResult.error((String)"Spacing has to be larger than separation");
        }
        return DataResult.success((Object)randomSpreadStructurePlacement);
    }, DataResult::success).codec();

    public RandomSpreadStructurePlacement(int n, int n2, RandomSpreadType randomSpreadType, int n3) {
        this(n, n2, randomSpreadType, n3, Vec3i.ZERO);
    }

    public ChunkPos getPotentialFeatureChunk(long l, int n, int n2) {
        int n3 = this.spacing();
        int n4 = this.separation();
        int n5 = Math.floorDiv(n, n3);
        int n6 = Math.floorDiv(n2, n3);
        WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(0L));
        worldgenRandom.setLargeFeatureWithSalt(l, n5, n6, this.salt());
        int n7 = n3 - n4;
        int n8 = this.spreadType().evaluate(worldgenRandom, n7);
        int n9 = this.spreadType().evaluate(worldgenRandom, n7);
        return new ChunkPos(n5 * n3 + n8, n6 * n3 + n9);
    }

    @Override
    public boolean isFeatureChunk(ChunkGenerator chunkGenerator, long l, int n, int n2) {
        ChunkPos chunkPos = this.getPotentialFeatureChunk(l, n, n2);
        return chunkPos.x == n && chunkPos.z == n2;
    }

    @Override
    public StructurePlacementType<?> type() {
        return StructurePlacementType.RANDOM_SPREAD;
    }
}

