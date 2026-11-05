/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.GsonBuilder
 */
package net.minecraft.world.level.storage.loot;

import com.google.gson.GsonBuilder;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import net.minecraft.world.level.storage.loot.providers.nbt.NbtProvider;
import net.minecraft.world.level.storage.loot.providers.nbt.NbtProviders;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;
import net.minecraft.world.level.storage.loot.providers.score.ScoreboardNameProvider;
import net.minecraft.world.level.storage.loot.providers.score.ScoreboardNameProviders;

public class Deserializers {
    public static GsonBuilder createConditionSerializer() {
        return new GsonBuilder().registerTypeAdapter(IntRange.class, (Object)new IntRange.Serializer()).registerTypeHierarchyAdapter(NumberProvider.class, NumberProviders.createGsonAdapter()).registerTypeHierarchyAdapter(LootItemCondition.class, LootItemConditions.createGsonAdapter()).registerTypeHierarchyAdapter(ScoreboardNameProvider.class, ScoreboardNameProviders.createGsonAdapter()).registerTypeHierarchyAdapter(LootContext.EntityTarget.class, (Object)new LootContext.EntityTarget.Serializer());
    }

    public static GsonBuilder createFunctionSerializer() {
        return Deserializers.createConditionSerializer().registerTypeHierarchyAdapter(LootPoolEntryContainer.class, LootPoolEntries.createGsonAdapter()).registerTypeHierarchyAdapter(LootItemFunction.class, LootItemFunctions.createGsonAdapter()).registerTypeHierarchyAdapter(NbtProvider.class, NbtProviders.createGsonAdapter());
    }

    public static GsonBuilder createLootTableSerializer() {
        return Deserializers.createFunctionSerializer().registerTypeAdapter(LootPool.class, (Object)new LootPool.Serializer()).registerTypeAdapter(LootTable.class, (Object)new LootTable.Serializer());
    }
}

