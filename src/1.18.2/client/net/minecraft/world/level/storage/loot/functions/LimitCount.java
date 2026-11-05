/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonDeserializationContext
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonSerializationContext
 */
package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LimitCount
extends LootItemConditionalFunction {
    final IntRange limiter;

    LimitCount(LootItemCondition[] lootItemConditionArray, IntRange intRange) {
        super(lootItemConditionArray);
        this.limiter = intRange;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.LIMIT_COUNT;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return this.limiter.getReferencedContextParams();
    }

    @Override
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        int n = this.limiter.clamp(lootContext, itemStack.getCount());
        itemStack.setCount(n);
        return itemStack;
    }

    public static LootItemConditionalFunction.Builder<?> limitCount(IntRange intRange) {
        return LimitCount.simpleBuilder(lootItemConditionArray -> new LimitCount((LootItemCondition[])lootItemConditionArray, intRange));
    }

    public static class Serializer
    extends LootItemConditionalFunction.Serializer<LimitCount> {
        @Override
        public void serialize(JsonObject jsonObject, LimitCount limitCount, JsonSerializationContext jsonSerializationContext) {
            super.serialize(jsonObject, limitCount, jsonSerializationContext);
            jsonObject.add("limit", jsonSerializationContext.serialize((Object)limitCount.limiter));
        }

        @Override
        public LimitCount deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditionArray) {
            IntRange intRange = GsonHelper.getAsObject(jsonObject, "limit", jsonDeserializationContext, IntRange.class);
            return new LimitCount(lootItemConditionArray, intRange);
        }

        @Override
        public /* synthetic */ LootItemConditionalFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditionArray) {
            return this.deserialize(jsonObject, jsonDeserializationContext, lootItemConditionArray);
        }
    }
}

