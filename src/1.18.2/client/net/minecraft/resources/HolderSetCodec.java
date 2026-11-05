/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Either
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DynamicOps
 */
package net.minecraft.resources;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.lang.invoke.LambdaMetafactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;

public class HolderSetCodec<E>
implements Codec<HolderSet<E>> {
    private final ResourceKey<? extends Registry<E>> registryKey;
    private final Codec<Holder<E>> elementCodec;
    private final Codec<List<Holder<E>>> homogenousListCodec;
    private final Codec<Either<TagKey<E>, List<Holder<E>>>> registryAwareCodec;

    private static <E> Codec<List<Holder<E>>> homogenousList(Codec<Holder<E>> codec, boolean bl) {
        Function function = ExtraCodecs.ensureHomogenous(Holder::kind);
        Codec codec2 = codec.listOf().flatXmap(function, function);
        if (bl) {
            return codec2;
        }
        return Codec.either((Codec)codec2, codec).xmap(either -> (List)either.map(list -> list, (Function<Holder, List>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)Ljava/lang/Object;, of(java.lang.Object ), (Lnet/minecraft/core/Holder;)Ljava/util/List;)()), list -> list.size() == 1 ? Either.right((Object)((Holder)list.get(0))) : Either.left((Object)list));
    }

    public static <E> Codec<HolderSet<E>> create(ResourceKey<? extends Registry<E>> resourceKey, Codec<Holder<E>> codec, boolean bl) {
        return new HolderSetCodec<E>(resourceKey, codec, bl);
    }

    private HolderSetCodec(ResourceKey<? extends Registry<E>> resourceKey, Codec<Holder<E>> codec, boolean bl) {
        this.registryKey = resourceKey;
        this.elementCodec = codec;
        this.homogenousListCodec = HolderSetCodec.homogenousList(codec, bl);
        this.registryAwareCodec = Codec.either(TagKey.hashedCodec(resourceKey), this.homogenousListCodec);
    }

    public <T> DataResult<Pair<HolderSet<E>, T>> decode(DynamicOps<T> dynamicOps, T t) {
        RegistryOps registryOps;
        Optional optional;
        if (dynamicOps instanceof RegistryOps && (optional = (registryOps = (RegistryOps)dynamicOps).registry(this.registryKey)).isPresent()) {
            Registry registry = optional.get();
            return this.registryAwareCodec.decode(dynamicOps, t).map(pair -> pair.mapFirst(either -> (HolderSet)either.map(registry::getOrCreateTag, HolderSet::direct)));
        }
        return this.decodeWithoutRegistry(dynamicOps, t);
    }

    public <T> DataResult<T> encode(HolderSet<E> holderSet, DynamicOps<T> dynamicOps, T t) {
        RegistryOps registryOps;
        Optional optional;
        if (dynamicOps instanceof RegistryOps && (optional = (registryOps = (RegistryOps)dynamicOps).registry(this.registryKey)).isPresent()) {
            if (!holderSet.isValidInRegistry(optional.get())) {
                return DataResult.error((String)("HolderSet " + holderSet + " is not valid in current registry set"));
            }
            return this.registryAwareCodec.encode((Object)holderSet.unwrap().mapRight((Function<List, List>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)Ljava/lang/Object;, copyOf(java.util.Collection ), (Ljava/util/List;)Ljava/util/List;)()), dynamicOps, t);
        }
        return this.encodeWithoutRegistry(holderSet, dynamicOps, t);
    }

    private <T> DataResult<Pair<HolderSet<E>, T>> decodeWithoutRegistry(DynamicOps<T> dynamicOps, T t) {
        return this.elementCodec.listOf().decode(dynamicOps, t).flatMap(pair -> {
            ArrayList<Holder.Direct> arrayList = new ArrayList<Holder.Direct>();
            for (Holder holder : (List)pair.getFirst()) {
                if (holder instanceof Holder.Direct) {
                    Holder.Direct direct = (Holder.Direct)holder;
                    arrayList.add(direct);
                    continue;
                }
                return DataResult.error((String)("Can't decode element " + holder + " without registry"));
            }
            return DataResult.success((Object)new Pair(HolderSet.direct(arrayList), pair.getSecond()));
        });
    }

    private <T> DataResult<T> encodeWithoutRegistry(HolderSet<E> holderSet, DynamicOps<T> dynamicOps, T t) {
        return this.homogenousListCodec.encode((Object)holderSet.stream().toList(), dynamicOps, t);
    }

    public /* synthetic */ DataResult encode(Object object, DynamicOps dynamicOps, Object object2) {
        return this.encode((HolderSet)object, dynamicOps, object2);
    }
}

