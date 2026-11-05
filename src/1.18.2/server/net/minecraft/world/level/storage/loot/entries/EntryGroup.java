/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 */
package net.minecraft.world.level.storage.loot.entries;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.world.level.storage.loot.entries.ComposableEntryContainer;
import net.minecraft.world.level.storage.loot.entries.CompositeEntryBase;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class EntryGroup
extends CompositeEntryBase {
    EntryGroup(LootPoolEntryContainer[] lootPoolEntryContainerArray, LootItemCondition[] lootItemConditionArray) {
        super(lootPoolEntryContainerArray, lootItemConditionArray);
    }

    @Override
    public LootPoolEntryType getType() {
        return LootPoolEntries.GROUP;
    }

    @Override
    protected ComposableEntryContainer compose(ComposableEntryContainer[] composableEntryContainerArray) {
        switch (composableEntryContainerArray.length) {
            case 0: {
                return ALWAYS_TRUE;
            }
            case 1: {
                return composableEntryContainerArray[0];
            }
            case 2: {
                ComposableEntryContainer composableEntryContainer = composableEntryContainerArray[0];
                ComposableEntryContainer composableEntryContainer2 = composableEntryContainerArray[1];
                return (lootContext, consumer) -> {
                    composableEntryContainer.expand(lootContext, consumer);
                    composableEntryContainer2.expand(lootContext, consumer);
                    return true;
                };
            }
        }
        return (lootContext, consumer) -> {
            for (ComposableEntryContainer composableEntryContainer : composableEntryContainerArray) {
                composableEntryContainer.expand(lootContext, consumer);
            }
            return true;
        };
    }

    public static Builder list(LootPoolEntryContainer.Builder<?> ... builderArray) {
        return new Builder(builderArray);
    }

    public static class Builder
    extends LootPoolEntryContainer.Builder<Builder> {
        private final List<LootPoolEntryContainer> entries = Lists.newArrayList();

        public Builder(LootPoolEntryContainer.Builder<?> ... builderArray) {
            for (LootPoolEntryContainer.Builder<?> builder : builderArray) {
                this.entries.add(builder.build());
            }
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        @Override
        public Builder append(LootPoolEntryContainer.Builder<?> builder) {
            this.entries.add(builder.build());
            return this;
        }

        @Override
        public LootPoolEntryContainer build() {
            return new EntryGroup(this.entries.toArray(new LootPoolEntryContainer[0]), this.getConditions());
        }

        @Override
        protected /* synthetic */ LootPoolEntryContainer.Builder getThis() {
            return this.getThis();
        }
    }
}

