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
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.ComposableEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntry;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public abstract class CompositeEntryBase
extends LootPoolEntryContainer {
    protected final LootPoolEntryContainer[] children;
    private final ComposableEntryContainer composedChildren;

    protected CompositeEntryBase(LootPoolEntryContainer[] lootPoolEntryContainerArray, LootItemCondition[] lootItemConditionArray) {
        super(lootItemConditionArray);
        this.children = lootPoolEntryContainerArray;
        this.composedChildren = this.compose(lootPoolEntryContainerArray);
    }

    @Override
    public void validate(ValidationContext validationContext) {
        super.validate(validationContext);
        if (this.children.length == 0) {
            validationContext.reportProblem("Empty children list");
        }
        for (int i = 0; i < this.children.length; ++i) {
            this.children[i].validate(validationContext.forChild(".entry[" + i + "]"));
        }
    }

    protected abstract ComposableEntryContainer compose(ComposableEntryContainer[] var1);

    @Override
    public final boolean expand(LootContext lootContext, Consumer<LootPoolEntry> consumer) {
        if (!this.canRun(lootContext)) {
            return false;
        }
        return this.composedChildren.expand(lootContext, consumer);
    }

    public static <T extends CompositeEntryBase> LootPoolEntryContainer.Serializer<T> createSerializer(final CompositeEntryConstructor<T> compositeEntryConstructor) {
        return new LootPoolEntryContainer.Serializer<T>(){

            @Override
            public void serializeCustom(JsonObject jsonObject, T t, JsonSerializationContext jsonSerializationContext) {
                jsonObject.add("children", jsonSerializationContext.serialize((Object)((CompositeEntryBase)t).children));
            }

            @Override
            public final T deserializeCustom(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditionArray) {
                LootPoolEntryContainer[] lootPoolEntryContainerArray = GsonHelper.getAsObject(jsonObject, "children", jsonDeserializationContext, LootPoolEntryContainer[].class);
                return compositeEntryConstructor.create(lootPoolEntryContainerArray, lootItemConditionArray);
            }

            @Override
            public /* synthetic */ LootPoolEntryContainer deserializeCustom(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditionArray) {
                return this.deserializeCustom(jsonObject, jsonDeserializationContext, lootItemConditionArray);
            }
        };
    }

    @FunctionalInterface
    public static interface CompositeEntryConstructor<T extends CompositeEntryBase> {
        public T create(LootPoolEntryContainer[] var1, LootItemCondition[] var2);
    }
}

