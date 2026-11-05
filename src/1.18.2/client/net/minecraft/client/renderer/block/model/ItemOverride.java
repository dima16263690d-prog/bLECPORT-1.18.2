/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.Maps
 *  com.google.gson.JsonDeserializationContext
 *  com.google.gson.JsonDeserializer
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParseException
 */
package net.minecraft.client.renderer.block.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public class ItemOverride {
    private final ResourceLocation model;
    private final List<Predicate> predicates;

    public ItemOverride(ResourceLocation resourceLocation, List<Predicate> list) {
        this.model = resourceLocation;
        this.predicates = ImmutableList.copyOf(list);
    }

    public ResourceLocation getModel() {
        return this.model;
    }

    public Stream<Predicate> getPredicates() {
        return this.predicates.stream();
    }

    public static class Predicate {
        private final ResourceLocation property;
        private final float value;

        public Predicate(ResourceLocation resourceLocation, float f) {
            this.property = resourceLocation;
            this.value = f;
        }

        public ResourceLocation getProperty() {
            return this.property;
        }

        public float getValue() {
            return this.value;
        }
    }

    protected static class Deserializer
    implements JsonDeserializer<ItemOverride> {
        protected Deserializer() {
        }

        public ItemOverride deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "model"));
            List<Predicate> list = this.getPredicates(jsonObject);
            return new ItemOverride(resourceLocation, list);
        }

        protected List<Predicate> getPredicates(JsonObject jsonObject) {
            LinkedHashMap linkedHashMap = Maps.newLinkedHashMap();
            JsonObject jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "predicate");
            for (Map.Entry entry2 : jsonObject2.entrySet()) {
                linkedHashMap.put(new ResourceLocation((String)entry2.getKey()), Float.valueOf(GsonHelper.convertToFloat((JsonElement)entry2.getValue(), (String)entry2.getKey())));
            }
            return (List)linkedHashMap.entrySet().stream().map(entry -> new Predicate((ResourceLocation)entry.getKey(), ((Float)entry.getValue()).floatValue())).collect(ImmutableList.toImmutableList());
        }

        public /* synthetic */ Object deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return this.deserialize(jsonElement, type, jsonDeserializationContext);
        }
    }
}

