/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableSet
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonNull
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonSyntaxException
 *  javax.annotation.Nullable
 */
package net.minecraft.advancements.critereon;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BlockPredicate {
    public static final BlockPredicate ANY = new BlockPredicate(null, null, StatePropertiesPredicate.ANY, NbtPredicate.ANY);
    @Nullable
    private final TagKey<Block> tag;
    @Nullable
    private final Set<Block> blocks;
    private final StatePropertiesPredicate properties;
    private final NbtPredicate nbt;

    public BlockPredicate(@Nullable TagKey<Block> tagKey, @Nullable Set<Block> set, StatePropertiesPredicate statePropertiesPredicate, NbtPredicate nbtPredicate) {
        this.tag = tagKey;
        this.blocks = set;
        this.properties = statePropertiesPredicate;
        this.nbt = nbtPredicate;
    }

    public boolean matches(ServerLevel serverLevel, BlockPos blockPos) {
        BlockEntity blockEntity;
        if (this == ANY) {
            return true;
        }
        if (!serverLevel.isLoaded(blockPos)) {
            return false;
        }
        BlockState blockState = serverLevel.getBlockState(blockPos);
        if (this.tag != null && !blockState.is(this.tag)) {
            return false;
        }
        if (this.blocks != null && !this.blocks.contains(blockState.getBlock())) {
            return false;
        }
        if (!this.properties.matches(blockState)) {
            return false;
        }
        return this.nbt == NbtPredicate.ANY || (blockEntity = serverLevel.getBlockEntity(blockPos)) != null && this.nbt.matches(blockEntity.saveWithFullMetadata());
    }

    public static BlockPredicate fromJson(@Nullable JsonElement jsonElement) {
        Object object;
        Object object2;
        if (jsonElement == null || jsonElement.isJsonNull()) {
            return ANY;
        }
        JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "block");
        NbtPredicate nbtPredicate = NbtPredicate.fromJson(jsonObject.get("nbt"));
        ImmutableSet immutableSet = null;
        JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "blocks", null);
        if (jsonArray != null) {
            object2 = ImmutableSet.builder();
            for (JsonElement jsonElement2 : jsonArray) {
                ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.convertToString(jsonElement2, "block"));
                object2.add((Object)Registry.BLOCK.getOptional(resourceLocation).orElseThrow(() -> new JsonSyntaxException("Unknown block id '" + resourceLocation + "'")));
            }
            immutableSet = object2.build();
        }
        object2 = null;
        if (jsonObject.has("tag")) {
            object = new ResourceLocation(GsonHelper.getAsString(jsonObject, "tag"));
            object2 = TagKey.create(Registry.BLOCK_REGISTRY, (ResourceLocation)object);
        }
        object = StatePropertiesPredicate.fromJson(jsonObject.get("state"));
        return new BlockPredicate((TagKey<Block>)((Object)object2), (Set<Block>)immutableSet, (StatePropertiesPredicate)object, nbtPredicate);
    }

    public JsonElement serializeToJson() {
        if (this == ANY) {
            return JsonNull.INSTANCE;
        }
        JsonObject jsonObject = new JsonObject();
        if (this.blocks != null) {
            JsonArray jsonArray = new JsonArray();
            for (Block block : this.blocks) {
                jsonArray.add(Registry.BLOCK.getKey(block).toString());
            }
            jsonObject.add("blocks", (JsonElement)jsonArray);
        }
        if (this.tag != null) {
            jsonObject.addProperty("tag", this.tag.location().toString());
        }
        jsonObject.add("nbt", this.nbt.serializeToJson());
        jsonObject.add("state", this.properties.serializeToJson());
        return jsonObject;
    }

    public static class Builder {
        @Nullable
        private Set<Block> blocks;
        @Nullable
        private TagKey<Block> tag;
        private StatePropertiesPredicate properties = StatePropertiesPredicate.ANY;
        private NbtPredicate nbt = NbtPredicate.ANY;

        private Builder() {
        }

        public static Builder block() {
            return new Builder();
        }

        public Builder of(Block ... blockArray) {
            this.blocks = ImmutableSet.copyOf((Object[])blockArray);
            return this;
        }

        public Builder of(Iterable<Block> iterable) {
            this.blocks = ImmutableSet.copyOf(iterable);
            return this;
        }

        public Builder of(TagKey<Block> tagKey) {
            this.tag = tagKey;
            return this;
        }

        public Builder hasNbt(CompoundTag compoundTag) {
            this.nbt = new NbtPredicate(compoundTag);
            return this;
        }

        public Builder setProperties(StatePropertiesPredicate statePropertiesPredicate) {
            this.properties = statePropertiesPredicate;
            return this;
        }

        public BlockPredicate build() {
            return new BlockPredicate(this.tag, this.blocks, this.properties, this.nbt);
        }
    }
}

