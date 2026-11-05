/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementList;
import net.minecraft.advancements.TreeNodePosition;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.storage.loot.PredicateManager;
import org.slf4j.Logger;

public class ServerAdvancementManager
extends SimpleJsonResourceReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().create();
    private AdvancementList advancements = new AdvancementList();
    private final PredicateManager predicateManager;

    public ServerAdvancementManager(PredicateManager predicateManager) {
        super(GSON, "advancements");
        this.predicateManager = predicateManager;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        HashMap hashMap = Maps.newHashMap();
        map.forEach((resourceLocation, jsonElement) -> {
            try {
                JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "advancement");
                Advancement.Builder builder = Advancement.Builder.fromJson(jsonObject, new DeserializationContext((ResourceLocation)resourceLocation, this.predicateManager));
                hashMap.put(resourceLocation, builder);
            }
            catch (Exception exception) {
                LOGGER.error("Parsing error loading custom advancement {}: {}", resourceLocation, (Object)exception.getMessage());
            }
        });
        AdvancementList advancementList = new AdvancementList();
        advancementList.add(hashMap);
        for (Advancement advancement : advancementList.getRoots()) {
            if (advancement.getDisplay() == null) continue;
            TreeNodePosition.run(advancement);
        }
        this.advancements = advancementList;
    }

    @Nullable
    public Advancement getAdvancement(ResourceLocation resourceLocation) {
        return this.advancements.get(resourceLocation);
    }

    public Collection<Advancement> getAllAdvancements() {
        return this.advancements.getAllAdvancements();
    }
}

