/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.blaze3d.pipeline;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;

public abstract class RenderTarget {
    private static final int RED_CHANNEL = 0;
    private static final int GREEN_CHANNEL = 1;
    private static final int BLUE_CHANNEL = 2;
    private static final int ALPHA_CHANNEL = 3;
    public int width;
    public int height;
    public int viewWidth;
    public int viewHeight;
    public final boolean useDepth;
    public int frameBufferId;
    protected int colorTextureId;
    protected int depthBufferId;
    private final float[] clearChannels = Util.make(() -> {
        float[] fArray = new float[]{1.0f, 1.0f, 1.0f, 0.0f};
        return fArray;
    });
    public int filterMode;

    public RenderTarget(boolean bl) {
        this.useDepth = bl;
        this.frameBufferId = -1;
        this.colorTextureId = -1;
        this.depthBufferId = -1;
    }

    public void resize(int n, int n2, boolean bl) {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> this._resize(n, n2, bl));
        } else {
            this._resize(n, n2, bl);
        }
    }

    private void _resize(int n, int n2, boolean bl) {
        RenderSystem.assertOnRenderThreadOrInit();
        GlStateManager._enableDepthTest();
        if (this.frameBufferId >= 0) {
            this.destroyBuffers();
        }
        this.createBuffers(n, n2, bl);
        GlStateManager._glBindFramebuffer(36160, 0);
    }

    public void destroyBuffers() {
        RenderSystem.assertOnRenderThreadOrInit();
        this.unbindRead();
        this.unbindWrite();
        if (this.depthBufferId > -1) {
            TextureUtil.releaseTextureId(this.depthBufferId);
            this.depthBufferId = -1;
        }
        if (this.colorTextureId > -1) {
            TextureUtil.releaseTextureId(this.colorTextureId);
            this.colorTextureId = -1;
        }
        if (this.frameBufferId > -1) {
            GlStateManager._glBindFramebuffer(36160, 0);
            GlStateManager._glDeleteFramebuffers(this.frameBufferId);
            this.frameBufferId = -1;
        }
    }

    public void copyDepthFrom(RenderTarget renderTarget) {
        RenderSystem.assertOnRenderThreadOrInit();
        GlStateManager._glBindFramebuffer(36008, renderTarget.frameBufferId);
        GlStateManager._glBindFramebuffer(36009, this.frameBufferId);
        GlStateManager._glBlitFrameBuffer(0, 0, renderTarget.width, renderTarget.height, 0, 0, this.width, this.height, 256, 9728);
        GlStateManager._glBindFramebuffer(36160, 0);
    }

    public void createBuffers(int n, int n2, boolean bl) {
        RenderSystem.assertOnRenderThreadOrInit();
        int n3 = RenderSystem.maxSupportedTextureSize();
        if (n <= 0 || n > n3 || n2 <= 0 || n2 > n3) {
            throw new IllegalArgumentException("Window " + n + "x" + n2 + " size out of bounds (max. size: " + n3 + ")");
        }
        this.viewWidth = n;
        this.viewHeight = n2;
        this.width = n;
        this.height = n2;
        this.frameBufferId = GlStateManager.glGenFramebuffers();
        this.colorTextureId = TextureUtil.generateTextureId();
        if (this.useDepth) {
            this.depthBufferId = TextureUtil.generateTextureId();
            GlStateManager._bindTexture(this.depthBufferId);
            GlStateManager._texParameter(3553, 10241, 9728);
            GlStateManager._texParameter(3553, 10240, 9728);
            GlStateManager._texParameter(3553, 34892, 0);
            GlStateManager._texParameter(3553, 10242, 33071);
            GlStateManager._texParameter(3553, 10243, 33071);
            GlStateManager._texImage2D(3553, 0, 6402, this.width, this.height, 0, 6402, 5126, null);
        }
        this.setFilterMode(9728);
        GlStateManager._bindTexture(this.colorTextureId);
        GlStateManager._texParameter(3553, 10242, 33071);
        GlStateManager._texParameter(3553, 10243, 33071);
        GlStateManager._texImage2D(3553, 0, 32856, this.width, this.height, 0, 6408, 5121, null);
        GlStateManager._glBindFramebuffer(36160, this.frameBufferId);
        GlStateManager._glFramebufferTexture2D(36160, 36064, 3553, this.colorTextureId, 0);
        if (this.useDepth) {
            GlStateManager._glFramebufferTexture2D(36160, 36096, 3553, this.depthBufferId, 0);
        }
        this.checkStatus();
        this.clear(bl);
        this.unbindRead();
    }

    public void setFilterMode(int n) {
        RenderSystem.assertOnRenderThreadOrInit();
        this.filterMode = n;
        GlStateManager._bindTexture(this.colorTextureId);
        GlStateManager._texParameter(3553, 10241, n);
        GlStateManager._texParameter(3553, 10240, n);
        GlStateManager._bindTexture(0);
    }

    public void checkStatus() {
        RenderSystem.assertOnRenderThreadOrInit();
        int n = GlStateManager.glCheckFramebufferStatus(36160);
        if (n == 36053) {
            return;
        }
        if (n == 36054) {
            throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT");
        }
        if (n == 36055) {
            throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT");
        }
        if (n == 36059) {
            throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER");
        }
        if (n == 36060) {
            throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER");
        }
        if (n == 36061) {
            throw new RuntimeException("GL_FRAMEBUFFER_UNSUPPORTED");
        }
        if (n == 1285) {
            throw new RuntimeException("GL_OUT_OF_MEMORY");
        }
        throw new RuntimeException("glCheckFramebufferStatus returned unknown status:" + n);
    }

    public void bindRead() {
        RenderSystem.assertOnRenderThread();
        GlStateManager._bindTexture(this.colorTextureId);
    }

    public void unbindRead() {
        RenderSystem.assertOnRenderThreadOrInit();
        GlStateManager._bindTexture(0);
    }

    public void bindWrite(boolean bl) {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> this._bindWrite(bl));
        } else {
            this._bindWrite(bl);
        }
    }

    private void _bindWrite(boolean bl) {
        RenderSystem.assertOnRenderThreadOrInit();
        GlStateManager._glBindFramebuffer(36160, this.frameBufferId);
        if (bl) {
            GlStateManager._viewport(0, 0, this.viewWidth, this.viewHeight);
        }
    }

    public void unbindWrite() {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> GlStateManager._glBindFramebuffer(36160, 0));
        } else {
            GlStateManager._glBindFramebuffer(36160, 0);
        }
    }

    public void setClearColor(float f, float f2, float f3, float f4) {
        this.clearChannels[0] = f;
        this.clearChannels[1] = f2;
        this.clearChannels[2] = f3;
        this.clearChannels[3] = f4;
    }

    public void blitToScreen(int n, int n2) {
        this.blitToScreen(n, n2, true);
    }

    public void blitToScreen(int n, int n2, boolean bl) {
        RenderSystem.assertOnGameThreadOrInit();
        if (!RenderSystem.isInInitPhase()) {
            RenderSystem.recordRenderCall(() -> this._blitToScreen(n, n2, bl));
        } else {
            this._blitToScreen(n, n2, bl);
        }
    }

    private void _blitToScreen(int n, int n2, boolean bl) {
        RenderSystem.assertOnRenderThread();
        GlStateManager._colorMask(true, true, true, false);
        GlStateManager._disableDepthTest();
        GlStateManager._depthMask(false);
        GlStateManager._viewport(0, 0, n, n2);
        if (bl) {
            GlStateManager._disableBlend();
        }
        Minecraft minecraft = Minecraft.getInstance();
        ShaderInstance shaderInstance = minecraft.gameRenderer.blitShader;
        shaderInstance.setSampler("DiffuseSampler", this.colorTextureId);
        Matrix4f matrix4f = Matrix4f.orthographic(n, -n2, 1000.0f, 3000.0f);
        RenderSystem.setProjectionMatrix(matrix4f);
        if (shaderInstance.MODEL_VIEW_MATRIX != null) {
            shaderInstance.MODEL_VIEW_MATRIX.set(Matrix4f.createTranslateMatrix(0.0f, 0.0f, -2000.0f));
        }
        if (shaderInstance.PROJECTION_MATRIX != null) {
            shaderInstance.PROJECTION_MATRIX.set(matrix4f);
        }
        shaderInstance.apply();
        float f = n;
        float f2 = n2;
        float f3 = (float)this.viewWidth / (float)this.width;
        float f4 = (float)this.viewHeight / (float)this.height;
        Tesselator tesselator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferBuilder.vertex(0.0, f2, 0.0).uv(0.0f, 0.0f).color(255, 255, 255, 255).endVertex();
        bufferBuilder.vertex(f, f2, 0.0).uv(f3, 0.0f).color(255, 255, 255, 255).endVertex();
        bufferBuilder.vertex(f, 0.0, 0.0).uv(f3, f4).color(255, 255, 255, 255).endVertex();
        bufferBuilder.vertex(0.0, 0.0, 0.0).uv(0.0f, f4).color(255, 255, 255, 255).endVertex();
        bufferBuilder.end();
        BufferUploader._endInternal(bufferBuilder);
        shaderInstance.clear();
        GlStateManager._depthMask(true);
        GlStateManager._colorMask(true, true, true, true);
    }

    public void clear(boolean bl) {
        RenderSystem.assertOnRenderThreadOrInit();
        this.bindWrite(true);
        GlStateManager._clearColor(this.clearChannels[0], this.clearChannels[1], this.clearChannels[2], this.clearChannels[3]);
        int n = 16384;
        if (this.useDepth) {
            GlStateManager._clearDepth(1.0);
            n |= 0x100;
        }
        GlStateManager._clear(n, bl);
        this.unbindWrite();
    }

    public int getColorTextureId() {
        return this.colorTextureId;
    }

    public int getDepthTextureId() {
        return this.depthBufferId;
    }
}

