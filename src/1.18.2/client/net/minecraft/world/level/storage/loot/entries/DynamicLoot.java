/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonDeserializationContext
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonSerializationContext
 */
package net.minecraft.world.level.storage.loot.entries;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class DynamicLoot
extends LootPoolSingletonContainer {
    final ResourceLocation name;

    DynamicLoot(ResourceLocation resourceLocation, int n, int n2, LootItemCondition[] lootItemConditionArray, LootItemFunction[] lootItemFunctionArray) {
        super(n, n2, lootItemConditionArray, lootItemFunctionArray);
        this.name = resourceLocation;
    }

    @Override
    public LootPoolEntryType getType() {
        return LootPoolEntries.DYNAMIC;
    }

    @Override
    public void createItemStack(Consumer<ItemStack> consumer, LootContext lootContext) {
        lootContext.addDynamicDrops(this.name, consumer);
    }

    public static LootPoolSingletonContainer.Builder<?> dynamicEntry(ResourceLocation resourceLocation) {
        return DynamicLoot.simpleBuilder((n, n2, lootItemConditionArray, lootItemFunctionArray) -> new DynamicLoot(resourceLocation, n, n2, lootItemConditionArray, lootItemFunctionArray));
    }

    public static class Serializer
    extends LootPoolSingletonContainer.Serializer<DynamicLoot> {
        @Override
        public void serializeCustom(JsonObject jsonObject, DynamicLoot dynamicLoot, JsonSerializationContext jsonSerializationContext) {
            super.serializeCustom(jsonObject, dynamicLoot, jsonSerializationContext);
            jsonObject.addProperty("name", dynamicLoot.name.toString());
        }

        @Override
        protected DynamicLoot deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, int n, int n2, LootItemCondition[] lootItemConditionArray, LootItemFunction[] lootItemFunctionArray) {
            ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "name"));
            return new DynamicLoot(resourceLocation, n, n2, lootItemConditionArray, lootItemFunctionArray);
        }

        @Override
        protected /* synthetic */ LootPoolSingletonContainer deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, int n, int n2, LootItemCondition[] lootItemConditionArray, LootItemFunction[] lootItemFunctionArray) {
            return this.deserialize(jsonObject, jsonDeserializationContext, n, n2, lootItemConditionArray, lootItemFunctionArray);
        }
    }
}

