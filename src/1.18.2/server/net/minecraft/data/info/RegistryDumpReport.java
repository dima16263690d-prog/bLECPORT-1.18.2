/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 */
package net.minecraft.data.info;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.file.Path;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;

public class RegistryDumpReport
implements DataProvider {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final DataGenerator generator;

    public RegistryDumpReport(DataGenerator dataGenerator) {
        this.generator = dataGenerator;
    }

    @Override
    public void run(HashCache hashCache) throws IOException {
        JsonObject jsonObject = new JsonObject();
        Registry.REGISTRY.holders().forEach(reference -> jsonObject.add(reference.key().location().toString(), RegistryDumpReport.dumpRegistry((Registry)reference.value())));
        Path path = this.generator.getOutputFolder().resolve("reports/registries.json");
        DataProvider.save(GSON, hashCache, (JsonElement)jsonObject, path);
    }

    private static <T> JsonElement dumpRegistry(Registry<T> registry) {
        JsonObject jsonObject = new JsonObject();
        if (registry instanceof DefaultedRegistry) {
            ResourceLocation resourceLocation = ((DefaultedRegistry)registry).getDefaultKey();
            jsonObject.addProperty("default", resourceLocation.toString());
        }
        int n = Registry.REGISTRY.getId(registry);
        jsonObject.addProperty("protocol_id", (Number)n);
        JsonObject jsonObject2 = new JsonObject();
        registry.holders().forEach(reference -> {
            Object t = reference.value();
            int n = registry.getId(t);
            JsonObject jsonObject2 = new JsonObject();
            jsonObject2.addProperty("protocol_id", (Number)n);
            jsonObject2.add(reference.key().location().toString(), (JsonElement)jsonObject2);
        });
        jsonObject.add("entries", (JsonElement)jsonObject2);
        return jsonObject;
    }

    @Override
    public String getName() {
        return "Registry Dump";
    }
}

