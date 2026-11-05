/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.google.common.collect.ImmutableMap
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.Decoder
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.Encoder
 *  com.mojang.serialization.Lifecycle
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.core;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import net.minecraft.core.HolderSet;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.RegistryLoader;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class RegistryCodecs {
    private static <T> MapCodec<RegistryEntry<T>> withNameAndId(ResourceKey<? extends Registry<T>> resourceKey, MapCodec<T> mapCodec) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group((App)ResourceLocation.CODEC.xmap(ResourceKey.elementKey(resourceKey), ResourceKey::location).fieldOf("name").forGetter(RegistryEntry::key), (App)Codec.INT.fieldOf("id").forGetter(RegistryEntry::id), (App)mapCodec.forGetter(RegistryEntry::value)).apply((Applicative)instance, RegistryEntry::new));
    }

    public static <T> Codec<Registry<T>> networkCodec(ResourceKey<? extends Registry<T>> resourceKey, Lifecycle lifecycle, Codec<T> codec) {
        return RegistryCodecs.withNameAndId(resourceKey, codec.fieldOf("element")).codec().listOf().xmap(list -> {
            MappedRegistry mappedRegistry = new MappedRegistry(resourceKey, lifecycle, null);
            for (RegistryEntry registryEntry : list) {
                ((WritableRegistry)mappedRegistry).registerMapping(registryEntry.id(), registryEntry.key(), registryEntry.value(), lifecycle);
            }
            return mappedRegistry;
        }, registry -> {
            ImmutableList.Builder builder = ImmutableList.builder();
            for (Object e : registry) {
                builder.add(new RegistryEntry(registry.getResourceKey(e).get(), registry.getId(e), e));
            }
            return builder.build();
        });
    }

    public static <E> Codec<Registry<E>> dataPackAwareCodec(ResourceKey<? extends Registry<E>> resourceKey, Lifecycle lifecycle, Codec<E> codec) {
        Codec<Map<ResourceKey<E>, E>> codec2 = RegistryCodecs.directCodec(resourceKey, codec);
        Encoder encoder = codec2.comap(registry -> ImmutableMap.copyOf(registry.entrySet()));
        return Codec.of((Encoder)encoder, RegistryCodecs.dataPackAwareDecoder(resourceKey, codec, codec2, lifecycle), (String)("DataPackRegistryCodec for " + resourceKey));
    }

    private static <E> Decoder<Registry<E>> dataPackAwareDecoder(final ResourceKey<? extends Registry<E>> resourceKey, final Codec<E> codec, Decoder<Map<ResourceKey<E>, E>> decoder, Lifecycle lifecycle) {
        final Decoder decoder2 = decoder.map(map -> {
            MappedRegistry mappedRegistry = new MappedRegistry(resourceKey, lifecycle, null);
            map.forEach((resourceKey, object) -> mappedRegistry.register(resourceKey, object, lifecycle));
            return mappedRegistry;
        });
        return new Decoder<Registry<E>>(){

            public <T> DataResult<Pair<Registry<E>, T>> decode(DynamicOps<T> dynamicOps, T t) {
                DataResult dataResult = decoder2.decode(dynamicOps, t);
                if (dynamicOps instanceof RegistryOps) {
                    RegistryOps registryOps = (RegistryOps)dynamicOps;
                    return registryOps.registryLoader().map(bound -> this.overrideFromResources(dataResult, registryOps, bound.loader())).orElseGet(() -> DataResult.error((String)"Can't load registry with this ops"));
                }
                return dataResult.map(pair -> pair.mapFirst(writableRegistry -> writableRegistry));
            }

            private <T> DataResult<Pair<Registry<E>, T>> overrideFromResources(DataResult<Pair<WritableRegistry<E>, T>> dataResult, RegistryOps<?> registryOps, RegistryLoader registryLoader) {
                return dataResult.flatMap(pair -> registryLoader.overrideRegistryFromResources((WritableRegistry)pair.getFirst(), resourceKey, codec, registryOps.getAsJson()).map(registry -> Pair.of((Object)registry, (Object)pair.getSecond())));
            }
        };
    }

    private static <T> Codec<Map<ResourceKey<T>, T>> directCodec(ResourceKey<? extends Registry<T>> resourceKey, Codec<T> codec) {
        return Codec.unboundedMap((Codec)ResourceLocation.CODEC.xmap(ResourceKey.elementKey(resourceKey), ResourceKey::location), codec);
    }

    public static <E> Codec<HolderSet<E>> homogeneousList(ResourceKey<? extends Registry<E>> resourceKey, Codec<E> codec) {
        return RegistryCodecs.homogeneousList(resourceKey, codec, false);
    }

    public static <E> Codec<HolderSet<E>> homogeneousList(ResourceKey<? extends Registry<E>> resourceKey, Codec<E> codec, boolean bl) {
        return HolderSetCodec.create(resourceKey, RegistryFileCodec.create(resourceKey, codec), bl);
    }

    public static <E> Codec<HolderSet<E>> homogeneousList(ResourceKey<? extends Registry<E>> resourceKey) {
        return RegistryCodecs.homogeneousList(resourceKey, false);
    }

    public static <E> Codec<HolderSet<E>> homogeneousList(ResourceKey<? extends Registry<E>> resourceKey, boolean bl) {
        return HolderSetCodec.create(resourceKey, RegistryFixedCodec.create(resourceKey), bl);
    }

    record RegistryEntry<T>(ResourceKey<T> key, int id, T value) {
    }
}

