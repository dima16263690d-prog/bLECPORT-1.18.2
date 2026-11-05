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
import javax.annotation.Nullable;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;

public class DynamicTexture
extends AbstractTexture {
    private static final Logger LOGGER = LogUtils.getLogger();
    @Nullable
    private NativeImage pixels;

    public DynamicTexture(NativeImage nativeImage) {
        this.pixels = nativeImage;
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> {
                TextureUtil.prepareImage(this.getId(), this.pixels.getWidth(), this.pixels.getHeight());
                this.upload();
            });
        } else {
            TextureUtil.prepareImage(this.getId(), this.pixels.getWidth(), this.pixels.getHeight());
            this.upload();
        }
    }

    public DynamicTexture(int n, int n2, boolean bl) {
        RenderSystem.assertOnGameThreadOrInit();
        this.pixels = new NativeImage(n, n2, bl);
        TextureUtil.prepareImage(this.getId(), this.pixels.getWidth(), this.pixels.getHeight());
    }

    @Override
    public void load(ResourceManager resourceManager) {
    }

    public void upload() {
        if (this.pixels != null) {
            this.bind();
            this.pixels.upload(0, 0, 0, false);
        } else {
            LOGGER.warn("Trying to upload disposed texture {}", (Object)this.getId());
        }
    }

    @Nullable
    public NativeImage getPixels() {
        return this.pixels;
    }

    public void setPixels(NativeImage nativeImage) {
        if (this.pixels != null) {
            this.pixels.close();
        }
        this.pixels = nativeImage;
    }

    @Override
    public void close() {
        if (this.pixels != null) {
            this.pixels.close();
            this.releaseId();
            this.pixels = null;
        }
    }
}

