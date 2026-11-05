/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.google.common.collect.Sets
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.client.resources.language;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.stream.Stream;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.client.resources.metadata.language.LanguageMetadataSection;
import net.minecraft.locale.Language;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.slf4j.Logger;

public class LanguageManager
implements ResourceManagerReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String DEFAULT_LANGUAGE_CODE = "en_us";
    private static final LanguageInfo DEFAULT_LANGUAGE = new LanguageInfo("en_us", "US", "English", false);
    private Map<String, LanguageInfo> languages = ImmutableMap.of((Object)"en_us", (Object)DEFAULT_LANGUAGE);
    private String currentCode;
    private LanguageInfo currentLanguage = DEFAULT_LANGUAGE;

    public LanguageManager(String string) {
        this.currentCode = string;
    }

    private static Map<String, LanguageInfo> extractLanguages(Stream<PackResources> stream) {
        HashMap hashMap = Maps.newHashMap();
        stream.forEach(packResources -> {
            try {
                LanguageMetadataSection languageMetadataSection = packResources.getMetadataSection(LanguageMetadataSection.SERIALIZER);
                if (languageMetadataSection != null) {
                    for (LanguageInfo languageInfo : languageMetadataSection.getLanguages()) {
                        hashMap.putIfAbsent(languageInfo.getCode(), languageInfo);
                    }
                }
            }
            catch (IOException | RuntimeException exception) {
                LOGGER.warn("Unable to parse language metadata section of resourcepack: {}", (Object)packResources.getName(), (Object)exception);
            }
        });
        return ImmutableMap.copyOf((Map)hashMap);
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        this.languages = LanguageManager.extractLanguages(resourceManager.listPacks());
        LanguageInfo languageInfo = this.languages.getOrDefault(DEFAULT_LANGUAGE_CODE, DEFAULT_LANGUAGE);
        this.currentLanguage = this.languages.getOrDefault(this.currentCode, languageInfo);
        ArrayList arrayList = Lists.newArrayList((Object[])new LanguageInfo[]{languageInfo});
        if (this.currentLanguage != languageInfo) {
            arrayList.add(this.currentLanguage);
        }
        ClientLanguage clientLanguage = ClientLanguage.loadFrom(resourceManager, arrayList);
        I18n.setLanguage(clientLanguage);
        Language.inject(clientLanguage);
    }

    public void setSelected(LanguageInfo languageInfo) {
        this.currentCode = languageInfo.getCode();
        this.currentLanguage = languageInfo;
    }

    public LanguageInfo getSelected() {
        return this.currentLanguage;
    }

    public SortedSet<LanguageInfo> getLanguages() {
        return Sets.newTreeSet(this.languages.values());
    }

    public LanguageInfo getLanguage(String string) {
        return this.languages.get(string);
    }
}

