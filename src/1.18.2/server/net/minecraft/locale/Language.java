/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.google.gson.Gson
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParseException
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.locale;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.StringDecomposer;
import org.slf4j.Logger;

public abstract class Language {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new Gson();
    private static final Pattern UNSUPPORTED_FORMAT_PATTERN = Pattern.compile("%(\\d+\\$)?[\\d.]*[df]");
    public static final String DEFAULT = "en_us";
    private static volatile Language instance = Language.loadDefault();

    private static Language loadDefault() {
        InputStream inputStream;
        ImmutableMap.Builder builder = ImmutableMap.builder();
        BiConsumer<String, String> biConsumer = (arg_0, arg_1) -> ((ImmutableMap.Builder)builder).put(arg_0, arg_1);
        String string = "/assets/minecraft/lang/en_us.json";
        try {
            inputStream = Language.class.getResourceAsStream("/assets/minecraft/lang/en_us.json");
            try {
                Language.loadFromJson(inputStream, biConsumer);
            }
            finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        }
        catch (JsonParseException | IOException throwable) {
            LOGGER.error("Couldn't read strings from {}", (Object)"/assets/minecraft/lang/en_us.json", (Object)throwable);
        }
        inputStream = builder.build();
        return new Language((Map)((Object)inputStream)){
            final /* synthetic */ Map val$storage;
            {
                this.val$storage = map;
            }

            @Override
            public String getOrDefault(String string) {
                return this.val$storage.getOrDefault(string, string);
            }

            @Override
            public boolean has(String string) {
                return this.val$storage.containsKey(string);
            }

            @Override
            public boolean isDefaultRightToLeft() {
                return false;
            }

            @Override
            public FormattedCharSequence getVisualOrder(FormattedText formattedText) {
                return formattedCharSink -> formattedText.visit((style, string) -> StringDecomposer.iterateFormatted(string, style, formattedCharSink) ? Optional.empty() : FormattedText.STOP_ITERATION, Style.EMPTY).isPresent();
            }
        };
    }

    public static void loadFromJson(InputStream inputStream, BiConsumer<String, String> biConsumer) {
        JsonObject jsonObject = (JsonObject)GSON.fromJson((Reader)new InputStreamReader(inputStream, StandardCharsets.UTF_8), JsonObject.class);
        for (Map.Entry entry : jsonObject.entrySet()) {
            String string = UNSUPPORTED_FORMAT_PATTERN.matcher(GsonHelper.convertToString((JsonElement)entry.getValue(), (String)entry.getKey())).replaceAll("%$1s");
            biConsumer.accept((String)entry.getKey(), string);
        }
    }

    public static Language getInstance() {
        return instance;
    }

    public static void inject(Language language) {
        instance = language;
    }

    public abstract String getOrDefault(String var1);

    public abstract boolean has(String var1);

    public abstract boolean isDefaultRightToLeft();

    public abstract FormattedCharSequence getVisualOrder(FormattedText var1);

    public List<FormattedCharSequence> getVisualOrder(List<FormattedText> list) {
        return (List)list.stream().map(this::getVisualOrder).collect(ImmutableList.toImmutableList());
    }
}

