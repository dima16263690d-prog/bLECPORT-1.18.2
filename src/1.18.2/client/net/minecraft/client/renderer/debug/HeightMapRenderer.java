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
import com.mojang.math.Vector3f;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;

public class HeightMapRenderer
implements DebugRenderer.SimpleDebugRenderer {
    private final Minecraft minecraft;
    private static final int CHUNK_DIST = 2;
    private static final float BOX_HEIGHT = 0.09375f;

    public HeightMapRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double d2, double d3) {
        ClientLevel clientLevel = this.minecraft.level;
        RenderSystem.disableBlend();
        RenderSystem.disableTexture();
        RenderSystem.enableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BlockPos blockPos = new BlockPos(d, 0.0, d3);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        for (int i = -2; i <= 2; ++i) {
            for (int j = -2; j <= 2; ++j) {
                ChunkAccess chunkAccess = clientLevel.getChunk(blockPos.offset(i * 16, 0, j * 16));
                for (Map.Entry<Heightmap.Types, Heightmap> entry : chunkAccess.getHeightmaps()) {
                    Heightmap.Types types = entry.getKey();
                    ChunkPos chunkPos = chunkAccess.getPos();
                    Vector3f vector3f = this.getColor(types);
                    for (int k = 0; k < 16; ++k) {
                        for (int i2 = 0; i2 < 16; ++i2) {
                            int n = SectionPos.sectionToBlockCoord(chunkPos.x, k);
                            int n2 = SectionPos.sectionToBlockCoord(chunkPos.z, i2);
                            float f = (float)((double)((float)clientLevel.getHeight(types, n, n2) + (float)types.ordinal() * 0.09375f) - d2);
                            LevelRenderer.addChainedFilledBoxVertices(bufferBuilder, (double)((float)n + 0.25f) - d, f, (double)((float)n2 + 0.25f) - d3, (double)((float)n + 0.75f) - d, f + 0.09375f, (double)((float)n2 + 0.75f) - d3, vector3f.x(), vector3f.y(), vector3f.z(), 1.0f);
                        }
                    }
                }
            }
        }
        tesselator.end();
        RenderSystem.enableTexture();
    }

    private Vector3f getColor(Heightmap.Types types) {
        switch (types) {
            case WORLD_SURFACE_WG: {
                return new Vector3f(1.0f, 1.0f, 0.0f);
            }
            case OCEAN_FLOOR_WG: {
                return new Vector3f(1.0f, 0.0f, 1.0f);
            }
            case WORLD_SURFACE: {
                return new Vector3f(0.0f, 0.7f, 0.0f);
            }
            case OCEAN_FLOOR: {
                return new Vector3f(0.0f, 0.0f, 0.5f);
            }
            case MOTION_BLOCKING: {
                return new Vector3f(0.0f, 0.3f, 0.3f);
            }
            case MOTION_BLOCKING_NO_LEAVES: {
                return new Vector3f(0.0f, 0.5f, 0.5f);
            }
        }
        return new Vector3f(0.0f, 0.0f, 0.0f);
    }
}

