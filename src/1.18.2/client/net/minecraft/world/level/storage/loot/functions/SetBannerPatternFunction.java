/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonDeserializationContext
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonSerializationContext
 *  com.google.gson.JsonSyntaxException
 *  com.mojang.datafixers.util.Pair
 */
package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetBannerPatternFunction
extends LootItemConditionalFunction {
    final List<Pair<BannerPattern, DyeColor>> patterns;
    final boolean append;

    SetBannerPatternFunction(LootItemCondition[] lootItemConditionArray, List<Pair<BannerPattern, DyeColor>> list, boolean bl) {
        super(lootItemConditionArray);
        this.patterns = list;
        this.append = bl;
    }

    @Override
    protected ItemStack run(ItemStack itemStack, LootContext lootContext) {
        ListTag listTag;
        CompoundTag compoundTag = BlockItem.getBlockEntityData(itemStack);
        if (compoundTag == null) {
            compoundTag = new CompoundTag();
        }
        BannerPattern.Builder builder = new BannerPattern.Builder();
        this.patterns.forEach(builder::addPattern);
        ListTag listTag2 = builder.toListTag();
        if (this.append) {
            listTag = compoundTag.getList("Patterns", 10).copy();
            listTag.addAll(listTag2);
        } else {
            listTag = listTag2;
        }
        compoundTag.put("Patterns", listTag);
        BlockItem.setBlockEntityData(itemStack, BlockEntityType.BANNER, compoundTag);
        return itemStack;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.SET_BANNER_PATTERN;
    }

    public static Builder setBannerPattern(boolean bl) {
        return new Builder(bl);
    }

    public static class Builder
    extends LootItemConditionalFunction.Builder<Builder> {
        private final ImmutableList.Builder<Pair<BannerPattern, DyeColor>> patterns = ImmutableList.builder();
        private final boolean append;

        Builder(boolean bl) {
            this.append = bl;
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new SetBannerPatternFunction(this.getConditions(), (List<Pair<BannerPattern, DyeColor>>)this.patterns.build(), this.append);
        }

        public Builder addPattern(BannerPattern bannerPattern, DyeColor dyeColor) {
            this.patterns.add((Object)Pair.of((Object)((Object)bannerPattern), (Object)dyeColor));
            return this;
        }

        @Override
        protected /* synthetic */ LootItemConditionalFunction.Builder getThis() {
            return this.getThis();
        }
    }

    public static class Serializer
    extends LootItemConditionalFunction.Serializer<SetBannerPatternFunction> {
        @Override
        public void serialize(JsonObject jsonObject, SetBannerPatternFunction setBannerPatternFunction, JsonSerializationContext jsonSerializationContext) {
            super.serialize(jsonObject, setBannerPatternFunction, jsonSerializationContext);
            JsonArray jsonArray = new JsonArray();
            setBannerPatternFunction.patterns.forEach(pair -> {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("pattern", ((BannerPattern)((Object)((Object)pair.getFirst()))).getFilename());
                jsonObject.addProperty("color", ((DyeColor)pair.getSecond()).getName());
                jsonArray.add((JsonElement)jsonObject);
            });
            jsonObject.add("patterns", (JsonElement)jsonArray);
            jsonObject.addProperty("append", Boolean.valueOf(setBannerPatternFunction.append));
        }

        @Override
        public SetBannerPatternFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditionArray) {
            int n;
            ImmutableList.Builder builder = ImmutableList.builder();
            JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "patterns");
            for (n = 0; n < jsonArray.size(); ++n) {
                JsonObject jsonObject2 = GsonHelper.convertToJsonObject(jsonArray.get(n), "pattern[" + n + "]");
                String string = GsonHelper.getAsString(jsonObject2, "pattern");
                BannerPattern bannerPattern = BannerPattern.byFilename(string);
                if (bannerPattern == null) {
                    throw new JsonSyntaxException("Unknown pattern: " + string);
                }
                String string2 = GsonHelper.getAsString(jsonObject2, "color");
                DyeColor dyeColor = DyeColor.byName(string2, null);
                if (dyeColor == null) {
                    throw new JsonSyntaxException("Unknown color: " + string2);
                }
                builder.add((Object)Pair.of((Object)((Object)bannerPattern), (Object)dyeColor));
            }
            n = GsonHelper.getAsBoolean(jsonObject, "append") ? 1 : 0;
            return new SetBannerPatternFunction(lootItemConditionArray, (List<Pair<BannerPattern, DyeColor>>)builder.build(), n != 0);
        }

        @Override
        public /* synthetic */ LootItemConditionalFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditionArray) {
            return this.deserialize(jsonObject, jsonDeserializationContext, lootItemConditionArray);
        }
    }
}

