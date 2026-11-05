/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Suppliers
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.Keyable
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.biome;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Keyable;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.LambdaMetafactory;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.slf4j.Logger;

public class BiomeGenerationSettings {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final BiomeGenerationSettings EMPTY = new BiomeGenerationSettings((Map<GenerationStep.Carving, HolderSet<ConfiguredWorldCarver<?>>>)ImmutableMap.of(), (List<HolderSet<PlacedFeature>>)ImmutableList.of());
    public static final MapCodec<BiomeGenerationSettings> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.simpleMap(GenerationStep.Carving.CODEC, (Codec)ConfiguredWorldCarver.LIST_CODEC.promotePartial(Util.prefix("Carver: ", arg_0 -> ((Logger)LOGGER).error(arg_0))), (Keyable)StringRepresentable.keys(GenerationStep.Carving.values())).fieldOf("carvers").forGetter(biomeGenerationSettings -> biomeGenerationSettings.carvers), (App)PlacedFeature.LIST_OF_LISTS_CODEC.promotePartial(Util.prefix("Features: ", arg_0 -> ((Logger)LOGGER).error(arg_0))).fieldOf("features").forGetter(biomeGenerationSettings -> biomeGenerationSettings.features)).apply((Applicative)instance, BiomeGenerationSettings::new));
    private final Map<GenerationStep.Carving, HolderSet<ConfiguredWorldCarver<?>>> carvers;
    private final List<HolderSet<PlacedFeature>> features;
    private final Supplier<List<ConfiguredFeature<?, ?>>> flowerFeatures;
    private final Supplier<Set<PlacedFeature>> featureSet;

    BiomeGenerationSettings(Map<GenerationStep.Carving, HolderSet<ConfiguredWorldCarver<?>>> map, List<HolderSet<PlacedFeature>> list) {
        this.carvers = map;
        this.features = list;
        this.flowerFeatures = Suppliers.memoize(() -> (List)list.stream().flatMap(HolderSet::stream).map(Holder::value).flatMap(PlacedFeature::getFeatures).filter(configuredFeature -> configuredFeature.feature() == Feature.FLOWER).collect(ImmutableList.toImmutableList()));
        this.featureSet = Suppliers.memoize(() -> list.stream().flatMap(HolderSet::stream).map(Holder::value).collect(Collectors.toSet()));
    }

    public Iterable<Holder<ConfiguredWorldCarver<?>>> getCarvers(GenerationStep.Carving carving) {
        return (Iterable)Objects.requireNonNullElseGet((Object)this.carvers.get(carving), (Supplier<Iterable>)LambdaMetafactory.metafactory(null, null, null, ()Ljava/lang/Object;, of(), ()Ljava/lang/Iterable;)());
    }

    public List<ConfiguredFeature<?, ?>> getFlowerFeatures() {
        return this.flowerFeatures.get();
    }

    public List<HolderSet<PlacedFeature>> features() {
        return this.features;
    }

    public boolean hasFeature(PlacedFeature placedFeature) {
        return this.featureSet.get().contains((Object)placedFeature);
    }

    public static class Builder {
        private final Map<GenerationStep.Carving, List<Holder<ConfiguredWorldCarver<?>>>> carvers = Maps.newLinkedHashMap();
        private final List<List<Holder<PlacedFeature>>> features = Lists.newArrayList();

        public Builder addFeature(GenerationStep.Decoration decoration, Holder<PlacedFeature> holder) {
            return this.addFeature(decoration.ordinal(), holder);
        }

        public Builder addFeature(int n, Holder<PlacedFeature> holder) {
            this.addFeatureStepsUpTo(n);
            this.features.get(n).add(holder);
            return this;
        }

        public Builder addCarver(GenerationStep.Carving carving2, Holder<? extends ConfiguredWorldCarver<?>> holder) {
            this.carvers.computeIfAbsent(carving2, carving -> Lists.newArrayList()).add(Holder.hackyErase(holder));
            return this;
        }

        private void addFeatureStepsUpTo(int n) {
            while (this.features.size() <= n) {
                this.features.add(Lists.newArrayList());
            }
        }

        public BiomeGenerationSettings build() {
            return new BiomeGenerationSettings((Map)this.carvers.entrySet().stream().collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, entry -> HolderSet.direct((List)entry.getValue()))), (List)this.features.stream().map(HolderSet::direct).collect(ImmutableList.toImmutableList()));
        }
    }
}

