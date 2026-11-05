/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Iterators
 *  com.google.common.collect.Maps
 *  com.google.common.collect.Sets
 *  com.google.common.collect.Sets$SetView
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Lifecycle
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  it.unimi.dsi.fastutil.objects.ObjectList
 *  javax.annotation.Nullable
 *  org.apache.commons.lang3.Validate
 *  org.slf4j.Logger
 */
package net.minecraft.core;

import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;

public class MappedRegistry<T>
extends WritableRegistry<T> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final ObjectList<Holder.Reference<T>> byId = new ObjectArrayList(256);
    private final Object2IntMap<T> toId = (Object2IntMap)Util.make(new Object2IntOpenCustomHashMap(Util.identityStrategy()), object2IntOpenCustomHashMap -> object2IntOpenCustomHashMap.defaultReturnValue(-1));
    private final Map<ResourceLocation, Holder.Reference<T>> byLocation = new HashMap<ResourceLocation, Holder.Reference<T>>();
    private final Map<ResourceKey<T>, Holder.Reference<T>> byKey = new HashMap<ResourceKey<T>, Holder.Reference<T>>();
    private final Map<T, Holder.Reference<T>> byValue = new IdentityHashMap<T, Holder.Reference<T>>();
    private final Map<T, Lifecycle> lifecycles = new IdentityHashMap<T, Lifecycle>();
    private Lifecycle elementsLifecycle;
    private volatile Map<TagKey<T>, HolderSet.Named<T>> tags = new IdentityHashMap<TagKey<T>, HolderSet.Named<T>>();
    private boolean frozen;
    @Nullable
    private final Function<T, Holder.Reference<T>> customHolderProvider;
    @Nullable
    private Map<T, Holder.Reference<T>> intrusiveHolderCache;
    @Nullable
    private List<Holder.Reference<T>> holdersInOrder;
    private int nextId;

    public MappedRegistry(ResourceKey<? extends Registry<T>> resourceKey, Lifecycle lifecycle, @Nullable Function<T, Holder.Reference<T>> function) {
        super(resourceKey, lifecycle);
        this.elementsLifecycle = lifecycle;
        this.customHolderProvider = function;
        if (function != null) {
            this.intrusiveHolderCache = new IdentityHashMap<T, Holder.Reference<T>>();
        }
    }

    private List<Holder.Reference<T>> holdersInOrder() {
        if (this.holdersInOrder == null) {
            this.holdersInOrder = this.byId.stream().filter(Objects::nonNull).toList();
        }
        return this.holdersInOrder;
    }

    private void validateWrite(ResourceKey<T> resourceKey) {
        if (this.frozen) {
            throw new IllegalStateException("Registry is already frozen (trying to add key " + resourceKey + ")");
        }
    }

    @Override
    public Holder<T> registerMapping(int n, ResourceKey<T> resourceKey, T t, Lifecycle lifecycle) {
        return this.registerMapping(n, resourceKey, t, lifecycle, true);
    }

    private Holder<T> registerMapping(int n, ResourceKey<T> resourceKey2, T t, Lifecycle lifecycle, boolean bl) {
        Holder.Reference reference;
        this.validateWrite(resourceKey2);
        Validate.notNull(resourceKey2);
        Validate.notNull(t);
        this.byId.size(Math.max(this.byId.size(), n + 1));
        this.toId.put(t, n);
        this.holdersInOrder = null;
        if (bl && this.byKey.containsKey(resourceKey2)) {
            Util.logAndPauseIfInIde("Adding duplicate key '" + resourceKey2 + "' to registry");
        }
        if (this.byValue.containsKey(t)) {
            Util.logAndPauseIfInIde("Adding duplicate value '" + t + "' to registry");
        }
        this.lifecycles.put(t, lifecycle);
        this.elementsLifecycle = this.elementsLifecycle.add(lifecycle);
        if (this.nextId <= n) {
            this.nextId = n + 1;
        }
        if (this.customHolderProvider != null) {
            reference = this.customHolderProvider.apply(t);
            Holder.Reference reference2 = this.byKey.put(resourceKey2, reference);
            if (reference2 != null && reference2 != reference) {
                throw new IllegalStateException("Invalid holder present for key " + resourceKey2);
            }
        } else {
            reference = this.byKey.computeIfAbsent(resourceKey2, resourceKey -> Holder.Reference.createStandAlone(this, resourceKey));
        }
        this.byLocation.put(resourceKey2.location(), reference);
        this.byValue.put(t, reference);
        reference.bind(resourceKey2, t);
        this.byId.set(n, (Object)reference);
        return reference;
    }

    @Override
    public Holder<T> register(ResourceKey<T> resourceKey, T t, Lifecycle lifecycle) {
        return this.registerMapping(this.nextId, resourceKey, t, lifecycle);
    }

    @Override
    public Holder<T> registerOrOverride(OptionalInt optionalInt, ResourceKey<T> resourceKey, T t, Lifecycle lifecycle) {
        int n;
        Object var6_6;
        this.validateWrite(resourceKey);
        Validate.notNull(resourceKey);
        Validate.notNull(t);
        Holder holder = this.byKey.get(resourceKey);
        Object v0 = var6_6 = holder != null && holder.isBound() ? holder.value() : null;
        if (var6_6 == null) {
            n = optionalInt.orElse(this.nextId);
        } else {
            n = this.toId.getInt(var6_6);
            if (optionalInt.isPresent() && optionalInt.getAsInt() != n) {
                throw new IllegalStateException("ID mismatch");
            }
            this.lifecycles.remove(var6_6);
            this.toId.removeInt(var6_6);
            this.byValue.remove(var6_6);
        }
        return this.registerMapping(n, resourceKey, t, lifecycle, false);
    }

    @Override
    @Nullable
    public ResourceLocation getKey(T t) {
        Holder.Reference<T> reference = this.byValue.get(t);
        return reference != null ? reference.key().location() : null;
    }

    @Override
    public Optional<ResourceKey<T>> getResourceKey(T t) {
        return Optional.ofNullable(this.byValue.get(t)).map(Holder.Reference::key);
    }

    @Override
    public int getId(@Nullable T t) {
        return this.toId.getInt(t);
    }

    @Override
    @Nullable
    public T get(@Nullable ResourceKey<T> resourceKey) {
        return MappedRegistry.getValueFromNullable(this.byKey.get(resourceKey));
    }

    @Override
    @Nullable
    public T byId(int n) {
        if (n < 0 || n >= this.byId.size()) {
            return null;
        }
        return MappedRegistry.getValueFromNullable((Holder.Reference)this.byId.get(n));
    }

    @Override
    public Optional<Holder<T>> getHolder(int n) {
        if (n < 0 || n >= this.byId.size()) {
            return Optional.empty();
        }
        return Optional.ofNullable((Holder)this.byId.get(n));
    }

    @Override
    public Optional<Holder<T>> getHolder(ResourceKey<T> resourceKey) {
        return Optional.ofNullable((Holder)this.byKey.get(resourceKey));
    }

    @Override
    public Holder<T> getOrCreateHolder(ResourceKey<T> resourceKey2) {
        return this.byKey.computeIfAbsent(resourceKey2, resourceKey -> {
            if (this.customHolderProvider != null) {
                throw new IllegalStateException("This registry can't create new holders without value");
            }
            this.validateWrite((ResourceKey<T>)resourceKey);
            return Holder.Reference.createStandAlone(this, resourceKey);
        });
    }

    @Override
    public int size() {
        return this.byKey.size();
    }

    @Override
    public Lifecycle lifecycle(T t) {
        return this.lifecycles.get(t);
    }

    @Override
    public Lifecycle elementsLifecycle() {
        return this.elementsLifecycle;
    }

    @Override
    public Iterator<T> iterator() {
        return Iterators.transform(this.holdersInOrder().iterator(), Holder::value);
    }

    @Override
    @Nullable
    public T get(@Nullable ResourceLocation resourceLocation) {
        Holder.Reference<T> reference = this.byLocation.get(resourceLocation);
        return MappedRegistry.getValueFromNullable(reference);
    }

    @Nullable
    private static <T> T getValueFromNullable(@Nullable Holder.Reference<T> reference) {
        return reference != null ? (T)reference.value() : null;
    }

    @Override
    public Set<ResourceLocation> keySet() {
        return Collections.unmodifiableSet(this.byLocation.keySet());
    }

    @Override
    public Set<Map.Entry<ResourceKey<T>, T>> entrySet() {
        return Collections.unmodifiableSet(Maps.transformValues(this.byKey, Holder::value).entrySet());
    }

    @Override
    public Stream<Holder.Reference<T>> holders() {
        return this.holdersInOrder().stream();
    }

    @Override
    public boolean isKnownTagName(TagKey<T> tagKey) {
        return this.tags.containsKey(tagKey);
    }

    @Override
    public Stream<Pair<TagKey<T>, HolderSet.Named<T>>> getTags() {
        return this.tags.entrySet().stream().map(entry -> Pair.of((Object)((Object)((TagKey)((Object)((Object)entry.getKey())))), (Object)((HolderSet.Named)entry.getValue())));
    }

    @Override
    public HolderSet.Named<T> getOrCreateTag(TagKey<T> tagKey) {
        HolderSet.Named<T> named = this.tags.get(tagKey);
        if (named == null) {
            named = this.createTag(tagKey);
            IdentityHashMap<TagKey<T>, HolderSet.Named<T>> identityHashMap = new IdentityHashMap<TagKey<T>, HolderSet.Named<T>>(this.tags);
            identityHashMap.put(tagKey, named);
            this.tags = identityHashMap;
        }
        return named;
    }

    private HolderSet.Named<T> createTag(TagKey<T> tagKey) {
        return new HolderSet.Named<T>(this, tagKey);
    }

    @Override
    public Stream<TagKey<T>> getTagNames() {
        return this.tags.keySet().stream();
    }

    @Override
    public boolean isEmpty() {
        return this.byKey.isEmpty();
    }

    @Override
    public Optional<Holder<T>> getRandom(Random random) {
        return Util.getRandomSafe(this.holdersInOrder(), random).map(Holder::hackyErase);
    }

    @Override
    public boolean containsKey(ResourceLocation resourceLocation) {
        return this.byLocation.containsKey(resourceLocation);
    }

    @Override
    public boolean containsKey(ResourceKey<T> resourceKey) {
        return this.byKey.containsKey(resourceKey);
    }

    @Override
    public Registry<T> freeze() {
        this.frozen = true;
        List list = this.byKey.entrySet().stream().filter(entry -> !((Holder.Reference)entry.getValue()).isBound()).map(entry -> ((ResourceKey)entry.getKey()).location()).sorted().toList();
        if (!list.isEmpty()) {
            throw new IllegalStateException("Unbound values in registry " + this.key() + ": " + list);
        }
        if (this.intrusiveHolderCache != null) {
            List list2 = this.intrusiveHolderCache.values().stream().filter(reference -> !reference.isBound()).toList();
            if (!list2.isEmpty()) {
                throw new IllegalStateException("Some intrusive holders were not added to registry: " + list2);
            }
            this.intrusiveHolderCache = null;
        }
        return this;
    }

    @Override
    public Holder.Reference<T> createIntrusiveHolder(T t) {
        if (this.customHolderProvider == null) {
            throw new IllegalStateException("This registry can't create intrusive holders");
        }
        if (this.frozen || this.intrusiveHolderCache == null) {
            throw new IllegalStateException("Registry is already frozen");
        }
        return this.intrusiveHolderCache.computeIfAbsent(t, object -> Holder.Reference.createIntrusive(this, object));
    }

    @Override
    public Optional<HolderSet.Named<T>> getTag(TagKey<T> tagKey) {
        return Optional.ofNullable(this.tags.get(tagKey));
    }

    @Override
    public void bindTags(Map<TagKey<T>, List<Holder<T>>> map) {
        IdentityHashMap<Holder.Reference, List> identityHashMap = new IdentityHashMap<Holder.Reference, List>();
        this.byKey.values().forEach(reference -> identityHashMap.put((Holder.Reference)reference, new ArrayList()));
        map.forEach((? super K tagKey, ? super V list) -> {
            for (Holder holder : list) {
                if (!holder.isValidInRegistry(this)) {
                    throw new IllegalStateException("Can't create named set " + tagKey + " containing value " + holder + " from outside registry " + this);
                }
                if (holder instanceof Holder.Reference) {
                    Holder.Reference reference = (Holder.Reference)holder;
                    ((List)identityHashMap.get(reference)).add(tagKey);
                    continue;
                }
                throw new IllegalStateException("Found direct holder " + holder + " value in tag " + tagKey);
            }
        });
        Sets.SetView setView = Sets.difference(this.tags.keySet(), map.keySet());
        if (!setView.isEmpty()) {
            LOGGER.warn("Not all defined tags for registry {} are present in data pack: {}", this.key(), (Object)setView.stream().map(tagKey -> tagKey.location().toString()).sorted().collect(Collectors.joining(", ")));
        }
        IdentityHashMap<TagKey<T>, HolderSet.Named<T>> identityHashMap2 = new IdentityHashMap<TagKey<T>, HolderSet.Named<T>>(this.tags);
        map.forEach((? super K tagKey, ? super V list) -> identityHashMap2.computeIfAbsent((TagKey<T>)((Object)tagKey), this::createTag).bind(list));
        identityHashMap.forEach(Holder.Reference::bindTags);
        this.tags = identityHashMap2;
    }

    @Override
    public void resetTags() {
        this.tags.values().forEach(named -> named.bind(List.of()));
        this.byKey.values().forEach(reference -> reference.bindTags(Set.of()));
    }
}

