/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.resources.metadata.language;

import java.util.Collection;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.client.resources.metadata.language.LanguageMetadataSectionSerializer;

public class LanguageMetadataSection {
    public static final LanguageMetadataSectionSerializer SERIALIZER = new LanguageMetadataSectionSerializer();
    public static final boolean DEFAULT_BIDIRECTIONAL = false;
    private final Collection<LanguageInfo> languages;

    public LanguageMetadataSection(Collection<LanguageInfo> collection) {
        this.languages = collection;
    }

    public Collection<LanguageInfo> getLanguages() {
        return this.languages;
    }
}

