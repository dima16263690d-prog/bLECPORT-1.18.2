/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonIOException
 *  com.google.gson.JsonParser
 *  com.google.gson.JsonSyntaxException
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DataResult$PartialResult
 *  com.mojang.serialization.Decoder
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.Encoder
 *  com.mojang.serialization.JsonOps
 *  com.mojang.serialization.Lifecycle
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  org.slf4j.Logger
 */
package net.minecraft.resources;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;

public interface RegistryResourceAccess {
    public <E> Collection<ResourceKey<E>> listResources(ResourceKey<? extends Registry<E>> var1);

    public <E> Optional<DataResult<ParsedEntry<E>>> parseElement(DynamicOps<JsonElement> var1, ResourceKey<? extends Registry<E>> var2, ResourceKey<E> var3, Decoder<E> var4);

    public static RegistryResourceAccess forResourceManager(final ResourceManager resourceManager) {
        return new RegistryResourceAccess(){
            private static final String JSON = ".json";

            @Override
            public <E> Collection<ResourceKey<E>> listResources(ResourceKey<? extends Registry<E>> resourceKey) {
                String string2 = 1.registryDirPath(resourceKey);
                HashSet hashSet = new HashSet();
                resourceManager.listResources(string2, string -> string.endsWith(JSON)).forEach(resourceLocation -> {
                    String string2 = resourceLocation.getPath();
                    String string3 = string2.substring(string2.length() + 1, string2.length() - JSON.length());
                    hashSet.add(ResourceKey.create(resourceKey, new ResourceLocation(resourceLocation.getNamespace(), string3)));
                });
                return hashSet;
            }

            /*
             * Enabled aggressive exception aggregation
             */
            @Override
            public <E> Optional<DataResult<ParsedEntry<E>>> parseElement(DynamicOps<JsonElement> dynamicOps, ResourceKey<? extends Registry<E>> resourceKey, ResourceKey<E> resourceKey2, Decoder<E> decoder) {
                ResourceLocation resourceLocation = 1.elementPath(resourceKey, resourceKey2);
                if (!resourceManager.hasResource(resourceLocation)) {
                    return Optional.empty();
                }
                try (Resource resource = resourceManager.getResource(resourceLocation);){
                    Optional<DataResult<ParsedEntry<E>>> optional;
                    try (InputStreamReader inputStreamReader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);){
                        JsonElement jsonElement = JsonParser.parseReader((Reader)inputStreamReader);
                        optional = Optional.of(decoder.parse(dynamicOps, (Object)jsonElement).map(ParsedEntry::createWithoutId));
                    }
                    return optional;
                }
                catch (JsonIOException | JsonSyntaxException | IOException throwable) {
                    return Optional.of(DataResult.error((String)("Failed to parse " + resourceLocation + " file: " + throwable.getMessage())));
                }
            }

            private static String registryDirPath(ResourceKey<? extends Registry<?>> resourceKey) {
                return resourceKey.location().getPath();
            }

            private static <E> ResourceLocation elementPath(ResourceKey<? extends Registry<E>> resourceKey, ResourceKey<E> resourceKey2) {
                return new ResourceLocation(resourceKey2.location().getNamespace(), 1.registryDirPath(resourceKey) + "/" + resourceKey2.location().getPath() + JSON);
            }

            public String toString() {
                return "ResourceAccess[" + resourceManager + "]";
            }
        };
    }

    public static final class InMemoryStorage
    implements RegistryResourceAccess {
        private static final Logger LOGGER = LogUtils.getLogger();
        private final Map<ResourceKey<?>, Entry> entries = Maps.newIdentityHashMap();

        public <E> void add(RegistryAccess registryAccess, ResourceKey<E> resourceKey, Encoder<E> encoder, int n, E e, Lifecycle lifecycle) {
            DataResult dataResult = encoder.encodeStart(RegistryOps.create(JsonOps.INSTANCE, registryAccess), e);
            Optional optional = dataResult.error();
            if (optional.isPresent()) {
                LOGGER.error("Error adding element: {}", (Object)((DataResult.PartialResult)optional.get()).message());
            } else {
                this.entries.put(resourceKey, new Entry((JsonElement)dataResult.result().get(), n, lifecycle));
            }
        }

        @Override
        public <E> Collection<ResourceKey<E>> listResources(ResourceKey<? extends Registry<E>> resourceKey) {
            return this.entries.keySet().stream().flatMap(resourceKey2 -> resourceKey2.cast(resourceKey).stream()).collect(Collectors.toList());
        }

        @Override
        public <E> Optional<DataResult<ParsedEntry<E>>> parseElement(DynamicOps<JsonElement> dynamicOps, ResourceKey<? extends Registry<E>> resourceKey, ResourceKey<E> resourceKey2, Decoder<E> decoder) {
            Entry entry = this.entries.get(resourceKey2);
            if (entry == null) {
                return Optional.of(DataResult.error((String)("Unknown element: " + resourceKey2)));
            }
            return Optional.of(decoder.parse(dynamicOps, (Object)entry.data).setLifecycle(entry.lifecycle).map(object -> ParsedEntry.createWithId(object, entry.id)));
        }

        static final class Entry
        extends Record {
            final JsonElement data;
            final int id;
            final Lifecycle lifecycle;

            Entry(JsonElement jsonElement, int n, Lifecycle lifecycle) {
                this.data = jsonElement;
                this.id = n;
                this.lifecycle = lifecycle;
            }

            public final String toString() {
                return ObjectMethods.bootstrap("toString", new MethodHandle[]{Entry.class, "data;id;lifecycle", "data", "id", "lifecycle"}, this);
            }

            public final int hashCode() {
                return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Entry.class, "data;id;lifecycle", "data", "id", "lifecycle"}, this);
            }

            public final boolean equals(Object object) {
                return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Entry.class, "data;id;lifecycle", "data", "id", "lifecycle"}, this, object);
            }

            public JsonElement data() {
                return this.data;
            }

            public int id() {
                return this.id;
            }

            public Lifecycle lifecycle() {
                return this.lifecycle;
            }
        }
    }

    public record ParsedEntry<E>(E value, OptionalInt fixedId) {
        public static <E> ParsedEntry<E> createWithoutId(E e) {
            return new ParsedEntry<E>(e, OptionalInt.empty());
        }

        public static <E> ParsedEntry<E> createWithId(E e, int n) {
            return new ParsedEntry<E>(e, OptionalInt.of(n));
        }
    }
}

