/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

public class CubeMap {
    private static final int SIDES = 6;
    private final ResourceLocation[] images = new ResourceLocation[6];

    public CubeMap(ResourceLocation resourceLocation) {
        for (int i = 0; i < 6; ++i) {
            this.images[i] = new ResourceLocation(resourceLocation.getNamespace(), resourceLocation.getPath() + "_" + i + ".png");
        }
    }

    public void render(Minecraft minecraft, float f, float f2, float f3) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        Matrix4f matrix4f = Matrix4f.perspective(85.0, (float)minecraft.getWindow().getWidth() / (float)minecraft.getWindow().getHeight(), 0.05f, 10.0f);
        RenderSystem.backupProjectionMatrix();
        RenderSystem.setProjectionMatrix(matrix4f);
        PoseStack poseStack = RenderSystem.getModelViewStack();
        poseStack.pushPose();
        poseStack.setIdentity();
        poseStack.mulPose(Vector3f.XP.rotationDegrees(180.0f));
        RenderSystem.applyModelViewMatrix();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
        RenderSystem.defaultBlendFunc();
        int n = 2;
        for (int i = 0; i < 4; ++i) {
            poseStack.pushPose();
            float f4 = ((float)(i % 2) / 2.0f - 0.5f) / 256.0f;
            float f5 = ((float)(i / 2) / 2.0f - 0.5f) / 256.0f;
            float f6 = 0.0f;
            poseStack.translate(f4, f5, 0.0);
            poseStack.mulPose(Vector3f.XP.rotationDegrees(f));
            poseStack.mulPose(Vector3f.YP.rotationDegrees(f2));
            RenderSystem.applyModelViewMatrix();
            for (int j = 0; j < 6; ++j) {
                RenderSystem.setShaderTexture(0, this.images[j]);
                bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
                int n2 = Math.round(255.0f * f3) / (i + 1);
                if (j == 0) {
                    bufferBuilder.vertex(-1.0, -1.0, 1.0).uv(0.0f, 0.0f).color(255, 255, 255, n2).endVertex();
                    bufferBuilder.vertex(-1.0, 1.0, 1.0).uv(0.0f, 1.0f).color(255, 255, 255, n2).endVertex();
                    bufferBuilder.vertex(1.0, 1.0, 1.0).uv(1.0f, 1.0f).color(255, 255, 255, n2).endVertex();
                    bufferBuilder.vertex(1.0, -1.0, 1.0).uv(1.0f, 0.0f).color(255, 255, 255, n2).endVertex();
                }
                if (j == 1) {
                    bufferBuilder.vertex(1.0, -1.0, 1.0).uv(0.0f, 0.0f).color(255, 255, 255, n2).endVertex();
                    bufferBuilder.vertex(1.0, 1.0, 1.0).uv(0.0f, 1.0f).color(255, 255, 255, n2).endVertex();
                    bufferBuilder.vertex(1.0, 1.0, -1.0).uv(1.0f, 1.0f).color(255, 255, 255, n2).endVertex();
                    bufferBuilder.vertex(1.0, -1.0, -1.0).uv(1.0f, 0.0f).color(255, 255, 255, n2).endVertex();
                }
                if (j == 2) {
                    bufferBuilder.vertex(1.0, -1.0, -1.0).uv(0.0f, 0.0f).color(255, 255, 255, n2).endVertex();
                    bufferBuilder.vertex(1.0, 1.0, -1.0).uv(0.0f, 1.0f).color(255, 255, 255, n2).endVertex();
                    bufferBuilder.vertex(-1.0, 1.0, -1.0).uv(1.0f, 1.0f).color(255, 255, 255, n2).endVertex();
                    bufferBuilder.vertex(-1.0, -1.0, -1.0).uv(1.0f, 0.0f).color(255, 255, 255, n2).endVertex();
                }
                if (j == 3) {
                    bufferBuilder.vertex(-1.0, -1.0, -1.0).uv(0.0f, 0.0f).color(255, 255, 255, n2).endVertex();
                    bufferBuilder.vertex(-1.0, 1.0, -1.0).uv(0.0f, 1.0f).color(255, 255, 255, n2).endVertex();
                    bufferBuilder.vertex(-1.0, 1.0, 1.0).uv(1.0f, 1.0f).color(255, 255, 255, n2).endVertex();
                    bufferBuilder.vertex(-1.0, -1.0, 1.0).uv(1.0f, 0.0f).color(255, 255, 255, n2).endVertex();
                }
                if (j == 4) {
                    bufferBuilder.vertex(-1.0, -1.0, -1.0).uv(0.0f, 0.0f).color(255, 255, 255, n2).endVertex();
                    bufferBuilder.vertex(-1.0, -1.0, 1.0).uv(0.0f, 1.0f).color(255, 255, 255, n2).endVertex();
                    bufferBuilder.vertex(1.0, -1.0, 1.0).uv(1.0f, 1.0f).color(255, 255, 255, n2).endVertex();
                    bufferBuilder.vertex(1.0, -1.0, -1.0).uv(1.0f, 0.0f).color(255, 255, 255, n2).endVertex();
                }
                if (j == 5) {
                    bufferBuilder.vertex(-1.0, 1.0, 1.0).uv(0.0f, 0.0f).color(255, 255, 255, n2).endVertex();
                    bufferBuilder.vertex(-1.0, 1.0, -1.0).uv(0.0f, 1.0f).color(255, 255, 255, n2).endVertex();
                    bufferBuilder.vertex(1.0, 1.0, -1.0).uv(1.0f, 1.0f).color(255, 255, 255, n2).endVertex();
                    bufferBuilder.vertex(1.0, 1.0, 1.0).uv(1.0f, 0.0f).color(255, 255, 255, n2).endVertex();
                }
                tesselator.end();
            }
            poseStack.popPose();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.colorMask(true, true, true, false);
        }
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.restoreProjectionMatrix();
        poseStack.popPose();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
    }

    public CompletableFuture<Void> preload(TextureManager textureManager, Executor executor) {
        CompletableFuture[] completableFutureArray = new CompletableFuture[6];
        for (int i = 0; i < completableFutureArray.length; ++i) {
            completableFutureArray[i] = textureManager.preload(this.images[i], executor);
        }
        return CompletableFuture.allOf(completableFutureArray);
    }
}

