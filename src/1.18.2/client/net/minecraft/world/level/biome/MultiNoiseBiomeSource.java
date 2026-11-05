/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import net.minecraft.world.level.biome.TerrainShaper;

public class MultiNoiseBiomeSource
extends BiomeSource {
    public static final MapCodec<MultiNoiseBiomeSource> DIRECT_CODEC = RecordCodecBuilder.mapCodec(instance2 -> instance2.group((App)ExtraCodecs.nonEmptyList(RecordCodecBuilder.create(instance -> instance.group((App)Climate.ParameterPoint.CODEC.fieldOf("parameters").forGetter(Pair::getFirst), (App)Biome.CODEC.fieldOf("biome").forGetter(Pair::getSecond)).apply((Applicative)instance, Pair::of)).listOf()).xmap(Climate.ParameterList::new, Climate.ParameterList::values).fieldOf("biomes").forGetter(multiNoiseBiomeSource -> multiNoiseBiomeSource.parameters)).apply((Applicative)instance2, MultiNoiseBiomeSource::new));
    public static final Codec<MultiNoiseBiomeSource> CODEC = Codec.mapEither(PresetInstance.CODEC, DIRECT_CODEC).xmap(either -> (MultiNoiseBiomeSource)either.map(PresetInstance::biomeSource, Function.identity()), multiNoiseBiomeSource -> multiNoiseBiomeSource.preset().map(Either::left).orElseGet(() -> Either.right((Object)multiNoiseBiomeSource))).codec();
    private final Climate.ParameterList<Holder<Biome>> parameters;
    private final Optional<PresetInstance> preset;

    private MultiNoiseBiomeSource(Climate.ParameterList<Holder<Biome>> parameterList) {
        this(parameterList, Optional.empty());
    }

    MultiNoiseBiomeSource(Climate.ParameterList<Holder<Biome>> parameterList, Optional<PresetInstance> optional) {
        super(parameterList.values().stream().map(Pair::getSecond));
        this.preset = optional;
        this.parameters = parameterList;
    }

    @Override
    protected Codec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    public BiomeSource withSeed(long l) {
        return this;
    }

    private Optional<PresetInstance> preset() {
        return this.preset;
    }

    public boolean stable(Preset preset) {
        return this.preset.isPresent() && Objects.equals(this.preset.get().preset(), preset);
    }

    @Override
    public Holder<Biome> getNoiseBiome(int n, int n2, int n3, Climate.Sampler sampler) {
        return this.getNoiseBiome(sampler.sample(n, n2, n3));
    }

    @VisibleForDebug
    public Holder<Biome> getNoiseBiome(Climate.TargetPoint targetPoint) {
        return this.parameters.findValue(targetPoint);
    }

    @Override
    public void addDebugInfo(List<String> list, BlockPos blockPos, Climate.Sampler sampler) {
        int n = QuartPos.fromBlock(blockPos.getX());
        int n2 = QuartPos.fromBlock(blockPos.getY());
        int n3 = QuartPos.fromBlock(blockPos.getZ());
        Climate.TargetPoint targetPoint = sampler.sample(n, n2, n3);
        float f = Climate.unquantizeCoord(targetPoint.continentalness());
        float f2 = Climate.unquantizeCoord(targetPoint.erosion());
        float f3 = Climate.unquantizeCoord(targetPoint.temperature());
        float f4 = Climate.unquantizeCoord(targetPoint.humidity());
        float f5 = Climate.unquantizeCoord(targetPoint.weirdness());
        double d = TerrainShaper.peaksAndValleys(f5);
        OverworldBiomeBuilder overworldBiomeBuilder = new OverworldBiomeBuilder();
        list.add("Biome builder PV: " + OverworldBiomeBuilder.getDebugStringForPeaksAndValleys(d) + " C: " + overworldBiomeBuilder.getDebugStringForContinentalness(f) + " E: " + overworldBiomeBuilder.getDebugStringForErosion(f2) + " T: " + overworldBiomeBuilder.getDebugStringForTemperature(f3) + " H: " + overworldBiomeBuilder.getDebugStringForHumidity(f4));
    }

    record PresetInstance(Preset preset, Registry<Biome> biomes) {
        public static final MapCodec<PresetInstance> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)ResourceLocation.CODEC.flatXmap(resourceLocation -> Optional.ofNullable(Preset.BY_NAME.get(resourceLocation)).map(DataResult::success).orElseGet(() -> DataResult.error((String)("Unknown preset: " + resourceLocation))), preset -> DataResult.success((Object)preset.name)).fieldOf("preset").stable().forGetter(PresetInstance::preset), (App)RegistryOps.retrieveRegistry(Registry.BIOME_REGISTRY).forGetter(PresetInstance::biomes)).apply((Applicative)instance, instance.stable(PresetInstance::new)));

        public MultiNoiseBiomeSource biomeSource() {
            return this.preset.biomeSource(this, true);
        }
    }

    public static class Preset {
        static final Map<ResourceLocation, Preset> BY_NAME = Maps.newHashMap();
        public static final Preset NETHER = new Preset(new ResourceLocation("nether"), registry -> new Climate.ParameterList(ImmutableList.of((Object)Pair.of((Object)((Object)Climate.parameters(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f)), registry.getOrCreateHolder(Biomes.NETHER_WASTES)), (Object)Pair.of((Object)((Object)Climate.parameters(0.0f, -0.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f)), registry.getOrCreateHolder(Biomes.SOUL_SAND_VALLEY)), (Object)Pair.of((Object)((Object)Climate.parameters(0.4f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f)), registry.getOrCreateHolder(Biomes.CRIMSON_FOREST)), (Object)Pair.of((Object)((Object)Climate.parameters(0.0f, 0.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.375f)), registry.getOrCreateHolder(Biomes.WARPED_FOREST)), (Object)Pair.of((Object)((Object)Climate.parameters(-0.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.175f)), registry.getOrCreateHolder(Biomes.BASALT_DELTAS)))));
        public static final Preset OVERWORLD = new Preset(new ResourceLocation("overworld"), registry -> {
            ImmutableList.Builder builder = ImmutableList.builder();
            new OverworldBiomeBuilder().addBiomes(pair -> builder.add((Object)pair.mapSecond(registry::getOrCreateHolder)));
            return new Climate.ParameterList(builder.build());
        });
        final ResourceLocation name;
        private final Function<Registry<Biome>, Climate.ParameterList<Holder<Biome>>> parameterSource;

        public Preset(ResourceLocation resourceLocation, Function<Registry<Biome>, Climate.ParameterList<Holder<Biome>>> function) {
            this.name = resourceLocation;
            this.parameterSource = function;
            BY_NAME.put(resourceLocation, this);
        }

        MultiNoiseBiomeSource biomeSource(PresetInstance presetInstance, boolean bl) {
            Climate.ParameterList<Holder<Biome>> parameterList = this.parameterSource.apply(presetInstance.biomes());
            return new MultiNoiseBiomeSource(parameterList, bl ? Optional.of(presetInstance) : Optional.empty());
        }

        public MultiNoiseBiomeSource biomeSource(Registry<Biome> registry, boolean bl) {
            return this.biomeSource(new PresetInstance(this, registry), bl);
        }

        public MultiNoiseBiomeSource biomeSource(Registry<Biome> registry) {
            return this.biomeSource(registry, true);
        }
    }
}

