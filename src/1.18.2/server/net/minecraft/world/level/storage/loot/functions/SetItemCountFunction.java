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
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

public class SetItemCountFunction
extends LootItemConditionalFunction {
    final NumberProvider value;
    final boolean add;

    SetItemCountFunction(LootItemCondition[] lootItemConditionArray, NumberProvider numberProvider, boolean bl) {
        super(lootItemConditionArray);
        this.value = numberProvider;
        this.add = bl;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.SET_COUNT;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return this.value.getReferencedContextParams();
    }

    @Override
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        int n = this.add ? itemStack.getCount() : 0;
        itemStack.setCount(Mth.clamp(n + this.value.getInt(lootContext), 0, itemStack.getMaxStackSize()));
        return itemStack;
    }

    public static LootItemConditionalFunction.Builder<?> setCount(NumberProvider numberProvider) {
        return SetItemCountFunction.simpleBuilder(lootItemConditionArray -> new SetItemCountFunction((LootItemCondition[])lootItemConditionArray, numberProvider, false));
    }

    public static LootItemConditionalFunction.Builder<?> setCount(NumberProvider numberProvider, boolean bl) {
        return SetItemCountFunction.simpleBuilder(lootItemConditionArray -> new SetItemCountFunction((LootItemCondition[])lootItemConditionArray, numberProvider, bl));
    }

    public static class Serializer
    extends LootItemConditionalFunction.Serializer<SetItemCountFunction> {
        @Override
        public void serialize(JsonObject jsonObject, SetItemCountFunction setItemCountFunction, JsonSerializationContext jsonSerializationContext) {
            super.serialize(jsonObject, setItemCountFunction, jsonSerializationContext);
            jsonObject.add("count", jsonSerializationContext.serialize((Object)setItemCountFunction.value));
            jsonObject.addProperty("add", Boolean.valueOf(setItemCountFunction.add));
        }

        @Override
        public SetItemCountFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditionArray) {
            NumberProvider numberProvider = GsonHelper.getAsObject(jsonObject, "count", jsonDeserializationContext, NumberProvider.class);
            boolean bl = GsonHelper.getAsBoolean(jsonObject, "add", false);
            return new SetItemCountFunction(lootItemConditionArray, numberProvider, bl);
        }

        @Override
        public /* synthetic */ LootItemConditionalFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditionArray) {
            return this.deserialize(jsonObject, jsonDeserializationContext, lootItemConditionArray);
        }
    }
}

