/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonDeserializationContext
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonSerializationContext
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.storage.loot.predicates;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;

public class TimeCheck
implements LootItemCondition {
    @Nullable
    final Long period;
    final IntRange value;

    TimeCheck(@Nullable Long l, IntRange intRange) {
        this.period = l;
        this.value = intRange;
    }

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.TIME_CHECK;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return this.value.getReferencedContextParams();
    }

    @Override
    public boolean test(LootContext lootContext) {
        ServerLevel serverLevel = lootContext.getLevel();
        long l = serverLevel.getDayTime();
        if (this.period != null) {
            l %= this.period.longValue();
        }
        return this.value.test(lootContext, (int)l);
    }

    public static Builder time(IntRange intRange) {
        return new Builder(intRange);
    }

    @Override
    public /* synthetic */ boolean test(Object object) {
        return this.test((LootContext)object);
    }

    public static class Builder
    implements LootItemCondition.Builder {
        @Nullable
        private Long period;
        private final IntRange value;

        public Builder(IntRange intRange) {
            this.value = intRange;
        }

        public Builder setPeriod(long l) {
            this.period = l;
            return this;
        }

        @Override
        public TimeCheck build() {
            return new TimeCheck(this.period, this.value);
        }

        @Override
        public /* synthetic */ LootItemCondition build() {
            return this.build();
        }
    }

    public static class Serializer
    implements net.minecraft.world.level.storage.loot.Serializer<TimeCheck> {
        @Override
        public void serialize(JsonObject jsonObject, TimeCheck timeCheck, JsonSerializationContext jsonSerializationContext) {
            jsonObject.addProperty("period", (Number)timeCheck.period);
            jsonObject.add("value", jsonSerializationContext.serialize((Object)timeCheck.value));
        }

        @Override
        public TimeCheck deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            Long l = jsonObject.has("period") ? Long.valueOf(GsonHelper.getAsLong(jsonObject, "period")) : null;
            IntRange intRange = GsonHelper.getAsObject(jsonObject, "value", jsonDeserializationContext, IntRange.class);
            return new TimeCheck(l, intRange);
        }

        @Override
        public /* synthetic */ Object deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            return this.deserialize(jsonObject, jsonDeserializationContext);
        }
    }
}

