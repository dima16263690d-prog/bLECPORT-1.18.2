/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableSet
 *  com.google.common.collect.ImmutableSet$Builder
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.mojang.datafixers.util.Either
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 */
package net.minecraft.tags;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public class Tag<T> {
    private static final Tag<?> EMPTY = new Tag(List.of());
    final List<T> elements;

    public Tag(Collection<T> collection) {
        this.elements = List.copyOf(collection);
    }

    public List<T> getValues() {
        return this.elements;
    }

    public static <T> Tag<T> empty() {
        return EMPTY;
    }

    static class OptionalTagEntry
    implements Entry {
        private final ResourceLocation id;

        public OptionalTagEntry(ResourceLocation resourceLocation) {
            this.id = resourceLocation;
        }

        @Override
        public <T> boolean build(Function<ResourceLocation, Tag<T>> function, Function<ResourceLocation, T> function2, Consumer<T> consumer) {
            Tag<T> tag = function.apply(this.id);
            if (tag != null) {
                tag.elements.forEach(consumer);
            }
            return true;
        }

        @Override
        public void serializeTo(JsonArray jsonArray) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("id", "#" + this.id);
            jsonObject.addProperty("required", Boolean.valueOf(false));
            jsonArray.add((JsonElement)jsonObject);
        }

        public String toString() {
            return "#" + this.id + "?";
        }

        @Override
        public void visitOptionalDependencies(Consumer<ResourceLocation> consumer) {
            consumer.accept(this.id);
        }

        @Override
        public boolean verifyIfPresent(Predicate<ResourceLocation> predicate, Predicate<ResourceLocation> predicate2) {
            return true;
        }
    }

    static class TagEntry
    implements Entry {
        private final ResourceLocation id;

        public TagEntry(ResourceLocation resourceLocation) {
            this.id = resourceLocation;
        }

        @Override
        public <T> boolean build(Function<ResourceLocation, Tag<T>> function, Function<ResourceLocation, T> function2, Consumer<T> consumer) {
            Tag<T> tag = function.apply(this.id);
            if (tag == null) {
                return false;
            }
            tag.elements.forEach(consumer);
            return true;
        }

        @Override
        public void serializeTo(JsonArray jsonArray) {
            jsonArray.add("#" + this.id);
        }

        public String toString() {
            return "#" + this.id;
        }

        @Override
        public boolean verifyIfPresent(Predicate<ResourceLocation> predicate, Predicate<ResourceLocation> predicate2) {
            return predicate2.test(this.id);
        }

        @Override
        public void visitRequiredDependencies(Consumer<ResourceLocation> consumer) {
            consumer.accept(this.id);
        }
    }

    static class OptionalElementEntry
    implements Entry {
        private final ResourceLocation id;

        public OptionalElementEntry(ResourceLocation resourceLocation) {
            this.id = resourceLocation;
        }

        @Override
        public <T> boolean build(Function<ResourceLocation, Tag<T>> function, Function<ResourceLocation, T> function2, Consumer<T> consumer) {
            T t = function2.apply(this.id);
            if (t != null) {
                consumer.accept(t);
            }
            return true;
        }

        @Override
        public void serializeTo(JsonArray jsonArray) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("id", this.id.toString());
            jsonObject.addProperty("required", Boolean.valueOf(false));
            jsonArray.add((JsonElement)jsonObject);
        }

        @Override
        public boolean verifyIfPresent(Predicate<ResourceLocation> predicate, Predicate<ResourceLocation> predicate2) {
            return true;
        }

        public String toString() {
            return this.id + "?";
        }
    }

    static class ElementEntry
    implements Entry {
        private final ResourceLocation id;

        public ElementEntry(ResourceLocation resourceLocation) {
            this.id = resourceLocation;
        }

        @Override
        public <T> boolean build(Function<ResourceLocation, Tag<T>> function, Function<ResourceLocation, T> function2, Consumer<T> consumer) {
            T t = function2.apply(this.id);
            if (t == null) {
                return false;
            }
            consumer.accept(t);
            return true;
        }

        @Override
        public void serializeTo(JsonArray jsonArray) {
            jsonArray.add(this.id.toString());
        }

        @Override
        public boolean verifyIfPresent(Predicate<ResourceLocation> predicate, Predicate<ResourceLocation> predicate2) {
            return predicate.test(this.id);
        }

        public String toString() {
            return this.id.toString();
        }
    }

    public static interface Entry {
        public <T> boolean build(Function<ResourceLocation, Tag<T>> var1, Function<ResourceLocation, T> var2, Consumer<T> var3);

        public void serializeTo(JsonArray var1);

        default public void visitRequiredDependencies(Consumer<ResourceLocation> consumer) {
        }

        default public void visitOptionalDependencies(Consumer<ResourceLocation> consumer) {
        }

        public boolean verifyIfPresent(Predicate<ResourceLocation> var1, Predicate<ResourceLocation> var2);
    }

    public static class Builder {
        private final List<BuilderEntry> entries = new ArrayList<BuilderEntry>();

        public static Builder tag() {
            return new Builder();
        }

        public Builder add(BuilderEntry builderEntry) {
            this.entries.add(builderEntry);
            return this;
        }

        public Builder add(Entry entry, String string) {
            return this.add(new BuilderEntry(entry, string));
        }

        public Builder addElement(ResourceLocation resourceLocation, String string) {
            return this.add(new ElementEntry(resourceLocation), string);
        }

        public Builder addOptionalElement(ResourceLocation resourceLocation, String string) {
            return this.add(new OptionalElementEntry(resourceLocation), string);
        }

        public Builder addTag(ResourceLocation resourceLocation, String string) {
            return this.add(new TagEntry(resourceLocation), string);
        }

        public Builder addOptionalTag(ResourceLocation resourceLocation, String string) {
            return this.add(new OptionalTagEntry(resourceLocation), string);
        }

        public <T> Either<Collection<BuilderEntry>, Tag<T>> build(Function<ResourceLocation, Tag<T>> function, Function<ResourceLocation, T> function2) {
            ImmutableSet.Builder builder = ImmutableSet.builder();
            ArrayList<BuilderEntry> arrayList = new ArrayList<BuilderEntry>();
            for (BuilderEntry builderEntry : this.entries) {
                if (builderEntry.entry().build(function, function2, arg_0 -> ((ImmutableSet.Builder)builder).add(arg_0))) continue;
                arrayList.add(builderEntry);
            }
            return arrayList.isEmpty() ? Either.right(new Tag(builder.build())) : Either.left(arrayList);
        }

        public Stream<BuilderEntry> getEntries() {
            return this.entries.stream();
        }

        public void visitRequiredDependencies(Consumer<ResourceLocation> consumer) {
            this.entries.forEach(builderEntry -> builderEntry.entry.visitRequiredDependencies(consumer));
        }

        public void visitOptionalDependencies(Consumer<ResourceLocation> consumer) {
            this.entries.forEach(builderEntry -> builderEntry.entry.visitOptionalDependencies(consumer));
        }

        public Builder addFromJson(JsonObject jsonObject, String string) {
            JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "values");
            ArrayList<Entry> arrayList = new ArrayList<Entry>();
            for (JsonElement jsonElement : jsonArray) {
                arrayList.add(Builder.parseEntry(jsonElement));
            }
            if (GsonHelper.getAsBoolean(jsonObject, "replace", false)) {
                this.entries.clear();
            }
            arrayList.forEach(entry -> this.entries.add(new BuilderEntry((Entry)entry, string)));
            return this;
        }

        private static Entry parseEntry(JsonElement jsonElement) {
            boolean bl;
            String string;
            Object object;
            if (jsonElement.isJsonObject()) {
                object = jsonElement.getAsJsonObject();
                string = GsonHelper.getAsString(object, "id");
                bl = GsonHelper.getAsBoolean(object, "required", true);
            } else {
                string = GsonHelper.convertToString(jsonElement, "id");
                bl = true;
            }
            if (string.startsWith("#")) {
                object = new ResourceLocation(string.substring(1));
                return bl ? new TagEntry((ResourceLocation)object) : new OptionalTagEntry((ResourceLocation)object);
            }
            object = new ResourceLocation(string);
            return bl ? new ElementEntry((ResourceLocation)object) : new OptionalElementEntry((ResourceLocation)object);
        }

        public JsonObject serializeToJson() {
            JsonObject jsonObject = new JsonObject();
            JsonArray jsonArray = new JsonArray();
            for (BuilderEntry builderEntry : this.entries) {
                builderEntry.entry().serializeTo(jsonArray);
            }
            jsonObject.addProperty("replace", Boolean.valueOf(false));
            jsonObject.add("values", (JsonElement)jsonArray);
            return jsonObject;
        }
    }

    public static final class BuilderEntry
    extends Record {
        final Entry entry;
        private final String source;

        public BuilderEntry(Entry entry, String string) {
            this.entry = entry;
            this.source = string;
        }

        public String toString() {
            return this.entry + " (from " + this.source + ")";
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{BuilderEntry.class, "entry;source", "entry", "source"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{BuilderEntry.class, "entry;source", "entry", "source"}, this, object);
        }

        public Entry entry() {
            return this.entry;
        }

        public String source() {
            return this.source;
        }
    }
}

