/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.gson.JsonDeserializationContext
 *  com.google.gson.JsonDeserializer
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParseException
 *  com.google.gson.JsonSerializationContext
 *  com.google.gson.JsonSerializer
 *  org.apache.commons.lang3.ArrayUtils
 *  org.apache.commons.lang3.mutable.MutableInt
 */
package net.minecraft.world.level.storage.loot;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntry;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.mutable.MutableInt;

public class LootPool {
    final LootPoolEntryContainer[] entries;
    final LootItemCondition[] conditions;
    private final Predicate<LootContext> compositeCondition;
    final LootItemFunction[] functions;
    private final BiFunction<ItemStack, LootContext, ItemStack> compositeFunction;
    final NumberProvider rolls;
    final NumberProvider bonusRolls;

    LootPool(LootPoolEntryContainer[] lootPoolEntryContainerArray, LootItemCondition[] lootItemConditionArray, LootItemFunction[] lootItemFunctionArray, NumberProvider numberProvider, NumberProvider numberProvider2) {
        this.entries = lootPoolEntryContainerArray;
        this.conditions = lootItemConditionArray;
        this.compositeCondition = LootItemConditions.andConditions(lootItemConditionArray);
        this.functions = lootItemFunctionArray;
        this.compositeFunction = LootItemFunctions.compose(lootItemFunctionArray);
        this.rolls = numberProvider;
        this.bonusRolls = numberProvider2;
    }

    private void addRandomItem(Consumer<ItemStack> consumer, LootContext lootContext) {
        Random random = lootContext.getRandom();
        ArrayList arrayList = Lists.newArrayList();
        MutableInt mutableInt = new MutableInt();
        for (LootPoolEntryContainer object : this.entries) {
            object.expand(lootContext, lootPoolEntry -> {
                int n = lootPoolEntry.getWeight(lootContext.getLuck());
                if (n > 0) {
                    arrayList.add(lootPoolEntry);
                    mutableInt.add(n);
                }
            });
        }
        int n = arrayList.size();
        if (mutableInt.intValue() == 0 || n == 0) {
            return;
        }
        if (n == 1) {
            ((LootPoolEntry)arrayList.get(0)).createItemStack(consumer, lootContext);
            return;
        }
        int n2 = random.nextInt(mutableInt.intValue());
        for (LootPoolEntry lootPoolEntry2 : arrayList) {
            if ((n2 -= lootPoolEntry2.getWeight(lootContext.getLuck())) >= 0) continue;
            lootPoolEntry2.createItemStack(consumer, lootContext);
            return;
        }
    }

    public void addRandomItems(Consumer<ItemStack> consumer, LootContext lootContext) {
        if (!this.compositeCondition.test(lootContext)) {
            return;
        }
        Consumer<ItemStack> consumer2 = LootItemFunction.decorate(this.compositeFunction, consumer, lootContext);
        int n = this.rolls.getInt(lootContext) + Mth.floor(this.bonusRolls.getFloat(lootContext) * lootContext.getLuck());
        for (int i = 0; i < n; ++i) {
            this.addRandomItem(consumer2, lootContext);
        }
    }

    public void validate(ValidationContext validationContext) {
        int n;
        for (n = 0; n < this.conditions.length; ++n) {
            this.conditions[n].validate(validationContext.forChild(".condition[" + n + "]"));
        }
        for (n = 0; n < this.functions.length; ++n) {
            this.functions[n].validate(validationContext.forChild(".functions[" + n + "]"));
        }
        for (n = 0; n < this.entries.length; ++n) {
            this.entries[n].validate(validationContext.forChild(".entries[" + n + "]"));
        }
        this.rolls.validate(validationContext.forChild(".rolls"));
        this.bonusRolls.validate(validationContext.forChild(".bonusRolls"));
    }

    public static Builder lootPool() {
        return new Builder();
    }

