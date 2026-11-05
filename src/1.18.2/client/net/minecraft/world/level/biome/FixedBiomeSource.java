/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.Sets
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Codec
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

public class FixedBiomeSource
extends BiomeSource
implements BiomeManager.NoiseBiomeSource {
    public static final Codec<FixedBiomeSource> CODEC = Biome.CODEC.fieldOf("biome").xmap(FixedBiomeSource::new, fixedBiomeSource -> fixedBiomeSource.biome).stable().codec();
    private final Holder<Biome> biome;

    public FixedBiomeSource(Holder<Biome> holder) {
        super((List<Holder<Biome>>)ImmutableList.of(holder));
        this.biome = holder;
    }

    @Override
    protected Codec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    public BiomeSource withSeed(long l) {
        return this;
    }

    @Override
    public Holder<Biome> getNoiseBiome(int n, int n2, int n3, Climate.Sampler sampler) {
        return this.biome;
    }

    @Override
    public Holder<Biome> getNoiseBiome(int n, int n2, int n3) {
        return this.biome;
    }

    @Override
    @Nullable
    public Pair<BlockPos, Holder<Biome>> findBiomeHorizontal(int n, int n2, int n3, int n4, int n5, Predicate<Holder<Biome>> predicate, Random random, boolean bl, Climate.Sampler sampler) {
        if (predicate.test(this.biome)) {
            if (bl) {
                return Pair.of((Object)new BlockPos(n, n2, n3), this.biome);
            }
            return Pair.of((Object)new BlockPos(n - n4 + random.nextInt(n4 * 2 + 1), n2, n3 - n4 + random.nextInt(n4 * 2 + 1)), this.biome);
        }
        return null;
    }

    @Override
    public Set<Holder<Biome>> getBiomesWithin(int n, int n2, int n3, int n4, Climate.Sampler sampler) {
        return Sets.newHashSet((Iterable)Set.of(this.biome));
    }
}

