/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableSet
 *  com.google.gson.JsonDeserializationContext
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonSerializationContext
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.storage.loot.providers.score;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.GsonAdapterFactory;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.providers.score.LootScoreProviderType;
import net.minecraft.world.level.storage.loot.providers.score.ScoreboardNameProvider;
import net.minecraft.world.level.storage.loot.providers.score.ScoreboardNameProviders;

public class ContextScoreboardNameProvider
implements ScoreboardNameProvider {
    final LootContext.EntityTarget target;

    ContextScoreboardNameProvider(LootContext.EntityTarget entityTarget) {
        this.target = entityTarget;
    }

    public static ScoreboardNameProvider forTarget(LootContext.EntityTarget entityTarget) {
        return new ContextScoreboardNameProvider(entityTarget);
    }

    @Override
    public LootScoreProviderType getType() {
        return ScoreboardNameProviders.CONTEXT;
    }

    @Override
    @Nullable
    public String getScoreboardName(LootContext lootContext) {
        Entity entity = lootContext.getParamOrNull(this.target.getParam());
        return entity != null ? entity.getScoreboardName() : null;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(this.target.getParam());
    }

    public static class InlineSerializer
    implements GsonAdapterFactory.InlineSerializer<ContextScoreboardNameProvider> {
        @Override
        public JsonElement serialize(ContextScoreboardNameProvider contextScoreboardNameProvider, JsonSerializationContext jsonSerializationContext) {
            return jsonSerializationContext.serialize((Object)contextScoreboardNameProvider.target);
        }

        @Override
        public ContextScoreboardNameProvider deserialize(JsonElement jsonElement, JsonDeserializationContext jsonDeserializationContext) {
            LootContext.EntityTarget entityTarget = (LootContext.EntityTarget)((Object)jsonDeserializationContext.deserialize(jsonElement, LootContext.EntityTarget.class));
            return new ContextScoreboardNameProvider(entityTarget);
        }

        @Override
        public /* synthetic */ Object deserialize(JsonElement jsonElement, JsonDeserializationContext jsonDeserializationContext) {
            return this.deserialize(jsonElement, jsonDeserializationContext);
        }
    }

    public static class Serializer
    implements net.minecraft.world.level.storage.loot.Serializer<ContextScoreboardNameProvider> {
        @Override
        public void serialize(JsonObject jsonObject, ContextScoreboardNameProvider contextScoreboardNameProvider, JsonSerializationContext jsonSerializationContext) {
            jsonObject.addProperty("target", contextScoreboardNameProvider.target.name());
        }

        @Override
        public ContextScoreboardNameProvider deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            LootContext.EntityTarget entityTarget = GsonHelper.getAsObject(jsonObject, "target", jsonDeserializationContext, LootContext.EntityTarget.class);
            return new ContextScoreboardNameProvider(entityTarget);
        }

        @Override
        public /* synthetic */ Object deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            return this.deserialize(jsonObject, jsonDeserializationContext);
        }
    }
}

