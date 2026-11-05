/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

public class TheEndBiomeSource
extends BiomeSource {
    public static final Codec<TheEndBiomeSource> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)RegistryOps.retrieveRegistry(Registry.BIOME_REGISTRY).forGetter(theEndBiomeSource -> null), (App)Codec.LONG.fieldOf("seed").stable().forGetter(theEndBiomeSource -> theEndBiomeSource.seed)).apply((Applicative)instance, instance.stable(TheEndBiomeSource::new)));
    private static final float ISLAND_THRESHOLD = -0.9f;
    public static final int ISLAND_CHUNK_DISTANCE = 64;
    private static final long ISLAND_CHUNK_DISTANCE_SQR = 4096L;
    private final SimplexNoise islandNoise;
    private final long seed;
    private final Holder<Biome> end;
    private final Holder<Biome> highlands;
    private final Holder<Biome> midlands;
    private final Holder<Biome> islands;
    private final Holder<Biome> barrens;

    public TheEndBiomeSource(Registry<Biome> registry, long l) {
        this(l, registry.getOrCreateHolder(Biomes.THE_END), registry.getOrCreateHolder(Biomes.END_HIGHLANDS), registry.getOrCreateHolder(Biomes.END_MIDLANDS), registry.getOrCreateHolder(Biomes.SMALL_END_ISLANDS), registry.getOrCreateHolder(Biomes.END_BARRENS));
    }

    private TheEndBiomeSource(long l, Holder<Biome> holder, Holder<Biome> holder2, Holder<Biome> holder3, Holder<Biome> holder4, Holder<Biome> holder5) {
        super((List<Holder<Biome>>)ImmutableList.of(holder, holder2, holder3, holder4, holder5));
        this.seed = l;
        this.end = holder;
        this.highlands = holder2;
        this.midlands = holder3;
        this.islands = holder4;
        this.barrens = holder5;
        WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(l));
        worldgenRandom.consumeCount(17292);
        this.islandNoise = new SimplexNoise(worldgenRandom);
    }

    @Override
    protected Codec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    public BiomeSource withSeed(long l) {
        return new TheEndBiomeSource(l, this.end, this.highlands, this.midlands, this.islands, this.barrens);
    }

    @Override
    public Holder<Biome> getNoiseBiome(int n, int n2, int n3, Climate.Sampler sampler) {
        int n4 = n >> 2;
        int n5 = n3 >> 2;
        if ((long)n4 * (long)n4 + (long)n5 * (long)n5 <= 4096L) {
            return this.end;
        }
        float f = TheEndBiomeSource.getHeightValue(this.islandNoise, n4 * 2 + 1, n5 * 2 + 1);
        if (f > 40.0f) {
            return this.highlands;
        }
        if (f >= 0.0f) {
            return this.midlands;
        }
        if (f < -20.0f) {
            return this.islands;
        }
        return this.barrens;
    }

    public boolean stable(long l) {
        return this.seed == l;
    }

    public static float getHeightValue(SimplexNoise simplexNoise, int n, int n2) {
        int n3 = n / 2;
        int n4 = n2 / 2;
        int n5 = n % 2;
        int n6 = n2 % 2;
        float f = 100.0f - Mth.sqrt(n * n + n2 * n2) * 8.0f;
        f = Mth.clamp(f, -100.0f, 80.0f);
        for (int i = -12; i <= 12; ++i) {
            for (int j = -12; j <= 12; ++j) {
                long l = n3 + i;
                long l2 = n4 + j;
                if (l * l + l2 * l2 <= 4096L || !(simplexNoise.getValue(l, l2) < (double)-0.9f)) continue;
                float f2 = (Mth.abs(l) * 3439.0f + Mth.abs(l2) * 147.0f) % 13.0f + 9.0f;
                float f3 = n5 - i * 2;
                float f4 = n6 - j * 2;
                float f5 = 100.0f - Mth.sqrt(f3 * f3 + f4 * f4) * f2;
                f5 = Mth.clamp(f5, -100.0f, 80.0f);
                f = Math.max(f, f5);
            }
        }
        return f;
    }
}

