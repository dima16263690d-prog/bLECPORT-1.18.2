/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.HashMultimap
 *  com.google.common.collect.Maps
 *  com.google.common.collect.Multimap
 *  com.google.common.collect.Sets
 *  com.google.gson.Gson
 *  com.google.gson.JsonObject
 *  com.mojang.logging.LogUtils
 *  org.apache.commons.io.IOUtils
 *  org.slf4j.Logger
 */
package net.minecraft.tags;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.Tag;
import net.minecraft.util.GsonHelper;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

public class TagLoader<T> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new Gson();
    private static final String PATH_SUFFIX = ".json";
    private static final int PATH_SUFFIX_LENGTH = ".json".length();
    private final Function<ResourceLocation, Optional<T>> idToValue;
    private final String directory;

    public TagLoader(Function<ResourceLocation, Optional<T>> function, String string) {
        this.idToValue = function;
        this.directory = string;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Map<ResourceLocation, Tag.Builder> load(ResourceManager resourceManager) {
        HashMap hashMap = Maps.newHashMap();
        for (ResourceLocation resourceLocation2 : resourceManager.listResources(this.directory, string -> string.endsWith(PATH_SUFFIX))) {
            String string2 = resourceLocation2.getPath();
            ResourceLocation resourceLocation3 = new ResourceLocation(resourceLocation2.getNamespace(), string2.substring(this.directory.length() + 1, string2.length() - PATH_SUFFIX_LENGTH));
            try {
                for (Resource resource : resourceManager.getResources(resourceLocation2)) {
                    try {
                        InputStream inputStream = resource.getInputStream();
                        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));){
                            JsonObject jsonObject = GsonHelper.fromJson(GSON, (Reader)bufferedReader, JsonObject.class);
                            if (jsonObject == null) {
                                LOGGER.error("Couldn't load tag list {} from {} in data pack {} as it is empty or null", new Object[]{resourceLocation3, resourceLocation2, resource.getSourceName()});
                                continue;
                            }
                            hashMap.computeIfAbsent(resourceLocation3, resourceLocation -> Tag.Builder.tag()).addFromJson(jsonObject, resource.getSourceName());
                        }
                        finally {
                            if (inputStream == null) continue;
                            inputStream.close();
                        }
                    }
                    catch (IOException | RuntimeException exception) {
                        LOGGER.error("Couldn't read tag list {} from {} in data pack {}", new Object[]{resourceLocation3, resourceLocation2, resource.getSourceName(), exception});
                    }
                    finally {
                        IOUtils.closeQuietly((Closeable)resource);
                    }
                }
            }
            catch (IOException iOException) {
                LOGGER.error("Couldn't read tag list {} from {}", new Object[]{resourceLocation3, resourceLocation2, iOException});
            }
        }
        return hashMap;
    }

    private static void visitDependenciesAndElement(Map<ResourceLocation, Tag.Builder> map, Multimap<ResourceLocation, ResourceLocation> multimap, Set<ResourceLocation> set, ResourceLocation resourceLocation2, BiConsumer<ResourceLocation, Tag.Builder> biConsumer) {
        if (!set.add(resourceLocation2)) {
            return;
        }
        multimap.get((Object)resourceLocation2).forEach(resourceLocation -> TagLoader.visitDependenciesAndElement(map, multimap, set, resourceLocation, biConsumer));
        Tag.Builder builder = map.get(resourceLocation2);
        if (builder != null) {
            biConsumer.accept(resourceLocation2, builder);
        }
    }

    private static boolean isCyclic(Multimap<ResourceLocation, ResourceLocation> multimap, ResourceLocation resourceLocation, ResourceLocation resourceLocation3) {
        Collection collection = multimap.get((Object)resourceLocation3);
        if (collection.contains(resourceLocation)) {
            return true;
        }
        return collection.stream().anyMatch(resourceLocation2 -> TagLoader.isCyclic(multimap, resourceLocation, resourceLocation2));
    }

    private static void addDependencyIfNotCyclic(Multimap<ResourceLocation, ResourceLocation> multimap, ResourceLocation resourceLocation, ResourceLocation resourceLocation2) {
        if (!TagLoader.isCyclic(multimap, resourceLocation, resourceLocation2)) {
            multimap.put((Object)resourceLocation, (Object)resourceLocation2);
        }
    }

    public Map<ResourceLocation, Tag<T>> build(Map<ResourceLocation, Tag.Builder> map) {
        HashMap hashMap = Maps.newHashMap();
        Function<ResourceLocation, Tag> function = hashMap::get;
        Function<ResourceLocation, Object> function2 = resourceLocation -> this.idToValue.apply((ResourceLocation)resourceLocation).orElse(null);
        HashMultimap hashMultimap = HashMultimap.create();
        map.forEach((arg_0, arg_1) -> TagLoader.lambda$build$6((Multimap)hashMultimap, arg_0, arg_1));
        map.forEach((arg_0, arg_1) -> TagLoader.lambda$build$8((Multimap)hashMultimap, arg_0, arg_1));
        HashSet hashSet = Sets.newHashSet();
        map.keySet().forEach(arg_0 -> TagLoader.lambda$build$12(map, (Multimap)hashMultimap, hashSet, function, function2, hashMap, arg_0));
        return hashMap;
    }

    public Map<ResourceLocation, Tag<T>> loadAndBuild(ResourceManager resourceManager) {
        return this.build(this.load(resourceManager));
    }

    private static /* synthetic */ void lambda$build$12(Map map, Multimap multimap, Set set, Function function, Function function2, Map map2, ResourceLocation resourceLocation2) {
        TagLoader.visitDependenciesAndElement(map, (Multimap<ResourceLocation, ResourceLocation>)multimap, set, resourceLocation2, (resourceLocation, builder) -> builder.build(function, function2).ifLeft(collection -> LOGGER.error("Couldn't load tag {} as it is missing following references: {}", resourceLocation, (Object)collection.stream().map(Objects::toString).collect(Collectors.joining(", ")))).ifRight(tag -> map2.put(resourceLocation, tag)));
    }

    private static /* synthetic */ void lambda$build$8(Multimap multimap, ResourceLocation resourceLocation, Tag.Builder builder) {
        builder.visitOptionalDependencies(resourceLocation2 -> TagLoader.addDependencyIfNotCyclic((Multimap<ResourceLocation, ResourceLocation>)multimap, resourceLocation, resourceLocation2));
    }

    private static /* synthetic */ void lambda$build$6(Multimap multimap, ResourceLocation resourceLocation, Tag.Builder builder) {
        builder.visitRequiredDependencies(resourceLocation2 -> TagLoader.addDependencyIfNotCyclic((Multimap<ResourceLocation, ResourceLocation>)multimap, resourceLocation, resourceLocation2));
    }
}

