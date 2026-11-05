/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.resources.metadata.texture;

import net.minecraft.client.resources.metadata.texture.TextureMetadataSectionSerializer;

public class TextureMetadataSection {
    public static final TextureMetadataSectionSerializer SERIALIZER = new TextureMetadataSectionSerializer();
    public static final boolean DEFAULT_BLUR = false;
    public static final boolean DEFAULT_CLAMP = false;
    private final boolean blur;
    private final boolean clamp;

    public TextureMetadataSection(boolean bl, boolean bl2) {
        this.blur = bl;
        this.clamp = bl2;
    }

    public boolean isBlur() {
        return this.blur;
    }

    public boolean isClamp() {
        return this.clamp;
    }
}

