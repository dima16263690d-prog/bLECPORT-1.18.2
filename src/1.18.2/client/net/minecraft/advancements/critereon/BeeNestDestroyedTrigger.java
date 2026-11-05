/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonSyntaxException
 *  javax.annotation.Nullable
 */
package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import javax.annotation.Nullable;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BeeNestDestroyedTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    static final ResourceLocation ID = new ResourceLocation("bee_nest_destroyed");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public TriggerInstance createInstance(JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext) {
        Block block = BeeNestDestroyedTrigger.deserializeBlock(jsonObject);
        ItemPredicate itemPredicate = ItemPredicate.fromJson(jsonObject.get("item"));
        MinMaxBounds.Ints ints = MinMaxBounds.Ints.fromJson(jsonObject.get("num_bees_inside"));
        return new TriggerInstance(composite, block, itemPredicate, ints);
    }

    @Nullable
    private static Block deserializeBlock(JsonObject jsonObject) {
        if (jsonObject.has("block")) {
            ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "block"));
            return Registry.BLOCK.getOptional(resourceLocation).orElseThrow(() -> new JsonSyntaxException("Unknown block type '" + resourceLocation + "'"));
        }
        return null;
    }

    public void trigger(ServerPlayer serverPlayer, BlockState blockState, ItemStack itemStack, int n) {
        this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(blockState, itemStack, n));
    }

    @Override
    public /* synthetic */ AbstractCriterionTriggerInstance createInstance(JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext) {
        return this.createInstance(jsonObject, composite, deserializationContext);
    }

    public static class TriggerInstance
    extends AbstractCriterionTriggerInstance {
        @Nullable
        private final Block block;
        private final ItemPredicate item;
        private final MinMaxBounds.Ints numBees;

        public TriggerInstance(EntityPredicate.Composite composite, @Nullable Block block, ItemPredicate itemPredicate, MinMaxBounds.Ints ints) {
            super(ID, composite);
            this.block = block;
            this.item = itemPredicate;
            this.numBees = ints;
        }

        public static TriggerInstance destroyedBeeNest(Block block, ItemPredicate.Builder builder, MinMaxBounds.Ints ints) {
            return new TriggerInstance(EntityPredicate.Composite.ANY, block, builder.build(), ints);
        }

        public boolean matches(BlockState blockState, ItemStack itemStack, int n) {
            if (this.block != null && !blockState.is(this.block)) {
                return false;
            }
            if (!this.item.matches(itemStack)) {
                return false;
            }
            return this.numBees.matches(n);
        }

        @Override
        public JsonObject serializeToJson(SerializationContext serializationContext) {
            JsonObject jsonObject = super.serializeToJson(serializationContext);
            if (this.block != null) {
                jsonObject.addProperty("block", Registry.BLOCK.getKey(this.block).toString());
            }
            jsonObject.add("item", this.item.serializeToJson());
            jsonObject.add("num_bees_inside", this.numBees.serializeToJson());
            return jsonObject;
        }
    }
}

