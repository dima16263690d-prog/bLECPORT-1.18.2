/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Lifecycle
 *  javax.annotation.Nonnull
 *  javax.annotation.Nullable
 */
package net.minecraft.core;

import com.mojang.serialization.Lifecycle;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class DefaultedRegistry<T>
extends MappedRegistry<T> {
    private final ResourceLocation defaultKey;
    private Holder<T> defaultValue;

    public DefaultedRegistry(String string, ResourceKey<? extends Registry<T>> resourceKey, Lifecycle lifecycle, @Nullable Function<T, Holder.Reference<T>> function) {
        super(resourceKey, lifecycle, function);
        this.defaultKey = new ResourceLocation(string);
    }

    @Override
    public Holder<T> registerMapping(int n, ResourceKey<T> resourceKey, T t, Lifecycle lifecycle) {
        Holder<T> holder = super.registerMapping(n, resourceKey, t, lifecycle);
        if (this.defaultKey.equals(resourceKey.location())) {
            this.defaultValue = holder;
        }
        return holder;
    }

    @Override
    public int getId(@Nullable T t) {
        int n = super.getId(t);
        return n == -1 ? super.getId(this.defaultValue.value()) : n;
    }

    @Override
    @Nonnull
    public ResourceLocation getKey(T t) {
        ResourceLocation resourceLocation = super.getKey(t);
        return resourceLocation == null ? this.defaultKey : resourceLocation;
    }

    @Override
    @Nonnull
    public T get(@Nullable ResourceLocation resourceLocation) {
        Object t = super.get(resourceLocation);
        return t == null ? this.defaultValue.value() : t;
    }

    @Override
    public Optional<T> getOptional(@Nullable ResourceLocation resourceLocation) {
        return Optional.ofNullable(super.get(resourceLocation));
    }

    @Override
    @Nonnull
    public T byId(int n) {
        Object t = super.byId(n);
        return t == null ? this.defaultValue.value() : t;
    }

    @Override
    public Optional<Holder<T>> getRandom(Random random) {
        return super.getRandom(random).or(() -> Optional.of(this.defaultValue));
    }

    public ResourceLocation getDefaultKey() {
        return this.defaultKey;
    }
}

