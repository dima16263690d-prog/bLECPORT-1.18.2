/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;

public class ChunkBorderRenderer
implements DebugRenderer.SimpleDebugRenderer {
    private final Minecraft minecraft;
    private static final int CELL_BORDER = FastColor.ARGB32.color(255, 0, 155, 155);
    private static final int YELLOW = FastColor.ARGB32.color(255, 255, 255, 0);

    public ChunkBorderRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double d2, double d3) {
        int n;
        int n2;
        RenderSystem.enableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Entity entity = this.minecraft.gameRenderer.getMainCamera().getEntity();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        double d4 = (double)this.minecraft.level.getMinBuildHeight() - d2;
        double d5 = (double)this.minecraft.level.getMaxBuildHeight() - d2;
        RenderSystem.disableTexture();
        RenderSystem.disableBlend();
        ChunkPos chunkPos = entity.chunkPosition();
        double d6 = (double)chunkPos.getMinBlockX() - d;
        double d7 = (double)chunkPos.getMinBlockZ() - d3;
        RenderSystem.lineWidth(1.0f);
        bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        for (n2 = -16; n2 <= 32; n2 += 16) {
            for (n = -16; n <= 32; n += 16) {
                bufferBuilder.vertex(d6 + (double)n2, d4, d7 + (double)n).color(1.0f, 0.0f, 0.0f, 0.0f).endVertex();
                bufferBuilder.vertex(d6 + (double)n2, d4, d7 + (double)n).color(1.0f, 0.0f, 0.0f, 0.5f).endVertex();
                bufferBuilder.vertex(d6 + (double)n2, d5, d7 + (double)n).color(1.0f, 0.0f, 0.0f, 0.5f).endVertex();
                bufferBuilder.vertex(d6 + (double)n2, d5, d7 + (double)n).color(1.0f, 0.0f, 0.0f, 0.0f).endVertex();
            }
        }
        for (n2 = 2; n2 < 16; n2 += 2) {
            n = n2 % 4 == 0 ? CELL_BORDER : YELLOW;
            bufferBuilder.vertex(d6 + (double)n2, d4, d7).color(1.0f, 1.0f, 0.0f, 0.0f).endVertex();
            bufferBuilder.vertex(d6 + (double)n2, d4, d7).color(n).endVertex();
            bufferBuilder.vertex(d6 + (double)n2, d5, d7).color(n).endVertex();
            bufferBuilder.vertex(d6 + (double)n2, d5, d7).color(1.0f, 1.0f, 0.0f, 0.0f).endVertex();
            bufferBuilder.vertex(d6 + (double)n2, d4, d7 + 16.0).color(1.0f, 1.0f, 0.0f, 0.0f).endVertex();
            bufferBuilder.vertex(d6 + (double)n2, d4, d7 + 16.0).color(n).endVertex();
            bufferBuilder.vertex(d6 + (double)n2, d5, d7 + 16.0).color(n).endVertex();
            bufferBuilder.vertex(d6 + (double)n2, d5, d7 + 16.0).color(1.0f, 1.0f, 0.0f, 0.0f).endVertex();
        }
        for (n2 = 2; n2 < 16; n2 += 2) {
            n = n2 % 4 == 0 ? CELL_BORDER : YELLOW;
            bufferBuilder.vertex(d6, d4, d7 + (double)n2).color(1.0f, 1.0f, 0.0f, 0.0f).endVertex();
            bufferBuilder.vertex(d6, d4, d7 + (double)n2).color(n).endVertex();
            bufferBuilder.vertex(d6, d5, d7 + (double)n2).color(n).endVertex();
            bufferBuilder.vertex(d6, d5, d7 + (double)n2).color(1.0f, 1.0f, 0.0f, 0.0f).endVertex();
            bufferBuilder.vertex(d6 + 16.0, d4, d7 + (double)n2).color(1.0f, 1.0f, 0.0f, 0.0f).endVertex();
            bufferBuilder.vertex(d6 + 16.0, d4, d7 + (double)n2).color(n).endVertex();
            bufferBuilder.vertex(d6 + 16.0, d5, d7 + (double)n2).color(n).endVertex();
            bufferBuilder.vertex(d6 + 16.0, d5, d7 + (double)n2).color(1.0f, 1.0f, 0.0f, 0.0f).endVertex();
        }
        for (n2 = this.minecraft.level.getMinBuildHeight(); n2 <= this.minecraft.level.getMaxBuildHeight(); n2 += 2) {
            double d8 = (double)n2 - d2;
            int n3 = n2 % 8 == 0 ? CELL_BORDER : YELLOW;
            bufferBuilder.vertex(d6, d8, d7).color(1.0f, 1.0f, 0.0f, 0.0f).endVertex();
            bufferBuilder.vertex(d6, d8, d7).color(n3).endVertex();
            bufferBuilder.vertex(d6, d8, d7 + 16.0).color(n3).endVertex();
            bufferBuilder.vertex(d6 + 16.0, d8, d7 + 16.0).color(n3).endVertex();
            bufferBuilder.vertex(d6 + 16.0, d8, d7).color(n3).endVertex();
            bufferBuilder.vertex(d6, d8, d7).color(n3).endVertex();
            bufferBuilder.vertex(d6, d8, d7).color(1.0f, 1.0f, 0.0f, 0.0f).endVertex();
        }
        tesselator.end();
        RenderSystem.lineWidth(2.0f);
        bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        for (n2 = 0; n2 <= 16; n2 += 16) {
            for (int i = 0; i <= 16; i += 16) {
                bufferBuilder.vertex(d6 + (double)n2, d4, d7 + (double)i).color(0.25f, 0.25f, 1.0f, 0.0f).endVertex();
                bufferBuilder.vertex(d6 + (double)n2, d4, d7 + (double)i).color(0.25f, 0.25f, 1.0f, 1.0f).endVertex();
                bufferBuilder.vertex(d6 + (double)n2, d5, d7 + (double)i).color(0.25f, 0.25f, 1.0f, 1.0f).endVertex();
                bufferBuilder.vertex(d6 + (double)n2, d5, d7 + (double)i).color(0.25f, 0.25f, 1.0f, 0.0f).endVertex();
            }
        }
        for (n2 = this.minecraft.level.getMinBuildHeight(); n2 <= this.minecraft.level.getMaxBuildHeight(); n2 += 16) {
            double d9 = (double)n2 - d2;
            bufferBuilder.vertex(d6, d9, d7).color(0.25f, 0.25f, 1.0f, 0.0f).endVertex();
            bufferBuilder.vertex(d6, d9, d7).color(0.25f, 0.25f, 1.0f, 1.0f).endVertex();
            bufferBuilder.vertex(d6, d9, d7 + 16.0).color(0.25f, 0.25f, 1.0f, 1.0f).endVertex();
            bufferBuilder.vertex(d6 + 16.0, d9, d7 + 16.0).color(0.25f, 0.25f, 1.0f, 1.0f).endVertex();
            bufferBuilder.vertex(d6 + 16.0, d9, d7).color(0.25f, 0.25f, 1.0f, 1.0f).endVertex();
            bufferBuilder.vertex(d6, d9, d7).color(0.25f, 0.25f, 1.0f, 1.0f).endVertex();
            bufferBuilder.vertex(d6, d9, d7).color(0.25f, 0.25f, 1.0f, 0.0f).endVertex();
        }
        tesselator.end();
        RenderSystem.lineWidth(1.0f);
        RenderSystem.enableBlend();
        RenderSystem.enableTexture();
    }
}

