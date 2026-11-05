/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableSet
 *  com.google.common.collect.Maps
 *  com.google.gson.JsonDeserializationContext
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonSerializationContext
 *  com.google.gson.JsonSyntaxException
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 */
package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

public class SetEnchantmentsFunction
extends LootItemConditionalFunction {
    final Map<Enchantment, NumberProvider> enchantments;
    final boolean add;

    SetEnchantmentsFunction(LootItemCondition[] lootItemConditionArray, Map<Enchantment, NumberProvider> map, boolean bl) {
        super(lootItemConditionArray);
        this.enchantments = ImmutableMap.copyOf(map);
        this.add = bl;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.SET_ENCHANTMENTS;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return (Set)this.enchantments.values().stream().flatMap(numberProvider -> numberProvider.getReferencedContextParams().stream()).collect(ImmutableSet.toImmutableSet());
    }

    @Override
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        Object2IntOpenHashMap object2IntOpenHashMap = new Object2IntOpenHashMap();
        this.enchantments.forEach((arg_0, arg_1) -> SetEnchantmentsFunction.lambda$run$1((Object2IntMap)object2IntOpenHashMap, lootContext, arg_0, arg_1));
        if (itemStack.getItem() == Items.BOOK) {
            ItemStack itemStack2 = new ItemStack(Items.ENCHANTED_BOOK);
            object2IntOpenHashMap.forEach((enchantment, n) -> EnchantedBookItem.addEnchantment(itemStack2, new EnchantmentInstance((Enchantment)enchantment, (int)n)));
            return itemStack2;
        }
        Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(itemStack);
        if (this.add) {
            object2IntOpenHashMap.forEach((enchantment, n) -> SetEnchantmentsFunction.updateEnchantment(map, enchantment, Math.max(map.getOrDefault(enchantment, 0) + n, 0)));
        } else {
            object2IntOpenHashMap.forEach((enchantment, n) -> SetEnchantmentsFunction.updateEnchantment(map, enchantment, Math.max(n, 0)));
        }
        EnchantmentHelper.setEnchantments(map, itemStack);
        return itemStack;
    }

    private static void updateEnchantment(Map<Enchantment, Integer> map, Enchantment enchantment, int n) {
        if (n == 0) {
            map.remove(enchantment);
        } else {
            map.put(enchantment, n);
        }
    }

    private static /* synthetic */ void lambda$run$1(Object2IntMap object2IntMap, LootContext lootContext, Enchantment enchantment, NumberProvider numberProvider) {
        object2IntMap.put((Object)enchantment, numberProvider.getInt(lootContext));
    }

    public static class Serializer
    extends LootItemConditionalFunction.Serializer<SetEnchantmentsFunction> {
        @Override
        public void serialize(JsonObject jsonObject, SetEnchantmentsFunction setEnchantmentsFunction, JsonSerializationContext jsonSerializationContext) {
            super.serialize(jsonObject, setEnchantmentsFunction, jsonSerializationContext);
            JsonObject jsonObject2 = new JsonObject();
            setEnchantmentsFunction.enchantments.forEach((enchantment, numberProvider) -> {
                ResourceLocation resourceLocation = Registry.ENCHANTMENT.getKey((Enchantment)enchantment);
                if (resourceLocation == null) {
                    throw new IllegalArgumentException("Don't know how to serialize enchantment " + enchantment);
                }
                jsonObject2.add(resourceLocation.toString(), jsonSerializationContext.serialize(numberProvider));
            });
            jsonObject.add("enchantments", (JsonElement)jsonObject2);
            jsonObject.addProperty("add", Boolean.valueOf(setEnchantmentsFunction.add));
        }

        @Override
        public SetEnchantmentsFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditionArray) {
            HashMap hashMap = Maps.newHashMap();
            if (jsonObject.has("enchantments")) {
                JsonObject jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "enchantments");
                for (Map.Entry entry : jsonObject2.entrySet()) {
                    String string = (String)entry.getKey();
                    JsonElement jsonElement = (JsonElement)entry.getValue();
                    Enchantment enchantment = Registry.ENCHANTMENT.getOptional(new ResourceLocation(string)).orElseThrow(() -> new JsonSyntaxException("Unknown enchantment '" + string + "'"));
                    NumberProvider numberProvider = (NumberProvider)jsonDeserializationContext.deserialize(jsonElement, NumberProvider.class);
                    hashMap.put(enchantment, numberProvider);
                }
            }
            boolean bl = GsonHelper.getAsBoolean(jsonObject, "add", false);
            return new SetEnchantmentsFunction(lootItemConditionArray, hashMap, bl);
        }

        @Override
        public /* synthetic */ LootItemConditionalFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditionArray) {
            return this.deserialize(jsonObject, jsonDeserializationContext, lootItemConditionArray);
        }
    }

    public static class Builder
    extends LootItemConditionalFunction.Builder<Builder> {
        private final Map<Enchantment, NumberProvider> enchantments = Maps.newHashMap();
        private final boolean add;

        public Builder() {
            this(false);
        }

        public Builder(boolean bl) {
            this.add = bl;
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        public Builder withEnchantment(Enchantment enchantment, NumberProvider numberProvider) {
            this.enchantments.put(enchantment, numberProvider);
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new SetEnchantmentsFunction(this.getConditions(), this.enchantments, this.add);
        }

        @Override
        protected /* synthetic */ LootItemConditionalFunction.Builder getThis() {
            return this.getThis();
        }
    }
}

