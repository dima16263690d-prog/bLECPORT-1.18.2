/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.biome;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

public class CheckerboardColumnBiomeSource
extends BiomeSource {
    public static final Codec<CheckerboardColumnBiomeSource> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Biome.LIST_CODEC.fieldOf("biomes").forGetter(checkerboardColumnBiomeSource -> checkerboardColumnBiomeSource.allowedBiomes), (App)Codec.intRange((int)0, (int)62).fieldOf("scale").orElse((Object)2).forGetter(checkerboardColumnBiomeSource -> checkerboardColumnBiomeSource.size)).apply((Applicative)instance, CheckerboardColumnBiomeSource::new));
    private final HolderSet<Biome> allowedBiomes;
    private final int bitShift;
    private final int size;

    public CheckerboardColumnBiomeSource(HolderSet<Biome> holderSet, int n) {
        super(holderSet.stream());
        this.allowedBiomes = holderSet;
        this.bitShift = n + 2;
        this.size = n;
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
        return this.allowedBiomes.get(Math.floorMod((n >> this.bitShift) + (n3 >> this.bitShift), this.allowedBiomes.size()));
    }
}

