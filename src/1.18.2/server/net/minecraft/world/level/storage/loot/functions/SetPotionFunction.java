/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonDeserializationContext
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonSerializationContext
 *  com.google.gson.JsonSyntaxException
 */
package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetPotionFunction
extends LootItemConditionalFunction {
    final Potion potion;

    SetPotionFunction(LootItemCondition[] lootItemConditionArray, Potion potion) {
        super(lootItemConditionArray);
        this.potion = potion;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.SET_POTION;
    }

    @Override
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        PotionUtils.setPotion(itemStack, this.potion);
        return itemStack;
    }

    public static LootItemConditionalFunction.Builder<?> setPotion(Potion potion) {
        return SetPotionFunction.simpleBuilder(lootItemConditionArray -> new SetPotionFunction((LootItemCondition[])lootItemConditionArray, potion));
    }

    public static class Serializer
    extends LootItemConditionalFunction.Serializer<SetPotionFunction> {
        @Override
        public void serialize(JsonObject jsonObject, SetPotionFunction setPotionFunction, JsonSerializationContext jsonSerializationContext) {
            super.serialize(jsonObject, setPotionFunction, jsonSerializationContext);
            jsonObject.addProperty("id", Registry.POTION.getKey(setPotionFunction.potion).toString());
        }

        @Override
        public SetPotionFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditionArray) {
            String string = GsonHelper.getAsString(jsonObject, "id");
            Potion potion = Registry.POTION.getOptional(ResourceLocation.tryParse(string)).orElseThrow(() -> new JsonSyntaxException("Unknown potion '" + string + "'"));
            return new SetPotionFunction(lootItemConditionArray, potion);
        }

        @Override
        public /* synthetic */ LootItemConditionalFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditionArray) {
            return this.deserialize(jsonObject, jsonDeserializationContext, lootItemConditionArray);
        }
    }
}

