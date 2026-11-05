/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class LecternRenderer
implements BlockEntityRenderer<LecternBlockEntity> {
    private final BookModel bookModel;

    public LecternRenderer(BlockEntityRendererProvider.Context context) {
        this.bookModel = new BookModel(context.bakeLayer(ModelLayers.BOOK));
    }

    @Override
    public void render(LecternBlockEntity lecternBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int n, int n2) {
        BlockState blockState = lecternBlockEntity.getBlockState();
        if (!blockState.getValue(LecternBlock.HAS_BOOK).booleanValue()) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.5, 1.0625, 0.5);
        float f2 = blockState.getValue(LecternBlock.FACING).getClockWise().toYRot();
        poseStack.mulPose(Vector3f.YP.rotationDegrees(-f2));
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(67.5f));
        poseStack.translate(0.0, -0.125, 0.0);
        this.bookModel.setupAnim(0.0f, 0.1f, 0.9f, 1.2f);
        VertexConsumer vertexConsumer = EnchantTableRenderer.BOOK_LOCATION.buffer(multiBufferSource, RenderType::entitySolid);
        this.bookModel.render(poseStack, vertexConsumer, n, n2, 1.0f, 1.0f, 1.0f, 1.0f);
        poseStack.popPose();
    }
}

