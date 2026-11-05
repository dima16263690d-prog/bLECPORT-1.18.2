/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  javax.annotation.Nullable
 */
package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.datafixers.util.Pair;
import java.nio.ByteBuffer;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;

public class BufferUploader {
    private static int lastVertexArrayObject;
    private static int lastVertexBufferObject;
    private static int lastIndexBufferObject;
    @Nullable
    private static VertexFormat lastFormat;

    public static void reset() {
        if (lastFormat != null) {
            lastFormat.clearBufferState();
            lastFormat = null;
        }
        GlStateManager._glBindBuffer(34963, 0);
        lastIndexBufferObject = 0;
        GlStateManager._glBindBuffer(34962, 0);
        lastVertexBufferObject = 0;
        GlStateManager._glBindVertexArray(0);
        lastVertexArrayObject = 0;
    }

    public static void invalidateElementArrayBufferBinding() {
        GlStateManager._glBindBuffer(34963, 0);
        lastIndexBufferObject = 0;
    }

    public static void end(BufferBuilder bufferBuilder) {
        if (!RenderSystem.isOnRenderThreadOrInit()) {
            RenderSystem.recordRenderCall(() -> {
                Pair<BufferBuilder.DrawState, ByteBuffer> pair = bufferBuilder.popNextBuffer();
                BufferBuilder.DrawState drawState = (BufferBuilder.DrawState)pair.getFirst();
                BufferUploader._end((ByteBuffer)pair.getSecond(), drawState.mode(), drawState.format(), drawState.vertexCount(), drawState.indexType(), drawState.indexCount(), drawState.sequentialIndex());
            });
        } else {
            Pair<BufferBuilder.DrawState, ByteBuffer> pair = bufferBuilder.popNextBuffer();
            BufferBuilder.DrawState drawState = (BufferBuilder.DrawState)pair.getFirst();
            BufferUploader._end((ByteBuffer)pair.getSecond(), drawState.mode(), drawState.format(), drawState.vertexCount(), drawState.indexType(), drawState.indexCount(), drawState.sequentialIndex());
        }
    }

    private static void _end(ByteBuffer byteBuffer, VertexFormat.Mode mode, VertexFormat vertexFormat, int n, VertexFormat.IndexType indexType, int n2, boolean bl) {
        int n3;
        int n4;
        Object object;
        RenderSystem.assertOnRenderThread();
        byteBuffer.clear();
        if (n <= 0) {
            return;
        }
        int n5 = n * vertexFormat.getVertexSize();
        BufferUploader.updateVertexSetup(vertexFormat);
        byteBuffer.position(0);
        byteBuffer.limit(n5);
        GlStateManager._glBufferData(34962, byteBuffer, 35048);
        if (bl) {
            object = RenderSystem.getSequentialBuffer(mode, n2);
            n4 = ((RenderSystem.AutoStorageIndexBuffer)object).name();
            if (n4 != lastIndexBufferObject) {
                GlStateManager._glBindBuffer(34963, n4);
                lastIndexBufferObject = n4;
            }
            n3 = ((RenderSystem.AutoStorageIndexBuffer)object).type().asGLType;
        } else {
            int n6 = vertexFormat.getOrCreateIndexBufferObject();
            if (n6 != lastIndexBufferObject) {
                GlStateManager._glBindBuffer(34963, n6);
                lastIndexBufferObject = n6;
            }
            byteBuffer.position(n5);
            byteBuffer.limit(n5 + n2 * indexType.bytes);
            GlStateManager._glBufferData(34963, byteBuffer, 35048);
            n3 = indexType.asGLType;
        }
        object = RenderSystem.getShader();
        for (n4 = 0; n4 < 8; ++n4) {
            int n7 = RenderSystem.getShaderTexture(n4);
            ((ShaderInstance)object).setSampler("Sampler" + n4, n7);
        }
        if (((ShaderInstance)object).MODEL_VIEW_MATRIX != null) {
            ((ShaderInstance)object).MODEL_VIEW_MATRIX.set(RenderSystem.getModelViewMatrix());
        }
        if (((ShaderInstance)object).PROJECTION_MATRIX != null) {
            ((ShaderInstance)object).PROJECTION_MATRIX.set(RenderSystem.getProjectionMatrix());
        }
        if (((ShaderInstance)object).INVERSE_VIEW_ROTATION_MATRIX != null) {
            ((ShaderInstance)object).INVERSE_VIEW_ROTATION_MATRIX.set(RenderSystem.getInverseViewRotationMatrix());
        }
        if (((ShaderInstance)object).COLOR_MODULATOR != null) {
            ((ShaderInstance)object).COLOR_MODULATOR.set(RenderSystem.getShaderColor());
        }
        if (((ShaderInstance)object).FOG_START != null) {
            ((ShaderInstance)object).FOG_START.set(RenderSystem.getShaderFogStart());
        }
        if (((ShaderInstance)object).FOG_END != null) {
            ((ShaderInstance)object).FOG_END.set(RenderSystem.getShaderFogEnd());
        }
        if (((ShaderInstance)object).FOG_COLOR != null) {
            ((ShaderInstance)object).FOG_COLOR.set(RenderSystem.getShaderFogColor());
        }
        if (((ShaderInstance)object).FOG_SHAPE != null) {
            ((ShaderInstance)object).FOG_SHAPE.set(RenderSystem.getShaderFogShape().getIndex());
        }
        if (((ShaderInstance)object).TEXTURE_MATRIX != null) {
            ((ShaderInstance)object).TEXTURE_MATRIX.set(RenderSystem.getTextureMatrix());
        }
        if (((ShaderInstance)object).GAME_TIME != null) {
            ((ShaderInstance)object).GAME_TIME.set(RenderSystem.getShaderGameTime());
        }
        if (((ShaderInstance)object).SCREEN_SIZE != null) {
            Window window = Minecraft.getInstance().getWindow();
            ((ShaderInstance)object).SCREEN_SIZE.set((float)window.getWidth(), (float)window.getHeight());
        }
        if (((ShaderInstance)object).LINE_WIDTH != null && (mode == VertexFormat.Mode.LINES || mode == VertexFormat.Mode.LINE_STRIP)) {
            ((ShaderInstance)object).LINE_WIDTH.set(RenderSystem.getShaderLineWidth());
        }
        RenderSystem.setupShaderLights((ShaderInstance)object);
        ((ShaderInstance)object).apply();
        GlStateManager._drawElements(mode.asGLMode, n2, n3, 0L);
        ((ShaderInstance)object).clear();
        byteBuffer.position(0);
    }

