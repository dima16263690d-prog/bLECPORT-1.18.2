/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableSet
 *  com.google.common.collect.ImmutableSet$Builder
 *  com.google.gson.JsonDeserializationContext
 *  com.google.gson.JsonDeserializer
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParseException
 *  com.google.gson.JsonSerializationContext
 *  com.google.gson.JsonSerializer
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

public class IntRange {
    @Nullable
    final NumberProvider min;
    @Nullable
    final NumberProvider max;
    private final IntLimiter limiter;
    private final IntChecker predicate;

    public Set<LootContextParam<?>> getReferencedContextParams() {
        ImmutableSet.Builder builder = ImmutableSet.builder();
        if (this.min != null) {
            builder.addAll(this.min.getReferencedContextParams());
        }
        if (this.max != null) {
            builder.addAll(this.max.getReferencedContextParams());
        }
        return builder.build();
    }

    IntRange(@Nullable NumberProvider numberProvider, @Nullable NumberProvider numberProvider2) {
        this.min = numberProvider;
        this.max = numberProvider2;
        if (numberProvider == null) {
            if (numberProvider2 == null) {
                this.limiter = (lootContext, n) -> n;
                this.predicate = (lootContext, n) -> true;
            } else {
                this.limiter = (lootContext, n) -> Math.min(numberProvider2.getInt(lootContext), n);
                this.predicate = (lootContext, n) -> n <= numberProvider2.getInt(lootContext);
            }
        } else if (numberProvider2 == null) {
            this.limiter = (lootContext, n) -> Math.max(numberProvider.getInt(lootContext), n);
            this.predicate = (lootContext, n) -> n >= numberProvider.getInt(lootContext);
        } else {
            this.limiter = (lootContext, n) -> Mth.clamp(n, numberProvider.getInt(lootContext), numberProvider2.getInt(lootContext));
            this.predicate = (lootContext, n) -> n >= numberProvider.getInt(lootContext) && n <= numberProvider2.getInt(lootContext);
        }
    }

    public static IntRange exact(int n) {
        ConstantValue constantValue = ConstantValue.exactly(n);
        return new IntRange(constantValue, constantValue);
    }

    public static IntRange range(int n, int n2) {
        return new IntRange(ConstantValue.exactly(n), ConstantValue.exactly(n2));
    }

    public static IntRange lowerBound(int n) {
        return new IntRange(ConstantValue.exactly(n), null);
    }

    public static IntRange upperBound(int n) {
        return new IntRange(null, ConstantValue.exactly(n));
    }

    public int clamp(LootContext lootContext, int n) {
        return this.limiter.apply(lootContext, n);
    }

    public boolean test(LootContext lootContext, int n) {
        return this.predicate.test(lootContext, n);
    }

    @FunctionalInterface
    static interface IntLimiter {
        public int apply(LootContext var1, int var2);
    }

    @FunctionalInterface
    static interface IntChecker {
        public boolean test(LootContext var1, int var2);
    }

    public static class Serializer
    implements JsonDeserializer<IntRange>,
    JsonSerializer<IntRange> {
        public IntRange deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) {
            if (jsonElement.isJsonPrimitive()) {
                return IntRange.exact(jsonElement.getAsInt());
            }
            JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "value");
            NumberProvider numberProvider = jsonObject.has("min") ? GsonHelper.getAsObject(jsonObject, "min", jsonDeserializationContext, NumberProvider.class) : null;
            NumberProvider numberProvider2 = jsonObject.has("max") ? GsonHelper.getAsObject(jsonObject, "max", jsonDeserializationContext, NumberProvider.class) : null;
            return new IntRange(numberProvider, numberProvider2);
        }

        public JsonElement serialize(IntRange intRange, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject jsonObject = new JsonObject();
            if (Objects.equals(intRange.max, intRange.min)) {
                return jsonSerializationContext.serialize((Object)intRange.min);
            }
            if (intRange.max != null) {
                jsonObject.add("max", jsonSerializationContext.serialize((Object)intRange.max));
            }
            if (intRange.min != null) {
                jsonObject.add("min", jsonSerializationContext.serialize((Object)intRange.min));
            }
            return jsonObject;
        }

        public /* synthetic */ JsonElement serialize(Object object, Type type, JsonSerializationContext jsonSerializationContext) {
            return this.serialize((IntRange)object, type, jsonSerializationContext);
        }

        public /* synthetic */ Object deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return this.deserialize(jsonElement, type, jsonDeserializationContext);
        }
    }
}

