/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableSet
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.Lifecycle
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.dimension;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

public final class LevelStem {
    public static final Codec<LevelStem> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)DimensionType.CODEC.fieldOf("type").forGetter(LevelStem::typeHolder), (App)ChunkGenerator.CODEC.fieldOf("generator").forGetter(LevelStem::generator)).apply((Applicative)instance, instance.stable(LevelStem::new)));
    public static final ResourceKey<LevelStem> OVERWORLD = ResourceKey.create(Registry.LEVEL_STEM_REGISTRY, new ResourceLocation("overworld"));
    public static final ResourceKey<LevelStem> NETHER = ResourceKey.create(Registry.LEVEL_STEM_REGISTRY, new ResourceLocation("the_nether"));
    public static final ResourceKey<LevelStem> END = ResourceKey.create(Registry.LEVEL_STEM_REGISTRY, new ResourceLocation("the_end"));
    private static final Set<ResourceKey<LevelStem>> BUILTIN_ORDER = ImmutableSet.of(OVERWORLD, NETHER, END);
    private final Holder<DimensionType> type;
    private final ChunkGenerator generator;

    public LevelStem(Holder<DimensionType> holder, ChunkGenerator chunkGenerator) {
        this.type = holder;
        this.generator = chunkGenerator;
    }

    public Holder<DimensionType> typeHolder() {
        return this.type;
    }

    public ChunkGenerator generator() {
        return this.generator;
    }

    public static Registry<LevelStem> sortMap(Registry<LevelStem> registry) {
        Object object;
        MappedRegistry<LevelStem> mappedRegistry = new MappedRegistry<LevelStem>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental(), null);
        for (ResourceKey<LevelStem> object2 : BUILTIN_ORDER) {
            object = registry.get(object2);
            if (object == null) continue;
            ((WritableRegistry)mappedRegistry).register(object2, (LevelStem)object, registry.lifecycle((LevelStem)object));
        }
        for (Map.Entry entry : registry.entrySet()) {
            object = (ResourceKey)entry.getKey();
            if (BUILTIN_ORDER.contains(object)) continue;
            ((WritableRegistry)mappedRegistry).register((ResourceKey<LevelStem>)object, (LevelStem)entry.getValue(), registry.lifecycle((LevelStem)entry.getValue()));
        }
        return mappedRegistry;
    }

    public static boolean stable(long l, Registry<LevelStem> registry) {
        if (registry.size() != BUILTIN_ORDER.size()) {
            return false;
        }
        Optional<LevelStem> optional = registry.getOptional(OVERWORLD);
        Optional<LevelStem> optional2 = registry.getOptional(NETHER);
        Optional<LevelStem> optional3 = registry.getOptional(END);
        if (optional.isEmpty() || optional2.isEmpty() || optional3.isEmpty()) {
            return false;
        }
        if (!optional.get().typeHolder().is(DimensionType.OVERWORLD_LOCATION) && !optional.get().typeHolder().is(DimensionType.OVERWORLD_CAVES_LOCATION)) {
            return false;
        }
        if (!optional2.get().typeHolder().is(DimensionType.NETHER_LOCATION)) {
            return false;
        }
        if (!optional3.get().typeHolder().is(DimensionType.END_LOCATION)) {
            return false;
        }
        if (!(optional2.get().generator() instanceof NoiseBasedChunkGenerator) || !(optional3.get().generator() instanceof NoiseBasedChunkGenerator)) {
            return false;
        }
        NoiseBasedChunkGenerator noiseBasedChunkGenerator = (NoiseBasedChunkGenerator)optional2.get().generator();
        NoiseBasedChunkGenerator noiseBasedChunkGenerator2 = (NoiseBasedChunkGenerator)optional3.get().generator();
        if (!noiseBasedChunkGenerator.stable(l, NoiseGeneratorSettings.NETHER)) {
            return false;
        }
        if (!noiseBasedChunkGenerator2.stable(l, NoiseGeneratorSettings.END)) {
            return false;
        }
        if (!(noiseBasedChunkGenerator.getBiomeSource() instanceof MultiNoiseBiomeSource)) {
            return false;
        }
        MultiNoiseBiomeSource multiNoiseBiomeSource = (MultiNoiseBiomeSource)noiseBasedChunkGenerator.getBiomeSource();
        if (!multiNoiseBiomeSource.stable(MultiNoiseBiomeSource.Preset.NETHER)) {
            return false;
        }
        BiomeSource biomeSource = optional.get().generator().getBiomeSource();
        if (biomeSource instanceof MultiNoiseBiomeSource && !((MultiNoiseBiomeSource)biomeSource).stable(MultiNoiseBiomeSource.Preset.OVERWORLD)) {
            return false;
        }
        if (!(noiseBasedChunkGenerator2.getBiomeSource() instanceof TheEndBiomeSource)) {
            return false;
        }
        TheEndBiomeSource theEndBiomeSource = (TheEndBiomeSource)noiseBasedChunkGenerator2.getBiomeSource();
        return theEndBiomeSource.stable(l);
    }
}