    public static void _endInternal(BufferBuilder bufferBuilder) {
        RenderSystem.assertOnRenderThread();
        Pair<BufferBuilder.DrawState, ByteBuffer> pair = bufferBuilder.popNextBuffer();
        BufferBuilder.DrawState drawState = (BufferBuilder.DrawState)pair.getFirst();
        ByteBuffer byteBuffer = (ByteBuffer)pair.getSecond();
        VertexFormat vertexFormat = drawState.format();
        int n = drawState.vertexCount();
        byteBuffer.clear();
        if (n <= 0) {
            return;
        }
        int n2 = n * vertexFormat.getVertexSize();
        BufferUploader.updateVertexSetup(vertexFormat);
        byteBuffer.position(0);
        byteBuffer.limit(n2);
        GlStateManager._glBufferData(34962, byteBuffer, 35048);
        RenderSystem.AutoStorageIndexBuffer autoStorageIndexBuffer = RenderSystem.getSequentialBuffer(drawState.mode(), drawState.indexCount());
        int n3 = autoStorageIndexBuffer.name();
        if (n3 != lastIndexBufferObject) {
            GlStateManager._glBindBuffer(34963, n3);
            lastIndexBufferObject = n3;
        }
        int n4 = autoStorageIndexBuffer.type().asGLType;
        GlStateManager._drawElements(drawState.mode().asGLMode, drawState.indexCount(), n4, 0L);
        byteBuffer.position(0);
    }

    private static void updateVertexSetup(VertexFormat vertexFormat) {
        boolean bl;
        int n = vertexFormat.getOrCreateVertexArrayObject();
        int n2 = vertexFormat.getOrCreateVertexBufferObject();
        boolean bl2 = bl = vertexFormat != lastFormat;
        if (bl) {
            BufferUploader.reset();
        }
        if (n != lastVertexArrayObject) {
            GlStateManager._glBindVertexArray(n);
            lastVertexArrayObject = n;
        }
        if (n2 != lastVertexBufferObject) {
            GlStateManager._glBindBuffer(34962, n2);
            lastVertexBufferObject = n2;
        }
        if (bl) {
            vertexFormat.setupBufferState();
            lastFormat = vertexFormat;
        }
    }
}

