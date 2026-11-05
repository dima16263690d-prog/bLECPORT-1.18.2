/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonDeserializationContext
 *  com.google.gson.JsonDeserializer
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParseException
 *  com.google.gson.JsonPrimitive
 *  com.google.gson.JsonSerializationContext
 *  com.google.gson.JsonSerializer
 *  com.google.gson.TypeAdapterFactory
 *  com.google.gson.stream.JsonReader
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.StringReader
 *  javax.annotation.Nullable
 */
package net.minecraft.network.chat;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.stream.JsonReader;
import com.mojang.brigadier.Message;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.KeybindComponent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.NbtComponent;
import net.minecraft.network.chat.ScoreComponent;
import net.minecraft.network.chat.SelectorComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.LowerCaseEnumTypeAdapterFactory;

public interface Component
extends Message,
FormattedText {
    public Style getStyle();

    public String getContents();

    @Override
    default public String getString() {
        return FormattedText.super.getString();
    }

    default public String getString(int n) {
        StringBuilder stringBuilder = new StringBuilder();
        this.visit(string -> {
            int n2 = n - stringBuilder.length();
            if (n2 <= 0) {
                return STOP_ITERATION;
            }
            stringBuilder.append(string.length() <= n2 ? string : string.substring(0, n2));
            return Optional.empty();
        });
        return stringBuilder.toString();
    }

    public List<Component> getSiblings();

    public MutableComponent plainCopy();

    public MutableComponent copy();

    public FormattedCharSequence getVisualOrderText();

    @Override
    default public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> styledContentConsumer, Style style) {
        Style style2 = this.getStyle().applyTo(style);
        Optional<T> optional = this.visitSelf(styledContentConsumer, style2);
        if (optional.isPresent()) {
            return optional;
        }
        for (Component component : this.getSiblings()) {
            Optional<T> optional2 = component.visit(styledContentConsumer, style2);
            if (!optional2.isPresent()) continue;
            return optional2;
        }
        return Optional.empty();
    }

    @Override
    default public <T> Optional<T> visit(FormattedText.ContentConsumer<T> contentConsumer) {
        Optional<T> optional = this.visitSelf(contentConsumer);
        if (optional.isPresent()) {
            return optional;
        }
        for (Component component : this.getSiblings()) {
            Optional<T> optional2 = component.visit(contentConsumer);
            if (!optional2.isPresent()) continue;
            return optional2;
        }
        return Optional.empty();
    }

    default public <T> Optional<T> visitSelf(FormattedText.StyledContentConsumer<T> styledContentConsumer, Style style) {
        return styledContentConsumer.accept(style, this.getContents());
    }

    default public <T> Optional<T> visitSelf(FormattedText.ContentConsumer<T> contentConsumer) {
        return contentConsumer.accept(this.getContents());
    }

    default public List<Component> toFlatList(Style style2) {
        ArrayList arrayList = Lists.newArrayList();
        this.visit((style, string) -> {
            if (!string.isEmpty()) {
                arrayList.add(new TextComponent(string).withStyle(style));
            }
            return Optional.empty();
        }, style2);
        return arrayList;
    }

    public static Component nullToEmpty(@Nullable String string) {
        return string != null ? new TextComponent(string) : TextComponent.EMPTY;
    }

    public static class Serializer
    implements JsonDeserializer<MutableComponent>,
    JsonSerializer<Component> {
        private static final Gson GSON = Util.make(() -> {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.disableHtmlEscaping();
            gsonBuilder.registerTypeHierarchyAdapter(Component.class, (Object)new Serializer());
            gsonBuilder.registerTypeHierarchyAdapter(Style.class, (Object)new Style.Serializer());
            gsonBuilder.registerTypeAdapterFactory((TypeAdapterFactory)new LowerCaseEnumTypeAdapterFactory());
            return gsonBuilder.create();
        });
        private static final Field JSON_READER_POS = Util.make(() -> {
            try {
                new JsonReader((Reader)new StringReader(""));
                Field field = JsonReader.class.getDeclaredField("pos");
                field.setAccessible(true);
                return field;
            }
            catch (NoSuchFieldException noSuchFieldException) {
                throw new IllegalStateException("Couldn't get field 'pos' for JsonReader", noSuchFieldException);
            }
        });
        private static final Field JSON_READER_LINESTART = Util.make(() -> {
            try {
                new JsonReader((Reader)new StringReader(""));
                Field field = JsonReader.class.getDeclaredField("lineStart");
                field.setAccessible(true);
                return field;
            }
            catch (NoSuchFieldException noSuchFieldException) {
                throw new IllegalStateException("Couldn't get field 'lineStart' for JsonReader", noSuchFieldException);
            }
        });

        /*
         * WARNING - void declaration
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        public MutableComponent deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            void var5_19;
            if (jsonElement.isJsonPrimitive()) {
                return new TextComponent(jsonElement.getAsString());
            }
            if (jsonElement.isJsonObject()) {
                void var5_17;
                Object object;
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                if (jsonObject.has("text")) {
                    TextComponent textComponent = new TextComponent(GsonHelper.getAsString(jsonObject, "text"));
                } else if (jsonObject.has("translate")) {
                    object = GsonHelper.getAsString(jsonObject, "translate");
                    if (jsonObject.has("with")) {
                        JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "with");
                        Object[] objectArray = new Object[jsonArray.size()];
                        for (int i = 0; i < objectArray.length; ++i) {
                            TextComponent textComponent;
                            objectArray[i] = this.deserialize(jsonArray.get(i), type, jsonDeserializationContext);
                            if (!(objectArray[i] instanceof TextComponent) || !(textComponent = (TextComponent)objectArray[i]).getStyle().isEmpty() || !textComponent.getSiblings().isEmpty()) continue;
                            objectArray[i] = textComponent.getText();
                        }
                        TranslatableComponent translatableComponent = new TranslatableComponent((String)object, objectArray);
                    } else {
                        TranslatableComponent translatableComponent = new TranslatableComponent((String)object);
                    }
                } else if (jsonObject.has("score")) {
                    object = GsonHelper.getAsJsonObject(jsonObject, "score");
                    if (!object.has("name") || !object.has("objective")) throw new JsonParseException("A score component needs a least a name and an objective");
                    ScoreComponent scoreComponent = new ScoreComponent(GsonHelper.getAsString((JsonObject)object, "name"), GsonHelper.getAsString((JsonObject)object, "objective"));
                } else if (jsonObject.has("selector")) {
                    object = this.parseSeparator(type, jsonDeserializationContext, jsonObject);
                    SelectorComponent selectorComponent = new SelectorComponent(GsonHelper.getAsString(jsonObject, "selector"), (Optional<Component>)object);
                } else if (jsonObject.has("keybind")) {
                    KeybindComponent keybindComponent = new KeybindComponent(GsonHelper.getAsString(jsonObject, "keybind"));
                } else {
                    if (!jsonObject.has("nbt")) throw new JsonParseException("Don't know how to turn " + jsonElement + " into a Component");
                    object = GsonHelper.getAsString(jsonObject, "nbt");
                    Optional<Component> optional = this.parseSeparator(type, jsonDeserializationContext, jsonObject);
                    boolean bl = GsonHelper.getAsBoolean(jsonObject, "interpret", false);
                    if (jsonObject.has("block")) {
                        NbtComponent.BlockNbtComponent blockNbtComponent = new NbtComponent.BlockNbtComponent((String)object, bl, GsonHelper.getAsString(jsonObject, "block"), optional);
                    } else if (jsonObject.has("entity")) {
                        NbtComponent.EntityNbtComponent entityNbtComponent = new NbtComponent.EntityNbtComponent((String)object, bl, GsonHelper.getAsString(jsonObject, "entity"), optional);
                    } else {
                        if (!jsonObject.has("storage")) throw new JsonParseException("Don't know how to turn " + jsonElement + " into a Component");
                        NbtComponent.StorageNbtComponent storageNbtComponent = new NbtComponent.StorageNbtComponent((String)object, bl, new ResourceLocation(GsonHelper.getAsString(jsonObject, "storage")), optional);
                    }
                }
                if (jsonObject.has("extra")) {
                    void var7_27;
                    object = GsonHelper.getAsJsonArray(jsonObject, "extra");
                    if (object.size() <= 0) throw new JsonParseException("Unexpected empty array of components");
                    boolean bl = false;
                    while (var7_27 < object.size()) {
                        var5_17.append(this.deserialize(object.get((int)var7_27), type, jsonDeserializationContext));
                        ++var7_27;
                    }
                }
                var5_17.setStyle((Style)jsonDeserializationContext.deserialize(jsonElement, Style.class));
                return var5_17;
            }
            if (!jsonElement.isJsonArray()) throw new JsonParseException("Don't know how to turn " + jsonElement + " into a Component");
            JsonArray jsonArray = jsonElement.getAsJsonArray();
            Object var5_18 = null;
            for (JsonElement jsonElement2 : jsonArray) {
                MutableComponent mutableComponent = this.deserialize(jsonElement2, jsonElement2.getClass(), jsonDeserializationContext);
                if (var5_19 == null) {
                    MutableComponent mutableComponent2 = mutableComponent;
                    continue;
                }
                var5_19.append(mutableComponent);
            }
            return var5_19;
        }

        private Optional<Component> parseSeparator(Type type, JsonDeserializationContext jsonDeserializationContext, JsonObject jsonObject) {
            if (jsonObject.has("separator")) {
                return Optional.of(this.deserialize(jsonObject.get("separator"), type, jsonDeserializationContext));
            }
            return Optional.empty();
        }

        private void serializeStyle(Style style, JsonObject jsonObject, JsonSerializationContext jsonSerializationContext) {
            JsonElement jsonElement = jsonSerializationContext.serialize((Object)style);
            if (jsonElement.isJsonObject()) {
                JsonObject jsonObject2 = (JsonObject)jsonElement;
                for (Map.Entry entry : jsonObject2.entrySet()) {
                    jsonObject.add((String)entry.getKey(), (JsonElement)entry.getValue());
                }
            }
        }

        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        public JsonElement serialize(Component component, Type type, JsonSerializationContext jsonSerializationContext) {
            Object object;
            JsonObject jsonObject = new JsonObject();
            if (!component.getStyle().isEmpty()) {
                this.serializeStyle(component.getStyle(), jsonObject, jsonSerializationContext);
            }
            if (!component.getSiblings().isEmpty()) {
                object = new JsonArray();
                for (Component component2 : component.getSiblings()) {
                    object.add(this.serialize(component2, component2.getClass(), jsonSerializationContext));
                }
                jsonObject.add("extra", (JsonElement)object);
            }
            if (component instanceof TextComponent) {
                jsonObject.addProperty("text", ((TextComponent)component).getText());
                return jsonObject;
            } else if (component instanceof TranslatableComponent) {
                object = (TranslatableComponent)component;
                jsonObject.addProperty("translate", ((TranslatableComponent)object).getKey());
                if (((TranslatableComponent)object).getArgs() == null || ((TranslatableComponent)object).getArgs().length <= 0) return jsonObject;
                var6_6 = new JsonArray();
                for (Object object2 : ((TranslatableComponent)object).getArgs()) {
                    if (object2 instanceof Component) {
                        var6_6.add(this.serialize((Component)object2, object2.getClass(), jsonSerializationContext));
                        continue;
                    }
                    var6_6.add((JsonElement)new JsonPrimitive(String.valueOf(object2)));
                }
                jsonObject.add("with", (JsonElement)var6_6);
                return jsonObject;
            } else if (component instanceof ScoreComponent) {
                object = (ScoreComponent)component;
                var6_6 = new JsonObject();
                var6_6.addProperty("name", ((ScoreComponent)object).getName());
                var6_6.addProperty("objective", ((ScoreComponent)object).getObjective());
                jsonObject.add("score", (JsonElement)var6_6);
                return jsonObject;
            } else if (component instanceof SelectorComponent) {
                object = (SelectorComponent)component;
                jsonObject.addProperty("selector", ((SelectorComponent)object).getPattern());
                this.serializeSeparator(jsonSerializationContext, jsonObject, ((SelectorComponent)object).getSeparator());
                return jsonObject;
            } else if (component instanceof KeybindComponent) {
                object = (KeybindComponent)component;
                jsonObject.addProperty("keybind", ((KeybindComponent)object).getName());
                return jsonObject;
            } else {
                if (!(component instanceof NbtComponent)) throw new IllegalArgumentException("Don't know how to serialize " + component + " as a Component");
                object = (NbtComponent)component;
                jsonObject.addProperty("nbt", ((NbtComponent)object).getNbtPath());
                jsonObject.addProperty("interpret", Boolean.valueOf(((NbtComponent)object).isInterpreting()));
                this.serializeSeparator(jsonSerializationContext, jsonObject, ((NbtComponent)object).separator);
                if (component instanceof NbtComponent.BlockNbtComponent) {
                    var6_6 = (NbtComponent.BlockNbtComponent)component;
                    jsonObject.addProperty("block", ((NbtComponent.BlockNbtComponent)var6_6).getPos());
                    return jsonObject;
                } else if (component instanceof NbtComponent.EntityNbtComponent) {
                    var6_6 = (NbtComponent.EntityNbtComponent)component;
                    jsonObject.addProperty("entity", ((NbtComponent.EntityNbtComponent)var6_6).getSelector());
                    return jsonObject;
                } else {
                    if (!(component instanceof NbtComponent.StorageNbtComponent)) throw new IllegalArgumentException("Don't know how to serialize " + component + " as a Component");
                    var6_6 = (NbtComponent.StorageNbtComponent)component;
                    jsonObject.addProperty("storage", ((NbtComponent.StorageNbtComponent)var6_6).getId().toString());
                }
            }
            return jsonObject;
        }

        private void serializeSeparator(JsonSerializationContext jsonSerializationContext, JsonObject jsonObject, Optional<Component> optional) {
            optional.ifPresent(component -> jsonObject.add("separator", this.serialize((Component)component, (Type)component.getClass(), jsonSerializationContext)));
        }

        public static String toJson(Component component) {
            return GSON.toJson((Object)component);
        }

        public static JsonElement toJsonTree(Component component) {
            return GSON.toJsonTree((Object)component);
        }

        @Nullable
        public static MutableComponent fromJson(String string) {
            return GsonHelper.fromJson(GSON, string, MutableComponent.class, false);
        }

        @Nullable
        public static MutableComponent fromJson(JsonElement jsonElement) {
            return (MutableComponent)GSON.fromJson(jsonElement, MutableComponent.class);
        }

        @Nullable
        public static MutableComponent fromJsonLenient(String string) {
            return GsonHelper.fromJson(GSON, string, MutableComponent.class, true);
        }

        public static MutableComponent fromJson(com.mojang.brigadier.StringReader stringReader) {
            try {
                JsonReader jsonReader = new JsonReader((Reader)new StringReader(stringReader.getRemaining()));
                jsonReader.setLenient(false);
                MutableComponent mutableComponent = (MutableComponent)GSON.getAdapter(MutableComponent.class).read(jsonReader);
                stringReader.setCursor(stringReader.getCursor() + Serializer.getPos(jsonReader));
                return mutableComponent;
            }
            catch (IOException | StackOverflowError throwable) {
                throw new JsonParseException(throwable);
            }
        }

        private static int getPos(JsonReader jsonReader) {
            try {
                return JSON_READER_POS.getInt(jsonReader) - JSON_READER_LINESTART.getInt(jsonReader) + 1;
            }
            catch (IllegalAccessException illegalAccessException) {
                throw new IllegalStateException("Couldn't read position of JsonReader", illegalAccessException);
            }
        }

        public /* synthetic */ JsonElement serialize(Object object, Type type, JsonSerializationContext jsonSerializationContext) {
            return this.serialize((Component)object, type, jsonSerializationContext);
        }

        public /* synthetic */ Object deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return this.deserialize(jsonElement, type, jsonDeserializationContext);
        }
    }
}

