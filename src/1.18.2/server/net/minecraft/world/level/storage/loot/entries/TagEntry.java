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
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntry;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class TagEntry
extends LootPoolSingletonContainer {
    final TagKey<Item> tag;
    final boolean expand;

    TagEntry(TagKey<Item> tagKey, boolean bl, int n, int n2, LootItemCondition[] lootItemConditionArray, LootItemFunction[] lootItemFunctionArray) {
        super(n, n2, lootItemConditionArray, lootItemFunctionArray);
        this.tag = tagKey;
        this.expand = bl;
    }

    @Override
    public LootPoolEntryType getType() {
        return LootPoolEntries.TAG;
    }

    @Override
    public void createItemStack(Consumer<ItemStack> consumer, LootContext lootContext) {
        Registry.ITEM.getTagOrEmpty(this.tag).forEach(holder -> consumer.accept(new ItemStack((Holder<Item>)holder)));
    }

    private boolean expandTag(LootContext lootContext, Consumer<LootPoolEntry> consumer) {
        if (this.canRun(lootContext)) {
            for (final Holder<Item> holder : Registry.ITEM.getTagOrEmpty(this.tag)) {
                consumer.accept(new LootPoolSingletonContainer.EntryBase(){

                    @Override
                    public void createItemStack(Consumer<ItemStack> consumer, LootContext lootContext) {
                        consumer.accept(new ItemStack(holder));
                    }
                });
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean expand(LootContext lootContext, Consumer<LootPoolEntry> consumer) {
        if (this.expand) {
            return this.expandTag(lootContext, consumer);
        }
        return super.expand(lootContext, consumer);
    }

    public static LootPoolSingletonContainer.Builder<?> tagContents(TagKey<Item> tagKey) {
        return TagEntry.simpleBuilder((n, n2, lootItemConditionArray, lootItemFunctionArray) -> new TagEntry(tagKey, false, n, n2, lootItemConditionArray, lootItemFunctionArray));
    }

    public static LootPoolSingletonContainer.Builder<?> expandTag(TagKey<Item> tagKey) {
        return TagEntry.simpleBuilder((n, n2, lootItemConditionArray, lootItemFunctionArray) -> new TagEntry(tagKey, true, n, n2, lootItemConditionArray, lootItemFunctionArray));
    }

    public static class Serializer
    extends LootPoolSingletonContainer.Serializer<TagEntry> {
        @Override
        public void serializeCustom(JsonObject jsonObject, TagEntry tagEntry, JsonSerializationContext jsonSerializationContext) {
            super.serializeCustom(jsonObject, tagEntry, jsonSerializationContext);
            jsonObject.addProperty("name", tagEntry.tag.location().toString());
            jsonObject.addProperty("expand", Boolean.valueOf(tagEntry.expand));
        }

        @Override
        protected TagEntry deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, int n, int n2, LootItemCondition[] lootItemConditionArray, LootItemFunction[] lootItemFunctionArray) {
            ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "name"));
            TagKey<Item> tagKey = TagKey.create(Registry.ITEM_REGISTRY, resourceLocation);
            boolean bl = GsonHelper.getAsBoolean(jsonObject, "expand");
            return new TagEntry(tagKey, bl, n, n2, lootItemConditionArray, lootItemFunctionArray);
        }

        @Override
        protected /* synthetic */ LootPoolSingletonContainer deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, int n, int n2, LootItemCondition[] lootItemConditionArray, LootItemFunction[] lootItemFunctionArray) {
            return this.deserialize(jsonObject, jsonDeserializationContext, n, n2, lootItemConditionArray, lootItemFunctionArray);
        }
    }
}

