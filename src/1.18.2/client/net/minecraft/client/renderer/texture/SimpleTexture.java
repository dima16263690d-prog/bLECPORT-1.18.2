/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.io.Closeable;
import java.io.IOException;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;

public class SimpleTexture
extends AbstractTexture {
    static final Logger LOGGER = LogUtils.getLogger();
    protected final ResourceLocation location;

    public SimpleTexture(ResourceLocation resourceLocation) {
        this.location = resourceLocation;
    }

    @Override
    public void load(ResourceManager resourceManager) throws IOException {
        boolean bl;
        boolean bl2;
        TextureImage textureImage = this.getTextureImage(resourceManager);
        textureImage.throwIfError();
        TextureMetadataSection textureMetadataSection = textureImage.getTextureMetadata();
        if (textureMetadataSection != null) {
            bl2 = textureMetadataSection.isBlur();
            bl = textureMetadataSection.isClamp();
        } else {
            bl2 = false;
            bl = false;
        }
        NativeImage nativeImage = textureImage.getImage();
        if (!RenderSystem.isOnRenderThreadOrInit()) {
            RenderSystem.recordRenderCall(() -> this.doLoad(nativeImage, bl2, bl));
        } else {
            this.doLoad(nativeImage, bl2, bl);
        }
    }

    private void doLoad(NativeImage nativeImage, boolean bl, boolean bl2) {
        TextureUtil.prepareImage(this.getId(), 0, nativeImage.getWidth(), nativeImage.getHeight());
        nativeImage.upload(0, 0, 0, 0, 0, nativeImage.getWidth(), nativeImage.getHeight(), bl, bl2, false, true);
    }

    protected TextureImage getTextureImage(ResourceManager resourceManager) {
        return TextureImage.load(resourceManager, this.location);
    }

    protected static class TextureImage
    implements Closeable {
        @Nullable
        private final TextureMetadataSection metadata;
        @Nullable
        private final NativeImage image;
        @Nullable
        private final IOException exception;

        public TextureImage(IOException iOException) {
            this.exception = iOException;
            this.metadata = null;
            this.image = null;
        }

        public TextureImage(@Nullable TextureMetadataSection textureMetadataSection, NativeImage nativeImage) {
            this.exception = null;
            this.metadata = textureMetadataSection;
            this.image = nativeImage;
        }

        public static TextureImage load(ResourceManager resourceManager, ResourceLocation resourceLocation) {
            TextureImage textureImage;
            block10: {
                Resource resource = resourceManager.getResource(resourceLocation);
                try {
                    NativeImage nativeImage = NativeImage.read(resource.getInputStream());
                    TextureMetadataSection textureMetadataSection = null;
                    try {
                        textureMetadataSection = resource.getMetadata(TextureMetadataSection.SERIALIZER);
                    }
                    catch (RuntimeException runtimeException) {
                        LOGGER.warn("Failed reading metadata of: {}", (Object)resourceLocation, (Object)runtimeException);
                    }
                    textureImage = new TextureImage(textureMetadataSection, nativeImage);
                    if (resource == null) break block10;
                }
                catch (Throwable throwable) {
                    try {
                        if (resource != null) {
                            try {
                                resource.close();
                            }
                            catch (Throwable throwable2) {
                                throwable.addSuppressed(throwable2);
                            }
                        }
                        throw throwable;
                    }
                    catch (IOException iOException) {
                        return new TextureImage(iOException);
                    }
                }
                resource.close();
            }
            return textureImage;
        }

        @Nullable
        public TextureMetadataSection getTextureMetadata() {
            return this.metadata;
        }

        public NativeImage getImage() throws IOException {
            if (this.exception != null) {
                throw this.exception;
            }
            return this.image;
        }

        @Override
        public void close() {
            if (this.image != null) {
                this.image.close();
            }
        }

        public void throwIfError() throws IOException {
            if (this.exception != null) {
                throw this.exception;
            }
        }
    }
}

