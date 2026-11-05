/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.ShulkerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.ShulkerRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Shulker;

public class ShulkerHeadLayer
extends RenderLayer<Shulker, ShulkerModel<Shulker>> {
    public ShulkerHeadLayer(RenderLayerParent<Shulker, ShulkerModel<Shulker>> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, Shulker shulker, float f, float f2, float f3, float f4, float f5, float f6) {
        ResourceLocation resourceLocation = ShulkerRenderer.getTextureLocation(shulker.getColor());
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entitySolid(resourceLocation));
        ((ShulkerModel)this.getParentModel()).getHead().render(poseStack, vertexConsumer, n, LivingEntityRenderer.getOverlayCoords(shulker, 0.0f));
    }
}