    public static class Builder
    implements FunctionUserBuilder<Builder>,
    ConditionUserBuilder<Builder> {
        private final List<LootPoolEntryContainer> entries = Lists.newArrayList();
        private final List<LootItemCondition> conditions = Lists.newArrayList();
        private final List<LootItemFunction> functions = Lists.newArrayList();
        private NumberProvider rolls = ConstantValue.exactly(1.0f);
        private NumberProvider bonusRolls = ConstantValue.exactly(0.0f);

        public Builder setRolls(NumberProvider numberProvider) {
            this.rolls = numberProvider;
            return this;
        }

        @Override
        public Builder unwrap() {
            return this;
        }

        public Builder setBonusRolls(NumberProvider numberProvider) {
            this.bonusRolls = numberProvider;
            return this;
        }

        public Builder add(LootPoolEntryContainer.Builder<?> builder) {
            this.entries.add(builder.build());
            return this;
        }

        @Override
        public Builder when(LootItemCondition.Builder builder) {
            this.conditions.add(builder.build());
            return this;
        }

        @Override
        public Builder apply(LootItemFunction.Builder builder) {
            this.functions.add(builder.build());
            return this;
        }

        public LootPool build() {
            if (this.rolls == null) {
                throw new IllegalArgumentException("Rolls not set");
            }
            return new LootPool(this.entries.toArray(new LootPoolEntryContainer[0]), this.conditions.toArray(new LootItemCondition[0]), this.functions.toArray(new LootItemFunction[0]), this.rolls, this.bonusRolls);
        }

        @Override
        public /* synthetic */ Object unwrap() {
            return this.unwrap();
        }

        @Override
        public /* synthetic */ Object apply(LootItemFunction.Builder builder) {
            return this.apply(builder);
        }

        @Override
        public /* synthetic */ Object when(LootItemCondition.Builder builder) {
            return this.when(builder);
        }
    }

    public static class Serializer
    implements JsonDeserializer<LootPool>,
    JsonSerializer<LootPool> {
        public LootPool deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "loot pool");
            LootPoolEntryContainer[] lootPoolEntryContainerArray = GsonHelper.getAsObject(jsonObject, "entries", jsonDeserializationContext, LootPoolEntryContainer[].class);
            LootItemCondition[] lootItemConditionArray = GsonHelper.getAsObject(jsonObject, "conditions", new LootItemCondition[0], jsonDeserializationContext, LootItemCondition[].class);
            LootItemFunction[] lootItemFunctionArray = GsonHelper.getAsObject(jsonObject, "functions", new LootItemFunction[0], jsonDeserializationContext, LootItemFunction[].class);
            NumberProvider numberProvider = GsonHelper.getAsObject(jsonObject, "rolls", jsonDeserializationContext, NumberProvider.class);
            NumberProvider numberProvider2 = GsonHelper.getAsObject(jsonObject, "bonus_rolls", ConstantValue.exactly(0.0f), jsonDeserializationContext, NumberProvider.class);
            return new LootPool(lootPoolEntryContainerArray, lootItemConditionArray, lootItemFunctionArray, numberProvider, numberProvider2);
        }

        public JsonElement serialize(LootPool lootPool, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("rolls", jsonSerializationContext.serialize((Object)lootPool.rolls));
            jsonObject.add("bonus_rolls", jsonSerializationContext.serialize((Object)lootPool.bonusRolls));
            jsonObject.add("entries", jsonSerializationContext.serialize((Object)lootPool.entries));
            if (!ArrayUtils.isEmpty((Object[])lootPool.conditions)) {
                jsonObject.add("conditions", jsonSerializationContext.serialize((Object)lootPool.conditions));
            }
            if (!ArrayUtils.isEmpty((Object[])lootPool.functions)) {
                jsonObject.add("functions", jsonSerializationContext.serialize((Object)lootPool.functions));
            }
            return jsonObject;
        }

        public /* synthetic */ JsonElement serialize(Object object, Type type, JsonSerializationContext jsonSerializationContext) {
            return this.serialize((LootPool)object, type, jsonSerializationContext);
        }

        public /* synthetic */ Object deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return this.deserialize(jsonElement, type, jsonDeserializationContext);
        }
    }
}

