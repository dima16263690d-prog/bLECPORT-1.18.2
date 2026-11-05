/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StructureMode;

public class StructureBlockRenderer
implements BlockEntityRenderer<StructureBlockEntity> {
    public StructureBlockRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(StructureBlockEntity structureBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int n, int n2) {
        double d;
        double d2;
        double d3;
        double d4;
        if (!Minecraft.getInstance().player.canUseGameMasterBlocks() && !Minecraft.getInstance().player.isSpectator()) {
            return;
        }
        BlockPos blockPos = structureBlockEntity.getStructurePos();
        Vec3i vec3i = structureBlockEntity.getStructureSize();
        if (vec3i.getX() < 1 || vec3i.getY() < 1 || vec3i.getZ() < 1) {
            return;
        }
        if (structureBlockEntity.getMode() != StructureMode.SAVE && structureBlockEntity.getMode() != StructureMode.LOAD) {
            return;
        }
        double d5 = blockPos.getX();
        double d6 = blockPos.getZ();
        double d7 = blockPos.getY();
        double d8 = d7 + (double)vec3i.getY();
        double d9 = switch (structureBlockEntity.getMirror()) {
            case Mirror.LEFT_RIGHT -> {
                d4 = vec3i.getX();
                yield -vec3i.getZ();
            }
            case Mirror.FRONT_BACK -> {
                d4 = -vec3i.getX();
                yield vec3i.getZ();
            }
            default -> {
                d4 = vec3i.getX();
                yield vec3i.getZ();
            }
        };
        double d10 = switch (structureBlockEntity.getRotation()) {
            case Rotation.CLOCKWISE_90 -> {
                d3 = d9 < 0.0 ? d5 : d5 + 1.0;
                d2 = d4 < 0.0 ? d6 + 1.0 : d6;
                d = d3 - d9;
                yield d2 + d4;
            }
            case Rotation.CLOCKWISE_180 -> {
                d3 = d4 < 0.0 ? d5 : d5 + 1.0;
                d2 = d9 < 0.0 ? d6 : d6 + 1.0;
                d = d3 - d4;
                yield d2 - d9;
            }
            case Rotation.COUNTERCLOCKWISE_90 -> {
                d3 = d9 < 0.0 ? d5 + 1.0 : d5;
                d2 = d4 < 0.0 ? d6 : d6 + 1.0;
                d = d3 + d9;
                yield d2 - d4;
            }
            default -> {
                d3 = d4 < 0.0 ? d5 + 1.0 : d5;
                d2 = d9 < 0.0 ? d6 + 1.0 : d6;
                d = d3 + d4;
                yield d2 + d9;
            }
        };
        float f2 = 1.0f;
        float f3 = 0.9f;
        float f4 = 0.5f;
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.lines());
        if (structureBlockEntity.getMode() == StructureMode.SAVE || structureBlockEntity.getShowBoundingBox()) {
            LevelRenderer.renderLineBox(poseStack, vertexConsumer, d3, d7, d2, d, d8, d10, 0.9f, 0.9f, 0.9f, 1.0f, 0.5f, 0.5f, 0.5f);
        }
        if (structureBlockEntity.getMode() == StructureMode.SAVE && structureBlockEntity.getShowAir()) {
            this.renderInvisibleBlocks(structureBlockEntity, vertexConsumer, blockPos, poseStack);
        }
    }

    private void renderInvisibleBlocks(StructureBlockEntity structureBlockEntity, VertexConsumer vertexConsumer, BlockPos blockPos, PoseStack poseStack) {
        Level level = structureBlockEntity.getLevel();
        BlockPos blockPos2 = structureBlockEntity.getBlockPos();
        BlockPos blockPos3 = blockPos2.offset(blockPos);
        for (BlockPos blockPos4 : BlockPos.betweenClosed(blockPos3, blockPos3.offset(structureBlockEntity.getStructureSize()).offset(-1, -1, -1))) {
            boolean bl;
            BlockState blockState = level.getBlockState(blockPos4);
            boolean bl2 = blockState.isAir();
            boolean bl3 = blockState.is(Blocks.STRUCTURE_VOID);
            boolean bl4 = blockState.is(Blocks.BARRIER);
            boolean bl5 = blockState.is(Blocks.LIGHT);
            boolean bl6 = bl = bl3 || bl4 || bl5;
            if (!bl2 && !bl) continue;
            float f = bl2 ? 0.05f : 0.0f;
            double d = (float)(blockPos4.getX() - blockPos2.getX()) + 0.45f - f;
            double d2 = (float)(blockPos4.getY() - blockPos2.getY()) + 0.45f - f;
            double d3 = (float)(blockPos4.getZ() - blockPos2.getZ()) + 0.45f - f;
            double d4 = (float)(blockPos4.getX() - blockPos2.getX()) + 0.55f + f;
            double d5 = (float)(blockPos4.getY() - blockPos2.getY()) + 0.55f + f;
            double d6 = (float)(blockPos4.getZ() - blockPos2.getZ()) + 0.55f + f;
            if (bl2) {
                LevelRenderer.renderLineBox(poseStack, vertexConsumer, d, d2, d3, d4, d5, d6, 0.5f, 0.5f, 1.0f, 1.0f, 0.5f, 0.5f, 1.0f);
                continue;
            }
            if (bl3) {
                LevelRenderer.renderLineBox(poseStack, vertexConsumer, d, d2, d3, d4, d5, d6, 1.0f, 0.75f, 0.75f, 1.0f, 1.0f, 0.75f, 0.75f);
                continue;
            }
            if (bl4) {
                LevelRenderer.renderLineBox(poseStack, vertexConsumer, d, d2, d3, d4, d5, d6, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f);
                continue;
            }
            if (!bl5) continue;
            LevelRenderer.renderLineBox(poseStack, vertexConsumer, d, d2, d3, d4, d5, d6, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f);
        }
    }

    @Override
    public boolean shouldRenderOffScreen(StructureBlockEntity structureBlockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 96;
    }
}

